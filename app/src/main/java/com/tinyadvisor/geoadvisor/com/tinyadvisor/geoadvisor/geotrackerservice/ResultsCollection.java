package com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice;

import android.os.Bundle;

import com.tinyadvisor.geoadvisor.Constants;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

/**
 * Created by tkhakimyanov on 12.07.2016.
 */

public class ResultsCollection {
    public static final long WRITE_INTERVAL = 60 * 1000; // 60 seconds
    public static final long TRACKED_PERIOD = 8 * 60 * 60 * 1000; // 8 hours

    ArrayList<ActivityResult> activityCollections = new ArrayList<ActivityResult>();
    ArrayList<AddressResult> addressCollections = new ArrayList<AddressResult>();

    void add(ActivityResult activityResult, AddressResult addressResult) {
        if (activityCollections.size() > TRACKED_PERIOD / WRITE_INTERVAL) {
            activityCollections.remove(0);
            addressResult.remove(0);
        }
        activityCollections.add(activityResult);
        addressCollections.add(addressResult);
    }

    void saveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putStringArray(Constants.STATS_TOP_LOCATIONS, mErrorMessage);
        savedInstanceState.putStringArray(Constants.STATS_TOP_ACTIVITIES, mErrorMessage);
    }
}
