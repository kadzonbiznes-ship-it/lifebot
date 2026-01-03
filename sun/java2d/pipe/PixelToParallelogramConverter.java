/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import sun.java2d.SunGraphics2D;
import sun.java2d.pipe.ParallelogramPipe;
import sun.java2d.pipe.PixelDrawPipe;
import sun.java2d.pipe.PixelToShapeConverter;
import sun.java2d.pipe.ShapeDrawPipe;

public class PixelToParallelogramConverter
extends PixelToShapeConverter
implements ShapeDrawPipe {
    ParallelogramPipe outrenderer;
    double minPenSize;
    double normPosition;
    double normRoundingBias;
    boolean adjustfill;

    public PixelToParallelogramConverter(ShapeDrawPipe shapepipe, ParallelogramPipe pgrampipe, double minPenSize, double normPosition, boolean adjustfill) {
        super(shapepipe);
        this.outrenderer = pgrampipe;
        this.minPenSize = minPenSize;
        this.normPosition = normPosition;
        this.normRoundingBias = 0.5 - normPosition;
        this.adjustfill = adjustfill;
    }

    @Override
    public void drawLine(SunGraphics2D sg2d, int x1, int y1, int x2, int y2) {
        if (!this.drawGeneralLine(sg2d, x1, y1, x2, y2)) {
            super.drawLine(sg2d, x1, y1, x2, y2);
        }
    }

    @Override
    public void drawRect(SunGraphics2D sg2d, int x, int y, int w, int h) {
        if (w >= 0 && h >= 0) {
            if (sg2d.strokeState < 3) {
                BasicStroke bs = (BasicStroke)sg2d.stroke;
                if (w > 0 && h > 0) {
                    if (bs.getLineJoin() == 0 && bs.getDashArray() == null) {
                        double lw = bs.getLineWidth();
                        this.drawRectangle(sg2d, x, y, w, h, lw);
                        return;
                    }
                } else {
                    this.drawLine(sg2d, x, y, x + w, y + h);
                    return;
                }
            }
            super.drawRect(sg2d, x, y, w, h);
        }
    }

    @Override
    public void fillRect(SunGraphics2D sg2d, int x, int y, int w, int h) {
        if (w > 0 && h > 0) {
            this.fillRectangle(sg2d, x, y, w, h);
        }
    }

    @Override
    public void draw(SunGraphics2D sg2d, Shape s) {
        if (sg2d.strokeState < 3) {
            Line2D l2d;
            BasicStroke bs = (BasicStroke)sg2d.stroke;
            if (s instanceof Rectangle2D) {
                if (bs.getLineJoin() == 0 && bs.getDashArray() == null) {
                    Rectangle2D r2d = (Rectangle2D)s;
                    double w = r2d.getWidth();
                    double h = r2d.getHeight();
                    double x = r2d.getX();
                    double y = r2d.getY();
                    if (w >= 0.0 && h >= 0.0) {
                        double lw = bs.getLineWidth();
                        this.drawRectangle(sg2d, x, y, w, h, lw);
                    }
                    return;
                }
            } else if (s instanceof Line2D && this.drawGeneralLine(sg2d, (l2d = (Line2D)s).getX1(), l2d.getY1(), l2d.getX2(), l2d.getY2())) {
                return;
            }
        }
        this.outpipe.draw(sg2d, s);
    }

    @Override
    public void fill(SunGraphics2D sg2d, Shape s) {
        if (s instanceof Rectangle2D) {
            Rectangle2D r2d = (Rectangle2D)s;
            double w = r2d.getWidth();
            double h = r2d.getHeight();
            if (w > 0.0 && h > 0.0) {
                double x = r2d.getX();
                double y = r2d.getY();
                this.fillRectangle(sg2d, x, y, w, h);
            }
            return;
        }
        this.outpipe.fill(sg2d, s);
    }

    static double len(double x, double y) {
        return x == 0.0 ? Math.abs(y) : (y == 0.0 ? Math.abs(x) : Math.sqrt(x * x + y * y));
    }

    double normalize(double v) {
        return Math.floor(v + this.normRoundingBias) + this.normPosition;
    }

    public boolean drawGeneralLine(SunGraphics2D sg2d, double ux1, double uy1, double ux2, double uy2) {
        double udy;
        double udx;
        double x2;
        double y1;
        double x1;
        if (sg2d.strokeState == 3 || sg2d.strokeState == 1) {
            return false;
        }
        BasicStroke bs = (BasicStroke)sg2d.stroke;
        int cap = bs.getEndCap();
        if (cap == 1 || bs.getDashArray() != null) {
            return false;
        }
        double lw = bs.getLineWidth();
        double dx = ux2 - ux1;
        double dy = uy2 - uy1;
        double y2 = switch (sg2d.transformState) {
            case 3, 4 -> {
                double[] coords = new double[]{ux1, uy1, ux2, uy2};
                sg2d.transform.transform(coords, 0, coords, 0, 2);
                x1 = coords[0];
                y1 = coords[1];
                x2 = coords[2];
                yield coords[3];
            }
            case 1, 2 -> {
                double tx = sg2d.transform.getTranslateX();
                double ty = sg2d.transform.getTranslateY();
                x1 = ux1 + tx;
                y1 = uy1 + ty;
                x2 = ux2 + tx;
                yield uy2 + ty;
            }
            case 0 -> {
                x1 = ux1;
                y1 = uy1;
                x2 = ux2;
                yield uy2;
            }
            default -> throw new InternalError("unknown TRANSFORM state...");
        };
        if (sg2d.strokeHint != 2) {
            if (sg2d.strokeState == 0 && this.outrenderer instanceof PixelDrawPipe) {
                int ix1 = (int)Math.floor(x1 - (double)sg2d.transX);
                int iy1 = (int)Math.floor(y1 - (double)sg2d.transY);
                int ix2 = (int)Math.floor(x2 - (double)sg2d.transX);
                int iy2 = (int)Math.floor(y2 - (double)sg2d.transY);
                ((PixelDrawPipe)((Object)this.outrenderer)).drawLine(sg2d, ix1, iy1, ix2, iy2);
                return true;
            }
            x1 = this.normalize(x1);
            y1 = this.normalize(y1);
            x2 = this.normalize(x2);
            y2 = this.normalize(y2);
        }
        if (sg2d.transformState >= 3) {
            double len = PixelToParallelogramConverter.len(dx, dy);
            if (len == 0.0) {
                len = 1.0;
                dx = 1.0;
            }
            double[] unitvector = new double[]{dy / len, -dx / len};
            sg2d.transform.deltaTransform(unitvector, 0, unitvector, 0, 1);
            lw *= PixelToParallelogramConverter.len(unitvector[0], unitvector[1]);
        }
        lw = Math.max(lw, this.minPenSize);
        dx = x2 - x1;
        dy = y2 - y1;
        double len = PixelToParallelogramConverter.len(dx, dy);
        if (len == 0.0) {
            if (cap == 0) {
                return true;
            }
            udx = lw;
            udy = 0.0;
        } else {
            udx = lw * dx / len;
            udy = lw * dy / len;
        }
        double px = x1 + udy / 2.0;
        double py = y1 - udx / 2.0;
        if (cap == 2) {
            px -= udx / 2.0;
            py -= udy / 2.0;
            dx += udx;
            dy += udy;
        }
        this.outrenderer.fillParallelogram(sg2d, ux1, uy1, ux2, uy2, px, py, -udy, udx, dx, dy);
        return true;
    }

    public void fillRectangle(SunGraphics2D sg2d, double rx, double ry, double rw, double rh) {
        AffineTransform txform = sg2d.transform;
        double dx1 = txform.getScaleX();
        double dy1 = txform.getShearY();
        double dx2 = txform.getShearX();
        double dy2 = txform.getScaleY();
        double px = rx * dx1 + ry * dx2 + txform.getTranslateX();
        double py = rx * dy1 + ry * dy2 + txform.getTranslateY();
        dx1 *= rw;
        dy1 *= rw;
        dx2 *= rh;
        dy2 *= rh;
        if (this.adjustfill && sg2d.strokeState < 3 && sg2d.strokeHint != 2) {
            double newx = this.normalize(px);
            double newy = this.normalize(py);
            dx1 = this.normalize(px + dx1) - newx;
            dy1 = this.normalize(py + dy1) - newy;
            dx2 = this.normalize(px + dx2) - newx;
            dy2 = this.normalize(py + dy2) - newy;
            px = newx;
            py = newy;
        }
        this.outrenderer.fillParallelogram(sg2d, rx, ry, rx + rw, ry + rh, px, py, dx1, dy1, dx2, dy2);
    }

    public void drawRectangle(SunGraphics2D sg2d, double rx, double ry, double rw, double rh, double lw) {
        AffineTransform txform = sg2d.transform;
        double dx1 = txform.getScaleX();
        double dy1 = txform.getShearY();
        double dx2 = txform.getShearX();
        double dy2 = txform.getScaleY();
        double px = rx * dx1 + ry * dx2 + txform.getTranslateX();
        double py = rx * dy1 + ry * dy2 + txform.getTranslateY();
        double lw1 = PixelToParallelogramConverter.len(dx1, dy1) * lw;
        double lw2 = PixelToParallelogramConverter.len(dx2, dy2) * lw;
        dx1 *= rw;
        dy1 *= rw;
        dx2 *= rh;
        dy2 *= rh;
        if (sg2d.strokeState < 3 && sg2d.strokeHint != 2) {
            double newx = this.normalize(px);
            double newy = this.normalize(py);
            dx1 = this.normalize(px + dx1) - newx;
            dy1 = this.normalize(py + dy1) - newy;
            dx2 = this.normalize(px + dx2) - newx;
            dy2 = this.normalize(py + dy2) - newy;
            px = newx;
            py = newy;
        }
        lw1 = Math.max(lw1, this.minPenSize);
        lw2 = Math.max(lw2, this.minPenSize);
        double len1 = PixelToParallelogramConverter.len(dx1, dy1);
        double len2 = PixelToParallelogramConverter.len(dx2, dy2);
        if (lw1 >= len1 || lw2 >= len2) {
            this.fillOuterParallelogram(sg2d, rx, ry, rx + rw, ry + rh, px, py, dx1, dy1, dx2, dy2, len1, len2, lw1, lw2);
        } else {
            this.outrenderer.drawParallelogram(sg2d, rx, ry, rx + rw, ry + rh, px, py, dx1, dy1, dx2, dy2, lw1 / len1, lw2 / len2);
        }
    }

    public void fillOuterParallelogram(SunGraphics2D sg2d, double ux1, double uy1, double ux2, double uy2, double px, double py, double dx1, double dy1, double dx2, double dy2, double len1, double len2, double lw1, double lw2) {
        double udx1 = dx1 / len1;
        double udy1 = dy1 / len1;
        double udx2 = dx2 / len2;
        double udy2 = dy2 / len2;
        if (len1 == 0.0) {
            if (len2 == 0.0) {
                udx2 = 0.0;
                udy2 = 1.0;
            }
            udx1 = udy2;
            udy1 = -udx2;
        } else if (len2 == 0.0) {
            udx2 = udy1;
            udy2 = -udx1;
        }
        this.outrenderer.fillParallelogram(sg2d, ux1, uy1, ux2, uy2, px -= ((udx1 *= lw1) + (udx2 *= lw2)) / 2.0, py -= ((udy1 *= lw1) + (udy2 *= lw2)) / 2.0, dx1 += udx1, dy1 += udy1, dx2 += udx2, dy2 += udy2);
    }
}

