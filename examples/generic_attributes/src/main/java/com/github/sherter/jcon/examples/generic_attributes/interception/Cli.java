package com.github.sherter.jcon.examples.generic_attributes.interception;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.github.sherter.jcon.InterceptableForwarder;
import com.github.sherter.jcon.networking.Reactor;
import org.projectfloodlight.openflow.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Starting the TIL from command line interface.
 */
class Cli {
  private static final Logger log = LoggerFactory.getLogger(Cli.class);

  public static void main(String[] args) throws IOException {
    Args parsedArgs = new Args();
    JCommander.newBuilder().addObject(parsedArgs).build().parse(args);

    ch.qos.logback.classic.Logger logger =
            (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    // TODO Turn off logger for evaluations to warrant reproducible results.
    //logger.setLevel(Level.OFF);


    Reactor reactor = new Reactor(Selector.open());
    MessageInterceptor genericAttributeService = new MessageInterceptor();

    // Intercepts given messages.
    InterceptableForwarder forwarder = new InterceptableForwarder.Builder(reactor, parsedArgs.upstreamAddress)
            .interceptUpstream(OFEchoRequest.class, genericAttributeService::echoRequestReceived)
            .interceptUpstream(OFFeaturesReply.class, genericAttributeService::featuresReplyReceived)
            .interceptUpstream(OFPortStatus.class, genericAttributeService::switchPortStatusReceived)
            .interceptUpstream(OFStatsReply.class, genericAttributeService::switchStatsReplyReceived)
            .interceptUpstream(OFPacketIn.class, genericAttributeService::packetInReceived)
            .interceptUpstream(OFBarrierReply.class, genericAttributeService::barrierReplyReceived)
            .interceptUpstream(OFErrorMsg.class, genericAttributeService::errorMsgReceived)
            .interceptDownstream(OFEchoRequest.class, genericAttributeService::echoRequestReceived)
            .interceptDownstream(OFFeaturesRequest.class, genericAttributeService::featuresRequestReceived)
            .interceptDownstream(OFStatsRequest.class, genericAttributeService::statsRequestReceived)
            .interceptDownstream(OFFlowMod.class, genericAttributeService::flowModReceived)
            .interceptDownstream(OFPacketOut.class, genericAttributeService::packetOutReceived)
            .interceptDownstream(OFBarrierRequest.class, genericAttributeService::barrierRequestReceived)
            .interceptDownstream(OFErrorMsg.class, genericAttributeService::errorMsgReceived)
            .interceptDownstream(OFExperimenter.class, genericAttributeService::experimenterReceived)
            .build();

    log.info(
            "Listening on {} and forwarding to {} ...",
            parsedArgs.listenAddress,
            parsedArgs.upstreamAddress);

    forwarder.listenOn(parsedArgs.listenAddress);
    reactor.loop();
  }

  static class Args {
    @Parameter(
      names = {"-l", "--listen"},
      converter = InetSocketAddressConverter.class,
      required = true,
      description = "format: 'host:port'; opens TCP server socket expecting OpenFlow packets"
    )
    InetSocketAddress listenAddress;

    @Parameter(
      names = {"-u", "--upstream"},
      converter = InetSocketAddressConverter.class,
      description = "format: 'host:port'; connection parameters for upper layer"
    )
    InetSocketAddress upstreamAddress;
  }

  static class InetSocketAddressConverter implements IStringConverter<InetSocketAddress> {
    @Override
    public InetSocketAddress convert(String value) {
      String[] splits = value.split(":");
      checkArgument(splits.length == 2);
      return new InetSocketAddress(splits[0], Integer.parseInt(splits[1]));
    }
  }
}
