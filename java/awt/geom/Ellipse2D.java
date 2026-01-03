/*
 * Decompiled with CFR 0.152.
 */
package java.awt.geom;

import java.awt.geom.AffineTransform;
import java.awt.geom.EllipseIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.io.Serializable;

public abstract class Ellipse2D
extends RectangularShape {
    protected Ellipse2D() {
    }

    @Override
    public boolean contains(double x, double y) {
        double ellw = this.getWidth();
        if (ellw <= 0.0) {
            return false;
        }
        double normx = (x - this.getX()) / ellw - 0.5;
        double ellh = this.getHeight();
        if (ellh <= 0.0) {
            return false;
        }
        double normy = (y - this.getY()) / ellh - 0.5;
        return normx * normx + normy * normy < 0.25;
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        if (w <= 0.0 || h <= 0.0) {
            return false;
        }
        double ellw = this.getWidth();
        if (ellw <= 0.0) {
            return false;
        }
        double normx0 = (x - this.getX()) / ellw - 0.5;
        double normx1 = normx0 + w / ellw;
        double ellh = this.getHeight();
        if (ellh <= 0.0) {
            return false;
        }
        double normy0 = (y - this.getY()) / ellh - 0.5;
        double normy1 = normy0 + h / ellh;
        double nearx = normx0 > 0.0 ? normx0 : (normx1 < 0.0 ? normx1 : 0.0);
        double neary = normy0 > 0.0 ? normy0 : (normy1 < 0.0 ? normy1 : 0.0);
        return nearx * nearx + neary * neary < 0.25;
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return this.contains(x, y) && this.contains(x + w, y) && this.contains(x, y + h) && this.contains(x + w, y + h);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return new EllipseIterator(this, at);
    }

    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(this.getX());
        bits += java.lang.Double.doubleToLongBits(this.getY()) * 37L;
        bits += java.lang.Double.doubleToLongBits(this.getWidth()) * 43L;
        return (int)(bits += java.lang.Double.doubleToLongBits(this.getHeight()) * 47L) ^ (int)(bits >> 32);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Ellipse2D) {
            Ellipse2D e2d = (Ellipse2D)obj;
            return this.getX() == e2d.getX() && this.getY() == e2d.getY() && this.getWidth() == e2d.getWidth() && this.getHeight() == e2d.getHeight();
        }
        return false;
    }

    public static class Double
    extends Ellipse2D
    implements Serializable {
        public double x;
        public double y;
        public double width;
        public double height;
        private static final long serialVersionUID = 5555464816372320683L;

        public Double() {
        }

        public Double(double x, double y, double w, double h) {
            this.setFrame(x, y, w, h);
        }

        @Override
        public double getX() {
            return this.x;
        }

        @Override
        public double getY() {
            return this.y;
        }

        @Override
        public double getWidth() {
            return this.width;
        }

        @Override
        public double getHeight() {
            return this.height;
        }

        @Override
        public boolean isEmpty() {
            return this.width <= 0.0 || this.height <= 0.0;
        }

        @Override
        public void setFrame(double x, double y, double w, double h) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }

        @Override
        public Rectangle2D getBounds2D() {
            return new Rectangle2D.Double(this.x, this.y, this.width, this.height);
        }
    }

    public static class Float
    extends Ellipse2D
    implements Serializable {
        public float x;
        public float y;
        public float width;
        public float height;
        private static final long serialVersionUID = -6633761252372475977L;

        public Float() {
        }

        public Float(float x, float y, float w, float h) {
            this.setFrame(x, y, w, h);
        }

        @Override
        public double getX() {
            return this.x;
        }

        @Override
        public double getY() {
            return this.y;
        }

        @Override
        public double getWidth() {
            return this.width;
        }

        @Override
        public double getHeight() {
            return this.height;
        }

        @Override
        public boolean isEmpty() {
            return (double)this.width <= 0.0 || (double)this.height <= 0.0;
        }

        public void setFrame(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }

        @Override
        public void setFrame(double x, double y, double w, double h) {
            this.x = (float)x;
            this.y = (float)y;
            this.width = (float)w;
            this.height = (float)h;
        }

        @Override
        public Rectangle2D getBounds2D() {
            return new Rectangle2D.Float(this.x, this.y, this.width, this.height);
        }
    }
}

