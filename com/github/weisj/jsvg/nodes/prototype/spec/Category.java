/*
 * Decompiled with CFR 0.152.
 */
package com.github.weisj.jsvg.nodes.prototype.spec;

public enum Category {
    Animation(false),
    BasicShape,
    Container,
    Descriptive(false),
    FilterPrimitive,
    Gradient,
    Graphic,
    GraphicsReferencing,
    Shape,
    Structural,
    TextContent,
    TextContentChild,
    None;

    private final boolean effectivelyAllowed;

    private Category() {
        this(true);
    }

    private Category(boolean effectivelyAllowed) {
        this.effectivelyAllowed = effectivelyAllowed;
    }

    public boolean isEffectivelyAllowed() {
        return this.effectivelyAllowed;
    }
}

