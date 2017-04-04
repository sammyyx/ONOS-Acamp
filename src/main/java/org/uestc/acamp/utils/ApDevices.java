package org.uestc.acamp.utils;

/**
 * Created by sammy on 17-3-28.
 * tools for ApDevice
 */
public class ApDevices {
    public static int apId = 0;

    public static int assignNewApId() {
        apId++;
        return apId;
    }
}
