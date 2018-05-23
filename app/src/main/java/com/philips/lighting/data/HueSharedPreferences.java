package com.philips.lighting.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;
import java.util.List;

public class HueSharedPreferences {
    private static final String HUE_SHARED_PREFERENCES_STORE = "HueSharedPrefs";
    private static final String LAST_APP_VERSION_NAME = "AppVersionName";

    private static final String DEFAULT_SCHEDULE_ID = "-1";

    private static final String WAKE_TIME = "WakeTimeL";
    private static final String WAKE_TIME_RELATIVE = "WakeTimeRelative";
    private static final String DEFAULT_WAKE_TIME_RELATIVE = "8:00";
    private static final String WAKE_TIME_AT = "WakeTimeAt";

    private static final String WAKE_SCHEDULE_ID = "WakeScheduleId";
    private static final String WAKE_LIGHT_TIME = "WakeLightTimeL";
    private static final String WAKE_LIGHT_TIME_OFFSET = "WakeLightTimeOffset";
    private static final int DEFAULT_WAKE_LIGHT_TIME = 5;

    private static final String WAKE_END_SCHEDULE_ID = "WakeEndScheduleId";
    private static final String WAKE_END_TIME = "WakeEndTime";
    private static final int DEFAULT_WAKE_END_OFFSET = 30;

    private static final String WAKE_LIGHT_IS_ACTIVE = "WakeLightIsActive";

    private static final String SLEEP_SCHEDULE_ID = "SleepScheduleId";
    private static final String SLEEP_IS_ACTIVE = "SleepIsActive";

    private static final String ALARM_TIME = "AlarmTimeL";
    private static final String ALARM_TIME_OFFSET = "AlarmTimeOffset";
    private static final int DEFAULT_ALARM_TIME_RELATIVE = 0;
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


    public String getLastAppVersionName() {
        return mSharedPreferences.getString(LAST_APP_VERSION_NAME, null);
    }

    public boolean setLastAppVersionName(String versionName) {
        mSharedPreferencesEditor.putString(LAST_APP_VERSION_NAME, versionName);
        return (mSharedPreferencesEditor.commit());
    }

    public long getWakeTime() {
        return mSharedPreferences.getLong(WAKE_TIME, -1);
    }

    public void setWakeTime(long wakeTime) {
        mSharedPreferencesEditor.putLong(WAKE_TIME, wakeTime);
        mSharedPreferencesEditor.apply();
    }

    public String getWakeTimeRelative() {
        return mSharedPreferences.getString(WAKE_TIME_RELATIVE, DEFAULT_WAKE_TIME_RELATIVE);
    }

    public boolean setWakeTimeRelative(String wakeTime) {
        mSharedPreferencesEditor.putString(WAKE_TIME_RELATIVE, wakeTime);
        return (mSharedPreferencesEditor.commit());
    }

    public boolean isWakeTimeAt() {
        return mSharedPreferences.getBoolean(WAKE_TIME_AT, false);
    }

    public void setWakeTimeAt(boolean isAt) {
        mSharedPreferencesEditor.putBoolean(WAKE_TIME_AT, isAt);
        mSharedPreferencesEditor.apply();
    }

    public String getWakeScheduleId() {
        return mSharedPreferences.getString(WAKE_SCHEDULE_ID, DEFAULT_SCHEDULE_ID);
    }

    public boolean setWakeScheduleId(String wakeScheduleId) {
        mSharedPreferencesEditor.putString(WAKE_SCHEDULE_ID, wakeScheduleId);
        return (mSharedPreferencesEditor.commit());
    }

    public long getWakeLightTime() {
        return mSharedPreferences.getLong(WAKE_LIGHT_TIME, -1);
    }

    public void setWakeLightTime(long wakeTime) {
        mSharedPreferencesEditor.putLong(WAKE_LIGHT_TIME, wakeTime);
        mSharedPreferencesEditor.apply();
    }

    public int getWakeLightTimeOffset() {
        return mSharedPreferences.getInt(WAKE_LIGHT_TIME_OFFSET, DEFAULT_WAKE_LIGHT_TIME);
    }

    public void setWakeLightTimeOffset(int wakeTime) {
        mSharedPreferencesEditor.putInt(WAKE_LIGHT_TIME_OFFSET, wakeTime);
        mSharedPreferencesEditor.apply();
    }

    public String getWakeEndScheduleId() {
        return mSharedPreferences.getString(WAKE_END_SCHEDULE_ID, DEFAULT_SCHEDULE_ID);
    }

    public boolean setWakeEndScheduleId(String wakeEndScheduleId) {
        mSharedPreferencesEditor.putString(WAKE_END_SCHEDULE_ID, wakeEndScheduleId);
        return (mSharedPreferencesEditor.commit());
    }

    public int getWakeEndTimeOffset() {
        return mSharedPreferences.getInt(WAKE_END_TIME, DEFAULT_WAKE_END_OFFSET);
    }

    public void setWakeEndTimeOffset(int wakeTime) {
        mSharedPreferencesEditor.putInt(WAKE_END_TIME, wakeTime);
        mSharedPreferencesEditor.apply();
    }

    public boolean isWakeLightActive() {
        return mSharedPreferences.getBoolean(WAKE_LIGHT_IS_ACTIVE, false);
    }

    public boolean setWakeLightActive(boolean isActive) {
        mSharedPreferencesEditor.putBoolean(WAKE_LIGHT_IS_ACTIVE, isActive);
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

    public long getAlarmTime() {
        return mSharedPreferences.getLong(ALARM_TIME, -1);
    }

    public void setAlarmTime(long time) {
        mSharedPreferencesEditor.putLong(ALARM_TIME, time);
        mSharedPreferencesEditor.apply();
    }

    public int getAlarmTimeOffset() {
        return mSharedPreferences.getInt(ALARM_TIME_OFFSET, DEFAULT_ALARM_TIME_RELATIVE);
    }

    public boolean setAlarmTimeOffset(int time) {
        mSharedPreferencesEditor.putInt(ALARM_TIME_OFFSET, time);
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

    public String getWakeDaysRaw() {
        return mSharedPreferences.getString(WAKE_DAYS, DEFAULT_WAKE_DAYS);
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
