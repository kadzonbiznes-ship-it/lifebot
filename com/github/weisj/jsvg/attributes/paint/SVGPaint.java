/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.attributes.paint;

import com.github.weisj.jsvg.attributes.paint.AwtSVGPaint;
import com.github.weisj.jsvg.attributes.paint.PaintParser;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SVGPaint {
    public static final AwtSVGPaint DEFAULT_PAINT = new AwtSVGPaint(PaintParser.DEFAULT_COLOR);
    public static final SVGPaint NONE = new SVGPaint(){

        @Override
        public void fillShape(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
        }

        @Override
        public void drawShape(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
        }

        public String toString() {
            return "SVGPaint.None";
        }
    };
    public static final SVGPaint CURRENT_COLOR = new SVGPaint(){

        @Override
        public void fillShape(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
            throw new IllegalStateException("Sentinel color CURRENT_COLOR shouldn't be used for painting directly");
        }

        @Override
        public void drawShape(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
            throw new IllegalStateException("Sentinel color CURRENT_COLOR shouldn't be used for painting directly");
        }

        public String toString() {
            return "SVGPaint.CurrentColor";
        }
    };
    public static final SVGPaint CONTEXT_FILL = new SVGPaint(){

        @Override
        public void fillShape(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
            throw new IllegalStateException("Sentinel color CONTEXT_FILL shouldn't be used for painting directly");
        }

        @Override
        public void drawShape(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
            throw new IllegalStateException("Sentinel color CONTEXT_FILL shouldn't be used for painting directly");
        }

        public String toString() {
            return "SVGPaint.ContextFill";
        }
    };
    public static final SVGPaint CONTEXT_STROKE = new SVGPaint(){

        @Override
        public void fillShape(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
            throw new IllegalStateException("Sentinel color CONTEXT_STROKE shouldn't be used for painting directly");
        }

        @Override
        public void drawShape(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
            throw new IllegalStateException("Sentinel color CONTEXT_STROKE shouldn't be used for painting directly");
        }

        public String toString() {
            return "SVGPaint.ContextStroke";
        }
    };

    public void fillShape(@NotNull Graphics2D var1, @NotNull RenderContext var2, @NotNull Shape var3, @Nullable Rectangle2D var4);

    public void drawShape(@NotNull Graphics2D var1, @NotNull RenderContext var2, @NotNull Shape var3, @Nullable Rectangle2D var4);

    default public boolean isVisible() {
        return this != NONE;
    }
}

