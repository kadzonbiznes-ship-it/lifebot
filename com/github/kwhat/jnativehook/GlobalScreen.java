/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.kwhat.jnativehook.DefaultLibraryLocator
 *  com.github.kwhat.jnativehook.NativeLibraryLocator
 */
package com.github.kwhat.jnativehook;

import com.github.kwhat.jnativehook.DefaultLibraryLocator;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.NativeLibraryLocator;
import com.github.kwhat.jnativehook.NativeMonitorInfo;
import com.github.kwhat.jnativehook.dispatcher.DefaultDispatchService;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseMotionListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelListener;
import java.io.File;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;

public class GlobalScreen {
    protected static Logger log;
    protected static NativeHookThread hookThread;
    protected static ExecutorService eventExecutor;
    protected static EventListenerList eventListeners;

    protected GlobalScreen() {
    }

    public static void addNativeKeyListener(NativeKeyListener listener) {
        if (listener != null) {
            eventListeners.add(NativeKeyListener.class, listener);
        }
    }

    public static void removeNativeKeyListener(NativeKeyListener listener) {
        if (listener != null) {
            eventListeners.remove(NativeKeyListener.class, listener);
        }
    }

    public static void addNativeMouseListener(NativeMouseListener listener) {
        if (listener != null) {
            eventListeners.add(NativeMouseListener.class, listener);
        }
    }

    public static void removeNativeMouseListener(NativeMouseListener listener) {
        if (listener != null) {
            eventListeners.remove(NativeMouseListener.class, listener);
        }
    }

    public static void addNativeMouseMotionListener(NativeMouseMotionListener listener) {
        if (listener != null) {
            eventListeners.add(NativeMouseMotionListener.class, listener);
        }
    }

    public static void removeNativeMouseMotionListener(NativeMouseMotionListener listener) {
        if (listener != null) {
            eventListeners.remove(NativeMouseMotionListener.class, listener);
        }
    }

    public static void addNativeMouseWheelListener(NativeMouseWheelListener listener) {
        if (listener != null) {
            eventListeners.add(NativeMouseWheelListener.class, listener);
        }
    }

    public static void removeNativeMouseWheelListener(NativeMouseWheelListener listener) {
        if (listener != null) {
            eventListeners.remove(NativeMouseWheelListener.class, listener);
        }
    }

    public static native NativeMonitorInfo[] getNativeMonitors();

    public static native Integer getAutoRepeatRate();

    public static native Integer getAutoRepeatDelay();

    public static native Integer getPointerAccelerationMultiplier();

    public static native Integer getPointerAccelerationThreshold();

    public static native Integer getPointerSensitivity();

