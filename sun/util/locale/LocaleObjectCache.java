/*
 * Decompiled with CFR 0.152.
 */
package sun.util.locale;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class LocaleObjectCache<K, V> {
    private final ConcurrentMap<K, CacheEntry<K, V>> map;
    private final ReferenceQueue<V> queue = new ReferenceQueue();

    public LocaleObjectCache() {
        this(16, 0.75f, 16);
    }

    public LocaleObjectCache(int initialCapacity, float loadFactor, int concurrencyLevel) {
        this.map = new ConcurrentHashMap<K, CacheEntry<K, V>>(initialCapacity, loadFactor, concurrencyLevel);
    }

    public V get(K key) {
        V value = null;
        this.cleanStaleEntries();
        CacheEntry<K, V> entry = (CacheEntry<K, V>)this.map.get(key);
        if (entry != null) {
            value = (V)entry.get();
        }
        if (value == null) {
            key = this.normalizeKey(key);
            V newVal = this.createObject(key);
            if (key == null || newVal == null) {
                return null;
            }
            CacheEntry<K, V> newEntry = new CacheEntry<K, V>(key, newVal, this.queue);
            entry = this.map.putIfAbsent(key, newEntry);
            if (entry == null) {
                value = newVal;
            } else {
                value = (V)entry.get();
                if (value == null) {
                    this.map.put(key, newEntry);
                    value = newVal;
                }
            }
        }
        return value;
    }

    protected V put(K key, V value) {
        CacheEntry<K, V> entry = new CacheEntry<K, V>(key, value, this.queue);
        CacheEntry<K, V> oldEntry = this.map.put(key, entry);
        return oldEntry == null ? null : (V)oldEntry.get();
    }

    private void cleanStaleEntries() {
        CacheEntry entry;
        while ((entry = (CacheEntry)this.queue.poll()) != null) {
            this.map.remove(entry.getKey(), entry);
        }
    }

    protected abstract V createObject(K var1);

    protected K normalizeKey(K key) {
        return key;
    }

    private static class CacheEntry<K, V>
    extends SoftReference<V> {
        private K key;

        CacheEntry(K key, V value, ReferenceQueue<V> queue) {
            super(value, queue);
            this.key = key;
        }

        K getKey() {
            return this.key;
        }
    }
}

