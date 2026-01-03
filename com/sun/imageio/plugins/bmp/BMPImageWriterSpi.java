/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.bmp;

import com.sun.imageio.plugins.bmp.BMPImageWriter;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.util.Locale;
import javax.imageio.IIOException;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageOutputStream;

public class BMPImageWriterSpi
extends ImageWriterSpi {
    private static String[] readerSpiNames = new String[]{"com.sun.imageio.plugins.bmp.BMPImageReaderSpi"};
    private static String[] formatNames = new String[]{"bmp", "BMP"};
    private static String[] extensions = new String[]{"bmp"};
    private static String[] mimeType = new String[]{"image/bmp"};
    private boolean registered = false;

    public BMPImageWriterSpi() {
        super("Oracle Corporation", "1.0", formatNames, extensions, mimeType, "com.sun.imageio.plugins.bmp.BMPImageWriter", new Class[]{ImageOutputStream.class}, readerSpiNames, false, null, null, null, null, true, "javax_imageio_bmp_1.0", "com.sun.imageio.plugins.bmp.BMPMetadataFormat", null, null);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Standard BMP Image Writer";
    }

    @Override
    public void onRegistration(ServiceRegistry registry, Class<?> category) {
        if (this.registered) {
            return;
        }
        this.registered = true;
    }

    @Override
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        int dataType = type.getSampleModel().getDataType();
        if (dataType < 0 || dataType > 3) {
            return false;
        }
        SampleModel sm = type.getSampleModel();
        int numBands = sm.getNumBands();
        if (numBands != 1 && numBands != 3) {
            return false;
        }
        if (numBands == 1 && dataType != 0) {
            return false;
        }
        return dataType <= 0 || sm instanceof SinglePixelPackedSampleModel;
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) throws IIOException {
        return new BMPImageWriter(this);
    }
}

