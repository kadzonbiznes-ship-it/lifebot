/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.nodes.filter.Filter$FilterInfo
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.renderer;

import com.github.weisj.jsvg.attributes.FillRule;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.attributes.font.MeasurableFontSpec;
import com.github.weisj.jsvg.geometry.size.MeasureContext;
import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Mask;
import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.filter.Filter;
import com.github.weisj.jsvg.nodes.prototype.HasClip;
import com.github.weisj.jsvg.nodes.prototype.HasFillRule;
import com.github.weisj.jsvg.nodes.prototype.HasFilter;
import com.github.weisj.jsvg.nodes.prototype.HasFontContext;
import com.github.weisj.jsvg.nodes.prototype.HasFontRenderContext;
import com.github.weisj.jsvg.nodes.prototype.HasPaintContext;
import com.github.weisj.jsvg.nodes.prototype.HasShape;
import com.github.weisj.jsvg.nodes.prototype.Instantiator;
import com.github.weisj.jsvg.nodes.prototype.Mutator;
import com.github.weisj.jsvg.nodes.prototype.Renderable;
import com.github.weisj.jsvg.nodes.prototype.Transformable;
import com.github.weisj.jsvg.renderer.ContextElementAttributes;
import com.github.weisj.jsvg.renderer.FontRenderContext;
import com.github.weisj.jsvg.renderer.GraphicsUtil;
import com.github.weisj.jsvg.renderer.PaintContext;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NodeRenderer {
    private static final boolean CLIP_DEBUG = false;

    private NodeRenderer() {
    }

    public static void renderNode(@NotNull SVGNode node, @NotNull RenderContext context, @NotNull Graphics2D g) {
        try (Info info = NodeRenderer.createRenderInfo(node, context, g, null);){
            if (info != null) {
                info.renderable.render(info.context, info.graphics());
            }
        }
    }

    @NotNull
    public static RenderContext createChildContext(@NotNull SVGNode node, @NotNull RenderContext context, @Nullable Instantiator instantiator) {
        return NodeRenderer.setupRenderContext(instantiator, node, context);
    }

    @Nullable
    public static Info createRenderInfo(@NotNull SVGNode node, @NotNull RenderContext context, @NotNull Graphics2D g, @Nullable Instantiator instantiator) {
        Filter filter;
        if (!(node instanceof Renderable)) {
            return null;
        }
        Renderable renderable = (Renderable)((Object)node);
        boolean instantiated = renderable.requiresInstantiation();
        if (instantiated && (instantiator == null || !instantiator.canInstantiate(node))) {
            return null;
        }
        if (!renderable.isVisible(context)) {
            return null;
        }
        RenderContext childContext = NodeRenderer.createChildContext(node, context, instantiator);
        Graphics2D childGraphics = (Graphics2D)g.create();
        if (renderable instanceof Transformable && ((Transformable)((Object)renderable)).shouldTransform()) {
            ((Transformable)((Object)renderable)).applyTransform(childGraphics, childContext);
        }
        Rectangle2D elementBounds = null;
        if (renderable instanceof HasClip) {
            ClipPath childClip;
            Mask mask = ((HasClip)((Object)renderable)).mask();
            if (mask != null && !(elementBounds = NodeRenderer.elementBounds(renderable, childContext)).isEmpty()) {
                GraphicsUtil.safelySetPaint(childGraphics, mask.createMaskPaint(g, childContext, elementBounds));
            }
            if ((childClip = ((HasClip)((Object)renderable)).clipPath()) != null) {
                if (!childClip.isValid()) {
                    return null;
                }
                if (elementBounds == null) {
                    elementBounds = NodeRenderer.elementBounds(renderable, childContext);
                }
                Shape childClipShape = childClip.clipShape(childContext, elementBounds);
                childGraphics.clip(childClipShape);
            }
        }
        Filter filter2 = filter = renderable instanceof HasFilter ? ((HasFilter)((Object)renderable)).filter() : null;
        if (filter != null && filter.hasEffect()) {
            if (elementBounds == null) {
                elementBounds = NodeRenderer.elementBounds(renderable, childContext);
            }
            return new InfoWithFilter(renderable, childContext, childGraphics, filter, elementBounds);
        }
        return new Info(renderable, childContext, childGraphics);
    }

    @NotNull
    private static Rectangle2D elementBounds(@NotNull Object node, @NotNull RenderContext childContext) {
        Rectangle2D elementBounds;
        if (node instanceof HasShape) {
            elementBounds = ((HasShape)node).untransformedElementBounds(childContext);
        } else {
            MeasureContext measureContext = childContext.measureContext();
            elementBounds = new ViewBox(measureContext.viewWidth(), measureContext.viewHeight());
        }
        return elementBounds;
    }

    @NotNull
    public static RenderContext setupRenderContext(@NotNull Object node, @NotNull RenderContext context) {
        return NodeRenderer.setupRenderContext(null, node, context);
    }

    @NotNull
    private static RenderContext setupRenderContext(@Nullable Instantiator instantiator, @NotNull Object node, @NotNull RenderContext context) {
        @Nullable Mutator<PaintContext> paintContext = null;
        Mutator<MeasurableFontSpec> fontSpec = null;
        FontRenderContext fontRenderContext = null;
        FillRule fillRule = null;
        if (node instanceof HasPaintContext) {
            paintContext = ((HasPaintContext)node).paintContext();
        }
        if (node instanceof HasFontContext) {
            fontSpec = ((HasFontContext)node).fontSpec();
        }
        if (node instanceof HasFontRenderContext) {
            fontRenderContext = ((HasFontRenderContext)node).fontRenderContext();
        }
        if (node instanceof HasFillRule) {
            fillRule = ((HasFillRule)node).fillRule();
        }
        ContextElementAttributes contextElementAttributes = null;
        if (instantiator != null) {
            contextElementAttributes = instantiator.createContextAttributes(context);
        }
        return context.derive(paintContext, fontSpec, null, fontRenderContext, fillRule, contextElementAttributes);
    }

    @NotNull
    public static RenderContext setupInnerViewRenderContext(@NotNull ViewBox viewBox, @NotNull RenderContext context, boolean inheritAttributes) {
        if (inheritAttributes) {
            return context.derive(null, null, viewBox, null, null, null);
        }
        MeasureContext newMeasure = context.measureContext().derive(viewBox, Float.NaN, Float.NaN);
        return new RenderContext(context.targetComponent(), new AffineTransform(), new AffineTransform(), PaintContext.createDefault(), newMeasure, FontRenderContext.createDefault(), MeasurableFontSpec.createDefault(), context.fillRule(), context.contextElementAttributes());
    }

    public static class Info
    implements AutoCloseable {
        @NotNull
        public final Renderable renderable;
        @NotNull
        public final RenderContext context;
        @NotNull
        public final Graphics2D g;

        Info(@NotNull Renderable renderable, @NotNull RenderContext context, @NotNull Graphics2D g) {
            this.renderable = renderable;
            this.context = context;
            this.g = g;
        }

        @NotNull
        public Graphics2D graphics() {
            return this.g;
        }

        @Override
        public void close() {
            this.g.dispose();
        }
    }

    private static final class InfoWithFilter
    extends Info {
        @NotNull
        private final Filter filter;
        @NotNull
        private final Filter.FilterInfo filterInfo;

        InfoWithFilter(@NotNull Renderable renderable, @NotNull RenderContext context, @NotNull Graphics2D g, @NotNull Filter filter, @NotNull Rectangle2D elementBounds) {
            super(renderable, context, g);
            this.filter = filter;
            this.filterInfo = filter.createFilterInfo(g, context, elementBounds);
        }

        @Override
        @NotNull
        public Graphics2D graphics() {
            return this.filterInfo.graphics();
        }

        @Override
        public void close() {
            this.filter.applyFilter(this.g, this.context, this.filterInfo);
            this.filterInfo.blitImage(this.g, this.context);
            this.filterInfo.close();
            super.close();
        }
    }
}

