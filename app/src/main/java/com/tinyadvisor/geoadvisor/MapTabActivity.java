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

public class MapTabActivity extends Activity {

    protected  static final String TAG = "MAPACTIVITY";
    protected GoogleMap mGoogleMap;
    protected TextView mAddressTextView;
    protected TextView mAddressTitleTextView;
    protected TextView mActivityTextView;
    protected TextView mActivityTitleTextView;
    protected GeoState mGeoState;
    private LatLng mCurrentLatLng;
    private String mCurrentAddress;


    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    GeoServiceResultReceiver mGeoServiceResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map_tab);
        mAddressTextView = (TextView)findViewById(R.id.detected_address);
        mAddressTitleTextView = (TextView)findViewById(R.id.detected_address_title);
        mActivityTextView = (TextView)findViewById(R.id.detected_activity_name);
        mActivityTitleTextView = (TextView)findViewById(R.id.detected_activity_name_title);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mGoogleMap = mapFragment.getMap();
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setIndoorEnabled(true);

        mGeoState = new GeoState();
        mGeoState.updateValuesFromBundle(savedInstanceState);

        startGeoTrackerService();
        updateMapUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startGeoTrackerService();
    }

    void startGeoTrackerService() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("enable_background_service_checkbox", true)) {
            if(mGeoServiceResults == null) {
                mGeoServiceResults = new GeoServiceResultReceiver(this, null);
                Intent intent = new Intent(this, GeoTrackerService.class);
                intent.putExtra(Constants.RECEIVER, mGeoServiceResults);
                startService(intent);
            }
        } else {
            mGeoServiceResults = null;
        }
    }

    /**
     * Stores activity data in the Bundle.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        mGeoState.saveInstanceState(savedInstanceState, true);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void updateMapUI() {
        LatLng newLatLng = mGeoState.getLatLng();

        if(newLatLng != null) {
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

            if (mGeoState.getAddress() != null)
                mCurrentAddress = mGeoState.getAddress();
        }

        if(mCurrentAddress != null) {
            mAddressTitleTextView.setVisibility(View.VISIBLE);
            mAddressTextView.setText(mGeoState.getAddress());
        } else {
            mAddressTitleTextView.setVisibility(View.GONE);
            mAddressTextView.setText("");
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
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra(Constants.RECEIVER, mGeoServiceResults);
                startActivity(intent);
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
                case Constants.GEO_STATE:
                    mGeoState.updateValuesFromBundle(resultData);
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

