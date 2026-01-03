/*
 * Decompiled with CFR 0.152.
 */
package java.awt.event;

import java.awt.AWTEvent;
import java.awt.ActiveEvent;
import sun.awt.AWTAccessor;

public class InvocationEvent
extends AWTEvent
implements ActiveEvent {
    public static final int INVOCATION_FIRST = 1200;
    public static final int INVOCATION_DEFAULT = 1200;
    public static final int INVOCATION_LAST = 1200;
    protected Runnable runnable;
    protected volatile Object notifier;
    private final Runnable listener;
    private volatile boolean dispatched;
    protected boolean catchExceptions;
    private Exception exception = null;
    private Throwable throwable = null;
    private long when;
    private static final long serialVersionUID = 436056344909459450L;

    public InvocationEvent(Object source, Runnable runnable) {
        this(source, 1200, runnable, null, null, false);
    }

    public InvocationEvent(Object source, Runnable runnable, Object notifier, boolean catchThrowables) {
        this(source, 1200, runnable, notifier, null, catchThrowables);
    }

    public InvocationEvent(Object source, Runnable runnable, Runnable listener, boolean catchThrowables) {
        this(source, 1200, runnable, null, listener, catchThrowables);
    }

    protected InvocationEvent(Object source, int id, Runnable runnable, Object notifier, boolean catchThrowables) {
        this(source, id, runnable, notifier, null, catchThrowables);
    }

    private InvocationEvent(Object source, int id, Runnable runnable, Object notifier, Runnable listener, boolean catchThrowables) {
        super(source, id);
        this.runnable = runnable;
        this.notifier = notifier;
        this.listener = listener;
        this.catchExceptions = catchThrowables;
        this.when = System.currentTimeMillis();
    }

    @Override
    public void dispatch() {
        try {
            if (this.catchExceptions) {
                try {
                    this.runnable.run();
                }
                catch (Throwable t) {
                    if (t instanceof Exception) {
                        this.exception = (Exception)t;
                    }
                    this.throwable = t;
                }
            } else {
                this.runnable.run();
            }
        }
        finally {
            this.finishedDispatching(true);
        }
    }

    public Exception getException() {
        return this.catchExceptions ? this.exception : null;
    }

    public Throwable getThrowable() {
        return this.catchExceptions ? this.throwable : null;
    }

    public long getWhen() {
        return this.when;
    }

    public boolean isDispatched() {
        return this.dispatched;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void finishedDispatching(boolean dispatched) {
        this.dispatched = dispatched;
        if (this.notifier != null) {
            Object object = this.notifier;
            synchronized (object) {
                this.notifier.notifyAll();
            }
        }
        if (this.listener != null) {
            this.listener.run();
        }
    }

    @Override
    public String paramString() {
        return (switch (this.id) {
            case 1200 -> "INVOCATION_DEFAULT";
            default -> "unknown type";
        }) + ",runnable=" + String.valueOf(this.runnable) + ",notifier=" + String.valueOf(this.notifier) + ",catchExceptions=" + this.catchExceptions + ",when=" + this.when;
    }

    static {
        AWTAccessor.setInvocationEventAccessor(new AWTAccessor.InvocationEventAccessor(){

            @Override
            public void dispose(InvocationEvent invocationEvent) {
                invocationEvent.finishedDispatching(false);
            }
        });
    }
}

