/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import java.awt.Rectangle;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.GraphicsPrimitiveProxy;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;

public final class CustomComponent {
    public static void register() {
        Class<CustomComponent> owner = CustomComponent.class;
        GraphicsPrimitive[] primitives = new GraphicsPrimitive[]{new GraphicsPrimitiveProxy(owner, "OpaqueCopyAnyToArgb", Blit.methodSignature, Blit.primTypeID, SurfaceType.Any, CompositeType.SrcNoEa, SurfaceType.IntArgb), new GraphicsPrimitiveProxy(owner, "OpaqueCopyArgbToAny", Blit.methodSignature, Blit.primTypeID, SurfaceType.IntArgb, CompositeType.SrcNoEa, SurfaceType.Any), new GraphicsPrimitiveProxy(owner, "XorCopyArgbToAny", Blit.methodSignature, Blit.primTypeID, SurfaceType.IntArgb, CompositeType.Xor, SurfaceType.Any)};
        GraphicsPrimitiveMgr.register(primitives);
    }

    public static Region getRegionOfInterest(SurfaceData src, SurfaceData dst, Region clip, int srcx, int srcy, int dstx, int dsty, int w, int h) {
        Region ret = Region.getInstanceXYWH(dstx, dsty, w, h);
        ret = ret.getIntersection(dst.getBounds());
        Rectangle r = src.getBounds();
        r.translate(dstx - srcx, dsty - srcy);
        ret = ret.getIntersection(r);
        if (clip != null) {
            ret = ret.getIntersection(clip);
        }
        return ret;
    }
}

