/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import java.awt.geom.Path2D;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.FillPath;
import sun.java2d.loops.GeneralRenderer;
import sun.java2d.loops.PixelWriter;
import sun.java2d.loops.PixelWriterDrawHandler;
import sun.java2d.loops.ProcessPath;
import sun.java2d.loops.SurfaceType;

class SetFillPathANY
extends FillPath {
    SetFillPathANY() {
        super(SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any);
    }

    @Override
    public void FillPath(SunGraphics2D sg2d, SurfaceData sData, int transx, int transy, Path2D.Float p2df) {
        PixelWriter pw = GeneralRenderer.createSolidPixelWriter(sg2d, sData);
        ProcessPath.fillPath(new PixelWriterDrawHandler(sData, pw, sg2d.getCompClip(), sg2d.strokeHint), p2df, transx, transy);
    }
}

