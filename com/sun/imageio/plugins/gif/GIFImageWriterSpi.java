/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.gif;

import com.sun.imageio.plugins.common.PaletteBuilder;
import com.sun.imageio.plugins.gif.GIFImageWriter;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.util.Locale;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public class GIFImageWriterSpi
extends ImageWriterSpi {
    private static final String vendorName = "Oracle Corporation";
    private static final String version = "1.0";
    private static final String[] names = new String[]{"gif", "GIF"};
    private static final String[] suffixes = new String[]{"gif"};
    private static final String[] MIMETypes = new String[]{"image/gif"};
    private static final String writerClassName = "com.sun.imageio.plugins.gif.GIFImageWriter";
    private static final String[] readerSpiNames = new String[]{"com.sun.imageio.plugins.gif.GIFImageReaderSpi"};

    public GIFImageWriterSpi() {
        super(vendorName, version, names, suffixes, MIMETypes, writerClassName, new Class[]{ImageOutputStream.class}, readerSpiNames, true, "javax_imageio_gif_stream_1.0", "com.sun.imageio.plugins.gif.GIFStreamMetadataFormat", null, null, true, "javax_imageio_gif_image_1.0", "com.sun.imageio.plugins.gif.GIFImageMetadataFormat", null, null);
    }

    @Override
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        boolean canEncode;
        if (type == null) {
            throw new IllegalArgumentException("type == null!");
        }
        SampleModel sm = type.getSampleModel();
        ColorModel cm = type.getColorModel();
        boolean bl = canEncode = sm.getNumBands() == 1 && sm.getSampleSize(0) <= 8 && sm.getWidth() <= 65535 && sm.getHeight() <= 65535 && (cm == null || cm.getComponentSize()[0] <= 8);
        if (canEncode) {
            return true;
        }
        return PaletteBuilder.canCreatePalette(type);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Standard GIF image writer";
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) {
        return new GIFImageWriter(this);
    }
}

