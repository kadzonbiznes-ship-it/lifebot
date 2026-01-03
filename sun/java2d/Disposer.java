/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentLinkedDeque;
import sun.awt.util.ThreadGroupUtils;
import sun.java2d.DefaultDisposerRecord;
import sun.java2d.DisposerRecord;
import sun.java2d.DisposerTarget;
import sun.security.action.GetPropertyAction;

public class Disposer
implements Runnable {
    private static final ReferenceQueue<Object> queue = new ReferenceQueue();
    private static final Hashtable<Reference<Object>, DisposerRecord> records = new Hashtable();
    private static Disposer disposerInstance;
    public static final int WEAK = 0;
    public static final int PHANTOM = 1;
    public static int refType;
    private static ConcurrentLinkedDeque<DisposerRecord> deferredRecords;
    public static volatile boolean pollingQueue;

    public static void addRecord(Object target, long disposeMethod, long pData) {
        disposerInstance.add(target, new DefaultDisposerRecord(disposeMethod, pData));
    }

    public static void addRecord(Object target, DisposerRecord rec) {
        disposerInstance.add(target, rec);
    }

    synchronized void add(Object target, DisposerRecord rec) {
        if (target instanceof DisposerTarget) {
            target = ((DisposerTarget)target).getDisposerReferent();
        }
        Reference ref = refType == 1 ? new PhantomReference<Object>(target, queue) : new WeakReference<Object>(target, queue);
        records.put(ref, rec);
    }

    @Override
    public void run() {
        while (true) {
            try {
                while (true) {
                    Reference<Object> obj = queue.remove();
                    obj.clear();
                    DisposerRecord rec = records.remove(obj);
                    Disposer.safeDispose(rec);
                    obj = null;
                    rec = null;
                    Disposer.clearDeferredRecords();
                }
            }
            catch (Exception e) {
                System.out.println("Exception while removing reference.");
                continue;
            }
            break;
        }
    }

    private static void safeDispose(DisposerRecord rec) {
        try {
            rec.dispose();
        }
        catch (Exception e) {
            System.out.println("Exception while disposing deferred rec.");
        }
    }

    private static void clearDeferredRecords() {
        while (!deferredRecords.isEmpty()) {
            DisposerRecord rec = deferredRecords.pollFirst();
            if (rec == null) continue;
            Disposer.safeDispose(rec);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void pollRemove() {
        if (pollingQueue) {
            return;
        }
        pollingQueue = true;
        int freed = 0;
        int deferred = 0;
        try {
            Reference<Object> obj;
            while (freed < 10000 && deferred < 100 && (obj = queue.poll()) != null) {
                ++freed;
                obj.clear();
                DisposerRecord rec = records.remove(obj);
                if (rec instanceof PollDisposable) {
                    Disposer.safeDispose(rec);
                    obj = null;
                    rec = null;
                    continue;
                }
                if (rec == null) continue;
                ++deferred;
                deferredRecords.offerLast(rec);
            }
        }
        catch (Exception e) {
            System.out.println("Exception while removing reference.");
        }
        finally {
            pollingQueue = false;
        }
    }

    private static native void initIDs();

    public static void addReference(Reference<Object> ref, DisposerRecord rec) {
        records.put(ref, rec);
    }

    public static void addObjectRecord(Object obj, DisposerRecord rec) {
        records.put(new WeakReference<Object>(obj, queue), rec);
    }

    public static ReferenceQueue<Object> getQueue() {
        return queue;
    }

    static {
        refType = 1;
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                System.loadLibrary("awt");
                return null;
            }
        });
        Disposer.initIDs();
        String type = AccessController.doPrivileged(new GetPropertyAction("sun.java2d.reftype"));
        if (type != null) {
            if (type.equals("weak")) {
                refType = 0;
                System.err.println("Using WEAK refs");
            } else {
                refType = 1;
                System.err.println("Using PHANTOM refs");
            }
        }
        disposerInstance = new Disposer();
        AccessController.doPrivileged(() -> {
            String name = "Java2D Disposer";
            ThreadGroup rootTG = ThreadGroupUtils.getRootThreadGroup();
            Thread t = new Thread(rootTG, disposerInstance, name, 0L, false);
            t.setContextClassLoader(null);
            t.setDaemon(true);
            t.setPriority(10);
            t.start();
            return null;
        });
        deferredRecords = new ConcurrentLinkedDeque();
    }

    public static interface PollDisposable {
    }
}

