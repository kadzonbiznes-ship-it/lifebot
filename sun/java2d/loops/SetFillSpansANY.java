/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.FillSpans;
import sun.java2d.loops.GeneralRenderer;
import sun.java2d.loops.PixelWriter;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.SpanIterator;

class SetFillSpansANY
extends FillSpans {
    SetFillSpansANY() {
        super(SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any);
    }

    @Override
    public void FillSpans(SunGraphics2D sg2d, SurfaceData sData, SpanIterator si) {
        PixelWriter pw = GeneralRenderer.createSolidPixelWriter(sg2d, sData);
        int[] span = new int[4];
        while (si.nextSpan(span)) {
            GeneralRenderer.doSetRect(sData, pw, span[0], span[1], span[2], span[3]);
        }
    }
}

