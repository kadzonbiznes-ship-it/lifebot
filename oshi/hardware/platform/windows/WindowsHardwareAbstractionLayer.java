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
 *  oshi.hardware.platform.windows.WindowsDisplay
 *  oshi.hardware.platform.windows.WindowsLogicalVolumeGroup
 *  oshi.hardware.platform.windows.WindowsNetworkIF
 *  oshi.hardware.platform.windows.WindowsPowerSource
 *  oshi.hardware.platform.windows.WindowsSensors
 *  oshi.hardware.platform.windows.WindowsSoundCard
 *  oshi.hardware.platform.windows.WindowsUsbDevice
 */
package oshi.hardware.platform.windows;

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
import oshi.hardware.common.AbstractHardwareAbstractionLayer;
import oshi.hardware.platform.windows.WindowsCentralProcessor;
import oshi.hardware.platform.windows.WindowsComputerSystem;
import oshi.hardware.platform.windows.WindowsDisplay;
import oshi.hardware.platform.windows.WindowsGlobalMemory;
import oshi.hardware.platform.windows.WindowsGraphicsCard;
import oshi.hardware.platform.windows.WindowsHWDiskStore;
import oshi.hardware.platform.windows.WindowsLogicalVolumeGroup;
import oshi.hardware.platform.windows.WindowsNetworkIF;
import oshi.hardware.platform.windows.WindowsPowerSource;
import oshi.hardware.platform.windows.WindowsSensors;
import oshi.hardware.platform.windows.WindowsSoundCard;
import oshi.hardware.platform.windows.WindowsUsbDevice;

@ThreadSafe
public final class WindowsHardwareAbstractionLayer
extends AbstractHardwareAbstractionLayer {
    @Override
    public ComputerSystem createComputerSystem() {
        return new WindowsComputerSystem();
    }

    @Override
    public GlobalMemory createMemory() {
        return new WindowsGlobalMemory();
    }

    @Override
    public CentralProcessor createProcessor() {
        return new WindowsCentralProcessor();
    }

    @Override
    public Sensors createSensors() {
        return new WindowsSensors();
    }

    @Override
    public List<PowerSource> getPowerSources() {
        return WindowsPowerSource.getPowerSources();
    }

    @Override
    public List<HWDiskStore> getDiskStores() {
        return WindowsHWDiskStore.getDisks();
    }

    @Override
    public List<LogicalVolumeGroup> getLogicalVolumeGroups() {
        return WindowsLogicalVolumeGroup.getLogicalVolumeGroups();
    }

    @Override
    public List<Display> getDisplays() {
        return WindowsDisplay.getDisplays();
    }

    @Override
    public List<NetworkIF> getNetworkIFs(boolean includeLocalInterfaces) {
        return WindowsNetworkIF.getNetworks((boolean)includeLocalInterfaces);
    }

    @Override
    public List<UsbDevice> getUsbDevices(boolean tree) {
        return WindowsUsbDevice.getUsbDevices((boolean)tree);
    }

    @Override
    public List<SoundCard> getSoundCards() {
        return WindowsSoundCard.getSoundCards();
    }

    @Override
    public List<GraphicsCard> getGraphicsCards() {
        return WindowsGraphicsCard.getGraphicsCards();
    }
}

