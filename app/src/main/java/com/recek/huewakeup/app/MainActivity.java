package com.recek.huewakeup.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.quickstart.PHHomeActivity;
import com.philips.lighting.quickstart.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * MainActivity - The starting point for creating your own Hue App.
 * Currently contains a simple view with a button to change your lights to random colours.  Remove this and add your own app implementation here! Have fun!
 *
 * @author SteveyO
 */
public class MainActivity extends Activity {

    private static final Logger LOG = LoggerFactory.getLogger(MainActivity.class);

    private PHHueSDK phHueSDK;
    private HueSharedPreferences prefs;

    private WakeTimeFragment wakeTimeFragment;
    private WakeUpScheduleFragment wakeUpFragment;
    private SleepScheduleFragment sleepFragment;
    private AlarmScheduleFragment alarmFragment;
    private TextView statusText;

    public HueSharedPreferences getPrefs() {
        return prefs;
    }

    public PHBridge getBridge() {
        PHBridge bridge = phHueSDK.getSelectedBridge();
        if (bridge == null) {
            LOG.warn("Bridge not found!");
        }
        return bridge;
    }

    @Override
    protected void onResume() {
        LOG.debug("Resuming main activity.");
        super.onResume();
        if (getBridge() == null) {
            LOG.info("Connection to bridge lost. Reconnecting...");
            reconnect();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // INIT
        LOG.info("Creating main activity.");
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_main);
        phHueSDK = PHHueSDK.create();
        prefs = HueSharedPreferences.getInstance(getApplicationContext());

        // WIDGETS
        FragmentManager fragmentManager = getFragmentManager();
        wakeTimeFragment = (WakeTimeFragment) fragmentManager.findFragmentById(R.id.wakeTimeFragment);
        wakeUpFragment = (WakeUpScheduleFragment) fragmentManager.findFragmentById(R.id.wakeUpFragment);
        sleepFragment = (SleepScheduleFragment) fragmentManager.findFragmentById(R.id.sleepFragment);
        alarmFragment = (AlarmScheduleFragment) fragmentManager.findFragmentById(R.id.alarmFragment);

        final Button updateSchedulesBtn = (Button) findViewById(R.id.updateSchedulesBtn);
        updateSchedulesBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSchedules();
            }
        });

        final Button reconnectBtn = (Button) findViewById(R.id.reconnectBtn);
        reconnectBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                reconnect();
            }
        });

        statusText = (TextView) findViewById(R.id.statusText);
    }

    private void updateSchedules() {
        statusText.setText(R.string.txt_status_updating);
        boolean updated = false;

        Date wakeTime = wakeTimeFragment.onUpdate();
        if (wakeTime != null) {
            updated = wakeUpFragment.updateWakeUpSchedule(wakeTime);
            updated = sleepFragment.updateSleepSchedule() || updated;
            updated = alarmFragment.updateAlarmSchedule(wakeTime) || updated;
        }

        if (updated) {
            statusText.setText(R.string.txt_status_update_finished);
        } else {
            statusText.setText(R.string.txt_status_updated_nothing);
        }
    }

    private void reconnect() {
        Toast.makeText(this, "Reconnecting...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, PHHomeActivity.class);
        startActivity(intent);
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
