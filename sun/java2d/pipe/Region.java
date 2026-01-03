/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import sun.java2d.pipe.RegionClipSpanIterator;
import sun.java2d.pipe.RegionIterator;
import sun.java2d.pipe.RegionSpanIterator;
import sun.java2d.pipe.ShapeSpanIterator;
import sun.java2d.pipe.SpanIterator;

public final class Region {
    private static final int INIT_SIZE = 50;
    private static final int GROW_SIZE = 50;
    public static final Region EMPTY_REGION = new Region(0, 0, 0, 0);
    public static final Region WHOLE_REGION = new Region(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    private int lox;
    private int loy;
    private int hix;
    private int hiy;
    int endIndex;
    int[] bands;
    private static final int INCLUDE_A = 1;
    private static final int INCLUDE_B = 2;
    private static final int INCLUDE_COMMON = 4;

    private static native void initIDs();

    public static int dimAdd(int start, int dim) {
        if (dim <= 0) {
            return start;
        }
        if ((dim += start) < start) {
            return Integer.MAX_VALUE;
        }
        return dim;
    }

    public static int clipAdd(int v, int dv) {
        int newv = v + dv;
        if (newv > v != dv > 0) {
            newv = dv < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }
        return newv;
    }

    public static int clipRound(double coordinate) {
        double newv = coordinate - 0.5;
        if (newv < -2.147483648E9) {
            return Integer.MIN_VALUE;
        }
        if (newv > 2.147483647E9) {
            return Integer.MAX_VALUE;
        }
        return (int)Math.ceil(newv);
    }

    public static int clipScale(int v, double sv) {
        if (sv == 1.0) {
            return v;
        }
        double newv = (double)v * sv;
        if (newv < -2.147483648E9) {
            return Integer.MIN_VALUE;
        }
        if (newv > 2.147483647E9) {
            return Integer.MAX_VALUE;
        }
        return (int)Math.round(newv);
    }

    private Region(int lox, int loy, int hix, int hiy) {
        this.lox = lox;
        this.loy = loy;
        this.hix = hix;
        this.hiy = hiy;
    }

    private Region(int lox, int loy, int hix, int hiy, int[] bands, int end) {
        this.lox = lox;
        this.loy = loy;
        this.hix = hix;
        this.hiy = hiy;
        this.bands = bands;
        this.endIndex = end;
    }

    public static Region getInstance(Shape s, AffineTransform at) {
        return Region.getInstance(WHOLE_REGION, false, s, at);
    }

    public static Region getInstance(Region devBounds, Shape s, AffineTransform at) {
        return Region.getInstance(devBounds, false, s, at);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Region getInstance(Region devBounds, boolean normalize, Shape s, AffineTransform at) {
        if (s instanceof RectangularShape && ((RectangularShape)s).isEmpty()) {
            return EMPTY_REGION;
        }
        int[] box = new int[4];
        ShapeSpanIterator sr = new ShapeSpanIterator(normalize);
        try {
            sr.setOutputArea(devBounds);
            sr.appendPath(s.getPathIterator(at));
            sr.getPathBox(box);
            Region region = Region.getInstance(box, sr);
            return region;
        }
        finally {
            sr.dispose();
        }
    }

    static Region getInstance(int lox, int loy, int hix, int hiy, int[] edges) {
        int y1 = edges[0];
        int y2 = edges[1];
        if (hiy <= loy || hix <= lox || y2 <= y1) {
            return EMPTY_REGION;
        }
        int[] bands = new int[(y2 - y1) * 5];
        int end = 0;
        int index = 2;
        for (int y = y1; y < y2; ++y) {
            int spanhiy;
            int spanloy;
            int spanhix;
            int spanlox;
            if ((spanlox = Math.max(Region.clipAdd(lox, edges[index++]), lox)) >= (spanhix = Math.min(Region.clipAdd(lox, edges[index++]), hix)) || (spanloy = Math.max(Region.clipAdd(loy, y), loy)) >= (spanhiy = Math.min(Region.clipAdd(spanloy, 1), hiy))) continue;
            bands[end++] = spanloy;
            bands[end++] = spanhiy;
            bands[end++] = 1;
            bands[end++] = spanlox;
            bands[end++] = spanhix;
        }
        return end != 0 ? new Region(lox, loy, hix, hiy, bands, end) : EMPTY_REGION;
    }

    public static Region getInstance(Rectangle r) {
        return Region.getInstanceXYWH(r.x, r.y, r.width, r.height);
    }

    public static Region getInstanceXYWH(int x, int y, int w, int h) {
        return Region.getInstanceXYXY(x, y, Region.dimAdd(x, w), Region.dimAdd(y, h));
    }

    public static Region getInstance(int[] box) {
        return new Region(box[0], box[1], box[2], box[3]);
    }

    public static Region getInstanceXYXY(int lox, int loy, int hix, int hiy) {
        return new Region(lox, loy, hix, hiy);
    }

    public static Region getInstance(int[] box, SpanIterator si) {
        Region ret = new Region(box[0], box[1], box[2], box[3]);
        ret.appendSpans(si);
        return ret;
    }

    private void appendSpans(SpanIterator si) {
        int[] box = new int[6];
        while (si.nextSpan(box)) {
            this.appendSpan(box);
        }
        this.endRow(box);
        this.calcBBox();
    }

    public Region getScaledRegion(double sx, double sy) {
        if (sx == 0.0 || sy == 0.0 || this == EMPTY_REGION) {
            return EMPTY_REGION;
        }
        if (sx == 1.0 && sy == 1.0 || this == WHOLE_REGION) {
            return this;
        }
        int tlox = Region.clipScale(this.lox, sx);
        int tloy = Region.clipScale(this.loy, sy);
        int thix = Region.clipScale(this.hix, sx);
        int thiy = Region.clipScale(this.hiy, sy);
        Region ret = new Region(tlox, tloy, thix, thiy);
        int[] bands = this.bands;
        if (bands != null) {
            int end = this.endIndex;
            int[] newbands = new int[end];
            int i = 0;
            int j = 0;
            while (i < end) {
                int ncol;
                int y2;
                int y1;
                newbands[j++] = y1 = Region.clipScale(bands[i++], sy);
                newbands[j++] = y2 = Region.clipScale(bands[i++], sy);
                newbands[j++] = ncol = bands[i++];
                int savej = j;
                if (y1 < y2) {
                    while (--ncol >= 0) {
                        int x2;
                        int x1;
                        if ((x1 = Region.clipScale(bands[i++], sx)) >= (x2 = Region.clipScale(bands[i++], sx))) continue;
                        newbands[j++] = x1;
                        newbands[j++] = x2;
                    }
                } else {
                    i += ncol * 2;
                }
                if (j > savej) {
                    newbands[savej - 1] = (j - savej) / 2;
                    continue;
                }
                j = savej - 3;
            }
            if (j <= 5) {
                if (j < 5) {
                    ret.hiy = 0;
                    ret.hix = 0;
                    ret.loy = 0;
                    ret.lox = 0;
                } else {
                    ret.loy = newbands[0];
                    ret.hiy = newbands[1];
                    ret.lox = newbands[3];
                    ret.hix = newbands[4];
                }
            } else {
                ret.endIndex = j;
                ret.bands = newbands;
            }
        }
        return ret;
    }

    public Region getTranslatedRegion(int dx, int dy) {
        if ((dx | dy) == 0) {
            return this;
        }
        int tlox = this.lox + dx;
        int tloy = this.loy + dy;
        int thix = this.hix + dx;
        int thiy = this.hiy + dy;
        if (tlox > this.lox != dx > 0 || tloy > this.loy != dy > 0 || thix > this.hix != dx > 0 || thiy > this.hiy != dy > 0) {
            return this.getSafeTranslatedRegion(dx, dy);
        }
        Region ret = new Region(tlox, tloy, thix, thiy);
        int[] bands = this.bands;
        if (bands != null) {
            int end;
            ret.endIndex = end = this.endIndex;
            int[] newbands = new int[end];
            ret.bands = newbands;
            int i = 0;
            while (i < end) {
                int ncol;
                newbands[i] = bands[i] + dy;
                newbands[++i] = bands[i] + dy;
                newbands[++i] = ncol = bands[i];
                ++i;
                while (--ncol >= 0) {
                    newbands[i] = bands[i] + dx;
                    newbands[++i] = bands[i] + dx;
                    ++i;
                }
            }
        }
        return ret;
    }

    private Region getSafeTranslatedRegion(int dx, int dy) {
        int tlox = Region.clipAdd(this.lox, dx);
        int tloy = Region.clipAdd(this.loy, dy);
        int thix = Region.clipAdd(this.hix, dx);
        int thiy = Region.clipAdd(this.hiy, dy);
        Region ret = new Region(tlox, tloy, thix, thiy);
        int[] bands = this.bands;
        if (bands != null) {
            int end = this.endIndex;
            int[] newbands = new int[end];
            int i = 0;
            int j = 0;
            while (i < end) {
                int ncol;
                int y2;
                int y1;
                newbands[j++] = y1 = Region.clipAdd(bands[i++], dy);
                newbands[j++] = y2 = Region.clipAdd(bands[i++], dy);
                newbands[j++] = ncol = bands[i++];
                int savej = j;
                if (y1 < y2) {
                    while (--ncol >= 0) {
                        int x2;
                        int x1;
                        if ((x1 = Region.clipAdd(bands[i++], dx)) >= (x2 = Region.clipAdd(bands[i++], dx))) continue;
                        newbands[j++] = x1;
                        newbands[j++] = x2;
                    }
                } else {
                    i += ncol * 2;
                }
                if (j > savej) {
                    newbands[savej - 1] = (j - savej) / 2;
                    continue;
                }
                j = savej - 3;
            }
            if (j <= 5) {
                if (j < 5) {
                    ret.hiy = 0;
                    ret.hix = 0;
                    ret.loy = 0;
                    ret.lox = 0;
                } else {
                    ret.loy = newbands[0];
                    ret.hiy = newbands[1];
                    ret.lox = newbands[3];
                    ret.hix = newbands[4];
                }
            } else {
                ret.endIndex = j;
                ret.bands = newbands;
            }
        }
        return ret;
    }

    public Region getIntersection(Rectangle r) {
        return this.getIntersectionXYWH(r.x, r.y, r.width, r.height);
    }

    public Region getIntersectionXYWH(int x, int y, int w, int h) {
        return this.getIntersectionXYXY(x, y, Region.dimAdd(x, w), Region.dimAdd(y, h));
    }

    public Region getIntersection(Rectangle2D r) {
        if (r instanceof Rectangle) {
            return this.getIntersection((Rectangle)r);
        }
        return this.getIntersectionXYXY(r.getMinX(), r.getMinY(), r.getMaxX(), r.getMaxY());
    }

    public Region getIntersectionXYXY(double lox, double loy, double hix, double hiy) {
        if (Double.isNaN(lox) || Double.isNaN(loy) || Double.isNaN(hix) || Double.isNaN(hiy)) {
            return EMPTY_REGION;
        }
        return this.getIntersectionXYXY(Region.clipRound(lox), Region.clipRound(loy), Region.clipRound(hix), Region.clipRound(hiy));
    }

    public Region getIntersectionXYXY(int lox, int loy, int hix, int hiy) {
        if (this.isInsideXYXY(lox, loy, hix, hiy)) {
            return this;
        }
        Region ret = new Region(lox < this.lox ? this.lox : lox, loy < this.loy ? this.loy : loy, hix > this.hix ? this.hix : hix, hiy > this.hiy ? this.hiy : hiy);
        if (this.bands != null) {
            ret.appendSpans(this.getSpanIterator());
        }
        return ret;
    }

    public Region getIntersection(Region r) {
        if (this.isInsideQuickCheck(r)) {
            return this;
        }
        if (r.isInsideQuickCheck(this)) {
            return r;
        }
        Region ret = new Region(r.lox < this.lox ? this.lox : r.lox, r.loy < this.loy ? this.loy : r.loy, r.hix > this.hix ? this.hix : r.hix, r.hiy > this.hiy ? this.hiy : r.hiy);
        if (!ret.isEmpty()) {
            ret.filterSpans(this, r, 4);
        }
        return ret;
    }

    public Region getUnion(Region r) {
        if (r.isEmpty() || r.isInsideQuickCheck(this)) {
            return this;
        }
        if (this.isEmpty() || this.isInsideQuickCheck(r)) {
            return r;
        }
        Region ret = new Region(r.lox > this.lox ? this.lox : r.lox, r.loy > this.loy ? this.loy : r.loy, r.hix < this.hix ? this.hix : r.hix, r.hiy < this.hiy ? this.hiy : r.hiy);
        ret.filterSpans(this, r, 7);
        return ret;
    }

    public Region getDifference(Region r) {
        if (!r.intersectsQuickCheck(this)) {
            return this;
        }
        if (this.isInsideQuickCheck(r)) {
            return EMPTY_REGION;
        }
        Region ret = new Region(this.lox, this.loy, this.hix, this.hiy);
        ret.filterSpans(this, r, 1);
        return ret;
    }

    public Region getExclusiveOr(Region r) {
        if (r.isEmpty()) {
            return this;
        }
        if (this.isEmpty()) {
            return r;
        }
        Region ret = new Region(r.lox > this.lox ? this.lox : r.lox, r.loy > this.loy ? this.loy : r.loy, r.hix < this.hix ? this.hix : r.hix, r.hiy < this.hiy ? this.hiy : r.hiy);
        ret.filterSpans(this, r, 3);
        return ret;
    }

    private void filterSpans(Region ra, Region rb, int flags) {
        int[] abands = ra.bands;
        int[] bbands = rb.bands;
        if (abands == null) {
            abands = new int[]{ra.loy, ra.hiy, 1, ra.lox, ra.hix};
        }
        if (bbands == null) {
            bbands = new int[]{rb.loy, rb.hiy, 1, rb.lox, rb.hix};
        }
        int[] box = new int[6];
        int acolstart = 0;
        int ay1 = abands[acolstart++];
        int ay2 = abands[acolstart++];
        int acolend = abands[acolstart++];
        acolend = acolstart + 2 * acolend;
        int bcolstart = 0;
        int by1 = bbands[bcolstart++];
        int by2 = bbands[bcolstart++];
        int bcolend = bbands[bcolstart++];
        bcolend = bcolstart + 2 * bcolend;
        int y = this.loy;
        while (y < this.hiy) {
            int yend;
            if (y >= ay2) {
                if (acolend < ra.endIndex) {
                    acolstart = acolend;
                    ay1 = abands[acolstart++];
                    ay2 = abands[acolstart++];
                    acolend = abands[acolstart++];
                    acolend = acolstart + 2 * acolend;
                    continue;
                }
                if ((flags & 2) == 0) break;
                ay1 = ay2 = this.hiy;
                continue;
            }
            if (y >= by2) {
                if (bcolend < rb.endIndex) {
                    bcolstart = bcolend;
                    by1 = bbands[bcolstart++];
                    by2 = bbands[bcolstart++];
                    bcolend = bbands[bcolstart++];
                    bcolend = bcolstart + 2 * bcolend;
                    continue;
                }
                if ((flags & 1) == 0) break;
                by1 = by2 = this.hiy;
                continue;
            }
            if (y < by1) {
                if (y < ay1) {
                    y = Math.min(ay1, by1);
                    continue;
                }
                yend = Math.min(ay2, by1);
                if ((flags & 1) != 0) {
                    box[1] = y;
                    box[3] = yend;
                    acol = acolstart;
                    while (acol < acolend) {
                        box[0] = abands[acol++];
                        box[2] = abands[acol++];
                        this.appendSpan(box);
                    }
                }
            } else if (y < ay1) {
                yend = Math.min(by2, ay1);
                if ((flags & 2) != 0) {
                    box[1] = y;
                    box[3] = yend;
                    int bcol = bcolstart;
                    while (bcol < bcolend) {
                        box[0] = bbands[bcol++];
                        box[2] = bbands[bcol++];
                        this.appendSpan(box);
                    }
                }
            } else {
                yend = Math.min(ay2, by2);
                box[1] = y;
                box[3] = yend;
                acol = acolstart;
                int bcol = bcolstart;
                int ax1 = abands[acol++];
                int ax2 = abands[acol++];
                int bx1 = bbands[bcol++];
                int bx2 = bbands[bcol++];
                int x = Math.min(ax1, bx1);
                if (x < this.lox) {
                    x = this.lox;
                }
                while (x < this.hix) {
                    boolean appendit;
                    int xend;
                    if (x >= ax2) {
                        if (acol < acolend) {
                            ax1 = abands[acol++];
                            ax2 = abands[acol++];
                            continue;
                        }
                        if ((flags & 2) != 0) {
                            ax1 = ax2 = this.hix;
                            continue;
                        }
                        break;
                    }
                    if (x >= bx2) {
                        if (bcol < bcolend) {
                            bx1 = bbands[bcol++];
                            bx2 = bbands[bcol++];
                            continue;
                        }
                        if ((flags & 1) != 0) {
                            bx1 = bx2 = this.hix;
                            continue;
                        }
                        break;
                    }
                    if (x < bx1) {
                        if (x < ax1) {
                            xend = Math.min(ax1, bx1);
                            appendit = false;
                        } else {
                            xend = Math.min(ax2, bx1);
                            appendit = (flags & 1) != 0;
                        }
                    } else if (x < ax1) {
                        xend = Math.min(ax1, bx2);
                        appendit = (flags & 2) != 0;
                    } else {
                        xend = Math.min(ax2, bx2);
                        boolean bl = appendit = (flags & 4) != 0;
                    }
                    if (appendit) {
                        box[0] = x;
                        box[2] = xend;
                        this.appendSpan(box);
                    }
                    x = xend;
                }
            }
            y = yend;
        }
        this.endRow(box);
        this.calcBBox();
    }

    public Region getBoundsIntersection(Rectangle r) {
        return this.getBoundsIntersectionXYWH(r.x, r.y, r.width, r.height);
    }

    public Region getBoundsIntersectionXYWH(int x, int y, int w, int h) {
        return this.getBoundsIntersectionXYXY(x, y, Region.dimAdd(x, w), Region.dimAdd(y, h));
    }

    public Region getBoundsIntersectionXYXY(int lox, int loy, int hix, int hiy) {
        if (this.bands == null && this.lox >= lox && this.loy >= loy && this.hix <= hix && this.hiy <= hiy) {
            return this;
        }
        return new Region(lox < this.lox ? this.lox : lox, loy < this.loy ? this.loy : loy, hix > this.hix ? this.hix : hix, hiy > this.hiy ? this.hiy : hiy);
    }

    public Region getBoundsIntersection(Region r) {
        if (this.encompasses(r)) {
            return r;
        }
        if (r.encompasses(this)) {
            return this;
        }
        return new Region(r.lox < this.lox ? this.lox : r.lox, r.loy < this.loy ? this.loy : r.loy, r.hix > this.hix ? this.hix : r.hix, r.hiy > this.hiy ? this.hiy : r.hiy);
    }

    private void appendSpan(int[] box) {
        int spanhiy;
        int spanhix;
        int spanloy;
        int spanlox = box[0];
        if (spanlox < this.lox) {
            spanlox = this.lox;
        }
        if ((spanloy = box[1]) < this.loy) {
            spanloy = this.loy;
        }
        if ((spanhix = box[2]) > this.hix) {
            spanhix = this.hix;
        }
        if ((spanhiy = box[3]) > this.hiy) {
            spanhiy = this.hiy;
        }
        if (spanhix <= spanlox || spanhiy <= spanloy) {
            return;
        }
        int curYrow = box[4];
        if (this.endIndex == 0 || spanloy >= this.bands[curYrow + 1]) {
            if (this.bands == null) {
                this.bands = new int[50];
            } else {
                this.needSpace(5);
                this.endRow(box);
                curYrow = box[4];
            }
            this.bands[this.endIndex++] = spanloy;
            this.bands[this.endIndex++] = spanhiy;
            this.bands[this.endIndex++] = 0;
        } else if (spanloy == this.bands[curYrow] && spanhiy == this.bands[curYrow + 1] && spanlox >= this.bands[this.endIndex - 1]) {
            if (spanlox == this.bands[this.endIndex - 1]) {
                this.bands[this.endIndex - 1] = spanhix;
                return;
            }
            this.needSpace(2);
        } else {
            throw new InternalError("bad span");
        }
        this.bands[this.endIndex++] = spanlox;
        this.bands[this.endIndex++] = spanhix;
        int n = curYrow + 2;
        this.bands[n] = this.bands[n] + 1;
    }

    private void needSpace(int num) {
        if (this.endIndex + num >= this.bands.length) {
            int[] newbands = new int[this.bands.length + 50];
            System.arraycopy(this.bands, 0, newbands, 0, this.endIndex);
            this.bands = newbands;
        }
    }

    private void endRow(int[] box) {
        int[] bands;
        int cur = box[4];
        int prev = box[5];
        if (cur > prev && (bands = this.bands)[prev + 1] == bands[cur] && bands[prev + 2] == bands[cur + 2]) {
            int num = bands[cur + 2] * 2;
            cur += 3;
            prev += 3;
            while (num > 0 && bands[cur++] == bands[prev++]) {
                --num;
            }
            if (num == 0) {
                bands[box[5] + 1] = bands[prev + 1];
                this.endIndex = prev;
                return;
            }
        }
        box[5] = box[4];
        box[4] = this.endIndex;
    }

    private void calcBBox() {
        int[] bands = this.bands;
        if (this.endIndex <= 5) {
            if (this.endIndex == 0) {
                this.hiy = 0;
                this.hix = 0;
                this.loy = 0;
                this.lox = 0;
            } else {
                this.loy = bands[0];
                this.hiy = bands[1];
                this.lox = bands[3];
                this.hix = bands[4];
                this.endIndex = 0;
            }
            this.bands = null;
            return;
        }
        int lox = this.hix;
        int hix = this.lox;
        int hiyindex = 0;
        int i = 0;
        while (i < this.endIndex) {
            hiyindex = i;
            int numbands = bands[i + 2];
            if (lox > bands[i += 3]) {
                lox = bands[i];
            }
            if (hix >= bands[(i += numbands * 2) - 1]) continue;
            hix = bands[i - 1];
        }
        this.lox = lox;
        this.loy = bands[0];
        this.hix = hix;
        this.hiy = bands[hiyindex + 1];
    }

    public int getLoX() {
        return this.lox;
    }

    public int getLoY() {
        return this.loy;
    }

    public int getHiX() {
        return this.hix;
    }

    public int getHiY() {
        return this.hiy;
    }

    public int getWidth() {
        if (this.hix < this.lox) {
            return 0;
        }
        int w = this.hix - this.lox;
        if (w < 0) {
            w = Integer.MAX_VALUE;
        }
        return w;
    }

    public int getHeight() {
        if (this.hiy < this.loy) {
            return 0;
        }
        int h = this.hiy - this.loy;
        if (h < 0) {
            h = Integer.MAX_VALUE;
        }
        return h;
    }

    public boolean isEmpty() {
        return this.hix <= this.lox || this.hiy <= this.loy;
    }

    public boolean isRectangular() {
        return this.bands == null;
    }

    public boolean contains(int x, int y) {
        int numspans;
        if (x < this.lox || x >= this.hix || y < this.loy || y >= this.hiy) {
            return false;
        }
        if (this.bands == null) {
            return true;
        }
        for (int i = 0; i < this.endIndex; i += numspans * 2) {
            if (y < this.bands[i++]) {
                return false;
            }
            if (y >= this.bands[i++]) {
                numspans = this.bands[i++];
                continue;
            }
            int end = this.bands[i++];
            end = i + end * 2;
            while (i < end) {
                if (x < this.bands[i++]) {
                    return false;
                }
                if (x >= this.bands[i++]) continue;
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean isInsideXYWH(int x, int y, int w, int h) {
        return this.isInsideXYXY(x, y, Region.dimAdd(x, w), Region.dimAdd(y, h));
    }

    public boolean isInsideXYXY(int lox, int loy, int hix, int hiy) {
        return this.lox >= lox && this.loy >= loy && this.hix <= hix && this.hiy <= hiy;
    }

    public boolean isInsideQuickCheck(Region r) {
        return r.bands == null && r.lox <= this.lox && r.loy <= this.loy && r.hix >= this.hix && r.hiy >= this.hiy;
    }

    public boolean intersectsQuickCheckXYXY(int lox, int loy, int hix, int hiy) {
        return hix > this.lox && lox < this.hix && hiy > this.loy && loy < this.hiy;
    }

    public boolean intersectsQuickCheck(Region r) {
        return r.hix > this.lox && r.lox < this.hix && r.hiy > this.loy && r.loy < this.hiy;
    }

    public boolean encompasses(Region r) {
        return this.bands == null && this.lox <= r.lox && this.loy <= r.loy && this.hix >= r.hix && this.hiy >= r.hiy;
    }

    public boolean encompassesXYWH(int x, int y, int w, int h) {
        return this.encompassesXYXY(x, y, Region.dimAdd(x, w), Region.dimAdd(y, h));
    }

    public boolean encompassesXYXY(int lox, int loy, int hix, int hiy) {
        return this.bands == null && this.lox <= lox && this.loy <= loy && this.hix >= hix && this.hiy >= hiy;
    }

    public void getBounds(int[] pathbox) {
        pathbox[0] = this.lox;
        pathbox[1] = this.loy;
        pathbox[2] = this.hix;
        pathbox[3] = this.hiy;
    }

    public void clipBoxToBounds(int[] bbox) {
        if (bbox[0] < this.lox) {
            bbox[0] = this.lox;
        }
        if (bbox[1] < this.loy) {
            bbox[1] = this.loy;
        }
        if (bbox[2] > this.hix) {
            bbox[2] = this.hix;
        }
        if (bbox[3] > this.hiy) {
            bbox[3] = this.hiy;
        }
    }

    public RegionIterator getIterator() {
        return new RegionIterator(this);
    }

    public SpanIterator getSpanIterator() {
        return new RegionSpanIterator(this);
    }

    public SpanIterator getSpanIterator(int[] bbox) {
        SpanIterator result = this.getSpanIterator();
        result.intersectClipBox(bbox[0], bbox[1], bbox[2], bbox[3]);
        return result;
    }

    public SpanIterator filter(SpanIterator si) {
        if (this.bands == null) {
            si.intersectClipBox(this.lox, this.loy, this.hix, this.hiy);
        } else {
            si = new RegionClipSpanIterator(this, si);
        }
        return si;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Region[[");
        sb.append(this.lox);
        sb.append(", ");
        sb.append(this.loy);
        sb.append(" => ");
        sb.append(this.hix);
        sb.append(", ");
        sb.append(this.hiy);
        sb.append(']');
        if (this.bands != null) {
            int col = 0;
            while (col < this.endIndex) {
                sb.append("y{");
                sb.append(this.bands[col++]);
                sb.append(',');
                sb.append(this.bands[col++]);
                sb.append("}[");
                int end = this.bands[col++];
                end = col + end * 2;
                while (col < end) {
                    sb.append("x(");
                    sb.append(this.bands[col++]);
                    sb.append(", ");
                    sb.append(this.bands[col++]);
                    sb.append(')');
                }
                sb.append(']');
            }
        }
        sb.append(']');
        return sb.toString();
    }

    public int hashCode() {
        return this.isEmpty() ? 0 : this.lox * 3 + this.loy * 5 + this.hix * 7 + this.hiy * 9;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Region)) {
            return false;
        }
        Region r = (Region)o;
        if (this.isEmpty()) {
            return r.isEmpty();
        }
        if (r.isEmpty()) {
            return false;
        }
        if (r.lox != this.lox || r.loy != this.loy || r.hix != this.hix || r.hiy != this.hiy) {
            return false;
        }
        if (this.bands == null) {
            return r.bands == null;
        }
        if (r.bands == null) {
            return false;
        }
        if (this.endIndex != r.endIndex) {
            return false;
        }
        int[] abands = this.bands;
        int[] bbands = r.bands;
        for (int i = 0; i < this.endIndex; ++i) {
            if (abands[i] == bbands[i]) continue;
            return false;
        }
        return true;
    }

    static {
        Region.initIDs();
    }
}

