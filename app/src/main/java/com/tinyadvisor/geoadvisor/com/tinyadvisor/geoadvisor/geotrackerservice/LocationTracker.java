package com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.tinyadvisor.geoadvisor.Constants;


/**
 * Created by tkhakimyanov on 28.06.2015.
 */
abstract class LocationTracker implements
        ILocationTracker {

    protected final static String TAG = "LOCATION_TRACKER";


    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;
    LocationResult mLocationResult = new LocationResult();;

    void sendUpdatedLocation () {
        Bundle bundle = new Bundle();
        mLocationResult.saveInstanceState(bundle);
        sendResult(Constants.LOCATION_RESULT, bundle);
    }

    void sendLocationSettingsStatus(Status status) {
        Log.i(TAG, "Notifying activity that location settings are not sufficient");
        Bundle bundle = new Bundle();
        bundle.putParcelable("STATUS", status);
        sendResult(Constants.LOCATION_SETTINGS_STATUS, bundle);
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getPackageContext());
        String updateIntervalString = prefs.getString(Constants.UPDATE_INTERVAL_LIST, Constants.DEFAULT_UPDATE_INTERVAL);
        int updateInterval = Integer.parseInt(updateIntervalString);

        mLocationRequest.setInterval(updateInterval);
        mLocationRequest.setFastestInterval(updateInterval / Constants.FASTEST_UPDATE_INTERVAL_COEFFICIENT);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    protected void checkLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        getGoogleApiClient(),
                        mLocationSettingsRequest
                );
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i(TAG, "All location settings are satisfied.");
                        startLocationUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.w(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                                "upgrade location settings ");

                        sendLocationSettingsStatus(status);
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.e(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                                "not created.");
                        break;
                }
            }
        });
    }

    public void onLocationChanged(Location location) {
        if(mLocationResult.setLocation(location))
            sendUpdatedLocation();
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                getGoogleApiClient(), mLocationRequest, getLocationListener());
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(getGoogleApiClient(), getLocationListener());
    }

    public LocationResult getLocationResult() {
        return mLocationResult;
    }
}
