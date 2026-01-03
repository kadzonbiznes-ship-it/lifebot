/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import java.util.Objects;

public final class ImVec4
implements Cloneable {
    public float x;
    public float y;
    public float z;
    public float w;

    public ImVec4() {
    }

    public ImVec4(float x, float y, float z, float w) {
        this.set(x, y, z, w);
    }

    public ImVec4(ImVec4 value) {
        this.set(value.x, value.y, value.z, value.w);
    }

    public ImVec4 set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public ImVec4 set(ImVec4 value) {
        return this.set(value.x, value.y, value.z, value.w);
    }

    public ImVec4 plus(float x, float y, float z, float w) {
        return new ImVec4(this.x + x, this.y + y, this.z + z, this.w + w);
    }

    public ImVec4 plus(ImVec4 value) {
        return this.plus(value.x, value.y, value.z, value.w);
    }

    public ImVec4 minus(float x, float y, float z, float w) {
        return new ImVec4(this.x - x, this.y - y, this.z - z, this.w - w);
    }

    public ImVec4 minus(ImVec4 value) {
        return this.minus(value.x, value.y, value.z, value.w);
    }

    public ImVec4 times(float x, float y, float z, float w) {
        return new ImVec4(this.x * x, this.y * y, this.z * z, this.w * w);
    }

    public ImVec4 times(ImVec4 value) {
        return this.times(value.x, value.y, value.z, value.w);
    }

    public ImVec4 div(float x, float y, float z, float w) {
        return new ImVec4(this.x / x, this.y / y, this.z / z, this.w / w);
    }

    public ImVec4 div(ImVec4 value) {
        return this.div(value.x, value.y, value.z, value.w);
    }

    public String toString() {
        return "ImVec4{x=" + this.x + ", y=" + this.y + ", z=" + this.z + ", w=" + this.w + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ImVec4 imVec4 = (ImVec4)o;
        return Float.compare(imVec4.x, this.x) == 0 && Float.compare(imVec4.y, this.y) == 0 && Float.compare(imVec4.z, this.z) == 0 && Float.compare(imVec4.w, this.w) == 0;
    }

    public int hashCode() {
        return Objects.hash(Float.valueOf(this.x), Float.valueOf(this.y), Float.valueOf(this.z), Float.valueOf(this.w));
    }

    public ImVec4 clone() {
        return new ImVec4(this);
    }
}

