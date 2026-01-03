/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.jaiimageio.impl.plugins.pnm.I18N
 *  com.github.jaiimageio.impl.plugins.pnm.PNMMetadata
 *  com.github.jaiimageio.plugins.pnm.PNMImageWriteParam
 */
package com.github.jaiimageio.impl.plugins.pnm;

import com.github.jaiimageio.impl.common.ImageUtil;
import com.github.jaiimageio.impl.plugins.pnm.I18N;
import com.github.jaiimageio.impl.plugins.pnm.PNMMetadata;
import com.github.jaiimageio.plugins.pnm.PNMImageWriteParam;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public class PNMImageWriter
extends ImageWriter {
    private static final int PBM_ASCII = 49;
    private static final int PGM_ASCII = 50;
    private static final int PPM_ASCII = 51;
    private static final int PBM_RAW = 52;
    private static final int PGM_RAW = 53;
    private static final int PPM_RAW = 54;
    private static final int SPACE = 32;
    private static final String COMMENT = "# written by com.github.jaiimageio.impl.PNMImageWriter";
    private static byte[] lineSeparator;
    private int variant;
    private int maxValue;
    private ImageOutputStream stream = null;

    public PNMImageWriter(ImageWriterSpi originator) {
        super(originator);
    }

    @Override
    public void setOutput(Object output) {
        super.setOutput(output);
        if (output != null) {
            if (!(output instanceof ImageOutputStream)) {
                throw new IllegalArgumentException(I18N.getString((String)"PNMImageWriter0"));
            }
            this.stream = (ImageOutputStream)output;
        } else {
            this.stream = null;
        }
    }

    @Override
    public ImageWriteParam getDefaultWriteParam() {
        return new PNMImageWriteParam();
    }

    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
        return new PNMMetadata(imageType, param);
    }

    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
        if (inData == null) {
            throw new IllegalArgumentException("inData == null!");
        }
        if (imageType == null) {
            throw new IllegalArgumentException("imageType == null!");
        }
        PNMMetadata outData = null;
        if (inData instanceof PNMMetadata) {
            outData = (PNMMetadata)((PNMMetadata)inData).clone();
        } else {
            try {
                outData = new PNMMetadata(inData);
            }
            catch (IIOInvalidTreeException e) {
                outData = new PNMMetadata();
            }
        }
        outData.initialize(imageType, param);
        return outData;
    }

    @Override
    public boolean canWriteRasters() {
        return true;
    }

    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
        boolean writeOptimal;
        boolean isPBMInverted;
        byte[] blues;
        byte[] greens;
        byte[] reds;
        int numBands;
        int[] sourceBands;
        int w;
        int scaleY;
        int scaleX;
        SampleModel sampleModel;
        Rectangle sourceRegion;
        boolean writeRaster;
        Raster inputRaster;
        RenderedImage input;
        block80: {
            ComponentSampleModel csm;
            ColorModel colorModel;
            block79: {
                ImageTypeSpecifier imageType;
                this.clearAbortRequest();
                this.processImageStarted(0);
                if (param == null) {
                    param = this.getDefaultWriteParam();
                }
                input = null;
                inputRaster = null;
                writeRaster = image.hasRaster();
                sourceRegion = param.getSourceRegion();
                sampleModel = null;
                colorModel = null;
                if (writeRaster) {
                    inputRaster = image.getRaster();
                    sampleModel = inputRaster.getSampleModel();
                    sourceRegion = sourceRegion == null ? inputRaster.getBounds() : sourceRegion.intersection(inputRaster.getBounds());
                } else {
                    input = image.getRenderedImage();
                    sampleModel = input.getSampleModel();
                    colorModel = input.getColorModel();
                    Rectangle rect = new Rectangle(input.getMinX(), input.getMinY(), input.getWidth(), input.getHeight());
                    sourceRegion = sourceRegion == null ? rect : sourceRegion.intersection(rect);
                }
                if (sourceRegion.isEmpty()) {
                    throw new RuntimeException(I18N.getString((String)"PNMImageWrite1"));
                }
                ImageUtil.canEncodeImage(this, colorModel, sampleModel);
                scaleX = param.getSourceXSubsampling();
                scaleY = param.getSourceYSubsampling();
                int xOffset = param.getSubsamplingXOffset();
                int yOffset = param.getSubsamplingYOffset();
                sourceRegion.translate(xOffset, yOffset);
                sourceRegion.width -= xOffset;
                sourceRegion.height -= yOffset;
                int minX = sourceRegion.x / scaleX;
                int minY = sourceRegion.y / scaleY;
                w = (sourceRegion.width + scaleX - 1) / scaleX;
                int h = (sourceRegion.height + scaleY - 1) / scaleY;
                Rectangle destinationRegion = new Rectangle(minX, minY, w, h);
                int tileHeight = sampleModel.getHeight();
                int tileWidth = sampleModel.getWidth();
                int[] sampleSize = sampleModel.getSampleSize();
                sourceBands = param.getSourceBands();
                boolean noSubband = true;
                numBands = sampleModel.getNumBands();
                if (sourceBands != null) {
                    sampleModel = sampleModel.createSubsetSampleModel(sourceBands);
                    colorModel = null;
                    noSubband = false;
                    numBands = sampleModel.getNumBands();
                } else {
                    sourceBands = new int[numBands];
                    for (int i = 0; i < numBands; ++i) {
                        sourceBands[i] = i;
                    }
                }
                reds = null;
                greens = null;
                blues = null;
                isPBMInverted = false;
                if (numBands == 1) {
                    if (colorModel instanceof IndexColorModel) {
                        IndexColorModel icm = (IndexColorModel)colorModel;
                        int mapSize = icm.getMapSize();
                        if (mapSize < 1 << sampleSize[0]) {
                            throw new RuntimeException(I18N.getString((String)"PNMImageWrite2"));
                        }
                        if (sampleSize[0] == 1) {
                            this.variant = 52;
                            isPBMInverted = icm.getRed(1) > icm.getRed(0);
                        } else {
                            this.variant = 54;
                            reds = new byte[mapSize];
                            greens = new byte[mapSize];
                            blues = new byte[mapSize];
                            icm.getReds(reds);
                            icm.getGreens(greens);
                            icm.getBlues(blues);
                        }
                    } else {
                        this.variant = sampleSize[0] == 1 ? 52 : (sampleSize[0] <= 8 ? 53 : 50);
                    }
                } else if (numBands == 3) {
                    this.variant = sampleSize[0] <= 8 && sampleSize[1] <= 8 && sampleSize[2] <= 8 ? 54 : 51;
                } else {
                    throw new RuntimeException(I18N.getString((String)"PNMImageWrite3"));
                }
                IIOMetadata inputMetadata = image.getMetadata();
                if (colorModel != null) {
                    imageType = new ImageTypeSpecifier(colorModel, sampleModel);
                } else {
                    int dataType = sampleModel.getDataType();
                    switch (numBands) {
                        case 1: {
                            imageType = ImageTypeSpecifier.createGrayscale(sampleSize[0], dataType, false);
                            break;
                        }
                        case 3: {
                            ColorSpace cs = ColorSpace.getInstance(1000);
                            imageType = ImageTypeSpecifier.createInterleaved(cs, new int[]{0, 1, 2}, dataType, false, false);
                            break;
                        }
                        default: {
                            throw new IIOException("Cannot encode image with " + numBands + " bands!");
                        }
                    }
                }
                PNMMetadata metadata = inputMetadata != null ? (PNMMetadata)this.convertImageMetadata(inputMetadata, imageType, param) : (PNMMetadata)this.getDefaultImageMetadata(imageType, param);
                boolean isRawPNM = param instanceof PNMImageWriteParam ? ((PNMImageWriteParam)param).getRaw() : metadata.isRaw();
                this.maxValue = metadata.getMaxValue();
                for (int i = 0; i < sampleSize.length; ++i) {
                    int v = (1 << sampleSize[i]) - 1;
                    if (v <= this.maxValue) continue;
                    this.maxValue = v;
                }
                if (isRawPNM) {
                    int maxBitDepth = metadata.getMaxBitDepth();
                    if (!this.isRaw(this.variant) && maxBitDepth <= 8) {
                        this.variant += 3;
                    } else if (this.isRaw(this.variant) && maxBitDepth > 8) {
                        this.variant -= 3;
                    }
                } else if (this.isRaw(this.variant)) {
                    this.variant -= 3;
                }
                this.stream.writeByte(80);
                this.stream.writeByte(this.variant);
                this.stream.write(lineSeparator);
                this.stream.write(COMMENT.getBytes());
                Iterator comments = metadata.getComments();
                if (comments != null) {
                    while (comments.hasNext()) {
                        this.stream.write(lineSeparator);
                        String comment = "# " + (String)comments.next();
                        this.stream.write(comment.getBytes());
                    }
                }
                this.stream.write(lineSeparator);
                this.writeInteger(this.stream, w);
                this.stream.write(32);
                this.writeInteger(this.stream, h);
                if (this.variant != 52 && this.variant != 49) {
                    this.stream.write(lineSeparator);
                    this.writeInteger(this.stream, this.maxValue);
                }
                if (this.variant == 52 || this.variant == 53 || this.variant == 54) {
                    this.stream.write(10);
                }
                writeOptimal = false;
                if (this.variant != 52 || sampleModel.getTransferType() != 0 || !(sampleModel instanceof MultiPixelPackedSampleModel)) break block79;
                MultiPixelPackedSampleModel mppsm = (MultiPixelPackedSampleModel)sampleModel;
                int originX = 0;
                originX = writeRaster ? inputRaster.getMinX() : input.getMinX();
                if (mppsm.getBitOffset((sourceRegion.x - originX) % tileWidth) != 0 || mppsm.getPixelBitStride() != 1 || scaleX != 1) break block80;
                writeOptimal = true;
                break block80;
            }
            if ((this.variant == 53 || this.variant == 54) && sampleModel instanceof ComponentSampleModel && !(colorModel instanceof IndexColorModel) && (csm = (ComponentSampleModel)sampleModel).getPixelStride() == numBands && scaleX == 1) {
                writeOptimal = true;
                if (this.variant == 54) {
                    int[] bandOffsets = csm.getBandOffsets();
                    for (int b = 0; b < numBands; ++b) {
                        if (bandOffsets[b] == b) continue;
                        writeOptimal = false;
                        break;
                    }
                }
            }
        }
        if (writeOptimal) {
            int bytesPerRow = this.variant == 52 ? (w + 7) / 8 : w * sampleModel.getNumBands();
            byte[] bdata = null;
            byte[] invertedData = new byte[bytesPerRow];
            for (int j = 0; j < sourceRegion.height && !this.abortRequested(); ++j) {
                Raster lineRaster = null;
                if (writeRaster) {
                    lineRaster = inputRaster.createChild(sourceRegion.x, j, sourceRegion.width, 1, 0, 0, null);
                } else {
                    lineRaster = input.getData(new Rectangle(sourceRegion.x, sourceRegion.y + j, w, 1));
                    lineRaster = lineRaster.createTranslatedChild(0, 0);
                }
                bdata = ((DataBufferByte)lineRaster.getDataBuffer()).getData();
                sampleModel = lineRaster.getSampleModel();
                int offset = 0;
                if (sampleModel instanceof ComponentSampleModel) {
                    offset = ((ComponentSampleModel)sampleModel).getOffset(lineRaster.getMinX() - lineRaster.getSampleModelTranslateX(), lineRaster.getMinY() - lineRaster.getSampleModelTranslateY());
                } else if (sampleModel instanceof MultiPixelPackedSampleModel) {
                    offset = ((MultiPixelPackedSampleModel)sampleModel).getOffset(lineRaster.getMinX() - lineRaster.getSampleModelTranslateX(), lineRaster.getMinX() - lineRaster.getSampleModelTranslateY());
                }
                if (isPBMInverted) {
                    int k = offset;
                    for (int m = 0; m < bytesPerRow; ++m) {
                        invertedData[m] = ~bdata[k];
                        ++k;
                    }
                    bdata = invertedData;
                    offset = 0;
                }
                this.stream.write(bdata, offset, bytesPerRow);
                this.processImageProgress(100.0f * (float)j / (float)sourceRegion.height);
            }
            this.stream.flush();
            if (this.abortRequested()) {
                this.processWriteAborted();
            } else {
                this.processImageComplete();
            }
            return;
        }
        int size = sourceRegion.width * numBands;
        int[] pixels = new int[size];
        byte[] bpixels = reds == null ? new byte[w * numBands] : new byte[w * 3];
        int count = 0;
        int lastRow = sourceRegion.y + sourceRegion.height;
        for (int row = sourceRegion.y; row < lastRow && !this.abortRequested(); row += scaleY) {
            int i;
            Raster src = null;
            src = writeRaster ? inputRaster.createChild(sourceRegion.x, row, sourceRegion.width, 1, sourceRegion.x, row, sourceBands) : input.getData(new Rectangle(sourceRegion.x, row, sourceRegion.width, 1));
            src.getPixels(sourceRegion.x, row, sourceRegion.width, 1, pixels);
            if (isPBMInverted) {
                for (i = 0; i < size; i += scaleX) {
                    int n = i;
                    bpixels[n] = (byte)(bpixels[n] ^ 1);
                }
            }
            switch (this.variant) {
                case 49: 
                case 50: {
                    for (i = 0; i < size; i += scaleX) {
                        if (count++ % 16 == 0) {
                            this.stream.write(lineSeparator);
                        } else {
                            this.stream.write(32);
                        }
                        this.writeInteger(this.stream, pixels[i]);
                    }
                    this.stream.write(lineSeparator);
                    break;
                }
                case 51: {
                    if (reds == null) {
                        for (i = 0; i < size; i += scaleX * numBands) {
                            for (int j = 0; j < numBands; ++j) {
                                if (count++ % 16 == 0) {
                                    this.stream.write(lineSeparator);
                                } else {
                                    this.stream.write(32);
                                }
                                this.writeInteger(this.stream, pixels[i + j]);
                            }
                        }
                    } else {
                        for (i = 0; i < size; i += scaleX) {
                            if (count++ % 5 == 0) {
                                this.stream.write(lineSeparator);
                            } else {
                                this.stream.write(32);
                            }
                            this.writeInteger(this.stream, reds[pixels[i]] & 0xFF);
                            this.stream.write(32);
                            this.writeInteger(this.stream, greens[pixels[i]] & 0xFF);
                            this.stream.write(32);
                            this.writeInteger(this.stream, blues[pixels[i]] & 0xFF);
                        }
                    }
                    this.stream.write(lineSeparator);
                    break;
                }
                case 52: {
                    int i2;
                    int kdst = 0;
                    boolean ksrc = false;
                    int b = 0;
                    int pos = 7;
                    for (i2 = 0; i2 < size; i2 += scaleX) {
                        b |= pixels[i2] << pos;
                        if (--pos != -1) continue;
                        bpixels[kdst++] = (byte)b;
                        b = 0;
                        pos = 7;
                    }
                    if (pos != 7) {
                        bpixels[kdst++] = (byte)b;
                    }
                    this.stream.write(bpixels, 0, kdst);
                    break;
                }
                case 53: {
                    int i2;
                    int j = 0;
                    for (i2 = 0; i2 < size; i2 += scaleX) {
                        bpixels[j++] = (byte)pixels[i2];
                    }
                    this.stream.write(bpixels, 0, w);
                    break;
                }
                case 54: {
                    int j;
                    int i2;
                    if (reds == null) {
                        int k = 0;
                        for (i2 = 0; i2 < size; i2 += scaleX * numBands) {
                            for (int j2 = 0; j2 < numBands; ++j2) {
                                bpixels[k++] = (byte)(pixels[i2 + j2] & 0xFF);
                            }
                        }
                    } else {
                        j = 0;
                        for (i2 = 0; i2 < size; i2 += scaleX) {
                            bpixels[j++] = reds[pixels[i2]];
                            bpixels[j++] = greens[pixels[i2]];
                            bpixels[j++] = blues[pixels[i2]];
                        }
                    }
                    this.stream.write(bpixels, 0, bpixels.length);
                }
            }
            this.processImageProgress(100.0f * (float)(row - sourceRegion.y) / (float)sourceRegion.height);
        }
        this.stream.flush();
        if (this.abortRequested()) {
            this.processWriteAborted();
        } else {
            this.processImageComplete();
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.stream = null;
    }

    private void writeInteger(ImageOutputStream output, int i) throws IOException {
        output.write(Integer.toString(i).getBytes());
    }

    private void writeByte(ImageOutputStream output, byte b) throws IOException {
        output.write(Byte.toString(b).getBytes());
    }

    private boolean isRaw(int v) {
        return v >= 52;
    }

    static {
        if (lineSeparator == null) {
            lineSeparator = System.getProperty("line.separator").getBytes();
        }
    }
}

