package com.github.sherter.jcon.examples.generic_attributes.bundles.configurable;

import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.Tag;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TagTransformation;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.FieldTag;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.FlowModType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Transformation of a configurable tag assignment scheme.
 */
public class COTransformation extends TagTransformation {
    private static final Logger logger = LoggerFactory.getLogger(COTransformation.class);

    public COTransformation(TaggingBundle bundle) {
        super(bundle);
        fieldSelector = new COSelector();

    }

    /**
     * Selecting the one field offered, as always the field with the maximum length is chosen.
     * @param availableFields
     * @param tag
     * @param flowModType
     * @return
     */
    @Override
    protected List<List<Tag>> getFieldEncoding(List<FieldTag> availableFields, Tag tag, FlowModType flowModType) {
        List<Tag> encodings = new ArrayList<>();

        encodings.add(tag);

        List<List<Tag>> list = new ArrayList<>();
        list.add(encodings);

        return list;
    }


}
