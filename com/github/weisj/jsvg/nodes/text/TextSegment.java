/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.nodes.text.GlyphCursor
 *  com.github.weisj.jsvg.nodes.text.TextMetrics
 *  com.github.weisj.jsvg.nodes.text.TextSegment$RenderableSegment$UseTextLengthForCalculation
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.text;

import com.github.weisj.jsvg.nodes.text.GlyphCursor;
import com.github.weisj.jsvg.nodes.text.TextMetrics;
import com.github.weisj.jsvg.nodes.text.TextSegment;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import org.jetbrains.annotations.NotNull;

interface TextSegment {

    public static interface RenderableSegment
    extends TextSegment {
        public void prepareSegmentForRendering(@NotNull GlyphCursor var1, @NotNull RenderContext var2);

        public void renderSegmentWithoutLayout(@NotNull GlyphCursor var1, @NotNull RenderContext var2, @NotNull Graphics2D var3);

        public boolean hasFixedLength();

        @NotNull
        public TextMetrics computeTextMetrics(@NotNull RenderContext var1, @NotNull UseTextLengthForCalculation var2);

        public void appendTextShape(@NotNull GlyphCursor var1, @NotNull Path2D var2, @NotNull RenderContext var3);
    }
}

