/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;

public class WritableRaster
extends Raster {
    protected WritableRaster(SampleModel sampleModel, Point origin) {
        this(sampleModel, sampleModel.createDataBuffer(), new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
    }

    protected WritableRaster(SampleModel sampleModel, DataBuffer dataBuffer, Point origin) {
        this(sampleModel, dataBuffer, new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
    }

    protected WritableRaster(SampleModel sampleModel, DataBuffer dataBuffer, Rectangle aRegion, Point sampleModelTranslate, WritableRaster parent) {
        super(sampleModel, dataBuffer, aRegion, sampleModelTranslate, parent);
    }

    public WritableRaster getWritableParent() {
        return (WritableRaster)this.parent;
    }

    public WritableRaster createWritableTranslatedChild(int childMinX, int childMinY) {
        return this.createWritableChild(this.minX, this.minY, this.width, this.height, childMinX, childMinY, null);
    }

    public WritableRaster createWritableChild(int parentX, int parentY, int w, int h, int childMinX, int childMinY, int[] bandList) {
        if (parentX < this.minX) {
            throw new RasterFormatException("parentX lies outside raster");
        }
        if (parentY < this.minY) {
            throw new RasterFormatException("parentY lies outside raster");
        }
        if (parentX + w < parentX || parentX + w > this.width + this.minX) {
            throw new RasterFormatException("(parentX + width) is outside raster");
        }
        if (parentY + h < parentY || parentY + h > this.height + this.minY) {
            throw new RasterFormatException("(parentY + height) is outside raster");
        }
        SampleModel sm = bandList != null ? this.sampleModel.createSubsetSampleModel(bandList) : this.sampleModel;
        int deltaX = childMinX - parentX;
        int deltaY = childMinY - parentY;
        return new WritableRaster(sm, this.getDataBuffer(), new Rectangle(childMinX, childMinY, w, h), new Point(this.sampleModelTranslateX + deltaX, this.sampleModelTranslateY + deltaY), this);
    }

    public void setDataElements(int x, int y, Object inData) {
        this.sampleModel.setDataElements(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, inData, this.dataBuffer);
    }

    public void setDataElements(int x, int y, Raster inRaster) {
        int dstOffX = x + inRaster.getMinX();
        int dstOffY = y + inRaster.getMinY();
        int width = inRaster.getWidth();
        int height = inRaster.getHeight();
        if (dstOffX < this.minX || dstOffY < this.minY || dstOffX + width > this.minX + this.width || dstOffY + height > this.minY + this.height) {
            throw new ArrayIndexOutOfBoundsException("Coordinate out of bounds!");
        }
        int srcOffX = inRaster.getMinX();
        int srcOffY = inRaster.getMinY();
        Object tdata = null;
        for (int startY = 0; startY < height; ++startY) {
            tdata = inRaster.getDataElements(srcOffX, srcOffY + startY, width, 1, tdata);
            this.setDataElements(dstOffX, dstOffY + startY, width, 1, tdata);
        }
    }

    public void setDataElements(int x, int y, int w, int h, Object inData) {
        this.sampleModel.setDataElements(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, w, h, inData, this.dataBuffer);
    }

    public void setRect(Raster srcRaster) {
        this.setRect(0, 0, srcRaster);
    }

    public void setRect(int dx, int dy, Raster srcRaster) {
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
        if (dstOffX + width > this.minX + this.width) {
            width = this.minX + this.width - dstOffX;
        }
        if (dstOffY + height > this.minY + this.height) {
            height = this.minY + this.height - dstOffY;
        }
        if (width <= 0 || height <= 0) {
            return;
        }
        switch (srcRaster.getSampleModel().getDataType()) {
            case 0: 
            case 1: 
            case 2: 
            case 3: {
                int[] iData = null;
                for (int startY = 0; startY < height; ++startY) {
                    iData = srcRaster.getPixels(srcOffX, srcOffY + startY, width, 1, iData);
                    this.setPixels(dstOffX, dstOffY + startY, width, 1, iData);
                }
                break;
            }
            case 4: {
                float[] fData = null;
                for (int startY = 0; startY < height; ++startY) {
                    fData = srcRaster.getPixels(srcOffX, srcOffY + startY, width, 1, fData);
                    this.setPixels(dstOffX, dstOffY + startY, width, 1, fData);
                }
                break;
            }
            case 5: {
                double[] dData = null;
                for (int startY = 0; startY < height; ++startY) {
                    dData = srcRaster.getPixels(srcOffX, srcOffY + startY, width, 1, dData);
                    this.setPixels(dstOffX, dstOffY + startY, width, 1, dData);
                }
                break;
            }
        }
    }

    public void setPixel(int x, int y, int[] iArray) {
        this.sampleModel.setPixel(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, iArray, this.dataBuffer);
    }

    public void setPixel(int x, int y, float[] fArray) {
        this.sampleModel.setPixel(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, fArray, this.dataBuffer);
    }

    public void setPixel(int x, int y, double[] dArray) {
        this.sampleModel.setPixel(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, dArray, this.dataBuffer);
    }

    public void setPixels(int x, int y, int w, int h, int[] iArray) {
        this.sampleModel.setPixels(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, w, h, iArray, this.dataBuffer);
    }

    public void setPixels(int x, int y, int w, int h, float[] fArray) {
        this.sampleModel.setPixels(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, w, h, fArray, this.dataBuffer);
    }

    public void setPixels(int x, int y, int w, int h, double[] dArray) {
        this.sampleModel.setPixels(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, w, h, dArray, this.dataBuffer);
    }

    public void setSample(int x, int y, int b, int s) {
        this.sampleModel.setSample(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, b, s, this.dataBuffer);
    }

    public void setSample(int x, int y, int b, float s) {
        this.sampleModel.setSample(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, b, s, this.dataBuffer);
    }

    public void setSample(int x, int y, int b, double s) {
        this.sampleModel.setSample(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, b, s, this.dataBuffer);
    }

    public void setSamples(int x, int y, int w, int h, int b, int[] iArray) {
        this.sampleModel.setSamples(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, w, h, b, iArray, this.dataBuffer);
    }

    public void setSamples(int x, int y, int w, int h, int b, float[] fArray) {
        this.sampleModel.setSamples(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, w, h, b, fArray, this.dataBuffer);
    }

    public void setSamples(int x, int y, int w, int h, int b, double[] dArray) {
        this.sampleModel.setSamples(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, w, h, b, dArray, this.dataBuffer);
    }
}

