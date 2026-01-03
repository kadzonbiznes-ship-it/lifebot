/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.spi;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.stream.FileCacheImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class OutputStreamImageOutputStreamSpi
extends ImageOutputStreamSpi {
    private static final String vendorName = "Oracle Corporation";
    private static final String version = "1.0";
    private static final Class<?> outputClass = OutputStream.class;

    public OutputStreamImageOutputStreamSpi() {
        super(vendorName, version, outputClass);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Service provider that instantiates an OutputStreamImageOutputStream from an OutputStream";
    }

    @Override
    public boolean canUseCacheFile() {
        return true;
    }

    @Override
    public boolean needsCacheFile() {
        return false;
    }

    @Override
    public ImageOutputStream createOutputStreamInstance(Object output, boolean useCache, File cacheDir) throws IOException {
        if (output instanceof OutputStream) {
            OutputStream os = (OutputStream)output;
            if (useCache) {
                return new FileCacheImageOutputStream(os, cacheDir);
            }
            return new MemoryCacheImageOutputStream(os);
        }
        throw new IllegalArgumentException();
    }
}

