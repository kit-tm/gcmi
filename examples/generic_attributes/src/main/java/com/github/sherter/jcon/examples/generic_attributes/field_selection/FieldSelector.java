package com.github.sherter.jcon.examples.generic_attributes.field_selection;

import com.github.sherter.jcon.examples.generic_attributes.configuration.AppsInfo;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TagType;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.FieldTag;

import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Super class for selection strategies.
 */
public abstract class FieldSelector {

    protected Set<FieldTag> freeFields = new LinkedHashSet<>();
    protected Set<FieldTag> usedFields = new LinkedHashSet<>();

    public FieldSelector() {
        initializeFieldTags();
    }

    public BigInteger getAvailableTaggingLength(TagType type) {
        BigInteger maxAppId = AppsInfo.getInstance().getMaxAppId();

        return getTaggingLength(type).add(maxAppId.negate());
    }

    protected abstract BigInteger getTaggingLength(TagType type);

    protected abstract void initializeFieldTags();

    public Set<FieldTag> getUsedFields() {
        return usedFields;
    }

    public abstract List<FieldTag> getSelection();

    /**
     * Tag Transformation selected given fields.
     * @param selectedFields
     */
    public void fieldsSelected(List<FieldTag> selectedFields) {
        for (FieldTag fieldTag : selectedFields) {
            if (freeFields.contains(fieldTag)) {
                usedFields.add(fieldTag);
                freeFields.remove(fieldTag);
            }
        }
    }

}
