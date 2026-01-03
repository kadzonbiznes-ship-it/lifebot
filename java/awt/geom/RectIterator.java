/*
 * Decompiled with CFR 0.152.
 */
package java.awt.geom;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.NoSuchElementException;

class RectIterator
implements PathIterator {
    double x;
    double y;
    double w;
    double h;
    AffineTransform affine;
    int index;

    RectIterator(Rectangle2D r, AffineTransform at) {
        this.x = r.getX();
        this.y = r.getY();
        this.w = r.getWidth();
        this.h = r.getHeight();
        this.affine = at;
        if (this.w < 0.0 || this.h < 0.0) {
            this.index = 6;
        }
    }

    @Override
    public int getWindingRule() {
        return 1;
    }

    @Override
    public boolean isDone() {
        return this.index > 5;
    }

    @Override
    public void next() {
        ++this.index;
    }

    @Override
    public int currentSegment(float[] coords) {
        if (this.isDone()) {
            throw new NoSuchElementException("rect iterator out of bounds");
        }
        if (this.index == 5) {
            return 4;
        }
        coords[0] = (float)this.x;
        coords[1] = (float)this.y;
        if (this.index == 1 || this.index == 2) {
            coords[0] = coords[0] + (float)this.w;
        }
        if (this.index == 2 || this.index == 3) {
            coords[1] = coords[1] + (float)this.h;
        }
        if (this.affine != null) {
            this.affine.transform(coords, 0, coords, 0, 1);
        }
        return this.index == 0 ? 0 : 1;
    }

    @Override
    public int currentSegment(double[] coords) {
        if (this.isDone()) {
            throw new NoSuchElementException("rect iterator out of bounds");
        }
        if (this.index == 5) {
            return 4;
        }
        coords[0] = this.x;
        coords[1] = this.y;
        if (this.index == 1 || this.index == 2) {
            coords[0] = coords[0] + this.w;
        }
        if (this.index == 2 || this.index == 3) {
            coords[1] = coords[1] + this.h;
        }
        if (this.affine != null) {
            this.affine.transform(coords, 0, coords, 0, 1);
        }
        return this.index == 0 ? 0 : 1;
    }
}

