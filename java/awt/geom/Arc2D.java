/*
 * Decompiled with CFR 0.152.
 */
package java.awt.geom;

import java.awt.geom.AffineTransform;
import java.awt.geom.ArcIterator;
import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class Arc2D
extends RectangularShape {
    public static final int OPEN = 0;
    public static final int CHORD = 1;
    public static final int PIE = 2;
    private int type;

    protected Arc2D() {
        this(0);
    }

    protected Arc2D(int type) {
        this.setArcType(type);
    }

    public abstract double getAngleStart();

    public abstract double getAngleExtent();

    public int getArcType() {
        return this.type;
    }

    public Point2D getStartPoint() {
        double angle = Math.toRadians(-this.getAngleStart());
        double x = this.getX() + (Math.cos(angle) * 0.5 + 0.5) * this.getWidth();
        double y = this.getY() + (Math.sin(angle) * 0.5 + 0.5) * this.getHeight();
        return new Point2D.Double(x, y);
    }

    public Point2D getEndPoint() {
        double angle = Math.toRadians(-this.getAngleStart() - this.getAngleExtent());
        double x = this.getX() + (Math.cos(angle) * 0.5 + 0.5) * this.getWidth();
        double y = this.getY() + (Math.sin(angle) * 0.5 + 0.5) * this.getHeight();
        return new Point2D.Double(x, y);
    }

    public abstract void setArc(double var1, double var3, double var5, double var7, double var9, double var11, int var13);

    public void setArc(Point2D loc, Dimension2D size, double angSt, double angExt, int closure) {
        this.setArc(loc.getX(), loc.getY(), size.getWidth(), size.getHeight(), angSt, angExt, closure);
    }

    public void setArc(Rectangle2D rect, double angSt, double angExt, int closure) {
        this.setArc(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), angSt, angExt, closure);
    }

    public void setArc(Arc2D a) {
        this.setArc(a.getX(), a.getY(), a.getWidth(), a.getHeight(), a.getAngleStart(), a.getAngleExtent(), a.type);
    }

    public void setArcByCenter(double x, double y, double radius, double angSt, double angExt, int closure) {
        this.setArc(x - radius, y - radius, radius * 2.0, radius * 2.0, angSt, angExt, closure);
    }

    public void setArcByTangent(Point2D p1, Point2D p2, Point2D p3, double radius) {
        double ang1 = Math.atan2(p1.getY() - p2.getY(), p1.getX() - p2.getX());
        double ang2 = Math.atan2(p3.getY() - p2.getY(), p3.getX() - p2.getX());
        double diff = ang2 - ang1;
        if (diff > Math.PI) {
            ang2 -= Math.PI * 2;
        } else if (diff < -Math.PI) {
            ang2 += Math.PI * 2;
        }
        double bisect = (ang1 + ang2) / 2.0;
        double theta = Math.abs(ang2 - bisect);
        double dist = radius / Math.sin(theta);
        double x = p2.getX() + dist * Math.cos(bisect);
        double y = p2.getY() + dist * Math.sin(bisect);
        if (ang1 < ang2) {
            ang1 -= 1.5707963267948966;
            ang2 += 1.5707963267948966;
        } else {
            ang1 += 1.5707963267948966;
            ang2 -= 1.5707963267948966;
        }
        ang1 = Math.toDegrees(-ang1);
        ang2 = Math.toDegrees(-ang2);
        diff = ang2 - ang1;
        diff = diff < 0.0 ? (diff += 360.0) : (diff -= 360.0);
        this.setArcByCenter(x, y, radius, ang1, diff, this.type);
    }

    public abstract void setAngleStart(double var1);

    public abstract void setAngleExtent(double var1);

    public void setAngleStart(Point2D p) {
        double dx = this.getHeight() * (p.getX() - this.getCenterX());
        double dy = this.getWidth() * (p.getY() - this.getCenterY());
        this.setAngleStart(-Math.toDegrees(Math.atan2(dy, dx)));
    }

    public void setAngles(double x1, double y1, double x2, double y2) {
        double x = this.getCenterX();
        double y = this.getCenterY();
        double w = this.getWidth();
        double h = this.getHeight();
        double ang1 = Math.atan2(w * (y - y1), h * (x1 - x));
        double ang2 = Math.atan2(w * (y - y2), h * (x2 - x));
        if ((ang2 -= ang1) <= 0.0) {
            ang2 += Math.PI * 2;
        }
        this.setAngleStart(Math.toDegrees(ang1));
        this.setAngleExtent(Math.toDegrees(ang2));
    }

    public void setAngles(Point2D p1, Point2D p2) {
        this.setAngles(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public void setArcType(int type) {
        if (type < 0 || type > 2) {
            throw new IllegalArgumentException("invalid type for Arc: " + type);
        }
        this.type = type;
    }

    @Override
    public void setFrame(double x, double y, double w, double h) {
        this.setArc(x, y, w, h, this.getAngleStart(), this.getAngleExtent(), this.type);
    }

    @Override
    public Rectangle2D getBounds2D() {
        double x1;
        double y1;
        double x2;
        double y2;
        if (this.isEmpty()) {
            return this.makeBounds(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
        if (this.getArcType() == 2) {
            y2 = 0.0;
            x2 = 0.0;
            y1 = 0.0;
            x1 = 0.0;
        } else {
            y1 = 1.0;
            x1 = 1.0;
            y2 = -1.0;
            x2 = -1.0;
        }
        double angle = 0.0;
        for (int i = 0; i < 6; ++i) {
            if (i < 4) {
                if (!this.containsAngle(angle += 90.0)) {
                    continue;
                }
            } else {
                angle = i == 4 ? this.getAngleStart() : (angle += this.getAngleExtent());
            }
            double rads = Math.toRadians(-angle);
            double xe = Math.cos(rads);
            double ye = Math.sin(rads);
            x1 = Math.min(x1, xe);
            y1 = Math.min(y1, ye);
            x2 = Math.max(x2, xe);
            y2 = Math.max(y2, ye);
        }
        double w = this.getWidth();
        double h = this.getHeight();
        x2 = (x2 - x1) * 0.5 * w;
        y2 = (y2 - y1) * 0.5 * h;
        x1 = this.getX() + (x1 * 0.5 + 0.5) * w;
        y1 = this.getY() + (y1 * 0.5 + 0.5) * h;
        return this.makeBounds(x1, y1, x2, y2);
    }

    protected abstract Rectangle2D makeBounds(double var1, double var3, double var5, double var7);

    static double normalizeDegrees(double angle) {
        if (angle > 180.0) {
            if (angle <= 540.0) {
                angle -= 360.0;
            } else if ((angle = Math.IEEEremainder(angle, 360.0)) == -180.0) {
                angle = 180.0;
            }
        } else if (angle <= -180.0) {
            if (angle > -540.0) {
                angle += 360.0;
            } else if ((angle = Math.IEEEremainder(angle, 360.0)) == -180.0) {
                angle = 180.0;
            }
        }
        return angle;
    }

    public boolean containsAngle(double angle) {
        boolean backwards;
        double angExt = this.getAngleExtent();
        boolean bl = backwards = angExt < 0.0;
        if (backwards) {
            angExt = -angExt;
        }
        if (angExt >= 360.0) {
            return true;
        }
        angle = Arc2D.normalizeDegrees(angle) - Arc2D.normalizeDegrees(this.getAngleStart());
        if (backwards) {
            angle = -angle;
        }
        if (angle < 0.0) {
            angle += 360.0;
        }
        return angle >= 0.0 && angle < angExt;
    }

    @Override
    public boolean contains(double x, double y) {
        double y2;
        double x2;
        double y1;
        boolean inside;
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
        double distSq = normx * normx + normy * normy;
        if (distSq >= 0.25) {
            return false;
        }
        double angExt = Math.abs(this.getAngleExtent());
        if (angExt >= 360.0) {
            return true;
        }
        boolean inarc = this.containsAngle(-Math.toDegrees(Math.atan2(normy, normx)));
        if (this.type == 2) {
            return inarc;
        }
        if (inarc) {
            if (angExt >= 180.0) {
                return true;
            }
        } else if (angExt <= 180.0) {
            return false;
        }
        double angle = Math.toRadians(-this.getAngleStart());
        double x1 = Math.cos(angle);
        boolean bl = inside = Line2D.relativeCCW(x1, y1 = Math.sin(angle), x2 = Math.cos(angle += Math.toRadians(-this.getAngleExtent())), y2 = Math.sin(angle), 2.0 * normx, 2.0 * normy) * Line2D.relativeCCW(x1, y1, x2, y2, 0.0, 0.0) >= 0;
        return inarc ? !inside : inside;
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        double aw = this.getWidth();
        double ah = this.getHeight();
        if (w <= 0.0 || h <= 0.0 || aw <= 0.0 || ah <= 0.0) {
            return false;
        }
        double ext = this.getAngleExtent();
        if (ext == 0.0) {
            return false;
        }
        double ax = this.getX();
        double ay = this.getY();
        double axw = ax + aw;
        double ayh = ay + ah;
        double xw = x + w;
        double yh = y + h;
        if (x >= axw || y >= ayh || xw <= ax || yh <= ay) {
            return false;
        }
        double axc = this.getCenterX();
        double ayc = this.getCenterY();
        Point2D sp = this.getStartPoint();
        Point2D ep = this.getEndPoint();
        double sx = sp.getX();
        double sy = sp.getY();
        double ex = ep.getX();
        double ey = ep.getY();
        if (ayc >= y && ayc <= yh && (sx < xw && ex < xw && axc < xw && axw > x && this.containsAngle(0.0) || sx > x && ex > x && axc > x && ax < xw && this.containsAngle(180.0))) {
            return true;
        }
        if (axc >= x && axc <= xw && (sy > y && ey > y && ayc > y && ay < yh && this.containsAngle(90.0) || sy < yh && ey < yh && ayc < yh && ayh > y && this.containsAngle(270.0))) {
            return true;
        }
        Rectangle2D.Double rect = new Rectangle2D.Double(x, y, w, h);
        if (this.type == 2 || Math.abs(ext) > 180.0 ? rect.intersectsLine(axc, ayc, sx, sy) || rect.intersectsLine(axc, ayc, ex, ey) : rect.intersectsLine(sx, sy, ex, ey)) {
            return true;
        }
        return this.contains(x, y) || this.contains(x + w, y) || this.contains(x, y + h) || this.contains(x + w, y + h);
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return this.contains(x, y, w, h, null);
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return this.contains(r.getX(), r.getY(), r.getWidth(), r.getHeight(), r);
    }

    private boolean contains(double x, double y, double w, double h, Rectangle2D origrect) {
        double ye;
        double angle;
        double xe;
        double yc;
        if (!(this.contains(x, y) && this.contains(x + w, y) && this.contains(x, y + h) && this.contains(x + w, y + h))) {
            return false;
        }
        if (this.type != 2 || Math.abs(this.getAngleExtent()) <= 180.0) {
            return true;
        }
        if (origrect == null) {
            origrect = new Rectangle2D.Double(x, y, w, h);
        }
        double halfW = this.getWidth() / 2.0;
        double halfH = this.getHeight() / 2.0;
        double xc = this.getX() + halfW;
        if (origrect.intersectsLine(xc, yc = this.getY() + halfH, xe = xc + halfW * Math.cos(angle = Math.toRadians(-this.getAngleStart())), ye = yc + halfH * Math.sin(angle))) {
            return false;
        }
        xe = xc + halfW * Math.cos(angle += Math.toRadians(-this.getAngleExtent()));
        return !origrect.intersectsLine(xc, yc, xe, ye = yc + halfH * Math.sin(angle));
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return new ArcIterator(this, at);
    }

    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(this.getX());
        bits += java.lang.Double.doubleToLongBits(this.getY()) * 37L;
        bits += java.lang.Double.doubleToLongBits(this.getWidth()) * 43L;
        bits += java.lang.Double.doubleToLongBits(this.getHeight()) * 47L;
        bits += java.lang.Double.doubleToLongBits(this.getAngleStart()) * 53L;
        bits += java.lang.Double.doubleToLongBits(this.getAngleExtent()) * 59L;
        return (int)(bits += (long)(this.getArcType() * 61)) ^ (int)(bits >> 32);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Arc2D) {
            Arc2D a2d = (Arc2D)obj;
            return this.getX() == a2d.getX() && this.getY() == a2d.getY() && this.getWidth() == a2d.getWidth() && this.getHeight() == a2d.getHeight() && this.getAngleStart() == a2d.getAngleStart() && this.getAngleExtent() == a2d.getAngleExtent() && this.getArcType() == a2d.getArcType();
        }
        return false;
    }

    public static class Double
    extends Arc2D
    implements Serializable {
        public double x;
        public double y;
        public double width;
        public double height;
        public double start;
        public double extent;
        private static final long serialVersionUID = 728264085846882001L;

        public Double() {
            super(0);
        }

        public Double(int type) {
            super(type);
        }

        public Double(double x, double y, double w, double h, double start, double extent, int type) {
            super(type);
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.start = start;
            this.extent = extent;
        }

        public Double(Rectangle2D ellipseBounds, double start, double extent, int type) {
            super(type);
            this.x = ellipseBounds.getX();
            this.y = ellipseBounds.getY();
            this.width = ellipseBounds.getWidth();
            this.height = ellipseBounds.getHeight();
            this.start = start;
            this.extent = extent;
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
        public double getAngleStart() {
            return this.start;
        }

        @Override
        public double getAngleExtent() {
            return this.extent;
        }

        @Override
        public boolean isEmpty() {
            return this.width <= 0.0 || this.height <= 0.0;
        }

        @Override
        public void setArc(double x, double y, double w, double h, double angSt, double angExt, int closure) {
            this.setArcType(closure);
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.start = angSt;
            this.extent = angExt;
        }

        @Override
        public void setAngleStart(double angSt) {
            this.start = angSt;
        }

        @Override
        public void setAngleExtent(double angExt) {
            this.extent = angExt;
        }

        @Override
        protected Rectangle2D makeBounds(double x, double y, double w, double h) {
            return new Rectangle2D.Double(x, y, w, h);
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            s.writeByte(this.getArcType());
        }

        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            s.defaultReadObject();
            try {
                this.setArcType(s.readByte());
            }
            catch (IllegalArgumentException iae) {
                throw new InvalidObjectException(iae.getMessage());
            }
        }
    }

    public static class Float
    extends Arc2D
    implements Serializable {
        public float x;
        public float y;
        public float width;
        public float height;
        public float start;
        public float extent;
        private static final long serialVersionUID = 9130893014586380278L;

        public Float() {
            super(0);
        }

        public Float(int type) {
            super(type);
        }

        public Float(float x, float y, float w, float h, float start, float extent, int type) {
            super(type);
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.start = start;
            this.extent = extent;
        }

        public Float(Rectangle2D ellipseBounds, float start, float extent, int type) {
            super(type);
            this.x = (float)ellipseBounds.getX();
            this.y = (float)ellipseBounds.getY();
            this.width = (float)ellipseBounds.getWidth();
            this.height = (float)ellipseBounds.getHeight();
            this.start = start;
            this.extent = extent;
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
        public double getAngleStart() {
            return this.start;
        }

        @Override
        public double getAngleExtent() {
            return this.extent;
        }

        @Override
        public boolean isEmpty() {
            return (double)this.width <= 0.0 || (double)this.height <= 0.0;
        }

        @Override
        public void setArc(double x, double y, double w, double h, double angSt, double angExt, int closure) {
            this.setArcType(closure);
            this.x = (float)x;
            this.y = (float)y;
            this.width = (float)w;
            this.height = (float)h;
            this.start = (float)angSt;
            this.extent = (float)angExt;
        }

        @Override
        public void setAngleStart(double angSt) {
            this.start = (float)angSt;
        }

        @Override
        public void setAngleExtent(double angExt) {
            this.extent = (float)angExt;
        }

        @Override
        protected Rectangle2D makeBounds(double x, double y, double w, double h) {
            return new Rectangle2D.Float((float)x, (float)y, (float)w, (float)h);
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            s.writeByte(this.getArcType());
        }

        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            s.defaultReadObject();
            try {
                this.setArcType(s.readByte());
            }
            catch (IllegalArgumentException iae) {
                throw new InvalidObjectException(iae.getMessage());
            }
        }
    }
}

