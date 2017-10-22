package com.recek.huewakeup.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.quickstart.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaysSettingsActivity extends Activity {

    private static final Logger LOG = LoggerFactory.getLogger(DaysSettingsActivity.class);

    private HueSharedPreferences prefs;
    private DayCheckBoxAdapter dayCheckBoxAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_days_settings);
        prefs = HueSharedPreferences.getInstance(getApplicationContext());

        dayCheckBoxAdapter = new DayCheckBoxAdapter(this, prefs);
        GridView gridView = (GridView) findViewById(R.id.daysGridView);
        gridView.setAdapter(dayCheckBoxAdapter);

        Button okBtn = (Button) findViewById(R.id.daysOkBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOkBtnClicked();
            }
        });
    }

    private void onOkBtnClicked() {
        dayCheckBoxAdapter.saveToPrefs();
        finish();
    }
}
