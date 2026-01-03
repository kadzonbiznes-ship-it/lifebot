/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import sun.font.GlyphList;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.DrawGlyphList;
import sun.java2d.loops.DrawGlyphListAA;
import sun.java2d.loops.DrawLine;
import sun.java2d.loops.DrawPath;
import sun.java2d.loops.DrawPolygons;
import sun.java2d.loops.DrawRect;
import sun.java2d.loops.FillPath;
import sun.java2d.loops.FillRect;
import sun.java2d.loops.FillSpans;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.GraphicsPrimitiveProxy;
import sun.java2d.loops.PixelWriter;
import sun.java2d.loops.SolidPixelWriter;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.XORComposite;
import sun.java2d.loops.XorPixelWriter;
import sun.java2d.pipe.Region;

public final class GeneralRenderer {
    static final int OUTCODE_TOP = 1;
    static final int OUTCODE_BOTTOM = 2;
    static final int OUTCODE_LEFT = 4;
    static final int OUTCODE_RIGHT = 8;

    public static void register() {
        Class<GeneralRenderer> owner = GeneralRenderer.class;
        GraphicsPrimitive[] primitives = new GraphicsPrimitive[]{new GraphicsPrimitiveProxy(owner, "SetFillRectANY", FillRect.methodSignature, FillRect.primTypeID, SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any), new GraphicsPrimitiveProxy(owner, "SetFillPathANY", FillPath.methodSignature, FillPath.primTypeID, SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any), new GraphicsPrimitiveProxy(owner, "SetFillSpansANY", FillSpans.methodSignature, FillSpans.primTypeID, SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any), new GraphicsPrimitiveProxy(owner, "SetDrawLineANY", DrawLine.methodSignature, DrawLine.primTypeID, SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any), new GraphicsPrimitiveProxy(owner, "SetDrawPolygonsANY", DrawPolygons.methodSignature, DrawPolygons.primTypeID, SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any), new GraphicsPrimitiveProxy(owner, "SetDrawPathANY", DrawPath.methodSignature, DrawPath.primTypeID, SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any), new GraphicsPrimitiveProxy(owner, "SetDrawRectANY", DrawRect.methodSignature, DrawRect.primTypeID, SurfaceType.AnyColor, CompositeType.SrcNoEa, SurfaceType.Any), new GraphicsPrimitiveProxy(owner, "XorFillRectANY", FillRect.methodSignature, FillRect.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any), new GraphicsPrimitiveProxy(owner, "XorFillPathANY", FillPath.methodSignature, FillPath.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any), new GraphicsPrimitiveProxy(owner, "XorFillSpansANY", FillSpans.methodSignature, FillSpans.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any), new GraphicsPrimitiveProxy(owner, "XorDrawLineANY", DrawLine.methodSignature, DrawLine.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any), new GraphicsPrimitiveProxy(owner, "XorDrawPolygonsANY", DrawPolygons.methodSignature, DrawPolygons.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any), new GraphicsPrimitiveProxy(owner, "XorDrawPathANY", DrawPath.methodSignature, DrawPath.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any), new GraphicsPrimitiveProxy(owner, "XorDrawRectANY", DrawRect.methodSignature, DrawRect.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any), new GraphicsPrimitiveProxy(owner, "XorDrawGlyphListANY", DrawGlyphList.methodSignature, DrawGlyphList.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any), new GraphicsPrimitiveProxy(owner, "XorDrawGlyphListAAANY", DrawGlyphListAA.methodSignature, DrawGlyphListAA.primTypeID, SurfaceType.AnyColor, CompositeType.Xor, SurfaceType.Any)};
        GraphicsPrimitiveMgr.register(primitives);
    }

    static void doDrawPoly(SurfaceData sData, PixelWriter pw, int[] xPoints, int[] yPoints, int off, int nPoints, Region clip, int transx, int transy, boolean close) {
        int y1;
        int x1;
        int[] tmp = null;
        if (nPoints <= 0) {
            return;
        }
        int mx = x1 = xPoints[off] + transx;
        int my = y1 = yPoints[off] + transy;
        while (--nPoints > 0) {
            int x2 = xPoints[++off] + transx;
            int y2 = yPoints[off] + transy;
            tmp = GeneralRenderer.doDrawLine(sData, pw, tmp, clip, x1, y1, x2, y2);
            x1 = x2;
            y1 = y2;
        }
        if (close && (x1 != mx || y1 != my)) {
            tmp = GeneralRenderer.doDrawLine(sData, pw, tmp, clip, x1, y1, mx, my);
        }
    }

