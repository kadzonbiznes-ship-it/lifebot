/*
 * Decompiled with CFR 0.152.
 */
package sun.util.calendar;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import sun.util.calendar.CalendarSystem;
import sun.util.calendar.Gregorian;
import sun.util.calendar.ZoneInfoFile;

public class ZoneInfo
extends TimeZone {
    private static final int UTC_TIME = 0;
    private static final int STANDARD_TIME = 1;
    private static final int WALL_TIME = 2;
    private static final long OFFSET_MASK = 15L;
    private static final long DST_MASK = 240L;
    private static final int DST_NSHIFT = 4;
    private static final int TRANSITION_NSHIFT = 12;
    private int rawOffset;
    private int rawOffsetDiff = 0;
    private int checksum;
    private int dstSavings;
    private long[] transitions;
    private int[] offsets;
    private int[] simpleTimeZoneParams;
    private boolean willGMTOffsetChange = false;
    private transient boolean dirty = false;
    private static final long serialVersionUID = 2653134537216586139L;
    private transient SimpleTimeZone lastRule;

    public ZoneInfo() {
    }

    public ZoneInfo(String ID, int rawOffset) {
        this(ID, rawOffset, 0, 0, null, null, null, false);
    }

    ZoneInfo(String ID, int rawOffset, int dstSavings, int checksum, long[] transitions, int[] offsets, int[] simpleTimeZoneParams, boolean willGMTOffsetChange) {
        this.setID(ID);
        this.rawOffset = rawOffset;
        this.dstSavings = dstSavings;
        this.checksum = checksum;
        this.transitions = transitions;
        this.offsets = offsets;
        this.simpleTimeZoneParams = simpleTimeZoneParams;
        this.willGMTOffsetChange = willGMTOffsetChange;
    }

    @Override
    public int getOffset(long date) {
        return this.getOffsets(date, null, 0);
    }

    public int getOffsets(long utc, int[] offsets) {
        return this.getOffsets(utc, offsets, 0);
    }

    public int getOffsetsByStandard(long standard, int[] offsets) {
        return this.getOffsets(standard, offsets, 1);
    }

    public int getOffsetsByWall(long wall, int[] offsets) {
        return this.getOffsets(wall, offsets, 2);
    }

    private int getOffsets(long date, int[] offsets, int type) {
        if (this.transitions == null) {
            int offset = this.getLastRawOffset();
            if (offsets != null) {
                offsets[0] = offset;
                offsets[1] = 0;
            }
            return offset;
        }
        int index = this.getTransitionIndex(date -= (long)this.rawOffsetDiff, type);
        if (index < 0) {
            int offset = this.getLastRawOffset();
            if (offsets != null) {
                offsets[0] = offset;
                offsets[1] = 0;
            }
            return offset;
        }
        if (index < this.transitions.length) {
            long val = this.transitions[index];
            int offset = this.offsets[(int)(val & 0xFL)] + this.rawOffsetDiff;
            if (offsets != null) {
                int dst = (int)(val >>> 4 & 0xFL);
                int save = dst == 0 ? 0 : this.offsets[dst];
                offsets[0] = offset - save;
                offsets[1] = save;
            }
            return offset;
        }
        SimpleTimeZone tz = this.getLastRule();
        if (tz != null) {
            int dstoffset;
            int rawoffset = tz.getRawOffset();
            long msec = date;
            if (type != 0) {
                msec -= (long)this.rawOffset;
            }
            if ((dstoffset = tz.getOffset(msec) - this.rawOffset) > 0 && tz.getOffset(msec - (long)dstoffset) == rawoffset && type == 2) {
                dstoffset = 0;
            }
            if (offsets != null) {
                offsets[0] = rawoffset;
                offsets[1] = dstoffset;
            }
            return rawoffset + dstoffset;
        }
        long val = this.transitions[this.transitions.length - 1];
        int offset = this.offsets[(int)(val & 0xFL)] + this.rawOffsetDiff;
        if (offsets != null) {
            int dst = (int)(val >>> 4 & 0xFL);
            int save = dst == 0 ? 0 : this.offsets[dst];
            offsets[0] = offset - save;
            offsets[1] = save;
        }
        return offset;
    }

    private int getTransitionIndex(long date, int type) {
        int low = 0;
        int high = this.transitions.length - 1;
        while (low <= high) {
            int dstIndex;
            int mid = (low + high) / 2;
            long val = this.transitions[mid];
            long midVal = val >> 12;
            if (type != 0) {
                midVal += (long)this.offsets[(int)(val & 0xFL)];
            }
            if (type == 1 && (dstIndex = (int)(val >>> 4 & 0xFL)) != 0) {
                midVal -= (long)this.offsets[dstIndex];
            }
            if (midVal < date) {
                low = mid + 1;
                continue;
            }
            if (midVal > date) {
                high = mid - 1;
                continue;
            }
            return mid;
        }
        if (low >= this.transitions.length) {
            return low;
        }
        return low - 1;
    }

    @Override
    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
        if (milliseconds < 0 || milliseconds >= 86400000) {
            throw new IllegalArgumentException();
        }
        if (era == 0) {
            year = 1 - year;
        } else if (era != 1) {
            throw new IllegalArgumentException();
        }
        Gregorian gcal = CalendarSystem.getGregorianCalendar();
        Gregorian.Date date = gcal.newCalendarDate(null);
        date.setDate(year, month + 1, day);
        if (!gcal.validate(date)) {
            throw new IllegalArgumentException();
        }
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException();
        }
        if (this.transitions == null) {
            return this.getLastRawOffset();
        }
        long dateInMillis = gcal.getTime(date) + (long)milliseconds;
        return this.getOffsets(dateInMillis -= (long)this.rawOffset, null, 0);
    }

    @Override
    public synchronized void setRawOffset(int offsetMillis) {
        if (offsetMillis == this.rawOffset + this.rawOffsetDiff) {
            return;
        }
        this.rawOffsetDiff = offsetMillis - this.rawOffset;
        if (this.lastRule != null) {
            this.lastRule.setRawOffset(offsetMillis);
        }
        this.dirty = true;
    }

    @Override
    public int getRawOffset() {
        if (!this.willGMTOffsetChange) {
            return this.rawOffset + this.rawOffsetDiff;
        }
        int[] offsets = new int[2];
        this.getOffsets(System.currentTimeMillis(), offsets, 0);
        return offsets[0];
    }

    public boolean isDirty() {
        return this.dirty;
    }

    private int getLastRawOffset() {
        return this.rawOffset + this.rawOffsetDiff;
    }

    @Override
    public boolean useDaylightTime() {
        return this.simpleTimeZoneParams != null;
    }

    @Override
    public boolean observesDaylightTime() {
        if (this.simpleTimeZoneParams != null) {
            return true;
        }
        if (this.transitions == null) {
            return false;
        }
        long utc = System.currentTimeMillis() - (long)this.rawOffsetDiff;
        int index = this.getTransitionIndex(utc, 0);
        if (index < 0) {
            return false;
        }
        for (int i = index; i < this.transitions.length; ++i) {
            if ((this.transitions[i] & 0xF0L) == 0L) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean inDaylightTime(Date date) {
        if (date == null) {
            throw new NullPointerException();
        }
        if (this.transitions == null) {
            return false;
        }
        long utc = date.getTime() - (long)this.rawOffsetDiff;
        int index = this.getTransitionIndex(utc, 0);
        if (index < 0) {
            return false;
        }
        if (index < this.transitions.length) {
            return (this.transitions[index] & 0xF0L) != 0L;
        }
        SimpleTimeZone tz = this.getLastRule();
        if (tz != null) {
            return tz.inDaylightTime(date);
        }
        return (this.transitions[this.transitions.length - 1] & 0xF0L) != 0L;
    }

    @Override
    public int getDSTSavings() {
        return this.dstSavings;
    }

    public String toString() {
        return this.getClass().getName() + "[id=\"" + this.getID() + "\",offset=" + this.getLastRawOffset() + ",dstSavings=" + this.dstSavings + ",useDaylight=" + this.useDaylightTime() + ",transitions=" + (this.transitions != null ? this.transitions.length : 0) + ",lastRule=" + (this.lastRule == null ? this.getLastRuleInstance() : this.lastRule) + "]";
    }

    public static String[] getAvailableIDs() {
        return ZoneInfoFile.getZoneIds();
    }

    public static String[] getAvailableIDs(int rawOffset) {
        return ZoneInfoFile.getZoneIds(rawOffset);
    }

    public static TimeZone getTimeZone(String ID) {
        return ZoneInfoFile.getZoneInfo(ID);
    }

    private synchronized SimpleTimeZone getLastRule() {
        if (this.lastRule == null) {
            this.lastRule = this.getLastRuleInstance();
        }
        return this.lastRule;
    }

    public SimpleTimeZone getLastRuleInstance() {
        if (this.simpleTimeZoneParams == null) {
            return null;
        }
        if (this.simpleTimeZoneParams.length == 10) {
            return new SimpleTimeZone(this.getLastRawOffset(), this.getID(), this.simpleTimeZoneParams[0], this.simpleTimeZoneParams[1], this.simpleTimeZoneParams[2], this.simpleTimeZoneParams[3], this.simpleTimeZoneParams[4], this.simpleTimeZoneParams[5], this.simpleTimeZoneParams[6], this.simpleTimeZoneParams[7], this.simpleTimeZoneParams[8], this.simpleTimeZoneParams[9], this.dstSavings);
        }
        return new SimpleTimeZone(this.getLastRawOffset(), this.getID(), this.simpleTimeZoneParams[0], this.simpleTimeZoneParams[1], this.simpleTimeZoneParams[2], this.simpleTimeZoneParams[3], this.simpleTimeZoneParams[4], this.simpleTimeZoneParams[5], this.simpleTimeZoneParams[6], this.simpleTimeZoneParams[7], this.dstSavings);
    }

    @Override
    public Object clone() {
        ZoneInfo zi = (ZoneInfo)super.clone();
        zi.lastRule = null;
        return zi;
    }

    public int hashCode() {
        return this.getLastRawOffset() ^ this.checksum;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ZoneInfo)) {
            return false;
        }
        ZoneInfo that = (ZoneInfo)obj;
        return this.getID().equals(that.getID()) && this.getLastRawOffset() == that.getLastRawOffset() && this.checksum == that.checksum;
    }

    @Override
    public boolean hasSameRules(TimeZone other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof ZoneInfo)) {
            if (this.getRawOffset() != other.getRawOffset()) {
                return false;
            }
            return this.transitions == null && !this.useDaylightTime() && !other.useDaylightTime();
        }
        if (this.getLastRawOffset() != ((ZoneInfo)other).getLastRawOffset()) {
            return false;
        }
        return this.checksum == ((ZoneInfo)other).checksum;
    }

    public static Map<String, String> getAliasTable() {
        return ZoneInfoFile.getAliasMap();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.dirty = true;
    }
}

