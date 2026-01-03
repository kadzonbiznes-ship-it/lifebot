/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.geometry;

import com.github.weisj.jsvg.geometry.AWTSVGShape;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Shape;
import java.awt.geom.Path2D;
import org.jetbrains.annotations.NotNull;

public final class FillRuleAwareAWTSVGShape
extends AWTSVGShape<Path2D> {
    public FillRuleAwareAWTSVGShape(@NotNull Path2D shape) {
        super(shape);
    }

    @Override
    @NotNull
    public Shape shape(@NotNull RenderContext context, boolean validate) {
        ((Path2D)this.shape).setWindingRule(context.fillRule().awtWindingRule);
        return super.shape(context, validate);
    }
}

