/*
 * Decompiled with CFR 0.152.
 */
package javax.swing.text;

import java.io.Serializable;

abstract class GapVector
implements Serializable {
    private Object array;
    private int g0;
    private int g1;

    public GapVector() {
        this(10);
    }

    public GapVector(int initialLength) {
        this.array = this.allocateArray(initialLength);
        this.g0 = 0;
        this.g1 = initialLength;
    }

    protected abstract Object allocateArray(int var1);

    protected abstract int getArrayLength();

    protected final Object getArray() {
        return this.array;
    }

    protected final int getGapStart() {
        return this.g0;
    }

    protected final int getGapEnd() {
        return this.g1;
    }

    protected void replace(int position, int rmSize, Object addItems, int addSize) {
        int addOffset = 0;
        if (addSize == 0) {
            this.close(position, rmSize);
            return;
        }
        if (rmSize > addSize) {
            this.close(position + addSize, rmSize - addSize);
        } else {
            int endSize = addSize - rmSize;
            int end = this.open(position + rmSize, endSize);
            System.arraycopy(addItems, rmSize, this.array, end, endSize);
            addSize = rmSize;
        }
        System.arraycopy(addItems, addOffset, this.array, position, addSize);
    }

    void close(int position, int nItems) {
        if (nItems == 0) {
            return;
        }
        int end = position + nItems;
        int new_gs = this.g1 - this.g0 + nItems;
        if (end <= this.g0) {
            if (this.g0 != end) {
                this.shiftGap(end);
            }
            this.shiftGapStartDown(this.g0 - nItems);
        } else if (position >= this.g0) {
            if (this.g0 != position) {
                this.shiftGap(position);
            }
            this.shiftGapEndUp(this.g0 + new_gs);
        } else {
            this.shiftGapStartDown(position);
            this.shiftGapEndUp(this.g0 + new_gs);
        }
    }

    int open(int position, int nItems) {
        int gapSize = this.g1 - this.g0;
        if (nItems == 0) {
            if (position > this.g0) {
                position += gapSize;
            }
            return position;
        }
        this.shiftGap(position);
        if (nItems >= gapSize) {
            this.shiftEnd(this.getArrayLength() - gapSize + nItems);
            gapSize = this.g1 - this.g0;
        }
        this.g0 += nItems;
        return position;
    }

    void resize(int nsize) {
        Object narray = this.allocateArray(nsize);
        System.arraycopy(this.array, 0, narray, 0, Math.min(nsize, this.getArrayLength()));
        this.array = narray;
    }

    protected void shiftEnd(int newSize) {
        int oldSize = this.getArrayLength();
        int oldGapEnd = this.g1;
        int upperSize = oldSize - oldGapEnd;
        int arrayLength = this.getNewArraySize(newSize);
        int newGapEnd = arrayLength - upperSize;
        this.resize(arrayLength);
        this.g1 = newGapEnd;
        if (upperSize != 0) {
            System.arraycopy(this.array, oldGapEnd, this.array, newGapEnd, upperSize);
        }
    }

    int getNewArraySize(int reqSize) {
        return (reqSize + 1) * 2;
    }

    protected void shiftGap(int newGapStart) {
        if (newGapStart == this.g0) {
            return;
        }
        int oldGapStart = this.g0;
        int dg = newGapStart - oldGapStart;
        int oldGapEnd = this.g1;
        int newGapEnd = oldGapEnd + dg;
        int gapSize = oldGapEnd - oldGapStart;
        this.g0 = newGapStart;
        this.g1 = newGapEnd;
        if (dg > 0) {
            System.arraycopy(this.array, oldGapEnd, this.array, oldGapStart, dg);
        } else if (dg < 0) {
            System.arraycopy(this.array, newGapStart, this.array, newGapEnd, -dg);
        }
    }

    protected void shiftGapStartDown(int newGapStart) {
        this.g0 = newGapStart;
    }

    protected void shiftGapEndUp(int newGapEnd) {
        this.g1 = newGapEnd;
    }
}

