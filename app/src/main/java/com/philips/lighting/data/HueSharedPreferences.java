package com.philips.lighting.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;
import java.util.List;

public class HueSharedPreferences {
    private static final String HUE_SHARED_PREFERENCES_STORE = "HueSharedPrefs";
    private static final String LAST_CONNECTED_USERNAME = "LastConnectedUsername";
    private static final String LAST_CONNECTED_IP = "LastConnectedIP";

    private static final String DEFAULT_SCHEDULE_ID = "-1";

    private static final String WAKE_SCHEDULE_ID = "WakeScheduleId";
    private static final String WAKE_TIME = "WakeTime";
    private static final String DEFAULT_WAKE_TIME = "8:00";

    private static final String WAKE_END_SCHEDULE_ID = "WakeEndScheduleId";
    private static final String WAKE_END_TIME = "WakeEndTime";
    private static final String DEFAULT_WAKE_END_TIME = "0:30";

    private static final String SLEEP_SCHEDULE_ID = "SleepScheduleId";
    private static final String SLEEP_IS_ACTIVE = "SleepIsActive";

    private static final String ALARM_TIME = "AlarmTime";
    private static final String DEFAULT_ALARM_TIME = "8:00";
    private static final String ALARM_IS_ACTIVE = "AlarmIsActive";
    private static final String ALARM_SOUND = "AlarmSound";

    private static final String WAKE_DAYS = "WakeDays";
    private static final String DEFAULT_WAKE_DAYS = "0000000";
    public static final String[] DAYS = {
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday"
    };


    private static HueSharedPreferences instance = null;
    private SharedPreferences mSharedPreferences = null;

    private Editor mSharedPreferencesEditor = null;


    public void create() {

    }

    public static HueSharedPreferences getInstance(Context ctx) {
        if (instance == null) {
            instance = new HueSharedPreferences(ctx);
        }
        return instance;
    }

    private HueSharedPreferences(Context appContext) {
        mSharedPreferences = appContext.getSharedPreferences(HUE_SHARED_PREFERENCES_STORE, 0); // 0 - for private mode
        mSharedPreferencesEditor = mSharedPreferences.edit();
    }


    public String getUsername() {
        return mSharedPreferences.getString(LAST_CONNECTED_USERNAME, "");
    }

    public boolean setUsername(String username) {
        mSharedPreferencesEditor.putString(LAST_CONNECTED_USERNAME, username);
        return (mSharedPreferencesEditor.commit());
    }

    public String getLastConnectedIPAddress() {
        return mSharedPreferences.getString(LAST_CONNECTED_IP, "");
    }

    public boolean setLastConnectedIPAddress(String ipAddress) {
        mSharedPreferencesEditor.putString(LAST_CONNECTED_IP, ipAddress);
        return (mSharedPreferencesEditor.commit());
    }

    public String getWakeScheduleId() {
        return mSharedPreferences.getString(WAKE_SCHEDULE_ID, DEFAULT_SCHEDULE_ID);
    }

    public boolean setWakeScheduleId(String wakeScheduleId) {
        mSharedPreferencesEditor.putString(WAKE_SCHEDULE_ID, wakeScheduleId);
        return (mSharedPreferencesEditor.commit());
    }

    public String getWakeTime() {
        return mSharedPreferences.getString(WAKE_TIME, DEFAULT_WAKE_TIME);
    }

    public boolean setWakeTime(String wakeTime) {
        mSharedPreferencesEditor.putString(WAKE_TIME, wakeTime);
        return (mSharedPreferencesEditor.commit());
    }

    public String getWakeEndScheduleId() {
        return mSharedPreferences.getString(WAKE_END_SCHEDULE_ID, DEFAULT_SCHEDULE_ID);
    }

    public boolean setWakeEndScheduleId(String wakeEndScheduleId) {
        mSharedPreferencesEditor.putString(WAKE_END_SCHEDULE_ID, wakeEndScheduleId);
        return (mSharedPreferencesEditor.commit());
    }

    public String getWakeEndTime() {
        return mSharedPreferences.getString(WAKE_END_TIME, DEFAULT_WAKE_END_TIME);
    }

    public boolean setWakeEndTime(String wakeTime) {
        mSharedPreferencesEditor.putString(WAKE_END_TIME, wakeTime);
        return (mSharedPreferencesEditor.commit());
    }

    public String getSleepScheduleId() {
        return mSharedPreferences.getString(SLEEP_SCHEDULE_ID, DEFAULT_SCHEDULE_ID);
    }

    public boolean setSleepScheduleId(String sleepScheduleId) {
        mSharedPreferencesEditor.putString(SLEEP_SCHEDULE_ID, sleepScheduleId);
        return (mSharedPreferencesEditor.commit());
    }

    public boolean isSleepActive() {
        return mSharedPreferences.getBoolean(SLEEP_IS_ACTIVE, false);
    }

    public boolean setSleepActive(boolean isSleepActive) {
        mSharedPreferencesEditor.putBoolean(SLEEP_IS_ACTIVE, isSleepActive);
        return (mSharedPreferencesEditor.commit());
    }

    public String getAlarmTime() {
        return mSharedPreferences.getString(ALARM_TIME, DEFAULT_ALARM_TIME);
    }

    public boolean setAlarmTime(String time) {
        mSharedPreferencesEditor.putString(ALARM_TIME, time);
        return (mSharedPreferencesEditor.commit());
    }

    public boolean isAlarmActive() {
        return mSharedPreferences.getBoolean(ALARM_IS_ACTIVE, false);
    }

    public boolean setAlarmActive(boolean isActive) {
        mSharedPreferencesEditor.putBoolean(ALARM_IS_ACTIVE, isActive);
        return (mSharedPreferencesEditor.commit());
    }

    public String getAlarmSoundUri() {
        return mSharedPreferences.getString(ALARM_SOUND, null);
    }

    public boolean setAlarmSoundUri(String uri) {
        mSharedPreferencesEditor.putString(ALARM_SOUND, uri);
        return (mSharedPreferencesEditor.commit());
    }

    public List<Boolean> getWakeDays() {
        String checkedDays = mSharedPreferences.getString(WAKE_DAYS, DEFAULT_WAKE_DAYS);
        final List<Boolean> daysList = new ArrayList<>();
        for (int i = 0; i < checkedDays.length(); i++) {
            char checked = checkedDays.charAt(i);
            daysList.add(checked == '1');
        }
        return daysList;
    }

    public boolean setWakeDays(final List<Boolean> daysList) {
        StringBuilder sb = new StringBuilder();
        for (Boolean checked : daysList) {
            sb.append(checked ? "1" : "0");
        }
        mSharedPreferencesEditor.putString(WAKE_DAYS, sb.toString());
        return (mSharedPreferencesEditor.commit());
    }
}
