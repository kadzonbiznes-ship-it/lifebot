/*
 * Decompiled with CFR 0.152.
 */
package imgui.type;

public final class ImBoolean
implements Cloneable,
Comparable<ImBoolean> {
    private final boolean[] data = new boolean[]{false};

    public ImBoolean() {
    }

    public ImBoolean(ImBoolean imBoolean) {
        this.data[0] = imBoolean.data[0];
    }

    public ImBoolean(boolean value) {
        this.data[0] = value;
    }

    public boolean get() {
        return this.data[0];
    }

    public boolean[] getData() {
        return this.data;
    }

    public void set(boolean value) {
        this.data[0] = value;
    }

    public void set(ImBoolean value) {
        this.set(value.get());
    }

    public String toString() {
        return String.valueOf(this.data[0]);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ImBoolean imBoolean = (ImBoolean)o;
        return this.data[0] == imBoolean.data[0];
    }

    public int hashCode() {
        return Boolean.hashCode(this.data[0]);
    }

    public ImBoolean clone() {
        return new ImBoolean(this);
    }

    @Override
    public int compareTo(ImBoolean o) {
        return Boolean.compare(this.get(), o.get());
    }
}

