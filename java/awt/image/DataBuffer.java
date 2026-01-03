/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import sun.awt.image.SunWritableRaster;
import sun.java2d.StateTrackable;
import sun.java2d.StateTrackableDelegate;

public abstract class DataBuffer {
    public static final int TYPE_BYTE = 0;
    public static final int TYPE_USHORT = 1;
    public static final int TYPE_SHORT = 2;
    public static final int TYPE_INT = 3;
    public static final int TYPE_FLOAT = 4;
    public static final int TYPE_DOUBLE = 5;
    public static final int TYPE_UNDEFINED = 32;
    protected int dataType;
    protected int banks;
    protected int offset;
    protected int size;
    protected int[] offsets;
    StateTrackableDelegate theTrackable;
    private static final int[] dataTypeSize = new int[]{8, 16, 16, 32, 32, 64};

    public static int getDataTypeSize(int type) {
        if (type < 0 || type > 5) {
            throw new IllegalArgumentException("Unknown data type " + type);
        }
        return dataTypeSize[type];
    }

    protected DataBuffer(int dataType, int size) {
        this(StateTrackable.State.UNTRACKABLE, dataType, size);
    }

    DataBuffer(StateTrackable.State initialState, int dataType, int size) {
        this.theTrackable = StateTrackableDelegate.createInstance(initialState);
        this.dataType = dataType;
        this.banks = 1;
        this.size = size;
        this.offset = 0;
        this.offsets = new int[1];
    }

    protected DataBuffer(int dataType, int size, int numBanks) {
        this(StateTrackable.State.UNTRACKABLE, dataType, size, numBanks);
    }

    DataBuffer(StateTrackable.State initialState, int dataType, int size, int numBanks) {
        this.theTrackable = StateTrackableDelegate.createInstance(initialState);
        this.dataType = dataType;
        this.banks = numBanks;
        this.size = size;
        this.offset = 0;
        this.offsets = new int[this.banks];
    }

    protected DataBuffer(int dataType, int size, int numBanks, int offset) {
        this(StateTrackable.State.UNTRACKABLE, dataType, size, numBanks, offset);
    }

    DataBuffer(StateTrackable.State initialState, int dataType, int size, int numBanks, int offset) {
        this.theTrackable = StateTrackableDelegate.createInstance(initialState);
        this.dataType = dataType;
        this.banks = numBanks;
        this.size = size;
        this.offset = offset;
        this.offsets = new int[numBanks];
        for (int i = 0; i < numBanks; ++i) {
            this.offsets[i] = offset;
        }
    }

    protected DataBuffer(int dataType, int size, int numBanks, int[] offsets) {
        this(StateTrackable.State.UNTRACKABLE, dataType, size, numBanks, offsets);
    }

    DataBuffer(StateTrackable.State initialState, int dataType, int size, int numBanks, int[] offsets) {
        if (numBanks != offsets.length) {
            throw new ArrayIndexOutOfBoundsException("Number of banks does not match number of bank offsets");
        }
        this.theTrackable = StateTrackableDelegate.createInstance(initialState);
        this.dataType = dataType;
        this.banks = numBanks;
        this.size = size;
        this.offset = offsets[0];
        this.offsets = (int[])offsets.clone();
    }

    public int getDataType() {
        return this.dataType;
    }

    public int getSize() {
        return this.size;
    }

    public int getOffset() {
        return this.offset;
    }

    public int[] getOffsets() {
        return (int[])this.offsets.clone();
    }

    public int getNumBanks() {
        return this.banks;
    }

    public int getElem(int i) {
        return this.getElem(0, i);
    }

    public abstract int getElem(int var1, int var2);

    public void setElem(int i, int val) {
        this.setElem(0, i, val);
    }

    public abstract void setElem(int var1, int var2, int var3);

    public float getElemFloat(int i) {
        return this.getElem(i);
    }

    public float getElemFloat(int bank, int i) {
        return this.getElem(bank, i);
    }

    public void setElemFloat(int i, float val) {
        this.setElem(i, (int)val);
    }

    public void setElemFloat(int bank, int i, float val) {
        this.setElem(bank, i, (int)val);
    }

    public double getElemDouble(int i) {
        return this.getElem(i);
    }

    public double getElemDouble(int bank, int i) {
        return this.getElem(bank, i);
    }

    public void setElemDouble(int i, double val) {
        this.setElem(i, (int)val);
    }

    public void setElemDouble(int bank, int i, double val) {
        this.setElem(bank, i, (int)val);
    }

    static int[] toIntArray(Object obj) {
        if (obj instanceof int[]) {
            return (int[])obj;
        }
        if (obj == null) {
            return null;
        }
        if (obj instanceof short[]) {
            short[] sdata = (short[])obj;
            int[] idata = new int[sdata.length];
            for (int i = 0; i < sdata.length; ++i) {
                idata[i] = sdata[i] & 0xFFFF;
            }
            return idata;
        }
        if (obj instanceof byte[]) {
            byte[] bdata = (byte[])obj;
            int[] idata = new int[bdata.length];
            for (int i = 0; i < bdata.length; ++i) {
                idata[i] = 0xFF & bdata[i];
            }
            return idata;
        }
        return null;
    }

    static {
        SunWritableRaster.setDataStealer(new SunWritableRaster.DataStealer(){

            @Override
            public byte[] getData(DataBufferByte dbb, int bank) {
                return dbb.bankdata[bank];
            }

            @Override
            public short[] getData(DataBufferUShort dbus, int bank) {
                return dbus.bankdata[bank];
            }

            @Override
            public int[] getData(DataBufferInt dbi, int bank) {
                return dbi.bankdata[bank];
            }

            @Override
            public StateTrackableDelegate getTrackable(DataBuffer db) {
                return db.theTrackable;
            }

            @Override
            public void setTrackable(DataBuffer db, StateTrackableDelegate trackable) {
                db.theTrackable = trackable;
            }
        });
    }
}

