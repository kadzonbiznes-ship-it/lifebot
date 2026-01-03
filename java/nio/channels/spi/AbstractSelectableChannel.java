/*
 * Decompiled with CFR 0.152.
 */
package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectionKey;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.function.Consumer;

public abstract class AbstractSelectableChannel
extends SelectableChannel {
    private final SelectorProvider provider;
    private SelectionKey[] keys = null;
    private int keyCount = 0;
    private final Object keyLock = new Object();
    private final Object regLock = new Object();
    private volatile boolean nonBlocking;

    protected AbstractSelectableChannel(SelectorProvider provider) {
        this.provider = provider;
    }

    @Override
    public final SelectorProvider provider() {
        return this.provider;
    }

    private void addKey(SelectionKey k) {
        assert (Thread.holdsLock(this.keyLock));
        int i = 0;
        if (this.keys != null && this.keyCount < this.keys.length) {
            for (i = 0; i < this.keys.length && this.keys[i] != null; ++i) {
            }
        } else if (this.keys == null) {
            this.keys = new SelectionKey[2];
        } else {
            int n = this.keys.length * 2;
            SelectionKey[] ks = new SelectionKey[n];
            for (i = 0; i < this.keys.length; ++i) {
                ks[i] = this.keys[i];
            }
            this.keys = ks;
            i = this.keyCount;
        }
        this.keys[i] = k;
        ++this.keyCount;
    }

    private SelectionKey findKey(Selector sel) {
        assert (Thread.holdsLock(this.keyLock));
        if (this.keys == null) {
            return null;
        }
        for (int i = 0; i < this.keys.length; ++i) {
            if (this.keys[i] == null || this.keys[i].selector() != sel) continue;
            return this.keys[i];
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void removeKey(SelectionKey k) {
        Object object = this.keyLock;
        synchronized (object) {
            for (int i = 0; i < this.keys.length; ++i) {
                if (this.keys[i] != k) continue;
                this.keys[i] = null;
                --this.keyCount;
            }
            ((AbstractSelectionKey)k).invalidate();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean haveValidKeys() {
        Object object = this.keyLock;
        synchronized (object) {
            if (this.keyCount == 0) {
                return false;
            }
            for (int i = 0; i < this.keys.length; ++i) {
                if (this.keys[i] == null || !this.keys[i].isValid()) continue;
                return true;
            }
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final boolean isRegistered() {
        Object object = this.keyLock;
        synchronized (object) {
            return this.keyCount != 0;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final SelectionKey keyFor(Selector sel) {
        Object object = this.keyLock;
        synchronized (object) {
            return this.findKey(sel);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void forEach(Consumer<SelectionKey> action) {
        Object object = this.keyLock;
        synchronized (object) {
            SelectionKey[] keys = this.keys;
            if (keys != null) {
                Arrays.stream(keys).filter(k -> k != null).forEach(action::accept);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final SelectionKey register(Selector sel, int ops, Object att) throws ClosedChannelException {
        if ((ops & ~this.validOps()) != 0) {
            throw new IllegalArgumentException();
        }
        if (!this.isOpen()) {
            throw new ClosedChannelException();
        }
        Object object = this.regLock;
        synchronized (object) {
            if (this.isBlocking()) {
                throw new IllegalBlockingModeException();
            }
            Object object2 = this.keyLock;
            synchronized (object2) {
                if (!this.isOpen()) {
                    throw new ClosedChannelException();
                }
                SelectionKey k = this.findKey(sel);
                if (k != null) {
                    k.attach(att);
                    k.interestOps(ops);
                } else {
                    k = ((AbstractSelector)sel).register(this, ops, att);
                    this.addKey(k);
                }
                return k;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected final void implCloseChannel() throws IOException {
        this.implCloseSelectableChannel();
        SelectionKey[] copyOfKeys = null;
        SelectionKey[] selectionKeyArray = this.keyLock;
        synchronized (this.keyLock) {
            if (this.keys != null) {
                copyOfKeys = (SelectionKey[])this.keys.clone();
            }
            // ** MonitorExit[var2_2] (shouldn't be in output)
            if (copyOfKeys != null) {
                for (SelectionKey k : copyOfKeys) {
                    if (k == null) continue;
                    k.cancel();
                }
            }
            return;
        }
    }

    protected abstract void implCloseSelectableChannel() throws IOException;

    @Override
    public final boolean isBlocking() {
        return !this.nonBlocking;
    }

    @Override
    public final Object blockingLock() {
        return this.regLock;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final SelectableChannel configureBlocking(boolean block) throws IOException {
        Object object = this.regLock;
        synchronized (object) {
            boolean blocking;
            if (!this.isOpen()) {
                throw new ClosedChannelException();
            }
            boolean bl = blocking = !this.nonBlocking;
            if (block != blocking) {
                if (block && this.haveValidKeys()) {
                    throw new IllegalBlockingModeException();
                }
                this.implConfigureBlocking(block);
                this.nonBlocking = !block;
            }
        }
        return this;
    }

    protected abstract void implConfigureBlocking(boolean var1) throws IOException;
}

