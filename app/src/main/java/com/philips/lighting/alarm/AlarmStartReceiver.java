package com.philips.lighting.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmStartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Start sound service to play sound for alarm
        context.startService(new Intent(context, AlarmSoundService.class));

        // Start alarm activity to enable user interaction
        Intent startActivityIntent = new Intent(context, AlarmActivity.class);
        startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startActivityIntent);
    }
}
