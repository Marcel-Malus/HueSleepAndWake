package com.philips.lighting.quickstart;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.philips.lighting.alarm.AlarmSoundService;
import com.philips.lighting.alarm.AlarmStartReceiver;
import com.philips.lighting.data.MyConst;

import java.util.Calendar;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.ALARM_SERVICE;
import static com.philips.lighting.quickstart.PHHomeActivity.TAG;

/**
 * @since 2017-09-14.
 */
public class AlarmScheduleFragment extends AbstractScheduleFragment {

    private static final int PICK_AUDIO_REQUEST = 0;

    private Switch alarmSwitch;
    private EditText inputTimeTxt;
    private TextView statusTxt;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private Uri alarmSoundUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.schedule_alarm, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Button pickAlarmBtn = (Button) getActivity().findViewById(R.id.pickAlarmSoundBtn);
        pickAlarmBtn.setOnClickListener(createPickAlarmListener());

        alarmSwitch = (Switch) getActivity().findViewById(R.id.alarmSwitch);
        alarmSwitch.setChecked(getPrefs().isAlarmActive());

        inputTimeTxt = (EditText) getActivity().findViewById(R.id.alarmTime);
        inputTimeTxt.setText(getPrefs().getAlarmTime());

        statusTxt = (TextView) getActivity().findViewById(R.id.alarmStatusTxt);

        alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
    }

    private View.OnClickListener createPickAlarmListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent audioIntent = new Intent();
                audioIntent.setType("audio/*");
                audioIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(audioIntent, PICK_AUDIO_REQUEST);
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            // error
            return;
        }
        if (requestCode == PICK_AUDIO_REQUEST) {
            alarmSoundUri = data.getData();
            Log.i(TAG, "Got alarmSoundUri: " + alarmSoundUri.getPath());
        }
    }

    @Override
    protected TextView getStatusView() {
        return statusTxt;
    }

    public boolean updateAlarmSchedule() {

        getPrefs().setAlarmActive(alarmSwitch.isChecked());
        if (!alarmSwitch.isChecked()) {
            return turnOffAlarm();
        }

        String alarmTimeStr = inputTimeTxt.getText().toString();
        Calendar cal = Calendar.getInstance();

        Date alarmDate = calculateRelativeTimeTo(cal, alarmTimeStr);
        if (alarmDate == null) {
            return false;
        }
        getPrefs().setAlarmTime(alarmTimeStr);

        Intent intent = new Intent(getActivity(), AlarmStartReceiver.class);
        if (alarmSoundUri != null) {
            intent.putExtra(MyConst.ALARM_SOUND_URI, alarmSoundUri);
        }
        pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmDate.getTime(), pendingIntent);
        // TODO: Change to every 24h:
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmDate.getTime(), 60000, pendingIntent);

        statusTxt.setText(R.string.txt_status_alarm_on);
        return true;
    }

    private boolean turnOffAlarm() {
        alarmManager.cancel(pendingIntent);
        //Stop the Media Player Service to stop sound
        getActivity().stopService(new Intent(getActivity(), AlarmSoundService.class));

        statusTxt.setText(R.string.txt_status_alarm_off);
        return true;
    }
}
