/*
 * Decompiled with CFR 0.152.
 */
package java.lang;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

final class WeakPairMap<K1, K2, V> {
    private final ConcurrentHashMap<Pair<K1, K2>, V> map = new ConcurrentHashMap();
    private final ReferenceQueue<Object> queue = new ReferenceQueue();

    WeakPairMap() {
    }

    public boolean containsKeyPair(K1 k1, K2 k2) {
        this.expungeStaleAssociations();
        return this.map.containsKey(Pair.lookup(k1, k2));
    }

    public V get(K1 k1, K2 k2) {
        this.expungeStaleAssociations();
        return this.map.get(Pair.lookup(k1, k2));
    }

    public V put(K1 k1, K2 k2, V v) {
        this.expungeStaleAssociations();
        return this.map.put(Pair.weak(k1, k2, this.queue), v);
    }

    public V putIfAbsent(K1 k1, K2 k2, V v) {
        this.expungeStaleAssociations();
        return this.map.putIfAbsent(Pair.weak(k1, k2, this.queue), v);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public V computeIfAbsent(K1 k1, K2 k2, BiFunction<? super K1, ? super K2, ? extends V> mappingFunction) {
        this.expungeStaleAssociations();
        try {
            Object object = this.map.computeIfAbsent(Pair.weak(k1, k2, this.queue), pair -> mappingFunction.apply((Object)pair.first(), (Object)pair.second()));
            return (V)object;
        }
        finally {
            Reference.reachabilityFence(k1);
            Reference.reachabilityFence(k2);
        }
    }

    public Collection<V> values() {
        this.expungeStaleAssociations();
        return this.map.values();
    }

    private void expungeStaleAssociations() {
        WeakRefPeer peer;
        while ((peer = (WeakRefPeer)this.queue.poll()) != null) {
            this.map.remove(peer.weakPair());
        }
    }

    private static interface Pair<K1, K2> {
        public static <K1, K2> Pair<K1, K2> weak(K1 k1, K2 k2, ReferenceQueue<Object> queue) {
            return new Weak<K1, K2>(k1, k2, queue);
        }

        public static <K1, K2> Pair<K1, K2> lookup(K1 k1, K2 k2) {
            return new Lookup<K1, K2>(k1, k2);
        }

        public K1 first();

        public K2 second();

        public static int hashCode(Object first, Object second) {
            return System.identityHashCode(first) ^ System.identityHashCode(second);
        }

        public static boolean equals(Object first, Object second, Pair<?, ?> p) {
            return first != null && second != null && first == p.first() && second == p.second();
        }

        public static final class Weak<K1, K2>
        extends WeakRefPeer<K1>
        implements Pair<K1, K2> {
            private final int hash;
            private final WeakRefPeer<K2> peer;

            Weak(K1 k1, K2 k2, ReferenceQueue<Object> queue) {
                super(k1, queue);
                this.hash = Pair.hashCode(k1, k2);
                this.peer = new WeakRefPeer<K2>(k2, queue){

                    @Override
                    Weak<?, ?> weakPair() {
                        return this;
                    }
                };
            }

            @Override
            Weak<?, ?> weakPair() {
                return this;
            }

            @Override
            public K1 first() {
                return (K1)this.get();
            }

            @Override
            public K2 second() {
                return (K2)this.peer.get();
            }

            public int hashCode() {
                return this.hash;
            }

            public boolean equals(Object obj) {
                return this == obj || obj instanceof Pair && Pair.equals(this.first(), this.second(), (Pair)obj);
            }
        }

        public static final class Lookup<K1, K2>
        implements Pair<K1, K2> {
            private final K1 k1;
            private final K2 k2;

            Lookup(K1 k1, K2 k2) {
                this.k1 = Objects.requireNonNull(k1);
                this.k2 = Objects.requireNonNull(k2);
            }

            @Override
            public K1 first() {
                return this.k1;
            }

            @Override
            public K2 second() {
                return this.k2;
            }

            public int hashCode() {
                return Pair.hashCode(this.k1, this.k2);
            }

            public boolean equals(Object obj) {
                return obj instanceof Pair && Pair.equals(this.k1, this.k2, (Pair)obj);
            }
        }
    }

    private static abstract class WeakRefPeer<K>
    extends WeakReference<K> {
        WeakRefPeer(K k, ReferenceQueue<Object> queue) {
            super(Objects.requireNonNull(k), queue);
        }

        abstract Pair.Weak<?, ?> weakPair();
    }
}

