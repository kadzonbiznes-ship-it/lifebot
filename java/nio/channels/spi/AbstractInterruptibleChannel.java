/*
 * Decompiled with CFR 0.152.
 */
package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.Channel;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.InterruptibleChannel;
import jdk.internal.access.SharedSecrets;
import sun.nio.ch.Interruptible;

public abstract class AbstractInterruptibleChannel
implements Channel,
InterruptibleChannel {
    private final Object closeLock = new Object();
    private volatile boolean closed;
    private Interruptible interruptor;
    private volatile Thread interrupted;

    protected AbstractInterruptibleChannel() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void close() throws IOException {
        Object object = this.closeLock;
        synchronized (object) {
            if (this.closed) {
                return;
            }
            this.closed = true;
            this.implCloseChannel();
        }
    }

    protected abstract void implCloseChannel() throws IOException;

    @Override
    public final boolean isOpen() {
        return !this.closed;
    }

    protected final void begin() {
        if (this.interruptor == null) {
            this.interruptor = new Interruptible(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public void interrupt(Thread target) {
                    Object object = AbstractInterruptibleChannel.this.closeLock;
                    synchronized (object) {
                        if (AbstractInterruptibleChannel.this.closed) {
                            return;
                        }
                        AbstractInterruptibleChannel.this.closed = true;
                        AbstractInterruptibleChannel.this.interrupted = target;
                        try {
                            AbstractInterruptibleChannel.this.implCloseChannel();
                        }
                        catch (IOException iOException) {
                            // empty catch block
                        }
                    }
                }
            };
        }
        AbstractInterruptibleChannel.blockedOn(this.interruptor);
        Thread me = Thread.currentThread();
        if (me.isInterrupted()) {
            this.interruptor.interrupt(me);
        }
    }

    protected final void end(boolean completed) throws AsynchronousCloseException {
        AbstractInterruptibleChannel.blockedOn(null);
        Thread interrupted = this.interrupted;
        if (interrupted != null && interrupted == Thread.currentThread()) {
            this.interrupted = null;
            throw new ClosedByInterruptException();
        }
        if (!completed && this.closed) {
            throw new AsynchronousCloseException();
        }
    }

    static void blockedOn(Interruptible intr) {
        SharedSecrets.getJavaLangAccess().blockedOn(intr);
    }
}

