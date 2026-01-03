/*
 * Decompiled with CFR 0.152.
 */
package java.nio.channels.spi;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelectionKey;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashSet;
import java.util.Set;
import sun.nio.ch.Interruptible;
import sun.nio.ch.SelectorImpl;

public abstract class AbstractSelector
extends Selector {
    private static final VarHandle CLOSED;
    private volatile boolean closed;
    private final SelectorProvider provider;
    private final Set<SelectionKey> cancelledKeys;
    private Interruptible interruptor = null;

    protected AbstractSelector(SelectorProvider provider) {
        this.provider = provider;
        this.cancelledKeys = this instanceof SelectorImpl ? Set.of() : new HashSet<SelectionKey>();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void cancel(SelectionKey k) {
        Set<SelectionKey> set = this.cancelledKeys;
        synchronized (set) {
            this.cancelledKeys.add(k);
        }
    }

    @Override
    public final void close() throws IOException {
        boolean changed = CLOSED.compareAndSet(this, false, true);
        if (changed) {
            this.implCloseSelector();
        }
    }

    protected abstract void implCloseSelector() throws IOException;

    @Override
    public final boolean isOpen() {
        return !this.closed;
    }

    @Override
    public final SelectorProvider provider() {
        return this.provider;
    }

    protected final Set<SelectionKey> cancelledKeys() {
        return this.cancelledKeys;
    }

    protected abstract SelectionKey register(AbstractSelectableChannel var1, int var2, Object var3);

    protected final void deregister(AbstractSelectionKey key) {
        ((AbstractSelectableChannel)key.channel()).removeKey(key);
    }

    protected final void begin() {
        if (this.interruptor == null) {
            this.interruptor = new Interruptible(){

                @Override
                public void interrupt(Thread ignore) {
                    AbstractSelector.this.wakeup();
                }
            };
        }
        AbstractInterruptibleChannel.blockedOn(this.interruptor);
        Thread me = Thread.currentThread();
        if (me.isInterrupted()) {
            this.interruptor.interrupt(me);
        }
    }

    protected final void end() {
        AbstractInterruptibleChannel.blockedOn(null);
    }

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            CLOSED = l.findVarHandle(AbstractSelector.class, "closed", Boolean.TYPE);
        }
        catch (Exception e) {
            throw new InternalError(e);
        }
    }
}

