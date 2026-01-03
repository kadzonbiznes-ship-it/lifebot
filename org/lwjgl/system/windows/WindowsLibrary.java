/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.windows.WindowsUtil
 */
package org.lwjgl.system.windows;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Library;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.SharedLibrary;
import org.lwjgl.system.windows.WinBase;
import org.lwjgl.system.windows.WindowsUtil;

public class WindowsLibrary
extends SharedLibrary.Default {
    public static final long HINSTANCE;

    public WindowsLibrary(String name) {
        this(name, WindowsLibrary.loadLibrary(name));
    }

    public WindowsLibrary(String name, long handle) {
        super(name, handle);
    }

    private static long loadLibrary(String name) {
        long handle;
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer pi = stack.mallocInt(1);
            handle = WinBase.LoadLibrary(pi, stack.UTF16(name));
            if (handle == 0L) {
                throw new UnsatisfiedLinkError("Failed to load library: " + name + " (error code = " + pi.get(0) + ")");
            }
        }
        return handle;
    }

    @Override
    public @Nullable String getPath() {
        int maxLen = 256;
        ByteBuffer buffer = MemoryUtil.memAlloc(maxLen);
        try {
            while (true) {
                String string;
                int err;
                int len;
                try (MemoryStack stack = MemoryStack.stackPush();){
                    IntBuffer pi = stack.mallocInt(1);
                    len = WinBase.GetModuleFileName(pi, this.address(), buffer);
                    err = pi.get(0);
                }
                if (err == 0) {
                    string = len == 0 ? null : MemoryUtil.memUTF16(buffer, len);
                    return string;
                }
                if (err != 122) {
                    string = null;
                    return string;
                }
                maxLen = maxLen * 3 / 2;
                buffer = MemoryUtil.memRealloc(buffer, maxLen);
            }
        }
        finally {
            MemoryUtil.memFree(buffer);
        }
    }

    @Override
    public long getFunctionAddress(ByteBuffer functionName) {
        return WinBase.GetProcAddress(null, this.address(), functionName);
    }

    @Override
    public void free() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer pi = stack.mallocInt(1);
            if (!WinBase.FreeLibrary(pi, this.address())) {
                WindowsUtil.windowsThrowException((String)("Failed to unload library: " + this.getName()), (IntBuffer)pi);
            }
        }
    }

    static {
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer pi = stack.mallocInt(1);
            HINSTANCE = WinBase.GetModuleHandle(pi, stack.UTF16(Library.JNI_LIBRARY_NAME));
            if (HINSTANCE == 0L) {
                WindowsUtil.windowsThrowException((String)"Failed to retrieve LWJGL module handle.", (IntBuffer)pi);
            }
        }
    }
}

