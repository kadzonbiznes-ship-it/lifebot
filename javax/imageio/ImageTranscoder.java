/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;

public interface ImageTranscoder {
    public IIOMetadata convertStreamMetadata(IIOMetadata var1, ImageWriteParam var2);

    public IIOMetadata convertImageMetadata(IIOMetadata var1, ImageTypeSpecifier var2, ImageWriteParam var3);
}

