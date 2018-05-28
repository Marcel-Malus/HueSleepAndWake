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
import com.recek.huewakeup.settings.WakeLightSettingsActivity;
import com.recek.huewakeup.util.AbsoluteTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.recek.huesleepwake.R.id.lightSwitch;

/**
 * @since 2017-09-14.
 */
public class WakeUpScheduleFragment extends AbstractScheduleFragment {

    private static final Logger LOG = LoggerFactory.getLogger(WakeUpScheduleFragment.class);

    private static final String DEFAULT_WAKE_UP_SCHEDULE_NAME = "HDuD_Default_WakeUp";
    private static final String DEFAULT_WAKE_UP_PRE_SCHEDULE_NAME = "HDuD_Default_Pre_WakeUp";
    private static final String DEFAULT_WAKE_END_SCHEDULE_NAME = "HDuD_Default_WakeEnd";
    // Brightness is a scale from 1 (the minimum) to 254 (the maximum). Note: a brightness of 1 is not off.
    public static final int WAKE_UP_BRIGHTNESS = 200;
    // 100ms * 6000 = 600s = 10m
    public static final int WAKE_UP_TRANSITION = 6000;

    private TextView statusTxt;
    private Switch wakeLightSwitch;

    @Override
    protected long getSavedTime() {
        return getPrefs().getWakeLightTime();
    }

