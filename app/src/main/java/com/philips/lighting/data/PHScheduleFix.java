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
        if (localTime != null) {
            // TODO: Read W... from current schedule
            String lt = "W120/T" + SDF.format(localTime);
            json.put("localtime", lt);
        }
        if (status != null) {
            json.put("status", status);
        }
        return json.toString();
    }

    @Override
    public String toString() {
        return this.name;
    }
}