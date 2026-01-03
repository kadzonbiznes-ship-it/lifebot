/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.marlin;

import sun.java2d.marlin.DPathConsumer2D;
import sun.java2d.marlin.MarlinProperties;

final class PathSimplifier
implements DPathConsumer2D {
    private static final double PIX_THRESHOLD = MarlinProperties.getPathSimplifierPixelTolerance();
    private static final double SQUARE_TOLERANCE = PIX_THRESHOLD * PIX_THRESHOLD;
    private DPathConsumer2D delegate;
    private double cx;
    private double cy;
    private boolean skipped;
    private double sx;
    private double sy;

    PathSimplifier() {
    }

    PathSimplifier init(DPathConsumer2D delegate) {
        this.delegate = delegate;
        this.skipped = false;
        return this;
    }

    private void finishPath() {
        if (this.skipped) {
            this._lineTo(this.sx, this.sy);
        }
    }

    @Override
    public void pathDone() {
        this.finishPath();
        this.delegate.pathDone();
    }

    @Override
    public void closePath() {
        this.finishPath();
        this.delegate.closePath();
    }

    @Override
    public void moveTo(double xe, double ye) {
        this.finishPath();
        this.delegate.moveTo(xe, ye);
        this.cx = xe;
        this.cy = ye;
    }

    @Override
    public void lineTo(double xe, double ye) {
        double dx = xe - this.cx;
        double dy = ye - this.cy;
        if (dx * dx + dy * dy <= SQUARE_TOLERANCE) {
            this.skipped = true;
            this.sx = xe;
            this.sy = ye;
            return;
        }
        this._lineTo(xe, ye);
    }

    private void _lineTo(double xe, double ye) {
        this.delegate.lineTo(xe, ye);
        this.cx = xe;
        this.cy = ye;
        this.skipped = false;
    }

    @Override
    public void quadTo(double x1, double y1, double xe, double ye) {
        double dx = xe - this.cx;
        double dy = ye - this.cy;
        if (dx * dx + dy * dy <= SQUARE_TOLERANCE && (dx = x1 - this.cx) * dx + (dy = y1 - this.cy) * dy <= SQUARE_TOLERANCE) {
            this.skipped = true;
            this.sx = xe;
            this.sy = ye;
            return;
        }
        this.delegate.quadTo(x1, y1, xe, ye);
        this.cx = xe;
        this.cy = ye;
        this.skipped = false;
    }

    @Override
    public void curveTo(double x1, double y1, double x2, double y2, double xe, double ye) {
        double dx = xe - this.cx;
        double dy = ye - this.cy;
        if (dx * dx + dy * dy <= SQUARE_TOLERANCE && (dx = x1 - this.cx) * dx + (dy = y1 - this.cy) * dy <= SQUARE_TOLERANCE && (dx = x2 - this.cx) * dx + (dy = y2 - this.cy) * dy <= SQUARE_TOLERANCE) {
            this.skipped = true;
            this.sx = xe;
            this.sy = ye;
            return;
        }
        this.delegate.curveTo(x1, y1, x2, y2, xe, ye);
        this.cx = xe;
        this.cy = ye;
        this.skipped = false;
    }

    @Override
    public long getNativeConsumer() {
        return 0L;
    }
}

