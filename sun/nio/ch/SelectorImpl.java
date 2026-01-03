/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.IllegalSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import sun.nio.ch.SelChImpl;
import sun.nio.ch.SelectionKeyImpl;
import sun.nio.ch.Util;

public abstract class SelectorImpl
extends AbstractSelector {
    private final Set<SelectionKey> keys;
    private final Set<SelectionKey> selectedKeys;
    private final Set<SelectionKey> publicKeys;
    private final Set<SelectionKey> publicSelectedKeys;
    private final Deque<SelectionKeyImpl> cancelledKeys = new ArrayDeque<SelectionKeyImpl>();
    private boolean inSelect;

    protected SelectorImpl(SelectorProvider sp) {
        super(sp);
        this.keys = ConcurrentHashMap.newKeySet();
        this.selectedKeys = new HashSet<SelectionKey>();
        this.publicKeys = Collections.unmodifiableSet(this.keys);
        this.publicSelectedKeys = Util.ungrowableSet(this.selectedKeys);
    }

    private void ensureOpen() {
        if (!this.isOpen()) {
            throw new ClosedSelectorException();
        }
    }

    @Override
    public final Set<SelectionKey> keys() {
        this.ensureOpen();
        return this.publicKeys;
    }

    @Override
    public final Set<SelectionKey> selectedKeys() {
        this.ensureOpen();
        return this.publicSelectedKeys;
    }

    protected final void begin(boolean blocking) {
        if (blocking) {
            this.begin();
        }
    }

    protected final void end(boolean blocking) {
        if (blocking) {
            this.end();
        }
    }

    protected abstract int doSelect(Consumer<SelectionKey> var1, long var2) throws IOException;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private int lockAndDoSelect(Consumer<SelectionKey> action, long timeout) throws IOException {
        SelectorImpl selectorImpl = this;
        synchronized (selectorImpl) {
            this.ensureOpen();
            if (this.inSelect) {
                throw new IllegalStateException("select in progress");
            }
            this.inSelect = true;
            try {
                Set<SelectionKey> set = this.publicSelectedKeys;
                synchronized (set) {
                    int n = this.doSelect(action, timeout);
                    return n;
                }
            }
            finally {
                this.inSelect = false;
            }
        }
    }

    @Override
    public final int select(long timeout) throws IOException {
        if (timeout < 0L) {
            throw new IllegalArgumentException("Negative timeout");
        }
        return this.lockAndDoSelect(null, timeout == 0L ? -1L : timeout);
    }

    @Override
    public final int select() throws IOException {
        return this.lockAndDoSelect(null, -1L);
    }

    @Override
    public final int selectNow() throws IOException {
        return this.lockAndDoSelect(null, 0L);
    }

    @Override
    public final int select(Consumer<SelectionKey> action, long timeout) throws IOException {
        Objects.requireNonNull(action);
        if (timeout < 0L) {
            throw new IllegalArgumentException("Negative timeout");
        }
        return this.lockAndDoSelect(action, timeout == 0L ? -1L : timeout);
    }

    @Override
    public final int select(Consumer<SelectionKey> action) throws IOException {
        Objects.requireNonNull(action);
        return this.lockAndDoSelect(action, -1L);
    }

    @Override
    public final int selectNow(Consumer<SelectionKey> action) throws IOException {
        Objects.requireNonNull(action);
        return this.lockAndDoSelect(action, 0L);
    }

    protected abstract void implClose() throws IOException;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void implCloseSelector() throws IOException {
        this.wakeup();
        SelectorImpl selectorImpl = this;
        synchronized (selectorImpl) {
            this.implClose();
            Set<SelectionKey> set = this.publicSelectedKeys;
            synchronized (set) {
                Iterator<SelectionKey> i = this.keys.iterator();
                while (i.hasNext()) {
                    SelectionKeyImpl ski = (SelectionKeyImpl)i.next();
                    this.deregister(ski);
                    SelectableChannel selch = ski.channel();
                    if (!selch.isOpen() && !selch.isRegistered()) {
                        ((SelChImpl)((Object)selch)).kill();
                    }
                    this.selectedKeys.remove(ski);
                    i.remove();
                }
                assert (this.selectedKeys.isEmpty());
            }
        }
    }

    @Override
    protected final SelectionKey register(AbstractSelectableChannel ch, int ops, Object attachment) {
        if (!(ch instanceof SelChImpl)) {
            throw new IllegalSelectorException();
        }
        SelectionKeyImpl k = new SelectionKeyImpl((SelChImpl)((Object)ch), this);
        if (attachment != null) {
            k.attach(attachment);
        }
        this.implRegister(k);
        this.keys.add(k);
        try {
            k.interestOps(ops);
        }
        catch (ClosedSelectorException e) {
            assert (ch.keyFor(this) == null);
            this.keys.remove(k);
            k.cancel();
            throw e;
        }
        catch (CancelledKeyException cancelledKeyException) {
            // empty catch block
        }
        return k;
    }

    protected void implRegister(SelectionKeyImpl ski) {
        this.ensureOpen();
    }

    protected abstract void implDereg(SelectionKeyImpl var1) throws IOException;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void cancel(SelectionKeyImpl ski) {
        Deque<SelectionKeyImpl> deque = this.cancelledKeys;
        synchronized (deque) {
            this.cancelledKeys.addLast(ski);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final void processDeregisterQueue() throws IOException {
        assert (Thread.holdsLock(this));
        assert (Thread.holdsLock(this.publicSelectedKeys));
        Deque<SelectionKeyImpl> deque = this.cancelledKeys;
        synchronized (deque) {
            SelectionKeyImpl ski;
            while ((ski = this.cancelledKeys.pollFirst()) != null) {
                this.implDereg(ski);
                this.selectedKeys.remove(ski);
                this.keys.remove(ski);
                this.deregister(ski);
                SelectableChannel ch = ski.channel();
                if (ch.isOpen() || ch.isRegistered()) continue;
                ((SelChImpl)((Object)ch)).kill();
            }
        }
    }

    protected final int processReadyEvents(int rOps, SelectionKeyImpl ski, Consumer<SelectionKey> action) {
        if (action != null) {
            ski.translateAndSetReadyOps(rOps);
            if ((ski.nioReadyOps() & ski.nioInterestOps()) != 0) {
                action.accept(ski);
                this.ensureOpen();
                return 1;
            }
        } else {
            assert (Thread.holdsLock(this.publicSelectedKeys));
            if (this.selectedKeys.contains(ski)) {
                if (ski.translateAndUpdateReadyOps(rOps)) {
                    return 1;
                }
            } else {
                ski.translateAndSetReadyOps(rOps);
                if ((ski.nioReadyOps() & ski.nioInterestOps()) != 0) {
                    this.selectedKeys.add(ski);
                    return 1;
                }
            }
        }
        return 0;
    }

    protected abstract void setEventOps(SelectionKeyImpl var1);
}

