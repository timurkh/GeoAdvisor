package com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.tinyadvisor.geoadvisor.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tkhakimyanov on 09.07.2015.
 */
public class ActivityService extends IntentService {

    public static final String RESULT_DATA_KEY = Constants.PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String ERROR_MESSAGE_KEY = Constants.PACKAGE_NAME + ".RESULT_ERROR_MESSAGE";

    public ActivityService() {
        super("ActivityService");
    }

    @Override
    public void onCreate() {
        Log.i(Constants.TAG, "ActivityService created");
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        if(result != null) {
            // Get the most probable activity from the list of activities in the update
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();

            // Get the type of activity
            int activityType = mostProbableActivity.getType();

            if (activityType == DetectedActivity.ON_FOOT) {
                DetectedActivity betterActivity = walkingOrRunning(result.getProbableActivities());
                if (null != betterActivity)
                    mostProbableActivity = betterActivity;
            }

            Log.i(Constants.TAG, "ActivityService: detected " + mostProbableActivity.toString());

            // Check if receiver was properly registered.
            Intent localIntent = new Intent(Constants.BROADCAST_ACTIVITY);

            // Broadcast the list of detected activities.
            localIntent.putExtra(Constants.ACTIVITY_EXTRA, mostProbableActivity);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }



    private DetectedActivity walkingOrRunning(List<DetectedActivity> probableActivities) {
        DetectedActivity myActivity = null;
        int confidence = 0;
        for (DetectedActivity activity : probableActivities) {
            if (activity.getType() != DetectedActivity.RUNNING && activity.getType() != DetectedActivity.WALKING)
                continue;

            if (activity.getConfidence() > confidence)
                myActivity = activity;
        }

        return myActivity;
    }
}
