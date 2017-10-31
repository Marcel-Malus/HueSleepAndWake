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
import com.philips.lighting.quickstart.R;
import com.recek.huewakeup.settings.WakeLightSettingsActivity;

import java.util.Calendar;
import java.util.Date;

import static com.philips.lighting.quickstart.R.id.lightSwitch;

/**
 * @since 2017-09-14.
 */
public class WakeUpScheduleFragment extends AbstractScheduleFragment {

    private TextView statusTxt;
    private Switch wakeLightSwitch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wake_light, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        statusTxt = (TextView) getActivity().findViewById(R.id.wakeLightStatusTxt);
        String wakeUpScheduleId = getPrefs().getWakeScheduleId();
        setInitialStatus(wakeUpScheduleId);

        wakeLightSwitch = (Switch) getActivity().findViewById(lightSwitch);
        wakeLightSwitch.setChecked(getPrefs().isWakeLightActive());

        final ImageButton settingsBtn = (ImageButton) getActivity().findViewById(R.id.lightSettingsBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startActivityIntent = new Intent(getActivity(), WakeLightSettingsActivity.class);
                getActivity().startActivity(startActivityIntent);
            }
        });
    }

    @Override
    protected TextView getStatusView() {
        return statusTxt;
    }

    public boolean updateWakeUpSchedule(Date wakeTime) {
        PHScheduleFix schedule = findScheduleById(getPrefs().getWakeScheduleId());
        if (schedule == null) {
            return false;
        }

        getPrefs().setWakeLightActive(wakeLightSwitch.isChecked());
        if (!wakeLightSwitch.isChecked()) {
            return disableSchedule(schedule);
        }

        String wakeTimeStr = getPrefs().getWakeLightTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeTime);

        Date wakeUpDate = updateSchedule(schedule, wakeTimeStr, cal, false, true);

        if (wakeUpDate != null) {
            // TODO: evaluate wakeEnd update.
            updateWakeEndSchedule(wakeTime);
            return true;
        }

        return false;
    }

    private boolean updateWakeEndSchedule(Date wakeDate) {
        PHScheduleFix schedule = findScheduleById(getPrefs().getWakeEndScheduleId());
        if (schedule == null) {
            return false;
        }
        String wakeTimeStr = getPrefs().getWakeEndTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeDate);

        Date wakeEndDate = updateSchedule(schedule, wakeTimeStr, cal, false, false);
        return wakeEndDate != null;
    }
}
