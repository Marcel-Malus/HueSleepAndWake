package com.philips.lighting.quickstart;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.data.PHScheduleFix;
import com.philips.lighting.hue.listener.PHHTTPListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHSchedule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MyApplicationActivity - The starting point for creating your own Hue App.
 * Currently contains a simple view with a button to change your lights to random colours.  Remove this and add your own app implementation here! Have fun!
 *
 * @author SteveyO
 */
public class MyApplicationActivity extends Activity {

    private static final String TAG = "HueWakeUpApp";
    private static final String TIME_FORMAT = "^((2[0-3]|1[0-9]|0[0-9]|[0-9])(:([0-5][0-9]|[0-9])){0,2})$";
    private static final String ONE_MINUTE = "0:1";
    private static final PHScheduleFix NONE_SCHEDULE = new PHScheduleFix("-1", "NONE");

    private PHHueSDK phHueSDK;
    private HueSharedPreferences prefs;

    private final Map<String, PHScheduleFix> idToScheduleMap = new HashMap<>();

    private TextView statusText;
    private EditText wakeTime;
    private EditText wakeEndTime;
    private Switch sleepSwitch;
    private Spinner wakeUpScheduleSpinner;
    private Spinner wakeEndScheduleSpinner;
    private Spinner sleepScheduleSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // INIT
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_main);
        phHueSDK = PHHueSDK.create();
        prefs = HueSharedPreferences.getInstance(getApplicationContext());
        boolean isConnected = getIntent().getBooleanExtra("isConnected", true);
        // TODO: rePopulate onResume?
        populateAvailableSchedules(isConnected);

        // WIDGETS
        initWakeUpWidgets();
        initWakeEndWidgets();
        initSleepWidgets();

        // UPDATE
        final Button alarmBtn = (Button) findViewById(R.id.setAlarmBtn);
        alarmBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAlarm();
            }
        });

        statusText = (TextView) findViewById(R.id.statusText);
    }

    private void populateAvailableSchedules(boolean isConnected) {
        if (isConnected) {
            PHBridge bridge = phHueSDK.getSelectedBridge();
            Map<String, PHSchedule> scheduleMap = bridge.getResourceCache().getSchedules();
            for (Map.Entry<String, PHSchedule> scheduleEntry : scheduleMap.entrySet()) {
                PHScheduleFix scheduleFix = new PHScheduleFix(scheduleEntry.getValue(),
                        bridge.getResourceCache().getBridgeConfiguration());
                idToScheduleMap.put(scheduleEntry.getKey(), scheduleFix);
            }
        } else {
            idToScheduleMap.put("-2", new PHScheduleFix("-2", "Aufwachen"));
            idToScheduleMap.put("-3", new PHScheduleFix("-3", "Wecker aus"));
            idToScheduleMap.put("-4", new PHScheduleFix("-4", "Schlafen"));
        }
    }

    private void initWakeUpWidgets() {
        wakeUpScheduleSpinner = (Spinner) findViewById(R.id.wakeUpSpinner);
        String wakeUpScheduleId = prefs.getWakeScheduleId();
        buildAndAddAdapter(wakeUpScheduleSpinner, wakeUpScheduleId);

        wakeTime = (EditText) findViewById(R.id.wakeTime);
        wakeTime.setText(prefs.getWakeTime());
    }

    private void initWakeEndWidgets() {
        wakeEndScheduleSpinner = (Spinner) findViewById(R.id.wakeEndSpinner);
        String wakeEndScheduleId = prefs.getWakeEndScheduleId();
        buildAndAddAdapter(wakeEndScheduleSpinner, wakeEndScheduleId);

        wakeEndTime = (EditText) findViewById(R.id.wakeEndTime);
        wakeEndTime.setText(prefs.getWakeEndTime());
    }

    private void initSleepWidgets() {
        sleepScheduleSpinner = (Spinner) findViewById(R.id.sleepSpinner);
        String sleepScheduleId = prefs.getSleepScheduleId();
        buildAndAddAdapter(sleepScheduleSpinner, sleepScheduleId);

        sleepSwitch = (Switch) findViewById(R.id.sleepSwitch);
        sleepSwitch.setChecked(prefs.isSleepActive());
    }

    private void buildAndAddAdapter(Spinner scheduleSpinner, String selectedScheduleId) {
        List<PHScheduleFix> schedules = new ArrayList<>();
        schedules.add(NONE_SCHEDULE);
        schedules.addAll(idToScheduleMap.values());
        ArrayAdapter<PHScheduleFix> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, schedules);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scheduleSpinner.setAdapter(adapter);
        int selectedIdx = schedules.indexOf(idToScheduleMap.get(selectedScheduleId));
        if (selectedIdx != -1) {
            scheduleSpinner.setSelection(selectedIdx);
        }
    }

    private void updateAlarm() {
        statusText.setText(R.string.txt_status_updating);
        boolean updatedNothing = true;
        PHBridge bridge = phHueSDK.getSelectedBridge();

        PHScheduleFix wakeUpSchedule = getSelectedValidSchedule(wakeUpScheduleSpinner);
        if (wakeUpSchedule != null) {
            Date wakeUpDate = updateWakeUpSchedule(bridge, wakeUpSchedule);
            updatedNothing = false;
            PHScheduleFix wakeEndSchedule = getSelectedValidSchedule(wakeEndScheduleSpinner);
            if (wakeUpDate != null && wakeEndSchedule != null) {
                updateWakeUpEndSchedule(bridge, wakeEndSchedule, wakeUpDate);
            }
        }

        PHScheduleFix sleepSchedule = getSelectedValidSchedule(sleepScheduleSpinner);
        if (sleepSchedule != null && sleepSwitch.isChecked()) {
            updateSleepSchedule(bridge, sleepSchedule);
            updatedNothing = false;
        }

        if (updatedNothing) {
            statusText.setText(R.string.txt_status_updated_nothing);
        }
    }

    private PHScheduleFix getSelectedValidSchedule(Spinner scheduleSpinner) {
        PHScheduleFix schedule = (PHScheduleFix) scheduleSpinner.getSelectedItem();
        return schedule != null && !schedule.getId().startsWith("-") ? schedule : null;
    }

    private Date updateWakeUpSchedule(PHBridge bridge, PHScheduleFix schedule) {
        String wakeTimeStr = wakeTime.getText().toString();
        Calendar cal = Calendar.getInstance();

        Date wakeUpDate = updateSchedule(bridge, schedule, wakeTimeStr, cal, putListenerWakeUp);
        if (wakeUpDate != null) {
            prefs.setWakeTime(wakeTimeStr);
            prefs.setWakeScheduleId(schedule.getId());
        }
        return wakeUpDate;
    }

    private boolean updateWakeUpEndSchedule(PHBridge bridge, PHScheduleFix schedule, Date wakeDate) {
        String wakeTimeStr = wakeEndTime.getText().toString();
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeDate);

        Date wakeEndDate = updateSchedule(bridge, schedule, wakeTimeStr, cal, putListenerSlim);
        if (wakeEndDate != null) {
            prefs.setWakeEndTime(wakeTimeStr);
            prefs.setWakeEndScheduleId(schedule.getId());
            return true;
        }
        return false;
    }

    private void updateSleepSchedule(PHBridge bridge, PHScheduleFix schedule) {
        updateSchedule(bridge, schedule, ONE_MINUTE,Calendar.getInstance(), putListenerSlim);
        prefs.setSleepActive(sleepSwitch.isChecked());
        prefs.setSleepScheduleId(schedule.getId());
    }

    private Date updateSchedule(PHBridge bridge, PHScheduleFix scheduleFix, String timeStr,
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
            Toast.makeText(this, "Error building request JSON", Toast.LENGTH_SHORT).show();
            return null;
        }

        Log.i(TAG, "Sending PUT to " + id + " with " + json);
        bridge.doHTTPPut(scheduleFix.getUrl(), json, putListener);
        return wakeTime;
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
                    notifyUserWithUiThread(firstResponseObj.toString());
                }
            } catch (JSONException e) {
                Log.e(TAG, "Could not read JSON response. Error: " + e.getMessage());
            }
        }
    };

    PHHTTPListener putListenerSlim = new PHHTTPListener() {

        @Override
        public void onHTTPResponse(String jsonResponse) {
            Log.i(TAG, "RESPONSE-END : " + jsonResponse);
        }
    };

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
        statusText.setText(msg);
        Toast.makeText(MyApplicationActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void notifyUser(final int msgId) {
        statusText.setText(msgId);
        Toast.makeText(MyApplicationActivity.this, msgId, Toast.LENGTH_SHORT).show();
    }

    private void notifyUserWithUiThread(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyUser(msg);
            }
        });
    }

    @Override
    protected void onDestroy() {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        if (bridge != null) {

            if (phHueSDK.isHeartbeatEnabled(bridge)) {
                phHueSDK.disableHeartbeat(bridge);
            }

            phHueSDK.disconnect(bridge);
            super.onDestroy();
        }
    }
}
