/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.attributes.MarkerOrientation
 *  com.github.weisj.jsvg.attributes.MarkerOrientation$MarkerType
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.renderer;

import com.github.weisj.jsvg.attributes.MarkerOrientation;
import com.github.weisj.jsvg.attributes.PaintOrder;
import com.github.weisj.jsvg.attributes.VectorEffect;
import com.github.weisj.jsvg.attributes.paint.SVGPaint;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.nodes.Marker;
import com.github.weisj.jsvg.nodes.ShapeNode;
import com.github.weisj.jsvg.renderer.GraphicsUtil;
import com.github.weisj.jsvg.renderer.NodeRenderer;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.util.GraphicsResetHelper;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ShapeRenderer {
    private static final boolean DEBUG_MARKERS = false;

    private ShapeRenderer() {
    }

    public static void renderWithPaintOrder(@NotNull Graphics2D g, boolean canBeFilledHint, @NotNull PaintOrder paintOrder, @NotNull ShapePaintContext shapePaintContext, @NotNull PaintShape paintShape, @Nullable ShapeMarkerInfo markerInfo) {
        Set vectorEffects = shapePaintContext.vectorEffects;
        VectorEffect.applyEffects(shapePaintContext.vectorEffects, g, shapePaintContext.context, shapePaintContext.transform);
        GraphicsResetHelper resetHelper = new GraphicsResetHelper(g);
        for (PaintOrder.Phase phase : paintOrder.phases()) {
            RenderContext phaseContext = shapePaintContext.context.deriveForChildGraphics();
            switch (phase) {
                case FILL: {
                    if (!canBeFilledHint) break;
                    ShapeRenderer.renderShapeFill(phaseContext, resetHelper.graphics(), paintShape);
                    break;
                }
                case STROKE: {
                    Shape strokeShape = paintShape.shape;
                    if (vectorEffects.contains(VectorEffect.NonScalingStroke) && !vectorEffects.contains(VectorEffect.NonScalingSize)) {
                        strokeShape = VectorEffect.applyNonScalingStroke(resetHelper.graphics(), phaseContext, strokeShape);
                    }
                    ShapeRenderer.renderShapeStroke(phaseContext, resetHelper.graphics(), new PaintShape(strokeShape, paintShape.bounds), shapePaintContext.stroke);
                    break;
                }
                case MARKERS: {
                    if (markerInfo == null) break;
                    ShapeRenderer.renderMarkers(resetHelper.graphics(), phaseContext, paintShape, markerInfo);
                }
            }
            resetHelper.reset();
        }
    }

    private static void renderMarkers(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull PaintShape paintShape, @NotNull ShapeMarkerInfo markerInfo) {
        if (markerInfo.markerStart == null && markerInfo.markerMid == null && markerInfo.markerEnd == null) {
            return;
        }
        ShapeRenderer.renderMarkersImpl(g, context, paintShape.shape.getPathIterator(null), markerInfo);
    }

    private static void renderShapeStroke(@NotNull RenderContext context, @NotNull Graphics2D g, @NotNull PaintShape paintShape, @Nullable Stroke stroke) {
        PaintWithOpacity paintWithOpacity = new PaintWithOpacity(context.strokePaint(), context.strokeOpacity());
        if (stroke == null || !paintWithOpacity.isVisible()) {
            return;
        }
        g.setComposite(GraphicsUtil.deriveComposite(g, paintWithOpacity.opacity));
        g.setStroke(stroke);
        paintWithOpacity.paint.drawShape(g, context, paintShape.shape, paintShape.bounds);
    }

    private static void renderShapeFill(@NotNull RenderContext context, @NotNull Graphics2D g, @NotNull PaintShape paintShape) {
        PaintWithOpacity paintWithOpacity = new PaintWithOpacity(context.fillPaint(), context.fillOpacity());
        if (!paintWithOpacity.isVisible()) {
            return;
        }
        g.setComposite(GraphicsUtil.deriveComposite(g, paintWithOpacity.opacity));
        paintWithOpacity.paint.fillShape(g, context, paintShape.shape, paintShape.bounds);
    }

    private static void renderMarkersImpl(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull PathIterator iterator, @NotNull ShapeMarkerInfo markerInfo) {
        float[] args = new float[6];
        float x = 0.0f;
        float y = 0.0f;
        float xStart = 0.0f;
        float yStart = 0.0f;
        float dxIn = 0.0f;
        float dyIn = 0.0f;
        Marker start = markerInfo.markerStart;
        Marker mid = markerInfo.markerMid;
        Marker end = markerInfo.markerEnd;
        boolean onlyFirst = mid == null && end == null;
        Marker markerToPaint = null;
        MarkerOrientation.MarkerType markerToPaintType = null;
        block7: while (!iterator.isDone()) {
            float dyOut;
            float dxOut;
            int type = iterator.currentSegment(args);
            iterator.next();
            Marker nextMarker = iterator.isDone() ? end : mid;
            MarkerOrientation.MarkerType nextMarkerType = iterator.isDone() ? MarkerOrientation.MarkerType.END : MarkerOrientation.MarkerType.MID;
            float xPaint = x;
            float yPaint = y;
            float dx = dxIn;
            float dy = dyIn;
            switch (type) {
                case 0: {
                    dxIn = 0.0f;
                    dyIn = 0.0f;
                    x = xStart = args[0];
                    y = yStart = args[1];
                    if (markerInfo.shouldPaintStartEndMarkersInMiddle || markerToPaint == null) {
                        nextMarker = start;
                        nextMarkerType = MarkerOrientation.MarkerType.START;
                    }
                    if (markerToPaint != null) {
                        ShapeRenderer.paintSingleMarker(markerInfo.node, context, g, markerToPaintType, markerToPaint, xPaint, yPaint, 0.0f, 0.0f, dx, dy);
                        if (onlyFirst) {
                            return;
                        }
                    }
                    markerToPaint = nextMarker;
                    markerToPaintType = nextMarkerType;
                    continue block7;
                }
                case 1: {
                    dxOut = dxIn = args[0] - x;
                    dyOut = dyIn = args[1] - y;
                    x = args[0];
                    y = args[1];
                    break;
                }
                case 2: {
                    dxOut = args[0] - x;
                    dyOut = args[1] - y;
                    dxIn = args[2] - args[0];
                    dyIn = args[3] - args[1];
                    x = args[2];
                    y = args[3];
                    break;
                }
                case 3: {
                    dxOut = args[0] - x;
                    dyOut = args[1] - y;
                    dxIn = args[4] - args[2];
                    dyIn = args[5] - args[3];
                    x = args[4];
                    y = args[5];
                    break;
                }
                case 4: {
                    dxOut = dxIn = xStart - x;
                    dyOut = dyIn = yStart - y;
                    x = xStart;
                    y = yStart;
                    if (!markerInfo.shouldPaintStartEndMarkersInMiddle) break;
                    nextMarker = end;
                    nextMarkerType = MarkerOrientation.MarkerType.END;
                    break;
                }
                default: {
                    throw new IllegalStateException();
                }
            }
            ShapeRenderer.paintSingleMarker(markerInfo.node, context, g, markerToPaintType, markerToPaint, xPaint, yPaint, dx, dy, dxOut, dyOut);
            if (onlyFirst) {
                return;
            }
            markerToPaint = nextMarker;
            markerToPaintType = nextMarkerType;
        }
        ShapeRenderer.paintSingleMarker(markerInfo.node, context, g, markerToPaintType, markerToPaint, x, y, dxIn, dyIn, 0.0f, 0.0f);
    }

    public static void paintSingleMarker(@NotNull ShapeNode shapeNode, @NotNull RenderContext context, @NotNull Graphics2D g, @Nullable MarkerOrientation.MarkerType type, @Nullable Marker marker, float x, float y, float dxIn, float dyIn, float dxOut, float dyOut) {
        if (marker == null) {
            return;
        }
        assert (type != null);
        MarkerOrientation orientation = marker.orientation();
        float rotation = orientation.orientationFor(type, dxIn, dyIn, dxOut, dyOut);
        Graphics2D markerGraphics = (Graphics2D)g.create();
        RenderContext markerContext = context.deriveForChildGraphics();
        markerContext.translate(markerGraphics, x, y);
        markerContext.rotate(markerGraphics, rotation);
        try (NodeRenderer.Info info = NodeRenderer.createRenderInfo(marker, markerContext, markerGraphics, shapeNode);){
            if (info != null) {
                info.renderable.render(info.context, info.graphics());
            }
        }
        markerGraphics.dispose();
    }

    private static void paintDebugMarker(@NotNull RenderContext context, @NotNull Graphics2D g, @NotNull Marker marker, float rotation) {
        FloatSize size = marker.size(context);
        Path2D.Float p = new Path2D.Float();
        ((Path2D)p).moveTo(0.0, size.height / 2.0f);
        ((Path2D)p).lineTo(size.width, size.height / 2.0f);
        ((Path2D)p).moveTo(0.8 * (double)size.width, 0.35f * size.height);
        ((Path2D)p).lineTo(size.width, size.height / 2.0f);
        ((Path2D)p).lineTo(0.8 * (double)size.width, 0.65f * size.height);
        g.setStroke(new BasicStroke(0.5f));
        g.setColor(Color.MAGENTA.darker().darker());
        g.draw(new Rectangle2D.Float(0.0f, 0.0f, size.width, size.height));
        g.draw(p);
        g.rotate(rotation);
        g.setColor(Color.MAGENTA);
        g.draw(new Rectangle2D.Float(0.0f, 0.0f, size.width, size.height));
        g.draw(p);
    }

    public static final class ShapePaintContext {
        @NotNull
        private final RenderContext context;
        @NotNull
        private final Set<VectorEffect> vectorEffects;
        @NotNull
        private final Stroke stroke;
        @Nullable
        private final AffineTransform transform;

        public ShapePaintContext(@NotNull RenderContext context, @NotNull Set<VectorEffect> vectorEffects, @NotNull Stroke stroke, @Nullable AffineTransform transform) {
            this.context = context;
            this.vectorEffects = vectorEffects;
            this.stroke = stroke;
            this.transform = transform;
        }
    }

    public static final class PaintShape {
        @NotNull
        private final Shape shape;
        @Nullable
        private final Rectangle2D bounds;

        public PaintShape(@NotNull Shape shape, @Nullable Rectangle2D bounds) {
            this.shape = shape;
            this.bounds = bounds;
        }
    }

    public static final class ShapeMarkerInfo {
        @NotNull
        private final ShapeNode node;
        @Nullable
        private final Marker markerStart;
        @Nullable
        private final Marker markerMid;
        @Nullable
        private final Marker markerEnd;
        private final boolean shouldPaintStartEndMarkersInMiddle;

        public ShapeMarkerInfo(@NotNull ShapeNode node, @Nullable Marker markerStart, @Nullable Marker markerMid, @Nullable Marker markerEnd, boolean shouldPaintStartEndMarkersInMiddle) {
            this.node = node;
            this.markerStart = markerStart;
            this.markerMid = markerMid;
            this.markerEnd = markerEnd;
            this.shouldPaintStartEndMarkersInMiddle = shouldPaintStartEndMarkersInMiddle;
        }
    }

    private static final class PaintWithOpacity {
        @NotNull
        private final SVGPaint paint;
        private final float opacity;

        private PaintWithOpacity(@NotNull SVGPaint paint, float opacity) {
            this.paint = paint;
            this.opacity = opacity;
        }

        boolean isVisible() {
            return this.opacity > 0.0f && this.paint.isVisible();
        }
    }
}

