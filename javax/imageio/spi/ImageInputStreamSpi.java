/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.spi;

import java.io.File;
import java.io.IOException;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.stream.ImageInputStream;

public abstract class ImageInputStreamSpi
extends IIOServiceProvider {
    protected Class<?> inputClass;

    protected ImageInputStreamSpi() {
    }

    public ImageInputStreamSpi(String vendorName, String version, Class<?> inputClass) {
        super(vendorName, version);
        this.inputClass = inputClass;
    }

    public Class<?> getInputClass() {
        return this.inputClass;
    }

    public boolean canUseCacheFile() {
        return false;
    }

    public boolean needsCacheFile() {
        return false;
    }

    public abstract ImageInputStream createInputStreamInstance(Object var1, boolean var2, File var3) throws IOException;

    public ImageInputStream createInputStreamInstance(Object input) throws IOException {
        return this.createInputStreamInstance(input, true, null);
    }
}

