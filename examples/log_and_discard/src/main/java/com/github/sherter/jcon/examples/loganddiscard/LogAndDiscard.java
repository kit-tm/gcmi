package com.github.sherter.jcon.examples.loganddiscard;

import com.github.sherter.jcon.networking.Handler;
import com.github.sherter.jcon.networking.Reactor;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LogAndDiscard {

  static final Logger log = LoggerFactory.getLogger(LogAndDiscard.class);

  private static void received(Handler handler, ByteBuffer data) {
    log.info("Received from {}: {}", handler.remoteAddress(), data.toString());
  }

  private static void accepted(Handler handler) {
    log.info("Accepted {}", handler.remoteAddress());
  }

  private static void disconnected(Handler handler, Throwable cause) {
    log.info("Disconnected {}; Cause: {}", handler.remoteAddress(), cause);
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    InetSocketAddress listenAddress = new InetSocketAddress("0.0.0.0", 50000);

    Reactor reactor = new Reactor(Selector.open());
    reactor.listen(
        listenAddress,
        handler ->
            new Handler.Callbacks(
                data -> received(handler, data), throwable -> disconnected(handler, throwable)),
        LogAndDiscard::accepted);

    log.info("Listening on {}. Received data will be logged and then discarded...", listenAddress);
    reactor.loop();
  }
}
