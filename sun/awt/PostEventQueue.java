/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import sun.awt.AWTAutoShutdown;
import sun.awt.EventQueueItem;
import sun.awt.SunToolkit;

class PostEventQueue {
    private EventQueueItem queueHead = null;
    private EventQueueItem queueTail = null;
    private final EventQueue eventQueue;
    private Thread flushThread = null;

    PostEventQueue(EventQueue eq) {
        this.eventQueue = eq;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void flush() {
        Thread newThread = Thread.currentThread();
        try {
            EventQueueItem tempQueue;
            PostEventQueue postEventQueue = this;
            synchronized (postEventQueue) {
                if (newThread == this.flushThread) {
                    return;
                }
                while (this.flushThread != null) {
                    this.wait();
                }
                if (this.queueHead == null) {
                    return;
                }
                this.flushThread = newThread;
                tempQueue = this.queueHead;
                this.queueTail = null;
                this.queueHead = null;
            }
            try {
                while (tempQueue != null) {
                    this.eventQueue.postEvent(tempQueue.event);
                    tempQueue = tempQueue.next;
                }
            }
            finally {
                postEventQueue = this;
                synchronized (postEventQueue) {
                    this.flushThread = null;
                    this.notifyAll();
                }
            }
        }
        catch (InterruptedException e) {
            newThread.interrupt();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void postEvent(AWTEvent event) {
        EventQueueItem item = new EventQueueItem(event);
        PostEventQueue postEventQueue = this;
        synchronized (postEventQueue) {
            if (this.queueHead == null) {
                this.queueHead = this.queueTail = item;
            } else {
                this.queueTail.next = item;
                this.queueTail = item;
            }
        }
        SunToolkit.wakeupEventQueue(this.eventQueue, event.getSource() == AWTAutoShutdown.getInstance());
    }
}

