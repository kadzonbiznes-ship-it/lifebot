/*
 * Decompiled with CFR 0.152.
 */
package java.awt.geom;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.RoundRectangle2D;
import java.util.NoSuchElementException;

class RoundRectIterator
implements PathIterator {
    double x;
    double y;
    double w;
    double h;
    double aw;
    double ah;
    AffineTransform affine;
    int index;
    private static final double angle = 0.7853981633974483;
    private static final double a = 1.0 - Math.cos(0.7853981633974483);
    private static final double b = Math.tan(0.7853981633974483);
    private static final double c = Math.sqrt(1.0 + b * b) - 1.0 + a;
    private static final double cv = 1.3333333333333333 * a * b / c;
    private static final double acv = (1.0 - cv) / 2.0;
    private static double[][] ctrlpts = new double[][]{{0.0, 0.0, 0.0, 0.5}, {0.0, 0.0, 1.0, -0.5}, {0.0, 0.0, 1.0, -acv, 0.0, acv, 1.0, 0.0, 0.0, 0.5, 1.0, 0.0}, {1.0, -0.5, 1.0, 0.0}, {1.0, -acv, 1.0, 0.0, 1.0, 0.0, 1.0, -acv, 1.0, 0.0, 1.0, -0.5}, {1.0, 0.0, 0.0, 0.5}, {1.0, 0.0, 0.0, acv, 1.0, -acv, 0.0, 0.0, 1.0, -0.5, 0.0, 0.0}, {0.0, 0.5, 0.0, 0.0}, {0.0, acv, 0.0, 0.0, 0.0, 0.0, 0.0, acv, 0.0, 0.0, 0.0, 0.5}, new double[0]};
    private static int[] types = new int[]{0, 1, 3, 1, 3, 1, 3, 1, 3, 4};

    RoundRectIterator(RoundRectangle2D rr, AffineTransform at) {
        this.x = rr.getX();
        this.y = rr.getY();
        this.w = rr.getWidth();
        this.h = rr.getHeight();
        this.aw = Math.min(this.w, Math.abs(rr.getArcWidth()));
        this.ah = Math.min(this.h, Math.abs(rr.getArcHeight()));
        this.affine = at;
        if (this.aw < 0.0 || this.ah < 0.0) {
            this.index = ctrlpts.length;
        }
    }

    @Override
    public int getWindingRule() {
        return 1;
    }

    @Override
    public boolean isDone() {
        return this.index >= ctrlpts.length;
    }

    @Override
    public void next() {
        ++this.index;
    }

    @Override
    public int currentSegment(float[] coords) {
        if (this.isDone()) {
            throw new NoSuchElementException("roundrect iterator out of bounds");
        }
        double[] ctrls = ctrlpts[this.index];
        int nc = 0;
        for (int i = 0; i < ctrls.length; i += 4) {
            coords[nc++] = (float)(this.x + ctrls[i + 0] * this.w + ctrls[i + 1] * this.aw);
            coords[nc++] = (float)(this.y + ctrls[i + 2] * this.h + ctrls[i + 3] * this.ah);
        }
        if (this.affine != null) {
            this.affine.transform(coords, 0, coords, 0, nc / 2);
        }
        return types[this.index];
    }

    @Override
    public int currentSegment(double[] coords) {
        if (this.isDone()) {
            throw new NoSuchElementException("roundrect iterator out of bounds");
        }
        double[] ctrls = ctrlpts[this.index];
        int nc = 0;
        for (int i = 0; i < ctrls.length; i += 4) {
            coords[nc++] = this.x + ctrls[i + 0] * this.w + ctrls[i + 1] * this.aw;
            coords[nc++] = this.y + ctrls[i + 2] * this.h + ctrls[i + 3] * this.ah;
        }
        if (this.affine != null) {
            this.affine.transform(coords, 0, coords, 0, nc / 2);
        }
        return types[this.index];
    }
}

