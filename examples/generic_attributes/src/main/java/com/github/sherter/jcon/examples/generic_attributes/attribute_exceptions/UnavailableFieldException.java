package com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions;

import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.TagType;

public class UnavailableFieldException extends TagTransformException {
    public TagType tagType;

    public UnavailableFieldException(TagType tagType) {
        this.tagType = tagType;
    }
}
