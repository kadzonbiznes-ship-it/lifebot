/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes.prototype;

import com.github.weisj.jsvg.geometry.size.MeasureContext;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Transformable {
    default public boolean shouldTransform() {
        return true;
    }

    @Nullable
    public AffineTransform transform();

    @NotNull
    public Point2D transformOrigin(@NotNull MeasureContext var1);

    default public void applyTransform(@NotNull Graphics2D g, @NotNull RenderContext context) {
        AffineTransform transform = this.transform();
        if (transform != null) {
            Point2D transformOrigin = this.transformOrigin(context.measureContext());
            AffineTransform conjugate = AffineTransform.getTranslateInstance(transformOrigin.getX(), transformOrigin.getY());
            conjugate.concatenate(transform);
            conjugate.translate(-transformOrigin.getX(), -transformOrigin.getY());
            g.transform(conjugate);
            context.userSpaceTransform().concatenate(conjugate);
        }
    }

    default public Shape transformShape(@NotNull Shape shape, @NotNull MeasureContext measureContext) {
        AffineTransform transform = this.transform();
        if (transform != null) {
            Point2D transformOrigin = this.transformOrigin(measureContext);
            AffineTransform at = new AffineTransform();
            at.translate(transformOrigin.getX(), transformOrigin.getY());
            at.concatenate(transform);
            at.translate(-transformOrigin.getX(), -transformOrigin.getY());
            return at.createTransformedShape(shape);
        }
        return shape;
    }
}

