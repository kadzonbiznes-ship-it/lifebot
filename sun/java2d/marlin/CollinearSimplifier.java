/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import sun.java2d.marlin.DPathConsumer2D;

final class CollinearSimplifier
implements DPathConsumer2D {
    static final double EPS = 1.0E-4;
    DPathConsumer2D delegate;
    SimplifierState state;
    double px1;
    double py1;
    double px2;
    double py2;
    double pslope;

    CollinearSimplifier() {
    }

    public CollinearSimplifier init(DPathConsumer2D delegate) {
        this.delegate = delegate;
        this.state = SimplifierState.Empty;
        return this;
    }

    @Override
    public void pathDone() {
        this.emitStashedLine();
        this.state = SimplifierState.Empty;
        this.delegate.pathDone();
    }

    @Override
    public void closePath() {
        this.emitStashedLine();
        this.state = SimplifierState.Empty;
        this.delegate.closePath();
    }

    @Override
    public long getNativeConsumer() {
        return 0L;
    }

    @Override
    public void quadTo(double x1, double y1, double x2, double y2) {
        this.emitStashedLine();
        this.delegate.quadTo(x1, y1, x2, y2);
        this.state = SimplifierState.PreviousPoint;
        this.px1 = x2;
        this.py1 = y2;
    }

    @Override
    public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
        this.emitStashedLine();
        this.delegate.curveTo(x1, y1, x2, y2, x3, y3);
        this.state = SimplifierState.PreviousPoint;
        this.px1 = x3;
        this.py1 = y3;
    }

    @Override
    public void moveTo(double x, double y) {
        this.emitStashedLine();
        this.delegate.moveTo(x, y);
        this.state = SimplifierState.PreviousPoint;
        this.px1 = x;
        this.py1 = y;
    }

    @Override
    public void lineTo(double x, double y) {
        switch (this.state.ordinal()) {
            case 0: {
                this.delegate.lineTo(x, y);
                this.state = SimplifierState.PreviousPoint;
                this.px1 = x;
                this.py1 = y;
                return;
            }
            case 1: {
                this.state = SimplifierState.PreviousLine;
                this.px2 = x;
                this.py2 = y;
                this.pslope = CollinearSimplifier.getSlope(this.px1, this.py1, x, y);
                return;
            }
            case 2: {
                double slope = CollinearSimplifier.getSlope(this.px2, this.py2, x, y);
                if (slope == this.pslope || Math.abs(this.pslope - slope) < 1.0E-4) {
                    this.px2 = x;
                    this.py2 = y;
                    return;
                }
                this.delegate.lineTo(this.px2, this.py2);
                this.px1 = this.px2;
                this.py1 = this.py2;
                this.px2 = x;
                this.py2 = y;
                this.pslope = slope;
                return;
            }
        }
    }

    private void emitStashedLine() {
        if (this.state == SimplifierState.PreviousLine) {
            this.delegate.lineTo(this.px2, this.py2);
        }
    }

    private static double getSlope(double x1, double y1, double x2, double y2) {
        double dy = y2 - y1;
        if (dy == 0.0) {
            return x2 > x1 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }
        return (x2 - x1) / dy;
    }

    static enum SimplifierState {
        Empty,
        PreviousPoint,
        PreviousLine;

    }
}

