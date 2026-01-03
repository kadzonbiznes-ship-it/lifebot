/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.spi;

import javax.imageio.ImageTranscoder;
import javax.imageio.spi.IIOServiceProvider;

public abstract class ImageTranscoderSpi
extends IIOServiceProvider {
    protected ImageTranscoderSpi() {
    }

    public ImageTranscoderSpi(String vendorName, String version) {
        super(vendorName, version);
    }

    public abstract String getReaderServiceProviderName();

    public abstract String getWriterServiceProviderName();

    public abstract ImageTranscoder createTranscoderInstance();
}

