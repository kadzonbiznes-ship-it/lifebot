/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.spi;

import java.io.File;
import java.io.IOException;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.stream.ImageOutputStream;

public abstract class ImageOutputStreamSpi
extends IIOServiceProvider {
    protected Class<?> outputClass;

    protected ImageOutputStreamSpi() {
    }

    public ImageOutputStreamSpi(String vendorName, String version, Class<?> outputClass) {
        super(vendorName, version);
        this.outputClass = outputClass;
    }

    public Class<?> getOutputClass() {
        return this.outputClass;
    }

    public boolean canUseCacheFile() {
        return false;
    }

    public boolean needsCacheFile() {
        return false;
    }

    public abstract ImageOutputStream createOutputStreamInstance(Object var1, boolean var2, File var3) throws IOException;

    public ImageOutputStream createOutputStreamInstance(Object output) throws IOException {
        return this.createOutputStreamInstance(output, true, null);
    }
}

