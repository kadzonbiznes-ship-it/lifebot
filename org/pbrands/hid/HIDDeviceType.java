/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.hid;

import lombok.Generated;

public enum HIDDeviceType {
    KFC("Interception"),
    UBER("Razer HID");

    private final String deviceName;

    private HIDDeviceType(String deviceName) {
        this.deviceName = deviceName;
    }

    public String toString() {
        return this.deviceName;
    }

    @Generated
    public String getDeviceName() {
        return this.deviceName;
    }
}

