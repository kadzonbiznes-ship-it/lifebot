/*
 * Decompiled with CFR 0.152.
 */
package com.github.kwhat.jnativehook;

public class NativeHookException
extends Exception {
    private static final long serialVersionUID = 8952825837670265527L;
    private int code = 0;
    public static final int UNSPECIFIED_FAILURE = 0;
    public static final int HOOK_FAILURE = 1;
    public static final int X11_OPEN_DISPLAY = 32;
    public static final int X11_RECORD_NOT_FOUND = 33;
    public static final int X11_RECORD_ALLOC_RANGE = 34;
    public static final int X11_RECORD_CREATE_CONTEXT = 35;
    public static final int X11_RECORD_ENABLE_CONTEXT = 36;
    public static final int X11_RECORD_GET_CONTEXT = 37;
    public static final int WIN_SET_HOOK = 48;
    public static final int DARWIN_AXAPI_DISABLED = 64;
    public static final int DARWIN_CREATE_EVENT_PORT = 65;
    public static final int DARWIN_CREATE_RUN_LOOP_SOURCE = 66;
    public static final int DARWIN_GET_RUNLOOP = 67;
    public static final int DARWIN_CREATE_OBSERVER = 68;

    public NativeHookException() {
    }

    public NativeHookException(int code) {
        this.code = code;
    }

    public NativeHookException(String message) {
        super(message);
    }

    public NativeHookException(int code, String message) {
        super(message);
        this.code = code;
    }

    public NativeHookException(String message, Throwable cause) {
        super(message, cause);
    }

    public NativeHookException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public NativeHookException(Throwable cause) {
        super(cause);
    }

    public NativeHookException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}

