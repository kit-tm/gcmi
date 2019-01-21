package com.github.sherter.jcon.examples.generic_attributes.optimizations.fields;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.ver13.OFFactoryVer13;

/**
 * Creating Actions based on the delivered value.
 */
public abstract class FieldRewrite {
    protected static OFFactoryVer13 factory = (OFFactoryVer13) OFFactories.getFactory(OFVersion.OF_13);
    protected MatchFields fieldType;

    public FieldRewrite(MatchFields fieldType) {
        this.fieldType = fieldType;
    }

    public MatchFields getFieldType() {
        return fieldType;
    }

    public abstract OFAction getAction();
}
