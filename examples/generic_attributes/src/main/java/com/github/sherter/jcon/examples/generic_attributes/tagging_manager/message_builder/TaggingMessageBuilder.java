package com.github.sherter.jcon.examples.generic_attributes.tagging_manager.message_builder;

import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.InsufficientTagSpaceException;
import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.TaggingException;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.Tag;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TagTransformation;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TransformedAction;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TransformedMatch;
import com.github.sherter.jcon.examples.generic_attributes.tagging_manager.FlowModType;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionSetField;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.projectfloodlight.openflow.protocol.oxm.OFOxm;
import org.projectfloodlight.openflow.protocol.ver13.OFFactoryVer13;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv6Address;
import org.projectfloodlight.openflow.types.OFValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to assemble a FlowMod message by consulting the TagTransformation in case of the
 * existance of a Tag field.
 */
public class TaggingMessageBuilder {
    private static final Logger logger = LoggerFactory.getLogger(TaggingMessageBuilder.class);
    private static OFFactoryVer13 factory = (OFFactoryVer13) OFFactories.getFactory(OFVersion.OF_13);

    /**
     * Builds a FlowMod message from an existing message, changing match and action fields.
     * @param flowMod
     * @param match
     * @param actions
     * @return
     */
    public static OFFlowMod buildFromExistingMsg(OFFlowMod flowMod, Match match, List<OFAction> actions) {
        return flowMod.createBuilder()
                .setMatch(match)
                .setActions(actions)
                .build();
    }

    /**
     * Transforms Tag Match fields using the Tag Transformation.
     * @param tagTransformation
     * @param match
     * @param required
     * @param flowModType
     * @return
     * @throws InsufficientTagSpaceException
     */
    public static List<TransformedMatch> getTransformedMatch(TagTransformation tagTransformation, Match match,
            RequirementsDetermination required, FlowModType flowModType) throws TaggingException {

        List<TransformedMatch> transformedMatches = new ArrayList<>();

        if (match instanceof OFMatchV3) {
            OFMatchV3 matchV3 = (OFMatchV3) match;
            OFOxmList list = matchV3.getOxmList();

            OFOxm tagField = null;
            Tag tag = null;

            // Marking a Tag Match.
            for (OFOxm<?> ofOxMatch : list) {
                if (ofOxMatch.getMatchField().id.equals(MatchFields.GEN_TAG)) {
                    tagField = ofOxMatch;
                }
            }

            if (tagField != null) {
                tag = getTag((IPv6Address) tagField.getValue(), tagField);
                required.requiredInMatch(tag);
            }

            // necessary to also match on ether type, e.g. matching on IP fields
            EthType type = required.getRequirement();
            if (type != null) {
                OFOxmList.Builder etherTypeBuilder = getMatchBuilder(list, false);
                etherTypeBuilder.set(factory.oxms().buildEthType().setValue(type).build());
                list = etherTypeBuilder.build();
            }

            // Tag transformation if Tag field was recognized.
            if (tag != null) {
                List<TransformedMatch> transformed = tagTransformation.getTransformedMatch(tag, flowModType);


                for (TransformedMatch transformedMatch : transformed) {
                    OFOxmList.Builder builder = getMatchBuilder(list, true);

                    for (OFOxm<?> attributeMatch : transformedMatch.matches) {
                        builder.set(attributeMatch);
                    }

                    OFOxmList tagList = builder.build();


                    Match newMatch = matchV3.createBuilder().setOxmList(tagList).build();
                    transformedMatch.match = newMatch;
                    transformedMatches.add(transformedMatch);
                }

                // No Tag field was delivered, returning original message.
            } else {
                Match newMatch = matchV3.createBuilder().setOxmList(list).build();
                transformedMatches.add(new TransformedMatch(newMatch));
            }

        }

        return transformedMatches;
    }




    /**
     * Creates an OFOxmList.Builder based on the given list.
     * @param list
     * @return
     */
    private static OFOxmList.Builder getMatchBuilder(OFOxmList list, boolean unsetTag) {
        OFOxmList.Builder builder = list.createBuilder();

        for (OFOxm<?> ofOxMatch : list) {
            if (ofOxMatch.getMatchField().id.equals(MatchFields.GEN_TAG) && unsetTag) {
                builder.unset(ofOxMatch.getMatchField());
            } else {
                builder.set(ofOxMatch);
            }
        }

        return builder;
    }

    /**
     * Transforms Tag Action fields using the Tag Transformation.
     * @param tagTransformation
     * @param actions
     * @param requirements
     * @param flowModType
     * @return
     * @throws InsufficientTagSpaceException
     */
    public static List<TransformedAction> getTransformedActions(TagTransformation tagTransformation, List<OFAction> actions,
            RequirementsDetermination requirements, FlowModType flowModType) throws TaggingException {

        ArrayList<OFAction> nonTags = new ArrayList<>();
        OFAction tagAction = null;

        // Scanning for Tag Action.
        for (OFAction action : actions) {
            if (action instanceof OFActionSetField) {
                OFOxm attribute = ((OFActionSetField) action).getField();
                OFValueType setFieldType = attribute.getValue();

                if (setFieldType instanceof IPv6Address && attribute.getMatchField().id.equals(MatchFields.GEN_TAG)) {
                    tagAction = action;
                } else {
                    nonTags.add(action);
                }

            } else {
                nonTags.add(action);
            }
        }

        ArrayList<MatchFields> usedFields = new ArrayList<>();
        List<TransformedAction> transformedActions = new ArrayList<>();

        // Tag Action is available, transformation is required
        if(tagAction != null) {
            OFOxm attribute = ((OFActionSetField) tagAction).getField();
            OFValueType setFieldType = attribute.getValue();
            IPv6Address value = (IPv6Address) setFieldType;

            Tag tag = getTag(value, attribute);
            requirements.requiredInAction(tag);

            List<TransformedAction> transformed = tagTransformation.getTransformedAction(tag, flowModType);

            for (TransformedAction transformedAction : transformed) {
                ArrayList<OFAction> clonedActions = (ArrayList<OFAction>) nonTags.clone();
                ArrayList<MatchFields> clonedUsed = (ArrayList<MatchFields>) usedFields.clone();

                clonedActions.addAll(0, transformedAction.actions);
                clonedUsed.addAll(0, transformedAction.usedFields);

                transformedActions.add(new TransformedAction(clonedUsed, clonedActions));

            }

            // no Tag transformation is required.
        } else {
            transformedActions.add(new TransformedAction(new ArrayList<>(), actions));
        }

        return transformedActions;
    }

    private static Tag getTag(IPv6Address value, OFOxm ofOxm) {
        Tag tag;

        if (ofOxm.isMasked()) {
            tag = new Tag(value, (IPv6Address) ofOxm.getMask());
        } else {
            tag = new Tag(value);
        }

        return tag;
    }

}
