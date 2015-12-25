package com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice;

import android.location.Location;
import android.os.Bundle;

import com.tinyadvisor.geoadvisor.Constants;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Timur on 02-Dec-15.
 */
public class ActivityAtLocation {
    AddressResult mAddress = new AddressResult();
    ActivityResult mActivity = new ActivityResult();
    String mLastUpdateTime;

    public boolean setResult(int resultCode, Bundle resultData) {
        ActivityAtLocation mCurrentActivityAtLocation;
        switch (resultCode) {
            case Constants.ADDRESS_RESULT:
                mAddress.updateValuesFromBundle(resultData);
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                return true;
            case Constants.ACTIVITY_RESULT:
                mActivity.updateValuesFromBundle(resultData);
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                return true;
        }
        return false;
    }
}
