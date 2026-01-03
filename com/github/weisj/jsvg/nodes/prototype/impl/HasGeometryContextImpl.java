/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes.prototype.impl;

import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.MeasureContext;
import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Mask;
import com.github.weisj.jsvg.nodes.filter.Filter;
import com.github.weisj.jsvg.nodes.prototype.HasGeometryContext;
import com.github.weisj.jsvg.parser.AttributeNode;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HasGeometryContextImpl
implements HasGeometryContext {
    @Nullable
    private final AffineTransform transform;
    @NotNull
    private final Length transformOriginX;
    @NotNull
    private final Length transformOriginY;
    @Nullable
    private final ClipPath clipPath;
    @Nullable
    private final Mask mask;
    @Nullable
    private final Filter filter;

    private HasGeometryContextImpl(@Nullable AffineTransform transform, @NotNull Length transformOriginX, @NotNull Length transformOriginY, @Nullable ClipPath clipPath, @Nullable Mask mask, @Nullable Filter filter) {
        this.transform = transform;
        this.transformOriginX = transformOriginX;
        this.transformOriginY = transformOriginY;
        this.clipPath = clipPath;
        this.mask = mask;
        this.filter = filter;
    }

    @NotNull
    public static HasGeometryContext parse(@NotNull AttributeNode attributeNode) {
        Length[] transformOrigin = attributeNode.getLengthList("transform-origin");
        return new HasGeometryContextImpl(attributeNode.parseTransform("transform"), transformOrigin.length > 0 ? transformOrigin[0] : Length.ZERO, transformOrigin.length > 1 ? transformOrigin[1] : Length.ZERO, attributeNode.getClipPath(), attributeNode.getMask(), attributeNode.getFilter());
    }

    @Override
    @Nullable
    public ClipPath clipPath() {
        return this.clipPath;
    }

    @Override
    @Nullable
    public Mask mask() {
        return this.mask;
    }

    @Override
    @Nullable
    public Filter filter() {
        return this.filter;
    }

    @Override
    @Nullable
    public AffineTransform transform() {
        return this.transform;
    }

    @Override
    @NotNull
    public Point2D transformOrigin(@NotNull MeasureContext context) {
        return new Point2D.Float(this.transformOriginX.resolveWidth(context), this.transformOriginY.resolveHeight(context));
    }
}

