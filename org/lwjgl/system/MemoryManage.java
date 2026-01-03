/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Checks;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryAccessJNI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.libc.LibCStdlib;

final class MemoryManage {
    private MemoryManage() {
    }

    static MemoryUtil.MemoryAllocator getInstance() {
        Object allocator = Configuration.MEMORY_ALLOCATOR.get();
        if (allocator instanceof MemoryUtil.MemoryAllocator) {
            return (MemoryUtil.MemoryAllocator)allocator;
        }
        if (!"system".equals(allocator)) {
            String className = allocator == null || "jemalloc".equals(allocator) ? "org.lwjgl.system.jemalloc.JEmallocAllocator" : ("rpmalloc".equals(allocator) ? "org.lwjgl.system.rpmalloc.RPmallocAllocator" : allocator.toString());
            try {
                Class<?> allocatorClass = Class.forName(className);
                return (MemoryUtil.MemoryAllocator)allocatorClass.getConstructor(new Class[0]).newInstance(new Object[0]);
            }
            catch (Throwable t) {
                if (Checks.DEBUG && (allocator != null || !(t instanceof ClassNotFoundException))) {
                    t.printStackTrace(APIUtil.DEBUG_STREAM);
                }
                APIUtil.apiLog(String.format("Warning: Failed to instantiate memory allocator: %s. Using the system default.", className));
            }
        }
        return new StdlibAllocator();
    }

    private static class StdlibAllocator
    implements MemoryUtil.MemoryAllocator {
        private StdlibAllocator() {
        }

        @Override
        public long getMalloc() {
            return MemoryAccessJNI.malloc;
        }

        @Override
        public long getCalloc() {
            return MemoryAccessJNI.calloc;
        }

        @Override
        public long getRealloc() {
            return MemoryAccessJNI.realloc;
        }

        @Override
        public long getFree() {
            return MemoryAccessJNI.free;
        }

        @Override
        public long getAlignedAlloc() {
            return MemoryAccessJNI.aligned_alloc;
        }

        @Override
        public long getAlignedFree() {
            return MemoryAccessJNI.aligned_free;
        }

        @Override
        public long malloc(long size) {
            return LibCStdlib.nmalloc(size);
        }

        @Override
        public long calloc(long num, long size) {
            return LibCStdlib.ncalloc(num, size);
        }

        @Override
        public long realloc(long ptr, long size) {
            return LibCStdlib.nrealloc(ptr, size);
        }

        @Override
        public void free(long ptr) {
            LibCStdlib.nfree(ptr);
        }

        @Override
        public long aligned_alloc(long alignment, long size) {
            return LibCStdlib.naligned_alloc(alignment, size);
        }

        @Override
        public void aligned_free(long ptr) {
            LibCStdlib.naligned_free(ptr);
        }
    }
}

