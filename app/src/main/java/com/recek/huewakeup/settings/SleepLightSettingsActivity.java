package com.recek.huewakeup.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import com.philips.lighting.data.PHScheduleFix;
import com.recek.huesleepwake.R;

public class SleepLightSettingsActivity extends AbstractHueSettingsActivity {

    private Spinner sleepScheduleSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_ligth_settings);

        sleepScheduleSpinner = (Spinner) findViewById(R.id.sleepLightSpinner);
        String sleepScheduleId = getPrefs().getSleepScheduleId();
        buildAndAddAdapter(sleepScheduleSpinner, sleepScheduleId);

        Button okButton = (Button) findViewById(R.id.sleepLightSettingsOkBtn);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOkClicked();
            }
        });

        Button cancelButton = (Button) findViewById(R.id.sleepLightSettingsCancelBtn);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelClicked();
            }
        });
    }

    private void onOkClicked() {
        PHScheduleFix sleepSchedule = getSelectedValidSchedule(sleepScheduleSpinner);
        if (sleepSchedule != null) {
            getPrefs().setSleepScheduleId(sleepSchedule.getId());
        }

        finish();
    }

    private void onCancelClicked() {
        finish();
    }

}
