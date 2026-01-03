/*
 * Decompiled with CFR 0.152.
 */
package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface IntConsumer {
    public void accept(int var1);

    default public IntConsumer andThen(IntConsumer after) {
        Objects.requireNonNull(after);
        return t -> {
            this.accept(t);
            after.accept(t);
        };
    }
}

