/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry.size;

import com.github.weisj.jsvg.geometry.size.Length;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;

public enum Unit {
    PX,
    CM,
    MM,
    IN,
    EM,
    REM,
    EX,
    PT,
    PC,
    PERCENTAGE("%"),
    Raw("");

    private static final Unit[] units;
    @NotNull
    private final String suffix;

    public static Unit[] units() {
        return units;
    }

    private Unit(String suffix) {
        this.suffix = suffix;
    }

    private Unit() {
        this.suffix = this.name().toLowerCase(Locale.ENGLISH);
    }

    @NotNull
    public Length valueOf(float value) {
        if (value == 0.0f) {
            return Length.ZERO;
        }
        return new Length(this, value);
    }

    @NotNull
    public String suffix() {
        return this.suffix;
    }

    static {
        units = Unit.values();
    }
}

