/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.parser.DomProcessor
 *  com.github.weisj.jsvg.parser.css.impl.SimpleCssParser
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.parser;

import com.github.weisj.jsvg.attributes.paint.DefaultPaintParser;
import com.github.weisj.jsvg.attributes.paint.PaintParser;
import com.github.weisj.jsvg.parser.DomProcessor;
import com.github.weisj.jsvg.parser.ParserProvider;
import com.github.weisj.jsvg.parser.css.CssParser;
import com.github.weisj.jsvg.parser.css.impl.SimpleCssParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultParserProvider
implements ParserProvider {
    @Override
    @NotNull
    public PaintParser createPaintParser() {
        return new DefaultPaintParser();
    }

    @Override
    @NotNull
    public CssParser createCssParser() {
        return new SimpleCssParser();
    }

    @Override
    @Nullable
    public DomProcessor createPreProcessor() {
        return null;
    }

    @Override
    @Nullable
    public DomProcessor createPostProcessor() {
        return null;
    }
}

