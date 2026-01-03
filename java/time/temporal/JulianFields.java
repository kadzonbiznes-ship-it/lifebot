/*
 * Decompiled with CFR 0.152.
 */
package java.time.temporal;

import java.time.DateTimeException;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.ValueRange;
import java.util.Map;

public final class JulianFields {
    private static final long JULIAN_DAY_OFFSET = 2440588L;
    public static final TemporalField JULIAN_DAY = Field.JULIAN_DAY;
    public static final TemporalField MODIFIED_JULIAN_DAY = Field.MODIFIED_JULIAN_DAY;
    public static final TemporalField RATA_DIE = Field.RATA_DIE;

    private JulianFields() {
        throw new AssertionError((Object)"Not instantiable");
    }

    private static enum Field implements TemporalField
    {
        JULIAN_DAY("JulianDay", ChronoUnit.DAYS, ChronoUnit.FOREVER, 2440588L),
        MODIFIED_JULIAN_DAY("ModifiedJulianDay", ChronoUnit.DAYS, ChronoUnit.FOREVER, 40587L),
        RATA_DIE("RataDie", ChronoUnit.DAYS, ChronoUnit.FOREVER, 719163L);

        private final transient String name;
        private final transient TemporalUnit baseUnit;
        private final transient TemporalUnit rangeUnit;
        private final transient ValueRange range;
        private final transient long offset;

        private Field(String name, TemporalUnit baseUnit, TemporalUnit rangeUnit, long offset) {
            this.name = name;
            this.baseUnit = baseUnit;
            this.rangeUnit = rangeUnit;
            this.range = ValueRange.of(-365243219162L + offset, 365241780471L + offset);
            this.offset = offset;
        }

        @Override
        public TemporalUnit getBaseUnit() {
            return this.baseUnit;
        }

        @Override
        public TemporalUnit getRangeUnit() {
            return this.rangeUnit;
        }

        @Override
        public boolean isDateBased() {
            return true;
        }

        @Override
        public boolean isTimeBased() {
            return false;
        }

        @Override
        public ValueRange range() {
            return this.range;
        }

        @Override
        public boolean isSupportedBy(TemporalAccessor temporal) {
            return temporal.isSupported(ChronoField.EPOCH_DAY);
        }

        @Override
        public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
            if (!this.isSupportedBy(temporal)) {
                throw new DateTimeException("Unsupported field: " + this);
            }
            return this.range();
        }

        @Override
        public long getFrom(TemporalAccessor temporal) {
            return temporal.getLong(ChronoField.EPOCH_DAY) + this.offset;
        }

        @Override
        public <R extends Temporal> R adjustInto(R temporal, long newValue) {
            if (!this.range().isValidValue(newValue)) {
                throw new DateTimeException("Invalid value: " + this.name + " " + newValue);
            }
            return (R)temporal.with(ChronoField.EPOCH_DAY, Math.subtractExact(newValue, this.offset));
        }

        @Override
        public ChronoLocalDate resolve(Map<TemporalField, Long> fieldValues, TemporalAccessor partialTemporal, ResolverStyle resolverStyle) {
            long value = fieldValues.remove(this);
            Chronology chrono = Chronology.from(partialTemporal);
            if (resolverStyle == ResolverStyle.LENIENT) {
                return chrono.dateEpochDay(Math.subtractExact(value, this.offset));
            }
            this.range().checkValidValue(value, this);
            return chrono.dateEpochDay(value - this.offset);
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}

