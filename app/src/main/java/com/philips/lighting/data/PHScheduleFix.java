package com.philips.lighting.data;

import com.philips.lighting.hue.sdk.wrapper.domain.BridgeConfiguration;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.Schedule;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.ScheduleStatus;
import com.recek.huewakeup.util.MyDateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * @since 2017-08-28.
 */
public class PHScheduleFix {

    private final Schedule schedule;
    private final BridgeConfiguration bridgeConfig;
    private final String url;

    private final String id;
    private final String name;

    private Date localTime;
    private boolean enabled;
    private String status;
    private String days;

    public PHScheduleFix(String id, String name) {
        this.id = id;
        this.name = name;

        this.schedule = null;
        this.bridgeConfig = null;
        this.url = null;
    }

    public PHScheduleFix(Schedule schedule, BridgeConfiguration bridgeConfig) {
        this.schedule = schedule;
        this.bridgeConfig = bridgeConfig;

        this.id = schedule.getIdentifier();
        this.name = schedule.getName();

        this.url = buildUrl();
    }

    private String buildUrl() {
        return "http://" +
                bridgeConfig.getNetworkConfiguration().getIpAddress() +
                "/api/" +
                bridgeConfig.getName() +
                "/schedules/" +
                id;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Date getLocalTime() {
        return localTime;
    }

    public void setLocalTime(Date localTime) {
        this.localTime = localTime;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        enabled = true;
        if (!schedule.getStatus().equals(ScheduleStatus.ENABLED))
            this.status = "enabled";
    }

    public void disable() {
        enabled = false;
        if (!schedule.getStatus().equals(ScheduleStatus.DISABLED))
            this.status = "disabled";
    }

    public String buildJson() throws JSONException {
        JSONObject json = new JSONObject();
        boolean isDirty = false;
        if (localTime != null && days != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("W");
            sb.append(days);
            sb.append("/T");
            sb.append(MyDateUtils.SDF_TIME.format(localTime));
            json.put("localtime", sb.toString());
            isDirty = true;
        }
        if (status != null) {
            json.put("status", status);
            isDirty = true;
        }

        return isDirty ? json.toString() : null;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
