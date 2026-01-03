/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import sun.java2d.StateTrackable;
import sun.java2d.StateTrackableDelegate;
import sun.java2d.SurfaceData;

public class SunWritableRaster
extends WritableRaster {
    private static DataStealer stealer;
    private StateTrackableDelegate theTrackable;

    public static void setDataStealer(DataStealer ds) {
        if (stealer != null) {
            throw new InternalError("Attempt to set DataStealer twice");
        }
        stealer = ds;
    }

    public static byte[] stealData(DataBufferByte dbb, int bank) {
        return stealer.getData(dbb, bank);
    }

    public static short[] stealData(DataBufferUShort dbus, int bank) {
        return stealer.getData(dbus, bank);
    }

    public static int[] stealData(DataBufferInt dbi, int bank) {
        return stealer.getData(dbi, bank);
    }

    public static StateTrackableDelegate stealTrackable(DataBuffer db) {
        return stealer.getTrackable(db);
    }

    public static void setTrackable(DataBuffer db, StateTrackableDelegate trackable) {
        stealer.setTrackable(db, trackable);
    }

    public static void makeTrackable(DataBuffer db) {
        stealer.setTrackable(db, StateTrackableDelegate.createInstance(StateTrackable.State.STABLE));
    }

    public static void markDirty(DataBuffer db) {
        stealer.getTrackable(db).markDirty();
    }

    public static void markDirty(WritableRaster wr) {
        if (wr instanceof SunWritableRaster) {
            ((SunWritableRaster)wr).markDirty();
        } else {
            SunWritableRaster.markDirty(wr.getDataBuffer());
        }
    }

    public static void markDirty(Image img) {
        SurfaceData.getPrimarySurfaceData(img).markDirty();
    }

    public SunWritableRaster(SampleModel sampleModel, Point origin) {
        super(sampleModel, origin);
        this.theTrackable = SunWritableRaster.stealTrackable(this.dataBuffer);
    }

    public SunWritableRaster(SampleModel sampleModel, DataBuffer dataBuffer, Point origin) {
        super(sampleModel, dataBuffer, origin);
        this.theTrackable = SunWritableRaster.stealTrackable(dataBuffer);
    }

    public SunWritableRaster(SampleModel sampleModel, DataBuffer dataBuffer, Rectangle aRegion, Point sampleModelTranslate, WritableRaster parent) {
        super(sampleModel, dataBuffer, aRegion, sampleModelTranslate, parent);
        this.theTrackable = SunWritableRaster.stealTrackable(dataBuffer);
    }

    public final void markDirty() {
        this.theTrackable.markDirty();
    }

    public static interface DataStealer {
        public byte[] getData(DataBufferByte var1, int var2);

        public short[] getData(DataBufferUShort var1, int var2);

        public int[] getData(DataBufferInt var1, int var2);

        public StateTrackableDelegate getTrackable(DataBuffer var1);

        public void setTrackable(DataBuffer var1, StateTrackableDelegate var2);
    }
}

