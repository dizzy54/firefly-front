package com.dizzy1354.firefly;

/**
 * Created by akshat on 11/12/15.
 */
public class Constants {

    private Constants() {
    }

    /**
     * Eddystone-UID frame type value.
     */
    static final byte UID_FRAME_TYPE = 0x00;

    /**
     * Eddystone-URL frame type value.
     */
    static final byte URL_FRAME_TYPE = 0x10;

    /**
     * Eddystone-TLM frame type value.
     */
    static final byte TLM_FRAME_TYPE = 0x20;

    /**
     * Minimum expected Tx power (in dBm) in UID and URL frames.
     */
    //static final int MIN_EXPECTED_TX_POWER = -100;

    /**
     * Maximum expected Tx power (in dBm) in UID and URL frames.
     */
    //static final int MAX_EXPECTED_TX_POWER = 20;

    public static final int MIN_SPOT_INTERVAL = 3;  // Minimum time between 2 spots of same beacon
    public static final String SERVER_URL = "http://firefly-staging.elasticbeanstalk.com/";
    public static final int VEHICLE_UPDATE_INTERVAL = 5*1000;  // Minimum time between 2 live vehicle queries in milliseconds

}
