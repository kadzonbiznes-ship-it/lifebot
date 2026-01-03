/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.beans.Transient;
import java.io.Serializable;

public class Rectangle
extends Rectangle2D
implements Shape,
Serializable {
    public int x;
    public int y;
    public int width;
    public int height;
    private static final long serialVersionUID = -4345857070255674764L;

    private static native void initIDs();

    public Rectangle() {
        this(0, 0, 0, 0);
    }

    public Rectangle(Rectangle r) {
        this(r.x, r.y, r.width, r.height);
    }

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle(int width, int height) {
        this(0, 0, width, height);
    }

    public Rectangle(Point p, Dimension d) {
        this(p.x, p.y, d.width, d.height);
    }

    public Rectangle(Point p) {
        this(p.x, p.y, 0, 0);
    }

    public Rectangle(Dimension d) {
        this(0, 0, d.width, d.height);
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
    @Transient
    public Rectangle getBounds() {
        return new Rectangle(this.x, this.y, this.width, this.height);
    }

    @Override
    public Rectangle2D getBounds2D() {
        return new Rectangle(this.x, this.y, this.width, this.height);
    }

    public void setBounds(Rectangle r) {
        this.setBounds(r.x, r.y, r.width, r.height);
    }

    public void setBounds(int x, int y, int width, int height) {
        this.reshape(x, y, width, height);
    }

    @Override
    public void setRect(double x, double y, double width, double height) {
        int newh;
        int newy;
        int neww;
        int newx;
        if (x > 4.294967294E9) {
            newx = Integer.MAX_VALUE;
            neww = -1;
        } else {
            newx = Rectangle.clip(x, false);
            if (width >= 0.0) {
                width += x - (double)newx;
            }
            neww = Rectangle.clip(width, width >= 0.0);
        }
        if (y > 4.294967294E9) {
            newy = Integer.MAX_VALUE;
            newh = -1;
        } else {
            newy = Rectangle.clip(y, false);
            if (height >= 0.0) {
                height += y - (double)newy;
            }
            newh = Rectangle.clip(height, height >= 0.0);
        }
        this.reshape(newx, newy, neww, newh);
    }

    private static int clip(double v, boolean doceil) {
        if (v <= -2.147483648E9) {
            return Integer.MIN_VALUE;
        }
        if (v >= 2.147483647E9) {
            return Integer.MAX_VALUE;
        }
        return (int)(doceil ? Math.ceil(v) : Math.floor(v));
    }

    @Deprecated
    public void reshape(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Point getLocation() {
        return new Point(this.x, this.y);
    }

    public void setLocation(Point p) {
        this.setLocation(p.x, p.y);
    }

    public void setLocation(int x, int y) {
        this.move(x, y);
    }

    @Deprecated
    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void translate(int dx, int dy) {
        int oldv = this.x;
        int newv = oldv + dx;
        if (dx < 0) {
            if (newv > oldv) {
                if (this.width >= 0) {
                    this.width += newv - Integer.MIN_VALUE;
                }
                newv = Integer.MIN_VALUE;
            }
        } else if (newv < oldv) {
            if (this.width >= 0) {
                this.width += newv - Integer.MAX_VALUE;
                if (this.width < 0) {
                    this.width = Integer.MAX_VALUE;
                }
            }
            newv = Integer.MAX_VALUE;
        }
        this.x = newv;
        oldv = this.y;
        newv = oldv + dy;
        if (dy < 0) {
            if (newv > oldv) {
                if (this.height >= 0) {
                    this.height += newv - Integer.MIN_VALUE;
                }
                newv = Integer.MIN_VALUE;
            }
        } else if (newv < oldv) {
            if (this.height >= 0) {
                this.height += newv - Integer.MAX_VALUE;
                if (this.height < 0) {
                    this.height = Integer.MAX_VALUE;
                }
            }
            newv = Integer.MAX_VALUE;
        }
        this.y = newv;
    }

    public Dimension getSize() {
        return new Dimension(this.width, this.height);
    }

    public void setSize(Dimension d) {
        this.setSize(d.width, d.height);
    }

    public void setSize(int width, int height) {
        this.resize(width, height);
    }

    @Deprecated
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean contains(Point p) {
        return this.contains(p.x, p.y);
    }

    public boolean contains(int x, int y) {
        return this.inside(x, y);
    }

    public boolean contains(Rectangle r) {
        return this.contains(r.x, r.y, r.width, r.height);
    }

    public boolean contains(int X, int Y, int W, int H) {
        int w = this.width;
        int h = this.height;
        if ((w | h | W | H) < 0) {
            return false;
        }
        int x = this.x;
        int y = this.y;
        if (X < x || Y < y) {
            return false;
        }
        w += x;
        if ((W += X) <= X ? w >= x || W > w : w >= x && W > w) {
            return false;
        }
        h += y;
        return !((H += Y) <= Y ? h >= y || H > h : h >= y && H > h);
    }

    @Deprecated
    public boolean inside(int X, int Y) {
        int w = this.width;
        int h = this.height;
        if ((w | h) < 0) {
            return false;
        }
        int x = this.x;
        int y = this.y;
        if (X < x || Y < y) {
            return false;
        }
        h += y;
        return !((w += x) >= x && w <= X || h >= y && h <= Y);
    }

    public boolean intersects(Rectangle r) {
        int tw = this.width;
        int th = this.height;
        int rw = r.width;
        int rh = r.height;
        if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
            return false;
        }
        int tx = this.x;
        int ty = this.y;
        int rx = r.x;
        int ry = r.y;
        rh += ry;
        tw += tx;
        th += ty;
        return !((rw += rx) >= rx && rw <= tx || rh >= ry && rh <= ty || tw >= tx && tw <= rx || th >= ty && th <= ry);
    }

    public Rectangle intersection(Rectangle r) {
        int tx1 = this.x;
        int ty1 = this.y;
        int rx1 = r.x;
        int ry1 = r.y;
        long tx2 = tx1;
        tx2 += (long)this.width;
        long ty2 = ty1;
        ty2 += (long)this.height;
        long rx2 = rx1;
        rx2 += (long)r.width;
        long ry2 = ry1;
        ry2 += (long)r.height;
        if (tx1 < rx1) {
            tx1 = rx1;
        }
        if (ty1 < ry1) {
            ty1 = ry1;
        }
        if (tx2 > rx2) {
            tx2 = rx2;
        }
        if (ty2 > ry2) {
            ty2 = ry2;
        }
        ty2 -= (long)ty1;
        if ((tx2 -= (long)tx1) < Integer.MIN_VALUE) {
            tx2 = Integer.MIN_VALUE;
        }
        if (ty2 < Integer.MIN_VALUE) {
            ty2 = Integer.MIN_VALUE;
        }
        return new Rectangle(tx1, ty1, (int)tx2, (int)ty2);
    }

    public Rectangle union(Rectangle r) {
        long tx2 = this.width;
        long ty2 = this.height;
        if ((tx2 | ty2) < 0L) {
            return new Rectangle(r);
        }
        long rx2 = r.width;
        long ry2 = r.height;
        if ((rx2 | ry2) < 0L) {
            return new Rectangle(this);
        }
        int tx1 = this.x;
        int ty1 = this.y;
        tx2 += (long)tx1;
        ty2 += (long)ty1;
        int rx1 = r.x;
        int ry1 = r.y;
        rx2 += (long)rx1;
        ry2 += (long)ry1;
        if (tx1 > rx1) {
            tx1 = rx1;
        }
        if (ty1 > ry1) {
            ty1 = ry1;
        }
        if (tx2 < rx2) {
            tx2 = rx2;
        }
        if (ty2 < ry2) {
            ty2 = ry2;
        }
        ty2 -= (long)ty1;
        if ((tx2 -= (long)tx1) > Integer.MAX_VALUE) {
            tx2 = Integer.MAX_VALUE;
        }
        if (ty2 > Integer.MAX_VALUE) {
            ty2 = Integer.MAX_VALUE;
        }
        return new Rectangle(tx1, ty1, (int)tx2, (int)ty2);
    }

    public void add(int newx, int newy) {
        if ((this.width | this.height) < 0) {
            this.x = newx;
            this.y = newy;
            this.height = 0;
            this.width = 0;
            return;
        }
        int x1 = this.x;
        int y1 = this.y;
        long x2 = this.width;
        long y2 = this.height;
        x2 += (long)x1;
        y2 += (long)y1;
        if (x1 > newx) {
            x1 = newx;
        }
        if (y1 > newy) {
            y1 = newy;
        }
        if (x2 < (long)newx) {
            x2 = newx;
        }
        if (y2 < (long)newy) {
            y2 = newy;
        }
        y2 -= (long)y1;
        if ((x2 -= (long)x1) > Integer.MAX_VALUE) {
            x2 = Integer.MAX_VALUE;
        }
        if (y2 > Integer.MAX_VALUE) {
            y2 = Integer.MAX_VALUE;
        }
        this.reshape(x1, y1, (int)x2, (int)y2);
    }

    public void add(Point pt) {
        this.add(pt.x, pt.y);
    }

    public void add(Rectangle r) {
        long ry2;
        long rx2;
        long tx2 = this.width;
        long ty2 = this.height;
        if ((tx2 | ty2) < 0L) {
            this.reshape(r.x, r.y, r.width, r.height);
        }
        if (((rx2 = (long)r.width) | (ry2 = (long)r.height)) < 0L) {
            return;
        }
        int tx1 = this.x;
        int ty1 = this.y;
        tx2 += (long)tx1;
        ty2 += (long)ty1;
        int rx1 = r.x;
        int ry1 = r.y;
        rx2 += (long)rx1;
        ry2 += (long)ry1;
        if (tx1 > rx1) {
            tx1 = rx1;
        }
        if (ty1 > ry1) {
            ty1 = ry1;
        }
        if (tx2 < rx2) {
            tx2 = rx2;
        }
        if (ty2 < ry2) {
            ty2 = ry2;
        }
        ty2 -= (long)ty1;
        if ((tx2 -= (long)tx1) > Integer.MAX_VALUE) {
            tx2 = Integer.MAX_VALUE;
        }
        if (ty2 > Integer.MAX_VALUE) {
            ty2 = Integer.MAX_VALUE;
        }
        this.reshape(tx1, ty1, (int)tx2, (int)ty2);
    }

    public void grow(int h, int v) {
        long x0 = this.x;
        long y0 = this.y;
        long x1 = this.width;
        long y1 = this.height;
        x1 += x0;
        y1 += y0;
        y0 -= (long)v;
        y1 += (long)v;
        if ((x1 += (long)h) < (x0 -= (long)h)) {
            if ((x1 -= x0) < Integer.MIN_VALUE) {
                x1 = Integer.MIN_VALUE;
            }
            if (x0 < Integer.MIN_VALUE) {
                x0 = Integer.MIN_VALUE;
            } else if (x0 > Integer.MAX_VALUE) {
                x0 = Integer.MAX_VALUE;
            }
        } else {
            if (x0 < Integer.MIN_VALUE) {
                x0 = Integer.MIN_VALUE;
            } else if (x0 > Integer.MAX_VALUE) {
                x0 = Integer.MAX_VALUE;
            }
            if ((x1 -= x0) < Integer.MIN_VALUE) {
                x1 = Integer.MIN_VALUE;
            } else if (x1 > Integer.MAX_VALUE) {
                x1 = Integer.MAX_VALUE;
            }
        }
        if (y1 < y0) {
            if ((y1 -= y0) < Integer.MIN_VALUE) {
                y1 = Integer.MIN_VALUE;
            }
            if (y0 < Integer.MIN_VALUE) {
                y0 = Integer.MIN_VALUE;
            } else if (y0 > Integer.MAX_VALUE) {
                y0 = Integer.MAX_VALUE;
            }
        } else {
            if (y0 < Integer.MIN_VALUE) {
                y0 = Integer.MIN_VALUE;
            } else if (y0 > Integer.MAX_VALUE) {
                y0 = Integer.MAX_VALUE;
            }
            if ((y1 -= y0) < Integer.MIN_VALUE) {
                y1 = Integer.MIN_VALUE;
            } else if (y1 > Integer.MAX_VALUE) {
                y1 = Integer.MAX_VALUE;
            }
        }
        this.reshape((int)x0, (int)y0, (int)x1, (int)y1);
    }

    @Override
    public boolean isEmpty() {
        return this.width <= 0 || this.height <= 0;
    }

    @Override
    public int outcode(double x, double y) {
        int out = 0;
        if (this.width <= 0) {
            out |= 5;
        } else if (x < (double)this.x) {
            out |= 1;
        } else if (x > (double)this.x + (double)this.width) {
            out |= 4;
        }
        if (this.height <= 0) {
            out |= 0xA;
        } else if (y < (double)this.y) {
            out |= 2;
        } else if (y > (double)this.y + (double)this.height) {
            out |= 8;
        }
        return out;
    }

    @Override
    public Rectangle2D createIntersection(Rectangle2D r) {
        if (r instanceof Rectangle) {
            return this.intersection((Rectangle)r);
        }
        Rectangle2D.Double dest = new Rectangle2D.Double();
        Rectangle2D.intersect(this, r, dest);
        return dest;
    }

    @Override
    public Rectangle2D createUnion(Rectangle2D r) {
        if (r instanceof Rectangle) {
            return this.union((Rectangle)r);
        }
        Rectangle2D.Double dest = new Rectangle2D.Double();
        Rectangle2D.union(this, r, dest);
        return dest;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rectangle) {
            Rectangle r = (Rectangle)obj;
            return this.x == r.x && this.y == r.y && this.width == r.width && this.height == r.height;
        }
        return super.equals(obj);
    }

    public String toString() {
        return this.getClass().getName() + "[x=" + this.x + ",y=" + this.y + ",width=" + this.width + ",height=" + this.height + "]";
    }

    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            Rectangle.initIDs();
        }
    }
}

