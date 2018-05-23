package com.recek.huewakeup.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.wrapper.connection.BridgeResponseCallback;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.HueError;
import com.philips.lighting.hue.sdk.wrapper.domain.ReturnCode;
import com.philips.lighting.hue.sdk.wrapper.domain.clip.ClipResponse;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightState;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.Schedule;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.ScheduleStatus;
import com.philips.lighting.quickstart.BridgeHolder;
import com.recek.huesleepwake.R;
import com.recek.huewakeup.settings.SleepLightSettingsActivity;
import com.recek.huewakeup.util.AbsoluteTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;

/**
 * @since 2017-09-14.
 */
public class SleepScheduleFragment extends AbstractScheduleFragment {

    private static final Logger LOG = LoggerFactory.getLogger(SleepScheduleFragment.class);
    private static final String DEFAULT_SLEEP_SCHEDULE_NAME = "HDuD_Default_Sleep";
    // 100ms * 9000 = 900s = 15m
    private static final int SLEEP_TRANSITION_TIME = 9000;

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


    public void initDefaultSchedules() {
        Schedule sleepSchedule = findScheduleById(getPrefs().getSleepScheduleId());
        if (sleepSchedule != null) {
            // Schedule is set. No need to set default.
            return;
        }

        Bridge bridge = BridgeHolder.get();
        List<Schedule> scheduleList = bridge.getBridgeState().getSchedules();
        // Does default already exist?
        for (Schedule schedule : scheduleList) {
            if (schedule.getName().equals(DEFAULT_SLEEP_SCHEDULE_NAME)) {
                LOG.info("Found default schedule {}", DEFAULT_SLEEP_SCHEDULE_NAME);
                // Will not be set, because it is maybe set on "NONE" on purpose.
//                getPrefs().setSleepScheduleId(schedule.getIdentifier());
                return;
            }
        }

        Schedule schedule = new Schedule();
        schedule.setName(DEFAULT_SLEEP_SCHEDULE_NAME);
        schedule.setDescription("Default sleep schedule for Hue Dusk and Dawn");

        LightState lightState = new LightState();
        lightState.setOn(false);
        lightState.setBrightness(1);
        lightState.setTransitionTime(SLEEP_TRANSITION_TIME);

        setScheduleDefaultsAndUpload(schedule, lightState, new BridgeResponseCallback() {
            @Override
            public void handleCallback(Bridge bridge, ReturnCode returnCode,
                                       List<ClipResponse> responses, List<HueError> errors) {
                if (returnCode == ReturnCode.SUCCESS) {
                    // Identifier of the created resource
                    String identifier = responses.get(0).getStringValue();
                    getPrefs().setSleepScheduleId(identifier);
                    LOG.info("Created default sleep schedule with id {}", identifier);
                } else {
                    LOG.warn("Unable to create default schedule. {} errors.", errors.size());
                    if (errors.size() > 0) {
                        LOG.warn("1. Error: {}", errors.get(0));
                    }
                }
            }
        });
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
