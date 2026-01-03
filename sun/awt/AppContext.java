/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.event.InvocationEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.SoftReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import jdk.internal.access.JavaAWTAccess;
import jdk.internal.access.SharedSecrets;
import sun.awt.AWTAutoShutdown;
import sun.awt.MostRecentKeyValue;
import sun.awt.SunToolkit;
import sun.util.logging.PlatformLogger;

public final class AppContext {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.AppContext");
    public static final Object EVENT_QUEUE_KEY = new StringBuffer("EventQueue");
    public static final Object EVENT_QUEUE_LOCK_KEY = new StringBuilder("EventQueue.Lock");
    public static final Object EVENT_QUEUE_COND_KEY = new StringBuilder("EventQueue.Condition");
    private static final Map<ThreadGroup, AppContext> threadGroup2appContext = Collections.synchronizedMap(new IdentityHashMap());
    private static volatile AppContext mainAppContext;
    private static final Object getAppContextLock;
    private final Map<Object, Object> table = new HashMap<Object, Object>();
    private final ThreadGroup threadGroup;
    private PropertyChangeSupport changeSupport = null;
    public static final String DISPOSED_PROPERTY_NAME = "disposed";
    public static final String GUI_DISPOSED = "guidisposed";
    private volatile State state = State.VALID;
    private static final AtomicInteger numAppContexts;
    private final ClassLoader contextClassLoader;
    private static final ThreadLocal<AppContext> threadAppContext;
    private long DISPOSAL_TIMEOUT = 5000L;
    private long THREAD_INTERRUPT_TIMEOUT = 1000L;
    private MostRecentKeyValue mostRecentKeyValue = null;
    private MostRecentKeyValue shadowMostRecentKeyValue = null;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Set<AppContext> getAppContexts() {
        Map<ThreadGroup, AppContext> map = threadGroup2appContext;
        synchronized (map) {
            return new HashSet<AppContext>(threadGroup2appContext.values());
        }
    }

    public boolean isDisposed() {
        return this.state == State.DISPOSED;
    }

