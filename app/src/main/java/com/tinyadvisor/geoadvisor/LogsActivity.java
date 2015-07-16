package com.tinyadvisor.geoadvisor;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;


public class LogsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);
        //getActionBar().setDisplayHomeAsUpEnabled(true);

/*        LogsFragment logs = new LogsFragment();
        logs.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction().add(R.id.logs_fragment, logs).commit();
 */   }


}
