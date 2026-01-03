/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.parser.DomProcessor
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser;

import com.github.weisj.jsvg.attributes.paint.PaintParser;
import com.github.weisj.jsvg.parser.DomProcessor;
import com.github.weisj.jsvg.parser.css.CssParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ParserProvider {
    @NotNull
    public PaintParser createPaintParser();

    @NotNull
    public CssParser createCssParser();

    @Nullable
    public DomProcessor createPreProcessor();

    @Nullable
    public DomProcessor createPostProcessor();
}

