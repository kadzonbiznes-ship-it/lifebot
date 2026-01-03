/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.cmm.lcms;

import java.awt.color.CMMException;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.nio.ByteOrder;
import sun.awt.image.ByteComponentRaster;
import sun.awt.image.IntegerComponentRaster;
import sun.awt.image.ShortComponentRaster;

final class LCMSImageLayout {
    private static final int SWAPFIRST = 16384;
    private static final int DOSWAP = 1024;
    private static final int PT_GRAY_8 = LCMSImageLayout.CHANNELS_SH(1) | LCMSImageLayout.BYTES_SH(1);
    private static final int PT_GRAY_16 = LCMSImageLayout.CHANNELS_SH(1) | LCMSImageLayout.BYTES_SH(2);
    private static final int PT_RGB_8 = LCMSImageLayout.CHANNELS_SH(3) | LCMSImageLayout.BYTES_SH(1);
    private static final int PT_RGBA_8 = PT_RGB_8 | LCMSImageLayout.EXTRA_SH(1);
    private static final int PT_ARGB_8 = PT_RGBA_8 | 0x4000;
    private static final int PT_ARGB_8_PREMUL = PT_ARGB_8 | LCMSImageLayout.PREMUL_SH(1);
    private static final int PT_BGR_8 = PT_RGB_8 | 0x400;
    private static final int PT_ABGR_8 = PT_BGR_8 | LCMSImageLayout.EXTRA_SH(1);
    private static final int PT_ABGR_8_PREMUL = PT_ABGR_8 | LCMSImageLayout.PREMUL_SH(1);
    private static final int SWAP_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? 1024 : 0;
    private static final int DT_BYTE = 0;
    private static final int DT_SHORT = 1;
    private static final int DT_INT = 2;
    int pixelType;
    int dataType;
    int width;
    int height;
    int nextRowOffset;
    private int nextPixelOffset;
    int offset;
    Object dataArray;
    private int dataArrayLength;

    static int BYTES_SH(int x) {
        return x;
    }

    private static int EXTRA_SH(int x) {
        return x << 7;
    }

    static int CHANNELS_SH(int x) {
        return x << 3;
    }

    static int PREMUL_SH(int x) {
        return x << 23;
    }

    private LCMSImageLayout(Object data, int length, int nc, int dt, int size) {
        this.dataArray = data;
        this.dataType = dt;
        this.dataArrayLength = length * size;
        this.pixelType = LCMSImageLayout.CHANNELS_SH(nc) | LCMSImageLayout.BYTES_SH(size);
        this.width = length / nc;
        this.height = 1;
        this.nextPixelOffset = nc * size;
        this.nextRowOffset = this.dataArrayLength;
        this.verify();
    }

    LCMSImageLayout(byte[] data, int nc) {
        this(data, data.length, nc, 0, 1);
    }

    LCMSImageLayout(short[] data, int nc) {
        this(data, data.length, nc, 1, 2);
    }

    private LCMSImageLayout() {
    }

    static LCMSImageLayout createImageLayout(BufferedImage image) {
        LCMSImageLayout l = new LCMSImageLayout();
        switch (image.getType()) {
            case 1: 
            case 2: {
                l.pixelType = PT_ARGB_8 ^ SWAP_ENDIAN;
                break;
            }
            case 3: {
                l.pixelType = PT_ARGB_8_PREMUL ^ SWAP_ENDIAN;
                break;
            }
            case 4: {
                l.pixelType = PT_ABGR_8 ^ SWAP_ENDIAN;
                break;
            }
            case 5: {
                l.pixelType = PT_BGR_8;
                break;
            }
            case 6: {
                l.pixelType = PT_ABGR_8;
                break;
            }
            case 7: {
                l.pixelType = PT_ABGR_8_PREMUL;
                break;
            }
            case 10: {
                l.pixelType = PT_GRAY_8;
                break;
            }
            case 11: {
                l.pixelType = PT_GRAY_16;
                break;
            }
            default: {
                ColorModel cm = image.getColorModel();
                if (cm instanceof ComponentColorModel) {
                    int[] cs;
                    ComponentColorModel ccm = (ComponentColorModel)cm;
                    for (int s : cs = ccm.getComponentSize()) {
                        if (s == 8) continue;
                        return null;
                    }
                    return LCMSImageLayout.createImageLayout(image.getRaster(), cm);
                }
                return null;
            }
        }
        l.width = image.getWidth();
        l.height = image.getHeight();
        switch (image.getType()) {
            case 1: 
            case 2: 
            case 3: 
            case 4: {
                IntegerComponentRaster intRaster = (IntegerComponentRaster)image.getRaster();
                l.nextRowOffset = LCMSImageLayout.safeMult(4, intRaster.getScanlineStride());
                l.nextPixelOffset = LCMSImageLayout.safeMult(4, intRaster.getPixelStride());
                l.offset = LCMSImageLayout.safeMult(4, intRaster.getDataOffset(0));
                l.dataArray = intRaster.getDataStorage();
                l.dataArrayLength = 4 * intRaster.getDataStorage().length;
                l.dataType = 2;
                break;
            }
            case 5: 
            case 6: 
            case 7: {
                ByteComponentRaster byteRaster = (ByteComponentRaster)image.getRaster();
                l.nextRowOffset = byteRaster.getScanlineStride();
                l.nextPixelOffset = byteRaster.getPixelStride();
                int firstBand = byteRaster.getSampleModel().getNumBands() - 1;
                l.offset = byteRaster.getDataOffset(firstBand);
                l.dataArray = byteRaster.getDataStorage();
                l.dataArrayLength = byteRaster.getDataStorage().length;
                l.dataType = 0;
                break;
            }
            case 10: {
                ByteComponentRaster byteRaster = (ByteComponentRaster)image.getRaster();
                l.nextRowOffset = byteRaster.getScanlineStride();
                l.nextPixelOffset = byteRaster.getPixelStride();
                l.offset = byteRaster.getDataOffset(0);
                l.dataArray = byteRaster.getDataStorage();
                l.dataArrayLength = byteRaster.getDataStorage().length;
                l.dataType = 0;
                break;
            }
            case 11: {
                ShortComponentRaster shortRaster = (ShortComponentRaster)image.getRaster();
                l.nextRowOffset = LCMSImageLayout.safeMult(2, shortRaster.getScanlineStride());
                l.nextPixelOffset = LCMSImageLayout.safeMult(2, shortRaster.getPixelStride());
                l.offset = LCMSImageLayout.safeMult(2, shortRaster.getDataOffset(0));
                l.dataArray = shortRaster.getDataStorage();
                l.dataArrayLength = 2 * shortRaster.getDataStorage().length;
                l.dataType = 1;
                break;
            }
            default: {
                return null;
            }
        }
        l.verify();
        return l;
    }

