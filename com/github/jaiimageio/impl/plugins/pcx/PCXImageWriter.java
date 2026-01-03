/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.jaiimageio.impl.plugins.pcx.PCXMetadata
 */
package com.github.jaiimageio.impl.plugins.pcx;

import com.github.jaiimageio.impl.common.ImageUtil;
import com.github.jaiimageio.impl.plugins.pcx.PCXConstants;
import com.github.jaiimageio.impl.plugins.pcx.PCXImageWriterSpi;
import com.github.jaiimageio.impl.plugins.pcx.PCXMetadata;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.nio.ByteOrder;
import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

public class PCXImageWriter
extends ImageWriter
implements PCXConstants {
    private ImageOutputStream ios;
    private Rectangle sourceRegion;
    private Rectangle destinationRegion;
    private int colorPlanes;
    private int bytesPerLine;
    private Raster inputRaster = null;
    private int scaleX;
    private int scaleY;

    public PCXImageWriter(PCXImageWriterSpi imageWriterSpi) {
        super(imageWriterSpi);
    }

    @Override
    public void setOutput(Object output) {
        super.setOutput(output);
        if (output != null) {
            if (!(output instanceof ImageOutputStream)) {
                throw new IllegalArgumentException("output not instance of ImageOutputStream");
            }
            this.ios = (ImageOutputStream)output;
            this.ios.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        } else {
            this.ios = null;
        }
    }

    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
        if (inData instanceof PCXMetadata) {
            return inData;
        }
        return null;
    }

    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
        PCXMetadata md = new PCXMetadata();
        md.bitsPerPixel = (byte)imageType.getSampleModel().getSampleSize()[0];
        return md;
    }

    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return null;
    }

    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
        if (this.ios == null) {
            throw new IllegalStateException("output stream is null");
        }
        if (image == null) {
            throw new IllegalArgumentException("image is null");
        }
        this.clearAbortRequest();
        this.processImageStarted(0);
        if (param == null) {
            param = this.getDefaultWriteParam();
        }
        boolean writeRaster = image.hasRaster();
        this.sourceRegion = param.getSourceRegion();
        SampleModel sampleModel = null;
        ColorModel colorModel = null;
        if (writeRaster) {
            this.inputRaster = image.getRaster();
            sampleModel = this.inputRaster.getSampleModel();
            colorModel = ImageUtil.createColorModel(null, sampleModel);
            this.sourceRegion = this.sourceRegion == null ? this.inputRaster.getBounds() : this.sourceRegion.intersection(this.inputRaster.getBounds());
        } else {
            RenderedImage input = image.getRenderedImage();
            this.inputRaster = input.getData();
            sampleModel = input.getSampleModel();
            colorModel = input.getColorModel();
            Rectangle rect = new Rectangle(input.getMinX(), input.getMinY(), input.getWidth(), input.getHeight());
            this.sourceRegion = this.sourceRegion == null ? rect : this.sourceRegion.intersection(rect);
        }
        if (this.sourceRegion.isEmpty()) {
            throw new IllegalArgumentException("source region is empty");
        }
        IIOMetadata imageMetadata = image.getMetadata();
        PCXMetadata pcxImageMetadata = null;
        ImageTypeSpecifier imageType = new ImageTypeSpecifier(colorModel, sampleModel);
        pcxImageMetadata = imageMetadata != null ? (PCXMetadata)this.convertImageMetadata(imageMetadata, imageType, param) : (PCXMetadata)this.getDefaultImageMetadata(imageType, param);
        this.scaleX = param.getSourceXSubsampling();
        this.scaleY = param.getSourceYSubsampling();
        int xOffset = param.getSubsamplingXOffset();
        int yOffset = param.getSubsamplingYOffset();
        int dataType = sampleModel.getDataType();
        this.sourceRegion.translate(xOffset, yOffset);
        this.sourceRegion.width -= xOffset;
        this.sourceRegion.height -= yOffset;
        int minX = this.sourceRegion.x / this.scaleX;
        int minY = this.sourceRegion.y / this.scaleY;
        int w = (this.sourceRegion.width + this.scaleX - 1) / this.scaleX;
        int h = (this.sourceRegion.height + this.scaleY - 1) / this.scaleY;
        xOffset = this.sourceRegion.x % this.scaleX;
        yOffset = this.sourceRegion.y % this.scaleY;
        this.destinationRegion = new Rectangle(minX, minY, w, h);
        boolean noTransform = this.destinationRegion.equals(this.sourceRegion);
        int[] sourceBands = param.getSourceBands();
        boolean noSubband = true;
        int numBands = sampleModel.getNumBands();
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
        this.ios.writeByte(10);
        this.ios.writeByte(5);
        this.ios.writeByte(1);
        int bitsPerPixel = sampleModel.getSampleSize(0);
        this.ios.writeByte(bitsPerPixel);
        this.ios.writeShort(this.destinationRegion.x);
        this.ios.writeShort(this.destinationRegion.y);
        this.ios.writeShort(this.destinationRegion.x + this.destinationRegion.width - 1);
        this.ios.writeShort(this.destinationRegion.y + this.destinationRegion.height - 1);
        this.ios.writeShort(pcxImageMetadata.hdpi);
        this.ios.writeShort(pcxImageMetadata.vdpi);
        byte[] smallpalette = this.createSmallPalette(colorModel);
        this.ios.write(smallpalette);
        this.ios.writeByte(0);
        this.colorPlanes = sampleModel.getNumBands();
        this.ios.writeByte(this.colorPlanes);
        this.bytesPerLine = this.destinationRegion.width * bitsPerPixel / 8;
        this.bytesPerLine += this.bytesPerLine % 2;
        this.ios.writeShort(this.bytesPerLine);
        if (colorModel.getColorSpace().getType() == 6) {
            this.ios.writeShort(2);
        } else {
            this.ios.writeShort(1);
        }
        this.ios.writeShort(pcxImageMetadata.hsize);
        this.ios.writeShort(pcxImageMetadata.vsize);
        for (int i = 0; i < 54; ++i) {
            this.ios.writeByte(0);
        }
        if (this.colorPlanes == 1 && bitsPerPixel == 1) {
            this.write1Bit();
        } else if (this.colorPlanes == 1 && bitsPerPixel == 4) {
            this.write4Bit();
        } else {
            this.write8Bit();
        }
        if (this.colorPlanes == 1 && bitsPerPixel == 8 && colorModel.getColorSpace().getType() != 6) {
            this.ios.writeByte(12);
            this.ios.write(this.createLargePalette(colorModel));
        }
        if (this.abortRequested()) {
            this.processWriteAborted();
        } else {
            this.processImageComplete();
        }
    }

    private void write4Bit() throws IOException {
        int[] unpacked = new int[this.sourceRegion.width];
        int[] samples = new int[this.bytesPerLine];
        for (int line = 0; line < this.sourceRegion.height; line += this.scaleY) {
            this.inputRaster.getSamples(this.sourceRegion.x, line + this.sourceRegion.y, this.sourceRegion.width, 1, 0, unpacked);
            int val = 0;
            int dst = 0;
            boolean nibble = false;
            for (int x = 0; x < this.sourceRegion.width; x += this.scaleX) {
                val |= unpacked[x] & 0xF;
                if (nibble) {
                    samples[dst++] = val;
                    nibble = false;
                    val = 0;
                    continue;
                }
                nibble = true;
                val <<= 4;
            }
            int last = samples[0];
            int count = 0;
            for (int x = 0; x < this.bytesPerLine; x += this.scaleX) {
                int sample = samples[x];
                if (sample != last || count == 63) {
                    this.writeRLE(last, count);
                    count = 1;
                    last = sample;
                    continue;
                }
                ++count;
            }
            if (count >= 1) {
                this.writeRLE(last, count);
            }
            this.processImageProgress(100.0f * (float)line / (float)this.sourceRegion.height);
        }
    }

    private void write1Bit() throws IOException {
        int[] unpacked = new int[this.sourceRegion.width];
        int[] samples = new int[this.bytesPerLine];
        for (int line = 0; line < this.sourceRegion.height; line += this.scaleY) {
            this.inputRaster.getSamples(this.sourceRegion.x, line + this.sourceRegion.y, this.sourceRegion.width, 1, 0, unpacked);
            int val = 0;
            int dst = 0;
            int bit = 128;
            for (int x = 0; x < this.sourceRegion.width; x += this.scaleX) {
                if (unpacked[x] > 0) {
                    val |= bit;
                }
                if (bit == 1) {
                    samples[dst++] = val;
                    bit = 128;
                    val = 0;
                    continue;
                }
                bit >>= 1;
            }
            int last = samples[0];
            int count = 0;
            for (int x = 0; x < this.bytesPerLine; x += this.scaleX) {
                int sample = samples[x];
                if (sample != last || count == 63) {
                    this.writeRLE(last, count);
                    count = 1;
                    last = sample;
                    continue;
                }
                ++count;
            }
            if (count >= 1) {
                this.writeRLE(last, count);
            }
            this.processImageProgress(100.0f * (float)line / (float)this.sourceRegion.height);
        }
    }

    private void write8Bit() throws IOException {
        int[][] samples = new int[this.colorPlanes][this.bytesPerLine];
        for (int line = 0; line < this.sourceRegion.height; line += this.scaleY) {
            for (int band = 0; band < this.colorPlanes; ++band) {
                this.inputRaster.getSamples(this.sourceRegion.x, line + this.sourceRegion.y, this.sourceRegion.width, 1, band, samples[band]);
            }
            int last = samples[0][0];
            int count = 0;
            for (int band = 0; band < this.colorPlanes; ++band) {
                for (int x = 0; x < this.bytesPerLine; x += this.scaleX) {
                    int sample = samples[band][x];
                    if (sample != last || count == 63) {
                        this.writeRLE(last, count);
                        count = 1;
                        last = sample;
                        continue;
                    }
                    ++count;
                }
            }
            if (count >= 1) {
                this.writeRLE(last, count);
            }
            this.processImageProgress(100.0f * (float)line / (float)this.sourceRegion.height);
        }
    }

    private void writeRLE(int val, int count) throws IOException {
        if (count == 1 && (val & 0xC0) != 192) {
            this.ios.writeByte(val);
        } else {
            this.ios.writeByte(0xC0 | count);
            this.ios.writeByte(val);
        }
    }

    private byte[] createSmallPalette(ColorModel cm) {
        byte[] palette = new byte[48];
        if (!(cm instanceof IndexColorModel)) {
            return palette;
        }
        IndexColorModel icm = (IndexColorModel)cm;
        if (icm.getMapSize() > 16) {
            return palette;
        }
        int offset = 0;
        for (int i = 0; i < icm.getMapSize(); ++i) {
            palette[offset++] = (byte)icm.getRed(i);
            palette[offset++] = (byte)icm.getGreen(i);
            palette[offset++] = (byte)icm.getBlue(i);
        }
        return palette;
    }

    private byte[] createLargePalette(ColorModel cm) {
        byte[] palette = new byte[768];
        if (!(cm instanceof IndexColorModel)) {
            return palette;
        }
        IndexColorModel icm = (IndexColorModel)cm;
        int offset = 0;
        for (int i = 0; i < icm.getMapSize(); ++i) {
            palette[offset++] = (byte)icm.getRed(i);
            palette[offset++] = (byte)icm.getGreen(i);
            palette[offset++] = (byte)icm.getBlue(i);
        }
        return palette;
    }
}

