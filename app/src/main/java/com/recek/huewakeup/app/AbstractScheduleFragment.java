package com.recek.huewakeup.app;

import android.app.Fragment;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.data.PHScheduleFix;
import com.philips.lighting.hue.listener.PHHTTPListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHSchedule;
import com.recek.huesleepwake.R;
import com.recek.huewakeup.util.MyDateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.recek.huewakeup.util.MyDateUtils.SDF_TIME_SHORT;

/**
 * @since 2017-09-14.
 */
public abstract class AbstractScheduleFragment extends Fragment {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractScheduleFragment.class);
    private static final String SUCCESS = "success";

    private HueSharedPreferences prefs;
    private PHHTTPListener putListener;
    private MainActivity mainActivity;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        prefs = mainActivity.getPrefs();
        putListener = createPutListener();
    }

    protected abstract TextView getStatusView();

    protected abstract void onSuccess();

    protected HueSharedPreferences getPrefs() {
        return prefs;
    }


    protected PHScheduleFix findScheduleById(String scheduleId) {
        PHBridge bridge = mainActivity.getBridge();
        if (bridge == null) {
            return null;
        }
        Map<String, PHSchedule> scheduleMap = bridge.getResourceCache().getSchedules();
        for (Map.Entry<String, PHSchedule> scheduleEntry : scheduleMap.entrySet()) {
            if (Objects.equals(scheduleEntry.getKey(), (scheduleId))) {
                return new PHScheduleFix(scheduleEntry.getValue(),
                        bridge.getResourceCache().getBridgeConfiguration());
            }
        }
        LOG.warn("Schedule-{} not found.", scheduleId);
        return null;
    }


    protected void setInitialStatus(String scheduleId) {
        PHScheduleFix scheduleFix = findScheduleById(scheduleId);
        if (scheduleFix != null) {
            PHSchedule schedule = scheduleFix.getSchedule();
            Date date = schedule.getDate();
            if (date != null && schedule.getStatus().equals(PHSchedule.PHScheduleStatus.ENABLED)) {
                getStatusView().setText(getResources()
                        .getString(R.string.txt_status_current_setting,
                                SDF_TIME_SHORT.format(date)));
                return;
            }
        }
        getStatusView().setText(R.string.txt_status_not_set);
    }

    protected Date updateSchedule(PHScheduleFix scheduleFix, String timeStr,
                                  Calendar startCal, boolean before) {
        PHBridge bridge = mainActivity.getBridge();
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
        bridge.doHTTPPut(scheduleFix.getUrl(), json, putListener);
        return wakeTime;
    }

    protected boolean disableSchedule(PHScheduleFix scheduleFix) {
        PHBridge bridge = mainActivity.getBridge();
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
            bridge.doHTTPPut(scheduleFix.getUrl(), json, putListener);
            return true;
        }

        return false;
    }

    private void notifyUser(final String msg) {
        getStatusView().setText(msg);
//        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private void notifyUser(final int msgId) {
        getStatusView().setText(msgId);
//        Toast.makeText(getActivity(), msgId, Toast.LENGTH_SHORT).show();
    }

    protected void notifyUserWithUiThread(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyUser(msg);
            }
        });
    }


    private void onSuccessWithUiThread() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onSuccess();
            }
        });
    }

    private PHHTTPListener createPutListener() {
        return new PHHTTPListener() {

            @Override
            public void onHTTPResponse(String jsonResponse) {
                LOG.info("RESPONSE: {}", jsonResponse);
                try {
                    int successCnt = 0;
                    int failCnt = 0;
                    JSONArray jsonArray = new JSONArray(jsonResponse);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject responseObj = jsonArray.getJSONObject(0);
                        String status = responseObj.keys().next();
                        if (Objects.equals(status, SUCCESS)) {
                            successCnt++;
                        } else {
                            failCnt++;
                        }
                    }

                    if (failCnt == 0) {
                        onSuccessWithUiThread();
                    } else if (successCnt == 0) {
                        notifyUserWithUiThread("Failed totally :(");
                        LOG.error("Response indicates errors.");
                    } else {
                        notifyUserWithUiThread(
                                "Failed partially (" + failCnt + "/" + (failCnt + successCnt) + ")");
                        LOG.error("Response indicates errors.");
                    }
                } catch (JSONException e) {
                    LOG.error("Could not read JSON response. Error: {}", e.getMessage());
                }
            }
        };
    }
}
