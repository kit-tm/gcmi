package com.github.sherter.jcon.examples.generic_attributes.tag_transformation;

import org.projectfloodlight.openflow.protocol.OFOxmList;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import java.util.ArrayList;

/**
 * Container class for transformed matches.
 */
public class TransformedMatch {
    public ArrayList<MatchFields> usedFields;
    public OFOxmList matches;
    public Match match;

    public TransformedMatch(ArrayList<MatchFields> usedFields, OFOxmList matches) {
        this.usedFields = usedFields;
        this.matches = matches;
    }

    public TransformedMatch(Match match) {
        this.match = match;
    }
}
