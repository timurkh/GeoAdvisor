package com.tinyadvisor.geoadvisor;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice.GeoTrackerService;

/**
 * Created by tkhakimyanov on 16.07.2015.
 */
public class MapActivity extends Activity {

    protected  static final String TAG = "MAP_ACTIVITY";

    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    GeoServiceResultReceiver mGeoServiceResults;

    MapTabFragment getMapFragment() {
        return (MapTabFragment)getFragmentManager().findFragmentById(R.id.map_fragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        startGeoTrackerService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startGeoTrackerService();
        getMapFragment().updateMapUI();
    }

    void startGeoTrackerService() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean(Constants.ENABLE_BACKGROUND_SERVICE_CHECKBOX, true)) {
            mGeoServiceResults = new GeoServiceResultReceiver(this, null);
            Intent intent = new Intent(this, GeoTrackerService.class);
            intent.putExtra(Constants.RECEIVER, mGeoServiceResults);
            startService(intent);
        } else {
            mGeoServiceResults = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_logs:
                startActivity(new Intent(this, LogsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class GeoServiceResultReceiver extends ResultReceiver {
        Activity mActivity;

        GeoServiceResultReceiver(Activity activity, Handler handler){
            super(handler);
            mActivity = activity;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case Constants.LOCATION_RESULT:
                    getMapFragment().updateLocationResult(resultData);
                    break;
                case Constants.ADDRESS_RESULT:
                    getMapFragment().updateAddressResult(resultData);
                    break;
                case Constants.ACTIVITY_RESULT:
                    getMapFragment().updateActivityResult(resultData);
                    break;
                case Constants.GOOGLE_PLAY_SERVICES_UNAVAILABLE:
                    GooglePlayServicesUtil.getErrorDialog(resultData.getInt("STATUS"), mActivity, 0).show();
                    break;
                case Constants.LOCATION_SETTINGS_STATUS:
                    try {
                        Status status = resultData.getParcelable("STATUS");
                        // Show the dialog by calling startResolutionForResult(), and check the result
                        // in onActivityResult().
                        status.startResolutionForResult(mActivity, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(TAG, "PendingIntent unable to execute request.");
                    }
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }
    }

}
