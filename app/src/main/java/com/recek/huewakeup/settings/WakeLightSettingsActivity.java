package com.recek.huewakeup.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.philips.lighting.data.PHScheduleFix;
import com.philips.lighting.quickstart.R;

import static com.recek.huewakeup.util.MyDateUtils.hasCorrectFormat;

public class WakeLightSettingsActivity extends AbstractHueSettingsActivity {

    private EditText wakeInputTimeTxt;
    private Spinner wakeScheduleSpinner;
    private EditText wakeEndInputTimeTxt;
    private Spinner wakeEndScheduleSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wake_light_settings);

        // WAKE UP
        wakeScheduleSpinner = (Spinner) findViewById(R.id.wakeLightSpinner);
        String wakeUpScheduleId = getPrefs().getWakeScheduleId();
        buildAndAddAdapter(wakeScheduleSpinner, wakeUpScheduleId);

        wakeInputTimeTxt = (EditText) findViewById(R.id.wakeLightTime);
        wakeInputTimeTxt.setText(getPrefs().getWakeLightTime());

        // WAKE END
        wakeEndScheduleSpinner = (Spinner) findViewById(R.id.wakeLightEndSpinner);
        String wakeEndScheduleId = getPrefs().getWakeEndScheduleId();
        buildAndAddAdapter(wakeEndScheduleSpinner, wakeEndScheduleId);

        wakeEndInputTimeTxt = (EditText) findViewById(R.id.wakeLightEndTime);
        wakeEndInputTimeTxt.setText(getPrefs().getWakeEndTime());

        // BUTTONS
        Button okButton = (Button) findViewById(R.id.lightSettingsOkBtn);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOkClicked();
            }
        });

        Button cancelButton = (Button) findViewById(R.id.lightSettingsCancelBtn);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelClicked();
            }
        });
    }

    private void onOkClicked() {
        String wakeTime = wakeInputTimeTxt.getText().toString();
        if (hasCorrectFormat(wakeTime)) {
            getPrefs().setWakeLightTime(wakeTime);
        }
        PHScheduleFix wakeSchedule = getSelectedValidSchedule(wakeScheduleSpinner);
        if (wakeSchedule != null) {
            getPrefs().setWakeScheduleId(wakeSchedule.getId());
        }

        String wakeEndTime = wakeEndInputTimeTxt.getText().toString();
        if (hasCorrectFormat(wakeEndTime)) {
            getPrefs().setWakeEndTime(wakeEndTime);
        }
        PHScheduleFix wakeEndSchedule = getSelectedValidSchedule(wakeEndScheduleSpinner);
        if (wakeEndSchedule != null) {
            getPrefs().setWakeEndScheduleId(wakeEndSchedule.getId());
        }

        finish();
    }

    private void onCancelClicked() {
        finish();
    }
}
