/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.spi;

import java.io.File;
import java.util.Locale;
import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

public class FileImageOutputStreamSpi
extends ImageOutputStreamSpi {
    private static final String vendorName = "Oracle Corporation";
    private static final String version = "1.0";
    private static final Class<?> outputClass = File.class;

    public FileImageOutputStreamSpi() {
        super(vendorName, version, outputClass);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Service provider that instantiates a FileImageOutputStream from a File";
    }

    @Override
    public ImageOutputStream createOutputStreamInstance(Object output, boolean useCache, File cacheDir) {
        if (output instanceof File) {
            try {
                return new FileImageOutputStream((File)output);
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        throw new IllegalArgumentException();
    }
}

