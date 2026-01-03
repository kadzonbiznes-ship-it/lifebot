/*
 * Decompiled with CFR 0.152.
 */
package java.awt.geom;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectIterator;
import java.io.Serializable;

public abstract class RoundRectangle2D
extends RectangularShape {
    protected RoundRectangle2D() {
    }

    public abstract double getArcWidth();

    public abstract double getArcHeight();

    public abstract void setRoundRect(double var1, double var3, double var5, double var7, double var9, double var11);

    public void setRoundRect(RoundRectangle2D rr) {
        this.setRoundRect(rr.getX(), rr.getY(), rr.getWidth(), rr.getHeight(), rr.getArcWidth(), rr.getArcHeight());
    }

    @Override
    public void setFrame(double x, double y, double w, double h) {
        this.setRoundRect(x, y, w, h, this.getArcWidth(), this.getArcHeight());
    }

    @Override
    public boolean contains(double x, double y) {
        double d;
        double d2;
        if (this.isEmpty()) {
            return false;
        }
        double rrx0 = this.getX();
        double rry0 = this.getY();
        double rrx1 = rrx0 + this.getWidth();
        double rry1 = rry0 + this.getHeight();
        if (x < rrx0 || y < rry0 || x >= rrx1 || y >= rry1) {
            return false;
        }
        double aw = Math.min(this.getWidth(), Math.abs(this.getArcWidth())) / 2.0;
        double ah = Math.min(this.getHeight(), Math.abs(this.getArcHeight())) / 2.0;
        rrx0 += aw;
        if (x >= d2) {
            double d3;
            rrx0 = rrx1 - aw;
            if (x < d3) {
                return true;
            }
        }
        rry0 += ah;
        if (y >= d) {
            double d4;
            rry0 = rry1 - ah;
            if (y < d4) {
                return true;
            }
        }
        return (x = (x - rrx0) / aw) * x + (y = (y - rry0) / ah) * y <= 1.0;
    }

    private int classify(double coord, double left, double right, double arcsize) {
        if (coord < left) {
            return 0;
        }
        if (coord < left + arcsize) {
            return 1;
        }
        if (coord < right - arcsize) {
            return 2;
        }
        if (coord < right) {
            return 3;
        }
        return 4;
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        if (this.isEmpty() || w <= 0.0 || h <= 0.0) {
            return false;
        }
        double rrx0 = this.getX();
        double rry0 = this.getY();
        double rrx1 = rrx0 + this.getWidth();
        double rry1 = rry0 + this.getHeight();
        if (x + w <= rrx0 || x >= rrx1 || y + h <= rry0 || y >= rry1) {
            return false;
        }
        double aw = Math.min(this.getWidth(), Math.abs(this.getArcWidth())) / 2.0;
        double ah = Math.min(this.getHeight(), Math.abs(this.getArcHeight())) / 2.0;
        int x0class = this.classify(x, rrx0, rrx1, aw);
        int x1class = this.classify(x + w, rrx0, rrx1, aw);
        int y0class = this.classify(y, rry0, rry1, ah);
        int y1class = this.classify(y + h, rry0, rry1, ah);
        if (x0class == 2 || x1class == 2 || y0class == 2 || y1class == 2) {
            return true;
        }
        if (x0class < 2 && x1class > 2 || y0class < 2 && y1class > 2) {
            return true;
        }
        x = x1class == 1 ? (x = x + w - (rrx0 + aw)) : (x = x - (rrx1 - aw));
        y = y1class == 1 ? (y = y + h - (rry0 + ah)) : (y = y - (rry1 - ah));
        return (x /= aw) * x + (y /= ah) * y <= 1.0;
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        if (this.isEmpty() || w <= 0.0 || h <= 0.0) {
            return false;
        }
        return this.contains(x, y) && this.contains(x + w, y) && this.contains(x, y + h) && this.contains(x + w, y + h);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return new RoundRectIterator(this, at);
    }

    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(this.getX());
        bits += java.lang.Double.doubleToLongBits(this.getY()) * 37L;
        bits += java.lang.Double.doubleToLongBits(this.getWidth()) * 43L;
        bits += java.lang.Double.doubleToLongBits(this.getHeight()) * 47L;
        bits += java.lang.Double.doubleToLongBits(this.getArcWidth()) * 53L;
        return (int)(bits += java.lang.Double.doubleToLongBits(this.getArcHeight()) * 59L) ^ (int)(bits >> 32);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof RoundRectangle2D) {
            RoundRectangle2D rr2d = (RoundRectangle2D)obj;
            return this.getX() == rr2d.getX() && this.getY() == rr2d.getY() && this.getWidth() == rr2d.getWidth() && this.getHeight() == rr2d.getHeight() && this.getArcWidth() == rr2d.getArcWidth() && this.getArcHeight() == rr2d.getArcHeight();
        }
        return false;
    }

    public static class Double
    extends RoundRectangle2D
    implements Serializable {
        public double x;
        public double y;
        public double width;
        public double height;
        public double arcwidth;
        public double archeight;
        private static final long serialVersionUID = 1048939333485206117L;

        public Double() {
        }

        public Double(double x, double y, double w, double h, double arcw, double arch) {
            this.setRoundRect(x, y, w, h, arcw, arch);
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
        public double getArcWidth() {
            return this.arcwidth;
        }

        @Override
        public double getArcHeight() {
            return this.archeight;
        }

        @Override
        public boolean isEmpty() {
            return this.width <= 0.0 || this.height <= 0.0;
        }

        @Override
        public void setRoundRect(double x, double y, double w, double h, double arcw, double arch) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.arcwidth = arcw;
            this.archeight = arch;
        }

        @Override
        public void setRoundRect(RoundRectangle2D rr) {
            this.x = rr.getX();
            this.y = rr.getY();
            this.width = rr.getWidth();
            this.height = rr.getHeight();
            this.arcwidth = rr.getArcWidth();
            this.archeight = rr.getArcHeight();
        }

        @Override
        public Rectangle2D getBounds2D() {
            return new Rectangle2D.Double(this.x, this.y, this.width, this.height);
        }
    }

    public static class Float
    extends RoundRectangle2D
    implements Serializable {
        public float x;
        public float y;
        public float width;
        public float height;
        public float arcwidth;
        public float archeight;
        private static final long serialVersionUID = -3423150618393866922L;

        public Float() {
        }

        public Float(float x, float y, float w, float h, float arcw, float arch) {
            this.setRoundRect(x, y, w, h, arcw, arch);
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
        public double getArcWidth() {
            return this.arcwidth;
        }

        @Override
        public double getArcHeight() {
            return this.archeight;
        }

        @Override
        public boolean isEmpty() {
            return this.width <= 0.0f || this.height <= 0.0f;
        }

        public void setRoundRect(float x, float y, float w, float h, float arcw, float arch) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.arcwidth = arcw;
            this.archeight = arch;
        }

        @Override
        public void setRoundRect(double x, double y, double w, double h, double arcw, double arch) {
            this.x = (float)x;
            this.y = (float)y;
            this.width = (float)w;
            this.height = (float)h;
            this.arcwidth = (float)arcw;
            this.archeight = (float)arch;
        }

        @Override
        public void setRoundRect(RoundRectangle2D rr) {
            this.x = (float)rr.getX();
            this.y = (float)rr.getY();
            this.width = (float)rr.getWidth();
            this.height = (float)rr.getHeight();
            this.arcwidth = (float)rr.getArcWidth();
            this.archeight = (float)rr.getArcHeight();
        }

        @Override
        public Rectangle2D getBounds2D() {
            return new Rectangle2D.Float(this.x, this.y, this.width, this.height);
        }
    }
}

