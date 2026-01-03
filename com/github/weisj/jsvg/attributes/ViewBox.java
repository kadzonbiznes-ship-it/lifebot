/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.attributes;

import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.geometry.size.Length;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;

public final class ViewBox
extends Rectangle2D.Float {
    public ViewBox(float[] viewBox) {
        super(viewBox[0], viewBox[1], viewBox[2], viewBox[3]);
    }

    public ViewBox(float x, float y, float w, float h) {
        super(x, y, w, h);
    }

    public ViewBox(float w, float h) {
        this(0.0f, 0.0f, w, h);
    }

    public ViewBox(FloatSize size) {
        this(size.width, size.height);
    }

    public ViewBox(@NotNull Rectangle2D bounds) {
        this((float)bounds.getX(), (float)bounds.getY(), (float)bounds.getWidth(), (float)bounds.getHeight());
    }

    public void setSize(@NotNull FloatSize size) {
        this.width = size.width;
        this.height = size.height;
    }

    @NotNull
    public FloatSize size() {
        return new FloatSize(this.width, this.height);
    }

    @NotNull
    public Point2D.Float location() {
        return new Point2D.Float(this.x, this.y);
    }

    public boolean hasSpecifiedX() {
        return Length.isSpecified(this.x);
    }

    public boolean hasSpecifiedY() {
        return Length.isSpecified(this.y);
    }

    public boolean hasSpecifiedWidth() {
        return Length.isSpecified(this.width);
    }

    public boolean hasSpecifiedHeight() {
        return Length.isSpecified(this.height);
    }

    @Override
    public String toString() {
        return "ViewBox[" + this.x + "," + this.y + "," + this.width + "," + this.height + "]";
    }

    public float normedDiagonalLength() {
        return (float)Math.sqrt((this.width * this.width + this.height * this.height) / 2.0f);
    }
}

