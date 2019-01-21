package com.github.sherter.jcon.examples.generic_attributes.configuration;

import java.math.BigInteger;
import java.util.HashMap;

/**
 * Delivers App Ids for isolation.
 */
public class AppsInfo {
    private BigInteger taggingAppIds = new BigInteger("0");

    private static final short APP_ID_LENGTH = 2;
    private static final BigInteger MAX_APP_ID =
            new BigInteger("2").pow(APP_ID_LENGTH).add(new BigInteger("-1"));
    private static final BigInteger ONE = new BigInteger("1");

    private static AppsInfo INSTANCE;

    private HashMap<BigInteger, AppConfiguration> configurations = new HashMap();

    private AppsInfo() {

    }

    public static AppsInfo getInstance() {
        if (INSTANCE == null)
            INSTANCE = new AppsInfo();

        return INSTANCE;
    }

    public AppConfiguration getConfiguration(BigInteger id) {
        return configurations.get(id);
    }


    public BigInteger getMaxAppId() {
        return MAX_APP_ID;
    }

    public short getAppIdLength() {
        return APP_ID_LENGTH;
    }

    public BigInteger addApp(AppConfiguration configuration) {
        if(MAX_APP_ID.add(taggingAppIds.negate()).signum() == 1) {
            BigInteger appId = taggingAppIds;
            configurations.put(appId, configuration);

            taggingAppIds = taggingAppIds.add(ONE);

            return appId;
        } else {
            return new BigInteger("-1");
        }

    }

}
