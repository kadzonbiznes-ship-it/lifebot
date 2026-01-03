/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTEvent;
import java.awt.ActiveEvent;
import java.awt.Component;
import java.awt.EventDispatchThread;
import java.awt.EventFilter;
import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.awt.SentEvent;
import java.awt.Toolkit;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.LinkedList;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.SunToolkit;

class SequencedEvent
extends AWTEvent
implements ActiveEvent {
    private static final long serialVersionUID = 547742659238625067L;
    private static final int ID = 1006;
    private static final LinkedList<SequencedEvent> list = new LinkedList();
    private final AWTEvent nested;
    private AppContext appContext;
    private boolean disposed;
    private final LinkedList<AWTEvent> pendingEvents = new LinkedList();
    private static boolean fxAppThreadIsDispatchThread;
    private Thread fxCheckSequenceThread;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SequencedEvent(AWTEvent nested) {
        super(nested.getSource(), 1006);
        this.nested = nested;
        SunToolkit.setSystemGenerated(nested);
        if (fxAppThreadIsDispatchThread) {
            this.fxCheckSequenceThread = new Thread(){

                @Override
                public void run() {
                    while (!SequencedEvent.this.isFirstOrDisposed()) {
                        try {
                            Thread.sleep(100L);
                        }
                        catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            };
        }
        Class<SequencedEvent> clazz = SequencedEvent.class;
        synchronized (SequencedEvent.class) {
            list.add(this);
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    @Override
    public final void dispatch() {
        try {
            block10: {
                block11: {
                    block12: {
                        this.appContext = AppContext.getAppContext();
                        if (SequencedEvent.getFirst() == this) break block10;
                        if (!EventQueue.isDispatchThread()) break block11;
                        if (!(Thread.currentThread() instanceof EventDispatchThread)) break block12;
                        EventDispatchThread edt = (EventDispatchThread)Thread.currentThread();
                        edt.pumpEventsForFilter(() -> !this.isFirstOrDisposed(), new SequencedEventsFilter(this));
                        break block10;
                    }
                    if (!fxAppThreadIsDispatchThread) break block10;
                    this.fxCheckSequenceThread.start();
                    try {
                        this.fxCheckSequenceThread.join(500L);
                    }
                    catch (InterruptedException interruptedException) {}
                    break block10;
                }
                while (!this.isFirstOrDisposed()) {
                    Class<SequencedEvent> clazz = SequencedEvent.class;
                    // MONITORENTER : java.awt.SequencedEvent.class
                    try {
                        SequencedEvent.class.wait(1000L);
                    }
                    catch (InterruptedException e) {
                        // MONITOREXIT : clazz
                        break;
                    }
                }
            }
            if (this.disposed) return;
            KeyboardFocusManager.getCurrentKeyboardFocusManager().setCurrentSequencedEvent(this);
            Toolkit.getEventQueue().dispatchEvent(this.nested);
            return;
        }
        finally {
            this.dispose();
        }
    }

    private static final boolean isOwnerAppContextDisposed(SequencedEvent se) {
        Object target;
        if (se != null && (target = se.nested.getSource()) instanceof Component) {
            return ((Component)target).appContext.isDisposed();
        }
        return false;
    }

    public final boolean isFirstOrDisposed() {
        if (this.disposed) {
            return true;
        }
        return this == SequencedEvent.getFirstWithContext() || this.disposed;
    }

    private static final synchronized SequencedEvent getFirst() {
        return list.getFirst();
    }

    private static final SequencedEvent getFirstWithContext() {
        SequencedEvent first = SequencedEvent.getFirst();
        while (SequencedEvent.isOwnerAppContextDisposed(first)) {
            first.dispose();
            first = SequencedEvent.getFirst();
        }
        return first;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void dispose() {
        Class<SequencedEvent> clazz = SequencedEvent.class;
        synchronized (SequencedEvent.class) {
            if (this.disposed) {
                // ** MonitorExit[var1_1] (shouldn't be in output)
                return;
            }
            if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getCurrentSequencedEvent() == this) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().setCurrentSequencedEvent(null);
            }
            this.disposed = true;
            // ** MonitorExit[var1_1] (shouldn't be in output)
            SequencedEvent next = null;
            Class<SequencedEvent> clazz2 = SequencedEvent.class;
            synchronized (SequencedEvent.class) {
                SequencedEvent.class.notifyAll();
                if (list.getFirst() == this) {
                    list.removeFirst();
                    if (!list.isEmpty()) {
                        next = list.getFirst();
                    }
                } else {
                    list.remove(this);
                }
                // ** MonitorExit[var2_3] (shouldn't be in output)
                if (next != null && next.appContext != null) {
                    SunToolkit.postEvent(next.appContext, new SentEvent());
                }
                for (AWTEvent e : this.pendingEvents) {
                    SunToolkit.postEvent(this.appContext, e);
                }
                return;
            }
        }
    }

    static {
        AWTAccessor.setSequencedEventAccessor(new AWTAccessor.SequencedEventAccessor(){

            @Override
            public AWTEvent getNested(AWTEvent sequencedEvent) {
                return ((SequencedEvent)sequencedEvent).nested;
            }

            @Override
            public boolean isSequencedEvent(AWTEvent event) {
                return event instanceof SequencedEvent;
            }

            @Override
            public AWTEvent create(AWTEvent event) {
                return new SequencedEvent(event);
            }
        });
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                fxAppThreadIsDispatchThread = "true".equals(System.getProperty("javafx.embed.singleThread"));
                return null;
            }
        });
    }

    private static final class SequencedEventsFilter
    implements EventFilter {
        private final SequencedEvent currentSequencedEvent;

        private SequencedEventsFilter(SequencedEvent currentSequencedEvent) {
            this.currentSequencedEvent = currentSequencedEvent;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public EventFilter.FilterAction acceptEvent(AWTEvent ev) {
            if (ev.getID() == 1006) {
                Class<SequencedEvent> clazz = SequencedEvent.class;
                synchronized (SequencedEvent.class) {
                    SequencedEvent iev;
                    Iterator iterator = list.iterator();
                    while (iterator.hasNext() && !(iev = (SequencedEvent)iterator.next()).equals(this.currentSequencedEvent)) {
                        if (!iev.equals(ev)) continue;
                        // ** MonitorExit[var2_2] (shouldn't be in output)
                        return EventFilter.FilterAction.ACCEPT;
                    }
                    // ** MonitorExit[var2_2] (shouldn't be in output)
                }
            } else if (ev.getID() == 1007) {
                return EventFilter.FilterAction.ACCEPT;
            }
            {
                this.currentSequencedEvent.pendingEvents.add(ev);
                return EventFilter.FilterAction.REJECT;
            }
        }
    }
}

