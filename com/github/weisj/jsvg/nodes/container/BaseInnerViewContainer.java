/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.MustBeInvokedByOverriders
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes.container;

import com.github.weisj.jsvg.attributes.Overflow;
import com.github.weisj.jsvg.attributes.PreserveAspectRatio;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.MeasureContext;
import com.github.weisj.jsvg.nodes.SVG;
import com.github.weisj.jsvg.nodes.container.CommonRenderableContainerNode;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.renderer.NodeRenderer;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseInnerViewContainer
extends CommonRenderableContainerNode {
    protected ViewBox viewBox;
    protected PreserveAspectRatio preserveAspectRatio;
    private Overflow overflow;

    @NotNull
    protected abstract Point2D outerLocation(@NotNull MeasureContext var1);

    @Nullable
    protected abstract Point2D anchorLocation(@NotNull MeasureContext var1);

    @NotNull
    public abstract FloatSize size(@NotNull RenderContext var1);

    @NotNull
    protected abstract Overflow defaultOverflow();

    @Nullable
    public ViewBox viewBox(@NotNull RenderContext context) {
        return this.viewBox != null ? this.viewBox : new ViewBox(this.size(context));
    }

    @Override
    @MustBeInvokedByOverriders
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.viewBox = attributeNode.getViewBox();
        this.preserveAspectRatio = PreserveAspectRatio.parse(attributeNode.getValue("preserveAspectRatio"), attributeNode.parser());
        this.overflow = attributeNode.getEnum("overflow", this.defaultOverflow());
    }

    @Override
    public final void render(@NotNull RenderContext context, @NotNull Graphics2D g) {
        this.renderWithSize(this.size(context), this.viewBox(context), null, context, g);
    }

    @NotNull
    protected RenderContext createInnerContext(@NotNull RenderContext context, @NotNull ViewBox viewBox) {
        return NodeRenderer.setupInnerViewRenderContext(viewBox, context, true);
    }

    public final void renderWithSize(@NotNull FloatSize useSiteSize, @Nullable ViewBox view, @NotNull RenderContext context, @NotNull Graphics2D g) {
        this.renderWithSize(useSiteSize, view, null, context, g);
    }

    public final void renderWithSize(@NotNull FloatSize useSiteSize, @Nullable ViewBox view, @Nullable PreserveAspectRatio preserveAspectRatio, @NotNull RenderContext context, @NotNull Graphics2D g) {
        boolean shouldClip;
        MeasureContext measureContext = context.measureContext();
        Point2D outerPos = this.outerLocation(measureContext);
        if (Length.isUnspecified(useSiteSize.width) || Length.isUnspecified(useSiteSize.height)) {
            FloatSize size = this.size(context);
            if (Length.isUnspecified(useSiteSize.width)) {
                useSiteSize.width = size.width;
            }
            if (Length.isUnspecified(useSiteSize.height)) {
                useSiteSize.height = size.height;
            }
        }
        if (preserveAspectRatio == null) {
            preserveAspectRatio = this.preserveAspectRatio;
        }
        AffineTransform viewTransform = view != null ? preserveAspectRatio.computeViewPortTransform(useSiteSize, view) : null;
        FloatSize viewSize = view != null ? view.size() : useSiteSize;
        RenderContext innerContext = this.createInnerContext(context, new ViewBox(viewSize));
        MeasureContext innerMeasure = innerContext.measureContext();
        innerContext.translate(g, outerPos);
        Point2D anchorPos = this.anchorLocation(innerMeasure);
        if (anchorPos != null) {
            if (viewTransform != null) {
                anchorPos.setLocation(anchorPos.getX() * viewTransform.getScaleX() - viewTransform.getTranslateX(), anchorPos.getY() * viewTransform.getScaleY() - viewTransform.getTranslateY());
            }
            innerContext.translate(g, anchorPos);
        }
        if (shouldClip = this.overflow.establishesClip()) {
            g.clip(new ViewBox(useSiteSize));
        }
        if (viewTransform != null) {
            innerContext.transform(g, viewTransform);
            if (shouldClip) {
                g.clip(view);
            }
        }
        if (this instanceof SVG && ((SVG)this).isTopLevel()) {
            context.setRootTransform(g.getTransform());
            innerContext.setRootTransform(g.getTransform());
        }
        super.render(innerContext, g);
    }
}

