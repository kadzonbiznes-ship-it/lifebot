/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.spi;

import java.io.File;
import java.util.Locale;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

public class FileImageInputStreamSpi
extends ImageInputStreamSpi {
    private static final String vendorName = "Oracle Corporation";
    private static final String version = "1.0";
    private static final Class<?> inputClass = File.class;

    public FileImageInputStreamSpi() {
        super(vendorName, version, inputClass);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Service provider that instantiates a FileImageInputStream from a File";
    }

    @Override
    public ImageInputStream createInputStreamInstance(Object input, boolean useCache, File cacheDir) {
        if (input instanceof File) {
            try {
                return new FileImageInputStream((File)input);
            }
            catch (Exception e) {
                return null;
            }
        }
        throw new IllegalArgumentException();
    }
}

