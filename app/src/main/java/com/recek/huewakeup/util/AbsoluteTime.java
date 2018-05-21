package com.recek.huewakeup.util;

/**
 * TODO: Make solid conversions between string and number representation of the time + always up to date validation.
 */
public class AbsoluteTime {

    public int seconds;
    public int minutes;
    public int hours;
    public boolean isValid;

    public AbsoluteTime(int hours, int minutes, int seconds) {
        this.seconds = seconds;
        this.minutes = minutes;
        this.hours = hours;
        isValid = true;
    }

    public AbsoluteTime(String timeString) {

        if (!MyDateUtils.hasCorrectFormat(timeString)) {
            isValid = false;
        } else {
            String[] timeParts = timeString.split(":");
            hours = Integer.valueOf(timeParts[0]);
            minutes = timeParts.length > 1 ? Integer.valueOf(timeParts[1]) : 0;
            seconds = timeParts.length > 2 ? Integer.valueOf(timeParts[2]) : 0;
            isValid = true;
        }
    }

    public void before() {
        if (isValid) {
            hours *= -1;
            minutes *= -1;
            seconds *= -1;
        }
    }
}
