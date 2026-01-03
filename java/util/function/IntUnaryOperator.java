/*
 * Decompiled with CFR 0.152.
 */
package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface IntUnaryOperator {
    public int applyAsInt(int var1);

    default public IntUnaryOperator compose(IntUnaryOperator before) {
        Objects.requireNonNull(before);
        return v -> this.applyAsInt(before.applyAsInt(v));
    }

    default public IntUnaryOperator andThen(IntUnaryOperator after) {
        Objects.requireNonNull(after);
        return t -> after.applyAsInt(this.applyAsInt(t));
    }

    public static IntUnaryOperator identity() {
        return t -> t;
    }
}

