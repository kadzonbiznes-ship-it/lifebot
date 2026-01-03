/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.path;

import java.awt.geom.Point2D;
import org.jetbrains.annotations.NotNull;

public final class BuildHistory {
    @NotNull
    final Point2D.Float startPoint = new Point2D.Float();
    @NotNull
    final Point2D.Float lastPoint = new Point2D.Float();
    @NotNull
    final Point2D.Float lastKnot = new Point2D.Float();

    public void setStartPoint(@NotNull Point2D point) {
        this.startPoint.setLocation(point);
    }

    public void setLastPoint(@NotNull Point2D point) {
        this.lastPoint.setLocation(point);
    }

    public void setLastKnot(float x, float y) {
        this.lastKnot.setLocation(x, y);
    }

    public void setLastKnot(@NotNull Point2D point) {
        this.lastKnot.setLocation(point);
    }
}

