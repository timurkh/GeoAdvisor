package com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.location.LocationListener;

/**
 * Created by tkhakimyanov on 27.06.2015.
 */
public interface IAddresssTracker {

    void sendResult(int resultCode, Bundle resultData);
    Context getPackageContext();
}
