package com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;

import com.tinyadvisor.geoadvisor.Constants;

/**
 * Created by tkhakimyanov on 27.06.2015.
 */
abstract class AddressTracker
    implements IAddresssTracker {


    private boolean mAddressRequested;
    private Location mAddressRequestedLocation;
    private AddressResult mAddressResult = new AddressResult();
    private AddressResultReceiver mAddressResultReceiver = new AddressResultReceiver(new Handler());

    protected void sendUpdatedAddress() {
        Bundle bundle = new Bundle();
        mAddressResult.saveInstanceState(bundle);
        sendResult(Constants.ADDRESS_RESULT, bundle);
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService(Location location) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getPackageContext());
        if(prefs.getBoolean(Constants.ENABLE_BACKGROUND_SERVICE_CHECKBOX, true)) {

            mAddressRequested = true;
            mAddressRequestedLocation = location;

            // Create an intent for passing to the intent service responsible for fetching the address.
            Intent intent = new Intent(getPackageContext(), AddressService.class);

            // Pass the result receiver as an extra to the service.
            intent.putExtra(Constants.RECEIVER, mAddressResultReceiver);

            // Pass the location data as an extra to the service.
            intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);

            // Start the service. If the service isn't already running, it is instantiated and started
            // (creating a process for it if needed); if it is running then it remains running. The
            // service kills itself automatically once all intents are processed.
            getPackageContext().startService(intent);
        }
    }

    public void onLocationChanged(Location location) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getPackageContext());
        if(prefs.getBoolean(Constants.TRACK_ADDRESS_CHECKBOX, true)) {
            if (!mAddressRequested)
                if (mAddressResult.getAddress() == null || mAddressRequestedLocation == null || (mAddressRequestedLocation.distanceTo(location) > Constants.DISTANCE_TO_UPDATE_MAP))
                    startIntentService(location);
        } else {
            mAddressResult.setAddress(null);
        }
    }

    public AddressResult getAddressResult() {
        return mAddressResult;
    }

    /**
     * Receiver for data sent from AddressService.
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from AddressService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Check if we have error here
            if(resultData.containsKey(AddressService.ERROR_MESSAGE_KEY)) {
                mAddressResult.setErrorMessage(resultData.getString(AddressService.ERROR_MESSAGE_KEY));
            } else {
                mAddressResult.setAddress((Address) resultData.getParcelable(AddressService.RESULT_DATA_KEY));
                mAddressRequested = false;
            }
            sendUpdatedAddress();
        }
    }
}
