package com.github.sherter.jcon.examples.generic_attributes.bundles.multi_field;

import com.github.sherter.jcon.examples.generic_attributes.field_selection.FieldSelector;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TagType;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.FieldTag;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.IPv4Tag;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.MacTag;
import org.projectfloodlight.openflow.protocol.match.MatchFields;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Fixed Selector, always using the same order and fields.
 */
public class MFSelector extends FieldSelector {
    List<FieldTag> fields = new ArrayList<>();

    public MFSelector() {
        super();
    }

    @Override
    protected BigInteger getTaggingLength(TagType type) {
        int totalLength = 0;
        for (FieldTag fieldTag: usedFields) {
            totalLength += fieldTag.getBitLength();

        }

        return new BigInteger("2").pow(totalLength);
    }

    @Override
    protected void initializeFieldTags() {
        usedFields.add(new MacTag(MatchFields.ETH_DST, 0.0));
        usedFields.add(new MacTag(MatchFields.ETH_SRC, 0.0));
        usedFields.add(new IPv4Tag(MatchFields.IPV4_DST, 0.0));

    }

    @Override
    public List<FieldTag> getSelection() {
        if(fields.isEmpty()) {
            fields.addAll(usedFields);
        }
        return fields;
    }
}
