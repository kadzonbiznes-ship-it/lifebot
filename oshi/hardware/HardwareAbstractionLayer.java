/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 *  oshi.hardware.Display
 *  oshi.hardware.LogicalVolumeGroup
 *  oshi.hardware.NetworkIF
 *  oshi.hardware.PowerSource
 *  oshi.hardware.SoundCard
 *  oshi.hardware.UsbDevice
 */
package oshi.hardware;

import java.util.Collections;
import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.Display;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HWDiskStore;
import oshi.hardware.LogicalVolumeGroup;
import oshi.hardware.NetworkIF;
import oshi.hardware.PowerSource;
import oshi.hardware.Sensors;
import oshi.hardware.SoundCard;
import oshi.hardware.UsbDevice;

@ThreadSafe
public interface HardwareAbstractionLayer {
    public ComputerSystem getComputerSystem();

    public CentralProcessor getProcessor();

    public GlobalMemory getMemory();

    public List<PowerSource> getPowerSources();

    public List<HWDiskStore> getDiskStores();

    default public List<LogicalVolumeGroup> getLogicalVolumeGroups() {
        return Collections.emptyList();
    }

    public List<NetworkIF> getNetworkIFs();

    public List<NetworkIF> getNetworkIFs(boolean var1);

    public List<Display> getDisplays();

    public Sensors getSensors();

    public List<UsbDevice> getUsbDevices(boolean var1);

    public List<SoundCard> getSoundCards();

    public List<GraphicsCard> getGraphicsCards();
}

