package com.philips.lighting.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmStopReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Stop the Media Player Service to stop sound
        context.stopService(new Intent(context, AlarmSoundService.class));
    }
}
