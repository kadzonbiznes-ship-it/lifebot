/*
 * Decompiled with CFR 0.152.
 */
package java.time;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.DateTimeException;
import java.time.Ser;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.time.zone.ZoneRules;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import jdk.internal.ValueBased;
import jdk.internal.vm.annotation.Stable;

@ValueBased
public final class ZoneOffset
extends ZoneId
implements TemporalAccessor,
TemporalAdjuster,
Comparable<ZoneOffset>,
Serializable {
    private static final ConcurrentMap<Integer, ZoneOffset> SECONDS_CACHE = new ConcurrentHashMap<Integer, ZoneOffset>(16, 0.75f, 4);
    private static final ConcurrentMap<String, ZoneOffset> ID_CACHE = new ConcurrentHashMap<String, ZoneOffset>(16, 0.75f, 4);
    private static final int MAX_SECONDS = 64800;
    private static final long serialVersionUID = 2357656521762053153L;
    public static final ZoneOffset UTC = ZoneOffset.ofTotalSeconds(0);
    public static final ZoneOffset MIN = ZoneOffset.ofTotalSeconds(-64800);
    public static final ZoneOffset MAX = ZoneOffset.ofTotalSeconds(64800);
    private final int totalSeconds;
    private final transient String id;
    @Stable
    private transient ZoneRules rules;

    public static ZoneOffset of(String offsetId) {
        int seconds;
        int minutes;
        int hours;
        Objects.requireNonNull(offsetId, "offsetId");
        ZoneOffset offset = (ZoneOffset)ID_CACHE.get(offsetId);
        if (offset != null) {
            return offset;
        }
        switch (offsetId.length()) {
            case 2: {
                offsetId = offsetId.charAt(0) + "0" + offsetId.charAt(1);
            }
            case 3: {
                hours = ZoneOffset.parseNumber(offsetId, 1, false);
                minutes = 0;
                seconds = 0;
                break;
            }
            case 5: {
                hours = ZoneOffset.parseNumber(offsetId, 1, false);
                minutes = ZoneOffset.parseNumber(offsetId, 3, false);
                seconds = 0;
                break;
            }
            case 6: {
                hours = ZoneOffset.parseNumber(offsetId, 1, false);
                minutes = ZoneOffset.parseNumber(offsetId, 4, true);
                seconds = 0;
                break;
            }
            case 7: {
                hours = ZoneOffset.parseNumber(offsetId, 1, false);
                minutes = ZoneOffset.parseNumber(offsetId, 3, false);
                seconds = ZoneOffset.parseNumber(offsetId, 5, false);
                break;
            }
            case 9: {
                hours = ZoneOffset.parseNumber(offsetId, 1, false);
                minutes = ZoneOffset.parseNumber(offsetId, 4, true);
                seconds = ZoneOffset.parseNumber(offsetId, 7, true);
                break;
            }
            default: {
                throw new DateTimeException("Invalid ID for ZoneOffset, invalid format: " + offsetId);
            }
        }
        char first = offsetId.charAt(0);
        if (first != '+' && first != '-') {
            throw new DateTimeException("Invalid ID for ZoneOffset, plus/minus not found when expected: " + offsetId);
        }
        if (first == '-') {
            return ZoneOffset.ofHoursMinutesSeconds(-hours, -minutes, -seconds);
        }
        return ZoneOffset.ofHoursMinutesSeconds(hours, minutes, seconds);
    }

    private static int parseNumber(CharSequence offsetId, int pos, boolean precededByColon) {
        if (precededByColon && offsetId.charAt(pos - 1) != ':') {
            throw new DateTimeException("Invalid ID for ZoneOffset, colon not found when expected: " + offsetId);
        }
        char ch1 = offsetId.charAt(pos);
        char ch2 = offsetId.charAt(pos + 1);
        if (ch1 < '0' || ch1 > '9' || ch2 < '0' || ch2 > '9') {
            throw new DateTimeException("Invalid ID for ZoneOffset, non numeric characters found: " + offsetId);
        }
        return (ch1 - 48) * 10 + (ch2 - 48);
    }

    public static ZoneOffset ofHours(int hours) {
        return ZoneOffset.ofHoursMinutesSeconds(hours, 0, 0);
    }

    public static ZoneOffset ofHoursMinutes(int hours, int minutes) {
        return ZoneOffset.ofHoursMinutesSeconds(hours, minutes, 0);
    }

    public static ZoneOffset ofHoursMinutesSeconds(int hours, int minutes, int seconds) {
        ZoneOffset.validate(hours, minutes, seconds);
        int totalSeconds = ZoneOffset.totalSeconds(hours, minutes, seconds);
        return ZoneOffset.ofTotalSeconds(totalSeconds);
    }

    public static ZoneOffset from(TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "temporal");
        ZoneOffset offset = temporal.query(TemporalQueries.offset());
        if (offset == null) {
            throw new DateTimeException("Unable to obtain ZoneOffset from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName());
        }
        return offset;
    }

    private static void validate(int hours, int minutes, int seconds) {
        if (hours < -18 || hours > 18) {
            throw new DateTimeException("Zone offset hours not in valid range: value " + hours + " is not in the range -18 to 18");
        }
        if (hours > 0) {
            if (minutes < 0 || seconds < 0) {
                throw new DateTimeException("Zone offset minutes and seconds must be positive because hours is positive");
            }
        } else if (hours < 0) {
            if (minutes > 0 || seconds > 0) {
                throw new DateTimeException("Zone offset minutes and seconds must be negative because hours is negative");
            }
        } else if (minutes > 0 && seconds < 0 || minutes < 0 && seconds > 0) {
            throw new DateTimeException("Zone offset minutes and seconds must have the same sign");
        }
        if (minutes < -59 || minutes > 59) {
            throw new DateTimeException("Zone offset minutes not in valid range: value " + minutes + " is not in the range -59 to 59");
        }
        if (seconds < -59 || seconds > 59) {
            throw new DateTimeException("Zone offset seconds not in valid range: value " + seconds + " is not in the range -59 to 59");
        }
        if (Math.abs(hours) == 18 && (minutes | seconds) != 0) {
            throw new DateTimeException("Zone offset not in valid range: -18:00 to +18:00");
        }
    }

    private static int totalSeconds(int hours, int minutes, int seconds) {
        return hours * 3600 + minutes * 60 + seconds;
    }

    public static ZoneOffset ofTotalSeconds(int totalSeconds) {
        if (totalSeconds < -64800 || totalSeconds > 64800) {
            throw new DateTimeException("Zone offset not in valid range: -18:00 to +18:00");
        }
        if (totalSeconds % 900 == 0) {
            return SECONDS_CACHE.computeIfAbsent(totalSeconds, totalSecs -> {
                ZoneOffset result = new ZoneOffset((int)totalSecs);
                ID_CACHE.putIfAbsent(result.getId(), result);
                return result;
            });
        }
        return new ZoneOffset(totalSeconds);
    }

    private ZoneOffset(int totalSeconds) {
        this.totalSeconds = totalSeconds;
        this.id = ZoneOffset.buildId(totalSeconds);
    }

    private static String buildId(int totalSeconds) {
        if (totalSeconds == 0) {
            return "Z";
        }
        int absTotalSeconds = Math.abs(totalSeconds);
        StringBuilder buf = new StringBuilder();
        int absHours = absTotalSeconds / 3600;
        int absMinutes = absTotalSeconds / 60 % 60;
        buf.append(totalSeconds < 0 ? "-" : "+").append(absHours < 10 ? "0" : "").append(absHours).append(absMinutes < 10 ? ":0" : ":").append(absMinutes);
        int absSeconds = absTotalSeconds % 60;
        if (absSeconds != 0) {
            buf.append(absSeconds < 10 ? ":0" : ":").append(absSeconds);
        }
        return buf.toString();
    }

    public int getTotalSeconds() {
        return this.totalSeconds;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public ZoneRules getRules() {
        ZoneRules rules = this.rules;
        if (rules == null) {
            rules = this.rules = ZoneRules.of(this);
        }
        return rules;
    }

    @Override
    public ZoneId normalized() {
        return this;
    }

    @Override
    ZoneOffset getOffset(long epochSecond) {
        return this;
    }

    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field == ChronoField.OFFSET_SECONDS;
        }
        return field != null && field.isSupportedBy(this);
    }

    @Override
    public ValueRange range(TemporalField field) {
        return TemporalAccessor.super.range(field);
    }

    @Override
    public int get(TemporalField field) {
        if (field == ChronoField.OFFSET_SECONDS) {
            return this.totalSeconds;
        }
        if (field instanceof ChronoField) {
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return this.range(field).checkValidIntValue(this.getLong(field), field);
    }

    @Override
    public long getLong(TemporalField field) {
        if (field == ChronoField.OFFSET_SECONDS) {
            return this.totalSeconds;
        }
        if (field instanceof ChronoField) {
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return field.getFrom(this);
    }

    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.offset() || query == TemporalQueries.zone()) {
            return (R)this;
        }
        return TemporalAccessor.super.query(query);
    }

    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.OFFSET_SECONDS, this.totalSeconds);
    }

    @Override
    public int compareTo(ZoneOffset other) {
        return other.totalSeconds - this.totalSeconds;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ZoneOffset) {
            return this.totalSeconds == ((ZoneOffset)obj).totalSeconds;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.totalSeconds;
    }

    @Override
    public String toString() {
        return this.id;
    }

    private Object writeReplace() {
        return new Ser(8, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    @Override
    void write(DataOutput out) throws IOException {
        out.writeByte(8);
        this.writeExternal(out);
    }

    void writeExternal(DataOutput out) throws IOException {
        int offsetSecs = this.totalSeconds;
        int offsetByte = offsetSecs % 900 == 0 ? offsetSecs / 900 : 127;
        out.writeByte(offsetByte);
        if (offsetByte == 127) {
            out.writeInt(offsetSecs);
        }
    }

    static ZoneOffset readExternal(DataInput in) throws IOException {
        byte offsetByte = in.readByte();
        return offsetByte == 127 ? ZoneOffset.ofTotalSeconds(in.readInt()) : ZoneOffset.ofTotalSeconds(offsetByte * 900);
    }
}

