/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.geom.Point2D;
import java.beans.Transient;
import java.io.Serializable;

public class Point
extends Point2D
implements Serializable {
    public int x;
    public int y;
    private static final long serialVersionUID = -5276940640259749850L;

    public Point() {
        this(0, 0);
    }

    public Point(Point p) {
        this(p.x, p.y);
    }

    public Point(int x, int y) {
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

    @Transient
    public Point getLocation() {
        return new Point(this.x, this.y);
    }

    public void setLocation(Point p) {
        this.setLocation(p.x, p.y);
    }

    public void setLocation(int x, int y) {
        this.move(x, y);
    }

    @Override
    public void setLocation(double x, double y) {
        this.x = (int)Math.floor(x + 0.5);
        this.y = (int)Math.floor(y + 0.5);
    }

    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void translate(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point pt = (Point)obj;
            return this.x == pt.x && this.y == pt.y;
        }
        return super.equals(obj);
    }

    public String toString() {
        return this.getClass().getName() + "[x=" + this.x + ",y=" + this.y + "]";
    }
}

