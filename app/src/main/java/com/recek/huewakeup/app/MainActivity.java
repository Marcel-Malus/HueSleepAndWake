package com.recek.huewakeup.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.quickstart.PHHomeActivity;
import com.philips.lighting.util.PHUtil;
import com.recek.huesleepwake.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * MainActivity - The starting point for the HueSleepWake app.
 *
 * @author Marcel Hrnecek
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
    private boolean isConnected = false;

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
        if (!isConnected && !phHueSDK.isAccessPointConnected(PHUtil.loadLastAccessPointConnected(prefs))) {
            LOG.warn("Connection to access point lost.");
            statusText.setText(R.string.txt_status_not_connected);
        } else {
            statusText.setText(R.string.txt_status_connected);
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
        isConnected = getIntent().getBooleanExtra("isConnected", false);


        // WIDGETS
        FragmentManager fragmentManager = getFragmentManager();
        wakeTimeFragment = (WakeTimeFragment) fragmentManager.findFragmentById(R.id.wakeTimeFragment);
        wakeUpFragment = (WakeUpScheduleFragment) fragmentManager.findFragmentById(R.id.wakeUpFragment);
        sleepFragment = (SleepScheduleFragment) fragmentManager.findFragmentById(R.id.sleepFragment);
        alarmFragment = (AlarmScheduleFragment) fragmentManager.findFragmentById(R.id.alarmFragment);

        final Button updateSchedulesBtn = findViewById(R.id.updateSchedulesBtn);
        updateSchedulesBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSchedules();
            }
        });

        final Button reconnectBtn = findViewById(R.id.reconnectBtn);
        reconnectBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                reconnect();
            }
        });

        statusText = findViewById(R.id.statusText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_help:
                startHelpActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startHelpActivity() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
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
        statusText.setText(R.string.txt_status_reconnecting);
//        PHAccessPoint lastAccessPoint = PHUtil.loadLastAccessPointConnected(prefs);
//        if (lastAccessPoint != null && !phHueSDK.isAccessPointConnected(lastAccessPoint)) {
//            phHueSDK.connect(lastAccessPoint);
//            statusText.setText(R.string.txt_status_connected);
//        } else {
//            statusText.setText(R.string.txt_status_reconnect_failed);
//        }

        Intent intent = new Intent(this, PHHomeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        isConnected = false;
        statusText.setText(R.string.txt_status_paused);
        super.onPause();
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
