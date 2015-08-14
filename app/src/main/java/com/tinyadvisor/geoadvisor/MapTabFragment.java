package com.tinyadvisor.geoadvisor;

import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice.ActivityResult;
import com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice.AddressResult;
import com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice.LocationResult;

public class MapTabFragment extends Fragment {

    protected  static final String TAG = "MAP_FRAGMENT";
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mLocationTextView = (TextView)view.findViewById(R.id.location);
        mLocationTitleTextView = (TextView)view.findViewById(R.id.location_title);
        mAddressTextView = (TextView)view.findViewById(R.id.detected_address);
        mAddressTitleTextView = (TextView)view.findViewById(R.id.detected_address_title);
        mActivityTextView = (TextView)view.findViewById(R.id.detected_activity_name);
        mActivityTitleTextView = (TextView)view.findViewById(R.id.detected_activity_name_title);

        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        mGoogleMap = mapFragment.getMap();
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setIndoorEnabled(true);

        mLocationResult.updateValuesFromBundle(savedInstanceState);
        mAddressResult.updateValuesFromBundle(savedInstanceState);
        mActivityResult.updateValuesFromBundle(savedInstanceState);

        updateMapUI();

        return view;
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

    public void updateAddressResult(Bundle bundle) {
        mAddressResult.updateValuesFromBundle(bundle);
        updateMapUI();
    }

    public void updateLocationResult(Bundle bundle) {
        mLocationResult.updateValuesFromBundle(bundle);
        updateMapUI();
    }

    public void updateActivityResult(Bundle bundle) {
        mActivityResult.updateValuesFromBundle(bundle);
        updateMapUI();
    }

    protected void updateMapUI() {
        LatLng newLatLng = mLocationResult.getLatLng();

        FragmentActivity activity = getActivity();
        if(activity != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);

            if (newLatLng != null && prefs.getBoolean(Constants.ENABLE_BACKGROUND_SERVICE_CHECKBOX, true)) {
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
            } else {
                mGoogleMap.setMyLocationEnabled(false);
                mLocationTitleTextView.setVisibility(View.GONE);
                mLocationTextView.setVisibility(View.GONE);
                mCurrentAddress = null;
            }

            if (mCurrentAddress != null && prefs.getBoolean(Constants.TRACK_ADDRESS_CHECKBOX, true)) {
                mAddressTitleTextView.setVisibility(View.VISIBLE);
                mAddressTextView.setVisibility(View.VISIBLE);
                mAddressTextView.setText(mCurrentAddress);
            } else {
                mAddressTitleTextView.setVisibility(View.GONE);
                mAddressTextView.setVisibility(View.GONE);
            }

            if (mCurrentActivity != null && prefs.getBoolean(Constants.TRACK_ACTIVITY_CHECKBOX, true)) {
                mActivityTitleTextView.setVisibility(View.VISIBLE);
                mActivityTextView.setVisibility(View.VISIBLE);
                mActivityTextView.setText(mCurrentActivity);
            } else {
                mActivityTitleTextView.setVisibility(View.GONE);
                mActivityTextView.setVisibility(View.GONE);
            }
        }
    }

}

