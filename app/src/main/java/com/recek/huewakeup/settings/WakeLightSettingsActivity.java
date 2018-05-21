package com.recek.huewakeup.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.wrapper.domain.resource.Schedule;
import com.recek.huesleepwake.R;

public class WakeLightSettingsActivity extends AbstractHueSettingsActivity {

    private TextView wakeTimeTxt;
    private Spinner wakeScheduleSpinner;
    private TextView wakeEndTimeTxt;
    private Spinner wakeEndScheduleSpinner;
    private int currentWakeTimeOffset;
    private int currentWakeEndTimeOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wake_light_settings);

        // WAKE UP
        wakeScheduleSpinner = findViewById(R.id.wakeLightSpinner);
        String wakeUpScheduleId = getPrefs().getWakeScheduleId();
        buildAndAddAdapter(wakeScheduleSpinner, wakeUpScheduleId);

        wakeTimeTxt = findViewById(R.id.wakeLightTimeTxt);
        currentWakeTimeOffset = getPrefs().getWakeLightTimeOffset();
        wakeTimeTxt.setText(getString(R.string.txt_set_light_time, currentWakeTimeOffset));

        SeekBar wakeTimeSeekBar = findViewById(R.id.wakeLightSeekBar);
        wakeTimeSeekBar.setProgress(currentWakeTimeOffset);
        wakeTimeSeekBar.setOnSeekBarChangeListener(createWakeTimeSeekBarListener());

        // WAKE END
        wakeEndScheduleSpinner = findViewById(R.id.wakeLightEndSpinner);
        String wakeEndScheduleId = getPrefs().getWakeEndScheduleId();
        buildAndAddAdapter(wakeEndScheduleSpinner, wakeEndScheduleId);

        wakeEndTimeTxt = findViewById(R.id.wakeLightEndTimeTxt);
        currentWakeEndTimeOffset = getPrefs().getWakeEndTimeOffset();
        wakeEndTimeTxt
                .setText(getString(R.string.txt_set_light_end_time, currentWakeEndTimeOffset));

        SeekBar wakeTimeEndSeekBar = findViewById(R.id.wakeLightEndSeekBar);
        wakeTimeEndSeekBar.setProgress(currentWakeEndTimeOffset);
        wakeTimeEndSeekBar.setOnSeekBarChangeListener(createWakeEndTimeSeekBarListener());

        // BUTTONS
        Button okButton = findViewById(R.id.lightSettingsOkBtn);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOkClicked();
            }
        });

        Button cancelButton = findViewById(R.id.lightSettingsCancelBtn);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelClicked();
            }
        });
    }

    private SeekBar.OnSeekBarChangeListener createWakeTimeSeekBarListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentWakeTimeOffset = progress;
                wakeTimeTxt.setText(getString(R.string.txt_set_light_time, currentWakeTimeOffset));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
    }

    private SeekBar.OnSeekBarChangeListener createWakeEndTimeSeekBarListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentWakeEndTimeOffset = progress;
                wakeEndTimeTxt.setText(
                        getString(R.string.txt_set_light_end_time, currentWakeEndTimeOffset));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
    }

    private void onOkClicked() {
        getPrefs().setWakeLightTimeOffset(currentWakeTimeOffset);

        Schedule wakeSchedule = getSelectedValidSchedule(wakeScheduleSpinner);
        if (wakeSchedule != null) {
            getPrefs().setWakeScheduleId(wakeSchedule.getIdentifier());
        } else {
            getPrefs().setWakeScheduleId(null);
        }

        getPrefs().setWakeEndTimeOffset(currentWakeEndTimeOffset);

        Schedule wakeEndSchedule = getSelectedValidSchedule(wakeEndScheduleSpinner);
        if (wakeEndSchedule != null) {
            getPrefs().setWakeEndScheduleId(wakeEndSchedule.getIdentifier());
        } else {
            getPrefs().setWakeEndScheduleId(null);
        }

        finish();
    }

    private void onCancelClicked() {
        finish();
    }
}
