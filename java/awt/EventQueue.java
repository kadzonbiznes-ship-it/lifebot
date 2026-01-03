/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTEvent;
import java.awt.ActiveEvent;
import java.awt.Component;
import java.awt.Conditional;
import java.awt.EventDispatchThread;
import java.awt.EventFilter;
import java.awt.MenuComponent;
import java.awt.Queue;
import java.awt.Rectangle;
import java.awt.SecondaryLoop;
import java.awt.SentEvent;
import java.awt.SequencedEvent;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.WaitDispatchSupport;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.InvocationEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.PaintEvent;
import java.awt.event.WindowEvent;
import java.awt.peer.ComponentPeer;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.EmptyStackException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import jdk.internal.access.JavaSecurityAccess;
import jdk.internal.access.SharedSecrets;
import sun.awt.AWTAccessor;
import sun.awt.AWTAutoShutdown;
import sun.awt.AppContext;
import sun.awt.EventQueueItem;
import sun.awt.FwDispatcher;
import sun.awt.PeerEvent;
import sun.awt.SunToolkit;
import sun.awt.dnd.SunDropTargetEvent;
import sun.util.logging.PlatformLogger;

public class EventQueue {
    private static final AtomicInteger threadInitNumber = new AtomicInteger();
    private static final int LOW_PRIORITY = 0;
    private static final int NORM_PRIORITY = 1;
    private static final int HIGH_PRIORITY = 2;
    private static final int ULTIMATE_PRIORITY = 3;
    private static final int NUM_PRIORITIES = 4;
    private Queue[] queues = new Queue[4];
    private EventQueue nextQueue;
    private EventQueue previousQueue;
    private final Lock pushPopLock;
    private final Condition pushPopCond;
    private static final Runnable dummyRunnable = new Runnable(){

        @Override
        public void run() {
        }
    };
    private EventDispatchThread dispatchThread;
    private final ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
    private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private long mostRecentEventTime = System.currentTimeMillis();
    private long mostRecentKeyEventTime = System.currentTimeMillis();
    private WeakReference<AWTEvent> currentEvent;
    private volatile int waitForID;
    private final AppContext appContext;
    private final String name = "AWT-EventQueue-" + threadInitNumber.getAndIncrement();
    private FwDispatcher fwDispatcher;
    private static volatile PlatformLogger eventLog;
    private static boolean fxAppThreadIsDispatchThread;
    private static final int PAINT = 0;
    private static final int UPDATE = 1;
    private static final int MOVE = 2;
    private static final int DRAG = 3;
    private static final int PEER = 4;
    private static final int CACHE_LENGTH = 5;
    private static final JavaSecurityAccess javaSecurityAccess;

    private static final PlatformLogger getEventLog() {
        if (eventLog == null) {
            eventLog = PlatformLogger.getLogger("java.awt.event.EventQueue");
        }
        return eventLog;
    }

    public EventQueue() {
        for (int i = 0; i < 4; ++i) {
            this.queues[i] = new Queue();
        }
        this.appContext = AppContext.getAppContext();
        this.pushPopLock = (Lock)this.appContext.get(AppContext.EVENT_QUEUE_LOCK_KEY);
        this.pushPopCond = (Condition)this.appContext.get(AppContext.EVENT_QUEUE_COND_KEY);
    }

    public void postEvent(AWTEvent theEvent) {
        SunToolkit.flushPendingEvents(this.appContext);
        this.postEventPrivate(theEvent);
    }

    private void postEventPrivate(AWTEvent theEvent) {
        theEvent.isPosted = true;
        this.pushPopLock.lock();
        try {
            if (this.nextQueue != null) {
                this.nextQueue.postEventPrivate(theEvent);
                return;
            }
            if (this.dispatchThread == null) {
                if (theEvent.getSource() == AWTAutoShutdown.getInstance()) {
                    return;
                }
                this.initDispatchThread();
            }
            this.postEvent(theEvent, EventQueue.getPriority(theEvent));
        }
        finally {
            this.pushPopLock.unlock();
        }
    }

    private static int getPriority(AWTEvent theEvent) {
        int id;
        if (theEvent instanceof PeerEvent) {
            PeerEvent peerEvent = (PeerEvent)theEvent;
            if ((peerEvent.getFlags() & 2L) != 0L) {
                return 3;
            }
            if ((peerEvent.getFlags() & 1L) != 0L) {
                return 2;
            }
            if ((peerEvent.getFlags() & 4L) != 0L) {
                return 0;
            }
        }
        if ((id = theEvent.getID()) >= 800 && id <= 801) {
            return 0;
        }
        return 1;
    }

