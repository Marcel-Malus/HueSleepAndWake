package com.recek.huewakeup.app;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import com.recek.huewakeup.util.AbsoluteTime;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {

    private TimePickerDialog.OnTimeSetListener onTimeSetListener;
    private String timeString;

    public void setTimeSetListener(TimePickerDialog.OnTimeSetListener onTimeSetListener) {
        this.onTimeSetListener = onTimeSetListener;
    }

    public void setCurrentTime(String timeString) {
        this.timeString = timeString;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AbsoluteTime absoluteTime = new AbsoluteTime(timeString);
        int hour;
        int minute;
        if (absoluteTime.isValid) {
            hour = absoluteTime.hours;
            minute = absoluteTime.minutes;
        } else {
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), onTimeSetListener, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }
}
