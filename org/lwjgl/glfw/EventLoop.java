/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.macosx.LibC
 *  org.lwjgl.system.macosx.ObjCRuntime
 */
package org.lwjgl.glfw;

import org.lwjgl.system.Configuration;
import org.lwjgl.system.JNI;
import org.lwjgl.system.Platform;
import org.lwjgl.system.macosx.LibC;
import org.lwjgl.system.macosx.ObjCRuntime;

final class EventLoop {
    private EventLoop() {
    }

    static void check() {
        if (Platform.get() == Platform.MACOSX && !EventLoop.isMainThread()) {
            throw new IllegalStateException(EventLoop.isJavaStartedOnFirstThread() ? "GLFW may only be used on the main thread. This check may be disabled with Configuration.GLFW_CHECK_THREAD0." : "GLFW may only be used on the main thread and that thread must be the first thread in the process. Please run the JVM with -XstartOnFirstThread. This check may be disabled with Configuration.GLFW_CHECK_THREAD0.");
        }
    }

    private static boolean isMainThread() {
        if (!Configuration.GLFW_CHECK_THREAD0.get(true).booleanValue() || Configuration.GLFW_LIBRARY_NAME.get("").contains("glfw_async")) {
            return true;
        }
        long objc_msgSend = ObjCRuntime.getLibrary().getFunctionAddress("objc_msgSend");
        long NSThread = ObjCRuntime.objc_getClass((CharSequence)"NSThread");
        long currentThread = JNI.invokePPP(NSThread, ObjCRuntime.sel_getUid((CharSequence)"currentThread"), objc_msgSend);
        return JNI.invokePPZ(currentThread, ObjCRuntime.sel_getUid((CharSequence)"isMainThread"), objc_msgSend);
    }

    private static boolean isJavaStartedOnFirstThread() {
        return "1".equals(System.getenv().get("JAVA_STARTED_ON_FIRST_THREAD_" + LibC.getpid()));
    }
}

