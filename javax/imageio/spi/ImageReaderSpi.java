/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.spi;

import java.io.IOException;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.stream.ImageInputStream;

public abstract class ImageReaderSpi
extends ImageReaderWriterSpi {
    @Deprecated
    public static final Class<?>[] STANDARD_INPUT_TYPE = new Class[]{ImageInputStream.class};
    protected Class<?>[] inputTypes = null;
    protected String[] writerSpiNames = null;
    private Class<?> readerClass = null;

    protected ImageReaderSpi() {
    }

    public ImageReaderSpi(String vendorName, String version, String[] names, String[] suffixes, String[] MIMETypes, String readerClassName, Class<?>[] inputTypes, String[] writerSpiNames, boolean supportsStandardStreamMetadataFormat, String nativeStreamMetadataFormatName, String nativeStreamMetadataFormatClassName, String[] extraStreamMetadataFormatNames, String[] extraStreamMetadataFormatClassNames, boolean supportsStandardImageMetadataFormat, String nativeImageMetadataFormatName, String nativeImageMetadataFormatClassName, String[] extraImageMetadataFormatNames, String[] extraImageMetadataFormatClassNames) {
        super(vendorName, version, names, suffixes, MIMETypes, readerClassName, supportsStandardStreamMetadataFormat, nativeStreamMetadataFormatName, nativeStreamMetadataFormatClassName, extraStreamMetadataFormatNames, extraStreamMetadataFormatClassNames, supportsStandardImageMetadataFormat, nativeImageMetadataFormatName, nativeImageMetadataFormatClassName, extraImageMetadataFormatNames, extraImageMetadataFormatClassNames);
        if (inputTypes == null) {
            throw new IllegalArgumentException("inputTypes == null!");
        }
        if (inputTypes.length == 0) {
            throw new IllegalArgumentException("inputTypes.length == 0!");
        }
        if (inputTypes == STANDARD_INPUT_TYPE) {
            Class[] classArray = new Class[1];
            v1 = classArray;
            classArray[0] = ImageInputStream.class;
        } else {
            v1 = this.inputTypes = (Class[])inputTypes.clone();
        }
        if (writerSpiNames != null && writerSpiNames.length > 0) {
            this.writerSpiNames = (String[])writerSpiNames.clone();
        }
    }

    public Class<?>[] getInputTypes() {
        return (Class[])this.inputTypes.clone();
    }

    public abstract boolean canDecodeInput(Object var1) throws IOException;

    public ImageReader createReaderInstance() throws IOException {
        return this.createReaderInstance(null);
    }

    public abstract ImageReader createReaderInstance(Object var1) throws IOException;

    public boolean isOwnReader(ImageReader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("reader == null!");
        }
        String name = reader.getClass().getName();
        return name.equals(this.pluginClassName);
    }

    public String[] getImageWriterSpiNames() {
        return this.writerSpiNames == null ? null : (String[])this.writerSpiNames.clone();
    }
}

