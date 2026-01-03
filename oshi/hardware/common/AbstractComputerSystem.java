/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.Immutable
 */
package oshi.hardware.common;

import java.util.function.Supplier;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.Baseboard;
import oshi.hardware.ComputerSystem;
import oshi.hardware.Firmware;
import oshi.util.Memoizer;

@Immutable
public abstract class AbstractComputerSystem
implements ComputerSystem {
    private final Supplier<Firmware> firmware = Memoizer.memoize(this::createFirmware);
    private final Supplier<Baseboard> baseboard = Memoizer.memoize(this::createBaseboard);

    @Override
    public Firmware getFirmware() {
        return this.firmware.get();
    }

    protected abstract Firmware createFirmware();

    @Override
    public Baseboard getBaseboard() {
        return this.baseboard.get();
    }

    protected abstract Baseboard createBaseboard();

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("manufacturer=").append(this.getManufacturer()).append(", ");
        sb.append("model=").append(this.getModel()).append(", ");
        sb.append("serial number=").append(this.getSerialNumber()).append(", ");
        sb.append("uuid=").append(this.getHardwareUUID());
        return sb.toString();
    }
}

