/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

public class HardwareInfoCollector {
    private SystemInfo systemInfo = new SystemInfo();
    private HardwareAbstractionLayer hardware = this.systemInfo.getHardware();
    private OperatingSystem os = this.systemInfo.getOperatingSystem();

    public String getCPUInfo() {
        CentralProcessor processor = this.hardware.getProcessor();
        String cpuName = processor.getProcessorIdentifier().getName();
        return cpuName;
    }

    public String getGPUInfo() {
        StringBuilder gpuInfo = new StringBuilder();
        for (GraphicsCard gpu : this.hardware.getGraphicsCards()) {
            gpuInfo.append(gpu.getName()).append(", ");
        }
        if (gpuInfo.length() > 0) {
            gpuInfo.setLength(gpuInfo.length() - 2);
        }
        return gpuInfo.toString();
    }

    public long getRAMSize() {
        GlobalMemory memory = this.hardware.getMemory();
        return memory.getTotal() / 0x100000L;
    }

    public long getStorageSize() {
        long totalStorage = 0L;
        for (HWDiskStore disk : this.hardware.getDiskStores()) {
            totalStorage += disk.getSize();
        }
        return totalStorage / 0x100000L;
    }

    public String getMotherboardInfo() {
        ComputerSystem computerSystem = this.hardware.getComputerSystem();
        return "Manufacturer: " + computerSystem.getBaseboard().getManufacturer() + "; Model: " + computerSystem.getBaseboard().getModel() + "; Version: " + computerSystem.getBaseboard().getVersion();
    }

    public String getOSInfo() {
        return this.os.toString();
    }
}

