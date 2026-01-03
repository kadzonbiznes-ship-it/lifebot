/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.metadata;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataController;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.Node;

public abstract class IIOMetadata {
    protected boolean standardFormatSupported;
    protected String nativeMetadataFormatName = null;
    protected String nativeMetadataFormatClassName = null;
    protected String[] extraMetadataFormatNames = null;
    protected String[] extraMetadataFormatClassNames = null;
    protected IIOMetadataController defaultController = null;
    protected IIOMetadataController controller = null;

    protected IIOMetadata() {
    }

    protected IIOMetadata(boolean standardMetadataFormatSupported, String nativeMetadataFormatName, String nativeMetadataFormatClassName, String[] extraMetadataFormatNames, String[] extraMetadataFormatClassNames) {
        this.standardFormatSupported = standardMetadataFormatSupported;
        this.nativeMetadataFormatName = nativeMetadataFormatName;
        this.nativeMetadataFormatClassName = nativeMetadataFormatClassName;
        if (extraMetadataFormatNames != null) {
            if (extraMetadataFormatNames.length == 0) {
                throw new IllegalArgumentException("extraMetadataFormatNames.length == 0!");
            }
            if (extraMetadataFormatClassNames == null) {
                throw new IllegalArgumentException("extraMetadataFormatNames != null && extraMetadataFormatClassNames == null!");
            }
            if (extraMetadataFormatClassNames.length != extraMetadataFormatNames.length) {
                throw new IllegalArgumentException("extraMetadataFormatClassNames.length != extraMetadataFormatNames.length!");
            }
            this.extraMetadataFormatNames = (String[])extraMetadataFormatNames.clone();
            this.extraMetadataFormatClassNames = (String[])extraMetadataFormatClassNames.clone();
        } else if (extraMetadataFormatClassNames != null) {
            throw new IllegalArgumentException("extraMetadataFormatNames == null && extraMetadataFormatClassNames != null!");
        }
    }

    public boolean isStandardMetadataFormatSupported() {
        return this.standardFormatSupported;
    }

    public abstract boolean isReadOnly();

    public String getNativeMetadataFormatName() {
        return this.nativeMetadataFormatName;
    }

    public String[] getExtraMetadataFormatNames() {
        if (this.extraMetadataFormatNames == null) {
            return null;
        }
        return (String[])this.extraMetadataFormatNames.clone();
    }

    public String[] getMetadataFormatNames() {
        String nativeName = this.getNativeMetadataFormatName();
        String standardName = this.isStandardMetadataFormatSupported() ? "javax_imageio_1.0" : null;
        String[] extraNames = this.getExtraMetadataFormatNames();
        int numFormats = 0;
        if (nativeName != null) {
            ++numFormats;
        }
        if (standardName != null) {
            ++numFormats;
        }
        if (extraNames != null) {
            numFormats += extraNames.length;
        }
        if (numFormats == 0) {
            return null;
        }
        String[] formats = new String[numFormats];
        int index = 0;
        if (nativeName != null) {
            formats[index++] = nativeName;
        }
        if (standardName != null) {
            formats[index++] = standardName;
        }
        if (extraNames != null) {
            for (int i = 0; i < extraNames.length; ++i) {
                formats[index++] = extraNames[i];
            }
        }
        return formats;
    }

    public IIOMetadataFormat getMetadataFormat(String formatName) {
        if (formatName == null) {
            throw new IllegalArgumentException("formatName == null!");
        }
        if (this.standardFormatSupported && formatName.equals("javax_imageio_1.0")) {
            return IIOMetadataFormatImpl.getStandardFormatInstance();
        }
        String formatClassName = null;
        if (formatName.equals(this.nativeMetadataFormatName)) {
            formatClassName = this.nativeMetadataFormatClassName;
        } else if (this.extraMetadataFormatNames != null) {
            for (int i = 0; i < this.extraMetadataFormatNames.length; ++i) {
                if (!formatName.equals(this.extraMetadataFormatNames[i])) continue;
                formatClassName = this.extraMetadataFormatClassNames[i];
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
        Module thisModule = IIOMetadata.class.getModule();
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

    public abstract Node getAsTree(String var1);

    public abstract void mergeTree(String var1, Node var2) throws IIOInvalidTreeException;

    protected IIOMetadataNode getStandardChromaNode() {
        return null;
    }

    protected IIOMetadataNode getStandardCompressionNode() {
        return null;
    }

    protected IIOMetadataNode getStandardDataNode() {
        return null;
    }

    protected IIOMetadataNode getStandardDimensionNode() {
        return null;
    }

    protected IIOMetadataNode getStandardDocumentNode() {
        return null;
    }

    protected IIOMetadataNode getStandardTextNode() {
        return null;
    }

    protected IIOMetadataNode getStandardTileNode() {
        return null;
    }

    protected IIOMetadataNode getStandardTransparencyNode() {
        return null;
    }

    private void append(IIOMetadataNode root, IIOMetadataNode node) {
        if (node != null) {
            root.appendChild(node);
        }
    }

    protected final IIOMetadataNode getStandardTree() {
        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        this.append(root, this.getStandardChromaNode());
        this.append(root, this.getStandardCompressionNode());
        this.append(root, this.getStandardDataNode());
        this.append(root, this.getStandardDimensionNode());
        this.append(root, this.getStandardDocumentNode());
        this.append(root, this.getStandardTextNode());
        this.append(root, this.getStandardTileNode());
        this.append(root, this.getStandardTransparencyNode());
        return root;
    }

    public void setFromTree(String formatName, Node root) throws IIOInvalidTreeException {
        this.reset();
        this.mergeTree(formatName, root);
    }

    public abstract void reset();

    public void setController(IIOMetadataController controller) {
        this.controller = controller;
    }

    public IIOMetadataController getController() {
        return this.controller;
    }

    public IIOMetadataController getDefaultController() {
        return this.defaultController;
    }

    public boolean hasController() {
        return this.getController() != null;
    }

    public boolean activateController() {
        if (!this.hasController()) {
            throw new IllegalStateException("hasController() == false!");
        }
        return this.getController().activate(this);
    }
}

