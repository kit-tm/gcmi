package com.github.sherter.jcon.examples.generic_attributes.bundles.configurable;

import com.github.sherter.jcon.examples.generic_attributes.field_selection.ReallocatableFieldSelector;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TagType;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.*;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Fixed Selector, always using the same order and fields.
 */
public class COSelector extends ReallocatableFieldSelector {
    private static final Logger logger = LoggerFactory.getLogger(COSelector.class);

    private HashMap<Short, FieldTag> fieldMappings = new HashMap<>();

    public COSelector() {
        super();
        initializeMappings();
    }


    private void initializeMappings() {
        fieldMappings.put((short) 0, new MacTag(MatchFields.ETH_SRC));
        fieldMappings.put((short) 1, new MacTag(MatchFields.ETH_DST));
        fieldMappings.put((short) 2, new IPv4Tag(MatchFields.IPV4_SRC));
        fieldMappings.put((short) 3, new IPv4Tag(MatchFields.IPV4_DST));
        fieldMappings.put((short) 4, new IPv6Tag(MatchFields.IPV6_SRC));
        fieldMappings.put((short) 5, new IPv6Tag(MatchFields.IPV6_DST));
        fieldMappings.put((short) 6, new DSCPTag());
    }

    @Override
    protected BigInteger getTaggingLength(TagType type) {
        return new BigInteger("2").pow(usedFields.iterator().next().getBitLength());
    }

    @Override
    protected void initializeFieldTags() {
        usedFields.add(new MacTag(MatchFields.ETH_DST));
        freeFields.add(new MacTag(MatchFields.ETH_SRC));
    }

    /**
     * Adding fields for tagging use.
     *
     * @param fieldToAdd
     */
    public void addField(Short fieldToAdd, Set<MatchFields> usedFields) {
        FieldTag toAdd = fieldMappings.get(fieldToAdd);

        if (!usedFields.contains(toAdd) && !usedFields.contains(toAdd.getField())) {
            freeFields.add(toAdd);
        }


    }

    /**
     * Returns a list of fields that have to be reallocated.
     *
     * @param fieldToRemove
     * @return
     */
    public MatchFields getFieldToRemove(Short fieldToRemove) {
        MatchFields matchField = null;

        FieldTag toRemove = fieldMappings.get(fieldToRemove);

        if (freeFields.contains(toRemove) || usedFields.contains(toRemove)) {
            matchField = toRemove.getField();
        }

        return matchField;

    }

    /**
     * Only using one field. Then, the field with the maximum number of bits is chosen.
     *
     * @return
     */
    @Override
    public List<FieldTag> getSelection() {
        if (usedFields.isEmpty()) {
            FieldTag currentMax = null;

            for (FieldTag fieldTag : freeFields) {
                if (currentMax == null || fieldTag.getBitLength() > currentMax.getBitLength()) {
                    currentMax = fieldTag;
                }
            }
            usedFields.add(currentMax);
        }

        ArrayList<FieldTag> field = new ArrayList<>();
        field.add(usedFields.iterator().next());

        return field;
    }
}
