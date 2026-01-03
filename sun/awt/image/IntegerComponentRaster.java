/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import sun.awt.image.NativeLibLoader;
import sun.awt.image.SunWritableRaster;

public class IntegerComponentRaster
extends SunWritableRaster {
    static final int TYPE_CUSTOM = 0;
    static final int TYPE_BYTE_SAMPLES = 1;
    static final int TYPE_USHORT_SAMPLES = 2;
    static final int TYPE_INT_SAMPLES = 3;
    static final int TYPE_BYTE_BANDED_SAMPLES = 4;
    static final int TYPE_USHORT_BANDED_SAMPLES = 5;
    static final int TYPE_INT_BANDED_SAMPLES = 6;
    static final int TYPE_BYTE_PACKED_SAMPLES = 7;
    static final int TYPE_USHORT_PACKED_SAMPLES = 8;
    static final int TYPE_INT_PACKED_SAMPLES = 9;
    static final int TYPE_INT_8BIT_SAMPLES = 10;
    static final int TYPE_BYTE_BINARY_SAMPLES = 11;
    protected int bandOffset;
    protected int[] dataOffsets;
    protected int scanlineStride;
    protected int pixelStride;
    protected int[] data;
    protected int numDataElems;
    int type;
    private int maxX;
    private int maxY;

    private static native void initIDs();

    public IntegerComponentRaster(SampleModel sampleModel, Point origin) {
        this(sampleModel, (DataBufferInt)sampleModel.createDataBuffer(), new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
    }

    public IntegerComponentRaster(SampleModel sampleModel, DataBufferInt dataBuffer, Point origin) {
        this(sampleModel, dataBuffer, new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
    }

    public IntegerComponentRaster(SampleModel sampleModel, DataBufferInt dataBuffer, Rectangle aRegion, Point origin, IntegerComponentRaster parent) {
        super(sampleModel, (DataBuffer)dataBuffer, aRegion, origin, parent);
        boolean notByteBoundary;
        SinglePixelPackedSampleModel sppsm;
        this.maxX = this.minX + this.width;
        this.maxY = this.minY + this.height;
        if (dataBuffer.getNumBanks() != 1) {
            throw new RasterFormatException("DataBuffer for IntegerComponentRasters must only have 1 bank.");
        }
        this.data = IntegerComponentRaster.stealData(dataBuffer, 0);
        if (sampleModel instanceof SinglePixelPackedSampleModel) {
            sppsm = (SinglePixelPackedSampleModel)sampleModel;
            int[] boffsets = sppsm.getBitOffsets();
            notByteBoundary = false;
            for (int i = 1; i < boffsets.length; ++i) {
                if (boffsets[i] % 8 == 0) continue;
                notByteBoundary = true;
            }
        } else {
            throw new RasterFormatException("IntegerComponentRasters must have SinglePixelPackedSampleModel");
        }
        this.type = notByteBoundary ? 9 : 10;
        this.scanlineStride = sppsm.getScanlineStride();
        this.pixelStride = 1;
        this.dataOffsets = new int[1];
        this.dataOffsets[0] = dataBuffer.getOffset();
        this.bandOffset = this.dataOffsets[0];
        int xOffset = aRegion.x - origin.x;
        int yOffset = aRegion.y - origin.y;
        this.dataOffsets[0] = this.dataOffsets[0] + (xOffset + yOffset * this.scanlineStride);
        this.numDataElems = sppsm.getNumDataElements();
        this.verify();
    }

    public int[] getDataOffsets() {
        return (int[])this.dataOffsets.clone();
    }

    public int getDataOffset(int band) {
        return this.dataOffsets[band];
    }

    public int getScanlineStride() {
        return this.scanlineStride;
    }

    public int getPixelStride() {
        return this.pixelStride;
    }

    public int[] getDataStorage() {
        return this.data;
    }

    @Override
    public Object getDataElements(int x, int y, Object obj) {
        if (x < this.minX || y < this.minY || x >= this.maxX || y >= this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int[] outData = obj == null ? new int[this.numDataElements] : (int[])obj;
        int off = (y - this.minY) * this.scanlineStride + (x - this.minX) * this.pixelStride;
        for (int band = 0; band < this.numDataElements; ++band) {
            outData[band] = this.data[this.dataOffsets[band] + off];
        }
        return outData;
    }

    @Override
    public Object getDataElements(int x, int y, int w, int h, Object obj) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int[] outData = obj instanceof int[] ? (int[])obj : new int[this.numDataElements * w * h];
        int yoff = (y - this.minY) * this.scanlineStride + (x - this.minX) * this.pixelStride;
        int off = 0;
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
        return outData;
    }

    @Override
    public void setDataElements(int x, int y, Object obj) {
        if (x < this.minX || y < this.minY || x >= this.maxX || y >= this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int[] inData = (int[])obj;
        int off = (y - this.minY) * this.scanlineStride + (x - this.minX) * this.pixelStride;
        for (int i = 0; i < this.numDataElements; ++i) {
            this.data[this.dataOffsets[i] + off] = inData[i];
        }
        this.markDirty();
    }

    @Override
    public void setDataElements(int x, int y, Raster inRaster) {
        int dstOffX = x + inRaster.getMinX();
        int dstOffY = y + inRaster.getMinY();
        int width = inRaster.getWidth();
        int height = inRaster.getHeight();
        if (dstOffX < this.minX || dstOffY < this.minY || dstOffX + width > this.maxX || dstOffY + height > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        this.setDataElements(dstOffX, dstOffY, width, height, inRaster);
    }

    private void setDataElements(int dstX, int dstY, int width, int height, Raster inRaster) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int srcOffX = inRaster.getMinX();
        int srcOffY = inRaster.getMinY();
        int[] tdata = null;
        if (inRaster instanceof IntegerComponentRaster && this.pixelStride == 1 && this.numDataElements == 1) {
            int toff;
            IntegerComponentRaster ict = (IntegerComponentRaster)inRaster;
            if (ict.getNumDataElements() != 1) {
                throw new ArrayIndexOutOfBoundsException("Number of bands does not match");
            }
            tdata = ict.getDataStorage();
            int tss = ict.getScanlineStride();
            int srcOffset = toff = ict.getDataOffset(0);
            int dstOffset = this.dataOffsets[0] + (dstY - this.minY) * this.scanlineStride + (dstX - this.minX);
            if (ict.getPixelStride() == this.pixelStride) {
                width *= this.pixelStride;
                for (int startY = 0; startY < height; ++startY) {
                    System.arraycopy(tdata, srcOffset, this.data, dstOffset, width);
                    srcOffset += tss;
                    dstOffset += this.scanlineStride;
                }
                this.markDirty();
                return;
            }
        }
        Object odata = null;
        for (int startY = 0; startY < height; ++startY) {
            odata = inRaster.getDataElements(srcOffX, srcOffY + startY, width, 1, odata);
            this.setDataElements(dstX, dstY + startY, width, 1, odata);
        }
    }

    @Override
    public void setDataElements(int x, int y, int w, int h, Object obj) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int[] inData = (int[])obj;
        int yoff = (y - this.minY) * this.scanlineStride + (x - this.minX) * this.pixelStride;
        int off = 0;
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
        this.markDirty();
    }

    @Override
    public WritableRaster createWritableChild(int x, int y, int width, int height, int x0, int y0, int[] bandList) {
        if (x < this.minX) {
            throw new RasterFormatException("x lies outside raster");
        }
        if (y < this.minY) {
            throw new RasterFormatException("y lies outside raster");
        }
        if (x + width < x || x + width > this.minX + this.width) {
            throw new RasterFormatException("(x + width) is outside raster");
        }
        if (y + height < y || y + height > this.minY + this.height) {
            throw new RasterFormatException("(y + height) is outside raster");
        }
        SampleModel sm = bandList != null ? this.sampleModel.createSubsetSampleModel(bandList) : this.sampleModel;
        int deltaX = x0 - x;
        int deltaY = y0 - y;
        return new IntegerComponentRaster(sm, (DataBufferInt)this.dataBuffer, new Rectangle(x0, y0, width, height), new Point(this.sampleModelTranslateX + deltaX, this.sampleModelTranslateY + deltaY), this);
    }

    @Override
    public Raster createChild(int x, int y, int width, int height, int x0, int y0, int[] bandList) {
        return this.createWritableChild(x, y, width, height, x0, y0, bandList);
    }

    @Override
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException("negative " + (w <= 0 ? "width" : "height"));
        }
        SampleModel sm = this.sampleModel.createCompatibleSampleModel(w, h);
        return new IntegerComponentRaster(sm, new Point(0, 0));
    }

    @Override
    public WritableRaster createCompatibleWritableRaster() {
        return this.createCompatibleWritableRaster(this.width, this.height);
    }

    protected final void verify() {
        if (this.width <= 0 || this.height <= 0 || this.height > Integer.MAX_VALUE / this.width) {
            throw new RasterFormatException("Invalid raster dimension");
        }
        if (this.dataOffsets[0] < 0) {
            throw new RasterFormatException("Data offset (" + this.dataOffsets[0] + ") must be >= 0");
        }
        if ((long)this.minX - (long)this.sampleModelTranslateX < 0L || (long)this.minY - (long)this.sampleModelTranslateY < 0L) {
            throw new RasterFormatException("Incorrect origin/translate: (" + this.minX + ", " + this.minY + ") / (" + this.sampleModelTranslateX + ", " + this.sampleModelTranslateY + ")");
        }
        if (this.scanlineStride < 0 || this.scanlineStride > Integer.MAX_VALUE / this.height) {
            throw new RasterFormatException("Incorrect scanline stride: " + this.scanlineStride);
        }
        if ((this.height > 1 || this.minY - this.sampleModelTranslateY > 0) && this.scanlineStride > this.data.length) {
            throw new RasterFormatException("Incorrect scanline stride: " + this.scanlineStride);
        }
        int lastScanOffset = (this.height - 1) * this.scanlineStride;
        if (this.pixelStride < 0 || this.pixelStride > Integer.MAX_VALUE / this.width || this.pixelStride > this.data.length) {
            throw new RasterFormatException("Incorrect pixel stride: " + this.pixelStride);
        }
        int lastPixelOffset = (this.width - 1) * this.pixelStride;
        if (lastPixelOffset > Integer.MAX_VALUE - lastScanOffset) {
            throw new RasterFormatException("Incorrect raster attributes");
        }
        lastPixelOffset += lastScanOffset;
        int maxIndex = 0;
        for (int i = 0; i < this.numDataElements; ++i) {
            if (this.dataOffsets[i] > Integer.MAX_VALUE - lastPixelOffset) {
                throw new RasterFormatException("Incorrect band offset: " + this.dataOffsets[i]);
            }
            int index = lastPixelOffset + this.dataOffsets[i];
            if (index <= maxIndex) continue;
            maxIndex = index;
        }
        if (this.data.length <= maxIndex) {
            throw new RasterFormatException("Data array too small (should be > " + maxIndex + " )");
        }
    }

    public String toString() {
        return "IntegerComponentRaster: width = " + this.width + " height = " + this.height + " #Bands = " + this.numBands + " #DataElements " + this.numDataElements + " xOff = " + this.sampleModelTranslateX + " yOff = " + this.sampleModelTranslateY + " dataOffset[0] " + this.dataOffsets[0];
    }

    static {
        NativeLibLoader.loadLibraries();
        IntegerComponentRaster.initIDs();
    }
}

