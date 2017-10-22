package com.philips.lighting.data;

import com.philips.lighting.model.PHBridgeConfiguration;
import com.philips.lighting.model.PHSchedule;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @since 2017-08-28.
 */
public class PHScheduleFix {
    // TODO: Load locale.
    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss", Locale.GERMANY);

    private final PHSchedule schedule;
    private final PHBridgeConfiguration bridgeConfig;
    private final String url;

    private final String id;
    private final String name;

    private Date localTime;
    private String status;
    private String days;

    public PHScheduleFix(String id, String name) {
        this.id = id;
        this.name = name;

        this.schedule = null;
        this.bridgeConfig = null;
        this.url = null;
    }

    public PHScheduleFix(PHSchedule schedule, PHBridgeConfiguration bridgeConfig) {
        this.schedule = schedule;
        this.bridgeConfig = bridgeConfig;

        this.id = schedule.getIdentifier();
        this.name = schedule.getName();

        this.url = buildUrl();
    }

    private String buildUrl() {
        return "http://" +
                bridgeConfig.getIpAddress() +
                "/api/" +
                bridgeConfig.getUsername() +
                "/schedules/" +
                id;
    }

    public PHSchedule getSchedule() {
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

    public void setLocalTime(Date localTime) {
        this.localTime = localTime;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public void enable() {
        if (!schedule.getStatus().equals(PHSchedule.PHScheduleStatus.ENABLED))
            this.status = "enabled";
    }

    public void disable() {
        if (!schedule.getStatus().equals(PHSchedule.PHScheduleStatus.DISABLED))
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
            sb.append(SDF.format(localTime));
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
