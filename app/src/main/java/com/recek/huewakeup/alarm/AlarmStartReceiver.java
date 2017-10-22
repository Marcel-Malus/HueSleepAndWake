package com.recek.huewakeup.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.philips.lighting.data.HueSharedPreferences;

import java.util.Calendar;
import java.util.List;

public class AlarmStartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        HueSharedPreferences prefs = HueSharedPreferences.getInstance(context);
        List<Boolean> wakeDays = prefs.getWakeDays();
        Calendar calendar = Calendar.getInstance();
        // Converting SUN-SAT (1-7) to MON-SUN (0-6)
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (dayOfWeek == -1) {
            dayOfWeek = 6;
        }

        if (wakeDays.size() > dayOfWeek && wakeDays.get(dayOfWeek)) {
            //Start sound service to play sound for alarm
            Intent startSoundIntent = new Intent(context, AlarmSoundService.class);
            context.startService(startSoundIntent);

            // Start alarm activity to enable user interaction
            Intent startActivityIntent = new Intent(context, AlarmActivity.class);
            startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startActivityIntent);
        }
    }
}
