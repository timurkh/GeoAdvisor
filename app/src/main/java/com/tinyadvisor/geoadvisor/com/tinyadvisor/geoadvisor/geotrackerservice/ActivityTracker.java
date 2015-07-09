package com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import android.os.Handler;
import android.util.Log;

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

    private ActivityResult mActivityResult;
    private DetectedActivityResultReceiver mActivityResultReceiver = new DetectedActivityResultReceiver(new Handler());
    private PendingIntent mActivityDetectionPendingIntent;

    ActivityTracker() {
        mActivityResult = new ActivityResult();
    }

    ActivityResult getActivityResult() {
        return mActivityResult;
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mActivityDetectionPendingIntent != null) {
            return mActivityDetectionPendingIntent;
        }
        Intent intent = new Intent(getPackageContext(), ActivityService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mActivityResultReceiver);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        mActivityDetectionPendingIntent = PendingIntent.getService(getPackageContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return mActivityDetectionPendingIntent;
    }

    public void requestActivityUpdates() {

        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                getGoogleApiClient(),
                Constants.UPDATE_INTERVAL_IN_MILLISECONDS,
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

        } else {
            mActivityResult.setErrorMessage(status.getStatusMessage());
            Log.e(TAG, "Error adding or removing activity detection: " + status.getStatusMessage());
        }
    }

    /**
     * Receiver for data sent from AddressService.
     */
    class DetectedActivityResultReceiver extends ResultReceiver {
        public DetectedActivityResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Check if we have error here
            if(resultData.containsKey(ActivityService.ERROR_MESSAGE_KEY)) {
                mActivityResult.setErrorMessage(resultData.getString(AddressService.ERROR_MESSAGE_KEY));
            } else {
                mActivityResult.setActivity((DetectedActivity) resultData.getParcelable(ActivityService.RESULT_DATA_KEY));
            }
            sendUpdatedActivity();
        }
    }

    protected void sendUpdatedActivity() {
        Bundle bundle = new Bundle();
        mActivityResult.saveInstanceState(bundle);
        sendResult(Constants.ACTIVITY_RESULT, bundle);
    }
}
