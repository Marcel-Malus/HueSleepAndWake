package com.recek.huewakeup.app;

import android.os.Bundle;

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
import com.recek.huewakeup.util.AbsoluteTime;
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


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    protected abstract void onSuccess();

    protected abstract void onFailure();

    protected Schedule findScheduleById(String scheduleId) {
        if (!BridgeHolder.hasBridge()) {
            LOG.warn("No bridge present.");
            return null;
        }
        Schedule schedule = BridgeHolder.get().getBridgeState().getSchedule(scheduleId);
        if (schedule == null) {
            LOG.warn("Schedule-{} not found.", scheduleId);
            return null;
        }
        return schedule;
    }

    /**
     * @param schedule to update.
     * @param offset   additional offset to startCal.
     * @param startCal date-time to wakeup.
     * @param before   offset before startCal?
     * @return wakeTime (incl. offset) when success. Null else.
     */
    protected Date updateSchedule(Schedule schedule, AbsoluteTime offset,
                                  Calendar startCal, boolean before) {
        LOG.info("Updating schedule {} ({}).", schedule.getName(), schedule.getIdentifier());

        Date wakeTime = MyDateUtils.calculateRelativeTimeTo(startCal, offset, before);
        if (wakeTime == null) {
            notifyUser(R.string.txt_status_wrong_format);
            return null;
        }


        TimePatternBuilder timePatternBuilder = new TimePatternBuilder();
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeTime);
        timePatternBuilder.startAt(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND));
        MyDateUtils.setWeekDay(cal, timePatternBuilder);
        // Doesn't seem to work
//        cal.add(Calendar.MINUTE, 30);
//        timePatternBuilder.endAt(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));

        schedule.setLocalTime(timePatternBuilder.build());
        schedule.setStatus(ScheduleStatus.ENABLED);
        if (schedule.getAutoDelete() != null) {
            LOG.info("Auto-delete not null.");
            schedule.setAutoDelete(null);
        }

        if (!BridgeHolder.hasBridge()) {
            LOG.warn("No bridge present.");
            return null;
        } else {
            BridgeHolder.get()
                    .updateResource(schedule, BridgeConnectionType.LOCAL, createUpdateCallback());
            return wakeTime;
        }
    }

    protected void disableSchedule(Schedule schedule) {
        if (schedule.getStatus() == ScheduleStatus.DISABLED) {
            // Already disabled. Nothing to update.
            getStatusTxt().setText(R.string.txt_status_alarm_off);
            return;
        }
        if (!BridgeHolder.hasBridge()) {
            return;
        }

        LOG.info("Disabling schedule {} ({}).", schedule.getName(), schedule.getIdentifier());

        schedule.setStatus(ScheduleStatus.DISABLED);

        BridgeHolder.get()
                .updateResource(schedule, BridgeConnectionType.LOCAL, createUpdateCallback());
    }

    private BridgeResponseCallback createUpdateCallback() {
        return new BridgeResponseCallback() {
            @Override
            public void handleCallback(Bridge bridge, ReturnCode returnCode,
                                       List<ClipResponse> listR, List<HueError> listE) {
                if (returnCode == ReturnCode.SUCCESS) {
                    // Identifier (Name) of the created resource
                    String identifier = listR.get(0).getStringValue();
                    LOG.info("Update successful for: " + identifier);
                    onSuccessWithUiThread();
                } else {
                    LOG.error("Update schedule failed. ErrorCode: {}", returnCode.name());
                    if (listE.size() > 0) {
                        LOG.error("Update schedule failed. First error: {}", listE.get(0));
                    }
                    onFailureWithUiThread();
                }
            }
        };
    }

    private void onSuccessWithUiThread() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onSuccess();
            }
        });
    }

    private void onFailureWithUiThread() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onFailure();
            }
        });
    }

    private void notifyUser(final int msgId) {
        getStatusTxt().setText(msgId);
//        Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();
    }
}
