/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.jaiimageio.stream.FileChannelImageOutputStream
 */
package com.github.jaiimageio.impl.stream;

import com.github.jaiimageio.impl.common.PackageUtil;
import com.github.jaiimageio.stream.FileChannelImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.WritableByteChannel;
import java.util.Locale;
import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.stream.FileCacheImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class ChannelImageOutputStreamSpi
extends ImageOutputStreamSpi {
    public ChannelImageOutputStreamSpi() {
        super(PackageUtil.getVendor(), PackageUtil.getVersion(), WritableByteChannel.class);
    }

    @Override
    public ImageOutputStream createOutputStreamInstance(Object output, boolean useCache, File cacheDir) throws IOException {
        if (output == null || !(output instanceof WritableByteChannel)) {
            throw new IllegalArgumentException("Cannot create ImageOutputStream from " + output.getClass().getName());
        }
        ImageOutputStreamImpl stream = null;
        if (output instanceof FileChannel) {
            FileChannel channel = (FileChannel)output;
            try {
                channel.map(FileChannel.MapMode.READ_ONLY, channel.position(), 1L);
                stream = new FileChannelImageOutputStream((FileChannel)output);
            }
            catch (NonReadableChannelException nonReadableChannelException) {
                // empty catch block
            }
        }
        if (stream == null) {
            OutputStream outStream = Channels.newOutputStream((WritableByteChannel)output);
            if (useCache) {
                try {
                    stream = new FileCacheImageOutputStream(outStream, cacheDir);
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
            if (stream == null) {
                stream = new MemoryCacheImageOutputStream(outStream);
            }
        }
        return stream;
    }

    @Override
    public String getDescription(Locale locale) {
        return "NIO Channel ImageOutputStream";
    }
}

