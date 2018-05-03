package com.philips.lighting.quickstart;

import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;

/**
 * Since 23/01/2018.
 */
public class BridgeHolder {

    private static final BridgeHolder INSTANCE = new BridgeHolder();

    private Bridge bridge = null;

    public static Bridge get() {
        return INSTANCE.bridge;
    }

    public static void set(Bridge bridge) {
        INSTANCE.bridge = bridge;
    }

    public static void clear() {
        INSTANCE.bridge = null;
    }

    private BridgeHolder() {
    }
}
