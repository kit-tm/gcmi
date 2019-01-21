package com.github.sherter.jcon.examples.generic_attributes.tagging_manager.message_builder;

import com.github.sherter.jcon.examples.generic_attributes.configuration.AppsInfo;
import com.github.sherter.jcon.examples.generic_attributes.optimizations.TaggingOptimizer;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.Tag;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;

/**
 * Determining the requirements of a flow rule so that tagging can be used in various protocol layers.
 */
public class RequirementsDetermination {
    private EthType optimizationType;
    private EthType actionType;
    private EthType matchType;

    public void requiredInOptimization(TaggingOptimizer optimizer, DatapathId datapathId) {
        if (optimizer != null) {
            optimizationType = optimizer.getRequirements(datapathId);
        }
    }

    private EthType getTypeFromTag(Tag tag) {
        if (tag != null) {
            return AppsInfo.getInstance().getConfiguration(tag.extractAppId()).etherType;
        } else {
            return null;
        }
    }

    public void requiredInAction(Tag tag) {
        actionType = getTypeFromTag(tag);
    }

    public void requiredInMatch(Tag tag) {
        matchType = getTypeFromTag(tag);
    }

    // Requirements can arise from: Match -> Actions -> Optimizations
    public EthType getRequirement() {
        if (matchType != null) {
            return matchType;
        } else if (actionType != null) {
            return actionType;
        } else if (optimizationType != null) {
            return optimizationType;
        } else {
            return null;
        }
    }

}
