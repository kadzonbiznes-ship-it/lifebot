/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry;

import com.github.weisj.jsvg.geometry.size.MeasureContext;
import org.jetbrains.annotations.NotNull;

public interface MeasurableLength {
    public double pathLength(@NotNull MeasureContext var1);
}

