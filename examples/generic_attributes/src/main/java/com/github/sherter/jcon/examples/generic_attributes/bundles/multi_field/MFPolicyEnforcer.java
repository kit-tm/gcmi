package com.github.sherter.jcon.examples.generic_attributes.bundles.multi_field;

import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.conflict_policies.ConflictPolicyEffect;
import com.github.sherter.jcon.examples.generic_attributes.conflict_policies.ConflictPolicyEnforcer;
import com.github.sherter.jcon.examples.generic_attributes.conflict_policies.policies.DisableTagging;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.types.DatapathId;

import java.util.Set;

/**
 * Applies the shutdown of tagging as soon as a conflict is detected.
 */
public class MFPolicyEnforcer extends ConflictPolicyEnforcer {

    public MFPolicyEnforcer(TaggingBundle taggingBundle) {
        super(taggingBundle);
    }

    @Override
    protected ConflictPolicyEffect applyPolicies(OFFlowMod flowMod, DatapathId datapathId, Set<OFOxm> conflicting) {
        DisableTagging disableTagging = new DisableTagging(taggingBundle);
        disableTagging.applyPolicy(flowMod, datapathId, conflicting);

        return ConflictPolicyEffect.SEND_DIRECT;
    }

}
