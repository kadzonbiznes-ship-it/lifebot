/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna.internal;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cleaner {
    private static final Cleaner INSTANCE = new Cleaner();
    private final ReferenceQueue<Object> referenceQueue = new ReferenceQueue();
    private Thread cleanerThread;
    private CleanerRef firstCleanable;

    public static Cleaner getCleaner() {
        return INSTANCE;
    }

    private Cleaner() {
    }

    public synchronized Cleanable register(Object obj, Runnable cleanupTask) {
        return this.add(new CleanerRef(this, obj, this.referenceQueue, cleanupTask));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private synchronized CleanerRef add(CleanerRef ref) {
        ReferenceQueue<Object> referenceQueue = this.referenceQueue;
        synchronized (referenceQueue) {
            if (this.firstCleanable == null) {
                this.firstCleanable = ref;
            } else {
                ref.setNext(this.firstCleanable);
                this.firstCleanable.setPrevious(ref);
                this.firstCleanable = ref;
            }
            if (this.cleanerThread == null) {
                Logger.getLogger(Cleaner.class.getName()).log(Level.FINE, "Starting CleanerThread");
                this.cleanerThread = new CleanerThread();
                this.cleanerThread.start();
            }
            return ref;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private synchronized boolean remove(CleanerRef ref) {
        ReferenceQueue<Object> referenceQueue = this.referenceQueue;
        synchronized (referenceQueue) {
            boolean inChain = false;
            if (ref == this.firstCleanable) {
                this.firstCleanable = ref.getNext();
                inChain = true;
            }
            if (ref.getPrevious() != null) {
                ref.getPrevious().setNext(ref.getNext());
            }
            if (ref.getNext() != null) {
                ref.getNext().setPrevious(ref.getPrevious());
            }
            if (ref.getPrevious() != null || ref.getNext() != null) {
                inChain = true;
            }
            ref.setNext(null);
            ref.setPrevious(null);
            return inChain;
        }
    }

    private static class CleanerRef
    extends PhantomReference<Object>
    implements Cleanable {
        private final Cleaner cleaner;
        private final Runnable cleanupTask;
        private CleanerRef previous;
        private CleanerRef next;

        public CleanerRef(Cleaner cleaner, Object referent, ReferenceQueue<? super Object> q, Runnable cleanupTask) {
            super(referent, q);
            this.cleaner = cleaner;
            this.cleanupTask = cleanupTask;
        }

        @Override
        public void clean() {
            if (this.cleaner.remove(this)) {
                this.cleanupTask.run();
            }
        }

        CleanerRef getPrevious() {
            return this.previous;
        }

        void setPrevious(CleanerRef previous) {
            this.previous = previous;
        }

        CleanerRef getNext() {
            return this.next;
        }

        void setNext(CleanerRef next) {
            this.next = next;
        }
    }

    private class CleanerThread
    extends Thread {
        private static final long CLEANER_LINGER_TIME = 30000L;

        public CleanerThread() {
            super("JNA Cleaner");
            this.setDaemon(true);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void run() {
            while (true) {
                try {
                    while (true) {
                        Reference ref;
                        if ((ref = Cleaner.this.referenceQueue.remove(30000L)) instanceof CleanerRef) {
                            ((CleanerRef)ref).clean();
                            continue;
                        }
                        if (ref == null) break;
                    }
                    ReferenceQueue referenceQueue = Cleaner.this.referenceQueue;
                    synchronized (referenceQueue) {
                        Logger logger = Logger.getLogger(Cleaner.class.getName());
                        if (Cleaner.this.firstCleanable == null) {
                            Cleaner.this.cleanerThread = null;
                            logger.log(Level.FINE, "Shutting down CleanerThread");
                            break;
                        }
                        if (logger.isLoggable(Level.FINER)) {
                            StringBuilder registeredCleaners = new StringBuilder();
                            CleanerRef cleanerRef = Cleaner.this.firstCleanable;
                            while (cleanerRef != null) {
                                if (registeredCleaners.length() != 0) {
                                    registeredCleaners.append(", ");
                                }
                                registeredCleaners.append(cleanerRef.cleanupTask.toString());
                                cleanerRef = cleanerRef.next;
                            }
                            logger.log(Level.FINER, "Registered Cleaners: {0}", registeredCleaners.toString());
                        }
                        continue;
                    }
                }
                catch (InterruptedException ex) {
                }
                catch (Exception ex) {
                    Logger.getLogger(Cleaner.class.getName()).log(Level.SEVERE, null, ex);
                    continue;
                }
                break;
            }
        }
    }

    public static interface Cleanable {
        public void clean();
    }
}

