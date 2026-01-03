/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package com.github.weisj.jsvg.renderer;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

public final class GraphicsUtil {
    private static final Logger LOGGER = Logger.getLogger(GraphicsUtil.class.getName());

    private GraphicsUtil() {
    }

    public static void safelySetPaint(@NotNull Graphics2D g, @NotNull Paint paint) {
        g.setPaint(GraphicsUtil.setupPaint(g.getPaint(), paint));
    }

    @NotNull
    public static Paint setupPaint(@NotNull Paint current, @NotNull Paint paint) {
        if (current instanceof WrappingPaint) {
            ((WrappingPaint)((Object)current)).setPaint(paint);
            return current;
        }
        return paint;
    }

    @NotNull
    public static Graphics2D createGraphics(@NotNull BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.clipRect(0, 0, image.getWidth(), image.getHeight());
        return g;
    }

    @NotNull
    public static Composite deriveComposite(@NotNull Graphics2D g, float opacity) {
        Composite composite = g.getComposite();
        if (composite instanceof AlphaComposite) {
            AlphaComposite ac = (AlphaComposite)composite;
            return AlphaComposite.getInstance(ac.getRule(), ac.getAlpha() * opacity);
        }
        if (composite != null) {
            LOGGER.warning(String.format("Composite %s will be overridden by opacity %s", composite, Float.valueOf(opacity)));
        }
        return AlphaComposite.getInstance(3, opacity);
    }

    public static interface WrappingPaint {
        public void setPaint(@NotNull Paint var1);
    }
}

