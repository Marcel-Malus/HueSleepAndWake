package com.recek.huewakeup.app;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.quickstart.R;
import com.recek.huewakeup.alarm.AlarmSoundService;
import com.recek.huewakeup.alarm.AlarmStartReceiver;
import com.recek.huewakeup.settings.AlarmSettingsActivity;
import com.recek.huewakeup.util.MyDateUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;
import static com.recek.huewakeup.util.MyDateUtils.SDF_TIME;

/**
 * @since 2017-09-14.
 */
public class AlarmScheduleFragment extends Fragment {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmScheduleFragment.class);

    private Switch alarmSwitch;
    private TextView statusTxt;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private HueSharedPreferences prefs;

    private HueSharedPreferences getPrefs() {
        return prefs;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alarm, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prefs = HueSharedPreferences.getInstance(getActivity());

        final ImageButton settingsBtn = (ImageButton) getActivity().findViewById(R.id.alarmSettingsBtn);
        settingsBtn.setOnClickListener(createSettingsListener());

        alarmSwitch = (Switch) getActivity().findViewById(R.id.alarmSwitch);
        alarmSwitch.setChecked(getPrefs().isAlarmActive());

        statusTxt = (TextView) getActivity().findViewById(R.id.alarmStatusTxt);
        setInitialStatus();

        alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
    }

    private void setInitialStatus() {
        String alarmTime = getPrefs().getAlarmTime();
        if (alarmTime != null) {
            statusTxt.setText(getResources().getString(R.string.txt_status_current_setting, alarmTime));
        } else {
            statusTxt.setText(R.string.txt_status_not_set);
        }
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

    public boolean updateAlarmSchedule(Date wakeTime) {
        getPrefs().setAlarmActive(alarmSwitch.isChecked());
        if (!alarmSwitch.isChecked()) {
            return turnOffAlarm();
        }

        String alarmTimeRel = getPrefs().getAlarmTimeRelative();
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeTime);

        Date alarmDate = MyDateUtils.calculateRelativeTimeTo(cal, alarmTimeRel, false);
        if (alarmDate == null) {
            statusTxt.setText(R.string.txt_status_wrong_format);
            return false;
        }

        Intent intent = new Intent(getActivity(), AlarmStartReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmDate.getTime(), pendingIntent);
        // TODO: Change to every 24h:
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmDate.getTime(), 60000, pendingIntent);

        String alarmTimeStr = SDF_TIME.format(alarmDate);
        statusTxt.setText(getString(R.string.txt_status_alarm_on, alarmTimeStr));
        getPrefs().setAlarmTime(alarmTimeStr);
        LOG.debug("Setting sound alarm to: {}.", alarmTimeStr);
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

        getPrefs().setAlarmTime(null);
        statusTxt.setText(R.string.txt_status_alarm_off);
        LOG.debug("Turned off sound alarm.");
        return true;
    }
}
