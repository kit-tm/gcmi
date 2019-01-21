package com.github.sherter.jcon.examples.generic_attributes.bundles;

import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.TaggingException;
import com.github.sherter.jcon.examples.generic_attributes.configuration.BundleConfiguration;
import com.github.sherter.jcon.examples.generic_attributes.conflict_policies.ConflictPolicyEnforcer;
import com.github.sherter.jcon.examples.generic_attributes.message_store.FlowRuleStore;
import com.github.sherter.jcon.examples.generic_attributes.optimizations.TaggingOptimizer;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TagTransformation;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TransformedAction;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TransformedMatch;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.FlowModType;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.TaggingManager;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.message_builder.RequirementsDetermination;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.message_builder.TaggingMessageBuilder;
import org.projectfloodlight.openflow.protocol.OFFlowDelete;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.types.DatapathId;

import java.util.ArrayList;
import java.util.List;

/**
 * Bundles the implementation of various components into a complete functioning set.
 * Other Bundles than the AUBundle might be created.
 */
public class TaggingBundle {
    protected TaggingManager taggingManager;
    protected ConflictPolicyEnforcer conflictPolicyEnforcer;
    protected TagTransformation tagTransformation;
    protected FlowRuleStore flowRuleStore;
    protected TaggingOptimizer taggingOptimizer;
    protected BundleConfiguration bundleConfiguration;

    public TaggingBundle(TaggingManager manager) {
        this.taggingManager = manager;
    }

    public FlowRuleStore getFlowRuleStore() {
        return flowRuleStore;
    }

    public ConflictPolicyEnforcer getConflictPolicyEnforcer() {
        return conflictPolicyEnforcer;
    }

    public TaggingManager getTaggingManager() {
        return taggingManager;
    }

    public TagTransformation getTagTransformation() {
        return tagTransformation;
    }

    public BundleConfiguration getBundleConfiguration() {
        return bundleConfiguration;
    }


    public void invalidationRequested() {
        tagTransformation.invalidateAll();
    }

    /**
     * FlowMod message is transformed and may be stored in FlowRuleStore.
     * @param flowMod
     * @param datapathId
     * @param flowModType
     * @return
     * @throws TaggingException
     */
    public List<OFMessage> getTransformedMessage(OFFlowMod flowMod, DatapathId datapathId, FlowModType flowModType)
            throws TaggingException {
        ArrayList<OFMessage> messages = new ArrayList<>();
        if(flowModType.equals(FlowModType.UNKNOWN)) {
            if(flowMod instanceof OFFlowDelete)
                flowModType = FlowModType.DELETE;
            else
                flowModType = FlowModType.ADD;
        }
        RequirementsDetermination requirements = new RequirementsDetermination();
        requirements.requiredInOptimization(taggingOptimizer, datapathId);


        List<TransformedAction> transformedActions =
                TaggingMessageBuilder.getTransformedActions(tagTransformation, flowMod.getActions(), requirements, flowModType);
        List<TransformedMatch> transformedMatches =
                TaggingMessageBuilder.getTransformedMatch(tagTransformation, flowMod.getMatch(),
                requirements, flowModType);

        // add dummy element
        if (transformedMatches.isEmpty()) {
            transformedMatches.add(new TransformedMatch(null, null));
        }

        boolean deletion = false;

        for (TransformedMatch match : transformedMatches) {
            ArrayList<MatchFields> fields = match.usedFields;

            for (TransformedAction action : transformedActions) {

                ArrayList<MatchFields> currentFields = (fields != null) ? (ArrayList) fields.clone() : new ArrayList<>();

                if (currentFields == null) {
                    currentFields = new ArrayList<>();
                }

                for (MatchFields actionField : action.usedFields) {
                    if (!currentFields.contains(actionField)) {
                        currentFields.add(actionField);
                    }
                }

                if (flowModType.equals(FlowModType.DELETE) && !deletion) {
                    deletion = flowRuleStore.reportDeleteMessage(match.match);
                } else {
                    if (!currentFields.isEmpty()) {
                        flowRuleStore.reportNewMessage(currentFields, flowMod, match.match, datapathId);
                    }
                }

                List<OFAction> actions = action.actions;

                if (taggingOptimizer != null)
                    actions = taggingOptimizer.optimizeActions(match.match, action.actions, datapathId);

                messages.add(TaggingMessageBuilder.buildFromExistingMsg(flowMod, match.match, actions));
            }
        }

        return messages;
    }

}