    private void verify() {
        LCMSImageLayout.checkIndex(this.offset, this.dataArrayLength);
        if (this.nextPixelOffset != LCMSImageLayout.getBytesPerPixel(this.pixelType)) {
            throw new CMMException("Invalid image layout");
        }
        int lastScanOffset = LCMSImageLayout.safeMult(this.nextRowOffset, this.height - 1);
        int lastPixelOffset = LCMSImageLayout.safeMult(this.nextPixelOffset, this.width - 1);
        long off = (long)this.offset + (long)lastPixelOffset + (long)lastScanOffset;
        LCMSImageLayout.checkIndex(off, this.dataArrayLength);
    }

    private static int checkIndex(long index, int length) {
        if (index < 0L || index >= (long)length) {
            throw new CMMException("Invalid image layout");
        }
        return (int)index;
    }

    private static int safeMult(int a, int b) {
        long res = (long)a * (long)b;
        return LCMSImageLayout.checkIndex(res, Integer.MAX_VALUE);
    }

    static LCMSImageLayout createImageLayout(Raster r, ColorModel cm) {
        LCMSImageLayout l = new LCMSImageLayout();
        if (r instanceof ByteComponentRaster) {
            ByteComponentRaster br = (ByteComponentRaster)r;
            SampleModel sampleModel = r.getSampleModel();
            if (sampleModel instanceof ComponentSampleModel) {
                ComponentSampleModel csm = (ComponentSampleModel)sampleModel;
                int numBands = br.getNumBands();
                boolean hasAlpha = cm != null && cm.hasAlpha();
                l.pixelType = (hasAlpha ? LCMSImageLayout.CHANNELS_SH(numBands - 1) | LCMSImageLayout.EXTRA_SH(1) : LCMSImageLayout.CHANNELS_SH(numBands)) | LCMSImageLayout.BYTES_SH(1);
                if (hasAlpha && cm.isAlphaPremultiplied()) {
                    l.pixelType |= LCMSImageLayout.PREMUL_SH(1);
                }
                int[] bandOffsets = csm.getBandOffsets();
                BandOrder order = BandOrder.getBandOrder(bandOffsets);
                int firstBand = 0;
                switch (order.ordinal()) {
                    case 1: {
                        l.pixelType |= 0x400;
                        firstBand = numBands - 1;
                        break;
                    }
                    case 0: {
                        break;
                    }
                    default: {
                        return null;
                    }
                }
                l.nextRowOffset = br.getScanlineStride();
                l.nextPixelOffset = br.getPixelStride();
                l.offset = br.getDataOffset(firstBand);
                l.dataType = 0;
                byte[] data = br.getDataStorage();
                l.dataArray = data;
                l.dataArrayLength = data.length;
                l.width = br.getWidth();
                l.height = br.getHeight();
                l.verify();
                return l;
            }
        }
        return null;
    }

    private static int getBytesPerPixel(int pixelType) {
        int bytesPerSample = 7 & pixelType;
        int colorSamplesPerPixel = 0xF & pixelType >> 3;
        int extraSamplesPerPixel = 7 & pixelType >> 7;
        return bytesPerSample * (colorSamplesPerPixel + extraSamplesPerPixel);
    }

    private static enum BandOrder {
        DIRECT,
        INVERTED,
        ARBITRARY,
        UNKNOWN;


        static BandOrder getBandOrder(int[] bandOffsets) {
            BandOrder order = UNKNOWN;
            int numBands = bandOffsets.length;
            block5: for (int i = 0; order != ARBITRARY && i < bandOffsets.length; ++i) {
                switch (order.ordinal()) {
                    case 3: {
                        if (bandOffsets[i] == i) {
                            order = DIRECT;
                            continue block5;
                        }
                        if (bandOffsets[i] == numBands - 1 - i) {
                            order = INVERTED;
                            continue block5;
                        }
                        order = ARBITRARY;
                        continue block5;
                    }
                    case 0: {
                        if (bandOffsets[i] == i) continue block5;
                        order = ARBITRARY;
                        continue block5;
                    }
                    case 1: {
                        if (bandOffsets[i] == numBands - 1 - i) continue block5;
                        order = ARBITRARY;
                    }
                }
            }
            return order;
        }
    }
}

