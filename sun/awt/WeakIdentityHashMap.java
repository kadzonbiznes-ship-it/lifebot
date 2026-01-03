/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

class WeakIdentityHashMap<K, V>
implements Map<K, V> {
    private final Map<WeakKey<K>, V> map;
    private final transient ReferenceQueue<K> queue = new ReferenceQueue();

    public WeakIdentityHashMap() {
        this.map = new HashMap<WeakKey<K>, V>(16);
    }

    public WeakIdentityHashMap(int initialSize) {
        this.map = new HashMap<WeakKey<K>, V>(initialSize);
    }

    private Map<WeakKey<K>, V> getMap() {
        Reference<K> ref;
        while ((ref = this.queue.poll()) != null) {
            this.map.remove(ref);
        }
        return this.map;
    }

    @Override
    public int size() {
        return this.getMap().size();
    }

    @Override
    public boolean isEmpty() {
        return this.getMap().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.getMap().containsKey(new WeakKey<Object>(key, null));
    }

    @Override
    public boolean containsValue(Object value) {
        return this.getMap().containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.getMap().get(new WeakKey<Object>(key, null));
    }

    @Override
    public V put(K key, V value) {
        return this.getMap().put(new WeakKey<K>(key, this.queue), value);
    }

    @Override
    public V remove(Object key) {
        return this.getMap().remove(new WeakKey<Object>(key, null));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<K, V> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        this.getMap().clear();
    }

    @Override
    public Set<K> keySet() {
        return new AbstractSet<K>(){

            @Override
            public Iterator<K> iterator() {
                return new Iterator<K>(){
                    private K next;
                    Iterator<WeakKey<K>> iterator;
                    {
                        this.iterator = WeakIdentityHashMap.this.getMap().keySet().iterator();
                    }

                    @Override
                    public boolean hasNext() {
                        while (this.iterator.hasNext()) {
                            this.next = this.iterator.next().get();
                            if (this.next == null) continue;
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public K next() {
                        if (this.next == null && !this.hasNext()) {
                            throw new NoSuchElementException();
                        }
                        Object ret = this.next;
                        this.next = null;
                        return ret;
                    }
                };
            }

            @Override
            public int size() {
                return WeakIdentityHashMap.this.getMap().keySet().size();
            }
        };
    }

    @Override
    public Collection<V> values() {
        return this.getMap().values();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new AbstractSet<Map.Entry<K, V>>(){

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                final Iterator iterator = WeakIdentityHashMap.this.getMap().entrySet().iterator();
                return new Iterator<Map.Entry<K, V>>(){

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public Map.Entry<K, V> next() {
                        return new Map.Entry<K, V>(){
                            Map.Entry<WeakKey<K>, V> entry;
                            {
                                this.entry = (Map.Entry)iterator.next();
                            }

                            @Override
                            public K getKey() {
                                return this.entry.getKey().get();
                            }

                            @Override
                            public V getValue() {
                                return this.entry.getValue();
                            }

                            @Override
                            public V setValue(V value) {
                                return null;
                            }
                        };
                    }
                };
            }

            @Override
            public int size() {
                return WeakIdentityHashMap.this.getMap().entrySet().size();
            }
        };
    }

    private static class WeakKey<K>
    extends WeakReference<K> {
        private final int hash;

        WeakKey(K key, ReferenceQueue<K> q) {
            super(key, q);
            this.hash = System.identityHashCode(key);
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof WeakKey) {
                return this.get() == ((WeakKey)o).get();
            }
            return false;
        }

        public int hashCode() {
            return this.hash;
        }
    }
}

