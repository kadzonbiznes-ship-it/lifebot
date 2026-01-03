/*
 * Decompiled with CFR 0.152.
 */
package java.awt.font;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphJustificationInfo;
import java.awt.font.GlyphMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public abstract class GlyphVector
implements Cloneable {
    public static final int FLAG_HAS_TRANSFORMS = 1;
    public static final int FLAG_HAS_POSITION_ADJUSTMENTS = 2;
    public static final int FLAG_RUN_RTL = 4;
    public static final int FLAG_COMPLEX_GLYPHS = 8;
    public static final int FLAG_MASK = 15;

    protected GlyphVector() {
    }

    public abstract Font getFont();

    public abstract FontRenderContext getFontRenderContext();

    public abstract void performDefaultLayout();

    public abstract int getNumGlyphs();

    public abstract int getGlyphCode(int var1);

    public abstract int[] getGlyphCodes(int var1, int var2, int[] var3);

    public int getGlyphCharIndex(int glyphIndex) {
        return glyphIndex;
    }

    public int[] getGlyphCharIndices(int beginGlyphIndex, int numEntries, int[] codeReturn) {
        if (codeReturn == null) {
            codeReturn = new int[numEntries];
        }
        int i = 0;
        int j = beginGlyphIndex;
        while (i < numEntries) {
            codeReturn[i] = this.getGlyphCharIndex(j);
            ++i;
            ++j;
        }
        return codeReturn;
    }

    public abstract Rectangle2D getLogicalBounds();

    public abstract Rectangle2D getVisualBounds();

    public Rectangle getPixelBounds(FontRenderContext renderFRC, float x, float y) {
        Rectangle2D rect = this.getVisualBounds();
        int l = (int)Math.floor(rect.getX() + (double)x);
        int t = (int)Math.floor(rect.getY() + (double)y);
        int r = (int)Math.ceil(rect.getMaxX() + (double)x);
        int b = (int)Math.ceil(rect.getMaxY() + (double)y);
        return new Rectangle(l, t, r - l, b - t);
    }

    public abstract Shape getOutline();

    public abstract Shape getOutline(float var1, float var2);

    public abstract Shape getGlyphOutline(int var1);

    public Shape getGlyphOutline(int glyphIndex, float x, float y) {
        Shape s = this.getGlyphOutline(glyphIndex);
        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        return at.createTransformedShape(s);
    }

    public abstract Point2D getGlyphPosition(int var1);

    public abstract void setGlyphPosition(int var1, Point2D var2);

    public abstract AffineTransform getGlyphTransform(int var1);

    public abstract void setGlyphTransform(int var1, AffineTransform var2);

    public int getLayoutFlags() {
        return 0;
    }

    public abstract float[] getGlyphPositions(int var1, int var2, float[] var3);

    public abstract Shape getGlyphLogicalBounds(int var1);

    public abstract Shape getGlyphVisualBounds(int var1);

    public Rectangle getGlyphPixelBounds(int index, FontRenderContext renderFRC, float x, float y) {
        Rectangle2D rect = this.getGlyphVisualBounds(index).getBounds2D();
        int l = (int)Math.floor(rect.getX() + (double)x);
        int t = (int)Math.floor(rect.getY() + (double)y);
        int r = (int)Math.ceil(rect.getMaxX() + (double)x);
        int b = (int)Math.ceil(rect.getMaxY() + (double)y);
        return new Rectangle(l, t, r - l, b - t);
    }

    public abstract GlyphMetrics getGlyphMetrics(int var1);

    public abstract GlyphJustificationInfo getGlyphJustificationInfo(int var1);

    public abstract boolean equals(GlyphVector var1);
}

