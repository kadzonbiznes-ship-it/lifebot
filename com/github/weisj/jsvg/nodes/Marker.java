/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.attributes.MarkerOrientation
 *  com.github.weisj.jsvg.attributes.MarkerUnitType
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.attributes.AttributeParser;
import com.github.weisj.jsvg.attributes.MarkerOrientation;
import com.github.weisj.jsvg.attributes.MarkerUnitType;
import com.github.weisj.jsvg.attributes.Overflow;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.MeasureContext;
import com.github.weisj.jsvg.nodes.Anchor;
import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Image;
import com.github.weisj.jsvg.nodes.Mask;
import com.github.weisj.jsvg.nodes.Pattern;
import com.github.weisj.jsvg.nodes.Style;
import com.github.weisj.jsvg.nodes.View;
import com.github.weisj.jsvg.nodes.container.BaseInnerViewContainer;
import com.github.weisj.jsvg.nodes.filter.Filter;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.nodes.text.Text;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.renderer.NodeRenderer;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.geom.Point2D;
import org.jetbrains.annotations.NotNull;

@ElementCategories(value={Category.Container})
@PermittedContent(categories={Category.Animation, Category.Descriptive, Category.Shape, Category.Structural, Category.Gradient}, anyOf={Anchor.class, ClipPath.class, Filter.class, Image.class, Mask.class, Marker.class, Pattern.class, Style.class, Text.class, View.class})
public final class Marker
extends BaseInnerViewContainer {
    public static final String TAG = "marker";
    private Length refX;
    private Length refY;
    private MarkerOrientation orientation;
    private MarkerUnitType markerUnits;
    private Length markerHeight;
    private Length markerWidth;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @NotNull
    public MarkerOrientation orientation() {
        return this.orientation;
    }

    @Override
    @NotNull
    protected Point2D outerLocation(@NotNull MeasureContext context) {
        return new Point2D.Float(0.0f, 0.0f);
    }

    @Override
    @NotNull
    protected Point2D anchorLocation(@NotNull MeasureContext context) {
        return new Point2D.Float(-this.refX.resolveWidth(context), -this.refY.resolveHeight(context));
    }

    @Override
    @NotNull
    protected Overflow defaultOverflow() {
        return Overflow.Hidden;
    }

    @Override
    @NotNull
    public FloatSize size(@NotNull RenderContext context) {
        MeasureContext measure = context.measureContext();
        if (this.markerUnits == MarkerUnitType.StrokeWidth) {
            Length strokeWidthLength = context.strokeContext().strokeWidth;
            assert (strokeWidthLength != null);
            float strokeWidth = strokeWidthLength.resolveLength(measure);
            return new FloatSize(this.markerWidth.raw() * strokeWidth, this.markerHeight.raw() * strokeWidth);
        }
        return new FloatSize(this.markerWidth.resolveWidth(measure), this.markerHeight.resolveHeight(measure));
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.refX = attributeNode.getHorizontalReferenceLength("refX");
        this.refY = attributeNode.getHorizontalReferenceLength("refY");
        this.orientation = MarkerOrientation.parse((String)attributeNode.getValue("orient"), (AttributeParser)attributeNode.parser());
        this.markerUnits = attributeNode.getEnum("markerUnits", MarkerUnitType.StrokeWidth);
        this.markerWidth = attributeNode.getLength("markerWidth", 3.0f);
        this.markerHeight = attributeNode.getLength("markerHeight", 3.0f);
    }

    @Override
    public boolean requiresInstantiation() {
        return true;
    }

    @Override
    @NotNull
    protected RenderContext createInnerContext(@NotNull RenderContext context, @NotNull ViewBox viewBox) {
        return NodeRenderer.setupInnerViewRenderContext(viewBox, context, false);
    }
}

