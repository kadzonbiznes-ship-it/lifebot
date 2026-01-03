/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

public class NativeThread {
    private static final long VIRTUAL_THREAD_ID = -1L;

    public static long current() {
        if (Thread.currentThread().isVirtual()) {
            return -1L;
        }
        return 0L;
    }

    static long currentNativeThread() {
        return 0L;
    }

    static void signal(long tid) {
        throw new UnsupportedOperationException();
    }

    static boolean isNativeThread(long tid) {
        return false;
    }

    static boolean isVirtualThread(long tid) {
        return tid == -1L;
    }
}

