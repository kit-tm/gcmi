package com.github.sherter.jcon.examples.learningswitch;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import net.floodlightcontroller.packet.Ethernet;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleSwitch {

  private static final Logger log = LoggerFactory.getLogger(SimpleSwitch.class);

  private final Table<DatapathId, MacAddress, OFPort> macToPort = HashBasedTable.create();

  void messageHandler(OFMessage message, SdnAppFramework.ConnectionContext context) {
    switch (message.getType()) {
      case PACKET_IN:
        handlePacketIn((OFPacketIn) message, context);
        break;
      case FEATURES_REPLY:
        installTableMissFlow(context);
      default:
        // don't care

    }
  }

  private void installTableMissFlow(SdnAppFramework.ConnectionContext context) {
    OFActionOutput outputToController =
        context.messageFactory.actions().buildOutput().setPort(OFPort.CONTROLLER).build();
    OFFlowAdd flowAdd =
        context
            .messageFactory
            .buildFlowAdd()
            .setPriority(0)
            .setActions(ImmutableList.of(outputToController))
            .build();
    context.downstreamSender().accept(flowAdd);
  }

  private void handlePacketIn(OFPacketIn message, SdnAppFramework.ConnectionContext context) {
    log.info("handling packet_in: {}", message);
    checkState(context.currentStage == SdnAppFramework.Stage.MAIN);
    byte[] ethernetData = message.getData();
    Ethernet eth = new Ethernet();
    eth.deserialize(ethernetData, 0, ethernetData.length);

    if (eth.getEtherType().equals(EthType.LLDP)) {
      return;
    }
    OFPort inPort = message.getMatch().get(MatchField.IN_PORT);

    macToPort.put(context.datapathId, eth.getSourceMACAddress(), inPort);

    OFPort outPort;
    if (macToPort.contains(context.datapathId, eth.getDestinationMACAddress())) {
      outPort = macToPort.get(context.datapathId, eth.getDestinationMACAddress());
    } else {
      outPort = OFPort.FLOOD;
    }

    OFActionOutput action = context.messageFactory.actions().buildOutput().setPort(outPort).build();
    if (!outPort.equals(OFPort.FLOOD)) {
      addFlow(context, inPort, eth.getDestinationMACAddress(), action);
    }

    OFPacketOut.Builder packetOutBuilder = context.messageFactory.buildPacketOut();
    if (message.getBufferId().equals(OFBufferId.NO_BUFFER)) {
      packetOutBuilder.setData(message.getData());
    }
    OFPacketOut packetOut =
        packetOutBuilder
            .setInPort(inPort)
            .setActions(ImmutableList.of(action))
            .setBufferId(message.getBufferId())
            .setXid(context.xids.createForUs())
            .build();
    context.downstreamSender().accept(packetOut);
  }

  private void addFlow(
      SdnAppFramework.ConnectionContext context,
      OFPort inPort,
      MacAddress dstMac,
      OFActionOutput action) {
    Match match =
        context
            .messageFactory
            .buildMatch()
            .setExact(MatchField.ETH_DST, dstMac)
            .setExact(MatchField.IN_PORT, inPort)
            .build();
    OFFlowAdd flowAdd =
        context
            .messageFactory
            .buildFlowAdd()
            .setMatch(match)
            .setActions(ImmutableList.of(action))
            .setXid(context.xids.createForUs())
            .setPriority(1)
            .build();
    log.info("adding flow: {}", flowAdd);
    context.downstreamSender().accept(flowAdd);
  }
}
