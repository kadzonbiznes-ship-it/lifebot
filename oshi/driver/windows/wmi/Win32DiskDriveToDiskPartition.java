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
public final class Win32DiskDriveToDiskPartition {
    private static final String WIN32_DISK_DRIVE_TO_DISK_PARTITION = "Win32_DiskDriveToDiskPartition";

    private Win32DiskDriveToDiskPartition() {
    }

    public static WbemcliUtil.WmiResult<DriveToPartitionProperty> queryDriveToPartition(WmiQueryHandler h) {
        WbemcliUtil.WmiQuery<DriveToPartitionProperty> driveToPartitionQuery = new WbemcliUtil.WmiQuery<DriveToPartitionProperty>(WIN32_DISK_DRIVE_TO_DISK_PARTITION, DriveToPartitionProperty.class);
        return h.queryWMI(driveToPartitionQuery, false);
    }

    public static enum DriveToPartitionProperty {
        ANTECEDENT,
        DEPENDENT;

    }
}

