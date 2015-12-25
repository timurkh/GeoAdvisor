package com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.tinyadvisor.geoadvisor.Constants;


/**
 * Created by tkhakimyanov on 09.07.2015.
 */
abstract class ActivityTracker implements
        IActivityTracker,
        ResultCallback<Status> {

    protected final static String TAG = "ACTIVITY_TRACKER";

    private ActivityResult mActivityResult = new ActivityResult();
    protected ActivityBroadcastReceiver mBroadcastActivityReceiver = new ActivityBroadcastReceiver();


    ActivityResult getActivityResult() {
        return mActivityResult;
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(getPackageContext(), ActivityService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(getPackageContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void requestActivityUpdates() {

        if (!getGoogleApiClient().isConnected()) {
            Toast.makeText(getPackageContext(), "GoogleApiClient is not connected yet",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //register broadcasts receiver
        mBroadcastActivityReceiver = new ActivityBroadcastReceiver();
        LocalBroadcastManager.getInstance(getPackageContext()).registerReceiver(mBroadcastActivityReceiver,
                new IntentFilter(Constants.BROADCAST_ACTIVITY));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getPackageContext());
        String updateIntervalString = prefs.getString(Constants.UPDATE_INTERVAL_LIST, Constants.DEFAULT_UPDATE_INTERVAL);
        int updateInterval = Integer.parseInt(updateIntervalString);

        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                getGoogleApiClient(),
                updateInterval,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }

    public void removeActivityUpdates() {
        // Remove all activity updates for the PendingIntent that was used to request activity
        // updates.
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                getGoogleApiClient(),
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);

        // Unregister broadcasts receiver
        LocalBroadcastManager.getInstance(getPackageContext()).unregisterReceiver(mBroadcastActivityReceiver);

        mActivityResult.setActivity(null);
    }

    /**
     * Runs when the result of calling requestActivityUpdates() and removeActivityUpdates() becomes
     * available. Either method can complete successfully or with an error.
     *
     * @param status The Status returned through a PendingIntent when requestActivityUpdates()
     *               or removeActivityUpdates() are called.
     */
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.i(TAG, "Successfully added or removed activity detection");
        } else {
            mActivityResult.setErrorMessage(status.getStatusMessage());
            Log.e(TAG, "Error adding or removing activity detection: " + status.getStatusMessage());
        }
    }

    protected void sendUpdatedActivity() {
        Bundle bundle = new Bundle();
        mActivityResult.saveInstanceState(bundle);
        sendResult(Constants.ACTIVITY_RESULT, bundle);
    }

    public void switchActivityUpdates() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getPackageContext());
        if(prefs.getBoolean(Constants.TRACK_ACTIVITY_CHECKBOX, true))
            requestActivityUpdates();
        else
            removeActivityUpdates();
    }

    public class ActivityBroadcastReceiver extends BroadcastReceiver {
        protected static final String TAG = "ACTIVITY_BROADCAST_RECEIVER";

        @Override
        public void onReceive(Context context, Intent intent) {
            DetectedActivity activity = intent.getParcelableExtra(Constants.ACTIVITY_EXTRA);
            mActivityResult.setActivity(activity);
            sendUpdatedActivity();
        }
    }
}
