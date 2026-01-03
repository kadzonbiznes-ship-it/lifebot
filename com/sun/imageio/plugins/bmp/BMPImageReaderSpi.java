/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.bmp;

import com.sun.imageio.plugins.bmp.BMPImageReader;
import com.sun.imageio.plugins.common.ReaderUtil;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

public class BMPImageReaderSpi
extends ImageReaderSpi {
    private static String[] writerSpiNames = new String[]{"com.sun.imageio.plugins.bmp.BMPImageWriterSpi"};
    private static String[] formatNames = new String[]{"bmp", "BMP"};
    private static String[] extensions = new String[]{"bmp"};
    private static String[] mimeType = new String[]{"image/bmp"};
    private boolean registered = false;

    public BMPImageReaderSpi() {
        super("Oracle Corporation", "1.0", formatNames, extensions, mimeType, "com.sun.imageio.plugins.bmp.BMPImageReader", new Class[]{ImageInputStream.class}, writerSpiNames, false, null, null, null, null, true, "javax_imageio_bmp_1.0", "com.sun.imageio.plugins.bmp.BMPMetadataFormat", null, null);
    }

    @Override
    public void onRegistration(ServiceRegistry registry, Class<?> category) {
        if (this.registered) {
            return;
        }
        this.registered = true;
    }

    @Override
    public String getDescription(Locale locale) {
        return "Standard BMP Image Reader";
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        if (!(source instanceof ImageInputStream)) {
            return false;
        }
        ImageInputStream stream = (ImageInputStream)source;
        byte[] b = new byte[2];
        stream.mark();
        boolean full = ReaderUtil.tryReadFully(stream, b);
        stream.reset();
        return full && b[0] == 66 && b[1] == 77;
    }

    @Override
    public ImageReader createReaderInstance(Object extension) throws IIOException {
        return new BMPImageReader(this);
    }
}

