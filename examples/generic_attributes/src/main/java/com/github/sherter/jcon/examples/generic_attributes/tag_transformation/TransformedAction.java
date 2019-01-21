package com.github.sherter.jcon.examples.generic_attributes.tag_transformation;

import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import java.util.ArrayList;
import java.util.List;

/**
 * Container class for transformed actions.
 */
public class TransformedAction {
    public ArrayList<MatchFields> usedFields;
    public List<OFAction> actions;

    public TransformedAction(ArrayList<MatchFields> usedFields, List<OFAction> actions) {
        this.usedFields = usedFields;
        this.actions = actions;
    }
}
