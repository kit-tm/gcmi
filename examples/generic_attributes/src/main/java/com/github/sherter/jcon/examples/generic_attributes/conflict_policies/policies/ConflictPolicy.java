package com.github.sherter.jcon.examples.generic_attributes.conflict_policies.policies;

import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.TaggingException;
import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.TaggingManager;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.types.DatapathId;

import java.util.Set;

/**
 * ConflictPolicy that is used in case of a conflict.
 */
public abstract class ConflictPolicy {
    protected TaggingBundle taggingBundle;
    protected TaggingManager manager;

    public ConflictPolicy(TaggingBundle taggingBundle) {
        this.taggingBundle = taggingBundle;
        manager = taggingBundle.getTaggingManager();
    }

    /**
     * Applies a policy for a message.
     * @param flowMod
     * @param datapathId
     * @param conflicting
     * @throws TaggingException
     */
    public abstract void applyPolicy(OFFlowMod flowMod, DatapathId datapathId, Set<OFOxm> conflicting)
            throws TaggingException;
}
