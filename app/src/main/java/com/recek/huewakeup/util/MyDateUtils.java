package com.recek.huewakeup.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @since 2017-10-27.
 */
public class MyDateUtils {

    private static final Logger LOG = LoggerFactory.getLogger(MyDateUtils.class);

    // TODO: locale
    public static final SimpleDateFormat SDF_TIME = new SimpleDateFormat("HH:mm:ss", Locale.GERMANY);
    private static final String TEXT_TIME_FORMAT = "^((2[0-3]|1[0-9]|0[0-9]|[0-9])(:([0-5][0-9]|[0-9])){0,2})$";
    private static final String BLANK_WAKE_DAYS = "00000000";


    public static boolean hasCorrectFormat(String timeStr) {
        return timeStr != null && timeStr.matches(TEXT_TIME_FORMAT);
    }

    public static Date calculateRelativeTimeTo(Calendar cal, String timeStr, boolean before) {
        if (!hasCorrectFormat(timeStr)) {
            return null;
        }

        String[] timeParts = timeStr.split(":");
        int hours = Integer.valueOf(timeParts[0]);
        int minutes = timeParts.length == 2 ? Integer.valueOf(timeParts[1]) : 0;
        int seconds = timeParts.length == 3 ? Integer.valueOf(timeParts[2]) : 0;

        if (before) {
            hours *= -1;
            minutes *= -1;
            seconds *= -1;
        }

        cal.add(Calendar.SECOND, seconds);
        cal.add(Calendar.MINUTE, minutes);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        return cal.getTime();
    }


    public static String calculateWakeUpDays(Date wakeTime, boolean useDayOfSchedule, String rawWakeDays) {
        String wakeDays;
        if (useDayOfSchedule) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(wakeTime);
            // Converting SUN-SAT (1-7) to MON-SUN (1-7)
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
            if (dayOfWeek == 0) {
                dayOfWeek = 7;
            }
            StringBuilder sb = new StringBuilder(BLANK_WAKE_DAYS);
            sb.setCharAt(dayOfWeek, '1');
            wakeDays = sb.toString();
        } else {
            wakeDays = "0" + rawWakeDays;
        }
        try {
            int wakeDaysHueFormat = Integer.parseInt(wakeDays, 2);
            return String.valueOf(wakeDaysHueFormat);
        } catch (NumberFormatException e) {
            LOG.error("Could not interpret days ({}) for wake up: {}.", wakeDays, e.getMessage());
            return null;
        }
    }
}
