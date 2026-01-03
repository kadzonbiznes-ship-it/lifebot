/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.libc.LibCString;

final class MultiReleaseMemCopy {
    private MultiReleaseMemCopy() {
    }

    static void copy(long src, long dst, long bytes) {
        if (bytes <= 160L) {
            if (Pointer.BITS64 && ((src | dst) & 7L) == 0L) {
                MemoryUtil.memCopyAligned64(src, dst, (int)bytes & 0xFF);
            } else {
                MemoryUtil.UNSAFE.copyMemory(null, src, null, dst, bytes);
            }
            return;
        }
        LibCString.nmemcpy(dst, src, bytes);
    }
}

