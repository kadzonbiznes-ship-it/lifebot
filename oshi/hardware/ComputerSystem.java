/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.Immutable
 */
package oshi.hardware;

import oshi.annotation.concurrent.Immutable;
import oshi.hardware.Baseboard;
import oshi.hardware.Firmware;

@Immutable
public interface ComputerSystem {
    public String getManufacturer();

    public String getModel();

    public String getSerialNumber();

    public String getHardwareUUID();

    public Firmware getFirmware();

    public Baseboard getBaseboard();
}

