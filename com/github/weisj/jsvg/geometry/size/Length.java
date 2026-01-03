/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.geometry.size.Length$1
 *  com.google.errorprone.annotations.Immutable
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.size;

import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.MeasureContext;
import com.github.weisj.jsvg.geometry.size.Unit;
import com.google.errorprone.annotations.Immutable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

@Immutable
public final class Length {
    public static final float UNSPECIFIED_RAW = Float.NaN;
    @NotNull
    public static final Length UNSPECIFIED = new Length(Unit.Raw, Float.NaN);
    @NotNull
    public static final Length ZERO = new Length(Unit.Raw, 0.0f);
    @NotNull
    private final Unit unit;
    private final float value;
    private static final float pixelsPerInch = 96.0f;
    private static final float inchesPerCm = 0.3936f;

    public Length(@NotNull Unit unit, float value) {
        this.unit = unit;
        this.value = value;
    }

    public static boolean isUnspecified(float value) {
        return Float.isNaN(value);
    }

    public static boolean isSpecified(float value) {
        return !Length.isUnspecified(value);
    }

    private float resolveNonPercentage(@NotNull MeasureContext context) {
        if (this.isUnspecified()) {
            throw new IllegalStateException("Can't resolve size of unspecified length");
        }
        if (this.unit == Unit.Raw) {
            return this.value;
        }
        assert (this.unit != Unit.PERCENTAGE);
        switch (1.$SwitchMap$com$github$weisj$jsvg$geometry$size$Unit[this.unit.ordinal()]) {
            case 1: {
                return this.value;
            }
            case 2: {
                return 96.0f * this.value;
            }
            case 3: {
                return 37.7856f * this.value;
            }
            case 4: {
                return 3.7785597f * this.value;
            }
            case 5: {
                return 1.3333334f * this.value;
            }
            case 6: {
                return 16.0f * this.value;
            }
            case 7: {
                return context.em() * this.value;
            }
            case 8: {
                return context.rem() * this.value;
            }
            case 9: {
                return context.ex() * this.value;
            }
        }
        throw new UnsupportedOperationException("Not implemented: Can't convert " + (Object)((Object)this.unit) + " to pixel");
    }

    public float resolveWidth(@NotNull MeasureContext context) {
        if (this.unit == Unit.PERCENTAGE) {
            return this.value * context.viewWidth() / 100.0f;
        }
        return this.resolveNonPercentage(context);
    }

    public float resolveHeight(@NotNull MeasureContext context) {
        if (this.unit == Unit.PERCENTAGE) {
            return this.value * context.viewHeight() / 100.0f;
        }
        return this.resolveNonPercentage(context);
    }

    public float resolveLength(@NotNull MeasureContext context) {
        if (this.unit == Unit.PERCENTAGE) {
            return this.value / 100.0f * context.normedDiagonalLength();
        }
        return this.resolveNonPercentage(context);
    }

    public float resolveFontSize(@NotNull MeasureContext context) {
        if (this.unit == Unit.PERCENTAGE) {
            return this.value / 100.0f * context.em();
        }
        return this.resolveNonPercentage(context);
    }

    public String toString() {
        return this.value + this.unit.suffix();
    }

    public boolean isZero() {
        return this.value == 0.0f;
    }

    public float raw() {
        return this.value;
    }

    @NotNull
    public Unit unit() {
        return this.unit;
    }

    public boolean isUnspecified() {
        return Length.isUnspecified(this.raw());
    }

    public boolean isSpecified() {
        return !this.isUnspecified();
    }

    @NotNull
    public Length coerceNonNegative() {
        if (this.isSpecified() && this.raw() <= 0.0f) {
            return ZERO;
        }
        return this;
    }

    public Length orElseIfUnspecified(float value) {
        if (this.isUnspecified()) {
            return Unit.Raw.valueOf(value);
        }
        return this;
    }

    public Length multiply(float scalingFactor) {
        if (scalingFactor == 0.0f) {
            return ZERO;
        }
        return new Length(this.unit(), scalingFactor * this.raw());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Length)) {
            return false;
        }
        Length length = (Length)o;
        return this.unit == length.unit && Float.compare(length.value, this.value) == 0;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.unit, Float.valueOf(this.value)});
    }
}

