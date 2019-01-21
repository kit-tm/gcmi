package com.github.sherter.jcon.examples.generic_attributes.optimizations.fields;

import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.types.IPv4Address;

/**
 * Rewriting a IP address on an edge switch.
 */
public class IPv4Rewrite extends FieldRewrite {
    private IPv4Address address;

    public IPv4Rewrite(IPv4Address address, MatchFields fieldType) {
        super(fieldType);
        this.address = address;

        if (!(fieldType.equals(MatchFields.IPV4_SRC) || fieldType.equals(MatchFields.IPV4_DST))) {
            throw new IllegalArgumentException("Wrong type was delivered for IPv4Rewrite object.");
        }
    }

    @Override
    public OFAction getAction() {
        if (fieldType.equals(MatchFields.IPV4_DST)) {
            return factory.actions().buildSetField()
                    .setField(factory.oxms().buildIpv4Dst().setValue(address).build())
                    .build();

        } else if (fieldType.equals(MatchFields.IPV4_SRC)) {
            return factory.actions().buildSetField()
                    .setField(factory.oxms().buildIpv4Src().setValue(address).build())
                    .build();

        }
        return null;
    }
}

