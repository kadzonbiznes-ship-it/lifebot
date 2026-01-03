/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.util.Arrays;
import java.util.Map;
import sun.security.util.MemoryCache;
import sun.security.util.NullCache;

public abstract class Cache<K, V> {
    protected Cache() {
    }

    public abstract int size();

    public abstract void clear();

    public abstract void put(K var1, V var2);

    public abstract V get(Object var1);

    public abstract void remove(Object var1);

    public abstract V pull(Object var1);

    public abstract void setCapacity(int var1);

    public abstract void setTimeout(int var1);

    public abstract void accept(CacheVisitor<K, V> var1);

    public static <K, V> Cache<K, V> newSoftMemoryCache(int size) {
        return new MemoryCache(true, size);
    }

    public static <K, V> Cache<K, V> newSoftMemoryCache(int size, int timeout) {
        return new MemoryCache(true, size, timeout);
    }

    public static <K, V> Cache<K, V> newHardMemoryCache(int size) {
        return new MemoryCache(false, size);
    }

    public static <K, V> Cache<K, V> newNullCache() {
        return NullCache.INSTANCE;
    }

    public static <K, V> Cache<K, V> newHardMemoryCache(int size, int timeout) {
        return new MemoryCache(false, size, timeout);
    }

    public static interface CacheVisitor<K, V> {
        public void visit(Map<K, V> var1);
    }

    public static class EqualByteArray {
        private final byte[] b;
        private int hash;

        public EqualByteArray(byte[] b) {
            this.b = b;
        }

        public int hashCode() {
            int h = this.hash;
            if (h == 0 && this.b.length > 0) {
                this.hash = h = Arrays.hashCode(this.b);
            }
            return h;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof EqualByteArray)) {
                return false;
            }
            EqualByteArray other = (EqualByteArray)obj;
            return Arrays.equals(this.b, other.b);
        }
    }
}

