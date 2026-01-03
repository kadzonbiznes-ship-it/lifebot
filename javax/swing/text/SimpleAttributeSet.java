/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class SimpleAttributeSet
implements MutableAttributeSet,
Serializable,
Cloneable {
    private static final long serialVersionUID = -6631553454711782652L;
    public static final AttributeSet EMPTY = new EmptyAttributeSet();
    private transient LinkedHashMap<Object, Object> table = new LinkedHashMap(3);

    public SimpleAttributeSet() {
    }

    public SimpleAttributeSet(AttributeSet source) {
        this.addAttributes(source);
    }

    public boolean isEmpty() {
        return this.table.isEmpty();
    }

    @Override
    public int getAttributeCount() {
        return this.table.size();
    }

    @Override
    public boolean isDefined(Object attrName) {
        return this.table.containsKey(attrName);
    }

    @Override
    public boolean isEqual(AttributeSet attr) {
        return this.getAttributeCount() == attr.getAttributeCount() && this.containsAttributes(attr);
    }

    @Override
    public AttributeSet copyAttributes() {
        return (AttributeSet)this.clone();
    }

    @Override
    public Enumeration<?> getAttributeNames() {
        return Collections.enumeration(this.table.keySet());
    }

    @Override
    public Object getAttribute(Object name) {
        AttributeSet parent;
        Object value = this.table.get(name);
        if (value == null && (parent = this.getResolveParent()) != null) {
            value = parent.getAttribute(name);
        }
        return value;
    }

    @Override
    public boolean containsAttribute(Object name, Object value) {
        return value.equals(this.getAttribute(name));
    }

    @Override
    public boolean containsAttributes(AttributeSet attributes) {
        boolean result = true;
        Enumeration<?> names = attributes.getAttributeNames();
        while (result && names.hasMoreElements()) {
            Object name = names.nextElement();
            result = attributes.getAttribute(name).equals(this.getAttribute(name));
        }
        return result;
    }

    @Override
    public void addAttribute(Object name, Object value) {
        this.table.put(name, value);
    }

    @Override
    public void addAttributes(AttributeSet attributes) {
        Enumeration<?> names = attributes.getAttributeNames();
        while (names.hasMoreElements()) {
            Object name = names.nextElement();
            this.addAttribute(name, attributes.getAttribute(name));
        }
    }

    @Override
    public void removeAttribute(Object name) {
        this.table.remove(name);
    }

    @Override
    public void removeAttributes(Enumeration<?> names) {
        while (names.hasMoreElements()) {
            this.removeAttribute(names.nextElement());
        }
    }

    @Override
    public void removeAttributes(AttributeSet attributes) {
        if (attributes == this) {
            this.table.clear();
        } else {
            Enumeration<?> names = attributes.getAttributeNames();
            while (names.hasMoreElements()) {
                Object name = names.nextElement();
                Object value = attributes.getAttribute(name);
                if (!value.equals(this.getAttribute(name))) continue;
                this.removeAttribute(name);
            }
        }
    }

    @Override
    public AttributeSet getResolveParent() {
        return (AttributeSet)this.table.get(StyleConstants.ResolveAttribute);
    }

    @Override
    public void setResolveParent(AttributeSet parent) {
        this.addAttribute(StyleConstants.ResolveAttribute, parent);
    }

    public Object clone() {
        SimpleAttributeSet attr;
        try {
            attr = (SimpleAttributeSet)super.clone();
            attr.table = (LinkedHashMap)this.table.clone();
        }
        catch (CloneNotSupportedException cnse) {
            attr = null;
        }
        return attr;
    }

    public int hashCode() {
        return this.table.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AttributeSet) {
            AttributeSet attrs = (AttributeSet)obj;
            return this.isEqual(attrs);
        }
        return false;
    }

    public String toString() {
        Object s = "";
        Enumeration<?> names = this.getAttributeNames();
        while (names.hasMoreElements()) {
            Object key = names.nextElement();
            Object value = this.getAttribute(key);
            if (value instanceof AttributeSet) {
                s = (String)s + String.valueOf(key) + "=**AttributeSet** ";
                continue;
            }
            s = (String)s + String.valueOf(key) + "=" + String.valueOf(value) + " ";
        }
        return s;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        StyleContext.writeAttributeSet(s, this);
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        this.table = new LinkedHashMap(3);
        StyleContext.readAttributeSet(s, this);
    }

    static class EmptyAttributeSet
    implements AttributeSet,
    Serializable {
        private static final long serialVersionUID = -8714803568785904228L;

        EmptyAttributeSet() {
        }

        @Override
        public int getAttributeCount() {
            return 0;
        }

        @Override
        public boolean isDefined(Object attrName) {
            return false;
        }

        @Override
        public boolean isEqual(AttributeSet attr) {
            return attr.getAttributeCount() == 0;
        }

        @Override
        public AttributeSet copyAttributes() {
            return this;
        }

        @Override
        public Object getAttribute(Object key) {
            return null;
        }

        @Override
        public Enumeration<?> getAttributeNames() {
            return Collections.emptyEnumeration();
        }

        @Override
        public boolean containsAttribute(Object name, Object value) {
            return false;
        }

        @Override
        public boolean containsAttributes(AttributeSet attributes) {
            return attributes.getAttributeCount() == 0;
        }

        @Override
        public AttributeSet getResolveParent() {
            return null;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            return obj instanceof AttributeSet && ((AttributeSet)obj).getAttributeCount() == 0;
        }

        public int hashCode() {
            return 0;
        }
    }
}

