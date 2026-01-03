/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.FontFormatException;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.peer.FontPeer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import sun.awt.ComponentFactory;
import sun.font.AttributeMap;
import sun.font.AttributeValues;
import sun.font.CompositeFont;
import sun.font.CoreMetrics;
import sun.font.CreatedFontTracker;
import sun.font.EAttribute;
import sun.font.Font2D;
import sun.font.Font2DHandle;
import sun.font.FontAccess;
import sun.font.FontDesignMetrics;
import sun.font.FontLineMetrics;
import sun.font.FontManager;
import sun.font.FontManagerFactory;
import sun.font.FontUtilities;
import sun.font.GlyphLayout;
import sun.font.StandardGlyphVector;

public class Font
implements Serializable {
    private Hashtable<Object, Object> fRequestedAttributes;
    public static final String DIALOG = "Dialog";
    public static final String DIALOG_INPUT = "DialogInput";
    public static final String SANS_SERIF = "SansSerif";
    public static final String SERIF = "Serif";
    public static final String MONOSPACED = "Monospaced";
    public static final int PLAIN = 0;
    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int ROMAN_BASELINE = 0;
    public static final int CENTER_BASELINE = 1;
    public static final int HANGING_BASELINE = 2;
    public static final int TRUETYPE_FONT = 0;
    public static final int TYPE1_FONT = 1;
    protected String name;
    protected int style;
    protected int size;
    protected float pointSize;
    private transient FontPeer peer;
    private transient long pData;
    private transient Font2DHandle font2DHandle;
    private transient AttributeValues values;
    private transient boolean hasLayoutAttributes;
    private transient boolean createdFont = false;
    private transient boolean nonIdentityTx;
    private static final AffineTransform identityTx;
    private static final long serialVersionUID = -4206021311591459213L;
    private static final int RECOGNIZED_MASK;
    private static final int PRIMARY_MASK;
    private static final int SECONDARY_MASK;
    private static final int LAYOUT_MASK;
    private static final int EXTRA_MASK;
    private static final float[] ssinfo;
    transient int hash;
    private int fontSerializedDataVersion = 1;
    private transient SoftReference<FontLineMetrics> flmref;
    public static final int LAYOUT_LEFT_TO_RIGHT = 0;
    public static final int LAYOUT_RIGHT_TO_LEFT = 1;
    public static final int LAYOUT_NO_START_CONTEXT = 2;
    public static final int LAYOUT_NO_LIMIT_CONTEXT = 4;

    private FontPeer getFontPeer() {
        Toolkit tk;
        if (this.peer == null && (tk = Toolkit.getDefaultToolkit()) instanceof ComponentFactory) {
            this.peer = ((ComponentFactory)((Object)tk)).getFontPeer(this.name, this.style);
        }
        return this.peer;
    }

    private AttributeValues getAttributeValues() {
        if (this.values == null) {
            AttributeValues valuesTmp = new AttributeValues();
            valuesTmp.setFamily(this.name);
            valuesTmp.setSize(this.pointSize);
            if ((this.style & 1) != 0) {
                valuesTmp.setWeight(2.0f);
            }
            if ((this.style & 2) != 0) {
                valuesTmp.setPosture(0.2f);
            }
            valuesTmp.defineAll(PRIMARY_MASK);
            this.values = valuesTmp;
        }
        return this.values;
    }

    private Font2D getFont2D() {
        FontManager fm = FontManagerFactory.getInstance();
        if (this.font2DHandle == null) {
            this.font2DHandle = fm.findFont2D((String)this.name, (int)this.style, (int)2).handle;
        }
        return this.font2DHandle.font2D;
    }

    public Font(String name, int style, int size) {
        this.name = name != null ? name : "Default";
        this.style = (style & 0xFFFFFFFC) == 0 ? style : 0;
        this.size = size;
        this.pointSize = size;
    }

    private Font(String name, int style, float sizePts) {
        this.name = name != null ? name : "Default";
        this.style = (style & 0xFFFFFFFC) == 0 ? style : 0;
        this.size = (int)((double)sizePts + 0.5);
        this.pointSize = sizePts;
    }

    private Font(String name, int style, float sizePts, boolean created, Font2DHandle handle) {
        this(name, style, sizePts);
        this.createdFont = created;
        if (created) {
            if (handle.font2D instanceof CompositeFont && handle.font2D.getStyle() != style) {
                FontManager fm = FontManagerFactory.getInstance();
                this.font2DHandle = fm.getNewComposite(null, style, handle);
            } else {
                this.font2DHandle = handle;
            }
        }
    }

    private Font(File fontFile, int fontFormat, boolean isCopy, CreatedFontTracker tracker) throws FontFormatException {
        this.createdFont = true;
        FontManager fm = FontManagerFactory.getInstance();
        Font2D[] fonts = fm.createFont2D(fontFile, fontFormat, false, isCopy, tracker);
        this.font2DHandle = fonts[0].handle;
        this.name = this.font2DHandle.font2D.getFontName(Locale.getDefault());
        this.style = 0;
        this.size = 1;
        this.pointSize = 1.0f;
    }

    private Font(AttributeValues values, String oldName, int oldStyle, boolean created, Font2DHandle handle) {
        this.createdFont = created;
        if (created) {
            this.font2DHandle = handle;
            String newName = null;
            if (oldName != null && oldName.equals(newName = values.getFamily())) {
                newName = null;
            }
            int newStyle = 0;
            if (oldStyle == -1) {
                newStyle = -1;
            } else {
                if (values.getWeight() >= 2.0f) {
                    newStyle = 1;
                }
                if (values.getPosture() >= 0.2f) {
                    newStyle |= 2;
                }
                if (oldStyle == newStyle) {
                    newStyle = -1;
                }
            }
            if (handle.font2D instanceof CompositeFont) {
                if (newStyle != -1 || newName != null) {
                    FontManager fm = FontManagerFactory.getInstance();
                    this.font2DHandle = fm.getNewComposite(newName, newStyle, handle);
                }
            } else if (newName != null) {
                this.createdFont = false;
                this.font2DHandle = null;
            }
        }
        this.initFromValues(values);
    }

    public Font(Map<? extends AttributedCharacterIterator.Attribute, ?> attributes) {
        this.initFromValues(AttributeValues.fromMap(attributes, RECOGNIZED_MASK));
    }

    protected Font(Font font) {
        if (font.values != null) {
            this.initFromValues(font.getAttributeValues().clone());
        } else {
            this.name = font.name;
            this.style = font.style;
            this.size = font.size;
            this.pointSize = font.pointSize;
        }
        this.font2DHandle = font.font2DHandle;
        this.createdFont = font.createdFont;
    }

    private void initFromValues(AttributeValues values) {
        this.values = values;
        values.defineAll(PRIMARY_MASK);
        this.name = values.getFamily();
        this.pointSize = values.getSize();
        this.size = (int)((double)values.getSize() + 0.5);
        if (values.getWeight() >= 2.0f) {
            this.style |= 1;
        }
        if (values.getPosture() >= 0.2f) {
            this.style |= 2;
        }
        this.nonIdentityTx = values.anyNonDefault(EXTRA_MASK);
        this.hasLayoutAttributes = values.anyNonDefault(LAYOUT_MASK);
    }

    public static boolean textRequiresLayout(char[] chars, int start, int end) {
        if (chars == null) {
            throw new NullPointerException("null char array");
        }
        if (start < 0 || end > chars.length) {
            throw new ArrayIndexOutOfBoundsException("start < 0 or end > len");
        }
        return FontUtilities.isComplexScript(chars, start, end);
    }

    public static Font getFont(Map<? extends AttributedCharacterIterator.Attribute, ?> attributes) {
        if (attributes instanceof AttributeMap && ((AttributeMap)attributes).getValues() != null) {
            AttributeValues values = ((AttributeMap)attributes).getValues();
            if (values.isNonDefault(EAttribute.EFONT)) {
                Font font = values.getFont();
                if (!values.anyDefined(SECONDARY_MASK)) {
                    return font;
                }
                values = font.getAttributeValues().clone();
                values.merge(attributes, SECONDARY_MASK);
                return new Font(values, font.name, font.style, font.createdFont, font.font2DHandle);
            }
            return new Font(attributes);
        }
        Font font = (Font)attributes.get(TextAttribute.FONT);
        if (font != null) {
            if (attributes.size() > 1) {
                AttributeValues values = font.getAttributeValues().clone();
                values.merge(attributes, SECONDARY_MASK);
                return new Font(values, font.name, font.style, font.createdFont, font.font2DHandle);
            }
            return font;
        }
        return new Font(attributes);
    }

    private static boolean hasTempPermission() {
        if (System.getSecurityManager() == null) {
            return true;
        }
        File f = null;
        boolean hasPerm = false;
        try {
            f = Files.createTempFile("+~JT", ".tmp", new FileAttribute[0]).toFile();
            f.delete();
            f = null;
            hasPerm = true;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return hasPerm;
    }

    public static Font[] createFonts(InputStream fontStream) throws FontFormatException, IOException {
        boolean fontFormat = false;
        if (Font.hasTempPermission()) {
            return Font.createFont0(0, fontStream, true, null);
        }
        CreatedFontTracker tracker = CreatedFontTracker.getTracker();
        boolean acquired = false;
        try {
            acquired = tracker.acquirePermit();
            if (!acquired) {
                throw new IOException("Timed out waiting for resources.");
            }
            Font[] fontArray = Font.createFont0(0, fontStream, true, tracker);
            return fontArray;
        }
        catch (InterruptedException e) {
            throw new IOException("Problem reading font data.");
        }
        finally {
            if (acquired) {
                tracker.releasePermit();
            }
        }
    }

    private Font(Font2D font2D) {
        this.createdFont = true;
        this.font2DHandle = font2D.handle;
        this.name = font2D.getFontName(Locale.getDefault());
        this.style = 0;
        this.size = 1;
        this.pointSize = 1.0f;
    }

    public static Font[] createFonts(File fontFile) throws FontFormatException, IOException {
        int fontFormat = 0;
        fontFile = Font.checkFontFile(fontFormat, fontFile);
        FontManager fm = FontManagerFactory.getInstance();
        Font2D[] font2DArr = fm.createFont2D(fontFile, fontFormat, true, false, null);
        int num = font2DArr.length;
        Font[] fonts = new Font[num];
        for (int i = 0; i < num; ++i) {
            fonts[i] = new Font(font2DArr[i]);
        }
        return fonts;
    }

    public static Font createFont(int fontFormat, InputStream fontStream) throws FontFormatException, IOException {
        if (Font.hasTempPermission()) {
            return Font.createFont0(fontFormat, fontStream, false, null)[0];
        }
        CreatedFontTracker tracker = CreatedFontTracker.getTracker();
        boolean acquired = false;
        try {
            acquired = tracker.acquirePermit();
            if (!acquired) {
                throw new IOException("Timed out waiting for resources.");
            }
            Font font = Font.createFont0(fontFormat, fontStream, false, tracker)[0];
            return font;
        }
        catch (InterruptedException e) {
            throw new IOException("Problem reading font data.");
        }
        finally {
            if (acquired) {
                tracker.releasePermit();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Font[] createFont0(int fontFormat, InputStream fontStream, boolean allFonts, CreatedFontTracker tracker) throws FontFormatException, IOException {
        Font[] fontArray;
        block28: {
            File tFile;
            block29: {
                if (fontFormat != 0 && fontFormat != 1) {
                    throw new IllegalArgumentException("font format not recognized");
                }
                boolean copiedFontData = false;
                tFile = AccessController.doPrivileged(new PrivilegedExceptionAction<File>(){

                    @Override
                    public File run() throws IOException {
                        return Files.createTempFile("+~JF", ".tmp", new FileAttribute[0]).toFile();
                    }
                });
                if (tracker != null) {
                    tracker.add(tFile);
                }
                int totalSize = 0;
                try {
                    OutputStream outStream = AccessController.doPrivileged(new PrivilegedExceptionAction<OutputStream>(){

                        @Override
                        public OutputStream run() throws IOException {
                            return new FileOutputStream(tFile);
                        }
                    });
                    if (tracker != null) {
                        tracker.set(tFile, outStream);
                    }
                    try (OutputStream outputStream = outStream;){
                        int bytesRead;
                        byte[] buf = new byte[8192];
                        while ((bytesRead = fontStream.read(buf)) >= 0) {
                            if (tracker != null) {
                                if (totalSize + bytesRead > 0x2000000) {
                                    throw new IOException("File too big.");
                                }
                                if (totalSize + tracker.getNumBytes() > 0x14000000) {
                                    throw new IOException("Total files too big.");
                                }
                                totalSize += bytesRead;
                                tracker.addBytes(bytesRead);
                            }
                            outStream.write(buf, 0, bytesRead);
                        }
                    }
                    copiedFontData = true;
                    FontManager fm = FontManagerFactory.getInstance();
                    Font2D[] font2DArr = fm.createFont2D(tFile, fontFormat, allFonts, true, tracker);
                    int num = font2DArr.length;
                    Font[] fonts = new Font[num];
                    for (int i = 0; i < num; ++i) {
                        fonts[i] = new Font(font2DArr[i]);
                    }
                    fontArray = fonts;
                    if (tracker != null) {
                        tracker.remove(tFile);
                    }
                    if (copiedFontData) break block28;
                    if (tracker == null) break block29;
                    tracker.subBytes(totalSize);
                }
                catch (Throwable throwable) {
                    try {
                        if (tracker != null) {
                            tracker.remove(tFile);
                        }
                        if (!copiedFontData) {
                            if (tracker != null) {
                                tracker.subBytes(totalSize);
                            }
                            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>(tFile){
                                final /* synthetic */ File val$tFile;
                                {
                                    this.val$tFile = file;
                                }

                                @Override
                                public Void run() {
                                    this.val$tFile.delete();
                                    return null;
                                }
                            });
                        }
                        throw throwable;
                    }
                    catch (Throwable t) {
                        if (t instanceof FontFormatException) {
                            throw (FontFormatException)t;
                        }
                        if (t instanceof IOException) {
                            throw (IOException)t;
                        }
                        Throwable cause = t.getCause();
                        if (cause instanceof FontFormatException) {
                            throw (FontFormatException)cause;
                        }
                        throw new IOException("Problem reading font data.");
                    }
                }
            }
            AccessController.doPrivileged(new /* invalid duplicate definition of identical inner class */);
        }
        return fontArray;
    }

    public static Font createFont(int fontFormat, File fontFile) throws FontFormatException, IOException {
        fontFile = Font.checkFontFile(fontFormat, fontFile);
        return new Font(fontFile, fontFormat, false, null);
    }

    private static File checkFontFile(int fontFormat, File fontFile) throws FontFormatException, IOException {
        fontFile = new File(fontFile.getPath());
        if (fontFormat != 0 && fontFormat != 1) {
            throw new IllegalArgumentException("font format not recognized");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            FilePermission filePermission = new FilePermission(fontFile.getPath(), "read");
            sm.checkPermission(filePermission);
        }
        if (!fontFile.canRead()) {
            throw new IOException("Can't read " + String.valueOf(fontFile));
        }
        return fontFile;
    }

    public AffineTransform getTransform() {
        if (this.nonIdentityTx) {
            AffineTransform at;
            AttributeValues values = this.getAttributeValues();
            AffineTransform affineTransform = at = values.isNonDefault(EAttribute.ETRANSFORM) ? new AffineTransform(values.getTransform()) : new AffineTransform();
            if (values.getSuperscript() != 0) {
                int ss;
                int superscript = values.getSuperscript();
                double trans = 0.0;
                int n = 0;
                boolean up = superscript > 0;
                int sign = up ? -1 : 1;
                int n2 = ss = up ? superscript : -superscript;
                while ((ss & 7) > n) {
                    int newn = ss & 7;
                    trans += (double)((float)sign * (ssinfo[newn] - ssinfo[n]));
                    ss >>= 3;
                    sign = -sign;
                    n = newn;
                }
                double scale = Math.pow(0.6666666666666666, n);
                at.preConcatenate(AffineTransform.getTranslateInstance(0.0, trans *= (double)this.pointSize));
                at.scale(scale, scale);
            }
            if (values.isNonDefault(EAttribute.EWIDTH)) {
                at.scale(values.getWidth(), 1.0);
            }
            return at;
        }
        return new AffineTransform();
    }

    public String getFamily() {
        return this.getFamily_NoClientCode();
    }

    final String getFamily_NoClientCode() {
        return this.getFamily(Locale.getDefault());
    }

    public String getFamily(Locale l) {
        if (l == null) {
            throw new NullPointerException("null locale doesn't mean default");
        }
        return this.getFont2D().getFamilyName(l);
    }

    public String getPSName() {
        return this.getFont2D().getPostscriptName();
    }

    public String getName() {
        return this.name;
    }

    public String getFontName() {
        return this.getFontName(Locale.getDefault());
    }

    public String getFontName(Locale l) {
        if (l == null) {
            throw new NullPointerException("null locale doesn't mean default");
        }
        return this.getFont2D().getFontName(l);
    }

    public int getStyle() {
        return this.style;
    }

    public int getSize() {
        return this.size;
    }

    public float getSize2D() {
        return this.pointSize;
    }

    public boolean isPlain() {
        return this.style == 0;
    }

    public boolean isBold() {
        return (this.style & 1) != 0;
    }

    public boolean isItalic() {
        return (this.style & 2) != 0;
    }

    public boolean isTransformed() {
        return this.nonIdentityTx;
    }

    public boolean hasLayoutAttributes() {
        return this.hasLayoutAttributes;
    }

    public static Font getFont(String nm) {
        return Font.getFont(nm, null);
    }

    public static Font decode(String str) {
        int strlen;
        int styleIndex;
        int sizeIndex;
        char sepChar;
        int fontStyle;
        int fontSize;
        String styleName;
        String fontName;
        block20: {
            int lastSpace;
            fontName = str;
            styleName = "";
            fontSize = 12;
            fontStyle = 0;
            if (str == null) {
                return new Font(DIALOG, fontStyle, fontSize);
            }
            int lastHyphen = str.lastIndexOf(45);
            sepChar = lastHyphen > (lastSpace = str.lastIndexOf(32)) ? (char)'-' : ' ';
            sizeIndex = str.lastIndexOf(sepChar);
            styleIndex = str.lastIndexOf(sepChar, sizeIndex - 1);
            strlen = str.length();
            if (sizeIndex > 0 && sizeIndex + 1 < strlen) {
                try {
                    fontSize = Integer.parseInt(str.substring(sizeIndex + 1));
                    if (fontSize <= 0) {
                        fontSize = 12;
                    }
                }
                catch (NumberFormatException e) {
                    styleIndex = sizeIndex;
                    sizeIndex = strlen;
                    if (str.charAt(sizeIndex - 1) != sepChar) break block20;
                    --sizeIndex;
                }
            }
        }
        if (styleIndex >= 0 && styleIndex + 1 < strlen) {
            styleName = str.substring(styleIndex + 1, sizeIndex);
            if ((styleName = styleName.toLowerCase(Locale.ENGLISH)).equals("bolditalic")) {
                fontStyle = 3;
            } else if (styleName.equals("italic")) {
                fontStyle = 2;
            } else if (styleName.equals("bold")) {
                fontStyle = 1;
            } else if (styleName.equals("plain")) {
                fontStyle = 0;
            } else {
                styleIndex = sizeIndex;
                if (str.charAt(styleIndex - 1) == sepChar) {
                    --styleIndex;
                }
            }
            fontName = str.substring(0, styleIndex);
        } else {
            int fontEnd = strlen;
            if (styleIndex > 0) {
                fontEnd = styleIndex;
            } else if (sizeIndex > 0) {
                fontEnd = sizeIndex;
            }
            if (fontEnd > 0 && str.charAt(fontEnd - 1) == sepChar) {
                --fontEnd;
            }
            fontName = str.substring(0, fontEnd);
        }
        return new Font(fontName, fontStyle, fontSize);
    }

    public static Font getFont(String nm, Font font) {
        String str = null;
        try {
            str = System.getProperty(nm);
        }
        catch (SecurityException securityException) {
            // empty catch block
        }
        if (str == null) {
            return font;
        }
        return Font.decode(str);
    }

    public int hashCode() {
        if (this.hash == 0) {
            this.hash = this.name.hashCode() ^ this.style ^ this.size;
            if (this.nonIdentityTx && this.values != null && this.values.getTransform() != null) {
                this.hash ^= this.values.getTransform().hashCode();
            }
        }
        return this.hash;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Font) {
            Font font = (Font)obj;
            if (this.size == font.size && this.style == font.style && this.nonIdentityTx == font.nonIdentityTx && this.hasLayoutAttributes == font.hasLayoutAttributes && this.pointSize == font.pointSize && this.name.equals(font.name)) {
                if (this.values == null) {
                    if (font.values == null) {
                        return true;
                    }
                    return this.getAttributeValues().equals(font.values);
                }
                return this.values.equals(font.getAttributeValues());
            }
        }
        return false;
    }

    public String toString() {
        String strStyle = this.isBold() ? (this.isItalic() ? "bolditalic" : "bold") : (this.isItalic() ? "italic" : "plain");
        return this.getClass().getName() + "[family=" + this.getFamily() + ",name=" + this.name + ",style=" + strStyle + ",size=" + this.size + "]";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        if (this.values != null) {
            AttributeValues attributeValues = this.values;
            synchronized (attributeValues) {
                this.fRequestedAttributes = this.values.toSerializableHashtable();
                s.defaultWriteObject();
                this.fRequestedAttributes = null;
            }
        } else {
            s.defaultWriteObject();
        }
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        if (this.pointSize == 0.0f) {
            this.pointSize = this.size;
        }
        if (this.fRequestedAttributes != null) {
            try {
                this.values = this.getAttributeValues();
                AttributeValues extras = AttributeValues.fromSerializableHashtable(this.fRequestedAttributes);
                if (!AttributeValues.is16Hashtable(this.fRequestedAttributes)) {
                    extras.unsetDefault();
                }
                this.values = this.getAttributeValues().merge(extras);
                this.nonIdentityTx = this.values.anyNonDefault(EXTRA_MASK);
                this.hasLayoutAttributes = this.values.anyNonDefault(LAYOUT_MASK);
            }
            catch (Throwable t) {
                throw new IOException(t);
            }
            finally {
                this.fRequestedAttributes = null;
            }
        }
    }

    public int getNumGlyphs() {
        return this.getFont2D().getNumGlyphs();
    }

    public int getMissingGlyphCode() {
        return this.getFont2D().getMissingGlyphCode();
    }

    public byte getBaselineFor(char c) {
        return this.getFont2D().getBaselineFor(c);
    }

    public Map<TextAttribute, ?> getAttributes() {
        return new AttributeMap(this.getAttributeValues());
    }

    public AttributedCharacterIterator.Attribute[] getAvailableAttributes() {
        AttributedCharacterIterator.Attribute[] attributes = new AttributedCharacterIterator.Attribute[]{TextAttribute.FAMILY, TextAttribute.WEIGHT, TextAttribute.WIDTH, TextAttribute.POSTURE, TextAttribute.SIZE, TextAttribute.TRANSFORM, TextAttribute.SUPERSCRIPT, TextAttribute.CHAR_REPLACEMENT, TextAttribute.FOREGROUND, TextAttribute.BACKGROUND, TextAttribute.UNDERLINE, TextAttribute.STRIKETHROUGH, TextAttribute.RUN_DIRECTION, TextAttribute.BIDI_EMBEDDING, TextAttribute.JUSTIFICATION, TextAttribute.INPUT_METHOD_HIGHLIGHT, TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.SWAP_COLORS, TextAttribute.NUMERIC_SHAPING, TextAttribute.KERNING, TextAttribute.LIGATURES, TextAttribute.TRACKING};
        return attributes;
    }

    public Font deriveFont(int style, float size) {
        if (this.values == null) {
            return new Font(this.name, style, size, this.createdFont, this.font2DHandle);
        }
        AttributeValues newValues = this.getAttributeValues().clone();
        int oldStyle = this.style != style ? this.style : -1;
        Font.applyStyle(style, newValues);
        newValues.setSize(size);
        return new Font(newValues, null, oldStyle, this.createdFont, this.font2DHandle);
    }

    public Font deriveFont(int style, AffineTransform trans) {
        AttributeValues newValues = this.getAttributeValues().clone();
        int oldStyle = this.style != style ? this.style : -1;
        Font.applyStyle(style, newValues);
        Font.applyTransform(trans, newValues);
        return new Font(newValues, null, oldStyle, this.createdFont, this.font2DHandle);
    }

    public Font deriveFont(float size) {
        if (this.values == null) {
            return new Font(this.name, this.style, size, this.createdFont, this.font2DHandle);
        }
        AttributeValues newValues = this.getAttributeValues().clone();
        newValues.setSize(size);
        return new Font(newValues, null, -1, this.createdFont, this.font2DHandle);
    }

    public Font deriveFont(AffineTransform trans) {
        AttributeValues newValues = this.getAttributeValues().clone();
        Font.applyTransform(trans, newValues);
        return new Font(newValues, null, -1, this.createdFont, this.font2DHandle);
    }

    public Font deriveFont(int style) {
        if (this.values == null) {
            return new Font(this.name, style, (float)this.size, this.createdFont, this.font2DHandle);
        }
        AttributeValues newValues = this.getAttributeValues().clone();
        int oldStyle = this.style != style ? this.style : -1;
        Font.applyStyle(style, newValues);
        return new Font(newValues, null, oldStyle, this.createdFont, this.font2DHandle);
    }

    public Font deriveFont(Map<? extends AttributedCharacterIterator.Attribute, ?> attributes) {
        if (attributes == null) {
            return this;
        }
        AttributeValues newValues = this.getAttributeValues().clone();
        newValues.merge(attributes, RECOGNIZED_MASK);
        return new Font(newValues, this.name, this.style, this.createdFont, this.font2DHandle);
    }

    public boolean canDisplay(char c) {
        return this.getFont2D().canDisplay(c);
    }

    public boolean canDisplay(int codePoint) {
        if (!Character.isValidCodePoint(codePoint)) {
            throw new IllegalArgumentException("invalid code point: " + Integer.toHexString(codePoint));
        }
        return this.getFont2D().canDisplay(codePoint);
    }

    public int canDisplayUpTo(String str) {
        Font2D font2d = this.getFont2D();
        int len = str.length();
        for (int i = 0; i < len; ++i) {
            char c = str.charAt(i);
            if (font2d.canDisplay(c)) continue;
            if (!Character.isHighSurrogate(c)) {
                return i;
            }
            if (!font2d.canDisplay(str.codePointAt(i))) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    public int canDisplayUpTo(char[] text, int start, int limit) {
        Font2D font2d = this.getFont2D();
        for (int i = start; i < limit; ++i) {
            char c = text[i];
            if (font2d.canDisplay(c)) continue;
            if (!Character.isHighSurrogate(c)) {
                return i;
            }
            if (!font2d.canDisplay(Character.codePointAt(text, i, limit))) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    public int canDisplayUpTo(CharacterIterator iter, int start, int limit) {
        Font2D font2d = this.getFont2D();
        char c = iter.setIndex(start);
        for (int i = start; i < limit; ++i) {
            if (!font2d.canDisplay(c)) {
                if (!Character.isHighSurrogate(c)) {
                    return i;
                }
                char c2 = iter.next();
                if (!Character.isLowSurrogate(c2)) {
                    return i;
                }
                if (!font2d.canDisplay(Character.toCodePoint(c, c2))) {
                    return i;
                }
                ++i;
            }
            c = iter.next();
        }
        return -1;
    }

    public float getItalicAngle() {
        return this.getItalicAngle(null);
    }

    private float getItalicAngle(FontRenderContext frc) {
        Object fm;
        Object aa;
        if (frc == null) {
            aa = RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
            fm = RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
        } else {
            aa = frc.getAntiAliasingHint();
            fm = frc.getFractionalMetricsHint();
        }
        return this.getFont2D().getItalicAngle(this, identityTx, aa, fm);
    }

    public boolean hasUniformLineMetrics() {
        return false;
    }

    private FontLineMetrics defaultLineMetrics(FontRenderContext frc) {
        FontLineMetrics flm = null;
        if (this.flmref == null || (flm = this.flmref.get()) == null || !flm.frc.equals(frc)) {
            AffineTransform ctx;
            float[] metrics = new float[8];
            this.getFont2D().getFontMetrics(this, identityTx, frc.getAntiAliasingHint(), frc.getFractionalMetricsHint(), metrics);
            float ascent = metrics[0];
            float descent = metrics[1];
            float leading = metrics[2];
            float ssOffset = 0.0f;
            if (this.values != null && this.values.getSuperscript() != 0) {
                ssOffset = (float)this.getTransform().getTranslateY();
                ascent -= ssOffset;
                descent += ssOffset;
            }
            float height = ascent + descent + leading;
            int baselineIndex = 0;
            float[] baselineOffsets = new float[]{0.0f, (descent / 2.0f - ascent) / 2.0f, -ascent};
            float strikethroughOffset = metrics[4];
            float strikethroughThickness = metrics[5];
            float underlineOffset = metrics[6];
            float underlineThickness = metrics[7];
            float italicAngle = this.getItalicAngle(frc);
            if (this.isTransformed() && (ctx = this.values.getCharTransform()) != null) {
                Point2D.Float pt = new Point2D.Float();
                pt.setLocation(0.0f, strikethroughOffset);
                ctx.deltaTransform(pt, pt);
                strikethroughOffset = pt.y;
                pt.setLocation(0.0f, strikethroughThickness);
                ctx.deltaTransform(pt, pt);
                strikethroughThickness = pt.y;
                pt.setLocation(0.0f, underlineOffset);
                ctx.deltaTransform(pt, pt);
                underlineOffset = pt.y;
                pt.setLocation(0.0f, underlineThickness);
                ctx.deltaTransform(pt, pt);
                underlineThickness = pt.y;
            }
            CoreMetrics cm = new CoreMetrics(ascent, descent, leading, height, baselineIndex, baselineOffsets, strikethroughOffset += ssOffset, strikethroughThickness, underlineOffset += ssOffset, underlineThickness, ssOffset, italicAngle);
            flm = new FontLineMetrics(0, cm, frc);
            this.flmref = new SoftReference<FontLineMetrics>(flm);
        }
        return (FontLineMetrics)flm.clone();
    }

    public LineMetrics getLineMetrics(String str, FontRenderContext frc) {
        FontLineMetrics flm = this.defaultLineMetrics(frc);
        flm.numchars = str.length();
        return flm;
    }

    public LineMetrics getLineMetrics(String str, int beginIndex, int limit, FontRenderContext frc) {
        FontLineMetrics flm = this.defaultLineMetrics(frc);
        int numChars = limit - beginIndex;
        flm.numchars = numChars < 0 ? 0 : numChars;
        return flm;
    }

    public LineMetrics getLineMetrics(char[] chars, int beginIndex, int limit, FontRenderContext frc) {
        FontLineMetrics flm = this.defaultLineMetrics(frc);
        int numChars = limit - beginIndex;
        flm.numchars = numChars < 0 ? 0 : numChars;
        return flm;
    }

    public LineMetrics getLineMetrics(CharacterIterator ci, int beginIndex, int limit, FontRenderContext frc) {
        FontLineMetrics flm = this.defaultLineMetrics(frc);
        int numChars = limit - beginIndex;
        flm.numchars = numChars < 0 ? 0 : numChars;
        return flm;
    }

    public Rectangle2D getStringBounds(String str, FontRenderContext frc) {
        char[] array = str.toCharArray();
        return this.getStringBounds(array, 0, array.length, frc);
    }

    public Rectangle2D getStringBounds(String str, int beginIndex, int limit, FontRenderContext frc) {
        String substr = str.substring(beginIndex, limit);
        return this.getStringBounds(substr, frc);
    }

    public Rectangle2D getStringBounds(char[] chars, int beginIndex, int limit, FontRenderContext frc) {
        boolean simple;
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException("beginIndex: " + beginIndex);
        }
        if (limit > chars.length) {
            throw new IndexOutOfBoundsException("limit: " + limit);
        }
        if (beginIndex > limit) {
            throw new IndexOutOfBoundsException("range length: " + (limit - beginIndex));
        }
        boolean bl = simple = this.values == null || this.values.getKerning() == 0 && this.values.getLigatures() == 0 && this.values.getTracking() == 0.0f && this.values.getBaselineTransform() == null;
        if (simple) {
            boolean bl2 = simple = !FontUtilities.isComplexText(chars, beginIndex, limit);
        }
        if (simple || limit - beginIndex == 0) {
            FontDesignMetrics metrics = FontDesignMetrics.getMetrics(this, frc);
            return metrics.getSimpleBounds(chars, beginIndex, limit - beginIndex);
        }
        String str = new String(chars, beginIndex, limit - beginIndex);
        TextLayout tl = new TextLayout(str, this, frc);
        return new Rectangle2D.Float(0.0f, -tl.getAscent(), tl.getAdvance(), tl.getAscent() + tl.getDescent() + tl.getLeading());
    }

    public Rectangle2D getStringBounds(CharacterIterator ci, int beginIndex, int limit, FontRenderContext frc) {
        int start = ci.getBeginIndex();
        int end = ci.getEndIndex();
        if (beginIndex < start) {
            throw new IndexOutOfBoundsException("beginIndex: " + beginIndex);
        }
        if (limit > end) {
            throw new IndexOutOfBoundsException("limit: " + limit);
        }
        if (beginIndex > limit) {
            throw new IndexOutOfBoundsException("range length: " + (limit - beginIndex));
        }
        char[] arr = new char[limit - beginIndex];
        ci.setIndex(beginIndex);
        for (int idx = 0; idx < arr.length; ++idx) {
            arr[idx] = ci.current();
            ci.next();
        }
        return this.getStringBounds(arr, 0, arr.length, frc);
    }

    public Rectangle2D getMaxCharBounds(FontRenderContext frc) {
        float[] metrics = new float[4];
        this.getFont2D().getFontMetrics(this, frc, metrics);
        return new Rectangle2D.Float(0.0f, -metrics[0], metrics[3], metrics[0] + metrics[1] + metrics[2]);
    }

    public GlyphVector createGlyphVector(FontRenderContext frc, String str) {
        return new StandardGlyphVector(this, str, frc);
    }

    public GlyphVector createGlyphVector(FontRenderContext frc, char[] chars) {
        return new StandardGlyphVector(this, chars, frc);
    }

    public GlyphVector createGlyphVector(FontRenderContext frc, CharacterIterator ci) {
        return new StandardGlyphVector(this, ci, frc);
    }

    public GlyphVector createGlyphVector(FontRenderContext frc, int[] glyphCodes) {
        return new StandardGlyphVector(this, glyphCodes, frc);
    }

    public GlyphVector layoutGlyphVector(FontRenderContext frc, char[] text, int start, int limit, int flags) {
        GlyphLayout gl = GlyphLayout.get(null);
        StandardGlyphVector gv = gl.layout(this, frc, text, start, limit - start, flags, null);
        GlyphLayout.done(gl);
        return gv;
    }

    private static void applyTransform(AffineTransform trans, AttributeValues values) {
        if (trans == null) {
            throw new IllegalArgumentException("transform must not be null");
        }
        values.setTransform(trans);
    }

    private static void applyStyle(int style, AttributeValues values) {
        values.setWeight((style & 1) != 0 ? 2.0f : 1.0f);
        values.setPosture((style & 2) != 0 ? 0.2f : 0.0f);
    }

    private static native void initIDs();

    static {
        Toolkit.loadLibraries();
        Font.initIDs();
        FontAccess.setFontAccess(new FontAccessImpl());
        identityTx = new AffineTransform();
        RECOGNIZED_MASK = AttributeValues.MASK_ALL & ~AttributeValues.getMask(EAttribute.EFONT);
        PRIMARY_MASK = AttributeValues.getMask(EAttribute.EFAMILY, EAttribute.EWEIGHT, EAttribute.EWIDTH, EAttribute.EPOSTURE, EAttribute.ESIZE, EAttribute.ETRANSFORM, EAttribute.ESUPERSCRIPT, EAttribute.ETRACKING);
        SECONDARY_MASK = RECOGNIZED_MASK & ~PRIMARY_MASK;
        LAYOUT_MASK = AttributeValues.getMask(EAttribute.ECHAR_REPLACEMENT, EAttribute.EFOREGROUND, EAttribute.EBACKGROUND, EAttribute.EUNDERLINE, EAttribute.ESTRIKETHROUGH, EAttribute.ERUN_DIRECTION, EAttribute.EBIDI_EMBEDDING, EAttribute.EJUSTIFICATION, EAttribute.EINPUT_METHOD_HIGHLIGHT, EAttribute.EINPUT_METHOD_UNDERLINE, EAttribute.ESWAP_COLORS, EAttribute.ENUMERIC_SHAPING, EAttribute.EKERNING, EAttribute.ELIGATURES, EAttribute.ETRACKING, EAttribute.ESUPERSCRIPT);
        EXTRA_MASK = AttributeValues.getMask(EAttribute.ETRANSFORM, EAttribute.ESUPERSCRIPT, EAttribute.EWIDTH);
        ssinfo = new float[]{0.0f, 0.375f, 0.625f, 0.7916667f, 0.9027778f, 0.9768519f, 1.0262346f, 1.0591564f};
    }

    private static class FontAccessImpl
    extends FontAccess {
        private FontAccessImpl() {
        }

        @Override
        public Font2D getFont2D(Font font) {
            return font.getFont2D();
        }

        @Override
        public void setFont2D(Font font, Font2DHandle handle) {
            font.font2DHandle = handle;
        }

        @Override
        public void setCreatedFont(Font font) {
            font.createdFont = true;
        }

        @Override
        public boolean isCreatedFont(Font font) {
            return font.createdFont;
        }

        @Override
        public FontPeer getFontPeer(Font font) {
            return font.getFontPeer();
        }
    }
}

