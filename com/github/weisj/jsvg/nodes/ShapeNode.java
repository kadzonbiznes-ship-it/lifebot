/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.attributes.PaintOrder;
import com.github.weisj.jsvg.attributes.VectorEffect;
import com.github.weisj.jsvg.attributes.font.FontParser;
import com.github.weisj.jsvg.attributes.font.FontSize;
import com.github.weisj.jsvg.attributes.font.MeasurableFontSpec;
import com.github.weisj.jsvg.geometry.MeasurableShape;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.MeasureContext;
import com.github.weisj.jsvg.nodes.Marker;
import com.github.weisj.jsvg.nodes.RenderableSVGNode;
import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.prototype.HasFontContext;
import com.github.weisj.jsvg.nodes.prototype.HasPaintContext;
import com.github.weisj.jsvg.nodes.prototype.HasShape;
import com.github.weisj.jsvg.nodes.prototype.HasVectorEffects;
import com.github.weisj.jsvg.nodes.prototype.Instantiator;
import com.github.weisj.jsvg.nodes.prototype.Mutator;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.renderer.PaintContext;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.renderer.ShapeRenderer;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public abstract class ShapeNode
extends RenderableSVGNode
implements HasShape,
HasPaintContext,
HasFontContext,
HasVectorEffects,
Instantiator {
    private PaintOrder paintOrder;
    private PaintContext paintContext;
    private FontSize fontSize;
    private Length fontSizeAdjust;
    private Length pathLength;
    private MeasurableShape shape;
    private Marker markerStart;
    private Marker markerMid;
    private Marker markerEnd;
    private Set<VectorEffect> vectorEffects;

    @NotNull
    public PaintContext paintContext() {
        return this.paintContext;
    }

    @Override
    @NotNull
    public Mutator<MeasurableFontSpec> fontSpec() {
        return s -> s.withFontSize(this.fontSize, this.fontSizeAdjust);
    }

    @NotNull
    public MeasurableShape shape() {
        return this.shape;
    }

    @Override
    @NotNull
    public Set<VectorEffect> vectorEffects() {
        return this.vectorEffects;
    }

    @Override
    public final void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.paintOrder = PaintOrder.parse(attributeNode);
        this.paintContext = PaintContext.parse(attributeNode);
        this.fontSize = FontParser.parseFontSize(attributeNode);
        this.fontSizeAdjust = FontParser.parseSizeAdjust(attributeNode);
        this.shape = this.buildShape(attributeNode);
        this.pathLength = attributeNode.getLength("pathLength", Length.UNSPECIFIED);
        Marker template = attributeNode.getElementByHref(Marker.class, attributeNode.getValue("marker"));
        this.markerStart = attributeNode.getElementByHref(Marker.class, attributeNode.getValue("marker-start"));
        if (this.markerStart == null) {
            this.markerStart = template;
        }
        this.markerMid = attributeNode.getElementByHref(Marker.class, attributeNode.getValue("marker-mid"));
        if (this.markerMid == null) {
            this.markerMid = template;
        }
        this.markerEnd = attributeNode.getElementByHref(Marker.class, attributeNode.getValue("marker-end"));
        if (this.markerEnd == null) {
            this.markerEnd = template;
        }
        this.vectorEffects = VectorEffect.parse(attributeNode);
    }

    @NotNull
    protected abstract MeasurableShape buildShape(@NotNull AttributeNode var1);

    @Override
    @NotNull
    public Shape untransformedElementShape(@NotNull RenderContext context) {
        return this.shape.shape(context);
    }

    @Override
    @NotNull
    public Rectangle2D untransformedElementBounds(@NotNull RenderContext context) {
        return this.shape.bounds(context, true);
    }

    @Override
    public boolean isVisible(@NotNull RenderContext context) {
        return super.isVisible(context);
    }

    @Override
    public boolean canInstantiate(@NotNull SVGNode node) {
        return node instanceof Marker;
    }

    @Override
    public final void render(@NotNull RenderContext context, @NotNull Graphics2D g) {
        MeasureContext measureContext = context.measureContext();
        Shape paintShape = this.shape.shape(context);
        Rectangle2D bounds = this.shape.usesOptimizedBoundsCalculation() ? this.shape.bounds(context, false) : null;
        float pathLengthFactor = 1.0f;
        if (this.pathLength.isSpecified()) {
            double effectiveLength = this.pathLength.resolveLength(measureContext);
            double actualLength = this.shape.pathLength(measureContext);
            pathLengthFactor = (float)(actualLength / effectiveLength);
        }
        Stroke effectiveStroke = context.stroke(pathLengthFactor);
        ShapeRenderer.renderWithPaintOrder(g, this.shape.canBeFilled(), this.paintOrder, new ShapeRenderer.ShapePaintContext(context, this.vectorEffects(), effectiveStroke, this.transform()), new ShapeRenderer.PaintShape(paintShape, bounds), new ShapeRenderer.ShapeMarkerInfo(this, this.markerStart, this.markerMid, this.markerEnd, this.shouldPaintStartEndMarkersInMiddle()));
    }

    protected boolean shouldPaintStartEndMarkersInMiddle() {
        return true;
    }
}

