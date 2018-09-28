package com.recek.huewakeup.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.Schedule;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.ScheduleStatus;
import com.philips.lighting.quickstart.BridgeHolder;
import com.recek.huesleepwake.R;
import com.recek.huewakeup.settings.WakeLightSettingsActivity;
import com.recek.huewakeup.util.AbsoluteTime;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.recek.huesleepwake.R.id.lightSwitch;
import static com.recek.huewakeup.util.DefaultSchedules.DEFAULT_PRE_WAKE_UP_SCHEDULE_NAME;
import static com.recek.huewakeup.util.DefaultSchedules.DEFAULT_WAKE_END_SCHEDULE_NAME;
import static com.recek.huewakeup.util.DefaultSchedules.DEFAULT_WAKE_UP_SCHEDULE_NAME;

/**
 * @since 2017-09-14.
 */
public class WakeUpScheduleFragment extends AbstractScheduleFragment {

    private TextView statusTxt;
    private Switch wakeLightSwitch;

    @Override
    protected long getSavedTime() {
        return getPrefs().getWakeLightTime();
    }

    @Override
    protected TextView getStatusTxt() {
        return statusTxt;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wake_light, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        statusTxt = getActivity().findViewById(R.id.wakeLightStatusTxt);
        updateStatus();

        wakeLightSwitch = getActivity().findViewById(lightSwitch);
        wakeLightSwitch.setChecked(getPrefs().isWakeLightActive());

        final ImageButton settingsBtn = getActivity()
                .findViewById(R.id.lightSettingsBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startActivityIntent = new Intent(getActivity(),
                        WakeLightSettingsActivity.class);
                getActivity().startActivity(startActivityIntent);
            }
        });
    }

    @Override
    protected void onSuccess() {
        Schedule wakeSchedule = findScheduleById(getPrefs().getWakeScheduleId());
        Schedule wakeEndSchedule = findScheduleById(getPrefs().getWakeEndScheduleId());

        StringBuilder sb = new StringBuilder();
        boolean updated = false;
        if (wakeSchedule != null) {
            appendStatus(sb, wakeSchedule);
            updated = true;
        }
        if (updated && wakeEndSchedule != null) {
            appendStatus(sb, wakeEndSchedule);
        }
        if (!updated) {
            sb.append(getString(R.string.txt_status_updated_nothing));
        }
        statusTxt.setText(sb.toString());
    }

    @Override
    protected void onFailure() {
        statusTxt.setText(R.string.txt_status_update_failed);
    }

    private void appendStatus(StringBuilder sb, Schedule schedule) {
        if (schedule.getStatus() == ScheduleStatus.ENABLED) {
            String localTime = schedule.getLocalTime().toString();
            int startIdx = localTime.indexOf('T') + 1;
            String timeStr = localTime.substring(startIdx, startIdx + 5);
            if (sb.length() == 0) {
                sb.append(getString(R.string.txt_status_alarm_on, timeStr));
            } else {
                sb.append(" - ");
                sb.append(timeStr);
            }
        } else if (sb.length() == 0) {
            sb.append(getString(R.string.txt_status_alarm_off));
            getPrefs().setWakeLightTime(-1);
        }
    }

    public void updateWakeUpSchedule(Date wakeTime) {
        statusTxt.setText(getString(R.string.txt_status_updating));
        Schedule wakeSchedule = findScheduleById(getPrefs().getWakeScheduleId());
        if (wakeSchedule == null) {
            statusTxt.setText(getString(R.string.txt_status_no_schedule_found));
            return;
        }

        getPrefs().setWakeLightActive(wakeLightSwitch.isChecked());
        if (!wakeLightSwitch.isChecked()) {
            disableDefaultWakeUpSchedules();
            if (!wakeSchedule.getName().equals(DEFAULT_WAKE_UP_SCHEDULE_NAME)) {
                disableSchedule(wakeSchedule);
            }
            disableWakeEndSchedule();
            return;
        }

        AbsoluteTime absoluteTime = new AbsoluteTime(0, getPrefs().getWakeLightTimeOffset(), 0);
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeTime);

        Date wakeUpDate = updateSchedule(wakeSchedule, absoluteTime, cal, true);

        if (wakeUpDate != null) {
            if (wakeSchedule.getName().equals(DEFAULT_WAKE_UP_SCHEDULE_NAME)) {
                updatePreWakeUpSchedule(wakeUpDate);
            } else {
                disableDefaultWakeUpSchedules();
            }

            getPrefs().setWakeLightTime(wakeUpDate.getTime());
            updateWakeEndSchedule(wakeTime);
        }
    }

    private void disableDefaultWakeUpSchedules() {
        if (!BridgeHolder.hasBridge()) {
            return;
        }
        Bridge bridge = BridgeHolder.get();
        List<Schedule> scheduleList = bridge.getBridgeState().getSchedules();
        for (Schedule schedule : scheduleList) {
            if (schedule.getName().equals(DEFAULT_WAKE_UP_SCHEDULE_NAME)) {
                disableSchedule(schedule);
            } else if (schedule.getName().equals(DEFAULT_PRE_WAKE_UP_SCHEDULE_NAME)) {
                disableSchedule(schedule);
            }
        }
    }

    private void updatePreWakeUpSchedule(Date wakeUpDate) {
        Schedule wakeSchedule = findScheduleById(getPrefs().getPreWakeScheduleId());
        if (wakeSchedule == null) {
            return;
        }
        AbsoluteTime absoluteTime = new AbsoluteTime(0, 0, 10);
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeUpDate);

        updateSchedule(wakeSchedule, absoluteTime, cal, true);
    }

    private void updateWakeEndSchedule(Date wakeTime) {
        Schedule wakeEndSchedule = findScheduleById(getPrefs().getWakeEndScheduleId());
        if (wakeEndSchedule == null) {
            return;
        }

        AbsoluteTime absoluteTime = new AbsoluteTime(0, getPrefs().getWakeEndTimeOffset(), 0);
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeTime);

        updateSchedule(wakeEndSchedule, absoluteTime, cal, false);

        if (!wakeEndSchedule.getName().equals(DEFAULT_WAKE_END_SCHEDULE_NAME)) {
            disableDefaultWakeEndSchedule();
        }
    }

    private void disableWakeEndSchedule() {
        Schedule wakeEndSchedule = findScheduleById(getPrefs().getWakeEndScheduleId());
        if (wakeEndSchedule == null) {
            return;
        }
        disableDefaultWakeEndSchedule();
        if (!wakeEndSchedule.getName().equals(DEFAULT_WAKE_END_SCHEDULE_NAME)) {
            disableSchedule(wakeEndSchedule);
        }
    }

    private void disableDefaultWakeEndSchedule() {
        if (!BridgeHolder.hasBridge()) {
            return;
        }
        Bridge bridge = BridgeHolder.get();
        List<Schedule> scheduleList = bridge.getBridgeState().getSchedules();
        for (Schedule schedule : scheduleList) {
            if (schedule.getName().equals(DEFAULT_WAKE_END_SCHEDULE_NAME)) {
                disableSchedule(schedule);
                return;
            }
        }
    }


}
