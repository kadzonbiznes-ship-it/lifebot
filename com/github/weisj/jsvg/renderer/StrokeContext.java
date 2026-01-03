/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.renderer;

import com.github.weisj.jsvg.attributes.stroke.LineCap;
import com.github.weisj.jsvg.attributes.stroke.LineJoin;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.Unit;
import com.github.weisj.jsvg.parser.AttributeNode;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StrokeContext {
    @Nullable
    public final Length strokeWidth;
    @Nullable
    public final LineCap lineCap;
    @Nullable
    public final LineJoin lineJoin;
    public final float miterLimit;
    public final Length[] dashPattern;
    @Nullable
    public final Length dashOffset;

    public StrokeContext(@Nullable Length strokeWidth, @Nullable LineCap lineCap, @Nullable LineJoin lineJoin, float miterLimit, @NotNull Length[] dashPattern, @Nullable Length dashOffset) {
        this.strokeWidth = strokeWidth;
        this.lineCap = lineCap;
        this.lineJoin = lineJoin;
        this.miterLimit = miterLimit;
        this.dashPattern = StrokeContext.validateDashPattern(dashPattern);
        this.dashOffset = dashOffset;
    }

    private static Length[] validateDashPattern(@NotNull Length[] pattern) {
        if (pattern.length == 0) {
            return pattern;
        }
        for (Length length : pattern) {
            if (length.raw() < 0.0f) {
                return new Length[0];
            }
            if (length.isZero()) continue;
            return pattern;
        }
        return new Length[0];
    }

    @NotNull
    public StrokeContext derive(@Nullable StrokeContext context) {
        if (context == null) {
            return this;
        }
        if (context.isTrivial()) {
            return this;
        }
        return new StrokeContext(context.strokeWidth != null ? context.strokeWidth : this.strokeWidth, context.lineCap != null ? context.lineCap : this.lineCap, context.lineJoin != null ? context.lineJoin : this.lineJoin, Length.isSpecified(context.miterLimit) ? context.miterLimit : this.miterLimit, context.dashPattern.length > 0 ? context.dashPattern : this.dashPattern, context.dashOffset != null ? context.dashOffset : this.dashOffset);
    }

    public boolean isTrivial() {
        return this.strokeWidth == null && this.lineCap == null && this.lineJoin == null && Length.isUnspecified(this.miterLimit) && this.dashPattern.length == 0 && this.dashOffset == null;
    }

    @NotNull
    public static StrokeContext createDefault() {
        return new StrokeContext(Unit.Raw.valueOf(1.0f), LineCap.Butt, LineJoin.Miter, 4.0f, new Length[0], Length.ZERO);
    }

    @NotNull
    public static StrokeContext parse(@NotNull AttributeNode attributeNode) {
        return new StrokeContext(attributeNode.getLength("stroke-width"), attributeNode.getEnumNullable("stroke-linecap", LineCap.class), attributeNode.getEnumNullable("stroke-linejoin", LineJoin.class), attributeNode.getNonNegativeFloat("stroke-miterlimit", Float.NaN), attributeNode.getLengthList("stroke-dasharray"), attributeNode.getLength("stroke-dashoffset"));
    }

    public String toString() {
        return "StrokeContext{strokeWidth=" + this.strokeWidth + ", lineCap=" + (Object)((Object)this.lineCap) + ", lineJoin=" + (Object)((Object)this.lineJoin) + ", miterLimit=" + this.miterLimit + ", dashPattern=" + Arrays.toString(this.dashPattern) + ", dashOffset=" + this.dashOffset + '}';
    }
}

