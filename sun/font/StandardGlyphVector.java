/*
 * Decompiled with CFR 0.152.
 */
package sun.font;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphJustificationInfo;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.SoftReference;
import java.text.CharacterIterator;
import sun.font.AttributeMap;
import sun.font.AttributeValues;
import sun.font.DelegatingShape;
import sun.font.Font2D;
import sun.font.FontStrike;
import sun.font.FontStrikeDesc;
import sun.font.FontSubstitution;
import sun.font.FontUtilities;
import sun.font.StrikeMetrics;
import sun.java2d.loops.FontInfo;

public class StandardGlyphVector
extends GlyphVector {
    private Font font;
    private FontRenderContext frc;
    private int[] glyphs;
    private int[] userGlyphs;
    private float[] positions;
    private int[] charIndices;
    private int flags;
    private static final int UNINITIALIZED_FLAGS = -1;
    private GlyphTransformInfo gti;
    private AffineTransform ftx;
    private AffineTransform dtx;
    private AffineTransform invdtx;
    private AffineTransform frctx;
    private Font2D font2D;
    private SoftReference<GlyphStrike> fsref;
    private SoftReference<Shape[]> lbcacheRef;
    private SoftReference<Shape[]> vbcacheRef;
    public static final int FLAG_USES_VERTICAL_BASELINE = 128;
    public static final int FLAG_USES_VERTICAL_METRICS = 256;
    public static final int FLAG_USES_ALTERNATE_ORIENTATION = 512;

    public StandardGlyphVector(Font font, String str, FontRenderContext frc) {
        this.init(font, str.toCharArray(), 0, str.length(), frc, -1);
    }

    public StandardGlyphVector(Font font, char[] text, FontRenderContext frc) {
        this.init(font, text, 0, text.length, frc, -1);
    }

    public StandardGlyphVector(Font font, char[] text, int start, int count, FontRenderContext frc) {
        this.init(font, text, start, count, frc, -1);
    }

    private float getTracking(Font font) {
        if (font.hasLayoutAttributes()) {
            AttributeValues values = ((AttributeMap)font.getAttributes()).getValues();
            return values.getTracking();
        }
        return 0.0f;
    }

    public StandardGlyphVector(Font font, FontRenderContext frc, int[] glyphs, float[] positions, int[] indices, int flags) {
        this.initGlyphVector(font, frc, glyphs, positions, indices, flags);
        float track = this.getTracking(font);
        if (track != 0.0f) {
            Point2D.Float trackPt = new Point2D.Float(track *= font.getSize2D(), 0.0f);
            if (font.isTransformed()) {
                AffineTransform at = font.getTransform();
                at.deltaTransform(trackPt, trackPt);
            }
            float[] deltas = new float[]{trackPt.x, trackPt.y};
            for (int j = 0; j < deltas.length; ++j) {
                float inc = deltas[j];
                float prevPos = 0.0f;
                if (inc == 0.0f) continue;
                float delta = 0.0f;
                for (int i = j; i < positions.length; i += 2) {
                    if (i == j || prevPos != positions[i]) {
                        prevPos = positions[i];
                        int n = i;
                        positions[n] = positions[n] + delta;
                        delta += inc;
                        continue;
                    }
                    if (prevPos != positions[i]) continue;
                    positions[i] = positions[i - 2];
                }
            }
        }
    }

    public void initGlyphVector(Font font, FontRenderContext frc, int[] glyphs, float[] positions, int[] indices, int flags) {
        this.font = font;
        this.frc = frc;
        this.glyphs = glyphs;
        this.userGlyphs = glyphs;
        this.positions = positions;
        this.charIndices = indices;
        this.flags = flags;
        this.initFontData();
    }

    public StandardGlyphVector(Font font, CharacterIterator iter, FontRenderContext frc) {
        int offset = iter.getBeginIndex();
        char[] text = new char[iter.getEndIndex() - offset];
        char c = iter.first();
        while (c != '\uffff') {
            text[iter.getIndex() - offset] = c;
            c = iter.next();
        }
        this.init(font, text, 0, text.length, frc, -1);
    }

    public StandardGlyphVector(Font font, int[] glyphs, FontRenderContext frc) {
        this.font = font;
        this.frc = frc;
        this.flags = -1;
        this.initFontData();
        this.userGlyphs = glyphs;
        this.glyphs = this.getValidatedGlyphs(this.userGlyphs);
    }

    public static StandardGlyphVector getStandardGV(GlyphVector gv, FontInfo info) {
        Object aaHint;
        if (info.aaHint == 2 && (aaHint = gv.getFontRenderContext().getAntiAliasingHint()) != RenderingHints.VALUE_TEXT_ANTIALIAS_ON && aaHint != RenderingHints.VALUE_TEXT_ANTIALIAS_GASP) {
            FontRenderContext frc = gv.getFontRenderContext();
            frc = new FontRenderContext(frc.getTransform(), RenderingHints.VALUE_TEXT_ANTIALIAS_ON, frc.getFractionalMetricsHint());
            return new StandardGlyphVector(gv, frc);
        }
        if (gv instanceof StandardGlyphVector) {
            return (StandardGlyphVector)gv;
        }
        return new StandardGlyphVector(gv, gv.getFontRenderContext());
    }

    @Override
    public Font getFont() {
        return this.font;
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return this.frc;
    }

    @Override
    public void performDefaultLayout() {
        this.positions = null;
        if (this.getTracking(this.font) == 0.0f) {
            this.clearFlags(2);
        }
    }

    @Override
    public int getNumGlyphs() {
        return this.glyphs.length;
    }

    @Override
    public int getGlyphCode(int glyphIndex) {
        return this.userGlyphs[glyphIndex];
    }

    @Override
    public int[] getGlyphCodes(int start, int count, int[] result) {
        if (count < 0) {
            throw new IllegalArgumentException("count = " + count);
        }
        if (start < 0) {
            throw new IndexOutOfBoundsException("start = " + start);
        }
        if (start > this.glyphs.length - count) {
            throw new IndexOutOfBoundsException("start + count = " + (start + count));
        }
        if (result == null) {
            result = new int[count];
        }
        for (int i = 0; i < count; ++i) {
            result[i] = this.userGlyphs[i + start];
        }
        return result;
    }

    @Override
    public int getGlyphCharIndex(int ix) {
        if (ix < 0 && ix >= this.glyphs.length) {
            throw new IndexOutOfBoundsException("" + ix);
        }
        if (this.charIndices == null) {
            if ((this.getLayoutFlags() & 4) != 0) {
                return this.glyphs.length - 1 - ix;
            }
            return ix;
        }
        return this.charIndices[ix];
    }

    @Override
    public int[] getGlyphCharIndices(int start, int count, int[] result) {
        if (start < 0 || count < 0 || count > this.glyphs.length - start) {
            throw new IndexOutOfBoundsException(start + ", " + count);
        }
        if (result == null) {
            result = new int[count];
        }
        if (this.charIndices == null) {
            if ((this.getLayoutFlags() & 4) != 0) {
                int n = this.glyphs.length - 1 - start;
                for (int i = 0; i < count; ++i) {
                    result[i] = n--;
                }
            } else {
                int n = start;
                for (int i = 0; i < count; ++i) {
                    result[i] = n++;
                }
            }
        } else {
            for (int i = 0; i < count; ++i) {
                result[i] = this.charIndices[i + start];
            }
        }
        return result;
    }

    @Override
    public Rectangle2D getLogicalBounds() {
        this.setFRCTX();
        this.initPositions();
        LineMetrics lm = this.font.getLineMetrics("", this.frc);
        float minX = 0.0f;
        float minY = -lm.getAscent();
        float maxX = 0.0f;
        float maxY = lm.getDescent() + lm.getLeading();
        if (this.glyphs.length > 0) {
            maxX = this.positions[this.positions.length - 2];
        }
        return new Rectangle2D.Float(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public Rectangle2D getVisualBounds() {
        Rectangle2D result = null;
        for (int i = 0; i < this.glyphs.length; ++i) {
            Rectangle2D glyphVB = this.getGlyphVisualBounds(i).getBounds2D();
            if (glyphVB.isEmpty()) continue;
            if (result == null) {
                result = glyphVB;
                continue;
            }
            Rectangle2D.union(result, glyphVB, result);
        }
        if (result == null) {
            result = new Rectangle2D.Float(0.0f, 0.0f, 0.0f, 0.0f);
        }
        return result;
    }

    @Override
    public Rectangle getPixelBounds(FontRenderContext renderFRC, float x, float y) {
        return this.getGlyphsPixelBounds(renderFRC, x, y, 0, this.glyphs.length);
    }

    @Override
    public Shape getOutline() {
        return this.getGlyphsOutline(0, this.glyphs.length, 0.0f, 0.0f);
    }

    @Override
    public Shape getOutline(float x, float y) {
        return this.getGlyphsOutline(0, this.glyphs.length, x, y);
    }

    @Override
    public Shape getGlyphOutline(int ix) {
        return this.getGlyphsOutline(ix, 1, 0.0f, 0.0f);
    }

    @Override
    public Shape getGlyphOutline(int ix, float x, float y) {
        return this.getGlyphsOutline(ix, 1, x, y);
    }

    @Override
    public Point2D getGlyphPosition(int ix) {
        this.initPositions();
        return new Point2D.Float(this.positions[ix *= 2], this.positions[ix + 1]);
    }

    @Override
    public void setGlyphPosition(int ix, Point2D pos) {
        if (ix < 0 || ix > this.glyphs.length) {
            throw new IndexOutOfBoundsException("ix = " + ix);
        }
        this.initPositions();
        int ix2 = ix << 1;
        this.positions[ix2] = (float)pos.getX();
        this.positions[ix2 + 1] = (float)pos.getY();
        if (ix < this.glyphs.length) {
            this.clearCaches(ix);
        }
        this.addFlags(2);
    }

    @Override
    public AffineTransform getGlyphTransform(int ix) {
        if (ix < 0 || ix >= this.glyphs.length) {
            throw new IndexOutOfBoundsException("ix = " + ix);
        }
        if (this.gti != null) {
            return this.gti.getGlyphTransform(ix);
        }
        return null;
    }

    @Override
    public void setGlyphTransform(int ix, AffineTransform newTX) {
        if (ix < 0 || ix >= this.glyphs.length) {
            throw new IndexOutOfBoundsException("ix = " + ix);
        }
        if (this.gti == null) {
            if (newTX == null || newTX.isIdentity()) {
                return;
            }
            this.gti = new GlyphTransformInfo(this);
        }
        this.gti.setGlyphTransform(ix, newTX);
        if (this.gti.transformCount() == 0) {
            this.gti = null;
        }
    }

    @Override
    public int getLayoutFlags() {
        if (this.flags == -1) {
            this.flags = 0;
            if (this.charIndices != null && this.glyphs.length > 1) {
                boolean ltr = true;
                boolean rtl = true;
                int rtlix = this.charIndices.length;
                for (int i = 0; i < this.charIndices.length && (ltr || rtl); ++i) {
                    int cx = this.charIndices[i];
                    ltr = ltr && cx == i;
                    rtl = rtl && cx == --rtlix;
                }
                if (rtl) {
                    this.flags |= 4;
                }
                if (!rtl && !ltr) {
                    this.flags |= 8;
                }
            }
        }
        return this.flags;
    }

    @Override
    public float[] getGlyphPositions(int start, int count, float[] result) {
        if (count < 0) {
            throw new IllegalArgumentException("count = " + count);
        }
        if (start < 0) {
            throw new IndexOutOfBoundsException("start = " + start);
        }
        if (start > this.glyphs.length + 1 - count) {
            throw new IndexOutOfBoundsException("start + count = " + (start + count));
        }
        return this.internalGetGlyphPositions(start, count, 0, result);
    }

    @Override
    public Shape getGlyphLogicalBounds(int ix) {
        Shape result;
        Shape[] lbcache;
        if (ix < 0 || ix >= this.glyphs.length) {
            throw new IndexOutOfBoundsException("ix = " + ix);
        }
        if (this.lbcacheRef == null || (lbcache = this.lbcacheRef.get()) == null) {
            lbcache = new Shape[this.glyphs.length];
            this.lbcacheRef = new SoftReference<Shape[]>(lbcache);
        }
        if ((result = lbcache[ix]) == null) {
            this.setFRCTX();
            this.initPositions();
            ADL adl = new ADL();
            GlyphStrike gs = this.getGlyphStrike(ix);
            gs.getADL(adl);
            Point2D.Float adv = gs.strike.getGlyphMetrics(this.glyphs[ix]);
            float wx = adv.x;
            float wy = adv.y;
            float hx = adl.descentX + adl.leadingX + adl.ascentX;
            float hy = adl.descentY + adl.leadingY + adl.ascentY;
            float x = this.positions[ix * 2] + gs.dx - adl.ascentX;
            float y = this.positions[ix * 2 + 1] + gs.dy - adl.ascentY;
            GeneralPath gp = new GeneralPath();
            gp.moveTo(x, y);
            gp.lineTo(x + wx, y + wy);
            gp.lineTo(x + wx + hx, y + wy + hy);
            gp.lineTo(x + hx, y + hy);
            gp.closePath();
            lbcache[ix] = result = new DelegatingShape(gp);
        }
        return result;
    }

    @Override
    public Shape getGlyphVisualBounds(int ix) {
        Shape result;
        Shape[] vbcache;
        if (ix < 0 || ix >= this.glyphs.length) {
            throw new IndexOutOfBoundsException("ix = " + ix);
        }
        if (this.vbcacheRef == null || (vbcache = this.vbcacheRef.get()) == null) {
            vbcache = new Shape[this.glyphs.length];
            this.vbcacheRef = new SoftReference<Shape[]>(vbcache);
        }
        if ((result = vbcache[ix]) == null) {
            vbcache[ix] = result = new DelegatingShape(this.getGlyphOutlineBounds(ix));
        }
        return result;
    }

    @Override
    public Rectangle getGlyphPixelBounds(int index, FontRenderContext renderFRC, float x, float y) {
        return this.getGlyphsPixelBounds(renderFRC, x, y, index, 1);
    }

    @Override
    public GlyphMetrics getGlyphMetrics(int ix) {
        if (ix < 0 || ix >= this.glyphs.length) {
            throw new IndexOutOfBoundsException("ix = " + ix);
        }
        Rectangle2D vb = this.getGlyphVisualBounds(ix).getBounds2D();
        Point2D pt = this.getGlyphPosition(ix);
        vb.setRect(vb.getMinX() - pt.getX(), vb.getMinY() - pt.getY(), vb.getWidth(), vb.getHeight());
        Point2D.Float adv = this.getGlyphStrike((int)ix).strike.getGlyphMetrics(this.glyphs[ix]);
        GlyphMetrics gm = new GlyphMetrics(true, adv.x, adv.y, vb, 0);
        return gm;
    }

    @Override
    public GlyphJustificationInfo getGlyphJustificationInfo(int ix) {
        if (ix < 0 || ix >= this.glyphs.length) {
            throw new IndexOutOfBoundsException("ix = " + ix);
        }
        return null;
    }

    @Override
    public boolean equals(GlyphVector rhs) {
        if (this == rhs) {
            return true;
        }
        if (rhs == null) {
            return false;
        }
        try {
            int i;
            StandardGlyphVector other = (StandardGlyphVector)rhs;
            if (this.glyphs.length != other.glyphs.length) {
                return false;
            }
            for (i = 0; i < this.glyphs.length; ++i) {
                if (this.glyphs[i] == other.glyphs[i]) continue;
                return false;
            }
            if (!this.font.equals(other.font)) {
                return false;
            }
            if (!this.frc.equals(other.frc)) {
                return false;
            }
            if (other.positions == null != (this.positions == null)) {
                if (this.positions == null) {
                    this.initPositions();
                } else {
                    other.initPositions();
                }
            }
            if (this.positions != null) {
                for (i = 0; i < this.positions.length; ++i) {
                    if (this.positions[i] == other.positions[i]) continue;
                    return false;
                }
            }
            if (this.gti == null) {
                return other.gti == null;
            }
            return this.gti.equals(other.gti);
        }
        catch (ClassCastException e) {
            return false;
        }
    }

    public int hashCode() {
        return this.font.hashCode() ^ this.glyphs.length;
    }

    public boolean equals(Object rhs) {
        try {
            return this.equals((GlyphVector)rhs);
        }
        catch (ClassCastException e) {
            return false;
        }
    }

    public StandardGlyphVector copy() {
        return (StandardGlyphVector)this.clone();
    }

    public Object clone() {
        try {
            StandardGlyphVector result = (StandardGlyphVector)super.clone();
            result.clearCaches();
            if (this.positions != null) {
                result.positions = (float[])this.positions.clone();
            }
            if (this.gti != null) {
                result.gti = new GlyphTransformInfo(result, this.gti);
            }
            return result;
        }
        catch (CloneNotSupportedException cloneNotSupportedException) {
            return this;
        }
    }

    public void setGlyphPositions(float[] srcPositions, int srcStart, int start, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count = " + count);
        }
        this.initPositions();
        int i = start * 2;
        int e = i + count * 2;
        int p = srcStart;
        while (i < e) {
            this.positions[i] = srcPositions[p];
            ++i;
            ++p;
        }
        this.clearCaches();
        this.addFlags(2);
    }

    public void setGlyphPositions(float[] srcPositions) {
        int requiredLength = this.glyphs.length * 2 + 2;
        if (srcPositions.length != requiredLength) {
            throw new IllegalArgumentException("srcPositions.length != " + requiredLength);
        }
        this.positions = (float[])srcPositions.clone();
        this.clearCaches();
        this.addFlags(2);
    }

    public float[] getGlyphPositions(float[] result) {
        return this.internalGetGlyphPositions(0, this.glyphs.length + 1, 0, result);
    }

    public AffineTransform[] getGlyphTransforms(int start, int count, AffineTransform[] result) {
        if (start < 0 || count < 0 || start + count > this.glyphs.length) {
            throw new IllegalArgumentException("start: " + start + " count: " + count);
        }
        if (this.gti == null) {
            return null;
        }
        if (result == null) {
            result = new AffineTransform[count];
        }
        int i = 0;
        while (i < count) {
            result[i] = this.gti.getGlyphTransform(start);
            ++i;
            ++start;
        }
        return result;
    }

    public AffineTransform[] getGlyphTransforms() {
        return this.getGlyphTransforms(0, this.glyphs.length, null);
    }

    public void setGlyphTransforms(AffineTransform[] srcTransforms, int srcStart, int start, int count) {
        int e = start + count;
        for (int i = start; i < e; ++i) {
            this.setGlyphTransform(i, srcTransforms[srcStart + i]);
        }
    }

    public void setGlyphTransforms(AffineTransform[] srcTransforms) {
        this.setGlyphTransforms(srcTransforms, 0, 0, this.glyphs.length);
    }

    public float[] getGlyphInfo() {
        this.setFRCTX();
        this.initPositions();
        float[] result = new float[this.glyphs.length * 8];
        int i = 0;
        int n = 0;
        while (i < this.glyphs.length) {
            float x = this.positions[i * 2];
            float y = this.positions[i * 2 + 1];
            result[n] = x;
            result[n + 1] = y;
            int glyphID = this.glyphs[i];
            GlyphStrike s = this.getGlyphStrike(i);
            Point2D.Float adv = s.strike.getGlyphMetrics(glyphID);
            result[n + 2] = adv.x;
            result[n + 3] = adv.y;
            Rectangle2D vb = this.getGlyphVisualBounds(i).getBounds2D();
            result[n + 4] = (float)vb.getMinX();
            result[n + 5] = (float)vb.getMinY();
            result[n + 6] = (float)vb.getWidth();
            result[n + 7] = (float)vb.getHeight();
            ++i;
            n += 8;
        }
        return result;
    }

    boolean needsPositions(double[] devTX) {
        return this.gti != null || (this.getLayoutFlags() & 2) != 0 || !StandardGlyphVector.matchTX(devTX, this.frctx);
    }

    Object setupGlyphImages(long[] images, float[] positions, double[] devTX) {
        this.initPositions();
        this.setRenderTransform(devTX);
        if (this.gti != null) {
            return this.gti.setupGlyphImages(images, positions, this.dtx);
        }
        GlyphStrike gs = this.getDefaultStrike();
        gs.strike.getGlyphImagePtrs(this.glyphs, images, this.glyphs.length);
        if (positions != null) {
            if (this.dtx.isIdentity()) {
                System.arraycopy(this.positions, 0, positions, 0, this.glyphs.length * 2);
            } else {
                this.dtx.transform(this.positions, 0, positions, 0, this.glyphs.length);
            }
        }
        return gs;
    }

    private static boolean matchTX(double[] lhs, AffineTransform rhs) {
        return lhs[0] == rhs.getScaleX() && lhs[1] == rhs.getShearY() && lhs[2] == rhs.getShearX() && lhs[3] == rhs.getScaleY();
    }

    private static AffineTransform getNonTranslateTX(AffineTransform tx) {
        if (tx.getTranslateX() != 0.0 || tx.getTranslateY() != 0.0) {
            tx = new AffineTransform(tx.getScaleX(), tx.getShearY(), tx.getShearX(), tx.getScaleY(), 0.0, 0.0);
        }
        return tx;
    }

    private static boolean equalNonTranslateTX(AffineTransform lhs, AffineTransform rhs) {
        return lhs.getScaleX() == rhs.getScaleX() && lhs.getShearY() == rhs.getShearY() && lhs.getShearX() == rhs.getShearX() && lhs.getScaleY() == rhs.getScaleY();
    }

    private void setRenderTransform(double[] devTX) {
        assert (devTX.length == 4);
        if (!StandardGlyphVector.matchTX(devTX, this.dtx)) {
            this.resetDTX(new AffineTransform(devTX));
        }
    }

    private void setDTX(AffineTransform tx) {
        if (!StandardGlyphVector.equalNonTranslateTX(this.dtx, tx)) {
            this.resetDTX(StandardGlyphVector.getNonTranslateTX(tx));
        }
    }

    private void setFRCTX() {
        if (!StandardGlyphVector.equalNonTranslateTX(this.frctx, this.dtx)) {
            this.resetDTX(StandardGlyphVector.getNonTranslateTX(this.frctx));
        }
    }

    private void resetDTX(AffineTransform at) {
        this.fsref = null;
        this.dtx = at;
        this.invdtx = null;
        if (!this.dtx.isIdentity()) {
            try {
                this.invdtx = this.dtx.createInverse();
            }
            catch (NoninvertibleTransformException noninvertibleTransformException) {
                // empty catch block
            }
        }
        if (this.gti != null) {
            this.gti.strikesRef = null;
        }
    }

    private StandardGlyphVector(GlyphVector gv, FontRenderContext frc) {
        this.font = gv.getFont();
        this.frc = frc;
        this.initFontData();
        int nGlyphs = gv.getNumGlyphs();
        this.userGlyphs = gv.getGlyphCodes(0, nGlyphs, null);
        this.glyphs = gv instanceof StandardGlyphVector ? this.userGlyphs : this.getValidatedGlyphs(this.userGlyphs);
        this.flags = gv.getLayoutFlags() & 0xF;
        if ((this.flags & 2) != 0) {
            this.positions = gv.getGlyphPositions(0, nGlyphs + 1, null);
        }
        if ((this.flags & 8) != 0) {
            this.charIndices = gv.getGlyphCharIndices(0, nGlyphs, null);
        }
        if ((this.flags & 1) != 0) {
            AffineTransform[] txs = new AffineTransform[nGlyphs];
            for (int i = 0; i < nGlyphs; ++i) {
                txs[i] = gv.getGlyphTransform(i);
            }
            this.setGlyphTransforms(txs);
        }
    }

    int[] getValidatedGlyphs(int[] oglyphs) {
        int len = oglyphs.length;
        int[] vglyphs = new int[len];
        for (int i = 0; i < len; ++i) {
            vglyphs[i] = oglyphs[i] == 65534 || oglyphs[i] == 65535 ? oglyphs[i] : this.font2D.getValidatedGlyphCode(oglyphs[i]);
        }
        return vglyphs;
    }

    private void init(Font font, char[] text, int start, int count, FontRenderContext frc, int flags) {
        if (start < 0 || count < 0 || start + count > text.length) {
            throw new ArrayIndexOutOfBoundsException("start or count out of bounds");
        }
        this.font = font;
        this.frc = frc;
        this.flags = flags;
        if (this.getTracking(font) != 0.0f) {
            this.addFlags(2);
        }
        if (start != 0) {
            char[] temp = new char[count];
            System.arraycopy(text, start, temp, 0, count);
            text = temp;
        }
        this.initFontData();
        this.glyphs = new int[count];
        this.userGlyphs = this.glyphs;
        this.font2D.getMapper().charsToGlyphs(count, text, this.glyphs);
    }

    private void initFontData() {
        this.font2D = FontUtilities.getFont2D(this.font);
        if (this.font2D instanceof FontSubstitution) {
            this.font2D = ((FontSubstitution)((Object)this.font2D)).getCompositeFont2D();
        }
        float s = this.font.getSize2D();
        if (this.font.isTransformed()) {
            this.ftx = this.font.getTransform();
            if (this.ftx.getTranslateX() != 0.0 || this.ftx.getTranslateY() != 0.0) {
                this.addFlags(2);
            }
            this.ftx.setTransform(this.ftx.getScaleX(), this.ftx.getShearY(), this.ftx.getShearX(), this.ftx.getScaleY(), 0.0, 0.0);
            this.ftx.scale(s, s);
        } else {
            this.ftx = AffineTransform.getScaleInstance(s, s);
        }
        this.frctx = this.frc.getTransform();
        this.resetDTX(StandardGlyphVector.getNonTranslateTX(this.frctx));
    }

    private float[] internalGetGlyphPositions(int start, int count, int offset, float[] result) {
        if (result == null) {
            result = new float[offset + count * 2];
        }
        this.initPositions();
        int i = offset;
        int e = offset + count * 2;
        int p = start * 2;
        while (i < e) {
            result[i] = this.positions[p];
            ++i;
            ++p;
        }
        return result;
    }

    private Rectangle2D getGlyphOutlineBounds(int ix) {
        this.setFRCTX();
        this.initPositions();
        return this.getGlyphStrike(ix).getGlyphOutlineBounds(this.glyphs[ix], this.positions[ix * 2], this.positions[ix * 2 + 1]);
    }

    private Shape getGlyphsOutline(int start, int count, float x, float y) {
        this.setFRCTX();
        this.initPositions();
        GeneralPath result = new GeneralPath(1);
        int i = start;
        int e = start + count;
        int n = start * 2;
        while (i < e) {
            float px = x + this.positions[n];
            float py = y + this.positions[n + 1];
            this.getGlyphStrike(i).appendGlyphOutline(this.glyphs[i], result, px, py);
            ++i;
            n += 2;
        }
        return result;
    }

    private Rectangle getGlyphsPixelBounds(FontRenderContext frc, float x, float y, int start, int count) {
        this.initPositions();
        AffineTransform tx = null;
        tx = frc == null || frc.equals(this.frc) ? this.frctx : frc.getTransform();
        this.setDTX(tx);
        if (this.gti != null) {
            return this.gti.getGlyphsPixelBounds(tx, x, y, start, count);
        }
        FontStrike fs = this.getDefaultStrike().strike;
        Rectangle result = null;
        Rectangle r = new Rectangle();
        Point2D.Float pt = new Point2D.Float();
        int n = start * 2;
        while (--count >= 0) {
            pt.x = x + this.positions[n++];
            pt.y = y + this.positions[n++];
            tx.transform(pt, pt);
            fs.getGlyphImageBounds(this.glyphs[start++], pt, r);
            if (r.isEmpty()) continue;
            if (result == null) {
                result = new Rectangle(r);
                continue;
            }
            result.add(r);
        }
        return result != null ? result : r;
    }

    private void clearCaches(int ix) {
        Shape[] vbcache;
        Shape[] lbcache;
        if (this.lbcacheRef != null && (lbcache = this.lbcacheRef.get()) != null) {
            lbcache[ix] = null;
        }
        if (this.vbcacheRef != null && (vbcache = this.vbcacheRef.get()) != null) {
            vbcache[ix] = null;
        }
    }

    private void clearCaches() {
        this.lbcacheRef = null;
        this.vbcacheRef = null;
    }

    private void initPositions() {
        if (this.positions == null) {
            this.setFRCTX();
            this.positions = new float[this.glyphs.length * 2 + 2];
            Point2D.Float trackPt = null;
            float track = this.getTracking(this.font);
            if (track != 0.0f) {
                trackPt = new Point2D.Float(track *= this.font.getSize2D(), 0.0f);
            }
            Point2D.Float pt = new Point2D.Float(0.0f, 0.0f);
            if (this.font.isTransformed()) {
                AffineTransform at = this.font.getTransform();
                at.transform(pt, pt);
                this.positions[0] = pt.x;
                this.positions[1] = pt.y;
                if (trackPt != null) {
                    at.deltaTransform(trackPt, trackPt);
                }
            }
            int i = 0;
            int n = 2;
            while (i < this.glyphs.length) {
                this.getGlyphStrike(i).addDefaultGlyphAdvance(this.glyphs[i], pt);
                if (trackPt != null) {
                    pt.x += trackPt.x;
                    pt.y += trackPt.y;
                }
                this.positions[n] = pt.x;
                this.positions[n + 1] = pt.y;
                ++i;
                n += 2;
            }
        }
    }

    private void addFlags(int newflags) {
        this.flags = this.getLayoutFlags() | newflags;
    }

    private void clearFlags(int clearedFlags) {
        this.flags = this.getLayoutFlags() & ~clearedFlags;
    }

    private GlyphStrike getGlyphStrike(int ix) {
        if (this.gti == null) {
            return this.getDefaultStrike();
        }
        return this.gti.getStrike(ix);
    }

    private GlyphStrike getDefaultStrike() {
        GlyphStrike gs = null;
        if (this.fsref != null) {
            gs = this.fsref.get();
        }
        if (gs == null) {
            gs = GlyphStrike.create(this, this.dtx, null);
            this.fsref = new SoftReference<GlyphStrike>(gs);
        }
        return gs;
    }

    public String toString() {
        return this.appendString(null).toString();
    }

    StringBuffer appendString(StringBuffer buf) {
        if (buf == null) {
            buf = new StringBuffer();
        }
        try {
            int i;
            buf.append("SGV{font: ");
            buf.append(this.font.toString());
            buf.append(", frc: ");
            buf.append(this.frc.toString());
            buf.append(", glyphs: (");
            buf.append(this.glyphs.length);
            buf.append(")[");
            for (i = 0; i < this.glyphs.length; ++i) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(Integer.toHexString(this.glyphs[i]));
            }
            buf.append("]");
            if (this.positions != null) {
                buf.append(", positions: (");
                buf.append(this.positions.length);
                buf.append(")[");
                for (i = 0; i < this.positions.length; i += 2) {
                    if (i > 0) {
                        buf.append(", ");
                    }
                    buf.append(this.positions[i]);
                    buf.append("@");
                    buf.append(this.positions[i + 1]);
                }
                buf.append("]");
            }
            if (this.charIndices != null) {
                buf.append(", indices: (");
                buf.append(this.charIndices.length);
                buf.append(")[");
                for (i = 0; i < this.charIndices.length; ++i) {
                    if (i > 0) {
                        buf.append(", ");
                    }
                    buf.append(this.charIndices[i]);
                }
                buf.append("]");
            }
            buf.append(", flags:");
            if (this.getLayoutFlags() == 0) {
                buf.append(" default");
            } else {
                if ((this.flags & 1) != 0) {
                    buf.append(" tx");
                }
                if ((this.flags & 2) != 0) {
                    buf.append(" pos");
                }
                if ((this.flags & 4) != 0) {
                    buf.append(" rtl");
                }
                if ((this.flags & 8) != 0) {
                    buf.append(" complex");
                }
            }
        }
        catch (Exception e) {
            buf.append(' ').append(e.getMessage());
        }
        buf.append('}');
        return buf;
    }

    static final class GlyphTransformInfo {
        StandardGlyphVector sgv;
        int[] indices;
        double[] transforms;
        SoftReference<GlyphStrike[]> strikesRef;
        boolean haveAllStrikes;

        GlyphTransformInfo(StandardGlyphVector sgv) {
            this.sgv = sgv;
        }

        GlyphTransformInfo(StandardGlyphVector sgv, GlyphTransformInfo rhs) {
            this.sgv = sgv;
            this.indices = rhs.indices == null ? null : (int[])rhs.indices.clone();
            this.transforms = rhs.transforms == null ? null : (double[])rhs.transforms.clone();
            this.strikesRef = null;
        }

        public boolean equals(GlyphTransformInfo rhs) {
            if (rhs == null) {
                return false;
            }
            if (rhs == this) {
                return true;
            }
            if (this.indices.length != rhs.indices.length) {
                return false;
            }
            if (this.transforms.length != rhs.transforms.length) {
                return false;
            }
            for (int i = 0; i < this.indices.length; ++i) {
                int rix;
                int tix = this.indices[i];
                if (tix == 0 != ((rix = rhs.indices[i]) == 0)) {
                    return false;
                }
                if (tix == 0) continue;
                tix *= 6;
                rix *= 6;
                for (int j = 6; j > 0; --j) {
                    if (this.indices[--tix] == rhs.indices[--rix]) continue;
                    return false;
                }
            }
            return true;
        }

        void setGlyphTransform(int glyphIndex, AffineTransform newTX) {
            double[] temp = new double[6];
            boolean isIdentity = true;
            if (newTX == null || newTX.isIdentity()) {
                temp[3] = 1.0;
                temp[0] = 1.0;
            } else {
                isIdentity = false;
                newTX.getMatrix(temp);
            }
            if (this.indices == null) {
                if (isIdentity) {
                    return;
                }
                this.indices = new int[this.sgv.glyphs.length];
                this.indices[glyphIndex] = 1;
                this.transforms = temp;
            } else {
                boolean addSlot = false;
                int newIndex = -1;
                if (isIdentity) {
                    newIndex = 0;
                } else {
                    int i;
                    addSlot = true;
                    block0: for (i = 0; i < this.transforms.length; i += 6) {
                        for (int j = 0; j < 6; ++j) {
                            if (this.transforms[i + j] != temp[j]) continue block0;
                        }
                        addSlot = false;
                        break;
                    }
                    newIndex = i / 6 + 1;
                }
                int oldIndex = this.indices[glyphIndex];
                if (newIndex != oldIndex) {
                    boolean removeSlot = false;
                    if (oldIndex != 0) {
                        removeSlot = true;
                        for (int i = 0; i < this.indices.length; ++i) {
                            if (this.indices[i] != oldIndex || i == glyphIndex) continue;
                            removeSlot = false;
                            break;
                        }
                    }
                    if (removeSlot && addSlot) {
                        newIndex = oldIndex;
                        System.arraycopy(temp, 0, this.transforms, (newIndex - 1) * 6, 6);
                    } else if (removeSlot) {
                        if (this.transforms.length == 6) {
                            this.indices = null;
                            this.transforms = null;
                            this.sgv.clearCaches(glyphIndex);
                            this.sgv.clearFlags(1);
                            this.strikesRef = null;
                            return;
                        }
                        double[] ttemp = new double[this.transforms.length - 6];
                        System.arraycopy(this.transforms, 0, ttemp, 0, (oldIndex - 1) * 6);
                        System.arraycopy(this.transforms, oldIndex * 6, ttemp, (oldIndex - 1) * 6, this.transforms.length - oldIndex * 6);
                        this.transforms = ttemp;
                        for (int i = 0; i < this.indices.length; ++i) {
                            if (this.indices[i] <= oldIndex) continue;
                            int n = i;
                            this.indices[n] = this.indices[n] - 1;
                        }
                        if (newIndex > oldIndex) {
                            // empty if block
                        }
                    } else if (addSlot) {
                        double[] ttemp = new double[this.transforms.length + 6];
                        System.arraycopy(this.transforms, 0, ttemp, 0, this.transforms.length);
                        System.arraycopy(temp, 0, ttemp, this.transforms.length, 6);
                        this.transforms = ttemp;
                    }
                    this.indices[glyphIndex] = --newIndex;
                }
            }
            this.sgv.clearCaches(glyphIndex);
            this.sgv.addFlags(1);
            this.strikesRef = null;
        }

        AffineTransform getGlyphTransform(int ix) {
            int index = this.indices[ix];
            if (index == 0) {
                return null;
            }
            int x = (index - 1) * 6;
            return new AffineTransform(this.transforms[x + 0], this.transforms[x + 1], this.transforms[x + 2], this.transforms[x + 3], this.transforms[x + 4], this.transforms[x + 5]);
        }

        int transformCount() {
            if (this.transforms == null) {
                return 0;
            }
            return this.transforms.length / 6;
        }

        Object setupGlyphImages(long[] images, float[] positions, AffineTransform tx) {
            int len = this.sgv.glyphs.length;
            GlyphStrike[] sl = this.getAllStrikes();
            for (int i = 0; i < len; ++i) {
                GlyphStrike gs = sl[this.indices[i]];
                int glyphID = this.sgv.glyphs[i];
                images[i] = gs.strike.getGlyphImagePtr(glyphID);
                gs.getGlyphPosition(glyphID, i * 2, this.sgv.positions, positions);
            }
            tx.transform(positions, 0, positions, 0, len);
            return sl;
        }

        Rectangle getGlyphsPixelBounds(AffineTransform tx, float x, float y, int start, int count) {
            Rectangle result = null;
            Rectangle r = new Rectangle();
            Point2D.Float pt = new Point2D.Float();
            int n = start * 2;
            while (--count >= 0) {
                GlyphStrike gs = this.getStrike(start);
                pt.x = x + this.sgv.positions[n++] + gs.dx;
                pt.y = y + this.sgv.positions[n++] + gs.dy;
                tx.transform(pt, pt);
                gs.strike.getGlyphImageBounds(this.sgv.glyphs[start++], pt, r);
                if (r.isEmpty()) continue;
                if (result == null) {
                    result = new Rectangle(r);
                    continue;
                }
                result.add(r);
            }
            return result != null ? result : r;
        }

        GlyphStrike getStrike(int glyphIndex) {
            if (this.indices != null) {
                GlyphStrike[] strikes = this.getStrikeArray();
                return this.getStrikeAtIndex(strikes, this.indices[glyphIndex]);
            }
            return this.sgv.getDefaultStrike();
        }

        private GlyphStrike[] getAllStrikes() {
            if (this.indices == null) {
                return null;
            }
            GlyphStrike[] strikes = this.getStrikeArray();
            if (!this.haveAllStrikes) {
                for (int i = 0; i < strikes.length; ++i) {
                    this.getStrikeAtIndex(strikes, i);
                }
                this.haveAllStrikes = true;
            }
            return strikes;
        }

        private GlyphStrike[] getStrikeArray() {
            GlyphStrike[] strikes = null;
            if (this.strikesRef != null) {
                strikes = this.strikesRef.get();
            }
            if (strikes == null) {
                this.haveAllStrikes = false;
                strikes = new GlyphStrike[this.transformCount() + 1];
                this.strikesRef = new SoftReference<GlyphStrike[]>(strikes);
            }
            return strikes;
        }

        private GlyphStrike getStrikeAtIndex(GlyphStrike[] strikes, int strikeIndex) {
            GlyphStrike strike = strikes[strikeIndex];
            if (strike == null) {
                if (strikeIndex == 0) {
                    strike = this.sgv.getDefaultStrike();
                } else {
                    int ix = (strikeIndex - 1) * 6;
                    AffineTransform gtx = new AffineTransform(this.transforms[ix], this.transforms[ix + 1], this.transforms[ix + 2], this.transforms[ix + 3], this.transforms[ix + 4], this.transforms[ix + 5]);
                    strike = GlyphStrike.create(this.sgv, this.sgv.dtx, gtx);
                }
                strikes[strikeIndex] = strike;
            }
            return strike;
        }
    }

    static class ADL {
        public float ascentX;
        public float ascentY;
        public float descentX;
        public float descentY;
        public float leadingX;
        public float leadingY;

        ADL() {
        }

        public String toString() {
            return this.toStringBuffer(null).toString();
        }

        protected StringBuffer toStringBuffer(StringBuffer result) {
            if (result == null) {
                result = new StringBuffer();
            }
            result.append("ax: ");
            result.append(this.ascentX);
            result.append(" ay: ");
            result.append(this.ascentY);
            result.append(" dx: ");
            result.append(this.descentX);
            result.append(" dy: ");
            result.append(this.descentY);
            result.append(" lx: ");
            result.append(this.leadingX);
            result.append(" ly: ");
            result.append(this.leadingY);
            return result;
        }
    }

    public static final class GlyphStrike {
        StandardGlyphVector sgv;
        FontStrike strike;
        float dx;
        float dy;

        static GlyphStrike create(StandardGlyphVector sgv, AffineTransform dtx, AffineTransform gtx) {
            float dx = 0.0f;
            float dy = 0.0f;
            AffineTransform tx = sgv.ftx;
            if (!dtx.isIdentity() || gtx != null) {
                tx = new AffineTransform(sgv.ftx);
                if (gtx != null) {
                    tx.preConcatenate(gtx);
                    dx = (float)tx.getTranslateX();
                    dy = (float)tx.getTranslateY();
                }
                if (!dtx.isIdentity()) {
                    tx.preConcatenate(dtx);
                }
            }
            int ptSize = 1;
            Object aaHint = sgv.frc.getAntiAliasingHint();
            if (aaHint == RenderingHints.VALUE_TEXT_ANTIALIAS_GASP && !tx.isIdentity() && (tx.getType() & 0xFFFFFFFE) != 0) {
                double shearx = tx.getShearX();
                if (shearx != 0.0) {
                    double scaley = tx.getScaleY();
                    ptSize = (int)Math.sqrt(shearx * shearx + scaley * scaley);
                } else {
                    ptSize = (int)Math.abs(tx.getScaleY());
                }
            }
            int aa = FontStrikeDesc.getAAHintIntVal(aaHint, sgv.font2D, ptSize);
            int fm = FontStrikeDesc.getFMHintIntVal(sgv.frc.getFractionalMetricsHint());
            FontStrikeDesc desc = new FontStrikeDesc(dtx, tx, sgv.font.getStyle(), aa, fm);
            Font2D f2d = sgv.font2D;
            if (f2d instanceof FontSubstitution) {
                f2d = ((FontSubstitution)((Object)f2d)).getCompositeFont2D();
            }
            FontStrike strike = f2d.handle.font2D.getStrike(desc);
            return new GlyphStrike(sgv, strike, dx, dy);
        }

        private GlyphStrike(StandardGlyphVector sgv, FontStrike strike, float dx, float dy) {
            this.sgv = sgv;
            this.strike = strike;
            this.dx = dx;
            this.dy = dy;
        }

        void getADL(ADL result) {
            StrikeMetrics sm = this.strike.getFontMetrics();
            Point2D.Float delta = null;
            if (this.sgv.font.isTransformed()) {
                delta = new Point2D.Float();
                delta.x = (float)this.sgv.font.getTransform().getTranslateX();
                delta.y = (float)this.sgv.font.getTransform().getTranslateY();
            }
            result.ascentX = -sm.ascentX;
            result.ascentY = -sm.ascentY;
            result.descentX = sm.descentX;
            result.descentY = sm.descentY;
            result.leadingX = sm.leadingX;
            result.leadingY = sm.leadingY;
        }

        void getGlyphPosition(int glyphID, int ix, float[] positions, float[] result) {
            result[ix] = positions[ix] + this.dx;
            result[++ix] = positions[ix] + this.dy;
        }

        void addDefaultGlyphAdvance(int glyphID, Point2D.Float result) {
            Point2D.Float adv = this.strike.getGlyphMetrics(glyphID);
            result.x += adv.x + this.dx;
            result.y += adv.y + this.dy;
        }

        Rectangle2D getGlyphOutlineBounds(int glyphID, float x, float y) {
            Rectangle2D result = null;
            if (this.sgv.invdtx == null) {
                result = new Rectangle2D.Float();
                result.setRect(this.strike.getGlyphOutlineBounds(glyphID));
            } else {
                GeneralPath gp = this.strike.getGlyphOutline(glyphID, 0.0f, 0.0f);
                gp.transform(this.sgv.invdtx);
                result = gp.getBounds2D();
            }
            if (!result.isEmpty()) {
                result.setRect(result.getMinX() + (double)x + (double)this.dx, result.getMinY() + (double)y + (double)this.dy, result.getWidth(), result.getHeight());
            }
            return result;
        }

        void appendGlyphOutline(int glyphID, GeneralPath result, float x, float y) {
            GeneralPath gp = null;
            if (this.sgv.invdtx == null) {
                gp = this.strike.getGlyphOutline(glyphID, x + this.dx, y + this.dy);
            } else {
                gp = this.strike.getGlyphOutline(glyphID, 0.0f, 0.0f);
                gp.transform(this.sgv.invdtx);
                gp.transform(AffineTransform.getTranslateInstance(x + this.dx, y + this.dy));
            }
            PathIterator iterator = gp.getPathIterator(null);
            result.append(iterator, false);
        }
    }
}

