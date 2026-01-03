/*
 * Decompiled with CFR 0.152.
 */
package java.awt.geom;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.Transient;

public abstract class RectangularShape
implements Shape,
Cloneable {
    protected RectangularShape() {
    }

    public abstract double getX();

    public abstract double getY();

    public abstract double getWidth();

    public abstract double getHeight();

    public double getMinX() {
        return this.getX();
    }

    public double getMinY() {
        return this.getY();
    }

    public double getMaxX() {
        return this.getX() + this.getWidth();
    }

    public double getMaxY() {
        return this.getY() + this.getHeight();
    }

    public double getCenterX() {
        return this.getX() + this.getWidth() / 2.0;
    }

    public double getCenterY() {
        return this.getY() + this.getHeight() / 2.0;
    }

    @Transient
    public Rectangle2D getFrame() {
        return new Rectangle2D.Double(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    public abstract boolean isEmpty();

    public abstract void setFrame(double var1, double var3, double var5, double var7);

    public void setFrame(Point2D loc, Dimension2D size) {
        this.setFrame(loc.getX(), loc.getY(), size.getWidth(), size.getHeight());
    }

    public void setFrame(Rectangle2D r) {
        this.setFrame(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    public void setFrameFromDiagonal(double x1, double y1, double x2, double y2) {
        double t;
        if (x2 < x1) {
            t = x1;
            x1 = x2;
            x2 = t;
        }
        if (y2 < y1) {
            t = y1;
            y1 = y2;
            y2 = t;
        }
        this.setFrame(x1, y1, x2 - x1, y2 - y1);
    }

    public void setFrameFromDiagonal(Point2D p1, Point2D p2) {
        this.setFrameFromDiagonal(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public void setFrameFromCenter(double centerX, double centerY, double cornerX, double cornerY) {
        double halfW = Math.abs(cornerX - centerX);
        double halfH = Math.abs(cornerY - centerY);
        this.setFrame(centerX - halfW, centerY - halfH, halfW * 2.0, halfH * 2.0);
    }

    public void setFrameFromCenter(Point2D center, Point2D corner) {
        this.setFrameFromCenter(center.getX(), center.getY(), corner.getX(), corner.getY());
    }

    @Override
    public boolean contains(Point2D p) {
        return this.contains(p.getX(), p.getY());
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return this.intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return this.contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public Rectangle getBounds() {
        double width = this.getWidth();
        double height = this.getHeight();
        if (width < 0.0 || height < 0.0) {
            return new Rectangle();
        }
        double x = this.getX();
        double y = this.getY();
        double x1 = Math.floor(x);
        double y1 = Math.floor(y);
        double x2 = Math.ceil(x + width);
        double y2 = Math.ceil(y + height);
        return new Rectangle((int)x1, (int)y1, (int)(x2 - x1), (int)(y2 - y1));
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new FlatteningPathIterator(this.getPathIterator(at), flatness);
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}

