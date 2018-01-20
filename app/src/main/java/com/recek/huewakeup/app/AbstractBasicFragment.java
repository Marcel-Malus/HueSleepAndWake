package com.recek.huewakeup.app;

import android.app.Fragment;
import android.widget.TextView;

import com.recek.huesleepwake.R;
import com.recek.huewakeup.util.MyDateUtils;

import java.util.Date;

/**
 * Since 20/01/2018.
 */
public abstract class AbstractBasicFragment extends Fragment {

    protected abstract long getSavedTime();

    protected abstract TextView getStatusTxt();

    protected void updateStatus() {
        long savedTime = getSavedTime();
        if (savedTime != -1) {
            if (savedTime > new Date().getTime()) {
                String savedTimeStr = MyDateUtils.SDF_TIME_SHORT.format(new Date(savedTime));
                getStatusTxt().setText(
                        getResources().getString(R.string.txt_status_alarm_on, savedTimeStr));
            } else {
                getStatusTxt().setText(R.string.txt_status_outdated);
            }
        } else {
            getStatusTxt().setText(R.string.txt_status_not_set);
        }
    }
}
