/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.attributes.UnitType
 *  com.github.weisj.jsvg.renderer.MaskedPaint
 *  com.github.weisj.jsvg.util.BlittableImage
 *  com.github.weisj.jsvg.util.ImageUtil
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.attributes.UnitType;
import com.github.weisj.jsvg.attributes.paint.PaintParser;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.Unit;
import com.github.weisj.jsvg.nodes.Anchor;
import com.github.weisj.jsvg.nodes.ClipPath;
import com.github.weisj.jsvg.nodes.Image;
import com.github.weisj.jsvg.nodes.Marker;
import com.github.weisj.jsvg.nodes.Pattern;
import com.github.weisj.jsvg.nodes.SVGNode;
import com.github.weisj.jsvg.nodes.Style;
import com.github.weisj.jsvg.nodes.View;
import com.github.weisj.jsvg.nodes.container.CommonRenderableContainerNode;
import com.github.weisj.jsvg.nodes.filter.Filter;
import com.github.weisj.jsvg.nodes.prototype.Instantiator;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.nodes.text.Text;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.renderer.MaskedPaint;
import com.github.weisj.jsvg.renderer.RenderContext;
import com.github.weisj.jsvg.util.BlittableImage;
import com.github.weisj.jsvg.util.ImageUtil;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.Raster;
import org.jetbrains.annotations.NotNull;

@ElementCategories(value={Category.Container})
@PermittedContent(categories={Category.Animation, Category.Descriptive, Category.Shape, Category.Structural, Category.Gradient}, anyOf={Anchor.class, ClipPath.class, Filter.class, Image.class, Marker.class, Mask.class, Pattern.class, Style.class, Text.class, View.class})
public final class Mask
extends CommonRenderableContainerNode
implements Instantiator {
    private static final boolean DEBUG = false;
    public static final String TAG = "mask";
    private Length x;
    private Length y;
    private Length width;
    private Length height;
    private UnitType maskContentUnits;
    private UnitType maskUnits;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.x = attributeNode.getLength("x", Unit.PERCENTAGE.valueOf(-10.0f));
        this.y = attributeNode.getLength("y", Unit.PERCENTAGE.valueOf(-10.0f));
        this.width = attributeNode.getLength("width", Unit.PERCENTAGE.valueOf(120.0f));
        this.height = attributeNode.getLength("height", Unit.PERCENTAGE.valueOf(120.0f));
        this.maskContentUnits = attributeNode.getEnum("maskContentUnits", UnitType.UserSpaceOnUse);
        this.maskUnits = attributeNode.getEnum("maskUnits", UnitType.ObjectBoundingBox);
    }

    @NotNull
    public Paint createMaskPaint(@NotNull Graphics2D g, @NotNull RenderContext context, @NotNull Rectangle2D objectBounds) {
        Rectangle2D.Double maskBounds = this.maskUnits.computeViewBounds(context.measureContext(), objectBounds, this.x, this.y, this.width, this.height);
        BlittableImage blitImage = BlittableImage.create(ImageUtil::createLuminosityBuffer, (RenderContext)context, (Rectangle2D)g.getClipBounds(), (Rectangle2D)maskBounds.createIntersection(objectBounds), (Rectangle2D)objectBounds, (UnitType)this.maskContentUnits);
        Rectangle2D maskBoundsInUserSpace = blitImage.boundsInUserSpace();
        if (this.isInvalidMaskingArea(maskBoundsInUserSpace)) {
            return PaintParser.DEFAULT_COLOR;
        }
        blitImage.renderNode(g, (SVGNode)this, (Instantiator)this);
        Point2D.Double offset = new Point2D.Double(maskBoundsInUserSpace.getX(), maskBoundsInUserSpace.getY());
        context.rootTransform().transform(offset, offset);
        return new MaskedPaint((Paint)PaintParser.DEFAULT_COLOR, (Raster)blitImage.image().getRaster(), (Point2D)offset);
    }

    private boolean isInvalidMaskingArea(@NotNull Rectangle2D area) {
        return area.isEmpty() || Double.isNaN(area.getWidth()) || Double.isNaN(area.getHeight());
    }

    @Override
    public boolean requiresInstantiation() {
        return true;
    }

    @Override
    public boolean canInstantiate(@NotNull SVGNode node) {
        return node == this;
    }
}

