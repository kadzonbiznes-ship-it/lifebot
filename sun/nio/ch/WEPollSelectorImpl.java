/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import jdk.internal.misc.Blocker;
import sun.nio.ch.IOUtil;
import sun.nio.ch.Net;
import sun.nio.ch.PipeImpl;
import sun.nio.ch.SelectionKeyImpl;
import sun.nio.ch.SelectorImpl;
import sun.nio.ch.WEPoll;

class WEPollSelectorImpl
extends SelectorImpl {
    private static final int NUM_EPOLLEVENTS = 256;
    private final long eph;
    private final long pollArrayAddress;
    private final Map<Integer, SelectionKeyImpl> fdToKey = new HashMap<Integer, SelectionKeyImpl>();
    private final Object updateLock = new Object();
    private final Deque<SelectionKeyImpl> updateKeys = new ArrayDeque<SelectionKeyImpl>();
    private final Object interruptLock = new Object();
    private boolean interruptTriggered;
    private final PipeImpl pipe;
    private final int fd0Val;
    private final int fd1Val;

    WEPollSelectorImpl(SelectorProvider sp) throws IOException {
        super(sp);
        this.eph = WEPoll.create();
        this.pollArrayAddress = WEPoll.allocatePollArray(256);
        try {
            this.pipe = new PipeImpl(sp, true, false);
        }
        catch (IOException ioe) {
            WEPoll.freePollArray(this.pollArrayAddress);
            WEPoll.close(this.eph);
            throw ioe;
        }
        this.fd0Val = this.pipe.source().getFDVal();
        this.fd1Val = this.pipe.sink().getFDVal();
        WEPoll.ctl(this.eph, 1, this.fd0Val, 1);
    }

    private void ensureOpen() {
        if (!this.isOpen()) {
            throw new ClosedSelectorException();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected int doSelect(Consumer<SelectionKey> action, long timeout) throws IOException {
        int numEntries;
        assert (Thread.holdsLock(this));
        int to = (int)Math.min(timeout, Integer.MAX_VALUE);
        boolean blocking = to != 0;
        this.processUpdateQueue();
        this.processDeregisterQueue();
        try {
            this.begin(blocking);
            long comp = Blocker.begin(blocking);
            try {
                numEntries = WEPoll.wait(this.eph, this.pollArrayAddress, 256, to);
            }
            finally {
                Blocker.end(comp);
            }
        }
        finally {
            this.end(blocking);
        }
        this.processDeregisterQueue();
        return this.processEvents(numEntries, action);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void processUpdateQueue() {
        assert (Thread.holdsLock(this));
        Object object = this.updateLock;
        synchronized (object) {
            SelectionKeyImpl ski;
            while ((ski = this.updateKeys.pollFirst()) != null) {
                int registeredOps;
                if (!ski.isValid()) continue;
                int fd = ski.getFDVal();
                SelectionKeyImpl previous = this.fdToKey.putIfAbsent(fd, ski);
                assert (previous == null || previous == ski);
                int newOps = ski.translateInterestOps();
                if (newOps == (registeredOps = ski.registeredEvents())) continue;
                if (newOps == 0) {
                    WEPoll.ctl(this.eph, 3, fd, 0);
                } else {
                    int events = WEPollSelectorImpl.toEPollEvents(newOps);
                    if (registeredOps == 0) {
                        WEPoll.ctl(this.eph, 1, fd, events);
                    } else {
                        WEPoll.ctl(this.eph, 2, fd, events);
                    }
                }
                ski.registeredEvents(newOps);
            }
        }
    }

    private int processEvents(int numEntries, Consumer<SelectionKey> action) throws IOException {
        assert (Thread.holdsLock(this));
        boolean interrupted = false;
        int numKeysUpdated = 0;
        for (int i = 0; i < numEntries; ++i) {
            long event = WEPoll.getEvent(this.pollArrayAddress, i);
            int fd = WEPoll.getDescriptor(event);
            if (fd == this.fd0Val) {
                interrupted = true;
                continue;
            }
            SelectionKeyImpl ski = this.fdToKey.get(fd);
            if (ski == null) continue;
            int events = WEPoll.getEvents(event);
            if ((events & 2) != 0) {
                Net.discardOOB(ski.getFD());
            }
            int rOps = WEPollSelectorImpl.toReadyOps(events);
            numKeysUpdated += this.processReadyEvents(rOps, ski, action);
        }
        if (interrupted) {
            this.clearInterrupt();
        }
        return numKeysUpdated;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void implClose() throws IOException {
        assert (!this.isOpen() && Thread.holdsLock(this));
        Object object = this.interruptLock;
        synchronized (object) {
            this.interruptTriggered = true;
        }
        WEPoll.close(this.eph);
        WEPoll.freePollArray(this.pollArrayAddress);
        this.pipe.sink().close();
        this.pipe.source().close();
    }

    @Override
    protected void implDereg(SelectionKeyImpl ski) throws IOException {
        assert (!ski.isValid() && Thread.holdsLock(this));
        int fd = ski.getFDVal();
        if (this.fdToKey.remove(fd) != null) {
            if (ski.registeredEvents() != 0) {
                WEPoll.ctl(this.eph, 3, fd, 0);
                ski.registeredEvents(0);
            }
        } else assert (ski.registeredEvents() == 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setEventOps(SelectionKeyImpl ski) {
        this.ensureOpen();
        Object object = this.updateLock;
        synchronized (object) {
            this.updateKeys.addLast(ski);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Selector wakeup() {
        Object object = this.interruptLock;
        synchronized (object) {
            if (!this.interruptTriggered) {
                try {
                    IOUtil.write1(this.fd1Val, (byte)0);
                }
                catch (IOException ioe) {
                    throw new InternalError(ioe);
                }
                this.interruptTriggered = true;
            }
        }
        return this;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void clearInterrupt() throws IOException {
        Object object = this.interruptLock;
        synchronized (object) {
            IOUtil.drain(this.fd0Val);
            this.interruptTriggered = false;
        }
    }

    private static int toEPollEvents(int ops) {
        int events = 2;
        if ((ops & Net.POLLIN) != 0) {
            events |= 1;
        }
        if ((ops & (Net.POLLOUT | Net.POLLCONN)) != 0) {
            events |= 4;
        }
        return events;
    }

    private static int toReadyOps(int events) {
        int ops = 0;
        if ((events & 1) != 0) {
            ops |= Net.POLLIN;
        }
        if ((events & 4) != 0) {
            ops |= Net.POLLOUT | Net.POLLCONN;
        }
        if ((events & 0x10) != 0) {
            ops |= Net.POLLHUP;
        }
        if ((events & 8) != 0) {
            ops |= Net.POLLERR;
        }
        return ops;
    }
}

