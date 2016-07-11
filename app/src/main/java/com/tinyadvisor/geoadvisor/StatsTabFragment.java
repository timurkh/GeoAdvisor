package com.tinyadvisor.geoadvisor;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    void setTopLocations(Parcelable [] locations) {
        mLocations.setText(TextUtils.join("\n", locations));
    }

    void setTopActivities(Parcelable [] activities) {
        mLocations.setText(TextUtils.join("\n", activities));
    }

}
