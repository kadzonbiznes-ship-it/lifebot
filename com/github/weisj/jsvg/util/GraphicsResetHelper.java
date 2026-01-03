/*
 * Decompiled with CFR 0.152.
 */
package com.github.weisj.jsvg.util;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

public class GraphicsResetHelper {
    private final Graphics2D graphics;
    private final Composite originalComposite;
    private final Paint originalPaint;
    private final Stroke originalStroke;
    private final AffineTransform originalTransform;

    public GraphicsResetHelper(Graphics2D graphics) {
        this.graphics = graphics;
        this.originalComposite = graphics.getComposite();
        this.originalPaint = graphics.getPaint();
        this.originalStroke = graphics.getStroke();
        this.originalTransform = graphics.getTransform();
    }

    public Graphics2D graphics() {
        return this.graphics;
    }

    public void reset() {
        this.graphics.setComposite(this.originalComposite);
        this.graphics.setPaint(this.originalPaint);
        this.graphics.setStroke(this.originalStroke);
        this.graphics.setTransform(this.originalTransform);
    }
}

