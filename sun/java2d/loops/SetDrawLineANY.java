/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.DrawLine;
import sun.java2d.loops.GeneralRenderer;
import sun.java2d.loops.PixelWriter;
import sun.java2d.loops.SurfaceType;

class SetDrawLineANY
extends DrawLine {
    SetDrawLineANY() {
        super(SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any);
    }

    @Override
    public void DrawLine(SunGraphics2D sg2d, SurfaceData sData, int x1, int y1, int x2, int y2) {
        PixelWriter pw = GeneralRenderer.createSolidPixelWriter(sg2d, sData);
        if (y1 >= y2) {
            GeneralRenderer.doDrawLine(sData, pw, null, sg2d.getCompClip(), x2, y2, x1, y1);
        } else {
            GeneralRenderer.doDrawLine(sData, pw, null, sg2d.getCompClip(), x1, y1, x2, y2);
        }
    }
}

