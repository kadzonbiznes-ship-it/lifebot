/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.MemoryManage$DebugAllocator
 */
package org.lwjgl.system;

import java.lang.reflect.Method;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.CallbackI;
import org.lwjgl.system.Checks;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryManage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.jni.JNINativeInterface;
import org.lwjgl.system.libffi.FFICIF;
import org.lwjgl.system.libffi.FFIClosure;
import org.lwjgl.system.libffi.LibFFI;

public abstract class Callback
implements Pointer,
NativeResource {
    private static final boolean DEBUG_ALLOCATOR = Configuration.DEBUG_MEMORY_ALLOCATOR.get(false);
    private static final ClosureRegistry CLOSURE_REGISTRY;
    private static final long CALLBACK_HANDLER;
    private long address;

    protected Callback(FFICIF cif) {
        this.address = Callback.create(cif, this);
    }

    protected Callback(long address) {
        if (Checks.CHECKS) {
            Checks.check(address);
        }
        this.address = address;
    }

    @Override
    public long address() {
        return this.address;
    }

    @Override
    public void free() {
        Callback.free(this.address());
    }

    private static native long getCallbackHandler(Method var0);

    static long create(FFICIF cif, Object instance) {
        long executableAddress;
        FFIClosure closure;
        try (MemoryStack stack = MemoryStack.stackPush();){
            PointerBuffer code = stack.mallocPointer(1);
            closure = LibFFI.ffi_closure_alloc(FFIClosure.SIZEOF, code);
            if (closure == null) {
                throw new OutOfMemoryError();
            }
            executableAddress = code.get(0);
            if (DEBUG_ALLOCATOR) {
                MemoryManage.DebugAllocator.track((long)executableAddress, (long)FFIClosure.SIZEOF);
            }
        }
        long user_data = JNINativeInterface.NewGlobalRef(instance);
        int errcode = LibFFI.ffi_prep_closure_loc(closure, cif, CALLBACK_HANDLER, user_data, executableAddress);
        if (errcode != 0) {
            JNINativeInterface.DeleteGlobalRef(user_data);
            LibFFI.ffi_closure_free(closure);
            throw new RuntimeException("Failed to prepare the libffi closure");
        }
        CLOSURE_REGISTRY.put(executableAddress, closure);
        return executableAddress;
    }

    public static <T extends CallbackI> T get(long functionPointer) {
        return (T)((CallbackI)MemoryUtil.memGlobalRefToObject(CLOSURE_REGISTRY.get(functionPointer).user_data()));
    }

    public static <T extends CallbackI> @Nullable T getSafe(long functionPointer) {
        return functionPointer == 0L ? null : (T)Callback.get(functionPointer);
    }

    public static void free(long functionPointer) {
        if (DEBUG_ALLOCATOR) {
            MemoryManage.DebugAllocator.untrack((long)functionPointer);
        }
        FFIClosure closure = CLOSURE_REGISTRY.remove(functionPointer);
        JNINativeInterface.DeleteGlobalRef(closure.user_data());
        LibFFI.ffi_closure_free(closure);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Callback)) {
            return false;
        }
        Callback that = (Callback)o;
        return this.address == that.address();
    }

    public int hashCode() {
        return (int)(this.address ^ this.address >>> 32);
    }

    public String toString() {
        return String.format("%s pointer [0x%X]", this.getClass().getSimpleName(), this.address);
    }

    static {
        try (MemoryStack stack = MemoryStack.stackPush();){
            PointerBuffer code = stack.mallocPointer(1);
            FFIClosure closure = LibFFI.ffi_closure_alloc(FFIClosure.SIZEOF, code);
            if (closure == null) {
                throw new OutOfMemoryError();
            }
            if (code.get(0) == closure.address()) {
                APIUtil.apiLog("Closure Registry: simple");
                CLOSURE_REGISTRY = new ClosureRegistry(){

                    @Override
                    public void put(long executableAddress, FFIClosure closure) {
                    }

                    @Override
                    public FFIClosure get(long executableAddress) {
                        return FFIClosure.create(executableAddress);
                    }

                    @Override
                    public FFIClosure remove(long executableAddress) {
                        return this.get(executableAddress);
                    }
                };
            } else {
                APIUtil.apiLog("Closure Registry: ConcurrentHashMap");
                CLOSURE_REGISTRY = new /* Unavailable Anonymous Inner Class!! */;
            }
            LibFFI.ffi_closure_free(closure);
        }
        try {
            CALLBACK_HANDLER = Callback.getCallbackHandler(CallbackI.class.getDeclaredMethod("callback", Long.TYPE, Long.TYPE));
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to initialize the native callback handler.", e);
        }
        MemoryUtil.getAllocator();
    }

    private static interface ClosureRegistry {
        public void put(long var1, FFIClosure var3);

        public FFIClosure get(long var1);

        public FFIClosure remove(long var1);
    }
}

