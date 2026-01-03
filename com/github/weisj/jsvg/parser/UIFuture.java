/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser;

import javax.swing.JComponent;
import org.jetbrains.annotations.Nullable;

public interface UIFuture<T> {
    public boolean checkIfReady(@Nullable JComponent var1);

    public T get();
}

