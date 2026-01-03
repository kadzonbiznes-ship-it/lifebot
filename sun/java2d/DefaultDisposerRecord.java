/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d;

import sun.java2d.DisposerRecord;

public class DefaultDisposerRecord
implements DisposerRecord {
    private long dataPointer;
    private long disposerMethodPointer;

    public DefaultDisposerRecord(long disposerMethodPointer, long dataPointer) {
        this.disposerMethodPointer = disposerMethodPointer;
        this.dataPointer = dataPointer;
    }

    @Override
    public void dispose() {
        DefaultDisposerRecord.invokeNativeDispose(this.disposerMethodPointer, this.dataPointer);
    }

    public long getDataPointer() {
        return this.dataPointer;
    }

    public long getDisposerMethodPointer() {
        return this.disposerMethodPointer;
    }

    public static native void invokeNativeDispose(long var0, long var2);
}

