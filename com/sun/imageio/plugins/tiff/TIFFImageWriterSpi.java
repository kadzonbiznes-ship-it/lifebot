/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.tiff;

import com.sun.imageio.plugins.tiff.TIFFImageWriter;
import java.util.Locale;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageOutputStream;

public class TIFFImageWriterSpi
extends ImageWriterSpi {
    private boolean registered = false;

    public TIFFImageWriterSpi() {
        super("Oracle Corporation", "1.0", new String[]{"tif", "TIF", "tiff", "TIFF"}, new String[]{"tif", "tiff"}, new String[]{"image/tiff"}, "com.sun.imageio.plugins.tiff.TIFFImageWriter", new Class[]{ImageOutputStream.class}, new String[]{"com.sun.imageio.plugins.tiff.TIFFImageReaderSpi"}, false, "javax_imageio_tiff_stream_1.0", "com.sun.imageio.plugins.tiff.TIFFStreamMetadataFormat", null, null, false, "javax_imageio_tiff_image_1.0", "com.sun.imageio.plugins.tiff.TIFFImageMetadataFormat", null, null);
    }

    @Override
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        return true;
    }

    @Override
    public String getDescription(Locale locale) {
        return "Standard TIFF image writer";
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) {
        return new TIFFImageWriter(this);
    }

    @Override
    public void onRegistration(ServiceRegistry registry, Class<?> category) {
        if (this.registered) {
            return;
        }
        this.registered = true;
    }
}

