/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 */
package oshi.hardware;

import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface VirtualMemory {
    public long getSwapTotal();

    public long getSwapUsed();

    public long getVirtualMax();

    public long getVirtualInUse();

    public long getSwapPagesIn();

    public long getSwapPagesOut();
}

