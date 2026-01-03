/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.util.NoSuchElementException;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import jdk.internal.ValueBased;

@ValueBased
public final class OptionalInt {
    private static final OptionalInt EMPTY = new OptionalInt();
    private final boolean isPresent;
    private final int value;

    private OptionalInt() {
        this.isPresent = false;
        this.value = 0;
    }

    public static OptionalInt empty() {
        return EMPTY;
    }

    private OptionalInt(int value) {
        this.isPresent = true;
        this.value = value;
    }

    public static OptionalInt of(int value) {
        return new OptionalInt(value);
    }

    public int getAsInt() {
        if (!this.isPresent) {
            throw new NoSuchElementException("No value present");
        }
        return this.value;
    }

    public boolean isPresent() {
        return this.isPresent;
    }

    public boolean isEmpty() {
        return !this.isPresent;
    }

    public void ifPresent(IntConsumer action) {
        if (this.isPresent) {
            action.accept(this.value);
        }
    }

    public void ifPresentOrElse(IntConsumer action, Runnable emptyAction) {
        if (this.isPresent) {
            action.accept(this.value);
        } else {
            emptyAction.run();
        }
    }

    public IntStream stream() {
        if (this.isPresent) {
            return IntStream.of(this.value);
        }
        return IntStream.empty();
    }

    public int orElse(int other) {
        return this.isPresent ? this.value : other;
    }

    public int orElseGet(IntSupplier supplier) {
        return this.isPresent ? this.value : supplier.getAsInt();
    }

    public int orElseThrow() {
        if (!this.isPresent) {
            throw new NoSuchElementException("No value present");
        }
        return this.value;
    }

    public <X extends Throwable> int orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (this.isPresent) {
            return this.value;
        }
        throw (Throwable)exceptionSupplier.get();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OptionalInt)) return false;
        OptionalInt other = (OptionalInt)obj;
        if (this.isPresent && other.isPresent) {
            if (this.value != other.value) return false;
            return true;
        } else if (this.isPresent != other.isPresent) return false;
        return true;
    }

    public int hashCode() {
        return this.isPresent ? Integer.hashCode(this.value) : 0;
    }

    public String toString() {
        return this.isPresent ? "OptionalInt[" + this.value + "]" : "OptionalInt.empty";
    }
}

