/*
 * Decompiled with CFR 0.152.
 */
package imgui.type;

public final class ImFloat
extends Number
implements Cloneable,
Comparable<ImFloat> {
    private final float[] data = new float[]{0.0f};

    public ImFloat() {
    }

    public ImFloat(ImFloat imFloat) {
        this.data[0] = imFloat.data[0];
    }

    public ImFloat(float value) {
        this.set(value);
    }

    public float get() {
        return this.data[0];
    }

    public float[] getData() {
        return this.data;
    }

    public void set(float value) {
        this.data[0] = value;
    }

    public void set(ImFloat value) {
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
        ImFloat imFloat = (ImFloat)o;
        return this.data[0] == imFloat.data[0];
    }

    public int hashCode() {
        return Float.hashCode(this.data[0]);
    }

    public ImFloat clone() {
        return new ImFloat(this);
    }

    @Override
    public int compareTo(ImFloat o) {
        return Float.compare(this.get(), o.get());
    }

    @Override
    public int intValue() {
        return (int)this.get();
    }

    @Override
    public long longValue() {
        return (long)this.get();
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

