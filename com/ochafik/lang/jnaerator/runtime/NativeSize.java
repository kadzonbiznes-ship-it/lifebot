/*
 * Decompiled with CFR 0.152.
 */
package com.ochafik.lang.jnaerator.runtime;

import com.sun.jna.IntegerType;
import com.sun.jna.Native;

public class NativeSize
extends IntegerType {
    public static int SIZE = Native.SIZE_T_SIZE;

    public NativeSize() {
        this(0L);
    }

    public NativeSize(long l) {
        super(SIZE, l);
    }
}

