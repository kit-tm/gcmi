package com.github.sherter.jcon.examples.generic_attributes.bundles.configurable;

import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.IncompatibleComponentsException;
import com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions.TaggingException;
import com.github.sherter.jcon.examples.generic_attributes.bundles.TaggingBundle;
import com.github.sherter.jcon.examples.generic_attributes.conflict_policies.policies.TagReallocation;
import com.github.sherter.jcon.examples.generic_attributes.field_selection.FieldSelector;
import com.github.sherter.jcon.examples.generic_attributes.field_selection.ReallocatableFieldSelector;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.FieldTag;
import org.projectfloodlight.openflow.protocol.match.MatchFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Triggers the reallocation for the configurable tagging bundle.
 */
public class COReallocation extends TagReallocation {
    private static final Logger logger = LoggerFactory.getLogger(COReallocation.class);

    public COReallocation(TaggingBundle implementation) {
        super(implementation);
    }

    public void triggerReallocation(List<MatchFields> matchFields) throws TaggingException {
        FieldSelector fieldSelector = taggingBundle.getTagTransformation().getFieldSelector();

        if (!(fieldSelector instanceof ReallocatableFieldSelector))
            throw new IncompatibleComponentsException();

        Set<FieldTag> toExclude = ((ReallocatableFieldSelector) fieldSelector).excludeFields(matchFields);
        reallocate(toExclude);
    }
}
