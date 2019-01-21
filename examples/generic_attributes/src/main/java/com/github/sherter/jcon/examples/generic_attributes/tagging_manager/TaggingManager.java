package com.github.sherter.jcon.examples.generic_attributes.tagging_manager;

import com.github.sherter.jcon.InterceptableForwarder;
import com.github.sherter.jcon.XidManager;
import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.TaggingException;
import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.UnavailableFieldException;
import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.conflict_policies.ConflictPolicyEffect;
import com.github.sherter.jcon.examples.generic_attributes.conflict_policies.policies.DisableTagging;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TagType;
import com.github.sherter.jcon.examples.generic_attributes.topology.TopologyManager;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.ver13.OFFactoryVer13;
import org.projectfloodlight.openflow.types.DatapathId;

import java.util.ArrayList;
import java.util.List;

/**
 * Central component of the approach. Realizes and hides tagging to TIL components outside.
 */
public class TaggingManager {
    private TILCallback tilCallback;
    public static OFFactoryVer13 MESSAGE_BUILDER = (OFFactoryVer13) OFFactories.getFactory(OFVersion.OF_13);
    private static OFFactoryVer13 factory = (OFFactoryVer13) OFFactories.getFactory(OFVersion.OF_13);

    private XidManager xidManager;
    private TaggingBundle taggingBundle;

    public TaggingManager(XidManager xidManager, TILCallback tilCallback, String selectedBundleName)
            throws TaggingException {

        this.xidManager = xidManager;
        this.tilCallback = tilCallback;
        taggingBundle = BundleSelector.instantiateBundle(this, selectedBundleName);


    }

    public XidManager getXidManager() {
        return xidManager;
    }

    public void disableTagging() {
        tilCallback.setTaggingState(false);
        taggingBundle.invalidationRequested();

    }

    /**
     * Registering new Switch and storing DatapathId in the Topology Manager.
     * @param msg
     * @param context
     */
    public void redirectSwitchFeatures(OFFeaturesReply msg, InterceptableForwarder.Context context) {
        DatapathId datapathId = msg.getDatapathId();

        TopologyManager.getInstance().updateSwitchIds(datapathId, context);

    }

    /**
     * Redirected Experimenter messages for potential tagging configuration.
     * @param experimenter
     * @param context
     * @return
     */
    public boolean redirectExperimenter(OFExperimenter experimenter, InterceptableForwarder.Context context) {
        DatapathId datapathId = TopologyManager.getInstance().getDataPathId(context);

        return taggingBundle.getBundleConfiguration().receivedExperimenterMessage(experimenter, datapathId);
    }


    /**
     * Method to return a transformed flow mod message. Other messages might be sent due to exceptions.
     * @param msg
     * @param context
     * @return
     */
    public synchronized List<OFMessage> redirectFlowMod(OFFlowMod msg, InterceptableForwarder.Context context) {

        TopologyManager topologyManager = TopologyManager.getInstance();
        ConflictPolicyEffect effect = taggingBundle.getConflictPolicyEnforcer().checkAndApply(msg, topologyManager.getDataPathId(context));

        switch (effect) {
            case ABORT: return null;
            case SEND_DIRECT:
                ArrayList<OFMessage> returnMessage = new ArrayList<>();
                returnMessage.add(msg);
                return returnMessage;
            case SEND_TRANSFORM:
                break;
        }

        try {
            return taggingBundle.getTransformedMessage(msg, topologyManager.getDataPathId(context), FlowModType.UNKNOWN);
        } catch(TaggingException e) {
            if (e instanceof UnavailableFieldException) {
                // Error code 0 -> whole tagging is disabled
                // Error code 1 -> Flat tagging is not available
                // Error code 2 -> Masked tagging is not available
                short errorCode = (short)((((UnavailableFieldException) e).tagType == TagType.FLAT) ? 1 : 2);
                OFTagError errorMessage = factory.buildTagError().setErrorCode(errorCode).build();

                context.sendUpstream(errorMessage);
            } else {
                new DisableTagging(taggingBundle).applyPolicy();
            }

            return null;
        }

    }


}