    AppContext(ThreadGroup threadGroup) {
        numAppContexts.incrementAndGet();
        this.threadGroup = threadGroup;
        threadGroup2appContext.put(threadGroup, this);
        this.contextClassLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>(this){

            @Override
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
        ReentrantLock eventQueuePushPopLock = new ReentrantLock();
        this.put(EVENT_QUEUE_LOCK_KEY, eventQueuePushPopLock);
        Condition eventQueuePushPopCond = eventQueuePushPopLock.newCondition();
        this.put(EVENT_QUEUE_COND_KEY, eventQueuePushPopCond);
    }

    private static void initMainAppContext() {
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                ThreadGroup currentThreadGroup = Thread.currentThread().getThreadGroup();
                ThreadGroup parentThreadGroup = currentThreadGroup.getParent();
                while (parentThreadGroup != null) {
                    currentThreadGroup = parentThreadGroup;
                    parentThreadGroup = currentThreadGroup.getParent();
                }
                mainAppContext = SunToolkit.createNewAppContext(currentThreadGroup);
                return null;
            }
        });
    }

    public static AppContext getAppContext() {
        if (numAppContexts.get() == 1 && mainAppContext != null) {
            return mainAppContext;
        }
        AppContext appContext = threadAppContext.get();
        if (null == appContext) {
            appContext = AccessController.doPrivileged(new PrivilegedAction<AppContext>(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public AppContext run() {
                    ThreadGroup currentThreadGroup;
                    ThreadGroup threadGroup = currentThreadGroup = Thread.currentThread().getThreadGroup();
                    Object object = getAppContextLock;
                    synchronized (object) {
                        if (numAppContexts.get() == 0) {
                            if (System.getProperty("javaplugin.version") == null && System.getProperty("javawebstart.version") == null) {
                                AppContext.initMainAppContext();
                            } else if (System.getProperty("javafx.version") != null && threadGroup.getParent() != null) {
                                SunToolkit.createNewAppContext();
                            }
                        }
                    }
                    AppContext context = threadGroup2appContext.get(threadGroup);
                    while (context == null) {
                        if ((threadGroup = threadGroup.getParent()) == null) {
                            ThreadGroup smThreadGroup;
                            SecurityManager securityManager = System.getSecurityManager();
                            if (securityManager != null && (smThreadGroup = securityManager.getThreadGroup()) != null) {
                                return threadGroup2appContext.get(smThreadGroup);
                            }
                            return null;
                        }
                        context = threadGroup2appContext.get(threadGroup);
                    }
                    for (ThreadGroup tg = currentThreadGroup; tg != threadGroup; tg = tg.getParent()) {
                        threadGroup2appContext.put(tg, context);
                    }
                    threadAppContext.set(context);
                    return context;
                }
            });
        }
        return appContext;
    }

    public static boolean isMainContext(AppContext ctx) {
        return ctx != null && ctx == mainAppContext;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void dispose() throws IllegalThreadStateException {
        System.err.println("WARNING: sun.awt.AppContext.dispose() no longer stops threads.\nAdditionally AppContext will be removed in a future release.\nRemove all uses of this internal class as soon as possible.\nThere is no replacement.\n");
        if (this.threadGroup.parentOf(Thread.currentThread().getThreadGroup())) {
            throw new IllegalThreadStateException("Current Thread is contained within AppContext to be disposed.");
        }
        AppContext appContext = this;
        synchronized (appContext) {
            if (this.state != State.VALID) {
                return;
            }
            this.state = State.BEING_DISPOSED;
        }
        final PropertyChangeSupport changeSupport = this.changeSupport;
        if (changeSupport != null) {
            changeSupport.firePropertyChange(DISPOSED_PROPERTY_NAME, false, true);
        }
        final Object notificationLock = new Object();
        Runnable runnable = new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                Window[] windowsToDispose;
                for (Window w : windowsToDispose = Window.getOwnerlessWindows()) {
                    try {
                        w.dispose();
                    }
                    catch (Throwable t) {
                        log.finer("exception occurred while disposing app context", t);
                    }
                }
                AccessController.doPrivileged(new PrivilegedAction<Void>(this){

                    @Override
                    public Void run() {
                        if (!GraphicsEnvironment.isHeadless() && SystemTray.isSupported()) {
                            TrayIcon[] trayIconsToDispose;
                            SystemTray systemTray = SystemTray.getSystemTray();
                            for (TrayIcon ti : trayIconsToDispose = systemTray.getTrayIcons()) {
                                systemTray.remove(ti);
                            }
                        }
                        return null;
                    }
                });
                if (changeSupport != null) {
                    changeSupport.firePropertyChange(AppContext.GUI_DISPOSED, false, true);
                }
                Object object = notificationLock;
                synchronized (object) {
                    notificationLock.notifyAll();
                }
            }
        };
        Object object = notificationLock;
        synchronized (object) {
            SunToolkit.postEvent(this, new InvocationEvent((Object)Toolkit.getDefaultToolkit(), runnable));
            try {
                notificationLock.wait(this.DISPOSAL_TIMEOUT);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        runnable = new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                Object object = notificationLock;
                synchronized (object) {
                    notificationLock.notifyAll();
                }
            }
        };
        object = notificationLock;
        synchronized (object) {
            SunToolkit.postEvent(this, new InvocationEvent((Object)Toolkit.getDefaultToolkit(), runnable));
            try {
                notificationLock.wait(this.DISPOSAL_TIMEOUT);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        object = this;
        synchronized (object) {
            this.state = State.DISPOSED;
        }
        this.threadGroup.interrupt();
        long startTime = System.currentTimeMillis();
        long endTime = startTime + this.THREAD_INTERRUPT_TIMEOUT;
        while (this.threadGroup.activeCount() > 0 && System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(10L);
            }
            catch (InterruptedException interruptedException) {}
        }
        int numSubGroups = this.threadGroup.activeGroupCount();
        if (numSubGroups > 0) {
            ThreadGroup[] subGroups = new ThreadGroup[numSubGroups];
            numSubGroups = this.threadGroup.enumerate(subGroups);
            for (int subGroup = 0; subGroup < numSubGroups; ++subGroup) {
                threadGroup2appContext.remove(subGroups[subGroup]);
            }
        }
        threadGroup2appContext.remove(this.threadGroup);
        threadAppContext.set(null);
        Map<Object, Object> map = this.table;
        synchronized (map) {
            this.table.clear();
        }
        numAppContexts.decrementAndGet();
        this.mostRecentKeyValue = null;
    }

    static void stopEventDispatchThreads() {
        for (AppContext appContext : AppContext.getAppContexts()) {
            if (appContext.isDisposed()) continue;
            PostShutdownEventRunnable r = new PostShutdownEventRunnable(appContext);
            if (appContext != AppContext.getAppContext()) {
                CreateThreadAction action = new CreateThreadAction(appContext, r);
                Thread thread = AccessController.doPrivileged(action);
                thread.start();
                continue;
            }
            r.run();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Object get(Object key) {
        Map<Object, Object> map = this.table;
        synchronized (map) {
            MostRecentKeyValue recent = this.mostRecentKeyValue;
            if (recent != null && recent.key == key) {
                return recent.value;
            }
            Object value = this.table.get(key);
            if (this.mostRecentKeyValue == null) {
                this.mostRecentKeyValue = new MostRecentKeyValue(key, value);
                this.shadowMostRecentKeyValue = new MostRecentKeyValue(key, value);
            } else {
                MostRecentKeyValue auxKeyValue = this.mostRecentKeyValue;
                this.shadowMostRecentKeyValue.setPair(key, value);
                this.mostRecentKeyValue = this.shadowMostRecentKeyValue;
                this.shadowMostRecentKeyValue = auxKeyValue;
            }
            return value;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Object put(Object key, Object value) {
        Map<Object, Object> map = this.table;
        synchronized (map) {
            MostRecentKeyValue recent = this.mostRecentKeyValue;
            if (recent != null && recent.key == key) {
                recent.value = value;
            }
            return this.table.put(key, value);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Object remove(Object key) {
        Map<Object, Object> map = this.table;
        synchronized (map) {
            MostRecentKeyValue recent = this.mostRecentKeyValue;
            if (recent != null && recent.key == key) {
                recent.value = null;
            }
            return this.table.remove(key);
        }
    }

    public ThreadGroup getThreadGroup() {
        return this.threadGroup;
    }

    public ClassLoader getContextClassLoader() {
        return this.contextClassLoader;
    }

    public String toString() {
        return this.getClass().getName() + "[threadGroup=" + this.threadGroup.getName() + "]";
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        if (this.changeSupport == null) {
            return new PropertyChangeListener[0];
        }
        return this.changeSupport.getPropertyChangeListeners();
    }

    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (this.changeSupport == null) {
            this.changeSupport = new PropertyChangeSupport(this);
        }
        this.changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (listener == null || this.changeSupport == null) {
            return;
        }
        this.changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        if (this.changeSupport == null) {
            return new PropertyChangeListener[0];
        }
        return this.changeSupport.getPropertyChangeListeners(propertyName);
    }

    public static <T> T getSoftReferenceValue(Object key, Supplier<T> supplier) {
        Object object;
        AppContext appContext = AppContext.getAppContext();
        SoftReference ref = (SoftReference)appContext.get(key);
        if (ref != null && (object = ref.get()) != null) {
            return object;
        }
        object = supplier.get();
        ref = new SoftReference(object);
        appContext.put(key, ref);
        return object;
    }

    static {
        getAppContextLock = new GetAppContextLock();
        numAppContexts = new AtomicInteger();
        threadAppContext = new ThreadLocal();
        SharedSecrets.setJavaAWTAccess(new JavaAWTAccess(){

            private boolean hasRootThreadGroup(final AppContext ecx) {
                return AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

                    @Override
                    public Boolean run() {
                        return ecx.threadGroup.getParent() == null;
                    }
                });
            }

            @Override
            public Object getAppletContext() {
                if (numAppContexts.get() == 0) {
                    return null;
                }
                AppContext ecx = null;
                if (numAppContexts.get() > 0) {
                    ecx = ecx != null ? ecx : AppContext.getAppContext();
                }
                boolean isMainAppContext = ecx == null || mainAppContext == ecx || mainAppContext == null && this.hasRootThreadGroup(ecx);
                return isMainAppContext ? null : ecx;
            }
        });
    }

    private static enum State {
        VALID,
        BEING_DISPOSED,
        DISPOSED;

    }

    static final class PostShutdownEventRunnable
    implements Runnable {
        private final AppContext appContext;

        PostShutdownEventRunnable(AppContext ac) {
            this.appContext = ac;
        }

        @Override
        public void run() {
            EventQueue eq = (EventQueue)this.appContext.get(EVENT_QUEUE_KEY);
            if (eq != null) {
                eq.postEvent(AWTAutoShutdown.getShutdownEvent());
            }
        }
    }

    static final class CreateThreadAction
    implements PrivilegedAction<Thread> {
        private final AppContext appContext;
        private final Runnable runnable;

        CreateThreadAction(AppContext ac, Runnable r) {
            this.appContext = ac;
            this.runnable = r;
        }

        @Override
        public Thread run() {
            Thread t = new Thread(this.appContext.getThreadGroup(), this.runnable, "AppContext Disposer", 0L, false);
            t.setContextClassLoader(this.appContext.getContextClassLoader());
            t.setPriority(6);
            t.setDaemon(true);
            return t;
        }
    }

    private static class GetAppContextLock {
        private GetAppContextLock() {
        }
    }
}

