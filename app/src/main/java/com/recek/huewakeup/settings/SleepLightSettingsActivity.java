package com.recek.huewakeup.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.wrapper.domain.resource.Schedule;
import com.recek.huesleepwake.R;

import static com.recek.huewakeup.util.DefaultSchedules.DEFAULT_SLEEP_SCHEDULE_NAME;

public class SleepLightSettingsActivity extends AbstractHueSettingsActivity {

    private Spinner sleepScheduleSpinner;
    private int currentSleepTransition;
    private TextView sleepTransitionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_ligth_settings);

        sleepScheduleSpinner = findViewById(R.id.sleepLightSpinner);
        String sleepScheduleId = getPrefs().getSleepScheduleId();
        buildAndAddAdapter(sleepScheduleSpinner, sleepScheduleId, DEFAULT_SLEEP_SCHEDULE_NAME);

        sleepTransitionText = findViewById(R.id.sleepLightTimeTxt);
        currentSleepTransition = getPrefs().getSleepTransition();
        sleepTransitionText
                .setText(getString(R.string.txt_sleep_light_time, currentSleepTransition));

        SeekBar wakeTimeSeekBar = findViewById(R.id.sleepLightSeekBar);
        wakeTimeSeekBar.setProgress(currentSleepTransition);
        wakeTimeSeekBar.setOnSeekBarChangeListener(createSleepTransitionSeekBarListener());


        Button okButton = findViewById(R.id.sleepLightSettingsOkBtn);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOkClicked();
            }
        });

        Button cancelButton = findViewById(R.id.sleepLightSettingsCancelBtn);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelClicked();
            }
        });
    }

    private SeekBar.OnSeekBarChangeListener createSleepTransitionSeekBarListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentSleepTransition = progress;
                sleepTransitionText
                        .setText(getString(R.string.txt_sleep_light_time, currentSleepTransition));
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
        Schedule sleepSchedule = getSelectedValidSchedule(sleepScheduleSpinner);
        if (sleepSchedule != null) {
            getPrefs().setSleepScheduleId(sleepSchedule.getIdentifier());
        } else {
            getPrefs().setSleepScheduleId(null);
        }

        getPrefs().setSleepTransition(currentSleepTransition);

        finish();
    }

    private void onCancelClicked() {
        finish();
    }

}
