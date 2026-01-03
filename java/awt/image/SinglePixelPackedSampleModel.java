/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.util.Arrays;

public class SinglePixelPackedSampleModel
extends SampleModel {
    private int[] bitMasks;
    private int[] bitOffsets;
    private int[] bitSizes;
    private int maxBitSize;
    private int scanlineStride;

    private static native void initIDs();

    public SinglePixelPackedSampleModel(int dataType, int w, int h, int[] bitMasks) {
        this(dataType, w, h, w, bitMasks);
        if (dataType != 0 && dataType != 1 && dataType != 3) {
            throw new IllegalArgumentException("Unsupported data type " + dataType);
        }
    }

    public SinglePixelPackedSampleModel(int dataType, int w, int h, int scanlineStride, int[] bitMasks) {
        super(dataType, w, h, bitMasks.length);
        if (dataType != 0 && dataType != 1 && dataType != 3) {
            throw new IllegalArgumentException("Unsupported data type " + dataType);
        }
        this.dataType = dataType;
        this.bitMasks = (int[])bitMasks.clone();
        this.scanlineStride = scanlineStride;
        this.bitOffsets = new int[this.numBands];
        this.bitSizes = new int[this.numBands];
        int maxMask = (int)((1L << DataBuffer.getDataTypeSize(dataType)) - 1L);
        this.maxBitSize = 0;
        for (int i = 0; i < this.numBands; ++i) {
            int bitOffset = 0;
            int bitSize = 0;
            int n = i;
            this.bitMasks[n] = this.bitMasks[n] & maxMask;
            int mask = this.bitMasks[i];
            if (mask != 0) {
                while ((mask & 1) == 0) {
                    mask >>>= 1;
                    ++bitOffset;
                }
                while ((mask & 1) == 1) {
                    mask >>>= 1;
                    ++bitSize;
                }
                if (mask != 0) {
                    throw new IllegalArgumentException("Mask " + bitMasks[i] + " must be contiguous");
                }
            }
            this.bitOffsets[i] = bitOffset;
            this.bitSizes[i] = bitSize;
            if (bitSize <= this.maxBitSize) continue;
            this.maxBitSize = bitSize;
        }
    }

    @Override
    public int getNumDataElements() {
        return 1;
    }

    private long getBufferSize() {
        long size = this.scanlineStride * (this.height - 1) + this.width;
        return size;
    }

    @Override
    public SampleModel createCompatibleSampleModel(int w, int h) {
        SinglePixelPackedSampleModel sampleModel = new SinglePixelPackedSampleModel(this.dataType, w, h, this.bitMasks);
        return sampleModel;
    }

    @Override
    public DataBuffer createDataBuffer() {
        DataBuffer dataBuffer = null;
        int size = (int)this.getBufferSize();
        switch (this.dataType) {
            case 0: {
                dataBuffer = new DataBufferByte(size);
                break;
            }
            case 1: {
                dataBuffer = new DataBufferUShort(size);
                break;
            }
            case 3: {
                dataBuffer = new DataBufferInt(size);
            }
        }
        return dataBuffer;
    }

    @Override
    public int[] getSampleSize() {
        return (int[])this.bitSizes.clone();
    }

    @Override
    public int getSampleSize(int band) {
        return this.bitSizes[band];
    }

    public int getOffset(int x, int y) {
        int offset = y * this.scanlineStride + x;
        return offset;
    }

    public int[] getBitOffsets() {
        return (int[])this.bitOffsets.clone();
    }

    public int[] getBitMasks() {
        return (int[])this.bitMasks.clone();
    }

    public int getScanlineStride() {
        return this.scanlineStride;
    }

    @Override
    public SampleModel createSubsetSampleModel(int[] bands) {
        if (bands.length > this.numBands) {
            throw new RasterFormatException("There are only " + this.numBands + " bands");
        }
        int[] newBitMasks = new int[bands.length];
        for (int i = 0; i < bands.length; ++i) {
            newBitMasks[i] = this.bitMasks[bands[i]];
        }
        return new SinglePixelPackedSampleModel(this.dataType, this.width, this.height, this.scanlineStride, newBitMasks);
    }

    @Override
    public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int type = this.getTransferType();
        switch (type) {
            case 0: {
                byte[] bdata = obj == null ? new byte[1] : (byte[])obj;
                bdata[0] = (byte)data.getElem(y * this.scanlineStride + x);
                obj = bdata;
                break;
            }
            case 1: {
                short[] sdata = obj == null ? new short[1] : (short[])obj;
                sdata[0] = (short)data.getElem(y * this.scanlineStride + x);
                obj = sdata;
                break;
            }
            case 3: {
                int[] idata = obj == null ? new int[1] : (int[])obj;
                idata[0] = data.getElem(y * this.scanlineStride + x);
                obj = idata;
            }
        }
        return obj;
    }

    @Override
    public int[] getPixel(int x, int y, int[] iArray, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int[] pixels = iArray == null ? new int[this.numBands] : iArray;
        int value = data.getElem(y * this.scanlineStride + x);
        for (int i = 0; i < this.numBands; ++i) {
            pixels[i] = (value & this.bitMasks[i]) >>> this.bitOffsets[i];
        }
        return pixels;
    }

    @Override
    public int[] getPixels(int x, int y, int w, int h, int[] iArray, DataBuffer data) {
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= this.width || w > this.width || x1 < 0 || x1 > this.width || y < 0 || y >= this.height || h > this.height || y1 < 0 || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int[] pixels = iArray != null ? iArray : new int[w * h * this.numBands];
        int lineOffset = y * this.scanlineStride + x;
        int dstOffset = 0;
        for (int i = 0; i < h; ++i) {
            for (int j = 0; j < w; ++j) {
                int value = data.getElem(lineOffset + j);
                for (int k = 0; k < this.numBands; ++k) {
                    pixels[dstOffset++] = (value & this.bitMasks[k]) >>> this.bitOffsets[k];
                }
            }
            lineOffset += this.scanlineStride;
        }
        return pixels;
    }

    @Override
    public int getSample(int x, int y, int b, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int sample = data.getElem(y * this.scanlineStride + x);
        return (sample & this.bitMasks[b]) >>> this.bitOffsets[b];
    }

    @Override
    public int[] getSamples(int x, int y, int w, int h, int b, int[] iArray, DataBuffer data) {
        if (x < 0 || y < 0 || x + w > this.width || y + h > this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int[] samples = iArray != null ? iArray : new int[w * h];
        int lineOffset = y * this.scanlineStride + x;
        int dstOffset = 0;
        for (int i = 0; i < h; ++i) {
            for (int j = 0; j < w; ++j) {
                int value = data.getElem(lineOffset + j);
                samples[dstOffset++] = (value & this.bitMasks[b]) >>> this.bitOffsets[b];
            }
            lineOffset += this.scanlineStride;
        }
        return samples;
    }

    @Override
    public void setDataElements(int x, int y, Object obj, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int type = this.getTransferType();
        switch (type) {
            case 0: {
                byte[] barray = (byte[])obj;
                data.setElem(y * this.scanlineStride + x, barray[0] & 0xFF);
                break;
            }
            case 1: {
                short[] sarray = (short[])obj;
                data.setElem(y * this.scanlineStride + x, sarray[0] & 0xFFFF);
                break;
            }
            case 3: {
                int[] iarray = (int[])obj;
                data.setElem(y * this.scanlineStride + x, iarray[0]);
            }
        }
    }

    @Override
    public void setPixel(int x, int y, int[] iArray, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int lineOffset = y * this.scanlineStride + x;
        int value = data.getElem(lineOffset);
        for (int i = 0; i < this.numBands; ++i) {
            value &= ~this.bitMasks[i];
            value |= iArray[i] << this.bitOffsets[i] & this.bitMasks[i];
        }
        data.setElem(lineOffset, value);
    }

    @Override
    public void setPixels(int x, int y, int w, int h, int[] iArray, DataBuffer data) {
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= this.width || w > this.width || x1 < 0 || x1 > this.width || y < 0 || y >= this.height || h > this.height || y1 < 0 || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int lineOffset = y * this.scanlineStride + x;
        int srcOffset = 0;
        for (int i = 0; i < h; ++i) {
            for (int j = 0; j < w; ++j) {
                int value = data.getElem(lineOffset + j);
                for (int k = 0; k < this.numBands; ++k) {
                    value &= ~this.bitMasks[k];
                    int srcValue = iArray[srcOffset++];
                    value |= srcValue << this.bitOffsets[k] & this.bitMasks[k];
                }
                data.setElem(lineOffset + j, value);
            }
            lineOffset += this.scanlineStride;
        }
    }

    @Override
    public void setSample(int x, int y, int b, int s, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int value = data.getElem(y * this.scanlineStride + x);
        value &= ~this.bitMasks[b];
        data.setElem(y * this.scanlineStride + x, value |= s << this.bitOffsets[b] & this.bitMasks[b]);
    }

    @Override
    public void setSamples(int x, int y, int w, int h, int b, int[] iArray, DataBuffer data) {
        if (x < 0 || y < 0 || x + w > this.width || y + h > this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int lineOffset = y * this.scanlineStride + x;
        int srcOffset = 0;
        for (int i = 0; i < h; ++i) {
            for (int j = 0; j < w; ++j) {
                int value = data.getElem(lineOffset + j);
                value &= ~this.bitMasks[b];
                int sample = iArray[srcOffset++];
                data.setElem(lineOffset + j, value |= sample << this.bitOffsets[b] & this.bitMasks[b]);
            }
            lineOffset += this.scanlineStride;
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof SinglePixelPackedSampleModel)) {
            return false;
        }
        SinglePixelPackedSampleModel that = (SinglePixelPackedSampleModel)o;
        return this.width == that.width && this.height == that.height && this.numBands == that.numBands && this.dataType == that.dataType && Arrays.equals(this.bitMasks, that.bitMasks) && Arrays.equals(this.bitOffsets, that.bitOffsets) && Arrays.equals(this.bitSizes, that.bitSizes) && this.maxBitSize == that.maxBitSize && this.scanlineStride == that.scanlineStride;
    }

    public int hashCode() {
        int i;
        int hash = 0;
        hash = this.width;
        hash <<= 8;
        hash ^= this.height;
        hash <<= 8;
        hash ^= this.numBands;
        hash <<= 8;
        hash ^= this.dataType;
        hash <<= 8;
        for (i = 0; i < this.bitMasks.length; ++i) {
            hash ^= this.bitMasks[i];
            hash <<= 8;
        }
        for (i = 0; i < this.bitOffsets.length; ++i) {
            hash ^= this.bitOffsets[i];
            hash <<= 8;
        }
        for (i = 0; i < this.bitSizes.length; ++i) {
            hash ^= this.bitSizes[i];
            hash <<= 8;
        }
        hash ^= this.maxBitSize;
        hash <<= 8;
        return hash ^= this.scanlineStride;
    }

    static {
        ColorModel.loadLibraries();
        SinglePixelPackedSampleModel.initIDs();
    }
}

