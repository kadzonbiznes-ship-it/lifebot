/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 */
package oshi.hardware;

import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.HWPartition;

@ThreadSafe
public interface HWDiskStore {
    public String getName();

    public String getModel();

    public String getSerial();

    public long getSize();

    public long getReads();

    public long getReadBytes();

    public long getWrites();

    public long getWriteBytes();

    public long getCurrentQueueLength();

    public long getTransferTime();

    public List<HWPartition> getPartitions();

    public long getTimeStamp();

    public boolean updateAttributes();
}

