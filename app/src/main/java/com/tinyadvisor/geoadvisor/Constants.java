package com.tinyadvisor.geoadvisor;

/**
 * Created by tkhakimyanov on 14.06.2015.
 */
public final class Constants {
    public static final int SUCCESS_RESULT = 0;

    public static final int FAILURE_RESULT = 1;

    public static final String PACKAGE_NAME =
            "com.tinyadvisor.geoadvisor";

    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String ACTIVITY = PACKAGE_NAME + ".ACTIVITY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    public static final String ACTIVITY_EXTRA = PACKAGE_NAME + ".ACTIVITY_EXTRA";
    public static final String DETECTED_ACTIVITIES = PACKAGE_NAME + ".DETECTED_ACTIVITIES";


    public static final String ENABLE_BACKGROUND_SERVICE = "enable_background_service_checkbox";
    public static final String TRACK_ADDRESS = "track_address_checkbox";
    public static final String TRACK_ACTIVITY = "track_activity_checkbox";

    public static final double DISTANCE_TO_MOVE_CAMERA = 100;
    public static final double DISTANCE_TO_UPDATE_MAP = 100;
    public static final double DISTANCE_TO_UPDATE_LOCATION = 10;

    public static final int LOCATION_RESULT = 0;
    public static final int ADDRESS_RESULT = 1;
    public static final int ACTIVITY_RESULT = 2;
    public static final int LOCATION_SETTINGS_STATUS = 3;
    public static final int GOOGLE_PLAY_SERVICES_UNAVAILABLE = 4;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 60000;
    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

}
