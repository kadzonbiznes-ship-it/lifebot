/*
 * Decompiled with CFR 0.152.
 */
package java.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

abstract class ChangeListenerMap<L extends EventListener> {
    private Map<String, L[]> map;

    ChangeListenerMap() {
    }

    protected abstract L[] newArray(int var1);

    protected abstract L newProxy(String var1, L var2);

    public final synchronized void add(String name, L listener) {
        EventListener[] array;
        if (this.map == null) {
            this.map = new HashMap<String, L[]>();
        }
        int size = (array = (EventListener[])this.map.get(name)) != null ? array.length : 0;
        EventListener[] clone = this.newArray(size + 1);
        clone[size] = listener;
        if (array != null) {
            System.arraycopy(array, 0, clone, 0, size);
        }
        this.map.put(name, clone);
    }

    public final synchronized void remove(String name, L listener) {
        EventListener[] array;
        if (this.map != null && (array = (EventListener[])this.map.get(name)) != null) {
            for (int i = 0; i < array.length; ++i) {
                if (!listener.equals(array[i])) continue;
                int size = array.length - 1;
                if (size > 0) {
                    EventListener[] clone = this.newArray(size);
                    System.arraycopy(array, 0, clone, 0, i);
                    System.arraycopy(array, i + 1, clone, i, size - i);
                    this.map.put(name, clone);
                    break;
                }
                this.map.remove(name);
                if (!this.map.isEmpty()) break;
                this.map = null;
                break;
            }
        }
    }

    public final synchronized L[] get(String name) {
        return this.map != null ? (EventListener[])this.map.get(name) : null;
    }

    public final void set(String name, L[] listeners) {
        if (listeners != null) {
            if (this.map == null) {
                this.map = new HashMap<String, L[]>();
            }
            this.map.put(name, listeners);
        } else if (this.map != null) {
            this.map.remove(name);
            if (this.map.isEmpty()) {
                this.map = null;
            }
        }
    }

    public final synchronized L[] getListeners() {
        if (this.map == null) {
            return this.newArray(0);
        }
        ArrayList<Object> list = new ArrayList<Object>();
        EventListener[] listeners = (EventListener[])this.map.get(null);
        if (listeners != null) {
            for (EventListener listener : listeners) {
                list.add(listener);
            }
        }
        for (Map.Entry entry : this.map.entrySet()) {
            String name = (String)entry.getKey();
            if (name == null) continue;
            for (EventListener listener : (EventListener[])entry.getValue()) {
                list.add(this.newProxy(name, listener));
            }
        }
        return list.toArray(this.newArray(list.size()));
    }

    public final L[] getListeners(String name) {
        EventListener[] listeners;
        if (name != null && (listeners = this.get(name)) != null) {
            return (EventListener[])listeners.clone();
        }
        return this.newArray(0);
    }

    public final synchronized boolean hasListeners(String name) {
        if (this.map == null) {
            return false;
        }
        EventListener[] array = (EventListener[])this.map.get(null);
        return array != null || name != null && null != this.map.get(name);
    }

    public final Set<Map.Entry<String, L[]>> getEntries() {
        return this.map != null ? this.map.entrySet() : Collections.emptySet();
    }

    public abstract L extract(L var1);
}

