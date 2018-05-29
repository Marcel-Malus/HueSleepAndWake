package com.recek.huewakeup.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.Schedule;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.builder.ScheduleBuilder;
import com.philips.lighting.quickstart.BridgeHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.recek.huewakeup.util.DefaultSchedules.DEFAULT_SCHEDULE_NAME;

public abstract class AbstractHueSettingsActivity extends AppCompatActivity {

    private static final Schedule NONE_SCHEDULE = new ScheduleBuilder().setIdentifier("-1").setName("NONE").build();

    private final Map<String, Schedule> idToScheduleMap = new HashMap<>();

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
        if (BridgeHolder.hasBridge()) {
            List<Schedule> scheduleList = BridgeHolder.get().getBridgeState().getSchedules();
            for (Schedule schedule : scheduleList) {
                idToScheduleMap.put(schedule.getIdentifier(), schedule);
            }
        } else {
            idToScheduleMap.put("-2",
                    new ScheduleBuilder().setIdentifier("-2").setName("Wake up").build());
            idToScheduleMap.put("-3",
                    new ScheduleBuilder().setIdentifier("-3").setName("Wake end").build());
            idToScheduleMap
                    .put("-4", new ScheduleBuilder().setIdentifier("-4").setName("Sleep").build());
        }
    }

    protected void buildAndAddAdapter(Spinner scheduleSpinner, String selectedScheduleId,
                                      String defaultSchedule) {
        List<ScheduleListItem> schedules = new ArrayList<>();
        schedules.add(new ScheduleListItem(NONE_SCHEDULE));
        for (Schedule schedule : idToScheduleMap.values()) {
            if (schedule.getName().startsWith(DEFAULT_SCHEDULE_NAME) && !schedule.getName()
                    .equals(defaultSchedule)) {
                continue;
            }
            schedules.add(new ScheduleListItem(schedule));
        }

        ArrayAdapter<ScheduleListItem> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, schedules);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleSpinner.setAdapter(adapter);
        scheduleSpinner.setSelection(findListItemIdx(schedules, selectedScheduleId));
    }

    private int findListItemIdx(List<ScheduleListItem> schedules, String scheduleId) {
        int noneIdx = 0;
        for (int i = 0; i < schedules.size(); i++) {
            String identifier = schedules.get(i).getSchedule().getIdentifier();
            if (Objects.equals(identifier, scheduleId)) {
                return i;
            }
            if (Objects.equals(identifier, "-1")) {
                noneIdx = i;
            }
        }
        return noneIdx;
    }

    protected Schedule getSelectedValidSchedule(Spinner scheduleSpinner) {
        ScheduleListItem si = (ScheduleListItem) scheduleSpinner.getSelectedItem();
        return si != null && !si.getSchedule().getIdentifier().startsWith("-") ? si.getSchedule() : null;
    }

    private class ScheduleListItem {

        private Schedule schedule;

        ScheduleListItem(Schedule schedule) {
            this.schedule = schedule;
        }

        public Schedule getSchedule() {
            return schedule;
        }

        @Override
        public String toString() {
            return schedule.getName();
        }
    }
}
