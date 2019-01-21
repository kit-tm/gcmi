package com.github.sherter.jcon.examples.generic_attributes.conflict_policies.policies;

import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.TaggingManager;
import com.github.sherter.jcon.examples.generic_attributes.topology.TopologyManager;
import org.projectfloodlight.openflow.protocol.OFErrorMsg;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModFailedCode;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.types.DatapathId;

import java.util.Set;

/**
 * ConflictPolicy to refuse an arriving flow rule. An error message is sent back to the controller application
 * to inform about the refusal.
 */
public class RuleRefusal extends ConflictPolicy {
    public RuleRefusal(TaggingBundle taggingBundle) {
        super(taggingBundle);
    }

    @Override
    public void applyPolicy(OFFlowMod flowMod, DatapathId datapathId, Set<OFOxm> conflicting) {
        OFErrorMsg messge = TaggingManager.MESSAGE_BUILDER.errorMsgs().buildFlowModFailedErrorMsg()
                .setCode(OFFlowModFailedCode.TABLE_FULL).setXid(manager.getXidManager().newOriginXid()).build();

        TopologyManager.getInstance().getContext(datapathId).sendUpstream(messge);

    }
}
