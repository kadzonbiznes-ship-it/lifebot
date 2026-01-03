/*
 * Decompiled with CFR 0.152.
 */
package java.awt.geom;

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.util.NoSuchElementException;

class EllipseIterator
implements PathIterator {
    double x;
    double y;
    double w;
    double h;
    AffineTransform affine;
    int index;
    public static final double CtrlVal = 0.5522847498307933;
    private static final double pcv = 0.7761423749153966;
    private static final double ncv = 0.22385762508460333;
    private static double[][] ctrlpts = new double[][]{{1.0, 0.7761423749153966, 0.7761423749153966, 1.0, 0.5, 1.0}, {0.22385762508460333, 1.0, 0.0, 0.7761423749153966, 0.0, 0.5}, {0.0, 0.22385762508460333, 0.22385762508460333, 0.0, 0.5, 0.0}, {0.7761423749153966, 0.0, 1.0, 0.22385762508460333, 1.0, 0.5}};

    EllipseIterator(Ellipse2D e, AffineTransform at) {
        this.x = e.getX();
        this.y = e.getY();
        this.w = e.getWidth();
        this.h = e.getHeight();
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
            throw new NoSuchElementException("ellipse iterator out of bounds");
        }
        if (this.index == 5) {
            return 4;
        }
        if (this.index == 0) {
            double[] ctrls = ctrlpts[3];
            coords[0] = (float)(this.x + ctrls[4] * this.w);
            coords[1] = (float)(this.y + ctrls[5] * this.h);
            if (this.affine != null) {
                this.affine.transform(coords, 0, coords, 0, 1);
            }
            return 0;
        }
        double[] ctrls = ctrlpts[this.index - 1];
        coords[0] = (float)(this.x + ctrls[0] * this.w);
        coords[1] = (float)(this.y + ctrls[1] * this.h);
        coords[2] = (float)(this.x + ctrls[2] * this.w);
        coords[3] = (float)(this.y + ctrls[3] * this.h);
        coords[4] = (float)(this.x + ctrls[4] * this.w);
        coords[5] = (float)(this.y + ctrls[5] * this.h);
        if (this.affine != null) {
            this.affine.transform(coords, 0, coords, 0, 3);
        }
        return 3;
    }

    @Override
    public int currentSegment(double[] coords) {
        if (this.isDone()) {
            throw new NoSuchElementException("ellipse iterator out of bounds");
        }
        if (this.index == 5) {
            return 4;
        }
        if (this.index == 0) {
            double[] ctrls = ctrlpts[3];
            coords[0] = this.x + ctrls[4] * this.w;
            coords[1] = this.y + ctrls[5] * this.h;
            if (this.affine != null) {
                this.affine.transform(coords, 0, coords, 0, 1);
            }
            return 0;
        }
        double[] ctrls = ctrlpts[this.index - 1];
        coords[0] = this.x + ctrls[0] * this.w;
        coords[1] = this.y + ctrls[1] * this.h;
        coords[2] = this.x + ctrls[2] * this.w;
        coords[3] = this.y + ctrls[3] * this.h;
        coords[4] = this.x + ctrls[4] * this.w;
        coords[5] = this.y + ctrls[5] * this.h;
        if (this.affine != null) {
            this.affine.transform(coords, 0, coords, 0, 3);
        }
        return 3;
    }
}

