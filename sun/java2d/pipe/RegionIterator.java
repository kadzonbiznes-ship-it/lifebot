/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import sun.java2d.pipe.Region;

public class RegionIterator {
    Region region;
    int curIndex;
    int numXbands;

    RegionIterator(Region r) {
        this.region = r;
    }

    public RegionIterator createCopy() {
        RegionIterator r = new RegionIterator(this.region);
        r.curIndex = this.curIndex;
        r.numXbands = this.numXbands;
        return r;
    }

    public void copyStateFrom(RegionIterator ri) {
        if (this.region != ri.region) {
            throw new InternalError("region mismatch");
        }
        this.curIndex = ri.curIndex;
        this.numXbands = ri.numXbands;
    }

    public boolean nextYRange(int[] range) {
        this.curIndex += this.numXbands * 2;
        this.numXbands = 0;
        if (this.curIndex >= this.region.endIndex) {
            return false;
        }
        range[1] = this.region.bands[this.curIndex++];
        range[3] = this.region.bands[this.curIndex++];
        this.numXbands = this.region.bands[this.curIndex++];
        return true;
    }

    public boolean nextXBand(int[] range) {
        if (this.numXbands <= 0) {
            return false;
        }
        --this.numXbands;
        range[0] = this.region.bands[this.curIndex++];
        range[2] = this.region.bands[this.curIndex++];
        return true;
    }
}

