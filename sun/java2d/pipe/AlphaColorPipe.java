/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.Rectangle;
import java.awt.Shape;
import sun.java2d.SunGraphics2D;
import sun.java2d.pipe.CompositePipe;
import sun.java2d.pipe.ParallelogramPipe;

public class AlphaColorPipe
implements CompositePipe,
ParallelogramPipe {
    @Override
    public Object startSequence(SunGraphics2D sg, Shape s, Rectangle dev, int[] abox) {
        return sg;
    }

    @Override
    public boolean needTile(Object context, int x, int y, int w, int h) {
        return true;
    }

    @Override
    public void renderPathTile(Object context, byte[] atile, int offset, int tilesize, int x, int y, int w, int h) {
        SunGraphics2D sg = (SunGraphics2D)context;
        sg.alphafill.MaskFill(sg, sg.getSurfaceData(), sg.composite, x, y, w, h, atile, offset, tilesize);
    }

    @Override
    public void skipTile(Object context, int x, int y) {
    }

    @Override
    public void endSequence(Object context) {
    }

    @Override
    public void fillParallelogram(SunGraphics2D sg, double ux1, double uy1, double ux2, double uy2, double x, double y, double dx1, double dy1, double dx2, double dy2) {
        sg.alphafill.FillAAPgram(sg, sg.getSurfaceData(), sg.composite, x, y, dx1, dy1, dx2, dy2);
    }

    @Override
    public void drawParallelogram(SunGraphics2D sg, double ux1, double uy1, double ux2, double uy2, double x, double y, double dx1, double dy1, double dx2, double dy2, double lw1, double lw2) {
        sg.alphafill.DrawAAPgram(sg, sg.getSurfaceData(), sg.composite, x, y, dx1, dy1, dx2, dy2, lw1, lw2);
    }
}

