package com.github.sherter.jcon.examples.generic_attributes.tagging_manager;

/**
 * Callback to disable tagging in case of unresolvable conflict.
 * May be extended for other cases.
 */
public interface TILCallback {

    void setTaggingState(boolean enable);
}
