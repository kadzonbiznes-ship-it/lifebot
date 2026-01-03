/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.attributes.paint;

import com.github.weisj.jsvg.attributes.paint.SVGPaint;
import com.github.weisj.jsvg.parser.AttributeNode;
import java.awt.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PaintParser {
    public static final Color DEFAULT_COLOR = Color.BLACK;

    @Nullable
    public Color parseColor(@NotNull String var1, @NotNull AttributeNode var2);

    @Nullable
    public SVGPaint parsePaint(@Nullable String var1, @NotNull AttributeNode var2);
}

