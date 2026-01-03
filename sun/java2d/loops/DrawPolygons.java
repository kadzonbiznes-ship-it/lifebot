/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.SurfaceType;

public class DrawPolygons
extends GraphicsPrimitive {
    public static final String methodSignature = "DrawPolygons(...)".toString();
    public static final int primTypeID = DrawPolygons.makePrimTypeID();

    public static DrawPolygons locate(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return (DrawPolygons)GraphicsPrimitiveMgr.locate(primTypeID, srctype, comptype, dsttype);
    }

    protected DrawPolygons(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public DrawPolygons(long pNativePrim, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public native void DrawPolygons(SunGraphics2D var1, SurfaceData var2, int[] var3, int[] var4, int[] var5, int var6, int var7, int var8, boolean var9);

    @Override
    public GraphicsPrimitive traceWrap() {
        return new TraceDrawPolygons(this);
    }

    private static class TraceDrawPolygons
    extends DrawPolygons {
        DrawPolygons target;

        public TraceDrawPolygons(DrawPolygons target) {
            super(target.getSourceType(), target.getCompositeType(), target.getDestType());
            this.target = target;
        }

        @Override
        public GraphicsPrimitive traceWrap() {
            return this;
        }

        @Override
        public void DrawPolygons(SunGraphics2D sg2d, SurfaceData sData, int[] xPoints, int[] yPoints, int[] nPoints, int numPolys, int transX, int transY, boolean close) {
            TraceDrawPolygons.tracePrimitive(this.target);
            this.target.DrawPolygons(sg2d, sData, xPoints, yPoints, nPoints, numPolys, transX, transY, close);
        }
    }
}

