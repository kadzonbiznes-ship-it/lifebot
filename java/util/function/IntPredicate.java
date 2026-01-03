/*
 * Decompiled with CFR 0.152.
 */
package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface IntPredicate {
    public boolean test(int var1);

    default public IntPredicate and(IntPredicate other) {
        Objects.requireNonNull(other);
        return value -> this.test(value) && other.test(value);
    }

    default public IntPredicate negate() {
        return value -> !this.test(value);
    }

    default public IntPredicate or(IntPredicate other) {
        Objects.requireNonNull(other);
        return value -> this.test(value) || other.test(value);
    }
}