    private void postEvent(AWTEvent theEvent, int priority) {
        boolean notifyID;
        if (this.coalesceEvent(theEvent, priority)) {
            return;
        }
        EventQueueItem newItem = new EventQueueItem(theEvent);
        this.cacheEQItem(newItem);
        boolean bl = notifyID = theEvent.getID() == this.waitForID;
        if (this.queues[priority].head == null) {
            boolean shouldNotify = this.noEvents();
            this.queues[priority].head = this.queues[priority].tail = newItem;
            if (shouldNotify) {
                if (theEvent.getSource() != AWTAutoShutdown.getInstance()) {
                    AWTAutoShutdown.getInstance().notifyThreadBusy(this.dispatchThread);
                }
                this.pushPopCond.signalAll();
            } else if (notifyID) {
                this.pushPopCond.signalAll();
            }
        } else {
            this.queues[priority].tail.next = newItem;
            this.queues[priority].tail = newItem;
            if (notifyID) {
                this.pushPopCond.signalAll();
            }
        }
    }

    private boolean coalescePaintEvent(PaintEvent e) {
        PaintEvent merged;
        EventQueueItem[] cache;
        ComponentPeer sourcePeer = ((Component)e.getSource()).peer;
        if (sourcePeer != null) {
            sourcePeer.coalescePaintEvent(e);
        }
        if ((cache = ((Component)e.getSource()).eventCache) == null) {
            return false;
        }
        int index = EventQueue.eventToCacheIndex(e);
        if (index != -1 && cache[index] != null && (merged = this.mergePaintEvents(e, (PaintEvent)cache[index].event)) != null) {
            cache[index].event = merged;
            return true;
        }
        return false;
    }

    private PaintEvent mergePaintEvents(PaintEvent a, PaintEvent b) {
        Rectangle aRect = a.getUpdateRect();
        Rectangle bRect = b.getUpdateRect();
        if (bRect.contains(aRect)) {
            return b;
        }
        if (aRect.contains(bRect)) {
            return a;
        }
        return null;
    }

    private boolean coalesceMouseEvent(MouseEvent e) {
        EventQueueItem[] cache = ((Component)e.getSource()).eventCache;
        if (cache == null) {
            return false;
        }
        int index = EventQueue.eventToCacheIndex(e);
        if (index != -1 && cache[index] != null) {
            cache[index].event = e;
            return true;
        }
        return false;
    }

    private boolean coalescePeerEvent(PeerEvent e) {
        EventQueueItem[] cache = ((Component)e.getSource()).eventCache;
        if (cache == null) {
            return false;
        }
        int index = EventQueue.eventToCacheIndex(e);
        if (index != -1 && cache[index] != null) {
            if ((e = e.coalesceEvents((PeerEvent)cache[index].event)) != null) {
                cache[index].event = e;
                return true;
            }
            cache[index] = null;
        }
        return false;
    }

    private boolean coalesceOtherEvent(AWTEvent e, int priority) {
        int id = e.getID();
        Component source = (Component)e.getSource();
        EventQueueItem entry = this.queues[priority].head;
        while (entry != null) {
            AWTEvent coalescedEvent;
            if (entry.event.getSource() == source && entry.event.getID() == id && (coalescedEvent = source.coalesceEvents(entry.event, e)) != null) {
                entry.event = coalescedEvent;
                return true;
            }
            entry = entry.next;
        }
        return false;
    }

    private boolean coalesceEvent(AWTEvent e, int priority) {
        if (!(e.getSource() instanceof Component)) {
            return false;
        }
        if (e instanceof PeerEvent) {
            return this.coalescePeerEvent((PeerEvent)e);
        }
        if (((Component)e.getSource()).isCoalescingEnabled() && this.coalesceOtherEvent(e, priority)) {
            return true;
        }
        if (e instanceof PaintEvent) {
            return this.coalescePaintEvent((PaintEvent)e);
        }
        if (e instanceof MouseEvent) {
            return this.coalesceMouseEvent((MouseEvent)e);
        }
        return false;
    }

