package com.tinyadvisor.geoadvisor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;


import java.util.ArrayList;

public class GeoTrackerService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    protected final static String TAG = "GEO_TRACKER_SERVICE";


    ResultReceiver mGeoServiceResults;

    protected GoogleApiClient mGoogleApiClient;
    protected LocationTrackerHelper mLocationTrackerHelper;
    protected AddressTrackerHelper mAddresssTrackerHelper;
    protected ActivityDetectionBroadcastReceiver mBroadcastReceiver;

    public static final int ONGOING_NOTIFICATION_ID = 1;


    @Override
    public void onCreate() {
        super.onCreate();

        if (!isGooglePlayServicesAvailable()) {
            Log.e(TAG, "Google Play Services are not available");
            stopSelf();
        }


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();
        mGoogleApiClient.connect();

        // Init address tracker before location tracker thus it could handle first recieved location
        mAddresssTrackerHelper = new AddressTrackerHelper() {
            @Override
            public void sendResult(int resultCode, Bundle resultData) {
                if(mGeoServiceResults != null)
                    mGeoServiceResults.send(resultCode, resultData);
            }

            @Override
            public Context getPackageContext() {
                return GeoTrackerService.this;
            }
        };

        mLocationTrackerHelper = new LocationTrackerHelper() {
            @Override
            public void sendResult(int resultCode, Bundle resultData) {
                if(mGeoServiceResults != null)
                    mGeoServiceResults.send(resultCode, resultData);
            }

            @Override
            public LocationListener getLocationListener() {
                return GeoTrackerService.this;
            }

            @Override
            public GoogleApiClient getGoogleApiClient() {
                return mGoogleApiClient;
            }
        };
        mLocationTrackerHelper.createLocationRequest();
        mLocationTrackerHelper.checkLocationSettings();
    }

    @Override
    public void onDestroy() {
        removeNotification();
        mLocationTrackerHelper.stopLocationUpdates();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            mGeoServiceResults = intent.getParcelableExtra(Constants.RECEIVER);
        }
        else {
            mGeoServiceResults = null;
            Log.w(TAG, "onStartCommand: null intent is passed, perhaps activity is dead");
        }

        createServiceStateNotification();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void createServiceStateNotification() {

        Intent notificationIntent = new Intent(this, MapTabActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.location)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), ))
                .setContentTitle("GeoADvisor: notification")
                .setContentText("GeoADvisor: text")
                .setContentIntent(contentIntent);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(ONGOING_NOTIFICATION_ID, notification);
    }

    private void removeNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(ONGOING_NOTIFICATION_ID);
    }

    public void sendResult(int resultCode, Bundle resultData) {
        if(mGeoServiceResults != null)
            mGeoServiceResults.send(resultCode, resultData);
    }

    void sendGooglePlayServiceUnavailable(int status) {
        if(mGeoServiceResults != null) {
            Log.i(TAG, "Notifying activity that Google Play Services is unavailable");
            Bundle bundle = new Bundle();
            bundle.putInt("STATUS", status);
            mGeoServiceResults.send(Constants.GOOGLE_PLAY_SERVICES_UNAVAILABLE, bundle);
        }
    }



    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            sendGooglePlayServiceUnavailable(status);
            return false;
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location != null) {
            mLocationTrackerHelper.onLocationChanged(location);
            mAddresssTrackerHelper.onLocationChanged(location);
        }

        mLocationTrackerHelper.startLocationUpdates();
    }



    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.e(TAG, "Connection to GoogleAPI failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.w(TAG, "Connection to GoogleAPI suspended");
        mGoogleApiClient.connect();
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mLocationTrackerHelper.onLocationChanged(location);
        mAddresssTrackerHelper.onLocationChanged(location);
    }
    /**
     * Gets a PendingIntent to be sent for each activity detection.
     *//*
    private PendingIntent getActivityDetectionPendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mActivityDetectionPendingIntent != null) {
            return mActivityDetectionPendingIntent;
        }
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }*/

    /**
     * Receiver for intents sent by DetectedActivitiesIntentService via a sendBroadcast().
     * Receives a list of one or more DetectedActivity objects associated with the current state of
     * the device.
     */
    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
        protected static final String TAG = "activity-detection-response-receiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<DetectedActivity> updatedActivities =
                    intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.DETECTED_ACTIVITIES, updatedActivities );
            sendResult(Constants.ACTIVITY_RESULT, bundle);
        }
    }
}
