/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 */
package oshi.hardware;

import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface Sensors {
    public double getCpuTemperature();

    public int[] getFanSpeeds();

    public double getCpuVoltage();
}

