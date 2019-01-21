package com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields;

import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.Tag;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.protocol.oxm.OFOxmIpDscp;
import org.projectfloodlight.openflow.protocol.oxm.OFOxmIpDscpMasked;
import org.projectfloodlight.openflow.types.IpDscp;

public class DSCPTag extends FieldTag {

    public DSCPTag(double probability) {
        super(MatchFields.IP_DSCP, probability);

    }

    public DSCPTag() {
        super(MatchFields.IP_DSCP);

    }

    @Override
    public int getBitLength() {
        return 6;
    }

    /**
     * Returning a DSCP based field given a tag with or without mask.
     *
     * @param tag
     * @return
     */
    @Override
    protected OFOxm getField(Tag tag) {
        if (tag.hasMask) {
            OFOxmIpDscpMasked.Builder builder = factory.oxms().buildIpDscpMasked();
            return builder.setValue(IpDscp.of(tag.tag.byteValue())).setMask(IpDscp.of(tag.mask.byteValue())).build();
        } else {
            OFOxmIpDscp.Builder builder = factory.oxms().buildIpDscp();
            return builder.setValue(IpDscp.of(tag.tag.byteValue())).build();
        }
    }
}
