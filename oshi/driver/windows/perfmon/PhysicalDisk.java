/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 */
package oshi.driver.windows.perfmon;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.windows.perfmon.PerfmonDisabled;
import oshi.util.platform.windows.PerfCounterWildcardQuery;
import oshi.util.tuples.Pair;

@ThreadSafe
public final class PhysicalDisk {
    private PhysicalDisk() {
    }

    public static Pair<List<String>, Map<PhysicalDiskProperty, List<Long>>> queryDiskCounters() {
        if (PerfmonDisabled.PERF_DISK_DISABLED) {
            return new Pair<List<String>, Map<PhysicalDiskProperty, List<Long>>>(Collections.emptyList(), Collections.emptyMap());
        }
        return PerfCounterWildcardQuery.queryInstancesAndValues(PhysicalDiskProperty.class, "PhysicalDisk", "Win32_PerfRawData_PerfDisk_PhysicalDisk WHERE Name!=\"_Total\"");
    }

    public static enum PhysicalDiskProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty
    {
        NAME("^_Total"),
        DISKREADSPERSEC("Disk Reads/sec"),
        DISKREADBYTESPERSEC("Disk Read Bytes/sec"),
        DISKWRITESPERSEC("Disk Writes/sec"),
        DISKWRITEBYTESPERSEC("Disk Write Bytes/sec"),
        CURRENTDISKQUEUELENGTH("Current Disk Queue Length"),
        PERCENTDISKTIME("% Disk Time");

        private final String counter;

        private PhysicalDiskProperty(String counter) {
            this.counter = counter;
        }

        @Override
        public String getCounter() {
            return this.counter;
        }
    }
}

