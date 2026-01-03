/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import sun.awt.image.IntegerComponentRaster;

public class IntegerInterleavedRaster
extends IntegerComponentRaster {
    private int maxX;
    private int maxY;

    public IntegerInterleavedRaster(SampleModel sampleModel, Point origin) {
        this(sampleModel, (DataBufferInt)sampleModel.createDataBuffer(), new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
    }

    public IntegerInterleavedRaster(SampleModel sampleModel, DataBufferInt dataBuffer, Point origin) {
        this(sampleModel, dataBuffer, new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
    }

    public IntegerInterleavedRaster(SampleModel sampleModel, DataBufferInt dataBuffer, Rectangle aRegion, Point origin, IntegerInterleavedRaster parent) {
        super(sampleModel, dataBuffer, aRegion, origin, parent);
        this.maxX = this.minX + this.width;
        this.maxY = this.minY + this.height;
        this.data = IntegerInterleavedRaster.stealData(dataBuffer, 0);
        if (!(sampleModel instanceof SinglePixelPackedSampleModel)) {
            throw new RasterFormatException("IntegerInterleavedRasters must have SinglePixelPackedSampleModel");
        }
        SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)sampleModel;
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
    public int[] getDataStorage() {
        return this.data;
    }

    @Override
    public Object getDataElements(int x, int y, Object obj) {
        if (x < this.minX || y < this.minY || x >= this.maxX || y >= this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int[] outData = obj == null ? new int[1] : (int[])obj;
        int off = (y - this.minY) * this.scanlineStride + (x - this.minX) + this.dataOffsets[0];
        outData[0] = this.data[off];
        return outData;
    }

    @Override
    public Object getDataElements(int x, int y, int w, int h, Object obj) {
        if (x < this.minX || y < this.minY || x + w > this.maxX || y + h > this.maxY) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int[] outData = obj instanceof int[] ? (int[])obj : new int[w * h];
        int yoff = (y - this.minY) * this.scanlineStride + (x - this.minX) + this.dataOffsets[0];
        int off = 0;
        for (int ystart = 0; ystart < h; ++ystart) {
            System.arraycopy(this.data, yoff, outData, off, w);
            off += w;
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
        int off = (y - this.minY) * this.scanlineStride + (x - this.minX) + this.dataOffsets[0];
        this.data[off] = inData[0];
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
        if (inRaster instanceof IntegerInterleavedRaster) {
            int toff;
            IntegerInterleavedRaster ict = (IntegerInterleavedRaster)inRaster;
            tdata = ict.getDataStorage();
            int tss = ict.getScanlineStride();
            int srcOffset = toff = ict.getDataOffset(0);
            int dstOffset = this.dataOffsets[0] + (dstY - this.minY) * this.scanlineStride + (dstX - this.minX);
            for (int startY = 0; startY < height; ++startY) {
                System.arraycopy(tdata, srcOffset, this.data, dstOffset, width);
                srcOffset += tss;
                dstOffset += this.scanlineStride;
            }
            this.markDirty();
            return;
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
        int yoff = (y - this.minY) * this.scanlineStride + (x - this.minX) + this.dataOffsets[0];
        int off = 0;
        for (int ystart = 0; ystart < h; ++ystart) {
            System.arraycopy(inData, off, this.data, yoff, w);
            off += w;
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
        return new IntegerInterleavedRaster(sm, (DataBufferInt)this.dataBuffer, new Rectangle(x0, y0, width, height), new Point(this.sampleModelTranslateX + deltaX, this.sampleModelTranslateY + deltaY), this);
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
        return new IntegerInterleavedRaster(sm, new Point(0, 0));
    }

    @Override
    public WritableRaster createCompatibleWritableRaster() {
        return this.createCompatibleWritableRaster(this.width, this.height);
    }

    @Override
    public String toString() {
        return "IntegerInterleavedRaster: width = " + this.width + " height = " + this.height + " #Bands = " + this.numBands + " xOff = " + this.sampleModelTranslateX + " yOff = " + this.sampleModelTranslateY + " dataOffset[0] " + this.dataOffsets[0];
    }
}

