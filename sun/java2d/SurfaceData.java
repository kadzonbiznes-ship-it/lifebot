/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d;

import java.awt.AWTPermission;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.security.Permission;
import sun.awt.image.SurfaceManager;
import sun.font.FontUtilities;
import sun.java2d.DisposerTarget;
import sun.java2d.InvalidPipeException;
import sun.java2d.NullSurfaceData;
import sun.java2d.StateTrackable;
import sun.java2d.StateTrackableDelegate;
import sun.java2d.StateTracker;
import sun.java2d.SunGraphics2D;
import sun.java2d.Surface;
import sun.java2d.SurfaceDataProxy;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.DrawGlyphList;
import sun.java2d.loops.DrawGlyphListAA;
import sun.java2d.loops.DrawGlyphListColor;
import sun.java2d.loops.DrawGlyphListLCD;
import sun.java2d.loops.DrawLine;
import sun.java2d.loops.DrawParallelogram;
import sun.java2d.loops.DrawPath;
import sun.java2d.loops.DrawPolygons;
import sun.java2d.loops.DrawRect;
import sun.java2d.loops.FillParallelogram;
import sun.java2d.loops.FillPath;
import sun.java2d.loops.FillRect;
import sun.java2d.loops.FillSpans;
import sun.java2d.loops.MaskFill;
import sun.java2d.loops.RenderCache;
import sun.java2d.loops.RenderLoops;
import sun.java2d.loops.SurfaceType;
import sun.java2d.pipe.AAShapePipe;
import sun.java2d.pipe.AATextRenderer;
import sun.java2d.pipe.AlphaColorPipe;
import sun.java2d.pipe.AlphaPaintPipe;
import sun.java2d.pipe.CompositePipe;
import sun.java2d.pipe.DrawImage;
import sun.java2d.pipe.DrawImagePipe;
import sun.java2d.pipe.GeneralCompositePipe;
import sun.java2d.pipe.LCDTextRenderer;
import sun.java2d.pipe.LoopBasedPipe;
import sun.java2d.pipe.LoopPipe;
import sun.java2d.pipe.OutlineTextRenderer;
import sun.java2d.pipe.ParallelogramPipe;
import sun.java2d.pipe.PixelToParallelogramConverter;
import sun.java2d.pipe.PixelToShapeConverter;
import sun.java2d.pipe.ShapeDrawPipe;
import sun.java2d.pipe.SolidTextRenderer;
import sun.java2d.pipe.SpanClipRenderer;
import sun.java2d.pipe.SpanShapeRenderer;
import sun.java2d.pipe.TextPipe;
import sun.java2d.pipe.TextRenderer;

