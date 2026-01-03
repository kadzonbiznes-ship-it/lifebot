/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import sun.awt.image.ByteComponentRaster;

public class ByteInterleavedRaster
extends ByteComponentRaster {
    boolean inOrder;
    int dbOffset;
    int dbOffsetPacked;
    boolean packed = false;
    int[] bitMasks;
    int[] bitOffsets;
    private int maxX = this.minX + this.width;
    private int maxY = this.minY + this.height;

    public ByteInterleavedRaster(SampleModel sampleModel, Point origin) {
        this(sampleModel, (DataBufferByte)sampleModel.createDataBuffer(), new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
    }

    public ByteInterleavedRaster(SampleModel sampleModel, DataBufferByte dataBuffer, Point origin) {
        this(sampleModel, dataBuffer, new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
    }

    private boolean isInterleaved(ComponentSampleModel sm) {
        int minOffset;
        int numBands = this.sampleModel.getNumBands();
        if (numBands == 1) {
            return true;
        }
        int[] bankIndices = sm.getBankIndices();
        for (int i = 0; i < numBands; ++i) {
            if (bankIndices[i] == 0) continue;
            return false;
        }
        int[] bandOffsets = sm.getBandOffsets();
        int maxOffset = minOffset = bandOffsets[0];
        for (int i = 1; i < numBands; ++i) {
            int offset = bandOffsets[i];
            if (offset < minOffset) {
                minOffset = offset;
            }
            if (offset <= maxOffset) continue;
            maxOffset = offset;
        }
        return maxOffset - minOffset < sm.getPixelStride();
    }

    public ByteInterleavedRaster(SampleModel sampleModel, DataBufferByte dataBuffer, Rectangle aRegion, Point origin, ByteInterleavedRaster parent) {
        super(sampleModel, dataBuffer, aRegion, origin, parent);
        this.data = ByteInterleavedRaster.stealData(dataBuffer, 0);
        int xOffset = aRegion.x - origin.x;
        int yOffset = aRegion.y - origin.y;
        if (sampleModel instanceof PixelInterleavedSampleModel || sampleModel instanceof ComponentSampleModel && this.isInterleaved((ComponentSampleModel)sampleModel)) {
            ComponentSampleModel csm = (ComponentSampleModel)sampleModel;
            this.scanlineStride = csm.getScanlineStride();
            this.pixelStride = csm.getPixelStride();
            this.dataOffsets = csm.getBandOffsets();
            int i = 0;
            while (i < this.getNumDataElements()) {
                int n = i++;
                this.dataOffsets[n] = this.dataOffsets[n] + (xOffset * this.pixelStride + yOffset * this.scanlineStride);
            }
        } else if (sampleModel instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)sampleModel;
            this.packed = true;
            this.bitMasks = sppsm.getBitMasks();
            this.bitOffsets = sppsm.getBitOffsets();
            this.scanlineStride = sppsm.getScanlineStride();
            this.pixelStride = 1;
            this.dataOffsets = new int[1];
            this.dataOffsets[0] = dataBuffer.getOffset();
            this.dataOffsets[0] = this.dataOffsets[0] + (xOffset * this.pixelStride + yOffset * this.scanlineStride);
        } else {
            throw new RasterFormatException("ByteInterleavedRasters must have PixelInterleavedSampleModel, SinglePixelPackedSampleModel or interleaved ComponentSampleModel.  Sample model is " + String.valueOf(sampleModel));
        }
        this.bandOffset = this.dataOffsets[0];
        this.dbOffsetPacked = dataBuffer.getOffset() - this.sampleModelTranslateY * this.scanlineStride - this.sampleModelTranslateX * this.pixelStride;
        this.dbOffset = this.dbOffsetPacked - (xOffset * this.pixelStride + yOffset * this.scanlineStride);
        this.inOrder = false;
        if (this.numDataElements == this.pixelStride) {
            this.inOrder = true;
            for (int i = 1; i < this.numDataElements; ++i) {
                if (this.dataOffsets[i] - this.dataOffsets[0] == i) continue;
                this.inOrder = false;
                break;
            }
        }
        this.verify();
    }

    @Override
    public int[] getDataOffsets() {
        return (int[])this.dataOffsets.clone();
    }

    @Override
    public int getDataOffset(int band) {
        return this.dataOffsets[band];
    }

    @Override
    public int getScanlineStride() {
        return this.scanlineStride;
    }

    @Override
    public int getPixelStride() {
        return this.pixelStride;
    }

    @Override
    public byte[] getDataStorage() {
        return this.data;
    }

    @Override
    public Object getDataElements(int x, int y, Object obj) {
        if (x < this.minX || y < this.minY || x >= this.maxX || y >= this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        byte[] outData = obj == null ? new byte[this.numDataElements] : (byte[])obj;
        int off = (y - this.minY) * this.scanlineStride + (x - this.minX) * this.pixelStride;
        for (int band = 0; band < this.numDataElements; ++band) {
            outData[band] = this.data[this.dataOffsets[band] + off];
        }
        return outData;
    }

    @Override
    public Object getDataElements(int x, int y, int w, int h, Object obj) {
        return this.getByteData(x, y, w, h, (byte[])obj);
    }

    @Override
    public byte[] getByteData(int x, int y, int w, int h, int band, byte[] outData) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        if (outData == null) {
            outData = new byte[w * h];
        }
        int yoff = (y - this.minY) * this.scanlineStride + (x - this.minX) * this.pixelStride + this.dataOffsets[band];
        int off = 0;
        if (this.pixelStride == 1) {
            if (this.scanlineStride == w) {
                System.arraycopy(this.data, yoff, outData, 0, w * h);
            } else {
                int ystart = 0;
                while (ystart < h) {
                    System.arraycopy(this.data, yoff, outData, off, w);
                    off += w;
                    ++ystart;
                    yoff += this.scanlineStride;
                }
            }
        } else {
            int ystart = 0;
            while (ystart < h) {
                int xoff = yoff;
                int xstart = 0;
                while (xstart < w) {
                    outData[off++] = this.data[xoff];
                    ++xstart;
                    xoff += this.pixelStride;
                }
                ++ystart;
                yoff += this.scanlineStride;
            }
        }
        return outData;
    }

    @Override
    public byte[] getByteData(int x, int y, int w, int h, byte[] outData) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        if (outData == null) {
            outData = new byte[this.numDataElements * w * h];
        }
        int yoff = (y - this.minY) * this.scanlineStride + (x - this.minX) * this.pixelStride;
        int off = 0;
        if (this.inOrder) {
            yoff += this.dataOffsets[0];
            int rowBytes = w * this.pixelStride;
            if (this.scanlineStride == rowBytes) {
                System.arraycopy(this.data, yoff, outData, off, rowBytes * h);
            } else {
                int ystart = 0;
                while (ystart < h) {
                    System.arraycopy(this.data, yoff, outData, off, rowBytes);
                    off += rowBytes;
                    ++ystart;
                    yoff += this.scanlineStride;
                }
            }
        } else if (this.numDataElements == 1) {
            yoff += this.dataOffsets[0];
            int ystart = 0;
            while (ystart < h) {
                int xoff = yoff;
                int xstart = 0;
                while (xstart < w) {
                    outData[off++] = this.data[xoff];
                    ++xstart;
                    xoff += this.pixelStride;
                }
                ++ystart;
                yoff += this.scanlineStride;
            }
        } else if (this.numDataElements == 2) {
            yoff += this.dataOffsets[0];
            int d1 = this.dataOffsets[1] - this.dataOffsets[0];
            int ystart = 0;
            while (ystart < h) {
                int xoff = yoff;
                int xstart = 0;
                while (xstart < w) {
                    outData[off++] = this.data[xoff];
                    outData[off++] = this.data[xoff + d1];
                    ++xstart;
                    xoff += this.pixelStride;
                }
                ++ystart;
                yoff += this.scanlineStride;
            }
        } else if (this.numDataElements == 3) {
            yoff += this.dataOffsets[0];
            int d1 = this.dataOffsets[1] - this.dataOffsets[0];
            int d2 = this.dataOffsets[2] - this.dataOffsets[0];
            int ystart = 0;
            while (ystart < h) {
                int xoff = yoff;
                int xstart = 0;
                while (xstart < w) {
                    outData[off++] = this.data[xoff];
                    outData[off++] = this.data[xoff + d1];
                    outData[off++] = this.data[xoff + d2];
                    ++xstart;
                    xoff += this.pixelStride;
                }
                ++ystart;
                yoff += this.scanlineStride;
            }
        } else if (this.numDataElements == 4) {
            yoff += this.dataOffsets[0];
            int d1 = this.dataOffsets[1] - this.dataOffsets[0];
            int d2 = this.dataOffsets[2] - this.dataOffsets[0];
            int d3 = this.dataOffsets[3] - this.dataOffsets[0];
            int ystart = 0;
            while (ystart < h) {
                int xoff = yoff;
                int xstart = 0;
                while (xstart < w) {
                    outData[off++] = this.data[xoff];
                    outData[off++] = this.data[xoff + d1];
                    outData[off++] = this.data[xoff + d2];
                    outData[off++] = this.data[xoff + d3];
                    ++xstart;
                    xoff += this.pixelStride;
                }
                ++ystart;
                yoff += this.scanlineStride;
            }
        } else {
            int ystart = 0;
            while (ystart < h) {
                int xoff = yoff;
                int xstart = 0;
                while (xstart < w) {
                    for (int c = 0; c < this.numDataElements; ++c) {
                        outData[off++] = this.data[this.dataOffsets[c] + xoff];
                    }
                    ++xstart;
                    xoff += this.pixelStride;
                }
                ++ystart;
                yoff += this.scanlineStride;
            }
        }
        return outData;
    }

    @Override
    public void setDataElements(int x, int y, Object obj) {
        if (x < this.minX || y < this.minY || x >= this.maxX || y >= this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        byte[] inData = (byte[])obj;
        int off = (y - this.minY) * this.scanlineStride + (x - this.minX) * this.pixelStride;
        for (int i = 0; i < this.numDataElements; ++i) {
            this.data[this.dataOffsets[i] + off] = inData[i];
        }
        this.markDirty();
    }

    @Override
    public void setDataElements(int x, int y, Raster inRaster) {
        int srcOffX = inRaster.getMinX();
        int srcOffY = inRaster.getMinY();
        int dstOffX = x + srcOffX;
        int dstOffY = y + srcOffY;
        int width = inRaster.getWidth();
        int height = inRaster.getHeight();
        if (dstOffX < this.minX || dstOffY < this.minY || dstOffX + width > this.maxX || dstOffY + height > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        this.setDataElements(dstOffX, dstOffY, srcOffX, srcOffY, width, height, inRaster);
    }

    private void setDataElements(int dstX, int dstY, int srcX, int srcY, int width, int height, Raster inRaster) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int srcOffX = inRaster.getMinX();
        int srcOffY = inRaster.getMinY();
        Object tdata = null;
        if (inRaster instanceof ByteInterleavedRaster) {
            ByteInterleavedRaster bct = (ByteInterleavedRaster)inRaster;
            byte[] bdata = bct.getDataStorage();
            if (this.inOrder && bct.inOrder && this.pixelStride == bct.pixelStride) {
                int toff = bct.getDataOffset(0);
                int tss = bct.getScanlineStride();
                int tps = bct.getPixelStride();
                int srcOffset = toff + (srcY - srcOffY) * tss + (srcX - srcOffX) * tps;
                int dstOffset = this.dataOffsets[0] + (dstY - this.minY) * this.scanlineStride + (dstX - this.minX) * this.pixelStride;
                int nbytes = width * this.pixelStride;
                for (int tmpY = 0; tmpY < height; ++tmpY) {
                    System.arraycopy(bdata, srcOffset, this.data, dstOffset, nbytes);
                    srcOffset += tss;
                    dstOffset += this.scanlineStride;
                }
                this.markDirty();
                return;
            }
        }
        for (int startY = 0; startY < height; ++startY) {
            tdata = inRaster.getDataElements(srcOffX, srcOffY + startY, width, 1, tdata);
            this.setDataElements(dstX, dstY + startY, width, 1, tdata);
        }
    }

    @Override
    public void setDataElements(int x, int y, int w, int h, Object obj) {
        this.putByteData(x, y, w, h, (byte[])obj);
    }

    @Override
    public void putByteData(int x, int y, int w, int h, int band, byte[] inData) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int yoff = (y - this.minY) * this.scanlineStride + (x - this.minX) * this.pixelStride + this.dataOffsets[band];
        int off = 0;
        if (this.pixelStride == 1) {
            if (this.scanlineStride == w) {
                System.arraycopy(inData, 0, this.data, yoff, w * h);
            } else {
                int ystart = 0;
                while (ystart < h) {
                    System.arraycopy(inData, off, this.data, yoff, w);
                    off += w;
                    ++ystart;
                    yoff += this.scanlineStride;
                }
            }
        } else {
            int ystart = 0;
            while (ystart < h) {
                int xoff = yoff;
                int xstart = 0;
                while (xstart < w) {
                    this.data[xoff] = inData[off++];
                    ++xstart;
                    xoff += this.pixelStride;
                }
                ++ystart;
                yoff += this.scanlineStride;
            }
        }
        this.markDirty();
    }

    @Override
    public void putByteData(int x, int y, int w, int h, byte[] inData) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int yoff = (y - this.minY) * this.scanlineStride + (x - this.minX) * this.pixelStride;
        int off = 0;
        if (this.inOrder) {
            yoff += this.dataOffsets[0];
            int rowBytes = w * this.pixelStride;
            if (rowBytes == this.scanlineStride) {
                System.arraycopy(inData, 0, this.data, yoff, rowBytes * h);
            } else {
                int ystart = 0;
                while (ystart < h) {
                    System.arraycopy(inData, off, this.data, yoff, rowBytes);
                    off += rowBytes;
                    ++ystart;
                    yoff += this.scanlineStride;
                }
            }
        } else if (this.numDataElements == 1) {
            yoff += this.dataOffsets[0];
            int ystart = 0;
            while (ystart < h) {
                int xoff = yoff;
                int xstart = 0;
                while (xstart < w) {
                    this.data[xoff] = inData[off++];
                    ++xstart;
                    xoff += this.pixelStride;
                }
                ++ystart;
                yoff += this.scanlineStride;
            }
        } else if (this.numDataElements == 2) {
            yoff += this.dataOffsets[0];
            int d1 = this.dataOffsets[1] - this.dataOffsets[0];
            int ystart = 0;
            while (ystart < h) {
                int xoff = yoff;
                int xstart = 0;
                while (xstart < w) {
                    this.data[xoff] = inData[off++];
                    this.data[xoff + d1] = inData[off++];
                    ++xstart;
                    xoff += this.pixelStride;
                }
                ++ystart;
                yoff += this.scanlineStride;
            }
        } else if (this.numDataElements == 3) {
            yoff += this.dataOffsets[0];
            int d1 = this.dataOffsets[1] - this.dataOffsets[0];
            int d2 = this.dataOffsets[2] - this.dataOffsets[0];
            int ystart = 0;
            while (ystart < h) {
                int xoff = yoff;
                int xstart = 0;
                while (xstart < w) {
                    this.data[xoff] = inData[off++];
                    this.data[xoff + d1] = inData[off++];
                    this.data[xoff + d2] = inData[off++];
                    ++xstart;
                    xoff += this.pixelStride;
                }
                ++ystart;
                yoff += this.scanlineStride;
            }
        } else if (this.numDataElements == 4) {
            yoff += this.dataOffsets[0];
            int d1 = this.dataOffsets[1] - this.dataOffsets[0];
            int d2 = this.dataOffsets[2] - this.dataOffsets[0];
            int d3 = this.dataOffsets[3] - this.dataOffsets[0];
            int ystart = 0;
            while (ystart < h) {
                int xoff = yoff;
                int xstart = 0;
                while (xstart < w) {
                    this.data[xoff] = inData[off++];
                    this.data[xoff + d1] = inData[off++];
                    this.data[xoff + d2] = inData[off++];
                    this.data[xoff + d3] = inData[off++];
                    ++xstart;
                    xoff += this.pixelStride;
                }
                ++ystart;
                yoff += this.scanlineStride;
            }
        } else {
            int ystart = 0;
            while (ystart < h) {
                int xoff = yoff;
                int xstart = 0;
                while (xstart < w) {
                    for (int c = 0; c < this.numDataElements; ++c) {
                        this.data[this.dataOffsets[c] + xoff] = inData[off++];
                    }
                    ++xstart;
                    xoff += this.pixelStride;
                }
                ++ystart;
                yoff += this.scanlineStride;
            }
        }
        this.markDirty();
    }

    @Override
    public int getSample(int x, int y, int b) {
        if (x < this.minX || y < this.minY || x >= this.maxX || y >= this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        if (this.packed) {
            int offset = y * this.scanlineStride + x + this.dbOffsetPacked;
            byte sample = this.data[offset];
            return (sample & this.bitMasks[b]) >>> this.bitOffsets[b];
        }
        int offset = y * this.scanlineStride + x * this.pixelStride + this.dbOffset;
        return this.data[offset + this.dataOffsets[b]] & 0xFF;
    }

    @Override
    public void setSample(int x, int y, int b, int s) {
        if (x < this.minX || y < this.minY || x >= this.maxX || y >= this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        if (this.packed) {
            int offset = y * this.scanlineStride + x + this.dbOffsetPacked;
            int bitMask = this.bitMasks[b];
            byte value = this.data[offset];
            value = (byte)(value & ~bitMask);
            this.data[offset] = value = (byte)(value | s << this.bitOffsets[b] & bitMask);
        } else {
            int offset = y * this.scanlineStride + x * this.pixelStride + this.dbOffset;
            this.data[offset + this.dataOffsets[b]] = (byte)s;
        }
        this.markDirty();
    }

    @Override
    public int[] getSamples(int x, int y, int w, int h, int b, int[] iArray) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int[] samples = iArray != null ? iArray : new int[w * h];
        int lineOffset = y * this.scanlineStride + x * this.pixelStride;
        int dstOffset = 0;
        if (this.packed) {
            lineOffset += this.dbOffsetPacked;
            int bitMask = this.bitMasks[b];
            int bitOffset = this.bitOffsets[b];
            for (int j = 0; j < h; ++j) {
                int sampleOffset = lineOffset;
                for (int i = 0; i < w; ++i) {
                    byte value = this.data[sampleOffset++];
                    samples[dstOffset++] = (value & bitMask) >>> bitOffset;
                }
                lineOffset += this.scanlineStride;
            }
        } else {
            lineOffset += this.dbOffset + this.dataOffsets[b];
            for (int j = 0; j < h; ++j) {
                int sampleOffset = lineOffset;
                for (int i = 0; i < w; ++i) {
                    samples[dstOffset++] = this.data[sampleOffset] & 0xFF;
                    sampleOffset += this.pixelStride;
                }
                lineOffset += this.scanlineStride;
            }
        }
        return samples;
    }

    @Override
    public void setSamples(int x, int y, int w, int h, int b, int[] iArray) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int lineOffset = y * this.scanlineStride + x * this.pixelStride;
        int srcOffset = 0;
        if (this.packed) {
            lineOffset += this.dbOffsetPacked;
            int bitMask = this.bitMasks[b];
            for (int j = 0; j < h; ++j) {
                int sampleOffset = lineOffset;
                for (int i = 0; i < w; ++i) {
                    byte value = this.data[sampleOffset];
                    value = (byte)(value & ~bitMask);
                    int sample = iArray[srcOffset++];
                    value = (byte)(value | sample << this.bitOffsets[b] & bitMask);
                    this.data[sampleOffset++] = value;
                }
                lineOffset += this.scanlineStride;
            }
        } else {
            lineOffset += this.dbOffset + this.dataOffsets[b];
            for (int i = 0; i < h; ++i) {
                int sampleOffset = lineOffset;
                for (int j = 0; j < w; ++j) {
                    this.data[sampleOffset] = (byte)iArray[srcOffset++];
                    sampleOffset += this.pixelStride;
                }
                lineOffset += this.scanlineStride;
            }
        }
        this.markDirty();
    }

    @Override
    public int[] getPixels(int x, int y, int w, int h, int[] iArray) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int[] pixels = iArray != null ? iArray : new int[w * h * this.numBands];
        int lineOffset = y * this.scanlineStride + x * this.pixelStride;
        int dstOffset = 0;
        if (this.packed) {
            lineOffset += this.dbOffsetPacked;
            for (int j = 0; j < h; ++j) {
                for (int i = 0; i < w; ++i) {
                    byte value = this.data[lineOffset + i];
                    for (int k = 0; k < this.numBands; ++k) {
                        pixels[dstOffset++] = (value & this.bitMasks[k]) >>> this.bitOffsets[k];
                    }
                }
                lineOffset += this.scanlineStride;
            }
        } else {
            lineOffset += this.dbOffset;
            int d0 = this.dataOffsets[0];
            if (this.numBands == 1) {
                for (int j = 0; j < h; ++j) {
                    int pixelOffset = lineOffset + d0;
                    for (int i = 0; i < w; ++i) {
                        pixels[dstOffset++] = this.data[pixelOffset] & 0xFF;
                        pixelOffset += this.pixelStride;
                    }
                    lineOffset += this.scanlineStride;
                }
            } else if (this.numBands == 2) {
                int d1 = this.dataOffsets[1] - d0;
                for (int j = 0; j < h; ++j) {
                    int pixelOffset = lineOffset + d0;
                    for (int i = 0; i < w; ++i) {
                        pixels[dstOffset++] = this.data[pixelOffset] & 0xFF;
                        pixels[dstOffset++] = this.data[pixelOffset + d1] & 0xFF;
                        pixelOffset += this.pixelStride;
                    }
                    lineOffset += this.scanlineStride;
                }
            } else if (this.numBands == 3) {
                int d1 = this.dataOffsets[1] - d0;
                int d2 = this.dataOffsets[2] - d0;
                for (int j = 0; j < h; ++j) {
                    int pixelOffset = lineOffset + d0;
                    for (int i = 0; i < w; ++i) {
                        pixels[dstOffset++] = this.data[pixelOffset] & 0xFF;
                        pixels[dstOffset++] = this.data[pixelOffset + d1] & 0xFF;
                        pixels[dstOffset++] = this.data[pixelOffset + d2] & 0xFF;
                        pixelOffset += this.pixelStride;
                    }
                    lineOffset += this.scanlineStride;
                }
            } else if (this.numBands == 4) {
                int d1 = this.dataOffsets[1] - d0;
                int d2 = this.dataOffsets[2] - d0;
                int d3 = this.dataOffsets[3] - d0;
                for (int j = 0; j < h; ++j) {
                    int pixelOffset = lineOffset + d0;
                    for (int i = 0; i < w; ++i) {
                        pixels[dstOffset++] = this.data[pixelOffset] & 0xFF;
                        pixels[dstOffset++] = this.data[pixelOffset + d1] & 0xFF;
                        pixels[dstOffset++] = this.data[pixelOffset + d2] & 0xFF;
                        pixels[dstOffset++] = this.data[pixelOffset + d3] & 0xFF;
                        pixelOffset += this.pixelStride;
                    }
                    lineOffset += this.scanlineStride;
                }
            } else {
                for (int j = 0; j < h; ++j) {
                    int pixelOffset = lineOffset;
                    for (int i = 0; i < w; ++i) {
                        for (int k = 0; k < this.numBands; ++k) {
                            pixels[dstOffset++] = this.data[pixelOffset + this.dataOffsets[k]] & 0xFF;
                        }
                        pixelOffset += this.pixelStride;
                    }
                    lineOffset += this.scanlineStride;
                }
            }
        }
        return pixels;
    }

    @Override
    public void setPixels(int x, int y, int w, int h, int[] iArray) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int lineOffset = y * this.scanlineStride + x * this.pixelStride;
        int srcOffset = 0;
        if (this.packed) {
            lineOffset += this.dbOffsetPacked;
            for (int j = 0; j < h; ++j) {
                for (int i = 0; i < w; ++i) {
                    int value = 0;
                    for (int k = 0; k < this.numBands; ++k) {
                        int srcValue = iArray[srcOffset++];
                        value |= srcValue << this.bitOffsets[k] & this.bitMasks[k];
                    }
                    this.data[lineOffset + i] = (byte)value;
                }
                lineOffset += this.scanlineStride;
            }
        } else {
            lineOffset += this.dbOffset;
            int d0 = this.dataOffsets[0];
            if (this.numBands == 1) {
                for (int j = 0; j < h; ++j) {
                    int pixelOffset = lineOffset + d0;
                    for (int i = 0; i < w; ++i) {
                        this.data[pixelOffset] = (byte)iArray[srcOffset++];
                        pixelOffset += this.pixelStride;
                    }
                    lineOffset += this.scanlineStride;
                }
            } else if (this.numBands == 2) {
                int d1 = this.dataOffsets[1] - d0;
                for (int j = 0; j < h; ++j) {
                    int pixelOffset = lineOffset + d0;
                    for (int i = 0; i < w; ++i) {
                        this.data[pixelOffset] = (byte)iArray[srcOffset++];
                        this.data[pixelOffset + d1] = (byte)iArray[srcOffset++];
                        pixelOffset += this.pixelStride;
                    }
                    lineOffset += this.scanlineStride;
                }
            } else if (this.numBands == 3) {
                int d1 = this.dataOffsets[1] - d0;
                int d2 = this.dataOffsets[2] - d0;
                for (int j = 0; j < h; ++j) {
                    int pixelOffset = lineOffset + d0;
                    for (int i = 0; i < w; ++i) {
                        this.data[pixelOffset] = (byte)iArray[srcOffset++];
                        this.data[pixelOffset + d1] = (byte)iArray[srcOffset++];
                        this.data[pixelOffset + d2] = (byte)iArray[srcOffset++];
                        pixelOffset += this.pixelStride;
                    }
                    lineOffset += this.scanlineStride;
                }
            } else if (this.numBands == 4) {
                int d1 = this.dataOffsets[1] - d0;
                int d2 = this.dataOffsets[2] - d0;
                int d3 = this.dataOffsets[3] - d0;
                for (int j = 0; j < h; ++j) {
                    int pixelOffset = lineOffset + d0;
                    for (int i = 0; i < w; ++i) {
                        this.data[pixelOffset] = (byte)iArray[srcOffset++];
                        this.data[pixelOffset + d1] = (byte)iArray[srcOffset++];
                        this.data[pixelOffset + d2] = (byte)iArray[srcOffset++];
                        this.data[pixelOffset + d3] = (byte)iArray[srcOffset++];
                        pixelOffset += this.pixelStride;
                    }
                    lineOffset += this.scanlineStride;
                }
            } else {
                for (int j = 0; j < h; ++j) {
                    int pixelOffset = lineOffset;
                    for (int i = 0; i < w; ++i) {
                        for (int k = 0; k < this.numBands; ++k) {
                            this.data[pixelOffset + this.dataOffsets[k]] = (byte)iArray[srcOffset++];
                        }
                        pixelOffset += this.pixelStride;
                    }
                    lineOffset += this.scanlineStride;
                }
            }
        }
        this.markDirty();
    }

    @Override
    public void setRect(int dx, int dy, Raster srcRaster) {
        if (!(srcRaster instanceof ByteInterleavedRaster)) {
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
        this.setDataElements(dstOffX, dstOffY, srcOffX, srcOffY, width, height, srcRaster);
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
        return new ByteInterleavedRaster(sm, (DataBufferByte)this.dataBuffer, new Rectangle(x0, y0, width, height), new Point(this.sampleModelTranslateX + deltaX, this.sampleModelTranslateY + deltaY), this);
    }

    @Override
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException("negative " + (w <= 0 ? "width" : "height"));
        }
        SampleModel sm = this.sampleModel.createCompatibleSampleModel(w, h);
        return new ByteInterleavedRaster(sm, new Point(0, 0));
    }

    @Override
    public WritableRaster createCompatibleWritableRaster() {
        return this.createCompatibleWritableRaster(this.width, this.height);
    }

    @Override
    public String toString() {
        return "ByteInterleavedRaster: width = " + this.width + " height = " + this.height + " #numDataElements " + this.numDataElements + " dataOff[0] = " + this.dataOffsets[0];
    }
}

