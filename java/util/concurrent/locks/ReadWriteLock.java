/*
 * Decompiled with CFR 0.152.
 */
package java.util.concurrent.locks;

import java.util.concurrent.locks.Lock;

public interface ReadWriteLock {
    public Lock readLock();

    public Lock writeLock();
}

