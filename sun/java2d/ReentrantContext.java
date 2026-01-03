/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d;

import java.lang.ref.Reference;

public class ReentrantContext {
    byte usage = 0;
    Reference<? extends ReentrantContext> reference = null;
}

