package com.recek.huewakeup.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.recek.huesleepwake.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

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
                LOG.debug("Stopping alarm.");
                stopAlarm();
            }
        });

        Button snoozeAlarmBtn = (Button) findViewById(R.id.snoozeAlarmBtn);
        snoozeAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snoozeAlarm();
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
        stopService(new Intent(this, AlarmSoundService.class));
        Toast.makeText(this, "Alarm stopped", Toast.LENGTH_SHORT).show();
    }

    private void snoozeAlarm() {
        LOG.debug("Snoozing alarm.");
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        if (alarmManager == null) {
            LOG.warn("Alarm manager is null. No snooze possible.");
            Toast.makeText(this, "Snooze failed!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, AlarmStartReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        // TODO: Make this choosable by settings.
        calendar.add(Calendar.MINUTE, 5);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Toast.makeText(this, "Snoozing 5 Minutes", Toast.LENGTH_SHORT).show();

        stopAlarm();
    }

    @Override
    protected void onDestroy() {
        stopAlarm();
        super.onDestroy();
    }
}