    static void doSetRect(SurfaceData sData, PixelWriter pw, int x1, int y1, int x2, int y2) {
        WritableRaster dstRast = (WritableRaster)sData.getRaster(x1, y1, x2 - x1, y2 - y1);
        pw.setRaster(dstRast);
        while (y1 < y2) {
            for (int x = x1; x < x2; ++x) {
                pw.writePixel(x, y1);
            }
            ++y1;
        }
    }

    static int[] doDrawLine(SurfaceData sData, PixelWriter pw, int[] boundPts, Region clip, int origx1, int origy1, int origx2, int origy2) {
        if (boundPts == null) {
            boundPts = new int[8];
        }
        boundPts[0] = origx1;
        boundPts[1] = origy1;
        boundPts[2] = origx2;
        boundPts[3] = origy2;
        if (!GeneralRenderer.adjustLine(boundPts, clip.getLoX(), clip.getLoY(), clip.getHiX(), clip.getHiY())) {
            return boundPts;
        }
        int x1 = boundPts[0];
        int y1 = boundPts[1];
        int x2 = boundPts[2];
        int y2 = boundPts[3];
        WritableRaster dstRast = (WritableRaster)sData.getRaster(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2) + 1, Math.abs(y1 - y2) + 1);
        pw.setRaster(dstRast);
        if (x1 == x2) {
            if (y1 > y2) {
                do {
                    pw.writePixel(x1, y1);
                } while (--y1 >= y2);
            } else {
                do {
                    pw.writePixel(x1, y1);
                } while (++y1 <= y2);
            }
        } else if (y1 == y2) {
            if (x1 > x2) {
                do {
                    pw.writePixel(x1, y1);
                } while (--x1 >= x2);
            } else {
                do {
                    pw.writePixel(x1, y1);
                } while (++x1 <= x2);
            }
        } else {
            int steps;
            int bumpminor;
            int bumpmajor;
            int errminor;
            int errmajor;
            boolean xmajor;
            int dx = boundPts[4];
            int dy = boundPts[5];
            int ax = boundPts[6];
            int ay = boundPts[7];
            if (ax >= ay) {
                xmajor = true;
                errmajor = ay * 2;
                errminor = ax * 2;
                bumpmajor = dx < 0 ? -1 : 1;
                bumpminor = dy < 0 ? -1 : 1;
                ax = -ax;
                steps = x2 - x1;
            } else {
                xmajor = false;
                errmajor = ax * 2;
                errminor = ay * 2;
                bumpmajor = dy < 0 ? -1 : 1;
                bumpminor = dx < 0 ? -1 : 1;
                ay = -ay;
                steps = y2 - y1;
            }
            int error = -(errminor / 2);
            if (y1 != origy1) {
                int ysteps = y1 - origy1;
                if (ysteps < 0) {
                    ysteps = -ysteps;
                }
                error += ysteps * ax * 2;
            }
            if (x1 != origx1) {
                int xsteps = x1 - origx1;
                if (xsteps < 0) {
                    xsteps = -xsteps;
                }
                error += xsteps * ay * 2;
            }
            if (steps < 0) {
                steps = -steps;
            }
            if (xmajor) {
                do {
                    pw.writePixel(x1, y1);
                    x1 += bumpmajor;
                    if ((error += errmajor) < 0) continue;
                    y1 += bumpminor;
                    error -= errminor;
                } while (--steps >= 0);
            } else {
                do {
                    pw.writePixel(x1, y1);
                    y1 += bumpmajor;
                    if ((error += errmajor) < 0) continue;
                    x1 += bumpminor;
                    error -= errminor;
                } while (--steps >= 0);
            }
        }
        return boundPts;
    }

    public static void doDrawRect(PixelWriter pw, SunGraphics2D sg2d, SurfaceData sData, int x, int y, int w, int h) {
        if (w < 0 || h < 0) {
            return;
        }
        int x2 = Region.dimAdd(Region.dimAdd(x, w), 1);
        int y2 = Region.dimAdd(Region.dimAdd(y, h), 1);
        Region r = sg2d.getCompClip().getBoundsIntersectionXYXY(x, y, x2, y2);
        if (r.isEmpty()) {
            return;
        }
        int cx1 = r.getLoX();
        int cy1 = r.getLoY();
        int cx2 = r.getHiX();
        int cy2 = r.getHiY();
        if (w < 2 || h < 2) {
            GeneralRenderer.doSetRect(sData, pw, cx1, cy1, cx2, cy2);
            return;
        }
        if (cy1 == y) {
            GeneralRenderer.doSetRect(sData, pw, cx1, cy1, cx2, cy1 + 1);
        }
        if (cx1 == x) {
            GeneralRenderer.doSetRect(sData, pw, cx1, cy1 + 1, cx1 + 1, cy2 - 1);
        }
        if (cx2 == x2) {
            GeneralRenderer.doSetRect(sData, pw, cx2 - 1, cy1 + 1, cx2, cy2 - 1);
        }
        if (cy2 == y2) {
            GeneralRenderer.doSetRect(sData, pw, cx1, cy2 - 1, cx2, cy2);
        }
    }

    static void doDrawGlyphList(SurfaceData sData, PixelWriter pw, GlyphList gl, int fromGlyph, int toGlyph, Region clip) {
        int[] bounds = gl.getBounds(toGlyph);
        clip.clipBoxToBounds(bounds);
        int cx1 = bounds[0];
        int cy1 = bounds[1];
        int cx2 = bounds[2];
        int cy2 = bounds[3];
        WritableRaster dstRast = (WritableRaster)sData.getRaster(cx1, cy1, cx2 - cx1, cy2 - cy1);
        pw.setRaster(dstRast);
        for (int i = fromGlyph; i < toGlyph; ++i) {
            gl.setGlyphIndex(i);
            int[] metrics = gl.getMetrics();
            int gx1 = metrics[0];
            int gy1 = metrics[1];
            int w = metrics[2];
            int gx2 = gx1 + w;
            int gy2 = gy1 + metrics[3];
            int off = 0;
            if (gx1 < cx1) {
                off = cx1 - gx1;
                gx1 = cx1;
            }
            if (gy1 < cy1) {
                off += (cy1 - gy1) * w;
                gy1 = cy1;
            }
            if (gx2 > cx2) {
                gx2 = cx2;
            }
            if (gy2 > cy2) {
                gy2 = cy2;
            }
            if (gx2 <= gx1 || gy2 <= gy1) continue;
            byte[] alpha = gl.getGrayBits();
            w -= gx2 - gx1;
            for (int y = gy1; y < gy2; ++y) {
                for (int x = gx1; x < gx2; ++x) {
                    if (alpha[off++] >= 0) continue;
                    pw.writePixel(x, y);
                }
                off += w;
            }
        }
    }

    static int outcode(int x, int y, int xmin, int ymin, int xmax, int ymax) {
        int code = y < ymin ? 1 : (y > ymax ? 2 : 0);
        if (x < xmin) {
            code |= 4;
        } else if (x > xmax) {
            code |= 8;
        }
        return code;
    }

    public static boolean adjustLine(int[] boundPts, int cxmin, int cymin, int cx2, int cy2) {
        int cxmax = cx2 - 1;
        int cymax = cy2 - 1;
        int x1 = boundPts[0];
        int y1 = boundPts[1];
        int x2 = boundPts[2];
        int y2 = boundPts[3];
        if (cxmax < cxmin || cymax < cymin) {
            return false;
        }
        if (x1 == x2) {
            if (x1 < cxmin || x1 > cxmax) {
                return false;
            }
            if (y1 > y2) {
                int t = y1;
                y1 = y2;
                y2 = t;
            }
            if (y1 < cymin) {
                y1 = cymin;
            }
            if (y2 > cymax) {
                y2 = cymax;
            }
            if (y1 > y2) {
                return false;
            }
            boundPts[1] = y1;
            boundPts[3] = y2;
        } else if (y1 == y2) {
            if (y1 < cymin || y1 > cymax) {
                return false;
            }
            if (x1 > x2) {
                int t = x1;
                x1 = x2;
                x2 = t;
            }
            if (x1 < cxmin) {
                x1 = cxmin;
            }
            if (x2 > cxmax) {
                x2 = cxmax;
            }
            if (x1 > x2) {
                return false;
            }
            boundPts[0] = x1;
            boundPts[2] = x2;
        } else {
            int dx = x2 - x1;
            int dy = y2 - y1;
            int ax = dx < 0 ? -dx : dx;
            int ay = dy < 0 ? -dy : dy;
            boolean xmajor = ax >= ay;
            int outcode1 = GeneralRenderer.outcode(x1, y1, cxmin, cymin, cxmax, cymax);
            int outcode2 = GeneralRenderer.outcode(x2, y2, cxmin, cymin, cxmax, cymax);
            while ((outcode1 | outcode2) != 0) {
                int xsteps;
                int ysteps;
                if ((outcode1 & outcode2) != 0) {
                    return false;
                }
                if (outcode1 != 0) {
                    if (0 != (outcode1 & 3)) {
                        y1 = 0 != (outcode1 & 1) ? cymin : cymax;
                        ysteps = y1 - boundPts[1];
                        if (ysteps < 0) {
                            ysteps = -ysteps;
                        }
                        xsteps = 2 * ysteps * ax + ay;
                        if (xmajor) {
                            xsteps += ay - ax - 1;
                        }
                        xsteps /= 2 * ay;
                        if (dx < 0) {
                            xsteps = -xsteps;
                        }
                        x1 = boundPts[0] + xsteps;
                    } else if (0 != (outcode1 & 0xC)) {
                        x1 = 0 != (outcode1 & 4) ? cxmin : cxmax;
                        xsteps = x1 - boundPts[0];
                        if (xsteps < 0) {
                            xsteps = -xsteps;
                        }
                        ysteps = 2 * xsteps * ay + ax;
                        if (!xmajor) {
                            ysteps += ax - ay - 1;
                        }
                        ysteps /= 2 * ax;
                        if (dy < 0) {
                            ysteps = -ysteps;
                        }
                        y1 = boundPts[1] + ysteps;
                    }
                    outcode1 = GeneralRenderer.outcode(x1, y1, cxmin, cymin, cxmax, cymax);
                    continue;
                }
                if (0 != (outcode2 & 3)) {
                    y2 = 0 != (outcode2 & 1) ? cymin : cymax;
                    ysteps = y2 - boundPts[3];
                    if (ysteps < 0) {
                        ysteps = -ysteps;
                    }
                    xsteps = 2 * ysteps * ax + ay;
                    xsteps = xmajor ? (xsteps += ay - ax) : --xsteps;
                    xsteps /= 2 * ay;
                    if (dx > 0) {
                        xsteps = -xsteps;
                    }
                    x2 = boundPts[2] + xsteps;
                } else if (0 != (outcode2 & 0xC)) {
                    x2 = 0 != (outcode2 & 4) ? cxmin : cxmax;
                    xsteps = x2 - boundPts[2];
                    if (xsteps < 0) {
                        xsteps = -xsteps;
                    }
                    ysteps = 2 * xsteps * ay + ax;
                    ysteps = xmajor ? --ysteps : (ysteps += ax - ay);
                    ysteps /= 2 * ax;
                    if (dy > 0) {
                        ysteps = -ysteps;
                    }
                    y2 = boundPts[3] + ysteps;
                }
                outcode2 = GeneralRenderer.outcode(x2, y2, cxmin, cymin, cxmax, cymax);
            }
            boundPts[0] = x1;
            boundPts[1] = y1;
            boundPts[2] = x2;
            boundPts[3] = y2;
            boundPts[4] = dx;
            boundPts[5] = dy;
            boundPts[6] = ax;
            boundPts[7] = ay;
        }
        return true;
    }

    static PixelWriter createSolidPixelWriter(SunGraphics2D sg2d, SurfaceData sData) {
        ColorModel dstCM = sData.getColorModel();
        Object srcPixel = dstCM.getDataElements(sg2d.eargb, null);
        return new SolidPixelWriter(srcPixel);
    }

    static PixelWriter createXorPixelWriter(SunGraphics2D sg2d, SurfaceData sData) {
        ColorModel dstCM = sData.getColorModel();
        Object srcPixel = dstCM.getDataElements(sg2d.eargb, null);
        XORComposite comp = (XORComposite)sg2d.getComposite();
        int xorrgb = comp.getXorColor().getRGB();
        Object xorPixel = dstCM.getDataElements(xorrgb, null);
        switch (dstCM.getTransferType()) {
            case 0: {
                return new XorPixelWriter.ByteData(srcPixel, xorPixel);
            }
            case 1: 
            case 2: {
                return new XorPixelWriter.ShortData(srcPixel, xorPixel);
            }
            case 3: {
                return new XorPixelWriter.IntData(srcPixel, xorPixel);
            }
            case 4: {
                return new XorPixelWriter.FloatData(srcPixel, xorPixel);
            }
            case 5: {
                return new XorPixelWriter.DoubleData(srcPixel, xorPixel);
            }
        }
        throw new InternalError("Unsupported XOR pixel type");
    }
}

