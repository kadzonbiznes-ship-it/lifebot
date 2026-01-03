/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.prototype;

import com.github.weisj.jsvg.nodes.prototype.Transformable;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;

public interface HasShape {
    @NotNull
    default public Shape elementShape(@NotNull RenderContext context) {
        Shape shape = this.untransformedElementShape(context);
        if (this instanceof Transformable) {
            return ((Transformable)((Object)this)).transformShape(shape, context.measureContext());
        }
        return shape;
    }

    @NotNull
    public Shape untransformedElementShape(@NotNull RenderContext var1);

    @NotNull
    default public Rectangle2D elementBounds(@NotNull RenderContext context) {
        Rectangle2D shape = this.untransformedElementBounds(context);
        if (this instanceof Transformable) {
            return ((Transformable)((Object)this)).transformShape(shape, context.measureContext()).getBounds2D();
        }
        return shape;
    }

    @NotNull
    public Rectangle2D untransformedElementBounds(@NotNull RenderContext var1);
}

