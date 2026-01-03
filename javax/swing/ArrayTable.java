/*
 * Decompiled with CFR 0.152.
 */
package javax.swing;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.ClientPropertyKey;

class ArrayTable
implements Cloneable {
    private Object table = null;
    private static final int ARRAY_BOUNDARY = 8;

    ArrayTable() {
    }

    static void writeArrayTable(ObjectOutputStream s, ArrayTable table) throws IOException {
        Object[] keys;
        if (table == null || (keys = table.getKeys(null)) == null) {
            s.writeInt(0);
        } else {
            int validCount = 0;
            for (int counter = 0; counter < keys.length; ++counter) {
                Object key = keys[counter];
                if (key instanceof Serializable && table.get(key) instanceof Serializable || key instanceof ClientPropertyKey && ((ClientPropertyKey)((Object)key)).getReportValueNotSerializable()) {
                    ++validCount;
                    continue;
                }
                keys[counter] = null;
            }
            s.writeInt(validCount);
            if (validCount > 0) {
                for (Object key : keys) {
                    if (key == null) continue;
                    s.writeObject(key);
                    s.writeObject(table.get(key));
                    if (--validCount == 0) break;
                }
            }
        }
    }

    public void put(Object key, Object value) {
        if (this.table == null) {
            this.table = new Object[]{key, value};
        } else {
            int size = this.size();
            if (size < 8) {
                if (this.containsKey(key)) {
                    Object[] tmp = (Object[])this.table;
                    for (int i = 0; i < tmp.length - 1; i += 2) {
                        if (!tmp[i].equals(key)) continue;
                        tmp[i + 1] = value;
                        break;
                    }
                } else {
                    Object[] array = (Object[])this.table;
                    int i = array.length;
                    Object[] tmp = new Object[i + 2];
                    System.arraycopy(array, 0, tmp, 0, i);
                    tmp[i] = key;
                    tmp[i + 1] = value;
                    this.table = tmp;
                }
            } else {
                if (size == 8 && this.isArray()) {
                    this.grow();
                }
                Hashtable tmp = (Hashtable)this.table;
                tmp.put(key, value);
            }
        }
    }

    public Object get(Object key) {
        Object value = null;
        if (this.table != null) {
            if (this.isArray()) {
                Object[] array = (Object[])this.table;
                for (int i = 0; i < array.length - 1; i += 2) {
                    if (!array[i].equals(key)) continue;
                    value = array[i + 1];
                    break;
                }
            } else {
                value = ((Hashtable)this.table).get(key);
            }
        }
        return value;
    }

    public int size() {
        if (this.table == null) {
            return 0;
        }
        int size = this.isArray() ? ((Object[])this.table).length / 2 : ((Hashtable)this.table).size();
        return size;
    }

    public boolean containsKey(Object key) {
        boolean contains = false;
        if (this.table != null) {
            if (this.isArray()) {
                Object[] array = (Object[])this.table;
                for (int i = 0; i < array.length - 1; i += 2) {
                    if (!array[i].equals(key)) continue;
                    contains = true;
                    break;
                }
            } else {
                contains = ((Hashtable)this.table).containsKey(key);
            }
        }
        return contains;
    }

    public Object remove(Object key) {
        Object value = null;
        if (key == null) {
            return null;
        }
        if (this.table != null) {
            if (this.isArray()) {
                int index = -1;
                Object[] array = (Object[])this.table;
                for (int i = array.length - 2; i >= 0; i -= 2) {
                    if (!array[i].equals(key)) continue;
                    index = i;
                    value = array[i + 1];
                    break;
                }
                if (index != -1) {
                    Object[] tmp = new Object[array.length - 2];
                    System.arraycopy(array, 0, tmp, 0, index);
                    if (index < tmp.length) {
                        System.arraycopy(array, index + 2, tmp, index, tmp.length - index);
                    }
                    this.table = tmp.length == 0 ? null : tmp;
                }
            } else {
                value = ((Hashtable)this.table).remove(key);
            }
            if (this.size() == 7 && !this.isArray()) {
                this.shrink();
            }
        }
        return value;
    }

    public void clear() {
        this.table = null;
    }

    public Object clone() {
        ArrayTable newArrayTable;
        block4: {
            newArrayTable = new ArrayTable();
            if (this.table == null) break block4;
            if (this.isArray()) {
                Object[] array = (Object[])this.table;
                for (int i = 0; i < array.length - 1; i += 2) {
                    newArrayTable.put(array[i], array[i + 1]);
                }
            } else {
                Hashtable tmp = (Hashtable)this.table;
                Enumeration keys = tmp.keys();
                while (keys.hasMoreElements()) {
                    Object o = keys.nextElement();
                    newArrayTable.put(o, tmp.get(o));
                }
            }
        }
        return newArrayTable;
    }

    public Object[] getKeys(Object[] keys) {
        if (this.table == null) {
            return null;
        }
        if (this.isArray()) {
            Object[] array = (Object[])this.table;
            if (keys == null) {
                keys = new Object[array.length / 2];
            }
            int i = 0;
            int index = 0;
            while (i < array.length - 1) {
                keys[index] = array[i];
                i += 2;
                ++index;
            }
        } else {
            Hashtable tmp = (Hashtable)this.table;
            Enumeration enum_ = tmp.keys();
            int counter = tmp.size();
            if (keys == null) {
                keys = new Object[counter];
            }
            while (counter > 0) {
                keys[--counter] = enum_.nextElement();
            }
        }
        return keys;
    }

    private boolean isArray() {
        return this.table instanceof Object[];
    }

    private void grow() {
        Object[] array = (Object[])this.table;
        Hashtable<Object, Object> tmp = new Hashtable<Object, Object>(array.length / 2);
        for (int i = 0; i < array.length; i += 2) {
            tmp.put(array[i], array[i + 1]);
        }
        this.table = tmp;
    }

    private void shrink() {
        Hashtable tmp = (Hashtable)this.table;
        Object[] array = new Object[tmp.size() * 2];
        Enumeration keys = tmp.keys();
        int j = 0;
        while (keys.hasMoreElements()) {
            Object o = keys.nextElement();
            array[j] = o;
            array[j + 1] = tmp.get(o);
            j += 2;
        }
        this.table = array;
    }
}

