/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import sun.awt.image.NativeLibLoader;
import sun.awt.image.SunWritableRaster;

public class ShortComponentRaster
extends SunWritableRaster {
    protected int bandOffset;
    protected int[] dataOffsets;
    protected int scanlineStride;
    protected int pixelStride;
    protected short[] data;
    int type;
    private int maxX;
    private int maxY;

    private static native void initIDs();

    public ShortComponentRaster(SampleModel sampleModel, Point origin) {
        this(sampleModel, (DataBufferUShort)sampleModel.createDataBuffer(), new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
    }

    public ShortComponentRaster(SampleModel sampleModel, DataBufferUShort dataBuffer, Point origin) {
        this(sampleModel, dataBuffer, new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
    }

    public ShortComponentRaster(SampleModel sampleModel, DataBufferUShort dataBuffer, Rectangle aRegion, Point origin, ShortComponentRaster parent) {
        super(sampleModel, (DataBuffer)dataBuffer, aRegion, origin, parent);
        this.maxX = this.minX + this.width;
        this.maxY = this.minY + this.height;
        this.data = ShortComponentRaster.stealData(dataBuffer, 0);
        if (dataBuffer.getNumBanks() != 1) {
            throw new RasterFormatException("DataBuffer for ShortComponentRasters must only have 1 bank.");
        }
        int dbOffset = dataBuffer.getOffset();
        if (sampleModel instanceof ComponentSampleModel) {
            ComponentSampleModel csm = (ComponentSampleModel)sampleModel;
            this.type = 2;
            this.scanlineStride = csm.getScanlineStride();
            this.pixelStride = csm.getPixelStride();
            this.dataOffsets = csm.getBandOffsets();
            int xOffset = aRegion.x - origin.x;
            int yOffset = aRegion.y - origin.y;
            int i = 0;
            while (i < this.getNumDataElements()) {
                int n = i++;
                this.dataOffsets[n] = this.dataOffsets[n] + (dbOffset + xOffset * this.pixelStride + yOffset * this.scanlineStride);
            }
        } else if (sampleModel instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)sampleModel;
            this.type = 8;
            this.scanlineStride = sppsm.getScanlineStride();
            this.pixelStride = 1;
            this.dataOffsets = new int[1];
            this.dataOffsets[0] = dbOffset;
            int xOffset = aRegion.x - origin.x;
            int yOffset = aRegion.y - origin.y;
            this.dataOffsets[0] = this.dataOffsets[0] + (xOffset + yOffset * this.scanlineStride);
        } else {
            throw new RasterFormatException("ShortComponentRasters must haveComponentSampleModel or SinglePixelPackedSampleModel");
        }
        this.bandOffset = this.dataOffsets[0];
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

    public short[] getDataStorage() {
        return this.data;
    }

    @Override
    public Object getDataElements(int x, int y, Object obj) {
        if (x < this.minX || y < this.minY || x >= this.maxX || y >= this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        short[] outData = obj == null ? new short[this.numDataElements] : (short[])obj;
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
        short[] outData = obj == null ? new short[w * h * this.numDataElements] : (short[])obj;
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

    public short[] getShortData(int x, int y, int w, int h, int band, short[] outData) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        if (outData == null) {
            outData = new short[this.numDataElements * w * h];
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

    public short[] getShortData(int x, int y, int w, int h, short[] outData) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        if (outData == null) {
            outData = new short[this.numDataElements * w * h];
        }
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
        short[] inData = (short[])obj;
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
        Object tdata = null;
        for (int startY = 0; startY < height; ++startY) {
            tdata = inRaster.getDataElements(srcOffX, srcOffY + startY, width, 1, tdata);
            this.setDataElements(dstX, dstY + startY, width, 1, tdata);
        }
    }

    @Override
    public void setDataElements(int x, int y, int w, int h, Object obj) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        short[] inData = (short[])obj;
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

    public void putShortData(int x, int y, int w, int h, int band, short[] inData) {
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

    public void putShortData(int x, int y, int w, int h, short[] inData) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
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
        return new ShortComponentRaster(sm, (DataBufferUShort)this.dataBuffer, new Rectangle(x0, y0, width, height), new Point(this.sampleModelTranslateX + deltaX, this.sampleModelTranslateY + deltaY), this);
    }

    @Override
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException("negative " + (w <= 0 ? "width" : "height"));
        }
        SampleModel sm = this.sampleModel.createCompatibleSampleModel(w, h);
        return new ShortComponentRaster(sm, new Point(0, 0));
    }

    @Override
    public WritableRaster createCompatibleWritableRaster() {
        return this.createCompatibleWritableRaster(this.width, this.height);
    }

    protected final void verify() {
        if (this.width <= 0 || this.height <= 0 || this.height > Integer.MAX_VALUE / this.width) {
            throw new RasterFormatException("Invalid raster dimension");
        }
        for (int i = 0; i < this.dataOffsets.length; ++i) {
            if (this.dataOffsets[i] >= 0) continue;
            throw new RasterFormatException("Data offsets for band " + i + "(" + this.dataOffsets[i] + ") must be >= 0");
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
        return "ShortComponentRaster: width = " + this.width + " height = " + this.height + " #numDataElements " + this.numDataElements;
    }

    static {
        NativeLibLoader.loadLibraries();
        ShortComponentRaster.initIDs();
    }
}

