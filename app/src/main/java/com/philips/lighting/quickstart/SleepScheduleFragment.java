package com.philips.lighting.quickstart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.philips.lighting.data.PHScheduleFix;
import com.philips.lighting.model.PHBridge;

import java.util.Calendar;

/**
 * @since 2017-09-14.
 */
public class SleepScheduleFragment extends AbstractScheduleFragment {

    private static final String ONE_MINUTE = "0:1";

    private Spinner scheduleSpinner;
    private Switch sleepSwitch;
    private TextView statusTxt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.schedule_sleep, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        scheduleSpinner = (Spinner) getActivity().findViewById(R.id.sleepSpinner);
        String sleepScheduleId = getPrefs().getSleepScheduleId();
        buildAndAddAdapter(scheduleSpinner, sleepScheduleId);

        sleepSwitch = (Switch) getActivity().findViewById(R.id.sleepSwitch);
        sleepSwitch.setChecked(getPrefs().isSleepActive());

        statusTxt = (TextView) getActivity().findViewById(R.id.sleepStatusTxt);
        setInitialStatus(sleepScheduleId);
    }

    @Override
    protected TextView getStatusView() {
        return statusTxt;
    }

    public boolean updateSleepSchedule(PHBridge bridge) {
        PHScheduleFix schedule = getSelectedValidSchedule(scheduleSpinner);
        if (schedule == null) {
            return false;
        }

        if (!sleepSwitch.isChecked()) {
            getPrefs().setSleepActive(false);
            return disableSchedule(bridge, schedule, getPutListener());
        }

        updateSchedule(bridge, schedule, ONE_MINUTE, Calendar.getInstance(), getPutListener());
        getPrefs().setSleepActive(true);
        getPrefs().setSleepScheduleId(schedule.getId());
        return true;
    }
}
