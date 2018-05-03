package com.recek.huewakeup.app;

import android.os.Bundle;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.data.PHScheduleFix;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeResponseCallback;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.HueError;
import com.philips.lighting.hue.sdk.wrapper.domain.ReturnCode;
import com.philips.lighting.hue.sdk.wrapper.domain.clip.ClipResponse;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.Schedule;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.ScheduleStatus;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.timepattern.TimePatternBuilder;
import com.philips.lighting.quickstart.BridgeHolder;
import com.recek.huesleepwake.R;
import com.recek.huewakeup.util.MyDateUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @since 2017-09-14.
 */
public abstract class AbstractScheduleFragment extends AbstractBasicFragment {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractScheduleFragment.class);

    private HueSharedPreferences prefs;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity mainActivity = (MainActivity) getActivity();
        prefs = mainActivity.getPrefs();
    }

    protected abstract void onSuccess();

    protected HueSharedPreferences getPrefs() {
        return prefs;
    }


    protected PHScheduleFix findScheduleById(String scheduleId) {
        Bridge bridge = BridgeHolder.get();
        if (bridge == null) {
            return null;
        }
        Schedule schedule = bridge.getBridgeState().getSchedule(scheduleId);
        if (schedule == null) {
            LOG.warn("Schedule-{} not found.", scheduleId);
            return null;
        }
        return new PHScheduleFix(schedule, bridge.getBridgeConfiguration());
    }

    protected Date updateSchedule(PHScheduleFix scheduleFix, String timeStr,
                                  Calendar startCal, boolean before) {
        Bridge bridge = BridgeHolder.get();
        if (bridge == null) {
            return null;
        }

        Schedule schedule = scheduleFix.getSchedule();

        LOG.info("Updating schedule {} ({}).", schedule.getName(), schedule.getIdentifier());

        Date wakeTime = MyDateUtils.calculateRelativeTimeTo(startCal, timeStr, before);
        if (wakeTime == null) {
            notifyUser(R.string.txt_status_wrong_format);
            return null;
        }


        TimePatternBuilder timePatternBuilder = new TimePatternBuilder();
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeTime);
        timePatternBuilder.startAt(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        MyDateUtils.setWeekDay(cal, timePatternBuilder);

        schedule.setLocalTime(timePatternBuilder.build());
        schedule.setStatus(ScheduleStatus.ENABLED);

        bridge.updateResource(schedule, BridgeConnectionType.LOCAL, createUpdateCallback());

        return wakeTime;
    }

    protected boolean disableSchedule(PHScheduleFix scheduleFix) {
        Bridge bridge = BridgeHolder.get();
        if (bridge == null) {
            return false;
        }

        Schedule schedule = scheduleFix.getSchedule();

        LOG.info("Updating schedule {} ({}).", schedule.getName(), schedule.getIdentifier());

        schedule.setStatus(ScheduleStatus.DISABLED);

        bridge.updateResource(schedule, BridgeConnectionType.LOCAL, createUpdateCallback());

        return false;
    }

    private BridgeResponseCallback createUpdateCallback() {
        return new BridgeResponseCallback() {
            @Override
            public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> listR, List<HueError> listE) {
                if (returnCode == ReturnCode.SUCCESS) {
                    // Identifier (Name) of the created resource
                    String identifier = listR.get(0).getStringValue();
                    LOG.info("Update successful for: " + identifier);
                    onSuccessWithUiThread();
                    // ...
                } else {
                    // ...
                    LOG.error("Update schedule failed. ErrorCode: {}, First error: {}", returnCode.name(), listE.get(0));
                }
            }
        };
    }

    private void onSuccessWithUiThread() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onSuccess();
            }
        });
    }

    private void notifyUser(final int msgId) {
        getStatusTxt().setText(msgId);
//        Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();
    }

}
