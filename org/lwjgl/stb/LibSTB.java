/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.stb;

import org.lwjgl.system.Configuration;
import org.lwjgl.system.Library;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Platform;

final class LibSTB {
    private LibSTB() {
    }

    static void initialize() {
    }

    private static native void setupMalloc(long var0, long var2, long var4, long var6, long var8, long var10);

    static {
        String libName = Platform.mapLibraryNameBundled("lwjgl_stb");
        Library.loadSystem(System::load, System::loadLibrary, LibSTB.class, "org.lwjgl.stb", libName);
        MemoryUtil.MemoryAllocator allocator = MemoryUtil.getAllocator(Configuration.DEBUG_MEMORY_ALLOCATOR_INTERNAL.get(true));
        LibSTB.setupMalloc(allocator.getMalloc(), allocator.getCalloc(), allocator.getRealloc(), allocator.getFree(), allocator.getAlignedAlloc(), allocator.getAlignedFree());
    }
}

