package com.recek.huewakeup.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.recek.huesleepwake.R;
import com.recek.huewakeup.alarm.AlarmSoundService;
import com.recek.huewakeup.alarm.AlarmStartReceiver;
import com.recek.huewakeup.settings.AlarmSettingsActivity;
import com.recek.huewakeup.util.AbsoluteTime;
import com.recek.huewakeup.util.MyDateUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;
import static com.recek.huewakeup.util.MyDateUtils.SDF_TIME_SHORT;

/**
 * @since 2017-09-14.
 */
public class AlarmScheduleFragment extends AbstractBasicFragment {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmScheduleFragment.class);

    private Switch alarmSwitch;
    private TextView statusTxt;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    @Override
    protected long getSavedTime() {
        return getPrefs().getAlarmTime();
    }

    @Override
    protected TextView getStatusTxt() {
        return statusTxt;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alarm, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ImageButton settingsBtn = getActivity().findViewById(R.id.alarmSettingsBtn);
        settingsBtn.setOnClickListener(createSettingsListener());

        alarmSwitch = getActivity().findViewById(R.id.alarmSwitch);
        alarmSwitch.setChecked(getPrefs().isAlarmActive());

        statusTxt = getActivity().findViewById(R.id.alarmStatusTxt);
        updateStatus();

        alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatus();
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

    public void updateAlarmSchedule(Date wakeTime) {
        getPrefs().setAlarmActive(alarmSwitch.isChecked());
        if (!alarmSwitch.isChecked()) {
            turnOffAlarm();
            return;
        }

        AbsoluteTime absoluteTime = new AbsoluteTime(0, getPrefs().getAlarmTimeOffset(), 0);
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeTime);

        Date alarmDate = MyDateUtils.calculateRelativeTimeTo(cal, absoluteTime, false);
        if (alarmDate == null) {
            statusTxt.setText(R.string.txt_status_wrong_format);
            return;
        }

        Intent intent = new Intent(getActivity(), AlarmStartReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // See: https://developer.android.com/training/monitoring-device-state/doze-standby
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmDate.getTime(),
                    pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmDate.getTime(), pendingIntent);
        }

        String alarmTimeStr = SDF_TIME_SHORT.format(alarmDate);
        statusTxt.setText(getString(R.string.txt_status_alarm_on, alarmTimeStr));
        getPrefs().setAlarmTime(alarmDate.getTime());
        LOG.debug("Setting sound alarm to: {}.", alarmTimeStr);
    }

    private void turnOffAlarm() {
        if (pendingIntent == null) {
            Intent intent = new Intent(getActivity(), AlarmStartReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);
        }
        alarmManager.cancel(pendingIntent);
        //Stop the Media Player Service to stop sound
        getActivity().stopService(new Intent(getActivity(), AlarmSoundService.class));

        getPrefs().setAlarmTime(-1);
        statusTxt.setText(R.string.txt_status_alarm_off);
        LOG.debug("Turned off sound alarm.");
    }
}
