package com.github.sherter.jcon.examples.generic_attributes.bundles.multi_field;

import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.Tag;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TagTransformation;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.FieldTag;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.FlowModType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Representing the encoding for a multi-field tag implementation.
 */
public class MFTransformation extends TagTransformation {
    private static final Logger logger = LoggerFactory.getLogger(MFTransformation.class);

    public MFTransformation(TaggingBundle bundle) {
        super(bundle);
        fieldSelector = new MFSelector();

    }


    /**
     * Incorporating multi-field tags.
     * @param availableFields
     * @param tag
     * @param flowModType
     * @return
     */
    @Override
    protected List<List<Tag>> getFieldEncoding(List<FieldTag> availableFields, Tag tag, FlowModType flowModType) {
        List<Tag> encodings = new ArrayList<>();
        int beginOfTag = 0;

        for (FieldTag fieldTag : availableFields) {
            int lengthOfTag = fieldTag.getBitLength();

            Tag tagForField = transformTag(tag, beginOfTag, lengthOfTag);
            if(tagForField != null)
                logger.info("Begin " + beginOfTag + " tag " + tagForField.tag);
            encodings.add(tagForField);

            beginOfTag += lengthOfTag;
        }

        List<List<Tag>> list = new ArrayList<>();
        list.add(encodings);

        return list;
    }

    /**
     * Completely transforms a tag by only focusing on a subset of the bits.
     * @param tag
     * @param beginOfTag
     * @param lengthOfTag
     * @return
     */
    private static Tag transformTag(Tag tag, int beginOfTag, int lengthOfTag) {
        BigInteger value = transformInteger(tag.tag, beginOfTag, lengthOfTag);

        if (tag.hasMask) {
            BigInteger mask = transformInteger(tag.mask, beginOfTag, lengthOfTag);

            if (mask.equals(ZERO)) {
                return null;
            } else {
                return new Tag(value, mask);
            }
        } else {
            if (value.equals(ZERO)) {
                return null;
            } else {
                return new Tag(value);
            }

        }

    }

    /**
     * Transforms a Big Integer by only regarding a subset of its bits.
     * @param valueToTransform
     * @param beginOfTag
     * @param lengthOfTag
     * @return
     */
    private static BigInteger transformInteger(BigInteger valueToTransform, int beginOfTag, int lengthOfTag) {
        BigInteger value = valueToTransform.shiftRight(beginOfTag);

        // mask = 2^lengthOfTag - 1
        BigInteger mask = TWO;
        mask = mask.pow(lengthOfTag);
        mask = mask.add(MINUS_ONE);

        value = value.and(mask);

        return value;

    }



}
