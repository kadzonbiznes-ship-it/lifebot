/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import javax.swing.ArrayTable;
import javax.swing.KeyStroke;

public class InputMap
implements Serializable {
    private transient ArrayTable arrayTable;
    private InputMap parent;

    public void setParent(InputMap map) {
        this.parent = map;
    }

    public InputMap getParent() {
        return this.parent;
    }

    public void put(KeyStroke keyStroke, Object actionMapKey) {
        if (keyStroke == null) {
            return;
        }
        if (actionMapKey == null) {
            this.remove(keyStroke);
        } else {
            if (this.arrayTable == null) {
                this.arrayTable = new ArrayTable();
            }
            this.arrayTable.put(keyStroke, actionMapKey);
        }
    }

    public Object get(KeyStroke keyStroke) {
        InputMap parent;
        if (this.arrayTable == null) {
            InputMap parent2 = this.getParent();
            if (parent2 != null) {
                return parent2.get(keyStroke);
            }
            return null;
        }
        Object value = this.arrayTable.get(keyStroke);
        if (value == null && (parent = this.getParent()) != null) {
            return parent.get(keyStroke);
        }
        return value;
    }

    public void remove(KeyStroke key) {
        if (this.arrayTable != null) {
            this.arrayTable.remove(key);
        }
    }

    public void clear() {
        if (this.arrayTable != null) {
            this.arrayTable.clear();
        }
    }

    public KeyStroke[] keys() {
        if (this.arrayTable == null) {
            return null;
        }
        Object[] keys = new KeyStroke[this.arrayTable.size()];
        this.arrayTable.getKeys(keys);
        return keys;
    }

    public int size() {
        if (this.arrayTable == null) {
            return 0;
        }
        return this.arrayTable.size();
    }

    public KeyStroke[] allKeys() {
        int counter;
        int count = this.size();
        InputMap parent = this.getParent();
        if (count == 0) {
            if (parent != null) {
                return parent.allKeys();
            }
            return this.keys();
        }
        if (parent == null) {
            return this.keys();
        }
        KeyStroke[] keys = this.keys();
        KeyStroke[] pKeys = parent.allKeys();
        if (pKeys == null) {
            return keys;
        }
        if (keys == null) {
            return pKeys;
        }
        HashMap<KeyStroke, KeyStroke> keyMap = new HashMap<KeyStroke, KeyStroke>();
        for (counter = keys.length - 1; counter >= 0; --counter) {
            keyMap.put(keys[counter], keys[counter]);
        }
        for (counter = pKeys.length - 1; counter >= 0; --counter) {
            keyMap.put(pKeys[counter], pKeys[counter]);
        }
        KeyStroke[] allKeys = new KeyStroke[keyMap.size()];
        return keyMap.keySet().toArray(allKeys);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        ArrayTable.writeArrayTable(s, this.arrayTable);
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        for (int counter = s.readInt() - 1; counter >= 0; --counter) {
            this.put((KeyStroke)s.readObject(), s.readObject());
        }
    }
}

