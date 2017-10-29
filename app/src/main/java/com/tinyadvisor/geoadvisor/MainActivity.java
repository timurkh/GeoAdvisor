package com.tinyadvisor.geoadvisor;

import android.app.ActionBar;
import android.app.Activity;


import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.Manifest;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice.GeoTrackerService;

/**
 * Created by tkhakimyanov on 16.07.2015.
 */
public class MainActivity extends FragmentActivity {

    protected static final String [] TAB_NAMES = new String [] {
            "Map",
            "Stats"
    };

    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    GeoServiceResultReceiver mGeoServiceResults;
    AdvisorPagerAdapter mAdvisorPagerAdapter;


    MapTabFragment getMapFragment() {
        return (MapTabFragment)mAdvisorPagerAdapter.getMapFragment();
    }

    StatsTabFragment getStatsFragment() {
        return (StatsTabFragment)mAdvisorPagerAdapter.getStatsFragment();
    }

    public void startGeoTrackerService(){
        //Check permissions and only in case of success launch GeoTracker service
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
        else {
            startGeoTrackerService_();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 123:
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    startGeoTrackerService_();
                } else {
                    // Close your app
                    closeNow();
                }
                break;
        }
    }

    private void closeNow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        } else {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        startGeoTrackerService();

        mAdvisorPagerAdapter = new AdvisorPagerAdapter(
                        getSupportFragmentManager());

        final ViewPager viewPager;
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(mAdvisorPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                actionBar.selectTab(actionBar.getTabAt(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // hide the given tab
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // probably ignore this event
            }
        };

        for (int i = 0; i < 2; i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(TAB_NAMES[i])
                            .setTabListener(tabListener));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startGeoTrackerService();
        getMapFragment().updateMapUI();
    }

    /*
    this one is to be called only after permissions checked
     */
    void startGeoTrackerService_() {
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
            MapTabFragment mapFragment = getMapFragment();

            if(mapFragment!=null) {
                switch (resultCode) {
                    case Constants.LOCATION_RESULT:
                        mapFragment.updateLocationResult(resultData);
                        break;
                    case Constants.ADDRESS_RESULT:
                        mapFragment.updateAddressResult(resultData);
                        break;
                    case Constants.ACTIVITY_RESULT:
                        mapFragment.updateActivityResult(resultData);
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
                            Log.e(Constants.TAG, "GeoServiceResultReceiver: status.startResolutionForResult failed.");
                        }
                        break;
                    case Constants.STATS_RESULT:
                        getStatsFragment().setTopLocations(resultData.getStringArray(Constants.STATS_TOP_LOCATIONS));
                        getStatsFragment().setTopActivities(resultData.getStringArray(Constants.STATS_TOP_ACTIVITIES));
                        break;
                }
            }
            super.onReceiveResult(resultCode, resultData);
        }
    }

    public class AdvisorPagerAdapter extends FragmentPagerAdapter {

        MapTabFragment mMapFragment = new MapTabFragment();
        StatsTabFragment mStatsTabFragment = new StatsTabFragment();

        public AdvisorPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            switch(i) {
                case 0:
                    return mMapFragment;
                case 1:
                    return mStatsTabFragment;
            }

            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }

        MapTabFragment getMapFragment() {
            return mMapFragment;
        }

        StatsTabFragment getStatsFragment() {
            return mStatsTabFragment;
        }
    }

}