public abstract class SurfaceData
implements Transparency,
DisposerTarget,
StateTrackable,
Surface {
    private long pData;
    private boolean valid;
    private boolean surfaceLost;
    private SurfaceType surfaceType;
    private ColorModel colorModel;
    private Object disposerReferent = new Object();
    private Object blitProxyKey;
    private StateTrackableDelegate stateDelegate;
    protected static final LoopPipe colorPrimitives;
    public static final TextPipe outlineTextRenderer;
    public static final TextPipe solidTextRenderer;
    public static final TextPipe aaTextRenderer;
    public static final TextPipe lcdTextRenderer;
    protected static final AlphaColorPipe colorPipe;
    protected static final PixelToShapeConverter colorViaShape;
    protected static final PixelToParallelogramConverter colorViaPgram;
    protected static final TextPipe colorText;
    protected static final CompositePipe clipColorPipe;
    protected static final TextPipe clipColorText;
    protected static final AAShapePipe AAColorShape;
    protected static final PixelToParallelogramConverter AAColorViaShape;
    protected static final PixelToParallelogramConverter AAColorViaPgram;
    protected static final AAShapePipe AAClipColorShape;
    protected static final PixelToParallelogramConverter AAClipColorViaShape;
    protected static final CompositePipe paintPipe;
    protected static final SpanShapeRenderer paintShape;
    protected static final PixelToShapeConverter paintViaShape;
    protected static final TextPipe paintText;
    protected static final CompositePipe clipPaintPipe;
    protected static final TextPipe clipPaintText;
    protected static final AAShapePipe AAPaintShape;
    protected static final PixelToParallelogramConverter AAPaintViaShape;
    protected static final AAShapePipe AAClipPaintShape;
    protected static final PixelToParallelogramConverter AAClipPaintViaShape;
    protected static final CompositePipe compPipe;
    protected static final SpanShapeRenderer compShape;
    protected static final PixelToShapeConverter compViaShape;
    protected static final TextPipe compText;
    protected static final CompositePipe clipCompPipe;
    protected static final TextPipe clipCompText;
    protected static final AAShapePipe AACompShape;
    protected static final PixelToParallelogramConverter AACompViaShape;
    protected static final AAShapePipe AAClipCompShape;
    protected static final PixelToParallelogramConverter AAClipCompViaShape;
    protected static final DrawImagePipe imagepipe;
    static final int LOOP_UNKNOWN = 0;
    static final int LOOP_FOUND = 1;
    static final int LOOP_NOTFOUND = 2;
    int haveLCDLoop;
    int havePgramXORLoop;
    int havePgramSolidLoop;
    private static RenderCache loopcache;
    static Permission compPermission;

    private static native void initIDs();

    protected SurfaceData(SurfaceType surfaceType, ColorModel cm) {
        this(StateTrackable.State.STABLE, surfaceType, cm);
    }

    protected SurfaceData(StateTrackable.State state, SurfaceType surfaceType, ColorModel cm) {
        this(StateTrackableDelegate.createInstance(state), surfaceType, cm);
    }

    protected SurfaceData(StateTrackableDelegate trackable, SurfaceType surfaceType, ColorModel cm) {
        this.stateDelegate = trackable;
        this.colorModel = cm;
        this.surfaceType = surfaceType;
        this.valid = true;
    }

    protected SurfaceData(StateTrackable.State state) {
        this.stateDelegate = StateTrackableDelegate.createInstance(state);
        this.valid = true;
    }

    protected void setBlitProxyKey(Object key) {
        if (SurfaceDataProxy.isCachingAllowed()) {
            this.blitProxyKey = key;
        }
    }

    public SurfaceData getSourceSurfaceData(Image img, int txtype, CompositeType comp, Color bgColor) {
        SurfaceManager srcMgr = SurfaceManager.getManager(img);
        SurfaceData srcData = srcMgr.getPrimarySurfaceData();
        if (img.getAccelerationPriority() > 0.0f && this.blitProxyKey != null) {
            SurfaceDataProxy sdp = (SurfaceDataProxy)srcMgr.getCacheData(this.blitProxyKey);
            if (sdp == null || !sdp.isValid()) {
                sdp = srcData.getState() == StateTrackable.State.UNTRACKABLE ? SurfaceDataProxy.UNCACHED : this.makeProxyFor(srcData);
                srcMgr.setCacheData(this.blitProxyKey, sdp);
            }
            srcData = sdp.replaceData(srcData, txtype, comp, bgColor);
        }
        return srcData;
    }

    public SurfaceDataProxy makeProxyFor(SurfaceData srcData) {
        return SurfaceDataProxy.UNCACHED;
    }

    public static SurfaceData getPrimarySurfaceData(Image img) {
        SurfaceManager sMgr = SurfaceManager.getManager(img);
        return sMgr.getPrimarySurfaceData();
    }

    public static SurfaceData restoreContents(Image img) {
        SurfaceManager sMgr = SurfaceManager.getManager(img);
        return sMgr.restoreContents();
    }

    @Override
    public StateTrackable.State getState() {
        return this.stateDelegate.getState();
    }

    @Override
    public StateTracker getStateTracker() {
        return this.stateDelegate.getStateTracker();
    }

    public final void markDirty() {
        this.stateDelegate.markDirty();
    }

    public void setSurfaceLost(boolean lost) {
        this.surfaceLost = lost;
        this.stateDelegate.markDirty();
    }

    public boolean isSurfaceLost() {
        return this.surfaceLost;
    }

    public final boolean isValid() {
        return this.valid;
    }

    @Override
    public Object getDisposerReferent() {
        return this.disposerReferent;
    }

    public long getNativeOps() {
        return this.pData;
    }

    public void invalidate() {
        this.valid = false;
        this.stateDelegate.markDirty();
    }

    public abstract SurfaceData getReplacement();

    private static PixelToParallelogramConverter makeConverter(AAShapePipe renderer, ParallelogramPipe pgrampipe) {
        return new PixelToParallelogramConverter(renderer, pgrampipe, 0.125, 0.499, false);
    }

    private static PixelToParallelogramConverter makeConverter(AAShapePipe renderer) {
        return SurfaceData.makeConverter(renderer, renderer);
    }

    public boolean canRenderLCDText(SunGraphics2D sg2d) {
        if (sg2d.compositeState <= 0 && sg2d.paintState <= 1 && sg2d.clipState <= 1 && sg2d.surfaceData.getTransparency() == 1) {
            if (this.haveLCDLoop == 0) {
                DrawGlyphListLCD loop = DrawGlyphListLCD.locate(SurfaceType.AnyColor, CompositeType.SrcNoEa, this.getSurfaceType());
                this.haveLCDLoop = loop != null ? 1 : 2;
            }
            return this.haveLCDLoop == 1;
        }
        return false;
    }

    public boolean canRenderParallelograms(SunGraphics2D sg2d) {
        if (sg2d.paintState <= 1) {
            if (sg2d.compositeState == 2) {
                if (this.havePgramXORLoop == 0) {
                    FillParallelogram loop = FillParallelogram.locate(SurfaceType.AnyColor, CompositeType.Xor, this.getSurfaceType());
                    this.havePgramXORLoop = loop != null ? 1 : 2;
                }
                return this.havePgramXORLoop == 1;
            }
            if (sg2d.compositeState <= 0 && sg2d.antialiasHint != 2 && sg2d.clipState != 2) {
                if (this.havePgramSolidLoop == 0) {
                    FillParallelogram loop = FillParallelogram.locate(SurfaceType.AnyColor, CompositeType.SrcNoEa, this.getSurfaceType());
                    this.havePgramSolidLoop = loop != null ? 1 : 2;
                }
                return this.havePgramSolidLoop == 1;
            }
        }
        return false;
    }

    public void validatePipe(SunGraphics2D sg2d) {
        sg2d.imagepipe = imagepipe;
        if (sg2d.compositeState == 2) {
            if (sg2d.paintState > 1) {
                sg2d.drawpipe = paintViaShape;
                sg2d.fillpipe = paintViaShape;
                sg2d.shapepipe = paintShape;
                sg2d.textpipe = outlineTextRenderer;
            } else {
                PixelToShapeConverter converter;
                if (this.canRenderParallelograms(sg2d)) {
                    converter = colorViaPgram;
                    sg2d.shapepipe = colorViaPgram;
                } else {
                    converter = colorViaShape;
                    sg2d.shapepipe = colorPrimitives;
                }
                if (sg2d.clipState == 2) {
                    sg2d.drawpipe = converter;
                    sg2d.fillpipe = converter;
                    sg2d.textpipe = outlineTextRenderer;
                } else {
                    if (sg2d.transformState >= 3) {
                        sg2d.drawpipe = converter;
                        sg2d.fillpipe = converter;
                    } else {
                        sg2d.drawpipe = sg2d.strokeState != 0 ? converter : colorPrimitives;
                        sg2d.fillpipe = colorPrimitives;
                    }
                    sg2d.textpipe = solidTextRenderer;
                }
            }
        } else if (sg2d.compositeState == 3) {
            if (sg2d.antialiasHint == 2) {
                if (sg2d.clipState == 2) {
                    sg2d.drawpipe = AAClipCompViaShape;
                    sg2d.fillpipe = AAClipCompViaShape;
                    sg2d.shapepipe = AAClipCompViaShape;
                    sg2d.textpipe = clipCompText;
                } else {
                    sg2d.drawpipe = AACompViaShape;
                    sg2d.fillpipe = AACompViaShape;
                    sg2d.shapepipe = AACompViaShape;
                    sg2d.textpipe = compText;
                }
            } else {
                sg2d.drawpipe = compViaShape;
                sg2d.fillpipe = compViaShape;
                sg2d.shapepipe = compShape;
                sg2d.textpipe = sg2d.clipState == 2 ? clipCompText : compText;
            }
        } else if (sg2d.antialiasHint == 2) {
            sg2d.alphafill = this.getMaskFill(sg2d);
            if (sg2d.alphafill != null) {
                if (sg2d.clipState == 2) {
                    sg2d.drawpipe = AAClipColorViaShape;
                    sg2d.fillpipe = AAClipColorViaShape;
                    sg2d.shapepipe = AAClipColorViaShape;
                    sg2d.textpipe = clipColorText;
                } else {
                    PixelToParallelogramConverter converter = sg2d.alphafill.canDoParallelograms() ? AAColorViaPgram : AAColorViaShape;
                    sg2d.drawpipe = converter;
                    sg2d.fillpipe = converter;
                    sg2d.shapepipe = converter;
                    sg2d.textpipe = sg2d.paintState > 1 || sg2d.compositeState > 0 ? colorText : this.getTextPipe(sg2d, true);
                }
            } else if (sg2d.clipState == 2) {
                sg2d.drawpipe = AAClipPaintViaShape;
                sg2d.fillpipe = AAClipPaintViaShape;
                sg2d.shapepipe = AAClipPaintViaShape;
                sg2d.textpipe = clipPaintText;
            } else {
                sg2d.drawpipe = AAPaintViaShape;
                sg2d.fillpipe = AAPaintViaShape;
                sg2d.shapepipe = AAPaintViaShape;
                sg2d.textpipe = paintText;
            }
        } else if (sg2d.paintState > 1 || sg2d.compositeState > 0 || sg2d.clipState == 2) {
            sg2d.drawpipe = paintViaShape;
            sg2d.fillpipe = paintViaShape;
            sg2d.shapepipe = paintShape;
            sg2d.alphafill = this.getMaskFill(sg2d);
            sg2d.textpipe = sg2d.alphafill != null ? (sg2d.clipState == 2 ? clipColorText : colorText) : (sg2d.clipState == 2 ? clipPaintText : paintText);
        } else {
            PixelToShapeConverter converter;
            if (this.canRenderParallelograms(sg2d)) {
                converter = colorViaPgram;
                sg2d.shapepipe = colorViaPgram;
            } else {
                converter = colorViaShape;
                sg2d.shapepipe = colorPrimitives;
            }
            if (sg2d.transformState >= 3) {
                sg2d.drawpipe = converter;
                sg2d.fillpipe = converter;
            } else {
                sg2d.drawpipe = sg2d.strokeState != 0 ? converter : colorPrimitives;
                sg2d.fillpipe = colorPrimitives;
            }
            sg2d.textpipe = this.getTextPipe(sg2d, false);
        }
        if (sg2d.textpipe instanceof LoopBasedPipe || sg2d.shapepipe instanceof LoopBasedPipe || sg2d.fillpipe instanceof LoopBasedPipe || sg2d.drawpipe instanceof LoopBasedPipe || sg2d.imagepipe instanceof LoopBasedPipe) {
            sg2d.loops = this.getRenderLoops(sg2d);
        }
    }

    private TextPipe getTextPipe(SunGraphics2D sg2d, boolean aaHintIsOn) {
        switch (sg2d.textAntialiasHint) {
            case 0: {
                if (aaHintIsOn) {
                    return aaTextRenderer;
                }
                return solidTextRenderer;
            }
            case 1: {
                return solidTextRenderer;
            }
            case 2: {
                return aaTextRenderer;
            }
        }
        switch (sg2d.getFontInfo().aaHint) {
            case 4: 
            case 6: {
                return lcdTextRenderer;
            }
            case 2: {
                return aaTextRenderer;
            }
            case 1: {
                return solidTextRenderer;
            }
        }
        if (aaHintIsOn) {
            return aaTextRenderer;
        }
        return solidTextRenderer;
    }

    private static SurfaceType getPaintSurfaceType(SunGraphics2D sg2d) {
        switch (sg2d.paintState) {
            case 0: {
                return SurfaceType.OpaqueColor;
            }
            case 1: {
                return SurfaceType.AnyColor;
            }
            case 2: {
                if (sg2d.paint.getTransparency() == 1) {
                    return SurfaceType.OpaqueGradientPaint;
                }
                return SurfaceType.GradientPaint;
            }
            case 3: {
                if (sg2d.paint.getTransparency() == 1) {
                    return SurfaceType.OpaqueLinearGradientPaint;
                }
                return SurfaceType.LinearGradientPaint;
            }
            case 4: {
                if (sg2d.paint.getTransparency() == 1) {
                    return SurfaceType.OpaqueRadialGradientPaint;
                }
                return SurfaceType.RadialGradientPaint;
            }
            case 5: {
                if (sg2d.paint.getTransparency() == 1) {
                    return SurfaceType.OpaqueTexturePaint;
                }
                return SurfaceType.TexturePaint;
            }
        }
        return SurfaceType.AnyPaint;
    }

    private static CompositeType getFillCompositeType(SunGraphics2D sg2d) {
        CompositeType compType = sg2d.imageComp;
        if (sg2d.compositeState == 0) {
            compType = compType == CompositeType.SrcOverNoEa ? CompositeType.OpaqueSrcOverNoEa : CompositeType.SrcNoEa;
        }
        return compType;
    }

    protected MaskFill getMaskFill(SunGraphics2D sg2d) {
        SurfaceType src = SurfaceData.getPaintSurfaceType(sg2d);
        CompositeType comp = SurfaceData.getFillCompositeType(sg2d);
        SurfaceType dst = this.getSurfaceType();
        return MaskFill.getFromCache(src, comp, dst);
    }

    public RenderLoops getRenderLoops(SunGraphics2D sg2d) {
        SurfaceType dst;
        CompositeType comp;
        SurfaceType src = SurfaceData.getPaintSurfaceType(sg2d);
        Object o = loopcache.get(src, comp = SurfaceData.getFillCompositeType(sg2d), dst = sg2d.getSurfaceData().getSurfaceType());
        if (o != null) {
            return (RenderLoops)o;
        }
        RenderLoops loops = SurfaceData.makeRenderLoops(src, comp, dst);
        loopcache.put(src, comp, dst, loops);
        return loops;
    }

    public static RenderLoops makeRenderLoops(SurfaceType src, CompositeType comp, SurfaceType dst) {
        RenderLoops loops = new RenderLoops();
        loops.drawLineLoop = DrawLine.locate(src, comp, dst);
        loops.fillRectLoop = FillRect.locate(src, comp, dst);
        loops.drawRectLoop = DrawRect.locate(src, comp, dst);
        loops.drawPolygonsLoop = DrawPolygons.locate(src, comp, dst);
        loops.drawPathLoop = DrawPath.locate(src, comp, dst);
        loops.fillPathLoop = FillPath.locate(src, comp, dst);
        loops.fillSpansLoop = FillSpans.locate(src, comp, dst);
        loops.fillParallelogramLoop = FillParallelogram.locate(src, comp, dst);
        loops.drawParallelogramLoop = DrawParallelogram.locate(src, comp, dst);
        loops.drawGlyphListLoop = DrawGlyphList.locate(src, comp, dst);
        loops.drawGlyphListAALoop = DrawGlyphListAA.locate(src, comp, dst);
        loops.drawGlyphListLCDLoop = DrawGlyphListLCD.locate(src, comp, dst);
        loops.drawGlyphListColorLoop = DrawGlyphListColor.locate(src, comp, dst);
        return loops;
    }

    public abstract GraphicsConfiguration getDeviceConfiguration();

    public final SurfaceType getSurfaceType() {
        return this.surfaceType;
    }

    public final ColorModel getColorModel() {
        return this.colorModel;
    }

    @Override
    public int getTransparency() {
        return this.getColorModel().getTransparency();
    }

    public abstract Raster getRaster(int var1, int var2, int var3, int var4);

    public boolean useTightBBoxes() {
        return true;
    }

    public int pixelFor(int rgb) {
        return this.surfaceType.pixelFor(rgb, this.colorModel);
    }

    public int pixelFor(Color c) {
        return this.pixelFor(c.getRGB());
    }

    public int rgbFor(int pixel) {
        return this.surfaceType.rgbFor(pixel, this.colorModel);
    }

    public abstract Rectangle getBounds();

    protected void checkCustomComposite() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (compPermission == null) {
                compPermission = new AWTPermission("readDisplayPixels");
            }
            sm.checkPermission(compPermission);
        }
    }

    protected static native boolean isOpaqueGray(IndexColorModel var0);

    public static boolean isNull(SurfaceData sd) {
        return sd == null || sd == NullSurfaceData.theInstance;
    }

    public boolean copyArea(SunGraphics2D sg2d, int x, int y, int w, int h, int dx, int dy) {
        return false;
    }

    public void flush() {
    }

    public abstract Object getDestination();

    public double getDefaultScaleX() {
        return 1.0;
    }

    public double getDefaultScaleY() {
        return 1.0;
    }

    public static <T> T convertTo(Class<T> surfaceDataClass, SurfaceData surfaceData) {
        try {
            return surfaceDataClass.cast(surfaceData);
        }
        catch (ClassCastException e) {
            throw new InvalidPipeException("wrong surface data type: " + String.valueOf(surfaceData));
        }
    }

    static {
        SurfaceData.initIDs();
        colorPrimitives = new LoopPipe();
        outlineTextRenderer = new OutlineTextRenderer();
        aaTextRenderer = new AATextRenderer();
        solidTextRenderer = FontUtilities.isMacOSX14 ? aaTextRenderer : new SolidTextRenderer();
        lcdTextRenderer = new LCDTextRenderer();
        colorPipe = new AlphaColorPipe();
        colorViaShape = new PixelToShapeLoopConverter(colorPrimitives);
        colorViaPgram = new PixelToPgramLoopConverter(colorPrimitives, colorPrimitives, 1.0, 0.25, true);
        colorText = new TextRenderer(colorPipe);
        clipColorPipe = new SpanClipRenderer(colorPipe);
        clipColorText = new TextRenderer(clipColorPipe);
        AAColorShape = new AAShapePipe(colorPipe);
        AAColorViaShape = SurfaceData.makeConverter(AAColorShape);
        AAColorViaPgram = SurfaceData.makeConverter(AAColorShape, colorPipe);
        AAClipColorShape = new AAShapePipe(clipColorPipe);
        AAClipColorViaShape = SurfaceData.makeConverter(AAClipColorShape);
        paintPipe = new AlphaPaintPipe();
        paintShape = new SpanShapeRenderer.Composite(paintPipe);
        paintViaShape = new PixelToShapeConverter(paintShape);
        paintText = new TextRenderer(paintPipe);
        clipPaintPipe = new SpanClipRenderer(paintPipe);
        clipPaintText = new TextRenderer(clipPaintPipe);
        AAPaintShape = new AAShapePipe(paintPipe);
        AAPaintViaShape = SurfaceData.makeConverter(AAPaintShape);
        AAClipPaintShape = new AAShapePipe(clipPaintPipe);
        AAClipPaintViaShape = SurfaceData.makeConverter(AAClipPaintShape);
        compPipe = new GeneralCompositePipe();
        compShape = new SpanShapeRenderer.Composite(compPipe);
        compViaShape = new PixelToShapeConverter(compShape);
        compText = new TextRenderer(compPipe);
        clipCompPipe = new SpanClipRenderer(compPipe);
        clipCompText = new TextRenderer(clipCompPipe);
        AACompShape = new AAShapePipe(compPipe);
        AACompViaShape = SurfaceData.makeConverter(AACompShape);
        AAClipCompShape = new AAShapePipe(clipCompPipe);
        AAClipCompViaShape = SurfaceData.makeConverter(AAClipCompShape);
        imagepipe = new DrawImage();
        loopcache = new RenderCache(30);
    }

    static class PixelToShapeLoopConverter
    extends PixelToShapeConverter
    implements LoopBasedPipe {
        public PixelToShapeLoopConverter(ShapeDrawPipe pipe) {
            super(pipe);
        }
    }

    static class PixelToPgramLoopConverter
    extends PixelToParallelogramConverter
    implements LoopBasedPipe {
        public PixelToPgramLoopConverter(ShapeDrawPipe shapepipe, ParallelogramPipe pgrampipe, double minPenSize, double normPosition, boolean adjustfill) {
            super(shapepipe, pgrampipe, minPenSize, normPosition, adjustfill);
        }
    }
}

