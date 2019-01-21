package com.github.sherter.jcon.examples.generic_attributes.tag_transformation;

import com.github.sherter.jcon.examples.generic_attributes.configuration.AppsInfo;
import org.projectfloodlight.openflow.types.IPv6Address;

import java.math.BigInteger;

/**
 * Tag field using IPv6 addresses and bit masks if needed.
 */
public class Tag {
    public BigInteger tag;
    public BigInteger mask;
    public boolean hasMask;

    public Tag(IPv6Address tag) {
        this.tag = getBigInt(tag);
        hasMask = false;
    }

    public Tag(BigInteger bigInteger) {
        this.tag = bigInteger;
        hasMask = false;
    }

    public Tag(BigInteger value, BigInteger mask) {
        this.tag = value;
        this.mask = mask;
        hasMask = true;
    }

    public Tag(IPv6Address tag, IPv6Address mask) {
        this.tag = getBigInt(tag);
        this.mask = getBigInt(mask);
        hasMask = true;
    }

    public BigInteger extractAppId() {
        short idLength = AppsInfo.getInstance().getAppIdLength();

        // ((1 << idLength) - 1) & value
        return new BigInteger("1").shiftLeft(idLength).add(new BigInteger("-1")).and(tag);
    }

    /**
     * Masked and non-masked are differentiated as well as the IPv6 address value.
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Tag) {
            Tag toCompare = (Tag) o;
            if (tag.equals(toCompare.tag)) {
                if (hasMask) {
                    return mask.equals(toCompare.mask);

                } else {
                    return true;
                }

            }
        }
        return false;
    }

    /**
     * Integer representation.
     *
     * @param iPv6Address
     * @return
     */
    private static BigInteger getBigInt(IPv6Address iPv6Address) {
        byte[] bytes = iPv6Address.getBytes();

        return new BigInteger(1, bytes);

    }
}
