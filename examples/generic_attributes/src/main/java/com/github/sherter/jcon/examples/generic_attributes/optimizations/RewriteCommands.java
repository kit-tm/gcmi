package com.github.sherter.jcon.examples.generic_attributes.optimizations;

import com.github.sherter.jcon.examples.generic_attributes.optimizations.fields.FieldRewrite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Delivers rewrite actions for a switch.
 */
public class RewriteCommands {
    private HashMap<Integer, List<FieldRewrite>> portToRewriter = new HashMap<>();

    public RewriteCommands(int portNumber, FieldRewrite fieldRewrite) {
        addRewriter(portNumber, fieldRewrite);
    }


    public void addRewriter(int portNumber, FieldRewrite fieldRewrite) {
        portToRewriter.computeIfAbsent(portNumber, f -> portToRewriter.put(f, new ArrayList<FieldRewrite>()));

        portToRewriter.get(portNumber).add(fieldRewrite);
    }

    public List<FieldRewrite> getRelevantRewriters(int portNumber) {
        return portToRewriter.get(portNumber);
    }
}
