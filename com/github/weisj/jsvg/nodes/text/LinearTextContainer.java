/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.weisj.jsvg.nodes.text.GlyphCursor
 *  org.jetbrains.annotations.MustBeInvokedByOverriders
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.nodes.text;

import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.nodes.text.GlyphCursor;
import com.github.weisj.jsvg.nodes.text.TextContainer;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

abstract class LinearTextContainer
extends TextContainer {
    protected Length[] x;
    protected Length[] y;
    protected Length[] dx;
    protected Length[] dy;
    protected float[] rotate;

    LinearTextContainer() {
    }

    @Override
    @MustBeInvokedByOverriders
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        this.x = attributeNode.getLengthList("x");
        this.y = attributeNode.getLengthList("y");
        this.dx = attributeNode.getLengthList("dx");
        this.dy = attributeNode.getLengthList("dy");
        this.rotate = attributeNode.getFloatList("rotate");
    }

    @Override
    @NotNull
    public Shape untransformedElementShape(@NotNull RenderContext context) {
        Path2D.Float textPath = new Path2D.Float();
        this.appendTextShape(this.createCursor(), textPath, context);
        return textPath;
    }

    @Override
    public void render(@NotNull RenderContext context, @NotNull Graphics2D g) {
        this.renderSegment(this.createCursor(), context, g);
    }

    @NotNull
    private GlyphCursor createCursor() {
        return new GlyphCursor(0.0f, 0.0f, new AffineTransform());
    }

    @Override
    protected GlyphCursor createLocalCursor(@NotNull RenderContext context, @NotNull GlyphCursor current) {
        GlyphCursor local = current.derive();
        if (this.x.length != 0) {
            local.xLocations = this.x;
            local.xOff = 0;
        }
        if (this.y.length != 0) {
            local.yLocations = this.y;
            local.yOff = 0;
        }
        if (this.dx.length != 0) {
            local.xDeltas = this.dx;
            local.dyOff = 0;
        }
        if (this.dy.length != 0) {
            local.yDeltas = this.dy;
            local.dyOff = 0;
        }
        if (this.rotate.length != 0) {
            local.rotations = this.rotate;
            local.rotOff = 0;
        }
        return local;
    }

    @Override
    protected void cleanUpLocalCursor(@NotNull GlyphCursor current, @NotNull GlyphCursor local) {
        current.updateFrom(local);
        if (this.x.length == 0) {
            current.xOff = local.xOff;
        }
        if (this.y.length == 0) {
            current.yOff = local.yOff;
        }
        if (this.dx.length == 0) {
            current.dxOff = local.dxOff;
        }
        if (this.dy.length == 0) {
            current.dyOff = local.dyOff;
        }
        if (this.rotate.length == 0) {
            current.rotOff = local.rotOff;
        }
    }
}

