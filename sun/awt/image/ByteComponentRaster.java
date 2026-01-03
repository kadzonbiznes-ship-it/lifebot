/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import sun.awt.image.NativeLibLoader;
import sun.awt.image.SunWritableRaster;

public class ByteComponentRaster
extends SunWritableRaster {
    protected int bandOffset;
    protected int[] dataOffsets;
    protected int scanlineStride;
    protected int pixelStride;
    protected byte[] data;
    int type;
    private int maxX;
    private int maxY;

    private static native void initIDs();

    public ByteComponentRaster(SampleModel sampleModel, Point origin) {
        this(sampleModel, (DataBufferByte)sampleModel.createDataBuffer(), new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
    }

    public ByteComponentRaster(SampleModel sampleModel, DataBufferByte dataBuffer, Point origin) {
        this(sampleModel, dataBuffer, new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
    }

    public ByteComponentRaster(SampleModel sampleModel, DataBufferByte dataBuffer, Rectangle aRegion, Point origin, ByteComponentRaster parent) {
        super(sampleModel, (DataBuffer)dataBuffer, aRegion, origin, parent);
        this.maxX = this.minX + this.width;
        this.maxY = this.minY + this.height;
        this.data = ByteComponentRaster.stealData(dataBuffer, 0);
        if (dataBuffer.getNumBanks() != 1) {
            throw new RasterFormatException("DataBuffer for ByteComponentRasters must only have 1 bank.");
        }
        int dbOffset = dataBuffer.getOffset();
        if (sampleModel instanceof ComponentSampleModel) {
            ComponentSampleModel ism = (ComponentSampleModel)sampleModel;
            this.type = 1;
            this.scanlineStride = ism.getScanlineStride();
            this.pixelStride = ism.getPixelStride();
            this.dataOffsets = ism.getBandOffsets();
            int xOffset = aRegion.x - origin.x;
            int yOffset = aRegion.y - origin.y;
            int i = 0;
            while (i < this.getNumDataElements()) {
                int n = i++;
                this.dataOffsets[n] = this.dataOffsets[n] + (dbOffset + xOffset * this.pixelStride + yOffset * this.scanlineStride);
            }
        } else if (sampleModel instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)sampleModel;
            this.type = 7;
            this.scanlineStride = sppsm.getScanlineStride();
            this.pixelStride = 1;
            this.dataOffsets = new int[1];
            this.dataOffsets[0] = dbOffset;
            int xOffset = aRegion.x - origin.x;
            int yOffset = aRegion.y - origin.y;
            this.dataOffsets[0] = this.dataOffsets[0] + (xOffset * this.pixelStride + yOffset * this.scanlineStride);
        } else {
            throw new RasterFormatException("IntegerComponentRasters must have ComponentSampleModel or SinglePixelPackedSampleModel");
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
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        byte[] outData = obj == null ? new byte[w * h * this.numDataElements] : (byte[])obj;
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

    public byte[] getByteData(int x, int y, int w, int h, int band, byte[] outData) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        if (outData == null) {
            outData = new byte[this.scanlineStride * h];
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

    public byte[] getByteData(int x, int y, int w, int h, byte[] outData) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        if (outData == null) {
            outData = new byte[this.numDataElements * this.scanlineStride * h];
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
        byte[] inData = (byte[])obj;
        int off = (y - this.minY) * this.scanlineStride + (x - this.minX) * this.pixelStride;
        for (int i = 0; i < this.numDataElements; ++i) {
            this.data[this.dataOffsets[i] + off] = inData[i];
        }
        this.markDirty();
    }

    @Override
    public void setDataElements(int x, int y, Raster inRaster) {
        int dstOffX = inRaster.getMinX() + x;
        int dstOffY = inRaster.getMinY() + y;
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
        if (inRaster instanceof ByteComponentRaster) {
            ByteComponentRaster bct = (ByteComponentRaster)inRaster;
            byte[] bdata = bct.getDataStorage();
            if (this.numDataElements == 1) {
                int toff = bct.getDataOffset(0);
                int tss = bct.getScanlineStride();
                int srcOffset = toff;
                int dstOffset = this.dataOffsets[0] + (dstY - this.minY) * this.scanlineStride + (dstX - this.minX);
                if (this.pixelStride == bct.getPixelStride()) {
                    width *= this.pixelStride;
                    for (int tmpY = 0; tmpY < height; ++tmpY) {
                        System.arraycopy(bdata, srcOffset, this.data, dstOffset, width);
                        srcOffset += tss;
                        dstOffset += this.scanlineStride;
                    }
                    this.markDirty();
                    return;
                }
            }
        }
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
        byte[] inData = (byte[])obj;
        int yoff = (y - this.minY) * this.scanlineStride + (x - this.minX) * this.pixelStride;
        int off = 0;
        if (this.numDataElements == 1) {
            int srcOffset = 0;
            int dstOffset = yoff + this.dataOffsets[0];
            for (int ystart = 0; ystart < h; ++ystart) {
                int xoff = yoff;
                System.arraycopy(inData, srcOffset, this.data, dstOffset, w);
                srcOffset += w;
                dstOffset += this.scanlineStride;
            }
            this.markDirty();
            return;
        }
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

    public void putByteData(int x, int y, int w, int h, byte[] inData) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int yoff = (y - this.minY) * this.scanlineStride + (x - this.minX) * this.pixelStride;
        int off = 0;
        if (this.numDataElements == 1) {
            yoff += this.dataOffsets[0];
            if (this.pixelStride == 1) {
                if (this.scanlineStride == w) {
                    System.arraycopy(inData, 0, this.data, yoff, w * h);
                } else {
                    for (int ystart = 0; ystart < h; ++ystart) {
                        System.arraycopy(inData, off, this.data, yoff, w);
                        off += w;
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
        return new ByteComponentRaster(sm, (DataBufferByte)this.dataBuffer, new Rectangle(x0, y0, width, height), new Point(this.sampleModelTranslateX + deltaX, this.sampleModelTranslateY + deltaY), this);
    }

    @Override
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException("negative " + (w <= 0 ? "width" : "height"));
        }
        SampleModel sm = this.sampleModel.createCompatibleSampleModel(w, h);
        return new ByteComponentRaster(sm, new Point(0, 0));
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
        return "ByteComponentRaster: width = " + this.width + " height = " + this.height + " #numDataElements " + this.numDataElements + " dataOff[0] = " + this.dataOffsets[0];
    }

    static {
        NativeLibLoader.loadLibraries();
        ByteComponentRaster.initIDs();
    }
}