    @Override
    protected TextView getStatusTxt() {
        return statusTxt;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wake_light, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        statusTxt = getActivity().findViewById(R.id.wakeLightStatusTxt);
        updateStatus();

        wakeLightSwitch = getActivity().findViewById(lightSwitch);
        wakeLightSwitch.setChecked(getPrefs().isWakeLightActive());

        final ImageButton settingsBtn = getActivity()
                .findViewById(R.id.lightSettingsBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startActivityIntent = new Intent(getActivity(),
                        WakeLightSettingsActivity.class);
                getActivity().startActivity(startActivityIntent);
            }
        });
    }

    @Override
    protected void onSuccess() {
        Schedule wakeSchedule = findScheduleById(getPrefs().getWakeScheduleId());
        Schedule wakeEndSchedule = findScheduleById(getPrefs().getWakeEndScheduleId());

        StringBuilder sb = new StringBuilder();
        boolean updated = false;
        if (wakeSchedule != null) {
            appendStatus(sb, wakeSchedule);
            updated = true;
        }
        if (updated && wakeEndSchedule != null) {
            appendStatus(sb, wakeEndSchedule);
        }
        if (!updated) {
            sb.append(getString(R.string.txt_status_updated_nothing));
        }
        statusTxt.setText(sb.toString());
    }

    @Override
    protected void onFailure() {
        statusTxt.setText(R.string.txt_status_update_failed);
    }


    public void initDefaultSchedules() {
        Schedule wakeSchedule = findScheduleById(getPrefs().getWakeScheduleId());
        Schedule wakeEndSchedule = findScheduleById(getPrefs().getWakeEndScheduleId());
        if (wakeSchedule != null && wakeEndSchedule != null) {
            // Schedules are set. No need to set defaults.
            return;
        }

        Bridge bridge = BridgeHolder.get();
        List<Schedule> scheduleList = bridge.getBridgeState().getSchedules();
        // Do defaults already exist? If so than set them as current.
        for (Schedule schedule : scheduleList) {
            if (schedule.getName().equals(DEFAULT_WAKE_UP_SCHEDULE_NAME) && wakeSchedule == null) {
                LOG.info("Found default schedule {}", DEFAULT_WAKE_UP_SCHEDULE_NAME);
                // Will not be set, because it is maybe set on "NONE" on purpose.
//                getPrefs().setWakeScheduleId(schedule.getIdentifier());
                wakeSchedule = schedule;
            } else if (schedule.getName()
                    .equals(DEFAULT_WAKE_END_SCHEDULE_NAME) && wakeEndSchedule == null) {
                LOG.info("Found default schedule {}", DEFAULT_WAKE_END_SCHEDULE_NAME);
                // Will not be set, because it is maybe set on "NONE" on purpose.
//                getPrefs().setWakeEndScheduleId(schedule.getIdentifier());
                wakeEndSchedule = schedule;
            }
        }

        if (wakeSchedule == null) {
            createDefaultWakeSchedule();
        }
        if (wakeEndSchedule == null) {
            createDefaultWakeEndSchedule();
        }
    }

    private void createDefaultWakeSchedule() {
        Schedule schedule = new Schedule();
        schedule.setName(DEFAULT_WAKE_UP_SCHEDULE_NAME);
        schedule.setDescription("Default wake up schedule for Hue Dusk and Dawn");

        LightState lightState = new LightState();
        // this screws up transition (thus pre schedule needed, see below)
//        lightState.setOn(true);
        lightState.setBrightness(WAKE_UP_BRIGHTNESS);
        lightState.setTransitionTime(WAKE_UP_TRANSITION);

        setScheduleDefaultsAndUpload(schedule, lightState, new BridgeResponseCallback() {
            @Override
            public void handleCallback(Bridge bridge, ReturnCode returnCode,
                                       List<ClipResponse> responses, List<HueError> errors) {
                if (returnCode == ReturnCode.SUCCESS) {
                    // Identifier of the created resource
                    String identifier = responses.get(0).getStringValue();
                    getPrefs().setWakeScheduleId(identifier);
                    LOG.info("Created default wake up schedule with id {}", identifier);
                    createDefaultPreWakeSchedule();
                } else {
                    LOG.warn("Unable to create default schedule. {} errors.", errors.size());
                    if (errors.size() > 0) {
                        LOG.warn("1. Error: {}", errors.get(0));
                    }
                }
            }
        });
    }

    // Necessary because transition does not work with switching lights on and setting brightness at once.
    private void createDefaultPreWakeSchedule() {
        Schedule schedule = new Schedule();
        schedule.setName(DEFAULT_WAKE_UP_PRE_SCHEDULE_NAME);
        schedule.setDescription("Default pre wake up schedule for Hue Dusk and Dawn");

        LightState lightState = new LightState();
        lightState.setOn(true);
        lightState.setBrightness(1);

        setScheduleDefaultsAndUpload(schedule, lightState, new BridgeResponseCallback() {
            @Override
            public void handleCallback(Bridge bridge, ReturnCode returnCode,
                                       List<ClipResponse> responses, List<HueError> errors) {
                if (returnCode == ReturnCode.SUCCESS) {
                    // Identifier of the created resource
                    String identifier = responses.get(0).getStringValue();
                    getPrefs().setPreWakeScheduleId(identifier);
                    LOG.info("Created default pre wake up schedule with id {}", identifier);
                } else {
                    LOG.warn("Unable to create default schedule. {} errors.", errors.size());
                    if (errors.size() > 0) {
                        LOG.warn("1. Error: {}", errors.get(0));
                    }
                }
            }
        });
    }

    private void createDefaultWakeEndSchedule() {
        Schedule schedule = new Schedule();
        schedule.setName(DEFAULT_WAKE_END_SCHEDULE_NAME);
        schedule.setDescription("Default wake end schedule for Hue Dusk and Dawn");

        LightState lightState = new LightState();
        lightState.setOn(false);

        setScheduleDefaultsAndUpload(schedule, lightState, new BridgeResponseCallback() {
            @Override
            public void handleCallback(Bridge bridge, ReturnCode returnCode,
                                       List<ClipResponse> responses, List<HueError> errors) {
                if (returnCode == ReturnCode.SUCCESS) {
                    // Identifier of the created resource
                    String identifier = responses.get(0).getStringValue();
                    getPrefs().setWakeEndScheduleId(identifier);
                    LOG.info("Created default wake end schedule with id {}", identifier);
                } else {
                    LOG.warn("Unable to create default schedule. {} errors.", errors.size());
                    if (errors.size() > 0) {
                        LOG.warn("1. Error: {}", errors.get(0));
                    }
                }
            }
        });
    }

    private void appendStatus(StringBuilder sb, Schedule schedule) {
        if (schedule.getStatus() == ScheduleStatus.ENABLED) {
            String localTime = schedule.getLocalTime().toString();
            int startIdx = localTime.indexOf('T') + 1;
            String timeStr = localTime.substring(startIdx, startIdx + 5);
            if (sb.length() == 0) {
                sb.append(getString(R.string.txt_status_alarm_on, timeStr));
            } else {
                sb.append(" - ");
                sb.append(timeStr);
            }
        } else {
            sb.append(getString(R.string.txt_status_alarm_off));
            getPrefs().setWakeLightTime(-1);
        }
    }

    public void updateWakeUpSchedule(Date wakeTime) {
        statusTxt.setText(getString(R.string.txt_status_updating));
        Schedule wakeSchedule = findScheduleById(getPrefs().getWakeScheduleId());
        if (wakeSchedule == null) {
            statusTxt.setText(getString(R.string.txt_status_no_schedule_found));
            return;
        }

        getPrefs().setWakeLightActive(wakeLightSwitch.isChecked());
        if (!wakeLightSwitch.isChecked()) {
            disableSchedule(wakeSchedule);
            return;
        }

        AbsoluteTime absoluteTime = new AbsoluteTime(0, getPrefs().getWakeLightTimeOffset(), 0);
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeTime);

        Date wakeUpDate = updateSchedule(wakeSchedule, absoluteTime, cal, true);

        if (wakeUpDate != null) {
            if (wakeSchedule.getName().equals(DEFAULT_WAKE_UP_SCHEDULE_NAME)) {
                // else disable preWakeUp?
                updatePreWakeUpSchedule(wakeUpDate);
            }

            getPrefs().setWakeLightTime(wakeUpDate.getTime());
            // TODO: evaluate wakeEnd update.
            updateWakeEndSchedule(wakeTime);
        }
    }

    private void updatePreWakeUpSchedule(Date wakeUpDate) {
        Schedule wakeSchedule = findScheduleById(getPrefs().getPreWakeScheduleId());
        if (wakeSchedule == null) {
            return;
        }
        AbsoluteTime absoluteTime = new AbsoluteTime(0, 0, 10);
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeUpDate);

        updateSchedule(wakeSchedule, absoluteTime, cal, true);
    }

    private boolean updateWakeEndSchedule(Date wakeTime) {
        Schedule wakeEndSchedule = findScheduleById(getPrefs().getWakeEndScheduleId());
        if (wakeEndSchedule == null) {
            return false;
        }

        AbsoluteTime absoluteTime = new AbsoluteTime(0, getPrefs().getWakeEndTimeOffset(), 0);
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeTime);

        Date wakeEndDate = updateSchedule(wakeEndSchedule, absoluteTime, cal, false);
        return wakeEndDate != null;
    }
}
