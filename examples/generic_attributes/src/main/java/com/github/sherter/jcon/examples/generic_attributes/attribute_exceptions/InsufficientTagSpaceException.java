package com.github.sherter.jcon.examples.generic_attributes.attribute_exceptions;

import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.Tag;
import com.github.sherter.jcon.examples.generic_attributes.tag_transformation.tag_fields.FieldTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * Exception thrown in case of insufficient tag space. May be raised after conflict resolution
 * with lowered header field number.
 */
public class InsufficientTagSpaceException extends TagTransformException {
    private static final Logger logger = LoggerFactory.getLogger(FieldTag.class);

    public InsufficientTagSpaceException(Tag tag, BigInteger comparison) {
        logger.error("Insufficient Tag space Exception. (Value = " + tag.tag + "; Mask = " + tag.mask
                + "; Field = " + comparison + ")");

    }

    public InsufficientTagSpaceException(Tag tag) {
        logger.error("Insufficient Tag space Exception. (Value = " + tag.tag + "; Mask = " + tag.mask
                + ")");

    }

}
