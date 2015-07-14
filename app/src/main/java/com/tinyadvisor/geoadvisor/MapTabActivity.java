package com.tinyadvisor.geoadvisor;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice.ActivityResult;
import com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice.AddressResult;
import com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice.GeoTrackerService;
import com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice.LocationResult;

public class MapTabActivity extends Activity {

    protected  static final String TAG = "MAPACTIVITY";
    protected GoogleMap mGoogleMap;

    protected LocationResult mLocationResult = new LocationResult();
    private LatLng mCurrentLatLng;
    protected TextView mLocationTextView;
    protected TextView mLocationTitleTextView;

    protected AddressResult mAddressResult = new AddressResult();
    protected String mCurrentAddress;
    protected TextView mAddressTextView;
    protected TextView mAddressTitleTextView;

    protected ActivityResult mActivityResult = new ActivityResult();
    private String mCurrentActivity;
    protected TextView mActivityTextView;
    protected TextView mActivityTitleTextView;

    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    GeoServiceResultReceiver mGeoServiceResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map_tab);
        mLocationTextView = (TextView)findViewById(R.id.location);
        mLocationTitleTextView = (TextView)findViewById(R.id.location_title);
        mAddressTextView = (TextView)findViewById(R.id.detected_address);
        mAddressTitleTextView = (TextView)findViewById(R.id.detected_address_title);
        mActivityTextView = (TextView)findViewById(R.id.detected_activity_name);
        mActivityTitleTextView = (TextView)findViewById(R.id.detected_activity_name_title);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mGoogleMap = mapFragment.getMap();
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setIndoorEnabled(true);

        mLocationResult.updateValuesFromBundle(savedInstanceState);
        mAddressResult.updateValuesFromBundle(savedInstanceState);
        mActivityResult.updateValuesFromBundle(savedInstanceState);

        startGeoTrackerService();
        updateMapUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startGeoTrackerService();
        updateMapUI();
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

    /**
     * Stores activity data in the Bundle.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        mLocationResult.saveInstanceState(savedInstanceState);
        mAddressResult.saveInstanceState(savedInstanceState);
        mActivityResult.saveInstanceState(savedInstanceState);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void updateMapUI() {
        LatLng newLatLng = mLocationResult.getLatLng();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String currentAddress;

        if(newLatLng != null && prefs.getBoolean(Constants.ENABLE_BACKGROUND_SERVICE_CHECKBOX, true)) {
            mGoogleMap.setMyLocationEnabled(true);
            boolean cameraTracksCurrentLocation = true;
            LatLng cameraLatLng = mGoogleMap.getCameraPosition().target;
            if (mCurrentLatLng != null) {
                float[] distance = new float[1];
                Location.distanceBetween(cameraLatLng.latitude, cameraLatLng.longitude, mCurrentLatLng.latitude, mCurrentLatLng.longitude, distance);
                cameraTracksCurrentLocation = distance[0] < Constants.DISTANCE_TO_MOVE_CAMERA;
            }

            if (cameraTracksCurrentLocation) {
                if (mCurrentLatLng == null) // we are first time here
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15));
                else
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(newLatLng));
            }

            mCurrentLatLng = newLatLng;

            if (mAddressResult.getDefined())
                mCurrentAddress = mAddressResult.getState();

            if (mActivityResult.getDefined())
                mCurrentActivity = mActivityResult.getState();

            mLocationTitleTextView.setVisibility(View.VISIBLE);
            mLocationTextView.setVisibility(View.VISIBLE);
            mLocationTextView.setText(mLocationResult.getState());
        }
        else {
            mGoogleMap.setMyLocationEnabled(false);
            mLocationTitleTextView.setVisibility(View.GONE);
            mLocationTextView.setVisibility(View.GONE);
            mCurrentAddress = null;
        }

        if(mCurrentAddress != null && prefs.getBoolean(Constants.TRACK_ADDRESS_CHECKBOX, true)) {
            mAddressTitleTextView.setVisibility(View.VISIBLE);
            mAddressTextView.setVisibility(View.VISIBLE);
            mAddressTextView.setText(mCurrentAddress);
        } else {
            mAddressTitleTextView.setVisibility(View.GONE);
            mAddressTextView.setVisibility(View.GONE);
        }

        if(mCurrentActivity != null && prefs.getBoolean(Constants.TRACK_ACTIVITY_CHECKBOX, true)) {
            mActivityTitleTextView.setVisibility(View.VISIBLE);
            mActivityTextView.setVisibility(View.VISIBLE);
            mActivityTextView.setText(mCurrentActivity);
        } else {
            mActivityTitleTextView.setVisibility(View.GONE);
            mActivityTextView.setVisibility(View.GONE);
        }
    }

    /*
     *
     * Next functionality should go out of map tab
     *
     *
     */
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

    class GeoServiceResultReceiver extends  ResultReceiver {
        Activity mActivity;

        GeoServiceResultReceiver(Activity activity, Handler handler){
            super(handler);
            mActivity = activity;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case Constants.LOCATION_RESULT:
                    mLocationResult.updateValuesFromBundle(resultData);
                    updateMapUI();
                    break;
                case Constants.ADDRESS_RESULT:
                    mAddressResult.updateValuesFromBundle(resultData);
                    updateMapUI();
                    break;
                case Constants.ACTIVITY_RESULT:
                    mActivityResult.updateValuesFromBundle(resultData);
                    updateMapUI();
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

