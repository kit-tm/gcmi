package com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields;

import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.Tag;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.oxm.*;
import org.projectfloodlight.openflow.types.MacAddress;

/**
 * FieldTag class hiding Mac address specific operations.
 */
public class MacTag extends FieldTag {

    public MacTag(MatchFields type, double probability) {
        super(type, probability);
        checkType();

    }

    public MacTag(MatchFields type) {
        super(type);
        checkType();

    }

    private void checkType() throws IllegalArgumentException {
        if (!(fieldType.equals(MatchFields.ETH_DST) || fieldType.equals(MatchFields.ETH_SRC))) {
            throw new IllegalArgumentException("Wrong type was delivered for MacTag object.");
        }
    }

    @Override
    public int getBitLength() {
        return 48;
    }

    /**
     * Returning a Mac based field given a tag with or without mask.
     * @param tag
     * @return
     */
    @Override
    protected OFOxm getField(Tag tag) {
        if (fieldType.equals(MatchFields.ETH_DST)) {
            OFOxm oxm = null;

            if (tag != null) {
                if (tag.hasMask) {
                    OFOxmEthDstMasked.Builder builder = factory.oxms().buildEthDstMasked();
                    oxm = builder.setValue(MacAddress.of(tag.tag.longValue())).setMask(MacAddress.of(tag.mask.longValue())).build();
                } else {
                    OFOxmEthDst.Builder builder = factory.oxms().buildEthDst();
                    oxm = builder.setValue(MacAddress.of(tag.tag.longValue())).build();
                }

            }

            return oxm;
        } else if (fieldType.equals(MatchFields.ETH_SRC)) {
            OFOxm oxm = null;

            if (tag != null) {
                if (tag.hasMask) {
                    OFOxmEthSrcMasked.Builder builder = factory.oxms().buildEthSrcMasked();
                    oxm = builder.setValue(MacAddress.of(tag.tag.longValue())).setMask(MacAddress.of(tag.mask.longValue())).build();
                } else {
                    OFOxmEthSrc.Builder builder = factory.oxms().buildEthSrc();
                    oxm = builder.setValue(MacAddress.of(tag.tag.longValue())).build();
                }

            }

            return oxm;
        }
        return null;

    }


}

