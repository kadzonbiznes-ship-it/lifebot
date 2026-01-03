/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.jpeg;

import com.sun.imageio.plugins.jpeg.JPEG;
import com.sun.imageio.plugins.jpeg.JPEGImageWriter;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.util.Locale;
import javax.imageio.IIOException;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public class JPEGImageWriterSpi
extends ImageWriterSpi {
    private static String[] readerSpiNames = new String[]{"com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi"};

    public JPEGImageWriterSpi() {
        super("Oracle Corporation", "0.5", JPEG.names, JPEG.suffixes, JPEG.MIMETypes, "com.sun.imageio.plugins.jpeg.JPEGImageWriter", new Class[]{ImageOutputStream.class}, readerSpiNames, true, "javax_imageio_jpeg_stream_1.0", "com.sun.imageio.plugins.jpeg.JPEGStreamMetadataFormat", null, null, true, "javax_imageio_jpeg_image_1.0", "com.sun.imageio.plugins.jpeg.JPEGImageMetadataFormat", null, null);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Standard JPEG Image Writer";
    }

    @Override
    public boolean isFormatLossless() {
        return false;
    }

    @Override
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        SampleModel sampleModel = type.getSampleModel();
        ColorModel cm = type.getColorModel();
        if (cm.hasAlpha()) {
            return false;
        }
        int[] sampleSize = sampleModel.getSampleSize();
        int bitDepth = sampleSize[0];
        for (int i = 1; i < sampleSize.length; ++i) {
            if (sampleSize[i] <= bitDepth) continue;
            bitDepth = sampleSize[i];
        }
        return bitDepth >= 1 && bitDepth <= 8;
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) throws IIOException {
        return new JPEGImageWriter(this);
    }
}

