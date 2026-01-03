/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.attributes.UnitType
 *  com.github.weisj.jsvg.renderer.TransformedPaint
 *  com.github.weisj.jsvg.util.ImageUtil
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.attributes.Overflow;
import com.github.weisj.jsvg.attributes.PreserveAspectRatio;
import com.github.weisj.jsvg.attributes.UnitType;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.attributes.paint.SVGPaint;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.MeasureContext;
import com.github.weisj.jsvg.nodes.Anchor;
import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Image;
import com.github.weisj.jsvg.nodes.Marker;
import com.github.weisj.jsvg.nodes.Mask;
import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.Style;
import com.github.weisj.jsvg.nodes.View;
import com.github.weisj.jsvg.nodes.container.BaseInnerViewContainer;
import com.github.weisj.jsvg.nodes.filter.Filter;
import com.github.weisj.jsvg.nodes.prototype.ShapedContainer;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.nodes.text.Text;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.renderer.GraphicsUtil;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.TransformedPaint;
import com.github.weisj.jsvg.util.ImageUtil;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ElementCategories(value={Category.Container})
@PermittedContent(categories={Category.Animation, Category.Descriptive, Category.Shape, Category.Structural, Category.Gradient}, anyOf={Anchor.class, ClipPath.class, Filter.class, Image.class, Mask.class, Marker.class, Pattern.class, Style.class, Text.class, View.class})
public final class Pattern
extends BaseInnerViewContainer
implements SVGPaint,
ShapedContainer<SVGNode> {
    public static final String TAG = "pattern";
    private Length x;
    private Length y;
    private Length width;
    private Length height;
    private UnitType patternUnits;
    private UnitType patternContentUnits;
    private AffineTransform patternTransform;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Override
    @NotNull
    protected Point2D outerLocation(@NotNull MeasureContext context) {
        return new Point2D.Float(0.0f, 0.0f);
    }

    @Override
    @Nullable
    protected Point2D anchorLocation(@NotNull MeasureContext context) {
        return null;
    }

    @Override
    @NotNull
    protected Overflow defaultOverflow() {
        return Overflow.Hidden;
    }

    @Override
    @NotNull
    public FloatSize size(@NotNull RenderContext context) {
        return new FloatSize(this.width.resolveWidth(context.measureContext()), this.height.resolveHeight(context.measureContext()));
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        Pattern template = this.parseTemplate(attributeNode);
        if (this.viewBox == null && template != null) {
            this.viewBox = template.viewBox;
        }
        this.preserveAspectRatio = template != null ? template.preserveAspectRatio : this.preserveAspectRatio;
        this.x = attributeNode.getLength("x", template != null ? template.x : Length.ZERO);
        this.y = attributeNode.getLength("y", template != null ? template.y : Length.ZERO);
        this.width = attributeNode.getLength("width", template != null ? template.width : Length.ZERO).coerceNonNegative();
        this.height = attributeNode.getLength("height", template != null ? template.height : Length.ZERO).coerceNonNegative();
        this.patternTransform = attributeNode.parseTransform("patternTransform");
        if (this.patternTransform == null && template != null) {
            this.patternTransform = template.patternTransform;
        }
        this.patternUnits = attributeNode.getEnum("patternUnits", template != null ? template.patternUnits : UnitType.ObjectBoundingBox);
        this.patternContentUnits = attributeNode.getEnum("patternContentUnits", template != null ? template.patternContentUnits : UnitType.UserSpaceOnUse);
    }

    @Nullable
    private Pattern parseTemplate(@NotNull AttributeNode attributeNode) {
        Pattern template = attributeNode.getElementByHref(Pattern.class, attributeNode.getHref());
        return template != this ? template : null;
    }

    @Override
    public boolean isVisible() {
        return !this.width.isZero() && !this.height.isZero() && SVGPaint.super.isVisible();
    }

    @Override
    public boolean requiresInstantiation() {
        return true;
    }

    @Override
    public void fillShape(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
        Rectangle2D b = bounds != null ? bounds : shape.getBounds2D();
        GraphicsUtil.safelySetPaint(g, this.paintForBounds(g, context, b));
        g.fill(shape);
    }

    @Override
    public void drawShape(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
        Rectangle2D b = bounds != null ? bounds : shape.getBounds2D();
        GraphicsUtil.safelySetPaint(g, this.paintForBounds(g, context, b));
        g.setPaint(this.paintForBounds(g, context, b));
        g.draw(shape);
    }

    @NotNull
    private Paint paintForBounds(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull Rectangle2D bounds) {
        FloatSize size;
        MeasureContext measure = context.measureContext();
        Rectangle2D.Double patternBounds = this.patternUnits.computeViewBounds(measure, bounds, this.x, this.y, this.width, this.height);
        BufferedImage img = ImageUtil.createCompatibleTransparentImage((Graphics2D)g, (double)patternBounds.width, (double)patternBounds.height);
        Graphics2D imgGraphics = GraphicsUtil.createGraphics(img);
        imgGraphics.setRenderingHints(g.getRenderingHints());
        imgGraphics.scale((double)img.getWidth() / patternBounds.width, (double)img.getHeight() / patternBounds.height);
        RenderContext patternContext = RenderContext.createInitial(null, this.patternContentUnits.deriveMeasure(measure));
        patternContext.setRootTransform(imgGraphics.getTransform());
        ViewBox view = this.viewBox;
        PreserveAspectRatio aspectRation = this.preserveAspectRatio;
        if (view == null && this.patternContentUnits == UnitType.ObjectBoundingBox) {
            size = new FloatSize(img.getWidth(), img.getHeight());
            view = new ViewBox(0.0f, 0.0f, 1.0f, 1.0f);
            aspectRation = PreserveAspectRatio.none();
        } else {
            size = new FloatSize((float)patternBounds.getWidth(), (float)patternBounds.getHeight());
        }
        this.renderWithSize(size, view, aspectRation, patternContext, imgGraphics);
        imgGraphics.dispose();
        return this.patternTransform != null ? new TransformedPaint((Paint)new TexturePaint(img, patternBounds), this.patternTransform) : new TexturePaint(img, patternBounds);
    }
}

