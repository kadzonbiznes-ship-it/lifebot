/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.parser.AttributeNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MetaSVGNode
implements SVGNode {
    @Override
    @Nullable
    public String id() {
        return null;
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
    }

    @Override
    public void addContent(char[] content) {
    }
}

