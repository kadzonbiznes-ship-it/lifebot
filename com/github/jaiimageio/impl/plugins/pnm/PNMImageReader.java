/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.jaiimageio.impl.plugins.pnm.I18N
 *  com.github.jaiimageio.impl.plugins.pnm.PNMMetadata
 */
package com.github.jaiimageio.impl.plugins.pnm;

import com.github.jaiimageio.impl.common.ImageUtil;
import com.github.jaiimageio.impl.plugins.pnm.I18N;
import com.github.jaiimageio.impl.plugins.pnm.PNMMetadata;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public class PNMImageReader
extends ImageReader {
    private static final int PBM_ASCII = 49;
    private static final int PGM_ASCII = 50;
    private static final int PPM_ASCII = 51;
    private static final int PBM_RAW = 52;
    private static final int PGM_RAW = 53;
    private static final int PPM_RAW = 54;
    private static final int LINE_FEED = 10;
    private static byte[] lineSeparator;
    private int variant;
    private int maxValue;
    private ImageInputStream iis = null;
    private boolean gotHeader = false;
    private long imageDataOffset;
    private int width;
    private int height;
    private String aLine;
    private StringTokenizer token;
    private PNMMetadata metadata;

    public PNMImageReader(ImageReaderSpi originator) {
        super(originator);
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        this.iis = (ImageInputStream)input;
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        return 1;
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        this.checkIndex(imageIndex);
        this.readHeader();
        return this.width;
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        this.checkIndex(imageIndex);
        this.readHeader();
        return this.height;
    }

    public int getVariant() {
        return this.variant;
    }

    public int getMaxValue() {
        return this.maxValue;
    }

    private void checkIndex(int imageIndex) {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(I18N.getString((String)"PNMImageReader1"));
        }
    }

    public synchronized void readHeader() throws IOException {
        if (this.gotHeader) {
            this.iis.seek(this.imageDataOffset);
            return;
        }
        if (this.iis != null) {
            if (this.iis.readByte() != 80) {
                throw new RuntimeException(I18N.getString((String)"PNMImageReader0"));
            }
            this.variant = this.iis.readByte();
            if (this.variant < 49 || this.variant > 54) {
                throw new RuntimeException(I18N.getString((String)"PNMImageReader0"));
            }
            this.metadata = new PNMMetadata();
            this.metadata.setVariant(this.variant);
            this.iis.readLine();
            this.readComments(this.iis, this.metadata);
            this.width = this.readInteger(this.iis);
            this.height = this.readInteger(this.iis);
            this.maxValue = this.variant == 49 || this.variant == 52 ? 1 : this.readInteger(this.iis);
            this.metadata.setWidth(this.width);
            this.metadata.setHeight(this.height);
            this.metadata.setMaxBitDepth(this.maxValue);
            this.gotHeader = true;
            this.imageDataOffset = this.iis.getStreamPosition();
        }
    }

    public Iterator getImageTypes(int imageIndex) throws IOException {
        this.checkIndex(imageIndex);
        this.readHeader();
        int tmp = (this.variant - 49) % 3;
        ArrayList<ImageTypeSpecifier> list = new ArrayList<ImageTypeSpecifier>(1);
        int dataType = 3;
        if (this.maxValue < 256) {
            dataType = 0;
        } else if (this.maxValue < 65536) {
            dataType = 1;
        }
        SampleModel sampleModel = null;
        ColorModel colorModel = null;
        if (this.variant == 49 || this.variant == 52) {
            sampleModel = new MultiPixelPackedSampleModel(0, this.width, this.height, 1);
            byte[] color = new byte[]{-1, 0};
            colorModel = new IndexColorModel(1, 2, color, color, color);
        } else {
            int[] nArray;
            int n = tmp == 1 ? 1 : 3;
            int n2 = this.width * (tmp == 1 ? 1 : 3);
            if (tmp == 1) {
                int[] nArray2 = new int[1];
                nArray = nArray2;
                nArray2[0] = 0;
            } else {
                int[] nArray3 = new int[3];
                nArray3[0] = 0;
                nArray3[1] = 1;
                nArray = nArray3;
                nArray3[2] = 2;
            }
            sampleModel = new PixelInterleavedSampleModel(dataType, this.width, this.height, n, n2, nArray);
            colorModel = ImageUtil.createColorModel(null, sampleModel);
        }
        list.add(new ImageTypeSpecifier(colorModel, sampleModel));
        return list.iterator();
    }

    @Override
    public ImageReadParam getDefaultReadParam() {
        return new ImageReadParam();
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        this.checkIndex(imageIndex);
        this.readHeader();
        return this.metadata;
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    @Override
    public boolean isRandomAccessEasy(int imageIndex) throws IOException {
        this.checkIndex(imageIndex);
        return true;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        BufferedImage bi;
        block77: {
            boolean noTransform;
            this.checkIndex(imageIndex);
            this.clearAbortRequest();
            this.processImageStarted(imageIndex);
            if (param == null) {
                param = this.getDefaultReadParam();
            }
            this.readHeader();
            Rectangle sourceRegion = new Rectangle(0, 0, 0, 0);
            Rectangle destinationRegion = new Rectangle(0, 0, 0, 0);
            PNMImageReader.computeRegions(param, this.width, this.height, param.getDestination(), sourceRegion, destinationRegion);
            int scaleX = param.getSourceXSubsampling();
            int scaleY = param.getSourceYSubsampling();
            int[] sourceBands = param.getSourceBands();
            int[] destBands = param.getDestinationBands();
            boolean seleBand = sourceBands != null && destBands != null;
            boolean bl = noTransform = destinationRegion.equals(new Rectangle(0, 0, this.width, this.height)) || seleBand;
            if (this.isRaw(this.variant) && this.maxValue >= 256) {
                this.maxValue = 255;
            }
            int numBands = 1;
            if (this.variant == 51 || this.variant == 54) {
                numBands = 3;
            }
            if (!seleBand) {
                sourceBands = new int[numBands];
                destBands = new int[numBands];
                for (int i = 0; i < numBands; ++i) {
                    destBands[i] = sourceBands[i] = i;
                }
            }
            int dataType = 3;
            if (this.maxValue < 256) {
                dataType = 0;
            } else if (this.maxValue < 65536) {
                dataType = 1;
            }
            SampleModel sampleModel = null;
            ColorModel colorModel = null;
            if (this.variant == 49 || this.variant == 52) {
                sampleModel = new MultiPixelPackedSampleModel(0, destinationRegion.width, destinationRegion.height, 1);
                byte[] color = new byte[]{-1, 0};
                colorModel = new IndexColorModel(1, 2, color, color, color);
            } else {
                sampleModel = new PixelInterleavedSampleModel(dataType, destinationRegion.width, destinationRegion.height, sourceBands.length, destinationRegion.width * sourceBands.length, destBands);
                colorModel = ImageUtil.createColorModel(null, sampleModel);
            }
            bi = param.getDestination();
            WritableRaster raster = null;
            if (bi == null) {
                sampleModel = sampleModel.createCompatibleSampleModel(destinationRegion.x + destinationRegion.width, destinationRegion.y + destinationRegion.height);
                if (seleBand) {
                    sampleModel = sampleModel.createSubsetSampleModel(sourceBands);
                }
                raster = Raster.createWritableRaster(sampleModel, new Point());
                bi = new BufferedImage(colorModel, raster, false, null);
            } else {
                raster = bi.getWritableTile(0, 0);
                sampleModel = bi.getSampleModel();
                colorModel = bi.getColorModel();
                noTransform &= destinationRegion.equals(raster.getBounds());
            }
            block0 : switch (this.variant) {
                case 52: {
                    DataBuffer dataBuffer = raster.getDataBuffer();
                    byte[] buf = ((DataBufferByte)dataBuffer).getData();
                    if (noTransform) {
                        this.iis.readFully(buf, 0, buf.length);
                        this.processImageUpdate(bi, 0, 0, this.width, this.height, 1, 1, destBands);
                        this.processImageProgress(100.0f);
                        break;
                    }
                    if (scaleX == 1 && sourceRegion.x % 8 == 0) {
                        int skip = sourceRegion.x >> 3;
                        int originalLS = this.width + 7 >> 3;
                        int destLS = raster.getWidth() + 7 >> 3;
                        int readLength = sourceRegion.width + 7 >> 3;
                        int offset = sourceRegion.y * originalLS;
                        this.iis.skipBytes(offset + skip);
                        offset = originalLS * (scaleY - 1) + originalLS - readLength;
                        byte[] lineData = new byte[readLength];
                        int bitoff = destinationRegion.x & 7;
                        boolean reformat = bitoff != 0;
                        int i = 0;
                        int j = 0;
                        int k = destinationRegion.y * destLS + (destinationRegion.x >> 3);
                        while (i < destinationRegion.height) {
                            if (reformat) {
                                this.iis.read(lineData, 0, readLength);
                                int mask1 = 255 << bitoff & 0xFF;
                                int mask2 = ~mask1 & 0xFF;
                                int shift = 8 - bitoff;
                                int n = 0;
                                int m = k;
                                while (n < readLength - 1) {
                                    buf[m] = (byte)((lineData[n] & mask2) << shift | (lineData[n + 1] & mask1) >> bitoff);
                                    ++n;
                                    ++m;
                                }
                                buf[m] = (byte)((lineData[n] & mask2) << shift);
                            } else {
                                this.iis.read(buf, k, readLength);
                            }
                            this.iis.skipBytes(offset);
                            k += destLS;
                            this.processImageUpdate(bi, 0, i, destinationRegion.width, 1, 1, 1, destBands);
                            this.processImageProgress(100.0f * (float)i / (float)destinationRegion.height);
                            ++i;
                            j += scaleY;
                        }
                    } else {
                        int originalLS = this.width + 7 >> 3;
                        byte[] data = new byte[originalLS];
                        this.iis.skipBytes(sourceRegion.y * originalLS);
                        int destLS = bi.getWidth() + 7 >> 3;
                        int offset = originalLS * (scaleY - 1);
                        int dsx = destLS * destinationRegion.y + (destinationRegion.x >> 3);
                        int i = 0;
                        int j = 0;
                        int n = dsx;
                        while (i < destinationRegion.height) {
                            this.iis.read(data, 0, originalLS);
                            this.iis.skipBytes(offset);
                            int b = 0;
                            int pos = 7 - (destinationRegion.x & 7);
                            for (int m = sourceRegion.x; m < sourceRegion.x + sourceRegion.width; m += scaleX) {
                                b |= (data[m >> 3] >> 7 - (m & 7) & 1) << pos;
                                if (--pos != -1) continue;
                                buf[n++] = (byte)b;
                                b = 0;
                                pos = 7;
                            }
                            if (pos != 7) {
                                buf[n++] = (byte)b;
                            }
                            n += destinationRegion.x >> 3;
                            this.processImageUpdate(bi, 0, i, destinationRegion.width, 1, 1, 1, destBands);
                            this.processImageProgress(100.0f * (float)i / (float)destinationRegion.height);
                            ++i;
                            j += scaleY;
                        }
                    }
                    break;
                }
                case 49: {
                    DataBuffer dataBuffer = raster.getDataBuffer();
                    byte[] buf = ((DataBufferByte)dataBuffer).getData();
                    if (noTransform) {
                        int n = 0;
                        for (int i = 0; i < this.height; ++i) {
                            int b = 0;
                            int pos = 7;
                            for (int j = 0; j < this.width; ++j) {
                                b |= (this.readInteger(this.iis) & 1) << pos;
                                if (--pos != -1) continue;
                                buf[n++] = (byte)b;
                                b = 0;
                                pos = 7;
                            }
                            if (pos != 7) {
                                buf[n++] = (byte)b;
                            }
                            this.processImageUpdate(bi, 0, i, this.width, 1, 1, 1, destBands);
                            this.processImageProgress(100.0f * (float)i / (float)this.height);
                        }
                    } else {
                        this.skipInteger(this.iis, sourceRegion.y * this.width + sourceRegion.x);
                        int skipX = scaleX - 1;
                        int skipY = (scaleY - 1) * this.width + this.width - destinationRegion.width * scaleX;
                        int dsx = (bi.getWidth() + 7 >> 3) * destinationRegion.y + (destinationRegion.x >> 3);
                        int n = dsx;
                        for (int i = 0; i < destinationRegion.height; ++i) {
                            int b = 0;
                            int pos = 7 - (destinationRegion.x & 7);
                            for (int j = 0; j < destinationRegion.width; ++j) {
                                b |= (this.readInteger(this.iis) & 1) << pos;
                                if (--pos == -1) {
                                    buf[n++] = (byte)b;
                                    b = 0;
                                    pos = 7;
                                }
                                this.skipInteger(this.iis, skipX);
                            }
                            if (pos != 7) {
                                buf[n++] = (byte)b;
                            }
                            n += destinationRegion.x >> 3;
                            this.skipInteger(this.iis, skipY);
                            this.processImageUpdate(bi, 0, i, destinationRegion.width, 1, 1, 1, destBands);
                            this.processImageProgress(100.0f * (float)i / (float)destinationRegion.height);
                        }
                    }
                    break;
                }
                case 50: 
                case 51: 
                case 53: 
                case 54: {
                    int skipX = (scaleX - 1) * numBands;
                    int skipY = (scaleY * this.width - destinationRegion.width * scaleX) * numBands;
                    int dsx = (bi.getWidth() * destinationRegion.y + destinationRegion.x) * numBands;
                    switch (dataType) {
                        case 0: {
                            DataBufferByte bbuf = (DataBufferByte)raster.getDataBuffer();
                            byte[] byteArray = bbuf.getData();
                            if (this.isRaw(this.variant)) {
                                if (noTransform) {
                                    this.iis.readFully(byteArray);
                                    this.processImageUpdate(bi, 0, 0, this.width, this.height, 1, 1, destBands);
                                    this.processImageProgress(100.0f);
                                    break block0;
                                }
                                this.iis.skipBytes(sourceRegion.y * this.width * numBands);
                                int skip = (scaleY - 1) * this.width * numBands;
                                byte[] data = new byte[this.width * numBands];
                                int pixelStride = scaleX * numBands;
                                int sx = sourceRegion.x * numBands;
                                int ex = this.width;
                                int n = dsx;
                                for (int i = 0; i < destinationRegion.height; ++i) {
                                    this.iis.read(data);
                                    int j = sourceRegion.x;
                                    int k = sx;
                                    while (j < sourceRegion.x + sourceRegion.width) {
                                        for (int m = 0; m < sourceBands.length; ++m) {
                                            byteArray[n + destBands[m]] = data[k + sourceBands[m]];
                                        }
                                        n += sourceBands.length;
                                        j += scaleX;
                                        k += pixelStride;
                                    }
                                    n += destinationRegion.x * numBands;
                                    this.iis.skipBytes(skip);
                                    this.processImageUpdate(bi, 0, i, destinationRegion.width, 1, 1, 1, destBands);
                                    this.processImageProgress(100.0f * (float)i / (float)destinationRegion.height);
                                }
                            } else {
                                this.skipInteger(this.iis, (sourceRegion.y * this.width + sourceRegion.x) * numBands);
                                if (seleBand) {
                                    byte[] data = new byte[numBands];
                                    int n = dsx;
                                    for (int i = 0; i < destinationRegion.height; ++i) {
                                        for (int j = 0; j < destinationRegion.width; ++j) {
                                            int k;
                                            for (k = 0; k < numBands; ++k) {
                                                data[k] = (byte)this.readInteger(this.iis);
                                            }
                                            for (k = 0; k < sourceBands.length; ++k) {
                                                byteArray[n + destBands[k]] = data[sourceBands[k]];
                                            }
                                            n += sourceBands.length;
                                            this.skipInteger(this.iis, skipX);
                                        }
                                        n += destinationRegion.x * sourceBands.length;
                                        this.skipInteger(this.iis, skipY);
                                        this.processImageUpdate(bi, 0, i, destinationRegion.width, 1, 1, 1, destBands);
                                        this.processImageProgress(100.0f * (float)i / (float)destinationRegion.height);
                                    }
                                } else {
                                    int n = dsx;
                                    for (int i = 0; i < destinationRegion.height; ++i) {
                                        for (int j = 0; j < destinationRegion.width; ++j) {
                                            for (int k = 0; k < numBands; ++k) {
                                                byteArray[n++] = (byte)this.readInteger(this.iis);
                                            }
                                            this.skipInteger(this.iis, skipX);
                                        }
                                        n += destinationRegion.x * sourceBands.length;
                                        this.skipInteger(this.iis, skipY);
                                        this.processImageUpdate(bi, 0, i, destinationRegion.width, 1, 1, 1, destBands);
                                        this.processImageProgress(100.0f * (float)i / (float)destinationRegion.height);
                                    }
                                }
                            }
                            break block77;
                        }
                        case 1: {
                            DataBufferUShort sbuf = (DataBufferUShort)raster.getDataBuffer();
                            short[] shortArray = sbuf.getData();
                            this.skipInteger(this.iis, sourceRegion.y * this.width * numBands + sourceRegion.x);
                            if (seleBand) {
                                short[] data = new short[numBands];
                                int n = dsx;
                                for (int i = 0; i < destinationRegion.height; ++i) {
                                    for (int j = 0; j < destinationRegion.width; ++j) {
                                        int k;
                                        for (k = 0; k < numBands; ++k) {
                                            data[k] = (short)this.readInteger(this.iis);
                                        }
                                        for (k = 0; k < sourceBands.length; ++k) {
                                            shortArray[n + destBands[k]] = data[sourceBands[k]];
                                        }
                                        n += sourceBands.length;
                                        this.skipInteger(this.iis, skipX);
                                    }
                                    n += destinationRegion.x * sourceBands.length;
                                    this.skipInteger(this.iis, skipY);
                                    this.processImageUpdate(bi, 0, i, destinationRegion.width, 1, 1, 1, destBands);
                                    this.processImageProgress(100.0f * (float)i / (float)destinationRegion.height);
                                }
                            } else {
                                int n = dsx;
                                for (int i = 0; i < destinationRegion.height; ++i) {
                                    for (int j = 0; j < destinationRegion.width; ++j) {
                                        for (int k = 0; k < numBands; ++k) {
                                            shortArray[n++] = (short)this.readInteger(this.iis);
                                        }
                                        this.skipInteger(this.iis, skipX);
                                    }
                                    n += destinationRegion.x * sourceBands.length;
                                    this.skipInteger(this.iis, skipY);
                                    this.processImageUpdate(bi, 0, i, destinationRegion.width, 1, 1, 1, destBands);
                                    this.processImageProgress(100.0f * (float)i / (float)destinationRegion.height);
                                }
                            }
                            break block77;
                        }
                        case 3: {
                            DataBufferInt ibuf = (DataBufferInt)raster.getDataBuffer();
                            int[] intArray = ibuf.getData();
                            this.skipInteger(this.iis, sourceRegion.y * this.width * numBands + sourceRegion.x);
                            if (seleBand) {
                                int[] data = new int[numBands];
                                int n = dsx;
                                for (int i = 0; i < destinationRegion.height; ++i) {
                                    for (int j = 0; j < destinationRegion.width; ++j) {
                                        int k;
                                        for (k = 0; k < numBands; ++k) {
                                            data[k] = this.readInteger(this.iis);
                                        }
                                        for (k = 0; k < sourceBands.length; ++k) {
                                            intArray[n + destBands[k]] = data[sourceBands[k]];
                                        }
                                        n += sourceBands.length;
                                        this.skipInteger(this.iis, skipX);
                                    }
                                    n += destinationRegion.x * sourceBands.length;
                                    this.skipInteger(this.iis, skipY);
                                    this.processImageUpdate(bi, 0, i, destinationRegion.width, 1, 1, 1, destBands);
                                    this.processImageProgress(100.0f * (float)i / (float)destinationRegion.height);
                                }
                            } else {
                                int n = dsx;
                                for (int i = 0; i < destinationRegion.height; ++i) {
                                    for (int j = 0; j < destinationRegion.width; ++j) {
                                        for (int k = 0; k < numBands; ++k) {
                                            intArray[n++] = this.readInteger(this.iis);
                                        }
                                        this.skipInteger(this.iis, skipX);
                                    }
                                    n += destinationRegion.x * sourceBands.length;
                                    this.skipInteger(this.iis, skipY);
                                    this.processImageUpdate(bi, 0, i, destinationRegion.width, 1, 1, 1, destBands);
                                    this.processImageProgress(100.0f * (float)i / (float)destinationRegion.height);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (this.abortRequested()) {
            this.processReadAborted();
        } else {
            this.processImageComplete();
        }
        return bi;
    }

    @Override
    public boolean canReadRaster() {
        return true;
    }

    @Override
    public Raster readRaster(int imageIndex, ImageReadParam param) throws IOException {
        BufferedImage bi = this.read(imageIndex, param);
        return bi.getData();
    }

    @Override
    public void reset() {
        super.reset();
        this.iis = null;
        this.gotHeader = false;
        System.gc();
    }

    private boolean isRaw(int v) {
        return v >= 52;
    }

    private void readComments(ImageInputStream stream, PNMMetadata metadata) throws IOException {
        String line = null;
        int pos = -1;
        stream.mark();
        while ((line = stream.readLine()) != null && (pos = line.indexOf("#")) >= 0) {
            metadata.addComment(line.substring(pos + 1).trim());
        }
        stream.reset();
    }

    private int readInteger(ImageInputStream stream) throws IOException {
        boolean foundDigit = false;
        while (this.aLine == null) {
            this.aLine = stream.readLine();
            if (this.aLine == null) {
                return 0;
            }
            int pos = this.aLine.indexOf("#");
            if (pos == 0) {
                this.aLine = null;
            } else if (pos > 0) {
                this.aLine = this.aLine.substring(0, pos - 1);
            }
            if (this.aLine == null) continue;
            this.token = new StringTokenizer(this.aLine);
        }
        while (this.token.hasMoreTokens()) {
            String s = this.token.nextToken();
            try {
                return new Integer(s);
            }
            catch (NumberFormatException e) {
            }
        }
        if (!foundDigit) {
            this.aLine = null;
            return this.readInteger(stream);
        }
        return 0;
    }

    private void skipInteger(ImageInputStream stream, int num) throws IOException {
        for (int i = 0; i < num; ++i) {
            this.readInteger(stream);
        }
    }

    static {
        if (lineSeparator == null) {
            lineSeparator = System.getProperty("line.separator").getBytes();
        }
    }
}

