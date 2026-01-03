/*
 * Decompiled with CFR 0.152.
 */
package java.time.zone;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.chrono.IsoChronology;
import java.time.temporal.TemporalAdjusters;
import java.time.zone.Ser;
import java.time.zone.ZoneOffsetTransition;
import java.util.Objects;

public final class ZoneOffsetTransitionRule
implements Serializable {
    private static final long serialVersionUID = 6889046316657758795L;
    private final Month month;
    private final byte dom;
    private final DayOfWeek dow;
    private final LocalTime time;
    private final boolean timeEndOfDay;
    private final TimeDefinition timeDefinition;
    private final ZoneOffset standardOffset;
    private final ZoneOffset offsetBefore;
    private final ZoneOffset offsetAfter;

    public static ZoneOffsetTransitionRule of(Month month, int dayOfMonthIndicator, DayOfWeek dayOfWeek, LocalTime time, boolean timeEndOfDay, TimeDefinition timeDefinition, ZoneOffset standardOffset, ZoneOffset offsetBefore, ZoneOffset offsetAfter) {
        Objects.requireNonNull(month, "month");
        Objects.requireNonNull(time, "time");
        Objects.requireNonNull(timeDefinition, "timeDefinition");
        Objects.requireNonNull(standardOffset, "standardOffset");
        Objects.requireNonNull(offsetBefore, "offsetBefore");
        Objects.requireNonNull(offsetAfter, "offsetAfter");
        if (dayOfMonthIndicator < -28 || dayOfMonthIndicator > 31 || dayOfMonthIndicator == 0) {
            throw new IllegalArgumentException("Day of month indicator must be between -28 and 31 inclusive excluding zero");
        }
        if (timeEndOfDay && !time.equals(LocalTime.MIDNIGHT)) {
            throw new IllegalArgumentException("Time must be midnight when end of day flag is true");
        }
        if (time.getNano() != 0) {
            throw new IllegalArgumentException("Time's nano-of-second must be zero");
        }
        return new ZoneOffsetTransitionRule(month, dayOfMonthIndicator, dayOfWeek, time, timeEndOfDay, timeDefinition, standardOffset, offsetBefore, offsetAfter);
    }

    ZoneOffsetTransitionRule(Month month, int dayOfMonthIndicator, DayOfWeek dayOfWeek, LocalTime time, boolean timeEndOfDay, TimeDefinition timeDefinition, ZoneOffset standardOffset, ZoneOffset offsetBefore, ZoneOffset offsetAfter) {
        assert (time.getNano() == 0);
        this.month = month;
        this.dom = (byte)dayOfMonthIndicator;
        this.dow = dayOfWeek;
        this.time = time;
        this.timeEndOfDay = timeEndOfDay;
        this.timeDefinition = timeDefinition;
        this.standardOffset = standardOffset;
        this.offsetBefore = offsetBefore;
        this.offsetAfter = offsetAfter;
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    private Object writeReplace() {
        return new Ser(3, this);
    }

    void writeExternal(DataOutput out) throws IOException {
        int timeSecs = this.timeEndOfDay ? 86400 : this.time.toSecondOfDay();
        int stdOffset = this.standardOffset.getTotalSeconds();
        int beforeDiff = this.offsetBefore.getTotalSeconds() - stdOffset;
        int afterDiff = this.offsetAfter.getTotalSeconds() - stdOffset;
        int timeByte = timeSecs % 3600 == 0 ? (this.timeEndOfDay ? 24 : this.time.getHour()) : 31;
        int stdOffsetByte = stdOffset % 900 == 0 ? stdOffset / 900 + 128 : 255;
        int beforeByte = beforeDiff == 0 || beforeDiff == 1800 || beforeDiff == 3600 ? beforeDiff / 1800 : 3;
        int afterByte = afterDiff == 0 || afterDiff == 1800 || afterDiff == 3600 ? afterDiff / 1800 : 3;
        int dowByte = this.dow == null ? 0 : this.dow.getValue();
        int b = (this.month.getValue() << 28) + (this.dom + 32 << 22) + (dowByte << 19) + (timeByte << 14) + (this.timeDefinition.ordinal() << 12) + (stdOffsetByte << 4) + (beforeByte << 2) + afterByte;
        out.writeInt(b);
        if (timeByte == 31) {
            out.writeInt(timeSecs);
        }
        if (stdOffsetByte == 255) {
            out.writeInt(stdOffset);
        }
        if (beforeByte == 3) {
            out.writeInt(this.offsetBefore.getTotalSeconds());
        }
        if (afterByte == 3) {
            out.writeInt(this.offsetAfter.getTotalSeconds());
        }
    }

    static ZoneOffsetTransitionRule readExternal(DataInput in) throws IOException {
        int data = in.readInt();
        Month month = Month.of(data >>> 28);
        int dom = ((data & 0xFC00000) >>> 22) - 32;
        int dowByte = (data & 0x380000) >>> 19;
        DayOfWeek dow = dowByte == 0 ? null : DayOfWeek.of(dowByte);
        int timeByte = (data & 0x7C000) >>> 14;
        TimeDefinition defn = TimeDefinition.values()[(data & 0x3000) >>> 12];
        int stdByte = (data & 0xFF0) >>> 4;
        int beforeByte = (data & 0xC) >>> 2;
        int afterByte = data & 3;
        LocalTime time = timeByte == 31 ? LocalTime.ofSecondOfDay(in.readInt()) : LocalTime.of(timeByte % 24, 0);
        ZoneOffset std = stdByte == 255 ? ZoneOffset.ofTotalSeconds(in.readInt()) : ZoneOffset.ofTotalSeconds((stdByte - 128) * 900);
        ZoneOffset before = beforeByte == 3 ? ZoneOffset.ofTotalSeconds(in.readInt()) : ZoneOffset.ofTotalSeconds(std.getTotalSeconds() + beforeByte * 1800);
        ZoneOffset after = afterByte == 3 ? ZoneOffset.ofTotalSeconds(in.readInt()) : ZoneOffset.ofTotalSeconds(std.getTotalSeconds() + afterByte * 1800);
        return ZoneOffsetTransitionRule.of(month, dom, dow, time, timeByte == 24, defn, std, before, after);
    }

    public Month getMonth() {
        return this.month;
    }

    public int getDayOfMonthIndicator() {
        return this.dom;
    }

    public DayOfWeek getDayOfWeek() {
        return this.dow;
    }

    public LocalTime getLocalTime() {
        return this.time;
    }

    public boolean isMidnightEndOfDay() {
        return this.timeEndOfDay;
    }

    public TimeDefinition getTimeDefinition() {
        return this.timeDefinition;
    }

    public ZoneOffset getStandardOffset() {
        return this.standardOffset;
    }

    public ZoneOffset getOffsetBefore() {
        return this.offsetBefore;
    }

    public ZoneOffset getOffsetAfter() {
        return this.offsetAfter;
    }

    public ZoneOffsetTransition createTransition(int year) {
        LocalDate date;
        if (this.dom < 0) {
            date = LocalDate.of(year, this.month, this.month.length(IsoChronology.INSTANCE.isLeapYear(year)) + 1 + this.dom);
            if (this.dow != null) {
                date = date.with(TemporalAdjusters.previousOrSame(this.dow));
            }
        } else {
            date = LocalDate.of(year, this.month, (int)this.dom);
            if (this.dow != null) {
                date = date.with(TemporalAdjusters.nextOrSame(this.dow));
            }
        }
        if (this.timeEndOfDay) {
            date = date.plusDays(1L);
        }
        LocalDateTime localDT = LocalDateTime.of(date, this.time);
        LocalDateTime transition = this.timeDefinition.createDateTime(localDT, this.standardOffset, this.offsetBefore);
        return new ZoneOffsetTransition(transition, this.offsetBefore, this.offsetAfter);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object otherRule) {
        if (otherRule == this) {
            return true;
        }
        if (!(otherRule instanceof ZoneOffsetTransitionRule)) return false;
        ZoneOffsetTransitionRule other = (ZoneOffsetTransitionRule)otherRule;
        if (this.month != other.month) return false;
        if (this.dom != other.dom) return false;
        if (this.dow != other.dow) return false;
        if (this.timeDefinition != other.timeDefinition) return false;
        if (this.timeEndOfDay != other.timeEndOfDay) return false;
        if (!this.time.equals(other.time)) return false;
        if (!this.standardOffset.equals(other.standardOffset)) return false;
        if (!this.offsetBefore.equals(other.offsetBefore)) return false;
        if (!this.offsetAfter.equals(other.offsetAfter)) return false;
        return true;
    }

    public int hashCode() {
        int hash = (this.time.toSecondOfDay() + (this.timeEndOfDay ? 1 : 0) << 15) + (this.month.ordinal() << 11) + (this.dom + 32 << 5) + ((this.dow == null ? 7 : this.dow.ordinal()) << 2) + this.timeDefinition.ordinal();
        return hash ^ this.standardOffset.hashCode() ^ this.offsetBefore.hashCode() ^ this.offsetAfter.hashCode();
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("TransitionRule[").append(this.offsetBefore.compareTo(this.offsetAfter) > 0 ? "Gap " : "Overlap ").append(this.offsetBefore).append(" to ").append(this.offsetAfter).append(", ");
        if (this.dow != null) {
            if (this.dom == -1) {
                buf.append(this.dow.name()).append(" on or before last day of ").append(this.month.name());
            } else if (this.dom < 0) {
                buf.append(this.dow.name()).append(" on or before last day minus ").append(-this.dom - 1).append(" of ").append(this.month.name());
            } else {
                buf.append(this.dow.name()).append(" on or after ").append(this.month.name()).append(' ').append(this.dom);
            }
        } else {
            buf.append(this.month.name()).append(' ').append(this.dom);
        }
        buf.append(" at ").append(this.timeEndOfDay ? "24:00" : this.time.toString()).append(" ").append((Object)this.timeDefinition).append(", standard offset ").append(this.standardOffset).append(']');
        return buf.toString();
    }

    public static enum TimeDefinition {
        UTC,
        WALL,
        STANDARD;


        public LocalDateTime createDateTime(LocalDateTime dateTime, ZoneOffset standardOffset, ZoneOffset wallOffset) {
            switch (this.ordinal()) {
                case 0: {
                    int difference = wallOffset.getTotalSeconds() - ZoneOffset.UTC.getTotalSeconds();
                    return dateTime.plusSeconds(difference);
                }
                case 2: {
                    int difference = wallOffset.getTotalSeconds() - standardOffset.getTotalSeconds();
                    return dateTime.plusSeconds(difference);
                }
            }
            return dateTime;
        }
    }
}

