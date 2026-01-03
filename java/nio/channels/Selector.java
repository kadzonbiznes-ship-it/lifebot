/*
 * Decompiled with CFR 0.152.
 */
package java.nio.channels;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.spi.SelectorProvider;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public abstract class Selector
implements Closeable {
    protected Selector() {
    }

    public static Selector open() throws IOException {
        return SelectorProvider.provider().openSelector();
    }

    public abstract boolean isOpen();

    public abstract SelectorProvider provider();

    public abstract Set<SelectionKey> keys();

    public abstract Set<SelectionKey> selectedKeys();

    public abstract int selectNow() throws IOException;

    public abstract int select(long var1) throws IOException;

    public abstract int select() throws IOException;

    public int select(Consumer<SelectionKey> action, long timeout) throws IOException {
        if (timeout < 0L) {
            throw new IllegalArgumentException("Negative timeout");
        }
        return this.doSelect(Objects.requireNonNull(action), timeout);
    }

    public int select(Consumer<SelectionKey> action) throws IOException {
        return this.select(action, 0L);
    }

    public int selectNow(Consumer<SelectionKey> action) throws IOException {
        return this.doSelect(Objects.requireNonNull(action), -1L);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int doSelect(Consumer<SelectionKey> action, long timeout) throws IOException {
        Selector selector = this;
        synchronized (selector) {
            Set<SelectionKey> selectedKeys;
            Set<SelectionKey> set = selectedKeys = this.selectedKeys();
            synchronized (set) {
                selectedKeys.clear();
                int numKeySelected = timeout < 0L ? this.selectNow() : this.select(timeout);
                Set<SelectionKey> keysToConsume = Set.copyOf(selectedKeys);
                assert (keysToConsume.size() == numKeySelected);
                selectedKeys.clear();
                keysToConsume.forEach(k -> {
                    action.accept((SelectionKey)k);
                    if (!this.isOpen()) {
                        throw new ClosedSelectorException();
                    }
                });
                return numKeySelected;
            }
        }
    }

    public abstract Selector wakeup();

    @Override
    public abstract void close() throws IOException;
}

