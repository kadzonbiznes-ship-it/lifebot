/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.png;

import com.sun.imageio.plugins.png.PNGImageWriter;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.SampleModel;
import java.util.Locale;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public class PNGImageWriterSpi
extends ImageWriterSpi {
    private static final String vendorName = "Oracle Corporation";
    private static final String version = "1.0";
    private static final String[] names = new String[]{"png", "PNG"};
    private static final String[] suffixes = new String[]{"png"};
    private static final String[] MIMETypes = new String[]{"image/png", "image/x-png"};
    private static final String writerClassName = "com.sun.imageio.plugins.png.PNGImageWriter";
    private static final String[] readerSpiNames = new String[]{"com.sun.imageio.plugins.png.PNGImageReaderSpi"};

    public PNGImageWriterSpi() {
        super(vendorName, version, names, suffixes, MIMETypes, writerClassName, new Class[]{ImageOutputStream.class}, readerSpiNames, false, null, null, null, null, true, "javax_imageio_png_1.0", "com.sun.imageio.plugins.png.PNGMetadataFormat", null, null);
    }

    @Override
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        SampleModel sampleModel = type.getSampleModel();
        ColorModel colorModel = type.getColorModel();
        int[] sampleSize = sampleModel.getSampleSize();
        int bitDepth = sampleSize[0];
        for (int i = 1; i < sampleSize.length; ++i) {
            if (sampleSize[i] <= bitDepth) continue;
            bitDepth = sampleSize[i];
        }
        if (bitDepth < 1 || bitDepth > 16) {
            return false;
        }
        int numBands = sampleModel.getNumBands();
        if (numBands < 1 || numBands > 4) {
            return false;
        }
        boolean hasAlpha = colorModel.hasAlpha();
        if (colorModel instanceof IndexColorModel) {
            return true;
        }
        if ((numBands == 1 || numBands == 3) && hasAlpha) {
            return false;
        }
        return numBands != 2 && numBands != 4 || hasAlpha;
    }

    @Override
    public String getDescription(Locale locale) {
        return "Standard PNG image writer";
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) {
        return new PNGImageWriter(this);
    }
}

