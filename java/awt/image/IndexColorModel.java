/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.math.BigInteger;
import java.util.Arrays;
import sun.awt.image.BufImgSurfaceData;

public class IndexColorModel
extends ColorModel {
    private int[] rgb;
    private int map_size;
    private int pixel_mask;
    private int transparent_index = -1;
    private boolean allgrayopaque;
    private BigInteger validBits;
    private volatile int hashCode;
    private BufImgSurfaceData.ICMColorData colorData = null;
    private static int[] opaqueBits = new int[]{8, 8, 8};
    private static int[] alphaBits = new int[]{8, 8, 8, 8};
    private static final int CACHESIZE = 40;
    private int[] lookupcache = new int[40];

    private static native void initIDs();

    public IndexColorModel(int bits, int size, byte[] r, byte[] g, byte[] b) {
        super(bits, opaqueBits, ColorSpace.getInstance(1000), false, false, 1, ColorModel.getDefaultTransferType(bits));
        if (bits < 1 || bits > 16) {
            throw new IllegalArgumentException("Number of bits must be between 1 and 16.");
        }
        this.setRGBs(size, r, g, b, null);
        this.calculatePixelMask();
    }

    public IndexColorModel(int bits, int size, byte[] r, byte[] g, byte[] b, int trans) {
        super(bits, opaqueBits, ColorSpace.getInstance(1000), false, false, 1, ColorModel.getDefaultTransferType(bits));
        if (bits < 1 || bits > 16) {
            throw new IllegalArgumentException("Number of bits must be between 1 and 16.");
        }
        this.setRGBs(size, r, g, b, null);
        this.setTransparentPixel(trans);
        this.calculatePixelMask();
    }

    public IndexColorModel(int bits, int size, byte[] r, byte[] g, byte[] b, byte[] a) {
        super(bits, alphaBits, ColorSpace.getInstance(1000), true, false, 3, ColorModel.getDefaultTransferType(bits));
        if (bits < 1 || bits > 16) {
            throw new IllegalArgumentException("Number of bits must be between 1 and 16.");
        }
        this.setRGBs(size, r, g, b, a);
        this.calculatePixelMask();
    }

    public IndexColorModel(int bits, int size, byte[] cmap, int start, boolean hasalpha) {
        this(bits, size, cmap, start, hasalpha, -1);
        if (bits < 1 || bits > 16) {
            throw new IllegalArgumentException("Number of bits must be between 1 and 16.");
        }
    }

    public IndexColorModel(int bits, int size, byte[] cmap, int start, boolean hasalpha, int trans) {
        super(bits, opaqueBits, ColorSpace.getInstance(1000), false, false, 1, ColorModel.getDefaultTransferType(bits));
        if (bits < 1 || bits > 16) {
            throw new IllegalArgumentException("Number of bits must be between 1 and 16.");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Map size (" + size + ") must be >= 1");
        }
        this.map_size = size;
        this.rgb = new int[this.calcRealMapSize(bits, size)];
        int j = start;
        int alpha = 255;
        boolean allgray = true;
        int transparency = 1;
        for (int i = 0; i < size; ++i) {
            int r = cmap[j++] & 0xFF;
            int g = cmap[j++] & 0xFF;
            int b = cmap[j++] & 0xFF;
            boolean bl = allgray = allgray && r == g && g == b;
            if (hasalpha && (alpha = cmap[j++] & 0xFF) != 255) {
                if (alpha == 0) {
                    if (transparency == 1) {
                        transparency = 2;
                    }
                    if (this.transparent_index < 0) {
                        this.transparent_index = i;
                    }
                } else {
                    transparency = 3;
                }
                allgray = false;
            }
            this.rgb[i] = alpha << 24 | r << 16 | g << 8 | b;
        }
        this.allgrayopaque = allgray;
        this.setTransparency(transparency);
        this.setTransparentPixel(trans);
        this.calculatePixelMask();
    }

    public IndexColorModel(int bits, int size, int[] cmap, int start, boolean hasalpha, int trans, int transferType) {
        super(bits, opaqueBits, ColorSpace.getInstance(1000), false, false, 1, transferType);
        if (bits < 1 || bits > 16) {
            throw new IllegalArgumentException("Number of bits must be between 1 and 16.");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Map size (" + size + ") must be >= 1");
        }
        if (transferType != 0 && transferType != 1) {
            throw new IllegalArgumentException("transferType must be eitherDataBuffer.TYPE_BYTE or DataBuffer.TYPE_USHORT");
        }
        this.setRGBs(size, cmap, start, hasalpha);
        this.setTransparentPixel(trans);
        this.calculatePixelMask();
    }

    public IndexColorModel(int bits, int size, int[] cmap, int start, int transferType, BigInteger validBits) {
        super(bits, alphaBits, ColorSpace.getInstance(1000), true, false, 3, transferType);
        if (bits < 1 || bits > 16) {
            throw new IllegalArgumentException("Number of bits must be between 1 and 16.");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Map size (" + size + ") must be >= 1");
        }
        if (transferType != 0 && transferType != 1) {
            throw new IllegalArgumentException("transferType must be eitherDataBuffer.TYPE_BYTE or DataBuffer.TYPE_USHORT");
        }
        if (validBits != null) {
            for (int i = 0; i < size; ++i) {
                if (validBits.testBit(i)) continue;
                this.validBits = validBits;
                break;
            }
        }
        this.setRGBs(size, cmap, start, true);
        this.calculatePixelMask();
    }

    private void setRGBs(int size, byte[] r, byte[] g, byte[] b, byte[] a) {
        if (size < 1) {
            throw new IllegalArgumentException("Map size (" + size + ") must be >= 1");
        }
        this.map_size = size;
        this.rgb = new int[this.calcRealMapSize(this.pixel_bits, size)];
        int alpha = 255;
        int transparency = 1;
        boolean allgray = true;
        for (int i = 0; i < size; ++i) {
            int rc = r[i] & 0xFF;
            int gc = g[i] & 0xFF;
            int bc = b[i] & 0xFF;
            boolean bl = allgray = allgray && rc == gc && gc == bc;
            if (a != null && (alpha = a[i] & 0xFF) != 255) {
                if (alpha == 0) {
                    if (transparency == 1) {
                        transparency = 2;
                    }
                    if (this.transparent_index < 0) {
                        this.transparent_index = i;
                    }
                } else {
                    transparency = 3;
                }
                allgray = false;
            }
            this.rgb[i] = alpha << 24 | rc << 16 | gc << 8 | bc;
        }
        this.allgrayopaque = allgray;
        this.setTransparency(transparency);
    }

    private void setRGBs(int size, int[] cmap, int start, boolean hasalpha) {
        this.map_size = size;
        this.rgb = new int[this.calcRealMapSize(this.pixel_bits, size)];
        int j = start;
        int transparency = 1;
        boolean allgray = true;
        BigInteger validBits = this.validBits;
        int i = 0;
        while (i < size) {
            if (validBits == null || validBits.testBit(i)) {
                int cmaprgb = cmap[j];
                int r = cmaprgb >> 16 & 0xFF;
                int g = cmaprgb >> 8 & 0xFF;
                int b = cmaprgb & 0xFF;
                boolean bl = allgray = allgray && r == g && g == b;
                if (hasalpha) {
                    int alpha = cmaprgb >>> 24;
                    if (alpha != 255) {
                        if (alpha == 0) {
                            if (transparency == 1) {
                                transparency = 2;
                            }
                            if (this.transparent_index < 0) {
                                this.transparent_index = i;
                            }
                        } else {
                            transparency = 3;
                        }
                        allgray = false;
                    }
                } else {
                    cmaprgb |= 0xFF000000;
                }
                this.rgb[i] = cmaprgb;
            }
            ++i;
            ++j;
        }
        this.allgrayopaque = allgray;
        this.setTransparency(transparency);
    }

    private int calcRealMapSize(int bits, int size) {
        int newSize = Math.max(1 << bits, size);
        return Math.max(newSize, 256);
    }

    private BigInteger getAllValid() {
        int numbytes = (this.map_size + 7) / 8;
        byte[] valid = new byte[numbytes];
        Arrays.fill(valid, (byte)-1);
        valid[0] = (byte)(255 >>> numbytes * 8 - this.map_size);
        return new BigInteger(1, valid);
    }

    @Override
    public int getTransparency() {
        return this.transparency;
    }

    @Override
    public int[] getComponentSize() {
        if (this.nBits == null) {
            if (this.supportsAlpha) {
                this.nBits = new int[4];
                this.nBits[3] = 8;
            } else {
                this.nBits = new int[3];
            }
            this.nBits[2] = 8;
            this.nBits[1] = 8;
            this.nBits[0] = 8;
        }
        return (int[])this.nBits.clone();
    }

    public final int getMapSize() {
        return this.map_size;
    }

    public final int getTransparentPixel() {
        return this.transparent_index;
    }

    public final void getReds(byte[] r) {
        for (int i = 0; i < this.map_size; ++i) {
            r[i] = (byte)(this.rgb[i] >> 16);
        }
    }

    public final void getGreens(byte[] g) {
        for (int i = 0; i < this.map_size; ++i) {
            g[i] = (byte)(this.rgb[i] >> 8);
        }
    }

    public final void getBlues(byte[] b) {
        for (int i = 0; i < this.map_size; ++i) {
            b[i] = (byte)this.rgb[i];
        }
    }

    public final void getAlphas(byte[] a) {
        for (int i = 0; i < this.map_size; ++i) {
            a[i] = (byte)(this.rgb[i] >> 24);
        }
    }

    public final void getRGBs(int[] rgb) {
        System.arraycopy(this.rgb, 0, rgb, 0, this.map_size);
    }

    private void setTransparentPixel(int trans) {
        if (trans >= 0 && trans < this.map_size) {
            int n = trans;
            this.rgb[n] = this.rgb[n] & 0xFFFFFF;
            this.transparent_index = trans;
            this.allgrayopaque = false;
            if (this.transparency == 1) {
                this.setTransparency(2);
            }
        }
    }

    private void setTransparency(int transparency) {
        if (this.transparency != transparency) {
            this.transparency = transparency;
            if (transparency == 1) {
                this.supportsAlpha = false;
                this.numComponents = 3;
                this.nBits = opaqueBits;
            } else {
                this.supportsAlpha = true;
                this.numComponents = 4;
                this.nBits = alphaBits;
            }
        }
    }

    private void calculatePixelMask() {
        int maskbits = this.pixel_bits;
        if (maskbits == 3) {
            maskbits = 4;
        } else if (maskbits > 4 && maskbits < 8) {
            maskbits = 8;
        }
        this.pixel_mask = (1 << maskbits) - 1;
    }

    @Override
    public final int getRed(int pixel) {
        return this.rgb[pixel & this.pixel_mask] >> 16 & 0xFF;
    }

    @Override
    public final int getGreen(int pixel) {
        return this.rgb[pixel & this.pixel_mask] >> 8 & 0xFF;
    }

    @Override
    public final int getBlue(int pixel) {
        return this.rgb[pixel & this.pixel_mask] & 0xFF;
    }

    @Override
    public final int getAlpha(int pixel) {
        return this.rgb[pixel & this.pixel_mask] >> 24 & 0xFF;
    }

    @Override
    public final int getRGB(int pixel) {
        return this.rgb[pixel & this.pixel_mask];
    }

    @Override
    public synchronized Object getDataElements(int rgb, Object pixel) {
        int red = rgb >> 16 & 0xFF;
        int green = rgb >> 8 & 0xFF;
        int blue = rgb & 0xFF;
        int alpha = rgb >>> 24;
        int pix = 0;
        for (int i = 38; i >= 0 && (pix = this.lookupcache[i]) != 0; i -= 2) {
            if (rgb != this.lookupcache[i + 1]) continue;
            return this.installpixel(pixel, ~pix);
        }
        if (this.allgrayopaque) {
            int minDist = 256;
            int gray = (red * 77 + green * 150 + blue * 29 + 128) / 256;
            for (int i = 0; i < this.map_size; ++i) {
                if (this.rgb[i] == 0) continue;
                int d = (this.rgb[i] & 0xFF) - gray;
                if (d < 0) {
                    d = -d;
                }
                if (d >= minDist) continue;
                pix = i;
                if (d != 0) {
                    minDist = d;
                    continue;
                }
                break;
            }
        } else if (this.transparency == 1) {
            int lutrgb;
            int i;
            smallestError = Integer.MAX_VALUE;
            int[] lut = this.rgb;
            for (i = 0; i < this.map_size; ++i) {
                lutrgb = lut[i];
                if (lutrgb != rgb || lutrgb == 0) continue;
                pix = i;
                smallestError = 0;
                break;
            }
            if (smallestError != 0) {
                for (i = 0; i < this.map_size; ++i) {
                    int tmp;
                    int currentError;
                    lutrgb = lut[i];
                    if (lutrgb == 0 || (currentError = (tmp = (lutrgb >> 16 & 0xFF) - red) * tmp) >= smallestError || (currentError += (tmp = (lutrgb >> 8 & 0xFF) - green) * tmp) >= smallestError || (currentError += (tmp = (lutrgb & 0xFF) - blue) * tmp) >= smallestError) continue;
                    pix = i;
                    smallestError = currentError;
                }
            }
        } else if (alpha == 0 && this.transparent_index >= 0) {
            pix = this.transparent_index;
        } else {
            smallestError = Integer.MAX_VALUE;
            int[] lut = this.rgb;
            for (int i = 0; i < this.map_size; ++i) {
                int lutrgb = lut[i];
                if (lutrgb == rgb) {
                    if (this.validBits != null && !this.validBits.testBit(i)) continue;
                    pix = i;
                    break;
                }
                int tmp = (lutrgb >> 16 & 0xFF) - red;
                int currentError = tmp * tmp;
                if (currentError >= smallestError || (currentError += (tmp = (lutrgb >> 8 & 0xFF) - green) * tmp) >= smallestError || (currentError += (tmp = (lutrgb & 0xFF) - blue) * tmp) >= smallestError || (currentError += (tmp = (lutrgb >>> 24) - alpha) * tmp) >= smallestError || this.validBits != null && !this.validBits.testBit(i)) continue;
                pix = i;
                smallestError = currentError;
            }
        }
        System.arraycopy(this.lookupcache, 2, this.lookupcache, 0, 38);
        this.lookupcache[39] = rgb;
        this.lookupcache[38] = ~pix;
        return this.installpixel(pixel, pix);
    }

    private Object installpixel(Object pixel, int pix) {
        switch (this.transferType) {
            case 3: {
                if (pixel == null) {
                    intObj = new int[1];
                    pixel = intObj;
                } else {
                    intObj = (int[])pixel;
                }
                intObj[0] = pix;
                break;
            }
            case 0: {
                if (pixel == null) {
                    byteObj = new byte[1];
                    pixel = byteObj;
                } else {
                    byteObj = (byte[])pixel;
                }
                byteObj[0] = (byte)pix;
                break;
            }
            case 1: {
                if (pixel == null) {
                    shortObj = new short[1];
                    pixel = shortObj;
                } else {
                    shortObj = (short[])pixel;
                }
                shortObj[0] = (short)pix;
                break;
            }
            default: {
                throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
            }
        }
        return pixel;
    }

    @Override
    public int[] getComponents(int pixel, int[] components, int offset) {
        if (components == null) {
            components = new int[offset + this.numComponents];
        }
        components[offset + 0] = this.getRed(pixel);
        components[offset + 1] = this.getGreen(pixel);
        components[offset + 2] = this.getBlue(pixel);
        if (this.supportsAlpha && components.length - offset > 3) {
            components[offset + 3] = this.getAlpha(pixel);
        }
        return components;
    }

    @Override
    public int[] getComponents(Object pixel, int[] components, int offset) {
        return this.getComponents(switch (this.transferType) {
            case 0 -> {
                byte[] bdata = (byte[])pixel;
                yield bdata[0] & 0xFF;
            }
            case 1 -> {
                short[] sdata = (short[])pixel;
                yield sdata[0] & 0xFFFF;
            }
            case 3 -> {
                int[] idata = (int[])pixel;
                yield idata[0];
            }
            default -> throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
        }, components, offset);
    }

    @Override
    public int getDataElement(int[] components, int offset) {
        int rgb = components[offset + 0] << 16 | components[offset + 1] << 8 | components[offset + 2];
        rgb = this.supportsAlpha ? (rgb |= components[offset + 3] << 24) : (rgb |= 0xFF000000);
        Object inData = this.getDataElements(rgb, null);
        return switch (this.transferType) {
            case 0 -> {
                byte[] bdata = (byte[])inData;
                yield bdata[0] & 0xFF;
            }
            case 1 -> {
                short[] sdata = (short[])inData;
                yield sdata[0];
            }
            case 3 -> {
                int[] idata = (int[])inData;
                yield idata[0];
            }
            default -> throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
        };
    }

    @Override
    public Object getDataElements(int[] components, int offset, Object pixel) {
        int rgb = components[offset + 0] << 16 | components[offset + 1] << 8 | components[offset + 2];
        rgb = this.supportsAlpha ? (rgb |= components[offset + 3] << 24) : (rgb &= 0xFF000000);
        return this.getDataElements(rgb, pixel);
    }

    @Override
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        WritableRaster raster;
        if (this.pixel_bits == 1 || this.pixel_bits == 2 || this.pixel_bits == 4) {
            raster = Raster.createPackedRaster(0, w, h, 1, this.pixel_bits, null);
        } else if (this.pixel_bits <= 8) {
            raster = Raster.createInterleavedRaster(0, w, h, 1, null);
        } else if (this.pixel_bits <= 16) {
            raster = Raster.createInterleavedRaster(1, w, h, 1, null);
        } else {
            throw new UnsupportedOperationException("This method is not supported  for pixel bits > 16.");
        }
        return raster;
    }

    @Override
    public boolean isCompatibleRaster(Raster raster) {
        int size = raster.getSampleModel().getSampleSize(0);
        return raster.getTransferType() == this.transferType && raster.getNumBands() == 1 && 1 << size >= this.map_size;
    }

    @Override
    public SampleModel createCompatibleSampleModel(int w, int h) {
        int[] off = new int[]{0};
        if (this.pixel_bits == 1 || this.pixel_bits == 2 || this.pixel_bits == 4) {
            return new MultiPixelPackedSampleModel(this.transferType, w, h, this.pixel_bits);
        }
        return new ComponentSampleModel(this.transferType, w, h, 1, w, off);
    }

    @Override
    public boolean isCompatibleSampleModel(SampleModel sm) {
        if (!(sm instanceof ComponentSampleModel) && !(sm instanceof MultiPixelPackedSampleModel)) {
            return false;
        }
        if (sm.getTransferType() != this.transferType) {
            return false;
        }
        return sm.getNumBands() == 1;
    }

    public BufferedImage convertToIntDiscrete(Raster raster, boolean forceARGB) {
        if (!this.isCompatibleRaster(raster)) {
            throw new IllegalArgumentException("This raster is not compatiblewith this IndexColorModel.");
        }
        ColorModel cm = forceARGB || this.transparency == 3 ? ColorModel.getRGBdefault() : (this.transparency == 2 ? new DirectColorModel(25, 0xFF0000, 65280, 255, 0x1000000) : new DirectColorModel(24, 0xFF0000, 65280, 255));
        int w = raster.getWidth();
        int h = raster.getHeight();
        WritableRaster discreteRaster = cm.createCompatibleWritableRaster(w, h);
        Object obj = null;
        int[] data = null;
        int rX = raster.getMinX();
        int rY = raster.getMinY();
        int y = 0;
        while (y < h) {
            data = (obj = raster.getDataElements(rX, rY, w, 1, obj)) instanceof int[] ? (int[])obj : DataBuffer.toIntArray(obj);
            for (int x = 0; x < w; ++x) {
                data[x] = this.rgb[data[x] & this.pixel_mask];
            }
            discreteRaster.setDataElements(0, y, w, 1, data);
            ++y;
            ++rY;
        }
        return new BufferedImage(cm, discreteRaster, false, null);
    }

    public boolean isValid(int pixel) {
        return pixel >= 0 && pixel < this.map_size && (this.validBits == null || this.validBits.testBit(pixel));
    }

    public boolean isValid() {
        return this.validBits == null;
    }

    public BigInteger getValidPixels() {
        if (this.validBits == null) {
            return this.getAllValid();
        }
        return this.validBits;
    }

    @Override
    public String toString() {
        return "IndexColorModel: #pixelBits = " + this.pixel_bits + " numComponents = " + this.numComponents + " color space = " + String.valueOf(this.colorSpace) + " transparency = " + this.transparency + " transIndex   = " + this.transparent_index + " has alpha = " + this.supportsAlpha + " isAlphaPre = " + this.isAlphaPremultiplied;
    }

    @Override
    public boolean equals(Object obj) {
        boolean testValidBits;
        if (!(obj instanceof IndexColorModel)) {
            return false;
        }
        IndexColorModel cm = (IndexColorModel)obj;
        if (this.supportsAlpha != cm.hasAlpha() || this.isAlphaPremultiplied != cm.isAlphaPremultiplied() || this.pixel_bits != cm.getPixelSize() || this.transparency != cm.getTransparency() || this.numComponents != cm.getNumComponents() || !this.colorSpace.equals(cm.colorSpace) || this.transferType != cm.transferType || this.map_size != cm.map_size || this.transparent_index != cm.transparent_index) {
            return false;
        }
        if (!Arrays.equals(this.nBits, cm.getComponentSize())) {
            return false;
        }
        if (this.validBits == cm.validBits) {
            testValidBits = false;
        } else {
            if (this.validBits == null || cm.validBits == null) {
                return false;
            }
            testValidBits = !this.validBits.equals(cm.validBits);
        }
        if (testValidBits) {
            for (int i = 0; i < this.map_size; ++i) {
                if (this.rgb[i] == cm.rgb[i] && this.validBits.testBit(i) == cm.validBits.testBit(i)) continue;
                return false;
            }
        } else {
            for (int i = 0; i < this.map_size; ++i) {
                if (this.rgb[i] == cm.rgb[i]) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = this.hashCode;
        if (result == 0) {
            result = 7;
            result = 89 * result + this.pixel_bits;
            result = 89 * result + Arrays.hashCode(this.nBits);
            result = 89 * result + this.transparency;
            result = 89 * result + (this.supportsAlpha ? 1 : 0);
            result = 89 * result + (this.isAlphaPremultiplied ? 1 : 0);
            result = 89 * result + this.numComponents;
            result = 89 * result + this.colorSpace.hashCode();
            result = 89 * result + this.transferType;
            result = 89 * result + Arrays.hashCode(this.rgb);
            result = 89 * result + this.map_size;
            this.hashCode = result = 89 * result + this.transparent_index;
        }
        return result;
    }

    static {
        ColorModel.loadLibraries();
        IndexColorModel.initIDs();
    }
}

