/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.FileDescriptor;
import java.lang.invoke.ConstantBootstraps;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectionKey;
import sun.nio.ch.SelChImpl;
import sun.nio.ch.SelectorImpl;

public final class SelectionKeyImpl
extends AbstractSelectionKey {
    private static final VarHandle INTERESTOPS = ConstantBootstraps.fieldVarHandle(MethodHandles.lookup(), "interestOps", VarHandle.class, SelectionKeyImpl.class, Integer.TYPE);
    private final SelChImpl channel;
    private final SelectorImpl selector;
    private volatile int interestOps;
    private volatile int readyOps;
    private int registeredEvents;
    private volatile boolean reset;
    private int index;
    int lastPolled;

    SelectionKeyImpl(SelChImpl ch, SelectorImpl sel) {
        this.channel = ch;
        this.selector = sel;
    }

    private void ensureValid() {
        if (!this.isValid()) {
            throw new CancelledKeyException();
        }
    }

    FileDescriptor getFD() {
        return this.channel.getFD();
    }

    int getFDVal() {
        return this.channel.getFDVal();
    }

    @Override
    public SelectableChannel channel() {
        return (SelectableChannel)((Object)this.channel);
    }

    @Override
    public Selector selector() {
        return this.selector;
    }

    @Override
    public int interestOps() {
        this.ensureValid();
        return this.interestOps;
    }

    @Override
    public SelectionKey interestOps(int ops) {
        this.ensureValid();
        if ((ops & ~this.channel().validOps()) != 0) {
            throw new IllegalArgumentException();
        }
        int oldOps = INTERESTOPS.getAndSet(this, ops);
        if (ops != oldOps) {
            this.selector.setEventOps(this);
        }
        return this;
    }

    @Override
    public int interestOpsOr(int ops) {
        this.ensureValid();
        if ((ops & ~this.channel().validOps()) != 0) {
            throw new IllegalArgumentException();
        }
        int oldVal = INTERESTOPS.getAndBitwiseOr(this, ops);
        if (oldVal != (oldVal | ops)) {
            this.selector.setEventOps(this);
        }
        return oldVal;
    }

    @Override
    public int interestOpsAnd(int ops) {
        this.ensureValid();
        int oldVal = INTERESTOPS.getAndBitwiseAnd(this, ops);
        if (oldVal != (oldVal & ops)) {
            this.selector.setEventOps(this);
        }
        return oldVal;
    }

    @Override
    public int readyOps() {
        this.ensureValid();
        return this.readyOps;
    }

    public void nioReadyOps(int ops) {
        this.readyOps = ops;
    }

    public int nioReadyOps() {
        return this.readyOps;
    }

    public SelectionKey nioInterestOps(int ops) {
        if ((ops & ~this.channel().validOps()) != 0) {
            throw new IllegalArgumentException();
        }
        this.interestOps = ops;
        this.selector.setEventOps(this);
        return this;
    }

    public int nioInterestOps() {
        return this.interestOps;
    }

    int translateInterestOps() {
        return this.channel.translateInterestOps(this.interestOps);
    }

    boolean translateAndSetReadyOps(int ops) {
        return this.channel.translateAndSetReadyOps(ops, this);
    }

    boolean translateAndUpdateReadyOps(int ops) {
        return this.channel.translateAndUpdateReadyOps(ops, this);
    }

    void registeredEvents(int events) {
        this.registeredEvents = events;
    }

    int registeredEvents() {
        return this.registeredEvents;
    }

    int getIndex() {
        return this.index;
    }

    void setIndex(int i) {
        this.index = i;
    }

    void reset() {
        this.reset = true;
        this.selector.setEventOps(this);
        this.selector.wakeup();
    }

    boolean getAndClearReset() {
        assert (Thread.holdsLock(this.selector));
        boolean r = this.reset;
        if (r) {
            this.reset = false;
        }
        return r;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("channel=").append(this.channel).append(", selector=").append(this.selector);
        if (this.isValid()) {
            sb.append(", interestOps=").append(this.interestOps).append(", readyOps=").append(this.readyOps);
        } else {
            sb.append(", invalid");
        }
        return sb.toString();
    }
}

