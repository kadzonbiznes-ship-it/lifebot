/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.attributes.UnitType
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.attributes.UnitType;
import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.Use;
import com.github.weisj.jsvg.nodes.container.ContainerNode;
import com.github.weisj.jsvg.nodes.prototype.ShapedContainer;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.nodes.text.Text;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;

@ElementCategories(value={})
@PermittedContent(categories={Category.Animation, Category.Descriptive, Category.Shape}, anyOf={Use.class, Text.class})
public final class ClipPath
extends ContainerNode
implements ShapedContainer<SVGNode> {
    public static final String TAG = "clippath";
    private boolean isValid;
    private UnitType clipPathUnits;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    public boolean isValid() {
        return this.isValid;
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.isValid = this.checkIsValid();
        this.clipPathUnits = attributeNode.getEnum("clipPathUnits", UnitType.UserSpaceOnUse);
    }

    private boolean checkIsValid() {
        for (SVGNode sVGNode : this.children()) {
            SVGNode referenced;
            if (!(sVGNode instanceof Use) || (referenced = ((Use)sVGNode).referencedNode()) == null || this.isAcceptableType(referenced)) continue;
            return false;
        }
        return true;
    }

    @NotNull
    public Shape clipShape(@NotNull RenderContext context, @NotNull Rectangle2D elementBounds) {
        Area areaShape;
        Shape shape = ShapedContainer.super.elementShape(context);
        if (this.clipPathUnits == UnitType.ObjectBoundingBox) {
            shape = this.clipPathUnits.viewTransform(elementBounds).createTransformedShape(shape);
        }
        if ((areaShape = new Area(shape)).isRectangular()) {
            return areaShape.getBounds();
        }
        return areaShape;
    }
}

