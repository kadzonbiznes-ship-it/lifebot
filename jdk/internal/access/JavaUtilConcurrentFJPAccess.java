/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.access;

import java.util.concurrent.ForkJoinPool;

public interface JavaUtilConcurrentFJPAccess {
    public long beginCompensatedBlock(ForkJoinPool var1);

    public void endCompensatedBlock(ForkJoinPool var1, long var2);
}

