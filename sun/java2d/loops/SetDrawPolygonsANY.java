/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.DrawPolygons;
import sun.java2d.loops.GeneralRenderer;
import sun.java2d.loops.PixelWriter;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.Region;

class SetDrawPolygonsANY
extends DrawPolygons {
    SetDrawPolygonsANY() {
        super(SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any);
    }

    @Override
    public void DrawPolygons(SunGraphics2D sg2d, SurfaceData sData, int[] xPoints, int[] yPoints, int[] nPoints, int numPolys, int transx, int transy, boolean close) {
        PixelWriter pw = GeneralRenderer.createSolidPixelWriter(sg2d, sData);
        int off = 0;
        Region clip = sg2d.getCompClip();
        for (int i = 0; i < numPolys; ++i) {
            int numpts = nPoints[i];
            GeneralRenderer.doDrawPoly(sData, pw, xPoints, yPoints, off, numpts, clip, transx, transy, close);
            off += numpts;
        }
    }
}

