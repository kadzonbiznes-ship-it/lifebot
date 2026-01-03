/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.windows;

import java.util.Map;
import java.util.WeakHashMap;
import sun.awt.windows.WToolkit;

abstract class WObjectPeer {
    volatile long pData;
    private volatile boolean destroyed;
    volatile Object target;
    private volatile boolean disposed;
    volatile Error createError;
    private final Object stateLock = new Object();
    private volatile Map<WObjectPeer, WObjectPeer> childPeers;

    WObjectPeer() {
    }

    public static WObjectPeer getPeerForTarget(Object t) {
        WObjectPeer peer = (WObjectPeer)WToolkit.targetToPeer(t);
        return peer;
    }

    public long getData() {
        return this.pData;
    }

    public Object getTarget() {
        return this.target;
    }

    public final Object getStateLock() {
        return this.stateLock;
    }

    protected abstract void disposeImpl();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void dispose() {
        boolean call_disposeImpl = false;
        WObjectPeer wObjectPeer = this;
        synchronized (wObjectPeer) {
            if (!this.disposed) {
                call_disposeImpl = true;
                this.disposed = true;
            }
        }
        if (call_disposeImpl) {
            if (this.childPeers != null) {
                this.disposeChildPeers();
            }
            this.disposeImpl();
        }
    }

    protected final boolean isDisposed() {
        return this.disposed;
    }

    private static native void initIDs();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void addChildPeer(WObjectPeer child) {
        Object object = this.getStateLock();
        synchronized (object) {
            if (this.childPeers == null) {
                this.childPeers = new WeakHashMap<WObjectPeer, WObjectPeer>();
            }
            if (this.isDisposed()) {
                throw new IllegalStateException("Parent peer is disposed");
            }
            this.childPeers.put(child, this);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void disposeChildPeers() {
        Object object = this.getStateLock();
        synchronized (object) {
            for (WObjectPeer child : this.childPeers.keySet()) {
                if (child == null) continue;
                try {
                    child.dispose();
                }
                catch (Exception exception) {}
            }
        }
    }

    static {
        WObjectPeer.initIDs();
    }
}

