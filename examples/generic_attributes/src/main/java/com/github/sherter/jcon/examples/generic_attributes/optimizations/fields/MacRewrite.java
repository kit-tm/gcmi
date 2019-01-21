package com.github.sherter.jcon.examples.generic_attributes.optimizations.fields;

import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.MacTag;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.types.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rewriting a MAC address on an edge switch.
 */
public class MacRewrite extends FieldRewrite {
    private static final Logger logger = LoggerFactory.getLogger(MacTag.class);
    private MacAddress macAddress;

    public MacRewrite(MacAddress macAddress, MatchFields fieldType) {
        super(fieldType);
        this.macAddress = macAddress;

        if (!(fieldType.equals(MatchFields.ETH_SRC) || fieldType.equals(MatchFields.ETH_DST))) {
            throw new IllegalArgumentException("Wrong type was delivered for MacRewrite object.");
        }
    }

    @Override
    public OFAction getAction() {
        if (fieldType.equals(MatchFields.ETH_DST)) {
            return factory.actions().buildSetField()
                    .setField(factory.oxms().buildEthDst().setValue(macAddress).build())
                    .build();

        } else if (fieldType.equals(MatchFields.ETH_SRC)) {
            return factory.actions().buildSetField()
                    .setField(factory.oxms().buildEthSrc().setValue(macAddress).build())
                    .build();

        }
        return null;
    }
}
