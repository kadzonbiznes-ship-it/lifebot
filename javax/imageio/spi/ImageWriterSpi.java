/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.spi;

import java.awt.image.RenderedImage;
import java.io.IOException;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public abstract class ImageWriterSpi
extends ImageReaderWriterSpi {
    @Deprecated
    public static final Class<?>[] STANDARD_OUTPUT_TYPE = new Class[]{ImageOutputStream.class};
    protected Class<?>[] outputTypes = null;
    protected String[] readerSpiNames = null;
    private Class<?> writerClass = null;

    protected ImageWriterSpi() {
    }

    public ImageWriterSpi(String vendorName, String version, String[] names, String[] suffixes, String[] MIMETypes, String writerClassName, Class<?>[] outputTypes, String[] readerSpiNames, boolean supportsStandardStreamMetadataFormat, String nativeStreamMetadataFormatName, String nativeStreamMetadataFormatClassName, String[] extraStreamMetadataFormatNames, String[] extraStreamMetadataFormatClassNames, boolean supportsStandardImageMetadataFormat, String nativeImageMetadataFormatName, String nativeImageMetadataFormatClassName, String[] extraImageMetadataFormatNames, String[] extraImageMetadataFormatClassNames) {
        super(vendorName, version, names, suffixes, MIMETypes, writerClassName, supportsStandardStreamMetadataFormat, nativeStreamMetadataFormatName, nativeStreamMetadataFormatClassName, extraStreamMetadataFormatNames, extraStreamMetadataFormatClassNames, supportsStandardImageMetadataFormat, nativeImageMetadataFormatName, nativeImageMetadataFormatClassName, extraImageMetadataFormatNames, extraImageMetadataFormatClassNames);
        if (outputTypes == null) {
            throw new IllegalArgumentException("outputTypes == null!");
        }
        if (outputTypes.length == 0) {
            throw new IllegalArgumentException("outputTypes.length == 0!");
        }
        if (outputTypes == STANDARD_OUTPUT_TYPE) {
            Class[] classArray = new Class[1];
            v1 = classArray;
            classArray[0] = ImageOutputStream.class;
        } else {
            v1 = this.outputTypes = (Class[])outputTypes.clone();
        }
        if (readerSpiNames != null && readerSpiNames.length > 0) {
            this.readerSpiNames = (String[])readerSpiNames.clone();
        }
    }

    public boolean isFormatLossless() {
        return true;
    }

    public Class<?>[] getOutputTypes() {
        return (Class[])this.outputTypes.clone();
    }

    public abstract boolean canEncodeImage(ImageTypeSpecifier var1);

    public boolean canEncodeImage(RenderedImage im) {
        return this.canEncodeImage(ImageTypeSpecifier.createFromRenderedImage(im));
    }

    public ImageWriter createWriterInstance() throws IOException {
        return this.createWriterInstance(null);
    }

    public abstract ImageWriter createWriterInstance(Object var1) throws IOException;

    public boolean isOwnWriter(ImageWriter writer) {
        if (writer == null) {
            throw new IllegalArgumentException("writer == null!");
        }
        String name = writer.getClass().getName();
        return name.equals(this.pluginClassName);
    }

    public String[] getImageReaderSpiNames() {
        return this.readerSpiNames == null ? null : (String[])this.readerSpiNames.clone();
    }
}

