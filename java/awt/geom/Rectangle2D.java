/*
 * Decompiled with CFR 0.152.
 */
package java.awt.geom;

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.RectIterator;
import java.awt.geom.RectangularShape;
import java.io.Serializable;

public abstract class Rectangle2D
extends RectangularShape {
    public static final int OUT_LEFT = 1;
    public static final int OUT_TOP = 2;
    public static final int OUT_RIGHT = 4;
    public static final int OUT_BOTTOM = 8;

    protected Rectangle2D() {
    }

    public abstract void setRect(double var1, double var3, double var5, double var7);

    public void setRect(Rectangle2D r) {
        this.setRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    public boolean intersectsLine(double x1, double y1, double x2, double y2) {
        int out1;
        int out2 = this.outcode(x2, y2);
        if (out2 == 0) {
            return true;
        }
        while ((out1 = this.outcode(x1, y1)) != 0) {
            if ((out1 & out2) != 0) {
                return false;
            }
            if ((out1 & 5) != 0) {
                double x = this.getX();
                if ((out1 & 4) != 0) {
                    x += this.getWidth();
                }
                y1 += (x - x1) * (y2 - y1) / (x2 - x1);
                x1 = x;
                continue;
            }
            double y = this.getY();
            if ((out1 & 8) != 0) {
                y += this.getHeight();
            }
            x1 += (y - y1) * (x2 - x1) / (y2 - y1);
            y1 = y;
        }
        return true;
    }

    public boolean intersectsLine(Line2D l) {
        return this.intersectsLine(l.getX1(), l.getY1(), l.getX2(), l.getY2());
    }

    public abstract int outcode(double var1, double var3);

    public int outcode(Point2D p) {
        return this.outcode(p.getX(), p.getY());
    }

    @Override
    public void setFrame(double x, double y, double w, double h) {
        this.setRect(x, y, w, h);
    }

    @Override
    public Rectangle2D getBounds2D() {
        return (Rectangle2D)this.clone();
    }

    @Override
    public boolean contains(double x, double y) {
        double x0 = this.getX();
        double y0 = this.getY();
        return x >= x0 && y >= y0 && x < x0 + this.getWidth() && y < y0 + this.getHeight();
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        if (this.isEmpty() || w <= 0.0 || h <= 0.0) {
            return false;
        }
        double x0 = this.getX();
        double y0 = this.getY();
        return x + w > x0 && y + h > y0 && x < x0 + this.getWidth() && y < y0 + this.getHeight();
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        if (this.isEmpty() || w <= 0.0 || h <= 0.0) {
            return false;
        }
        double x0 = this.getX();
        double y0 = this.getY();
        return x >= x0 && y >= y0 && x + w <= x0 + this.getWidth() && y + h <= y0 + this.getHeight();
    }

    public abstract Rectangle2D createIntersection(Rectangle2D var1);

    public static void intersect(Rectangle2D src1, Rectangle2D src2, Rectangle2D dest) {
        double x1 = Math.max(src1.getMinX(), src2.getMinX());
        double y1 = Math.max(src1.getMinY(), src2.getMinY());
        double x2 = Math.min(src1.getMaxX(), src2.getMaxX());
        double y2 = Math.min(src1.getMaxY(), src2.getMaxY());
        dest.setFrame(x1, y1, x2 - x1, y2 - y1);
    }

    public abstract Rectangle2D createUnion(Rectangle2D var1);

    public static void union(Rectangle2D src1, Rectangle2D src2, Rectangle2D dest) {
        double x1 = Math.min(src1.getMinX(), src2.getMinX());
        double y1 = Math.min(src1.getMinY(), src2.getMinY());
        double x2 = Math.max(src1.getMaxX(), src2.getMaxX());
        double y2 = Math.max(src1.getMaxY(), src2.getMaxY());
        dest.setFrameFromDiagonal(x1, y1, x2, y2);
    }

    public void add(double newx, double newy) {
        double x1 = Math.min(this.getMinX(), newx);
        double x2 = Math.max(this.getMaxX(), newx);
        double y1 = Math.min(this.getMinY(), newy);
        double y2 = Math.max(this.getMaxY(), newy);
        this.setRect(x1, y1, x2 - x1, y2 - y1);
    }

    public void add(Point2D pt) {
        this.add(pt.getX(), pt.getY());
    }

    public void add(Rectangle2D r) {
        double x1 = Math.min(this.getMinX(), r.getMinX());
        double x2 = Math.max(this.getMaxX(), r.getMaxX());
        double y1 = Math.min(this.getMinY(), r.getMinY());
        double y2 = Math.max(this.getMaxY(), r.getMaxY());
        this.setRect(x1, y1, x2 - x1, y2 - y1);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return new RectIterator(this, at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new RectIterator(this, at);
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
        if (obj instanceof Rectangle2D) {
            Rectangle2D r2d = (Rectangle2D)obj;
            return this.getX() == r2d.getX() && this.getY() == r2d.getY() && this.getWidth() == r2d.getWidth() && this.getHeight() == r2d.getHeight();
        }
        return false;
    }

    public static class Double
    extends Rectangle2D
    implements Serializable {
        public double x;
        public double y;
        public double width;
        public double height;
        private static final long serialVersionUID = 7771313791441850493L;

        public Double() {
        }

        public Double(double x, double y, double w, double h) {
            this.setRect(x, y, w, h);
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
        public void setRect(double x, double y, double w, double h) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }

        @Override
        public void setRect(Rectangle2D r) {
            this.x = r.getX();
            this.y = r.getY();
            this.width = r.getWidth();
            this.height = r.getHeight();
        }

        @Override
        public int outcode(double x, double y) {
            int out = 0;
            if (this.width <= 0.0) {
                out |= 5;
            } else if (x < this.x) {
                out |= 1;
            } else if (x > this.x + this.width) {
                out |= 4;
            }
            if (this.height <= 0.0) {
                out |= 0xA;
            } else if (y < this.y) {
                out |= 2;
            } else if (y > this.y + this.height) {
                out |= 8;
            }
            return out;
        }

        @Override
        public Rectangle2D getBounds2D() {
            return new Double(this.x, this.y, this.width, this.height);
        }

        @Override
        public Rectangle2D createIntersection(Rectangle2D r) {
            Double dest = new Double();
            Rectangle2D.intersect(this, r, dest);
            return dest;
        }

        @Override
        public Rectangle2D createUnion(Rectangle2D r) {
            Double dest = new Double();
            Rectangle2D.union(this, r, dest);
            return dest;
        }

        public String toString() {
            return this.getClass().getName() + "[x=" + this.x + ",y=" + this.y + ",w=" + this.width + ",h=" + this.height + "]";
        }
    }

    public static class Float
    extends Rectangle2D
    implements Serializable {
        public float x;
        public float y;
        public float width;
        public float height;
        private static final long serialVersionUID = 3798716824173675777L;

        public Float() {
        }

        public Float(float x, float y, float w, float h) {
            this.setRect(x, y, w, h);
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
            return this.width <= 0.0f || this.height <= 0.0f;
        }

        public void setRect(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        }

        @Override
        public void setRect(double x, double y, double w, double h) {
            this.x = (float)x;
            this.y = (float)y;
            this.width = (float)w;
            this.height = (float)h;
        }

        @Override
        public void setRect(Rectangle2D r) {
            this.x = (float)r.getX();
            this.y = (float)r.getY();
            this.width = (float)r.getWidth();
            this.height = (float)r.getHeight();
        }

        @Override
        public int outcode(double x, double y) {
            int out = 0;
            if (this.width <= 0.0f) {
                out |= 5;
            } else if (x < (double)this.x) {
                out |= 1;
            } else if (x > (double)this.x + (double)this.width) {
                out |= 4;
            }
            if (this.height <= 0.0f) {
                out |= 0xA;
            } else if (y < (double)this.y) {
                out |= 2;
            } else if (y > (double)this.y + (double)this.height) {
                out |= 8;
            }
            return out;
        }

        @Override
        public Rectangle2D getBounds2D() {
            return new Float(this.x, this.y, this.width, this.height);
        }

        @Override
        public Rectangle2D createIntersection(Rectangle2D r) {
            Rectangle2D dest = r instanceof Float ? new Float() : new Double();
            Rectangle2D.intersect(this, r, dest);
            return dest;
        }

        @Override
        public Rectangle2D createUnion(Rectangle2D r) {
            Rectangle2D dest = r instanceof Float ? new Float() : new Double();
            Rectangle2D.union(this, r, dest);
            return dest;
        }

        public String toString() {
            return this.getClass().getName() + "[x=" + this.x + ",y=" + this.y + ",w=" + this.width + ",h=" + this.height + "]";
        }
    }
}

