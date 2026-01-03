/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import sun.awt.image.NativeLibLoader;
import sun.awt.image.SunWritableRaster;

public class BytePackedRaster
extends SunWritableRaster {
    int dataBitOffset;
    int scanlineStride;
    int pixelBitStride;
    int bitMask;
    byte[] data;
    int shiftOffset;
    int type;
    private int maxX;
    private int maxY;

    private static native void initIDs();

    public BytePackedRaster(SampleModel sampleModel, Point origin) {
        this(sampleModel, (DataBufferByte)sampleModel.createDataBuffer(), new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
    }

    public BytePackedRaster(SampleModel sampleModel, DataBufferByte dataBuffer, Point origin) {
        this(sampleModel, dataBuffer, new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
    }

    public BytePackedRaster(SampleModel sampleModel, DataBufferByte dataBuffer, Rectangle aRegion, Point origin, BytePackedRaster parent) {
        super(sampleModel, (DataBuffer)dataBuffer, aRegion, origin, parent);
        this.maxX = this.minX + this.width;
        this.maxY = this.minY + this.height;
        this.data = BytePackedRaster.stealData(dataBuffer, 0);
        if (dataBuffer.getNumBanks() != 1) {
            throw new RasterFormatException("DataBuffer for BytePackedRasters must only have 1 bank.");
        }
        int dbOffset = dataBuffer.getOffset();
        if (sampleModel instanceof MultiPixelPackedSampleModel) {
            MultiPixelPackedSampleModel mppsm = (MultiPixelPackedSampleModel)sampleModel;
            this.type = 11;
            this.pixelBitStride = mppsm.getPixelBitStride();
            if (this.pixelBitStride != 1 && this.pixelBitStride != 2 && this.pixelBitStride != 4) {
                throw new RasterFormatException("BytePackedRasters must have a bit depth of 1, 2, or 4");
            }
            this.scanlineStride = mppsm.getScanlineStride();
            this.dataBitOffset = mppsm.getDataBitOffset() + dbOffset * 8;
            int xOffset = aRegion.x - origin.x;
            int yOffset = aRegion.y - origin.y;
            this.dataBitOffset += xOffset * this.pixelBitStride + yOffset * this.scanlineStride * 8;
        } else {
            throw new RasterFormatException("BytePackedRasters must haveMultiPixelPackedSampleModel");
        }
        this.bitMask = (1 << this.pixelBitStride) - 1;
        this.shiftOffset = 8 - this.pixelBitStride;
        this.verify(false);
    }

    public int getDataBitOffset() {
        return this.dataBitOffset;
    }

    public int getScanlineStride() {
        return this.scanlineStride;
    }

    public int getPixelBitStride() {
        return this.pixelBitStride;
    }

    public byte[] getDataStorage() {
        return this.data;
    }

    @Override
    public Object getDataElements(int x, int y, Object obj) {
        if (x < this.minX || y < this.minY || x >= this.maxX || y >= this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        byte[] outData = obj == null ? new byte[this.numDataElements] : (byte[])obj;
        int bitnum = this.dataBitOffset + (x - this.minX) * this.pixelBitStride;
        int element = this.data[(y - this.minY) * this.scanlineStride + (bitnum >> 3)] & 0xFF;
        int shift = this.shiftOffset - (bitnum & 7);
        outData[0] = (byte)(element >> shift & this.bitMask);
        return outData;
    }

    @Override
    public Object getDataElements(int x, int y, int w, int h, Object outData) {
        return this.getByteData(x, y, w, h, (byte[])outData);
    }

    public Object getPixelData(int x, int y, int w, int h, Object obj) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        byte[] outData = obj == null ? new byte[this.numDataElements * w * h] : (byte[])obj;
        int pixbits = this.pixelBitStride;
        int scanbit = this.dataBitOffset + (x - this.minX) * pixbits;
        int index = (y - this.minY) * this.scanlineStride;
        int outindex = 0;
        byte[] data = this.data;
        for (int j = 0; j < h; ++j) {
            int bitnum = scanbit;
            for (int i = 0; i < w; ++i) {
                int shift = this.shiftOffset - (bitnum & 7);
                outData[outindex++] = (byte)(this.bitMask & data[index + (bitnum >> 3)] >> shift);
                bitnum += pixbits;
            }
            index += this.scanlineStride;
        }
        return outData;
    }

    public byte[] getByteData(int x, int y, int w, int h, int band, byte[] outData) {
        return this.getByteData(x, y, w, h, outData);
    }

    public byte[] getByteData(int x, int y, int w, int h, byte[] outData) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        if (outData == null) {
            outData = new byte[w * h];
        }
        int pixbits = this.pixelBitStride;
        int scanbit = this.dataBitOffset + (x - this.minX) * pixbits;
        int index = (y - this.minY) * this.scanlineStride;
        int outindex = 0;
        byte[] data = this.data;
        for (int j = 0; j < h; ++j) {
            int i;
            int bitnum = scanbit;
            for (i = 0; i < w && (bitnum & 7) != 0; ++i) {
                int shift = this.shiftOffset - (bitnum & 7);
                outData[outindex++] = (byte)(this.bitMask & data[index + (bitnum >> 3)] >> shift);
                bitnum += pixbits;
            }
            int inIndex = index + (bitnum >> 3);
            switch (pixbits) {
                case 1: {
                    byte element;
                    while (i < w - 7) {
                        element = data[inIndex++];
                        outData[outindex++] = (byte)(element >> 7 & 1);
                        outData[outindex++] = (byte)(element >> 6 & 1);
                        outData[outindex++] = (byte)(element >> 5 & 1);
                        outData[outindex++] = (byte)(element >> 4 & 1);
                        outData[outindex++] = (byte)(element >> 3 & 1);
                        outData[outindex++] = (byte)(element >> 2 & 1);
                        outData[outindex++] = (byte)(element >> 1 & 1);
                        outData[outindex++] = (byte)(element & 1);
                        bitnum += 8;
                        i += 8;
                    }
                    break;
                }
                case 2: {
                    byte element;
                    while (i < w - 7) {
                        element = data[inIndex++];
                        outData[outindex++] = (byte)(element >> 6 & 3);
                        outData[outindex++] = (byte)(element >> 4 & 3);
                        outData[outindex++] = (byte)(element >> 2 & 3);
                        outData[outindex++] = (byte)(element & 3);
                        element = data[inIndex++];
                        outData[outindex++] = (byte)(element >> 6 & 3);
                        outData[outindex++] = (byte)(element >> 4 & 3);
                        outData[outindex++] = (byte)(element >> 2 & 3);
                        outData[outindex++] = (byte)(element & 3);
                        bitnum += 16;
                        i += 8;
                    }
                    break;
                }
                case 4: {
                    byte element;
                    while (i < w - 7) {
                        element = data[inIndex++];
                        outData[outindex++] = (byte)(element >> 4 & 0xF);
                        outData[outindex++] = (byte)(element & 0xF);
                        element = data[inIndex++];
                        outData[outindex++] = (byte)(element >> 4 & 0xF);
                        outData[outindex++] = (byte)(element & 0xF);
                        element = data[inIndex++];
                        outData[outindex++] = (byte)(element >> 4 & 0xF);
                        outData[outindex++] = (byte)(element & 0xF);
                        element = data[inIndex++];
                        outData[outindex++] = (byte)(element >> 4 & 0xF);
                        outData[outindex++] = (byte)(element & 0xF);
                        bitnum += 32;
                        i += 8;
                    }
                    break;
                }
            }
            while (i < w) {
                int shift = this.shiftOffset - (bitnum & 7);
                outData[outindex++] = (byte)(this.bitMask & data[index + (bitnum >> 3)] >> shift);
                bitnum += pixbits;
                ++i;
            }
            index += this.scanlineStride;
        }
        return outData;
    }

    @Override
    public void setDataElements(int x, int y, Object obj) {
        if (x < this.minX || y < this.minY || x >= this.maxX || y >= this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        byte[] inData = (byte[])obj;
        int bitnum = this.dataBitOffset + (x - this.minX) * this.pixelBitStride;
        int index = (y - this.minY) * this.scanlineStride + (bitnum >> 3);
        int shift = this.shiftOffset - (bitnum & 7);
        byte element = this.data[index];
        element = (byte)(element & ~(this.bitMask << shift));
        this.data[index] = element = (byte)(element | (inData[0] & this.bitMask) << shift);
        this.markDirty();
    }

    @Override
    public void setDataElements(int x, int y, Raster inRaster) {
        if (!(inRaster instanceof BytePackedRaster) || ((BytePackedRaster)inRaster).pixelBitStride != this.pixelBitStride) {
            super.setDataElements(x, y, inRaster);
            return;
        }
        int srcOffX = inRaster.getMinX();
        int srcOffY = inRaster.getMinY();
        int dstOffX = srcOffX + x;
        int dstOffY = srcOffY + y;
        int width = inRaster.getWidth();
        int height = inRaster.getHeight();
        if (dstOffX < this.minX || dstOffY < this.minY || dstOffX + width > this.maxX || dstOffY + height > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        this.setDataElements(dstOffX, dstOffY, srcOffX, srcOffY, width, height, (BytePackedRaster)inRaster);
    }

    private void setDataElements(int dstX, int dstY, int srcX, int srcY, int width, int height, BytePackedRaster inRaster) {
        if (width <= 0 || height <= 0) {
            return;
        }
        byte[] inData = inRaster.data;
        byte[] outData = this.data;
        int inscan = inRaster.scanlineStride;
        int outscan = this.scanlineStride;
        int inbit = inRaster.dataBitOffset + 8 * (srcY - inRaster.minY) * inscan + (srcX - inRaster.minX) * inRaster.pixelBitStride;
        int outbit = this.dataBitOffset + 8 * (dstY - this.minY) * outscan + (dstX - this.minX) * this.pixelBitStride;
        int copybits = width * this.pixelBitStride;
        if ((inbit & 7) == (outbit & 7)) {
            int j;
            int outbyte;
            int inbyte;
            int bitpos = outbit & 7;
            if (bitpos != 0) {
                int bits = 8 - bitpos;
                int inbyte2 = inbit >> 3;
                int outbyte2 = outbit >> 3;
                int mask = 255 >> bitpos;
                if (copybits < bits) {
                    mask &= 255 << bits - copybits;
                    bits = copybits;
                }
                for (int j2 = 0; j2 < height; ++j2) {
                    int element = outData[outbyte2];
                    element &= ~mask;
                    outData[outbyte2] = (byte)(element |= inData[inbyte2] & mask);
                    inbyte2 += inscan;
                    outbyte2 += outscan;
                }
                inbit += bits;
                outbit += bits;
                copybits -= bits;
            }
            if (copybits >= 8) {
                inbyte = inbit >> 3;
                outbyte = outbit >> 3;
                int copybytes = copybits >> 3;
                if (copybytes == inscan && inscan == outscan) {
                    System.arraycopy(inData, inbyte, outData, outbyte, inscan * height);
                } else {
                    for (j = 0; j < height; ++j) {
                        System.arraycopy(inData, inbyte, outData, outbyte, copybytes);
                        inbyte += inscan;
                        outbyte += outscan;
                    }
                }
                int bits = copybytes * 8;
                inbit += bits;
                outbit += bits;
                copybits -= bits;
            }
            if (copybits > 0) {
                inbyte = inbit >> 3;
                outbyte = outbit >> 3;
                int mask = 65280 >> copybits & 0xFF;
                for (j = 0; j < height; ++j) {
                    int element = outData[outbyte];
                    element &= ~mask;
                    outData[outbyte] = (byte)(element |= inData[inbyte] & mask);
                    inbyte += inscan;
                    outbyte += outscan;
                }
            }
        } else {
            int outbyte;
            int inbyte;
            byte inData0;
            int rshift;
            int lshift;
            int bitpos = outbit & 7;
            if (bitpos != 0 || copybits < 8) {
                int bits = 8 - bitpos;
                int inbyte3 = inbit >> 3;
                int outbyte3 = outbit >> 3;
                lshift = inbit & 7;
                rshift = 8 - lshift;
                int mask = 255 >> bitpos;
                if (copybits < bits) {
                    mask &= 255 << bits - copybits;
                    bits = copybits;
                }
                int lastByte = inData.length - 1;
                for (int j = 0; j < height; ++j) {
                    inData0 = inData[inbyte3];
                    byte inData1 = 0;
                    if (inbyte3 < lastByte) {
                        inData1 = inData[inbyte3 + 1];
                    }
                    int element = outData[outbyte3];
                    element &= ~mask;
                    outData[outbyte3] = (byte)(element |= (inData0 << lshift | (inData1 & 0xFF) >> rshift) >> bitpos & mask);
                    inbyte3 += inscan;
                    outbyte3 += outscan;
                }
                inbit += bits;
                outbit += bits;
                copybits -= bits;
            }
            if (copybits >= 8) {
                inbyte = inbit >> 3;
                outbyte = outbit >> 3;
                int copybytes = copybits >> 3;
                lshift = inbit & 7;
                rshift = 8 - lshift;
                for (int j = 0; j < height; ++j) {
                    int ibyte = inbyte + j * inscan;
                    int obyte = outbyte + j * outscan;
                    inData0 = inData[ibyte];
                    for (int i = 0; i < copybytes; ++i) {
                        byte inData1 = inData[ibyte + 1];
                        int val = inData0 << lshift | (inData1 & 0xFF) >> rshift;
                        outData[obyte] = (byte)val;
                        inData0 = inData1;
                        ++ibyte;
                        ++obyte;
                    }
                }
                int bits = copybytes * 8;
                inbit += bits;
                outbit += bits;
                copybits -= bits;
            }
            if (copybits > 0) {
                inbyte = inbit >> 3;
                outbyte = outbit >> 3;
                int mask = 65280 >> copybits & 0xFF;
                lshift = inbit & 7;
                rshift = 8 - lshift;
                int lastByte = inData.length - 1;
                for (int j = 0; j < height; ++j) {
                    byte inData02 = inData[inbyte];
                    byte inData1 = 0;
                    if (inbyte < lastByte) {
                        inData1 = inData[inbyte + 1];
                    }
                    int element = outData[outbyte];
                    element &= ~mask;
                    outData[outbyte] = (byte)(element |= (inData02 << lshift | (inData1 & 0xFF) >> rshift) & mask);
                    inbyte += inscan;
                    outbyte += outscan;
                }
            }
        }
        this.markDirty();
    }

    @Override
    public void setRect(int dx, int dy, Raster srcRaster) {
        if (!(srcRaster instanceof BytePackedRaster) || ((BytePackedRaster)srcRaster).pixelBitStride != this.pixelBitStride) {
            super.setRect(dx, dy, srcRaster);
            return;
        }
        int width = srcRaster.getWidth();
        int height = srcRaster.getHeight();
        int srcOffX = srcRaster.getMinX();
        int srcOffY = srcRaster.getMinY();
        int dstOffX = dx + srcOffX;
        int dstOffY = dy + srcOffY;
        if (dstOffX < this.minX) {
            int skipX = this.minX - dstOffX;
            width -= skipX;
            srcOffX += skipX;
            dstOffX = this.minX;
        }
        if (dstOffY < this.minY) {
            int skipY = this.minY - dstOffY;
            height -= skipY;
            srcOffY += skipY;
            dstOffY = this.minY;
        }
        if (dstOffX + width > this.maxX) {
            width = this.maxX - dstOffX;
        }
        if (dstOffY + height > this.maxY) {
            height = this.maxY - dstOffY;
        }
        this.setDataElements(dstOffX, dstOffY, srcOffX, srcOffY, width, height, (BytePackedRaster)srcRaster);
    }

    @Override
    public void setDataElements(int x, int y, int w, int h, Object obj) {
        this.putByteData(x, y, w, h, (byte[])obj);
    }

    public void putByteData(int x, int y, int w, int h, int band, byte[] inData) {
        this.putByteData(x, y, w, h, inData);
    }

    public void putByteData(int x, int y, int w, int h, byte[] inData) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        if (w == 0 || h == 0) {
            return;
        }
        int pixbits = this.pixelBitStride;
        int scanbit = this.dataBitOffset + (x - this.minX) * pixbits;
        int index = (y - this.minY) * this.scanlineStride;
        int outindex = 0;
        byte[] data = this.data;
        for (int j = 0; j < h; ++j) {
            int element;
            int i;
            int bitnum = scanbit;
            for (i = 0; i < w && (bitnum & 7) != 0; ++i) {
                int shift = this.shiftOffset - (bitnum & 7);
                element = data[index + (bitnum >> 3)];
                element &= ~(this.bitMask << shift);
                data[index + (bitnum >> 3)] = (byte)(element |= (inData[outindex++] & this.bitMask) << shift);
                bitnum += pixbits;
            }
            int inIndex = index + (bitnum >> 3);
            switch (pixbits) {
                case 1: {
                    while (i < w - 7) {
                        element = (inData[outindex++] & 1) << 7;
                        element |= (inData[outindex++] & 1) << 6;
                        element |= (inData[outindex++] & 1) << 5;
                        element |= (inData[outindex++] & 1) << 4;
                        element |= (inData[outindex++] & 1) << 3;
                        element |= (inData[outindex++] & 1) << 2;
                        element |= (inData[outindex++] & 1) << 1;
                        data[inIndex++] = (byte)(element |= inData[outindex++] & 1);
                        bitnum += 8;
                        i += 8;
                    }
                    break;
                }
                case 2: {
                    while (i < w - 7) {
                        element = (inData[outindex++] & 3) << 6;
                        element |= (inData[outindex++] & 3) << 4;
                        element |= (inData[outindex++] & 3) << 2;
                        data[inIndex++] = (byte)(element |= inData[outindex++] & 3);
                        element = (inData[outindex++] & 3) << 6;
                        element |= (inData[outindex++] & 3) << 4;
                        element |= (inData[outindex++] & 3) << 2;
                        data[inIndex++] = (byte)(element |= inData[outindex++] & 3);
                        bitnum += 16;
                        i += 8;
                    }
                    break;
                }
                case 4: {
                    while (i < w - 7) {
                        element = (inData[outindex++] & 0xF) << 4;
                        data[inIndex++] = (byte)(element |= inData[outindex++] & 0xF);
                        element = (inData[outindex++] & 0xF) << 4;
                        data[inIndex++] = (byte)(element |= inData[outindex++] & 0xF);
                        element = (inData[outindex++] & 0xF) << 4;
                        data[inIndex++] = (byte)(element |= inData[outindex++] & 0xF);
                        element = (inData[outindex++] & 0xF) << 4;
                        data[inIndex++] = (byte)(element |= inData[outindex++] & 0xF);
                        bitnum += 32;
                        i += 8;
                    }
                    break;
                }
            }
            while (i < w) {
                int shift = this.shiftOffset - (bitnum & 7);
                element = data[index + (bitnum >> 3)];
                element &= ~(this.bitMask << shift);
                data[index + (bitnum >> 3)] = (byte)(element |= (inData[outindex++] & this.bitMask) << shift);
                bitnum += pixbits;
                ++i;
            }
            index += this.scanlineStride;
        }
        this.markDirty();
    }

    @Override
    public int[] getPixels(int x, int y, int w, int h, int[] iArray) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        if (iArray == null) {
            iArray = new int[w * h];
        }
        int pixbits = this.pixelBitStride;
        int scanbit = this.dataBitOffset + (x - this.minX) * pixbits;
        int index = (y - this.minY) * this.scanlineStride;
        int outindex = 0;
        byte[] data = this.data;
        for (int j = 0; j < h; ++j) {
            int i;
            int bitnum = scanbit;
            for (i = 0; i < w && (bitnum & 7) != 0; ++i) {
                int shift = this.shiftOffset - (bitnum & 7);
                iArray[outindex++] = this.bitMask & data[index + (bitnum >> 3)] >> shift;
                bitnum += pixbits;
            }
            int inIndex = index + (bitnum >> 3);
            switch (pixbits) {
                case 1: {
                    byte element;
                    while (i < w - 7) {
                        element = data[inIndex++];
                        iArray[outindex++] = element >> 7 & 1;
                        iArray[outindex++] = element >> 6 & 1;
                        iArray[outindex++] = element >> 5 & 1;
                        iArray[outindex++] = element >> 4 & 1;
                        iArray[outindex++] = element >> 3 & 1;
                        iArray[outindex++] = element >> 2 & 1;
                        iArray[outindex++] = element >> 1 & 1;
                        iArray[outindex++] = element & 1;
                        bitnum += 8;
                        i += 8;
                    }
                    break;
                }
                case 2: {
                    byte element;
                    while (i < w - 7) {
                        element = data[inIndex++];
                        iArray[outindex++] = element >> 6 & 3;
                        iArray[outindex++] = element >> 4 & 3;
                        iArray[outindex++] = element >> 2 & 3;
                        iArray[outindex++] = element & 3;
                        element = data[inIndex++];
                        iArray[outindex++] = element >> 6 & 3;
                        iArray[outindex++] = element >> 4 & 3;
                        iArray[outindex++] = element >> 2 & 3;
                        iArray[outindex++] = element & 3;
                        bitnum += 16;
                        i += 8;
                    }
                    break;
                }
                case 4: {
                    byte element;
                    while (i < w - 7) {
                        element = data[inIndex++];
                        iArray[outindex++] = element >> 4 & 0xF;
                        iArray[outindex++] = element & 0xF;
                        element = data[inIndex++];
                        iArray[outindex++] = element >> 4 & 0xF;
                        iArray[outindex++] = element & 0xF;
                        element = data[inIndex++];
                        iArray[outindex++] = element >> 4 & 0xF;
                        iArray[outindex++] = element & 0xF;
                        element = data[inIndex++];
                        iArray[outindex++] = element >> 4 & 0xF;
                        iArray[outindex++] = element & 0xF;
                        bitnum += 32;
                        i += 8;
                    }
                    break;
                }
            }
            while (i < w) {
                int shift = this.shiftOffset - (bitnum & 7);
                iArray[outindex++] = this.bitMask & data[index + (bitnum >> 3)] >> shift;
                bitnum += pixbits;
                ++i;
            }
            index += this.scanlineStride;
        }
        return iArray;
    }

    @Override
    public void setPixels(int x, int y, int w, int h, int[] iArray) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int pixbits = this.pixelBitStride;
        int scanbit = this.dataBitOffset + (x - this.minX) * pixbits;
        int index = (y - this.minY) * this.scanlineStride;
        int outindex = 0;
        byte[] data = this.data;
        for (int j = 0; j < h; ++j) {
            int element;
            int i;
            int bitnum = scanbit;
            for (i = 0; i < w && (bitnum & 7) != 0; ++i) {
                int shift = this.shiftOffset - (bitnum & 7);
                element = data[index + (bitnum >> 3)];
                element &= ~(this.bitMask << shift);
                data[index + (bitnum >> 3)] = (byte)(element |= (iArray[outindex++] & this.bitMask) << shift);
                bitnum += pixbits;
            }
            int inIndex = index + (bitnum >> 3);
            switch (pixbits) {
                case 1: {
                    while (i < w - 7) {
                        element = (iArray[outindex++] & 1) << 7;
                        element |= (iArray[outindex++] & 1) << 6;
                        element |= (iArray[outindex++] & 1) << 5;
                        element |= (iArray[outindex++] & 1) << 4;
                        element |= (iArray[outindex++] & 1) << 3;
                        element |= (iArray[outindex++] & 1) << 2;
                        element |= (iArray[outindex++] & 1) << 1;
                        data[inIndex++] = (byte)(element |= iArray[outindex++] & 1);
                        bitnum += 8;
                        i += 8;
                    }
                    break;
                }
                case 2: {
                    while (i < w - 7) {
                        element = (iArray[outindex++] & 3) << 6;
                        element |= (iArray[outindex++] & 3) << 4;
                        element |= (iArray[outindex++] & 3) << 2;
                        data[inIndex++] = (byte)(element |= iArray[outindex++] & 3);
                        element = (iArray[outindex++] & 3) << 6;
                        element |= (iArray[outindex++] & 3) << 4;
                        element |= (iArray[outindex++] & 3) << 2;
                        data[inIndex++] = (byte)(element |= iArray[outindex++] & 3);
                        bitnum += 16;
                        i += 8;
                    }
                    break;
                }
                case 4: {
                    while (i < w - 7) {
                        element = (iArray[outindex++] & 0xF) << 4;
                        data[inIndex++] = (byte)(element |= iArray[outindex++] & 0xF);
                        element = (iArray[outindex++] & 0xF) << 4;
                        data[inIndex++] = (byte)(element |= iArray[outindex++] & 0xF);
                        element = (iArray[outindex++] & 0xF) << 4;
                        data[inIndex++] = (byte)(element |= iArray[outindex++] & 0xF);
                        element = (iArray[outindex++] & 0xF) << 4;
                        data[inIndex++] = (byte)(element |= iArray[outindex++] & 0xF);
                        bitnum += 32;
                        i += 8;
                    }
                    break;
                }
            }
            while (i < w) {
                int shift = this.shiftOffset - (bitnum & 7);
                element = data[index + (bitnum >> 3)];
                element &= ~(this.bitMask << shift);
                data[index + (bitnum >> 3)] = (byte)(element |= (iArray[outindex++] & this.bitMask) << shift);
                bitnum += pixbits;
                ++i;
            }
            index += this.scanlineStride;
        }
        this.markDirty();
    }

    @Override
    public Raster createChild(int x, int y, int width, int height, int x0, int y0, int[] bandList) {
        WritableRaster newRaster = this.createWritableChild(x, y, width, height, x0, y0, bandList);
        return newRaster;
    }

    @Override
    public WritableRaster createWritableChild(int x, int y, int width, int height, int x0, int y0, int[] bandList) {
        if (x < this.minX) {
            throw new RasterFormatException("x lies outside the raster");
        }
        if (y < this.minY) {
            throw new RasterFormatException("y lies outside the raster");
        }
        if (x + width < x || x + width > this.minX + this.width) {
            throw new RasterFormatException("(x + width) is outside of Raster");
        }
        if (y + height < y || y + height > this.minY + this.height) {
            throw new RasterFormatException("(y + height) is outside of Raster");
        }
        SampleModel sm = bandList != null ? this.sampleModel.createSubsetSampleModel(bandList) : this.sampleModel;
        int deltaX = x0 - x;
        int deltaY = y0 - y;
        return new BytePackedRaster(sm, (DataBufferByte)this.dataBuffer, new Rectangle(x0, y0, width, height), new Point(this.sampleModelTranslateX + deltaX, this.sampleModelTranslateY + deltaY), this);
    }

    @Override
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException("negative " + (w <= 0 ? "width" : "height"));
        }
        SampleModel sm = this.sampleModel.createCompatibleSampleModel(w, h);
        return new BytePackedRaster(sm, new Point(0, 0));
    }

    @Override
    public WritableRaster createCompatibleWritableRaster() {
        return this.createCompatibleWritableRaster(this.width, this.height);
    }

    private void verify(boolean strictCheck) {
        if (this.dataBitOffset < 0) {
            throw new RasterFormatException("Data offsets must be >= 0");
        }
        if (this.width <= 0 || this.height <= 0 || this.height > Integer.MAX_VALUE / this.width) {
            throw new RasterFormatException("Invalid raster dimension");
        }
        if (this.width - 1 > Integer.MAX_VALUE / this.pixelBitStride) {
            throw new RasterFormatException("Invalid raster dimension");
        }
        if ((long)this.minX - (long)this.sampleModelTranslateX < 0L || (long)this.minY - (long)this.sampleModelTranslateY < 0L) {
            throw new RasterFormatException("Incorrect origin/translate: (" + this.minX + ", " + this.minY + ") / (" + this.sampleModelTranslateX + ", " + this.sampleModelTranslateY + ")");
        }
        if (this.scanlineStride < 0 || this.scanlineStride > Integer.MAX_VALUE / this.height) {
            throw new RasterFormatException("Invalid scanline stride");
        }
        if ((this.height > 1 || this.minY - this.sampleModelTranslateY > 0) && this.scanlineStride > this.data.length) {
            throw new RasterFormatException("Incorrect scanline stride: " + this.scanlineStride);
        }
        long lastbit = (long)this.dataBitOffset + (long)(this.height - 1) * (long)this.scanlineStride * 8L + (long)(this.width - 1) * (long)this.pixelBitStride + (long)this.pixelBitStride - 1L;
        if (lastbit < 0L || lastbit / 8L >= (long)this.data.length) {
            throw new RasterFormatException("raster dimensions overflow array bounds");
        }
        if (strictCheck && this.height > 1 && (lastbit = (long)(this.width * this.pixelBitStride - 1)) / 8L >= (long)this.scanlineStride) {
            throw new RasterFormatException("data for adjacent scanlines overlaps");
        }
    }

    public String toString() {
        return "BytePackedRaster: width = " + this.width + " height = " + this.height + " #channels " + this.numBands + " xOff = " + this.sampleModelTranslateX + " yOff = " + this.sampleModelTranslateY;
    }

    static {
        NativeLibLoader.loadLibraries();
        BytePackedRaster.initIDs();
    }
}

