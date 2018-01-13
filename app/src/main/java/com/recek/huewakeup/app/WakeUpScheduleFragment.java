package com.recek.huewakeup.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.philips.lighting.data.PHScheduleFix;
import com.recek.huesleepwake.R;
import com.recek.huewakeup.settings.WakeLightSettingsActivity;

import java.util.Calendar;
import java.util.Date;

import static com.recek.huesleepwake.R.id.lightSwitch;
import static com.recek.huewakeup.util.MyDateUtils.SDF_TIME_SHORT;

/**
 * @since 2017-09-14.
 */
public class WakeUpScheduleFragment extends AbstractScheduleFragment {

    private TextView statusTxt;
    private Switch wakeLightSwitch;
    private PHScheduleFix wakeSchedule;
    private PHScheduleFix wakeEndSchedule;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wake_light, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        statusTxt = getActivity().findViewById(R.id.wakeLightStatusTxt);
        String wakeUpScheduleId = getPrefs().getWakeScheduleId();
        setInitialStatus(wakeUpScheduleId);

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
    protected TextView getStatusView() {
        return statusTxt;
    }

    @Override
    protected void onSuccess() {
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

    private void appendStatus(StringBuilder sb, PHScheduleFix schedule) {
        if (schedule.isEnabled()) {
            String timeStr = SDF_TIME_SHORT.format(schedule.getLocalTime());
            if (sb.length() == 0) {
                sb.append(getString(R.string.txt_status_alarm_on, timeStr));
            } else {
                sb.append(" - ");
                sb.append(timeStr);
            }
        } else {
            sb.append(getString(R.string.txt_status_alarm_off));
        }
    }

    public boolean updateWakeUpSchedule(Date wakeTime) {
        wakeSchedule = findScheduleById(getPrefs().getWakeScheduleId());
        if (wakeSchedule == null) {
            return false;
        }
        statusTxt.setText(getString(R.string.txt_status_updating));

        getPrefs().setWakeLightActive(wakeLightSwitch.isChecked());
        if (!wakeLightSwitch.isChecked()) {
            return disableSchedule(wakeSchedule);
        }

        String wakeTimeStr = getPrefs().getWakeLightTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeTime);

        Date wakeUpDate = updateSchedule(wakeSchedule, wakeTimeStr, cal, true);

        if (wakeUpDate != null) {
            // TODO: evaluate wakeEnd update.
            updateWakeEndSchedule(wakeTime);
            return true;
        }

        return false;
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
