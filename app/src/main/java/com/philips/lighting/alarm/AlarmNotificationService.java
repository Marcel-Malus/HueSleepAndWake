package com.philips.lighting.alarm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.philips.lighting.quickstart.MyApplicationActivity;
import com.philips.lighting.quickstart.R;

/**
 * @since 2017-09-17.
 */
public class AlarmNotificationService extends IntentService {

    private NotificationManager alarmNotificationManager;

    //Notification ID for Alarm
    public static final int NOTIFICATION_ID = 1;

    public AlarmNotificationService() {
        super("AlarmNotificationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        alarmNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        // get pending intent for click
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MyApplicationActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        // get pending intent for swipe / dismiss
        PendingIntent deleteIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0,
                new Intent(this, AlarmStopReceiver.class), 0);

        //Create notification
        // TODO: implement notification channel and add to builder
        String notificationText = getString(R.string.notification_alarm_txt);
        NotificationCompat.Builder alarmNotificationBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Alarm")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                        .setContentText(notificationText)
                        .setAutoCancel(true);
        alarmNotificationBuilder.setContentIntent(contentIntent);
        alarmNotificationBuilder.setDeleteIntent(deleteIntent);

        //notify notification manager about new notification
        alarmNotificationManager.notify(NOTIFICATION_ID, alarmNotificationBuilder.build());
    }

}
