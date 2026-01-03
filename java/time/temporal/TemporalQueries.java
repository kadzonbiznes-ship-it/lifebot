/*
 * Decompiled with CFR 0.152.
 */
package java.time.temporal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.Chronology;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.time.temporal.TemporalUnit;

public final class TemporalQueries {
    static final TemporalQuery<ZoneId> ZONE_ID = new TemporalQuery<ZoneId>(){

        @Override
        public ZoneId queryFrom(TemporalAccessor temporal) {
            return temporal.query(ZONE_ID);
        }

        public String toString() {
            return "ZoneId";
        }
    };
    static final TemporalQuery<Chronology> CHRONO = new TemporalQuery<Chronology>(){

        @Override
        public Chronology queryFrom(TemporalAccessor temporal) {
            return temporal.query(CHRONO);
        }

        public String toString() {
            return "Chronology";
        }
    };
    static final TemporalQuery<TemporalUnit> PRECISION = new TemporalQuery<TemporalUnit>(){

        @Override
        public TemporalUnit queryFrom(TemporalAccessor temporal) {
            return temporal.query(PRECISION);
        }

        public String toString() {
            return "Precision";
        }
    };
    static final TemporalQuery<ZoneOffset> OFFSET = new TemporalQuery<ZoneOffset>(){

        @Override
        public ZoneOffset queryFrom(TemporalAccessor temporal) {
            if (temporal.isSupported(ChronoField.OFFSET_SECONDS)) {
                return ZoneOffset.ofTotalSeconds(temporal.get(ChronoField.OFFSET_SECONDS));
            }
            return null;
        }

        public String toString() {
            return "ZoneOffset";
        }
    };
    static final TemporalQuery<ZoneId> ZONE = new TemporalQuery<ZoneId>(){

        @Override
        public ZoneId queryFrom(TemporalAccessor temporal) {
            ZoneId zone = temporal.query(ZONE_ID);
            return zone != null ? zone : (ZoneId)temporal.query(OFFSET);
        }

        public String toString() {
            return "Zone";
        }
    };
    static final TemporalQuery<LocalDate> LOCAL_DATE = new TemporalQuery<LocalDate>(){

        @Override
        public LocalDate queryFrom(TemporalAccessor temporal) {
            if (temporal.isSupported(ChronoField.EPOCH_DAY)) {
                return LocalDate.ofEpochDay(temporal.getLong(ChronoField.EPOCH_DAY));
            }
            return null;
        }

        public String toString() {
            return "LocalDate";
        }
    };
    static final TemporalQuery<LocalTime> LOCAL_TIME = new TemporalQuery<LocalTime>(){

        @Override
        public LocalTime queryFrom(TemporalAccessor temporal) {
            if (temporal.isSupported(ChronoField.NANO_OF_DAY)) {
                return LocalTime.ofNanoOfDay(temporal.getLong(ChronoField.NANO_OF_DAY));
            }
            return null;
        }

        public String toString() {
            return "LocalTime";
        }
    };

    private TemporalQueries() {
    }

    public static TemporalQuery<ZoneId> zoneId() {
        return ZONE_ID;
    }

    public static TemporalQuery<Chronology> chronology() {
        return CHRONO;
    }

    public static TemporalQuery<TemporalUnit> precision() {
        return PRECISION;
    }

    public static TemporalQuery<ZoneId> zone() {
        return ZONE;
    }

    public static TemporalQuery<ZoneOffset> offset() {
        return OFFSET;
    }

    public static TemporalQuery<LocalDate> localDate() {
        return LOCAL_DATE;
    }

    public static TemporalQuery<LocalTime> localTime() {
        return LOCAL_TIME;
    }
}

