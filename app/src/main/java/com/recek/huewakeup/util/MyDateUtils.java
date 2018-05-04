package com.recek.huewakeup.util;

import com.philips.lighting.hue.sdk.wrapper.domain.resource.timepattern.TimePatternBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @since 2017-10-27.
 */
public class MyDateUtils {

    // TODO: locale
    public static final SimpleDateFormat SDF_TIME_SHORT = new SimpleDateFormat("HH:mm", Locale.US);
    private static final String TEXT_TIME_FORMAT = "^((2[0-3]|1[0-9]|0[0-9]|[0-9])(:([0-5][0-9]|[0-9])){0,2})$";


    public static boolean hasCorrectFormat(String timeStr) {
        return timeStr != null && timeStr.matches(TEXT_TIME_FORMAT);
    }

    public static Date calculateRelativeTimeTo(Calendar cal, String timeStr, boolean before) {
        if (!hasCorrectFormat(timeStr)) {
            return null;
        }

        String[] timeParts = timeStr.split(":");
        int hours = Integer.valueOf(timeParts[0]);
        int minutes = timeParts.length > 1 ? Integer.valueOf(timeParts[1]) : 0;
        int seconds = timeParts.length > 2 ? Integer.valueOf(timeParts[2]) : 0;

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

    public static void setWeekDay(Calendar cal, TimePatternBuilder timePatternBuilder) {
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case 1:
                timePatternBuilder.repeatsOnSunday();
                break;
            case 2:
                timePatternBuilder.repeatsOnMonday();
                break;
            case 3:
                timePatternBuilder.repeatsOnTuesday();
                break;
            case 4:
                timePatternBuilder.repeatsOnWednesday();
                break;
            case 5:
                timePatternBuilder.repeatsOnThursday();
                break;
            case 6:
                timePatternBuilder.repeatsOnFriday();
                break;
            case 7:
                timePatternBuilder.repeatsOnSaturday();
                break;
        }
    }
}
