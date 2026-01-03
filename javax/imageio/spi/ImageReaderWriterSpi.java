/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.spi;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.spi.IIOServiceProvider;

public abstract class ImageReaderWriterSpi
extends IIOServiceProvider {
    protected String[] names = null;
    protected String[] suffixes = null;
    protected String[] MIMETypes = null;
    protected String pluginClassName = null;
    protected boolean supportsStandardStreamMetadataFormat = false;
    protected String nativeStreamMetadataFormatName = null;
    protected String nativeStreamMetadataFormatClassName = null;
    protected String[] extraStreamMetadataFormatNames = null;
    protected String[] extraStreamMetadataFormatClassNames = null;
    protected boolean supportsStandardImageMetadataFormat = false;
    protected String nativeImageMetadataFormatName = null;
    protected String nativeImageMetadataFormatClassName = null;
    protected String[] extraImageMetadataFormatNames = null;
    protected String[] extraImageMetadataFormatClassNames = null;

    public ImageReaderWriterSpi(String vendorName, String version, String[] names, String[] suffixes, String[] MIMETypes, String pluginClassName, boolean supportsStandardStreamMetadataFormat, String nativeStreamMetadataFormatName, String nativeStreamMetadataFormatClassName, String[] extraStreamMetadataFormatNames, String[] extraStreamMetadataFormatClassNames, boolean supportsStandardImageMetadataFormat, String nativeImageMetadataFormatName, String nativeImageMetadataFormatClassName, String[] extraImageMetadataFormatNames, String[] extraImageMetadataFormatClassNames) {
        super(vendorName, version);
        if (names == null) {
            throw new IllegalArgumentException("names == null!");
        }
        if (names.length == 0) {
            throw new IllegalArgumentException("names.length == 0!");
        }
        if (pluginClassName == null) {
            throw new IllegalArgumentException("pluginClassName == null!");
        }
        this.names = (String[])names.clone();
        if (suffixes != null && suffixes.length > 0) {
            this.suffixes = (String[])suffixes.clone();
        }
        if (MIMETypes != null && MIMETypes.length > 0) {
            this.MIMETypes = (String[])MIMETypes.clone();
        }
        this.pluginClassName = pluginClassName;
        this.supportsStandardStreamMetadataFormat = supportsStandardStreamMetadataFormat;
        this.nativeStreamMetadataFormatName = nativeStreamMetadataFormatName;
        this.nativeStreamMetadataFormatClassName = nativeStreamMetadataFormatClassName;
        if (extraStreamMetadataFormatNames != null && extraStreamMetadataFormatNames.length > 0) {
            this.extraStreamMetadataFormatNames = (String[])extraStreamMetadataFormatNames.clone();
        }
        if (extraStreamMetadataFormatClassNames != null && extraStreamMetadataFormatClassNames.length > 0) {
            this.extraStreamMetadataFormatClassNames = (String[])extraStreamMetadataFormatClassNames.clone();
        }
        this.supportsStandardImageMetadataFormat = supportsStandardImageMetadataFormat;
        this.nativeImageMetadataFormatName = nativeImageMetadataFormatName;
        this.nativeImageMetadataFormatClassName = nativeImageMetadataFormatClassName;
        if (extraImageMetadataFormatNames != null && extraImageMetadataFormatNames.length > 0) {
            this.extraImageMetadataFormatNames = (String[])extraImageMetadataFormatNames.clone();
        }
        if (extraImageMetadataFormatClassNames != null && extraImageMetadataFormatClassNames.length > 0) {
            this.extraImageMetadataFormatClassNames = (String[])extraImageMetadataFormatClassNames.clone();
        }
    }

    public ImageReaderWriterSpi() {
    }

    public String[] getFormatNames() {
        return (String[])this.names.clone();
    }

    public String[] getFileSuffixes() {
        return this.suffixes == null ? null : (String[])this.suffixes.clone();
    }

    public String[] getMIMETypes() {
        return this.MIMETypes == null ? null : (String[])this.MIMETypes.clone();
    }

    public String getPluginClassName() {
        return this.pluginClassName;
    }

    public boolean isStandardStreamMetadataFormatSupported() {
        return this.supportsStandardStreamMetadataFormat;
    }

    public String getNativeStreamMetadataFormatName() {
        return this.nativeStreamMetadataFormatName;
    }

    public String[] getExtraStreamMetadataFormatNames() {
        return this.extraStreamMetadataFormatNames == null ? null : (String[])this.extraStreamMetadataFormatNames.clone();
    }

    public boolean isStandardImageMetadataFormatSupported() {
        return this.supportsStandardImageMetadataFormat;
    }

    public String getNativeImageMetadataFormatName() {
        return this.nativeImageMetadataFormatName;
    }

    public String[] getExtraImageMetadataFormatNames() {
        return this.extraImageMetadataFormatNames == null ? null : (String[])this.extraImageMetadataFormatNames.clone();
    }

    public IIOMetadataFormat getStreamMetadataFormat(String formatName) {
        return this.getMetadataFormat(formatName, this.supportsStandardStreamMetadataFormat, this.nativeStreamMetadataFormatName, this.nativeStreamMetadataFormatClassName, this.extraStreamMetadataFormatNames, this.extraStreamMetadataFormatClassNames);
    }

    public IIOMetadataFormat getImageMetadataFormat(String formatName) {
        return this.getMetadataFormat(formatName, this.supportsStandardImageMetadataFormat, this.nativeImageMetadataFormatName, this.nativeImageMetadataFormatClassName, this.extraImageMetadataFormatNames, this.extraImageMetadataFormatClassNames);
    }

    private IIOMetadataFormat getMetadataFormat(String formatName, boolean supportsStandard, String nativeName, String nativeClassName, String[] extraNames, String[] extraClassNames) {
        if (formatName == null) {
            throw new IllegalArgumentException("formatName == null!");
        }
        if (supportsStandard && formatName.equals("javax_imageio_1.0")) {
            return IIOMetadataFormatImpl.getStandardFormatInstance();
        }
        String formatClassName = null;
        if (formatName.equals(nativeName)) {
            formatClassName = nativeClassName;
        } else if (extraNames != null) {
            for (int i = 0; i < extraNames.length; ++i) {
                if (!formatName.equals(extraNames[i])) continue;
                formatClassName = extraClassNames[i];
                break;
            }
        }
        if (formatClassName == null) {
            throw new IllegalArgumentException("Unsupported format name");
        }
        try {
            String className = formatClassName;
            PrivilegedAction<Class> pa = () -> this.getMetadataFormatClass(className);
            Class cls = AccessController.doPrivileged(pa);
            Method meth = cls.getMethod("getInstance", new Class[0]);
            return (IIOMetadataFormat)meth.invoke(null, new Object[0]);
        }
        catch (Exception e) {
            throw new IllegalStateException("Can't obtain format", e);
        }
    }

    private Class<?> getMetadataFormatClass(String formatClassName) {
        Module thisModule = ImageReaderWriterSpi.class.getModule();
        Module targetModule = this.getClass().getModule();
        Class<?> c = null;
        try {
            ClassLoader cl = this.getClass().getClassLoader();
            c = Class.forName(formatClassName, false, cl);
            if (!IIOMetadataFormat.class.isAssignableFrom(c)) {
                return null;
            }
        }
        catch (ClassNotFoundException cl) {
            // empty catch block
        }
        if (thisModule.equals(targetModule) || c == null) {
            return c;
        }
        if (targetModule.isNamed()) {
            String pn;
            int i = formatClassName.lastIndexOf(".");
            String string = pn = i > 0 ? formatClassName.substring(0, i) : "";
            if (!targetModule.isExported(pn, thisModule)) {
                throw new IllegalStateException("Class " + formatClassName + " in named module must be exported to java.desktop module.");
            }
        }
        return c;
    }
}

