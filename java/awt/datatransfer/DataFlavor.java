/*
 * Decompiled with CFR 0.152.
 */
package java.awt.datatransfer;

import java.awt.datatransfer.MimeType;
import java.awt.datatransfer.MimeTypeParameterList;
import java.awt.datatransfer.MimeTypeParseException;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OptionalDataException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import sun.datatransfer.DataFlavorUtil;
import sun.reflect.misc.ReflectUtil;

public class DataFlavor
implements Externalizable,
Cloneable {
    private static final long serialVersionUID = 8367026044764648243L;
    public static final DataFlavor stringFlavor = DataFlavor.createConstant(String.class, "Unicode String");
    public static final DataFlavor imageFlavor = DataFlavor.createConstant("image/x-java-image; class=java.awt.Image", "Image");
    @Deprecated
    public static final DataFlavor plainTextFlavor = DataFlavor.createConstant("text/plain; charset=unicode; class=java.io.InputStream", "Plain Text");
    public static final String javaSerializedObjectMimeType = "application/x-java-serialized-object";
    public static final DataFlavor javaFileListFlavor = DataFlavor.createConstant("application/x-java-file-list;class=java.util.List", null);
    public static final String javaJVMLocalObjectMimeType = "application/x-java-jvm-local-objectref";
    public static final String javaRemoteObjectMimeType = "application/x-java-remote-object";
    public static final DataFlavor selectionHtmlFlavor = DataFlavor.initHtml("selection");
    public static final DataFlavor fragmentHtmlFlavor = DataFlavor.initHtml("fragment");
    public static final DataFlavor allHtmlFlavor = DataFlavor.initHtml("all");
    transient int atom;
    MimeType mimeType;
    private String humanPresentableName;
    private Class<?> representationClass;

    protected static final Class<?> tryToLoadClass(String className, ClassLoader fallback) throws ClassNotFoundException {
        ReflectUtil.checkPackageAccess(className);
        try {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(new RuntimePermission("getClassLoader"));
            }
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            try {
                return Class.forName(className, true, loader);
            }
            catch (ClassNotFoundException exception) {
                loader = Thread.currentThread().getContextClassLoader();
                if (loader != null) {
                    try {
                        return Class.forName(className, true, loader);
                    }
                    catch (ClassNotFoundException classNotFoundException) {}
                }
            }
        }
        catch (SecurityException securityException) {
            // empty catch block
        }
        return Class.forName(className, true, fallback);
    }

    private static DataFlavor createConstant(Class<?> rc, String prn) {
        try {
            return new DataFlavor(rc, prn);
        }
        catch (Exception e) {
            return null;
        }
    }

    private static DataFlavor createConstant(String mt, String prn) {
        try {
            return new DataFlavor(mt, prn);
        }
        catch (Exception e) {
            return null;
        }
    }

    private static DataFlavor initHtml(String htmlFlavorType) {
        try {
            return new DataFlavor("text/html; class=java.lang.String;document=" + htmlFlavorType + ";charset=Unicode");
        }
        catch (Exception e) {
            return null;
        }
    }

    public DataFlavor() {
    }

    private DataFlavor(String primaryType, String subType, MimeTypeParameterList params, Class<?> representationClass, String humanPresentableName) {
        if (primaryType == null) {
            throw new NullPointerException("primaryType");
        }
        if (subType == null) {
            throw new NullPointerException("subType");
        }
        if (representationClass == null) {
            throw new NullPointerException("representationClass");
        }
        if (params == null) {
            params = new MimeTypeParameterList();
        }
        params.set("class", representationClass.getName());
        if (humanPresentableName == null && (humanPresentableName = params.get("humanPresentableName")) == null) {
            humanPresentableName = primaryType + "/" + subType;
        }
        try {
            this.mimeType = new MimeType(primaryType, subType, params);
        }
        catch (MimeTypeParseException mtpe) {
            throw new IllegalArgumentException("MimeType Parse Exception: " + mtpe.getMessage());
        }
        this.representationClass = representationClass;
        this.humanPresentableName = humanPresentableName;
        this.mimeType.removeParameter("humanPresentableName");
    }

    public DataFlavor(Class<?> representationClass, String humanPresentableName) {
        this("application", "x-java-serialized-object", null, representationClass, humanPresentableName);
        if (representationClass == null) {
            throw new NullPointerException("representationClass");
        }
    }

    public DataFlavor(String mimeType, String humanPresentableName) {
        if (mimeType == null) {
            throw new NullPointerException("mimeType");
        }
        try {
            this.initialize(mimeType, humanPresentableName, this.getClass().getClassLoader());
        }
        catch (MimeTypeParseException mtpe) {
            throw new IllegalArgumentException("failed to parse:" + mimeType);
        }
        catch (ClassNotFoundException cnfe) {
            throw new IllegalArgumentException("can't find specified class: " + cnfe.getMessage());
        }
    }

    public DataFlavor(String mimeType, String humanPresentableName, ClassLoader classLoader) throws ClassNotFoundException {
        if (mimeType == null) {
            throw new NullPointerException("mimeType");
        }
        try {
            this.initialize(mimeType, humanPresentableName, classLoader);
        }
        catch (MimeTypeParseException mtpe) {
            throw new IllegalArgumentException("failed to parse:" + mimeType);
        }
    }

    public DataFlavor(String mimeType) throws ClassNotFoundException {
        if (mimeType == null) {
            throw new NullPointerException("mimeType");
        }
        try {
            this.initialize(mimeType, null, this.getClass().getClassLoader());
        }
        catch (MimeTypeParseException mtpe) {
            throw new IllegalArgumentException("failed to parse:" + mimeType);
        }
    }

    private void initialize(String mimeType, String humanPresentableName, ClassLoader classLoader) throws MimeTypeParseException, ClassNotFoundException {
        if (mimeType == null) {
            throw new NullPointerException("mimeType");
        }
        this.mimeType = new MimeType(mimeType);
        String rcn = this.getParameter("class");
        if (rcn == null) {
            if (javaSerializedObjectMimeType.equals(this.mimeType.getBaseType())) {
                throw new IllegalArgumentException("no representation class specified for:" + mimeType);
            }
            this.representationClass = InputStream.class;
        } else {
            this.representationClass = DataFlavor.tryToLoadClass(rcn, classLoader);
        }
        this.mimeType.setParameter("class", this.representationClass.getName());
        if (humanPresentableName == null && (humanPresentableName = this.mimeType.getParameter("humanPresentableName")) == null) {
            humanPresentableName = this.mimeType.getPrimaryType() + "/" + this.mimeType.getSubType();
        }
        this.humanPresentableName = humanPresentableName;
        this.mimeType.removeParameter("humanPresentableName");
    }

    public String toString() {
        Object string = this.getClass().getName();
        string = (String)string + "[" + this.paramString() + "]";
        return string;
    }

    private String paramString() {
        Object params = "";
        params = (String)params + "mimetype=";
        params = this.mimeType == null ? (String)params + "null" : (String)params + this.mimeType.getBaseType();
        params = (String)params + ";representationclass=";
        params = this.representationClass == null ? (String)params + "null" : (String)params + this.representationClass.getName();
        if (DataFlavorUtil.isFlavorCharsetTextType(this) && (this.isRepresentationClassInputStream() || this.isRepresentationClassByteBuffer() || byte[].class.equals(this.representationClass))) {
            params = (String)params + ";charset=" + DataFlavorUtil.getTextCharset(this);
        }
        return params;
    }

    public static final DataFlavor getTextPlainUnicodeFlavor() {
        return new DataFlavor("text/plain;charset=" + DataFlavorUtil.getDesktopService().getDefaultUnicodeEncoding() + ";class=java.io.InputStream", "Plain Text");
    }

    public static final DataFlavor selectBestTextFlavor(DataFlavor[] availableFlavors) {
        if (availableFlavors == null || availableFlavors.length == 0) {
            return null;
        }
        DataFlavor bestFlavor = Collections.max(Arrays.asList(availableFlavors), DataFlavorUtil.getTextFlavorComparator());
        if (!bestFlavor.isFlavorTextType()) {
            return null;
        }
        return bestFlavor;
    }

    public Reader getReaderForText(Transferable transferable) throws UnsupportedFlavorException, IOException {
        Object transferObject = transferable.getTransferData(this);
        if (transferObject == null) {
            throw new IllegalArgumentException("getTransferData() returned null");
        }
        if (transferObject instanceof Reader) {
            return (Reader)transferObject;
        }
        if (transferObject instanceof String) {
            return new StringReader((String)transferObject);
        }
        if (transferObject instanceof CharBuffer) {
            CharBuffer buffer = (CharBuffer)transferObject;
            int size = buffer.remaining();
            char[] chars = new char[size];
            buffer.get(chars, 0, size);
            return new CharArrayReader(chars);
        }
        if (transferObject instanceof char[]) {
            return new CharArrayReader((char[])transferObject);
        }
        InputStream stream = null;
        if (transferObject instanceof InputStream) {
            stream = (InputStream)transferObject;
        } else if (transferObject instanceof ByteBuffer) {
            ByteBuffer buffer = (ByteBuffer)transferObject;
            int size = buffer.remaining();
            byte[] bytes = new byte[size];
            buffer.get(bytes, 0, size);
            stream = new ByteArrayInputStream(bytes);
        } else if (transferObject instanceof byte[]) {
            stream = new ByteArrayInputStream((byte[])transferObject);
        }
        if (stream == null) {
            throw new IllegalArgumentException("transfer data is not Reader, String, CharBuffer, char array, InputStream, ByteBuffer, or byte array");
        }
        String encoding = this.getParameter("charset");
        return encoding == null ? new InputStreamReader(stream) : new InputStreamReader(stream, encoding);
    }

    public String getMimeType() {
        return this.mimeType != null ? this.mimeType.toString() : null;
    }

    public Class<?> getRepresentationClass() {
        return this.representationClass;
    }

    public String getHumanPresentableName() {
        return this.humanPresentableName;
    }

    public String getPrimaryType() {
        return this.mimeType != null ? this.mimeType.getPrimaryType() : null;
    }

    public String getSubType() {
        return this.mimeType != null ? this.mimeType.getSubType() : null;
    }

    public String getParameter(String paramName) {
        if (paramName.equals("humanPresentableName")) {
            return this.humanPresentableName;
        }
        return this.mimeType != null ? this.mimeType.getParameter(paramName) : null;
    }

    public void setHumanPresentableName(String humanPresentableName) {
        this.humanPresentableName = humanPresentableName;
    }

    public boolean equals(Object o) {
        return o instanceof DataFlavor && this.equals((DataFlavor)o);
    }

    public boolean equals(DataFlavor that) {
        if (that == null) {
            return false;
        }
        if (this == that) {
            return true;
        }
        if (!Objects.equals(this.getRepresentationClass(), that.getRepresentationClass())) {
            return false;
        }
        if (this.mimeType == null) {
            if (that.mimeType != null) {
                return false;
            }
        } else {
            if (!this.mimeType.match(that.mimeType)) {
                return false;
            }
            if ("text".equals(this.getPrimaryType())) {
                String thatDocument;
                String thisDocument;
                String thatCharset;
                String thisCharset;
                if (DataFlavorUtil.doesSubtypeSupportCharset(this) && this.representationClass != null && !this.isStandardTextRepresentationClass() && !Objects.equals(thisCharset = DataFlavorUtil.canonicalName(this.getParameter("charset")), thatCharset = DataFlavorUtil.canonicalName(that.getParameter("charset")))) {
                    return false;
                }
                if ("html".equals(this.getSubType()) && !Objects.equals(thisDocument = this.getParameter("document"), thatDocument = that.getParameter("document"))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Deprecated
    public boolean equals(String s) {
        if (s == null || this.mimeType == null) {
            return false;
        }
        return this.isMimeTypeEqual(s);
    }

    public int hashCode() {
        int total = 0;
        if (this.representationClass != null) {
            total += this.representationClass.hashCode();
        }
        if (this.mimeType != null) {
            String primaryType = this.mimeType.getPrimaryType();
            if (primaryType != null) {
                total += primaryType.hashCode();
            }
            if ("text".equals(primaryType)) {
                String document;
                String charset;
                if (DataFlavorUtil.doesSubtypeSupportCharset(this) && this.representationClass != null && !this.isStandardTextRepresentationClass() && (charset = DataFlavorUtil.canonicalName(this.getParameter("charset"))) != null) {
                    total += charset.hashCode();
                }
                if ("html".equals(this.getSubType()) && (document = this.getParameter("document")) != null) {
                    total += document.hashCode();
                }
            }
        }
        return total;
    }

    public boolean match(DataFlavor that) {
        return this.equals(that);
    }

    public boolean isMimeTypeEqual(String mimeType) {
        if (mimeType == null) {
            throw new NullPointerException("mimeType");
        }
        if (this.mimeType == null) {
            return false;
        }
        try {
            return this.mimeType.match(new MimeType(mimeType));
        }
        catch (MimeTypeParseException mtpe) {
            return false;
        }
    }

    public final boolean isMimeTypeEqual(DataFlavor dataFlavor) {
        return this.isMimeTypeEqual(dataFlavor.mimeType);
    }

    private boolean isMimeTypeEqual(MimeType mtype) {
        if (this.mimeType == null) {
            return mtype == null;
        }
        return this.mimeType.match(mtype);
    }

    private boolean isStandardTextRepresentationClass() {
        return this.isRepresentationClassReader() || String.class.equals(this.representationClass) || this.isRepresentationClassCharBuffer() || char[].class.equals(this.representationClass);
    }

    public boolean isMimeTypeSerializedObject() {
        return this.isMimeTypeEqual(javaSerializedObjectMimeType);
    }

    public final Class<?> getDefaultRepresentationClass() {
        return InputStream.class;
    }

    public final String getDefaultRepresentationClassAsString() {
        return this.getDefaultRepresentationClass().getName();
    }

    public boolean isRepresentationClassInputStream() {
        return InputStream.class.isAssignableFrom(this.representationClass);
    }

    public boolean isRepresentationClassReader() {
        return Reader.class.isAssignableFrom(this.representationClass);
    }

    public boolean isRepresentationClassCharBuffer() {
        return CharBuffer.class.isAssignableFrom(this.representationClass);
    }

    public boolean isRepresentationClassByteBuffer() {
        return ByteBuffer.class.isAssignableFrom(this.representationClass);
    }

    public boolean isRepresentationClassSerializable() {
        return Serializable.class.isAssignableFrom(this.representationClass);
    }

    public boolean isRepresentationClassRemote() {
        return DataFlavorUtil.RMI.isRemote(this.representationClass);
    }

    public boolean isFlavorSerializedObjectType() {
        return this.isRepresentationClassSerializable() && this.isMimeTypeEqual(javaSerializedObjectMimeType);
    }

    public boolean isFlavorRemoteObjectType() {
        return this.isRepresentationClassRemote() && this.isRepresentationClassSerializable() && this.isMimeTypeEqual(javaRemoteObjectMimeType);
    }

    public boolean isFlavorJavaFileListType() {
        if (this.mimeType == null || this.representationClass == null) {
            return false;
        }
        return List.class.isAssignableFrom(this.representationClass) && this.mimeType.match(DataFlavor.javaFileListFlavor.mimeType);
    }

    public boolean isFlavorTextType() {
        return DataFlavorUtil.isFlavorCharsetTextType(this) || DataFlavorUtil.isFlavorNoncharsetTextType(this);
    }

    @Override
    public synchronized void writeExternal(ObjectOutput os) throws IOException {
        if (this.mimeType != null) {
            this.mimeType.setParameter("humanPresentableName", this.humanPresentableName);
            os.writeObject(this.mimeType);
            this.mimeType.removeParameter("humanPresentableName");
        } else {
            os.writeObject(null);
        }
        os.writeObject(this.representationClass);
    }

    @Override
    public synchronized void readExternal(ObjectInput is) throws IOException, ClassNotFoundException {
        block5: {
            String rcn = null;
            this.mimeType = (MimeType)is.readObject();
            if (this.mimeType != null) {
                this.humanPresentableName = this.mimeType.getParameter("humanPresentableName");
                this.mimeType.removeParameter("humanPresentableName");
                rcn = this.mimeType.getParameter("class");
                if (rcn == null) {
                    throw new IOException("no class parameter specified in: " + String.valueOf(this.mimeType));
                }
            }
            try {
                this.representationClass = (Class)is.readObject();
            }
            catch (OptionalDataException ode) {
                if (!ode.eof || ode.length != 0) {
                    throw ode;
                }
                if (rcn == null) break block5;
                this.representationClass = DataFlavor.tryToLoadClass(rcn, this.getClass().getClassLoader());
            }
        }
    }

    public Object clone() throws CloneNotSupportedException {
        Object newObj = super.clone();
        if (this.mimeType != null) {
            ((DataFlavor)newObj).mimeType = (MimeType)this.mimeType.clone();
        }
        return newObj;
    }

    @Deprecated
    protected String normalizeMimeTypeParameter(String parameterName, String parameterValue) {
        return parameterValue;
    }

    @Deprecated
    protected String normalizeMimeType(String mimeType) {
        return mimeType;
    }
}

