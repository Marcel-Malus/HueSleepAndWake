package com.recek.huewakeup.alarm;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.data.HueSharedPreferences;
import com.recek.huesleepwake.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

import static com.recek.huewakeup.util.MyDateUtils.SDF_TIME_SHORT;

/**
 * Will be displayed, when the sound alarm goes on.
 */
public class AlarmActivity extends AppCompatActivity {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmActivity.class);

    private TextView alarmTxtStatus;
    private Button stopAlarmBtn;
    private PendingIntent pendingIntent;
    private boolean isAlarmStopped = false;
    private HueSharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("Creating alarm activity.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        prefs = HueSharedPreferences.getInstance(getApplicationContext());

        stopAlarmBtn = findViewById(R.id.stopAlarmBtn);
        stopAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAlarmStopped) {
                    closeAlarm();
                } else {
                    stopAlarm();
                }
            }
        });

        Button snoozeAlarmBtn = findViewById(R.id.snoozeAlarmBtn);
        snoozeAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snoozeAlarm();
            }
        });

        alarmTxtStatus = findViewById(R.id.alarmTxtStatus);
        alarmTxtStatus.setText(R.string.txt_alarm_status_active);

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(
                Context.KEYGUARD_SERVICE);
        //TODO: Whats the new way?
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
        alarmTxtStatus.setText(R.string.txt_alarm_status_stopped);
        stopAlarmBtn.setText(R.string.btn_close_alarm);
        stopAlarmSound();
    }

    private void closeAlarm() {
        LOG.debug("Closing alarm.");
        if (!isFinishing()) {
            if (pendingIntent != null) {
                showDismissSnoozeDialog();
            } else {
                finish();
            }
        }
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
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, prefs.getSnoozeTime());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // See: https://developer.android.com/training/monitoring-device-state/doze-standby
            alarmManager
                    .setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                            pendingIntent);
        } else {
            alarmManager
                    .setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
        alarmTxtStatus.setText(getResources().getString(R.string.txt_alarm_status_snooze,
                SDF_TIME_SHORT.format(calendar.getTime())));
        stopAlarmBtn.setText(R.string.btn_close_alarm);
        stopAlarmSound();
    }

    private void stopAlarmSound() {
        stopService(new Intent(this, AlarmSoundService.class));
        isAlarmStopped = true;
    }

    private void showDismissSnoozeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.txt_alarm_dismiss_alarm)
                .setPositiveButton(R.string.btn_dismiss, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LOG.debug("Dismissing snooze.");
                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        if (alarmManager != null && pendingIntent != null) {
                            alarmManager.cancel(pendingIntent);
                        }
                        finish();
                    }
                })
                .setNegativeButton(R.string.btn_keep, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LOG.debug("Keeping snooze.");
                        finish();
                    }
                })
                .setNeutralButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LOG.debug("Cancel close.");
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onStop() {
        LOG.debug("Stopping alarm activity");
        if (!isFinishing() && isAlarmStopped) {
            // This is necessary for snooze to close the "old" AlarmActivity.
            finish();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        LOG.debug("Destroying alarm activity");
        if (!isAlarmStopped) {
            stopService(new Intent(this, AlarmSoundService.class));
        }
        super.onDestroy();
    }
}
