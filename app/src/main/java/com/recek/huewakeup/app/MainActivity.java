package com.recek.huewakeup.app;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnection;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateCacheType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedEvent;
import com.philips.lighting.hue.sdk.wrapper.connection.ConnectionEvent;
import com.philips.lighting.hue.sdk.wrapper.connection.HeartbeatManager;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.HueError;
import com.philips.lighting.quickstart.BridgeHolder;
import com.philips.lighting.quickstart.MainHueActivity;
import com.recek.huesleepwake.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * MainActivity - The starting point for the HueSleepWake app.
 *
 * @author Marcel Hrnecek
 */
public class MainActivity extends AppCompatActivity {

    private static final Logger LOG = LoggerFactory.getLogger(MainActivity.class);

    private WakeTimeFragment wakeTimeFragment;
    private WakeUpScheduleFragment wakeUpFragment;
    private SleepScheduleFragment sleepFragment;
    private AlarmScheduleFragment alarmFragment;
    private TextView statusText;
    private boolean isConnected = false;

    @Override
    protected void onRestart() {
        super.onRestart();

        LOG.info("onRestart. Checking Connection...");
        statusText.setText(R.string.txt_status_checking_connection);
        BridgeConnection connection = BridgeHolder.get().getBridgeConnection(BridgeConnectionType.LOCAL);
        HeartbeatManager heartbeatManager = connection.getHeartbeatManager();
        heartbeatManager.performOneHeartbeat(BridgeStateCacheType.BRIDGE_CONFIG);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // INIT
        LOG.info("Creating main activity.");
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_main);
        isConnected = getIntent().getBooleanExtra("isConnected", false);


        // WIDGETS
        FragmentManager fragmentManager = getFragmentManager();
        wakeTimeFragment = (WakeTimeFragment) fragmentManager
                .findFragmentById(R.id.wakeTimeFragment);
        wakeUpFragment = (WakeUpScheduleFragment) fragmentManager
                .findFragmentById(R.id.wakeUpFragment);
        sleepFragment = (SleepScheduleFragment) fragmentManager
                .findFragmentById(R.id.sleepFragment);
        alarmFragment = (AlarmScheduleFragment) fragmentManager
                .findFragmentById(R.id.alarmFragment);

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
        BridgeHolder.get().addBridgeStateUpdatedCallback(bridgeStateUpdatedCallback);
        BridgeHolder.get().setBridgeConnectionCallback(bridgeConnectionCallback);
        updateConnectionStatus();
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
        Date wakeTime = wakeTimeFragment.onUpdate();
        if (wakeTime != null) {
            wakeUpFragment.updateWakeUpSchedule(wakeTime);
            sleepFragment.updateSleepSchedule();
            alarmFragment.updateAlarmSchedule(wakeTime);
        }
    }

    private void reconnect() {
        statusText.setText(R.string.txt_status_reconnecting);
        Intent intent = new Intent(this, MainHueActivity.class);
        startActivity(intent);
    }

    private void updateConnectionStatus() {
        if (isConnected) {
            statusText.setText(R.string.txt_status_connected);
        } else {
            statusText.setText(R.string.txt_status_not_connected);
        }
    }

    private void updateConnectionStatusOnUiThread() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateConnectionStatus();
            }
        });
    }

    @Override
    protected void onStop() {
        statusText.setText(R.string.txt_status_stopped);
        super.onStop();
    }

    /**
     * The callback the receives bridge state update events
     */
    private BridgeStateUpdatedCallback bridgeStateUpdatedCallback = new BridgeStateUpdatedCallback() {
        @Override
        public void onBridgeStateUpdated(Bridge bridge,
                                         BridgeStateUpdatedEvent bridgeStateUpdatedEvent) {
            LOG.info("Bridge state updated event: " + bridgeStateUpdatedEvent);

            switch (bridgeStateUpdatedEvent) {
                case UNKNOWN:
                    LOG.warn("Unknown bridge state.");
                    // Is that so?
                    isConnected = false;
                    break;
                default:
                    isConnected = true;
                    break;
            }

            updateConnectionStatusOnUiThread();
        }
    };

    /**
     * The callback that receives bridge connection events
     */
    private BridgeConnectionCallback bridgeConnectionCallback = new BridgeConnectionCallback() {
        @Override
        public void onConnectionEvent(BridgeConnection bridgeConnection,
                                      ConnectionEvent connectionEvent) {
            LOG.info("Connection event: " + connectionEvent);

            switch (connectionEvent) {
                case CONNECTED:
                case CONNECTION_RESTORED:
                    isConnected = true;
                    break;
                default:
                    isConnected = false;
                    break;
            }

            updateConnectionStatusOnUiThread();
        }


        @Override
        public void onConnectionError(BridgeConnection bridgeConnection, List<HueError> list) {
            for (HueError error : list) {
                LOG.error("Connection error: " + error.toString());
            }
        }
    };
}
