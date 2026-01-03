/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.FontFormatException;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import sun.font.Font2D;
import sun.font.Font2DHandle;
import sun.font.StrikeMetrics;

public abstract class PhysicalFont
extends Font2D {
    protected String platName;
    protected Object nativeNames;

    public boolean equals(Object o) {
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        PhysicalFont other = (PhysicalFont)o;
        return this.fullName.equals(other.fullName) && (this.platName == null && other.platName == null || this.platName != null && this.platName.equals(other.platName));
    }

    public int hashCode() {
        return this.fullName.hashCode() + (this.platName != null ? this.platName.hashCode() : 0);
    }

    PhysicalFont(String platname, Object nativeNames) throws FontFormatException {
        this.handle = new Font2DHandle(this);
        this.platName = platname;
        this.nativeNames = nativeNames;
    }

    protected PhysicalFont() {
        this.handle = new Font2DHandle(this);
    }

    Point2D.Float getGlyphPoint(long pScalerContext, int glyphCode, int ptNumber) {
        return new Point2D.Float();
    }

    abstract StrikeMetrics getFontMetrics(long var1);

    abstract float getGlyphAdvance(long var1, int var3);

    abstract void getGlyphMetrics(long var1, int var3, Point2D.Float var4);

    abstract long getGlyphImage(long var1, int var3);

    abstract Rectangle2D.Float getGlyphOutlineBounds(long var1, int var3);

    abstract GeneralPath getGlyphOutline(long var1, int var3, float var4, float var5);

    abstract GeneralPath getGlyphVectorOutline(long var1, int[] var3, int var4, float var5, float var6);
}

