/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.errorprone.annotations.Immutable
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.geometry.size;

import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.attributes.font.SVGFont;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.geometry.size.Length;
import com.google.errorprone.annotations.Immutable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Immutable
public final class MeasureContext {
    private final float vw;
    private final float vh;
    private final float em;
    private final float ex;

    public MeasureContext(float vw, float vh, float em, float ex) {
        this.vw = vw;
        this.vh = vh;
        this.em = em;
        this.ex = ex;
    }

    @NotNull
    public static MeasureContext createInitial(@NotNull FloatSize viewBoxSize, float em, float ex) {
        return new MeasureContext(viewBoxSize.width, viewBoxSize.height, em, ex);
    }

    @NotNull
    public MeasureContext derive(float viewWidth, float viewHeight) {
        return new MeasureContext(viewWidth, viewHeight, this.em, this.ex);
    }

    @NotNull
    public MeasureContext derive(@Nullable ViewBox viewBox, float em, float ex) {
        if (viewBox == null && Length.isUnspecified(em) && Length.isUnspecified(ex)) {
            return this;
        }
        float newVw = this.vw;
        float newVh = this.vh;
        if (viewBox != null) {
            if (viewBox.hasSpecifiedWidth()) {
                newVw = viewBox.width;
            }
            if (viewBox.hasSpecifiedHeight()) {
                newVh = viewBox.height;
            }
        }
        float effectiveEm = Length.isUnspecified(em) ? this.em : em;
        float effectiveEx = Length.isUnspecified(ex) ? this.ex : ex;
        return new MeasureContext(newVw, newVh, effectiveEm, effectiveEx);
    }

    public float viewWidth() {
        return this.vw;
    }

    public float viewHeight() {
        return this.vh;
    }

    public float normedDiagonalLength() {
        return (float)Math.sqrt((this.vw * this.vw + this.vh * this.vh) / 2.0f);
    }

    public float em() {
        return this.em;
    }

    public float rem() {
        return SVGFont.defaultFontSize();
    }

    public float ex() {
        return this.ex;
    }

    public String toString() {
        return "MeasureContext{vw=" + this.vw + ", vh=" + this.vh + ", em=" + this.em + ", ex=" + this.ex + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MeasureContext)) {
            return false;
        }
        MeasureContext that = (MeasureContext)o;
        return Float.compare(that.vw, this.vw) == 0 && Float.compare(that.vh, this.vh) == 0 && Float.compare(that.em, this.em) == 0 && Float.compare(that.ex, this.ex) == 0;
    }

    public int hashCode() {
        return Objects.hash(Float.valueOf(this.vw), Float.valueOf(this.vh), Float.valueOf(this.em), Float.valueOf(this.ex));
    }
}

