/*
 * Decompiled with CFR 0.152.
 */
package java.nio.channels.spi;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelector;
import sun.nio.ch.SelectionKeyImpl;
import sun.nio.ch.SelectorImpl;

public abstract class AbstractSelectionKey
extends SelectionKey {
    private static final VarHandle INVALID;
    private volatile boolean invalid;

    protected AbstractSelectionKey() {
    }

    @Override
    public final boolean isValid() {
        return !this.invalid;
    }

    void invalidate() {
        this.invalid = true;
    }

    @Override
    public final void cancel() {
        boolean changed = INVALID.compareAndSet(this, false, true);
        if (changed) {
            Selector sel = this.selector();
            if (sel instanceof SelectorImpl) {
                ((SelectorImpl)sel).cancel((SelectionKeyImpl)this);
            } else {
                ((AbstractSelector)sel).cancel(this);
            }
        }
    }

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            INVALID = l.findVarHandle(AbstractSelectionKey.class, "invalid", Boolean.TYPE);
        }
        catch (Exception e) {
            throw new InternalError(e);
        }
    }
}

