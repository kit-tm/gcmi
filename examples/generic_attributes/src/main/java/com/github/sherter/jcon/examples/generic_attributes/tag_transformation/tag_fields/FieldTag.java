package com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields;

import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.InsufficientTagSpaceException;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.Tag;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.FlowModType;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.protocol.ver13.OFFactoryVer13;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashMap;

/**
 * Component hiding header field specific operations.
 */
public abstract class FieldTag {
    protected static OFFactoryVer13 factory = (OFFactoryVer13) OFFactories.getFactory(OFVersion.OF_13);
    private static final Logger logger = LoggerFactory.getLogger(FieldTag.class);
    protected MatchFields fieldType;
    private double probability;
    private HashMap<Tag, Boolean> containsTag = new HashMap<>();
    private long numberOfTags = 0L;

    protected BigInteger comparison;

    public FieldTag(MatchFields field, double probability) {
        fieldType = field;
        this.probability = probability;
        cacheComparison();
    }

    public FieldTag(MatchFields field) {
        fieldType = field;
        cacheComparison();
    }

    private void cacheComparison() {
        comparison = new BigInteger("2");
        comparison = comparison.pow(getBitLength());
    }

    public abstract int getBitLength();

    public double getProbability() { return probability; }

    public long getNumberOfTags() { return numberOfTags; }

    public MatchFields getField() {
        return fieldType;
    }

    protected abstract OFOxm getField(Tag tag);

    private OFAction getAction(Tag tag) {
        return factory.actions().buildSetField().setField(getField(tag)).build();
    }

    /**
     * Checks to ensure header field is long enough and tag should be mapped to the header field.
     * @param tag
     * @param flowModType
     * @return
     * @throws InsufficientTagSpaceException
     */
    private boolean doChecks(Tag tag, FlowModType flowModType) throws InsufficientTagSpaceException {
        if(tag == null) {
            return false;
        } else if (!sizeCheck(tag)) {
            throw new InsufficientTagSpaceException(tag, comparison);
        } else {
            checkForNewTag(tag, flowModType);
        }

        return true;
    }

    /**
     * Checks if delivered tag exceeds header field space
     * @param tag
     * @return
     */
    public boolean sizeCheck(Tag tag) {
        if(comparison.add(tag.tag.negate()).signum() == -1) {
            logger.error("Value cannot be transformed, because it is bigger than header field space available.");
            return false;
        }

        if (tag.hasMask && comparison.add(tag.mask.negate()).signum() == -1) {
            logger.error("Mask cannot be transformed, because it is bigger than header field space available.");
            return false;
        }

        return true;
    }

    /**
     *
     * @param value
     * @return
     */
    public boolean sizeCheck(BigInteger value) {
        if(comparison.add(value.negate()).signum() == -1) {
            logger.error("Value cannot be transformed, because it is bigger than header field space available.");
            return false;
        }
        return true;
    }

    /**
     * Builds an action by consulting sub-cass method.
     * @param tag
     * @param flowModType
     * @return
     */
    public OFAction buildAction(Tag tag, FlowModType flowModType) throws InsufficientTagSpaceException {
        if(!doChecks(tag, flowModType))
            return null;

        return getAction(tag);

    }

    /**
     * Builds a match by consulting sub-cass method.
     * @param tag
     * @param flowModType
     * @return
     */
    public OFOxm buildMatch(Tag tag, FlowModType flowModType) throws InsufficientTagSpaceException {
        if(!doChecks(tag, flowModType))
            return null;

        return getField(tag);
    }

    /**
     * Monitoring assigned number of tags. Used for example by the optimized selection conflict_policies.
     * @param tag
     * @param flowModType
     */
    private void checkForNewTag(Tag tag, FlowModType flowModType) {
        if (containsTag.get(tag) != null) {
            if (flowModType.equals(FlowModType.DELETE)) {
                containsTag.remove(tag);
                numberOfTags = (numberOfTags > 0) ? numberOfTags - 1 : 0;

            }
        } else {
            if (!flowModType.equals(FlowModType.DELETE)) {
                containsTag.put(tag, true);
                numberOfTags++;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FieldTag) {
            return ((FieldTag) o).getField().equals(fieldType);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return fieldType.hashCode();
    }
}

