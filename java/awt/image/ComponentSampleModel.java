/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.util.Arrays;

public class ComponentSampleModel
extends SampleModel {
    protected int[] bandOffsets;
    protected int[] bankIndices;
    protected int numBands = 1;
    protected int numBanks = 1;
    protected int scanlineStride;
    protected int pixelStride;

    public ComponentSampleModel(int dataType, int w, int h, int pixelStride, int scanlineStride, int[] bandOffsets) {
        super(dataType, w, h, bandOffsets.length);
        this.dataType = dataType;
        this.pixelStride = pixelStride;
        this.scanlineStride = scanlineStride;
        this.bandOffsets = (int[])bandOffsets.clone();
        this.numBands = this.bandOffsets.length;
        if (pixelStride < 0) {
            throw new IllegalArgumentException("Pixel stride must be >= 0");
        }
        if (scanlineStride < 0) {
            throw new IllegalArgumentException("Scanline stride must be >= 0");
        }
        if (dataType < 0 || dataType > 5) {
            throw new IllegalArgumentException("Unsupported dataType.");
        }
        this.bankIndices = new int[this.numBands];
        this.verify();
    }

    public ComponentSampleModel(int dataType, int w, int h, int pixelStride, int scanlineStride, int[] bankIndices, int[] bandOffsets) {
        super(dataType, w, h, bandOffsets.length);
        this.dataType = dataType;
        this.pixelStride = pixelStride;
        this.scanlineStride = scanlineStride;
        this.bandOffsets = (int[])bandOffsets.clone();
        this.bankIndices = (int[])bankIndices.clone();
        if (this.bandOffsets.length != this.bankIndices.length) {
            throw new IllegalArgumentException("Length of bandOffsets must equal length of bankIndices.");
        }
        if (pixelStride < 0) {
            throw new IllegalArgumentException("Pixel stride must be >= 0");
        }
        if (scanlineStride < 0) {
            throw new IllegalArgumentException("Scanline stride must be >= 0");
        }
        if (dataType < 0 || dataType > 5) {
            throw new IllegalArgumentException("Unsupported dataType.");
        }
        int maxBank = this.bankIndices[0];
        if (maxBank < 0) {
            throw new IllegalArgumentException("Index of bank 0 is less than 0 (" + maxBank + ")");
        }
        for (int i = 1; i < this.bankIndices.length; ++i) {
            if (this.bankIndices[i] > maxBank) {
                maxBank = this.bankIndices[i];
                continue;
            }
            if (this.bankIndices[i] >= 0) continue;
            throw new IllegalArgumentException("Index of bank " + i + " is less than 0 (" + maxBank + ")");
        }
        this.numBanks = maxBank + 1;
        this.numBands = this.bandOffsets.length;
        this.verify();
    }

    private void verify() {
        int requiredSize = this.getBufferSize();
    }

    private int getBufferSize() {
        int maxBandOff = this.bandOffsets[0];
        for (int i = 1; i < this.bandOffsets.length; ++i) {
            maxBandOff = Math.max(maxBandOff, this.bandOffsets[i]);
        }
        if (maxBandOff < 0 || maxBandOff > 0x7FFFFFFE) {
            throw new IllegalArgumentException("Invalid band offset");
        }
        if (this.pixelStride < 0 || this.pixelStride > Integer.MAX_VALUE / this.width) {
            throw new IllegalArgumentException("Invalid pixel stride");
        }
        if (this.scanlineStride < 0 || this.scanlineStride > Integer.MAX_VALUE / this.height) {
            throw new IllegalArgumentException("Invalid scanline stride");
        }
        int val = this.pixelStride * (this.width - 1);
        int size = maxBandOff + 1;
        if (val > Integer.MAX_VALUE - size) {
            throw new IllegalArgumentException("Invalid pixel stride");
        }
        val = this.scanlineStride * (this.height - 1);
        if (val > Integer.MAX_VALUE - (size += val)) {
            throw new IllegalArgumentException("Invalid scan stride");
        }
        return size += val;
    }

    int[] orderBands(int[] orig, int step) {
        int i;
        int[] map = new int[orig.length];
        int[] ret = new int[orig.length];
        for (i = 0; i < map.length; ++i) {
            map[i] = i;
        }
        for (i = 0; i < ret.length; ++i) {
            int index = i;
            for (int j = i + 1; j < ret.length; ++j) {
                if (orig[map[index]] <= orig[map[j]]) continue;
                index = j;
            }
            ret[map[index]] = i * step;
            map[index] = map[i];
        }
        return ret;
    }

    @Override
    public SampleModel createCompatibleSampleModel(int w, int h) {
        int[] bandOff;
        Object ret = null;
        int minBandOff = this.bandOffsets[0];
        int maxBandOff = this.bandOffsets[0];
        for (int i = 1; i < this.bandOffsets.length; ++i) {
            minBandOff = Math.min(minBandOff, this.bandOffsets[i]);
            maxBandOff = Math.max(maxBandOff, this.bandOffsets[i]);
        }
        int bands = this.bandOffsets.length;
        int pStride = Math.abs(this.pixelStride);
        int lStride = Math.abs(this.scanlineStride);
        int bStride = Math.abs(maxBandOff -= minBandOff);
        if (pStride > lStride) {
            if (pStride > bStride) {
                if (lStride > bStride) {
                    bandOff = new int[this.bandOffsets.length];
                    for (i = 0; i < bands; ++i) {
                        bandOff[i] = this.bandOffsets[i] - minBandOff;
                    }
                    lStride = bStride + 1;
                    pStride = lStride * h;
                } else {
                    bandOff = this.orderBands(this.bandOffsets, lStride * h);
                    pStride = bands * lStride * h;
                }
            } else {
                pStride = lStride * h;
                bandOff = this.orderBands(this.bandOffsets, pStride * w);
            }
        } else if (pStride > bStride) {
            bandOff = new int[this.bandOffsets.length];
            for (i = 0; i < bands; ++i) {
                bandOff[i] = this.bandOffsets[i] - minBandOff;
            }
            pStride = bStride + 1;
            lStride = pStride * w;
        } else if (lStride > bStride) {
            bandOff = this.orderBands(this.bandOffsets, pStride * w);
            lStride = bands * pStride * w;
        } else {
            lStride = pStride * w;
            bandOff = this.orderBands(this.bandOffsets, lStride * h);
        }
        int base = 0;
        if (this.scanlineStride < 0) {
            base += lStride * h;
            lStride *= -1;
        }
        if (this.pixelStride < 0) {
            base += pStride * w;
            pStride *= -1;
        }
        int i = 0;
        while (i < bands) {
            int n = i++;
            bandOff[n] = bandOff[n] + base;
        }
        return new ComponentSampleModel(this.dataType, w, h, pStride, lStride, this.bankIndices, bandOff);
    }

    @Override
    public SampleModel createSubsetSampleModel(int[] bands) {
        if (bands.length > this.bankIndices.length) {
            throw new RasterFormatException("There are only " + this.bankIndices.length + " bands");
        }
        int[] newBankIndices = new int[bands.length];
        int[] newBandOffsets = new int[bands.length];
        for (int i = 0; i < bands.length; ++i) {
            newBankIndices[i] = this.bankIndices[bands[i]];
            newBandOffsets[i] = this.bandOffsets[bands[i]];
        }
        return new ComponentSampleModel(this.dataType, this.width, this.height, this.pixelStride, this.scanlineStride, newBankIndices, newBandOffsets);
    }

    @Override
    public DataBuffer createDataBuffer() {
        DataBuffer dataBuffer = null;
        int size = this.getBufferSize();
        switch (this.dataType) {
            case 0: {
                dataBuffer = new DataBufferByte(size, this.numBanks);
                break;
            }
            case 1: {
                dataBuffer = new DataBufferUShort(size, this.numBanks);
                break;
            }
            case 2: {
                dataBuffer = new DataBufferShort(size, this.numBanks);
                break;
            }
            case 3: {
                dataBuffer = new DataBufferInt(size, this.numBanks);
                break;
            }
            case 4: {
                dataBuffer = new DataBufferFloat(size, this.numBanks);
                break;
            }
            case 5: {
                dataBuffer = new DataBufferDouble(size, this.numBanks);
            }
        }
        return dataBuffer;
    }

    public int getOffset(int x, int y) {
        int offset = y * this.scanlineStride + x * this.pixelStride + this.bandOffsets[0];
        return offset;
    }

    public int getOffset(int x, int y, int b) {
        int offset = y * this.scanlineStride + x * this.pixelStride + this.bandOffsets[b];
        return offset;
    }

    @Override
    public final int[] getSampleSize() {
        int[] sampleSize = new int[this.numBands];
        int sizeInBits = this.getSampleSize(0);
        for (int i = 0; i < this.numBands; ++i) {
            sampleSize[i] = sizeInBits;
        }
        return sampleSize;
    }

    @Override
    public final int getSampleSize(int band) {
        return DataBuffer.getDataTypeSize(this.dataType);
    }

    public final int[] getBankIndices() {
        return (int[])this.bankIndices.clone();
    }

    public final int[] getBandOffsets() {
        return (int[])this.bandOffsets.clone();
    }

    public final int getScanlineStride() {
        return this.scanlineStride;
    }

    public final int getPixelStride() {
        return this.pixelStride;
    }

    @Override
    public final int getNumDataElements() {
        return this.getNumBands();
    }

    @Override
    public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int type = this.getTransferType();
        int numDataElems = this.getNumDataElements();
        int pixelOffset = y * this.scanlineStride + x * this.pixelStride;
        switch (type) {
            case 0: {
                byte[] bdata = obj == null ? new byte[numDataElems] : (byte[])obj;
                for (int i = 0; i < numDataElems; ++i) {
                    bdata[i] = (byte)data.getElem(this.bankIndices[i], pixelOffset + this.bandOffsets[i]);
                }
                obj = bdata;
                break;
            }
            case 1: 
            case 2: {
                short[] sdata = obj == null ? new short[numDataElems] : (short[])obj;
                for (int i = 0; i < numDataElems; ++i) {
                    sdata[i] = (short)data.getElem(this.bankIndices[i], pixelOffset + this.bandOffsets[i]);
                }
                obj = sdata;
                break;
            }
            case 3: {
                int[] idata = obj == null ? new int[numDataElems] : (int[])obj;
                for (int i = 0; i < numDataElems; ++i) {
                    idata[i] = data.getElem(this.bankIndices[i], pixelOffset + this.bandOffsets[i]);
                }
                obj = idata;
                break;
            }
            case 4: {
                float[] fdata = obj == null ? new float[numDataElems] : (float[])obj;
                for (int i = 0; i < numDataElems; ++i) {
                    fdata[i] = data.getElemFloat(this.bankIndices[i], pixelOffset + this.bandOffsets[i]);
                }
                obj = fdata;
                break;
            }
            case 5: {
                double[] ddata = obj == null ? new double[numDataElems] : (double[])obj;
                for (int i = 0; i < numDataElems; ++i) {
                    ddata[i] = data.getElemDouble(this.bankIndices[i], pixelOffset + this.bandOffsets[i]);
                }
                obj = ddata;
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
        int pixelOffset = y * this.scanlineStride + x * this.pixelStride;
        for (int i = 0; i < this.numBands; ++i) {
            pixels[i] = data.getElem(this.bankIndices[i], pixelOffset + this.bandOffsets[i]);
        }
        return pixels;
    }

    @Override
    public int[] getPixels(int x, int y, int w, int h, int[] iArray, DataBuffer data) {
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= this.width || w > this.width || x1 < 0 || x1 > this.width || y < 0 || y >= this.height || y > this.height || y1 < 0 || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int[] pixels = iArray != null ? iArray : new int[w * h * this.numBands];
        int lineOffset = y * this.scanlineStride + x * this.pixelStride;
        int srcOffset = 0;
        for (int i = 0; i < h; ++i) {
            int pixelOffset = lineOffset;
            for (int j = 0; j < w; ++j) {
                for (int k = 0; k < this.numBands; ++k) {
                    pixels[srcOffset++] = data.getElem(this.bankIndices[k], pixelOffset + this.bandOffsets[k]);
                }
                pixelOffset += this.pixelStride;
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
        int sample = data.getElem(this.bankIndices[b], y * this.scanlineStride + x * this.pixelStride + this.bandOffsets[b]);
        return sample;
    }

    @Override
    public float getSampleFloat(int x, int y, int b, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        float sample = data.getElemFloat(this.bankIndices[b], y * this.scanlineStride + x * this.pixelStride + this.bandOffsets[b]);
        return sample;
    }

    @Override
    public double getSampleDouble(int x, int y, int b, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        double sample = data.getElemDouble(this.bankIndices[b], y * this.scanlineStride + x * this.pixelStride + this.bandOffsets[b]);
        return sample;
    }

    @Override
    public int[] getSamples(int x, int y, int w, int h, int b, int[] iArray, DataBuffer data) {
        if (x < 0 || y < 0 || x + w > this.width || y + h > this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int[] samples = iArray != null ? iArray : new int[w * h];
        int lineOffset = y * this.scanlineStride + x * this.pixelStride + this.bandOffsets[b];
        int srcOffset = 0;
        for (int i = 0; i < h; ++i) {
            int sampleOffset = lineOffset;
            for (int j = 0; j < w; ++j) {
                samples[srcOffset++] = data.getElem(this.bankIndices[b], sampleOffset);
                sampleOffset += this.pixelStride;
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
        int numDataElems = this.getNumDataElements();
        int pixelOffset = y * this.scanlineStride + x * this.pixelStride;
        switch (type) {
            case 0: {
                byte[] barray = (byte[])obj;
                for (int i = 0; i < numDataElems; ++i) {
                    data.setElem(this.bankIndices[i], pixelOffset + this.bandOffsets[i], barray[i] & 0xFF);
                }
                break;
            }
            case 1: 
            case 2: {
                short[] sarray = (short[])obj;
                for (int i = 0; i < numDataElems; ++i) {
                    data.setElem(this.bankIndices[i], pixelOffset + this.bandOffsets[i], sarray[i] & 0xFFFF);
                }
                break;
            }
            case 3: {
                int[] iarray = (int[])obj;
                for (int i = 0; i < numDataElems; ++i) {
                    data.setElem(this.bankIndices[i], pixelOffset + this.bandOffsets[i], iarray[i]);
                }
                break;
            }
            case 4: {
                float[] farray = (float[])obj;
                for (int i = 0; i < numDataElems; ++i) {
                    data.setElemFloat(this.bankIndices[i], pixelOffset + this.bandOffsets[i], farray[i]);
                }
                break;
            }
            case 5: {
                double[] darray = (double[])obj;
                for (int i = 0; i < numDataElems; ++i) {
                    data.setElemDouble(this.bankIndices[i], pixelOffset + this.bandOffsets[i], darray[i]);
                }
                break;
            }
        }
    }

    @Override
    public void setPixel(int x, int y, int[] iArray, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int pixelOffset = y * this.scanlineStride + x * this.pixelStride;
        for (int i = 0; i < this.numBands; ++i) {
            data.setElem(this.bankIndices[i], pixelOffset + this.bandOffsets[i], iArray[i]);
        }
    }

    @Override
    public void setPixels(int x, int y, int w, int h, int[] iArray, DataBuffer data) {
        int x1 = x + w;
        int y1 = y + h;
        if (x < 0 || x >= this.width || w > this.width || x1 < 0 || x1 > this.width || y < 0 || y >= this.height || h > this.height || y1 < 0 || y1 > this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int lineOffset = y * this.scanlineStride + x * this.pixelStride;
        int srcOffset = 0;
        for (int i = 0; i < h; ++i) {
            int pixelOffset = lineOffset;
            for (int j = 0; j < w; ++j) {
                for (int k = 0; k < this.numBands; ++k) {
                    data.setElem(this.bankIndices[k], pixelOffset + this.bandOffsets[k], iArray[srcOffset++]);
                }
                pixelOffset += this.pixelStride;
            }
            lineOffset += this.scanlineStride;
        }
    }

    @Override
    public void setSample(int x, int y, int b, int s, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        data.setElem(this.bankIndices[b], y * this.scanlineStride + x * this.pixelStride + this.bandOffsets[b], s);
    }

    @Override
    public void setSample(int x, int y, int b, float s, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        data.setElemFloat(this.bankIndices[b], y * this.scanlineStride + x * this.pixelStride + this.bandOffsets[b], s);
    }

    @Override
    public void setSample(int x, int y, int b, double s, DataBuffer data) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        data.setElemDouble(this.bankIndices[b], y * this.scanlineStride + x * this.pixelStride + this.bandOffsets[b], s);
    }

    @Override
    public void setSamples(int x, int y, int w, int h, int b, int[] iArray, DataBuffer data) {
        if (x < 0 || y < 0 || x + w > this.width || y + h > this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int lineOffset = y * this.scanlineStride + x * this.pixelStride + this.bandOffsets[b];
        int srcOffset = 0;
        for (int i = 0; i < h; ++i) {
            int sampleOffset = lineOffset;
            for (int j = 0; j < w; ++j) {
                data.setElem(this.bankIndices[b], sampleOffset, iArray[srcOffset++]);
                sampleOffset += this.pixelStride;
            }
            lineOffset += this.scanlineStride;
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof ComponentSampleModel)) {
            return false;
        }
        ComponentSampleModel that = (ComponentSampleModel)o;
        return this.width == that.width && this.height == that.height && this.numBands == that.numBands && this.dataType == that.dataType && Arrays.equals(this.bandOffsets, that.bandOffsets) && Arrays.equals(this.bankIndices, that.bankIndices) && this.numBanks == that.numBanks && this.scanlineStride == that.scanlineStride && this.pixelStride == that.pixelStride;
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
        for (i = 0; i < this.bandOffsets.length; ++i) {
            hash ^= this.bandOffsets[i];
            hash <<= 8;
        }
        for (i = 0; i < this.bankIndices.length; ++i) {
            hash ^= this.bankIndices[i];
            hash <<= 8;
        }
        hash ^= this.numBanks;
        hash <<= 8;
        hash ^= this.scanlineStride;
        hash <<= 8;
        return hash ^= this.pixelStride;
    }
}

