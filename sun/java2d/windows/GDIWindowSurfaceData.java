/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.windows;

import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import sun.awt.Win32GraphicsConfig;
import sun.awt.Win32GraphicsDevice;
import sun.awt.windows.WComponentPeer;
import sun.java2d.InvalidPipeException;
import sun.java2d.ScreenUpdateManager;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.SurfaceDataProxy;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.RenderLoops;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.XORComposite;
import sun.java2d.pipe.PixelToShapeConverter;
import sun.java2d.pipe.Region;
import sun.java2d.windows.GDIBlitLoops;
import sun.java2d.windows.GDIRenderer;
import sun.java2d.windows.WindowsFlags;

public class GDIWindowSurfaceData
extends SurfaceData {
    private WComponentPeer peer;
    private Win32GraphicsConfig graphicsConfig;
    private RenderLoops solidloops;
    public static final String DESC_GDI = "GDI";
    public static final SurfaceType AnyGdi = SurfaceType.IntRgb.deriveSubType("GDI");
    public static final SurfaceType IntRgbGdi = SurfaceType.IntRgb.deriveSubType("GDI");
    public static final SurfaceType Ushort565RgbGdi = SurfaceType.Ushort565Rgb.deriveSubType("GDI");
    public static final SurfaceType Ushort555RgbGdi = SurfaceType.Ushort555Rgb.deriveSubType("GDI");
    public static final SurfaceType ThreeByteBgrGdi = SurfaceType.ThreeByteBgr.deriveSubType("GDI");
    private final double scaleX;
    private final double scaleY;
    protected static GDIRenderer gdiPipe;
    protected static PixelToShapeConverter gdiTxPipe;

    private static native void initIDs(Class<?> var0);

    public static SurfaceType getSurfaceType(ColorModel cm) {
        switch (cm.getPixelSize()) {
            case 24: 
            case 32: {
                if (cm instanceof DirectColorModel) {
                    if (((DirectColorModel)cm).getRedMask() == 0xFF0000) {
                        return IntRgbGdi;
                    }
                    return SurfaceType.IntRgbx;
                }
                return ThreeByteBgrGdi;
            }
            case 15: {
                return Ushort555RgbGdi;
            }
            case 16: {
                if (cm instanceof DirectColorModel && ((DirectColorModel)cm).getBlueMask() == 62) {
                    return SurfaceType.Ushort555Rgbx;
                }
                return Ushort565RgbGdi;
            }
            case 8: {
                if (cm.getColorSpace().getType() == 6 && cm instanceof ComponentColorModel) {
                    return SurfaceType.ByteGray;
                }
                if (cm instanceof IndexColorModel && GDIWindowSurfaceData.isOpaqueGray((IndexColorModel)cm)) {
                    return SurfaceType.Index8Gray;
                }
                return SurfaceType.ByteIndexedOpaque;
            }
        }
        throw new InvalidPipeException("Unsupported bit depth: " + cm.getPixelSize());
    }

    public static GDIWindowSurfaceData createData(WComponentPeer peer) {
        SurfaceType sType = GDIWindowSurfaceData.getSurfaceType(peer.getDeviceColorModel());
        return new GDIWindowSurfaceData(peer, sType);
    }

    @Override
    public SurfaceDataProxy makeProxyFor(SurfaceData srcData) {
        return SurfaceDataProxy.UNCACHED;
    }

    @Override
    public Raster getRaster(int x, int y, int w, int h) {
        throw new InternalError("not implemented yet");
    }

    @Override
    public void validatePipe(SunGraphics2D sg2d) {
        if (sg2d.antialiasHint != 2 && sg2d.paintState <= 1 && (sg2d.compositeState <= 0 || sg2d.compositeState == 2)) {
            if (sg2d.clipState == 2) {
                super.validatePipe(sg2d);
            } else {
                block0 : switch (sg2d.textAntialiasHint) {
                    case 0: 
                    case 1: {
                        sg2d.textpipe = solidTextRenderer;
                        break;
                    }
                    case 2: {
                        sg2d.textpipe = aaTextRenderer;
                        break;
                    }
                    default: {
                        switch (sg2d.getFontInfo().aaHint) {
                            case 4: 
                            case 6: {
                                sg2d.textpipe = lcdTextRenderer;
                                break block0;
                            }
                            case 2: {
                                sg2d.textpipe = aaTextRenderer;
                                break block0;
                            }
                        }
                        sg2d.textpipe = solidTextRenderer;
                    }
                }
            }
            sg2d.imagepipe = imagepipe;
            if (sg2d.transformState >= 3) {
                sg2d.drawpipe = gdiTxPipe;
                sg2d.fillpipe = gdiTxPipe;
            } else if (sg2d.strokeState != 0) {
                sg2d.drawpipe = gdiTxPipe;
                sg2d.fillpipe = gdiPipe;
            } else {
                sg2d.drawpipe = gdiPipe;
                sg2d.fillpipe = gdiPipe;
            }
            sg2d.shapepipe = gdiPipe;
            if (sg2d.loops == null) {
                sg2d.loops = this.getRenderLoops(sg2d);
            }
        } else {
            super.validatePipe(sg2d);
        }
    }

    @Override
    public RenderLoops getRenderLoops(SunGraphics2D sg2d) {
        if (sg2d.paintState <= 1 && sg2d.compositeState <= 0) {
            return this.solidloops;
        }
        return super.getRenderLoops(sg2d);
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return this.graphicsConfig;
    }

    private native void initOps(WComponentPeer var1, int var2, int var3, int var4, int var5, int var6);

    private GDIWindowSurfaceData(WComponentPeer peer, SurfaceType sType) {
        super(sType, peer.getDeviceColorModel());
        ColorModel cm = peer.getDeviceColorModel();
        this.peer = peer;
        int rMask = 0;
        int gMask = 0;
        int bMask = 0;
        int depth = switch (cm.getPixelSize()) {
            case 24, 32 -> {
                if (cm instanceof DirectColorModel) {
                    yield 32;
                }
                yield 24;
            }
            default -> cm.getPixelSize();
        };
        if (cm instanceof DirectColorModel) {
            DirectColorModel dcm = (DirectColorModel)cm;
            rMask = dcm.getRedMask();
            gMask = dcm.getGreenMask();
            bMask = dcm.getBlueMask();
        }
        this.graphicsConfig = (Win32GraphicsConfig)peer.getGraphicsConfiguration();
        this.solidloops = this.graphicsConfig.getSolidLoops(sType);
        Win32GraphicsDevice gd = this.graphicsConfig.getDevice();
        this.scaleX = gd.getDefaultScaleX();
        this.scaleY = gd.getDefaultScaleY();
        this.initOps(peer, depth, rMask, gMask, bMask, gd.getScreen());
        this.setBlitProxyKey(this.graphicsConfig.getProxyKey());
    }

    @Override
    public double getDefaultScaleX() {
        return this.scaleX;
    }

    @Override
    public double getDefaultScaleY() {
        return this.scaleY;
    }

    @Override
    public SurfaceData getReplacement() {
        ScreenUpdateManager mgr = ScreenUpdateManager.getInstance();
        return mgr.getReplacementScreenSurface(this.peer, this);
    }

    @Override
    public Rectangle getBounds() {
        Rectangle r = this.peer.getBounds();
        r.y = 0;
        r.x = 0;
        r.width = Region.clipRound((double)r.width * this.scaleX);
        r.height = Region.clipRound((double)r.height * this.scaleY);
        return r;
    }

    @Override
    public boolean copyArea(SunGraphics2D sg2d, int x, int y, int w, int h, int dx, int dy) {
        CompositeType comptype = sg2d.imageComp;
        if (sg2d.clipState != 2 && (CompositeType.SrcOverNoEa.equals(comptype) || CompositeType.SrcNoEa.equals(comptype))) {
            int dstx1 = x + dx;
            int dsty1 = y + dy;
            int dstx2 = dstx1 + w;
            int dsty2 = dsty1 + h;
            Region clip = sg2d.getCompClip();
            if (dstx1 < clip.getLoX()) {
                dstx1 = clip.getLoX();
            }
            if (dsty1 < clip.getLoY()) {
                dsty1 = clip.getLoY();
            }
            if (dstx2 > clip.getHiX()) {
                dstx2 = clip.getHiX();
            }
            if (dsty2 > clip.getHiY()) {
                dsty2 = clip.getHiY();
            }
            if (dstx1 < dstx2 && dsty1 < dsty2) {
                gdiPipe.devCopyArea(this, dstx1 - dx, dsty1 - dy, dx, dy, dstx2 - dstx1, dsty2 - dsty1);
            }
            return true;
        }
        return false;
    }

    private native void invalidateSD();

    @Override
    public void invalidate() {
        if (this.isValid()) {
            this.invalidateSD();
            super.invalidate();
        }
    }

    @Override
    public Object getDestination() {
        return this.peer.getTarget();
    }

    public WComponentPeer getPeer() {
        return this.peer;
    }

    static {
        GDIWindowSurfaceData.initIDs(XORComposite.class);
        if (WindowsFlags.isGdiBlitEnabled()) {
            GDIBlitLoops.register();
        }
        gdiPipe = new GDIRenderer();
        if (GraphicsPrimitive.tracingEnabled()) {
            gdiPipe = gdiPipe.traceWrap();
        }
        gdiTxPipe = new PixelToShapeConverter(gdiPipe);
    }
}

