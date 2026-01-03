/*
 * Decompiled with CFR 0.152.
 */
package java.awt.geom;

import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.PathIterator;
import java.util.NoSuchElementException;

class ArcIterator
implements PathIterator {
    double x;
    double y;
    double w;
    double h;
    double angStRad;
    double increment;
    double cv;
    AffineTransform affine;
    int index;
    int arcSegs;
    int lineSegs;

    ArcIterator(Arc2D a, AffineTransform at) {
        this.w = a.getWidth() / 2.0;
        this.h = a.getHeight() / 2.0;
        this.x = a.getX() + this.w;
        this.y = a.getY() + this.h;
        this.angStRad = -Math.toRadians(a.getAngleStart());
        this.affine = at;
        double ext = -a.getAngleExtent();
        if (ext >= 360.0 || ext <= -360.0) {
            this.arcSegs = 4;
            this.increment = 1.5707963267948966;
            this.cv = 0.5522847498307933;
            if (ext < 0.0) {
                this.increment = -this.increment;
                this.cv = -this.cv;
            }
        } else {
            this.arcSegs = (int)Math.ceil(Math.abs(ext) / 90.0);
            this.increment = Math.toRadians(ext / (double)this.arcSegs);
            this.cv = ArcIterator.btan(this.increment);
            if (this.cv == 0.0) {
                this.arcSegs = 0;
            }
        }
        switch (a.getArcType()) {
            case 0: {
                this.lineSegs = 0;
                break;
            }
            case 1: {
                this.lineSegs = 1;
                break;
            }
            case 2: {
                this.lineSegs = 2;
            }
        }
        if (this.w < 0.0 || this.h < 0.0) {
            this.lineSegs = -1;
            this.arcSegs = -1;
        }
    }

    @Override
    public int getWindingRule() {
        return 1;
    }

    @Override
    public boolean isDone() {
        return this.index > this.arcSegs + this.lineSegs;
    }

    @Override
    public void next() {
        ++this.index;
    }

    private static double btan(double increment) {
        return 1.3333333333333333 * Math.sin(increment /= 2.0) / (1.0 + Math.cos(increment));
    }

    @Override
    public int currentSegment(float[] coords) {
        if (this.isDone()) {
            throw new NoSuchElementException("arc iterator out of bounds");
        }
        double angle = this.angStRad;
        if (this.index == 0) {
            coords[0] = (float)(this.x + Math.cos(angle) * this.w);
            coords[1] = (float)(this.y + Math.sin(angle) * this.h);
            if (this.affine != null) {
                this.affine.transform(coords, 0, coords, 0, 1);
            }
            return 0;
        }
        if (this.index > this.arcSegs) {
            if (this.index == this.arcSegs + this.lineSegs) {
                return 4;
            }
            coords[0] = (float)this.x;
            coords[1] = (float)this.y;
            if (this.affine != null) {
                this.affine.transform(coords, 0, coords, 0, 1);
            }
            return 1;
        }
        double relx = Math.cos(angle += this.increment * (double)(this.index - 1));
        double rely = Math.sin(angle);
        coords[0] = (float)(this.x + (relx - this.cv * rely) * this.w);
        coords[1] = (float)(this.y + (rely + this.cv * relx) * this.h);
        relx = Math.cos(angle += this.increment);
        rely = Math.sin(angle);
        coords[2] = (float)(this.x + (relx + this.cv * rely) * this.w);
        coords[3] = (float)(this.y + (rely - this.cv * relx) * this.h);
        coords[4] = (float)(this.x + relx * this.w);
        coords[5] = (float)(this.y + rely * this.h);
        if (this.affine != null) {
            this.affine.transform(coords, 0, coords, 0, 3);
        }
        return 3;
    }

    @Override
    public int currentSegment(double[] coords) {
        if (this.isDone()) {
            throw new NoSuchElementException("arc iterator out of bounds");
        }
        double angle = this.angStRad;
        if (this.index == 0) {
            coords[0] = this.x + Math.cos(angle) * this.w;
            coords[1] = this.y + Math.sin(angle) * this.h;
            if (this.affine != null) {
                this.affine.transform(coords, 0, coords, 0, 1);
            }
            return 0;
        }
        if (this.index > this.arcSegs) {
            if (this.index == this.arcSegs + this.lineSegs) {
                return 4;
            }
            coords[0] = this.x;
            coords[1] = this.y;
            if (this.affine != null) {
                this.affine.transform(coords, 0, coords, 0, 1);
            }
            return 1;
        }
        double relx = Math.cos(angle += this.increment * (double)(this.index - 1));
        double rely = Math.sin(angle);
        coords[0] = this.x + (relx - this.cv * rely) * this.w;
        coords[1] = this.y + (rely + this.cv * relx) * this.h;
        relx = Math.cos(angle += this.increment);
        rely = Math.sin(angle);
        coords[2] = this.x + (relx + this.cv * rely) * this.w;
        coords[3] = this.y + (rely - this.cv * relx) * this.h;
        coords[4] = this.x + relx * this.w;
        coords[5] = this.y + rely * this.h;
        if (this.affine != null) {
            this.affine.transform(coords, 0, coords, 0, 3);
        }
        return 3;
    }
}

