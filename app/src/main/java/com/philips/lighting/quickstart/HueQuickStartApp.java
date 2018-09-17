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
        String absolutePath = getFilesDir().getAbsolutePath();
        LOG.info("Storage Location: " + absolutePath);
        Persistence.setStorageLocation(absolutePath, "HueQuickStart");
        HueLog.setConsoleLogLevel(INFO);
        HueLog.setFileLogLevel(DEBUG, NETWORK + CLIENT + WRAPPER);

        // Setup handler for uncaught exceptions (For file logging).
        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                logUncaughtException(e);
                defaultUncaughtExceptionHandler.uncaughtException(thread, e);
            }
        });
    }

    public void logUncaughtException(Throwable e) {
        Logger LOG = LoggerFactory.getLogger(HueQuickStartApp.class);
        LOG.error("App crashed! Message: " + e.getMessage());
        LOG.error("Logging Stack Trace (top 5):");
        StackTraceElement[] stackTrace = e.getStackTrace();
        int maxFive = stackTrace.length < 5 ? stackTrace.length : 5;
        for (int i = 0; i < maxFive; i++) {
            StackTraceElement stackTraceElement = stackTrace[i];
            LOG.error(stackTraceElement.toString());
        }
    }
}
