/*
 * Decompiled with CFR 0.152.
 */
package java.awt.geom;

import java.io.Serializable;

public abstract class Point2D
implements Cloneable {
    protected Point2D() {
    }

    public abstract double getX();

    public abstract double getY();

    public abstract void setLocation(double var1, double var3);

    public void setLocation(Point2D p) {
        this.setLocation(p.getX(), p.getY());
    }

    public static double distanceSq(double x1, double y1, double x2, double y2) {
        return (x1 -= x2) * x1 + (y1 -= y2) * y1;
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 -= x2) * x1 + (y1 -= y2) * y1);
    }

    public double distanceSq(double px, double py) {
        return (px -= this.getX()) * px + (py -= this.getY()) * py;
    }

    public double distanceSq(Point2D pt) {
        double px = pt.getX() - this.getX();
        double py = pt.getY() - this.getY();
        return px * px + py * py;
    }

    public double distance(double px, double py) {
        return Math.sqrt((px -= this.getX()) * px + (py -= this.getY()) * py);
    }

    public double distance(Point2D pt) {
        double px = pt.getX() - this.getX();
        double py = pt.getY() - this.getY();
        return Math.sqrt(px * px + py * py);
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(this.getX());
        return (int)(bits ^= java.lang.Double.doubleToLongBits(this.getY()) * 31L) ^ (int)(bits >> 32);
    }

    public boolean equals(Object obj) {
        if (obj instanceof Point2D) {
            Point2D p2d = (Point2D)obj;
            return this.getX() == p2d.getX() && this.getY() == p2d.getY();
        }
        return super.equals(obj);
    }

    public static class Double
    extends Point2D
    implements Serializable {
        public double x;
        public double y;
        private static final long serialVersionUID = 6150783262733311327L;

        public Double() {
        }

        public Double(double x, double y) {
            this.x = x;
            this.y = y;
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
        public void setLocation(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public String toString() {
            return "Point2D.Double[" + this.x + ", " + this.y + "]";
        }
    }

    public static class Float
    extends Point2D
    implements Serializable {
        public float x;
        public float y;
        private static final long serialVersionUID = -2870572449815403710L;

        public Float() {
        }

        public Float(float x, float y) {
            this.x = x;
            this.y = y;
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
        public void setLocation(double x, double y) {
            this.x = (float)x;
            this.y = (float)y;
        }

        public void setLocation(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public String toString() {
            return "Point2D.Float[" + this.x + ", " + this.y + "]";
        }
    }
}

