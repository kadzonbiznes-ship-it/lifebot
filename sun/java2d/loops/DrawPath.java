/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import java.awt.geom.Path2D;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.SurfaceType;

public class DrawPath
extends GraphicsPrimitive {
    public static final String methodSignature = "DrawPath(...)".toString();
    public static final int primTypeID = DrawPath.makePrimTypeID();

    public static DrawPath locate(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return (DrawPath)GraphicsPrimitiveMgr.locate(primTypeID, srctype, comptype, dsttype);
    }

    protected DrawPath(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public DrawPath(long pNativePrim, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public native void DrawPath(SunGraphics2D var1, SurfaceData var2, int var3, int var4, Path2D.Float var5);

    @Override
    public GraphicsPrimitive traceWrap() {
        return new TraceDrawPath(this);
    }

    private static class TraceDrawPath
    extends DrawPath {
        DrawPath target;

        public TraceDrawPath(DrawPath target) {
            super(target.getSourceType(), target.getCompositeType(), target.getDestType());
            this.target = target;
        }

        @Override
        public GraphicsPrimitive traceWrap() {
            return this;
        }

        @Override
        public void DrawPath(SunGraphics2D sg2d, SurfaceData sData, int transX, int transY, Path2D.Float p2df) {
            TraceDrawPath.tracePrimitive(this.target);
            this.target.DrawPath(sg2d, sData, transX, transY, p2df);
        }
    }
}

