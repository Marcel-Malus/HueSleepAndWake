package com.philips.lighting.util;

import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.hue.sdk.PHAccessPoint;

/**
 * @since 2017-12-10.
 */
public final class PHUtil {

    public static PHAccessPoint loadLastAccessPointConnected(HueSharedPreferences prefs) {
        String lastIpAddress = prefs.getLastConnectedIPAddress();
        String lastUsername = prefs.getUsername();

        if (lastIpAddress != null && !lastIpAddress.equals("")) {
            PHAccessPoint lastAccessPoint = new PHAccessPoint();
            lastAccessPoint.setIpAddress(lastIpAddress);
            lastAccessPoint.setUsername(lastUsername);

            return lastAccessPoint;
        }
        return null;
    }
}
