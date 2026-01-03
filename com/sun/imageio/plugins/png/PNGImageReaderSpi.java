/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.png;

import com.sun.imageio.plugins.common.ReaderUtil;
import com.sun.imageio.plugins.png.PNGImageReader;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public class PNGImageReaderSpi
extends ImageReaderSpi {
    private static final String vendorName = "Oracle Corporation";
    private static final String version = "1.0";
    private static final String[] names = new String[]{"png", "PNG"};
    private static final String[] suffixes = new String[]{"png"};
    private static final String[] MIMETypes = new String[]{"image/png", "image/x-png"};
    private static final String readerClassName = "com.sun.imageio.plugins.png.PNGImageReader";
    private static final String[] writerSpiNames = new String[]{"com.sun.imageio.plugins.png.PNGImageWriterSpi"};

    public PNGImageReaderSpi() {
        super(vendorName, version, names, suffixes, MIMETypes, readerClassName, new Class[]{ImageInputStream.class}, writerSpiNames, false, null, null, null, null, true, "javax_imageio_png_1.0", "com.sun.imageio.plugins.png.PNGMetadataFormat", null, null);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Standard PNG image reader";
    }

    @Override
    public boolean canDecodeInput(Object input) throws IOException {
        if (!(input instanceof ImageInputStream)) {
            return false;
        }
        ImageInputStream stream = (ImageInputStream)input;
        byte[] b = new byte[8];
        stream.mark();
        boolean full = ReaderUtil.tryReadFully(stream, b);
        stream.reset();
        return full && b[0] == -119 && b[1] == 80 && b[2] == 78 && b[3] == 71 && b[4] == 13 && b[5] == 10 && b[6] == 26 && b[7] == 10;
    }

    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new PNGImageReader(this);
    }
}

