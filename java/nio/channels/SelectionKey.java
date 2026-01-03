/*
 * Decompiled with CFR 0.152.
 */
package java.nio.channels;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;

public abstract class SelectionKey {
    public static final int OP_READ = 1;
    public static final int OP_WRITE = 4;
    public static final int OP_CONNECT = 8;
    public static final int OP_ACCEPT = 16;
    private static final VarHandle ATTACHMENT;
    private volatile Object attachment;

    protected SelectionKey() {
    }

    public abstract SelectableChannel channel();

    public abstract Selector selector();

    public abstract boolean isValid();

    public abstract void cancel();

    public abstract int interestOps();

    public abstract SelectionKey interestOps(int var1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int interestOpsOr(int ops) {
        SelectionKey selectionKey = this;
        synchronized (selectionKey) {
            int oldVal = this.interestOps();
            this.interestOps(oldVal | ops);
            return oldVal;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int interestOpsAnd(int ops) {
        SelectionKey selectionKey = this;
        synchronized (selectionKey) {
            int oldVal = this.interestOps();
            this.interestOps(oldVal & ops);
            return oldVal;
        }
    }

    public abstract int readyOps();

    public final boolean isReadable() {
        return (this.readyOps() & 1) != 0;
    }

    public final boolean isWritable() {
        return (this.readyOps() & 4) != 0;
    }

    public final boolean isConnectable() {
        return (this.readyOps() & 8) != 0;
    }

    public final boolean isAcceptable() {
        return (this.readyOps() & 0x10) != 0;
    }

    public final Object attach(Object ob) {
        return ATTACHMENT.getAndSet(this, ob);
    }

    public final Object attachment() {
        return this.attachment;
    }

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            ATTACHMENT = l.findVarHandle(SelectionKey.class, "attachment", Object.class);
        }
        catch (Exception e) {
            throw new InternalError(e);
        }
    }
}

