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

public class ValidatePipe
implements PixelDrawPipe,
PixelFillPipe,
ShapeDrawPipe,
TextPipe,
DrawImagePipe {
    public boolean validate(SunGraphics2D sg) {
        sg.validatePipe();
        return true;
    }

    @Override
    public void drawLine(SunGraphics2D sg, int x1, int y1, int x2, int y2) {
        if (this.validate(sg)) {
            sg.drawpipe.drawLine(sg, x1, y1, x2, y2);
        }
    }

    @Override
    public void drawRect(SunGraphics2D sg, int x, int y, int width, int height) {
        if (this.validate(sg)) {
            sg.drawpipe.drawRect(sg, x, y, width, height);
        }
    }

    @Override
    public void fillRect(SunGraphics2D sg, int x, int y, int width, int height) {
        if (this.validate(sg)) {
            sg.fillpipe.fillRect(sg, x, y, width, height);
        }
    }

    @Override
    public void drawRoundRect(SunGraphics2D sg, int x, int y, int width, int height, int arcWidth, int arcHeight) {
        if (this.validate(sg)) {
            sg.drawpipe.drawRoundRect(sg, x, y, width, height, arcWidth, arcHeight);
        }
    }

    @Override
    public void fillRoundRect(SunGraphics2D sg, int x, int y, int width, int height, int arcWidth, int arcHeight) {
        if (this.validate(sg)) {
            sg.fillpipe.fillRoundRect(sg, x, y, width, height, arcWidth, arcHeight);
        }
    }

    @Override
    public void drawOval(SunGraphics2D sg, int x, int y, int width, int height) {
        if (this.validate(sg)) {
            sg.drawpipe.drawOval(sg, x, y, width, height);
        }
    }

    @Override
    public void fillOval(SunGraphics2D sg, int x, int y, int width, int height) {
        if (this.validate(sg)) {
            sg.fillpipe.fillOval(sg, x, y, width, height);
        }
    }

    @Override
    public void drawArc(SunGraphics2D sg, int x, int y, int width, int height, int startAngle, int arcAngle) {
        if (this.validate(sg)) {
            sg.drawpipe.drawArc(sg, x, y, width, height, startAngle, arcAngle);
        }
    }

    @Override
    public void fillArc(SunGraphics2D sg, int x, int y, int width, int height, int startAngle, int arcAngle) {
        if (this.validate(sg)) {
            sg.fillpipe.fillArc(sg, x, y, width, height, startAngle, arcAngle);
        }
    }

    @Override
    public void drawPolyline(SunGraphics2D sg, int[] xPoints, int[] yPoints, int nPoints) {
        if (this.validate(sg)) {
            sg.drawpipe.drawPolyline(sg, xPoints, yPoints, nPoints);
        }
    }

    @Override
    public void drawPolygon(SunGraphics2D sg, int[] xPoints, int[] yPoints, int nPoints) {
        if (this.validate(sg)) {
            sg.drawpipe.drawPolygon(sg, xPoints, yPoints, nPoints);
        }
    }

    @Override
    public void fillPolygon(SunGraphics2D sg, int[] xPoints, int[] yPoints, int nPoints) {
        if (this.validate(sg)) {
            sg.fillpipe.fillPolygon(sg, xPoints, yPoints, nPoints);
        }
    }

    @Override
    public void draw(SunGraphics2D sg, Shape s) {
        if (this.validate(sg)) {
            sg.shapepipe.draw(sg, s);
        }
    }

    @Override
    public void fill(SunGraphics2D sg, Shape s) {
        if (this.validate(sg)) {
            sg.shapepipe.fill(sg, s);
        }
    }

    @Override
    public void drawString(SunGraphics2D sg, String s, double x, double y) {
        if (this.validate(sg)) {
            sg.textpipe.drawString(sg, s, x, y);
        }
    }

    @Override
    public void drawGlyphVector(SunGraphics2D sg, GlyphVector g, float x, float y) {
        if (this.validate(sg)) {
            sg.textpipe.drawGlyphVector(sg, g, x, y);
        }
    }

    @Override
    public void drawChars(SunGraphics2D sg, char[] data, int offset, int length, int x, int y) {
        if (this.validate(sg)) {
            sg.textpipe.drawChars(sg, data, offset, length, x, y);
        }
    }

    @Override
    public boolean copyImage(SunGraphics2D sg, Image img, int x, int y, Color bgColor, ImageObserver observer) {
        if (this.validate(sg)) {
            return sg.imagepipe.copyImage(sg, img, x, y, bgColor, observer);
        }
        return false;
    }

    @Override
    public boolean copyImage(SunGraphics2D sg, Image img, int dx, int dy, int sx, int sy, int w, int h, Color bgColor, ImageObserver observer) {
        if (this.validate(sg)) {
            return sg.imagepipe.copyImage(sg, img, dx, dy, sx, sy, w, h, bgColor, observer);
        }
        return false;
    }

    @Override
    public boolean scaleImage(SunGraphics2D sg, Image img, int x, int y, int w, int h, Color bgColor, ImageObserver observer) {
        if (this.validate(sg)) {
            return sg.imagepipe.scaleImage(sg, img, x, y, w, h, bgColor, observer);
        }
        return false;
    }

    @Override
    public boolean scaleImage(SunGraphics2D sg, Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgColor, ImageObserver observer) {
        if (this.validate(sg)) {
            return sg.imagepipe.scaleImage(sg, img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgColor, observer);
        }
        return false;
    }

    @Override
    public boolean transformImage(SunGraphics2D sg, Image img, AffineTransform atfm, ImageObserver observer) {
        if (this.validate(sg)) {
            return sg.imagepipe.transformImage(sg, img, atfm, observer);
        }
        return false;
    }

    @Override
    public void transformImage(SunGraphics2D sg, BufferedImage img, BufferedImageOp op, int x, int y) {
        if (this.validate(sg)) {
            sg.imagepipe.transformImage(sg, img, op, x, y);
        }
    }
}

