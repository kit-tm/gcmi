package com.github.sherter.jcon.composer;

import com.github.sherter.jcon.InterceptableForwarder;
import com.github.sherter.jcon.InterceptableForwarder.Context;
import com.github.sherter.jcon.networking.Reactor;
import com.github.sherter.jcon.networking.Handler;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.util.List;
import java.lang.Math;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.instruction.*;
import org.projectfloodlight.openflow.protocol.action.*;
import org.projectfloodlight.openflow.protocol.match.*;
import org.projectfloodlight.openflow.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThrottleLayer implements Layer {
  private static final int BUCKET_FILL_INTERVAL_MS = 10;
  private static final int BUCKET_SIZE = 100;

  private static final Logger log = LoggerFactory.getLogger(ThrottleLayer.class);
  private static final OFFactory factory = OFFactories.getFactory(OFVersion.OF_13);

  private final LayerService service;
  private final Reactor reactor;
  private final RoutingProxy proxy;
  private final InterceptableForwarder forwarder;
  private final InetSocketAddress listenAddress;

  private final Thread reactorThread;
  private final Thread bucketThread;

  Map<Context, Integer> buckets;

  public ThrottleLayer(LayerService service) throws IOException {
    this.service = service;
    reactor = new Reactor(Selector.open());
    proxy = RoutingProxy.listenOn(reactor, null);

    forwarder = new InterceptableForwarder.Builder(reactor, proxy.listenAddress())
        .interceptDownstream(OFFlowStatsRequest.class, this::statsRequest).build();
    listenAddress = forwarder.listenOn(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));

    reactorThread = new Thread(() -> reactor.loop());
    bucketThread = new Thread(() -> fillBuckets());

    buckets = new HashMap<Context, Integer>();

    reactorThread.start();
    bucketThread.start();
  }

  private void fillBuckets() {
    try {
      while (true) {
        Thread.sleep(BUCKET_FILL_INTERVAL_MS);
        synchronized (this) {
          for (Map.Entry<Context, Integer> entry : buckets.entrySet()) {
            entry.setValue(Math.min(entry.getValue() + 1, BUCKET_SIZE));
          }
        }
      }
    } catch (InterruptedException e) {
      // Do nothing
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  private synchronized void statsRequest(OFFlowStatsRequest msg, InterceptableForwarder.Context context) {
    int bucket = buckets.getOrDefault(context, BUCKET_SIZE);
    if (bucket == 0)
      return;
    buckets.put(context, bucket - 1);
    context.sendDownstream(msg);
  }

  @Override
  public synchronized void destroy() {
    reactorThread.interrupt();
    bucketThread.interrupt();
    try {
      reactorThread.join();
      bucketThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    reactor.close();
    service.remove(this);
  }

  @Override
  public InetSocketAddress listenAddress() {
    return listenAddress;
  }

  @Override
  public void connectTo(List<InetSocketAddress> upstreamAddresses) {
    if (upstreamAddresses.size() > 1) {
      throw new RuntimeException("only supports one upstream controller");
    }
    if (upstreamAddresses.size() == 0) {
      proxy.connectTo(null);
    } else {
      proxy.connectTo(upstreamAddresses.get(0));
    }
  }

  @Override
  public String type() {
    return "throttle";
  }
}
