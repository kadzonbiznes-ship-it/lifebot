/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text.html;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.CSS;

class MuxingAttributeSet
implements AttributeSet,
Serializable {
    private AttributeSet[] attrs;

    public MuxingAttributeSet(AttributeSet[] attrs) {
        this.attrs = attrs;
    }

    protected MuxingAttributeSet() {
    }

    protected synchronized void setAttributes(AttributeSet[] attrs) {
        this.attrs = attrs;
    }

    protected synchronized AttributeSet[] getAttributes() {
        return this.attrs;
    }

    protected synchronized void insertAttributeSetAt(AttributeSet as, int index) {
        int numAttrs = this.attrs.length;
        AttributeSet[] newAttrs = new AttributeSet[numAttrs + 1];
        if (index < numAttrs) {
            if (index > 0) {
                System.arraycopy(this.attrs, 0, newAttrs, 0, index);
                System.arraycopy(this.attrs, index, newAttrs, index + 1, numAttrs - index);
            } else {
                System.arraycopy(this.attrs, 0, newAttrs, 1, numAttrs);
            }
        } else {
            System.arraycopy(this.attrs, 0, newAttrs, 0, numAttrs);
        }
        newAttrs[index] = as;
        this.attrs = newAttrs;
    }

    protected synchronized void removeAttributeSetAt(int index) {
        int numAttrs = this.attrs.length;
        AttributeSet[] newAttrs = new AttributeSet[numAttrs - 1];
        if (numAttrs > 0) {
            if (index == 0) {
                System.arraycopy(this.attrs, 1, newAttrs, 0, numAttrs - 1);
            } else if (index < numAttrs - 1) {
                System.arraycopy(this.attrs, 0, newAttrs, 0, index);
                System.arraycopy(this.attrs, index + 1, newAttrs, index, numAttrs - index - 1);
            } else {
                System.arraycopy(this.attrs, 0, newAttrs, 0, numAttrs - 1);
            }
        }
        this.attrs = newAttrs;
    }

    @Override
    public int getAttributeCount() {
        AttributeSet[] as = this.getAttributes();
        int n = 0;
        for (int i = 0; i < as.length; ++i) {
            n += as[i].getAttributeCount();
        }
        return n;
    }

    @Override
    public boolean isDefined(Object key) {
        AttributeSet[] as = this.getAttributes();
        for (int i = 0; i < as.length; ++i) {
            if (!as[i].isDefined(key)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isEqual(AttributeSet attr) {
        return this.getAttributeCount() == attr.getAttributeCount() && this.containsAttributes(attr);
    }

    @Override
    public AttributeSet copyAttributes() {
        AttributeSet[] as = this.getAttributes();
        SimpleAttributeSet a = new SimpleAttributeSet();
        boolean n = false;
        for (int i = as.length - 1; i >= 0; --i) {
            a.addAttributes(as[i]);
        }
        return a;
    }

    @Override
    public Object getAttribute(Object key) {
        AttributeSet[] as = this.getAttributes();
        int n = as.length;
        if (key != CSS.Attribute.TEXT_DECORATION) {
            for (int i = 0; i < n; ++i) {
                Object o = as[i].getAttribute(key);
                if (o == null) continue;
                return o;
            }
            return null;
        }
        String values = Arrays.stream(as).map(a -> a.getAttribute(key)).filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(","));
        return CSS.mergeTextDecoration(values);
    }

    @Override
    public Enumeration<?> getAttributeNames() {
        return new MuxingAttributeNameEnumeration();
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
        return null;
    }

    private class MuxingAttributeNameEnumeration
    implements Enumeration<Object> {
        private int attrIndex;
        private Enumeration<?> currentEnum;

        MuxingAttributeNameEnumeration() {
            this.updateEnum();
        }

        @Override
        public boolean hasMoreElements() {
            if (this.currentEnum == null) {
                return false;
            }
            return this.currentEnum.hasMoreElements();
        }

        @Override
        public Object nextElement() {
            if (this.currentEnum == null) {
                throw new NoSuchElementException("No more names");
            }
            Object retObject = this.currentEnum.nextElement();
            if (!this.currentEnum.hasMoreElements()) {
                this.updateEnum();
            }
            return retObject;
        }

        void updateEnum() {
            AttributeSet[] as = MuxingAttributeSet.this.getAttributes();
            this.currentEnum = null;
            while (this.currentEnum == null && this.attrIndex < as.length) {
                this.currentEnum = as[this.attrIndex++].getAttributeNames();
                if (this.currentEnum.hasMoreElements()) continue;
                this.currentEnum = null;
            }
        }
    }
}

