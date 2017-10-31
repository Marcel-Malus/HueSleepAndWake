package com.recek.huewakeup.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.philips.lighting.quickstart.R;
import com.recek.huewakeup.alarm.AlarmSoundService;
import com.recek.huewakeup.alarm.AlarmStartReceiver;
import com.recek.huewakeup.settings.AlarmSettingsActivity;
import com.recek.huewakeup.util.MyDateUtils;

import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;
import static com.recek.huewakeup.util.MyDateUtils.SDF_TIME;

/**
 * @since 2017-09-14.
 */
public class AlarmScheduleFragment extends AbstractScheduleFragment {

    private Switch alarmSwitch;
    private TextView statusTxt;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.schedule_alarm, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ImageButton settingsBtn = (ImageButton) getActivity().findViewById(R.id.alarmSettingsBtn);
        settingsBtn.setOnClickListener(createSettingsListener());

        alarmSwitch = (Switch) getActivity().findViewById(R.id.alarmSwitch);
        alarmSwitch.setChecked(getPrefs().isAlarmActive());

        statusTxt = (TextView) getActivity().findViewById(R.id.alarmStatusTxt);

        alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
    }

    private View.OnClickListener createSettingsListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startActivityIntent = new Intent(getActivity(), AlarmSettingsActivity.class);
                getActivity().startActivity(startActivityIntent);
            }
        };
    }

    @Override
    protected TextView getStatusView() {
        return statusTxt;
    }

    public boolean updateAlarmSchedule(Date wakeTime) {
        getPrefs().setAlarmActive(alarmSwitch.isChecked());
        if (!alarmSwitch.isChecked()) {
            return turnOffAlarm();
        }

        String alarmTimeStr = getPrefs().getAlarmTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeTime);

        Date alarmDate = MyDateUtils.calculateRelativeTimeTo(cal, alarmTimeStr, false);
        if (alarmDate == null) {
            statusTxt.setText(R.string.txt_status_wrong_format);
            return false;
        }

        Intent intent = new Intent(getActivity(), AlarmStartReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmDate.getTime(), pendingIntent);
        // TODO: Change to every 24h:
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmDate.getTime(), 60000, pendingIntent);

        statusTxt.setText(getString(R.string.txt_status_alarm_on, SDF_TIME.format(alarmDate)));
        return true;
    }

    private boolean turnOffAlarm() {
        if (pendingIntent == null) {
            Intent intent = new Intent(getActivity(), AlarmStartReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);
        }
        alarmManager.cancel(pendingIntent);
        //Stop the Media Player Service to stop sound
        getActivity().stopService(new Intent(getActivity(), AlarmSoundService.class));

        statusTxt.setText(R.string.txt_status_alarm_off);
        return true;
    }
}
