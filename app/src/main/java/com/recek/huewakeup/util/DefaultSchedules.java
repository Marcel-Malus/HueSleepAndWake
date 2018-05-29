package com.recek.huewakeup.util;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeResponseCallback;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.HueError;
import com.philips.lighting.hue.sdk.wrapper.domain.ReturnCode;
import com.philips.lighting.hue.sdk.wrapper.domain.clip.ClipResponse;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightState;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.Schedule;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.builder.ClipActionBuilder;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.timepattern.TimePatternBuilder;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridges;
import com.philips.lighting.quickstart.BridgeHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Since 28/05/2018.
 */
public class DefaultSchedules {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSchedules.class);

    public static final String DEFAULT_SCHEDULE_NAME = "HDuD_Default_";
    public static final String DEFAULT_WAKE_UP_SCHEDULE_NAME = DEFAULT_SCHEDULE_NAME + "WakeUp";
    public static final String DEFAULT_PRE_WAKE_UP_SCHEDULE_NAME = DEFAULT_SCHEDULE_NAME + "Pre_WakeUp";
    public static final String DEFAULT_WAKE_END_SCHEDULE_NAME = DEFAULT_SCHEDULE_NAME + "WakeEnd";
    public static final String DEFAULT_SLEEP_SCHEDULE_NAME = DEFAULT_SCHEDULE_NAME + "Sleep";
    public static final String DEFAULT_POST_SLEEP_SCHEDULE_NAME = DEFAULT_SCHEDULE_NAME + "Post_Sleep";
    // Brightness is a scale from 1 (the minimum) to 254 (the maximum). Note: a brightness of 1 is not off.
    private static final int WAKE_UP_BRIGHTNESS = 200;
    // 100ms * 6000 = 600s = 10m
    private static final int WAKE_UP_TRANSITION = 6000;
    // 100ms * 9000 = 900s = 15m
    public static final int SLEEP_TRANSITION_TIME_MNT = 15;
    private static final int SLEEP_TRANSITION_TIME = 9000;

    private final HueSharedPreferences prefs;

    public DefaultSchedules(HueSharedPreferences prefs) {
        this.prefs = prefs;
    }

    public void init() {
        Schedule wakeSchedule = findScheduleById(prefs.getWakeScheduleId());
        Schedule wakeEndSchedule = findScheduleById(prefs.getWakeEndScheduleId());
        Schedule sleepSchedule = findScheduleById(prefs.getSleepScheduleId());
        if (wakeSchedule != null && wakeEndSchedule != null && sleepSchedule != null) {
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
//                prefs.setWakeScheduleId(schedule.getIdentifier());
                wakeSchedule = schedule;
            } else if (schedule.getName().equals(DEFAULT_WAKE_END_SCHEDULE_NAME)
                    && wakeEndSchedule == null) {
                LOG.info("Found default schedule {}", DEFAULT_WAKE_END_SCHEDULE_NAME);
                // Will not be set, because it is maybe set on "NONE" on purpose.
//                prefs.setWakeEndScheduleId(schedule.getIdentifier());
                wakeEndSchedule = schedule;
            } else if (schedule.getName().equals(DEFAULT_SLEEP_SCHEDULE_NAME)
                    && sleepSchedule == null) {
                LOG.info("Found default schedule {}", DEFAULT_SLEEP_SCHEDULE_NAME);
                // Will not be set, because it is maybe set on "NONE" on purpose.
//                getPrefs().setSleepScheduleId(schedule.getIdentifier());
                sleepSchedule = schedule;
            }
        }

        if (wakeSchedule == null) {
            createDefaultWakeSchedule();
        }
        if (wakeEndSchedule == null) {
            createDefaultWakeEndSchedule();
        }
        if (sleepSchedule == null) {
            createDefaultSleepSchedule();
        }
    }

    private Schedule findScheduleById(String scheduleId) {
        Schedule schedule = BridgeHolder.get().getBridgeState().getSchedule(scheduleId);
        if (schedule == null) {
            LOG.warn("Schedule-{} not found.", scheduleId);
            return null;
        }
        return schedule;
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
                    prefs.setWakeScheduleId(identifier);
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
        schedule.setName(DEFAULT_PRE_WAKE_UP_SCHEDULE_NAME);
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
                    prefs.setPreWakeScheduleId(identifier);
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
                    prefs.setWakeEndScheduleId(identifier);
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

    private void createDefaultSleepSchedule() {
        Schedule schedule = new Schedule();
        schedule.setName(DEFAULT_SLEEP_SCHEDULE_NAME);
        schedule.setDescription("Default sleep schedule for Hue Dusk and Dawn");

        LightState lightState = new LightState();
        // this screws up transition (thus post schedule needed, see below)
//        lightState.setOn(false);
        lightState.setBrightness(1);
        lightState.setTransitionTime(SLEEP_TRANSITION_TIME);

        setScheduleDefaultsAndUpload(schedule, lightState, new BridgeResponseCallback() {
            @Override
            public void handleCallback(Bridge bridge, ReturnCode returnCode,
                                       List<ClipResponse> responses, List<HueError> errors) {
                if (returnCode == ReturnCode.SUCCESS) {
                    // Identifier of the created resource
                    String identifier = responses.get(0).getStringValue();
                    prefs.setSleepScheduleId(identifier);
                    LOG.info("Created default sleep schedule with id {}", identifier);
                    createDefaultPostSleepSchedule();
                } else {
                    LOG.warn("Unable to create default schedule. {} errors.", errors.size());
                    if (errors.size() > 0) {
                        LOG.warn("1. Error: {}", errors.get(0));
                    }
                }
            }
        });
    }

    // Necessary because transition does not work with switching lights off and setting brightness at once.
    private void createDefaultPostSleepSchedule() {

        Schedule schedule = new Schedule();
        schedule.setName(DEFAULT_POST_SLEEP_SCHEDULE_NAME);
        schedule.setDescription("Default post sleep schedule for Hue Dusk and Dawn");

        LightState lightState = new LightState();
        lightState.setOn(false);

        setScheduleDefaultsAndUpload(schedule, lightState, new BridgeResponseCallback() {
            @Override
            public void handleCallback(Bridge bridge, ReturnCode returnCode,
                                       List<ClipResponse> responses, List<HueError> errors) {
                if (returnCode == ReturnCode.SUCCESS) {
                    // Identifier of the created resource
                    String identifier = responses.get(0).getStringValue();
                    prefs.setPostSleepScheduleId(identifier);
                    LOG.info("Created default post sleep schedule with id {}", identifier);
                } else {
                    LOG.warn("Unable to create default schedule. {} errors.", errors.size());
                    if (errors.size() > 0) {
                        LOG.warn("1. Error: {}", errors.get(0));
                    }
                }
            }
        });
    }


    private void setScheduleDefaultsAndUpload(Schedule schedule,
                                              LightState lightState,
                                              BridgeResponseCallback bridgeResponseCallback) {

        ClipActionBuilder clipActionBuilder = new ClipActionBuilder();
        clipActionBuilder.setGroupLightState("1", lightState);

        // Triggers 1-1-2035 @ 10:05 A.M. Mandatory, will be overwritten on update.
        TimePatternBuilder timePatternBuilder = new TimePatternBuilder();
        timePatternBuilder.startAtDate(1, 1, 2035, 10, 5, 0);

        schedule.setLocalTime(timePatternBuilder.build());
        schedule.setRecycle(true);

        Bridge bridge = BridgeHolder.get();
        schedule.setClipAction(clipActionBuilder.setUsername(KnownBridges.retrieveWhitelistEntry(
                bridge.getIdentifier())).buildSingle(
                bridge.getBridgeConfiguration().getVersion()));

        bridge.createResource(schedule, BridgeConnectionType.LOCAL, bridgeResponseCallback);
    }
}
