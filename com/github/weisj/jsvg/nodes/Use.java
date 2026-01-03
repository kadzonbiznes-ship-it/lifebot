/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.attributes.FillRule;
import com.github.weisj.jsvg.attributes.font.AttributeFontSpec;
import com.github.weisj.jsvg.attributes.font.FontParser;
import com.github.weisj.jsvg.geometry.AWTSVGShape;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.MeasureContext;
import com.github.weisj.jsvg.nodes.RenderableSVGNode;
import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.container.CommonInnerViewContainer;
import com.github.weisj.jsvg.nodes.prototype.HasContext;
import com.github.weisj.jsvg.nodes.prototype.HasShape;
import com.github.weisj.jsvg.nodes.prototype.Instantiator;
import com.github.weisj.jsvg.nodes.prototype.Renderable;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.renderer.FontRenderContext;
import com.github.weisj.jsvg.renderer.NodeRenderer;
import com.github.weisj.jsvg.renderer.PaintContext;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ElementCategories(value={Category.Graphic, Category.GraphicsReferencing, Category.Structural})
@PermittedContent(categories={Category.Animation, Category.Descriptive})
public final class Use
extends RenderableSVGNode
implements HasContext,
HasShape,
Instantiator {
    public static final String TAG = "use";
    private Length x;
    private Length y;
    private Length width;
    private Length height;
    @Nullable
    private SVGNode referencedNode;
    private PaintContext paintContext;
    private FontRenderContext fontRenderContext;
    private AttributeFontSpec fontSpec;
    private FillRule fillRule;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Nullable
    public SVGNode referencedNode() {
        return this.referencedNode;
    }

    @Override
    public boolean isVisible(@NotNull RenderContext context) {
        return super.isVisible(context) && this.referencedNode instanceof Renderable;
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.x = attributeNode.getLength("x", 0.0f);
        this.y = attributeNode.getLength("y", 0.0f);
        this.width = attributeNode.getLength("width", Length.UNSPECIFIED);
        this.height = attributeNode.getLength("height", Length.UNSPECIFIED);
        String href = attributeNode.getValue("href");
        if (href == null) {
            href = attributeNode.getValue("xlink:href");
        }
        this.referencedNode = attributeNode.getElementByHref(SVGNode.class, href);
        this.paintContext = PaintContext.parse(attributeNode);
        this.fontRenderContext = FontRenderContext.parse(attributeNode);
        this.fontSpec = FontParser.parseFontSpec(attributeNode);
        this.fillRule = FillRule.parse(attributeNode);
    }

    @Override
    @NotNull
    public Shape untransformedElementShape(@NotNull RenderContext context) {
        return this.referencedNode instanceof HasShape ? ((HasShape)((Object)this.referencedNode)).elementShape(NodeRenderer.createChildContext(this.referencedNode, context, this)) : AWTSVGShape.EMPTY_SHAPE;
    }

    @Override
    @NotNull
    public Rectangle2D untransformedElementBounds(@NotNull RenderContext context) {
        return this.referencedNode instanceof HasShape ? ((HasShape)((Object)this.referencedNode)).elementBounds(NodeRenderer.createChildContext(this.referencedNode, context, this)) : AWTSVGShape.EMPTY_SHAPE;
    }

    @NotNull
    public PaintContext paintContext() {
        return this.paintContext;
    }

    @Override
    @NotNull
    public FontRenderContext fontRenderContext() {
        return this.fontRenderContext;
    }

    @NotNull
    public AttributeFontSpec fontSpec() {
        return this.fontSpec;
    }

    @Override
    @NotNull
    public FillRule fillRule() {
        return this.fillRule;
    }

    @Override
    public boolean canInstantiate(@NotNull SVGNode node) {
        return node instanceof CommonInnerViewContainer;
    }

    @Override
    public void render(@NotNull RenderContext context, @NotNull Graphics2D g) {
        if (this.referencedNode != null) {
            MeasureContext measureContext = context.measureContext();
            context.translate(g, this.x.resolveWidth(measureContext), this.y.resolveHeight(measureContext));
            try (NodeRenderer.Info info = NodeRenderer.createRenderInfo(this.referencedNode, context, g, this);){
                if (info == null) {
                    return;
                }
                if (this.referencedNode instanceof CommonInnerViewContainer) {
                    FloatSize targetViewBox = new FloatSize(Float.NaN, Float.NaN);
                    if (this.width.isSpecified()) {
                        targetViewBox.width = this.width.resolveWidth(measureContext);
                    }
                    if (this.height.isSpecified()) {
                        targetViewBox.height = this.height.resolveHeight(measureContext);
                    }
                    CommonInnerViewContainer view = (CommonInnerViewContainer)this.referencedNode;
                    view.renderWithSize(targetViewBox, view.viewBox(info.context), info.context, info.graphics());
                } else {
                    info.renderable.render(info.context, info.graphics());
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Use{x=" + this.x + ", y=" + this.y + ", width=" + this.width + ", height=" + this.height + ", referencedNode=" + (this.referencedNode != null ? this.referencedNode.id() : null) + ", styleContext=" + this.paintContext + ", fillRule=" + (Object)((Object)this.fillRule) + '}';
    }
}

