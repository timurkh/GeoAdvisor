package com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;

/**
 * Created by tkhakimyanov on 28.06.2015.
 */
public interface ILocationTracker {
    void sendResult(int resultCode, Bundle resultData);
    Context getPackageContext();
    LocationListener getLocationListener();
    GoogleApiClient getGoogleApiClient();
}
