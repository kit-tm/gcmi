package com.github.sherter.jcon.examples.generic_attributes.bundles.multi_field;

import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.conflict_policies.ConflictPolicyEffect;
import com.github.sherter.jcon.examples.generic_attributes.conflict_policies.ConflictPolicyEnforcer;
import com.github.sherter.jcon.examples.generic_attributes.conflict_policies.policies.RuleRefusal;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.types.DatapathId;

import java.util.Set;

/**
 * ConflictPolicy Enforcer that refuses rule placement in case of conflict.
 */
public class RefusalPolicyEnforcer extends ConflictPolicyEnforcer {

    public RefusalPolicyEnforcer(TaggingBundle taggingBundle) {
        super(taggingBundle);
    }

    @Override
    protected ConflictPolicyEffect applyPolicies(OFFlowMod flowMod, DatapathId datapathId, Set<OFOxm> conflicting) {
        RuleRefusal ruleRefusal = new RuleRefusal(taggingBundle);
        ruleRefusal.applyPolicy(flowMod, datapathId, conflicting);

        return ConflictPolicyEffect.ABORT;
    }


}
