/*
 * Decompiled with CFR 0.152.
 */
package com.ochafik.lang.jnaerator.runtime;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.ptr.ByReference;

public class NativeSizeByReference
extends ByReference {
    public NativeSizeByReference() {
        this(new NativeSize(0L));
    }

    public NativeSizeByReference(NativeSize nativeSize) {
        super(NativeSize.SIZE);
        this.setValue(nativeSize);
    }

    public void setValue(NativeSize nativeSize) {
        if (NativeSize.SIZE == 4) {
            this.getPointer().setInt(0L, nativeSize.intValue());
        } else if (NativeSize.SIZE == 8) {
            this.getPointer().setLong(0L, nativeSize.longValue());
        } else {
            throw new RuntimeException("GCCLong has to be either 4 or 8 bytes.");
        }
    }

    public NativeSize getValue() {
        if (NativeSize.SIZE == 4) {
            return new NativeSize((long)this.getPointer().getInt(0L));
        }
        if (NativeSize.SIZE == 8) {
            return new NativeSize(this.getPointer().getLong(0L));
        }
        throw new RuntimeException("GCCLong has to be either 4 or 8 bytes.");
    }
}

