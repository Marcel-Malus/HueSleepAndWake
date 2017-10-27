package com.recek.huewakeup.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.philips.lighting.data.PHScheduleFix;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.quickstart.R;

import java.util.Calendar;
import java.util.Date;

/**
 * @since 2017-09-14.
 */
public class WakeUpScheduleFragment extends AbstractScheduleFragment {

    private Spinner scheduleSpinner;
    private EditText inputTimeTxt;
    private TextView statusTxt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.schedule_wake_up, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        scheduleSpinner = (Spinner) getActivity().findViewById(R.id.wakeUpSpinner);
        String wakeUpScheduleId = getPrefs().getWakeScheduleId();
        buildAndAddAdapter(scheduleSpinner, wakeUpScheduleId);

        inputTimeTxt = (EditText) getActivity().findViewById(R.id.wakeLightTime);
        inputTimeTxt.setText(getPrefs().getWakeLightTime());

        statusTxt = (TextView) getActivity().findViewById(R.id.wakeLightStatusTxt);
        setInitialStatus(wakeUpScheduleId);
    }

    @Override
    protected TextView getStatusView() {
        return statusTxt;
    }

    public Date updateWakeUpSchedule(PHBridge bridge, Date wakeTime) {
        PHScheduleFix schedule = getSelectedValidSchedule(scheduleSpinner);
        if (schedule == null) {
            return null;
        }
        String wakeTimeStr = inputTimeTxt.getText().toString();
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeTime);

        Date wakeUpDate = updateSchedule(bridge, schedule, wakeTimeStr, cal, false, true);
        if (wakeUpDate != null) {
            getPrefs().setWakeLightTime(wakeTimeStr);
            getPrefs().setWakeScheduleId(schedule.getId());
        }
        return wakeUpDate;
    }
}
