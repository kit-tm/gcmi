package com.github.sherter.jcon.examples.generic_attributes.field_selection;

import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.FieldTag;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Field Selector offering the exclusion of fields.
 */
public abstract class ReallocatableFieldSelector extends FieldSelector {
    private static final Logger logger = LoggerFactory.getLogger(ReallocatableFieldSelector.class);

    /**
     * Fields are excluded from tagging due to a conflict occuring.
     * @param excludeForReconfiguration
     * @return
     */
    public Set<FieldTag> excludeOxmFields(Set<OFOxm> excludeForReconfiguration) {
        ArrayList<MatchFields> fields = new ArrayList<>();

        for (OFOxm oxm : excludeForReconfiguration) {
            fields.add(oxm.getMatchField().id);
        }

        return excludeFields(fields);
    }

    public Set<FieldTag> excludeFields(List<MatchFields> fields) {
        HashSet<FieldTag> toReconfigure = new LinkedHashSet<>();
        HashSet<FieldTag> newUsed = new LinkedHashSet<>();
        HashSet<FieldTag> newFree = new LinkedHashSet<>();

        for (FieldTag fieldTag : usedFields) {
            if(fields.contains(fieldTag.getField())) {
                toReconfigure.add(fieldTag);
            } else {
                newUsed.add(fieldTag);
            }
        }

        for (FieldTag fieldTag : freeFields) {
            if(!fields.contains(fieldTag.getField())) {
                newFree.add(fieldTag);
            }
        }

        // Only fields that are not affected by the conflict.
        usedFields = newUsed;
        freeFields = newFree;

        return toReconfigure;
    }
}
