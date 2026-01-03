/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 */
package oshi.driver.windows.wmi;

import com.sun.jna.platform.win32.COM.WbemcliUtil;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.platform.windows.WmiQueryHandler;

@ThreadSafe
public final class Win32DiskPartition {
    private static final String WIN32_DISK_PARTITION = "Win32_DiskPartition";

    private Win32DiskPartition() {
    }

    public static WbemcliUtil.WmiResult<DiskPartitionProperty> queryPartition(WmiQueryHandler h) {
        WbemcliUtil.WmiQuery<DiskPartitionProperty> partitionQuery = new WbemcliUtil.WmiQuery<DiskPartitionProperty>(WIN32_DISK_PARTITION, DiskPartitionProperty.class);
        return h.queryWMI(partitionQuery, false);
    }

    public static enum DiskPartitionProperty {
        INDEX,
        DESCRIPTION,
        DEVICEID,
        DISKINDEX,
        NAME,
        SIZE,
        TYPE;

    }
}

