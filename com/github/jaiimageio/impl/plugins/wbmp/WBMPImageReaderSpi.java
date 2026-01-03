/*
 * Decompiled with CFR 0.152.
 */
package com.github.jaiimageio.impl.plugins.wbmp;

import com.github.jaiimageio.impl.common.ImageUtil;
import com.github.jaiimageio.impl.common.PackageUtil;
import com.github.jaiimageio.impl.plugins.wbmp.WBMPImageReader;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

public class WBMPImageReaderSpi
extends ImageReaderSpi {
    private static String[] writerSpiNames = new String[]{"com.github.jaiimageio.impl.plugins.wbmp.WBMPImageWriterSpi"};
    private static String[] formatNames = new String[]{"wbmp", "WBMP"};
    private static String[] entensions = new String[]{"wbmp"};
    private static String[] mimeType = new String[]{"image/vnd.wap.wbmp"};
    private boolean registered = false;

    public WBMPImageReaderSpi() {
        super(PackageUtil.getVendor(), PackageUtil.getVersion(), formatNames, entensions, mimeType, "com.github.jaiimageio.impl.plugins.wbmp.WBMPImageReader", STANDARD_INPUT_TYPE, writerSpiNames, true, null, null, null, null, true, "com_sun_media_imageio_plugins_wbmp_image_1.0", "com.github.jaiimageio.impl.plugins.wbmp.WBMPMetadataFormat", null, null);
    }

    public void onRegistration(ServiceRegistry registry, Class category) {
        if (this.registered) {
            return;
        }
        this.registered = true;
        ImageUtil.processOnRegistration(registry, category, "WBMP", this, 8, 7);
    }

    @Override
    public String getDescription(Locale locale) {
        String desc = PackageUtil.getSpecificationTitle() + " WBMP Image Reader";
        return desc;
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        if (!(source instanceof ImageInputStream)) {
            return false;
        }
        ImageInputStream stream = (ImageInputStream)source;
        stream.mark();
        byte type = stream.readByte();
        byte fixHeaderField = stream.readByte();
        int width = ImageUtil.readMultiByteInteger(stream);
        int height = ImageUtil.readMultiByteInteger(stream);
        long remainingBytes = stream.length() - stream.getStreamPosition();
        stream.reset();
        if (type != 0 || fixHeaderField != 0) {
            return false;
        }
        if (width <= 0 || height <= 0) {
            return false;
        }
        long scanSize = width / 8 + (width % 8 == 0 ? 0 : 1);
        return remainingBytes == scanSize * (long)height;
    }

    @Override
    public ImageReader createReaderInstance(Object extension) throws IIOException {
        return new WBMPImageReader(this);
    }
}

