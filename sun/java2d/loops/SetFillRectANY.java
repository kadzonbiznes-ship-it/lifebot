/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.FillRect;
import sun.java2d.loops.GeneralRenderer;
import sun.java2d.loops.PixelWriter;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;

class SetFillRectANY
extends FillRect {
    SetFillRectANY() {
        super(SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any);
    }

    @Override
    public void FillRect(SunGraphics2D sg2d, SurfaceData sData, int x, int y, int w, int h) {
        PixelWriter pw = GeneralRenderer.createSolidPixelWriter(sg2d, sData);
        Region r = sg2d.getCompClip().getBoundsIntersectionXYWH(x, y, w, h);
        GeneralRenderer.doSetRect(sData, pw, r.getLoX(), r.getLoY(), r.getHiX(), r.getHiY());
    }
}

