/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import sun.java2d.SunGraphics2D;
import sun.java2d.pipe.TextPipe;

public class OutlineTextRenderer
implements TextPipe {
    public static final int THRESHHOLD = 100;

    @Override
    public void drawChars(SunGraphics2D g2d, char[] data, int offset, int length, int x, int y) {
        String s = new String(data, offset, length);
        this.drawString(g2d, s, x, y);
    }

    @Override
    public void drawString(SunGraphics2D g2d, String str, double x, double y) {
        if ("".equals(str)) {
            return;
        }
        TextLayout tl = new TextLayout(str, g2d.getFont(), g2d.getFontRenderContext());
        Shape s = tl.getOutline(AffineTransform.getTranslateInstance(x, y));
        int textAAHint = g2d.getFontInfo().aaHint;
        int prevaaHint = -1;
        if (textAAHint != 1 && g2d.antialiasHint != 2) {
            prevaaHint = g2d.antialiasHint;
            g2d.antialiasHint = 2;
            g2d.validatePipe();
        } else if (textAAHint == 1 && g2d.antialiasHint != 1) {
            prevaaHint = g2d.antialiasHint;
            g2d.antialiasHint = 1;
            g2d.validatePipe();
        }
        g2d.fill(s);
        if (prevaaHint != -1) {
            g2d.antialiasHint = prevaaHint;
            g2d.validatePipe();
        }
    }

    @Override
    public void drawGlyphVector(SunGraphics2D g2d, GlyphVector gv, float x, float y) {
        Shape s = gv.getOutline(x, y);
        int prevaaHint = -1;
        FontRenderContext frc = gv.getFontRenderContext();
        boolean aa = frc.isAntiAliased();
        if (aa && g2d.getGVFontInfo((Font)gv.getFont(), (FontRenderContext)frc).aaHint == 1) {
            aa = false;
        }
        if (aa && g2d.antialiasHint != 2) {
            prevaaHint = g2d.antialiasHint;
            g2d.antialiasHint = 2;
            g2d.validatePipe();
        } else if (!aa && g2d.antialiasHint != 1) {
            prevaaHint = g2d.antialiasHint;
            g2d.antialiasHint = 1;
            g2d.validatePipe();
        }
        g2d.fill(s);
        if (prevaaHint != -1) {
            g2d.antialiasHint = prevaaHint;
            g2d.validatePipe();
        }
    }
}

