package com.github.sherter.jcon.examples.generic_attributes.conflict_policies.policies;

import com.github.sherter.jcon.InterceptableForwarder;
import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.topology.TopologyManager;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFTagError;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.protocol.ver13.OFFactoryVer13;
import org.projectfloodlight.openflow.types.DatapathId;

import java.util.Set;

/**
 * ConflictPolicy to trigger the disconnection of tagging. Tagging Manager implements the behavior.
 */
public class DisableTagging extends ConflictPolicy {
    private static OFFactoryVer13 factory = (OFFactoryVer13) OFFactories.getFactory(OFVersion.OF_13);

    public DisableTagging(TaggingBundle taggingBundle) {
        super(taggingBundle);
    }

    @Override
    public void applyPolicy(OFFlowMod flowMod, DatapathId datapathId, Set<OFOxm> conflicting) {
        applyPolicy();
    }

    public void applyPolicy() {
        taggingBundle.getTaggingManager().disableTagging();

        // Error code 0 -> whole tagging is disabled
        OFTagError errorMessage = factory.buildTagError().setErrorCode((short) 0).build();

        Set<InterceptableForwarder.Context> contexts = TopologyManager.getInstance().getContexts();

        // notifying all participating apps that tagging was disabled
        if (contexts != null) {
            for (InterceptableForwarder.Context context : contexts) {
                context.sendUpstream(errorMessage);
            }
        }
    }
}
