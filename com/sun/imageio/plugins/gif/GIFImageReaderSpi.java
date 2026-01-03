/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.gif;

import com.sun.imageio.plugins.common.ReaderUtil;
import com.sun.imageio.plugins.gif.GIFImageReader;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public class GIFImageReaderSpi
extends ImageReaderSpi {
    private static final String vendorName = "Oracle Corporation";
    private static final String version = "1.0";
    private static final String[] names = new String[]{"gif", "GIF"};
    private static final String[] suffixes = new String[]{"gif"};
    private static final String[] MIMETypes = new String[]{"image/gif"};
    private static final String readerClassName = "com.sun.imageio.plugins.gif.GIFImageReader";
    private static final String[] writerSpiNames = new String[]{"com.sun.imageio.plugins.gif.GIFImageWriterSpi"};

    public GIFImageReaderSpi() {
        super(vendorName, version, names, suffixes, MIMETypes, readerClassName, new Class[]{ImageInputStream.class}, writerSpiNames, true, "javax_imageio_gif_stream_1.0", "com.sun.imageio.plugins.gif.GIFStreamMetadataFormat", null, null, true, "javax_imageio_gif_image_1.0", "com.sun.imageio.plugins.gif.GIFImageMetadataFormat", null, null);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Standard GIF image reader";
    }

    @Override
    public boolean canDecodeInput(Object input) throws IOException {
        if (!(input instanceof ImageInputStream)) {
            return false;
        }
        ImageInputStream stream = (ImageInputStream)input;
        byte[] b = new byte[6];
        stream.mark();
        boolean full = ReaderUtil.tryReadFully(stream, b);
        stream.reset();
        return full && b[0] == 71 && b[1] == 73 && b[2] == 70 && b[3] == 56 && (b[4] == 55 || b[4] == 57) && b[5] == 97;
    }

    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new GIFImageReader(this);
    }
}

