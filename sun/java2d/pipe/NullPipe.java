/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.Color;
import java.awt.Image;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import sun.java2d.SunGraphics2D;
import sun.java2d.pipe.DrawImagePipe;
import sun.java2d.pipe.PixelDrawPipe;
import sun.java2d.pipe.PixelFillPipe;
import sun.java2d.pipe.ShapeDrawPipe;
import sun.java2d.pipe.TextPipe;

public class NullPipe
implements PixelDrawPipe,
PixelFillPipe,
ShapeDrawPipe,
TextPipe,
DrawImagePipe {
    @Override
    public void drawLine(SunGraphics2D sg, int x1, int y1, int x2, int y2) {
    }

    @Override
    public void drawRect(SunGraphics2D sg, int x, int y, int width, int height) {
    }

    @Override
    public void fillRect(SunGraphics2D sg, int x, int y, int width, int height) {
    }

    @Override
    public void drawRoundRect(SunGraphics2D sg, int x, int y, int width, int height, int arcWidth, int arcHeight) {
    }

    @Override
    public void fillRoundRect(SunGraphics2D sg, int x, int y, int width, int height, int arcWidth, int arcHeight) {
    }

    @Override
    public void drawOval(SunGraphics2D sg, int x, int y, int width, int height) {
    }

    @Override
    public void fillOval(SunGraphics2D sg, int x, int y, int width, int height) {
    }

    @Override
    public void drawArc(SunGraphics2D sg, int x, int y, int width, int height, int startAngle, int arcAngle) {
    }

    @Override
    public void fillArc(SunGraphics2D sg, int x, int y, int width, int height, int startAngle, int arcAngle) {
    }

    @Override
    public void drawPolyline(SunGraphics2D sg, int[] xPoints, int[] yPoints, int nPoints) {
    }

    @Override
    public void drawPolygon(SunGraphics2D sg, int[] xPoints, int[] yPoints, int nPoints) {
    }

    @Override
    public void fillPolygon(SunGraphics2D sg, int[] xPoints, int[] yPoints, int nPoints) {
    }

    @Override
    public void draw(SunGraphics2D sg, Shape s) {
    }

    @Override
    public void fill(SunGraphics2D sg, Shape s) {
    }

    @Override
    public void drawString(SunGraphics2D sg, String s, double x, double y) {
    }

    @Override
    public void drawGlyphVector(SunGraphics2D sg, GlyphVector g, float x, float y) {
    }

    @Override
    public void drawChars(SunGraphics2D sg, char[] data, int offset, int length, int x, int y) {
    }

    @Override
    public boolean copyImage(SunGraphics2D sg, Image img, int x, int y, Color bgColor, ImageObserver observer) {
        return false;
    }

    @Override
    public boolean copyImage(SunGraphics2D sg, Image img, int dx, int dy, int sx, int sy, int w, int h, Color bgColor, ImageObserver observer) {
        return false;
    }

    @Override
    public boolean scaleImage(SunGraphics2D sg, Image img, int x, int y, int w, int h, Color bgColor, ImageObserver observer) {
        return false;
    }

    @Override
    public boolean scaleImage(SunGraphics2D sg, Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgColor, ImageObserver observer) {
        return false;
    }

    @Override
    public boolean transformImage(SunGraphics2D sg, Image img, AffineTransform atfm, ImageObserver observer) {
        return false;
    }

    @Override
    public void transformImage(SunGraphics2D sg, BufferedImage img, BufferedImageOp op, int x, int y) {
    }
}

