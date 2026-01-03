/*
 * Decompiled with CFR 0.152.
 */
package java.awt.geom;

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.util.NoSuchElementException;

class LineIterator
implements PathIterator {
    Line2D line;
    AffineTransform affine;
    int index;

    LineIterator(Line2D l, AffineTransform at) {
        this.line = l;
        this.affine = at;
    }

    @Override
    public int getWindingRule() {
        return 1;
    }

    @Override
    public boolean isDone() {
        return this.index > 1;
    }

    @Override
    public void next() {
        ++this.index;
    }

    @Override
    public int currentSegment(float[] coords) {
        int type;
        if (this.isDone()) {
            throw new NoSuchElementException("line iterator out of bounds");
        }
        if (this.index == 0) {
            coords[0] = (float)this.line.getX1();
            coords[1] = (float)this.line.getY1();
            type = 0;
        } else {
            coords[0] = (float)this.line.getX2();
            coords[1] = (float)this.line.getY2();
            type = 1;
        }
        if (this.affine != null) {
            this.affine.transform(coords, 0, coords, 0, 1);
        }
        return type;
    }

    @Override
    public int currentSegment(double[] coords) {
        int type;
        if (this.isDone()) {
            throw new NoSuchElementException("line iterator out of bounds");
        }
        if (this.index == 0) {
            coords[0] = this.line.getX1();
            coords[1] = this.line.getY1();
            type = 0;
        } else {
            coords[0] = this.line.getX2();
            coords[1] = this.line.getY2();
            type = 1;
        }
        if (this.affine != null) {
            this.affine.transform(coords, 0, coords, 0, 1);
        }
        return type;
    }
}

