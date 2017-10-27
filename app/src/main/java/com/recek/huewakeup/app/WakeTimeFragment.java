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

        MainActivity activity = (MainActivity) getActivity();
        prefs = activity.getPrefs();

        inputTimeTxt = (EditText) getActivity().findViewById(R.id.wakeTime);
        inputTimeTxt.setText(prefs.getWakeTime());

        statusTxt = (TextView) getActivity().findViewById(R.id.wakeStatusTxt);
//        setInitialStatus(prefs.getWakeTime());
    }

    public Date onUpdate() {
        String wakeTimeStr = inputTimeTxt.getText().toString();
        Calendar cal = Calendar.getInstance();

        Date wakeUpDate = MyDateUtils.calculateRelativeTimeTo(cal, wakeTimeStr, false);
        if (wakeUpDate != null) {
            prefs.setWakeTime(wakeTimeStr);
            statusTxt.setText(getString(R.string.txt_status_alarm_on, SDF_TIME.format(wakeUpDate)));
        } else {
            statusTxt.setText(getString(R.string.txt_status_wrong_format));
        }

        return wakeUpDate;
    }
}
