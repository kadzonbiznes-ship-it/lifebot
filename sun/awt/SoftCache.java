/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Deprecated
public class SoftCache
extends AbstractMap<Object, Object>
implements Map<Object, Object> {
    private Map<Object, Object> hash;
    private ReferenceQueue<Object> queue = new ReferenceQueue();
    private Set<Map.Entry<Object, Object>> entrySet = null;

    private void processQueue() {
        ValueCell vc;
        while ((vc = (ValueCell)this.queue.poll()) != null) {
            if (vc.isValid()) {
                this.hash.remove(vc.key);
                continue;
            }
            --ValueCell.dropped;
        }
    }

    public SoftCache(int initialCapacity, float loadFactor) {
        this.hash = new HashMap<Object, Object>(initialCapacity, loadFactor);
    }

    public SoftCache(int initialCapacity) {
        this.hash = new HashMap<Object, Object>(initialCapacity);
    }

    public SoftCache() {
        this.hash = new HashMap<Object, Object>();
    }

    @Override
    public int size() {
        return this.entrySet().size();
    }

    @Override
    public boolean isEmpty() {
        return this.entrySet().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return ValueCell.strip(this.hash.get(key), false) != null;
    }

    protected Object fill(Object key) {
        return null;
    }

    @Override
    public Object get(Object key) {
        this.processQueue();
        Object v = this.hash.get(key);
        if (v == null && (v = this.fill(key)) != null) {
            this.hash.put(key, ValueCell.create(key, v, this.queue));
            return v;
        }
        return ValueCell.strip(v, false);
    }

    @Override
    public Object put(Object key, Object value) {
        this.processQueue();
        ValueCell vc = ValueCell.create(key, value, this.queue);
        return ValueCell.strip(this.hash.put(key, vc), true);
    }

    @Override
    public Object remove(Object key) {
        this.processQueue();
        return ValueCell.strip(this.hash.remove(key), true);
    }

    @Override
    public void clear() {
        this.processQueue();
        this.hash.clear();
    }

    private static boolean valEquals(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        if (this.entrySet == null) {
            this.entrySet = new EntrySet();
        }
        return this.entrySet;
    }

    private static class ValueCell
    extends SoftReference<Object> {
        private static Object INVALID_KEY = new Object();
        private static int dropped = 0;
        private Object key;

        private ValueCell(Object key, Object value, ReferenceQueue<Object> queue) {
            super(value, queue);
            this.key = key;
        }

        private static ValueCell create(Object key, Object value, ReferenceQueue<Object> queue) {
            if (value == null) {
                return null;
            }
            return new ValueCell(key, value, queue);
        }

        private static Object strip(Object val, boolean drop) {
            if (val == null) {
                return null;
            }
            ValueCell vc = (ValueCell)val;
            Object o = vc.get();
            if (drop) {
                vc.drop();
            }
            return o;
        }

        private boolean isValid() {
            return this.key != INVALID_KEY;
        }

        private void drop() {
            super.clear();
            this.key = INVALID_KEY;
            ++dropped;
        }
    }

    private class EntrySet
    extends AbstractSet<Map.Entry<Object, Object>> {
        Set<Map.Entry<Object, Object>> hashEntries;

        private EntrySet() {
            this.hashEntries = SoftCache.this.hash.entrySet();
        }

        @Override
        public Iterator<Map.Entry<Object, Object>> iterator() {
            return new Iterator<Map.Entry<Object, Object>>(){
                Iterator<Map.Entry<Object, Object>> hashIterator;
                Entry next;
                {
                    this.hashIterator = EntrySet.this.hashEntries.iterator();
                    this.next = null;
                }

                @Override
                public boolean hasNext() {
                    while (this.hashIterator.hasNext()) {
                        Map.Entry<Object, Object> ent = this.hashIterator.next();
                        ValueCell vc = (ValueCell)ent.getValue();
                        Object v = null;
                        if (vc != null) {
                            Object t = vc.get();
                            v = t;
                            if (t == null) continue;
                        }
                        this.next = new Entry(ent, v);
                        return true;
                    }
                    return false;
                }

                @Override
                public Map.Entry<Object, Object> next() {
                    if (this.next == null && !this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    Entry e = this.next;
                    this.next = null;
                    return e;
                }

                @Override
                public void remove() {
                    this.hashIterator.remove();
                }
            };
        }

        @Override
        public boolean isEmpty() {
            return !this.iterator().hasNext();
        }

        @Override
        public int size() {
            int j = 0;
            Iterator<Map.Entry<Object, Object>> i = this.iterator();
            while (i.hasNext()) {
                ++j;
                i.next();
            }
            return j;
        }

        @Override
        public boolean remove(Object o) {
            SoftCache.this.processQueue();
            if (o instanceof Entry) {
                return this.hashEntries.remove(((Entry)o).ent);
            }
            return false;
        }
    }

    private class Entry
    implements Map.Entry<Object, Object> {
        private Map.Entry<Object, Object> ent;
        private Object value;

        Entry(Map.Entry<Object, Object> ent, Object value) {
            this.ent = ent;
            this.value = value;
        }

        @Override
        public Object getKey() {
            return this.ent.getKey();
        }

        @Override
        public Object getValue() {
            return this.value;
        }

        @Override
        public Object setValue(Object value) {
            return this.ent.setValue(ValueCell.create(this.ent.getKey(), value, SoftCache.this.queue));
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return SoftCache.valEquals(this.ent.getKey(), e.getKey()) && SoftCache.valEquals(this.value, e.getValue());
        }

        @Override
        public int hashCode() {
            Object k = this.getKey();
            return (k == null ? 0 : k.hashCode()) ^ (this.value == null ? 0 : this.value.hashCode());
        }
    }
}

