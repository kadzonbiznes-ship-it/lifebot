/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.jpeg;

import com.sun.imageio.plugins.jpeg.JPEG;
import com.sun.imageio.plugins.jpeg.JPEGImageReader;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public class JPEGImageReaderSpi
extends ImageReaderSpi {
    private static String[] writerSpiNames = new String[]{"com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi"};

    public JPEGImageReaderSpi() {
        super("Oracle Corporation", "0.5", JPEG.names, JPEG.suffixes, JPEG.MIMETypes, "com.sun.imageio.plugins.jpeg.JPEGImageReader", new Class[]{ImageInputStream.class}, writerSpiNames, true, "javax_imageio_jpeg_stream_1.0", "com.sun.imageio.plugins.jpeg.JPEGStreamMetadataFormat", null, null, true, "javax_imageio_jpeg_image_1.0", "com.sun.imageio.plugins.jpeg.JPEGImageMetadataFormat", null, null);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Standard JPEG Image Reader";
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        if (!(source instanceof ImageInputStream)) {
            return false;
        }
        ImageInputStream iis = (ImageInputStream)source;
        iis.mark();
        int byte1 = iis.read();
        int byte2 = iis.read();
        iis.reset();
        return byte1 == 255 && byte2 == 216;
    }

    @Override
    public ImageReader createReaderInstance(Object extension) throws IIOException {
        return new JPEGImageReader(this);
    }
}

