/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.image.DataBuffer;
import sun.java2d.StateTrackable;

public final class DataBufferInt
extends DataBuffer {
    int[] data;
    int[][] bankdata;

    public DataBufferInt(int size) {
        super(StateTrackable.State.STABLE, 3, size);
        this.data = new int[size];
        this.bankdata = new int[1][];
        this.bankdata[0] = this.data;
    }

    public DataBufferInt(int size, int numBanks) {
        super(StateTrackable.State.STABLE, 3, size, numBanks);
        this.bankdata = new int[numBanks][];
        for (int i = 0; i < numBanks; ++i) {
            this.bankdata[i] = new int[size];
        }
        this.data = this.bankdata[0];
    }

    public DataBufferInt(int[] dataArray, int size) {
        super(StateTrackable.State.UNTRACKABLE, 3, size);
        this.data = dataArray;
        this.bankdata = new int[1][];
        this.bankdata[0] = this.data;
    }

    public DataBufferInt(int[] dataArray, int size, int offset) {
        super(StateTrackable.State.UNTRACKABLE, 3, size, 1, offset);
        this.data = dataArray;
        this.bankdata = new int[1][];
        this.bankdata[0] = this.data;
    }

    public DataBufferInt(int[][] dataArray, int size) {
        super(StateTrackable.State.UNTRACKABLE, 3, size, dataArray.length);
        this.bankdata = (int[][])dataArray.clone();
        this.data = this.bankdata[0];
    }

    public DataBufferInt(int[][] dataArray, int size, int[] offsets) {
        super(StateTrackable.State.UNTRACKABLE, 3, size, dataArray.length, offsets);
        this.bankdata = (int[][])dataArray.clone();
        this.data = this.bankdata[0];
    }

    public int[] getData() {
        this.theTrackable.setUntrackable();
        return this.data;
    }

    public int[] getData(int bank) {
        this.theTrackable.setUntrackable();
        return this.bankdata[bank];
    }

    public int[][] getBankData() {
        this.theTrackable.setUntrackable();
        return (int[][])this.bankdata.clone();
    }

    @Override
    public int getElem(int i) {
        return this.data[i + this.offset];
    }

    @Override
    public int getElem(int bank, int i) {
        return this.bankdata[bank][i + this.offsets[bank]];
    }

    @Override
    public void setElem(int i, int val) {
        this.data[i + this.offset] = val;
        this.theTrackable.markDirty();
    }

    @Override
    public void setElem(int bank, int i, int val) {
        this.bankdata[bank][i + this.offsets[bank]] = val;
        this.theTrackable.markDirty();
    }
}

