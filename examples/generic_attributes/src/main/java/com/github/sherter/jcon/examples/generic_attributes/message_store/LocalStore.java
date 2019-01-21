package com.github.sherter.jcon.examples.generic_attributes.message_store;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Local Store to keep tag related messages on the TIL to reduce latency.
 */
public class LocalStore extends FlowRuleStore {
    private static final Logger logger = LoggerFactory.getLogger(LocalStore.class);

    private HashMap<MatchFields, ArrayList<OFFlowMod>> matchFieldMap = new HashMap<>();
    private HashMap<OFFlowMod, FlowRuleStoreEntry> flowModMap = new HashMap<>();

    private Set<MatchFields> usedFields = new HashSet<>();

    public Set<MatchFields> getUsedFields () {
        return usedFields;
    }

    @Override
    public List<DatapathId> getSwitchIds() {
        ArrayList<DatapathId> switchIds = new ArrayList<>();

        for (FlowRuleStoreEntry entry : flowModMap.values()) {
            if (!switchIds.contains(entry.datapathId))
                switchIds.add(entry.datapathId);
        }

        return switchIds;
    }

    /**
     * Adding new FlowMod message as it contains tags.
     * @param usedIn
     * @param flowMod
     * @param match
     * @param datapathId
     */
    @Override
    public void reportNewMessage(List<MatchFields> usedIn, OFFlowMod flowMod, Match match, DatapathId datapathId) {
        for (MatchFields matchField : usedIn) {
            matchFieldMap.computeIfAbsent(matchField, f -> matchFieldMap.put(f, new ArrayList<OFFlowMod>()));

            matchFieldMap.get(matchField).add(flowMod);

            if (flowModMap.get(flowMod) != null) {
                flowModMap.get(flowMod).addMatches(match, matchField);
            } else {
                flowModMap.put(flowMod, new FlowRuleStoreEntry(match, datapathId, matchField));
            }

        }
    }

    @Override
    public HashMap<OFFlowMod, FlowRuleStoreEntry> getFlowModMap() {
        return flowModMap;
    }

    /**
     * Deletion of a FlowMod message was instructed. Keeping TIL and switches consistent.
     * @param match
     * @return
     */
    @Override
    public boolean reportDeleteMessage(Match match) {
        OFFlowMod toDelete = null;

        for (OFFlowMod flowMod : flowModMap.keySet()) {
            if (flowModMap.get(flowMod).matches(match)) {
                toDelete = flowMod;
                break;
            }
        }

        if (toDelete != null) {
            final OFFlowMod deletionMod = toDelete;
            FlowRuleStoreEntry info = flowModMap.get(toDelete);

            for (MatchFields matchField : info.matchFields) {
                matchFieldMap.get(matchField).removeIf(mod -> mod.equals(deletionMod));
            }


            flowModMap.remove(toDelete);
            return true;
        } else
            return false;
    }

    @Override
    public void fieldsUsed(Set<OFOxm> fields) {
        for (OFOxm ofOxm : fields) {
            usedFields.add(ofOxm.getMatchField().id);
        }
    }

    /**
     * In case of tagging being disabled.
     */
    @Override
    public void invalidateRules() {
        matchFieldMap.clear();
        flowModMap.clear();
    }


    /**
     * Conflict resolution requires the retrieval of FlowMod messages that contain tags.
     * Will be removed as they are inserted again using different header fields.
     * @param matchField
     * @return
     */
    @Override
    public HashMap<OFFlowMod, FlowRuleStoreEntry> conflictResolutionRequested(MatchFields matchField) {
        HashMap<OFFlowMod, FlowRuleStoreEntry> toReassign = new HashMap<>();

        HashMap<MatchFields, ArrayList<OFFlowMod>> newMessages = new HashMap<>();
        ArrayList<OFFlowMod> list = matchFieldMap.get(matchField);

        if (list == null)
            return toReassign;

        for (OFFlowMod flowMod : list) {
            FlowRuleStoreEntry flowRuleStoreEntry = flowModMap.get(flowMod);
            toReassign.put(flowMod, flowRuleStoreEntry);

            for (MatchFields field : flowRuleStoreEntry.matchFields) {
                for (OFFlowMod toKeep : matchFieldMap.get(field)) {
                    if (!toKeep.equals(flowMod)) {
                        newMessages.computeIfAbsent(field, f -> newMessages.put(f, new ArrayList<OFFlowMod>()));
                        newMessages.get(field).add(toKeep);
                    }
                }
            }

            flowModMap.remove(flowMod);

        }

        // conflicting FlowMods were removed
        matchFieldMap = newMessages;

        return toReassign;

    }

}
