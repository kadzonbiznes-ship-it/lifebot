/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.geometry.SVGEllipse
 *  com.github.weisj.jsvg.geometry.util.GeometryUtil
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry;

import com.github.weisj.jsvg.geometry.MeasurableShape;
import com.github.weisj.jsvg.geometry.SVGEllipse;
import com.github.weisj.jsvg.geometry.size.MeasureContext;
import com.github.weisj.jsvg.geometry.util.GeometryUtil;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;

public class AWTSVGShape<T extends Shape>
implements MeasurableShape {
    public static final Rectangle2D EMPTY_SHAPE = new Rectangle();
    @NotNull
    protected final T shape;
    private Rectangle2D bounds;
    private double pathLength;

    public AWTSVGShape(@NotNull T shape) {
        this(shape, Double.NaN);
    }

    private AWTSVGShape(@NotNull T shape, double pathLength) {
        this.shape = shape;
        this.pathLength = pathLength;
    }

    @Override
    @NotNull
    public Shape shape(@NotNull RenderContext context, boolean validate) {
        return this.shape;
    }

    @Override
    @NotNull
    public Rectangle2D bounds(@NotNull RenderContext context, boolean validate) {
        if (this.bounds == null) {
            this.bounds = this.shape.getBounds2D();
        }
        return this.bounds;
    }

    @Override
    public double pathLength(@NotNull MeasureContext measureContext) {
        if (Double.isNaN(this.pathLength)) {
            this.pathLength = this.computePathLength();
        }
        return this.pathLength;
    }

    private double computePathLength() {
        if (this.shape instanceof Rectangle2D) {
            Rectangle2D r = (Rectangle2D)this.shape;
            return 2.0 * (r.getWidth() + r.getHeight());
        }
        if (this.shape instanceof Ellipse2D) {
            double h;
            Ellipse2D e = (Ellipse2D)this.shape;
            double w = e.getWidth();
            if (w == (h = e.getHeight())) {
                return Math.PI * w;
            }
            return SVGEllipse.ellipseCircumference((double)(w / 2.0), (double)(h / 2.0));
        }
        return this.computeGenericPathLength();
    }

    private double computeGenericPathLength() {
        return GeometryUtil.pathLength(this.shape);
    }
}

