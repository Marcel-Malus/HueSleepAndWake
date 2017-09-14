package com.philips.lighting.quickstart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.philips.lighting.data.PHScheduleFix;
import com.philips.lighting.hue.listener.PHHTTPListener;
import com.philips.lighting.model.PHBridge;

import java.util.Calendar;

import static com.philips.lighting.quickstart.PHHomeActivity.TAG;

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
        setInitialStatus(sleepScheduleId, statusTxt);
    }

    public boolean updateSleepSchedule(PHBridge bridge) {
        PHScheduleFix schedule = getSelectedValidSchedule(scheduleSpinner);
        if (schedule == null) {
            return false;
        }

        if (!sleepSwitch.isChecked()) {
            // TODO: disable schedule.
        }

        updateSchedule(bridge, schedule, ONE_MINUTE, Calendar.getInstance(), putListenerSlim);
        getPrefs().setSleepActive(sleepSwitch.isChecked());
        getPrefs().setSleepScheduleId(schedule.getId());
        return true;
    }


    PHHTTPListener putListenerSlim = new PHHTTPListener() {

        @Override
        public void onHTTPResponse(String jsonResponse) {
            Log.i(TAG, "RESPONSE-END : " + jsonResponse);
        }
    };
}
