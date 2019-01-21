package com.github.sherter.jcon.examples.generic_attributes.conflict_policies;

/**
 * Policies can have different effects. Used for signalling the Message Interception
 * what happened during tag transformation.
 */
public enum ConflictPolicyEffect {
    ABORT, SEND_DIRECT, SEND_TRANSFORM;
}
