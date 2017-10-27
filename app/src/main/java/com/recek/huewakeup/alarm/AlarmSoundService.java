package com.recek.huewakeup.alarm;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

import com.philips.lighting.data.HueSharedPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @since 2017-09-17.
 */
public class AlarmSoundService extends Service {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmSoundService.class);

    private Ringtone ringtone;
    private MediaPlayer mediaPlayer;
    private HueSharedPreferences prefs;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        prefs = HueSharedPreferences.getInstance(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        tryPlayingMediaPlayer();

        // Fallback plan
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                LOG.warn("Setting ringtone from notification");
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmUri);
            ringtone.play();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void tryPlayingMediaPlayer() {
        String alarmSoundUri = prefs.getAlarmSoundUri();

        if (alarmSoundUri != null) {
            Uri parsedUri = Uri.parse(alarmSoundUri);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            try {
                mediaPlayer.setDataSource(getApplicationContext(), parsedUri);
                mediaPlayer.prepare();
            } catch (IOException e) {
                LOG.warn("Saved alarm sound not found.");
                return;
            }

            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
        }
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