    private void cacheEQItem(EventQueueItem entry) {
        int index = EventQueue.eventToCacheIndex(entry.event);
        if (index != -1 && entry.event.getSource() instanceof Component) {
            Component source = (Component)entry.event.getSource();
            if (source.eventCache == null) {
                source.eventCache = new EventQueueItem[5];
            }
            source.eventCache[index] = entry;
        }
    }

    private void uncacheEQItem(EventQueueItem entry) {
        int index = EventQueue.eventToCacheIndex(entry.event);
        if (index != -1 && entry.event.getSource() instanceof Component) {
            Component source = (Component)entry.event.getSource();
            if (source.eventCache == null) {
                return;
            }
            source.eventCache[index] = null;
        }
    }

    private static int eventToCacheIndex(AWTEvent e) {
        switch (e.getID()) {
            case 800: {
                return 0;
            }
            case 801: {
                return 1;
            }
            case 503: {
                return 2;
            }
            case 506: {
                return e instanceof SunDropTargetEvent ? -1 : 3;
            }
        }
        return e instanceof PeerEvent ? 4 : -1;
    }

    private boolean noEvents() {
        for (int i = 0; i < 4; ++i) {
            if (this.queues[i].head == null) continue;
            return false;
        }
        return true;
    }

    public AWTEvent getNextEvent() throws InterruptedException {
        while (true) {
            SunToolkit.flushPendingEvents(this.appContext);
            this.pushPopLock.lock();
            try {
                AWTEvent event = this.getNextEventPrivate();
                if (event != null) {
                    AWTEvent aWTEvent = event;
                    return aWTEvent;
                }
                AWTAutoShutdown.getInstance().notifyThreadFree(this.dispatchThread);
                this.pushPopCond.await();
                continue;
            }
            finally {
                this.pushPopLock.unlock();
                continue;
            }
            break;
        }
    }

