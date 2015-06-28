package com.tinyadvisor.geoadvisor;

import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;

/**
 * Created by tkhakimyanov on 28.06.2015.
 */
public interface ILocationTrackerService {
    void sendResult(int resultCode, Bundle resultData);
    LocationListener getLocationListener();
    GoogleApiClient getGoogleApiClient();
}
