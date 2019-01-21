package com.github.sherter.jcon.examples.generic_attributes.bundles.automatic;

import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.configuration.BundleConfiguration;
import com.github.sherter.jcon.examples.generic_attributes.message_store.LocalStore;
import com.github.sherter.jcon.examples.generic_attributes.optimizations.RewriteOptimizer;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.TaggingManager;

/**
 * Bundling components related to an approach that dynamically chooses header fields and
 * uses reassignment based conflict resolution.
 */
public class AUBundle extends TaggingBundle {

    public AUBundle(TaggingManager manager) {
        super(manager);

        tagTransformation = new AUTransformation(this);
        conflictPolicyEnforcer = new AUPolicyEnforcer(this);
        flowRuleStore = new LocalStore();
        taggingOptimizer = new RewriteOptimizer(this);
        bundleConfiguration = new BundleConfiguration(this);
    }


}
