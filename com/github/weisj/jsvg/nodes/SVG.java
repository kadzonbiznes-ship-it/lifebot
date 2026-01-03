/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.attributes.Overflow;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.MeasureContext;
import com.github.weisj.jsvg.geometry.size.Unit;
import com.github.weisj.jsvg.nodes.Anchor;
import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Image;
import com.github.weisj.jsvg.nodes.Marker;
import com.github.weisj.jsvg.nodes.Mask;
import com.github.weisj.jsvg.nodes.Pattern;
import com.github.weisj.jsvg.nodes.Style;
import com.github.weisj.jsvg.nodes.View;
import com.github.weisj.jsvg.nodes.container.CommonInnerViewContainer;
import com.github.weisj.jsvg.nodes.filter.Filter;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.nodes.text.Text;
import com.github.weisj.jsvg.parser.AttributeNode;
import java.awt.geom.Point2D;
import org.jetbrains.annotations.NotNull;

@ElementCategories(value={Category.Container, Category.Structural})
@PermittedContent(categories={Category.Animation, Category.Descriptive, Category.Shape, Category.Structural, Category.Gradient}, anyOf={Anchor.class, ClipPath.class, Filter.class, Image.class, Mask.class, Marker.class, Pattern.class, Style.class, Text.class, View.class})
public final class SVG
extends CommonInnerViewContainer {
    public static final String TAG = "svg";
    @NotNull
    private static final Length TOP_LEVEL_TRANSFORM_ORIGIN = Unit.PERCENTAGE.valueOf(50.0f);
    private static final float FALLBACK_WIDTH = 300.0f;
    private static final float FALLBACK_HEIGHT = 150.0f;
    private boolean isTopLevel;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    public boolean isTopLevel() {
        return this.isTopLevel;
    }

    @Override
    public boolean shouldTransform() {
        return !this.isTopLevel();
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        this.isTopLevel = attributeNode.parent() == null;
        super.build(attributeNode);
    }

    @Override
    @NotNull
    public Point2D transformOrigin(@NotNull MeasureContext context) {
        if (!this.isTopLevel) {
            return super.transformOrigin(context);
        }
        return new Point2D.Float(TOP_LEVEL_TRANSFORM_ORIGIN.resolveWidth(context), TOP_LEVEL_TRANSFORM_ORIGIN.resolveHeight(context));
    }

    @Override
    @NotNull
    protected Overflow defaultOverflow() {
        return this.isTopLevel ? Overflow.Visible : Overflow.Hidden;
    }

    @NotNull
    public FloatSize sizeForTopLevel(float em, float ex) {
        MeasureContext topLevelContext = MeasureContext.createInitial(new FloatSize(100.0f, 100.0f), em, ex);
        return new FloatSize(this.width.orElseIfUnspecified(this.viewBox != null ? this.viewBox.width : 300.0f).resolveWidth(topLevelContext), this.height.orElseIfUnspecified(this.viewBox != null ? this.viewBox.height : 150.0f).resolveHeight(topLevelContext));
    }
}

