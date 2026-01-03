/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.parser.ValueUIFuture
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg.nodes;

import com.github.weisj.jsvg.SVGRenderingHints;
import com.github.weisj.jsvg.attributes.Overflow;
import com.github.weisj.jsvg.attributes.PreserveAspectRatio;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.MeasureContext;
import com.github.weisj.jsvg.nodes.RenderableSVGNode;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.parser.UIFuture;
import com.github.weisj.jsvg.parser.ValueUIFuture;
import com.github.weisj.jsvg.renderer.GraphicsUtil;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ElementCategories(value={Category.Graphic, Category.GraphicsReferencing})
@PermittedContent(categories={Category.Animation, Category.Descriptive})
public final class Image
extends RenderableSVGNode {
    private static final Logger LOGGER = Logger.getLogger(Image.class.getName());
    public static final String TAG = "image";
    private Length x;
    private Length y;
    private Length width;
    private Length height;
    private PreserveAspectRatio preserveAspectRatio;
    private Overflow overflow;
    private UIFuture<BufferedImage> imgResource;

    @Override
    @NotNull
    public String tagName() {
        return TAG;
    }

    @Override
    public boolean isVisible(@NotNull RenderContext context) {
        return this.imgResource != null && super.isVisible(context);
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.x = attributeNode.getLength("x", 0.0f);
        this.y = attributeNode.getLength("y", 0.0f);
        this.width = attributeNode.getLength("width", Length.UNSPECIFIED);
        this.height = attributeNode.getLength("height", Length.UNSPECIFIED);
        this.preserveAspectRatio = PreserveAspectRatio.parse(attributeNode.getValue("preserveAspectRatio"), attributeNode.parser());
        this.overflow = attributeNode.getEnum("overflow", Overflow.Hidden);
        String url = attributeNode.parser().parseUrl(attributeNode.getHref());
        if (url != null) {
            try {
                this.imgResource = attributeNode.resourceLoader().loadImage(new URI(url));
            }
            catch (IOException | URISyntaxException e) {
                LOGGER.log(Level.INFO, e.getMessage(), e);
                this.imgResource = null;
            }
        }
    }

    @Nullable
    private BufferedImage fetchImage(@NotNull RenderContext context) {
        if (this.imgResource == null) {
            return null;
        }
        if (this.imgResource instanceof ValueUIFuture) {
            return this.imgResource.get();
        }
        if (!this.imgResource.checkIfReady(context.targetComponent())) {
            return null;
        }
        BufferedImage img = this.imgResource.get();
        if (img != null) {
            this.imgResource = new ValueUIFuture((Object)img);
        }
        return img;
    }

    @Override
    public void render(@NotNull RenderContext context, @NotNull Graphics2D g) {
        BufferedImage img = this.fetchImage(context);
        if (img == null) {
            return;
        }
        MeasureContext measure = context.measureContext();
        int imgWidth = img.getWidth(context.targetComponent());
        int imgHeight = img.getHeight(context.targetComponent());
        if (imgWidth == 0 || imgHeight == 0) {
            return;
        }
        float viewWidth = this.width.orElseIfUnspecified(imgWidth).resolveWidth(measure);
        float viewHeight = this.height.orElseIfUnspecified(imgHeight).resolveHeight(measure);
        ViewBox viewBox = new ViewBox(imgWidth, imgHeight);
        g.translate(this.x.resolveWidth(measure), this.y.resolveHeight(measure));
        if (this.overflow.establishesClip()) {
            g.clip(new ViewBox(viewWidth, viewHeight));
        }
        AffineTransform imgTransform = this.preserveAspectRatio.computeViewPortTransform(new FloatSize(viewWidth, viewHeight), viewBox);
        Object imageAntialiasing = g.getRenderingHint(SVGRenderingHints.KEY_IMAGE_ANTIALIASING);
        if (imageAntialiasing == SVGRenderingHints.VALUE_IMAGE_ANTIALIASING_OFF) {
            g.drawImage(img, imgTransform, context.targetComponent());
        } else {
            g.transform(imgTransform);
            Rectangle imgRect = new Rectangle(0, 0, imgWidth, imgHeight);
            GraphicsUtil.safelySetPaint(g, new TexturePaint(img, imgRect));
            g.fill(imgRect);
        }
    }
}

