package com.recek.huewakeup.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.wrapper.domain.resource.Schedule;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.ScheduleStatus;
import com.recek.huesleepwake.R;
import com.recek.huewakeup.settings.WakeLightSettingsActivity;

import java.util.Calendar;
import java.util.Date;

import static com.recek.huesleepwake.R.id.lightSwitch;

/**
 * @since 2017-09-14.
 */
public class WakeUpScheduleFragment extends AbstractScheduleFragment {

    private TextView statusTxt;
    private Switch wakeLightSwitch;
    private Schedule wakeSchedule;
    private Schedule wakeEndSchedule;

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
        wakeSchedule = findScheduleById(getPrefs().getWakeScheduleId());
        wakeEndSchedule = findScheduleById(getPrefs().getWakeEndScheduleId());

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
        } else {
            sb.append(getString(R.string.txt_status_alarm_off));
            getPrefs().setWakeLightTime(-1);
        }
    }

    public void updateWakeUpSchedule(Date wakeTime) {
        wakeSchedule = findScheduleById(getPrefs().getWakeScheduleId());
        if (wakeSchedule == null) {
            return;
        }
        statusTxt.setText(getString(R.string.txt_status_updating));

        getPrefs().setWakeLightActive(wakeLightSwitch.isChecked());
        if (!wakeLightSwitch.isChecked()) {
            disableSchedule(wakeSchedule);
            return;
        }

        String wakeTimeStr = getPrefs().getRelativeWakeLightTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeTime);

        Date wakeUpDate = updateSchedule(wakeSchedule, wakeTimeStr, cal, true);

        if (wakeUpDate != null) {
            getPrefs().setWakeLightTime(wakeUpDate.getTime());
            // TODO: evaluate wakeEnd update.
            updateWakeEndSchedule(wakeTime);
        }
    }

    private boolean updateWakeEndSchedule(Date wakeTime) {
        wakeEndSchedule = findScheduleById(getPrefs().getWakeEndScheduleId());
        if (wakeEndSchedule == null) {
            return false;
        }
        String wakeEndTimeStr = getPrefs().getWakeEndTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeTime);

        Date wakeEndDate = updateSchedule(wakeEndSchedule, wakeEndTimeStr, cal, false);
        return wakeEndDate != null;
    }
}
