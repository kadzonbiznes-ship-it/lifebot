/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import sun.nio.ch.NativeThread;

class NativeThreadSet {
    private long[] elts;
    private int used = 0;
    private boolean waitingToEmpty;

    NativeThreadSet(int n) {
        this.elts = new long[n];
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    int add() {
        long th = NativeThread.currentNativeThread();
        if (th == 0L) {
            th = -1L;
        }
        NativeThreadSet nativeThreadSet = this;
        synchronized (nativeThreadSet) {
            int start = 0;
            if (this.used >= this.elts.length) {
                int on = this.elts.length;
                int nn = on * 2;
                long[] nelts = new long[nn];
                System.arraycopy(this.elts, 0, nelts, 0, on);
                this.elts = nelts;
                start = on;
            }
            for (int i = start; i < this.elts.length; ++i) {
                if (this.elts[i] != 0L) continue;
                this.elts[i] = th;
                ++this.used;
                return i;
            }
            assert (false);
            return -1;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void remove(int i) {
        NativeThreadSet nativeThreadSet = this;
        synchronized (nativeThreadSet) {
            assert (this.elts[i] == NativeThread.currentNativeThread() || this.elts[i] == -1L);
            this.elts[i] = 0L;
            --this.used;
            if (this.used == 0 && this.waitingToEmpty) {
                this.notifyAll();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    synchronized void signalAndWait() {
        boolean interrupted = false;
        while (this.used > 0) {
            int u = this.used;
            for (long th : this.elts) {
                if (th == 0L) continue;
                if (th != -1L) {
                    NativeThread.signal(th);
                }
                if (--u == 0) break;
            }
            this.waitingToEmpty = true;
            try {
                this.wait(50L);
            }
            catch (InterruptedException e) {
                interrupted = true;
            }
            finally {
                this.waitingToEmpty = false;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}

