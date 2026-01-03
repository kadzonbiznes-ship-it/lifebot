/*
 * Decompiled with CFR 0.152.
 */
package java.time.zone;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneOffsetTransitionRule;
import java.time.zone.ZoneRules;

final class Ser
implements Externalizable {
    private static final long serialVersionUID = -8885321777449118786L;
    static final byte ZRULES = 1;
    static final byte ZOT = 2;
    static final byte ZOTRULE = 3;
    private byte type;
    private Serializable object;

    public Ser() {
    }

    Ser(byte type, Serializable object) {
        this.type = type;
        this.object = object;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Ser.writeInternal(this.type, this.object, out);
    }

    static void write(Object object, DataOutput out) throws IOException {
        Ser.writeInternal((byte)1, object, out);
    }

    private static void writeInternal(byte type, Object object, DataOutput out) throws IOException {
        out.writeByte(type);
        switch (type) {
            case 1: {
                ((ZoneRules)object).writeExternal(out);
                break;
            }
            case 2: {
                ((ZoneOffsetTransition)object).writeExternal(out);
                break;
            }
            case 3: {
                ((ZoneOffsetTransitionRule)object).writeExternal(out);
                break;
            }
            default: {
                throw new InvalidClassException("Unknown serialized type");
            }
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.type = in.readByte();
        this.object = Ser.readInternal(this.type, in);
    }

    static Serializable read(DataInput in) throws IOException, ClassNotFoundException {
        byte type = in.readByte();
        return Ser.readInternal(type, in);
    }

    private static Serializable readInternal(byte type, DataInput in) throws IOException, ClassNotFoundException {
        return switch (type) {
            case 1 -> ZoneRules.readExternal(in);
            case 2 -> ZoneOffsetTransition.readExternal(in);
            case 3 -> ZoneOffsetTransitionRule.readExternal(in);
            default -> throw new StreamCorruptedException("Unknown serialized type");
        };
    }

    private Object readResolve() {
        return this.object;
    }

    static void writeOffset(ZoneOffset offset, DataOutput out) throws IOException {
        int offsetSecs = offset.getTotalSeconds();
        int offsetByte = offsetSecs % 900 == 0 ? offsetSecs / 900 : 127;
        out.writeByte(offsetByte);
        if (offsetByte == 127) {
            out.writeInt(offsetSecs);
        }
    }

    static ZoneOffset readOffset(DataInput in) throws IOException {
        byte offsetByte = in.readByte();
        return offsetByte == 127 ? ZoneOffset.ofTotalSeconds(in.readInt()) : ZoneOffset.ofTotalSeconds(offsetByte * 900);
    }

    static void writeEpochSec(long epochSec, DataOutput out) throws IOException {
        if (epochSec >= -4575744000L && epochSec < 10413792000L && epochSec % 900L == 0L) {
            int store = (int)((epochSec + 4575744000L) / 900L);
            out.writeByte(store >>> 16 & 0xFF);
            out.writeByte(store >>> 8 & 0xFF);
            out.writeByte(store & 0xFF);
        } else {
            out.writeByte(255);
            out.writeLong(epochSec);
        }
    }

    static long readEpochSec(DataInput in) throws IOException {
        int hiByte = in.readByte() & 0xFF;
        if (hiByte == 255) {
            return in.readLong();
        }
        int midByte = in.readByte() & 0xFF;
        int loByte = in.readByte() & 0xFF;
        long tot = (hiByte << 16) + (midByte << 8) + loByte;
        return tot * 900L - 4575744000L;
    }
}

