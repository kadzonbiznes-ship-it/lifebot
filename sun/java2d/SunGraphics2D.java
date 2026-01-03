/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.MultiResolutionImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import sun.awt.ConstrainableGraphics;
import sun.awt.SunHints;
import sun.awt.image.MultiResolutionToolkitImage;
import sun.awt.image.SurfaceManager;
import sun.awt.image.ToolkitImage;
import sun.awt.util.PerformanceLogger;
import sun.font.FontDesignMetrics;
import sun.font.FontUtilities;
import sun.java2d.DestSurfaceProvider;
import sun.java2d.InvalidPipeException;
import sun.java2d.NullSurfaceData;
import sun.java2d.Surface;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.FontInfo;
import sun.java2d.loops.MaskFill;
import sun.java2d.loops.RenderLoops;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.XORComposite;
import sun.java2d.pipe.DrawImagePipe;
import sun.java2d.pipe.LoopPipe;
import sun.java2d.pipe.PixelDrawPipe;
import sun.java2d.pipe.PixelFillPipe;
import sun.java2d.pipe.Region;
import sun.java2d.pipe.RenderingEngine;
import sun.java2d.pipe.ShapeDrawPipe;
import sun.java2d.pipe.ShapeSpanIterator;
import sun.java2d.pipe.TextPipe;
import sun.java2d.pipe.ValidatePipe;

