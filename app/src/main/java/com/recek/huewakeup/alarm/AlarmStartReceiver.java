package com.recek.huewakeup.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmStartReceiver extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmStartReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        LOG.info("Alarm broadcast received. Starting alarm sound and activity.");
        //Start sound service to play sound for alarm
        Intent startSoundIntent = new Intent(context, AlarmSoundService.class);
        context.startService(startSoundIntent);

        // Start alarm activity to enable user interaction
        Intent startActivityIntent = new Intent(context, AlarmActivity.class);
        startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startActivityIntent);
    }
}
