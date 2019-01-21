package com.github.sherter.jcon.examples.generic_attributes.interception;

import com.github.sherter.jcon.InterceptableForwarder;
import com.github.sherter.jcon.XidManager;
import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.TaggingException;
import com.github.sherter.jcon.examples.generic_attributes.interception.eval.BarrierMeasurer;
import com.github.sherter.jcon.examples.generic_attributes.interception.eval.ConflictEvaluationWriter;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.TILCallback;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.TaggingManager;
import org.projectfloodlight.openflow.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Exemplary MessageInterceptor that solely serves as a provider for tagging related messages.
 */
public class MessageInterceptor implements TILCallback {
    private TaggingManager taggingManager;
    private final XidManager xidManager = new XidManager();
    private static final Logger logger = LoggerFactory.getLogger(MessageInterceptor.class);
    private boolean taggingEnabled = true;

    // TODO Change bundle if needed.
    private String bundleName = "Configurable";

    public MessageInterceptor() {
        try {
            taggingManager = new TaggingManager(xidManager, this, bundleName);
            BarrierMeasurer.getInstance().setXidManager(xidManager);
        } catch (TaggingException e) {
            logger.error("Tagging cannot be started and will be disabled.");
            taggingEnabled = false;
        }


    }

    public void setTaggingState(boolean enable) {
        if (enable && !taggingEnabled) {
            try {
                taggingManager = new TaggingManager(xidManager, this, bundleName);
            } catch (TaggingException e) {
                return;
            }
        }

        taggingEnabled = enable;
    }

    /**
     * Received FlowMod message that has to be redirected to the Tagging Manager.
     *
     * @param msg
     * @param context
     */
    public void flowModReceived(OFFlowMod msg, InterceptableForwarder.Context context) {
        ConflictEvaluationWriter.getInstance().newRuleReceived();


        if (taggingEnabled) {
            List<OFMessage> messages = taggingManager.redirectFlowMod(msg, context);

            if (messages != null && !messages.isEmpty()) {
                for (OFMessage message : messages) {
                    context.sendDownstream(message);
                }
            }

        } else {
            context.sendDownstream(msg);
        }


    }

    /**
     * Experimenter message used for tag configuration.
     *
     * @param msg
     * @param context
     */
    public void experimenterReceived(OFExperimenter msg, InterceptableForwarder.Context context) {
        logger.info("Received Experimenter Message.");

        // Unknown Experimenters will be sent to the corresponding switch.
        if (!taggingManager.redirectExperimenter(msg, context)) {
            logger.info("Unknown Experimenter detected. Sending it Downstream.");
            context.sendDownstream(msg);
        }
    }

    private long time = 0;

    /**
     * Used for BarrierMeasurer.
     *
     * @param msg
     * @param context
     */
    public void barrierReplyReceived(OFBarrierReply msg, InterceptableForwarder.Context context) {
        logger.info("Received Barrier reply.");
        boolean sentByTil = BarrierMeasurer.getInstance().getBarrier().responseReceived(msg.getXid());

        // If not sent by the BarrierMeasurer, it was sent by a controller application.
        // -> sending message further to the controller
        if (!sentByTil) {
            long diff = System.currentTimeMillis() - time;
            logger.info("Sent app: " + diff);

            logger.info("Barrier request was not sent by TIL -> Send it to Controller.");
            context.sendUpstream(msg);
            //ConflictEvaluationWriter.getInstance().writeResults();
        } else {
            logger.info("Sent til");
            time = System.currentTimeMillis();

        }
    }


    public void controllerHelloReceived(OFHello msg, InterceptableForwarder.Context context) {
        logger.info("Received Controller Hello Message.");
        try {
            context.sendDownstream(msg);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }

    public void featuresReplyReceived(OFFeaturesReply msg, InterceptableForwarder.Context context) {
        taggingManager.redirectSwitchFeatures(msg, context);
        context.sendUpstream(msg);
    }


    public void switchHelloReceived(OFHello msg, InterceptableForwarder.Context context) {
        try {
            context.sendUpstream(msg);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void echoRequestReceived(OFEchoRequest msg, InterceptableForwarder.Context context) {
        context.sendDownstream(msg);
    }


    public void switchPortStatusReceived(OFPortStatus msg, InterceptableForwarder.Context context) {
        context.sendUpstream(msg);
    }

    public void switchStatsReplyReceived(OFStatsReply msg, InterceptableForwarder.Context context) {
        context.sendUpstream(msg);
    }

    public void packetInReceived(OFPacketIn msg, InterceptableForwarder.Context context) {
        context.sendUpstream(msg);
    }


    public void barrierRequestReceived(OFBarrierRequest msg, InterceptableForwarder.Context context) {
        logger.info("Received Barrier request.");
        context.sendDownstream(msg);
    }

    public void errorMsgReceived(OFErrorMsg msg, InterceptableForwarder.Context context) {
        context.sendUpstream(msg);
    }

    public void featuresRequestReceived(OFFeaturesRequest msg, InterceptableForwarder.Context context) {
        context.sendDownstream(msg);
    }

    public void statsRequestReceived(OFStatsRequest msg, InterceptableForwarder.Context context) {
        context.sendDownstream(msg);
    }


    public void packetOutReceived(OFPacketOut msg, InterceptableForwarder.Context context) {
        logger.info("Received PacketOut Message.");
        context.sendDownstream(msg);
    }


}
