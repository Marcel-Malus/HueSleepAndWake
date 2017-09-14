package com.philips.lighting.quickstart;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.data.PHScheduleFix;
import com.philips.lighting.hue.listener.PHHTTPListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHSchedule;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.philips.lighting.quickstart.PHHomeActivity.TAG;

/**
 * @since 2017-09-14.
 */
public abstract class AbstractScheduleFragment extends Fragment {

    // TODO: locale
    protected static final SimpleDateFormat SDF_TIME = new SimpleDateFormat("HH:mm:ss", Locale.GERMANY);

    private static final PHScheduleFix NONE_SCHEDULE = new PHScheduleFix("-1", "NONE");
    private static final String TIME_FORMAT = "^((2[0-3]|1[0-9]|0[0-9]|[0-9])(:([0-5][0-9]|[0-9])){0,2})$";

    private HueSharedPreferences prefs;
    private Map<String, PHScheduleFix> idToScheduleMap;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MyApplicationActivity activity = (MyApplicationActivity) getActivity();
        prefs = activity.getPrefs();
        idToScheduleMap = activity.getIdToScheduleMap();
    }

    protected HueSharedPreferences getPrefs() {
        return prefs;
    }

    protected void buildAndAddAdapter(Spinner scheduleSpinner, String selectedScheduleId) {
        List<PHScheduleFix> schedules = new ArrayList<>();
        schedules.add(NONE_SCHEDULE);
        schedules.addAll(idToScheduleMap.values());
        ArrayAdapter<PHScheduleFix> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, schedules);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleSpinner.setAdapter(adapter);
        int selectedIdx = schedules.indexOf(idToScheduleMap.get(selectedScheduleId));
        if (selectedIdx != -1) {
            scheduleSpinner.setSelection(selectedIdx);
        }
    }


    protected PHScheduleFix getSelectedValidSchedule(Spinner scheduleSpinner) {
        PHScheduleFix schedule = (PHScheduleFix) scheduleSpinner.getSelectedItem();
        return schedule != null && !schedule.getId().startsWith("-") ? schedule : null;
    }


    protected void setInitialStatus(String wakeUpScheduleId, TextView statusTxt) {
        PHScheduleFix scheduleFix = idToScheduleMap.get(wakeUpScheduleId);
        if (scheduleFix != null) {
            PHSchedule schedule = scheduleFix.getSchedule();
            Date date = schedule.getDate();
            if (date != null && schedule.getStatus().equals(PHSchedule.PHScheduleStatus.ENABLED)) {
                statusTxt.setText(getResources().getString(R.string.txt_status_current_setting, SDF_TIME.format(date)));
                return;
            }
        }
        statusTxt.setText(R.string.txt_status_not_set);
    }

    protected Date updateSchedule(PHBridge bridge, PHScheduleFix scheduleFix, String timeStr,
                                  Calendar startCal, PHHTTPListener putListener) {
        String id = scheduleFix.getId();
        Log.i(TAG, "Found alarm by name and id: " + scheduleFix.getName() + " / " + id);

        Date wakeTime = calculateRelativeTimeTo(startCal, timeStr);
        if (wakeTime == null) {
            return null;
        }
        scheduleFix.setLocalTime(wakeTime);
        scheduleFix.enable();
        final String json;
        try {
            json = scheduleFix.buildJson();
        } catch (JSONException e) {
            Log.e(TAG, "Could not build JSON. Error: " + e.getMessage());
            Toast.makeText(getActivity(), "Error building request JSON", Toast.LENGTH_SHORT).show();
            return null;
        }

        Log.i(TAG, "Sending PUT to " + id + " with " + json);
        bridge.doHTTPPut(scheduleFix.getUrl(), json, putListener);
        return wakeTime;
    }


    private Date calculateRelativeTimeTo(Calendar cal, String timeStr) {
        if (!timeStr.matches(TIME_FORMAT)) {
            notifyUser(R.string.txt_status_no_time_format);
            return null;
        }

        String[] timeParts = timeStr.split(":");
        int hours = Integer.valueOf(timeParts[0]);
        int minutes = timeParts.length == 2 ? Integer.valueOf(timeParts[1]) : 0;
        int seconds = timeParts.length == 3 ? Integer.valueOf(timeParts[2]) : 0;

        cal.add(Calendar.SECOND, seconds);
        cal.add(Calendar.MINUTE, minutes);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        return cal.getTime();
    }


    private void notifyUser(final String msg) {
//        statusText.setText(msg);
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private void notifyUser(final int msgId) {
//        statusText.setText(msgId);
        Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();
    }

    private void notifyUserWithUiThread(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyUser(msg);
            }
        });
    }
}
