/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Arrays;
import sun.awt.geom.Crossings;

public class Polygon
implements Shape,
Serializable {
    public int npoints;
    public int[] xpoints;
    public int[] ypoints;
    protected Rectangle bounds;
    private static final long serialVersionUID = -6460061437900069969L;
    private static final int MIN_LENGTH = 4;

    public Polygon() {
        this.xpoints = new int[4];
        this.ypoints = new int[4];
    }

    public Polygon(int[] xpoints, int[] ypoints, int npoints) {
        if (npoints > xpoints.length || npoints > ypoints.length) {
            throw new IndexOutOfBoundsException("npoints > xpoints.length || npoints > ypoints.length");
        }
        if (npoints < 0) {
            throw new NegativeArraySizeException("npoints < 0");
        }
        this.npoints = npoints;
        this.xpoints = Arrays.copyOf(xpoints, npoints);
        this.ypoints = Arrays.copyOf(ypoints, npoints);
    }

    public void reset() {
        this.npoints = 0;
        this.bounds = null;
    }

    public void invalidate() {
        this.bounds = null;
    }

    public void translate(int deltaX, int deltaY) {
        int i = 0;
        while (i < this.npoints) {
            int n = i;
            this.xpoints[n] = this.xpoints[n] + deltaX;
            int n2 = i++;
            this.ypoints[n2] = this.ypoints[n2] + deltaY;
        }
        if (this.bounds != null) {
            this.bounds.translate(deltaX, deltaY);
        }
    }

    void calculateBounds(int[] xpoints, int[] ypoints, int npoints) {
        int boundsMinX = Integer.MAX_VALUE;
        int boundsMinY = Integer.MAX_VALUE;
        int boundsMaxX = Integer.MIN_VALUE;
        int boundsMaxY = Integer.MIN_VALUE;
        for (int i = 0; i < npoints; ++i) {
            int x = xpoints[i];
            boundsMinX = Math.min(boundsMinX, x);
            boundsMaxX = Math.max(boundsMaxX, x);
            int y = ypoints[i];
            boundsMinY = Math.min(boundsMinY, y);
            boundsMaxY = Math.max(boundsMaxY, y);
        }
        this.bounds = new Rectangle(boundsMinX, boundsMinY, boundsMaxX - boundsMinX, boundsMaxY - boundsMinY);
    }

    void updateBounds(int x, int y) {
        if (x < this.bounds.x) {
            this.bounds.width += this.bounds.x - x;
            this.bounds.x = x;
        } else {
            this.bounds.width = Math.max(this.bounds.width, x - this.bounds.x);
        }
        if (y < this.bounds.y) {
            this.bounds.height += this.bounds.y - y;
            this.bounds.y = y;
        } else {
            this.bounds.height = Math.max(this.bounds.height, y - this.bounds.y);
        }
    }

    public void addPoint(int x, int y) {
        if (this.npoints >= this.xpoints.length || this.npoints >= this.ypoints.length) {
            int newLength = this.npoints * 2;
            if (newLength < 4) {
                newLength = 4;
            } else if ((newLength & newLength - 1) != 0) {
                newLength = Integer.highestOneBit(newLength);
            }
            this.xpoints = Arrays.copyOf(this.xpoints, newLength);
            this.ypoints = Arrays.copyOf(this.ypoints, newLength);
        }
        this.xpoints[this.npoints] = x;
        this.ypoints[this.npoints] = y;
        ++this.npoints;
        if (this.bounds != null) {
            this.updateBounds(x, y);
        }
    }

    @Override
    public Rectangle getBounds() {
        return this.getBoundingBox();
    }

    @Deprecated
    public Rectangle getBoundingBox() {
        if (this.npoints == 0) {
            return new Rectangle();
        }
        if (this.bounds == null) {
            this.calculateBounds(this.xpoints, this.ypoints, this.npoints);
        }
        return this.bounds.getBounds();
    }

    public boolean contains(Point p) {
        return this.contains(p.x, p.y);
    }

    public boolean contains(int x, int y) {
        return this.contains((double)x, (double)y);
    }

    @Deprecated
    public boolean inside(int x, int y) {
        return this.contains((double)x, (double)y);
    }

    @Override
    public Rectangle2D getBounds2D() {
        return this.getBounds();
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public boolean contains(double x, double y) {
        if (this.npoints <= 2 || !this.getBoundingBox().contains(x, y)) {
            return false;
        }
        hits = 0;
        lastx = this.xpoints[this.npoints - 1];
        lasty = this.ypoints[this.npoints - 1];
        for (i = 0; i < this.npoints; ++i) {
            block8: {
                block11: {
                    block12: {
                        block10: {
                            block9: {
                                curx = this.xpoints[i];
                                cury = this.ypoints[i];
                                if (cury == lasty) break block8;
                                if (curx >= lastx) break block9;
                                if (x >= (double)lastx) break block8;
                                leftx = curx;
                                break block10;
                            }
                            if (x >= (double)curx) break block8;
                            leftx = lastx;
                        }
                        if (cury >= lasty) break block11;
                        if (y < (double)cury || y >= (double)lasty) break block8;
                        if (!(x < (double)leftx)) break block12;
                        ++hits;
                        break block8;
                    }
                    test1 = x - (double)curx;
                    test2 = y - (double)cury;
                    ** GOTO lbl34
                }
                if (y < (double)lasty || y >= (double)cury) break block8;
                if (x < (double)leftx) {
                    ++hits;
                } else {
                    test1 = x - (double)lastx;
                    test2 = y - (double)lasty;
lbl34:
                    // 2 sources

                    if (test1 < test2 / (double)(lasty - cury) * (double)(lastx - curx)) {
                        ++hits;
                    }
                }
            }
            lastx = curx;
            lasty = cury;
        }
        return (hits & true) != false;
    }

    private Crossings getCrossings(double xlo, double ylo, double xhi, double yhi) {
        Crossings.EvenOdd cross = new Crossings.EvenOdd(xlo, ylo, xhi, yhi);
        int lastx = this.xpoints[this.npoints - 1];
        int lasty = this.ypoints[this.npoints - 1];
        for (int i = 0; i < this.npoints; ++i) {
            int curx = this.xpoints[i];
            int cury = this.ypoints[i];
            if (cross.accumulateLine(lastx, lasty, curx, cury)) {
                return null;
            }
            lastx = curx;
            lasty = cury;
        }
        return cross;
    }

    @Override
    public boolean contains(Point2D p) {
        return this.contains(p.getX(), p.getY());
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        if (this.npoints <= 0 || !this.getBoundingBox().intersects(x, y, w, h)) {
            return false;
        }
        Crossings cross = this.getCrossings(x, y, x + w, y + h);
        return cross == null || !cross.isEmpty();
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return this.intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        if (this.npoints <= 0 || !this.getBoundingBox().intersects(x, y, w, h)) {
            return false;
        }
        Crossings cross = this.getCrossings(x, y, x + w, y + h);
        return cross != null && cross.covers(y, y + h);
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return this.contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return new PolygonPathIterator(this, at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return this.getPathIterator(at);
    }

    static class PolygonPathIterator
    implements PathIterator {
        Polygon poly;
        AffineTransform transform;
        int index;

        public PolygonPathIterator(Polygon pg, AffineTransform at) {
            this.poly = pg;
            this.transform = at;
            if (pg.npoints == 0) {
                this.index = 1;
            }
        }

        @Override
        public int getWindingRule() {
            return 0;
        }

        @Override
        public boolean isDone() {
            return this.index > this.poly.npoints;
        }

        @Override
        public void next() {
            ++this.index;
        }

        @Override
        public int currentSegment(float[] coords) {
            if (this.index >= this.poly.npoints) {
                return 4;
            }
            coords[0] = this.poly.xpoints[this.index];
            coords[1] = this.poly.ypoints[this.index];
            if (this.transform != null) {
                this.transform.transform(coords, 0, coords, 0, 1);
            }
            return this.index == 0 ? 0 : 1;
        }

        @Override
        public int currentSegment(double[] coords) {
            if (this.index >= this.poly.npoints) {
                return 4;
            }
            coords[0] = this.poly.xpoints[this.index];
            coords[1] = this.poly.ypoints[this.index];
            if (this.transform != null) {
                this.transform.transform(coords, 0, coords, 0, 1);
            }
            return this.index == 0 ? 0 : 1;
        }
    }
}

