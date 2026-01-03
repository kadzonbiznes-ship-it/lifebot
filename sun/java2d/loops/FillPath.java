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

public class FillPath
extends GraphicsPrimitive {
    public static final String methodSignature = "FillPath(...)".toString();
    public static final int primTypeID = FillPath.makePrimTypeID();

    public static FillPath locate(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        return (FillPath)GraphicsPrimitiveMgr.locate(primTypeID, srctype, comptype, dsttype);
    }

    protected FillPath(SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public FillPath(long pNativePrim, SurfaceType srctype, CompositeType comptype, SurfaceType dsttype) {
        super(pNativePrim, methodSignature, primTypeID, srctype, comptype, dsttype);
    }

    public native void FillPath(SunGraphics2D var1, SurfaceData var2, int var3, int var4, Path2D.Float var5);

    @Override
    public GraphicsPrimitive traceWrap() {
        return new TraceFillPath(this);
    }

    private static class TraceFillPath
    extends FillPath {
        FillPath target;

        public TraceFillPath(FillPath target) {
            super(target.getSourceType(), target.getCompositeType(), target.getDestType());
            this.target = target;
        }

        @Override
        public GraphicsPrimitive traceWrap() {
            return this;
        }

        @Override
        public void FillPath(SunGraphics2D sg2d, SurfaceData sData, int transX, int transY, Path2D.Float p2df) {
            TraceFillPath.tracePrimitive(this.target);
            this.target.FillPath(sg2d, sData, transX, transY, p2df);
        }
    }
}

