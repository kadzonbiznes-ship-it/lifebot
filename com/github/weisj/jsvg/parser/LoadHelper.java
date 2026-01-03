/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.parser;

import com.github.weisj.jsvg.attributes.AttributeParser;
import com.github.weisj.jsvg.parser.ResourceLoader;
import org.jetbrains.annotations.NotNull;

public final class LoadHelper {
    @NotNull
    private final AttributeParser attributeParser;
    @NotNull
    private final ResourceLoader resourceLoader;

    public LoadHelper(@NotNull AttributeParser attributeParser, @NotNull ResourceLoader resourceLoader) {
        this.attributeParser = attributeParser;
        this.resourceLoader = resourceLoader;
    }

    @NotNull
    public AttributeParser attributeParser() {
        return this.attributeParser;
    }

    @NotNull
    public ResourceLoader resourceLoader() {
        return this.resourceLoader;
    }
}

