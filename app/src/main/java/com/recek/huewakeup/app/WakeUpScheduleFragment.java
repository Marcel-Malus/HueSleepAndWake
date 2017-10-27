package com.recek.huewakeup.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.philips.lighting.data.PHScheduleFix;
import com.philips.lighting.quickstart.R;
import com.recek.huewakeup.settings.WakeLightSettingsActivity;

import java.util.Calendar;
import java.util.Date;

/**
 * @since 2017-09-14.
 */
public class WakeUpScheduleFragment extends AbstractScheduleFragment {

    private TextView statusTxt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.schedule_wake_up, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        statusTxt = (TextView) getActivity().findViewById(R.id.wakeLightStatusTxt);
        String wakeUpScheduleId = getPrefs().getWakeScheduleId();
        setInitialStatus(wakeUpScheduleId);

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
