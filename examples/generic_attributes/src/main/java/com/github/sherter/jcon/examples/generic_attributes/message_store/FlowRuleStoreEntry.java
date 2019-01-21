package com.github.sherter.jcon.examples.generic_attributes.message_store;

import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.types.DatapathId;

import java.util.ArrayList;
import java.util.List;

/**
 * Container class for the FlowRuleStore.
 */
public class FlowRuleStoreEntry {
    public DatapathId datapathId;
    public List<Match> matches = new ArrayList<>();
    public List<MatchFields> matchFields = new ArrayList<>();

    public FlowRuleStoreEntry(Match match, DatapathId datapathId, MatchFields field) {
        matchFields.add(field);
        this.datapathId = datapathId;
        this.matches.add(match);
    }

    public void addMatches(Match match, MatchFields field) {
        matches.add(match);
        matchFields.add(field);

    }

    /**
     * Check if matches are equal. If true, then FlowMod has to be deleted due to a FlowMod Deletion message.
     * @param deletionMatch
     * @return
     */
    public boolean matches(Match deletionMatch) {
        for (Match match : matches) {
            if (match.equals(deletionMatch)) {
                return match.equals(deletionMatch);
            }
        }

        return false;
    }


}
