/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import sun.security.util.Cache;

class MemoryCache<K, V>
extends Cache<K, V> {
    private static final float LOAD_FACTOR = 0.75f;
    private static final boolean DEBUG = false;
    private final Map<K, CacheEntry<K, V>> cacheMap;
    private int maxSize;
    private long lifetime;
    private long nextExpirationTime = Long.MAX_VALUE;
    private final ReferenceQueue<V> queue;

    public MemoryCache(boolean soft, int maxSize) {
        this(soft, maxSize, 0);
    }

    public MemoryCache(boolean soft, int maxSize, int lifetime) {
        this.maxSize = maxSize;
        this.lifetime = (long)lifetime * 1000L;
        this.queue = soft ? new ReferenceQueue() : null;
        this.cacheMap = new LinkedHashMap<K, CacheEntry<K, V>>(1, 0.75f, true);
    }

    private void emptyQueue() {
        CacheEntry entry;
        if (this.queue == null) {
            return;
        }
        int startSize = this.cacheMap.size();
        while ((entry = (CacheEntry)((Object)this.queue.poll())) != null) {
            CacheEntry<K, V> currentEntry;
            Object key = entry.getKey();
            if (key == null || (currentEntry = this.cacheMap.remove(key)) == null || entry == currentEntry) continue;
            this.cacheMap.put(key, currentEntry);
        }
    }

    private void expungeExpiredEntries() {
        this.emptyQueue();
        if (this.lifetime == 0L) {
            return;
        }
        int cnt = 0;
        long time = System.currentTimeMillis();
        if (this.nextExpirationTime > time) {
            return;
        }
        this.nextExpirationTime = Long.MAX_VALUE;
        Iterator<CacheEntry<K, V>> t = this.cacheMap.values().iterator();
        while (t.hasNext()) {
            CacheEntry<K, V> entry = t.next();
            if (!entry.isValid(time)) {
                t.remove();
                ++cnt;
                continue;
            }
            if (this.nextExpirationTime <= entry.getExpirationTime()) continue;
            this.nextExpirationTime = entry.getExpirationTime();
        }
    }

    @Override
    public synchronized int size() {
        this.expungeExpiredEntries();
        return this.cacheMap.size();
    }

    @Override
    public synchronized void clear() {
        if (this.queue != null) {
            for (CacheEntry<K, V> entry : this.cacheMap.values()) {
                entry.invalidate();
            }
            while (this.queue.poll() != null) {
            }
        }
        this.cacheMap.clear();
    }

    @Override
    public synchronized void put(K key, V value) {
        CacheEntry<K, V> newEntry;
        CacheEntry<K, V> oldEntry;
        long expirationTime;
        this.emptyQueue();
        long l = expirationTime = this.lifetime == 0L ? 0L : System.currentTimeMillis() + this.lifetime;
        if (expirationTime < this.nextExpirationTime) {
            this.nextExpirationTime = expirationTime;
        }
        if ((oldEntry = this.cacheMap.put(key, newEntry = this.newEntry(key, value, expirationTime, this.queue))) != null) {
            oldEntry.invalidate();
            return;
        }
        if (this.maxSize > 0 && this.cacheMap.size() > this.maxSize) {
            this.expungeExpiredEntries();
            if (this.cacheMap.size() > this.maxSize) {
                Iterator<CacheEntry<K, V>> t = this.cacheMap.values().iterator();
                CacheEntry<K, V> lruEntry = t.next();
                t.remove();
                lruEntry.invalidate();
            }
        }
    }

    @Override
    public synchronized V get(Object key) {
        long time;
        this.emptyQueue();
        CacheEntry<K, V> entry = this.cacheMap.get(key);
        if (entry == null) {
            return null;
        }
        long l = time = this.lifetime == 0L ? 0L : System.currentTimeMillis();
        if (!entry.isValid(time)) {
            this.cacheMap.remove(key);
            return null;
        }
        return entry.getValue();
    }

    @Override
    public synchronized void remove(Object key) {
        this.emptyQueue();
        CacheEntry<K, V> entry = this.cacheMap.remove(key);
        if (entry != null) {
            entry.invalidate();
        }
    }

    @Override
    public synchronized V pull(Object key) {
        long time;
        this.emptyQueue();
        CacheEntry<K, V> entry = this.cacheMap.remove(key);
        if (entry == null) {
            return null;
        }
        long l = time = this.lifetime == 0L ? 0L : System.currentTimeMillis();
        if (entry.isValid(time)) {
            V value = entry.getValue();
            entry.invalidate();
            return value;
        }
        return null;
    }

    @Override
    public synchronized void setCapacity(int size) {
        this.expungeExpiredEntries();
        if (size > 0 && this.cacheMap.size() > size) {
            Iterator<CacheEntry<K, V>> t = this.cacheMap.values().iterator();
            for (int i = this.cacheMap.size() - size; i > 0; --i) {
                CacheEntry<K, V> lruEntry = t.next();
                t.remove();
                lruEntry.invalidate();
            }
        }
        this.maxSize = Math.max(size, 0);
    }

    @Override
    public synchronized void setTimeout(int timeout) {
        this.emptyQueue();
        this.lifetime = timeout > 0 ? (long)timeout * 1000L : 0L;
    }

    @Override
    public synchronized void accept(Cache.CacheVisitor<K, V> visitor) {
        this.expungeExpiredEntries();
        Map<K, V> cached = this.getCachedEntries();
        visitor.visit(cached);
    }

    private Map<K, V> getCachedEntries() {
        HashMap<K, V> kvmap = HashMap.newHashMap(this.cacheMap.size());
        for (CacheEntry<K, V> entry : this.cacheMap.values()) {
            kvmap.put(entry.getKey(), entry.getValue());
        }
        return kvmap;
    }

    protected CacheEntry<K, V> newEntry(K key, V value, long expirationTime, ReferenceQueue<V> queue) {
        if (queue != null) {
            return new SoftCacheEntry<K, V>(key, value, expirationTime, queue);
        }
        return new HardCacheEntry<K, V>(key, value, expirationTime);
    }

    private static interface CacheEntry<K, V> {
        public boolean isValid(long var1);

        public void invalidate();

        public K getKey();

        public V getValue();

        public long getExpirationTime();
    }

    private static class SoftCacheEntry<K, V>
    extends SoftReference<V>
    implements CacheEntry<K, V> {
        private K key;
        private long expirationTime;

        SoftCacheEntry(K key, V value, long expirationTime, ReferenceQueue<V> queue) {
            super(value, queue);
            this.key = key;
            this.expirationTime = expirationTime;
        }

        @Override
        public K getKey() {
            return this.key;
        }

        @Override
        public V getValue() {
            return (V)this.get();
        }

        @Override
        public long getExpirationTime() {
            return this.expirationTime;
        }

        @Override
        public boolean isValid(long currentTime) {
            boolean valid;
            boolean bl = valid = currentTime <= this.expirationTime && this.get() != null;
            if (!valid) {
                this.invalidate();
            }
            return valid;
        }

        @Override
        public void invalidate() {
            this.clear();
            this.key = null;
            this.expirationTime = -1L;
        }
    }

    private static class HardCacheEntry<K, V>
    implements CacheEntry<K, V> {
        private K key;
        private V value;
        private long expirationTime;

        HardCacheEntry(K key, V value, long expirationTime) {
            this.key = key;
            this.value = value;
            this.expirationTime = expirationTime;
        }

        @Override
        public K getKey() {
            return this.key;
        }

        @Override
        public V getValue() {
            return this.value;
        }

        @Override
        public long getExpirationTime() {
            return this.expirationTime;
        }

        @Override
        public boolean isValid(long currentTime) {
            boolean valid;
            boolean bl = valid = currentTime <= this.expirationTime;
            if (!valid) {
                this.invalidate();
            }
            return valid;
        }

        @Override
        public void invalidate() {
            this.key = null;
            this.value = null;
            this.expirationTime = -1L;
        }
    }
}

