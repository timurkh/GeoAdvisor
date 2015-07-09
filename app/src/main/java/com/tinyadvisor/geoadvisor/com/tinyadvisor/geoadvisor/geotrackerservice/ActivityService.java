package com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.tinyadvisor.geoadvisor.Constants;

import java.util.ArrayList;

/**
 * Created by tkhakimyanov on 09.07.2015.
 */
public class ActivityService extends IntentService {

    protected static final String TAG = "DETECTED_ACTIVITY_SERVICE";

    public static final String RESULT_DATA_KEY = Constants.PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String ERROR_MESSAGE_KEY = Constants.PACKAGE_NAME + ".RESULT_ERROR_MESSAGE";

    /**
     * The receiver where results are forwarded from this service.
     */
    protected ResultReceiver mReceiver;

    public ActivityService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        // Check if receiver was properly registered.
        if (mReceiver == null) {
            Log.wtf(TAG, "No receiver received. There is nowhere to send the results.");
            return;
        }

        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        DetectedActivity detectedActivities = result.getMostProbableActivity();
        deliverResultToReceiver(Constants.SUCCESS_RESULT, detectedActivities);
    }

    private void deliverResultToReceiver(int resultCode, DetectedActivity address) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(RESULT_DATA_KEY, address);
        mReceiver.send(resultCode, bundle);
    }
}
