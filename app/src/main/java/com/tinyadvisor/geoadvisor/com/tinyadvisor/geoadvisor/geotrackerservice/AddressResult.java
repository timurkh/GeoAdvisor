package com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice;

import android.location.Address;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by tkhakimyanov on 27.06.2015.
 */
public class AddressResult {

    protected final static String TAG = "ADDRESS_RESULT";

    protected String mLastUpdateTime;
    protected Address mCurrentAddress;
    protected String mErrorMessage;
    protected Boolean mDefined = false;

    // Keys for storing activity state in the Bundle.
    protected static final String LOCATION_ADDRESS_KEY = "location-address";
    protected final static String LAST_UPDATED_TIME_KEY = "location-address-last-updated-time-key";
    protected final static String ERROR_MESSAGE = "location-address-error-message";

    void Reset () {
        mLastUpdateTime = null;
        mCurrentAddress = null;
        mDefined = false;
    }

    void setErrorMessage(String message) {
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        mErrorMessage = message;
        mCurrentAddress = null;
    }


    void setAddress(Address address) {
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        mCurrentAddress = address;
        mErrorMessage = null;
    }

    public Address getAddress() {
        return mCurrentAddress;
    }

    public String getAddressAsText() {
        if(mErrorMessage != null) {
            return mErrorMessage;
        }
        else if(mCurrentAddress != null) {
                ArrayList<String> addressFragments = new ArrayList<String>();

                // Fetch the address lines using {@code getAddressLine},
                // join them, and send them to the thread. The {@link android.location.address}
                // class provides other options for fetching address details that you may prefer
                // to use. Here are some examples:
                // getLocality() ("Mountain View", for example)
                // getAdminArea() ("CA", for example)
                // getPostalCode() ("94043", for example)
                // getCountryCode() ("US", for example)
                // getCountryName() ("United States", for example)
                for (int i = 0; i < mCurrentAddress.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(mCurrentAddress.getAddressLine(i));
                }
                return TextUtils.join(", ", addressFragments);
        }
        return null;
    }

    public String getAddressLocality() {
        return mCurrentAddress.getLocality();
    }

    public String getLastUpdateTime() {
        return mLastUpdateTime;
    }

    public Boolean getDefined() {
        return mDefined;
    }

    public void updateValuesFromBundle(Bundle savedInstanceState) {

        if (savedInstanceState != null) {

            StringBuilder log = new StringBuilder(). append("Loaded values from bundle");
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_KEY);
                log.append(" mLastUpdateTime=" + mLastUpdateTime);
            }

            if (savedInstanceState.keySet().contains(ERROR_MESSAGE)) {
                mErrorMessage = savedInstanceState.getParcelable(ERROR_MESSAGE);
                mDefined = true;
                log.append(" mErrorMessage=" + mErrorMessage);
            } else if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mCurrentAddress = savedInstanceState.getParcelable(LOCATION_ADDRESS_KEY);
                mDefined = true;
                log.append(" mAddressOutput=" + getAddressAsText());
            } else {
                mDefined = false;
            }

            Log.i(TAG, log.toString());
        }
    }

    public static String makeFlat(String text) {
        return text.replace(System.lineSeparator(), ", ");
    }

    public void saveInstanceState(Bundle savedInstanceState){

        StringBuilder log = new StringBuilder("Saved instance state ");

        if(mErrorMessage != null) {
            savedInstanceState.putString(ERROR_MESSAGE, mErrorMessage);
            log.append(" mErrorMessage=" + mErrorMessage);
        }
        else if(mCurrentAddress != null) {
            savedInstanceState.putParcelable(LOCATION_ADDRESS_KEY, mCurrentAddress);
            log.append(" mAddressOutput=" + mCurrentAddress);
        }

        if(mLastUpdateTime != null) {
            savedInstanceState.putString(LAST_UPDATED_TIME_KEY, mLastUpdateTime);
            log.append(" mLastUpdateTime=" + mLastUpdateTime);
        }

        Log.i(TAG, log.toString());
    }

    public String getState() {
        String address = getAddressAsText();

        if (address != null) {
            StringBuilder state = new StringBuilder();
            state.append("[");
            state.append(getLastUpdateTime());
            state.append("] ");
            state.append(makeFlat(address));
            return state.toString();
        }
        return null;
    }

}
