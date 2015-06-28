package com.tinyadvisor.geoadvisor;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by tkhakimyanov on 27.06.2015.
 */
public class AddressResult {

    protected final static String TAG = "ADDRESS_RESULT";

    protected String mLastUpdateTime;
    protected String mCurrentAddress;
    protected Boolean mDefined = false;

    // Keys for storing activity state in the Bundle.
    protected static final String LOCATION_ADDRESS_KEY = "location-address";
    protected final static String LAST_UPDATED_TIME_KEY = "location-address-last-updated-time-key";

    void Reset () {
        mLastUpdateTime = null;
        mCurrentAddress = null;
        mDefined = false;
    }

    void setAddress(String address) {
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        mCurrentAddress = address;
    }

    public String getAddress() {
        return mCurrentAddress;
    }

    public Boolean getDefined() {
        return mDefined;
    }

    public void updateValuesFromBundle(Bundle savedInstanceState) {

        if (savedInstanceState != null) {

            StringBuilder log = new StringBuilder(). append("Loaded values from bundle");
            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_KEY);
                log.append(" mLastUpdateTime=" + mLastUpdateTime);
            }

            // Check savedInstanceState to see if the location address string was previously found
            // and stored in the Bundle. If it was found, display the address string in the UI.
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mCurrentAddress = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                mDefined = true;
                log.append(" mAddressOutput=" + mCurrentAddress);
            } else {
                mDefined = false;
            }

            Log.i(TAG, log.toString());
        }
    }

    void saveInstanceState(Bundle savedInstanceState){

        StringBuilder log = new StringBuilder("Saved instance state ");

        if(mLastUpdateTime != null) {
            savedInstanceState.putString(LAST_UPDATED_TIME_KEY, mLastUpdateTime);
            log.append(" mLastUpdateTime=" + mLastUpdateTime);
        }
        savedInstanceState.putString(LOCATION_ADDRESS_KEY, mCurrentAddress);
        log.append(" mAddressOutput=" + mCurrentAddress);

        Log.i(TAG, log.toString());
    }
}
