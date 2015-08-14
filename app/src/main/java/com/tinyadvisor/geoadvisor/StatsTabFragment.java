package com.tinyadvisor.geoadvisor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Timur on 14-Aug-15.
 */
public class StatsTabFragment extends Fragment {

    protected  static final String TAG = "MAP_FRAGMENT";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        return view;
    }
}
