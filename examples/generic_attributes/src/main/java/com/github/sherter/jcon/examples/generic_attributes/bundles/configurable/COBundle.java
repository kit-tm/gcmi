package com.github.sherter.jcon.examples.generic_attributes.bundles.configurable;

import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.message_store.LocalStore;
import com.github.sherter.jcon.examples.generic_attributes.optimizations.RewriteOptimizer;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.TaggingManager;

/**
 * Configurable Bundle uses one header field at a time and does reassigns in case of a conflict or reassignment request.
 */
public class COBundle extends TaggingBundle {
    public COBundle(TaggingManager manager) {
        super(manager);

        tagTransformation = new COTransformation(this);
        conflictPolicyEnforcer = new COPolicyEnforcer(this);
        flowRuleStore = new LocalStore();
        taggingOptimizer = new RewriteOptimizer(this);
        bundleConfiguration = new COConfiguration(this);
    }
}
