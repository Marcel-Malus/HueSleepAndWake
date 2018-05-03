package com.recek.huewakeup.app;

import android.os.Bundle;
import android.widget.Toast;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.data.PHScheduleFix;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.Schedule;
import com.philips.lighting.quickstart.BridgeHolder;
import com.recek.huesleepwake.R;
import com.recek.huewakeup.util.MyDateUtils;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

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

        String id = scheduleFix.getId();
        LOG.info("Updating schedule {} ({}).", scheduleFix.getName(), id);

        Date wakeTime = MyDateUtils.calculateRelativeTimeTo(startCal, timeStr, before);
        if (wakeTime == null) {
            notifyUser(R.string.txt_status_wrong_format);
            return null;
        }

        // Removed pick-a-day feature, so its always the "dayOfSchedule" now. Left the choice for case the feature comes back.
        String days = MyDateUtils.calculateWakeUpDays(wakeTime, true, prefs.getWakeDaysRaw());
        if (days == null) {
            return null;
        }

        scheduleFix.setLocalTime(wakeTime);
        scheduleFix.setDays(days);
        scheduleFix.enable();
        final String json;
        try {
            json = scheduleFix.buildJson();
        } catch (JSONException e) {
            LOG.error("Could not build JSON. Error: {}", e.getMessage());
            Toast.makeText(getActivity(), "Error building request JSON", Toast.LENGTH_SHORT).show();
            return null;
        }

        LOG.info("Sending PUT to {} with {}", id, json);
        // TODO: Update with new SDK
//        bridge.doHTTPPut(scheduleFix.getUrl(), json, putListener);
        return wakeTime;
    }

    protected boolean disableSchedule(PHScheduleFix scheduleFix) {
        Bridge bridge = BridgeHolder.get();
        if (bridge == null) {
            return false;
        }

        String id = scheduleFix.getId();
        LOG.info("Disabling schedule {} ({}).", scheduleFix.getName(), id);

        scheduleFix.disable();
        final String json;
        try {
            json = scheduleFix.buildJson();
        } catch (JSONException e) {
            LOG.error("Could not build JSON. Error: {}", e.getMessage());
            Toast.makeText(getActivity(), "Error building request JSON", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (json != null) {
            LOG.info("Sending PUT to {} with {}", id, json);
            // TODO: Update with new SDK
//            bridge.doHTTPPut(scheduleFix.getUrl(), json, putListener);
            return true;
        }

        return false;
    }

    private void notifyUser(final int msgId) {
        getStatusTxt().setText(msgId);
//        Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();
    }

}
