package com.github.sherter.jcon.examples.learningswitch;

import com.github.sherter.jcon.BufferingConsumer;
import com.github.sherter.jcon.InjectingConsumer;
import com.github.sherter.jcon.OFMessageChunker;
import com.github.sherter.jcon.OFMessageParsingConsumer;
import com.github.sherter.jcon.OFMessageSerializingConsumer;
import com.github.sherter.jcon.networking.Handler;
import com.github.sherter.jcon.networking.Reactor;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stages: (from ryu)
 *
 * <p>ryu.controller.handler.HANDSHAKE_DISPATCHER Sending and waiting for hello message
 *
 * <p>ryu.controller.handler.CONFIG_DISPATCHER Version negotiated and sent features-request message
 *
 * <p>ryu.controller.handler.MAIN_DISPATCHER Switch-features message received and sent set-config
 * message
 *
 * <p>ryu.controller.handler.DEAD_DISPATCHER Disconnect from the peer. Or disconnecting due to some
 * unrecoverable errors.
 *
 * <p>The intermediate layer app is only allowed to act while the MAIN_DISPATCHER stage is active
 */
class SdnAppFramework {

  private static final Logger log = LoggerFactory.getLogger(SdnAppFramework.class);
  private final InetSocketAddress upstreamAddress;
  private final Reactor reactor;
  private final SimpleSwitch app;
  private final Map<Handler, ConnectionContext> contexts = new HashMap<>();

  SdnAppFramework(InetSocketAddress upstreamAddress, Reactor reactor, SimpleSwitch app) {
    this.upstreamAddress = upstreamAddress;
    this.reactor = reactor;
    this.app = app;
  }

  /**
   * When a switch connects to this controller, a new Handler is created for managing the
   * connection. Since we operate in non-blocking mode, this Handler needs to know which methods to
   * call when, for example, new data arrived.
   *
   * <p>The returned function is responsible for creating these callbacks. The Handler is passed as
   * an argument to the function and the callbacks are returned (which may include references to the
   * Handler itself).
   *
   * <p>Two callbacks are required. The first one is called when new data is available. The new data
   * is passed as a byte array to the callback method. The second callback is called when the
   * connection is broken.
   */
  Function<Handler, Handler.Callbacks> callbackFactoryForNewSwitchConnections() {
    return switchHandler -> {
      ConnectionContext context = new ConnectionContext(switchHandler);
      contexts.put(switchHandler, context);
      return new Handler.Callbacks(
          new BufferingConsumer(
              new OFMessageParsingConsumer(
                  new InjectingConsumer<>(this::receivedFromDownstream, context)),
              OFMessageChunker.INSTANCE),
          throwable -> this.switchDisconnected(throwable, context));
    };
  }

  void acceptConnectionFromSwitch(Handler switchHandler) {
    try {
      log.info("switch connected from {}", switchHandler.remoteAddress());
      log.info("establishing new connection with controller");
      reactor.establish(upstreamAddress, callbackFactoryForNewControllerConnections(switchHandler));
    } catch (IOException e) {
      log.error("couldn't establish a connection with controller", e);
      switchHandler.close();
    }
  }

  private Function<Handler, Handler.Callbacks> callbackFactoryForNewControllerConnections(
      Handler switchHandler) {
    return controllerHandler -> {
      ConnectionContext context = contexts.get(switchHandler);
      context.setUpstreamHandler(controllerHandler);
      return new Handler.Callbacks(
          new BufferingConsumer(
              new OFMessageParsingConsumer(
                  new InjectingConsumer<>(this::receivedFromUpstream, context)),
              OFMessageChunker.INSTANCE),
          throwable -> controllerDisconnected(throwable, context));
    };
  }

  private void receivedFromDownstream(OFMessage message, ConnectionContext context) {
    log.info("↑ | {}", message);
    if (context.xids.isReplyForUs(message.getXid())) {
      log.info("forwarding exclusively to app, not to upstream");
      // TODO: is it save to forget the mapping here? Or is it possible, for example, to retrieve
      //       two error messages for the same request?
      context.xids.forgetOurId(message.getXid());

      // don't forward the message to upstream, is only interesting for us
      // TODO: process it
      app.messageHandler(message, context);
      return;
    }

    // set original xid (if necessary)
    if (context.xids.isReplacement(message.getXid())) {
      // is answer to a request from upstream, for which we needed to change the xid because this xid was already in use by us

      // TODO: is it save to forget the mapping here? Or is it possible, for example, to retrieve
      //       two error messages for the same request?
      long originalXid = context.xids.removeReplacement(message.getXid());

      message = message.createBuilder().setXid(originalXid).build();
    }
    // forward the message upstream and process it ourselves
    context.upstreamSender().accept(message);

    if (message.getType() == OFType.FEATURES_REPLY) {
      context.currentStage = Stage.MAIN;
      context.datapathId = ((OFFeaturesReply) message).getDatapathId();
    }
    // TODO here: pass message to "App", together with context and information about the current stage
    app.messageHandler(message, context);
  }

  private void receivedFromUpstream(OFMessage message, ConnectionContext context) {
    // upstream messages are simply forwarded downstream, the app doesn't interfere here
    log.info("↓ | {}", message);
    if (context.xids.needsReplacement(message.getXid())) {
      long replacement = context.xids.createReplacement(message.getXid());
      message = message.createBuilder().setXid(replacement).build();
    }
    if (message.getType() == OFType.FEATURES_REQUEST) {
      context.currentStage = Stage.CONFIG;
      context.messageFactory = OFFactories.getFactory(message.getVersion());
    }
    context.downstreamSender().accept(message);
  }

  private void switchDisconnected(Throwable cause, ConnectionContext context) {
    log.info(
        "switch connection broke (remote address: {}); cause: {}",
        context.downstreamHandler.remoteAddress(),
        cause == null ? "disconnected" : cause.getMessage());
    contexts.remove(context.downstreamHandler());
    context.upstreamHandler().close();
  }

  private void controllerDisconnected(Throwable cause, ConnectionContext context) {
    SocketAddress localAddress = context.upstreamHandler.localAddress();
    log.info(
        "controller connection broke (local address: {}); cause: {}",
        localAddress == null ? "not connected yet" : localAddress,
        cause == null ? "disconnected" : cause.getMessage());
    context.downstreamHandler().close();
  }

  static class ConnectionContext {
    private final Handler downstreamHandler;
    private final Consumer<OFMessage> downstreamSender;
    private Handler upstreamHandler;
    private Consumer<OFMessage> upstreamSender;
    DatapathId datapathId;
    XidManager xids = new XidManager();
    Stage currentStage = Stage.HANDSHAKE;
    OFFactory messageFactory;

    ConnectionContext(Handler downstream) {
      this.downstreamHandler = downstream;
      this.downstreamSender = new OFMessageSerializingConsumer(bytes -> downstream.send(bytes, null));
    }

    Handler downstreamHandler() {
      return downstreamHandler;
    }

    Consumer<OFMessage> downstreamSender() {
      return downstreamSender;
    }

    void setUpstreamHandler(Handler upstream) {
      this.upstreamHandler = upstream;
      this.upstreamSender = new OFMessageSerializingConsumer(bytes -> upstream.send(bytes, null));
    }

    Handler upstreamHandler() {
      return upstreamHandler;
    }

    Consumer<OFMessage> upstreamSender() {
      return upstreamSender;
    }
  }

  enum Stage {
    HANDSHAKE,
    CONFIG,
    MAIN,
    DEAD
  }
}
