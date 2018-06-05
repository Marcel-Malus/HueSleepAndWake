package com.recek.huewakeup.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.quickstart.BridgeHolder;
import com.recek.huesleepwake.R;
import com.recek.huewakeup.util.DefaultSchedules;

public class HelpActivity extends AppCompatActivity {

    private Button resetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        resetBtn = findViewById(R.id.resetButton);
        resetBtn.setEnabled(true);
    }

    public void resetSchedules(View view) {
        if (BridgeHolder.hasBridge()) {
            new DefaultSchedules(HueSharedPreferences.getInstance(this)).reset();
            // Prevent button spamming
            resetBtn.setEnabled(false);
            Toast.makeText(this, "Reset in progress. Check schedule settings for result.",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Cannot reset. No connection to Hue Bridge.", Toast.LENGTH_LONG)
                    .show();
        }
    }
}
