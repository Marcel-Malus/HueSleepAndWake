package com.philips.lighting.alarm;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.philips.lighting.data.MyConst;

/**
 * @since 2017-09-17.
 */
public class AlarmSoundService extends Service {

    private Ringtone ringtone;
    private MediaPlayer mediaPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Uri alarmSoundUri = intent.getParcelableExtra(MyConst.ALARM_SOUND_URI);

        if (alarmSoundUri != null) {
            mediaPlayer = MediaPlayer.create(this, alarmSoundUri);
            mediaPlayer.start();
        } else {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                Log.i("AlarmReceiver", "Setting ringtone from notification");
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmUri);
            ringtone.play();
        }

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }

        super.onDestroy();
    }
}
