/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;

public class MultiPixelPackedSampleModel
extends SampleModel {
    int pixelBitStride;
    int bitMask;
    int pixelsPerDataElement;
    int dataElementSize;
    int dataBitOffset;
    int scanlineStride;

    public MultiPixelPackedSampleModel(int dataType, int w, int h, int numberOfBits) {
        this(dataType, w, h, numberOfBits, (w * numberOfBits + DataBuffer.getDataTypeSize(dataType) - 1) / DataBuffer.getDataTypeSize(dataType), 0);
        if (dataType != 0 && dataType != 1 && dataType != 3) {
            throw new IllegalArgumentException("Unsupported data type " + dataType);
        }
    }

    public MultiPixelPackedSampleModel(int dataType, int w, int h, int numberOfBits, int scanlineStride, int dataBitOffset) {
        super(dataType, w, h, 1);
        if (dataType != 0 && dataType != 1 && dataType != 3) {
            throw new IllegalArgumentException("Unsupported data type " + dataType);
        }
        this.dataType = dataType;
        this.pixelBitStride = numberOfBits;
        this.scanlineStride = scanlineStride;
        this.dataBitOffset = dataBitOffset;
        this.dataElementSize = DataBuffer.getDataTypeSize(dataType);
        this.pixelsPerDataElement = this.dataElementSize / numberOfBits;
        if (this.pixelsPerDataElement * numberOfBits != this.dataElementSize) {
            throw new RasterFormatException("MultiPixelPackedSampleModel does not allow pixels to span data element boundaries");
        }
        this.bitMask = (1 << numberOfBits) - 1;
    }

    @Override
    public SampleModel createCompatibleSampleModel(int w, int h) {
        MultiPixelPackedSampleModel sampleModel = new MultiPixelPackedSampleModel(this.dataType, w, h, this.pixelBitStride);
        return sampleModel;
    }

    @Override
    public DataBuffer createDataBuffer() {
        DataBuffer dataBuffer = null;
        int size = this.scanlineStride * this.height;
        switch (this.dataType) {
            case 0: {
                dataBuffer = new DataBufferByte(size + (this.dataBitOffset + 7) / 8);
                break;
            }
            case 1: {
                dataBuffer = new DataBufferUShort(size + (this.dataBitOffset + 15) / 16);
                break;
            }
            case 3: {
                dataBuffer = new DataBufferInt(size + (this.dataBitOffset + 31) / 32);
            }
        }
        return dataBuffer;
    }

    @Override
    public int getNumDataElements() {
        return 1;
    }

    @Override
    public int[] getSampleSize() {
        int[] sampleSize = new int[]{this.pixelBitStride};
        return sampleSize;
    }

    @Override
    public int getSampleSize(int band) {
        return this.pixelBitStride;
    }

    public int getOffset(int x, int y) {
        int offset = y * this.scanlineStride;
        return offset += (x * this.pixelBitStride + this.dataBitOffset) / this.dataElementSize;
    }

    public int getBitOffset(int x) {
        return (x * this.pixelBitStride + this.dataBitOffset) % this.dataElementSize;
    }

    public int getScanlineStride() {
        return this.scanlineStride;
    }

    public int getPixelBitStride() {
        return this.pixelBitStride;
    }

    public int getDataBitOffset() {
        return this.dataBitOffset;
    }

    @Override
    public int getTransferType() {
        if (this.pixelBitStride > 16) {
            return 3;
        }
        if (this.pixelBitStride > 8) {
            return 1;
        }
        return 0;
    }

    @Override
    public SampleModel createSubsetSampleModel(int[] bands) {
        if (bands != null && bands.length != 1) {
            throw new RasterFormatException("MultiPixelPackedSampleModel has only one band.");
        }
        SampleModel sm = this.createCompatibleSampleModel(this.width, this.height);
        return sm;
    }

    @Override
    public int getSample(int x, int y, int b, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height || b != 0) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int bitnum = this.dataBitOffset + x * this.pixelBitStride;
        int element = data.getElem(y * this.scanlineStride + bitnum / this.dataElementSize);
        int shift = this.dataElementSize - (bitnum & this.dataElementSize - 1) - this.pixelBitStride;
        return element >> shift & this.bitMask;
    }

    @Override
    public void setSample(int x, int y, int b, int s, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height || b != 0) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int bitnum = this.dataBitOffset + x * this.pixelBitStride;
        int index = y * this.scanlineStride + bitnum / this.dataElementSize;
        int shift = this.dataElementSize - (bitnum & this.dataElementSize - 1) - this.pixelBitStride;
        int element = data.getElem(index);
        element &= ~(this.bitMask << shift);
        data.setElem(index, element |= (s & this.bitMask) << shift);
    }

    @Override
    public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int type = this.getTransferType();
        int bitnum = this.dataBitOffset + x * this.pixelBitStride;
        int shift = this.dataElementSize - (bitnum & this.dataElementSize - 1) - this.pixelBitStride;
        int element = 0;
        switch (type) {
            case 0: {
                byte[] bdata = obj == null ? new byte[1] : (byte[])obj;
                element = data.getElem(y * this.scanlineStride + bitnum / this.dataElementSize);
                bdata[0] = (byte)(element >> shift & this.bitMask);
                obj = bdata;
                break;
            }
            case 1: {
                short[] sdata = obj == null ? new short[1] : (short[])obj;
                element = data.getElem(y * this.scanlineStride + bitnum / this.dataElementSize);
                sdata[0] = (short)(element >> shift & this.bitMask);
                obj = sdata;
                break;
            }
            case 3: {
                int[] idata = obj == null ? new int[1] : (int[])obj;
                element = data.getElem(y * this.scanlineStride + bitnum / this.dataElementSize);
                idata[0] = element >> shift & this.bitMask;
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
        int[] pixels = iArray != null ? iArray : new int[this.numBands];
        int bitnum = this.dataBitOffset + x * this.pixelBitStride;
        int element = data.getElem(y * this.scanlineStride + bitnum / this.dataElementSize);
        int shift = this.dataElementSize - (bitnum & this.dataElementSize - 1) - this.pixelBitStride;
        pixels[0] = element >> shift & this.bitMask;
        return pixels;
    }

    @Override
    public void setDataElements(int x, int y, Object obj, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int type = this.getTransferType();
        int bitnum = this.dataBitOffset + x * this.pixelBitStride;
        int index = y * this.scanlineStride + bitnum / this.dataElementSize;
        int shift = this.dataElementSize - (bitnum & this.dataElementSize - 1) - this.pixelBitStride;
        int element = data.getElem(index);
        element &= ~(this.bitMask << shift);
        switch (type) {
            case 0: {
                byte[] barray = (byte[])obj;
                data.setElem(index, element |= (barray[0] & 0xFF & this.bitMask) << shift);
                break;
            }
            case 1: {
                short[] sarray = (short[])obj;
                data.setElem(index, element |= (sarray[0] & 0xFFFF & this.bitMask) << shift);
                break;
            }
            case 3: {
                int[] iarray = (int[])obj;
                data.setElem(index, element |= (iarray[0] & this.bitMask) << shift);
            }
        }
    }

    @Override
    public void setPixel(int x, int y, int[] iArray, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int bitnum = this.dataBitOffset + x * this.pixelBitStride;
        int index = y * this.scanlineStride + bitnum / this.dataElementSize;
        int shift = this.dataElementSize - (bitnum & this.dataElementSize - 1) - this.pixelBitStride;
        int element = data.getElem(index);
        element &= ~(this.bitMask << shift);
        data.setElem(index, element |= (iArray[0] & this.bitMask) << shift);
    }

    public boolean equals(Object o) {
        if (!(o instanceof MultiPixelPackedSampleModel)) {
            return false;
        }
        MultiPixelPackedSampleModel that = (MultiPixelPackedSampleModel)o;
        return this.width == that.width && this.height == that.height && this.numBands == that.numBands && this.dataType == that.dataType && this.pixelBitStride == that.pixelBitStride && this.bitMask == that.bitMask && this.pixelsPerDataElement == that.pixelsPerDataElement && this.dataElementSize == that.dataElementSize && this.dataBitOffset == that.dataBitOffset && this.scanlineStride == that.scanlineStride;
    }

    public int hashCode() {
        int hash = 0;
        hash = this.width;
        hash <<= 8;
        hash ^= this.height;
        hash <<= 8;
        hash ^= this.numBands;
        hash <<= 8;
        hash ^= this.dataType;
        hash <<= 8;
        hash ^= this.pixelBitStride;
        hash <<= 8;
        hash ^= this.bitMask;
        hash <<= 8;
        hash ^= this.pixelsPerDataElement;
        hash <<= 8;
        hash ^= this.dataElementSize;
        hash <<= 8;
        hash ^= this.dataBitOffset;
        hash <<= 8;
        return hash ^= this.scanlineStride;
    }
}

