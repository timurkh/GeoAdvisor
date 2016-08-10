package com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.tinyadvisor.geoadvisor.Constants;
import com.tinyadvisor.geoadvisor.MainActivity;
import com.tinyadvisor.geoadvisor.R;

import java.util.Timer;
import java.util.TimerTask;

public class GeoTrackerService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    protected final static String TAG = "GEO_TRACKER_SERVICE";
    public static final int ONGOING_NOTIFICATION_ID = 1;
    public static final int MSG_GET_STATS = 1;

    ResultReceiver mGeoServiceResults;

    protected GoogleApiClient mGoogleApiClient;
    protected LocationTracker mLocationTracker;
    protected AddressTracker mAddressTracker;
    protected ActivityTracker mActivityTracker;

    // run on another Thread to avoid crash
    // timer handling
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    ResultsCollection mResultsCollection = new ResultsCollection();

    @Override
    public void onCreate() {
        super.onCreate();

        if (!isGooglePlayServicesAvailable()) {
            Log.e(TAG, "Google Play Services are not available");
            stopSelf();
        }


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        initTrackers();
        setupTimer();
    }

    void initTrackers() {

        // Init address tracker before location tracker thus it could handle first recieved location
        mAddressTracker = new AddressTracker() {
            @Override
            public void sendResult(int resultCode, Bundle resultData) {
                GeoTrackerService.this.sendResult(resultCode, resultData);
            }

            @Override
            public Context getPackageContext() {
                return GeoTrackerService.this;
            }
        };

        mLocationTracker = new LocationTracker() {
            @Override
            public void sendResult(int resultCode, Bundle resultData) {
                GeoTrackerService.this.sendResult(resultCode, resultData);
            }

            @Override
            public LocationListener getLocationListener() {
                return GeoTrackerService.this;
            }

            @Override
            public GoogleApiClient getGoogleApiClient() {

                return mGoogleApiClient;
            }

            @Override
            public Context getPackageContext() {
                return GeoTrackerService.this;
            }
        };

        mActivityTracker = new ActivityTracker() {
            @Override
            public void sendResult(int resultCode, Bundle resultData) {
                GeoTrackerService.this.sendResult(resultCode, resultData);
            }

            @Override
            public Context getPackageContext() {
                return GeoTrackerService.this;
            }

            @Override
            public GoogleApiClient getGoogleApiClient() {
                return mGoogleApiClient;
            }
        };

        mLocationTracker.createLocationRequest();
        mLocationTracker.checkLocationSettings();
    }

    void setupTimer() {
        stopTimer();

        // recreate new
        mTimer = new Timer();

        // schedule task
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Integer writeInterval = Integer.parseInt(prefs.getString(Constants.UPDATE_INTERVAL_LIST, "60000"));

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimeWriteResultsTask(), 0, writeInterval);
    }

    void stopTimer() {
        //TODO serialize existing stats - need define data model
        if(mTimer != null) {
            mTimer.cancel();
        }
    }

    class TimeWriteResultsTask extends TimerTask {
        @Override public void run () {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mResultsCollection.add(mActivityTracker.getActivityResult(), mAddressTracker.getAddressResult());
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        stopTimer();
        removeNotification();
        mLocationTracker.stopLocationUpdates();
        mActivityTracker.removeActivityUpdates();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            int command = intent.getIntExtra(Constants.COMMAND, 0);
            switch(command) {
                case Constants.STATS_RESULT:
                    Bundle bundle = new Bundle();
                    mResultsCollection.saveInstanceState(bundle);
                    sendResult(Constants.STATS_RESULT, bundle);
                    break;
                case Constants.RESTART_TIMER:
                    setupTimer();
                    break;
                case Constants.SWITCH_ACTIVITY_DETECTION:
                    mActivityTracker.toggleActivityUpdates();
                    break;
                default:
                    mGeoServiceResults = intent.getParcelableExtra(Constants.RECEIVER);
                    mLocationTracker.sendUpdatedLocation();
                    mAddressTracker.sendUpdatedAddress();
                    mActivityTracker.sendUpdatedActivity();
            }
        }
        else {
            mGeoServiceResults = null;
            Log.w(TAG, "onStartCommand: null intent is passed, perhaps activity is dead");
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setNotificationMessage() {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        StringBuilder notificationTitle = new StringBuilder();
        notificationTitle.append(getResources().getString(R.string.app_name));

        StringBuilder notificationText = new StringBuilder();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String activity = null;
        if(prefs.getBoolean(Constants.TRACK_ACTIVITY_CHECKBOX, true))
            activity = mActivityTracker.getActivityResult().getActivityAsText();

        if(prefs.getBoolean(Constants.TRACK_ADDRESS_CHECKBOX, true)) {
            Address address = mAddressTracker.getAddressResult().getAddress();
            if (address != null) {

                notificationTitle.append(": ");

                if (activity != null) {
                    notificationTitle.append(activity);
                }

                notificationTitle.append("@");
                notificationTitle.append(address.getLocality());

                notificationText.append(mAddressTracker.getAddressResult().getState());
                notificationText.append(System.lineSeparator());

            } else {
                notificationText.append(getResources().getString(R.string.obtaining_address));
                notificationText.append(System.lineSeparator());
            }
        }

        if(activity != null) {
            notificationText.append(mActivityTracker.getActivityResult().getState());
            notificationText.append(System.lineSeparator());
        }

        if(mLocationTracker.getLocationResult().getLocation() != null) {
            notificationText.append(mLocationTracker.getLocationResult().getState());
            notificationText.append(System.lineSeparator());
        }

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.location)
                .setContentTitle(notificationTitle.toString())
                .setStyle(new Notification.BigTextStyle().bigText(notificationText.toString()))
                .setContentText(notificationText.toString())
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
        setNotificationMessage();

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
            mLocationTracker.onLocationChanged(location);
            mAddressTracker.onLocationChanged(location);
        }

        mLocationTracker.startLocationUpdates();
        mActivityTracker.toggleActivityUpdates();
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
        mLocationTracker.onLocationChanged(location);
        mAddressTracker.onLocationChanged(location);
    }
}
