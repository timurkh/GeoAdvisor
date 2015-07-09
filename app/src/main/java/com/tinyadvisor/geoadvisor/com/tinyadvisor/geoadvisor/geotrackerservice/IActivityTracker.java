package com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by tkhakimyanov on 09.07.2015.
 */
public interface IActivityTracker {
    void sendResult(int resultCode, Bundle resultData);
    Context getPackageContext();
    GoogleApiClient getGoogleApiClient();
}
