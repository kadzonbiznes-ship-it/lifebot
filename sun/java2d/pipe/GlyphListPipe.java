/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import sun.font.GlyphList;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.FontInfo;
import sun.java2d.pipe.TextPipe;

public abstract class GlyphListPipe
implements TextPipe {
    @Override
    public void drawString(SunGraphics2D sg2d, String s, double x, double y) {
        float devy;
        float devx;
        FontInfo info = sg2d.getFontInfo();
        if (info.nonInvertibleTx) {
            return;
        }
        if (info.pixelHeight > 100) {
            SurfaceData.outlineTextRenderer.drawString(sg2d, s, x, y);
            return;
        }
        if (sg2d.transformState >= 3) {
            double[] origin = new double[]{x + (double)info.originX, y + (double)info.originY};
            sg2d.transform.transform(origin, 0, origin, 0, 1);
            devx = (float)origin[0];
            devy = (float)origin[1];
        } else {
            devx = (float)(x + (double)info.originX + (double)sg2d.transX);
            devy = (float)(y + (double)info.originY + (double)sg2d.transY);
        }
        GlyphList gl = GlyphList.getInstance();
        if (gl.setFromString(info, s, devx, devy)) {
            this.drawGlyphList(sg2d, gl);
            gl.dispose();
        } else {
            gl.dispose();
            TextLayout tl = new TextLayout(s, sg2d.getFont(), sg2d.getFontRenderContext());
            tl.draw(sg2d, (float)x, (float)y);
        }
    }

    @Override
    public void drawChars(SunGraphics2D sg2d, char[] data, int offset, int length, int ix, int iy) {
        float y;
        float x;
        FontInfo info = sg2d.getFontInfo();
        if (info.nonInvertibleTx) {
            return;
        }
        if (info.pixelHeight > 100) {
            SurfaceData.outlineTextRenderer.drawChars(sg2d, data, offset, length, ix, iy);
            return;
        }
        if (sg2d.transformState >= 3) {
            double[] origin = new double[]{(float)ix + info.originX, (float)iy + info.originY};
            sg2d.transform.transform(origin, 0, origin, 0, 1);
            x = (float)origin[0];
            y = (float)origin[1];
        } else {
            x = (float)ix + info.originX + (float)sg2d.transX;
            y = (float)iy + info.originY + (float)sg2d.transY;
        }
        GlyphList gl = GlyphList.getInstance();
        if (gl.setFromChars(info, data, offset, length, x, y)) {
            this.drawGlyphList(sg2d, gl);
            gl.dispose();
        } else {
            gl.dispose();
            TextLayout tl = new TextLayout(new String(data, offset, length), sg2d.getFont(), sg2d.getFontRenderContext());
            tl.draw(sg2d, ix, iy);
        }
    }

    @Override
    public void drawGlyphVector(SunGraphics2D sg2d, GlyphVector gv, float x, float y) {
        FontRenderContext frc = gv.getFontRenderContext();
        FontInfo info = sg2d.getGVFontInfo(gv.getFont(), frc);
        if (info.nonInvertibleTx) {
            return;
        }
        if (info.pixelHeight > 100) {
            SurfaceData.outlineTextRenderer.drawGlyphVector(sg2d, gv, x, y);
            return;
        }
        if (sg2d.transformState >= 3) {
            double[] origin = new double[]{x, y};
            sg2d.transform.transform(origin, 0, origin, 0, 1);
            x = (float)origin[0];
            y = (float)origin[1];
        } else {
            x += (float)sg2d.transX;
            y += (float)sg2d.transY;
        }
        GlyphList gl = GlyphList.getInstance();
        gl.setFromGlyphVector(info, gv, x, y);
        this.drawGlyphList(sg2d, gl, info.aaHint);
        gl.dispose();
    }

    protected abstract void drawGlyphList(SunGraphics2D var1, GlyphList var2);

    protected void drawGlyphList(SunGraphics2D sg2d, GlyphList gl, int aaHint) {
        this.drawGlyphList(sg2d, gl);
    }
}

