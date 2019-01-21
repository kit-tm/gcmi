package com.github.sherter.jcon.examples.generic_attributes.bundles.automatic;

import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.InsufficientTagSpaceException;
import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.TaggingException;
import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.UnavailableFieldException;
import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.Tag;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TagTransformation;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TagType;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.FieldTag;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.FlowModType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;

/**
 * Dynamic Tag Transformation implementing Differentiated tags encoding.
 */
public class AUTransformation extends TagTransformation {
    private static final Logger logger = LoggerFactory.getLogger(AUTransformation.class);


    private Set<FieldTag> usedForFlat = new HashSet<>();
    private HashMap<Tag, MappingPair> tagMapping = new HashMap<>();
    private HashMap<FieldTag, BigInteger> currentMax = new HashMap<>();

    public AUTransformation(TaggingBundle bundle) {
        super(bundle);

        fieldSelector = new AUSelector();

    }

    /**
     * Differentiated Tags. Assigning every tag to a new value and retrieving it if it was part of an assignment before.
     *
     * @param availableFields
     * @param tag
     * @param flowModType
     * @return
     * @throws TaggingException
     */
    @Override
    protected List<List<Tag>> getFieldEncoding(List<FieldTag> availableFields, Tag tag, FlowModType flowModType) throws TaggingException {
        List<Tag> encodings = Arrays.asList(new Tag[availableFields.size()]);
        boolean foundField = false;

        // tag contains a mask -> use dedicated mask field
        if (tag.hasMask) {
            maskedTagRoutine(availableFields, tag, encodings);
            foundField = true;

            // tag does not contain a mask -> choose another field
        } else {
            MappingPair pair = tagMapping.get(tag.tag);

            // tag was never assigned -> create new mapping
            if (pair == null) {
                int idx = createNewFlatMapping(availableFields, tag);

                logger.info("To assign: Tag is: " + tag.tag + " and assigned to: " + tagMapping.get(tag).assigned.tag);

                encodings.set(idx, tagMapping.get(tag).assigned);

                if (idx >= 0)
                    foundField = true;

                // tag was already assigned...
            } else {
                // ... and can be used or should be deleted.
                if (flowModType.equals(FlowModType.ADD)
                        || flowModType.equals(FlowModType.DELETE)) {

                    logger.info("Assigned: Tag is: " + tag.tag + " and assigned to: " + tagMapping.get(tag).assigned.tag);

                    foundField = findIndexAndSet(availableFields, pair, encodings);

                    // but should be reassigned.
                } else if (flowModType.equals(FlowModType.REASSIGN)) {
                    int idx = createNewFlatMapping(availableFields, tag);
                    encodings.set(idx, tagMapping.get(tag).assigned);

                    if (idx >= 0)
                        foundField = true;
                }

            }
        }

        if (!foundField)
            throw new InsufficientTagSpaceException(tag);

        List<List<Tag>> list = new ArrayList<>();
        list.add(encodings);

        return list;
    }

    private boolean findIndexAndSet(List<FieldTag> availableFields, MappingPair pair, List<Tag> encodings) {
        boolean foundField = false;
        for (int i = 0; i < availableFields.size(); i++) {
            if (availableFields.get(i).equals(pair.fieldTag)) {
                encodings.set(i, pair.assigned);
                foundField = true;
                break;
            }
        }

        return foundField;
    }

    private int createNewFlatMapping(List<FieldTag> availableFields, Tag tag) {
        int idx = -1;
        FieldTag forMasking = ((AUSelector) fieldSelector).getMaskedFieldTag();

        for (int i = 0; i < availableFields.size(); i++) {
            FieldTag fieldTag = availableFields.get(i);
            if (!fieldTag.equals(forMasking)) {
                BigInteger currentMaxValue = currentMax.get(fieldTag);

                if (currentMaxValue == null) {
                    currentMaxValue = new BigInteger("0");
                } else {
                    currentMaxValue = currentMaxValue.add(new BigInteger("1"));
                }

                currentMax.put(fieldTag, currentMaxValue);

                if (fieldTag.sizeCheck(currentMaxValue)) {
                    MappingPair mappingPair = new MappingPair(new Tag(currentMaxValue), fieldTag);
                    tagMapping.put(tag, mappingPair);
                    usedForFlat.add(fieldTag);
                    idx = i;
                    break;
                }

            }
        }

        return idx;
    }

    /**
     * Handling the encoding of a masked tag.
     *
     * @param availableFields
     * @param tag
     * @param encodings
     */
    private void maskedTagRoutine(List<FieldTag> availableFields, Tag tag, List<Tag> encodings)
            throws UnavailableFieldException {
        FieldTag forMasking = ((AUSelector) fieldSelector).getMaskedFieldTag();

        if (forMasking != null) {
            for (int i = 0; i < availableFields.size(); i++) {
                if (forMasking.equals(availableFields.get(i))) {
                    encodings.set(i, tag);
                    break;
                }
            }
        } else {
            throw new UnavailableFieldException(TagType.MASKED);
        }

    }

}

class MappingPair {
    Tag assigned;
    FieldTag fieldTag;

    MappingPair(Tag assigned, FieldTag fieldTag) {
        this.assigned = assigned;
        this.fieldTag = fieldTag;
    }

}
