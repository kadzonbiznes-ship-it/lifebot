/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.stream;

import java.io.IOException;
import java.security.AccessController;
import java.util.Set;
import java.util.WeakHashMap;
import javax.imageio.stream.ImageInputStream;
import sun.awt.util.ThreadGroupUtils;

public class StreamCloser {
    private static WeakHashMap<CloseAction, Object> toCloseQueue;
    private static Thread streamCloser;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void addToQueue(CloseAction ca) {
        Class<StreamCloser> clazz = StreamCloser.class;
        synchronized (StreamCloser.class) {
            if (toCloseQueue == null) {
                toCloseQueue = new WeakHashMap();
            }
            toCloseQueue.put(ca, null);
            if (streamCloser == null) {
                Runnable streamCloserRunnable = new Runnable(){

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     * Enabled force condition propagation
                     * Lifted jumps to return sites
                     */
                    @Override
                    public void run() {
                        if (toCloseQueue == null) return;
                        Class<StreamCloser> clazz = StreamCloser.class;
                        synchronized (StreamCloser.class) {
                            Set<CloseAction> set = toCloseQueue.keySet();
                            CloseAction[] actions = new CloseAction[set.size()];
                            for (CloseAction ca : actions = set.toArray(actions)) {
                                if (ca == null) continue;
                                try {
                                    ca.performAction();
                                }
                                catch (IOException iOException) {
                                    // empty catch block
                                }
                            }
                            // ** MonitorExit[var1_1] (shouldn't be in output)
                            return;
                        }
                    }
                };
                AccessController.doPrivileged(() -> {
                    ThreadGroup tg = ThreadGroupUtils.getRootThreadGroup();
                    streamCloser = new Thread(tg, streamCloserRunnable, "StreamCloser", 0L, false);
                    streamCloser.setContextClassLoader(null);
                    Runtime.getRuntime().addShutdownHook(streamCloser);
                    return null;
                });
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void removeFromQueue(CloseAction ca) {
        Class<StreamCloser> clazz = StreamCloser.class;
        synchronized (StreamCloser.class) {
            if (toCloseQueue != null) {
                toCloseQueue.remove(ca);
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return;
        }
    }

    public static CloseAction createCloseAction(ImageInputStream iis) {
        return new CloseAction(iis);
    }

    public static final class CloseAction {
        private ImageInputStream iis;

        private CloseAction(ImageInputStream iis) {
            this.iis = iis;
        }

        public void performAction() throws IOException {
            if (this.iis != null) {
                this.iis.close();
            }
        }
    }
}

