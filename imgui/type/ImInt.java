/*
 * Decompiled with CFR 0.152.
 */
package imgui.type;

public final class ImInt
extends Number
implements Cloneable,
Comparable<ImInt> {
    private final int[] data = new int[]{0};

    public ImInt() {
    }

    public ImInt(ImInt imInt) {
        this.data[0] = imInt.data[0];
    }

    public ImInt(int value) {
        this.set(value);
    }

    public int get() {
        return this.data[0];
    }

    public int[] getData() {
        return this.data;
    }

    public void set(int value) {
        this.data[0] = value;
    }

    public void set(ImInt value) {
        this.set(value.get());
    }

    public String toString() {
        return String.valueOf(this.get());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ImInt imInt = (ImInt)o;
        return this.data[0] == imInt.data[0];
    }

    public int hashCode() {
        return Integer.hashCode(this.data[0]);
    }

    public ImInt clone() {
        return new ImInt(this);
    }

    @Override
    public int compareTo(ImInt o) {
        return Integer.compare(this.get(), o.get());
    }

    @Override
    public int intValue() {
        return this.get();
    }

    @Override
    public long longValue() {
        return this.get();
    }

    @Override
    public float floatValue() {
        return this.get();
    }

    @Override
    public double doubleValue() {
        return this.get();
    }
}

