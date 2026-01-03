/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.windows;

import java.awt.Composite;
import java.awt.Shape;
import java.awt.geom.Path2D;
import sun.java2d.InvalidPipeException;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.pipe.LoopPipe;
import sun.java2d.pipe.PixelDrawPipe;
import sun.java2d.pipe.PixelFillPipe;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.ShapeDrawPipe;
import sun.java2d.pipe.ShapeSpanIterator;
import sun.java2d.pipe.SpanIterator;
import sun.java2d.windows.GDIWindowSurfaceData;

public class GDIRenderer
implements PixelDrawPipe,
PixelFillPipe,
ShapeDrawPipe {
    native void doDrawLine(GDIWindowSurfaceData var1, Region var2, Composite var3, int var4, int var5, int var6, int var7, int var8);

    @Override
    public void drawLine(SunGraphics2D sg2d, int x1, int y1, int x2, int y2) {
        int transx = sg2d.transX;
        int transy = sg2d.transY;
        try {
            this.doDrawLine((GDIWindowSurfaceData)sg2d.surfaceData, sg2d.getCompClip(), sg2d.composite, sg2d.eargb, x1 + transx, y1 + transy, x2 + transx, y2 + transy);
        }
        catch (ClassCastException e) {
            throw new InvalidPipeException("wrong surface data type: " + String.valueOf(sg2d.surfaceData));
        }
    }

    native void doDrawRect(GDIWindowSurfaceData var1, Region var2, Composite var3, int var4, int var5, int var6, int var7, int var8);

    @Override
    public void drawRect(SunGraphics2D sg2d, int x, int y, int width, int height) {
        try {
            this.doDrawRect((GDIWindowSurfaceData)sg2d.surfaceData, sg2d.getCompClip(), sg2d.composite, sg2d.eargb, x + sg2d.transX, y + sg2d.transY, width, height);
        }
        catch (ClassCastException e) {
            throw new InvalidPipeException("wrong surface data type: " + String.valueOf(sg2d.surfaceData));
        }
    }

    native void doDrawRoundRect(GDIWindowSurfaceData var1, Region var2, Composite var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10);

    @Override
    public void drawRoundRect(SunGraphics2D sg2d, int x, int y, int width, int height, int arcWidth, int arcHeight) {
        try {
            this.doDrawRoundRect((GDIWindowSurfaceData)sg2d.surfaceData, sg2d.getCompClip(), sg2d.composite, sg2d.eargb, x + sg2d.transX, y + sg2d.transY, width, height, arcWidth, arcHeight);
        }
        catch (ClassCastException e) {
            throw new InvalidPipeException("wrong surface data type: " + String.valueOf(sg2d.surfaceData));
        }
    }

    native void doDrawOval(GDIWindowSurfaceData var1, Region var2, Composite var3, int var4, int var5, int var6, int var7, int var8);

    @Override
    public void drawOval(SunGraphics2D sg2d, int x, int y, int width, int height) {
        try {
            this.doDrawOval((GDIWindowSurfaceData)sg2d.surfaceData, sg2d.getCompClip(), sg2d.composite, sg2d.eargb, x + sg2d.transX, y + sg2d.transY, width, height);
        }
        catch (ClassCastException e) {
            throw new InvalidPipeException("wrong surface data type: " + String.valueOf(sg2d.surfaceData));
        }
    }

    native void doDrawArc(GDIWindowSurfaceData var1, Region var2, Composite var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10);

    @Override
    public void drawArc(SunGraphics2D sg2d, int x, int y, int width, int height, int startAngle, int arcAngle) {
        try {
            this.doDrawArc((GDIWindowSurfaceData)sg2d.surfaceData, sg2d.getCompClip(), sg2d.composite, sg2d.eargb, x + sg2d.transX, y + sg2d.transY, width, height, startAngle, arcAngle);
        }
        catch (ClassCastException e) {
            throw new InvalidPipeException("wrong surface data type: " + String.valueOf(sg2d.surfaceData));
        }
    }

    native void doDrawPoly(GDIWindowSurfaceData var1, Region var2, Composite var3, int var4, int var5, int var6, int[] var7, int[] var8, int var9, boolean var10);

    @Override
    public void drawPolyline(SunGraphics2D sg2d, int[] xpoints, int[] ypoints, int npoints) {
        try {
            this.doDrawPoly((GDIWindowSurfaceData)sg2d.surfaceData, sg2d.getCompClip(), sg2d.composite, sg2d.eargb, sg2d.transX, sg2d.transY, xpoints, ypoints, npoints, false);
        }
        catch (ClassCastException e) {
            throw new InvalidPipeException("wrong surface data type: " + String.valueOf(sg2d.surfaceData));
        }
    }

    @Override
    public void drawPolygon(SunGraphics2D sg2d, int[] xpoints, int[] ypoints, int npoints) {
        try {
            this.doDrawPoly((GDIWindowSurfaceData)sg2d.surfaceData, sg2d.getCompClip(), sg2d.composite, sg2d.eargb, sg2d.transX, sg2d.transY, xpoints, ypoints, npoints, true);
        }
        catch (ClassCastException e) {
            throw new InvalidPipeException("wrong surface data type: " + String.valueOf(sg2d.surfaceData));
        }
    }

    native void doFillRect(GDIWindowSurfaceData var1, Region var2, Composite var3, int var4, int var5, int var6, int var7, int var8);

    @Override
    public void fillRect(SunGraphics2D sg2d, int x, int y, int width, int height) {
        try {
            this.doFillRect((GDIWindowSurfaceData)sg2d.surfaceData, sg2d.getCompClip(), sg2d.composite, sg2d.eargb, x + sg2d.transX, y + sg2d.transY, width, height);
        }
        catch (ClassCastException e) {
            throw new InvalidPipeException("wrong surface data type: " + String.valueOf(sg2d.surfaceData));
        }
    }

    native void doFillRoundRect(GDIWindowSurfaceData var1, Region var2, Composite var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10);

    @Override
    public void fillRoundRect(SunGraphics2D sg2d, int x, int y, int width, int height, int arcWidth, int arcHeight) {
        try {
            this.doFillRoundRect((GDIWindowSurfaceData)sg2d.surfaceData, sg2d.getCompClip(), sg2d.composite, sg2d.eargb, x + sg2d.transX, y + sg2d.transY, width, height, arcWidth, arcHeight);
        }
        catch (ClassCastException e) {
            throw new InvalidPipeException("wrong surface data type: " + String.valueOf(sg2d.surfaceData));
        }
    }

    native void doFillOval(GDIWindowSurfaceData var1, Region var2, Composite var3, int var4, int var5, int var6, int var7, int var8);

    @Override
    public void fillOval(SunGraphics2D sg2d, int x, int y, int width, int height) {
        try {
            this.doFillOval((GDIWindowSurfaceData)sg2d.surfaceData, sg2d.getCompClip(), sg2d.composite, sg2d.eargb, x + sg2d.transX, y + sg2d.transY, width, height);
        }
        catch (ClassCastException e) {
            throw new InvalidPipeException("wrong surface data type: " + String.valueOf(sg2d.surfaceData));
        }
    }

    native void doFillArc(GDIWindowSurfaceData var1, Region var2, Composite var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10);

    @Override
    public void fillArc(SunGraphics2D sg2d, int x, int y, int width, int height, int startAngle, int arcAngle) {
        try {
            this.doFillArc((GDIWindowSurfaceData)sg2d.surfaceData, sg2d.getCompClip(), sg2d.composite, sg2d.eargb, x + sg2d.transX, y + sg2d.transY, width, height, startAngle, arcAngle);
        }
        catch (ClassCastException e) {
            throw new InvalidPipeException("wrong surface data type: " + String.valueOf(sg2d.surfaceData));
        }
    }

    native void doFillPoly(GDIWindowSurfaceData var1, Region var2, Composite var3, int var4, int var5, int var6, int[] var7, int[] var8, int var9);

    @Override
    public void fillPolygon(SunGraphics2D sg2d, int[] xpoints, int[] ypoints, int npoints) {
        try {
            this.doFillPoly((GDIWindowSurfaceData)sg2d.surfaceData, sg2d.getCompClip(), sg2d.composite, sg2d.eargb, sg2d.transX, sg2d.transY, xpoints, ypoints, npoints);
        }
        catch (ClassCastException e) {
            throw new InvalidPipeException("wrong surface data type: " + String.valueOf(sg2d.surfaceData));
        }
    }

    native void doShape(GDIWindowSurfaceData var1, Region var2, Composite var3, int var4, int var5, int var6, Path2D.Float var7, boolean var8);

    void doShape(SunGraphics2D sg2d, Shape s, boolean isfill) {
        int transY;
        int transX;
        Path2D.Float p2df;
        if (sg2d.transformState <= 1) {
            p2df = s instanceof Path2D.Float ? (Path2D.Float)s : new Path2D.Float(s);
            transX = sg2d.transX;
            transY = sg2d.transY;
        } else {
            p2df = new Path2D.Float(s, sg2d.transform);
            transX = 0;
            transY = 0;
        }
        try {
            this.doShape((GDIWindowSurfaceData)sg2d.surfaceData, sg2d.getCompClip(), sg2d.composite, sg2d.eargb, transX, transY, p2df, isfill);
        }
        catch (ClassCastException e) {
            throw new InvalidPipeException("wrong surface data type: " + String.valueOf(sg2d.surfaceData));
        }
    }

    public void doFillSpans(SunGraphics2D sg2d, SpanIterator si) {
        int[] box = new int[4];
        GDIWindowSurfaceData sd = SurfaceData.convertTo(GDIWindowSurfaceData.class, sg2d.surfaceData);
        Region clip = sg2d.getCompClip();
        Composite comp = sg2d.composite;
        int eargb = sg2d.eargb;
        while (si.nextSpan(box)) {
            this.doFillRect(sd, clip, comp, eargb, box[0], box[1], box[2] - box[0], box[3] - box[1]);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void draw(SunGraphics2D sg2d, Shape s) {
        if (sg2d.strokeState == 0) {
            this.doShape(sg2d, s, false);
        } else if (sg2d.strokeState < 3) {
            ShapeSpanIterator si = LoopPipe.getStrokeSpans(sg2d, s);
            try {
                this.doFillSpans(sg2d, si);
            }
            finally {
                si.dispose();
            }
        } else {
            this.doShape(sg2d, sg2d.stroke.createStrokedShape(s), true);
        }
    }

    @Override
    public void fill(SunGraphics2D sg2d, Shape s) {
        this.doShape(sg2d, s, true);
    }

    public native void devCopyArea(GDIWindowSurfaceData var1, int var2, int var3, int var4, int var5, int var6, int var7);

    public GDIRenderer traceWrap() {
        return new Tracer();
    }

    public static class Tracer
    extends GDIRenderer {
        @Override
        void doDrawLine(GDIWindowSurfaceData sData, Region clip, Composite comp, int color, int x1, int y1, int x2, int y2) {
            GraphicsPrimitive.tracePrimitive("GDIDrawLine");
            super.doDrawLine(sData, clip, comp, color, x1, y1, x2, y2);
        }

        @Override
        void doDrawRect(GDIWindowSurfaceData sData, Region clip, Composite comp, int color, int x, int y, int w, int h) {
            GraphicsPrimitive.tracePrimitive("GDIDrawRect");
            super.doDrawRect(sData, clip, comp, color, x, y, w, h);
        }

        @Override
        void doDrawRoundRect(GDIWindowSurfaceData sData, Region clip, Composite comp, int color, int x, int y, int w, int h, int arcW, int arcH) {
            GraphicsPrimitive.tracePrimitive("GDIDrawRoundRect");
            super.doDrawRoundRect(sData, clip, comp, color, x, y, w, h, arcW, arcH);
        }

        @Override
        void doDrawOval(GDIWindowSurfaceData sData, Region clip, Composite comp, int color, int x, int y, int w, int h) {
            GraphicsPrimitive.tracePrimitive("GDIDrawOval");
            super.doDrawOval(sData, clip, comp, color, x, y, w, h);
        }

        @Override
        void doDrawArc(GDIWindowSurfaceData sData, Region clip, Composite comp, int color, int x, int y, int w, int h, int angleStart, int angleExtent) {
            GraphicsPrimitive.tracePrimitive("GDIDrawArc");
            super.doDrawArc(sData, clip, comp, color, x, y, w, h, angleStart, angleExtent);
        }

        @Override
        void doDrawPoly(GDIWindowSurfaceData sData, Region clip, Composite comp, int color, int transx, int transy, int[] xpoints, int[] ypoints, int npoints, boolean isclosed) {
            GraphicsPrimitive.tracePrimitive("GDIDrawPoly");
            super.doDrawPoly(sData, clip, comp, color, transx, transy, xpoints, ypoints, npoints, isclosed);
        }

        @Override
        void doFillRect(GDIWindowSurfaceData sData, Region clip, Composite comp, int color, int x, int y, int w, int h) {
            GraphicsPrimitive.tracePrimitive("GDIFillRect");
            super.doFillRect(sData, clip, comp, color, x, y, w, h);
        }

        @Override
        void doFillRoundRect(GDIWindowSurfaceData sData, Region clip, Composite comp, int color, int x, int y, int w, int h, int arcW, int arcH) {
            GraphicsPrimitive.tracePrimitive("GDIFillRoundRect");
            super.doFillRoundRect(sData, clip, comp, color, x, y, w, h, arcW, arcH);
        }

        @Override
        void doFillOval(GDIWindowSurfaceData sData, Region clip, Composite comp, int color, int x, int y, int w, int h) {
            GraphicsPrimitive.tracePrimitive("GDIFillOval");
            super.doFillOval(sData, clip, comp, color, x, y, w, h);
        }

        @Override
        void doFillArc(GDIWindowSurfaceData sData, Region clip, Composite comp, int color, int x, int y, int w, int h, int angleStart, int angleExtent) {
            GraphicsPrimitive.tracePrimitive("GDIFillArc");
            super.doFillArc(sData, clip, comp, color, x, y, w, h, angleStart, angleExtent);
        }

        @Override
        void doFillPoly(GDIWindowSurfaceData sData, Region clip, Composite comp, int color, int transx, int transy, int[] xpoints, int[] ypoints, int npoints) {
            GraphicsPrimitive.tracePrimitive("GDIFillPoly");
            super.doFillPoly(sData, clip, comp, color, transx, transy, xpoints, ypoints, npoints);
        }

        @Override
        void doShape(GDIWindowSurfaceData sData, Region clip, Composite comp, int color, int transX, int transY, Path2D.Float p2df, boolean isfill) {
            GraphicsPrimitive.tracePrimitive(isfill ? "GDIFillShape" : "GDIDrawShape");
            super.doShape(sData, clip, comp, color, transX, transY, p2df, isfill);
        }

        @Override
        public void devCopyArea(GDIWindowSurfaceData sData, int srcx, int srcy, int dx, int dy, int w, int h) {
            GraphicsPrimitive.tracePrimitive("GDICopyArea");
            super.devCopyArea(sData, srcx, srcy, dx, dy, w, h);
        }
    }
}

