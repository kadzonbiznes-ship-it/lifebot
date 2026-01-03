/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 *  oshi.hardware.PhysicalMemory
 *  oshi.util.ExecutingCommand
 *  oshi.util.FormatUtil
 */
package oshi.hardware.common;

import java.util.ArrayList;
import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.GlobalMemory;
import oshi.hardware.PhysicalMemory;
import oshi.util.ExecutingCommand;
import oshi.util.FormatUtil;
import oshi.util.ParseUtil;

@ThreadSafe
public abstract class AbstractGlobalMemory
implements GlobalMemory {
    @Override
    public List<PhysicalMemory> getPhysicalMemory() {
        ArrayList<PhysicalMemory> pmList = new ArrayList<PhysicalMemory>();
        List dmi = ExecutingCommand.runNative((String)"dmidecode --type 17");
        int bank = 0;
        String bankLabel = "unknown";
        String locator = "";
        long capacity = 0L;
        long speed = 0L;
        String manufacturer = "unknown";
        String memoryType = "unknown";
        String partNumber = "unknown";
        String serialNumber = "unknown";
        for (String line : dmi) {
            String[] split;
            if (line.trim().contains("DMI type 17")) {
                if (bank++ <= 0) continue;
                if (capacity > 0L) {
                    pmList.add(new PhysicalMemory(bankLabel + locator, capacity, speed, manufacturer, memoryType, partNumber, serialNumber));
                }
                bankLabel = "unknown";
                locator = "";
                capacity = 0L;
                speed = 0L;
                continue;
            }
            if (bank <= 0 || (split = line.trim().split(":")).length != 2) continue;
            switch (split[0]) {
                case "Bank Locator": {
                    bankLabel = split[1].trim();
                    break;
                }
                case "Locator": {
                    locator = "/" + split[1].trim();
                    break;
                }
                case "Size": {
                    capacity = ParseUtil.parseDecimalMemorySizeToBinary(split[1].trim());
                    break;
                }
                case "Type": {
                    memoryType = split[1].trim();
                    break;
                }
                case "Speed": {
                    speed = ParseUtil.parseHertz(split[1]);
                    break;
                }
                case "Manufacturer": {
                    manufacturer = split[1].trim();
                    break;
                }
                case "PartNumber": 
                case "Part Number": {
                    partNumber = split[1].trim();
                    break;
                }
                case "Serial Number": {
                    serialNumber = split[1].trim();
                    break;
                }
            }
        }
        if (capacity > 0L) {
            pmList.add(new PhysicalMemory(bankLabel + locator, capacity, speed, manufacturer, memoryType, partNumber, serialNumber));
        }
        return pmList;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available: ");
        sb.append(FormatUtil.formatBytes((long)this.getAvailable()));
        sb.append("/");
        sb.append(FormatUtil.formatBytes((long)this.getTotal()));
        return sb.toString();
    }
}

