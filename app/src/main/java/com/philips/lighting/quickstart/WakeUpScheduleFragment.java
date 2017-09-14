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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import static com.philips.lighting.quickstart.PHHomeActivity.TAG;

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

        inputTimeTxt = (EditText) getActivity().findViewById(R.id.wakeTime);
        inputTimeTxt.setText(getPrefs().getWakeTime());

        statusTxt = (TextView) getActivity().findViewById(R.id.wakeUpStatusTxt);
        setInitialStatus(wakeUpScheduleId, statusTxt);
    }

    public Date updateWakeUpSchedule(PHBridge bridge) {
        PHScheduleFix schedule = getSelectedValidSchedule(scheduleSpinner);
        if (schedule == null) {
            return null;
        }
        String wakeTimeStr = inputTimeTxt.getText().toString();
        Calendar cal = Calendar.getInstance();

        Date wakeUpDate = updateSchedule(bridge, schedule, wakeTimeStr, cal, putListenerWakeUp);
        if (wakeUpDate != null) {
            getPrefs().setWakeTime(wakeTimeStr);
            getPrefs().setWakeScheduleId(schedule.getId());
        }
        return wakeUpDate;
    }

    PHHTTPListener putListenerWakeUp = new PHHTTPListener() {

        @Override
        public void onHTTPResponse(String jsonResponse) {
            Log.i(TAG, "RESPONSE: " + jsonResponse);
            try {
                JSONArray jsonArray = new JSONArray(jsonResponse);
                // TODO: Evaluate all array items.
                if (jsonArray.length() != 0) {
                    JSONObject firstResponseObj = jsonArray.getJSONObject(0);
//                    String firstStatus = firstResponseObj.keys().next();
//                    notifyUserWithUiThread(firstResponseObj.toString());
                }
            } catch (JSONException e) {
                Log.e(TAG, "Could not read JSON response. Error: " + e.getMessage());
            }
        }
    };


}
