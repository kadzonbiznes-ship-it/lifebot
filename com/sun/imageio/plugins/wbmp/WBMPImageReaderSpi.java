/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.wbmp;

import com.sun.imageio.plugins.wbmp.WBMPImageReader;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

public class WBMPImageReaderSpi
extends ImageReaderSpi {
    private static final int MAX_WBMP_WIDTH = 1024;
    private static final int MAX_WBMP_HEIGHT = 768;
    private static String[] writerSpiNames = new String[]{"com.sun.imageio.plugins.wbmp.WBMPImageWriterSpi"};
    private static String[] formatNames = new String[]{"wbmp", "WBMP"};
    private static String[] extensions = new String[]{"wbmp"};
    private static String[] mimeType = new String[]{"image/vnd.wap.wbmp"};
    private boolean registered = false;

    public WBMPImageReaderSpi() {
        super("Oracle Corporation", "1.0", formatNames, extensions, mimeType, "com.sun.imageio.plugins.wbmp.WBMPImageReader", new Class[]{ImageInputStream.class}, writerSpiNames, true, null, null, null, null, true, "javax_imageio_wbmp_1.0", "com.sun.imageio.plugins.wbmp.WBMPMetadataFormat", null, null);
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
        return "Standard WBMP Image Reader";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        if (!(source instanceof ImageInputStream)) {
            return false;
        }
        ImageInputStream stream = (ImageInputStream)source;
        stream.mark();
        try {
            int type = stream.read();
            int fixHeaderField = stream.read();
            if (type != 0 || fixHeaderField != 0) {
                boolean bl = false;
                return bl;
            }
            int width = WBMPImageReaderSpi.tryReadMultiByteInteger(stream);
            int height = WBMPImageReaderSpi.tryReadMultiByteInteger(stream);
            if (width <= 0 || height <= 0) {
                boolean bl = false;
                return bl;
            }
            long dataLength = stream.length();
            if (dataLength == -1L) {
                boolean bl = width < 1024 && height < 768;
                return bl;
            }
            long scanSize = width / 8 + (width % 8 == 0 ? 0 : 1);
            boolean bl = (dataLength -= stream.getStreamPosition()) == scanSize * (long)height;
            return bl;
        }
        finally {
            stream.reset();
        }
    }

    private static int tryReadMultiByteInteger(ImageInputStream stream) throws IOException {
        int value = stream.read();
        if (value < 0) {
            return -1;
        }
        int result = value & 0x7F;
        while ((value & 0x80) == 128) {
            if ((result & 0xFE000000) != 0) {
                return -1;
            }
            result <<= 7;
            value = stream.read();
            if (value < 0) {
                return -1;
            }
            result |= value & 0x7F;
        }
        return result;
    }

    @Override
    public ImageReader createReaderInstance(Object extension) throws IIOException {
        return new WBMPImageReader(this);
    }
}

