/*
 * Decompiled with CFR 0.152.
 */
package com.sun.beans.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Objects;

public abstract class Cache<K, V> {
    private static final int MAXIMUM_CAPACITY = 0x40000000;
    private final boolean identity;
    private final Kind keyKind;
    private final Kind valueKind;
    private final ReferenceQueue<Object> queue = new ReferenceQueue();
    private volatile CacheEntry<K, V>[] table = this.newTable(8);
    private int threshold = 6;
    private int size;

    public abstract V create(K var1);

    public Cache(Kind keyKind, Kind valueKind) {
        this(keyKind, valueKind, false);
    }

    public Cache(Kind keyKind, Kind valueKind, boolean identity) {
        Objects.requireNonNull(keyKind, "keyKind");
        Objects.requireNonNull(valueKind, "valueKind");
        this.keyKind = keyKind;
        this.valueKind = valueKind;
        this.identity = identity;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final V get(K key) {
        Objects.requireNonNull(key, "key");
        this.removeStaleEntries();
        int hash = this.hash(key);
        Object[] table = this.table;
        V current = this.getEntryValue(key, hash, (CacheEntry<K, V>)table[Cache.index(hash, table)]);
        if (current != null) {
            return current;
        }
        ReferenceQueue<Object> referenceQueue = this.queue;
        synchronized (referenceQueue) {
            current = this.getEntryValue(key, hash, this.table[Cache.index(hash, this.table)]);
            if (current != null) {
                return current;
            }
            V value = this.create(key);
            Objects.requireNonNull(value, "value");
            int index = Cache.index(hash, this.table);
            this.table[index] = new CacheEntry<K, V>(hash, key, value, this.table[index]);
            if (++this.size >= this.threshold) {
                if (this.table.length == 0x40000000) {
                    this.threshold = Integer.MAX_VALUE;
                } else {
                    this.removeStaleEntries();
                    table = this.newTable(this.table.length << 1);
                    this.transfer(this.table, (CacheEntry<K, V>[])table);
                    if (this.size >= this.threshold / 2) {
                        this.table = table;
                        this.threshold <<= 1;
                    } else {
                        this.transfer((CacheEntry<K, V>[])table, this.table);
                    }
                    this.removeStaleEntries();
                }
            }
            return value;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void remove(K key) {
        if (key != null) {
            ReferenceQueue<Object> referenceQueue = this.queue;
            synchronized (referenceQueue) {
                CacheEntry<K, V> prev;
                this.removeStaleEntries();
                int hash = this.hash(key);
                int index = Cache.index(hash, this.table);
                CacheEntry<K, V> entry = prev = this.table[index];
                while (entry != null) {
                    CacheEntry next = entry.next;
                    if (entry.matches(hash, key)) {
                        if (entry == prev) {
                            this.table[index] = next;
                        } else {
                            prev.next = next;
                        }
                        entry.unlink();
                        break;
                    }
                    prev = entry;
                    entry = next;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void clear() {
        ReferenceQueue<Object> referenceQueue = this.queue;
        synchronized (referenceQueue) {
            int index = this.table.length;
            while (0 < index--) {
                CacheEntry<K, V> entry = this.table[index];
                while (entry != null) {
                    CacheEntry next = entry.next;
                    entry.unlink();
                    entry = next;
                }
                this.table[index] = null;
            }
            while (null != this.queue.poll()) {
            }
        }
    }

    private int hash(Object key) {
        if (this.identity) {
            int hash = System.identityHashCode(key);
            return (hash << 1) - (hash << 8);
        }
        int hash = key.hashCode();
        hash ^= hash >>> 20 ^ hash >>> 12;
        return hash ^ hash >>> 7 ^ hash >>> 4;
    }

    private static int index(int hash, Object[] table) {
        return hash & table.length - 1;
    }

    private CacheEntry<K, V>[] newTable(int size) {
        return new CacheEntry[size];
    }

    private V getEntryValue(K key, int hash, CacheEntry<K, V> entry) {
        while (entry != null) {
            if (entry.matches(hash, key)) {
                return entry.value.getReferent();
            }
            entry = entry.next;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void removeStaleEntries() {
        Reference<Object> reference = this.queue.poll();
        if (reference != null) {
            ReferenceQueue<Object> referenceQueue = this.queue;
            synchronized (referenceQueue) {
                block3: do {
                    CacheEntry<K, V> prev;
                    Ref ref;
                    CacheEntry owner;
                    if (!(reference instanceof Ref) || (owner = (CacheEntry)(ref = (Ref)((Object)reference)).getOwner()) == null) continue;
                    int index = Cache.index(owner.hash, this.table);
                    CacheEntry<K, V> entry = prev = this.table[index];
                    while (entry != null) {
                        CacheEntry next = entry.next;
                        if (entry == owner) {
                            if (entry == prev) {
                                this.table[index] = next;
                            } else {
                                prev.next = next;
                            }
                            entry.unlink();
                            continue block3;
                        }
                        prev = entry;
                        entry = next;
                    }
                } while ((reference = this.queue.poll()) != null);
            }
        }
    }

    private void transfer(CacheEntry<K, V>[] oldTable, CacheEntry<K, V>[] newTable) {
        int oldIndex = oldTable.length;
        while (0 < oldIndex--) {
            CacheEntry<K, V> entry = oldTable[oldIndex];
            oldTable[oldIndex] = null;
            while (entry != null) {
                CacheEntry next = entry.next;
                if (entry.key.isStale() || entry.value.isStale()) {
                    entry.unlink();
                } else {
                    int newIndex = Cache.index(entry.hash, newTable);
                    entry.next = newTable[newIndex];
                    newTable[newIndex] = entry;
                }
                entry = next;
            }
        }
    }

    public static enum Kind {
        STRONG{

            @Override
            <T> Ref<T> create(Object owner, T value, ReferenceQueue<? super T> queue) {
                return new Strong<T>(owner, value);
            }
        }
        ,
        SOFT{

            @Override
            <T> Ref<T> create(Object owner, T referent, ReferenceQueue<? super T> queue) {
                return referent == null ? new Strong<T>(owner, referent) : new Soft<T>(owner, referent, queue);
            }
        }
        ,
        WEAK{

            @Override
            <T> Ref<T> create(Object owner, T referent, ReferenceQueue<? super T> queue) {
                return referent == null ? new Strong<T>(owner, referent) : new Weak<T>(owner, referent, queue);
            }
        };


        abstract <T> Ref<T> create(Object var1, T var2, ReferenceQueue<? super T> var3);

        private static final class Weak<T>
        extends WeakReference<T>
        implements Ref<T> {
            private Object owner;

            private Weak(Object owner, T referent, ReferenceQueue<? super T> queue) {
                super(referent, queue);
                this.owner = owner;
            }

            @Override
            public Object getOwner() {
                return this.owner;
            }

            @Override
            public T getReferent() {
                return this.get();
            }

            @Override
            public boolean isStale() {
                return null == this.get();
            }

            @Override
            public void removeOwner() {
                this.owner = null;
            }
        }

        private static final class Soft<T>
        extends SoftReference<T>
        implements Ref<T> {
            private Object owner;

            private Soft(Object owner, T referent, ReferenceQueue<? super T> queue) {
                super(referent, queue);
                this.owner = owner;
            }

            @Override
            public Object getOwner() {
                return this.owner;
            }

            @Override
            public T getReferent() {
                return this.get();
            }

            @Override
            public boolean isStale() {
                return null == this.get();
            }

            @Override
            public void removeOwner() {
                this.owner = null;
            }
        }

        private static final class Strong<T>
        implements Ref<T> {
            private Object owner;
            private final T referent;

            private Strong(Object owner, T referent) {
                this.owner = owner;
                this.referent = referent;
            }

            @Override
            public Object getOwner() {
                return this.owner;
            }

            @Override
            public T getReferent() {
                return this.referent;
            }

            @Override
            public boolean isStale() {
                return false;
            }

            @Override
            public void removeOwner() {
                this.owner = null;
            }
        }
    }

    private final class CacheEntry<K, V> {
        private final int hash;
        private final Ref<K> key;
        private final Ref<V> value;
        private volatile CacheEntry<K, V> next;

        private CacheEntry(int hash, K key, V value, CacheEntry<K, V> next) {
            this.hash = hash;
            this.key = Cache.this.keyKind.create(this, key, Cache.this.queue);
            this.value = Cache.this.valueKind.create(this, value, Cache.this.queue);
            this.next = next;
        }

        private boolean matches(int hash, Object object) {
            if (this.hash != hash) {
                return false;
            }
            K key = this.key.getReferent();
            return key == object || !Cache.this.identity && key != null && key.equals(object);
        }

        private void unlink() {
            this.next = null;
            this.key.removeOwner();
            this.value.removeOwner();
            --Cache.this.size;
        }
    }

    private static interface Ref<T> {
        public Object getOwner();

        public T getReferent();

        public boolean isStale();

        public void removeOwner();
    }
}

