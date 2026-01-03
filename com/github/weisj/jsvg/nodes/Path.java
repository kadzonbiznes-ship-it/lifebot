/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.attributes.FillRule;
import com.github.weisj.jsvg.geometry.AWTSVGShape;
import com.github.weisj.jsvg.geometry.MeasurableShape;
import com.github.weisj.jsvg.nodes.ShapeNode;
import com.github.weisj.jsvg.nodes.prototype.HasFillRule;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.util.PathUtil;
import java.awt.Rectangle;
import org.jetbrains.annotations.NotNull;

@ElementCategories(value={Category.Graphic, Category.Shape})
@PermittedContent(categories={Category.Animation, Category.Descriptive})
public final class Path
extends ShapeNode
implements HasFillRule {
    public static final String TAG = "path";
    private FillRule fillRule;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Override
    @NotNull
    public FillRule fillRule() {
        return this.fillRule;
    }

    @Override
    @NotNull
    protected MeasurableShape buildShape(@NotNull AttributeNode attributeNode) {
        this.fillRule = FillRule.parse(attributeNode);
        String pathValue = attributeNode.getValue("d");
        if (pathValue == null) {
            return new AWTSVGShape<Rectangle>(new Rectangle());
        }
        return PathUtil.parseFromPathData(pathValue, this.fillRule);
    }

    @Override
    protected boolean shouldPaintStartEndMarkersInMiddle() {
        return false;
    }
}

