/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.text;

import com.github.weisj.jsvg.nodes.Anchor;
import com.github.weisj.jsvg.nodes.prototype.HasGeometryContext;
import com.github.weisj.jsvg.nodes.prototype.impl.HasGeometryContextImpl;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.nodes.text.LinearTextContainer;
import com.github.weisj.jsvg.parser.AttributeNode;
import org.jetbrains.annotations.NotNull;

@ElementCategories(value={Category.Graphic, Category.TextContent})
@PermittedContent(categories={Category.Animation, Category.Descriptive, Category.TextContentChild}, anyOf={Anchor.class}, charData=true)
public final class Text
extends LinearTextContainer
implements HasGeometryContext.ByDelegate {
    public static final String TAG = "text";
    private HasGeometryContext geometryContext;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.geometryContext = HasGeometryContextImpl.parse(attributeNode);
    }

    @Override
    @NotNull
    public HasGeometryContext geometryContextDelegate() {
        return this.geometryContext;
    }
}

