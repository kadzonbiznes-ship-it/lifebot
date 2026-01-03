/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.prototype;

import com.github.weisj.jsvg.attributes.FillRule;
import com.github.weisj.jsvg.attributes.font.MeasurableFontSpec;
import com.github.weisj.jsvg.nodes.prototype.HasFillRule;
import com.github.weisj.jsvg.nodes.prototype.HasFontContext;
import com.github.weisj.jsvg.nodes.prototype.HasFontRenderContext;
import com.github.weisj.jsvg.nodes.prototype.HasPaintContext;
import com.github.weisj.jsvg.nodes.prototype.Mutator;
import com.github.weisj.jsvg.renderer.FontRenderContext;
import com.github.weisj.jsvg.renderer.PaintContext;
import org.jetbrains.annotations.NotNull;

public interface HasContext
extends HasPaintContext,
HasFontContext,
HasFontRenderContext,
HasFillRule {

    public static interface ByDelegate
    extends HasContext {
        @NotNull
        public HasContext contextDelegate();

        @Override
        @NotNull
        default public FillRule fillRule() {
            return this.contextDelegate().fillRule();
        }

        @Override
        @NotNull
        default public Mutator<MeasurableFontSpec> fontSpec() {
            return this.contextDelegate().fontSpec();
        }

        @Override
        @NotNull
        default public FontRenderContext fontRenderContext() {
            return this.contextDelegate().fontRenderContext();
        }

        @Override
        @NotNull
        default public Mutator<PaintContext> paintContext() {
            return this.contextDelegate().paintContext();
        }
    }
}

