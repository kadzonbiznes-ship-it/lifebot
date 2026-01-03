/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.DrawRect;
import sun.java2d.loops.GeneralRenderer;
import sun.java2d.loops.PixelWriter;
import sun.java2d.loops.SurfaceType;

class SetDrawRectANY
extends DrawRect {
    SetDrawRectANY() {
        super(SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any);
    }

    @Override
    public void DrawRect(SunGraphics2D sg2d, SurfaceData sData, int x, int y, int w, int h) {
        PixelWriter pw = GeneralRenderer.createSolidPixelWriter(sg2d, sData);
        GeneralRenderer.doDrawRect(pw, sg2d, sData, x, y, w, h);
    }
}

