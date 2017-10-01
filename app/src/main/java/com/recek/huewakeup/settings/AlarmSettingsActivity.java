package com.recek.huewakeup.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.quickstart.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmSettingsActivity extends Activity {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmSettingsActivity.class);
    private static final int PICK_AUDIO_REQUEST = 0;

    private HueSharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_settings);
        prefs = HueSharedPreferences.getInstance(getApplicationContext());

        final ImageButton pickAlarmBtn = (ImageButton) findViewById(R.id.pickAlarmSoundBtn);
        pickAlarmBtn.setOnClickListener(createPickAlarmListener());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            Toast.makeText(this, R.string.txt_status_error_pick, Toast.LENGTH_LONG).show();
            ;
            return;
        }
        if (requestCode == PICK_AUDIO_REQUEST) {
            Uri alarmSoundUri = data.getData();
            LOG.debug("Got alarmSoundUri: {}", alarmSoundUri.getPath());

            grantUriPermission(getPackageName(), alarmSoundUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            // Check for the freshest data.
            //noinspection WrongConstant
            getContentResolver().takePersistableUriPermission(alarmSoundUri, takeFlags);
            prefs.setAlarmSoundUri(alarmSoundUri.toString());

            // TODO: Update selected sound txt.
        }
    }
}
