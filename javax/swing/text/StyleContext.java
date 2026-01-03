/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import sun.font.FontUtilities;

public class StyleContext
implements Serializable,
AbstractDocument.AttributeContext {
    private static StyleContext defaultContext;
    public static final String DEFAULT_STYLE = "default";
    private static Hashtable<Object, String> freezeKeyMap;
    private static Hashtable<String, Object> thawKeyMap;
    private Style styles;
    private transient FontKey fontSearch = new FontKey(null, 0, 0);
    private transient Hashtable<FontKey, Font> fontTable = new Hashtable();
    private transient Map<SmallAttributeSet, WeakReference<SmallAttributeSet>> attributesPool = Collections.synchronizedMap(new WeakHashMap());
    private transient MutableAttributeSet search = new SimpleAttributeSet();
    private int unusedSets;
    static final int THRESHOLD = 9;

    public static final StyleContext getDefaultStyleContext() {
        if (defaultContext == null) {
            defaultContext = new StyleContext();
        }
        return defaultContext;
    }

    public StyleContext() {
        this.styles = new NamedStyle(null);
        this.addStyle(DEFAULT_STYLE, null);
    }

    public Style addStyle(String nm, Style parent) {
        NamedStyle style = new NamedStyle(nm, parent);
        if (nm != null) {
            this.styles.addAttribute(nm, style);
        }
        return style;
    }

    public void removeStyle(String nm) {
        this.styles.removeAttribute(nm);
    }

    public Style getStyle(String nm) {
        return (Style)this.styles.getAttribute(nm);
    }

    public Enumeration<?> getStyleNames() {
        return this.styles.getAttributeNames();
    }

    public void addChangeListener(ChangeListener l) {
        this.styles.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        this.styles.removeChangeListener(l);
    }

    public ChangeListener[] getChangeListeners() {
        return ((NamedStyle)this.styles).getChangeListeners();
    }

    public Font getFont(AttributeSet attr) {
        int style = 0;
        if (StyleConstants.isBold(attr)) {
            style |= 1;
        }
        if (StyleConstants.isItalic(attr)) {
            style |= 2;
        }
        String family = StyleConstants.getFontFamily(attr);
        int size = StyleConstants.getFontSize(attr);
        if (StyleConstants.isSuperscript(attr) || StyleConstants.isSubscript(attr)) {
            size -= 2;
        }
        return this.getFont(family, style, size);
    }

    public Color getForeground(AttributeSet attr) {
        return StyleConstants.getForeground(attr);
    }

    public Color getBackground(AttributeSet attr) {
        return StyleConstants.getBackground(attr);
    }

    public Font getFont(String family, int style, int size) {
        this.fontSearch.setValue(family, style, size);
        Font f = this.fontTable.get(this.fontSearch);
        if (f == null) {
            Style defaultStyle = this.getStyle(DEFAULT_STYLE);
            if (defaultStyle != null) {
                String FONT_ATTRIBUTE_KEY = "FONT_ATTRIBUTE_KEY";
                Font defaultFont = (Font)defaultStyle.getAttribute("FONT_ATTRIBUTE_KEY");
                if (defaultFont != null && defaultFont.getFamily().equalsIgnoreCase(family)) {
                    f = defaultFont.deriveFont(style, size);
                }
            }
            if (f == null) {
                f = new Font(family, style, size);
            }
            if (!FontUtilities.fontSupportsDefaultEncoding(f)) {
                f = FontUtilities.getCompositeFontUIResource(f);
            }
            FontKey key = new FontKey(family, style, size);
            this.fontTable.put(key, f);
        }
        return f;
    }

    public FontMetrics getFontMetrics(Font f) {
        return Toolkit.getDefaultToolkit().getFontMetrics(f);
    }

    @Override
    public synchronized AttributeSet addAttribute(AttributeSet old, Object name, Object value) {
        if (old.getAttributeCount() + 1 <= this.getCompressionThreshold()) {
            this.search.removeAttributes(this.search);
            this.search.addAttributes(old);
            this.search.addAttribute(name, value);
            this.reclaim(old);
            return this.getImmutableUniqueSet();
        }
        MutableAttributeSet ma = this.getMutableAttributeSet(old);
        ma.addAttribute(name, value);
        return ma;
    }

    @Override
    public synchronized AttributeSet addAttributes(AttributeSet old, AttributeSet attr) {
        if (old.getAttributeCount() + attr.getAttributeCount() <= this.getCompressionThreshold()) {
            this.search.removeAttributes(this.search);
            this.search.addAttributes(old);
            this.search.addAttributes(attr);
            this.reclaim(old);
            return this.getImmutableUniqueSet();
        }
        MutableAttributeSet ma = this.getMutableAttributeSet(old);
        ma.addAttributes(attr);
        return ma;
    }

    @Override
    public synchronized AttributeSet removeAttribute(AttributeSet old, Object name) {
        if (old.getAttributeCount() - 1 <= this.getCompressionThreshold()) {
            this.search.removeAttributes(this.search);
            this.search.addAttributes(old);
            this.search.removeAttribute(name);
            this.reclaim(old);
            return this.getImmutableUniqueSet();
        }
        MutableAttributeSet ma = this.getMutableAttributeSet(old);
        ma.removeAttribute(name);
        return ma;
    }

    @Override
    public synchronized AttributeSet removeAttributes(AttributeSet old, Enumeration<?> names) {
        if (old.getAttributeCount() <= this.getCompressionThreshold()) {
            this.search.removeAttributes(this.search);
            this.search.addAttributes(old);
            this.search.removeAttributes(names);
            this.reclaim(old);
            return this.getImmutableUniqueSet();
        }
        MutableAttributeSet ma = this.getMutableAttributeSet(old);
        ma.removeAttributes(names);
        return ma;
    }

    @Override
    public synchronized AttributeSet removeAttributes(AttributeSet old, AttributeSet attrs) {
        if (old.getAttributeCount() <= this.getCompressionThreshold()) {
            this.search.removeAttributes(this.search);
            this.search.addAttributes(old);
            this.search.removeAttributes(attrs);
            this.reclaim(old);
            return this.getImmutableUniqueSet();
        }
        MutableAttributeSet ma = this.getMutableAttributeSet(old);
        ma.removeAttributes(attrs);
        return ma;
    }

    @Override
    public AttributeSet getEmptySet() {
        return SimpleAttributeSet.EMPTY;
    }

    @Override
    public void reclaim(AttributeSet a) {
        if (SwingUtilities.isEventDispatchThread()) {
            this.attributesPool.size();
        }
    }

    protected int getCompressionThreshold() {
        return 9;
    }

    protected SmallAttributeSet createSmallAttributeSet(AttributeSet a) {
        return new SmallAttributeSet(this, a);
    }

    protected MutableAttributeSet createLargeAttributeSet(AttributeSet a) {
        return new SimpleAttributeSet(a);
    }

    synchronized void removeUnusedSets() {
        this.attributesPool.size();
    }

    AttributeSet getImmutableUniqueSet() {
        SmallAttributeSet a;
        SmallAttributeSet key = this.createSmallAttributeSet(this.search);
        WeakReference<SmallAttributeSet> reference = this.attributesPool.get(key);
        if (reference == null || (a = (SmallAttributeSet)reference.get()) == null) {
            a = key;
            this.attributesPool.put(a, new WeakReference<SmallAttributeSet>(a));
        }
        return a;
    }

    MutableAttributeSet getMutableAttributeSet(AttributeSet a) {
        if (a instanceof MutableAttributeSet && a != SimpleAttributeSet.EMPTY) {
            return (MutableAttributeSet)a;
        }
        return this.createLargeAttributeSet(a);
    }

    public String toString() {
        this.removeUnusedSets();
        Object s = "";
        for (SmallAttributeSet set : this.attributesPool.keySet()) {
            s = (String)s + String.valueOf(set) + "\n";
        }
        return s;
    }

    public void writeAttributes(ObjectOutputStream out, AttributeSet a) throws IOException {
        StyleContext.writeAttributeSet(out, a);
    }

    public void readAttributes(ObjectInputStream in, MutableAttributeSet a) throws ClassNotFoundException, IOException {
        StyleContext.readAttributeSet(in, a);
    }

    public static void writeAttributeSet(ObjectOutputStream out, AttributeSet a) throws IOException {
        int n = a.getAttributeCount();
        out.writeInt(n);
        Enumeration<?> keys = a.getAttributeNames();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (key instanceof Serializable) {
                out.writeObject(key);
            } else {
                String ioFmt = freezeKeyMap.get(key);
                if (ioFmt == null) {
                    throw new NotSerializableException(key.getClass().getName() + " is not serializable as a key in an AttributeSet");
                }
                out.writeObject(ioFmt);
            }
            Object value = a.getAttribute(key);
            String ioFmt = freezeKeyMap.get(value);
            if (value instanceof Serializable) {
                out.writeObject(ioFmt != null ? ioFmt : value);
                continue;
            }
            if (ioFmt == null) {
                throw new NotSerializableException(value.getClass().getName() + " is not serializable as a value in an AttributeSet");
            }
            out.writeObject(ioFmt);
        }
    }

    public static void readAttributeSet(ObjectInputStream in, MutableAttributeSet a) throws ClassNotFoundException, IOException {
        int n = in.readInt();
        for (int i = 0; i < n; ++i) {
            Object key = in.readObject();
            Object value = in.readObject();
            if (thawKeyMap != null) {
                Object staticValue;
                Object staticKey = thawKeyMap.get(key);
                if (staticKey != null) {
                    key = staticKey;
                }
                if ((staticValue = thawKeyMap.get(value)) != null) {
                    value = staticValue;
                }
            }
            a.addAttribute(key, value);
        }
    }

    public static void registerStaticAttributeKey(Object key) {
        String ioFmt = key.getClass().getName() + "." + key.toString();
        if (freezeKeyMap == null) {
            freezeKeyMap = new Hashtable();
            thawKeyMap = new Hashtable();
        }
        freezeKeyMap.put(key, ioFmt);
        thawKeyMap.put(ioFmt, key);
    }

    public static Object getStaticAttribute(Object key) {
        if (thawKeyMap == null || key == null) {
            return null;
        }
        return thawKeyMap.get(key);
    }

    public static Object getStaticAttributeKey(Object key) {
        return key.getClass().getName() + "." + key.toString();
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        this.removeUnusedSets();
        s.defaultWriteObject();
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        this.fontSearch = new FontKey(null, 0, 0);
        this.fontTable = new Hashtable();
        this.search = new SimpleAttributeSet();
        this.attributesPool = Collections.synchronizedMap(new WeakHashMap());
        ObjectInputStream.GetField f = s.readFields();
        Style newStyles = (Style)f.get("styles", null);
        if (newStyles == null) {
            throw new InvalidObjectException("Null styles");
        }
        this.styles = newStyles;
        this.unusedSets = f.get("unusedSets", 0);
    }

    static {
        try {
            int n = StyleConstants.keys.length;
            for (int i = 0; i < n; ++i) {
                StyleContext.registerStaticAttributeKey(StyleConstants.keys[i]);
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static class FontKey {
        private String family;
        private int style;
        private int size;

        public FontKey(String family, int style, int size) {
            this.setValue(family, style, size);
        }

        public void setValue(String family, int style, int size) {
            this.family = family != null ? family.intern() : null;
            this.style = style;
            this.size = size;
        }

        public int hashCode() {
            int fhash = this.family != null ? this.family.hashCode() : 0;
            return fhash ^ this.style ^ this.size;
        }

        public boolean equals(Object obj) {
            if (obj instanceof FontKey) {
                FontKey font = (FontKey)obj;
                return this.size == font.size && this.style == font.style && this.family == font.family;
            }
            return false;
        }
    }

    public class NamedStyle
    implements Style,
    Serializable {
        protected EventListenerList listenerList = new EventListenerList();
        protected transient ChangeEvent changeEvent = null;
        private transient AttributeSet attributes;

        public NamedStyle(String name, Style parent) {
            this.attributes = StyleContext.this.getEmptySet();
            if (name != null) {
                this.setName(name);
            }
            if (parent != null) {
                this.setResolveParent(parent);
            }
        }

        public NamedStyle(Style parent) {
            this(null, parent);
        }

        public NamedStyle() {
            this.attributes = StyleContext.this.getEmptySet();
        }

        public String toString() {
            return "NamedStyle:" + this.getName() + " " + String.valueOf(this.attributes);
        }

        @Override
        public String getName() {
            if (this.isDefined(StyleConstants.NameAttribute)) {
                return this.getAttribute(StyleConstants.NameAttribute).toString();
            }
            return null;
        }

        public void setName(String name) {
            if (name != null) {
                this.addAttribute(StyleConstants.NameAttribute, name);
            }
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            this.listenerList.add(ChangeListener.class, l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            this.listenerList.remove(ChangeListener.class, l);
        }

        public ChangeListener[] getChangeListeners() {
            return (ChangeListener[])this.listenerList.getListeners(ChangeListener.class);
        }

        protected void fireStateChanged() {
            Object[] listeners = this.listenerList.getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] != ChangeListener.class) continue;
                if (this.changeEvent == null) {
                    this.changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i + 1]).stateChanged(this.changeEvent);
            }
        }

        public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
            return this.listenerList.getListeners(listenerType);
        }

        @Override
        public int getAttributeCount() {
            return this.attributes.getAttributeCount();
        }

        @Override
        public boolean isDefined(Object attrName) {
            return this.attributes.isDefined(attrName);
        }

        @Override
        public boolean isEqual(AttributeSet attr) {
            return this.attributes.isEqual(attr);
        }

        @Override
        public AttributeSet copyAttributes() {
            NamedStyle a = new NamedStyle();
            a.attributes = this.attributes.copyAttributes();
            return a;
        }

        @Override
        public Object getAttribute(Object attrName) {
            return this.attributes.getAttribute(attrName);
        }

        @Override
        public Enumeration<?> getAttributeNames() {
            return this.attributes.getAttributeNames();
        }

        @Override
        public boolean containsAttribute(Object name, Object value) {
            return this.attributes.containsAttribute(name, value);
        }

        @Override
        public boolean containsAttributes(AttributeSet attrs) {
            return this.attributes.containsAttributes(attrs);
        }

        @Override
        public AttributeSet getResolveParent() {
            return this.attributes.getResolveParent();
        }

        @Override
        public void addAttribute(Object name, Object value) {
            StyleContext context = StyleContext.this;
            this.attributes = context.addAttribute(this.attributes, name, value);
            this.fireStateChanged();
        }

        @Override
        public void addAttributes(AttributeSet attr) {
            StyleContext context = StyleContext.this;
            this.attributes = context.addAttributes(this.attributes, attr);
            this.fireStateChanged();
        }

        @Override
        public void removeAttribute(Object name) {
            StyleContext context = StyleContext.this;
            this.attributes = context.removeAttribute(this.attributes, name);
            this.fireStateChanged();
        }

        @Override
        public void removeAttributes(Enumeration<?> names) {
            StyleContext context = StyleContext.this;
            this.attributes = context.removeAttributes(this.attributes, names);
            this.fireStateChanged();
        }

        @Override
        public void removeAttributes(AttributeSet attrs) {
            StyleContext context = StyleContext.this;
            this.attributes = attrs == this ? context.getEmptySet() : context.removeAttributes(this.attributes, attrs);
            this.fireStateChanged();
        }

        @Override
        public void setResolveParent(AttributeSet parent) {
            if (parent != null) {
                this.addAttribute(StyleConstants.ResolveAttribute, parent);
            } else {
                this.removeAttribute(StyleConstants.ResolveAttribute);
            }
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            StyleContext.writeAttributeSet(s, this.attributes);
        }

        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            s.defaultReadObject();
            this.attributes = SimpleAttributeSet.EMPTY;
            StyleContext.readAttributeSet(s, this);
        }
    }

    public class SmallAttributeSet
    implements AttributeSet {
        Object[] attributes;
        AttributeSet resolveParent;

        public SmallAttributeSet(StyleContext this$0, Object[] attributes) {
            this.attributes = Arrays.copyOf(attributes, attributes.length);
            this.updateResolveParent();
        }

        public SmallAttributeSet(StyleContext this$0, AttributeSet attrs) {
            int n = attrs.getAttributeCount();
            Object[] tbl = new Object[2 * n];
            Enumeration<?> names = attrs.getAttributeNames();
            int i = 0;
            while (names.hasMoreElements()) {
                tbl[i] = names.nextElement();
                tbl[i + 1] = attrs.getAttribute(tbl[i]);
                i += 2;
            }
            this.attributes = tbl;
            this.updateResolveParent();
        }

        private void updateResolveParent() {
            this.resolveParent = null;
            Object[] tbl = this.attributes;
            for (int i = 0; i < tbl.length; i += 2) {
                if (tbl[i] != StyleConstants.ResolveAttribute) continue;
                this.resolveParent = (AttributeSet)tbl[i + 1];
                break;
            }
        }

        Object getLocalAttribute(Object nm) {
            if (nm == StyleConstants.ResolveAttribute) {
                return this.resolveParent;
            }
            Object[] tbl = this.attributes;
            for (int i = 0; i < tbl.length; i += 2) {
                if (!nm.equals(tbl[i])) continue;
                return tbl[i + 1];
            }
            return null;
        }

        public String toString() {
            Object s = "{";
            Object[] tbl = this.attributes;
            for (int i = 0; i < tbl.length; i += 2) {
                s = tbl[i + 1] instanceof AttributeSet ? (String)s + String.valueOf(tbl[i]) + "=AttributeSet," : (String)s + String.valueOf(tbl[i]) + "=" + String.valueOf(tbl[i + 1]) + ",";
            }
            s = (String)s + "}";
            return s;
        }

        public int hashCode() {
            int code = 0;
            Object[] tbl = this.attributes;
            for (int i = 1; i < tbl.length; i += 2) {
                code ^= tbl[i].hashCode();
            }
            return code;
        }

        public boolean equals(Object obj) {
            if (obj instanceof AttributeSet) {
                AttributeSet attrs = (AttributeSet)obj;
                return this.getAttributeCount() == attrs.getAttributeCount() && this.containsAttributes(attrs);
            }
            return false;
        }

        public Object clone() {
            return this;
        }

        @Override
        public int getAttributeCount() {
            return this.attributes.length / 2;
        }

        @Override
        public boolean isDefined(Object key) {
            Object[] a = this.attributes;
            int n = a.length;
            for (int i = 0; i < n; i += 2) {
                if (!key.equals(a[i])) continue;
                return true;
            }
            return false;
        }

        @Override
        public boolean isEqual(AttributeSet attr) {
            if (attr instanceof SmallAttributeSet) {
                return attr == this;
            }
            return this.getAttributeCount() == attr.getAttributeCount() && this.containsAttributes(attr);
        }

        @Override
        public AttributeSet copyAttributes() {
            return this;
        }

        @Override
        public Object getAttribute(Object key) {
            AttributeSet parent;
            Object value = this.getLocalAttribute(key);
            if (value == null && (parent = this.getResolveParent()) != null) {
                value = parent.getAttribute(key);
            }
            return value;
        }

        @Override
        public Enumeration<?> getAttributeNames() {
            return new KeyEnumeration(this.attributes);
        }

        @Override
        public boolean containsAttribute(Object name, Object value) {
            return value.equals(this.getAttribute(name));
        }

        @Override
        public boolean containsAttributes(AttributeSet attrs) {
            boolean result = true;
            Enumeration<?> names = attrs.getAttributeNames();
            while (result && names.hasMoreElements()) {
                Object name = names.nextElement();
                result = attrs.getAttribute(name).equals(this.getAttribute(name));
            }
            return result;
        }

        @Override
        public AttributeSet getResolveParent() {
            return this.resolveParent;
        }
    }

    static class KeyBuilder {
        private Vector<Object> keys = new Vector();
        private Vector<Object> data = new Vector();

        KeyBuilder() {
        }

        public void initialize(AttributeSet a) {
            if (a instanceof SmallAttributeSet) {
                this.initialize(((SmallAttributeSet)a).attributes);
            } else {
                this.keys.removeAllElements();
                this.data.removeAllElements();
                Enumeration<?> names = a.getAttributeNames();
                while (names.hasMoreElements()) {
                    Object name = names.nextElement();
                    this.addAttribute(name, a.getAttribute(name));
                }
            }
        }

        private void initialize(Object[] sorted) {
            this.keys.removeAllElements();
            this.data.removeAllElements();
            int n = sorted.length;
            for (int i = 0; i < n; i += 2) {
                this.keys.addElement(sorted[i]);
                this.data.addElement(sorted[i + 1]);
            }
        }

        public Object[] createTable() {
            int n = this.keys.size();
            Object[] tbl = new Object[2 * n];
            for (int i = 0; i < n; ++i) {
                int offs = 2 * i;
                tbl[offs] = this.keys.elementAt(i);
                tbl[offs + 1] = this.data.elementAt(i);
            }
            return tbl;
        }

        int getCount() {
            return this.keys.size();
        }

        public void addAttribute(Object key, Object value) {
            this.keys.addElement(key);
            this.data.addElement(value);
        }

        public void addAttributes(AttributeSet attr) {
            if (attr instanceof SmallAttributeSet) {
                Object[] tbl = ((SmallAttributeSet)attr).attributes;
                int n = tbl.length;
                for (int i = 0; i < n; i += 2) {
                    this.addAttribute(tbl[i], tbl[i + 1]);
                }
            } else {
                Enumeration<?> names = attr.getAttributeNames();
                while (names.hasMoreElements()) {
                    Object name = names.nextElement();
                    this.addAttribute(name, attr.getAttribute(name));
                }
            }
        }

        public void removeAttribute(Object key) {
            int n = this.keys.size();
            for (int i = 0; i < n; ++i) {
                if (!this.keys.elementAt(i).equals(key)) continue;
                this.keys.removeElementAt(i);
                this.data.removeElementAt(i);
                return;
            }
        }

        public void removeAttributes(Enumeration<?> names) {
            while (names.hasMoreElements()) {
                Object name = names.nextElement();
                this.removeAttribute(name);
            }
        }

        public void removeAttributes(AttributeSet attr) {
            Enumeration<?> names = attr.getAttributeNames();
            while (names.hasMoreElements()) {
                Object name = names.nextElement();
                Object value = attr.getAttribute(name);
                this.removeSearchAttribute(name, value);
            }
        }

        private void removeSearchAttribute(Object ikey, Object value) {
            int n = this.keys.size();
            for (int i = 0; i < n; ++i) {
                if (!this.keys.elementAt(i).equals(ikey)) continue;
                if (this.data.elementAt(i).equals(value)) {
                    this.keys.removeElementAt(i);
                    this.data.removeElementAt(i);
                }
                return;
            }
        }
    }

    static class KeyEnumeration
    implements Enumeration<Object> {
        Object[] attr;
        int i;

        KeyEnumeration(Object[] attr) {
            this.attr = attr;
            this.i = 0;
        }

        @Override
        public boolean hasMoreElements() {
            return this.i < this.attr.length;
        }

        @Override
        public Object nextElement() {
            if (this.i < this.attr.length) {
                Object o = this.attr[this.i];
                this.i += 2;
                return o;
            }
            throw new NoSuchElementException();
        }
    }
}

