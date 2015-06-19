package com.tinyadvisor.geoadvisor;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by tkhakimyanov on 18.06.2015.
 */
public class GeoState {

    protected final static String TAG = "GEOSTATE";

    protected Location mCurrentLocation;
    protected String mLastUpdateTime;
    protected String mCurrentAddress;
    protected String mLastSentAddress;

    // Keys for storing activity state in the Bundle.
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    protected static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    protected static final String ADDRESS_REQUESTED_LOCATION_KEY = "address-request-location-pending";
    protected static final String LOCATION_ADDRESS_KEY = "location-address";

    void setLocation(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }

    void setAddress(String address) {
        mCurrentAddress = address;
    }

    public String getAddress() {
        return mCurrentAddress;
    }

    public Location getLocation() {
        return mCurrentLocation;
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
                log.append(" mLastUpdateTime=" + mLastUpdateTime.toString());
            }

            // Check savedInstanceState to see if the location address string was previously found
            // and stored in the Bundle. If it was found, display the address string in the UI.
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mCurrentAddress = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                log.append(" mAddressOutput=" + mCurrentAddress.toString());
            }

            Log.i(TAG, log.toString());
        }
    }

    void saveInstanceState(Bundle savedInstanceState){
        saveInstanceState(savedInstanceState, false);
    }

    void saveInstanceState(Bundle savedInstanceState, Boolean includeAddress){

        StringBuilder log = new StringBuilder("Saved instance state ");

        if(mCurrentLocation != null ) {
            savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
            log.append(" mCurrentLocation=" + mCurrentLocation.toString());
        }

        if(mLastUpdateTime != null) {
            savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
            log.append(" mLastUpdateTime=" + mLastUpdateTime.toString());
        }

        if(mCurrentAddress != null && includeAddress) {
            mLastSentAddress = mCurrentAddress;
            savedInstanceState.putString(LOCATION_ADDRESS_KEY, mCurrentAddress);
            log.append(" mAddressOutput=" + mCurrentAddress.toString());
        }

        Log.i(TAG, log.toString());
    }
}
