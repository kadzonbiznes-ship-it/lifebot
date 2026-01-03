/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public abstract class Graphics2D
extends Graphics {
    protected Graphics2D() {
    }

    @Override
    public void draw3DRect(int x, int y, int width, int height, boolean raised) {
        Paint p = this.getPaint();
        Color c = this.getColor();
        Color brighter = c.brighter();
        Color darker = c.darker();
        this.setColor(raised ? brighter : darker);
        this.fillRect(x, y, 1, height + 1);
        this.fillRect(x + 1, y, width - 1, 1);
        this.setColor(raised ? darker : brighter);
        this.fillRect(x + 1, y + height, width, 1);
        this.fillRect(x + width, y, 1, height);
        this.setPaint(p);
    }

    @Override
    public void fill3DRect(int x, int y, int width, int height, boolean raised) {
        Paint p = this.getPaint();
        Color c = this.getColor();
        Color brighter = c.brighter();
        Color darker = c.darker();
        if (!raised) {
            this.setColor(darker);
        } else if (p != c) {
            this.setColor(c);
        }
        this.fillRect(x + 1, y + 1, width - 2, height - 2);
        this.setColor(raised ? brighter : darker);
        this.fillRect(x, y, 1, height);
        this.fillRect(x + 1, y, width - 2, 1);
        this.setColor(raised ? darker : brighter);
        this.fillRect(x + 1, y + height - 1, width - 1, 1);
        this.fillRect(x + width - 1, y, 1, height - 1);
        this.setPaint(p);
    }

    public abstract void draw(Shape var1);

    public abstract boolean drawImage(Image var1, AffineTransform var2, ImageObserver var3);

    public abstract void drawImage(BufferedImage var1, BufferedImageOp var2, int var3, int var4);

    public abstract void drawRenderedImage(RenderedImage var1, AffineTransform var2);

    public abstract void drawRenderableImage(RenderableImage var1, AffineTransform var2);

    @Override
    public abstract void drawString(String var1, int var2, int var3);

    public abstract void drawString(String var1, float var2, float var3);

    @Override
    public abstract void drawString(AttributedCharacterIterator var1, int var2, int var3);

    public abstract void drawString(AttributedCharacterIterator var1, float var2, float var3);

    public abstract void drawGlyphVector(GlyphVector var1, float var2, float var3);

    public abstract void fill(Shape var1);

    public abstract boolean hit(Rectangle var1, Shape var2, boolean var3);

    public abstract GraphicsConfiguration getDeviceConfiguration();

    public abstract void setComposite(Composite var1);

    public abstract void setPaint(Paint var1);

    public abstract void setStroke(Stroke var1);

    public abstract void setRenderingHint(RenderingHints.Key var1, Object var2);

    public abstract Object getRenderingHint(RenderingHints.Key var1);

    public abstract void setRenderingHints(Map<?, ?> var1);

    public abstract void addRenderingHints(Map<?, ?> var1);

    public abstract RenderingHints getRenderingHints();

    @Override
    public abstract void translate(int var1, int var2);

    public abstract void translate(double var1, double var3);

    public abstract void rotate(double var1);

    public abstract void rotate(double var1, double var3, double var5);

    public abstract void scale(double var1, double var3);

    public abstract void shear(double var1, double var3);

    public abstract void transform(AffineTransform var1);

    public abstract void setTransform(AffineTransform var1);

    public abstract AffineTransform getTransform();

    public abstract Paint getPaint();

    public abstract Composite getComposite();

    public abstract void setBackground(Color var1);

    public abstract Color getBackground();

    public abstract Stroke getStroke();

    public abstract void clip(Shape var1);

    public abstract FontRenderContext getFontRenderContext();
}

