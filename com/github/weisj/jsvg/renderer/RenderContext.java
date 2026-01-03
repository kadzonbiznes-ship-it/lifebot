/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.attributes.font.FontResolver
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.renderer;

import com.github.weisj.jsvg.attributes.FillRule;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.attributes.font.FontResolver;
import com.github.weisj.jsvg.attributes.font.MeasurableFontSpec;
import com.github.weisj.jsvg.attributes.font.SVGFont;
import com.github.weisj.jsvg.attributes.paint.SVGPaint;
import com.github.weisj.jsvg.attributes.stroke.StrokeResolver;
import com.github.weisj.jsvg.geometry.size.MeasureContext;
import com.github.weisj.jsvg.nodes.prototype.Mutator;
import com.github.weisj.jsvg.renderer.ContextElementAttributes;
import com.github.weisj.jsvg.renderer.FontRenderContext;
import com.github.weisj.jsvg.renderer.PaintContext;
import com.github.weisj.jsvg.renderer.StrokeContext;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.ImageProducer;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RenderContext {
    @Nullable
    private final JComponent targetComponent;
    @NotNull
    private final MeasureContext measureContext;
    @NotNull
    private final PaintContext paintContext;
    @NotNull
    private final FontRenderContext fontRenderContext;
    @NotNull
    private final MeasurableFontSpec fontSpec;
    @NotNull
    private final FillRule fillRule;
    @Nullable
    private final ContextElementAttributes contextElementAttributes;
    @NotNull
    private final AffineTransform rootTransform;
    @NotNull
    private final AffineTransform userSpaceTransform;

    @NotNull
    public static RenderContext createInitial(@Nullable JComponent targetComponent, @NotNull MeasureContext measureContext) {
        return new RenderContext(targetComponent, new AffineTransform(), new AffineTransform(), PaintContext.createDefault(), measureContext, FontRenderContext.createDefault(), MeasurableFontSpec.createDefault(), FillRule.Nonzero, null);
    }

    RenderContext(@Nullable JComponent targetComponent, @NotNull AffineTransform rootTransform, @NotNull AffineTransform userSpaceTransform, @NotNull PaintContext paintContext, @NotNull MeasureContext measureContext, @NotNull FontRenderContext fontRenderContext, @NotNull MeasurableFontSpec fontSpec, @NotNull FillRule fillRule, @Nullable ContextElementAttributes contextElementAttributes) {
        this.targetComponent = targetComponent;
        this.rootTransform = rootTransform;
        this.userSpaceTransform = userSpaceTransform;
        this.paintContext = paintContext;
        this.measureContext = measureContext;
        this.fontRenderContext = fontRenderContext;
        this.fontSpec = fontSpec;
        this.fillRule = fillRule;
        this.contextElementAttributes = contextElementAttributes;
    }

    @NotNull
    RenderContext derive(@Nullable Mutator<PaintContext> context, @Nullable Mutator<MeasurableFontSpec> attributeFontSpec, @Nullable ViewBox viewBox, @Nullable FontRenderContext frc, @Nullable FillRule fillRule, @Nullable ContextElementAttributes contextAttributes) {
        FillRule newFillRule;
        if (context == null && viewBox == null && attributeFontSpec == null && frc == null) {
            return this;
        }
        PaintContext newPaintContext = this.paintContext;
        MeasurableFontSpec newFontSpec = this.fontSpec;
        FillRule fillRule2 = newFillRule = fillRule != null && fillRule != FillRule.Inherit ? fillRule : this.fillRule;
        if (context != null) {
            newPaintContext = context.mutate(this.paintContext);
        }
        if (attributeFontSpec != null) {
            newFontSpec = attributeFontSpec.mutate(newFontSpec);
        }
        ContextElementAttributes newContextAttributes = this.contextElementAttributes;
        if (contextAttributes != null) {
            newContextAttributes = contextAttributes;
        }
        float em = newFontSpec.effectiveSize(this.measureContext);
        float ex = SVGFont.exFromEm(em);
        MeasureContext newMeasureContext = this.measureContext.derive(viewBox, em, ex);
        FontRenderContext effectiveFrc = this.fontRenderContext.derive(frc);
        return new RenderContext(this.targetComponent, this.rootTransform, new AffineTransform(this.userSpaceTransform), newPaintContext, newMeasureContext, effectiveFrc, newFontSpec, newFillRule, newContextAttributes);
    }

    @NotNull
    public RenderContext deriveForChildGraphics() {
        return this.derive(t -> t, null, null, null, null, null);
    }

    @NotNull
    public StrokeContext strokeContext() {
        assert (this.paintContext.strokeContext != null);
        return this.paintContext.strokeContext;
    }

    @Nullable
    ContextElementAttributes contextElementAttributes() {
        return this.contextElementAttributes;
    }

    @NotNull
    public AffineTransform rootTransform() {
        return this.rootTransform;
    }

    @NotNull
    public AffineTransform userSpaceTransform() {
        return this.userSpaceTransform;
    }

    public void setRootTransform(@NotNull AffineTransform rootTransform) {
        this.rootTransform.setTransform(rootTransform);
        this.userSpaceTransform.setToIdentity();
    }

    public void setRootTransform(@NotNull AffineTransform rootTransform, @NotNull AffineTransform userSpaceTransform) {
        this.rootTransform.setTransform(rootTransform);
        this.userSpaceTransform.setTransform(userSpaceTransform);
    }

    public void translate(@NotNull Graphics2D g, @NotNull Point2D dp) {
        this.translate(g, dp.getX(), dp.getY());
    }

    public void translate(@NotNull Graphics2D g, double dx, double dy) {
        g.translate(dx, dy);
        this.userSpaceTransform.translate(dx, dy);
    }

    public void scale(@NotNull Graphics2D g, double sx, double sy) {
        g.scale(sx, sy);
        this.userSpaceTransform.scale(sx, sy);
    }

    public void rotate(@NotNull Graphics2D g, double angle) {
        g.rotate(angle);
        this.userSpaceTransform.rotate(angle);
    }

    public void transform(@NotNull Graphics2D g, @NotNull AffineTransform at) {
        g.transform(at);
        this.userSpaceTransform.concatenate(at);
    }

    @Nullable
    public JComponent targetComponent() {
        return this.targetComponent;
    }

    @NotNull
    public MeasureContext measureContext() {
        return this.measureContext;
    }

    @NotNull
    public FontRenderContext fontRenderContext() {
        return this.fontRenderContext;
    }

    @NotNull
    public FillRule fillRule() {
        return this.fillRule;
    }

    @NotNull
    public SVGPaint strokePaint() {
        return this.resolvePaint(this.paintContext.strokePaint);
    }

    @NotNull
    public SVGPaint fillPaint() {
        return this.resolvePaint(this.paintContext.fillPaint);
    }

    @NotNull
    private SVGPaint resolvePaint(@Nullable SVGPaint p) {
        if (p == SVGPaint.DEFAULT_PAINT || p == SVGPaint.CURRENT_COLOR) {
            return this.coerceNonNull(this.paintContext.color);
        }
        if (p == SVGPaint.CONTEXT_STROKE) {
            if (this.contextElementAttributes == null) {
                return SVGPaint.NONE;
            }
            return this.contextElementAttributes.strokePaint;
        }
        if (p == SVGPaint.CONTEXT_FILL) {
            if (this.contextElementAttributes == null) {
                return SVGPaint.NONE;
            }
            return this.contextElementAttributes.fillPaint;
        }
        return this.coerceNonNull(p);
    }

    @NotNull
    private SVGPaint coerceNonNull(@Nullable SVGPaint p) {
        return p != null ? p : SVGPaint.DEFAULT_PAINT;
    }

    public float rawOpacity() {
        return this.paintContext.opacity;
    }

    public float fillOpacity() {
        return this.paintContext.fillOpacity * this.paintContext.opacity;
    }

    public float strokeOpacity() {
        return this.paintContext.strokeOpacity * this.paintContext.opacity;
    }

    @NotNull
    public Stroke stroke(float pathLengthFactor) {
        return StrokeResolver.resolve(pathLengthFactor, this.measureContext, this.strokeContext());
    }

    @NotNull
    public SVGFont font() {
        return FontResolver.resolve((MeasurableFontSpec)this.fontSpec, (MeasureContext)this.measureContext);
    }

    public String toString() {
        return "RenderContext{\n  measureContext=" + this.measureContext + ",\n paintContext=" + this.paintContext + ",\n fontSpec=" + this.fontSpec + ",\n targetComponent=" + this.targetComponent + ",\n contextElementAttributes=" + this.contextElementAttributes + ",\n fillRule=" + (Object)((Object)this.fillRule) + ",\n baseTransform=" + this.rootTransform + ",\n userSpaceTransform=" + this.userSpaceTransform + "\n}";
    }

    @NotNull
    public Image createImage(@NotNull ImageProducer imageProducer) {
        if (this.targetComponent != null) {
            return this.targetComponent.createImage(imageProducer);
        }
        return Toolkit.getDefaultToolkit().createImage(imageProducer);
    }
}

