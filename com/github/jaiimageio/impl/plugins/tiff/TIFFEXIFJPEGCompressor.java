/*
 * Decompiled with CFR 0.152.
 */
package com.github.jaiimageio.impl.plugins.tiff;

import com.github.jaiimageio.impl.plugins.tiff.TIFFBaseJPEGCompressor;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;

public class TIFFEXIFJPEGCompressor
extends TIFFBaseJPEGCompressor {
    public TIFFEXIFJPEGCompressor(ImageWriteParam param) {
        super("EXIF JPEG", 6, false, param);
    }

    @Override
    public void setMetadata(IIOMetadata metadata) {
        super.setMetadata(metadata);
        this.initJPEGWriter(false, true);
    }
}

