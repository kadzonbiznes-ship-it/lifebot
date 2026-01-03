/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.wbmp;

import com.sun.imageio.plugins.wbmp.WBMPImageWriter;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.SampleModel;
import java.util.Locale;
import javax.imageio.IIOException;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageOutputStream;

public class WBMPImageWriterSpi
extends ImageWriterSpi {
    private static String[] readerSpiNames = new String[]{"com.sun.imageio.plugins.wbmp.WBMPImageReaderSpi"};
    private static String[] formatNames = new String[]{"wbmp", "WBMP"};
    private static String[] extensions = new String[]{"wbmp"};
    private static String[] mimeType = new String[]{"image/vnd.wap.wbmp"};
    private boolean registered = false;

    public WBMPImageWriterSpi() {
        super("Oracle Corporation", "1.0", formatNames, extensions, mimeType, "com.sun.imageio.plugins.wbmp.WBMPImageWriter", new Class[]{ImageOutputStream.class}, readerSpiNames, true, null, null, null, null, true, null, null, null, null);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Standard WBMP Image Writer";
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
        SampleModel sm = type.getSampleModel();
        if (!(sm instanceof MultiPixelPackedSampleModel)) {
            return false;
        }
        return sm.getSampleSize(0) == 1;
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) throws IIOException {
        return new WBMPImageWriter(this);
    }
}

