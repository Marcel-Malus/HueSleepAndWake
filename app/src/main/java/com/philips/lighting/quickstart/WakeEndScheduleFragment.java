package com.philips.lighting.quickstart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.philips.lighting.data.PHScheduleFix;
import com.philips.lighting.hue.listener.PHHTTPListener;
import com.philips.lighting.model.PHBridge;

import java.util.Calendar;
import java.util.Date;

import static com.philips.lighting.quickstart.PHHomeActivity.TAG;

/**
 * @since 2017-09-14.
 */
public class WakeEndScheduleFragment extends AbstractScheduleFragment {

    private Spinner scheduleSpinner;
    private EditText inputTimeTxt;
    private TextView statusTxt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.schedule_wake_end, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        scheduleSpinner = (Spinner) getActivity().findViewById(R.id.wakeEndSpinner);
        String wakeEndScheduleId = getPrefs().getWakeEndScheduleId();
        buildAndAddAdapter(scheduleSpinner, wakeEndScheduleId);

        inputTimeTxt = (EditText) getActivity().findViewById(R.id.wakeEndTime);
        inputTimeTxt.setText(getPrefs().getWakeEndTime());

        statusTxt = (TextView) getActivity().findViewById(R.id.wakeEndStatusTxt);
        setInitialStatus(wakeEndScheduleId, statusTxt);
    }


    public boolean updateWakeUpEndSchedule(PHBridge bridge, Date wakeDate) {
        PHScheduleFix schedule = getSelectedValidSchedule(scheduleSpinner);
        if (schedule == null) {
            return false;
        }
        String wakeTimeStr = inputTimeTxt.getText().toString();
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeDate);

        Date wakeEndDate = updateSchedule(bridge, schedule, wakeTimeStr, cal, putListenerSlim);
        if (wakeEndDate != null) {
            getPrefs().setWakeEndTime(wakeTimeStr);
            getPrefs().setWakeEndScheduleId(schedule.getId());
            return true;
        }
        return false;
    }


    PHHTTPListener putListenerSlim = new PHHTTPListener() {

        @Override
        public void onHTTPResponse(String jsonResponse) {
            Log.i(TAG, "RESPONSE-END : " + jsonResponse);
        }
    };
}
