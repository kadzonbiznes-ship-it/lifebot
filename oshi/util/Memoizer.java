/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 */
package oshi.util;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.GlobalConfig;

@ThreadSafe
public final class Memoizer {
    private static final Supplier<Long> DEFAULT_EXPIRATION_NANOS = Memoizer.memoize(Memoizer::queryExpirationConfig, TimeUnit.MINUTES.toNanos(1L));

    private Memoizer() {
    }

    private static long queryExpirationConfig() {
        return TimeUnit.MILLISECONDS.toNanos(GlobalConfig.get("oshi.util.memoizer.expiration", 300));
    }

    public static long defaultExpiration() {
        return DEFAULT_EXPIRATION_NANOS.get();
    }

    public static <T> Supplier<T> memoize(final Supplier<T> original, final long ttlNanos) {
        return new Supplier<T>(){
            private final Supplier<T> delegate;
            private volatile T value;
            private volatile long expirationNanos;
            {
                this.delegate = original;
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public T get() {
                long nanos = this.expirationNanos;
                long now = System.nanoTime();
                if (nanos == 0L || ttlNanos >= 0L && now - nanos >= 0L) {
                    1 var5_3 = this;
                    synchronized (var5_3) {
                        if (nanos == this.expirationNanos) {
                            Object t = this.delegate.get();
                            this.value = t;
                            nanos = now + ttlNanos;
                            this.expirationNanos = nanos == 0L ? 1L : nanos;
                            return t;
                        }
                    }
                }
                return this.value;
            }
        };
    }

    public static <T> Supplier<T> memoize(Supplier<T> original) {
        return Memoizer.memoize(original, -1L);
    }
}

