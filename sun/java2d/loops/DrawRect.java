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

public class DrawRect
extends GraphicsPrimitive {
    public static final String methodSignature = "DrawRect(...)".toString();
    public static final int primTypeID = DrawRect.makePrimTypeID();

    public static DrawRect locate(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return (DrawRect)GraphicsPrimitiveMgr.locate(primTypeID, srctype, comptype, dsttype);
    }

    protected DrawRect(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public DrawRect(long pNativePrim, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public native void DrawRect(SunGraphics2D var1, SurfaceData var2, int var3, int var4, int var5, int var6);

    @Override
    public GraphicsPrimitive traceWrap() {
        return new TraceDrawRect(this);
    }

    private static class TraceDrawRect
    extends DrawRect {
        DrawRect target;

        public TraceDrawRect(DrawRect target) {
            super(target.getSourceType(), target.getCompositeType(), target.getDestType());
            this.target = target;
        }

        @Override
        public GraphicsPrimitive traceWrap() {
            return this;
        }

        @Override
        public void DrawRect(SunGraphics2D sg2d, SurfaceData dest, int x1, int y1, int w, int h) {
            TraceDrawRect.tracePrimitive(this.target);
            this.target.DrawRect(sg2d, dest, x1, y1, w, h);
        }
    }
}

