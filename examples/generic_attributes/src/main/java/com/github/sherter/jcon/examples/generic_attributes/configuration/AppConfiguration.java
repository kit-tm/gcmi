package com.github.sherter.jcon.examples.generic_attributes.configuration;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;

/**
 * Holder for general configuration. May be extended in the future.
 */
public class AppConfiguration {
    public EthType etherType = EthType.IPv4;
    private DatapathId datapathId;

    public AppConfiguration(DatapathId datapathId, int type) {
        this.datapathId = datapathId;

        if (!(type == Integer.MAX_VALUE)) {
            etherType = EthType.of(type);
        }
    }
}
