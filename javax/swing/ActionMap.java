/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import javax.swing.Action;
import javax.swing.ArrayTable;

public class ActionMap
implements Serializable {
    private transient ArrayTable arrayTable;
    private ActionMap parent;

    public void setParent(ActionMap map) {
        this.parent = map;
    }

    public ActionMap getParent() {
        return this.parent;
    }

    public void put(Object key, Action action) {
        if (key == null) {
            return;
        }
        if (action == null) {
            this.remove(key);
        } else {
            if (this.arrayTable == null) {
                this.arrayTable = new ArrayTable();
            }
            this.arrayTable.put(key, action);
        }
    }

    public Action get(Object key) {
        ActionMap parent;
        Action value;
        Action action = value = this.arrayTable == null ? null : (Action)this.arrayTable.get(key);
        if (value == null && (parent = this.getParent()) != null) {
            return parent.get(key);
        }
        return value;
    }

    public void remove(Object key) {
        if (this.arrayTable != null) {
            this.arrayTable.remove(key);
        }
    }

    public void clear() {
        if (this.arrayTable != null) {
            this.arrayTable.clear();
        }
    }

    public Object[] keys() {
        if (this.arrayTable == null) {
            return null;
        }
        return this.arrayTable.getKeys(null);
    }

    public int size() {
        if (this.arrayTable == null) {
            return 0;
        }
        return this.arrayTable.size();
    }

    public Object[] allKeys() {
        int counter;
        int count = this.size();
        ActionMap parent = this.getParent();
        if (count == 0) {
            if (parent != null) {
                return parent.allKeys();
            }
            return this.keys();
        }
        if (parent == null) {
            return this.keys();
        }
        Object[] keys = this.keys();
        Object[] pKeys = parent.allKeys();
        if (pKeys == null) {
            return keys;
        }
        if (keys == null) {
            return pKeys;
        }
        HashMap<Object, Object> keyMap = new HashMap<Object, Object>();
        for (counter = keys.length - 1; counter >= 0; --counter) {
            keyMap.put(keys[counter], keys[counter]);
        }
        for (counter = pKeys.length - 1; counter >= 0; --counter) {
            keyMap.put(pKeys[counter], pKeys[counter]);
        }
        return keyMap.keySet().toArray();
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        ArrayTable.writeArrayTable(s, this.arrayTable);
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        for (int counter = s.readInt() - 1; counter >= 0; --counter) {
            this.put(s.readObject(), (Action)s.readObject());
        }
    }
}

