package com.recek.huewakeup.app;

import android.app.Fragment;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.data.PHScheduleFix;
import com.philips.lighting.hue.listener.PHHTTPListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHSchedule;
import com.philips.lighting.quickstart.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @since 2017-09-14.
 */
public abstract class AbstractScheduleFragment extends Fragment {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractScheduleFragment.class);
    // TODO: locale
    protected static final SimpleDateFormat SDF_TIME = new SimpleDateFormat("HH:mm:ss", Locale.GERMANY);

    private static final PHScheduleFix NONE_SCHEDULE = new PHScheduleFix("-1", "NONE");
    private static final String TIME_FORMAT = "^((2[0-3]|1[0-9]|0[0-9]|[0-9])(:([0-5][0-9]|[0-9])){0,2})$";
    private static final String BLANK_WAKE_DAYS = "00000000";

    private HueSharedPreferences prefs;
    private Map<String, PHScheduleFix> idToScheduleMap;
    private PHHTTPListener putListener;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        prefs = activity.getPrefs();
        idToScheduleMap = activity.getIdToScheduleMap();
        putListener = createPutListener();
    }

    protected abstract TextView getStatusView();

    protected HueSharedPreferences getPrefs() {
        return prefs;
    }

    protected PHHTTPListener getPutListener() {
        return putListener;
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


    protected void setInitialStatus(String wakeUpScheduleId) {
        PHScheduleFix scheduleFix = idToScheduleMap.get(wakeUpScheduleId);
        if (scheduleFix != null) {
            PHSchedule schedule = scheduleFix.getSchedule();
            Date date = schedule.getDate();
            if (date != null && schedule.getStatus().equals(PHSchedule.PHScheduleStatus.ENABLED)) {
                getStatusView().setText(getResources().getString(R.string.txt_status_current_setting, SDF_TIME.format(date)));
                return;
            }
        }
        getStatusView().setText(R.string.txt_status_not_set);
    }

    protected Date updateSchedule(PHBridge bridge, PHScheduleFix scheduleFix, String timeStr,
                                  Calendar startCal, boolean useDayOfSchedule, PHHTTPListener putListener) {
        String id = scheduleFix.getId();
        LOG.info("Found alarm by name and id: {} / {}", scheduleFix.getName(), id);

        Date wakeTime = calculateRelativeTimeTo(startCal, timeStr);
        if (wakeTime == null) {
            return null;
        }

        String days = calculateWakeUpDays(wakeTime, useDayOfSchedule);
        if (days == null) {
            return null;
        }

        scheduleFix.setLocalTime(wakeTime);
        scheduleFix.setDays(days);
        scheduleFix.enable();
        final String json;
        try {
            json = scheduleFix.buildJson();
        } catch (JSONException e) {
            LOG.error("Could not build JSON. Error: {}", e.getMessage());
            Toast.makeText(getActivity(), "Error building request JSON", Toast.LENGTH_SHORT).show();
            return null;
        }

        LOG.info("Sending PUT to {} with {}", id, json);
        bridge.doHTTPPut(scheduleFix.getUrl(), json, putListener);
        return wakeTime;
    }

    protected boolean disableSchedule(PHBridge bridge, PHScheduleFix scheduleFix, PHHTTPListener putListener) {
        String id = scheduleFix.getId();
        LOG.info("Found alarm by name and id: {} / {}", scheduleFix.getName(), id);

        scheduleFix.disable();
        final String json;
        try {
            json = scheduleFix.buildJson();
        } catch (JSONException e) {
            LOG.error("Could not build JSON. Error: {}", e.getMessage());
            Toast.makeText(getActivity(), "Error building request JSON", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (json != null) {
            LOG.info("Sending PUT to {} with {}", id, json);
            bridge.doHTTPPut(scheduleFix.getUrl(), json, putListener);
            return true;
        }

        return false;
    }


    protected Date calculateRelativeTimeTo(Calendar cal, String timeStr) {
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

    private String calculateWakeUpDays(Date wakeTime, boolean useDayOfSchedule) {
        String wakeDays;
        if (useDayOfSchedule) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(wakeTime);
            // Converting SUN-SAT (1-7) to MON-SUN (1-7)
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
            if (dayOfWeek == 0) {
                dayOfWeek = 7;
            }
            StringBuilder sb = new StringBuilder(BLANK_WAKE_DAYS);
            sb.setCharAt(dayOfWeek, '1');
            wakeDays = sb.toString();
        } else {
            wakeDays = prefs.getWakeDaysRaw();
            wakeDays = "0" + wakeDays;
        }
        try {
            int wakeDaysHueFormat = Integer.parseInt(wakeDays, 2);
            return String.valueOf(wakeDaysHueFormat);
        } catch (NumberFormatException e) {
            LOG.error("Could not interpret saved days for wake up: {}.", e.getMessage());
            return null;
        }
    }

    private void notifyUser(final String msg) {
        getStatusView().setText(msg);
//        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private void notifyUser(final int msgId) {
        getStatusView().setText(msgId);
//        Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();
    }

    protected void notifyUserWithUiThread(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyUser(msg);
            }
        });
    }

    private PHHTTPListener createPutListener() {
        return new PHHTTPListener() {

            @Override
            public void onHTTPResponse(String jsonResponse) {
                LOG.info("RESPONSE: {}", jsonResponse);
                try {
                    JSONArray jsonArray = new JSONArray(jsonResponse);
                    // TODO: Evaluate all array items.
                    if (jsonArray.length() != 0) {
                        JSONObject firstResponseObj = jsonArray.getJSONObject(0);
                        String firstStatus = firstResponseObj.keys().next();
                        // TODO: Better status
                        if (firstStatus.contains("success")) {
                            notifyUserWithUiThread("Success");
                        } else {
                            notifyUserWithUiThread("Error");
                        }
                    }
                } catch (JSONException e) {
                    LOG.error("Could not read JSON response. Error: {}", e.getMessage());
                }
            }
        };
    }
}
