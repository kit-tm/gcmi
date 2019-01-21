package com.github.sherter.jcon.examples.generic_attributes.bundles.multi_field;

import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.configuration.BundleConfiguration;
import com.github.sherter.jcon.examples.generic_attributes.message_store.LocalStore;
import com.github.sherter.jcon.examples.generic_attributes.optimizations.RewriteOptimizer;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.TaggingManager;

/**
 * Fixed Bundle uses multi-field header fields and does not reassign in case of a conflict.
 * Tagging will rather be disabled directly in case of a conflict.
 */
public class MFBundle extends TaggingBundle {
    public MFBundle(TaggingManager manager) {
        super(manager);

        tagTransformation = new MFTransformation(this);
        conflictPolicyEnforcer = new MFPolicyEnforcer(this);
        flowRuleStore = new LocalStore();
        taggingOptimizer = new RewriteOptimizer(this);
        bundleConfiguration = new BundleConfiguration(this);
    }
}
