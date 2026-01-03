/*
 * Decompiled with CFR 0.152.
 */
package imgui;

import java.util.Objects;

public final class ImVec2
implements Cloneable {
    public float x;
    public float y;

    public ImVec2() {
    }

    public ImVec2(float x, float y) {
        this.set(x, y);
    }

    public ImVec2(ImVec2 value) {
        this.set(value.x, value.y);
    }

    public ImVec2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public ImVec2 set(ImVec2 value) {
        return this.set(value.x, value.y);
    }

    public ImVec2 plus(float x, float y) {
        return new ImVec2(this.x + x, this.y + y);
    }

    public ImVec2 plus(ImVec2 value) {
        return this.plus(value.x, value.y);
    }

    public ImVec2 minus(float x, float y) {
        return new ImVec2(this.x - x, this.y - y);
    }

    public ImVec2 minus(ImVec2 value) {
        return this.minus(value.x, value.y);
    }

    public ImVec2 times(float x, float y) {
        return new ImVec2(this.x * x, this.y * y);
    }

    public ImVec2 times(ImVec2 value) {
        return this.times(value.x, value.y);
    }

    public ImVec2 div(float x, float y) {
        return new ImVec2(this.x / x, this.y / y);
    }

    public ImVec2 div(ImVec2 value) {
        return this.div(value.x, value.y);
    }

    public String toString() {
        return "ImVec2{x=" + this.x + ", y=" + this.y + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ImVec2 imVec2 = (ImVec2)o;
        return Float.compare(imVec2.x, this.x) == 0 && Float.compare(imVec2.y, this.y) == 0;
    }

    public int hashCode() {
        return Objects.hash(Float.valueOf(this.x), Float.valueOf(this.y));
    }

    public ImVec2 clone() {
        return new ImVec2(this);
    }
}

