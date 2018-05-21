package com.recek.huewakeup.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.wrapper.domain.resource.Schedule;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.ScheduleStatus;
import com.recek.huesleepwake.R;
import com.recek.huewakeup.settings.SleepLightSettingsActivity;
import com.recek.huewakeup.util.AbsoluteTime;

import java.util.Calendar;

/**
 * @since 2017-09-14.
 */
public class SleepScheduleFragment extends AbstractScheduleFragment {

    private Switch sleepSwitch;
    private TextView statusTxt;

    @Override
    protected long getSavedTime() {
        // Sleep time will not be saved.
        return -1;
    }

    @Override
    protected TextView getStatusTxt() {
        return statusTxt;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sleep_light, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sleepSwitch = getActivity().findViewById(R.id.sleepSwitch);
        boolean isSleepActive = getPrefs().isSleepActive();
        sleepSwitch.setChecked(isSleepActive);

        statusTxt = getActivity().findViewById(R.id.sleepStatusTxt);
        if (isSleepActive) {
            statusTxt.setText(R.string.txt_status_outdated);
        } else {
            statusTxt.setText(R.string.txt_status_disabled);
        }

        final ImageButton settingsBtn = getActivity().findViewById(R.id.sleepLightSettingsBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startActivityIntent = new Intent(getActivity(),
                        SleepLightSettingsActivity.class);
                getActivity().startActivity(startActivityIntent);
            }
        });
    }

    @Override
    protected void onSuccess() {
        Schedule schedule = findScheduleById(getPrefs().getSleepScheduleId());
        StringBuilder sb = new StringBuilder();
        if (schedule != null) {
            if (ScheduleStatus.ENABLED.equals(schedule.getStatus())) {
                sb.append(getString(R.string.txt_status_alarm_on, "Now"));
            } else {
                sb.append(getString(R.string.txt_status_alarm_off));
            }
        } else {
            sb.append(getString(R.string.txt_status_updated_nothing));
        }
        statusTxt.setText(sb.toString());
    }

    @Override
    protected void onFailure() {
        statusTxt.setText(R.string.txt_status_update_failed);
    }

    public void updateSleepSchedule() {
        statusTxt.setText(getString(R.string.txt_status_updating));
        Schedule schedule = findScheduleById(getPrefs().getSleepScheduleId());
        if (schedule == null) {
            statusTxt.setText(getString(R.string.txt_status_no_schedule_found));
            return;
        }

        if (!sleepSwitch.isChecked()) {
            getPrefs().setSleepActive(false);
            disableSchedule(schedule);
            return;
        }

        updateSchedule(schedule, new AbsoluteTime(0, 1, 0), Calendar.getInstance(), false);
        getPrefs().setSleepActive(true);
    }
}
