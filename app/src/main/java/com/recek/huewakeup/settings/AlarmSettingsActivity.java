package com.recek.huewakeup.settings;

import android.content.Intent;
import android.database.Cursor;
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

public class AlarmSettingsActivity extends AppCompatActivity {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmSettingsActivity.class);
    private static final int PICK_AUDIO_REQUEST = 0;

    private HueSharedPreferences prefs;
    private TextView pickedSoundTxt;
    private TextView offsetTextView;
    private int currentOffset = 0;
    private String alarmSoundUriStr = null;

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
        String alarmSoundUriString = prefs.getAlarmSoundUri();
        if (alarmSoundUriString != null) {
            Uri alarmSoundUri = Uri.parse(alarmSoundUriString);
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

    private void onOkClicked() {
        if (alarmSoundUriStr != null) {
            prefs.setAlarmSoundUri(alarmSoundUriStr);
        }

        prefs.setAlarmTimeOffset(currentOffset);

        finish();
    }

    private void onCancelClicked() {
        finish();
    }
}
