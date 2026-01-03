/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import sun.awt.image.BufferedImageGraphicsConfig;
import sun.awt.image.ByteComponentRaster;
import sun.awt.image.BytePackedRaster;
import sun.awt.image.IntegerComponentRaster;
import sun.awt.image.ShortComponentRaster;
import sun.awt.image.SunWritableRaster;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.RenderLoops;
import sun.java2d.loops.SurfaceType;

public class BufImgSurfaceData
extends SurfaceData {
    BufferedImage bufImg;
    private BufferedImageGraphicsConfig graphicsConfig;
    RenderLoops solidloops;
    private final double scaleX;
    private final double scaleY;
    private static final int DCM_RGBX_RED_MASK = -16777216;
    private static final int DCM_RGBX_GREEN_MASK = 0xFF0000;
    private static final int DCM_RGBX_BLUE_MASK = 65280;
    private static final int DCM_555X_RED_MASK = 63488;
    private static final int DCM_555X_GREEN_MASK = 1984;
    private static final int DCM_555X_BLUE_MASK = 62;
    private static final int DCM_4444_RED_MASK = 3840;
    private static final int DCM_4444_GREEN_MASK = 240;
    private static final int DCM_4444_BLUE_MASK = 15;
    private static final int DCM_4444_ALPHA_MASK = 61440;
    private static final int DCM_ARGBBM_ALPHA_MASK = 0x1000000;
    private static final int DCM_ARGBBM_RED_MASK = 0xFF0000;
    private static final int DCM_ARGBBM_GREEN_MASK = 65280;
    private static final int DCM_ARGBBM_BLUE_MASK = 255;
    private static final int CACHE_SIZE = 5;
    private static RenderLoops[] loopcache;
    private static SurfaceType[] typecache;

    private static native void initIDs(Class<?> var0, Class<?> var1);

    public static SurfaceData createData(BufferedImage bufImg) {
        return BufImgSurfaceData.createData(bufImg, 1.0, 1.0);
    }

    public static SurfaceData createData(BufferedImage bufImg, double scaleX, double scaleY) {
        if (bufImg == null) {
            throw new NullPointerException("BufferedImage cannot be null");
        }
        ColorModel cm = bufImg.getColorModel();
        int type = bufImg.getType();
        SurfaceData sData = switch (type) {
            case 4 -> BufImgSurfaceData.createDataIC(bufImg, SurfaceType.IntBgr, scaleX, scaleY);
            case 1 -> BufImgSurfaceData.createDataIC(bufImg, SurfaceType.IntRgb, scaleX, scaleY);
            case 2 -> BufImgSurfaceData.createDataIC(bufImg, SurfaceType.IntArgb, scaleX, scaleY);
            case 3 -> BufImgSurfaceData.createDataIC(bufImg, SurfaceType.IntArgbPre, scaleX, scaleY);
            case 5 -> BufImgSurfaceData.createDataBC(bufImg, SurfaceType.ThreeByteBgr, 2, scaleX, scaleY);
            case 6 -> BufImgSurfaceData.createDataBC(bufImg, SurfaceType.FourByteAbgr, 3, scaleX, scaleY);
            case 7 -> BufImgSurfaceData.createDataBC(bufImg, SurfaceType.FourByteAbgrPre, 3, scaleX, scaleY);
            case 8 -> BufImgSurfaceData.createDataSC(bufImg, SurfaceType.Ushort565Rgb, null, scaleX, scaleY);
            case 9 -> BufImgSurfaceData.createDataSC(bufImg, SurfaceType.Ushort555Rgb, null, scaleX, scaleY);
            case 13 -> {
                SurfaceType sType = switch (cm.getTransparency()) {
                    case 1 -> {
                        if (BufImgSurfaceData.isOpaqueGray((IndexColorModel)cm)) {
                            yield SurfaceType.Index8Gray;
                        }
                        yield SurfaceType.ByteIndexedOpaque;
                    }
                    case 2 -> SurfaceType.ByteIndexedBm;
                    case 3 -> SurfaceType.ByteIndexed;
                    default -> throw new InternalError("Unrecognized transparency");
                };
                yield BufImgSurfaceData.createDataBC(bufImg, sType, 0, scaleX, scaleY);
            }
            case 10 -> BufImgSurfaceData.createDataBC(bufImg, SurfaceType.ByteGray, 0, scaleX, scaleY);
            case 11 -> BufImgSurfaceData.createDataSC(bufImg, SurfaceType.UshortGray, null, scaleX, scaleY);
            case 12 -> {
                SampleModel sm = bufImg.getRaster().getSampleModel();
                SurfaceType sType = switch (sm.getSampleSize(0)) {
                    case 1 -> SurfaceType.ByteBinary1Bit;
                    case 2 -> SurfaceType.ByteBinary2Bit;
                    case 4 -> SurfaceType.ByteBinary4Bit;
                    default -> throw new InternalError("Unrecognized pixel size");
                };
                yield BufImgSurfaceData.createDataBP(bufImg, sType, scaleX, scaleY);
            }
            default -> {
                WritableRaster raster = bufImg.getRaster();
                int numBands = raster.getNumBands();
                if (raster instanceof IntegerComponentRaster && raster.getNumDataElements() == 1 && ((IntegerComponentRaster)raster).getPixelStride() == 1) {
                    SurfaceType sType = SurfaceType.AnyInt;
                    if (cm instanceof DirectColorModel) {
                        DirectColorModel dcm = (DirectColorModel)cm;
                        int aMask = dcm.getAlphaMask();
                        int rMask = dcm.getRedMask();
                        int gMask = dcm.getGreenMask();
                        int bMask = dcm.getBlueMask();
                        sType = numBands == 3 && aMask == 0 && rMask == -16777216 && gMask == 0xFF0000 && bMask == 65280 ? SurfaceType.IntRgbx : (numBands == 4 && aMask == 0x1000000 && rMask == 0xFF0000 && gMask == 65280 && bMask == 255 ? SurfaceType.IntArgbBm : SurfaceType.AnyDcm);
                    }
                    yield BufImgSurfaceData.createDataIC(bufImg, sType, scaleX, scaleY);
                }
                if (raster instanceof ShortComponentRaster && raster.getNumDataElements() == 1 && ((ShortComponentRaster)raster).getPixelStride() == 1) {
                    SurfaceType sType = SurfaceType.AnyShort;
                    IndexColorModel icm = null;
                    if (cm instanceof DirectColorModel) {
                        DirectColorModel dcm = (DirectColorModel)cm;
                        int aMask = dcm.getAlphaMask();
                        int rMask = dcm.getRedMask();
                        int gMask = dcm.getGreenMask();
                        int bMask = dcm.getBlueMask();
                        if (numBands == 3 && aMask == 0 && rMask == 63488 && gMask == 1984 && bMask == 62) {
                            sType = SurfaceType.Ushort555Rgbx;
                        } else if (numBands == 4 && aMask == 61440 && rMask == 3840 && gMask == 240 && bMask == 15) {
                            sType = SurfaceType.Ushort4444Argb;
                        }
                    } else if (cm instanceof IndexColorModel) {
                        icm = (IndexColorModel)cm;
                        if (icm.getPixelSize() == 12) {
                            sType = BufImgSurfaceData.isOpaqueGray(icm) ? SurfaceType.Index12Gray : SurfaceType.UshortIndexed;
                        } else {
                            icm = null;
                        }
                    }
                    yield BufImgSurfaceData.createDataSC(bufImg, sType, icm, scaleX, scaleY);
                }
                yield new BufImgSurfaceData(raster.getDataBuffer(), bufImg, SurfaceType.Custom, scaleX, scaleY);
            }
        };
        ((BufImgSurfaceData)sData).initSolidLoops();
        return sData;
    }

    public static SurfaceData createData(Raster ras, ColorModel cm) {
        throw new InternalError("SurfaceData not implemented for Raster/CM");
    }

    public static SurfaceData createDataIC(BufferedImage bImg, SurfaceType sType, double scaleX, double scaleY) {
        IntegerComponentRaster icRaster = (IntegerComponentRaster)bImg.getRaster();
        BufImgSurfaceData bisd = new BufImgSurfaceData(icRaster.getDataBuffer(), bImg, sType, scaleX, scaleY);
        bisd.initRaster(icRaster.getDataStorage(), icRaster.getDataOffset(0) * 4, 0, icRaster.getWidth(), icRaster.getHeight(), icRaster.getPixelStride() * 4, icRaster.getScanlineStride() * 4, null);
        return bisd;
    }

    public static SurfaceData createDataSC(BufferedImage bImg, SurfaceType sType, IndexColorModel icm, double scaleX, double scaleY) {
        ShortComponentRaster scRaster = (ShortComponentRaster)bImg.getRaster();
        BufImgSurfaceData bisd = new BufImgSurfaceData(scRaster.getDataBuffer(), bImg, sType, scaleX, scaleY);
        bisd.initRaster(scRaster.getDataStorage(), scRaster.getDataOffset(0) * 2, 0, scRaster.getWidth(), scRaster.getHeight(), scRaster.getPixelStride() * 2, scRaster.getScanlineStride() * 2, icm);
        return bisd;
    }

    public static SurfaceData createDataBC(BufferedImage bImg, SurfaceType sType, int primaryBank, double scaleX, double scaleY) {
        ByteComponentRaster bcRaster = (ByteComponentRaster)bImg.getRaster();
        BufImgSurfaceData bisd = new BufImgSurfaceData(bcRaster.getDataBuffer(), bImg, sType, scaleX, scaleY);
        ColorModel cm = bImg.getColorModel();
        IndexColorModel icm = cm instanceof IndexColorModel ? (IndexColorModel)cm : null;
        bisd.initRaster(bcRaster.getDataStorage(), bcRaster.getDataOffset(primaryBank), 0, bcRaster.getWidth(), bcRaster.getHeight(), bcRaster.getPixelStride(), bcRaster.getScanlineStride(), icm);
        return bisd;
    }

    public static SurfaceData createDataBP(BufferedImage bImg, SurfaceType sType, double scaleX, double scaleY) {
        BytePackedRaster bpRaster = (BytePackedRaster)bImg.getRaster();
        BufImgSurfaceData bisd = new BufImgSurfaceData(bpRaster.getDataBuffer(), bImg, sType, scaleX, scaleY);
        ColorModel cm = bImg.getColorModel();
        IndexColorModel icm = cm instanceof IndexColorModel ? (IndexColorModel)cm : null;
        bisd.initRaster(bpRaster.getDataStorage(), bpRaster.getDataBitOffset() / 8, bpRaster.getDataBitOffset() & 7, bpRaster.getWidth(), bpRaster.getHeight(), 0, bpRaster.getScanlineStride(), icm);
        return bisd;
    }

    @Override
    public RenderLoops getRenderLoops(SunGraphics2D sg2d) {
        if (sg2d.paintState <= 1 && sg2d.compositeState <= 0) {
            return this.solidloops;
        }
        return super.getRenderLoops(sg2d);
    }

    @Override
    public Raster getRaster(int x, int y, int w, int h) {
        return this.bufImg.getRaster();
    }

    protected native void initRaster(Object var1, int var2, int var3, int var4, int var5, int var6, int var7, IndexColorModel var8);

    public BufImgSurfaceData(DataBuffer db, BufferedImage bufImg, SurfaceType sType, double scaleX, double scaleY) {
        super(SunWritableRaster.stealTrackable(db), sType, bufImg.getColorModel());
        this.bufImg = bufImg;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    protected BufImgSurfaceData(SurfaceType surfaceType, ColorModel cm) {
        super(surfaceType, cm);
        this.scaleX = 1.0;
        this.scaleY = 1.0;
    }

    public void initSolidLoops() {
        this.solidloops = BufImgSurfaceData.getSolidLoops(this.getSurfaceType());
    }

    public static synchronized RenderLoops getSolidLoops(SurfaceType type) {
        for (int i = 4; i >= 0; --i) {
            SurfaceType t = typecache[i];
            if (t == type) {
                return loopcache[i];
            }
            if (t == null) break;
        }
        RenderLoops l = BufImgSurfaceData.makeRenderLoops(SurfaceType.OpaqueColor, CompositeType.SrcNoEa, type);
        System.arraycopy(loopcache, 1, loopcache, 0, 4);
        System.arraycopy(typecache, 1, typecache, 0, 4);
        BufImgSurfaceData.loopcache[4] = l;
        BufImgSurfaceData.typecache[4] = type;
        return l;
    }

    @Override
    public SurfaceData getReplacement() {
        return BufImgSurfaceData.restoreContents(this.bufImg);
    }

    @Override
    public synchronized GraphicsConfiguration getDeviceConfiguration() {
        if (this.graphicsConfig == null) {
            this.graphicsConfig = BufferedImageGraphicsConfig.getConfig(this.bufImg, this.scaleX, this.scaleY);
        }
        return this.graphicsConfig;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(this.bufImg.getWidth(), this.bufImg.getHeight());
    }

    @Override
    protected void checkCustomComposite() {
    }

    @Override
    public Object getDestination() {
        return this.bufImg;
    }

    @Override
    public double getDefaultScaleX() {
        return this.scaleX;
    }

    @Override
    public double getDefaultScaleY() {
        return this.scaleY;
    }

    static {
        BufImgSurfaceData.initIDs(IndexColorModel.class, ICMColorData.class);
        loopcache = new RenderLoops[5];
        typecache = new SurfaceType[5];
    }

    public static final class ICMColorData {
        private long pData = 0L;

        private ICMColorData(long pData) {
            this.pData = pData;
        }
    }
}

