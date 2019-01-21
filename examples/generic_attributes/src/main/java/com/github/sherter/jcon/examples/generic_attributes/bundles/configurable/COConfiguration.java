package com.github.sherter.jcon.examples.generic_attributes.bundles.configurable;

import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.TaggingException;
import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.configuration.BundleConfiguration;
import com.github.sherter.jcon.examples.generic_attributes.conflict_policies.policies.DisableTagging;
import org.projectfloodlight.openflow.protocol.OFExperimenter;
import org.projectfloodlight.openflow.protocol.OFTagReallocationRequest;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Handling the reception of configuration messages related to adding and disabling fields.
 */
public class COConfiguration extends BundleConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(COConfiguration.class);

    public COConfiguration(TaggingBundle bundle) {
        super(bundle);
    }

    @Override
    protected boolean specificExperimenterReceived(OFExperimenter message, DatapathId datapathId) {
        if (message instanceof OFTagReallocationRequest) {
            OFTagReallocationRequest request = (OFTagReallocationRequest) message;
            COSelector selector = (COSelector) taggingBundle.getTagTransformation().getFieldSelector();

            boolean addOperation = request.getOperation() > 0;
            Short fieldId = request.getHeaderField();

            if (addOperation) {
                logger.info("Header field added due to configuration change...");
                selector.addField(fieldId, taggingBundle.getFlowRuleStore().getUsedFields());

            } else {
                logger.warn("Tag fields will be reallocated due to configuration change...");
                MatchFields field = selector.getFieldToRemove(fieldId);

                if (field != null) {

                    COReallocation policy = new COReallocation(taggingBundle);

                    List<MatchFields> list = new ArrayList<>();
                    list.add(field);

                    try {
                        policy.triggerReallocation(list);
                    } catch (TaggingException e) {
                        logger.error("Reallocation failed. Tagging will be disabled.");
                        new DisableTagging(taggingBundle).applyPolicy();
                    }
                }
            }

            return true;
        }

        return false;
    }
}
