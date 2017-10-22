package com.recek.huewakeup.settings;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;

import com.philips.lighting.data.HueSharedPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.philips.lighting.data.HueSharedPreferences.DAYS;

/**
 * @since 2017-10-22.
 */
class DayCheckBoxAdapter extends BaseAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(DayCheckBoxAdapter.class);

    private final HueSharedPreferences prefs;
    private final List<Boolean> wakeDaysList;
    private final Context context;

    private GridView parent = null;

    DayCheckBoxAdapter(Context context, HueSharedPreferences prefs) {
        this.context = context;
        this.prefs = prefs;
        wakeDaysList = prefs.getWakeDays();
    }

    public int getCount() {
        return DAYS.length;
    }

    public Object getItem(int position) {
        return DAYS[position];
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        CheckBox checkBox;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            checkBox = new CheckBox(context);
            checkBox.setPadding(0, 8, 0, 8);
        } else {
            checkBox = (CheckBox) convertView;
        }
        if (this.parent == null) {
            this.parent = (GridView) parent;
        }

        checkBox.setText(DAYS[position]);
        checkBox.setChecked(wakeDaysList.get(position));
        return checkBox;
    }

    boolean saveToPrefs() {
        wakeDaysList.clear();
        for (int i = 0; i < parent.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) parent.getChildAt(i);
            LOG.debug("{}: {}", checkBox.getText(), checkBox.isChecked());
            wakeDaysList.add(checkBox.isChecked());
        }

        return prefs.setWakeDays(wakeDaysList);
    }
}
