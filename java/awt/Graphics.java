/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;

public abstract class Graphics {
    protected Graphics() {
    }

    public abstract Graphics create();

    public Graphics create(int x, int y, int width, int height) {
        Graphics g = this.create();
        if (g == null) {
            return null;
        }
        g.translate(x, y);
        g.clipRect(0, 0, width, height);
        return g;
    }

    public abstract void translate(int var1, int var2);

    public abstract Color getColor();

    public abstract void setColor(Color var1);

    public abstract void setPaintMode();

    public abstract void setXORMode(Color var1);

    public abstract Font getFont();

    public abstract void setFont(Font var1);

    public FontMetrics getFontMetrics() {
        return this.getFontMetrics(this.getFont());
    }

    public abstract FontMetrics getFontMetrics(Font var1);

    public abstract Rectangle getClipBounds();

    public abstract void clipRect(int var1, int var2, int var3, int var4);

    public abstract void setClip(int var1, int var2, int var3, int var4);

    public abstract Shape getClip();

    public abstract void setClip(Shape var1);

    public abstract void copyArea(int var1, int var2, int var3, int var4, int var5, int var6);

    public abstract void drawLine(int var1, int var2, int var3, int var4);

    public abstract void fillRect(int var1, int var2, int var3, int var4);

    public void drawRect(int x, int y, int width, int height) {
        if (width < 0 || height < 0) {
            return;
        }
        if (height == 0 || width == 0) {
            this.drawLine(x, y, x + width, y + height);
        } else {
            this.drawLine(x, y, x + width - 1, y);
            this.drawLine(x + width, y, x + width, y + height - 1);
            this.drawLine(x + width, y + height, x + 1, y + height);
            this.drawLine(x, y + height, x, y + 1);
        }
    }

    public abstract void clearRect(int var1, int var2, int var3, int var4);

    public abstract void drawRoundRect(int var1, int var2, int var3, int var4, int var5, int var6);

    public abstract void fillRoundRect(int var1, int var2, int var3, int var4, int var5, int var6);

    public void draw3DRect(int x, int y, int width, int height, boolean raised) {
        Color c = this.getColor();
        Color brighter = c.brighter();
        Color darker = c.darker();
        this.setColor(raised ? brighter : darker);
        this.drawLine(x, y, x, y + height);
        this.drawLine(x + 1, y, x + width - 1, y);
        this.setColor(raised ? darker : brighter);
        this.drawLine(x + 1, y + height, x + width, y + height);
        this.drawLine(x + width, y, x + width, y + height - 1);
        this.setColor(c);
    }

    public void fill3DRect(int x, int y, int width, int height, boolean raised) {
        Color c = this.getColor();
        Color brighter = c.brighter();
        Color darker = c.darker();
        if (!raised) {
            this.setColor(darker);
        }
        this.fillRect(x + 1, y + 1, width - 2, height - 2);
        this.setColor(raised ? brighter : darker);
        this.drawLine(x, y, x, y + height - 1);
        this.drawLine(x + 1, y, x + width - 2, y);
        this.setColor(raised ? darker : brighter);
        this.drawLine(x + 1, y + height - 1, x + width - 1, y + height - 1);
        this.drawLine(x + width - 1, y, x + width - 1, y + height - 2);
        this.setColor(c);
    }

    public abstract void drawOval(int var1, int var2, int var3, int var4);

    public abstract void fillOval(int var1, int var2, int var3, int var4);

    public abstract void drawArc(int var1, int var2, int var3, int var4, int var5, int var6);

    public abstract void fillArc(int var1, int var2, int var3, int var4, int var5, int var6);

    public abstract void drawPolyline(int[] var1, int[] var2, int var3);

    public abstract void drawPolygon(int[] var1, int[] var2, int var3);

    public void drawPolygon(Polygon p) {
        this.drawPolygon(p.xpoints, p.ypoints, p.npoints);
    }

    public abstract void fillPolygon(int[] var1, int[] var2, int var3);

    public void fillPolygon(Polygon p) {
        this.fillPolygon(p.xpoints, p.ypoints, p.npoints);
    }

    public abstract void drawString(String var1, int var2, int var3);

    public abstract void drawString(AttributedCharacterIterator var1, int var2, int var3);

    public void drawChars(char[] data, int offset, int length, int x, int y) {
        this.drawString(new String(data, offset, length), x, y);
    }

    public void drawBytes(byte[] data, int offset, int length, int x, int y) {
        this.drawString(new String(data, 0, offset, length), x, y);
    }

    public abstract boolean drawImage(Image var1, int var2, int var3, ImageObserver var4);

    public abstract boolean drawImage(Image var1, int var2, int var3, int var4, int var5, ImageObserver var6);

    public abstract boolean drawImage(Image var1, int var2, int var3, Color var4, ImageObserver var5);

    public abstract boolean drawImage(Image var1, int var2, int var3, int var4, int var5, Color var6, ImageObserver var7);

    public abstract boolean drawImage(Image var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, ImageObserver var10);

    public abstract boolean drawImage(Image var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, Color var10, ImageObserver var11);

    public abstract void dispose();

    @Deprecated(since="9", forRemoval=true)
    public void finalize() {
        this.dispose();
    }

    public String toString() {
        return this.getClass().getName() + "[font=" + String.valueOf(this.getFont()) + ",color=" + String.valueOf(this.getColor()) + "]";
    }

    @Deprecated
    public Rectangle getClipRect() {
        return this.getClipBounds();
    }

    public boolean hitClip(int x, int y, int width, int height) {
        Rectangle clipRect = this.getClipBounds();
        if (clipRect == null) {
            return true;
        }
        return clipRect.intersects(x, y, width, height);
    }

    public Rectangle getClipBounds(Rectangle r) {
        Rectangle clipRect = this.getClipBounds();
        if (clipRect != null) {
            r.x = clipRect.x;
            r.y = clipRect.y;
            r.width = clipRect.width;
            r.height = clipRect.height;
        } else if (r == null) {
            throw new NullPointerException("null rectangle parameter");
        }
        return r;
    }
}

