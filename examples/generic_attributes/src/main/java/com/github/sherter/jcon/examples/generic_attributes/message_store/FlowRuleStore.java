package com.github.sherter.jcon.examples.generic_attributes.message_store;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.types.DatapathId;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * FlowRuleStore offers the access of FlowMod messages in case of a conflict.
 * A variant that keeps local messages is implemented.
 */
public abstract class FlowRuleStore {

    public FlowRuleStore() {

    }
    public abstract Set<MatchFields> getUsedFields ();

    public abstract List<DatapathId> getSwitchIds();

    public abstract void fieldsUsed(Set<OFOxm> fields);

    public abstract void invalidateRules();

    public abstract HashMap<OFFlowMod, FlowRuleStoreEntry> getFlowModMap();

    public abstract HashMap<OFFlowMod, FlowRuleStoreEntry> conflictResolutionRequested(MatchFields matchField);

    public abstract void reportNewMessage(List<MatchFields> usedIn, OFFlowMod flowMod, Match match, DatapathId datapathId);

    public abstract boolean reportDeleteMessage(Match match);
}
