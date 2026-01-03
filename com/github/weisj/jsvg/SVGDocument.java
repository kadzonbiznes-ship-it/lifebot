/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.github.weisj.jsvg;

import com.github.weisj.jsvg.SVGRenderingHints;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.attributes.font.SVGFont;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.geometry.size.MeasureContext;
import com.github.weisj.jsvg.nodes.SVG;
import com.github.weisj.jsvg.renderer.NodeRenderer;
import com.github.weisj.jsvg.renderer.RenderContext;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Objects;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SVGDocument {
    private static final boolean DEBUG = false;
    @NotNull
    private final SVG root;
    @NotNull
    private final FloatSize size;

    public SVGDocument(@NotNull SVG root) {
        this.root = root;
        float em = SVGFont.defaultFontSize();
        this.size = root.sizeForTopLevel(em, SVGFont.exFromEm(em));
    }

    @NotNull
    public FloatSize size() {
        return this.size;
    }

    public void render(@Nullable JComponent component, @NotNull Graphics2D g) {
        this.render(component, g, null);
    }

    public void render(@Nullable JComponent component, @NotNull Graphics2D graphics2D, @Nullable ViewBox bounds) {
        Graphics2D g = (Graphics2D)graphics2D.create();
        this.setupSVGRenderingHints(g);
        Font f = g.getFont();
        if (f == null && component != null) {
            f = component.getFont();
        }
        float defaultEm = f != null ? f.getSize2D() : SVGFont.defaultFontSize();
        float defaultEx = SVGFont.exFromEm(defaultEm);
        MeasureContext initialMeasure = bounds != null ? MeasureContext.createInitial(bounds.size(), defaultEm, defaultEx) : MeasureContext.createInitial(this.root.sizeForTopLevel(defaultEm, defaultEx), defaultEm, defaultEx);
        RenderContext context = RenderContext.createInitial(component, initialMeasure);
        if (bounds == null) {
            bounds = new ViewBox(this.root.size(context));
        }
        this.root.applyTransform(g, context);
        g.clip(bounds);
        g.translate(bounds.x, bounds.y);
        try (NodeRenderer.Info info = NodeRenderer.createRenderInfo(this.root, context, g, null);){
            Objects.requireNonNull(info);
            this.root.renderWithSize(bounds.size(), this.root.viewBox(context), info.context, info.g);
        }
        g.dispose();
    }

    private void setupSVGRenderingHints(@NotNull Graphics2D g) {
        Object aaHint = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        if (aaHint != RenderingHints.VALUE_ANTIALIAS_DEFAULT) {
            this.setSVGRenderingHint(g, SVGRenderingHints.KEY_IMAGE_ANTIALIASING, aaHint == RenderingHints.VALUE_ANTIALIAS_ON ? SVGRenderingHints.VALUE_IMAGE_ANTIALIASING_ON : SVGRenderingHints.VALUE_IMAGE_ANTIALIASING_OFF);
        }
    }

    private void setSVGRenderingHint(@NotNull Graphics2D g, @NotNull RenderingHints.Key key, @NotNull Object o) {
        if (g.getRenderingHint(key) == null) {
            g.setRenderingHint(key, o);
        }
    }
}

