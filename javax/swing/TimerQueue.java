/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.security.AccessController;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import sun.awt.AppContext;

class TimerQueue
implements Runnable {
    private static final Object sharedInstanceKey = new StringBuffer("TimerQueue.sharedInstanceKey");
    private final DelayQueue<DelayedTimer> queue = new DelayQueue();
    private volatile boolean running;
    private final Lock runningLock = new ReentrantLock();
    private static final Object classLock = new Object();
    private static final long NANO_ORIGIN = System.nanoTime();

    public TimerQueue() {
        this.startIfNeeded();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static TimerQueue sharedInstance() {
        Object object = classLock;
        synchronized (object) {
            TimerQueue sharedInst = (TimerQueue)SwingUtilities.appContextGet(sharedInstanceKey);
            if (sharedInst == null) {
                sharedInst = new TimerQueue();
                SwingUtilities.appContextPut(sharedInstanceKey, sharedInst);
            }
            return sharedInst;
        }
    }

    void startIfNeeded() {
        if (!this.running) {
            this.runningLock.lock();
            if (this.running) {
                return;
            }
            try {
                ThreadGroup threadGroup = AppContext.getAppContext().getThreadGroup();
                AccessController.doPrivileged(() -> {
                    String name = "TimerQueue";
                    Thread timerThread = new Thread(threadGroup, this, name, 0L, false);
                    timerThread.setDaemon(true);
                    timerThread.setPriority(5);
                    timerThread.start();
                    return null;
                });
                this.running = true;
            }
            finally {
                this.runningLock.unlock();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void addTimer(Timer timer, long delayMillis) {
        timer.getLock().lock();
        try {
            if (!this.containsTimer(timer)) {
                this.addTimer(new DelayedTimer(timer, TimeUnit.MILLISECONDS.toNanos(delayMillis) + TimerQueue.now()));
            }
        }
        finally {
            timer.getLock().unlock();
        }
    }

    private void addTimer(DelayedTimer delayedTimer) {
        assert (delayedTimer != null && !this.containsTimer(delayedTimer.getTimer()));
        Timer timer = delayedTimer.getTimer();
        timer.getLock().lock();
        try {
            timer.delayedTimer = delayedTimer;
            this.queue.add(delayedTimer);
        }
        finally {
            timer.getLock().unlock();
        }
    }

    void removeTimer(Timer timer) {
        timer.getLock().lock();
        try {
            if (timer.delayedTimer != null) {
                this.queue.remove(timer.delayedTimer);
                timer.delayedTimer = null;
            }
        }
        finally {
            timer.getLock().unlock();
        }
    }

    boolean containsTimer(Timer timer) {
        timer.getLock().lock();
        try {
            boolean bl = timer.delayedTimer != null;
            return bl;
        }
        finally {
            timer.getLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        block13: {
            this.runningLock.lock();
            block9: while (true) {
                while (this.running) {
                    try {
                        DelayedTimer runningTimer = (DelayedTimer)this.queue.take();
                        Timer timer = runningTimer.getTimer();
                        timer.getLock().lock();
                        try {
                            DelayedTimer delayedTimer = timer.delayedTimer;
                            if (delayedTimer == runningTimer) {
                                timer.post();
                                timer.delayedTimer = null;
                                if (timer.isRepeats()) {
                                    delayedTimer.setTime(TimerQueue.now() + TimeUnit.MILLISECONDS.toNanos(timer.getDelay()));
                                    this.addTimer(delayedTimer);
                                }
                            }
                            timer.getLock().newCondition().awaitNanos(1L);
                        }
                        catch (SecurityException securityException) {}
                        continue block9;
                        finally {
                            timer.getLock().unlock();
                            continue block9;
                        }
                    }
                    catch (InterruptedException ie) {
                        if (!AppContext.getAppContext().isDisposed()) continue;
                        break block13;
                    }
                }
                break block13;
                {
                    continue block9;
                    break;
                }
                break;
            }
            finally {
                this.running = false;
                this.runningLock.unlock();
            }
        }
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("TimerQueue (");
        boolean isFirst = true;
        for (DelayedTimer delayedTimer : this.queue) {
            if (!isFirst) {
                buf.append(", ");
            }
            buf.append(delayedTimer.getTimer().toString());
            isFirst = false;
        }
        buf.append(")");
        return buf.toString();
    }

    private static long now() {
        return System.nanoTime() - NANO_ORIGIN;
    }

    static class DelayedTimer
    implements Delayed {
        private static final AtomicLong sequencer = new AtomicLong();
        private final long sequenceNumber;
        private volatile long time;
        private final Timer timer;

        DelayedTimer(Timer timer, long nanos) {
            this.timer = timer;
            this.time = nanos;
            this.sequenceNumber = sequencer.getAndIncrement();
        }

        @Override
        public final long getDelay(TimeUnit unit) {
            return unit.convert(this.time - TimerQueue.now(), TimeUnit.NANOSECONDS);
        }

        final void setTime(long nanos) {
            this.time = nanos;
        }

        final Timer getTimer() {
            return this.timer;
        }

        @Override
        public int compareTo(Delayed other) {
            if (other == this) {
                return 0;
            }
            if (other instanceof DelayedTimer) {
                DelayedTimer x = (DelayedTimer)other;
                long diff = this.time - x.time;
                if (diff < 0L) {
                    return -1;
                }
                if (diff > 0L) {
                    return 1;
                }
                if (this.sequenceNumber < x.sequenceNumber) {
                    return -1;
                }
                return 1;
            }
            long d = this.getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS);
            return d == 0L ? 0 : (d < 0L ? -1 : 1);
        }
    }
}

