package com.philips.lighting.quickstart;

import android.app.Application;

import com.philips.lighting.hue.sdk.wrapper.HueLog;
import com.philips.lighting.hue.sdk.wrapper.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.philips.lighting.hue.sdk.wrapper.HueLog.LogComponent.CLIENT;
import static com.philips.lighting.hue.sdk.wrapper.HueLog.LogComponent.NETWORK;
import static com.philips.lighting.hue.sdk.wrapper.HueLog.LogComponent.WRAPPER;
import static com.philips.lighting.hue.sdk.wrapper.HueLog.LogLevel.DEBUG;
import static com.philips.lighting.hue.sdk.wrapper.HueLog.LogLevel.INFO;

public class HueQuickStartApp extends Application {

    static {
        // Load the huesdk native library before calling any SDK method
        System.loadLibrary("huesdk");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Logger LOG = LoggerFactory.getLogger(HueQuickStartApp.class);
        LOG.info("HueQuickStartApp OnCreate.");

        // Configure the storage location and log level for the Hue SDK
        Persistence.setStorageLocation(getFilesDir().getAbsolutePath(), "HueQuickStart");
        HueLog.setConsoleLogLevel(INFO);
        HueLog.setFileLogLevel(DEBUG, NETWORK + CLIENT + WRAPPER);
    }
}
