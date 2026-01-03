/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.Immutable
 */
package oshi.hardware;

import oshi.annotation.concurrent.Immutable;

@Immutable
public interface GraphicsCard {
    public String getName();

    public String getDeviceId();

    public String getVendor();

    public String getVersionInfo();

    public long getVRam();
}

