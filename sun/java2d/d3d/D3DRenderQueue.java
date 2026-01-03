/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.d3d;

import sun.java2d.ScreenUpdateManager;
import sun.java2d.d3d.D3DScreenUpdateManager;
import sun.java2d.pipe.RenderBuffer;
import sun.java2d.pipe.RenderQueue;

public class D3DRenderQueue
extends RenderQueue {
    private static D3DRenderQueue theInstance;
    private static Thread rqThread;

    private D3DRenderQueue() {
    }

    public static synchronized D3DRenderQueue getInstance() {
        if (theInstance == null) {
            theInstance = new D3DRenderQueue();
            theInstance.flushAndInvokeNow(new Runnable(){

                @Override
                public void run() {
                    rqThread = Thread.currentThread();
                }
            });
        }
        return theInstance;
    }

    public static void sync() {
        if (theInstance != null) {
            D3DScreenUpdateManager mgr = (D3DScreenUpdateManager)ScreenUpdateManager.getInstance();
            mgr.runUpdateNow();
            theInstance.lock();
            try {
                theInstance.ensureCapacity(4);
                theInstance.getBuffer().putInt(76);
                theInstance.flushNow();
            }
            finally {
                theInstance.unlock();
            }
        }
    }

    public static void restoreDevices() {
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            rq.ensureCapacity(4);
            rq.getBuffer().putInt(77);
            rq.flushNow();
        }
        finally {
            rq.unlock();
        }
    }

    public static boolean isRenderQueueThread() {
        return Thread.currentThread() == rqThread;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void disposeGraphicsConfig(long pConfigInfo) {
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            RenderBuffer buf = rq.getBuffer();
            rq.ensureCapacityAndAlignment(12, 4);
            buf.putInt(74);
            buf.putLong(pConfigInfo);
            rq.flushNow();
        }
        finally {
            rq.unlock();
        }
    }

    @Override
    public void flushNow() {
        this.flushBuffer(null);
    }

    @Override
    public void flushAndInvokeNow(Runnable r) {
        this.flushBuffer(r);
    }

    private native void flushBuffer(long var1, int var3, Runnable var4);

    private void flushBuffer(Runnable task) {
        int limit = this.buf.position();
        if (limit > 0 || task != null) {
            this.flushBuffer(this.buf.getAddress(), limit, task);
        }
        this.buf.clear();
        this.refSet.clear();
    }
}

