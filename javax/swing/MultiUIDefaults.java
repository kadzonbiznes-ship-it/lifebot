/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.UIDefaults;

class MultiUIDefaults
extends UIDefaults {
    private UIDefaults[] tables;

    public MultiUIDefaults(UIDefaults[] defaults) {
        this.tables = defaults;
    }

    public MultiUIDefaults() {
        this.tables = new UIDefaults[0];
    }

    @Override
    public Object get(Object key) {
        Object value = super.get(key);
        if (value != null) {
            return value;
        }
        for (UIDefaults table : this.tables) {
            Object object = value = table != null ? table.get(key) : null;
            if (value == null) continue;
            return value;
        }
        return null;
    }

    @Override
    public Object get(Object key, Locale l) {
        Object value = super.get(key, l);
        if (value != null) {
            return value;
        }
        for (UIDefaults table : this.tables) {
            Object object = value = table != null ? table.get(key, l) : null;
            if (value == null) continue;
            return value;
        }
        return null;
    }

    @Override
    public int size() {
        return this.entrySet().size();
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public Enumeration<Object> keys() {
        return new MultiUIDefaultsEnumerator(MultiUIDefaultsEnumerator.Type.KEYS, this.entrySet());
    }

    @Override
    public Set<Object> keySet() {
        HashSet<Object> set = new HashSet<Object>();
        for (int i = this.tables.length - 1; i >= 0; --i) {
            if (this.tables[i] == null) continue;
            set.addAll(this.tables[i].keySet());
        }
        set.addAll(super.keySet());
        return set;
    }

    @Override
    public Enumeration<Object> elements() {
        return new MultiUIDefaultsEnumerator(MultiUIDefaultsEnumerator.Type.ELEMENTS, this.entrySet());
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        HashSet<Map.Entry<Object, Object>> set = new HashSet<Map.Entry<Object, Object>>();
        for (int i = this.tables.length - 1; i >= 0; --i) {
            if (this.tables[i] == null) continue;
            set.addAll(this.tables[i].entrySet());
        }
        set.addAll(super.entrySet());
        return set;
    }

    @Override
    protected void getUIError(String msg) {
        if (this.tables != null && this.tables.length > 0 && this.tables[0] != null) {
            this.tables[0].getUIError(msg);
        } else {
            super.getUIError(msg);
        }
    }

    @Override
    public Object remove(Object key) {
        Object value = null;
        for (int i = this.tables.length - 1; i >= 0; --i) {
            Object v;
            if (this.tables[i] == null || (v = this.tables[i].remove(key)) == null) continue;
            value = v;
        }
        Object v = super.remove(key);
        if (v != null) {
            value = v;
        }
        return value;
    }

    @Override
    public void clear() {
        super.clear();
        for (UIDefaults table : this.tables) {
            if (table == null) continue;
            table.clear();
        }
    }

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Enumeration<Object> keys = this.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            sb.append(String.valueOf(key) + "=" + String.valueOf(this.get(key)) + ", ");
        }
        int length = sb.length();
        if (length > 1) {
            sb.delete(length - 2, length);
        }
        sb.append("}");
        return sb.toString();
    }

    private static class MultiUIDefaultsEnumerator
    implements Enumeration<Object> {
        private Iterator<Map.Entry<Object, Object>> iterator;
        private Type type;

        MultiUIDefaultsEnumerator(Type type, Set<Map.Entry<Object, Object>> entries) {
            this.type = type;
            this.iterator = entries.iterator();
        }

        @Override
        public boolean hasMoreElements() {
            return this.iterator.hasNext();
        }

        @Override
        public Object nextElement() {
            switch (this.type.ordinal()) {
                case 0: {
                    return this.iterator.next().getKey();
                }
                case 1: {
                    return this.iterator.next().getValue();
                }
            }
            return null;
        }

        public static enum Type {
            KEYS,
            ELEMENTS;

        }
    }
}

