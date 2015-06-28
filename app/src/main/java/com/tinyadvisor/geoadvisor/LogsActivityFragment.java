package com.tinyadvisor.geoadvisor;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * A placeholder fragment containing a simple view.
 */
public class LogsActivityFragment extends Fragment {

    TextView mTextView;
    ScrollView mScrollView;

    public LogsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_logs, container, false);
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
                log.append(System.lineSeparator());
            }
            mTextView = (TextView)view.findViewById(R.id.log_text_view);
            mTextView.setText(log.toString());

            mScrollView = (ScrollView) view.findViewById(R.id.logs_scroller);

            mScrollView.post(new Runnable() {
                public void run() {
                    mScrollView.smoothScrollTo(0, mTextView.getBottom());
                }
            });
        } catch (IOException e) {
        }
        return view;
    }
}
