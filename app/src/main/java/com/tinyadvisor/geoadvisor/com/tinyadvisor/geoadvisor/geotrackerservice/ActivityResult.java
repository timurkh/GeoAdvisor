package com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by tkhakimyanov on 09.07.2015.
 */
public class ActivityResult {

    protected final static String TAG = "ACTIVITY_RESULT";

    protected String mLastUpdateTime;
    protected DetectedActivity mActivity;
    protected String mErrorMessage;
    protected Boolean mDefined = false;

    // Keys for storing activity state in the Bundle.
    protected static final String LOCATION_ACTIVITY_KEY = "activity";
    protected final static String LAST_UPDATED_TIME_KEY = "activity-last-updated-time-key";
    protected final static String ERROR_MESSAGE = "activity-error-message";

    void setErrorMessage(String message) {
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        mErrorMessage = message;
        mActivity = null;
    }

    void setActivity(DetectedActivity activity) {
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        mDefined = true;
        mActivity = activity;
        mErrorMessage = null;
    }

    DetectedActivity getActivity() {
        return mActivity;
    }

    String getLastUpdateTime() {
        return mLastUpdateTime;
    }

    public String getActivityAsText() {
        if(mActivity != null) {
            return DetectedActivity.zzgq(mActivity.getType());
        }

        return null;
    }

    public boolean getDefined() {
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
            } else if (savedInstanceState.keySet().contains(LOCATION_ACTIVITY_KEY)) {
                mActivity = savedInstanceState.getParcelable(LOCATION_ACTIVITY_KEY);
                mDefined = true;
                log.append(" mActivty=" + mActivity);
            } else {
                mDefined = false;
            }

            Log.i(TAG, log.toString());
        }
    }

    public void saveInstanceState(Bundle savedInstanceState){

        StringBuilder log = new StringBuilder("Saved instance state ");

        if(mErrorMessage != null) {
            savedInstanceState.putString(ERROR_MESSAGE, mErrorMessage);
            log.append(" mErrorMessage=" + mErrorMessage);
        }
        else if(mActivity != null) {
            savedInstanceState.putParcelable(LOCATION_ACTIVITY_KEY, mActivity);
            log.append(" mActivity=" + mActivity);
        }

        if(mLastUpdateTime != null) {
            savedInstanceState.putString(LAST_UPDATED_TIME_KEY, mLastUpdateTime);
            log.append(" mLastUpdateTime=" + mLastUpdateTime);
        }

        Log.i(TAG, log.toString());
    }

    public String getState() {
        String activity = getActivityAsText();

        if (activity != null) {
            StringBuilder state = new StringBuilder();
            state.append("[");
            state.append(getLastUpdateTime());
            state.append("] ");
            state.append(activity);
            state.append(" ");
            state.append(getActivity().getConfidence());
            return state.toString();
        }
        return null;
    }

}
