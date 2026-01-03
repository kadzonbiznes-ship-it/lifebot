/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import sun.java2d.pipe.Region;
import sun.java2d.pipe.RegionIterator;
import sun.java2d.pipe.SpanIterator;

public class RegionSpanIterator
implements SpanIterator {
    RegionIterator ri;
    int lox;
    int loy;
    int hix;
    int hiy;
    int curloy;
    int curhiy;
    boolean done = false;
    boolean isrect;

    public RegionSpanIterator(Region r) {
        int[] bounds = new int[4];
        r.getBounds(bounds);
        this.lox = bounds[0];
        this.loy = bounds[1];
        this.hix = bounds[2];
        this.hiy = bounds[3];
        this.isrect = r.isRectangular();
        this.ri = r.getIterator();
    }

    @Override
    public void getPathBox(int[] pathbox) {
        pathbox[0] = this.lox;
        pathbox[1] = this.loy;
        pathbox[2] = this.hix;
        pathbox[3] = this.hiy;
    }

    @Override
    public void intersectClipBox(int clox, int cloy, int chix, int chiy) {
        if (clox > this.lox) {
            this.lox = clox;
        }
        if (cloy > this.loy) {
            this.loy = cloy;
        }
        if (chix < this.hix) {
            this.hix = chix;
        }
        if (chiy < this.hiy) {
            this.hiy = chiy;
        }
        this.done = this.lox >= this.hix || this.loy >= this.hiy;
    }

    @Override
    public boolean nextSpan(int[] spanbox) {
        int curhix;
        int curlox;
        if (this.done) {
            return false;
        }
        if (this.isrect) {
            this.getPathBox(spanbox);
            this.done = true;
            return true;
        }
        int curloy = this.curloy;
        int curhiy = this.curhiy;
        while (true) {
            if (!this.ri.nextXBand(spanbox)) {
                if (!this.ri.nextYRange(spanbox)) {
                    this.done = true;
                    return false;
                }
                curloy = spanbox[1];
                curhiy = spanbox[3];
                if (curloy < this.loy) {
                    curloy = this.loy;
                }
                if (curhiy > this.hiy) {
                    curhiy = this.hiy;
                }
                if (curloy < this.hiy) continue;
                this.done = true;
                return false;
            }
            curlox = spanbox[0];
            curhix = spanbox[2];
            if (curlox < this.lox) {
                curlox = this.lox;
            }
            if (curhix > this.hix) {
                curhix = this.hix;
            }
            if (curlox < curhix && curloy < curhiy) break;
        }
        spanbox[0] = curlox;
        spanbox[1] = this.curloy = curloy;
        spanbox[2] = curhix;
        spanbox[3] = this.curhiy = curhiy;
        return true;
    }

    @Override
    public void skipDownTo(int y) {
        this.loy = y;
    }

    @Override
    public long getNativeIterator() {
        return 0L;
    }
}

