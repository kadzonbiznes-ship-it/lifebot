/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.internal.stream.util;

import com.sun.xml.internal.stream.util.BufferAllocator;
import java.lang.ref.SoftReference;

public class ThreadLocalBufferAllocator {
    private static final ThreadLocal<SoftReference<BufferAllocator>> TL = new ThreadLocal();

    public static BufferAllocator getBufferAllocator() {
        BufferAllocator ba = null;
        SoftReference<BufferAllocator> sr = TL.get();
        if (sr != null) {
            ba = sr.get();
        }
        if (ba == null) {
            ba = new BufferAllocator();
            sr = new SoftReference<BufferAllocator>(ba);
            TL.set(sr);
        }
        return ba;
    }
}