public final class SunGraphics2D
extends Graphics2D
implements ConstrainableGraphics,
Cloneable,
DestSurfaceProvider {
    public static final int PAINT_CUSTOM = 6;
    public static final int PAINT_TEXTURE = 5;
    public static final int PAINT_RAD_GRADIENT = 4;
    public static final int PAINT_LIN_GRADIENT = 3;
    public static final int PAINT_GRADIENT = 2;
    public static final int PAINT_ALPHACOLOR = 1;
    public static final int PAINT_OPAQUECOLOR = 0;
    public static final int COMP_CUSTOM = 3;
    public static final int COMP_XOR = 2;
    public static final int COMP_ALPHA = 1;
    public static final int COMP_ISCOPY = 0;
    public static final int STROKE_CUSTOM = 3;
    public static final int STROKE_WIDE = 2;
    public static final int STROKE_THINDASHED = 1;
    public static final int STROKE_THIN = 0;
    public static final int TRANSFORM_GENERIC = 4;
    public static final int TRANSFORM_TRANSLATESCALE = 3;
    public static final int TRANSFORM_ANY_TRANSLATE = 2;
    public static final int TRANSFORM_INT_TRANSLATE = 1;
    public static final int TRANSFORM_ISIDENT = 0;
    public static final int CLIP_SHAPE = 2;
    public static final int CLIP_RECTANGULAR = 1;
    public static final int CLIP_DEVICE = 0;
    public int eargb;
    public int pixel;
    public SurfaceData surfaceData;
    public PixelDrawPipe drawpipe;
    public PixelFillPipe fillpipe;
    public DrawImagePipe imagepipe;
    public ShapeDrawPipe shapepipe;
    public TextPipe textpipe;
    public MaskFill alphafill;
    public RenderLoops loops;
    public CompositeType imageComp;
    public int paintState;
    public int compositeState;
    public int strokeState;
    public int transformState;
    public int clipState;
    public Color foregroundColor;
    public Color backgroundColor;
    public AffineTransform transform;
    public int transX;
    public int transY;
    protected static final Stroke defaultStroke = new BasicStroke();
    protected static final Composite defaultComposite = AlphaComposite.SrcOver;
    private static final Font defaultFont = new Font("Dialog", 0, 12);
    public Paint paint;
    public Stroke stroke;
    public Composite composite;
    protected Font font;
    protected FontMetrics fontMetrics;
    public int renderHint;
    public int antialiasHint;
    public int textAntialiasHint;
    protected int fractionalMetricsHint;
    public int lcdTextContrast;
    private static int lcdTextContrastDefaultValue = 140;
    private int interpolationHint;
    public int strokeHint;
    public int interpolationType;
    public RenderingHints hints;
    public Region constrainClip;
    public int constrainX;
    public int constrainY;
    public Region clipRegion;
    public Shape usrClip;
    protected Region devClip;
    private int resolutionVariantHint;
    private boolean validFontInfo;
    private FontInfo fontInfo;
    private FontInfo glyphVectorFontInfo;
    private FontRenderContext glyphVectorFRC;
    private static final int slowTextTransformMask = 120;
    protected static ValidatePipe invalidpipe;
    private static final double[] IDENT_MATRIX;
    private static final AffineTransform IDENT_ATX;
    private static final int MINALLOCATED = 8;
    private static final int TEXTARRSIZE = 17;
    private static double[][] textTxArr;
    private static AffineTransform[] textAtArr;
    static final int NON_UNIFORM_SCALE_MASK = 36;
    public static final double MinPenSizeAA;
    public static final double MinPenSizeAASquared;
    public static final double MinPenSizeSquared = 1.000000001;
    static final int NON_RECTILINEAR_TRANSFORM_MASK = 48;
    Blit lastCAblit;
    Composite lastCAcomp;
    private FontRenderContext cachedFRC;

    public SunGraphics2D(SurfaceData sd, Color fg, Color bg, Font f) {
        this.surfaceData = sd;
        this.foregroundColor = fg;
        this.backgroundColor = bg;
        this.stroke = defaultStroke;
        this.composite = defaultComposite;
        this.paint = this.foregroundColor;
        this.imageComp = CompositeType.SrcOverNoEa;
        this.renderHint = 0;
        this.antialiasHint = 1;
        this.textAntialiasHint = 0;
        this.fractionalMetricsHint = 1;
        this.lcdTextContrast = lcdTextContrastDefaultValue;
        this.interpolationHint = -1;
        this.strokeHint = 0;
        this.resolutionVariantHint = 0;
        this.interpolationType = 1;
        this.transform = this.getDefaultTransform();
        if (!this.transform.isIdentity()) {
            this.invalidateTransform();
        }
        this.validateColor();
        this.font = f;
        if (this.font == null) {
            this.font = defaultFont;
        }
        this.setDevClip(sd.getBounds());
        this.invalidatePipe();
    }

    private AffineTransform getDefaultTransform() {
        GraphicsConfiguration gc = this.getDeviceConfiguration();
        return gc == null ? new AffineTransform() : gc.getDefaultTransform();
    }

    protected Object clone() {
        try {
            SunGraphics2D g = (SunGraphics2D)super.clone();
            g.transform = new AffineTransform(this.transform);
            if (this.hints != null) {
                g.hints = (RenderingHints)this.hints.clone();
            }
            if (this.fontInfo != null) {
                g.fontInfo = this.validFontInfo ? (FontInfo)this.fontInfo.clone() : null;
            }
            if (this.glyphVectorFontInfo != null) {
                g.glyphVectorFontInfo = (FontInfo)this.glyphVectorFontInfo.clone();
                g.glyphVectorFRC = this.glyphVectorFRC;
            }
            return g;
        }
        catch (CloneNotSupportedException cloneNotSupportedException) {
            return null;
        }
    }

    @Override
    public Graphics create() {
        return (Graphics)this.clone();
    }

    public void setDevClip(int x, int y, int w, int h) {
        Region c = this.constrainClip;
        this.devClip = c == null ? Region.getInstanceXYWH(x, y, w, h) : c.getIntersectionXYWH(x, y, w, h);
        this.validateCompClip();
    }

    public void setDevClip(Rectangle r) {
        this.setDevClip(r.x, r.y, r.width, r.height);
    }

    public void constrain(int x, int y, int w, int h, Region region) {
        if ((x | y) != 0) {
            this.translate(x, y);
        }
        if (this.transformState > 3) {
            this.clipRect(0, 0, w, h);
            return;
        }
        double scaleX = this.transform.getScaleX();
        double scaleY = this.transform.getScaleY();
        x = this.constrainX = (int)this.transform.getTranslateX();
        y = this.constrainY = (int)this.transform.getTranslateY();
        w = Region.dimAdd(x, Region.clipScale(w, scaleX));
        h = Region.dimAdd(y, Region.clipScale(h, scaleY));
        Region c = this.constrainClip;
        c = c == null ? Region.getInstanceXYXY(x, y, w, h) : c.getIntersectionXYXY(x, y, w, h);
        if (region != null) {
            region = region.getScaledRegion(scaleX, scaleY);
            region = region.getTranslatedRegion(x, y);
            c = c.getIntersection(region);
        }
        if (c == this.constrainClip) {
            return;
        }
        this.constrainClip = c;
        if (!this.devClip.isInsideQuickCheck(c)) {
            this.devClip = this.devClip.getIntersection(c);
            this.validateCompClip();
        }
    }

    @Override
    public void constrain(int x, int y, int w, int h) {
        this.constrain(x, y, w, h, null);
    }

    protected void invalidatePipe() {
        this.drawpipe = invalidpipe;
        this.fillpipe = invalidpipe;
        this.shapepipe = invalidpipe;
        this.textpipe = invalidpipe;
        this.imagepipe = invalidpipe;
        this.loops = null;
    }

    public void validatePipe() {
        if (!this.surfaceData.isValid()) {
            throw new InvalidPipeException("attempt to validate Pipe with invalid SurfaceData");
        }
        this.surfaceData.validatePipe(this);
    }

    Shape intersectShapes(Shape s1, Shape s2, boolean keep1, boolean keep2) {
        if (s1 instanceof Rectangle && s2 instanceof Rectangle) {
            return ((Rectangle)s1).intersection((Rectangle)s2);
        }
        if (s1 instanceof Rectangle2D) {
            return this.intersectRectShape((Rectangle2D)s1, s2, keep1, keep2);
        }
        if (s2 instanceof Rectangle2D) {
            return this.intersectRectShape((Rectangle2D)s2, s1, keep2, keep1);
        }
        return this.intersectByArea(s1, s2, keep1, keep2);
    }

    Shape intersectRectShape(Rectangle2D r, Shape s, boolean keep1, boolean keep2) {
        if (s instanceof Rectangle2D) {
            Rectangle2D r2 = (Rectangle2D)s;
            Rectangle2D outrect = !keep1 ? r : (!keep2 ? r2 : new Rectangle2D.Float());
            double x1 = Math.max(r.getX(), r2.getX());
            double x2 = Math.min(r.getX() + r.getWidth(), r2.getX() + r2.getWidth());
            double y1 = Math.max(r.getY(), r2.getY());
            double y2 = Math.min(r.getY() + r.getHeight(), r2.getY() + r2.getHeight());
            if (x2 - x1 < 0.0 || y2 - y1 < 0.0) {
                outrect.setFrameFromDiagonal(0.0, 0.0, 0.0, 0.0);
            } else {
                outrect.setFrameFromDiagonal(x1, y1, x2, y2);
            }
            return outrect;
        }
        if (r.contains(s.getBounds2D())) {
            if (keep2) {
                s = SunGraphics2D.cloneShape(s);
            }
            return s;
        }
        return this.intersectByArea(r, s, keep1, keep2);
    }

    protected static Shape cloneShape(Shape s) {
        return new GeneralPath(s);
    }

    Shape intersectByArea(Shape s1, Shape s2, boolean keep1, boolean keep2) {
        Area a1;
        if (!keep1 && s1 instanceof Area) {
            a1 = (Area)s1;
        } else if (!keep2 && s2 instanceof Area) {
            a1 = (Area)s2;
            s2 = s1;
        } else {
            a1 = new Area(s1);
        }
        Area a2 = s2 instanceof Area ? (Area)s2 : new Area(s2);
        a1.intersect(a2);
        if (a1.isRectangular()) {
            return a1.getBounds();
        }
        return a1;
    }

    public Region getCompClip() {
        if (!this.surfaceData.isValid()) {
            this.revalidateAll();
        }
        return this.clipRegion;
    }

    @Override
    public Font getFont() {
        if (this.font == null) {
            this.font = defaultFont;
        }
        return this.font;
    }

    public FontInfo checkFontInfo(FontInfo info, Font font, FontRenderContext frc) {
        AffineTransform devAt;
        if (info == null) {
            info = new FontInfo();
        }
        float ptSize = font.getSize2D();
        AffineTransform textAt = null;
        if (font.isTransformed()) {
            textAt = font.getTransform();
            textAt.scale(ptSize, ptSize);
            int txFontType = textAt.getType();
            info.originX = (float)textAt.getTranslateX();
            info.originY = (float)textAt.getTranslateY();
            textAt.translate(-info.originX, -info.originY);
            if (this.transformState >= 3) {
                info.devTx = new double[4];
                this.transform.getMatrix(info.devTx);
                devAt = new AffineTransform(info.devTx);
                textAt.preConcatenate(devAt);
            } else {
                info.devTx = IDENT_MATRIX;
                devAt = IDENT_ATX;
            }
            info.glyphTx = new double[4];
            textAt.getMatrix(info.glyphTx);
            double shearx = textAt.getShearX();
            double scaley = textAt.getScaleY();
            if (shearx != 0.0) {
                scaley = Math.sqrt(shearx * shearx + scaley * scaley);
            }
            info.pixelHeight = (int)(Math.abs(scaley) + 0.5);
        } else {
            boolean txFontType = false;
            info.originY = 0.0f;
            info.originX = 0.0f;
            if (this.transformState >= 3) {
                info.devTx = new double[4];
                this.transform.getMatrix(info.devTx);
                devAt = new AffineTransform(info.devTx);
                info.glyphTx = new double[4];
                for (int i = 0; i < 4; ++i) {
                    info.glyphTx[i] = info.devTx[i] * (double)ptSize;
                }
                textAt = new AffineTransform(info.glyphTx);
                double shearx = this.transform.getShearX();
                double scaley = this.transform.getScaleY();
                if (shearx != 0.0) {
                    scaley = Math.sqrt(shearx * shearx + scaley * scaley);
                }
                info.pixelHeight = (int)(Math.abs(scaley * (double)ptSize) + 0.5);
            } else {
                int pszInt = (int)ptSize;
                if (ptSize == (float)pszInt && pszInt >= 8 && pszInt < 17) {
                    info.glyphTx = textTxArr[pszInt];
                    textAt = textAtArr[pszInt];
                    info.pixelHeight = pszInt;
                } else {
                    info.pixelHeight = (int)((double)ptSize + 0.5);
                }
                if (textAt == null) {
                    info.glyphTx = new double[]{ptSize, 0.0, 0.0, ptSize};
                    textAt = new AffineTransform(info.glyphTx);
                }
                info.devTx = IDENT_MATRIX;
                devAt = IDENT_ATX;
            }
        }
        info.nonInvertibleTx = Math.abs(textAt.getDeterminant()) <= Double.MIN_VALUE;
        info.font2D = FontUtilities.getFont2D(font);
        int fmhint = this.fractionalMetricsHint;
        if (fmhint == 0) {
            fmhint = 1;
        }
        info.lcdSubPixPos = false;
        int aahint = frc == null ? this.textAntialiasHint : ((SunHints.Value)frc.getAntiAliasingHint()).getIndex();
        if (aahint == 0) {
            aahint = this.antialiasHint == 2 ? 2 : 1;
        } else if (aahint == 3) {
            aahint = info.font2D.useAAForPtSize(info.pixelHeight) ? 2 : 1;
        } else if (aahint >= 4) {
            if (!this.surfaceData.canRenderLCDText(this)) {
                aahint = 2;
            } else {
                info.lcdRGBOrder = true;
                if (aahint == 5) {
                    aahint = 4;
                    info.lcdRGBOrder = false;
                } else if (aahint == 7) {
                    aahint = 6;
                    info.lcdRGBOrder = false;
                }
                boolean bl = info.lcdSubPixPos = fmhint == 2 && aahint == 4;
            }
        }
        if (FontUtilities.isMacOSX14 && aahint == 1) {
            aahint = 2;
        }
        info.aaHint = aahint;
        info.fontStrike = info.font2D.getStrike(font, devAt, textAt, aahint, fmhint);
        return info;
    }

    public static boolean isRotated(double[] mtx) {
        return mtx[0] != mtx[3] || mtx[1] != 0.0 || mtx[2] != 0.0 || !(mtx[0] > 0.0);
    }

    @Override
    public void setFont(Font font) {
        if (font != null && font != this.font) {
            if (this.textAntialiasHint == 3 && this.textpipe != invalidpipe && (this.transformState > 2 || font.isTransformed() || this.fontInfo == null || this.fontInfo.aaHint == 2 != FontUtilities.getFont2D(font).useAAForPtSize(font.getSize()))) {
                this.textpipe = invalidpipe;
            }
            this.font = font;
            this.fontMetrics = null;
            this.validFontInfo = false;
        }
    }

    public FontInfo getFontInfo() {
        if (!this.validFontInfo) {
            this.fontInfo = this.checkFontInfo(this.fontInfo, this.font, null);
            this.validFontInfo = true;
        }
        return this.fontInfo;
    }

    public FontInfo getGVFontInfo(Font font, FontRenderContext frc) {
        if (this.glyphVectorFontInfo != null && this.glyphVectorFontInfo.font == font && this.glyphVectorFRC == frc) {
            return this.glyphVectorFontInfo;
        }
        this.glyphVectorFRC = frc;
        this.glyphVectorFontInfo = this.checkFontInfo(this.glyphVectorFontInfo, font, frc);
        return this.glyphVectorFontInfo;
    }

    @Override
    public FontMetrics getFontMetrics() {
        if (this.fontMetrics != null) {
            return this.fontMetrics;
        }
        this.fontMetrics = FontDesignMetrics.getMetrics(this.font, this.getFontRenderContext());
        return this.fontMetrics;
    }

    @Override
    public FontMetrics getFontMetrics(Font font) {
        if (this.fontMetrics != null && font == this.font) {
            return this.fontMetrics;
        }
        FontDesignMetrics fm = FontDesignMetrics.getMetrics(font, this.getFontRenderContext());
        if (this.font == font) {
            this.fontMetrics = fm;
        }
        return fm;
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        if (onStroke) {
            s = this.stroke.createStrokedShape(s);
        }
        s = this.transformShape(s);
        if ((this.constrainX | this.constrainY) != 0) {
            rect = new Rectangle(rect);
            rect.translate(this.constrainX, this.constrainY);
        }
        return s.intersects(rect);
    }

    public ColorModel getDeviceColorModel() {
        return this.surfaceData.getColorModel();
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return this.surfaceData.getDeviceConfiguration();
    }

    public SurfaceData getSurfaceData() {
        return this.surfaceData;
    }

    @Override
    public void setComposite(Composite comp) {
        int newCompState;
        CompositeType newCompType;
        if (this.composite == comp) {
            return;
        }
        if (comp instanceof AlphaComposite) {
            AlphaComposite alphacomp = (AlphaComposite)comp;
            newCompType = CompositeType.forAlphaComposite(alphacomp);
            newCompState = newCompType == CompositeType.SrcOverNoEa ? (this.paintState == 0 || this.paintState > 1 && this.paint.getTransparency() == 1 ? 0 : 1) : (newCompType == CompositeType.SrcNoEa || newCompType == CompositeType.Src || newCompType == CompositeType.Clear ? 0 : (this.surfaceData.getTransparency() == 1 && newCompType == CompositeType.SrcIn ? 0 : 1));
        } else if (comp instanceof XORComposite) {
            newCompState = 2;
            newCompType = CompositeType.Xor;
        } else {
            if (comp == null) {
                throw new IllegalArgumentException("null Composite");
            }
            this.surfaceData.checkCustomComposite();
            newCompState = 3;
            newCompType = CompositeType.General;
        }
        if (this.compositeState != newCompState || this.imageComp != newCompType) {
            this.compositeState = newCompState;
            this.imageComp = newCompType;
            this.invalidatePipe();
            this.validFontInfo = false;
        }
        this.composite = comp;
        if (this.paintState <= 1) {
            this.validateColor();
        }
    }

    @Override
    public void setPaint(Paint paint) {
        Class<?> paintClass;
        if (paint instanceof Color) {
            this.setColor((Color)paint);
            return;
        }
        if (paint == null || this.paint == paint) {
            return;
        }
        this.paint = paint;
        if (this.imageComp == CompositeType.SrcOverNoEa) {
            if (paint.getTransparency() == 1) {
                if (this.compositeState != 0) {
                    this.compositeState = 0;
                }
            } else if (this.compositeState == 0) {
                this.compositeState = 1;
            }
        }
        this.paintState = (paintClass = paint.getClass()) == GradientPaint.class ? 2 : (paintClass == LinearGradientPaint.class ? 3 : (paintClass == RadialGradientPaint.class ? 4 : (paintClass == TexturePaint.class ? 5 : 6)));
        this.validFontInfo = false;
        this.invalidatePipe();
    }

    private void validateBasicStroke(BasicStroke bs) {
        boolean aa;
        boolean bl = aa = this.antialiasHint == 2;
        if (this.transformState < 3) {
            this.strokeState = aa ? ((double)bs.getLineWidth() <= MinPenSizeAA ? (bs.getDashArray() == null ? 0 : 1) : 2) : (bs == defaultStroke ? 0 : (bs.getLineWidth() <= 1.0f ? (bs.getDashArray() == null ? 0 : 1) : 2));
        } else {
            double widthsquared;
            if ((this.transform.getType() & 0x24) == 0) {
                widthsquared = Math.abs(this.transform.getDeterminant());
            } else {
                double A = this.transform.getScaleX();
                double C = this.transform.getShearX();
                double B = this.transform.getShearY();
                double D = this.transform.getScaleY();
                double EA = A * A + B * B;
                double EB = 2.0 * (A * C + B * D);
                double EC2 = C * C + D * D;
                double hypot = Math.sqrt(EB * EB + (EA - EC2) * (EA - EC2));
                widthsquared = (EA + EC2 + hypot) / 2.0;
            }
            if (bs != defaultStroke) {
                widthsquared *= (double)(bs.getLineWidth() * bs.getLineWidth());
            }
            this.strokeState = widthsquared <= (aa ? MinPenSizeAASquared : 1.000000001) ? (bs.getDashArray() == null ? 0 : 1) : 2;
        }
    }

    @Override
    public void setStroke(Stroke s) {
        if (s == null) {
            throw new IllegalArgumentException("null Stroke");
        }
        int saveStrokeState = this.strokeState;
        this.stroke = s;
        if (s instanceof BasicStroke) {
            this.validateBasicStroke((BasicStroke)s);
        } else {
            this.strokeState = 3;
        }
        if (this.strokeState != saveStrokeState) {
            this.invalidatePipe();
        }
    }

    @Override
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        if (!hintKey.isCompatibleValue(hintValue)) {
            throw new IllegalArgumentException(String.valueOf(hintValue) + " is not compatible with " + String.valueOf(hintKey));
        }
        if (hintKey instanceof SunHints.Key) {
            boolean stateChanged;
            boolean textStateChanged = false;
            boolean recognized = true;
            SunHints.Key sunKey = (SunHints.Key)hintKey;
            int newHint = sunKey == SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST ? ((Integer)hintValue).intValue() : ((SunHints.Value)hintValue).getIndex();
            switch (sunKey.getIndex()) {
                case 0: {
                    boolean bl = stateChanged = this.renderHint != newHint;
                    if (!stateChanged) break;
                    this.renderHint = newHint;
                    if (this.interpolationHint != -1) break;
                    this.interpolationType = newHint == 2 ? 2 : 1;
                    break;
                }
                case 1: {
                    stateChanged = this.antialiasHint != newHint;
                    this.antialiasHint = newHint;
                    if (!stateChanged) break;
                    boolean bl = textStateChanged = this.textAntialiasHint == 0;
                    if (this.strokeState == 3) break;
                    this.validateBasicStroke((BasicStroke)this.stroke);
                    break;
                }
                case 2: {
                    textStateChanged = stateChanged = this.textAntialiasHint != newHint;
                    this.textAntialiasHint = newHint;
                    break;
                }
                case 3: {
                    textStateChanged = stateChanged = this.fractionalMetricsHint != newHint;
                    this.fractionalMetricsHint = newHint;
                    break;
                }
                case 100: {
                    stateChanged = false;
                    this.lcdTextContrast = newHint;
                    break;
                }
                case 5: {
                    this.interpolationHint = newHint;
                    switch (newHint) {
                        case 2: {
                            newHint = 3;
                            break;
                        }
                        case 1: {
                            newHint = 2;
                            break;
                        }
                        default: {
                            newHint = 1;
                        }
                    }
                    stateChanged = this.interpolationType != newHint;
                    this.interpolationType = newHint;
                    break;
                }
                case 8: {
                    stateChanged = this.strokeHint != newHint;
                    this.strokeHint = newHint;
                    break;
                }
                case 9: {
                    stateChanged = this.resolutionVariantHint != newHint;
                    this.resolutionVariantHint = newHint;
                    break;
                }
                default: {
                    recognized = false;
                    stateChanged = false;
                }
            }
            if (recognized) {
                if (stateChanged) {
                    this.invalidatePipe();
                    if (textStateChanged) {
                        this.fontMetrics = null;
                        this.cachedFRC = null;
                        this.validFontInfo = false;
                        this.glyphVectorFontInfo = null;
                    }
                }
                if (this.hints != null) {
                    this.hints.put(hintKey, hintValue);
                }
                return;
            }
        }
        if (this.hints == null) {
            this.hints = this.makeHints(null);
        }
        this.hints.put(hintKey, hintValue);
    }

    @Override
    public Object getRenderingHint(RenderingHints.Key hintKey) {
        if (this.hints != null) {
            return this.hints.get(hintKey);
        }
        if (!(hintKey instanceof SunHints.Key)) {
            return null;
        }
        int keyindex = ((SunHints.Key)hintKey).getIndex();
        switch (keyindex) {
            case 0: {
                return SunHints.Value.get(0, this.renderHint);
            }
            case 1: {
                return SunHints.Value.get(1, this.antialiasHint);
            }
            case 2: {
                return SunHints.Value.get(2, this.textAntialiasHint);
            }
            case 3: {
                return SunHints.Value.get(3, this.fractionalMetricsHint);
            }
            case 100: {
                return this.lcdTextContrast;
            }
            case 5: {
                switch (this.interpolationHint) {
                    case 0: {
                        return SunHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
                    }
                    case 1: {
                        return SunHints.VALUE_INTERPOLATION_BILINEAR;
                    }
                    case 2: {
                        return SunHints.VALUE_INTERPOLATION_BICUBIC;
                    }
                }
                return null;
            }
            case 8: {
                return SunHints.Value.get(8, this.strokeHint);
            }
            case 9: {
                return SunHints.Value.get(9, this.resolutionVariantHint);
            }
        }
        return null;
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
        this.hints = null;
        this.renderHint = 0;
        this.antialiasHint = 1;
        this.textAntialiasHint = 0;
        this.fractionalMetricsHint = 1;
        this.lcdTextContrast = lcdTextContrastDefaultValue;
        this.interpolationHint = -1;
        this.interpolationType = 1;
        boolean customHintPresent = false;
        for (Object key : hints.keySet()) {
            if (key == SunHints.KEY_RENDERING || key == SunHints.KEY_ANTIALIASING || key == SunHints.KEY_TEXT_ANTIALIASING || key == SunHints.KEY_FRACTIONALMETRICS || key == SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST || key == SunHints.KEY_STROKE_CONTROL || key == SunHints.KEY_INTERPOLATION) {
                this.setRenderingHint((RenderingHints.Key)key, hints.get(key));
                continue;
            }
            customHintPresent = true;
        }
        if (customHintPresent) {
            this.hints = this.makeHints(hints);
        }
        this.invalidatePipe();
    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
        boolean customHintPresent = false;
        for (Object key : hints.keySet()) {
            if (key == SunHints.KEY_RENDERING || key == SunHints.KEY_ANTIALIASING || key == SunHints.KEY_TEXT_ANTIALIASING || key == SunHints.KEY_FRACTIONALMETRICS || key == SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST || key == SunHints.KEY_STROKE_CONTROL || key == SunHints.KEY_INTERPOLATION) {
                this.setRenderingHint((RenderingHints.Key)key, hints.get(key));
                continue;
            }
            customHintPresent = true;
        }
        if (customHintPresent) {
            if (this.hints == null) {
                this.hints = this.makeHints(hints);
            } else {
                this.hints.putAll(hints);
            }
        }
    }

    @Override
    public RenderingHints getRenderingHints() {
        if (this.hints == null) {
            return this.makeHints(null);
        }
        return (RenderingHints)this.hints.clone();
    }

    RenderingHints makeHints(Map<?, ?> hints) {
        RenderingHints model = new RenderingHints(null);
        if (hints != null) {
            model.putAll(hints);
        }
        model.put(SunHints.KEY_RENDERING, SunHints.Value.get(0, this.renderHint));
        model.put(SunHints.KEY_ANTIALIASING, SunHints.Value.get(1, this.antialiasHint));
        model.put(SunHints.KEY_TEXT_ANTIALIASING, SunHints.Value.get(2, this.textAntialiasHint));
        model.put(SunHints.KEY_FRACTIONALMETRICS, SunHints.Value.get(3, this.fractionalMetricsHint));
        model.put(SunHints.KEY_TEXT_ANTIALIAS_LCD_CONTRAST, (Object)this.lcdTextContrast);
        Object value = switch (this.interpolationHint) {
            case 0 -> SunHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
            case 1 -> SunHints.VALUE_INTERPOLATION_BILINEAR;
            case 2 -> SunHints.VALUE_INTERPOLATION_BICUBIC;
            default -> null;
        };
        if (value != null) {
            model.put(SunHints.KEY_INTERPOLATION, value);
        }
        model.put(SunHints.KEY_STROKE_CONTROL, SunHints.Value.get(8, this.strokeHint));
        return model;
    }

    @Override
    public void translate(double tx, double ty) {
        this.transform.translate(tx, ty);
        this.invalidateTransform();
    }

    @Override
    public void rotate(double theta) {
        this.transform.rotate(theta);
        this.invalidateTransform();
    }

    @Override
    public void rotate(double theta, double x, double y) {
        this.transform.rotate(theta, x, y);
        this.invalidateTransform();
    }

    @Override
    public void scale(double sx, double sy) {
        this.transform.scale(sx, sy);
        this.invalidateTransform();
    }

    @Override
    public void shear(double shx, double shy) {
        this.transform.shear(shx, shy);
        this.invalidateTransform();
    }

    @Override
    public void transform(AffineTransform xform) {
        this.transform.concatenate(xform);
        this.invalidateTransform();
    }

    @Override
    public void translate(int x, int y) {
        this.transform.translate(x, y);
        if (this.transformState <= 1) {
            this.transX += x;
            this.transY += y;
            this.transformState = (this.transX | this.transY) == 0 ? 0 : 1;
        } else {
            this.invalidateTransform();
        }
    }

    @Override
    public void setTransform(AffineTransform Tx) {
        if ((this.constrainX | this.constrainY) == 0) {
            this.transform.setTransform(Tx);
        } else {
            this.transform.setToTranslation(this.constrainX, this.constrainY);
            this.transform.concatenate(Tx);
        }
        this.invalidateTransform();
    }

    protected void invalidateTransform() {
        int type = this.transform.getType();
        int origTransformState = this.transformState;
        if (type == 0) {
            this.transformState = 0;
            this.transY = 0;
            this.transX = 0;
        } else if (type == 1) {
            double dtx = this.transform.getTranslateX();
            double dty = this.transform.getTranslateY();
            this.transX = (int)Math.floor(dtx + 0.5);
            this.transY = (int)Math.floor(dty + 0.5);
            this.transformState = dtx == (double)this.transX && dty == (double)this.transY ? 1 : 2;
        } else if ((type & 0x78) == 0) {
            this.transformState = 3;
            this.transY = 0;
            this.transX = 0;
        } else {
            this.transformState = 4;
            this.transY = 0;
            this.transX = 0;
        }
        if (this.transformState >= 3 || origTransformState >= 3) {
            this.cachedFRC = null;
            this.validFontInfo = false;
            this.fontMetrics = null;
            this.glyphVectorFontInfo = null;
            if (this.transformState != origTransformState) {
                this.invalidatePipe();
            }
        }
        if (this.strokeState != 3) {
            this.validateBasicStroke((BasicStroke)this.stroke);
        }
    }

    @Override
    public AffineTransform getTransform() {
        if ((this.constrainX | this.constrainY) == 0) {
            return new AffineTransform(this.transform);
        }
        AffineTransform tx = AffineTransform.getTranslateInstance(-this.constrainX, -this.constrainY);
        tx.concatenate(this.transform);
        return tx;
    }

    public AffineTransform cloneTransform() {
        return new AffineTransform(this.transform);
    }

    @Override
    public Paint getPaint() {
        return this.paint;
    }

    @Override
    public Composite getComposite() {
        return this.composite;
    }

    @Override
    public Color getColor() {
        return this.foregroundColor;
    }

    void validateColor() {
        int eargb;
        if (this.imageComp == CompositeType.Clear) {
            eargb = 0;
        } else {
            eargb = this.foregroundColor.getRGB();
            if (this.compositeState <= 1 && this.imageComp != CompositeType.SrcNoEa && this.imageComp != CompositeType.SrcOverNoEa) {
                AlphaComposite alphacomp = (AlphaComposite)this.composite;
                int a = Math.round(alphacomp.getAlpha() * (float)(eargb >>> 24));
                eargb = eargb & 0xFFFFFF | a << 24;
            }
        }
        this.eargb = eargb;
        this.pixel = this.surfaceData.pixelFor(eargb);
    }

    @Override
    public void setColor(Color color) {
        if (color == null || color == this.paint) {
            return;
        }
        this.foregroundColor = color;
        this.paint = this.foregroundColor;
        this.validateColor();
        if (this.eargb >> 24 == -1) {
            if (this.paintState == 0) {
                return;
            }
            this.paintState = 0;
            if (this.imageComp == CompositeType.SrcOverNoEa) {
                this.compositeState = 0;
            }
        } else {
            if (this.paintState == 1) {
                return;
            }
            this.paintState = 1;
            if (this.imageComp == CompositeType.SrcOverNoEa) {
                this.compositeState = 1;
            }
        }
        this.validFontInfo = false;
        this.invalidatePipe();
    }

    @Override
    public void setBackground(Color color) {
        this.backgroundColor = color;
    }

    @Override
    public Color getBackground() {
        return this.backgroundColor;
    }

    @Override
    public Stroke getStroke() {
        return this.stroke;
    }

    @Override
    public Rectangle getClipBounds() {
        if (this.clipState == 0) {
            return null;
        }
        return this.getClipBounds(new Rectangle());
    }

    @Override
    public Rectangle getClipBounds(Rectangle r) {
        if (this.clipState != 0) {
            if (this.transformState <= 1) {
                if (this.usrClip instanceof Rectangle) {
                    r.setBounds((Rectangle)this.usrClip);
                } else {
                    r.setFrame(this.usrClip.getBounds2D());
                }
                r.translate(-this.transX, -this.transY);
            } else {
                r.setFrame(this.getClip().getBounds2D());
            }
        } else if (r == null) {
            throw new NullPointerException("null rectangle parameter");
        }
        return r;
    }

    @Override
    public boolean hitClip(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return false;
        }
        if (this.transformState > 1) {
            double[] d = new double[]{x, y, x + width, y, x, y + height, x + width, y + height};
            this.transform.transform(d, 0, d, 0, 4);
            x = (int)Math.floor(Math.min(Math.min(d[0], d[2]), Math.min(d[4], d[6])));
            y = (int)Math.floor(Math.min(Math.min(d[1], d[3]), Math.min(d[5], d[7])));
            width = (int)Math.ceil(Math.max(Math.max(d[0], d[2]), Math.max(d[4], d[6])));
            height = (int)Math.ceil(Math.max(Math.max(d[1], d[3]), Math.max(d[5], d[7])));
        } else {
            width += (x += this.transX);
            height += (y += this.transY);
        }
        try {
            if (!this.getCompClip().intersectsQuickCheckXYXY(x, y, width, height)) {
                return false;
            }
        }
        catch (InvalidPipeException e) {
            return false;
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void validateCompClip() {
        int origClipState = this.clipState;
        if (this.usrClip == null) {
            this.clipState = 0;
            this.clipRegion = this.devClip;
        } else if (this.usrClip instanceof Rectangle2D) {
            this.clipState = 1;
            this.clipRegion = this.devClip.getIntersection((Rectangle2D)this.usrClip);
        } else {
            PathIterator cpi = this.usrClip.getPathIterator(null);
            int[] box = new int[4];
            ShapeSpanIterator sr = LoopPipe.getFillSSI(this);
            try {
                Region r;
                sr.setOutputArea(this.devClip);
                sr.appendPath(cpi);
                sr.getPathBox(box);
                this.clipRegion = r = Region.getInstance(box, sr);
                this.clipState = r.isRectangular() ? 1 : 2;
            }
            finally {
                sr.dispose();
            }
        }
        if (origClipState != this.clipState && (this.clipState == 2 || origClipState == 2)) {
            this.validFontInfo = false;
            this.invalidatePipe();
        }
    }

    protected Shape transformShape(Shape s) {
        if (s == null) {
            return null;
        }
        if (this.transformState > 1) {
            return SunGraphics2D.transformShape(this.transform, s);
        }
        return SunGraphics2D.transformShape(this.transX, this.transY, s);
    }

    public Shape untransformShape(Shape s) {
        if (s == null) {
            return null;
        }
        if (this.transformState > 1) {
            try {
                return SunGraphics2D.transformShape(this.transform.createInverse(), s);
            }
            catch (NoninvertibleTransformException e) {
                return null;
            }
        }
        return SunGraphics2D.transformShape(-this.transX, -this.transY, s);
    }

    protected static Shape transformShape(int tx, int ty, Shape s) {
        if (s == null) {
            return null;
        }
        if (s instanceof Rectangle) {
            Rectangle r = s.getBounds();
            r.translate(tx, ty);
            return r;
        }
        if (s instanceof Rectangle2D) {
            Rectangle2D rect = (Rectangle2D)s;
            return new Rectangle2D.Double(rect.getX() + (double)tx, rect.getY() + (double)ty, rect.getWidth(), rect.getHeight());
        }
        if (tx == 0 && ty == 0) {
            return SunGraphics2D.cloneShape(s);
        }
        AffineTransform mat = AffineTransform.getTranslateInstance(tx, ty);
        return mat.createTransformedShape(s);
    }

    protected static Shape transformShape(AffineTransform tx, Shape clip) {
        if (clip == null) {
            return null;
        }
        if (clip instanceof Rectangle2D && (tx.getType() & 0x30) == 0) {
            double[] matrix;
            Rectangle2D rect = (Rectangle2D)clip;
            matrix = new double[]{rect.getX(), rect.getY(), matrix[0] + rect.getWidth(), matrix[1] + rect.getHeight()};
            tx.transform(matrix, 0, matrix, 0, 2);
            SunGraphics2D.fixRectangleOrientation(matrix, rect);
            return new Rectangle2D.Double(matrix[0], matrix[1], matrix[2] - matrix[0], matrix[3] - matrix[1]);
        }
        if (tx.isIdentity()) {
            return SunGraphics2D.cloneShape(clip);
        }
        return tx.createTransformedShape(clip);
    }

    private static void fixRectangleOrientation(double[] m, Rectangle2D clip) {
        double t;
        if (clip.getWidth() > 0.0 != m[2] - m[0] > 0.0) {
            t = m[0];
            m[0] = m[2];
            m[2] = t;
        }
        if (clip.getHeight() > 0.0 != m[3] - m[1] > 0.0) {
            t = m[1];
            m[1] = m[3];
            m[3] = t;
        }
    }

    @Override
    public void clipRect(int x, int y, int w, int h) {
        this.clip(new Rectangle(x, y, w, h));
    }

    @Override
    public void setClip(int x, int y, int w, int h) {
        this.setClip(new Rectangle(x, y, w, h));
    }

    @Override
    public Shape getClip() {
        return this.untransformShape(this.usrClip);
    }

    @Override
    public void setClip(Shape sh) {
        this.usrClip = this.transformShape(sh);
        this.validateCompClip();
    }

    @Override
    public void clip(Shape s) {
        s = this.transformShape(s);
        if (this.usrClip != null) {
            s = this.intersectShapes(this.usrClip, s, true, true);
        }
        this.usrClip = s;
        this.validateCompClip();
    }

    @Override
    public void setPaintMode() {
        this.setComposite(AlphaComposite.SrcOver);
    }

    @Override
    public void setXORMode(Color c) {
        if (c == null) {
            throw new IllegalArgumentException("null XORColor");
        }
        this.setComposite(new XORComposite(c, this.surfaceData));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void copyArea(int x, int y, int w, int h, int dx, int dy) {
        try {
            this.doCopyArea(x, y, w, h, dx, dy);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.doCopyArea(x, y, w, h, dx, dy);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    private void doCopyArea(int x, int y, int w, int h, int dx, int dy) {
        SurfaceData theData;
        if (w <= 0 || h <= 0) {
            return;
        }
        if (this.transformState != 0) {
            if (this.transformState <= 2) {
                x += this.transX;
                y += this.transY;
            } else if (this.transformState == 3) {
                double[] coords = new double[]{x, y, x + w, y + h, x + dx, y + dy};
                this.transform.transform(coords, 0, coords, 0, 3);
                x = (int)Math.ceil(coords[0] - 0.5);
                y = (int)Math.ceil(coords[1] - 0.5);
                w = (int)Math.ceil(coords[2] - 0.5) - x;
                h = (int)Math.ceil(coords[3] - 0.5) - y;
                dx = (int)Math.ceil(coords[4] - 0.5) - x;
                dy = (int)Math.ceil(coords[5] - 0.5) - y;
                if (w < 0) {
                    w = -w;
                    x -= w;
                }
                if (h < 0) {
                    h = -h;
                    y -= h;
                }
            } else {
                throw new InternalError("transformed copyArea not implemented yet");
            }
        }
        if ((theData = this.surfaceData).copyArea(this, x, y, w, h, dx, dy)) {
            return;
        }
        Region clip = this.getCompClip();
        Composite comp = this.composite;
        if (this.lastCAcomp != comp) {
            SurfaceType dsttype = theData.getSurfaceType();
            CompositeType comptype = this.imageComp;
            if (CompositeType.SrcOverNoEa.equals(comptype) && theData.getTransparency() == 1) {
                comptype = CompositeType.SrcNoEa;
            }
            this.lastCAblit = Blit.locate(dsttype, comptype, dsttype);
            this.lastCAcomp = comp;
        }
        Blit ob = this.lastCAblit;
        try {
            if (dy == 0 && dx > 0 && dx < w) {
                while (w > 0) {
                    int partW = Math.min(w, dx);
                    int sx = Math.addExact(x, w -= partW);
                    ob.Blit(theData, theData, comp, clip, sx, y, sx + dx, y + dy, partW, h);
                }
                return;
            }
            if (dy > 0 && dy < h && dx > -w && dx < w) {
                while (h > 0) {
                    int partH = Math.min(h, dy);
                    int sy = Math.addExact(y, h -= partH);
                    ob.Blit(theData, theData, comp, clip, x, sy, Math.addExact(x, dx), sy + dy, w, partH);
                }
                return;
            }
            ob.Blit(theData, theData, comp, clip, x, y, Math.addExact(x, dx), Math.addExact(y, dy), w, h);
        }
        catch (ArithmeticException ex) {
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        try {
            this.drawpipe.drawLine(this, x1, y1, x2, y2);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.drawpipe.drawLine(this, x1, y1, x2, y2);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawRoundRect(int x, int y, int w, int h, int arcW, int arcH) {
        try {
            this.drawpipe.drawRoundRect(this, x, y, w, h, arcW, arcH);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.drawpipe.drawRoundRect(this, x, y, w, h, arcW, arcH);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void fillRoundRect(int x, int y, int w, int h, int arcW, int arcH) {
        try {
            this.fillpipe.fillRoundRect(this, x, y, w, h, arcW, arcH);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.fillpipe.fillRoundRect(this, x, y, w, h, arcW, arcH);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawOval(int x, int y, int w, int h) {
        try {
            this.drawpipe.drawOval(this, x, y, w, h);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.drawpipe.drawOval(this, x, y, w, h);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void fillOval(int x, int y, int w, int h) {
        try {
            this.fillpipe.fillOval(this, x, y, w, h);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.fillpipe.fillOval(this, x, y, w, h);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawArc(int x, int y, int w, int h, int startAngl, int arcAngl) {
        try {
            this.drawpipe.drawArc(this, x, y, w, h, startAngl, arcAngl);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.drawpipe.drawArc(this, x, y, w, h, startAngl, arcAngl);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void fillArc(int x, int y, int w, int h, int startAngl, int arcAngl) {
        try {
            this.fillpipe.fillArc(this, x, y, w, h, startAngl, arcAngl);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.fillpipe.fillArc(this, x, y, w, h, startAngl, arcAngl);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        try {
            this.drawpipe.drawPolyline(this, xPoints, yPoints, nPoints);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.drawpipe.drawPolyline(this, xPoints, yPoints, nPoints);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        try {
            this.drawpipe.drawPolygon(this, xPoints, yPoints, nPoints);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.drawpipe.drawPolygon(this, xPoints, yPoints, nPoints);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        try {
            this.fillpipe.fillPolygon(this, xPoints, yPoints, nPoints);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.fillpipe.fillPolygon(this, xPoints, yPoints, nPoints);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawRect(int x, int y, int w, int h) {
        try {
            this.drawpipe.drawRect(this, x, y, w, h);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.drawpipe.drawRect(this, x, y, w, h);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void fillRect(int x, int y, int w, int h) {
        try {
            this.fillpipe.fillRect(this, x, y, w, h);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.fillpipe.fillRect(this, x, y, w, h);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    private void revalidateAll() {
        this.surfaceData = this.surfaceData.getReplacement();
        if (this.surfaceData == null) {
            this.surfaceData = NullSurfaceData.theInstance;
        }
        this.invalidatePipe();
        this.setDevClip(this.surfaceData.getBounds());
        if (this.paintState <= 1) {
            this.validateColor();
        }
        if (this.composite instanceof XORComposite) {
            Color c = ((XORComposite)this.composite).getXorColor();
            this.setComposite(new XORComposite(c, this.surfaceData));
        }
        this.validatePipe();
    }

    @Override
    public void clearRect(int x, int y, int w, int h) {
        Composite c = this.composite;
        Paint p = this.paint;
        this.setComposite(AlphaComposite.Src);
        this.setColor(this.getBackground());
        this.fillRect(x, y, w, h);
        this.setPaint(p);
        this.setComposite(c);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void draw(Shape s) {
        try {
            this.shapepipe.draw(this, s);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.shapepipe.draw(this, s);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void fill(Shape s) {
        try {
            this.shapepipe.fill(this, s);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.shapepipe.fill(this, s);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    private static boolean isIntegerTranslation(AffineTransform xform) {
        if (xform.isIdentity()) {
            return true;
        }
        if (xform.getType() == 1) {
            double tx = xform.getTranslateX();
            double ty = xform.getTranslateY();
            return tx == (double)((int)tx) && ty == (double)((int)ty);
        }
        return false;
    }

    private static int getTileIndex(int p, int tileGridOffset, int tileSize) {
        if ((p -= tileGridOffset) < 0) {
            p += 1 - tileSize;
        }
        return p / tileSize;
    }

    private static Rectangle getImageRegion(RenderedImage img, Region compClip, AffineTransform transform, AffineTransform xform, int padX, int padY) {
        Rectangle imageRect = new Rectangle(img.getMinX(), img.getMinY(), img.getWidth(), img.getHeight());
        Rectangle result = null;
        try {
            double y1;
            double x1;
            double[] p = new double[8];
            p[0] = p[2] = (double)compClip.getLoX();
            p[4] = p[6] = (double)compClip.getHiX();
            p[1] = p[5] = (double)compClip.getLoY();
            p[3] = p[7] = (double)compClip.getHiY();
            transform.inverseTransform(p, 0, p, 0, 4);
            xform.inverseTransform(p, 0, p, 0, 4);
            double x0 = x1 = p[0];
            double y0 = y1 = p[1];
            int i = 2;
            while (i < 8) {
                int n = i++;
                double pt = p[n];
                if (pt < x0) {
                    x0 = pt;
                } else if (pt > x1) {
                    x1 = pt;
                }
                pt = p[i++];
                if (pt < y0) {
                    y0 = pt;
                    continue;
                }
                if (!(pt > y1)) continue;
                y1 = pt;
            }
            int x = (int)x0 - padX;
            int w = (int)(x1 - x0 + (double)(2 * padX));
            int y = (int)y0 - padY;
            int h = (int)(y1 - y0 + (double)(2 * padY));
            Rectangle clipRect = new Rectangle(x, y, w, h);
            result = clipRect.intersection(imageRect);
        }
        catch (NoninvertibleTransformException nte) {
            result = imageRect;
        }
        return result;
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        Region clip;
        if (img == null) {
            return;
        }
        if (img instanceof BufferedImage) {
            BufferedImage bufImg = (BufferedImage)img;
            this.drawImage(bufImg, xform, null);
            return;
        }
        boolean isIntegerTranslate = this.transformState <= 1 && SunGraphics2D.isIntegerTranslation(xform);
        int pad = isIntegerTranslate ? 0 : 3;
        try {
            clip = this.getCompClip();
        }
        catch (InvalidPipeException e) {
            return;
        }
        Rectangle region = SunGraphics2D.getImageRegion(img, clip, this.transform, xform, pad, pad);
        if (region.width <= 0 || region.height <= 0) {
            return;
        }
        if (isIntegerTranslate) {
            this.drawTranslatedRenderedImage(img, region, (int)xform.getTranslateX(), (int)xform.getTranslateY());
            return;
        }
        Raster raster = img.getData(region);
        WritableRaster wRaster = Raster.createWritableRaster(raster.getSampleModel(), raster.getDataBuffer(), null);
        int minX = raster.getMinX();
        int minY = raster.getMinY();
        int width = raster.getWidth();
        int height = raster.getHeight();
        int px = minX - raster.getSampleModelTranslateX();
        int py = minY - raster.getSampleModelTranslateY();
        if (px != 0 || py != 0 || width != wRaster.getWidth() || height != wRaster.getHeight()) {
            wRaster = wRaster.createWritableChild(px, py, width, height, 0, 0, null);
        }
        AffineTransform transXform = (AffineTransform)xform.clone();
        transXform.translate(minX, minY);
        ColorModel cm = img.getColorModel();
        BufferedImage bufImg = new BufferedImage(cm, wRaster, cm.isAlphaPremultiplied(), null);
        this.drawImage(bufImg, transXform, null);
    }

    private boolean clipTo(Rectangle destRect, Rectangle clip) {
        int x1 = Math.max(destRect.x, clip.x);
        int x2 = Math.min(destRect.x + destRect.width, clip.x + clip.width);
        int y1 = Math.max(destRect.y, clip.y);
        int y2 = Math.min(destRect.y + destRect.height, clip.y + clip.height);
        if (x2 - x1 < 0 || y2 - y1 < 0) {
            destRect.width = -1;
            destRect.height = -1;
            return false;
        }
        destRect.x = x1;
        destRect.y = y1;
        destRect.width = x2 - x1;
        destRect.height = y2 - y1;
        return true;
    }

    private void drawTranslatedRenderedImage(RenderedImage img, Rectangle region, int i2uTransX, int i2uTransY) {
        int tileGridXOffset = img.getTileGridXOffset();
        int tileGridYOffset = img.getTileGridYOffset();
        int tileWidth = img.getTileWidth();
        int tileHeight = img.getTileHeight();
        int minTileX = SunGraphics2D.getTileIndex(region.x, tileGridXOffset, tileWidth);
        int minTileY = SunGraphics2D.getTileIndex(region.y, tileGridYOffset, tileHeight);
        int maxTileX = SunGraphics2D.getTileIndex(region.x + region.width - 1, tileGridXOffset, tileWidth);
        int maxTileY = SunGraphics2D.getTileIndex(region.y + region.height - 1, tileGridYOffset, tileHeight);
        ColorModel colorModel = img.getColorModel();
        Rectangle tileRect = new Rectangle();
        for (int ty = minTileY; ty <= maxTileY; ++ty) {
            for (int tx = minTileX; tx <= maxTileX; ++tx) {
                Raster raster = img.getTile(tx, ty);
                tileRect.x = tx * tileWidth + tileGridXOffset;
                tileRect.y = ty * tileHeight + tileGridYOffset;
                tileRect.width = tileWidth;
                tileRect.height = tileHeight;
                this.clipTo(tileRect, region);
                WritableRaster wRaster = null;
                if (raster instanceof WritableRaster) {
                    wRaster = (WritableRaster)raster;
                    wRaster = wRaster.createWritableChild(tileRect.x, tileRect.y, tileRect.width, tileRect.height, 0, 0, null);
                } else {
                    wRaster = Raster.createWritableRaster(raster.getSampleModel(), raster.getDataBuffer(), null);
                }
                BufferedImage bufImg = new BufferedImage(colorModel, wRaster, colorModel.isAlphaPremultiplied(), null);
                this.copyImage(bufImg, tileRect.x + i2uTransX, tileRect.y + i2uTransY, 0, 0, tileRect.width, tileRect.height, null, null);
            }
        }
    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        AffineTransform reverseTransform;
        if (img == null) {
            return;
        }
        AffineTransform pipeTransform = this.transform;
        AffineTransform concatTransform = new AffineTransform(xform);
        concatTransform.concatenate(pipeTransform);
        RenderContext rc = new RenderContext(concatTransform);
        try {
            reverseTransform = pipeTransform.createInverse();
        }
        catch (NoninvertibleTransformException nte) {
            rc = new RenderContext(pipeTransform);
            reverseTransform = new AffineTransform();
        }
        RenderedImage rendering = img.createRendering(rc);
        this.drawRenderedImage(rendering, reverseTransform);
    }

    protected Rectangle transformBounds(Rectangle rect, AffineTransform tx) {
        if (tx.isIdentity()) {
            return rect;
        }
        Shape s = SunGraphics2D.transformShape(tx, rect);
        return s.getBounds();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawString(String str, int x, int y) {
        if (str == null) {
            throw new NullPointerException("String is null");
        }
        if (this.font.hasLayoutAttributes()) {
            if (str.length() == 0) {
                return;
            }
            new TextLayout(str, this.font, this.getFontRenderContext()).draw(this, x, y);
            return;
        }
        try {
            this.textpipe.drawString(this, str, x, y);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.textpipe.drawString(this, str, x, y);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawString(String str, float x, float y) {
        if (str == null) {
            throw new NullPointerException("String is null");
        }
        if (this.font.hasLayoutAttributes()) {
            if (str.length() == 0) {
                return;
            }
            new TextLayout(str, this.font, this.getFontRenderContext()).draw(this, x, y);
            return;
        }
        try {
            this.textpipe.drawString(this, str, x, y);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.textpipe.drawString(this, str, x, y);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        if (iterator == null) {
            throw new NullPointerException("AttributedCharacterIterator is null");
        }
        if (iterator.getBeginIndex() == iterator.getEndIndex()) {
            return;
        }
        TextLayout tl = new TextLayout(iterator, this.getFontRenderContext());
        tl.draw(this, x, y);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        if (iterator == null) {
            throw new NullPointerException("AttributedCharacterIterator is null");
        }
        if (iterator.getBeginIndex() == iterator.getEndIndex()) {
            return;
        }
        TextLayout tl = new TextLayout(iterator, this.getFontRenderContext());
        tl.draw(this, x, y);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawGlyphVector(GlyphVector gv, float x, float y) {
        if (gv == null) {
            throw new NullPointerException("GlyphVector is null");
        }
        try {
            this.textpipe.drawGlyphVector(this, gv, x, y);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.textpipe.drawGlyphVector(this, gv, x, y);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawChars(char[] data, int offset, int length, int x, int y) {
        if (data == null) {
            throw new NullPointerException("char data is null");
        }
        if (offset < 0 || length < 0 || offset + length < length || offset + length > data.length) {
            throw new ArrayIndexOutOfBoundsException("bad offset/length");
        }
        if (this.font.hasLayoutAttributes()) {
            if (data.length == 0) {
                return;
            }
            new TextLayout(new String(data, offset, length), this.font, this.getFontRenderContext()).draw(this, x, y);
            return;
        }
        try {
            this.textpipe.drawChars(this, data, offset, length, x, y);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.textpipe.drawChars(this, data, offset, length, x, y);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawBytes(byte[] data, int offset, int length, int x, int y) {
        if (data == null) {
            throw new NullPointerException("byte data is null");
        }
        if (offset < 0 || length < 0 || offset + length < length || offset + length > data.length) {
            throw new ArrayIndexOutOfBoundsException("bad offset/length");
        }
        char[] chData = new char[length];
        int i = length;
        while (i-- > 0) {
            chData[i] = (char)(data[i + offset] & 0xFF);
        }
        if (this.font.hasLayoutAttributes()) {
            if (data.length == 0) {
                return;
            }
            new TextLayout(new String(chData), this.font, this.getFontRenderContext()).draw(this, x, y);
            return;
        }
        try {
            this.textpipe.drawChars(this, chData, 0, length, x, y);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.textpipe.drawChars(this, chData, 0, length, x, y);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    private Boolean drawHiDPIImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer, AffineTransform xform) {
        try {
            int height;
            int width;
            MultiResolutionImage mrImage;
            Image resolutionVariant;
            if (img instanceof VolatileImage) {
                SurfaceData sd = SurfaceManager.getManager(img).getPrimarySurfaceData();
                double scaleX = sd.getDefaultScaleX();
                double scaleY = sd.getDefaultScaleY();
                if (scaleX == 1.0 && scaleY == 1.0) {
                    return null;
                }
                sx1 = Region.clipRound((double)sx1 * scaleX);
                sx2 = Region.clipRound((double)sx2 * scaleX);
                sy1 = Region.clipRound((double)sy1 * scaleY);
                sy2 = Region.clipRound((double)sy2 * scaleY);
                AffineTransform tx = null;
                if (xform != null) {
                    tx = new AffineTransform(this.transform);
                    this.transform(xform);
                }
                boolean result = this.scaleImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
                if (tx != null) {
                    this.transform.setTransform(tx);
                    this.invalidateTransform();
                }
                return result;
            }
            if (img instanceof MultiResolutionImage && (resolutionVariant = this.getResolutionVariant(mrImage = (MultiResolutionImage)((Object)img), width = img.getWidth(observer), height = img.getHeight(observer), dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, xform)) != img && resolutionVariant != null) {
                ImageObserver rvObserver = MultiResolutionToolkitImage.getResolutionVariantObserver(img, observer, width, height, -1, -1);
                int rvWidth = resolutionVariant.getWidth(rvObserver);
                int rvHeight = resolutionVariant.getHeight(rvObserver);
                if (rvWidth < 0 || rvHeight < 0) {
                    resolutionVariant = mrImage.getResolutionVariant(width, height);
                    rvWidth = resolutionVariant.getWidth(rvObserver);
                    rvHeight = resolutionVariant.getHeight(rvObserver);
                }
                if (0 < width && 0 < height && 0 < rvWidth && 0 < rvHeight) {
                    double widthScale = (double)rvWidth / (double)width;
                    double heightScale = (double)rvHeight / (double)height;
                    if (resolutionVariant instanceof VolatileImage) {
                        SurfaceData sd = SurfaceManager.getManager(resolutionVariant).getPrimarySurfaceData();
                        widthScale *= sd.getDefaultScaleX();
                        heightScale *= sd.getDefaultScaleY();
                    }
                    sx1 = Region.clipScale(sx1, widthScale);
                    sy1 = Region.clipScale(sy1, heightScale);
                    sx2 = Region.clipScale(sx2, widthScale);
                    sy2 = Region.clipScale(sy2, heightScale);
                    observer = rvObserver;
                    img = resolutionVariant;
                    if (xform != null) {
                        assert (dx1 == 0 && dy1 == 0);
                        AffineTransform renderTX = new AffineTransform(xform);
                        renderTX.scale(1.0 / widthScale, 1.0 / heightScale);
                        return this.transformImage(img, renderTX, observer);
                    }
                    return this.scaleImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
                }
                return false;
            }
        }
        catch (InvalidPipeException e) {
            return false;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean scaleImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        try {
            boolean bl = this.imagepipe.scaleImage(this, img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
            return bl;
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                boolean bl = this.imagepipe.scaleImage(this, img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
                return bl;
            }
            catch (InvalidPipeException e2) {
                boolean bl = false;
                return bl;
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean transformImage(Image img, AffineTransform xform, ImageObserver observer) {
        try {
            boolean bl = this.imagepipe.transformImage(this, img, xform, observer);
            return bl;
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                boolean bl = this.imagepipe.transformImage(this, img, xform, observer);
                return bl;
            }
            catch (InvalidPipeException e2) {
                boolean bl = false;
                return bl;
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    private Image getResolutionVariant(MultiResolutionImage img, int srcWidth, int srcHeight, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, AffineTransform xform) {
        double destImageHeight;
        double destImageWidth;
        AffineTransform tx;
        if (srcWidth <= 0 || srcHeight <= 0) {
            return null;
        }
        int sw = sx2 - sx1;
        int sh = sy2 - sy1;
        if (sw == 0 || sh == 0) {
            return null;
        }
        if (xform == null) {
            tx = this.transform;
        } else {
            tx = new AffineTransform(this.transform);
            tx.concatenate(xform);
        }
        int type = tx.getType();
        int dw = dx2 - dx1;
        int dh = dy2 - dy1;
        if (this.resolutionVariantHint == 1) {
            destImageWidth = srcWidth;
            destImageHeight = srcHeight;
        } else if (this.resolutionVariantHint == 3) {
            AffineTransform configTransform = this.getDefaultTransform();
            if (configTransform.isIdentity()) {
                destImageWidth = srcWidth;
                destImageHeight = srcHeight;
            } else {
                destImageWidth = (double)srcWidth * configTransform.getScaleX();
                destImageHeight = (double)srcHeight * configTransform.getScaleY();
            }
        } else {
            double destRegionHeight;
            double destRegionWidth;
            if ((type & 0xFFFFFFBE) == 0) {
                destRegionWidth = dw;
                destRegionHeight = dh;
            } else if ((type & 0xFFFFFFB8) == 0) {
                destRegionWidth = (double)dw * tx.getScaleX();
                destRegionHeight = (double)dh * tx.getScaleY();
            } else {
                destRegionWidth = (double)dw * Math.hypot(tx.getScaleX(), tx.getShearY());
                destRegionHeight = (double)dh * Math.hypot(tx.getShearX(), tx.getScaleY());
            }
            destImageWidth = Math.abs((double)srcWidth * destRegionWidth / (double)sw);
            destImageHeight = Math.abs((double)srcHeight * destRegionHeight / (double)sh);
        }
        Image resolutionVariant = img.getResolutionVariant(destImageWidth, destImageHeight);
        if (resolutionVariant instanceof ToolkitImage && ((ToolkitImage)resolutionVariant).hasError()) {
            return null;
        }
        return resolutionVariant;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        return this.drawImage(img, x, y, width, height, null, observer);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean copyImage(Image img, int dx, int dy, int sx, int sy, int width, int height, Color bgcolor, ImageObserver observer) {
        try {
            boolean bl = this.imagepipe.copyImage(this, img, dx, dy, sx, sy, width, height, bgcolor, observer);
            return bl;
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                boolean bl = this.imagepipe.copyImage(this, img, dx, dy, sx, sy, width, height, bgcolor, observer);
                return bl;
            }
            catch (InvalidPipeException e2) {
                boolean bl = false;
                return bl;
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bg, ImageObserver observer) {
        int imgH;
        if (img == null) {
            return true;
        }
        if (width == 0 || height == 0) {
            return true;
        }
        int imgW = img.getWidth(null);
        Boolean hidpiImageDrawn = this.drawHiDPIImage(img, x, y, x + width, y + height, 0, 0, imgW, imgH = img.getHeight(null), bg, observer, null);
        if (hidpiImageDrawn != null) {
            return hidpiImageDrawn;
        }
        if (width == imgW && height == imgH) {
            return this.copyImage(img, x, y, 0, 0, width, height, bg, observer);
        }
        try {
            boolean bl = this.imagepipe.scaleImage(this, img, x, y, width, height, bg, observer);
            return bl;
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                boolean bl = this.imagepipe.scaleImage(this, img, x, y, width, height, bg, observer);
                return bl;
            }
            catch (InvalidPipeException e2) {
                boolean bl = false;
                return bl;
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return this.drawImage(img, x, y, null, observer);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean drawImage(Image img, int x, int y, Color bg, ImageObserver observer) {
        int imgH;
        if (img == null) {
            return true;
        }
        int imgW = img.getWidth(null);
        Boolean hidpiImageDrawn = this.drawHiDPIImage(img, x, y, x + imgW, y + (imgH = img.getHeight(null)), 0, 0, imgW, imgH, bg, observer, null);
        if (hidpiImageDrawn != null) {
            return hidpiImageDrawn;
        }
        try {
            boolean bl = this.imagepipe.copyImage(this, img, x, y, bg, observer);
            return bl;
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                boolean bl = this.imagepipe.copyImage(this, img, x, y, bg, observer);
                return bl;
            }
            catch (InvalidPipeException e2) {
                boolean bl = false;
                return bl;
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return this.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null, observer);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        if (img == null) {
            return true;
        }
        if (dx1 == dx2 || dy1 == dy2 || sx1 == sx2 || sy1 == sy2) {
            return true;
        }
        Boolean hidpiImageDrawn = this.drawHiDPIImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer, null);
        if (hidpiImageDrawn != null) {
            return hidpiImageDrawn;
        }
        if (sx2 - sx1 == dx2 - dx1 && sy2 - sy1 == dy2 - dy1) {
            int dstY;
            int srcY;
            int height;
            int dstX;
            int srcX;
            int width;
            if (sx2 > sx1) {
                width = sx2 - sx1;
                srcX = sx1;
                dstX = dx1;
            } else {
                width = sx1 - sx2;
                srcX = sx2;
                dstX = dx2;
            }
            if (sy2 > sy1) {
                height = sy2 - sy1;
                srcY = sy1;
                dstY = dy1;
            } else {
                height = sy1 - sy2;
                srcY = sy2;
                dstY = dy2;
            }
            return this.copyImage(img, dstX, dstY, srcX, srcY, width, height, bgcolor, observer);
        }
        try {
            boolean srcX = this.imagepipe.scaleImage(this, img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
            return srcX;
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                boolean srcY = this.imagepipe.scaleImage(this, img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
                return srcY;
            }
            catch (InvalidPipeException e2) {
                boolean bl = false;
                return bl;
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver observer) {
        int h;
        if (img == null) {
            return true;
        }
        if (xform == null || xform.isIdentity()) {
            return this.drawImage(img, 0, 0, null, observer);
        }
        int w = img.getWidth(null);
        Boolean hidpiImageDrawn = this.drawHiDPIImage(img, 0, 0, w, h = img.getHeight(null), 0, 0, w, h, null, observer, xform);
        if (hidpiImageDrawn != null) {
            return hidpiImageDrawn;
        }
        return this.transformImage(img, xform, observer);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawImage(BufferedImage bImg, BufferedImageOp op, int x, int y) {
        if (bImg == null) {
            return;
        }
        try {
            this.imagepipe.transformImage(this, bImg, op, x, y);
        }
        catch (InvalidPipeException e) {
            try {
                this.revalidateAll();
                this.imagepipe.transformImage(this, bImg, op, x, y);
            }
            catch (InvalidPipeException invalidPipeException) {
                // empty catch block
            }
        }
        finally {
            this.surfaceData.markDirty();
        }
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        if (this.cachedFRC == null) {
            int aahint = this.textAntialiasHint;
            if (aahint == 0 && this.antialiasHint == 2) {
                aahint = 2;
            }
            AffineTransform tx = null;
            if (this.transformState >= 3) {
                tx = this.transform.getTranslateX() == 0.0 && this.transform.getTranslateY() == 0.0 ? this.transform : new AffineTransform(this.transform.getScaleX(), this.transform.getShearY(), this.transform.getShearX(), this.transform.getScaleY(), 0.0, 0.0);
            }
            this.cachedFRC = new FontRenderContext(tx, SunHints.Value.get(2, aahint), SunHints.Value.get(3, this.fractionalMetricsHint));
        }
        return this.cachedFRC;
    }

    @Override
    public void dispose() {
        this.surfaceData = NullSurfaceData.theInstance;
        this.invalidatePipe();
    }

    @Override
    public void finalize() {
    }

    public Object getDestination() {
        return this.surfaceData.getDestination();
    }

    @Override
    public Surface getDestSurface() {
        return this.surfaceData;
    }

    static {
        if (PerformanceLogger.loggingEnabled()) {
            PerformanceLogger.setTime("SunGraphics2D static initialization");
        }
        invalidpipe = new ValidatePipe();
        IDENT_MATRIX = new double[]{1.0, 0.0, 0.0, 1.0};
        IDENT_ATX = new AffineTransform();
        textTxArr = new double[17][];
        textAtArr = new AffineTransform[17];
        for (int i = 8; i < 17; ++i) {
            SunGraphics2D.textTxArr[i] = new double[]{i, 0.0, 0.0, i};
            SunGraphics2D.textAtArr[i] = new AffineTransform(textTxArr[i]);
        }
        MinPenSizeAA = RenderingEngine.getInstance().getMinimumAAPenSize();
        MinPenSizeAASquared = MinPenSizeAA * MinPenSizeAA;
    }
}

