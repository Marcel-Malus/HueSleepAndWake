package com.recek.huewakeup.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.philips.lighting.data.PHScheduleFix;
import com.philips.lighting.quickstart.R;

import java.util.Calendar;
import java.util.Date;

/**
 * @since 2017-09-14.
 */
public class WakeEndScheduleFragment extends AbstractScheduleFragment {

    private TextView statusTxt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.schedule_wake_end, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        statusTxt = (TextView) getActivity().findViewById(R.id.wakeEndStatusTxt);
        String wakeEndScheduleId = getPrefs().getWakeEndScheduleId();
        setInitialStatus(wakeEndScheduleId);
    }


    @Override
    protected TextView getStatusView() {
        return statusTxt;
    }


    public boolean updateWakeUpEndSchedule(Date wakeDate) {
        PHScheduleFix schedule = findScheduleById(getPrefs().getWakeEndScheduleId());
        if (schedule == null) {
            return false;
        }
        String wakeTimeStr = getPrefs().getWakeEndTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(wakeDate);

        Date wakeEndDate = updateSchedule(schedule, wakeTimeStr, cal, false, false);
        return wakeEndDate != null;
    }
}
