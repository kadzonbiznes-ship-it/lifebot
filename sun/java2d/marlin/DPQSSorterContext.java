/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

final class DPQSSorterContext {
    static final boolean LOG_ALLOC = false;
    static final boolean CHECK_ALLOC = false;
    static final int MAX_RUN_CAPACITY = 5120;
    final int[] run = new int[5120];
    int[] auxA;
    int[] auxB;
    boolean runInit;

    DPQSSorterContext() {
    }

    void initBuffers(int length, int[] a, int[] b) {
        this.auxA = a;
        this.auxB = b;
        this.runInit = true;
    }
}

