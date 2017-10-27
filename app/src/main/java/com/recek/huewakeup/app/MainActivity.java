package com.recek.huewakeup.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.data.PHScheduleFix;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHSchedule;
import com.philips.lighting.quickstart.R;
import com.recek.huewakeup.settings.DaysSettingsActivity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * MainActivity - The starting point for creating your own Hue App.
 * Currently contains a simple view with a button to change your lights to random colours.  Remove this and add your own app implementation here! Have fun!
 *
 * @author SteveyO
 */
public class MainActivity extends Activity {

    private PHHueSDK phHueSDK;
    private HueSharedPreferences prefs;

    private final Map<String, PHScheduleFix> idToScheduleMap = new HashMap<>();

    private WakeTimeFragment wakeTimeFragment;
    private WakeUpScheduleFragment wakeUpFragment;
    private WakeEndScheduleFragment wakeEndFragment;
    private SleepScheduleFragment sleepFragment;
    private AlarmScheduleFragment alarmFragment;
    private TextView statusText;

    public HueSharedPreferences getPrefs() {
        return prefs;
    }

    public Map<String, PHScheduleFix> getIdToScheduleMap() {
        return idToScheduleMap;
    }

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
        FragmentManager fragmentManager = getFragmentManager();
        wakeTimeFragment = (WakeTimeFragment) fragmentManager.findFragmentById(R.id.wakeTimeFragment);
        wakeUpFragment = (WakeUpScheduleFragment) fragmentManager.findFragmentById(R.id.wakeUpFragment);
        wakeEndFragment = (WakeEndScheduleFragment) fragmentManager.findFragmentById(R.id.wakeEndFragment);
        sleepFragment = (SleepScheduleFragment) fragmentManager.findFragmentById(R.id.sleepFragment);
        alarmFragment = (AlarmScheduleFragment) fragmentManager.findFragmentById(R.id.alarmFragment);

        final Button updateSchedulesBtn = (Button) findViewById(R.id.updateSchedulesBtn);
        updateSchedulesBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSchedules();
            }
        });

        final Button daysBtn = (Button) findViewById(R.id.daysBtn);
        daysBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setWakeUpDays();
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

    private void updateSchedules() {
        statusText.setText(R.string.txt_status_updating);
        boolean updatedNothing = true;
        PHBridge bridge = phHueSDK.getSelectedBridge();

        Date wakeTime = wakeTimeFragment.onUpdate();
        if (wakeTime != null) {
            Date wakeUpDate = wakeUpFragment.updateWakeUpSchedule(bridge, wakeTime);
            if (wakeUpDate != null) {
                updatedNothing = false;
                wakeEndFragment.updateWakeUpEndSchedule(bridge, wakeTime);
            }

            if (sleepFragment.updateSleepSchedule(bridge)) {
                updatedNothing = false;
            }

            if (alarmFragment.updateAlarmSchedule(wakeTime)) {
                updatedNothing = false;
            }
        }

        if (updatedNothing) {
            statusText.setText(R.string.txt_status_updated_nothing);
        } else {
            statusText.setText(R.string.txt_status_update_finished);
        }
    }


    private void setWakeUpDays() {
        Intent startActivityIntent = new Intent(this, DaysSettingsActivity.class);
        startActivity(startActivityIntent);
    }

    @Override
    protected void onDestroy() {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        if (bridge != null) {

            if (phHueSDK.isHeartbeatEnabled(bridge)) {
                phHueSDK.disableHeartbeat(bridge);
            }

            phHueSDK.disconnect(bridge);
        }
        super.onDestroy();
    }
}
