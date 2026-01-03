/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import sun.util.ResourceBundleEnumeration;

public abstract class ListResourceBundle
extends ResourceBundle {
    private volatile Map<String, Object> lookup;

    @Override
    public final Object handleGetObject(String key) {
        if (this.lookup == null) {
            this.loadLookup();
        }
        if (key == null) {
            throw new NullPointerException();
        }
        return this.lookup.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        if (this.lookup == null) {
            this.loadLookup();
        }
        ResourceBundle parent = this.parent;
        return new ResourceBundleEnumeration(this.lookup.keySet(), parent != null ? parent.getKeys() : null);
    }

    @Override
    protected Set<String> handleKeySet() {
        if (this.lookup == null) {
            this.loadLookup();
        }
        return this.lookup.keySet();
    }

    protected abstract Object[][] getContents();

    private synchronized void loadLookup() {
        if (this.lookup != null) {
            return;
        }
        Object[][] contents = this.getContents();
        HashMap<String, Object> temp = HashMap.newHashMap(contents.length);
        for (Object[] content : contents) {
            String key = (String)content[0];
            Object value = content[1];
            if (key == null || value == null) {
                throw new NullPointerException();
            }
            temp.put(key, value);
        }
        this.lookup = temp;
    }
}

