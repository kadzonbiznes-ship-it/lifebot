/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.spi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

public class InputStreamImageInputStreamSpi
extends ImageInputStreamSpi {
    private static final String vendorName = "Oracle Corporation";
    private static final String version = "1.0";
    private static final Class<?> inputClass = InputStream.class;

    public InputStreamImageInputStreamSpi() {
        super(vendorName, version, inputClass);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Service provider that instantiates a FileCacheImageInputStream or MemoryCacheImageInputStream from an InputStream";
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
    public ImageInputStream createInputStreamInstance(Object input, boolean useCache, File cacheDir) throws IOException {
        if (input instanceof InputStream) {
            InputStream is = (InputStream)input;
            if (useCache) {
                return new FileCacheImageInputStream(is, cacheDir);
            }
            return new MemoryCacheImageInputStream(is);
        }
        throw new IllegalArgumentException();
    }
}

