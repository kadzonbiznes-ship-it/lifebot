/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.Channel;
import java.util.concurrent.TimeUnit;
import sun.nio.ch.Net;
import sun.nio.ch.Poller;
import sun.nio.ch.SelectionKeyImpl;

public interface SelChImpl
extends Channel {
    public FileDescriptor getFD();

    public int getFDVal();

    public boolean translateAndUpdateReadyOps(int var1, SelectionKeyImpl var2);

    public boolean translateAndSetReadyOps(int var1, SelectionKeyImpl var2);

    public int translateInterestOps(int var1);

    public void kill() throws IOException;

    default public void park(int event, long nanos) throws IOException {
        if (Thread.currentThread().isVirtual()) {
            Poller.poll(this.getFDVal(), event, nanos, this::isOpen);
        } else {
            long millis;
            if (nanos <= 0L) {
                millis = -1L;
            } else {
                millis = TimeUnit.NANOSECONDS.toMillis(nanos);
                if (nanos > TimeUnit.MILLISECONDS.toNanos(millis)) {
                    ++millis;
                }
            }
            Net.poll(this.getFD(), event, millis);
        }
    }

    default public void park(int event) throws IOException {
        this.park(event, 0L);
    }
}

