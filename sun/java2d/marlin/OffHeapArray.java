/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import jdk.internal.misc.Unsafe;
import jdk.internal.ref.CleanerFactory;
import sun.java2d.marlin.MarlinConst;
import sun.java2d.marlin.MarlinUtils;

final class OffHeapArray {
    static final Unsafe UNSAFE = Unsafe.getUnsafe();
    static final int SIZE_INT = Unsafe.ARRAY_INT_INDEX_SCALE;
    long address;
    long length;
    int used;

    OffHeapArray(Object parent, long len) {
        this.address = UNSAFE.allocateMemory(len);
        this.length = len;
        this.used = 0;
        if (MarlinConst.LOG_UNSAFE_MALLOC) {
            MarlinUtils.logInfo(System.currentTimeMillis() + ": OffHeapArray.allocateMemory =   " + len + " to addr = " + this.address);
        }
        CleanerFactory.cleaner().register(parent, this::free);
    }

    void resize(long len) {
        this.address = UNSAFE.reallocateMemory(this.address, len);
        this.length = len;
        if (MarlinConst.LOG_UNSAFE_MALLOC) {
            MarlinUtils.logInfo(System.currentTimeMillis() + ": OffHeapArray.reallocateMemory = " + len + " to addr = " + this.address);
        }
    }

    void free() {
        UNSAFE.freeMemory(this.address);
        if (MarlinConst.LOG_UNSAFE_MALLOC) {
            MarlinUtils.logInfo(System.currentTimeMillis() + ": OffHeapArray.freeMemory =       " + this.length + " at addr = " + this.address);
        }
        this.address = 0L;
    }

    void fill(byte val) {
        UNSAFE.setMemory(this.address, this.length, val);
    }
}

