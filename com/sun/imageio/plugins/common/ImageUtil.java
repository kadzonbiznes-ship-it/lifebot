/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.common;

import com.sun.imageio.plugins.common.BogusColorSpace;
import com.sun.imageio.plugins.common.I18N;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.Iterator;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;

public class ImageUtil {
    public static final ColorModel createColorModel(SampleModel sampleModel) {
        if (sampleModel == null) {
            throw new IllegalArgumentException("sampleModel == null!");
        }
        int dataType = sampleModel.getDataType();
        switch (dataType) {
            case 0: 
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 5: {
                break;
            }
            default: {
                return null;
            }
        }
        ColorModel colorModel = null;
        int[] sampleSize = sampleModel.getSampleSize();
        if (sampleModel instanceof ComponentSampleModel) {
            int numBands = sampleModel.getNumBands();
            ColorSpace colorSpace = null;
            colorSpace = numBands <= 2 ? ColorSpace.getInstance(1003) : (numBands <= 4 ? ColorSpace.getInstance(1000) : new BogusColorSpace(numBands));
            boolean hasAlpha = numBands == 2 || numBands == 4;
            boolean isAlphaPremultiplied = false;
            int transparency = hasAlpha ? 3 : 1;
            colorModel = new ComponentColorModel(colorSpace, sampleSize, hasAlpha, isAlphaPremultiplied, transparency, dataType);
        } else {
            if (sampleModel.getNumBands() <= 4 && sampleModel instanceof SinglePixelPackedSampleModel) {
                SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)sampleModel;
                int[] bitMasks = sppsm.getBitMasks();
                int rmask = 0;
                int gmask = 0;
                int bmask = 0;
                int amask = 0;
                int numBands = bitMasks.length;
                if (numBands <= 2) {
                    gmask = bmask = bitMasks[0];
                    rmask = bmask;
                    if (numBands == 2) {
                        amask = bitMasks[1];
                    }
                } else {
                    rmask = bitMasks[0];
                    gmask = bitMasks[1];
                    bmask = bitMasks[2];
                    if (numBands == 4) {
                        amask = bitMasks[3];
                    }
                }
                int bits = 0;
                for (int i = 0; i < sampleSize.length; ++i) {
                    bits += sampleSize[i];
                }
                return new DirectColorModel(bits, rmask, gmask, bmask, amask);
            }
            if (sampleModel instanceof MultiPixelPackedSampleModel) {
                int bitsPerSample = sampleSize[0];
                int numEntries = 1 << bitsPerSample;
                byte[] map = new byte[numEntries];
                for (int i = 0; i < numEntries; ++i) {
                    map[i] = (byte)(i * 255 / (numEntries - 1));
                }
                colorModel = new IndexColorModel(bitsPerSample, numEntries, map, map, map);
            }
        }
        return colorModel;
    }

    public static byte[] getPackedBinaryData(Raster raster, Rectangle rect) {
        byte[] binaryDataArray;
        block29: {
            int numBytesPerRow;
            int bitOffset;
            int eltOffset;
            int lineStride;
            DataBuffer dataBuffer;
            int rectHeight;
            int rectWidth;
            block27: {
                block30: {
                    block28: {
                        SampleModel sm = raster.getSampleModel();
                        if (!ImageUtil.isBinary(sm)) {
                            throw new IllegalArgumentException(I18N.getString("ImageUtil0"));
                        }
                        int rectX = rect.x;
                        int rectY = rect.y;
                        rectWidth = rect.width;
                        rectHeight = rect.height;
                        dataBuffer = raster.getDataBuffer();
                        int dx = rectX - raster.getSampleModelTranslateX();
                        int dy = rectY - raster.getSampleModelTranslateY();
                        MultiPixelPackedSampleModel mpp = (MultiPixelPackedSampleModel)sm;
                        lineStride = mpp.getScanlineStride();
                        eltOffset = dataBuffer.getOffset() + mpp.getOffset(dx, dy);
                        bitOffset = mpp.getBitOffset(dx);
                        numBytesPerRow = (rectWidth + 7) / 8;
                        if (dataBuffer instanceof DataBufferByte && eltOffset == 0 && bitOffset == 0 && numBytesPerRow == lineStride && ((DataBufferByte)dataBuffer).getData().length == numBytesPerRow * rectHeight) {
                            return ((DataBufferByte)dataBuffer).getData();
                        }
                        binaryDataArray = new byte[numBytesPerRow * rectHeight];
                        int b = 0;
                        if (bitOffset != 0) break block27;
                        if (!(dataBuffer instanceof DataBufferByte)) break block28;
                        byte[] data = ((DataBufferByte)dataBuffer).getData();
                        int stride = numBytesPerRow;
                        int offset = 0;
                        for (int y = 0; y < rectHeight; ++y) {
                            System.arraycopy(data, eltOffset, binaryDataArray, offset, stride);
                            offset += stride;
                            eltOffset += lineStride;
                        }
                        break block29;
                    }
                    if (!(dataBuffer instanceof DataBufferShort) && !(dataBuffer instanceof DataBufferUShort)) break block30;
                    short[] data = dataBuffer instanceof DataBufferShort ? ((DataBufferShort)dataBuffer).getData() : ((DataBufferUShort)dataBuffer).getData();
                    for (int y = 0; y < rectHeight; ++y) {
                        int xRemaining;
                        int i = eltOffset;
                        for (xRemaining = rectWidth; xRemaining > 8; xRemaining -= 16) {
                            short datum = data[i++];
                            binaryDataArray[b++] = (byte)(datum >>> 8 & 0xFF);
                            binaryDataArray[b++] = (byte)(datum & 0xFF);
                        }
                        if (xRemaining > 0) {
                            binaryDataArray[b++] = (byte)(data[i] >>> 8 & 0xFF);
                        }
                        eltOffset += lineStride;
                    }
                    break block29;
                }
                if (!(dataBuffer instanceof DataBufferInt)) break block29;
                int[] data = ((DataBufferInt)dataBuffer).getData();
                for (int y = 0; y < rectHeight; ++y) {
                    int xRemaining;
                    int i = eltOffset;
                    for (xRemaining = rectWidth; xRemaining > 24; xRemaining -= 32) {
                        int datum = data[i++];
                        binaryDataArray[b++] = (byte)(datum >>> 24 & 0xFF);
                        binaryDataArray[b++] = (byte)(datum >>> 16 & 0xFF);
                        binaryDataArray[b++] = (byte)(datum >>> 8 & 0xFF);
                        binaryDataArray[b++] = (byte)(datum & 0xFF);
                    }
                    int shift = 24;
                    while (xRemaining > 0) {
                        binaryDataArray[b++] = (byte)(data[i] >>> shift & 0xFF);
                        shift -= 8;
                        xRemaining -= 8;
                    }
                    eltOffset += lineStride;
                }
                break block29;
            }
            if (dataBuffer instanceof DataBufferByte) {
                byte[] data = ((DataBufferByte)dataBuffer).getData();
                if ((bitOffset & 7) == 0) {
                    int stride = numBytesPerRow;
                    int offset = 0;
                    for (int y = 0; y < rectHeight; ++y) {
                        System.arraycopy(data, eltOffset, binaryDataArray, offset, stride);
                        offset += stride;
                        eltOffset += lineStride;
                    }
                } else {
                    int leftShift = bitOffset & 7;
                    int rightShift = 8 - leftShift;
                    for (int y = 0; y < rectHeight; ++y) {
                        int i = eltOffset;
                        for (int xRemaining = rectWidth; xRemaining > 0; xRemaining -= 8) {
                            binaryDataArray[b++] = xRemaining > rightShift ? (byte)((data[i++] & 0xFF) << leftShift | (data[i] & 0xFF) >>> rightShift) : (byte)((data[i] & 0xFF) << leftShift);
                        }
                        eltOffset += lineStride;
                    }
                }
            } else if (dataBuffer instanceof DataBufferShort || dataBuffer instanceof DataBufferUShort) {
                short[] data = dataBuffer instanceof DataBufferShort ? ((DataBufferShort)dataBuffer).getData() : ((DataBufferUShort)dataBuffer).getData();
                for (int y = 0; y < rectHeight; ++y) {
                    int bOffset = bitOffset;
                    int x = 0;
                    while (x < rectWidth) {
                        int i = eltOffset + bOffset / 16;
                        int mod = bOffset % 16;
                        int left = data[i] & 0xFFFF;
                        if (mod <= 8) {
                            binaryDataArray[b++] = (byte)(left >>> 8 - mod);
                        } else {
                            int delta = mod - 8;
                            int right = data[i + 1] & 0xFFFF;
                            binaryDataArray[b++] = (byte)(left << delta | right >>> 16 - delta);
                        }
                        x += 8;
                        bOffset += 8;
                    }
                    eltOffset += lineStride;
                }
            } else if (dataBuffer instanceof DataBufferInt) {
                int[] data = ((DataBufferInt)dataBuffer).getData();
                for (int y = 0; y < rectHeight; ++y) {
                    int bOffset = bitOffset;
                    int x = 0;
                    while (x < rectWidth) {
                        int i = eltOffset + bOffset / 32;
                        int mod = bOffset % 32;
                        int left = data[i];
                        if (mod <= 24) {
                            binaryDataArray[b++] = (byte)(left >>> 24 - mod);
                        } else {
                            int delta = mod - 24;
                            int right = data[i + 1];
                            binaryDataArray[b++] = (byte)(left << delta | right >>> 32 - delta);
                        }
                        x += 8;
                        bOffset += 8;
                    }
                    eltOffset += lineStride;
                }
            }
        }
        return binaryDataArray;
    }

    public static byte[] getUnpackedBinaryData(Raster raster, Rectangle rect) {
        byte[] bdata;
        block8: {
            int maxX;
            int maxY;
            int bitOffset;
            int eltOffset;
            int lineStride;
            DataBuffer dataBuffer;
            block9: {
                block7: {
                    SampleModel sm = raster.getSampleModel();
                    if (!ImageUtil.isBinary(sm)) {
                        throw new IllegalArgumentException(I18N.getString("ImageUtil0"));
                    }
                    int rectX = rect.x;
                    int rectY = rect.y;
                    int rectWidth = rect.width;
                    int rectHeight = rect.height;
                    dataBuffer = raster.getDataBuffer();
                    int dx = rectX - raster.getSampleModelTranslateX();
                    int dy = rectY - raster.getSampleModelTranslateY();
                    MultiPixelPackedSampleModel mpp = (MultiPixelPackedSampleModel)sm;
                    lineStride = mpp.getScanlineStride();
                    eltOffset = dataBuffer.getOffset() + mpp.getOffset(dx, dy);
                    bitOffset = mpp.getBitOffset(dx);
                    bdata = new byte[rectWidth * rectHeight];
                    maxY = rectY + rectHeight;
                    maxX = rectX + rectWidth;
                    int k = 0;
                    if (!(dataBuffer instanceof DataBufferByte)) break block7;
                    byte[] data = ((DataBufferByte)dataBuffer).getData();
                    for (int y = rectY; y < maxY; ++y) {
                        int bOffset = eltOffset * 8 + bitOffset;
                        for (int x = rectX; x < maxX; ++x) {
                            byte b = data[bOffset / 8];
                            bdata[k++] = (byte)(b >>> (7 - bOffset & 7) & 1);
                            ++bOffset;
                        }
                        eltOffset += lineStride;
                    }
                    break block8;
                }
                if (!(dataBuffer instanceof DataBufferShort) && !(dataBuffer instanceof DataBufferUShort)) break block9;
                short[] data = dataBuffer instanceof DataBufferShort ? ((DataBufferShort)dataBuffer).getData() : ((DataBufferUShort)dataBuffer).getData();
                for (int y = rectY; y < maxY; ++y) {
                    int bOffset = eltOffset * 16 + bitOffset;
                    for (int x = rectX; x < maxX; ++x) {
                        short s = data[bOffset / 16];
                        bdata[k++] = (byte)(s >>> 15 - bOffset % 16 & 1);
                        ++bOffset;
                    }
                    eltOffset += lineStride;
                }
                break block8;
            }
            if (!(dataBuffer instanceof DataBufferInt)) break block8;
            int[] data = ((DataBufferInt)dataBuffer).getData();
            for (int y = rectY; y < maxY; ++y) {
                int bOffset = eltOffset * 32 + bitOffset;
                for (int x = rectX; x < maxX; ++x) {
                    int i = data[bOffset / 32];
                    bdata[k++] = (byte)(i >>> 31 - bOffset % 32 & 1);
                    ++bOffset;
                }
                eltOffset += lineStride;
            }
        }
        return bdata;
    }

    public static void setPackedBinaryData(byte[] binaryDataArray, WritableRaster raster, Rectangle rect) {
        block41: {
            int b;
            int bitOffset;
            int eltOffset;
            int lineStride;
            DataBuffer dataBuffer;
            int rectHeight;
            int rectWidth;
            block39: {
                block42: {
                    block40: {
                        SampleModel sm = raster.getSampleModel();
                        if (!ImageUtil.isBinary(sm)) {
                            throw new IllegalArgumentException(I18N.getString("ImageUtil0"));
                        }
                        int rectX = rect.x;
                        int rectY = rect.y;
                        rectWidth = rect.width;
                        rectHeight = rect.height;
                        dataBuffer = raster.getDataBuffer();
                        int dx = rectX - raster.getSampleModelTranslateX();
                        int dy = rectY - raster.getSampleModelTranslateY();
                        MultiPixelPackedSampleModel mpp = (MultiPixelPackedSampleModel)sm;
                        lineStride = mpp.getScanlineStride();
                        eltOffset = dataBuffer.getOffset() + mpp.getOffset(dx, dy);
                        bitOffset = mpp.getBitOffset(dx);
                        b = 0;
                        if (bitOffset != 0) break block39;
                        if (!(dataBuffer instanceof DataBufferByte)) break block40;
                        byte[] data = ((DataBufferByte)dataBuffer).getData();
                        if (data == binaryDataArray) {
                            return;
                        }
                        int stride = (rectWidth + 7) / 8;
                        int offset = 0;
                        for (int y = 0; y < rectHeight; ++y) {
                            System.arraycopy(binaryDataArray, offset, data, eltOffset, stride);
                            offset += stride;
                            eltOffset += lineStride;
                        }
                        break block41;
                    }
                    if (!(dataBuffer instanceof DataBufferShort) && !(dataBuffer instanceof DataBufferUShort)) break block42;
                    short[] data = dataBuffer instanceof DataBufferShort ? ((DataBufferShort)dataBuffer).getData() : ((DataBufferUShort)dataBuffer).getData();
                    for (int y = 0; y < rectHeight; ++y) {
                        int xRemaining;
                        int i = eltOffset;
                        for (xRemaining = rectWidth; xRemaining > 8; xRemaining -= 16) {
                            data[i++] = (short)((binaryDataArray[b++] & 0xFF) << 8 | binaryDataArray[b++] & 0xFF);
                        }
                        if (xRemaining > 0) {
                            data[i++] = (short)((binaryDataArray[b++] & 0xFF) << 8);
                        }
                        eltOffset += lineStride;
                    }
                    break block41;
                }
                if (!(dataBuffer instanceof DataBufferInt)) break block41;
                int[] data = ((DataBufferInt)dataBuffer).getData();
                for (int y = 0; y < rectHeight; ++y) {
                    int xRemaining;
                    int i = eltOffset;
                    for (xRemaining = rectWidth; xRemaining > 24; xRemaining -= 32) {
                        data[i++] = (binaryDataArray[b++] & 0xFF) << 24 | (binaryDataArray[b++] & 0xFF) << 16 | (binaryDataArray[b++] & 0xFF) << 8 | binaryDataArray[b++] & 0xFF;
                    }
                    int shift = 24;
                    while (xRemaining > 0) {
                        int n = i;
                        data[n] = data[n] | (binaryDataArray[b++] & 0xFF) << shift;
                        shift -= 8;
                        xRemaining -= 8;
                    }
                    eltOffset += lineStride;
                }
                break block41;
            }
            int stride = (rectWidth + 7) / 8;
            int offset = 0;
            if (dataBuffer instanceof DataBufferByte) {
                byte[] data = ((DataBufferByte)dataBuffer).getData();
                if ((bitOffset & 7) == 0) {
                    for (int y = 0; y < rectHeight; ++y) {
                        System.arraycopy(binaryDataArray, offset, data, eltOffset, stride);
                        offset += stride;
                        eltOffset += lineStride;
                    }
                } else {
                    int rightShift = bitOffset & 7;
                    int leftShift = 8 - rightShift;
                    int leftShift8 = 8 + leftShift;
                    byte mask = (byte)(255 << leftShift);
                    byte mask1 = ~mask;
                    for (int y = 0; y < rectHeight; ++y) {
                        int i = eltOffset;
                        for (int xRemaining = rectWidth; xRemaining > 0; xRemaining -= 8) {
                            byte datum = binaryDataArray[b++];
                            if (xRemaining > leftShift8) {
                                data[i] = (byte)(data[i] & mask | (datum & 0xFF) >>> rightShift);
                                data[++i] = (byte)((datum & 0xFF) << leftShift);
                                continue;
                            }
                            if (xRemaining > leftShift) {
                                data[i] = (byte)(data[i] & mask | (datum & 0xFF) >>> rightShift);
                                data[++i] = (byte)(data[i] & mask1 | (datum & 0xFF) << leftShift);
                                continue;
                            }
                            int remainMask = (1 << leftShift - xRemaining) - 1;
                            data[i] = (byte)(data[i] & (mask | remainMask) | (datum & 0xFF) >>> rightShift & ~remainMask);
                        }
                        eltOffset += lineStride;
                    }
                }
            } else if (dataBuffer instanceof DataBufferShort || dataBuffer instanceof DataBufferUShort) {
                short[] data = dataBuffer instanceof DataBufferShort ? ((DataBufferShort)dataBuffer).getData() : ((DataBufferUShort)dataBuffer).getData();
                int rightShift = bitOffset & 7;
                int leftShift = 8 - rightShift;
                int leftShift16 = 16 + leftShift;
                short mask = (short)(~(255 << leftShift));
                short mask1 = (short)(65535 << leftShift);
                short mask2 = ~mask1;
                for (int y = 0; y < rectHeight; ++y) {
                    int bOffset = bitOffset;
                    int xRemaining = rectWidth;
                    int x = 0;
                    while (x < rectWidth) {
                        int i = eltOffset + (bOffset >> 4);
                        int mod = bOffset & 0xF;
                        int datum = binaryDataArray[b++] & 0xFF;
                        if (mod <= 8) {
                            if (xRemaining < 8) {
                                datum &= 255 << 8 - xRemaining;
                            }
                            data[i] = (short)(data[i] & mask | datum << leftShift);
                        } else if (xRemaining > leftShift16) {
                            data[i] = (short)(data[i] & mask1 | datum >>> rightShift & 0xFFFF);
                            data[++i] = (short)(datum << leftShift & 0xFFFF);
                        } else if (xRemaining > leftShift) {
                            data[i] = (short)(data[i] & mask1 | datum >>> rightShift & 0xFFFF);
                            data[++i] = (short)(data[i] & mask2 | datum << leftShift & 0xFFFF);
                        } else {
                            int remainMask = (1 << leftShift - xRemaining) - 1;
                            data[i] = (short)(data[i] & (mask1 | remainMask) | datum >>> rightShift & 0xFFFF & ~remainMask);
                        }
                        x += 8;
                        bOffset += 8;
                        xRemaining -= 8;
                    }
                    eltOffset += lineStride;
                }
            } else if (dataBuffer instanceof DataBufferInt) {
                int[] data = ((DataBufferInt)dataBuffer).getData();
                int rightShift = bitOffset & 7;
                int leftShift = 8 - rightShift;
                int leftShift32 = 32 + leftShift;
                int mask = -1 << leftShift;
                int mask1 = ~mask;
                for (int y = 0; y < rectHeight; ++y) {
                    int bOffset = bitOffset;
                    int xRemaining = rectWidth;
                    int x = 0;
                    while (x < rectWidth) {
                        int i = eltOffset + (bOffset >> 5);
                        int mod = bOffset & 0x1F;
                        int datum = binaryDataArray[b++] & 0xFF;
                        if (mod <= 24) {
                            int shift = 24 - mod;
                            if (xRemaining < 8) {
                                datum &= 255 << 8 - xRemaining;
                            }
                            data[i] = data[i] & ~(255 << shift) | datum << shift;
                        } else if (xRemaining > leftShift32) {
                            data[i] = data[i] & mask | datum >>> rightShift;
                            data[++i] = datum << leftShift;
                        } else if (xRemaining > leftShift) {
                            data[i] = data[i] & mask | datum >>> rightShift;
                            data[++i] = data[i] & mask1 | datum << leftShift;
                        } else {
                            int remainMask = (1 << leftShift - xRemaining) - 1;
                            data[i] = data[i] & (mask | remainMask) | datum >>> rightShift & ~remainMask;
                        }
                        x += 8;
                        bOffset += 8;
                        xRemaining -= 8;
                    }
                    eltOffset += lineStride;
                }
            }
        }
    }

    public static void setUnpackedBinaryData(byte[] bdata, WritableRaster raster, Rectangle rect) {
        block11: {
            int k;
            int bitOffset;
            int eltOffset;
            int lineStride;
            DataBuffer dataBuffer;
            int rectHeight;
            int rectWidth;
            block12: {
                block10: {
                    SampleModel sm = raster.getSampleModel();
                    if (!ImageUtil.isBinary(sm)) {
                        throw new IllegalArgumentException(I18N.getString("ImageUtil0"));
                    }
                    int rectX = rect.x;
                    int rectY = rect.y;
                    rectWidth = rect.width;
                    rectHeight = rect.height;
                    dataBuffer = raster.getDataBuffer();
                    int dx = rectX - raster.getSampleModelTranslateX();
                    int dy = rectY - raster.getSampleModelTranslateY();
                    MultiPixelPackedSampleModel mpp = (MultiPixelPackedSampleModel)sm;
                    lineStride = mpp.getScanlineStride();
                    eltOffset = dataBuffer.getOffset() + mpp.getOffset(dx, dy);
                    bitOffset = mpp.getBitOffset(dx);
                    k = 0;
                    if (!(dataBuffer instanceof DataBufferByte)) break block10;
                    byte[] data = ((DataBufferByte)dataBuffer).getData();
                    for (int y = 0; y < rectHeight; ++y) {
                        int bOffset = eltOffset * 8 + bitOffset;
                        for (int x = 0; x < rectWidth; ++x) {
                            if (bdata[k++] != 0) {
                                int n = bOffset / 8;
                                data[n] = (byte)(data[n] | (byte)(1 << (7 - bOffset & 7)));
                            }
                            ++bOffset;
                        }
                        eltOffset += lineStride;
                    }
                    break block11;
                }
                if (!(dataBuffer instanceof DataBufferShort) && !(dataBuffer instanceof DataBufferUShort)) break block12;
                short[] data = dataBuffer instanceof DataBufferShort ? ((DataBufferShort)dataBuffer).getData() : ((DataBufferUShort)dataBuffer).getData();
                for (int y = 0; y < rectHeight; ++y) {
                    int bOffset = eltOffset * 16 + bitOffset;
                    for (int x = 0; x < rectWidth; ++x) {
                        if (bdata[k++] != 0) {
                            int n = bOffset / 16;
                            data[n] = (short)(data[n] | (short)(1 << 15 - bOffset % 16));
                        }
                        ++bOffset;
                    }
                    eltOffset += lineStride;
                }
                break block11;
            }
            if (!(dataBuffer instanceof DataBufferInt)) break block11;
            int[] data = ((DataBufferInt)dataBuffer).getData();
            for (int y = 0; y < rectHeight; ++y) {
                int bOffset = eltOffset * 32 + bitOffset;
                for (int x = 0; x < rectWidth; ++x) {
                    if (bdata[k++] != 0) {
                        int n = bOffset / 32;
                        data[n] = data[n] | 1 << 31 - bOffset % 32;
                    }
                    ++bOffset;
                }
                eltOffset += lineStride;
            }
        }
    }

    public static boolean isBinary(SampleModel sm) {
        return sm instanceof MultiPixelPackedSampleModel && ((MultiPixelPackedSampleModel)sm).getPixelBitStride() == 1 && sm.getNumBands() == 1;
    }

    public static ColorModel createColorModel(ColorSpace colorSpace, SampleModel sampleModel) {
        ColorModel colorModel = null;
        if (sampleModel == null) {
            throw new IllegalArgumentException(I18N.getString("ImageUtil1"));
        }
        int numBands = sampleModel.getNumBands();
        if (numBands < 1 || numBands > 4) {
            return null;
        }
        int dataType = sampleModel.getDataType();
        if (sampleModel instanceof ComponentSampleModel) {
            if (dataType < 0 || dataType > 5) {
                return null;
            }
            if (colorSpace == null) {
                colorSpace = numBands <= 2 ? ColorSpace.getInstance(1003) : ColorSpace.getInstance(1000);
            }
            boolean useAlpha = numBands == 2 || numBands == 4;
            int transparency = useAlpha ? 3 : 1;
            boolean premultiplied = false;
            int dataTypeSize = DataBuffer.getDataTypeSize(dataType);
            int[] bits = new int[numBands];
            for (int i = 0; i < numBands; ++i) {
                bits[i] = dataTypeSize;
            }
            colorModel = new ComponentColorModel(colorSpace, bits, useAlpha, premultiplied, transparency, dataType);
        } else if (sampleModel instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)sampleModel;
            int[] bitMasks = sppsm.getBitMasks();
            int rmask = 0;
            int gmask = 0;
            int bmask = 0;
            int amask = 0;
            numBands = bitMasks.length;
            if (numBands <= 2) {
                gmask = bmask = bitMasks[0];
                rmask = bmask;
                if (numBands == 2) {
                    amask = bitMasks[1];
                }
            } else {
                rmask = bitMasks[0];
                gmask = bitMasks[1];
                bmask = bitMasks[2];
                if (numBands == 4) {
                    amask = bitMasks[3];
                }
            }
            int[] sampleSize = sppsm.getSampleSize();
            int bits = 0;
            for (int i = 0; i < sampleSize.length; ++i) {
                bits += sampleSize[i];
            }
            if (colorSpace == null) {
                colorSpace = ColorSpace.getInstance(1000);
            }
            colorModel = new DirectColorModel(colorSpace, bits, rmask, gmask, bmask, amask, false, sampleModel.getDataType());
        } else if (sampleModel instanceof MultiPixelPackedSampleModel) {
            int bits = ((MultiPixelPackedSampleModel)sampleModel).getPixelBitStride();
            int size = 1 << bits;
            byte[] comp = new byte[size];
            for (int i = 0; i < size; ++i) {
                comp[i] = (byte)(255 * i / (size - 1));
            }
            colorModel = new IndexColorModel(bits, size, comp, comp, comp);
        }
        return colorModel;
    }

    public static int getElementSize(SampleModel sm) {
        int elementSize = DataBuffer.getDataTypeSize(sm.getDataType());
        if (sm instanceof MultiPixelPackedSampleModel) {
            MultiPixelPackedSampleModel mppsm = (MultiPixelPackedSampleModel)sm;
            return mppsm.getSampleSize(0) * mppsm.getNumBands();
        }
        if (sm instanceof ComponentSampleModel) {
            return sm.getNumBands() * elementSize;
        }
        if (sm instanceof SinglePixelPackedSampleModel) {
            return elementSize;
        }
        return elementSize * sm.getNumBands();
    }

    public static long getTileSize(SampleModel sm) {
        int elementSize = DataBuffer.getDataTypeSize(sm.getDataType());
        if (sm instanceof MultiPixelPackedSampleModel) {
            MultiPixelPackedSampleModel mppsm = (MultiPixelPackedSampleModel)sm;
            return (mppsm.getScanlineStride() * mppsm.getHeight() + (mppsm.getDataBitOffset() + elementSize - 1) / elementSize) * ((elementSize + 7) / 8);
        }
        if (sm instanceof ComponentSampleModel) {
            ComponentSampleModel csm = (ComponentSampleModel)sm;
            int[] bandOffsets = csm.getBandOffsets();
            int maxBandOff = bandOffsets[0];
            for (int i = 1; i < bandOffsets.length; ++i) {
                maxBandOff = Math.max(maxBandOff, bandOffsets[i]);
            }
            long size = 0L;
            int pixelStride = csm.getPixelStride();
            int scanlineStride = csm.getScanlineStride();
            if (maxBandOff >= 0) {
                size += (long)(maxBandOff + 1);
            }
            if (pixelStride > 0) {
                size += (long)(pixelStride * (sm.getWidth() - 1));
            }
            if (scanlineStride > 0) {
                size += (long)(scanlineStride * (sm.getHeight() - 1));
            }
            int[] bankIndices = csm.getBankIndices();
            maxBandOff = bankIndices[0];
            for (int i = 1; i < bankIndices.length; ++i) {
                maxBandOff = Math.max(maxBandOff, bankIndices[i]);
            }
            return size * (long)(maxBandOff + 1) * (long)((elementSize + 7) / 8);
        }
        if (sm instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)sm;
            long size = sppsm.getScanlineStride() * (sppsm.getHeight() - 1) + sppsm.getWidth();
            return size * (long)((elementSize + 7) / 8);
        }
        return 0L;
    }

    public static long getBandSize(SampleModel sm) {
        int elementSize = DataBuffer.getDataTypeSize(sm.getDataType());
        if (sm instanceof ComponentSampleModel) {
            ComponentSampleModel csm = (ComponentSampleModel)sm;
            int pixelStride = csm.getPixelStride();
            int scanlineStride = csm.getScanlineStride();
            long size = Math.min(pixelStride, scanlineStride);
            if (pixelStride > 0) {
                size += (long)(pixelStride * (sm.getWidth() - 1));
            }
            if (scanlineStride > 0) {
                size += (long)(scanlineStride * (sm.getHeight() - 1));
            }
            return size * (long)((elementSize + 7) / 8);
        }
        return ImageUtil.getTileSize(sm);
    }

    public static boolean isIndicesForGrayscale(byte[] r, byte[] g, byte[] b) {
        if (r.length != g.length || r.length != b.length) {
            return false;
        }
        int size = r.length;
        if (size != 256) {
            return false;
        }
        for (int i = 0; i < size; ++i) {
            byte temp = (byte)i;
            if (r[i] == temp && g[i] == temp && b[i] == temp) continue;
            return false;
        }
        return true;
    }

    public static String convertObjectToString(Object obj) {
        if (obj == null) {
            return "";
        }
        Object s = "";
        if (obj instanceof byte[]) {
            byte[] bArray = (byte[])obj;
            for (int i = 0; i < bArray.length; ++i) {
                s = (String)s + bArray[i] + " ";
            }
            return s;
        }
        if (obj instanceof int[]) {
            int[] iArray = (int[])obj;
            for (int i = 0; i < iArray.length; ++i) {
                s = (String)s + iArray[i] + " ";
            }
            return s;
        }
        if (obj instanceof short[]) {
            short[] sArray = (short[])obj;
            for (int i = 0; i < sArray.length; ++i) {
                s = (String)s + sArray[i] + " ";
            }
            return s;
        }
        return obj.toString();
    }

    public static final void canEncodeImage(ImageWriter writer, ImageTypeSpecifier type) throws IIOException {
        ImageWriterSpi spi = writer.getOriginatingProvider();
        if (type != null && spi != null && !spi.canEncodeImage(type)) {
            throw new IIOException(I18N.getString("ImageUtil2") + " " + writer.getClass().getName());
        }
    }

    public static final void canEncodeImage(ImageWriter writer, ColorModel colorModel, SampleModel sampleModel) throws IIOException {
        ImageTypeSpecifier type = null;
        if (colorModel != null && sampleModel != null) {
            type = new ImageTypeSpecifier(colorModel, sampleModel);
        }
        ImageUtil.canEncodeImage(writer, type);
    }

    public static final boolean imageIsContiguous(RenderedImage image) {
        SampleModel sm;
        if (image instanceof BufferedImage) {
            WritableRaster ras = ((BufferedImage)image).getRaster();
            sm = ras.getSampleModel();
        } else {
            sm = image.getSampleModel();
        }
        if (sm instanceof ComponentSampleModel) {
            ComponentSampleModel csm = (ComponentSampleModel)sm;
            if (csm.getPixelStride() != csm.getNumBands()) {
                return false;
            }
            int[] bandOffsets = csm.getBandOffsets();
            for (int i = 0; i < bandOffsets.length; ++i) {
                if (bandOffsets[i] == i) continue;
                return false;
            }
            int[] bankIndices = csm.getBankIndices();
            for (int i = 0; i < bandOffsets.length; ++i) {
                if (bankIndices[i] == 0) continue;
                return false;
            }
            return true;
        }
        return ImageUtil.isBinary(sm);
    }

    public static final ImageTypeSpecifier getDestinationType(ImageReadParam param, Iterator<ImageTypeSpecifier> imageTypes) throws IIOException {
        if (imageTypes == null || !imageTypes.hasNext()) {
            throw new IllegalArgumentException("imageTypes null or empty!");
        }
        ImageTypeSpecifier imageType = null;
        if (param != null) {
            imageType = param.getDestinationType();
        }
        if (imageType == null) {
            ImageTypeSpecifier o = imageTypes.next();
            if (!(o instanceof ImageTypeSpecifier)) {
                throw new IllegalArgumentException("Non-ImageTypeSpecifier retrieved from imageTypes!");
            }
            imageType = o;
        } else {
            boolean foundIt = false;
            while (imageTypes.hasNext()) {
                ImageTypeSpecifier type = imageTypes.next();
                if (!type.equals(imageType)) continue;
                foundIt = true;
                break;
            }
            if (!foundIt) {
                throw new IIOException("Destination type from ImageReadParam does not match!");
            }
        }
        return imageType;
    }

    public static boolean isNonStandardICCColorSpace(ColorSpace cs) {
        return cs instanceof ICC_ColorSpace && !cs.isCS_sRGB() && !cs.equals(ColorSpace.getInstance(1004)) && !cs.equals(ColorSpace.getInstance(1003)) && !cs.equals(ColorSpace.getInstance(1001)) && !cs.equals(ColorSpace.getInstance(1002));
    }
}

