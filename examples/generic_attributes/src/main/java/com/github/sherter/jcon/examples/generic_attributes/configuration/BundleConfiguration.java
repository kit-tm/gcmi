package com.github.sherter.jcon.examples.generic_attributes.configuration;

import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TagType;
import com.github.sherter.jcon.examples.generic_attributes.topology.TopologyManager;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.ver13.OFFactoryVer13;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv6Address;

import java.math.BigInteger;

/**
 * OpenFlow-based Experimenter configuration.
 */
public class BundleConfiguration {
    protected TaggingBundle taggingBundle;
    protected OFFactoryVer13 factory = (OFFactoryVer13) OFFactories.getFactory(OFVersion.OF_13);


    public BundleConfiguration(TaggingBundle bundle) {
        this.taggingBundle = bundle;
    }

    /**
     * May be overwritten by sub-classes to offer further configuration.
     * @param message
     * @param datapathId
     * @return
     */
    protected boolean specificExperimenterReceived(OFExperimenter message, DatapathId datapathId) {
        return false;
    }

    /**
     * Experimenter message is checked if it is a tagging related configuration experimenter.
     * @param message
     * @param datapathId
     * @return
     */
    public boolean receivedExperimenterMessage(OFExperimenter message, DatapathId datapathId) {
        if (message instanceof OFTagHandshakeRequest) {
            OFTagHandshakeRequest configurationRequest = (OFTagHandshakeRequest) message;

            AppsInfo appsInfo = AppsInfo.getInstance();

            AppConfiguration configuration = new AppConfiguration(datapathId, configurationRequest.getEtherType());

            byte[] id = AppsInfo.getInstance().addApp(configuration).toByteArray();
            byte[] alignedId = new byte[16];

            for (int i = 0; i < id.length; i++) {
                alignedId[i] = id[i];
            }

            IPv6Address appId = IPv6Address.of(alignedId);

            short appIdLength = appsInfo.getAppIdLength();

            OFMessage reply = factory.buildTagHandshakeReply().setXid(message.getXid())
                    .setAppId(appId).setAppIdLength(appIdLength).build();

            TopologyManager.getInstance().getContext(datapathId).sendUpstream(reply);

            return true;
        } else if (message instanceof OFTagSpaceRequest) {
            BigInteger flatLength = taggingBundle.getTagTransformation()
                    .getFieldSelector().getAvailableTaggingLength(TagType.FLAT);

            BigInteger maskedLength = taggingBundle.getTagTransformation()
                    .getFieldSelector().getAvailableTaggingLength(TagType.MASKED);

            OFMessage reply = factory.buildTagSpaceReply().setXid(message.getXid())
                    .setFlatTagLength(IPv6Address.of(flatLength.toByteArray()))
                    .setMaskedTagLength(IPv6Address.of(maskedLength.toByteArray())).build();

            TopologyManager.getInstance().getContext(datapathId).sendUpstream(reply);
        }

        return specificExperimenterReceived(message, datapathId);
    }
}
