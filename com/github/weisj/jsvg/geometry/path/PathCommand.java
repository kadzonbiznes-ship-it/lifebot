/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.path;

import com.github.weisj.jsvg.geometry.path.BuildHistory;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import org.jetbrains.annotations.NotNull;

public abstract class PathCommand {
    private final boolean isRelative;
    private final int nodeCount;

    protected PathCommand(int nodeCount) {
        this(false, nodeCount);
    }

    protected PathCommand(boolean isRelative, int nodeCount) {
        this.isRelative = isRelative;
        this.nodeCount = nodeCount;
    }

    protected Point2D.Float offset(@NotNull BuildHistory hist) {
        if (this.isRelative()) {
            return new Point2D.Float(hist.lastPoint.x, hist.lastPoint.y);
        }
        return new Point2D.Float(0.0f, 0.0f);
    }

    protected Point2D.Float lastKnotReflection(@NotNull BuildHistory hist) {
        float oldKx = hist.lastKnot.x;
        float oldKy = hist.lastKnot.y;
        float oldX = hist.lastPoint.x;
        float oldY = hist.lastPoint.y;
        float kx = oldX * 2.0f - oldKx;
        float ky = oldY * 2.0f - oldKy;
        return new Point2D.Float(kx, ky);
    }

    public boolean isRelative() {
        return this.isRelative;
    }

    public abstract void appendPath(@NotNull Path2D var1, @NotNull BuildHistory var2);

    public int nodeCount() {
        return this.nodeCount;
    }
}

