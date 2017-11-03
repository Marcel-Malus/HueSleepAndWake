package com.recek.huewakeup.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.philips.lighting.data.PHScheduleFix;
import com.philips.lighting.quickstart.R;
import com.recek.huewakeup.settings.SleepLightSettingsActivity;

import java.util.Calendar;

/**
 * @since 2017-09-14.
 */
public class SleepScheduleFragment extends AbstractScheduleFragment {

    private static final String ONE_MINUTE = "0:1";

    private Switch sleepSwitch;
    private TextView statusTxt;
    private PHScheduleFix schedule;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sleep_light, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        statusTxt = (TextView) getActivity().findViewById(R.id.sleepStatusTxt);
        String sleepScheduleId = getPrefs().getSleepScheduleId();
        setInitialStatus(sleepScheduleId);

        sleepSwitch = (Switch) getActivity().findViewById(R.id.sleepSwitch);
        sleepSwitch.setChecked(getPrefs().isSleepActive());

        final ImageButton settingsBtn = (ImageButton) getActivity().findViewById(R.id.sleepLightSettingsBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startActivityIntent = new Intent(getActivity(), SleepLightSettingsActivity.class);
                getActivity().startActivity(startActivityIntent);
            }
        });
    }

    @Override
    protected TextView getStatusView() {
        return statusTxt;
    }

    @Override
    protected void onSuccess() {
        StringBuilder sb = new StringBuilder();
        if (schedule != null) {
            if (schedule.isEnabled()) {
                sb.append(getString(R.string.txt_status_alarm_on, "Now"));
            } else {
                sb.append(getString(R.string.txt_status_alarm_off));
            }
        } else {
            sb.append(getString(R.string.txt_status_updated_nothing));
        }
        statusTxt.setText(sb.toString());
    }

    public boolean updateSleepSchedule() {
        schedule = findScheduleById(getPrefs().getSleepScheduleId());
        if (schedule == null) {
            return false;
        }

        if (!sleepSwitch.isChecked()) {
            getPrefs().setSleepActive(false);
            return disableSchedule(schedule);
        }

        updateSchedule(schedule, ONE_MINUTE, Calendar.getInstance(), true, false);
        getPrefs().setSleepActive(true);
        return true;
    }
}
