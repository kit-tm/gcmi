package com.github.sherter.jcon.examples.generic_attributes.conflict_policies;

import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TagTransformation;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFMatchV3;
import org.projectfloodlight.openflow.protocol.OFOxmList;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionSetField;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.types.DatapathId;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Enforces policies in case of a conflict. Can be extended to integrate new policies.
 */
public abstract class ConflictPolicyEnforcer {
    protected TaggingBundle taggingBundle;

    public ConflictPolicyEnforcer(TaggingBundle taggingBundle) {
        this.taggingBundle = taggingBundle;
    }

    /**
     * Method to enforce 1 or more policies.
     *
     * @param flowMod
     * @param datapathId
     * @return
     */
    protected abstract ConflictPolicyEffect applyPolicies(OFFlowMod flowMod, DatapathId datapathId, Set<OFOxm> conflicting);

    /**
     * Checking if conflict is occurring.
     * @param flowMod
     * @param datapathId
     * @return
     */
    public ConflictPolicyEffect checkAndApply(OFFlowMod flowMod, DatapathId datapathId) {

        TagTransformation converter = taggingBundle.getTagTransformation();
        Set<OFOxm> conflicting = new HashSet<>();
        Match match = flowMod.getMatch();

        // adding matches
        if (match instanceof OFMatchV3) {
            OFMatchV3 matchV3 = (OFMatchV3) match;
            OFOxmList list = matchV3.getOxmList();

            // Combining fields that are in conflict.
            for (OFOxm field : list) {
                if (converter.fieldInUse(field)) {
                    conflicting.add(field);
                }
            }
        }

        List<OFAction> actions = flowMod.getActions();

        // adding actions
        for (OFAction action: actions) {
            if (action instanceof OFActionSetField) {
                OFActionSetField setField = (OFActionSetField) action;

                if (converter.fieldInUse(setField.getField())) {
                    conflicting.add(setField.getField());
                }
            }
        }

        taggingBundle.getFlowRuleStore().fieldsUsed(conflicting);

        if (conflicting.isEmpty()) {
            return ConflictPolicyEffect.SEND_TRANSFORM;
        } else {
            return applyPolicies(flowMod, datapathId, conflicting);
        }


    }
}
