package com.github.sherter.jcon.examples.generic_attributes.conflict_policies.policies;

import com.github.sherter.jcon.InterceptableForwarder;
import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.IncompatibleComponentsException;
import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.TaggingException;
import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.field_selection.FieldSelector;
import com.github.sherter.jcon.examples.generic_attributes.field_selection.ReallocatableFieldSelector;
import com.github.sherter.jcon.examples.generic_attributes.message_store.FlowRuleStore;
import com.github.sherter.jcon.examples.generic_attributes.message_store.FlowRuleStoreEntry;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.FieldTag;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.FlowModType;
import com.github.sherter.jcon.examples.generic_attributes.topology.TopologyManager;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.protocol.ver13.OFFactoryVer13;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Reallocation based conflict resolution conflict_policies.
 */
public class TagReallocation extends ConflictPolicy {
    private static final Logger logger = LoggerFactory.getLogger(TagReallocation.class);
    private static OFFactoryVer13 factory = (OFFactoryVer13) OFFactories.getFactory(OFVersion.OF_13);
    private int numberOfReallocation = 0;

    public TagReallocation(TaggingBundle taggingBundle) {
        super(taggingBundle);
    }

    /**
     * Collects all conflicting fields and triggers the reallocation step.
     * @param flowMod
     * @param datapathId
     * @throws TaggingException
     */
    @Override
    public void applyPolicy(OFFlowMod flowMod, DatapathId datapathId, Set<OFOxm> conflicting)
            throws TaggingException {

        FieldSelector fieldSelector = taggingBundle.getTagTransformation().getFieldSelector();

        if (!(fieldSelector instanceof ReallocatableFieldSelector))
            throw new IncompatibleComponentsException();

        Set<FieldTag> toExclude = ((ReallocatableFieldSelector) fieldSelector).excludeOxmFields(conflicting);

        reallocate(toExclude);

    }

    /**
     * Reallocating tags to other fields.
     * @param toExclude
     * @throws TaggingException
     */
    protected void reallocate(Set<FieldTag> toExclude) throws TaggingException {
        FlowRuleStore flowRuleStore = taggingBundle.getFlowRuleStore();
        List<ConflictResolutionEntry> resolutionList = new ArrayList<>();


        //BarrierMeasurer.getInstance().startMeasuring("Reallocation time", flowRuleStore.getSwitchIds());
        long startPoint = System.currentTimeMillis();
        // Considering every header field that is currently in conflict with tagging.
        for (FieldTag fieldTag : toExclude) {
            MatchFields field = fieldTag.getField();

            HashMap<OFFlowMod, FlowRuleStoreEntry> messages = flowRuleStore.conflictResolutionRequested(field);
            resolutionList.add(new ConflictResolutionEntry(fieldTag, messages));
        }

        long time = System.currentTimeMillis() - startPoint;
        logger.info("Time to reallocate: " + time + "ms");

        if (resolutionList.size() > 0) {
            logger.warn("Invalidating rules now...");
            installNewRules(resolutionList);


            logger.warn("Waiting transitioning time now...");
             try {
                 // TODO Change if necessary
                Thread.sleep(50);
             } catch (InterruptedException e) {
                e.printStackTrace();
             }

            logger.warn("Deleting old rules now...");
            invalidateOldRules(resolutionList);
        }

        //BarrierMeasurer.getInstance().endMeasuring();
    }

    private void installNewRules(List<ConflictResolutionEntry> resolutionList) throws TaggingException {
        numberOfReallocation++;
        for (ConflictResolutionEntry entry : resolutionList) {
            HashMap<OFFlowMod, FlowRuleStoreEntry> messages = entry.messageEntries;

            // Every message for a given header field.
            for (OFFlowMod flowMod : messages.keySet()) {
                DatapathId switchDatapathId = messages.get(flowMod).datapathId;
                InterceptableForwarder.Context context = TopologyManager.getInstance()
                        .getContext(switchDatapathId);

                flowMod = flowMod.createBuilder().setPriority(flowMod.getPriority() + numberOfReallocation).build();

                // New transformed flow rules for a given FlowMod message.
                for (OFMessage message : taggingBundle.getTransformedMessage(flowMod, switchDatapathId, FlowModType.REASSIGN)) {
                    context.sendDownstream(message);
                }

            }
        }

    }


    private void invalidateOldRules(List<ConflictResolutionEntry> resolutionList) throws TaggingException {
        for (ConflictResolutionEntry entry : resolutionList) {
            HashMap<OFFlowMod, FlowRuleStoreEntry> messages = entry.messageEntries;

            // Every message for a given header field.
            for (OFFlowMod flowMod : messages.keySet()) {
                DatapathId switchDatapathId = messages.get(flowMod).datapathId;
                InterceptableForwarder.Context context = TopologyManager.getInstance()
                        .getContext(switchDatapathId);

                // Invalidation
                for (Match match : messages.get(flowMod).matches) {
                    OFFlowMod deleteMod = factory.buildFlowDeleteStrict().setMatch(match).setPriority(flowMod.getPriority()).build();
                    context.sendDownstream(deleteMod);
                }

            }
        }

    }
}

class ConflictResolutionEntry {
    FieldTag fieldTag;
    HashMap<OFFlowMod, FlowRuleStoreEntry> messageEntries;

    ConflictResolutionEntry(FieldTag fieldTag, HashMap<OFFlowMod, FlowRuleStoreEntry> messageEntries) {
        this.fieldTag = fieldTag;
        this.messageEntries = messageEntries;
    }
}
