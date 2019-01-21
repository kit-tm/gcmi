package com.github.sherter.jcon.examples.generic_attributes.bundles.automatic;

import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.TaggingException;
import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.conflict_policies.ConflictPolicyEffect;
import com.github.sherter.jcon.examples.generic_attributes.conflict_policies.ConflictPolicyEnforcer;
import com.github.sherter.jcon.examples.generic_attributes.conflict_policies.policies.DisableTagging;
import com.github.sherter.jcon.examples.generic_attributes.conflict_policies.policies.TagReallocation;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.types.DatapathId;

import java.util.Set;

/**
 * Default ConflictPolicyEnforcer for the Dynamic Bundle.
 * Uses reallocation first, then resorts to disabling tagging.
 */
public class AUPolicyEnforcer extends ConflictPolicyEnforcer {
    private DisableTagging disableTagging;
    private TagReallocation tagReallocation;

    public AUPolicyEnforcer(TaggingBundle taggingBundle) {
        super(taggingBundle);
        disableTagging = new DisableTagging(taggingBundle);
        tagReallocation = new TagReallocation(taggingBundle);

    }

    @Override
    protected ConflictPolicyEffect applyPolicies(OFFlowMod flowMod, DatapathId datapathId, Set<OFOxm> conflicting) {
        boolean disableTaggingFlag = false;

        // Attempt to reallocate.
        try {
            tagReallocation.applyPolicy(flowMod, datapathId, conflicting);

            // Reallocation failed -> Disable Tagging.
        } catch (TaggingException e) {
            disableTagging.applyPolicy(flowMod, datapathId, conflicting);
            disableTaggingFlag = true;
        }

        // Disable Tagging -> Send conflicting message to the switch.
        return (disableTaggingFlag) ? ConflictPolicyEffect.SEND_DIRECT : ConflictPolicyEffect.SEND_TRANSFORM;
    }

}
