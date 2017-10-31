package com.recek.huewakeup.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.quickstart.R;
import com.recek.huewakeup.util.MyDateUtils;

import java.util.Calendar;
import java.util.Date;

import static com.recek.huewakeup.util.MyDateUtils.SDF_TIME;

/**
 * @since 2017-09-14.
 */
public class WakeTimeFragment extends Fragment {

    private EditText inputTimeTxt;
    private TextView statusTxt;
    private HueSharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wake_time, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prefs = HueSharedPreferences.getInstance(getActivity());

        inputTimeTxt = (EditText) getActivity().findViewById(R.id.wakeTime);
        inputTimeTxt.setText(prefs.getWakeTimeRelative());

        statusTxt = (TextView) getActivity().findViewById(R.id.wakeStatusTxt);
        setInitialStatus();
    }


    private void setInitialStatus() {
        String wakeTime = prefs.getWakeTime();
        if (wakeTime != null) {
            statusTxt.setText(getResources().getString(R.string.txt_status_current_setting, wakeTime));
        } else {
            statusTxt.setText(R.string.txt_status_not_set);
        }
    }

    public Date onUpdate() {
        String wakeTimeRel = inputTimeTxt.getText().toString();
        Calendar cal = Calendar.getInstance();

        Date wakeUpDate = MyDateUtils.calculateRelativeTimeTo(cal, wakeTimeRel, false);
        if (wakeUpDate != null) {
            String wakeTimeStr = SDF_TIME.format(wakeUpDate);
            statusTxt.setText(getString(R.string.txt_status_alarm_on, wakeTimeStr));
            prefs.setWakeTimeRelative(wakeTimeRel);
            prefs.setWakeTime(wakeTimeStr);
        } else {
            statusTxt.setText(getString(R.string.txt_status_wrong_format));
        }

        return wakeUpDate;
    }
}
