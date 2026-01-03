/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.EventListener;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.SwingUtilities;
import javax.swing.TimerQueue;
import javax.swing.event.EventListenerList;

public class Timer
implements Serializable {
    protected EventListenerList listenerList = new EventListenerList();
    private final transient AtomicBoolean notify = new AtomicBoolean(false);
    private volatile int initialDelay;
    private volatile int delay;
    private volatile boolean repeats = true;
    private volatile boolean coalesce = true;
    private final transient Runnable doPostEvent;
    private static volatile boolean logTimers;
    private final transient Lock lock = new ReentrantLock();
    transient TimerQueue.DelayedTimer delayedTimer = null;
    private volatile String actionCommand;
    private volatile transient AccessControlContext acc = AccessController.getContext();

    public Timer(int delay, ActionListener listener) {
        this.delay = delay;
        this.initialDelay = delay;
        this.doPostEvent = new DoPostEvent();
        if (listener != null) {
            this.addActionListener(listener);
        }
    }

    final AccessControlContext getAccessControlContext() {
        if (this.acc == null) {
            throw new SecurityException("Timer is missing AccessControlContext");
        }
        return this.acc;
    }

    public void addActionListener(ActionListener listener) {
        this.listenerList.add(ActionListener.class, listener);
    }

    public void removeActionListener(ActionListener listener) {
        this.listenerList.remove(ActionListener.class, listener);
    }

    public ActionListener[] getActionListeners() {
        return (ActionListener[])this.listenerList.getListeners(ActionListener.class);
    }

    protected void fireActionPerformed(ActionEvent e) {
        Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] != ActionListener.class) continue;
            ((ActionListener)listeners[i + 1]).actionPerformed(e);
        }
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        return this.listenerList.getListeners(listenerType);
    }

    private TimerQueue timerQueue() {
        return TimerQueue.sharedInstance();
    }

    public static void setLogTimers(boolean flag) {
        logTimers = flag;
    }

    public static boolean getLogTimers() {
        return logTimers;
    }

    public void setDelay(int delay) {
        Timer.checkDelay(delay, "Invalid delay: ");
        this.delay = delay;
    }

    private static void checkDelay(int delay, String message) {
        if (delay < 0) {
            throw new IllegalArgumentException(message + delay);
        }
    }

    public int getDelay() {
        return this.delay;
    }

    public void setInitialDelay(int initialDelay) {
        Timer.checkDelay(initialDelay, "Invalid initial delay: ");
        this.initialDelay = initialDelay;
    }

    public int getInitialDelay() {
        return this.initialDelay;
    }

    public void setRepeats(boolean flag) {
        this.repeats = flag;
    }

    public boolean isRepeats() {
        return this.repeats;
    }

    public void setCoalesce(boolean flag) {
        boolean old = this.coalesce;
        this.coalesce = flag;
        if (!old && this.coalesce) {
            this.cancelEvent();
        }
    }

    public boolean isCoalesce() {
        return this.coalesce;
    }

    public void setActionCommand(String command) {
        this.actionCommand = command;
    }

    public String getActionCommand() {
        return this.actionCommand;
    }

    public void start() {
        this.timerQueue().addTimer(this, this.getInitialDelay());
    }

    public boolean isRunning() {
        return this.timerQueue().containsTimer(this);
    }

    public void stop() {
        this.getLock().lock();
        try {
            this.cancelEvent();
            this.timerQueue().removeTimer(this);
        }
        finally {
            this.getLock().unlock();
        }
    }

    public void restart() {
        this.getLock().lock();
        try {
            this.stop();
            this.start();
        }
        finally {
            this.getLock().unlock();
        }
    }

    void cancelEvent() {
        this.notify.set(false);
    }

    void post() {
        if (this.notify.compareAndSet(false, true) || !this.coalesce) {
            AccessController.doPrivileged(new PrivilegedAction<Void>(){

                @Override
                public Void run() {
                    SwingUtilities.invokeLater(Timer.this.doPostEvent);
                    return null;
                }
            }, this.getAccessControlContext());
        }
    }

    Lock getLock() {
        return this.lock;
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        this.acc = AccessController.getContext();
        ObjectInputStream.GetField f = in.readFields();
        EventListenerList newListenerList = (EventListenerList)f.get("listenerList", null);
        if (newListenerList == null) {
            throw new InvalidObjectException("Null listenerList");
        }
        this.listenerList = newListenerList;
        int newInitialDelay = f.get("initialDelay", 0);
        Timer.checkDelay(newInitialDelay, "Invalid initial delay: ");
        this.initialDelay = newInitialDelay;
        int newDelay = f.get("delay", 0);
        Timer.checkDelay(newDelay, "Invalid delay: ");
        this.delay = newDelay;
        this.repeats = f.get("repeats", false);
        this.coalesce = f.get("coalesce", false);
        this.actionCommand = (String)f.get("actionCommand", null);
    }

    private Object readResolve() {
        Timer timer = new Timer(this.getDelay(), null);
        timer.listenerList = this.listenerList;
        timer.initialDelay = this.initialDelay;
        timer.delay = this.delay;
        timer.repeats = this.repeats;
        timer.coalesce = this.coalesce;
        timer.actionCommand = this.actionCommand;
        return timer;
    }

    class DoPostEvent
    implements Runnable {
        DoPostEvent() {
        }

        @Override
        public void run() {
            if (logTimers) {
                System.out.println("Timer ringing: " + String.valueOf(Timer.this));
            }
            if (Timer.this.notify.get()) {
                Timer.this.fireActionPerformed(new ActionEvent(Timer.this, 0, Timer.this.getActionCommand(), System.currentTimeMillis(), 0));
                if (Timer.this.coalesce) {
                    Timer.this.cancelEvent();
                }
            }
        }

        Timer getTimer() {
            return Timer.this;
        }
    }
}

