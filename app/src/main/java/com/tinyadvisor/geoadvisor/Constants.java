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
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
    public static final String ACTIVITY_EXTRA = PACKAGE_NAME + ".ACTIVITY_EXTRA";
    public static final String BROADCAST_ACTIVITY = PACKAGE_NAME + ".BROADCAST_ACTIVITY";


    public static final String ENABLE_BACKGROUND_SERVICE_CHECKBOX = "enable_background_service_checkbox";
    public static final String TRACK_ADDRESS_CHECKBOX = "track_address_checkbox";
    public static final String TRACK_ACTIVITY_CHECKBOX = "track_activity_checkbox";
    public static final String UPDATE_INTERVAL_LIST = "update_interval_list";

    public static final double DISTANCE_TO_MOVE_CAMERA = 100;
    public static final double DISTANCE_TO_UPDATE_MAP = 100;
    public static final double DISTANCE_TO_UPDATE_LOCATION = 10;

    public static final int LOCATION_RESULT = 0;
    public static final int ADDRESS_RESULT = 1;
    public static final int ACTIVITY_RESULT = 2;
    public static final int LOCATION_SETTINGS_STATUS = 3;
    public static final int GOOGLE_PLAY_SERVICES_UNAVAILABLE = 4;
    public static final int STATS_RESULT = 5;


    public static final int FASTEST_UPDATE_INTERVAL_COEFFICIENT = 2;
    public static final String DEFAULT_UPDATE_INTERVAL = "1000";

    public static final String STATS_TOP_ACTIVITIES = "stats-top-activities";
    public static final String STATS_TOP_LOCATIONS = "stats-top-locations";
}
