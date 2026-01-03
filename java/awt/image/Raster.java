/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.RasterFormatException;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import sun.awt.image.ByteBandedRaster;
import sun.awt.image.ByteInterleavedRaster;
import sun.awt.image.BytePackedRaster;
import sun.awt.image.IntegerInterleavedRaster;
import sun.awt.image.ShortBandedRaster;
import sun.awt.image.ShortInterleavedRaster;
import sun.awt.image.SunWritableRaster;

public class Raster {
    protected SampleModel sampleModel;
    protected DataBuffer dataBuffer;
    protected int minX;
    protected int minY;
    protected int width;
    protected int height;
    protected int sampleModelTranslateX;
    protected int sampleModelTranslateY;
    protected int numBands;
    protected int numDataElements;
    protected Raster parent;

    private static native void initIDs();

    public static WritableRaster createInterleavedRaster(int dataType, int w, int h, int bands, Point location) {
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("w and h must be > 0");
        }
        long lsz = (long)w * (long)h;
        if (lsz > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Dimensions (width=" + w + " height=" + h + ") are too large");
        }
        int[] bandOffsets = new int[bands];
        for (int i = 0; i < bands; ++i) {
            bandOffsets[i] = i;
        }
        return Raster.createInterleavedRaster(dataType, w, h, w * bands, bands, bandOffsets, location);
    }

    public static WritableRaster createInterleavedRaster(int dataType, int w, int h, int scanlineStride, int pixelStride, int[] bandOffsets, Point location) {
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("w and h must be > 0");
        }
        long lsz = (long)w * (long)h;
        if (lsz > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Dimensions (width=" + w + " height=" + h + ") are too large");
        }
        if (pixelStride < 0) {
            throw new IllegalArgumentException("pixelStride is < 0");
        }
        if (scanlineStride < 0) {
            throw new IllegalArgumentException("scanlineStride is < 0");
        }
        int size = scanlineStride * (h - 1) + pixelStride * w;
        if (location == null) {
            location = new Point(0, 0);
        } else if ((double)w + location.getX() > 2.147483647E9 || (double)h + location.getY() > 2.147483647E9) {
            throw new RasterFormatException("location.x + w and location.y + h  cannot exceed Integer.MAX_VALUE");
        }
        return Raster.createInterleavedRaster(switch (dataType) {
            case 0 -> new DataBufferByte(size);
            case 1 -> new DataBufferUShort(size);
            default -> throw new IllegalArgumentException("Unsupported data type " + dataType);
        }, w, h, scanlineStride, pixelStride, bandOffsets, location);
    }

    public static WritableRaster createBandedRaster(int dataType, int w, int h, int bands, Point location) {
        if (bands < 1) {
            throw new ArrayIndexOutOfBoundsException("Number of bands (" + bands + ") must be greater than 0");
        }
        int[] bankIndices = new int[bands];
        for (int i = 0; i < bands; ++i) {
            bankIndices[i] = i;
        }
        int[] bandOffsets = new int[bands];
        return Raster.createBandedRaster(dataType, w, h, w, bankIndices, bandOffsets, location);
    }

    public static WritableRaster createBandedRaster(int dataType, int w, int h, int scanlineStride, int[] bankIndices, int[] bandOffsets, Point location) {
        int bands = bandOffsets.length;
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("w and h must be positive");
        }
        long lsz = (long)w * (long)h;
        if (lsz > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Dimensions (width=" + w + " height=" + h + ") are too large");
        }
        if (bankIndices == null) {
            throw new ArrayIndexOutOfBoundsException("Bank indices array is null");
        }
        if (bandOffsets == null) {
            throw new ArrayIndexOutOfBoundsException("Band offsets array is null");
        }
        if (location != null && ((double)w + location.getX() > 2.147483647E9 || (double)h + location.getY() > 2.147483647E9)) {
            throw new IllegalArgumentException("location.x + w and location.y + h  cannot exceed Integer.MAX_VALUE");
        }
        int maxBank = bankIndices[0];
        int maxBandOff = bandOffsets[0];
        for (int i = 1; i < bands; ++i) {
            if (bankIndices[i] > maxBank) {
                maxBank = bankIndices[i];
            }
            if (bandOffsets[i] <= maxBandOff) continue;
            maxBandOff = bandOffsets[i];
        }
        int banks = maxBank + 1;
        int size = maxBandOff + scanlineStride * (h - 1) + w;
        return Raster.createBandedRaster(switch (dataType) {
            case 0 -> new DataBufferByte(size, banks);
            case 1 -> new DataBufferUShort(size, banks);
            case 3 -> new DataBufferInt(size, banks);
            default -> throw new IllegalArgumentException("Unsupported data type " + dataType);
        }, w, h, scanlineStride, bankIndices, bandOffsets, location);
    }

    public static WritableRaster createPackedRaster(int dataType, int w, int h, int[] bandMasks, Point location) {
        return Raster.createPackedRaster(switch (dataType) {
            case 0 -> new DataBufferByte(w * h);
            case 1 -> new DataBufferUShort(w * h);
            case 3 -> new DataBufferInt(w * h);
            default -> throw new IllegalArgumentException("Unsupported data type " + dataType);
        }, w, h, w, bandMasks, location);
    }

    public static WritableRaster createPackedRaster(int dataType, int w, int h, int bands, int bitsPerBand, Point location) {
        if (bands <= 0) {
            throw new IllegalArgumentException("Number of bands (" + bands + ") must be greater than 0");
        }
        if (bitsPerBand <= 0) {
            throw new IllegalArgumentException("Bits per band (" + bitsPerBand + ") must be greater than 0");
        }
        if (bands != 1) {
            int[] masks = new int[bands];
            int mask = (1 << bitsPerBand) - 1;
            int shift = (bands - 1) * bitsPerBand;
            if (shift + bitsPerBand > DataBuffer.getDataTypeSize(dataType)) {
                throw new IllegalArgumentException("bitsPerBand(" + bitsPerBand + ") * bands is  greater than data type size.");
            }
            switch (dataType) {
                case 0: 
                case 1: 
                case 3: {
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unsupported data type " + dataType);
                }
            }
            for (int i = 0; i < bands; ++i) {
                masks[i] = mask << shift;
                shift -= bitsPerBand;
            }
            return Raster.createPackedRaster(dataType, w, h, masks, location);
        }
        double fw = w;
        return Raster.createPackedRaster(switch (dataType) {
            case 0 -> new DataBufferByte((int)Math.ceil(fw / (double)(8 / bitsPerBand)) * h);
            case 1 -> new DataBufferUShort((int)Math.ceil(fw / (double)(16 / bitsPerBand)) * h);
            case 3 -> new DataBufferInt((int)Math.ceil(fw / (double)(32 / bitsPerBand)) * h);
            default -> throw new IllegalArgumentException("Unsupported data type " + dataType);
        }, w, h, bitsPerBand, location);
    }

    public static WritableRaster createInterleavedRaster(DataBuffer dataBuffer, int w, int h, int scanlineStride, int pixelStride, int[] bandOffsets, Point location) {
        if (dataBuffer == null) {
            throw new NullPointerException("DataBuffer cannot be null");
        }
        if (location == null) {
            location = new Point(0, 0);
        } else if ((double)w + location.getX() > 2.147483647E9 || (double)h + location.getY() > 2.147483647E9) {
            throw new RasterFormatException("location.x + w and location.y + h  cannot exceed Integer.MAX_VALUE");
        }
        int dataType = dataBuffer.getDataType();
        PixelInterleavedSampleModel csm = new PixelInterleavedSampleModel(dataType, w, h, pixelStride, scanlineStride, bandOffsets);
        switch (dataType) {
            case 0: {
                if (!(dataBuffer instanceof DataBufferByte)) break;
                return new ByteInterleavedRaster((SampleModel)csm, (DataBufferByte)dataBuffer, location);
            }
            case 1: {
                if (!(dataBuffer instanceof DataBufferUShort)) break;
                return new ShortInterleavedRaster((SampleModel)csm, (DataBufferUShort)dataBuffer, location);
            }
            default: {
                throw new IllegalArgumentException("Unsupported data type " + dataType);
            }
        }
        return new SunWritableRaster(csm, dataBuffer, location);
    }

    public static WritableRaster createBandedRaster(DataBuffer dataBuffer, int w, int h, int scanlineStride, int[] bankIndices, int[] bandOffsets, Point location) {
        if (dataBuffer == null) {
            throw new NullPointerException("DataBuffer cannot be null");
        }
        if (bankIndices == null) {
            throw new NullPointerException("bankIndices cannot be null");
        }
        if (bandOffsets == null) {
            throw new NullPointerException("bandOffsets cannot be null");
        }
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("Width (" + w + ") and height (" + h + ") must be > 0");
        }
        if (scanlineStride < 0) {
            throw new IllegalArgumentException("Scanline stride must be >= 0");
        }
        int bands = bankIndices.length;
        if (bandOffsets.length != bands) {
            throw new IllegalArgumentException("bankIndices.length != bandOffsets.length");
        }
        if (location == null) {
            location = new Point(0, 0);
        } else if ((double)w + location.getX() > 2.147483647E9 || (double)h + location.getY() > 2.147483647E9) {
            throw new RasterFormatException("location.x + w and location.y + h  cannot exceed Integer.MAX_VALUE");
        }
        int dataType = dataBuffer.getDataType();
        BandedSampleModel bsm = new BandedSampleModel(dataType, w, h, scanlineStride, bankIndices, bandOffsets);
        switch (dataType) {
            case 0: {
                if (!(dataBuffer instanceof DataBufferByte)) break;
                return new ByteBandedRaster((SampleModel)bsm, (DataBufferByte)dataBuffer, location);
            }
            case 1: {
                if (!(dataBuffer instanceof DataBufferUShort)) break;
                return new ShortBandedRaster((SampleModel)bsm, (DataBufferUShort)dataBuffer, location);
            }
            case 3: {
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported data type " + dataType);
            }
        }
        return new SunWritableRaster(bsm, dataBuffer, location);
    }

    public static WritableRaster createPackedRaster(DataBuffer dataBuffer, int w, int h, int scanlineStride, int[] bandMasks, Point location) {
        if (dataBuffer == null) {
            throw new NullPointerException("DataBuffer cannot be null");
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        int dataType = dataBuffer.getDataType();
        SinglePixelPackedSampleModel sppsm = new SinglePixelPackedSampleModel(dataType, w, h, scanlineStride, bandMasks);
        switch (dataType) {
            case 0: {
                if (!(dataBuffer instanceof DataBufferByte)) break;
                return new ByteInterleavedRaster((SampleModel)sppsm, (DataBufferByte)dataBuffer, location);
            }
            case 1: {
                if (!(dataBuffer instanceof DataBufferUShort)) break;
                return new ShortInterleavedRaster((SampleModel)sppsm, (DataBufferUShort)dataBuffer, location);
            }
            case 3: {
                if (!(dataBuffer instanceof DataBufferInt)) break;
                return new IntegerInterleavedRaster((SampleModel)sppsm, (DataBufferInt)dataBuffer, location);
            }
            default: {
                throw new IllegalArgumentException("Unsupported data type " + dataType);
            }
        }
        return new SunWritableRaster(sppsm, dataBuffer, location);
    }

    public static WritableRaster createPackedRaster(DataBuffer dataBuffer, int w, int h, int bitsPerPixel, Point location) {
        int dataType;
        if (dataBuffer == null) {
            throw new NullPointerException("DataBuffer cannot be null");
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        if ((dataType = dataBuffer.getDataType()) != 0 && dataType != 1 && dataType != 3) {
            throw new IllegalArgumentException("Unsupported data type " + dataType);
        }
        if (dataBuffer.getNumBanks() != 1) {
            throw new RasterFormatException("DataBuffer for packed Rasters must only have 1 bank.");
        }
        MultiPixelPackedSampleModel mppsm = new MultiPixelPackedSampleModel(dataType, w, h, bitsPerPixel);
        if (dataBuffer instanceof DataBufferByte && (bitsPerPixel == 1 || bitsPerPixel == 2 || bitsPerPixel == 4)) {
            return new BytePackedRaster((SampleModel)mppsm, (DataBufferByte)dataBuffer, location);
        }
        return new SunWritableRaster(mppsm, dataBuffer, location);
    }

    public static Raster createRaster(SampleModel sm, DataBuffer db, Point location) {
        if (sm == null || db == null) {
            throw new NullPointerException("SampleModel and DataBuffer cannot be null");
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        int dataType = sm.getDataType();
        if (sm instanceof PixelInterleavedSampleModel) {
            switch (dataType) {
                case 0: {
                    if (!(db instanceof DataBufferByte)) break;
                    return new ByteInterleavedRaster(sm, (DataBufferByte)db, location);
                }
                case 1: {
                    if (!(db instanceof DataBufferUShort)) break;
                    return new ShortInterleavedRaster(sm, (DataBufferUShort)db, location);
                }
            }
        } else if (sm instanceof SinglePixelPackedSampleModel) {
            switch (dataType) {
                case 0: {
                    if (!(db instanceof DataBufferByte)) break;
                    return new ByteInterleavedRaster(sm, (DataBufferByte)db, location);
                }
                case 1: {
                    if (!(db instanceof DataBufferUShort)) break;
                    return new ShortInterleavedRaster(sm, (DataBufferUShort)db, location);
                }
                case 3: {
                    if (!(db instanceof DataBufferInt)) break;
                    return new IntegerInterleavedRaster(sm, (DataBufferInt)db, location);
                }
            }
        } else if (sm instanceof MultiPixelPackedSampleModel && dataType == 0 && db instanceof DataBufferByte && sm.getSampleSize(0) < 8) {
            return new BytePackedRaster(sm, (DataBufferByte)db, location);
        }
        return new Raster(sm, db, location);
    }

    public static WritableRaster createWritableRaster(SampleModel sm, Point location) {
        if (location == null) {
            location = new Point(0, 0);
        }
        return Raster.createWritableRaster(sm, sm.createDataBuffer(), location);
    }

    public static WritableRaster createWritableRaster(SampleModel sm, DataBuffer db, Point location) {
        if (sm == null || db == null) {
            throw new NullPointerException("SampleModel and DataBuffer cannot be null");
        }
        if (location == null) {
            location = new Point(0, 0);
        }
        int dataType = sm.getDataType();
        if (sm instanceof PixelInterleavedSampleModel) {
            switch (dataType) {
                case 0: {
                    if (!(db instanceof DataBufferByte)) break;
                    return new ByteInterleavedRaster(sm, (DataBufferByte)db, location);
                }
                case 1: {
                    if (!(db instanceof DataBufferUShort)) break;
                    return new ShortInterleavedRaster(sm, (DataBufferUShort)db, location);
                }
            }
        } else if (sm instanceof SinglePixelPackedSampleModel) {
            switch (dataType) {
                case 0: {
                    if (!(db instanceof DataBufferByte)) break;
                    return new ByteInterleavedRaster(sm, (DataBufferByte)db, location);
                }
                case 1: {
                    if (!(db instanceof DataBufferUShort)) break;
                    return new ShortInterleavedRaster(sm, (DataBufferUShort)db, location);
                }
                case 3: {
                    if (!(db instanceof DataBufferInt)) break;
                    return new IntegerInterleavedRaster(sm, (DataBufferInt)db, location);
                }
            }
        } else if (sm instanceof MultiPixelPackedSampleModel && dataType == 0 && db instanceof DataBufferByte && sm.getSampleSize(0) < 8) {
            return new BytePackedRaster(sm, (DataBufferByte)db, location);
        }
        return new SunWritableRaster(sm, db, location);
    }

    protected Raster(SampleModel sampleModel, Point origin) {
        this(sampleModel, sampleModel.createDataBuffer(), new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
    }

    protected Raster(SampleModel sampleModel, DataBuffer dataBuffer, Point origin) {
        this(sampleModel, dataBuffer, new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
    }

    protected Raster(SampleModel sampleModel, DataBuffer dataBuffer, Rectangle aRegion, Point sampleModelTranslate, Raster parent) {
        if (sampleModel == null || dataBuffer == null || aRegion == null || sampleModelTranslate == null) {
            throw new NullPointerException("SampleModel, dataBuffer, aRegion and sampleModelTranslate cannot be null");
        }
        this.sampleModel = sampleModel;
        this.dataBuffer = dataBuffer;
        this.minX = aRegion.x;
        this.minY = aRegion.y;
        this.width = aRegion.width;
        this.height = aRegion.height;
        if (this.width <= 0 || this.height <= 0) {
            throw new RasterFormatException("negative or zero " + (this.width <= 0 ? "width" : "height"));
        }
        if (this.minX + this.width < this.minX) {
            throw new RasterFormatException("overflow condition for X coordinates of Raster");
        }
        if (this.minY + this.height < this.minY) {
            throw new RasterFormatException("overflow condition for Y coordinates of Raster");
        }
        this.sampleModelTranslateX = sampleModelTranslate.x;
        this.sampleModelTranslateY = sampleModelTranslate.y;
        this.numBands = sampleModel.getNumBands();
        this.numDataElements = sampleModel.getNumDataElements();
        this.parent = parent;
    }

    public Raster getParent() {
        return this.parent;
    }

    public final int getSampleModelTranslateX() {
        return this.sampleModelTranslateX;
    }

    public final int getSampleModelTranslateY() {
        return this.sampleModelTranslateY;
    }

    public WritableRaster createCompatibleWritableRaster() {
        return new SunWritableRaster(this.sampleModel, new Point(0, 0));
    }

    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        if (w <= 0 || h <= 0) {
            throw new RasterFormatException("negative " + (w <= 0 ? "width" : "height"));
        }
        SampleModel sm = this.sampleModel.createCompatibleSampleModel(w, h);
        return new SunWritableRaster(sm, new Point(0, 0));
    }

    public WritableRaster createCompatibleWritableRaster(Rectangle rect) {
        if (rect == null) {
            throw new NullPointerException("Rect cannot be null");
        }
        return this.createCompatibleWritableRaster(rect.x, rect.y, rect.width, rect.height);
    }

    public WritableRaster createCompatibleWritableRaster(int x, int y, int w, int h) {
        WritableRaster ret = this.createCompatibleWritableRaster(w, h);
        return ret.createWritableChild(0, 0, w, h, x, y, null);
    }

    public Raster createTranslatedChild(int childMinX, int childMinY) {
        return this.createChild(this.minX, this.minY, this.width, this.height, childMinX, childMinY, null);
    }

    public Raster createChild(int parentX, int parentY, int width, int height, int childMinX, int childMinY, int[] bandList) {
        if (parentX < this.minX) {
            throw new RasterFormatException("parentX lies outside raster");
        }
        if (parentY < this.minY) {
            throw new RasterFormatException("parentY lies outside raster");
        }
        if (parentX + width < parentX || parentX + width > this.width + this.minX) {
            throw new RasterFormatException("(parentX + width) is outside raster");
        }
        if (parentY + height < parentY || parentY + height > this.height + this.minY) {
            throw new RasterFormatException("(parentY + height) is outside raster");
        }
        SampleModel subSampleModel = bandList == null ? this.sampleModel : this.sampleModel.createSubsetSampleModel(bandList);
        int deltaX = childMinX - parentX;
        int deltaY = childMinY - parentY;
        return new Raster(subSampleModel, this.getDataBuffer(), new Rectangle(childMinX, childMinY, width, height), new Point(this.sampleModelTranslateX + deltaX, this.sampleModelTranslateY + deltaY), this);
    }

    public Rectangle getBounds() {
        return new Rectangle(this.minX, this.minY, this.width, this.height);
    }

    public final int getMinX() {
        return this.minX;
    }

    public final int getMinY() {
        return this.minY;
    }

    public final int getWidth() {
        return this.width;
    }

    public final int getHeight() {
        return this.height;
    }

    public final int getNumBands() {
        return this.numBands;
    }

    public final int getNumDataElements() {
        return this.sampleModel.getNumDataElements();
    }

    public final int getTransferType() {
        return this.sampleModel.getTransferType();
    }

    public DataBuffer getDataBuffer() {
        return this.dataBuffer;
    }

    public SampleModel getSampleModel() {
        return this.sampleModel;
    }

    public Object getDataElements(int x, int y, Object outData) {
        return this.sampleModel.getDataElements(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, outData, this.dataBuffer);
    }

    public Object getDataElements(int x, int y, int w, int h, Object outData) {
        return this.sampleModel.getDataElements(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, w, h, outData, this.dataBuffer);
    }

    public int[] getPixel(int x, int y, int[] iArray) {
        return this.sampleModel.getPixel(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, iArray, this.dataBuffer);
    }

    public float[] getPixel(int x, int y, float[] fArray) {
        return this.sampleModel.getPixel(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, fArray, this.dataBuffer);
    }

    public double[] getPixel(int x, int y, double[] dArray) {
        return this.sampleModel.getPixel(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, dArray, this.dataBuffer);
    }

    public int[] getPixels(int x, int y, int w, int h, int[] iArray) {
        return this.sampleModel.getPixels(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, w, h, iArray, this.dataBuffer);
    }

    public float[] getPixels(int x, int y, int w, int h, float[] fArray) {
        return this.sampleModel.getPixels(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, w, h, fArray, this.dataBuffer);
    }

    public double[] getPixels(int x, int y, int w, int h, double[] dArray) {
        return this.sampleModel.getPixels(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, w, h, dArray, this.dataBuffer);
    }

    public int getSample(int x, int y, int b) {
        return this.sampleModel.getSample(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, b, this.dataBuffer);
    }

    public float getSampleFloat(int x, int y, int b) {
        return this.sampleModel.getSampleFloat(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, b, this.dataBuffer);
    }

    public double getSampleDouble(int x, int y, int b) {
        return this.sampleModel.getSampleDouble(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, b, this.dataBuffer);
    }

    public int[] getSamples(int x, int y, int w, int h, int b, int[] iArray) {
        return this.sampleModel.getSamples(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, w, h, b, iArray, this.dataBuffer);
    }

    public float[] getSamples(int x, int y, int w, int h, int b, float[] fArray) {
        return this.sampleModel.getSamples(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, w, h, b, fArray, this.dataBuffer);
    }

    public double[] getSamples(int x, int y, int w, int h, int b, double[] dArray) {
        return this.sampleModel.getSamples(x - this.sampleModelTranslateX, y - this.sampleModelTranslateY, w, h, b, dArray, this.dataBuffer);
    }

    static {
        ColorModel.loadLibraries();
        Raster.initIDs();
    }
}

