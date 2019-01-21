package com.github.sherter.jcon.examples.generic_attributes.tag_transformation;

import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.InsufficientTagSpaceException;
import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.TaggingException;
import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.configuration.AppsInfo;
import com.github.sherter.jcon.examples.generic_attributes.field_selection.FieldSelector;
import com.github.sherter.jcon.examples.generic_attributes.message_store.FlowRuleStore;
import com.github.sherter.jcon.examples.generic_attributes.message_store.FlowRuleStoreEntry;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.FieldTag;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.FlowModType;
import com.github.sherter.jcon.examples.generic_attributes.topology.TopologyManager;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFOxmList;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.protocol.ver13.OFFactoryVer13;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class responsible for transforming Tag fields into actual header fields.
 */
public abstract class TagTransformation {
    private static final Logger logger = LoggerFactory.getLogger(TagTransformation.class);
    protected TaggingBundle bundle;
    protected FieldSelector fieldSelector;
    private static OFFactoryVer13 factory = (OFFactoryVer13) OFFactories.getFactory(OFVersion.OF_13);

    protected static final BigInteger ZERO = new BigInteger("0");
    protected static final BigInteger ONE = new BigInteger("1");
    protected static final BigInteger MINUS_ONE = new BigInteger("-1");
    protected static final BigInteger TWO = new BigInteger("2");

    public TagTransformation(TaggingBundle bundle) {
        this.bundle = bundle;
    }

    public boolean fieldInUse(OFOxm field) {
        boolean inUse = false;

        for (FieldTag fieldTag : fieldSelector.getUsedFields()) {
            if (fieldTag.getField().equals(field.getMatchField().id)) {
                inUse = true;
                break;
            }
        }
        return inUse;
    }

    public FieldSelector getFieldSelector() {
        return fieldSelector;
    }

    /**
     * If a mask exists, every bit of the isolation mask has to be set to avoid inconsistencies.
     *
     * @param tag
     * @return
     */
    private boolean incorrectMaskCheck(Tag tag) {
        if (tag.hasMask) {
            BigInteger tagMask = tag.mask;
            BigInteger mask = TWO.pow(AppsInfo.getInstance().getAppIdLength()).add(MINUS_ONE);

            BigInteger maskedValue = tagMask.and(mask);

            // mask is not completely set
            return maskedValue.compareTo(mask) > 0;
        } else {
            return false;
        }
    }


    /**
     * Returns the encoding for every header field. Encoding is defined by sub classes.
     * @param availableFields
     * @param tag
     * @param flowModType
     * @return
     * @throws TaggingException
     */
    protected abstract List<List<Tag>> getFieldEncoding(List<FieldTag> availableFields, Tag tag, FlowModType flowModType)
            throws TaggingException;

    /**
     * Transforming Match Tag field. Note that for one Tag messages, multiple messages with
     * actual header fields can be created. This is the case for Tag transformations relying
     * on algorithms like PathSet.
     *
     * @param tag
     * @param flowModType
     * @return
     * @throws IllegalArgumentException
     * @throws InsufficientTagSpaceException
     */
    public List<TransformedMatch> getTransformedMatch(Tag tag, FlowModType flowModType)
            throws IllegalArgumentException, TaggingException {

        ArrayList<TransformedMatch> transformedMatches = new ArrayList<>();

        if (incorrectMaskCheck(tag))
            throw new IllegalArgumentException("IsolationId Mask is not correct.");

        List<FieldTag> fieldTags = fieldSelector.getSelection();

        // there is potentially more than one rule required for a tag transformation
        List<List<Tag>> encodingLists = getFieldEncoding(fieldTags, tag, flowModType);

        for (List<Tag> encodings : encodingLists) {

            if (fieldTags.size() != encodings.size()) {
                throw new IllegalArgumentException("Number of fields don't match number of encodings.");
            }

            ArrayList<OFOxm<?>> matches = new ArrayList<>();
            ArrayList<MatchFields> usedFields = new ArrayList<>();
            ArrayList<FieldTag> usedFieldTags = new ArrayList<>();

            for (int i = 0; i < fieldTags.size(); i++) {
                FieldTag fieldTag = fieldTags.get(i);
                Tag encoding = encodings.get(i);

                if (encoding == null)
                    continue;

                OFOxm oxm = fieldTag.buildMatch(encoding, flowModType);
                matches.add(oxm);
                usedFields.add(fieldTag.getField());
                usedFieldTags.add(fieldTag);
            }


            transformedMatches.add(new TransformedMatch(usedFields, OFOxmList.ofList(matches)));
            fieldSelector.fieldsSelected(usedFieldTags);
        }

        return transformedMatches;
    }

    /**
     * Transforming Action Tag field. Note that for one Tag messages, multiple messages with
     * actual header fields can be created. This is the case for Tag transformations relying
     * on algorithms like PathSet.
     *
     * @param tag
     * @param flowModType
     * @return
     * @throws IllegalArgumentException
     * @throws InsufficientTagSpaceException
     */
    public List<TransformedAction> getTransformedAction(Tag tag, FlowModType flowModType)
            throws IllegalArgumentException, TaggingException {

        ArrayList<TransformedAction> transformed = new ArrayList<>();

        if (incorrectMaskCheck(tag))
            throw new IllegalArgumentException("IsolationId Mask is not correct.");

        List<FieldTag> fieldTags = fieldSelector.getSelection();

        // there is potentially more than one rule required for a tag transformation
        List<List<Tag>> encodingLists = getFieldEncoding(fieldTags, tag, flowModType);

        for (List<Tag> encodings : encodingLists) {

            if (fieldTags.size() != encodings.size()) {
                throw new IllegalArgumentException("Number of fields don't match number of encodings.");
            }

            ArrayList<OFAction> actions = new ArrayList<>();
            ArrayList<MatchFields> usedFields = new ArrayList<>();
            ArrayList<FieldTag> usedFieldTags = new ArrayList<>();

            for (int i = 0; i < fieldTags.size(); i++) {
                FieldTag fieldTag = fieldTags.get(i);
                Tag encoding = encodings.get(i);

                if (encoding == null)
                    continue;

                OFAction action = fieldTag.buildAction(encoding, flowModType);
                actions.add(action);
                usedFields.add(fieldTag.getField());
                usedFieldTags.add(fieldTag);

            }

            fieldSelector.fieldsSelected(usedFieldTags);
            transformed.add(new TransformedAction(usedFields, actions));
        }

        return transformed;
    }

    /**
     * Unresolvable conflicts involve invalidating flow rules.
     */
    public void invalidateAll() {
        FlowRuleStore flowRuleStore = bundle.getFlowRuleStore();
        HashMap<OFFlowMod, FlowRuleStoreEntry> flowMods = flowRuleStore.getFlowModMap();

        for (OFFlowMod flowMod : flowMods.keySet()) {
            List<Match> matches = flowMods.get(flowMod).matches;

            for (Match match : matches) {
                OFFlowMod deleteMod = factory.buildFlowDelete().setMatch(match).build();
                TopologyManager.getInstance().getContext(flowMods.get(flowMod).datapathId).sendDownstream(deleteMod);
            }

        }

        flowRuleStore.invalidateRules();
    }
}
