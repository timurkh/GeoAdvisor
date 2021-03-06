package com.tinyadvisor.geoadvisor;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice.GeoTrackerService;


public class SettingsActivity extends PreferenceActivity {

    public SettingsActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        getFragmentManager().beginTransaction().replace(android.R.id.content, new GeneralPreferenceFragment()).commit();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    protected void startBackgroundService(Boolean boolValue, Integer command) {
        if(boolValue) {
            Intent intent = new Intent(this, GeoTrackerService.class);
            if(command != null)
                intent.putExtra(Constants.COMMAND, command);
            this.startService(intent);
        }
        else {
            this.stopService(new Intent(this, GeoTrackerService.class));
        }
    }


    public class GeneralPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        GeneralPreferenceFragment() {}

        private void setupPreferenceValueAndSummary(Preference preference) {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(this);

            // Trigger the listener immediately with the preference's
            // current value.
            Object value = PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext()).getAll().get(preference.getKey());
            onPreferenceChange(preference, value, false);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            setupPreferenceValueAndSummary(findPreference(Constants.ENABLE_BACKGROUND_SERVICE_CHECKBOX));
            setupPreferenceValueAndSummary(findPreference(Constants.TRACK_ADDRESS_CHECKBOX));
            setupPreferenceValueAndSummary(findPreference(Constants.TRACK_ACTIVITY_CHECKBOX));
            setupPreferenceValueAndSummary(findPreference(Constants.UPDATE_INTERVAL_LIST));
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            return onPreferenceChange(preference, value, true);
        }

        boolean onPreferenceChange(Preference preference, Object value, boolean changeServiceState) {
            if (preference instanceof ListPreference) {
                String stringValue = value.toString();

                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

                if(changeServiceState)
                    startBackgroundService(true, Constants.RESTART_TIMER);
            } else if (preference instanceof CheckBoxPreference) {
                Boolean boolValue = (Boolean)value;

                if(preference.getKey().toString().equals(Constants.ENABLE_BACKGROUND_SERVICE_CHECKBOX)) {

                    this.findPreference(Constants.TRACK_ADDRESS_CHECKBOX).setEnabled(boolValue);
                    this.findPreference(Constants.TRACK_ACTIVITY_CHECKBOX).setEnabled(boolValue);

                    if(changeServiceState)
                        startBackgroundService(boolValue, 0);

                } else if(preference.getKey().toString().equals(Constants.TRACK_ACTIVITY_CHECKBOX)) {
                    if(changeServiceState)
                        startBackgroundService(true, Constants.SWITCH_ACTIVITY_DETECTION);

                } else if(preference.getKey().toString().equals(Constants.TRACK_ADDRESS_CHECKBOX)) {
                    //if(changeServiceState)
                    //    startBackgroundService(true, 0);
                }

            } else {
                String stringValue = value.toString();

                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }

            return true;
        }
    }
}
