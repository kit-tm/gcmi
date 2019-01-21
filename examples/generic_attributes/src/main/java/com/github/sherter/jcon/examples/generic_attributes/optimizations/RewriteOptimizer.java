package com.github.sherter.jcon.examples.generic_attributes.optimizations;

import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.optimizations.fields.FieldRewrite;
import com.github.sherter.jcon.examples.generic_attributes.optimizations.fields.IPv4Rewrite;
import com.github.sherter.jcon.examples.generic_attributes.optimizations.fields.MacRewrite;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Implementation of an exemplary optimizer. Rewriting Mac addresses back before delivery to an end system.
 */
public class RewriteOptimizer extends TaggingOptimizer {
    private static final Logger logger = LoggerFactory.getLogger(RewriteOptimizer.class);

    private HashMap<DatapathId, RewriteCommands> switchCommands = new HashMap<>();

    public RewriteOptimizer(TaggingBundle taggingBundle) {
        super(taggingBundle);

        // TODO Optimizations may be rewritten to match a changed network.
       rewriteConfiguration();

    }

    private void rewriteConfiguration() {
        // Ingress Switch with DatapathId 1
        switchCommands.put(DatapathId.of(1), new RewriteCommands(1,
                new IPv4Rewrite(IPv4Address.of("98.3.0.1"), MatchFields.IPV4_DST)));
        switchCommands.get(DatapathId.of(1)).addRewriter(2,
                new IPv4Rewrite(IPv4Address.of("98.3.0.2"), MatchFields.IPV4_DST));
        switchCommands.get(DatapathId.of(1)).addRewriter(3,
                new IPv4Rewrite(IPv4Address.of("98.3.0.3"), MatchFields.IPV4_DST));
        switchCommands.get(DatapathId.of(1)).addRewriter(4,
                new IPv4Rewrite(IPv4Address.of("98.3.0.4"), MatchFields.IPV4_DST));

        switchCommands.get(DatapathId.of(1)).addRewriter(1,
                new MacRewrite(MacAddress.of(1), MatchFields.ETH_DST));
        switchCommands.get(DatapathId.of(1)).addRewriter(2,
                new MacRewrite(MacAddress.of(2), MatchFields.ETH_DST));
        switchCommands.get(DatapathId.of(1)).addRewriter(3,
                new MacRewrite(MacAddress.of(3), MatchFields.ETH_DST));
        switchCommands.get(DatapathId.of(1)).addRewriter(4,
                new MacRewrite(MacAddress.of(4), MatchFields.ETH_DST));

        // Diff Switch with DatapathId 2
        switchCommands.put(DatapathId.of(2), new RewriteCommands(1,
                new IPv4Rewrite(IPv4Address.of("98.3.0.5"), MatchFields.IPV4_DST)));

        switchCommands.get(DatapathId.of(2)).addRewriter(1,
                new MacRewrite(MacAddress.of(5), MatchFields.ETH_DST));

        // Distribution Switch with DatapathId 5
        switchCommands.put(DatapathId.of(5), new RewriteCommands(1,
                new IPv4Rewrite(IPv4Address.of("98.3.0.6"), MatchFields.IPV4_DST)));
        switchCommands.get(DatapathId.of(5)).addRewriter(2,
                new IPv4Rewrite(IPv4Address.of("98.3.0.7"), MatchFields.IPV4_DST));
        switchCommands.get(DatapathId.of(5)).addRewriter(3,
                new IPv4Rewrite(IPv4Address.of("98.3.0.8"), MatchFields.IPV4_DST));
        switchCommands.get(DatapathId.of(5)).addRewriter(4,
                new IPv4Rewrite(IPv4Address.of("98.3.0.9"), MatchFields.IPV4_DST));

        switchCommands.get(DatapathId.of(5)).addRewriter(1,
                new MacRewrite(MacAddress.of(6), MatchFields.ETH_DST));
        switchCommands.get(DatapathId.of(5)).addRewriter(2,
                new MacRewrite(MacAddress.of(7), MatchFields.ETH_DST));
        switchCommands.get(DatapathId.of(5)).addRewriter(3,
                new MacRewrite(MacAddress.of(8), MatchFields.ETH_DST));
        switchCommands.get(DatapathId.of(5)).addRewriter(4,
                new MacRewrite(MacAddress.of(9), MatchFields.ETH_DST));

    }

    // requirements for the match.
    @Override
    public EthType getRequirements(DatapathId datapathId) {
        return EthType.IPv4;
    }

    @Override
    public List<OFAction> optimizeActions(Match match, List<OFAction> actions, DatapathId datapathId) {
        RewriteCommands rewriteCommands = switchCommands.get(datapathId);
        ArrayList<OFAction> optimizedActions = new ArrayList<>(actions);

        if (rewriteCommands == null)
            return actions;

        int portNumber = getPortNumber(optimizedActions);

        List<FieldRewrite> fieldRewrites = rewriteCommands.getRelevantRewriters(portNumber);

        if (fieldRewrites == null)
            return actions;

        for (FieldRewrite fieldRewrite : fieldRewrites) {
            optimizedActions.add(0, fieldRewrite.getAction());
        }

        return optimizedActions;
    }

    /**
     * Only messages with a matching port number in the Output action are relevant.
     * @param actions
     * @return
     */
    private static int getPortNumber(List<OFAction> actions) {
        int portNumber = -1;

        for (OFAction action : actions) {
            if (action instanceof OFActionOutput) {
                portNumber = ((OFActionOutput) action).getPort().getPortNumber();
            }
        }

        return portNumber;
    }


}
