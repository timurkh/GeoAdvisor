package com.tinyadvisor.geoadvisor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tinyadvisor.geoadvisor.com.tinyadvisor.geoadvisor.geotrackerservice.GeoTrackerService;

/**
 * Created by Timur on 14-Aug-15.
 */
public class StatsTabFragment extends Fragment {

    protected  static final String TAG = "MAP_FRAGMENT";
    protected TextView mActivities;
    protected TextView mLocations;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        mLocations = (TextView)view.findViewById(R.id.top_locations);
        mActivities = (TextView)view.findViewById(R.id.top_activities);

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            Intent intent = new Intent(getActivity(), GeoTrackerService.class);
            intent.putExtra(Constants.COMMAND, Constants.STATS_RESULT);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            if(prefs.getBoolean(Constants.ENABLE_BACKGROUND_SERVICE_CHECKBOX, true)) {
                this.getActivity().startService(intent);
            }
        }
    }

    void setTopLocations(String [] locations) {
        if(locations != null && mLocations != null)
            mLocations.setText(TextUtils.join("\n", locations));
    }

    void setTopActivities(String [] activities) {
        if(activities != null && mActivities != null)
            mActivities.setText(TextUtils.join("\n", activities));
    }

}
