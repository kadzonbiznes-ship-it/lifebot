/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 */
package oshi.hardware.platform.windows;

import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.WbemcliUtil;
import com.sun.jna.platform.win32.Kernel32;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.windows.perfmon.PhysicalDisk;
import oshi.driver.windows.wmi.Win32DiskDrive;
import oshi.driver.windows.wmi.Win32DiskDriveToDiskPartition;
import oshi.driver.windows.wmi.Win32DiskPartition;
import oshi.driver.windows.wmi.Win32LogicalDiskToPartition;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.hardware.common.AbstractHWDiskStore;
import oshi.util.ParseUtil;
import oshi.util.platform.windows.WmiQueryHandler;
import oshi.util.platform.windows.WmiUtil;
import oshi.util.tuples.Pair;

@ThreadSafe
public final class WindowsHWDiskStore
extends AbstractHWDiskStore {
    private static final Logger LOG = LoggerFactory.getLogger(WindowsHWDiskStore.class);
    private static final String PHYSICALDRIVE_PREFIX = "\\\\.\\PHYSICALDRIVE";
    private static final Pattern DEVICE_ID = Pattern.compile(".*\\.DeviceID=\"(.*)\"");
    private static final int GUID_BUFSIZE = 100;
    private long reads = 0L;
    private long readBytes = 0L;
    private long writes = 0L;
    private long writeBytes = 0L;
    private long currentQueueLength = 0L;
    private long transferTime = 0L;
    private long timeStamp = 0L;
    private List<HWPartition> partitionList;

    private WindowsHWDiskStore(String name, String model, String serial, long size) {
        super(name, model, serial, size);
    }

    @Override
    public long getReads() {
        return this.reads;
    }

    @Override
    public long getReadBytes() {
        return this.readBytes;
    }

    @Override
    public long getWrites() {
        return this.writes;
    }

    @Override
    public long getWriteBytes() {
        return this.writeBytes;
    }

    @Override
    public long getCurrentQueueLength() {
        return this.currentQueueLength;
    }

    @Override
    public long getTransferTime() {
        return this.transferTime;
    }

    @Override
    public long getTimeStamp() {
        return this.timeStamp;
    }

    @Override
    public List<HWPartition> getPartitions() {
        return this.partitionList;
    }

    @Override
    public boolean updateAttributes() {
        String index = null;
        List<HWPartition> partitions = this.getPartitions();
        if (!partitions.isEmpty()) {
            index = Integer.toString(partitions.get(0).getMajor());
        } else if (this.getName().startsWith(PHYSICALDRIVE_PREFIX)) {
            index = this.getName().substring(PHYSICALDRIVE_PREFIX.length(), this.getName().length());
        } else {
            LOG.warn("Couldn't match index for {}", (Object)this.getName());
            return false;
        }
        DiskStats stats = WindowsHWDiskStore.queryReadWriteStats(index);
        if (stats.readMap.containsKey(index)) {
            this.reads = stats.readMap.getOrDefault(index, 0L);
            this.readBytes = stats.readByteMap.getOrDefault(index, 0L);
            this.writes = stats.writeMap.getOrDefault(index, 0L);
            this.writeBytes = stats.writeByteMap.getOrDefault(index, 0L);
            this.currentQueueLength = stats.queueLengthMap.getOrDefault(index, 0L);
            this.transferTime = stats.diskTimeMap.getOrDefault(index, 0L);
            this.timeStamp = stats.timeStamp;
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static List<HWDiskStore> getDisks() {
        WmiQueryHandler h = Objects.requireNonNull(WmiQueryHandler.createInstance());
        boolean comInit = false;
        try {
            comInit = h.initCOM();
            ArrayList<HWDiskStore> result = new ArrayList<HWDiskStore>();
            DiskStats stats = WindowsHWDiskStore.queryReadWriteStats(null);
            PartitionMaps maps = WindowsHWDiskStore.queryPartitionMaps(h);
            WbemcliUtil.WmiResult<Win32DiskDrive.DiskDriveProperty> vals = Win32DiskDrive.queryDiskDrive(h);
            for (int i = 0; i < vals.getResultCount(); ++i) {
                WindowsHWDiskStore ds = new WindowsHWDiskStore(WmiUtil.getString(vals, Win32DiskDrive.DiskDriveProperty.NAME, i), String.format(Locale.ROOT, "%s %s", WmiUtil.getString(vals, Win32DiskDrive.DiskDriveProperty.MODEL, i), WmiUtil.getString(vals, Win32DiskDrive.DiskDriveProperty.MANUFACTURER, i)).trim(), ParseUtil.hexStringToString(WmiUtil.getString(vals, Win32DiskDrive.DiskDriveProperty.SERIALNUMBER, i)), WmiUtil.getUint64(vals, Win32DiskDrive.DiskDriveProperty.SIZE, i));
                String index = Integer.toString(WmiUtil.getUint32(vals, Win32DiskDrive.DiskDriveProperty.INDEX, i));
                ds.reads = stats.readMap.getOrDefault(index, 0L);
                ds.readBytes = stats.readByteMap.getOrDefault(index, 0L);
                ds.writes = stats.writeMap.getOrDefault(index, 0L);
                ds.writeBytes = stats.writeByteMap.getOrDefault(index, 0L);
                ds.currentQueueLength = stats.queueLengthMap.getOrDefault(index, 0L);
                ds.transferTime = stats.diskTimeMap.getOrDefault(index, 0L);
                ds.timeStamp = stats.timeStamp;
                ArrayList partitions = new ArrayList();
                List partList = (List)maps.driveToPartitionMap.get(ds.getName());
                if (partList != null && !partList.isEmpty()) {
                    for (String part : partList) {
                        if (!maps.partitionMap.containsKey(part)) continue;
                        partitions.addAll((Collection)maps.partitionMap.get(part));
                    }
                }
                ds.partitionList = Collections.unmodifiableList(partitions.stream().sorted(Comparator.comparing(HWPartition::getName)).collect(Collectors.toList()));
                result.add(ds);
            }
            ArrayList<HWDiskStore> arrayList = result;
            return arrayList;
        }
        catch (COMException e) {
            LOG.warn("COM exception: {}", (Object)e.getMessage());
            List<HWDiskStore> list = Collections.emptyList();
            return list;
        }
        finally {
            if (comInit) {
                h.unInitCOM();
            }
        }
    }

    private static DiskStats queryReadWriteStats(String index) {
        DiskStats stats = new DiskStats();
        Pair<List<String>, Map<PhysicalDisk.PhysicalDiskProperty, List<Long>>> instanceValuePair = PhysicalDisk.queryDiskCounters();
        List<String> instances = instanceValuePair.getA();
        Map<PhysicalDisk.PhysicalDiskProperty, List<Long>> valueMap = instanceValuePair.getB();
        stats.timeStamp = System.currentTimeMillis();
        List<Long> readList = valueMap.get(PhysicalDisk.PhysicalDiskProperty.DISKREADSPERSEC);
        List<Long> readByteList = valueMap.get(PhysicalDisk.PhysicalDiskProperty.DISKREADBYTESPERSEC);
        List<Long> writeList = valueMap.get(PhysicalDisk.PhysicalDiskProperty.DISKWRITESPERSEC);
        List<Long> writeByteList = valueMap.get(PhysicalDisk.PhysicalDiskProperty.DISKWRITEBYTESPERSEC);
        List<Long> queueLengthList = valueMap.get(PhysicalDisk.PhysicalDiskProperty.CURRENTDISKQUEUELENGTH);
        List<Long> diskTimeList = valueMap.get(PhysicalDisk.PhysicalDiskProperty.PERCENTDISKTIME);
        if (instances.isEmpty() || readList == null || readByteList == null || writeList == null || writeByteList == null || queueLengthList == null || diskTimeList == null) {
            return stats;
        }
        for (int i = 0; i < instances.size(); ++i) {
            String name = WindowsHWDiskStore.getIndexFromName(instances.get(i));
            if (index != null && !index.equals(name)) continue;
            stats.readMap.put(name, readList.get(i));
            stats.readByteMap.put(name, readByteList.get(i));
            stats.writeMap.put(name, writeList.get(i));
            stats.writeByteMap.put(name, writeByteList.get(i));
            stats.queueLengthMap.put(name, queueLengthList.get(i));
            stats.diskTimeMap.put(name, diskTimeList.get(i) / 10000L);
        }
        return stats;
    }

    private static PartitionMaps queryPartitionMaps(WmiQueryHandler h) {
        Matcher mDep;
        Matcher mAnt;
        PartitionMaps maps = new PartitionMaps();
        WbemcliUtil.WmiResult<Win32DiskDriveToDiskPartition.DriveToPartitionProperty> drivePartitionMap = Win32DiskDriveToDiskPartition.queryDriveToPartition(h);
        for (int i = 0; i < drivePartitionMap.getResultCount(); ++i) {
            mAnt = DEVICE_ID.matcher(WmiUtil.getRefString(drivePartitionMap, Win32DiskDriveToDiskPartition.DriveToPartitionProperty.ANTECEDENT, i));
            mDep = DEVICE_ID.matcher(WmiUtil.getRefString(drivePartitionMap, Win32DiskDriveToDiskPartition.DriveToPartitionProperty.DEPENDENT, i));
            if (!mAnt.matches() || !mDep.matches()) continue;
            maps.driveToPartitionMap.computeIfAbsent(mAnt.group(1).replace("\\\\", "\\"), x -> new ArrayList()).add(mDep.group(1));
        }
        WbemcliUtil.WmiResult<Win32LogicalDiskToPartition.DiskToPartitionProperty> diskPartitionMap = Win32LogicalDiskToPartition.queryDiskToPartition(h);
        for (int i = 0; i < diskPartitionMap.getResultCount(); ++i) {
            mAnt = DEVICE_ID.matcher(WmiUtil.getRefString(diskPartitionMap, Win32LogicalDiskToPartition.DiskToPartitionProperty.ANTECEDENT, i));
            mDep = DEVICE_ID.matcher(WmiUtil.getRefString(diskPartitionMap, Win32LogicalDiskToPartition.DiskToPartitionProperty.DEPENDENT, i));
            long size = WmiUtil.getUint64(diskPartitionMap, Win32LogicalDiskToPartition.DiskToPartitionProperty.ENDINGADDRESS, i) - WmiUtil.getUint64(diskPartitionMap, Win32LogicalDiskToPartition.DiskToPartitionProperty.STARTINGADDRESS, i) + 1L;
            if (!mAnt.matches() || !mDep.matches()) continue;
            if (maps.partitionToLogicalDriveMap.containsKey(mAnt.group(1))) {
                ((List)maps.partitionToLogicalDriveMap.get(mAnt.group(1))).add(new Pair<String, Long>(mDep.group(1) + "\\", size));
                continue;
            }
            ArrayList<Pair<String, Long>> list = new ArrayList<Pair<String, Long>>();
            list.add(new Pair<String, Long>(mDep.group(1) + "\\", size));
            maps.partitionToLogicalDriveMap.put(mAnt.group(1), list);
        }
        WbemcliUtil.WmiResult<Win32DiskPartition.DiskPartitionProperty> hwPartitionQueryMap = Win32DiskPartition.queryPartition(h);
        for (int i = 0; i < hwPartitionQueryMap.getResultCount(); ++i) {
            String deviceID = WmiUtil.getString(hwPartitionQueryMap, Win32DiskPartition.DiskPartitionProperty.DEVICEID, i);
            List logicalDrives = (List)maps.partitionToLogicalDriveMap.get(deviceID);
            if (logicalDrives == null) continue;
            for (int j = 0; j < logicalDrives.size(); ++j) {
                Pair logicalDrive = (Pair)logicalDrives.get(j);
                if (logicalDrive == null || ((String)logicalDrive.getA()).isEmpty()) continue;
                char[] volumeChr = new char[100];
                Kernel32.INSTANCE.GetVolumeNameForVolumeMountPoint((String)logicalDrive.getA(), volumeChr, 100);
                String uuid = ParseUtil.parseUuidOrDefault(new String(volumeChr).trim(), "");
                HWPartition pt = new HWPartition(WmiUtil.getString(hwPartitionQueryMap, Win32DiskPartition.DiskPartitionProperty.NAME, i), WmiUtil.getString(hwPartitionQueryMap, Win32DiskPartition.DiskPartitionProperty.TYPE, i), WmiUtil.getString(hwPartitionQueryMap, Win32DiskPartition.DiskPartitionProperty.DESCRIPTION, i), uuid, (Long)logicalDrive.getB(), WmiUtil.getUint32(hwPartitionQueryMap, Win32DiskPartition.DiskPartitionProperty.DISKINDEX, i), WmiUtil.getUint32(hwPartitionQueryMap, Win32DiskPartition.DiskPartitionProperty.INDEX, i), (String)logicalDrive.getA());
                if (maps.partitionMap.containsKey(deviceID)) {
                    ((List)maps.partitionMap.get(deviceID)).add(pt);
                    continue;
                }
                ArrayList<HWPartition> ptlist = new ArrayList<HWPartition>();
                ptlist.add(pt);
                maps.partitionMap.put(deviceID, ptlist);
            }
        }
        return maps;
    }

    private static String getIndexFromName(String s) {
        if (s.isEmpty()) {
            return s;
        }
        return s.split("\\s")[0];
    }

    private static final class DiskStats {
        private final Map<String, Long> readMap = new HashMap<String, Long>();
        private final Map<String, Long> readByteMap = new HashMap<String, Long>();
        private final Map<String, Long> writeMap = new HashMap<String, Long>();
        private final Map<String, Long> writeByteMap = new HashMap<String, Long>();
        private final Map<String, Long> queueLengthMap = new HashMap<String, Long>();
        private final Map<String, Long> diskTimeMap = new HashMap<String, Long>();
        private long timeStamp;

        private DiskStats() {
        }
    }

    private static final class PartitionMaps {
        private final Map<String, List<String>> driveToPartitionMap = new HashMap<String, List<String>>();
        private final Map<String, List<Pair<String, Long>>> partitionToLogicalDriveMap = new HashMap<String, List<Pair<String, Long>>>();
        private final Map<String, List<HWPartition>> partitionMap = new HashMap<String, List<HWPartition>>();

        private PartitionMaps() {
        }
    }
}

