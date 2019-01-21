package com.github.sherter.jcon.examples.generic_attributes.optimizations;

import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;

import java.util.List;

/**
 * Used to further transform actions and rewriting original header field content before delivery to an end system.
 */
public abstract class TaggingOptimizer {
    private TaggingBundle taggingBundle;

    public TaggingOptimizer(TaggingBundle taggingBundle) {
        this.taggingBundle = taggingBundle;
    }

    public abstract List<OFAction> optimizeActions(Match match, List<OFAction> actions, DatapathId datapathId);

    public abstract EthType getRequirements(DatapathId datapathId);

}
