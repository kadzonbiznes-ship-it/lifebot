/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.MediaTracker;

abstract class MediaEntry {
    MediaTracker tracker;
    int ID;
    MediaEntry next;
    int status;
    boolean cancelled;
    static final int LOADING = 1;
    static final int ABORTED = 2;
    static final int ERRORED = 4;
    static final int COMPLETE = 8;
    static final int LOADSTARTED = 13;
    static final int DONE = 14;

    MediaEntry(MediaTracker mt, int id) {
        this.tracker = mt;
        this.ID = id;
    }

    abstract Object getMedia();

    static MediaEntry insert(MediaEntry head, MediaEntry me) {
        MediaEntry cur = head;
        MediaEntry prev = null;
        while (cur != null && cur.ID <= me.ID) {
            prev = cur;
            cur = cur.next;
        }
        me.next = cur;
        if (prev == null) {
            head = me;
        } else {
            prev.next = me;
        }
        return head;
    }

    int getID() {
        return this.ID;
    }

    abstract void startLoad();

    void cancel() {
        this.cancelled = true;
    }

    synchronized int getStatus(boolean doLoad, boolean doVerify) {
        if (doLoad && (this.status & 0xD) == 0) {
            this.status = this.status & 0xFFFFFFFD | 1;
            this.startLoad();
        }
        return this.status;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void setStatus(int flag) {
        MediaEntry mediaEntry = this;
        synchronized (mediaEntry) {
            this.status = flag;
        }
        this.tracker.setDone();
    }
}

