package com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields;

import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.Tag;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.oxm.*;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FieldTag class hiding Mac address specific operations.
 */
public class IPv4Tag extends FieldTag {
    private static final Logger logger = LoggerFactory.getLogger(IPv4Tag.class);

    public IPv4Tag(MatchFields type, double probability) {
        super(type, probability);
        checkType();

    }

    public IPv4Tag(MatchFields type) {
        super(type);
        checkType();
    }

    private void checkType() throws IllegalArgumentException {
        if (!(fieldType.equals(MatchFields.IPV4_DST) || fieldType.equals(MatchFields.IPV4_SRC))) {
            throw new IllegalArgumentException("Wrong type was delivered for MacTag object.");
        }
    }


    @Override
    public int getBitLength() {
        return 32;
    }

    /**
     * Returning an IPv4 based field given a tag with or without mask.
     *
     * @param tag
     * @return
     */
    @Override
    protected OFOxm getField(Tag tag) {
        if (fieldType.equals(MatchFields.IPV4_DST)) {
            OFOxm oxm = null;

            if (tag != null) {
                if (tag.hasMask) {
                    OFOxmIpv4DstMasked.Builder builder = factory.oxms().buildIpv4DstMasked();
                    oxm = builder.setValue(IPv4Address.of(tag.tag.intValue())).setMask(IPv4Address.of(tag.mask.intValue())).build();
                } else {
                    OFOxmIpv4Dst.Builder builder = factory.oxms().buildIpv4Dst();
                    oxm = builder.setValue(IPv4Address.of(tag.tag.intValue())).build();
                }

            }

            return oxm;
        } else if (fieldType.equals(MatchFields.IPV4_SRC)) {
            OFOxm oxm = null;

            if (tag != null) {
                if (tag.hasMask) {
                    OFOxmIpv4SrcMasked.Builder builder = factory.oxms().buildIpv4SrcMasked();
                    oxm = builder.setValue(IPv4Address.of(tag.tag.intValue())).setMask(IPv4Address.of(tag.mask.intValue())).build();
                } else {
                    OFOxmIpv4Src.Builder builder = factory.oxms().buildIpv4Src();
                    oxm = builder.setValue(IPv4Address.of(tag.tag.intValue())).build();
                }

            }

            return oxm;
        }
        return null;

    }
}