    public static native Integer getMultiClickIterval();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void registerNativeHook() throws NativeHookException {
        if (eventExecutor != null) {
            if (!eventExecutor.isShutdown()) {
                eventExecutor.shutdown();
            }
            while (!eventExecutor.isTerminated()) {
                Thread.yield();
            }
        } else {
            eventExecutor = new DefaultDispatchService();
        }
        if (hookThread == null || !hookThread.isAlive()) {
            NativeHookThread nativeHookThread = hookThread = new NativeHookThread();
            synchronized (nativeHookThread) {
                hookThread.start();
                try {
                    hookThread.wait();
                }
                catch (InterruptedException e) {
                    throw new NativeHookException(e);
                }
                NativeHookException exception = hookThread.getException();
                if (exception != null) {
                    throw exception;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void unregisterNativeHook() throws NativeHookException {
        if (GlobalScreen.isNativeHookRegistered()) {
            NativeHookThread nativeHookThread = hookThread;
            synchronized (nativeHookThread) {
                try {
                    hookThread.disable();
                    hookThread.join();
                }
                catch (Exception e) {
                    throw new NativeHookException(e.getCause());
                }
            }
            eventExecutor.shutdown();
        }
    }

    public static boolean isNativeHookRegistered() {
        return hookThread != null && hookThread.isAlive();
    }

    public static native void postNativeEvent(NativeInputEvent var0);

    public static void setEventDispatcher(ExecutorService dispatcher) {
        if (eventExecutor != null) {
            eventExecutor.shutdown();
        }
        eventExecutor = dispatcher;
    }

    static {
        Integer pointerAccelerationThreshold;
        Integer pointerAccelerationMultiplier;
        Integer pointerSensitivity;
        Integer multiClickIterval;
        Integer autoRepeatDelay;
        log = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        eventListeners = new EventListenerList();
        String libName = System.getProperty("jnativehook.lib.name", "JNativeHook");
        try {
            System.loadLibrary(libName);
        }
        catch (UnsatisfiedLinkError linkError) {
            String libLoader = System.getProperty("jnativehook.lib.locator", DefaultLibraryLocator.class.getCanonicalName());
            try {
                NativeLibraryLocator locator = Class.forName(libLoader).asSubclass(NativeLibraryLocator.class).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                Iterator libs = locator.getLibraries();
                while (libs.hasNext()) {
                    File lib = (File)libs.next();
                    if (!lib.exists() || !lib.isFile() || !lib.canRead()) continue;
                    System.load(lib.getPath());
                }
            }
            catch (Exception e) {
                log.severe(e.getMessage());
                throw new UnsatisfiedLinkError(e.getMessage());
            }
        }
        Integer autoRepeatRate = GlobalScreen.getAutoRepeatRate();
        if (autoRepeatRate != null) {
            System.setProperty("jnativehook.key.repeat.rate", autoRepeatRate.toString());
        }
        if ((autoRepeatDelay = GlobalScreen.getAutoRepeatDelay()) != null) {
            System.setProperty("jnativehook.key.repeat.delay", autoRepeatDelay.toString());
        }
        if ((multiClickIterval = GlobalScreen.getMultiClickIterval()) != null) {
            System.setProperty("jnativehook.button.multiclick.iterval", multiClickIterval.toString());
        }
        if ((pointerSensitivity = GlobalScreen.getPointerSensitivity()) != null) {
            System.setProperty("jnativehook.pointer.sensitivity", pointerSensitivity.toString());
        }
        if ((pointerAccelerationMultiplier = GlobalScreen.getPointerAccelerationMultiplier()) != null) {
            System.setProperty("jnativehook.pointer.acceleration.multiplier", pointerAccelerationMultiplier.toString());
        }
        if ((pointerAccelerationThreshold = GlobalScreen.getPointerAccelerationThreshold()) != null) {
            System.setProperty("jnativehook.pointer.acceleration.threshold", pointerAccelerationThreshold.toString());
        }
    }

    protected static class NativeHookThread
    extends Thread {
        protected NativeHookException exception;

        public NativeHookThread() {
            this.setName("JNativeHook Hook Thread");
            this.setDaemon(false);
            this.setPriority(10);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void run() {
            this.exception = null;
            try {
                this.enable();
            }
            catch (NativeHookException e) {
                this.exception = e;
            }
            NativeHookThread nativeHookThread = this;
            synchronized (nativeHookThread) {
                this.notifyAll();
            }
        }

        public NativeHookException getException() {
            return this.exception;
        }

        protected native void enable() throws NativeHookException;

        public native void disable() throws NativeHookException;

        protected static void dispatchEvent(NativeInputEvent event) {
            if (eventExecutor != null) {
                eventExecutor.execute(new EventDispatchTask(event));
            }
        }
    }

    private static class EventDispatchTask
    implements Runnable {
        private final NativeInputEvent event;

        public EventDispatchTask(NativeInputEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            if (this.event instanceof NativeKeyEvent) {
                this.processKeyEvent((NativeKeyEvent)this.event);
            } else if (this.event instanceof NativeMouseWheelEvent) {
                this.processMouseWheelEvent((NativeMouseWheelEvent)this.event);
            } else if (this.event instanceof NativeMouseEvent) {
                switch (this.event.getID()) {
                    case 2500: 
                    case 2501: 
                    case 2502: {
                        this.processButtonEvent((NativeMouseEvent)this.event);
                        break;
                    }
                    case 2503: 
                    case 2504: {
                        this.processMouseEvent((NativeMouseEvent)this.event);
                    }
                }
            }
        }

        private void processKeyEvent(NativeKeyEvent nativeEvent) {
            NativeKeyListener[] listeners;
            block5: for (NativeKeyListener listener : listeners = (NativeKeyListener[])eventListeners.getListeners(NativeKeyListener.class)) {
                switch (nativeEvent.getID()) {
                    case 2401: {
                        listener.nativeKeyPressed(nativeEvent);
                        continue block5;
                    }
                    case 2400: {
                        listener.nativeKeyTyped(nativeEvent);
                        continue block5;
                    }
                    case 2402: {
                        listener.nativeKeyReleased(nativeEvent);
                    }
                }
            }
        }

        private void processButtonEvent(NativeMouseEvent nativeEvent) {
            NativeMouseListener[] listeners;
            block5: for (NativeMouseListener listener : listeners = (NativeMouseListener[])eventListeners.getListeners(NativeMouseListener.class)) {
                switch (nativeEvent.getID()) {
                    case 2500: {
                        listener.nativeMouseClicked(nativeEvent);
                        continue block5;
                    }
                    case 2501: {
                        listener.nativeMousePressed(nativeEvent);
                        continue block5;
                    }
                    case 2502: {
                        listener.nativeMouseReleased(nativeEvent);
                    }
                }
            }
        }

        private void processMouseEvent(NativeMouseEvent nativeEvent) {
            NativeMouseMotionListener[] listeners;
            block4: for (NativeMouseMotionListener listener : listeners = (NativeMouseMotionListener[])eventListeners.getListeners(NativeMouseMotionListener.class)) {
                switch (nativeEvent.getID()) {
                    case 2503: {
                        listener.nativeMouseMoved(nativeEvent);
                        continue block4;
                    }
                    case 2504: {
                        listener.nativeMouseDragged(nativeEvent);
                    }
                }
            }
        }

        private void processMouseWheelEvent(NativeMouseWheelEvent nativeEvent) {
            NativeMouseWheelListener[] listeners;
            for (NativeMouseWheelListener listener : listeners = (NativeMouseWheelListener[])eventListeners.getListeners(NativeMouseWheelListener.class)) {
                listener.nativeMouseWheelMoved(nativeEvent);
            }
        }
    }
}

