package com.recek.huewakeup.settings;

import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.data.HueSharedPreferences;
import com.recek.huesleepwake.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AlarmSettingsActivity extends AppCompatActivity {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmSettingsActivity.class);
    private static final int PICK_AUDIO_REQUEST = 0;

    private HueSharedPreferences prefs;
    private MediaPlayer mediaPlayer;

    private TextView pickedSoundTxt;
    private TextView offsetTextView;
    private ImageButton playBtn;
    private TextView snoozeTextView;

    private int currentOffset = 0;
    private String alarmSoundUriStr = null;
    private boolean isAlarmPlaying = false;
    private int currentSnoozeTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_settings);
        prefs = HueSharedPreferences.getInstance(getApplicationContext());

        offsetTextView = findViewById(R.id.alarmTimeTxt);
        currentOffset = prefs.getAlarmTimeOffset();
        offsetTextView.setText(getString(R.string.txt_offset_sound_alarm, currentOffset));

        SeekBar offsetSeekBar = findViewById(R.id.alarmOffsetSeekBar);
        offsetSeekBar.setProgress(currentOffset);
        offsetSeekBar.setOnSeekBarChangeListener(createOffsetSeekBarListener());

        final ImageButton pickAlarmBtn = findViewById(R.id.pickAlarmSoundBtn);
        pickAlarmBtn.setOnClickListener(createPickAlarmListener());

        pickedSoundTxt = findViewById(R.id.pickedAlarmSoundTxt);
        initSavedSound();

        playBtn = findViewById(R.id.playAlarmSoundBtn);

        currentSnoozeTime = prefs.getSnoozeTime();
        snoozeTextView = findViewById(R.id.snoozeTimeTxt);
        snoozeTextView.setText(getString(R.string.txt_snooze_time, currentSnoozeTime));

        SeekBar snoozeTimeSeekBar = findViewById(R.id.snoozeTimeSeekBar);
        snoozeTimeSeekBar.setProgress(currentSnoozeTime);
        snoozeTimeSeekBar.setOnSeekBarChangeListener(createSnoozeSeekBarListener());

        // BUTTONS
        Button okButton = findViewById(R.id.alarmSettingsOkBtn);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOkClicked();
            }
        });

        Button cancelButton = findViewById(R.id.alarmSettingsCancelBtn);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelClicked();
            }
        });
    }

    private SeekBar.OnSeekBarChangeListener createSnoozeSeekBarListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentSnoozeTime = progress;
                snoozeTextView.setText(getString(R.string.txt_snooze_time, currentSnoozeTime));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
    }

    private SeekBar.OnSeekBarChangeListener createOffsetSeekBarListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentOffset = progress;
                offsetTextView.setText(getString(R.string.txt_offset_sound_alarm, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
    }


    private View.OnClickListener createPickAlarmListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent audioIntent = new Intent();
                audioIntent.setType("audio/*");
                audioIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                audioIntent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(audioIntent, PICK_AUDIO_REQUEST);
            }
        };
    }

    private void initSavedSound() {
        alarmSoundUriStr = prefs.getAlarmSoundUri();
        if (alarmSoundUriStr != null) {
            Uri alarmSoundUri = Uri.parse(alarmSoundUriStr);
            String fileName = extractFileName(alarmSoundUri);
            if (fileName != null) {
                pickedSoundTxt.setText(fileName);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            Toast.makeText(this, R.string.txt_status_error_pick, Toast.LENGTH_LONG).show();
            return;
        }
        if (requestCode == PICK_AUDIO_REQUEST) {
            Uri alarmSoundUri = data.getData();
            String fileName = extractFileName(alarmSoundUri);
            LOG.info("Got file: {}", fileName);

            grantUriPermission(getPackageName(), alarmSoundUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
            final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            // Check for the freshest data.
            //noinspection WrongConstant
            getContentResolver().takePersistableUriPermission(alarmSoundUri, takeFlags);
            alarmSoundUriStr = alarmSoundUri.toString();

            if (fileName != null) {
                pickedSoundTxt.setText(fileName);
            }
        }
    }

    private String extractFileName(Uri uri) {
        String fileName = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                cursor.moveToFirst();
                fileName = cursor.getString(nameIndex);
            }
        } catch (Exception e) {
            LOG.error("Could not read file name from uri.");
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return fileName;
    }

    public void onPlayClicked(View view) {
        if (isAlarmPlaying) {
            stopPlayingSound();
        } else if (alarmSoundUriStr != null) {
            LOG.info("Trying to play chosen media file.");
            Uri parsedUri = Uri.parse(alarmSoundUriStr);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            try {
                mediaPlayer.setDataSource(getApplicationContext(), parsedUri);
                mediaPlayer.prepare();
            } catch (IOException e) {
                LOG.warn("Failed loading alarm sound.");
                Toast.makeText(this, "Failed playing sound.", Toast.LENGTH_SHORT).show();
                return;
            }

            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.reset();
                    mp.release();
                    isAlarmPlaying = false;
                    playBtn.setImageResource(R.drawable.ic_play_orange);
                }
            });
            isAlarmPlaying = true;
            playBtn.setImageResource(R.drawable.ic_stop_orange);
        }
    }

    private void stopPlayingSound() {
        if (isAlarmPlaying) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
            }
            isAlarmPlaying = false;
            playBtn.setImageResource(R.drawable.ic_play_orange);
        }
    }

    private void onOkClicked() {
        if (alarmSoundUriStr != null) {
            prefs.setAlarmSoundUri(alarmSoundUriStr);
        }

        prefs.setAlarmTimeOffset(currentOffset);
        prefs.setSnoozeTime(currentSnoozeTime);

        finish();
    }

    private void onCancelClicked() {
        finish();
    }

    @Override
    protected void onStop() {
        stopPlayingSound();
        super.onStop();
    }
}
