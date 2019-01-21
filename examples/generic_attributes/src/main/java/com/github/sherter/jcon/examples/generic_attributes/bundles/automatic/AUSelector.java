package com.github.sherter.jcon.examples.generic_attributes.bundles.automatic;

import com.github.sherter.jcon.examples.generic_attributes.field_selection.ReallocatableFieldSelector;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TagType;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.DSCPTag;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.FieldTag;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.IPv4Tag;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.MacTag;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Optimized Selection conflict_policies.
 */
public class AUSelector extends ReallocatableFieldSelector {
    private static final Logger logger = LoggerFactory.getLogger(AUSelector.class);
    private FieldTag maskedTag;

    public AUSelector() {
    }

    public FieldTag getMaskedFieldTag() {
        return maskedTag;
    }

    @Override
    public BigInteger getTaggingLength(TagType type) {
        if (type.equals(TagType.FLAT)) {
            BigInteger size = new BigInteger("0");

            for (FieldTag fieldTag : freeFields) {
                if (!fieldTag.equals(maskedTag)) {
                    size = size.add(new BigInteger("2").pow(fieldTag.getBitLength()));
                }

            }

            for (FieldTag fieldTag : usedFields) {
                if (!fieldTag.equals(maskedTag)) {
                    size = size.add(new BigInteger("2").pow(fieldTag.getBitLength()));
                }

            }

            return size;
        } else {
            if (maskedTag != null) {
                int bitLength = maskedTag.getBitLength();
                return new BigInteger("2").pow(bitLength);
            } else {
                return new BigInteger("0");
            }
        }
    }

    @Override
    public Set<FieldTag> excludeOxmFields(Set<OFOxm> excludeForReconfiguration) {
        Set<FieldTag> exclude = super.excludeOxmFields(excludeForReconfiguration);

        if (exclude.contains(maskedTag))
            maskedTag = null;

        return exclude;
    }

    @Override
    public Set<FieldTag> excludeFields(List<MatchFields> fields) {
        Set<FieldTag> exclude = super.excludeFields(fields);

        if (exclude.contains(maskedTag))
            maskedTag = null;

        return exclude;
    }

    @Override
    protected void initializeFieldTags() {
        maskedTag = new MacTag(MatchFields.ETH_DST, 0.0);
        freeFields.add(maskedTag);
        freeFields.add(new DSCPTag(0.15));
        freeFields.add(new IPv4Tag(MatchFields.IPV4_DST, 0.2));
        freeFields.add(new MacTag(MatchFields.ETH_SRC, 0.6));
        freeFields.add(new IPv4Tag(MatchFields.IPV4_SRC, 0.7));
    }



    /**
     * Mapping function of the equation from Section 4.6.3.
     * @param value
     * @return
     */
    private static double mapFraction(double value) {
        return value;
    }

    /**
     * Equation from Section 4.6.3.
     * @param fieldTag
     * @param maxBitLength
     * @return
     */
    private static double computeFieldTagScore(FieldTag fieldTag, int maxBitLength) {
        double value = fieldTag.getProbability();
        long assignedTags = fieldTag.getNumberOfTags();

        value += mapFraction((double) assignedTags / Math.pow(2, maxBitLength));

        return value;
    }

    private static int compareFieldTags(FieldTag f1, FieldTag f2, int maxBitLength) {
        double f1Score = computeFieldTagScore(f1, maxBitLength);
        double f2Score = computeFieldTagScore(f2, maxBitLength);

        int f1Int = (int) (f1Score * 1000000);
        int f2Int = (int) (f2Score * 1000000);

        return f1Int - f2Int;
    }

    /**
     * Sorting based on the Equation from Section 4.6.3.
     * Multiple tags might be used at the same time, starting from the first item in the list.
     * @return
     */
    @Override
    public List<FieldTag> getSelection() {
        ArrayList<FieldTag> fieldTags = new ArrayList<>();

        fieldTags.addAll(usedFields);
        fieldTags.addAll(freeFields);

        int maxBitLength = fieldTags.stream().mapToInt(FieldTag::getBitLength).max()
                .orElseThrow(NoSuchElementException::new);

        fieldTags.sort((f1, f2) -> compareFieldTags(f1, f2, maxBitLength));

        return fieldTags;
    }





}
