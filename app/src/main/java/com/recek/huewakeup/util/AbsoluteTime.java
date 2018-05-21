package com.recek.huewakeup.util;

public class AbsoluteTime {

    private String timeString;
    public int seconds;
    public int minutes;
    public int hours;
    public boolean isValid;

    public AbsoluteTime(String timeString) {
        this.timeString = timeString;

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

    @Override
    public String toString() {
        return timeString;
    }
}
