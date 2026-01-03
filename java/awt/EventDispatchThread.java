/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Conditional;
import java.awt.Container;
import java.awt.EventFilter;
import java.awt.EventQueue;
import java.awt.ModalEventFilter;
import java.awt.Window;
import java.util.ArrayList;
import sun.awt.ModalExclude;
import sun.awt.SunToolkit;
import sun.awt.dnd.SunDragSourceContextPeer;
import sun.util.logging.PlatformLogger;

class EventDispatchThread
extends Thread {
    private static final PlatformLogger eventLog = PlatformLogger.getLogger("java.awt.event.EventDispatchThread");
    private EventQueue theQueue;
    private volatile boolean doDispatch = true;
    private static final int ANY_EVENT = -1;
    private ArrayList<EventFilter> eventFilters = new ArrayList();

    private EventDispatchThread() {
        throw new UnsupportedOperationException("Must erase locals");
    }

    EventDispatchThread(ThreadGroup group, String name, EventQueue queue) {
        super(group, null, name, 0L, false);
        this.setEventQueue(queue);
    }

    public void stopDispatching() {
        this.doDispatch = false;
    }

    @Override
    public void run() {
        try {
            this.pumpEvents(new Conditional(this){

                @Override
                public boolean evaluate() {
                    return true;
                }
            });
        }
        finally {
            this.getEventQueue().detachDispatchThread(this);
        }
    }

    void pumpEvents(Conditional cond) {
        this.pumpEvents(-1, cond);
    }

    void pumpEventsForHierarchy(Conditional cond, Component modalComponent) {
        this.pumpEventsForHierarchy(-1, cond, modalComponent);
    }

    void pumpEvents(int id, Conditional cond) {
        this.pumpEventsForHierarchy(id, cond, null);
    }

    void pumpEventsForHierarchy(int id, Conditional cond, Component modalComponent) {
        this.pumpEventsForFilter(id, cond, new HierarchyEventFilter(modalComponent));
    }

    void pumpEventsForFilter(Conditional cond, EventFilter filter) {
        this.pumpEventsForFilter(-1, cond, filter);
    }

    void pumpEventsForFilter(int id, Conditional cond, EventFilter filter) {
        this.addEventFilter(filter);
        this.doDispatch = true;
        while (this.doDispatch && !this.isInterrupted() && cond.evaluate()) {
            this.pumpOneEventForFilters(id);
        }
        this.removeEventFilter(filter);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void addEventFilter(EventFilter filter) {
        if (eventLog.isLoggable(PlatformLogger.Level.FINEST)) {
            eventLog.finest("adding the event filter: " + String.valueOf(filter));
        }
        ArrayList<EventFilter> arrayList = this.eventFilters;
        synchronized (arrayList) {
            if (!this.eventFilters.contains(filter)) {
                if (filter instanceof ModalEventFilter) {
                    ModalEventFilter cf;
                    EventFilter f;
                    ModalEventFilter newFilter = (ModalEventFilter)filter;
                    int k = 0;
                    for (k = 0; !(k >= this.eventFilters.size() || (f = this.eventFilters.get(k)) instanceof ModalEventFilter && (cf = (ModalEventFilter)f).compareTo(newFilter) > 0); ++k) {
                    }
                    this.eventFilters.add(k, filter);
                } else {
                    this.eventFilters.add(filter);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void removeEventFilter(EventFilter filter) {
        if (eventLog.isLoggable(PlatformLogger.Level.FINEST)) {
            eventLog.finest("removing the event filter: " + String.valueOf(filter));
        }
        ArrayList<EventFilter> arrayList = this.eventFilters;
        synchronized (arrayList) {
            this.eventFilters.remove(filter);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    boolean filterAndCheckEvent(AWTEvent event) {
        boolean eventOK = true;
        ArrayList<EventFilter> arrayList = this.eventFilters;
        synchronized (arrayList) {
            for (int i = this.eventFilters.size() - 1; i >= 0; --i) {
                EventFilter f = this.eventFilters.get(i);
                EventFilter.FilterAction accept = f.acceptEvent(event);
                if (accept == EventFilter.FilterAction.REJECT) {
                    eventOK = false;
                    break;
                }
                if (accept == EventFilter.FilterAction.ACCEPT_IMMEDIATELY) break;
            }
        }
        return eventOK && SunDragSourceContextPeer.checkEvent(event);
    }

    void pumpOneEventForFilters(int id) {
        AWTEvent event = null;
        boolean eventOK = false;
        try {
            EventQueue eq = null;
            do {
                eq = this.getEventQueue();
                event = id == -1 ? eq.getNextEvent() : eq.getNextEvent(id);
                eventOK = this.filterAndCheckEvent(event);
                if (eventOK) continue;
                event.consume();
            } while (!eventOK);
            if (eventLog.isLoggable(PlatformLogger.Level.FINEST)) {
                eventLog.finest("Dispatching: " + String.valueOf(event));
            }
            eq.dispatchEvent(event);
        }
        catch (InterruptedException interruptedException) {
            this.doDispatch = false;
        }
        catch (Throwable e) {
            this.processException(e);
        }
    }

    private void processException(Throwable e) {
        if (eventLog.isLoggable(PlatformLogger.Level.FINE)) {
            eventLog.fine("Processing exception: " + String.valueOf(e));
        }
        this.getUncaughtExceptionHandler().uncaughtException(this, e);
    }

    public synchronized EventQueue getEventQueue() {
        return this.theQueue;
    }

    public synchronized void setEventQueue(EventQueue eq) {
        this.theQueue = eq;
    }

    private static class HierarchyEventFilter
    implements EventFilter {
        private Component modalComponent;

        public HierarchyEventFilter(Component modalComponent) {
            this.modalComponent = modalComponent;
        }

        @Override
        public EventFilter.FilterAction acceptEvent(AWTEvent event) {
            if (this.modalComponent != null) {
                boolean windowClosingEvent;
                int eventID = event.getID();
                boolean mouseEvent = eventID >= 500 && eventID <= 507;
                boolean actionEvent = eventID >= 1001 && eventID <= 1001;
                boolean bl = windowClosingEvent = eventID == 201;
                if (Component.isInstanceOf(this.modalComponent, "javax.swing.JInternalFrame")) {
                    return windowClosingEvent ? EventFilter.FilterAction.REJECT : EventFilter.FilterAction.ACCEPT;
                }
                if (mouseEvent || actionEvent || windowClosingEvent) {
                    Object o = event.getSource();
                    if (o instanceof ModalExclude) {
                        return EventFilter.FilterAction.ACCEPT;
                    }
                    if (o instanceof Component) {
                        Component c;
                        boolean modalExcluded = false;
                        if (this.modalComponent instanceof Container) {
                            for (c = (Component)o; c != this.modalComponent && c != null; c = c.getParent()) {
                                if (!(c instanceof Window) || !SunToolkit.isModalExcluded((Window)c)) continue;
                                modalExcluded = true;
                                break;
                            }
                        }
                        if (!modalExcluded && c != this.modalComponent) {
                            return EventFilter.FilterAction.REJECT;
                        }
                    }
                }
            }
            return EventFilter.FilterAction.ACCEPT;
        }
    }
}

