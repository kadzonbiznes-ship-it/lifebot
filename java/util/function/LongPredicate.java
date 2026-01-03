/*
 * Decompiled with CFR 0.152.
 */
package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface LongPredicate {
    public boolean test(long var1);

    default public LongPredicate and(LongPredicate other) {
        Objects.requireNonNull(other);
        return value -> this.test(value) && other.test(value);
    }

    default public LongPredicate negate() {
        return value -> !this.test(value);
    }

    default public LongPredicate or(LongPredicate other) {
        Objects.requireNonNull(other);
        return value -> this.test(value) || other.test(value);
    }
}

