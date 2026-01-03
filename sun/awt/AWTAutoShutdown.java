/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.AWTEvent;
import java.security.AccessController;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import sun.awt.AppContext;
import sun.awt.util.ThreadGroupUtils;
import sun.util.logging.PlatformLogger;

public final class AWTAutoShutdown
implements Runnable {
    private static final AWTAutoShutdown theInstance = new AWTAutoShutdown();
    private final Object mainLock = new Object();
    private final Object activationLock = new Object();
    private final Set<Thread> busyThreadSet = new HashSet<Thread>(7);
    private boolean toolkitThreadBusy = false;
    private final Map<Object, Object> peerMap = new IdentityHashMap<Object, Object>();
    private Thread blockerThread = null;
    private boolean timeoutPassed = false;
    private static final int SAFETY_TIMEOUT = 1000;

    private AWTAutoShutdown() {
    }

    public static AWTAutoShutdown getInstance() {
        return theInstance;
    }

    public static void notifyToolkitThreadBusy() {
        AWTAutoShutdown.getInstance().setToolkitBusy(true);
    }

    public static void notifyToolkitThreadFree() {
        AWTAutoShutdown.getInstance().setToolkitBusy(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void notifyThreadBusy(Thread thread) {
        if (thread == null) {
            return;
        }
        Object object = this.activationLock;
        synchronized (object) {
            Object object2 = this.mainLock;
            synchronized (object2) {
                if (this.blockerThread == null) {
                    this.activateBlockerThread();
                } else if (this.isReadyToShutdown()) {
                    this.mainLock.notifyAll();
                    this.timeoutPassed = false;
                }
                this.busyThreadSet.add(thread);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void notifyThreadFree(Thread thread) {
        if (thread == null) {
            return;
        }
        Object object = this.activationLock;
        synchronized (object) {
            Object object2 = this.mainLock;
            synchronized (object2) {
                this.busyThreadSet.remove(thread);
                if (this.isReadyToShutdown()) {
                    this.mainLock.notifyAll();
                    this.timeoutPassed = false;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void notifyPeerMapUpdated() {
        Object object = this.activationLock;
        synchronized (object) {
            Object object2 = this.mainLock;
            synchronized (object2) {
                if (!this.isReadyToShutdown() && this.blockerThread == null) {
                    this.activateBlockerThread();
                } else {
                    this.mainLock.notifyAll();
                    this.timeoutPassed = false;
                }
            }
        }
    }

    private boolean isReadyToShutdown() {
        return !this.toolkitThreadBusy && this.peerMap.isEmpty() && this.busyThreadSet.isEmpty();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setToolkitBusy(boolean busy) {
        if (busy != this.toolkitThreadBusy) {
            Object object = this.activationLock;
            synchronized (object) {
                Object object2 = this.mainLock;
                synchronized (object2) {
                    if (busy != this.toolkitThreadBusy) {
                        if (busy) {
                            if (this.blockerThread == null) {
                                this.activateBlockerThread();
                            } else if (this.isReadyToShutdown()) {
                                this.mainLock.notifyAll();
                                this.timeoutPassed = false;
                            }
                            this.toolkitThreadBusy = busy;
                        } else {
                            this.toolkitThreadBusy = busy;
                            if (this.isReadyToShutdown()) {
                                this.mainLock.notifyAll();
                                this.timeoutPassed = false;
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        Thread currentThread = Thread.currentThread();
        boolean interrupted = false;
        Object object = this.mainLock;
        synchronized (object) {
            try {
                this.mainLock.notifyAll();
                block8: while (this.blockerThread == currentThread) {
                    this.mainLock.wait();
                    this.timeoutPassed = false;
                    while (this.isReadyToShutdown()) {
                        if (this.timeoutPassed) {
                            this.timeoutPassed = false;
                            this.blockerThread = null;
                            continue block8;
                        }
                        this.timeoutPassed = true;
                        this.mainLock.wait(1000L);
                    }
                }
            }
            catch (InterruptedException e) {
                interrupted = true;
            }
            finally {
                if (this.blockerThread == currentThread) {
                    this.blockerThread = null;
                }
            }
        }
        if (!interrupted) {
            AppContext.stopEventDispatchThreads();
        }
    }

    static AWTEvent getShutdownEvent() {
        return new AWTEvent(AWTAutoShutdown.getInstance(), 0){};
    }

    private void activateBlockerThread() {
        AccessController.doPrivileged(() -> {
            String name = "AWT-Shutdown";
            Thread thread = new Thread(ThreadGroupUtils.getRootThreadGroup(), this, name, 0L, false);
            thread.setContextClassLoader(null);
            thread.setDaemon(false);
            this.blockerThread = thread;
            return thread;
        }).start();
        try {
            this.mainLock.wait();
        }
        catch (InterruptedException e) {
            System.err.println("AWT blocker activation interrupted:");
            e.printStackTrace();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void registerPeer(Object target, Object peer) {
        Object object = this.activationLock;
        synchronized (object) {
            Object object2 = this.mainLock;
            synchronized (object2) {
                this.peerMap.put(target, peer);
                this.notifyPeerMapUpdated();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void unregisterPeer(Object target, Object peer) {
        Object object = this.activationLock;
        synchronized (object) {
            Object object2 = this.mainLock;
            synchronized (object2) {
                if (this.peerMap.get(target) == peer) {
                    this.peerMap.remove(target);
                    this.notifyPeerMapUpdated();
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    Object getPeer(Object target) {
        Object object = this.activationLock;
        synchronized (object) {
            Object object2 = this.mainLock;
            synchronized (object2) {
                return this.peerMap.get(target);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void dumpPeers(PlatformLogger aLog) {
        if (aLog.isLoggable(PlatformLogger.Level.FINE)) {
            Object object = this.activationLock;
            synchronized (object) {
                Object object2 = this.mainLock;
                synchronized (object2) {
                    aLog.fine("Mapped peers:");
                    for (Object key : this.peerMap.keySet()) {
                        aLog.fine(String.valueOf(key) + "->" + String.valueOf(this.peerMap.get(key)));
                    }
                }
            }
        }
    }
}

