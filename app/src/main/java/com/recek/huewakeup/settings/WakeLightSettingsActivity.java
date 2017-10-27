package com.recek.huewakeup.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.data.PHScheduleFix;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHSchedule;
import com.philips.lighting.quickstart.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WakeLightSettingsActivity extends Activity {

    private static final PHScheduleFix NONE_SCHEDULE = new PHScheduleFix("-1", "NONE");

    private final Map<String, PHScheduleFix> idToScheduleMap = new HashMap<>();

    private HueSharedPreferences prefs;
    private PHHueSDK phHueSDK;

    private EditText wakeInputTimeTxt;
    private Spinner wakeScheduleSpinner;
    private EditText wakeEndInputTimeTxt;
    private Spinner wakeEndScheduleSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wake_light_settings);

        // INIT

        prefs = HueSharedPreferences.getInstance(getApplicationContext());
        phHueSDK = PHHueSDK.create();
        populateAvailableSchedules();

        // WAKE UP

        wakeScheduleSpinner = (Spinner) findViewById(R.id.wakeLightSpinner);
        String wakeUpScheduleId = prefs.getWakeScheduleId();
        buildAndAddAdapter(wakeScheduleSpinner, wakeUpScheduleId);

        wakeInputTimeTxt = (EditText) findViewById(R.id.wakeLightTime);
        wakeInputTimeTxt.setText(prefs.getWakeLightTime());

        // WAKE END

        wakeEndScheduleSpinner = (Spinner) findViewById(R.id.wakeLightEndSpinner);
        String wakeEndScheduleId = prefs.getWakeEndScheduleId();
        buildAndAddAdapter(wakeEndScheduleSpinner, wakeEndScheduleId);

        wakeEndInputTimeTxt = (EditText) findViewById(R.id.wakeLightEndTime);
        wakeEndInputTimeTxt.setText(prefs.getWakeEndTime());

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

    private void populateAvailableSchedules() {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        if (bridge != null) {
            Map<String, PHSchedule> scheduleMap = bridge.getResourceCache().getSchedules();
            for (Map.Entry<String, PHSchedule> scheduleEntry : scheduleMap.entrySet()) {
                PHScheduleFix scheduleFix = new PHScheduleFix(scheduleEntry.getValue(),
                        bridge.getResourceCache().getBridgeConfiguration());
                idToScheduleMap.put(scheduleEntry.getKey(), scheduleFix);
            }
        } else {
            idToScheduleMap.put("-2", new PHScheduleFix("-2", "Aufwachen"));
            idToScheduleMap.put("-3", new PHScheduleFix("-3", "Wecker aus"));
            idToScheduleMap.put("-4", new PHScheduleFix("-4", "Schlafen"));
        }
    }

    private void buildAndAddAdapter(Spinner scheduleSpinner, String selectedScheduleId) {
        List<PHScheduleFix> schedules = new ArrayList<>();
        schedules.add(NONE_SCHEDULE);
        schedules.addAll(idToScheduleMap.values());
        ArrayAdapter<PHScheduleFix> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, schedules);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleSpinner.setAdapter(adapter);
        int selectedIdx = schedules.indexOf(idToScheduleMap.get(selectedScheduleId));
        if (selectedIdx != -1) {
            scheduleSpinner.setSelection(selectedIdx);
        }
    }

    private void onOkClicked() {
        //TODO: Validation
        prefs.setWakeLightTime(wakeInputTimeTxt.getText().toString());
        PHScheduleFix wakeSchedule = getSelectedValidSchedule(wakeScheduleSpinner);
        if (wakeSchedule != null) {
            prefs.setWakeScheduleId(wakeSchedule.getId());
        }

        prefs.setWakeEndTime(wakeEndInputTimeTxt.getText().toString());
        PHScheduleFix wakeEndSchedule = getSelectedValidSchedule(wakeEndScheduleSpinner);
        if (wakeEndSchedule != null) {
            prefs.setWakeEndScheduleId(wakeEndSchedule.getId());
        }

        finish();
    }

    private PHScheduleFix getSelectedValidSchedule(Spinner scheduleSpinner) {
        PHScheduleFix schedule = (PHScheduleFix) scheduleSpinner.getSelectedItem();
        return schedule != null && !schedule.getId().startsWith("-") ? schedule : null;
    }


    private void onCancelClicked() {
        finish();
    }

    @Override
    protected void onDestroy() {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        if (bridge != null) {

            if (phHueSDK.isHeartbeatEnabled(bridge)) {
                phHueSDK.disableHeartbeat(bridge);
            }

            phHueSDK.disconnect(bridge);
        }
        super.onDestroy();
    }
}
