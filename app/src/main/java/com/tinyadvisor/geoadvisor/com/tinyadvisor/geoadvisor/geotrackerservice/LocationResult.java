package com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice;

import android.location.Location;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.tinyadvisor.geoadvisor.Constants;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by tkhakimyanov on 18.06.2015.
 */
public class LocationResult {

    protected final static String TAG = "LOCATION_RESULT";

    protected Location mCurrentLocation;
    protected String mLastUpdateTime;

    // Keys for storing activity state in the Bundle.
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    void Reset () {
        mCurrentLocation = null;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }

    /**
     *
     * @return true if location has changed and false otherwise
     */
    boolean setLocation(Location location) {
        if(location != null && (mCurrentLocation == null || mCurrentLocation.distanceTo(location) > Constants.DISTANCE_TO_UPDATE_LOCATION)) {
            mCurrentLocation = location;
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            return true;
        }
        return  false;
    }

    public Location getLocation() {
        return mCurrentLocation;
    }

    public String getLastUpdateTime() {
        return mLastUpdateTime;
    }

    public LatLng getLatLng() {
        if(mCurrentLocation != null)
            return new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        return null;
    }

    public void updateValuesFromBundle(Bundle savedInstanceState) {

        if (savedInstanceState != null) {

            StringBuilder log = new StringBuilder(). append("Loaded values from bundle");
            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
                log.append(" mCurrentLocation=" + mCurrentLocation.toString());
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
                log.append(" mLastUpdateTime=" + mLastUpdateTime);
            }

            Log.i(TAG, log.toString());
        }
    }

    public void saveInstanceState(Bundle savedInstanceState){

        StringBuilder log = new StringBuilder("Saved instance state ");

        if(mCurrentLocation != null ) {
            savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
            log.append(" mCurrentLocation=" + mCurrentLocation.toString());
        }

        if(mLastUpdateTime != null) {
            savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
            log.append(" mLastUpdateTime=" + mLastUpdateTime);
        }

        Log.i(TAG, log.toString());
    }

    public String getShortState() {
        if(getLatLng() != null) {
            return getLatLng().toString();
        }

        return null;
    }

    public String getState() {
        StringBuilder state = new StringBuilder();
        state.append("[");
        state.append(getLastUpdateTime());
        state.append("] ");

        if (getLocation() != null) {
            state.append(getLatLng().toString());
            state.append(" accuracy ");
            state.append(getLocation().getAccuracy());
            state.append("m");
        } else {
            state.append("Obtaining location");
        }
        return state.toString();
    }

}
