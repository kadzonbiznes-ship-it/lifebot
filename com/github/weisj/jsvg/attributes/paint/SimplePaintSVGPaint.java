/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.attributes.paint;

import com.github.weisj.jsvg.attributes.paint.SVGPaint;
import com.github.weisj.jsvg.renderer.GraphicsUtil;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SimplePaintSVGPaint
extends SVGPaint {
    @NotNull
    public Paint paint();

    @Override
    default public void fillShape(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
        GraphicsUtil.safelySetPaint(g, this.paint());
        g.fill(shape);
    }

    @Override
    default public void drawShape(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
        GraphicsUtil.safelySetPaint(g, this.paint());
        g.draw(shape);
    }
}