    AWTEvent getNextEventPrivate() throws InterruptedException {
        for (int i = 3; i >= 0; --i) {
            if (this.queues[i].head == null) continue;
            EventQueueItem entry = this.queues[i].head;
            this.queues[i].head = entry.next;
            if (entry.next == null) {
                this.queues[i].tail = null;
            }
            this.uncacheEQItem(entry);
            return entry.event;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    AWTEvent getNextEvent(int id) throws InterruptedException {
        while (true) {
            SunToolkit.flushPendingEvents(this.appContext);
            this.pushPopLock.lock();
            try {
                for (int i = 0; i < 4; ++i) {
                    EventQueueItem entry = this.queues[i].head;
                    EventQueueItem prev = null;
                    while (entry != null) {
                        if (entry.event.getID() == id) {
                            if (prev == null) {
                                this.queues[i].head = entry.next;
                            } else {
                                prev.next = entry.next;
                            }
                            if (this.queues[i].tail == entry) {
                                this.queues[i].tail = prev;
                            }
                            this.uncacheEQItem(entry);
                            AWTEvent aWTEvent = entry.event;
                            return aWTEvent;
                        }
                        prev = entry;
                        entry = entry.next;
                    }
                }
                this.waitForID = id;
                this.pushPopCond.await();
                this.waitForID = 0;
                continue;
            }
            finally {
                this.pushPopLock.unlock();
                continue;
            }
            break;
        }
    }

    public AWTEvent peekEvent() {
        this.pushPopLock.lock();
        try {
            for (int i = 3; i >= 0; --i) {
                if (this.queues[i].head == null) continue;
                AWTEvent aWTEvent = this.queues[i].head.event;
                return aWTEvent;
            }
        }
        finally {
            this.pushPopLock.unlock();
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public AWTEvent peekEvent(int id) {
        this.pushPopLock.lock();
        try {
            for (int i = 3; i >= 0; --i) {
                EventQueueItem q = this.queues[i].head;
                while (q != null) {
                    if (q.event.getID() == id) {
                        AWTEvent aWTEvent = q.event;
                        return aWTEvent;
                    }
                    q = q.next;
                }
            }
        }
        finally {
            this.pushPopLock.unlock();
        }
        return null;
    }

    protected void dispatchEvent(final AWTEvent event) {
        final Object src = event.getSource();
        final PrivilegedAction<Void> action = new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                if (EventQueue.this.fwDispatcher == null || EventQueue.this.isDispatchThreadImpl()) {
                    EventQueue.this.dispatchEventImpl(event, src);
                } else {
                    EventQueue.this.fwDispatcher.scheduleDispatch(new Runnable(){

                        @Override
                        public void run() {
                            if (EventQueue.this.dispatchThread.filterAndCheckEvent(event)) {
                                EventQueue.this.dispatchEventImpl(event, src);
                            }
                        }
                    });
                }
                return null;
            }
        };
        AccessControlContext stack = AccessController.getContext();
        AccessControlContext srcAcc = EventQueue.getAccessControlContextFrom(src);
        final AccessControlContext eventAcc = event.getAccessControlContext();
        if (srcAcc == null) {
            javaSecurityAccess.doIntersectionPrivilege(action, stack, eventAcc);
        } else {
            javaSecurityAccess.doIntersectionPrivilege(new PrivilegedAction<Void>(){

                @Override
                public Void run() {
                    javaSecurityAccess.doIntersectionPrivilege(action, eventAcc);
                    return null;
                }
            }, stack, srcAcc);
        }
    }

    private static AccessControlContext getAccessControlContextFrom(Object src) {
        return src instanceof Component ? ((Component)src).getAccessControlContext() : (src instanceof MenuComponent ? ((MenuComponent)src).getAccessControlContext() : (src instanceof TrayIcon ? ((TrayIcon)src).getAccessControlContext() : null));
    }

    private void dispatchEventImpl(AWTEvent event, Object src) {
        event.isPosted = true;
        if (event instanceof ActiveEvent) {
            this.setCurrentEventAndMostRecentTimeImpl(event);
            ((ActiveEvent)((Object)event)).dispatch();
        } else if (src instanceof Component) {
            ((Component)src).dispatchEvent(event);
            event.dispatched();
        } else if (src instanceof MenuComponent) {
            ((MenuComponent)src).dispatchEvent(event);
        } else if (src instanceof TrayIcon) {
            ((TrayIcon)src).dispatchEvent(event);
        } else if (src instanceof AWTAutoShutdown) {
            if (this.noEvents()) {
                this.dispatchThread.stopDispatching();
            }
        } else if (EventQueue.getEventLog().isLoggable(PlatformLogger.Level.FINE)) {
            EventQueue.getEventLog().fine("Unable to dispatch event: " + String.valueOf(event));
        }
    }

    public static long getMostRecentEventTime() {
        return Toolkit.getEventQueue().getMostRecentEventTimeImpl();
    }

    private long getMostRecentEventTimeImpl() {
        this.pushPopLock.lock();
        try {
            long l = Thread.currentThread() == this.dispatchThread ? this.mostRecentEventTime : System.currentTimeMillis();
            return l;
        }
        finally {
            this.pushPopLock.unlock();
        }
    }

    long getMostRecentEventTimeEx() {
        this.pushPopLock.lock();
        try {
            long l = this.mostRecentEventTime;
            return l;
        }
        finally {
            this.pushPopLock.unlock();
        }
    }

    public static AWTEvent getCurrentEvent() {
        return Toolkit.getEventQueue().getCurrentEventImpl();
    }

    private AWTEvent getCurrentEventImpl() {
        this.pushPopLock.lock();
        try {
            if (Thread.currentThread() == this.dispatchThread || fxAppThreadIsDispatchThread) {
                AWTEvent aWTEvent = this.currentEvent != null ? (AWTEvent)this.currentEvent.get() : null;
                return aWTEvent;
            }
            AWTEvent aWTEvent = null;
            return aWTEvent;
        }
        finally {
            this.pushPopLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void push(EventQueue newEventQueue) {
        if (EventQueue.getEventLog().isLoggable(PlatformLogger.Level.FINE)) {
            EventQueue.getEventLog().fine("EventQueue.push(" + String.valueOf(newEventQueue) + ")");
        }
        this.pushPopLock.lock();
        try {
            EventQueue topQueue = this;
            while (topQueue.nextQueue != null) {
                topQueue = topQueue.nextQueue;
            }
            if (topQueue.fwDispatcher != null) {
                throw new RuntimeException("push() to queue with fwDispatcher");
            }
            if (topQueue.dispatchThread != null && topQueue.dispatchThread.getEventQueue() == this) {
                newEventQueue.dispatchThread = topQueue.dispatchThread;
                topQueue.dispatchThread.setEventQueue(newEventQueue);
            }
            while (topQueue.peekEvent() != null) {
                try {
                    newEventQueue.postEventPrivate(topQueue.getNextEventPrivate());
                }
                catch (InterruptedException ie) {
                    if (!EventQueue.getEventLog().isLoggable(PlatformLogger.Level.FINE)) continue;
                    EventQueue.getEventLog().fine("Interrupted push", ie);
                }
            }
            if (topQueue.dispatchThread != null) {
                topQueue.postEventPrivate(new InvocationEvent((Object)topQueue, dummyRunnable));
            }
            newEventQueue.previousQueue = topQueue;
            topQueue.nextQueue = newEventQueue;
            if (this.appContext.get(AppContext.EVENT_QUEUE_KEY) == topQueue) {
                this.appContext.put(AppContext.EVENT_QUEUE_KEY, newEventQueue);
            }
            this.pushPopCond.signalAll();
        }
        finally {
            this.pushPopLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void pop() throws EmptyStackException {
        if (EventQueue.getEventLog().isLoggable(PlatformLogger.Level.FINE)) {
            EventQueue.getEventLog().fine("EventQueue.pop(" + String.valueOf(this) + ")");
        }
        this.pushPopLock.lock();
        try {
            EventQueue topQueue = this;
            while (topQueue.nextQueue != null) {
                topQueue = topQueue.nextQueue;
            }
            EventQueue prevQueue = topQueue.previousQueue;
            if (prevQueue == null) {
                throw new EmptyStackException();
            }
            topQueue.previousQueue = null;
            prevQueue.nextQueue = null;
            while (topQueue.peekEvent() != null) {
                try {
                    prevQueue.postEventPrivate(topQueue.getNextEventPrivate());
                }
                catch (InterruptedException ie) {
                    if (!EventQueue.getEventLog().isLoggable(PlatformLogger.Level.FINE)) continue;
                    EventQueue.getEventLog().fine("Interrupted pop", ie);
                }
            }
            if (topQueue.dispatchThread != null && topQueue.dispatchThread.getEventQueue() == this) {
                prevQueue.dispatchThread = topQueue.dispatchThread;
                topQueue.dispatchThread.setEventQueue(prevQueue);
            }
            if (this.appContext.get(AppContext.EVENT_QUEUE_KEY) == this) {
                this.appContext.put(AppContext.EVENT_QUEUE_KEY, prevQueue);
            }
            topQueue.postEventPrivate(new InvocationEvent((Object)topQueue, dummyRunnable));
            this.pushPopCond.signalAll();
        }
        finally {
            this.pushPopLock.unlock();
        }
    }

    public SecondaryLoop createSecondaryLoop() {
        return this.createSecondaryLoop(null, null, 0L);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    SecondaryLoop createSecondaryLoop(Conditional cond, EventFilter filter, long interval) {
        this.pushPopLock.lock();
        try {
            if (this.nextQueue != null) {
                SecondaryLoop secondaryLoop = this.nextQueue.createSecondaryLoop(cond, filter, interval);
                return secondaryLoop;
            }
            if (this.fwDispatcher != null) {
                FwSecondaryLoopWrapper fwSecondaryLoopWrapper = new FwSecondaryLoopWrapper(this.fwDispatcher.createSecondaryLoop(), filter);
                return fwSecondaryLoopWrapper;
            }
            if (this.dispatchThread == null) {
                this.initDispatchThread();
            }
            WaitDispatchSupport waitDispatchSupport = new WaitDispatchSupport(this.dispatchThread, cond, filter, interval);
            return waitDispatchSupport;
        }
        finally {
            this.pushPopLock.unlock();
        }
    }

    public static boolean isDispatchThread() {
        EventQueue eq = Toolkit.getEventQueue();
        return eq.isDispatchThreadImpl();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final boolean isDispatchThreadImpl() {
        EventQueue eq = this;
        this.pushPopLock.lock();
        try {
            EventQueue next = eq.nextQueue;
            while (next != null) {
                eq = next;
                next = eq.nextQueue;
            }
            if (eq.fwDispatcher != null) {
                boolean bl = eq.fwDispatcher.isDispatchThread();
                return bl;
            }
            boolean bl = Thread.currentThread() == eq.dispatchThread;
            return bl;
        }
        finally {
            this.pushPopLock.unlock();
        }
    }

    final void initDispatchThread() {
        this.pushPopLock.lock();
        try {
            if (this.dispatchThread == null && !this.threadGroup.isDestroyed() && !this.appContext.isDisposed()) {
                this.dispatchThread = AccessController.doPrivileged(new PrivilegedAction<EventDispatchThread>(){

                    @Override
                    public EventDispatchThread run() {
                        EventDispatchThread t = new EventDispatchThread(EventQueue.this.threadGroup, EventQueue.this.name, EventQueue.this);
                        t.setContextClassLoader(EventQueue.this.classLoader);
                        t.setPriority(6);
                        t.setDaemon(false);
                        AWTAutoShutdown.getInstance().notifyThreadBusy(t);
                        return t;
                    }
                });
                this.dispatchThread.start();
            }
        }
        finally {
            this.pushPopLock.unlock();
        }
    }

    final void detachDispatchThread(EventDispatchThread edt) {
        SunToolkit.flushPendingEvents(this.appContext);
        this.pushPopLock.lock();
        try {
            if (edt == this.dispatchThread) {
                this.dispatchThread = null;
            }
            AWTAutoShutdown.getInstance().notifyThreadFree(edt);
            if (this.peekEvent() != null) {
                this.initDispatchThread();
            }
        }
        finally {
            this.pushPopLock.unlock();
        }
    }

    final EventDispatchThread getDispatchThread() {
        this.pushPopLock.lock();
        try {
            EventDispatchThread eventDispatchThread = this.dispatchThread;
            return eventDispatchThread;
        }
        finally {
            this.pushPopLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void removeSourceEvents(Object source, boolean removeAllEvents) {
        SunToolkit.flushPendingEvents(this.appContext);
        this.pushPopLock.lock();
        try {
            for (int i = 0; i < 4; ++i) {
                EventQueueItem entry = this.queues[i].head;
                EventQueueItem prev = null;
                while (entry != null) {
                    if (!(entry.event.getSource() != source || !removeAllEvents && (entry.event instanceof SequencedEvent || entry.event instanceof SentEvent || entry.event instanceof FocusEvent || entry.event instanceof WindowEvent || entry.event instanceof KeyEvent || entry.event instanceof InputMethodEvent))) {
                        if (entry.event instanceof SequencedEvent) {
                            ((SequencedEvent)entry.event).dispose();
                        }
                        if (entry.event instanceof SentEvent) {
                            ((SentEvent)entry.event).dispose();
                        }
                        if (entry.event instanceof InvocationEvent) {
                            AWTAccessor.getInvocationEventAccessor().dispose((InvocationEvent)entry.event);
                        }
                        if (entry.event instanceof SunDropTargetEvent) {
                            ((SunDropTargetEvent)entry.event).dispose();
                        }
                        if (prev == null) {
                            this.queues[i].head = entry.next;
                        } else {
                            prev.next = entry.next;
                        }
                        this.uncacheEQItem(entry);
                    } else {
                        prev = entry;
                    }
                    entry = entry.next;
                }
                this.queues[i].tail = prev;
            }
        }
        finally {
            this.pushPopLock.unlock();
        }
    }

    synchronized long getMostRecentKeyEventTime() {
        this.pushPopLock.lock();
        try {
            long l = this.mostRecentKeyEventTime;
            return l;
        }
        finally {
            this.pushPopLock.unlock();
        }
    }

    static void setCurrentEventAndMostRecentTime(AWTEvent e) {
        Toolkit.getEventQueue().setCurrentEventAndMostRecentTimeImpl(e);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setCurrentEventAndMostRecentTimeImpl(AWTEvent e) {
        this.pushPopLock.lock();
        try {
            if (!fxAppThreadIsDispatchThread && Thread.currentThread() != this.dispatchThread) {
                return;
            }
            this.currentEvent = new WeakReference<AWTEvent>(e);
            long mostRecentEventTime2 = Long.MIN_VALUE;
            if (e instanceof InputEvent) {
                InputEvent ie = (InputEvent)e;
                mostRecentEventTime2 = ie.getWhen();
                if (e instanceof KeyEvent) {
                    this.mostRecentKeyEventTime = ie.getWhen();
                }
            } else if (e instanceof InputMethodEvent) {
                InputMethodEvent ime = (InputMethodEvent)e;
                mostRecentEventTime2 = ime.getWhen();
            } else if (e instanceof ActionEvent) {
                ActionEvent ae = (ActionEvent)e;
                mostRecentEventTime2 = ae.getWhen();
            } else if (e instanceof InvocationEvent) {
                InvocationEvent ie = (InvocationEvent)e;
                mostRecentEventTime2 = ie.getWhen();
            }
            this.mostRecentEventTime = Math.max(this.mostRecentEventTime, mostRecentEventTime2);
        }
        finally {
            this.pushPopLock.unlock();
        }
    }

    public static void invokeLater(Runnable runnable) {
        Toolkit.getEventQueue().postEvent(new InvocationEvent((Object)Toolkit.getDefaultToolkit(), runnable));
    }

    public static void invokeAndWait(Runnable runnable) throws InterruptedException, InvocationTargetException {
        EventQueue.invokeAndWait(Toolkit.getDefaultToolkit(), runnable);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void invokeAndWait(Object source, Runnable runnable) throws InterruptedException, InvocationTargetException {
        if (EventQueue.isDispatchThread()) {
            throw new Error("Cannot call invokeAndWait from the event dispatcher thread");
        }
        class AWTInvocationLock {
            AWTInvocationLock() {
            }
        }
        AWTInvocationLock lock = new AWTInvocationLock();
        InvocationEvent event = new InvocationEvent(source, runnable, lock, true);
        AWTInvocationLock aWTInvocationLock = lock;
        synchronized (aWTInvocationLock) {
            Toolkit.getEventQueue().postEvent(event);
            while (!event.isDispatched()) {
                lock.wait();
            }
        }
        Throwable eventThrowable = event.getThrowable();
        if (eventThrowable != null) {
            throw new InvocationTargetException(eventThrowable);
        }
    }

    private void wakeup(boolean isShutdown) {
        this.pushPopLock.lock();
        try {
            if (this.nextQueue != null) {
                this.nextQueue.wakeup(isShutdown);
            } else if (this.dispatchThread != null) {
                this.pushPopCond.signalAll();
            } else if (!isShutdown) {
                this.initDispatchThread();
            }
        }
        finally {
            this.pushPopLock.unlock();
        }
    }

    private void setFwDispatcher(FwDispatcher dispatcher) {
        if (this.nextQueue != null) {
            this.nextQueue.setFwDispatcher(dispatcher);
        } else {
            this.fwDispatcher = dispatcher;
        }
    }

    static {
        AWTAccessor.setEventQueueAccessor(new AWTAccessor.EventQueueAccessor(){

            @Override
            public Thread getDispatchThread(EventQueue eventQueue) {
                return eventQueue.getDispatchThread();
            }

            @Override
            public boolean isDispatchThreadImpl(EventQueue eventQueue) {
                return eventQueue.isDispatchThreadImpl();
            }

            @Override
            public void removeSourceEvents(EventQueue eventQueue, Object source, boolean removeAllEvents) {
                eventQueue.removeSourceEvents(source, removeAllEvents);
            }

            @Override
            public boolean noEvents(EventQueue eventQueue) {
                return eventQueue.noEvents();
            }

            @Override
            public void wakeup(EventQueue eventQueue, boolean isShutdown) {
                eventQueue.wakeup(isShutdown);
            }

            @Override
            public void invokeAndWait(Object source, Runnable r) throws InterruptedException, InvocationTargetException {
                EventQueue.invokeAndWait(source, r);
            }

            @Override
            public void setFwDispatcher(EventQueue eventQueue, FwDispatcher dispatcher) {
                eventQueue.setFwDispatcher(dispatcher);
            }

            @Override
            public long getMostRecentEventTime(EventQueue eventQueue) {
                return eventQueue.getMostRecentEventTimeImpl();
            }
        });
        fxAppThreadIsDispatchThread = AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

            @Override
            public Boolean run() {
                return "true".equals(System.getProperty("javafx.embed.singleThread"));
            }
        });
        javaSecurityAccess = SharedSecrets.getJavaSecurityAccess();
    }

    private class FwSecondaryLoopWrapper
    implements SecondaryLoop {
        private final SecondaryLoop loop;
        private final EventFilter filter;

        public FwSecondaryLoopWrapper(SecondaryLoop loop, EventFilter filter) {
            this.loop = loop;
            this.filter = filter;
        }

        @Override
        public boolean enter() {
            if (this.filter != null) {
                EventQueue.this.dispatchThread.addEventFilter(this.filter);
            }
            return this.loop.enter();
        }

        @Override
        public boolean exit() {
            if (this.filter != null) {
                EventQueue.this.dispatchThread.removeEventFilter(this.filter);
            }
            return this.loop.exit();
        }
    }
}

