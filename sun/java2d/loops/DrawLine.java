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

public class DrawLine
extends GraphicsPrimitive {
    public static final String methodSignature = "DrawLine(...)".toString();
    public static final int primTypeID = DrawLine.makePrimTypeID();

    public static DrawLine locate(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return (DrawLine)GraphicsPrimitiveMgr.locate(primTypeID, srctype, comptype, dsttype);
    }

    protected DrawLine(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public DrawLine(long pNativePrim, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public native void DrawLine(SunGraphics2D var1, SurfaceData var2, int var3, int var4, int var5, int var6);

    @Override
    public GraphicsPrimitive traceWrap() {
        return new TraceDrawLine(this);
    }

    private static class TraceDrawLine
    extends DrawLine {
        DrawLine target;

        public TraceDrawLine(DrawLine target) {
            super(target.getSourceType(), target.getCompositeType(), target.getDestType());
            this.target = target;
        }

        @Override
        public GraphicsPrimitive traceWrap() {
            return this;
        }

        @Override
        public void DrawLine(SunGraphics2D sg2d, SurfaceData dest, int x1, int y1, int x2, int y2) {
            TraceDrawLine.tracePrimitive(this.target);
            this.target.DrawLine(sg2d, dest, x1, y1, x2, y2);
        }
    }
}

