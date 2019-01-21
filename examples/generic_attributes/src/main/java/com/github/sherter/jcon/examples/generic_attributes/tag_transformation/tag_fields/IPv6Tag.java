package com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields;

import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.Tag;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.oxm.*;
import org.projectfloodlight.openflow.types.IPv6Address;

/**
 * FieldTag class hiding Mac address specific operations.
 */
public class IPv6Tag extends FieldTag {

    public IPv6Tag(MatchFields type, double probability) {
        super(type, probability);
        checkType();

    }

    public IPv6Tag(MatchFields type) {
        super(type);
        checkType();

    }

    private void checkType() throws IllegalArgumentException {
        if (!(fieldType.equals(MatchFields.IPV6_DST) || fieldType.equals(MatchFields.IPV6_SRC))) {
            throw new IllegalArgumentException("Wrong type was delivered for MacTag object.");
        }
    }

    @Override
    public int getBitLength() {
        return 128;
    }

    /**
     * Returning an IPv6 based field given a tag with or without mask.
     *
     * @param tag
     * @return
     */
    @Override
    protected OFOxm getField(Tag tag) {
        if (fieldType.equals(MatchFields.IPV6_DST)) {
            OFOxm oxm = null;

            if (tag != null) {
                if (tag.hasMask) {
                    OFOxmIpv6DstMasked.Builder builder = factory.oxms().buildIpv6DstMasked();
                    oxm = builder.setValue(IPv6Address.of(tag.tag.toByteArray())).setMask(IPv6Address.of(tag.mask.toByteArray())).build();
                } else {
                    OFOxmIpv6Dst.Builder builder = factory.oxms().buildIpv6Dst();
                    oxm = builder.setValue(IPv6Address.of(tag.tag.toByteArray())).build();
                }

            }

            return oxm;
        } else if (fieldType.equals(MatchFields.IPV6_SRC)) {
            OFOxm oxm = null;

            if (tag != null) {
                if (tag.hasMask) {
                    OFOxmIpv6SrcMasked.Builder builder = factory.oxms().buildIpv6SrcMasked();
                    oxm = builder.setValue(IPv6Address.of(tag.tag.toByteArray())).setMask(IPv6Address.of(tag.mask.toByteArray())).build();
                } else {
                    OFOxmIpv6Src.Builder builder = factory.oxms().buildIpv6Src();
                    oxm = builder.setValue(IPv6Address.of(tag.tag.toByteArray())).build();
                }

            }

            return oxm;
        }
        return null;

    }
}

