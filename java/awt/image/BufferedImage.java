/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.TileObserver;
import java.awt.image.WritableRaster;
import java.awt.image.WritableRenderedImage;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import sun.awt.image.ByteComponentRaster;
import sun.awt.image.BytePackedRaster;
import sun.awt.image.IntegerComponentRaster;
import sun.awt.image.OffScreenImageSource;
import sun.awt.image.ShortComponentRaster;

public class BufferedImage
extends Image
implements WritableRenderedImage,
Transparency {
    private int imageType = 0;
    private ColorModel colorModel;
    private final WritableRaster raster;
    private OffScreenImageSource osis;
    private Hashtable<String, Object> properties;
    public static final int TYPE_CUSTOM = 0;
    public static final int TYPE_INT_RGB = 1;
    public static final int TYPE_INT_ARGB = 2;
    public static final int TYPE_INT_ARGB_PRE = 3;
    public static final int TYPE_INT_BGR = 4;
    public static final int TYPE_3BYTE_BGR = 5;
    public static final int TYPE_4BYTE_ABGR = 6;
    public static final int TYPE_4BYTE_ABGR_PRE = 7;
    public static final int TYPE_USHORT_565_RGB = 8;
    public static final int TYPE_USHORT_555_RGB = 9;
    public static final int TYPE_BYTE_GRAY = 10;
    public static final int TYPE_USHORT_GRAY = 11;
    public static final int TYPE_BYTE_BINARY = 12;
    public static final int TYPE_BYTE_INDEXED = 13;
    private static final int DCM_RED_MASK = 0xFF0000;
    private static final int DCM_GREEN_MASK = 65280;
    private static final int DCM_BLUE_MASK = 255;
    private static final int DCM_ALPHA_MASK = -16777216;
    private static final int DCM_565_RED_MASK = 63488;
    private static final int DCM_565_GRN_MASK = 2016;
    private static final int DCM_565_BLU_MASK = 31;
    private static final int DCM_555_RED_MASK = 31744;
    private static final int DCM_555_GRN_MASK = 992;
    private static final int DCM_555_BLU_MASK = 31;
    private static final int DCM_BGR_RED_MASK = 255;
    private static final int DCM_BGR_GRN_MASK = 65280;
    private static final int DCM_BGR_BLU_MASK = 0xFF0000;

    private static native void initIDs();

    public BufferedImage(int width, int height, int imageType) {
        switch (imageType) {
            case 1: {
                this.colorModel = new DirectColorModel(24, 0xFF0000, 65280, 255, 0);
                this.raster = this.colorModel.createCompatibleWritableRaster(width, height);
                break;
            }
            case 2: {
                this.colorModel = ColorModel.getRGBdefault();
                this.raster = this.colorModel.createCompatibleWritableRaster(width, height);
                break;
            }
            case 3: {
                this.colorModel = new DirectColorModel(ColorSpace.getInstance(1000), 32, 0xFF0000, 65280, 255, -16777216, true, 3);
                this.raster = this.colorModel.createCompatibleWritableRaster(width, height);
                break;
            }
            case 4: {
                this.colorModel = new DirectColorModel(24, 255, 65280, 0xFF0000);
                this.raster = this.colorModel.createCompatibleWritableRaster(width, height);
                break;
            }
            case 5: {
                ColorSpace cs = ColorSpace.getInstance(1000);
                int[] nBits = new int[]{8, 8, 8};
                int[] bOffs = new int[]{2, 1, 0};
                this.colorModel = new ComponentColorModel(cs, nBits, false, false, 1, 0);
                this.raster = Raster.createInterleavedRaster(0, width, height, width * 3, 3, bOffs, null);
                break;
            }
            case 6: {
                ColorSpace cs = ColorSpace.getInstance(1000);
                int[] nBits = new int[]{8, 8, 8, 8};
                int[] bOffs = new int[]{3, 2, 1, 0};
                this.colorModel = new ComponentColorModel(cs, nBits, true, false, 3, 0);
                this.raster = Raster.createInterleavedRaster(0, width, height, width * 4, 4, bOffs, null);
                break;
            }
            case 7: {
                ColorSpace cs = ColorSpace.getInstance(1000);
                int[] nBits = new int[]{8, 8, 8, 8};
                int[] bOffs = new int[]{3, 2, 1, 0};
                this.colorModel = new ComponentColorModel(cs, nBits, true, true, 3, 0);
                this.raster = Raster.createInterleavedRaster(0, width, height, width * 4, 4, bOffs, null);
                break;
            }
            case 10: {
                ColorSpace cs = ColorSpace.getInstance(1003);
                int[] nBits = new int[]{8};
                this.colorModel = new ComponentColorModel(cs, nBits, false, true, 1, 0);
                this.raster = this.colorModel.createCompatibleWritableRaster(width, height);
                break;
            }
            case 11: {
                ColorSpace cs = ColorSpace.getInstance(1003);
                int[] nBits = new int[]{16};
                this.colorModel = new ComponentColorModel(cs, nBits, false, true, 1, 1);
                this.raster = this.colorModel.createCompatibleWritableRaster(width, height);
                break;
            }
            case 12: {
                byte[] arr = new byte[]{0, -1};
                this.colorModel = new IndexColorModel(1, 2, arr, arr, arr);
                this.raster = Raster.createPackedRaster(0, width, height, 1, 1, null);
                break;
            }
            case 13: {
                int[] cmap = new int[256];
                int i = 0;
                for (int r = 0; r < 256; r += 51) {
                    for (int g = 0; g < 256; g += 51) {
                        for (int b = 0; b < 256; b += 51) {
                            cmap[i++] = r << 16 | g << 8 | b;
                        }
                    }
                }
                int grayIncr = 256 / (256 - i);
                int gray = grayIncr * 3;
                while (i < 256) {
                    cmap[i] = gray << 16 | gray << 8 | gray;
                    gray += grayIncr;
                    ++i;
                }
                this.colorModel = new IndexColorModel(8, 256, cmap, 0, false, -1, 0);
                this.raster = Raster.createInterleavedRaster(0, width, height, 1, null);
                break;
            }
            case 8: {
                this.colorModel = new DirectColorModel(16, 63488, 2016, 31);
                this.raster = this.colorModel.createCompatibleWritableRaster(width, height);
                break;
            }
            case 9: {
                this.colorModel = new DirectColorModel(15, 31744, 992, 31);
                this.raster = this.colorModel.createCompatibleWritableRaster(width, height);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown image type " + imageType);
            }
        }
        this.imageType = imageType;
    }

    public BufferedImage(int width, int height, int imageType, IndexColorModel cm) {
        if (cm.hasAlpha() && cm.isAlphaPremultiplied()) {
            throw new IllegalArgumentException("This image types do not have premultiplied alpha.");
        }
        switch (imageType) {
            case 12: {
                int bits;
                int mapSize = cm.getMapSize();
                if (mapSize <= 2) {
                    bits = 1;
                } else if (mapSize <= 4) {
                    bits = 2;
                } else if (mapSize <= 16) {
                    bits = 4;
                } else {
                    throw new IllegalArgumentException("Color map for TYPE_BYTE_BINARY must have no more than 16 entries");
                }
                this.raster = Raster.createPackedRaster(0, width, height, 1, bits, null);
                break;
            }
            case 13: {
                this.raster = Raster.createInterleavedRaster(0, width, height, 1, null);
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid image type (" + imageType + ").  Image type must be either TYPE_BYTE_BINARY or  TYPE_BYTE_INDEXED");
            }
        }
        if (!cm.isCompatibleRaster(this.raster)) {
            throw new IllegalArgumentException("Incompatible image type and IndexColorModel");
        }
        this.colorModel = cm;
        this.imageType = imageType;
    }

    public BufferedImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?, ?> properties) {
        if (!cm.isCompatibleRaster(raster)) {
            throw new IllegalArgumentException("Raster " + String.valueOf(raster) + " is incompatible with ColorModel " + String.valueOf(cm));
        }
        if (raster.minX != 0 || raster.minY != 0) {
            throw new IllegalArgumentException("Raster " + String.valueOf(raster) + " has minX or minY not equal to zero: " + raster.minX + " " + raster.minY);
        }
        this.colorModel = cm;
        this.raster = raster;
        if (properties != null && !properties.isEmpty()) {
            this.properties = new Hashtable();
            for (Object key : properties.keySet()) {
                if (!(key instanceof String)) continue;
                this.properties.put((String)key, properties.get(key));
            }
        }
        int numBands = raster.getNumBands();
        boolean isAlphaPre = cm.isAlphaPremultiplied();
        boolean isStandard = BufferedImage.isStandard(cm, raster);
        this.coerceData(isRasterPremultiplied);
        SampleModel sm = raster.getSampleModel();
        ColorSpace cs = cm.getColorSpace();
        int csType = cs.getType();
        if (csType != 5) {
            if (csType == 6 && isStandard && cm instanceof ComponentColorModel) {
                if (sm instanceof ComponentSampleModel && ((ComponentSampleModel)sm).getPixelStride() != numBands) {
                    this.imageType = 0;
                } else if (raster instanceof ByteComponentRaster && raster.getNumBands() == 1 && cm.getComponentSize(0) == 8 && ((ByteComponentRaster)raster).getPixelStride() == 1) {
                    this.imageType = 10;
                } else if (raster instanceof ShortComponentRaster && raster.getNumBands() == 1 && cm.getComponentSize(0) == 16 && ((ShortComponentRaster)raster).getPixelStride() == 1) {
                    this.imageType = 11;
                }
            } else {
                this.imageType = 0;
            }
            return;
        }
        if (raster instanceof IntegerComponentRaster && (numBands == 3 || numBands == 4)) {
            IntegerComponentRaster iraster = (IntegerComponentRaster)raster;
            int pixSize = cm.getPixelSize();
            if (iraster.getPixelStride() == 1 && isStandard && cm instanceof DirectColorModel && (pixSize == 32 || pixSize == 24)) {
                DirectColorModel dcm = (DirectColorModel)cm;
                int rmask = dcm.getRedMask();
                int gmask = dcm.getGreenMask();
                int bmask = dcm.getBlueMask();
                if (rmask == 0xFF0000 && gmask == 65280 && bmask == 255) {
                    if (dcm.getAlphaMask() == -16777216) {
                        this.imageType = isAlphaPre ? 3 : 2;
                    } else if (!dcm.hasAlpha()) {
                        this.imageType = 1;
                    }
                } else if (rmask == 255 && gmask == 65280 && bmask == 0xFF0000 && !dcm.hasAlpha()) {
                    this.imageType = 4;
                }
            }
        } else if (cm instanceof IndexColorModel && numBands == 1 && isStandard && (!cm.hasAlpha() || !isAlphaPre)) {
            ByteComponentRaster braster;
            IndexColorModel icm = (IndexColorModel)cm;
            int pixSize = icm.getPixelSize();
            if (raster instanceof BytePackedRaster) {
                this.imageType = 12;
            } else if (raster instanceof ByteComponentRaster && (braster = (ByteComponentRaster)raster).getPixelStride() == 1 && pixSize <= 8) {
                this.imageType = 13;
            }
        } else if (raster instanceof ShortComponentRaster && cm instanceof DirectColorModel && isStandard && numBands == 3 && !cm.hasAlpha()) {
            DirectColorModel dcm = (DirectColorModel)cm;
            if (dcm.getRedMask() == 63488) {
                if (dcm.getGreenMask() == 2016 && dcm.getBlueMask() == 31) {
                    this.imageType = 8;
                }
            } else if (dcm.getRedMask() == 31744 && dcm.getGreenMask() == 992 && dcm.getBlueMask() == 31) {
                this.imageType = 9;
            }
        } else if (raster instanceof ByteComponentRaster && cm instanceof ComponentColorModel && isStandard && raster.getSampleModel() instanceof PixelInterleavedSampleModel && (numBands == 3 || numBands == 4)) {
            ComponentColorModel ccm = (ComponentColorModel)cm;
            PixelInterleavedSampleModel csm = (PixelInterleavedSampleModel)raster.getSampleModel();
            ByteComponentRaster braster = (ByteComponentRaster)raster;
            int[] offs = csm.getBandOffsets();
            if (ccm.getNumComponents() != numBands) {
                throw new RasterFormatException("Number of components in ColorModel (" + ccm.getNumComponents() + ") does not match # in  Raster (" + numBands + ")");
            }
            int[] nBits = ccm.getComponentSize();
            boolean is8bit = true;
            for (int i = 0; i < numBands; ++i) {
                if (nBits[i] == 8) continue;
                is8bit = false;
                break;
            }
            if (is8bit && braster.getPixelStride() == numBands && offs[0] == numBands - 1 && offs[1] == numBands - 2 && offs[2] == numBands - 3) {
                if (numBands == 3 && !ccm.hasAlpha()) {
                    this.imageType = 5;
                } else if (offs[3] == 0 && ccm.hasAlpha()) {
                    this.imageType = isAlphaPre ? 7 : 6;
                }
            }
        }
    }

    private static boolean isStandard(ColorModel cm, WritableRaster wr) {
        final Class<?> cmClass = cm.getClass();
        final Class<?> wrClass = wr.getClass();
        final Class<?> smClass = wr.getSampleModel().getClass();
        PrivilegedAction<Boolean> checkClassLoadersAction = new PrivilegedAction<Boolean>(){

            @Override
            public Boolean run() {
                ClassLoader std = System.class.getClassLoader();
                return cmClass.getClassLoader() == std && smClass.getClassLoader() == std && wrClass.getClassLoader() == std;
            }
        };
        return AccessController.doPrivileged(checkClassLoadersAction);
    }

    public int getType() {
        return this.imageType;
    }

    @Override
    public ColorModel getColorModel() {
        return this.colorModel;
    }

    public WritableRaster getRaster() {
        return this.raster;
    }

    public WritableRaster getAlphaRaster() {
        return this.colorModel.getAlphaRaster(this.raster);
    }

    public int getRGB(int x, int y) {
        return this.colorModel.getRGB(this.raster.getDataElements(x, y, null));
    }

    public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
        int yoff = offset;
        int nbands = this.raster.getNumBands();
        int dataType = this.raster.getDataBuffer().getDataType();
        Object[] data = switch (dataType) {
            case 0 -> new byte[nbands];
            case 1 -> (Object[])new short[nbands];
            case 3 -> (Object[])new int[nbands];
            case 4 -> (Object[])new float[nbands];
            case 5 -> (Object[])new double[nbands];
            default -> throw new IllegalArgumentException("Unknown data buffer type: " + dataType);
        };
        if (rgbArray == null) {
            rgbArray = new int[offset + h * scansize];
        }
        int y = startY;
        while (y < startY + h) {
            int off = yoff;
            for (int x = startX; x < startX + w; ++x) {
                rgbArray[off++] = this.colorModel.getRGB(this.raster.getDataElements(x, y, data));
            }
            ++y;
            yoff += scansize;
        }
        return rgbArray;
    }

    public void setRGB(int x, int y, int rgb) {
        this.raster.setDataElements(x, y, this.colorModel.getDataElements(rgb, null));
    }

    public void setRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
        int yoff = offset;
        Object pixel = null;
        int y = startY;
        while (y < startY + h) {
            int off = yoff;
            for (int x = startX; x < startX + w; ++x) {
                pixel = this.colorModel.getDataElements(rgbArray[off++], pixel);
                this.raster.setDataElements(x, y, pixel);
            }
            ++y;
            yoff += scansize;
        }
    }

    @Override
    public int getWidth() {
        return this.raster.getWidth();
    }

    @Override
    public int getHeight() {
        return this.raster.getHeight();
    }

    @Override
    public int getWidth(ImageObserver observer) {
        return this.raster.getWidth();
    }

    @Override
    public int getHeight(ImageObserver observer) {
        return this.raster.getHeight();
    }

    @Override
    public ImageProducer getSource() {
        if (this.osis == null) {
            if (this.properties == null) {
                this.properties = new Hashtable();
            }
            this.osis = new OffScreenImageSource(this, this.properties);
        }
        return this.osis;
    }

    @Override
    public Object getProperty(String name, ImageObserver observer) {
        return this.getProperty(name);
    }

    @Override
    public Object getProperty(String name) {
        if (name == null) {
            throw new NullPointerException("null property name is not allowed");
        }
        if (this.properties == null) {
            return Image.UndefinedProperty;
        }
        Object o = this.properties.get(name);
        if (o == null) {
            o = Image.UndefinedProperty;
        }
        return o;
    }

    @Override
    public Graphics getGraphics() {
        return this.createGraphics();
    }

    public Graphics2D createGraphics() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return env.createGraphics(this);
    }

    public BufferedImage getSubimage(int x, int y, int w, int h) {
        return new BufferedImage(this.colorModel, this.raster.createWritableChild(x, y, w, h, 0, 0, null), this.colorModel.isAlphaPremultiplied(), this.properties);
    }

    public boolean isAlphaPremultiplied() {
        return this.colorModel.isAlphaPremultiplied();
    }

    public void coerceData(boolean isAlphaPremultiplied) {
        if (this.colorModel.hasAlpha() && this.colorModel.isAlphaPremultiplied() != isAlphaPremultiplied) {
            this.colorModel = this.colorModel.coerceData(this.raster, isAlphaPremultiplied);
        }
    }

    public String toString() {
        return "BufferedImage@" + Integer.toHexString(this.hashCode()) + ": type = " + this.imageType + " " + String.valueOf(this.colorModel) + " " + String.valueOf(this.raster);
    }

    @Override
    public Vector<RenderedImage> getSources() {
        return null;
    }

    @Override
    public String[] getPropertyNames() {
        if (this.properties == null || this.properties.isEmpty()) {
            return null;
        }
        Set<String> keys = this.properties.keySet();
        return keys.toArray(new String[keys.size()]);
    }

    @Override
    public int getMinX() {
        return this.raster.getMinX();
    }

    @Override
    public int getMinY() {
        return this.raster.getMinY();
    }

    @Override
    public SampleModel getSampleModel() {
        return this.raster.getSampleModel();
    }

    @Override
    public int getNumXTiles() {
        return 1;
    }

    @Override
    public int getNumYTiles() {
        return 1;
    }

    @Override
    public int getMinTileX() {
        return 0;
    }

    @Override
    public int getMinTileY() {
        return 0;
    }

    @Override
    public int getTileWidth() {
        return this.raster.getWidth();
    }

    @Override
    public int getTileHeight() {
        return this.raster.getHeight();
    }

    @Override
    public int getTileGridXOffset() {
        return this.raster.getMinX();
    }

    @Override
    public int getTileGridYOffset() {
        return this.raster.getMinY();
    }

    @Override
    public Raster getTile(int tileX, int tileY) {
        if (tileX == 0 && tileY == 0) {
            return this.raster;
        }
        throw new ArrayIndexOutOfBoundsException("BufferedImages only have one tile with index 0,0");
    }

    @Override
    public Raster getData() {
        int width = this.raster.getWidth();
        int height = this.raster.getHeight();
        int startX = this.raster.getMinX();
        int startY = this.raster.getMinY();
        WritableRaster wr = Raster.createWritableRaster(this.raster.getSampleModel(), new Point(this.raster.getSampleModelTranslateX(), this.raster.getSampleModelTranslateY()));
        Object tdata = null;
        for (int i = startY; i < startY + height; ++i) {
            tdata = this.raster.getDataElements(startX, i, width, 1, tdata);
            wr.setDataElements(startX, i, width, 1, tdata);
        }
        return wr;
    }

    @Override
    public Raster getData(Rectangle rect) {
        SampleModel sm = this.raster.getSampleModel();
        SampleModel nsm = sm.createCompatibleSampleModel(rect.width, rect.height);
        WritableRaster wr = Raster.createWritableRaster(nsm, rect.getLocation());
        int width = rect.width;
        int height = rect.height;
        int startX = rect.x;
        int startY = rect.y;
        Object tdata = null;
        for (int i = startY; i < startY + height; ++i) {
            tdata = this.raster.getDataElements(startX, i, width, 1, tdata);
            wr.setDataElements(startX, i, width, 1, tdata);
        }
        return wr;
    }

    @Override
    public WritableRaster copyData(WritableRaster outRaster) {
        if (outRaster == null) {
            return (WritableRaster)this.getData();
        }
        int width = outRaster.getWidth();
        int height = outRaster.getHeight();
        int startX = outRaster.getMinX();
        int startY = outRaster.getMinY();
        Object tdata = null;
        for (int i = startY; i < startY + height; ++i) {
            tdata = this.raster.getDataElements(startX, i, width, 1, tdata);
            outRaster.setDataElements(startX, i, width, 1, tdata);
        }
        return outRaster;
    }

    @Override
    public void setData(Raster r) {
        int width = r.getWidth();
        int height = r.getHeight();
        int startX = r.getMinX();
        int startY = r.getMinY();
        int[] tdata = null;
        Rectangle rclip = new Rectangle(startX, startY, width, height);
        Rectangle bclip = new Rectangle(0, 0, this.raster.width, this.raster.height);
        Rectangle intersect = rclip.intersection(bclip);
        if (intersect.isEmpty()) {
            return;
        }
        width = intersect.width;
        height = intersect.height;
        startX = intersect.x;
        for (int i = startY = intersect.y; i < startY + height; ++i) {
            tdata = r.getPixels(startX, i, width, 1, tdata);
            this.raster.setPixels(startX, i, width, 1, tdata);
        }
    }

    @Override
    public void addTileObserver(TileObserver to) {
    }

    @Override
    public void removeTileObserver(TileObserver to) {
    }

    @Override
    public boolean isTileWritable(int tileX, int tileY) {
        if (tileX == 0 && tileY == 0) {
            return true;
        }
        throw new IllegalArgumentException("Only 1 tile in image");
    }

    @Override
    public Point[] getWritableTileIndices() {
        Point[] p = new Point[]{new Point()};
        return p;
    }

    @Override
    public boolean hasTileWriters() {
        return true;
    }

    @Override
    public WritableRaster getWritableTile(int tileX, int tileY) {
        return this.raster;
    }

    @Override
    public void releaseWritableTile(int tileX, int tileY) {
    }

    @Override
    public int getTransparency() {
        return this.colorModel.getTransparency();
    }

    static {
        ColorModel.loadLibraries();
        BufferedImage.initIDs();
    }
}

