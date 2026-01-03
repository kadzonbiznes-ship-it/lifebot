/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.image.DataBuffer;
import sun.java2d.StateTrackable;

public final class DataBufferByte
extends DataBuffer {
    byte[] data;
    byte[][] bankdata;

    public DataBufferByte(int size) {
        super(StateTrackable.State.STABLE, 0, size);
        this.data = new byte[size];
        this.bankdata = new byte[1][];
        this.bankdata[0] = this.data;
    }

    public DataBufferByte(int size, int numBanks) {
        super(StateTrackable.State.STABLE, 0, size, numBanks);
        this.bankdata = new byte[numBanks][];
        for (int i = 0; i < numBanks; ++i) {
            this.bankdata[i] = new byte[size];
        }
        this.data = this.bankdata[0];
    }

    public DataBufferByte(byte[] dataArray, int size) {
        super(StateTrackable.State.UNTRACKABLE, 0, size);
        this.data = dataArray;
        this.bankdata = new byte[1][];
        this.bankdata[0] = this.data;
    }

    public DataBufferByte(byte[] dataArray, int size, int offset) {
        super(StateTrackable.State.UNTRACKABLE, 0, size, 1, offset);
        this.data = dataArray;
        this.bankdata = new byte[1][];
        this.bankdata[0] = this.data;
    }

    public DataBufferByte(byte[][] dataArray, int size) {
        super(StateTrackable.State.UNTRACKABLE, 0, size, dataArray.length);
        this.bankdata = (byte[][])dataArray.clone();
        this.data = this.bankdata[0];
    }

    public DataBufferByte(byte[][] dataArray, int size, int[] offsets) {
        super(StateTrackable.State.UNTRACKABLE, 0, size, dataArray.length, offsets);
        this.bankdata = (byte[][])dataArray.clone();
        this.data = this.bankdata[0];
    }

    public byte[] getData() {
        this.theTrackable.setUntrackable();
        return this.data;
    }

    public byte[] getData(int bank) {
        this.theTrackable.setUntrackable();
        return this.bankdata[bank];
    }

    public byte[][] getBankData() {
        this.theTrackable.setUntrackable();
        return (byte[][])this.bankdata.clone();
    }

    @Override
    public int getElem(int i) {
        return this.data[i + this.offset] & 0xFF;
    }

    @Override
    public int getElem(int bank, int i) {
        return this.bankdata[bank][i + this.offsets[bank]] & 0xFF;
    }

    @Override
    public void setElem(int i, int val) {
        this.data[i + this.offset] = (byte)val;
        this.theTrackable.markDirty();
    }

    @Override
    public void setElem(int bank, int i, int val) {
        this.bankdata[bank][i + this.offsets[bank]] = (byte)val;
        this.theTrackable.markDirty();
    }
}

