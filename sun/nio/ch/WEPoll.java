/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.IOException;
import jdk.internal.misc.Unsafe;
import sun.nio.ch.IOUtil;

class WEPoll {
    private static final Unsafe UNSAFE = Unsafe.getUnsafe();
    private static final int ADDRESS_SIZE = UNSAFE.addressSize();
    private static final int SIZEOF_EPOLLEVENT;
    private static final int OFFSETOF_EVENTS;
    private static final int OFFSETOF_SOCK;
    static final int EPOLL_CTL_ADD = 1;
    static final int EPOLL_CTL_MOD = 2;
    static final int EPOLL_CTL_DEL = 3;
    static final int EPOLLIN = 1;
    static final int EPOLLPRI = 2;
    static final int EPOLLOUT = 4;
    static final int EPOLLERR = 8;
    static final int EPOLLHUP = 16;
    static final int EPOLLONESHOT = Integer.MIN_VALUE;

    private WEPoll() {
    }

    static long allocatePollArray(int count) {
        long size = (long)count * (long)SIZEOF_EPOLLEVENT;
        long base = UNSAFE.allocateMemory(size);
        UNSAFE.setMemory(base, size, (byte)0);
        return base;
    }

    static void freePollArray(long address) {
        UNSAFE.freeMemory(address);
    }

    static long getEvent(long address, int i) {
        return address + (long)(SIZEOF_EPOLLEVENT * i);
    }

    static long getSocket(long eventAddress) {
        if (ADDRESS_SIZE == 8) {
            return UNSAFE.getLong(eventAddress + (long)OFFSETOF_SOCK);
        }
        return UNSAFE.getInt(eventAddress + (long)OFFSETOF_SOCK);
    }

    static int getDescriptor(long eventAddress) {
        long s = WEPoll.getSocket(eventAddress);
        int fd = (int)s;
        assert ((long)fd == s);
        return fd;
    }

    static int getEvents(long eventAddress) {
        return UNSAFE.getInt(eventAddress + (long)OFFSETOF_EVENTS);
    }

    private static native int eventSize();

    private static native int eventsOffset();

    private static native int dataOffset();

    static native long create() throws IOException;

    static native int ctl(long var0, int var2, long var3, int var5);

    static native int wait(long var0, long var2, int var4, int var5) throws IOException;

    static native void close(long var0);

    static {
        IOUtil.load();
        SIZEOF_EPOLLEVENT = WEPoll.eventSize();
        OFFSETOF_EVENTS = WEPoll.eventsOffset();
        OFFSETOF_SOCK = WEPoll.dataOffset();
    }
}

