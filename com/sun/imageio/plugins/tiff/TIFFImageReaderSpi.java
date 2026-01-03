/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.tiff;

import com.sun.imageio.plugins.common.ReaderUtil;
import com.sun.imageio.plugins.tiff.TIFFImageReader;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

public class TIFFImageReaderSpi
extends ImageReaderSpi {
    private boolean registered = false;

    public TIFFImageReaderSpi() {
        super("Oracle Corporation", "1.0", new String[]{"tif", "TIF", "tiff", "TIFF"}, new String[]{"tif", "tiff"}, new String[]{"image/tiff"}, "com.sun.imageio.plugins.tiff.TIFFImageReader", new Class[]{ImageInputStream.class}, new String[]{"com.sun.imageio.plugins.tiff.TIFFImageWriterSpi"}, false, "javax_imageio_tiff_stream_1.0", "com.sun.imageio.plugins.tiff.TIFFStreamMetadataFormat", null, null, true, "javax_imageio_tiff_image_1.0", "com.sun.imageio.plugins.tiff.TIFFImageMetadataFormat", null, null);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Standard TIFF image reader";
    }

    @Override
    public boolean canDecodeInput(Object input) throws IOException {
        if (!(input instanceof ImageInputStream)) {
            return false;
        }
        ImageInputStream stream = (ImageInputStream)input;
        byte[] b = new byte[4];
        stream.mark();
        boolean full = ReaderUtil.tryReadFully(stream, b);
        stream.reset();
        return full && (b[0] == 73 && b[1] == 73 && b[2] == 42 && b[3] == 0 || b[0] == 77 && b[1] == 77 && b[2] == 0 && b[3] == 42);
    }

    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new TIFFImageReader(this);
    }

    @Override
    public void onRegistration(ServiceRegistry registry, Class<?> category) {
        if (this.registered) {
            return;
        }
        this.registered = true;
    }
}

