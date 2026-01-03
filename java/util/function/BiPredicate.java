/*
 * Decompiled with CFR 0.152.
 */
package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface BiPredicate<T, U> {
    public boolean test(T var1, U var2);

    default public BiPredicate<T, U> and(BiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return (t, u) -> this.test(t, u) && other.test(t, u);
    }

    default public BiPredicate<T, U> negate() {
        return (t, u) -> !this.test(t, u);
    }

    default public BiPredicate<T, U> or(BiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return (t, u) -> this.test(t, u) || other.test(t, u);
    }
}

