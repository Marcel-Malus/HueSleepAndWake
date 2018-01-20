package com.recek.huewakeup.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.philips.lighting.data.HueSharedPreferences;
import com.recek.huesleepwake.R;
import com.recek.huewakeup.util.MyDateUtils;

import java.util.Calendar;
import java.util.Date;

import static com.recek.huewakeup.util.MyDateUtils.SDF_TIME_SHORT;

/**
 * @since 2017-09-14.
 */
public class WakeTimeFragment extends AbstractBasicFragment {

    private EditText inputTimeTxt;
    private TextView statusTxt;
    private HueSharedPreferences prefs;

    @Override
    protected long getSavedTime() {
        return prefs.getWakeTime();
    }

    @Override
    protected TextView getStatusTxt() {
        return statusTxt;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wake_time, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prefs = HueSharedPreferences.getInstance(getActivity());

        inputTimeTxt = getActivity().findViewById(R.id.wakeTime);
        inputTimeTxt.setText(prefs.getWakeTimeRelative());

        statusTxt = getActivity().findViewById(R.id.wakeStatusTxt);
        updateStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatus();
    }

    public Date onUpdate() {
        String wakeTimeRel = inputTimeTxt.getText().toString();
        Calendar cal = Calendar.getInstance();

        Date wakeUpDate = MyDateUtils.calculateRelativeTimeTo(cal, wakeTimeRel, false);
        if (wakeUpDate != null) {
            String wakeTimeStr = SDF_TIME_SHORT.format(wakeUpDate);
            statusTxt.setText(getString(R.string.txt_status_alarm_on, wakeTimeStr));
            prefs.setWakeTimeRelative(wakeTimeRel);
            prefs.setWakeTime(wakeUpDate.getTime());
        } else {
            statusTxt.setText(getString(R.string.txt_status_wrong_format));
        }

        return wakeUpDate;
    }
}
