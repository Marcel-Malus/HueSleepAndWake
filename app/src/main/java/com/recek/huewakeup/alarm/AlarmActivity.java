package com.recek.huewakeup.alarm;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.philips.lighting.quickstart.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Will be displayed, when the sound alarm goes on.
 */
public class AlarmActivity extends Activity {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("Creating alarm activity.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        Button stopAlarmBtn = (Button) findViewById(R.id.stopAlarmBtn);
        stopAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAlarm();
            }
        });

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
        keyguardLock.disableKeyguard();
        runOnUiThread(new Runnable() {
            public void run() {
                getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            }
        });
    }

    private void stopAlarm() {
        LOG.debug("Stopping alarm.");
        stopService(new Intent(this, AlarmSoundService.class));
        Toast.makeText(this, "Alarm stopped.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
