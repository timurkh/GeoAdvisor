package com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.tinyadvisor.geoadvisor.Constants;
import com.tinyadvisor.geoadvisor.MapTabFragment;
import com.tinyadvisor.geoadvisor.R;

public class GeoTrackerService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    protected final static String TAG = "GEO_TRACKER_SERVICE";


    ResultReceiver mGeoServiceResults;

    protected GoogleApiClient mGoogleApiClient;
    protected LocationTracker mLocationTracker;
    protected AddressTracker mAddresssTracker;
    protected ActivityTracker mActivityTracker;

    public static final int ONGOING_NOTIFICATION_ID = 1;


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

        // Init address tracker before location tracker thus it could handle first recieved location
        mAddresssTracker = new AddressTracker() {
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

    @Override
    public void onDestroy() {
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
            mGeoServiceResults = intent.getParcelableExtra(Constants.RECEIVER);
        }
        else {
            mGeoServiceResults = null;
            Log.w(TAG, "onStartCommand: null intent is passed, perhaps activity is dead");
        }

        mLocationTracker.sendUpdatedLocation();
        mAddresssTracker.sendUpdatedAddress();
        mActivityTracker.sendUpdatedActivity();

        if (mGoogleApiClient.isConnected())
            mActivityTracker.switchActivityUpdates();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void setNotificationMessage() {

        Intent notificationIntent = new Intent(this, MapTabFragment.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        StringBuilder notificationTitle = new StringBuilder();
        StringBuilder notificationText = new StringBuilder();

        Address address = mAddresssTracker.getAddressResult().getAddress();
        String activity = mActivityTracker.getActivityResult().getActivityAsText();
        notificationTitle.append(getResources().getString(R.string.app_name));

        if(address != null) {

            notificationTitle.append(": ");

            if(activity != null) {
                notificationTitle.append(activity);
            }

            notificationTitle.append("@");
            notificationTitle.append(address.getLocality());

            notificationText.append(mAddresssTracker.getAddressResult().getState());
            notificationText.append(System.lineSeparator());

        } else {
            notificationText.append(getResources().getString(R.string.obtaining_address));
            notificationText.append(System.lineSeparator());
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
            mAddresssTracker.onLocationChanged(location);
        }

        mLocationTracker.startLocationUpdates();
        mActivityTracker.switchActivityUpdates();
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
        mAddresssTracker.onLocationChanged(location);
    }
}
