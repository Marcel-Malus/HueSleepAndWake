package com.recek.huewakeup.settings;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.data.PHScheduleFix;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.Schedule;
import com.philips.lighting.quickstart.BridgeHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractHueSettingsActivity extends Activity {

    private static final PHScheduleFix NONE_SCHEDULE = new PHScheduleFix("-1", "NONE");

    private final Map<String, PHScheduleFix> idToScheduleMap = new HashMap<>();

    private HueSharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = HueSharedPreferences.getInstance(getApplicationContext());
        populateAvailableSchedules();
    }

    protected HueSharedPreferences getPrefs() {
        return prefs;
    }

    private void populateAvailableSchedules() {
        Bridge bridge = BridgeHolder.get();
        if (bridge != null) {
            List<Schedule> scheduleList = bridge.getBridgeState().getSchedules();
            for (Schedule schedule : scheduleList) {
                PHScheduleFix scheduleFix = new PHScheduleFix(schedule,
                        bridge.getBridgeConfiguration());
                idToScheduleMap.put(schedule.getIdentifier(), scheduleFix);
            }
        } else {
            idToScheduleMap.put("-2", new PHScheduleFix("-2", "Aufwachen"));
            idToScheduleMap.put("-3", new PHScheduleFix("-3", "Wecker aus"));
            idToScheduleMap.put("-4", new PHScheduleFix("-4", "Schlafen"));
        }
    }

    protected void buildAndAddAdapter(Spinner scheduleSpinner, String selectedScheduleId) {
        List<PHScheduleFix> schedules = new ArrayList<>();
        schedules.add(NONE_SCHEDULE);
        schedules.addAll(idToScheduleMap.values());
        ArrayAdapter<PHScheduleFix> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, schedules);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleSpinner.setAdapter(adapter);
        if (selectedScheduleId == null) {
            scheduleSpinner.setSelection(schedules.indexOf(NONE_SCHEDULE));
        } else {
            int selectedIdx = schedules.indexOf(idToScheduleMap.get(selectedScheduleId));
            if (selectedIdx != -1) {
                scheduleSpinner.setSelection(selectedIdx);
            }
        }
    }

    protected PHScheduleFix getSelectedValidSchedule(Spinner scheduleSpinner) {
        PHScheduleFix schedule = (PHScheduleFix) scheduleSpinner.getSelectedItem();
        return schedule != null && !schedule.getId().startsWith("-") ? schedule : null;
    }
}
