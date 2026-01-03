/*
 * Decompiled with CFR 0.152.
 */
package java.time.temporal;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import sun.util.locale.provider.CalendarDataUtility;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;

public final class IsoFields {
    public static final TemporalField DAY_OF_QUARTER = Field.DAY_OF_QUARTER;
    public static final TemporalField QUARTER_OF_YEAR = Field.QUARTER_OF_YEAR;
    public static final TemporalField WEEK_OF_WEEK_BASED_YEAR = Field.WEEK_OF_WEEK_BASED_YEAR;
    public static final TemporalField WEEK_BASED_YEAR = Field.WEEK_BASED_YEAR;
    public static final TemporalUnit WEEK_BASED_YEARS = Unit.WEEK_BASED_YEARS;
    public static final TemporalUnit QUARTER_YEARS = Unit.QUARTER_YEARS;

    private IsoFields() {
        throw new AssertionError((Object)"Not instantiable");
    }

    private static void ensureIso(TemporalAccessor temporal) {
        if (!IsoFields.isIso(temporal)) {
            throw new DateTimeException("Resolve requires ISO based chronology: " + Chronology.from(temporal));
        }
    }

    private static boolean isIso(TemporalAccessor temporal) {
        return Chronology.from(temporal).isIsoBased();
    }

    private static enum Field implements TemporalField
    {
        DAY_OF_QUARTER{

            @Override
            public TemporalUnit getBaseUnit() {
                return ChronoUnit.DAYS;
            }

            @Override
            public TemporalUnit getRangeUnit() {
                return QUARTER_YEARS;
            }

            @Override
            public ValueRange range() {
                return ValueRange.of(1L, 90L, 92L);
            }

            @Override
            public boolean isSupportedBy(TemporalAccessor temporal) {
                return temporal.isSupported(ChronoField.DAY_OF_YEAR) && temporal.isSupported(ChronoField.MONTH_OF_YEAR) && temporal.isSupported(ChronoField.YEAR) && IsoFields.isIso(temporal);
            }

            @Override
            public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
                if (!this.isSupportedBy(temporal)) {
                    throw new UnsupportedTemporalTypeException("Unsupported field: DayOfQuarter");
                }
                long qoy = temporal.getLong(QUARTER_OF_YEAR);
                if (qoy == 1L) {
                    long year = temporal.getLong(ChronoField.YEAR);
                    return IsoChronology.INSTANCE.isLeapYear(year) ? ValueRange.of(1L, 91L) : ValueRange.of(1L, 90L);
                }
                if (qoy == 2L) {
                    return ValueRange.of(1L, 91L);
                }
                if (qoy == 3L || qoy == 4L) {
                    return ValueRange.of(1L, 92L);
                }
                return this.range();
            }

            @Override
            public long getFrom(TemporalAccessor temporal) {
                if (!this.isSupportedBy(temporal)) {
                    throw new UnsupportedTemporalTypeException("Unsupported field: DayOfQuarter");
                }
                int doy = temporal.get(ChronoField.DAY_OF_YEAR);
                int moy = temporal.get(ChronoField.MONTH_OF_YEAR);
                long year = temporal.getLong(ChronoField.YEAR);
                return doy - QUARTER_DAYS[(moy - 1) / 3 + (IsoChronology.INSTANCE.isLeapYear(year) ? 4 : 0)];
            }

            @Override
            public <R extends Temporal> R adjustInto(R temporal, long newValue) {
                long curValue = this.getFrom(temporal);
                this.range().checkValidValue(newValue, this);
                return (R)temporal.with(ChronoField.DAY_OF_YEAR, temporal.getLong(ChronoField.DAY_OF_YEAR) + (newValue - curValue));
            }

            @Override
            public ChronoLocalDate resolve(Map<TemporalField, Long> fieldValues, TemporalAccessor partialTemporal, ResolverStyle resolverStyle) {
                LocalDate date;
                Long yearLong = fieldValues.get(ChronoField.YEAR);
                Long qoyLong = fieldValues.get(QUARTER_OF_YEAR);
                if (yearLong == null || qoyLong == null) {
                    return null;
                }
                int y = ChronoField.YEAR.checkValidIntValue(yearLong);
                long doq = fieldValues.get(DAY_OF_QUARTER);
                IsoFields.ensureIso(partialTemporal);
                if (resolverStyle == ResolverStyle.LENIENT) {
                    date = LocalDate.of(y, 1, 1).plusMonths(Math.multiplyExact(Math.subtractExact(qoyLong, 1L), 3));
                    doq = Math.subtractExact(doq, 1L);
                } else {
                    int qoy = QUARTER_OF_YEAR.range().checkValidIntValue(qoyLong, QUARTER_OF_YEAR);
                    date = LocalDate.of(y, (qoy - 1) * 3 + 1, 1);
                    if (doq < 1L || doq > 90L) {
                        if (resolverStyle == ResolverStyle.STRICT) {
                            this.rangeRefinedBy(date).checkValidValue(doq, this);
                        } else {
                            this.range().checkValidValue(doq, this);
                        }
                    }
                    --doq;
                }
                fieldValues.remove(this);
                fieldValues.remove(ChronoField.YEAR);
                fieldValues.remove(QUARTER_OF_YEAR);
                return date.plusDays(doq);
            }

            @Override
            public String toString() {
                return "DayOfQuarter";
            }
        }
        ,
        QUARTER_OF_YEAR{

            @Override
            public TemporalUnit getBaseUnit() {
                return QUARTER_YEARS;
            }

            @Override
            public TemporalUnit getRangeUnit() {
                return ChronoUnit.YEARS;
            }

            @Override
            public ValueRange range() {
                return ValueRange.of(1L, 4L);
            }

            @Override
            public boolean isSupportedBy(TemporalAccessor temporal) {
                return temporal.isSupported(ChronoField.MONTH_OF_YEAR) && IsoFields.isIso(temporal);
            }

            @Override
            public long getFrom(TemporalAccessor temporal) {
                if (!this.isSupportedBy(temporal)) {
                    throw new UnsupportedTemporalTypeException("Unsupported field: QuarterOfYear");
                }
                long moy = temporal.getLong(ChronoField.MONTH_OF_YEAR);
                return (moy + 2L) / 3L;
            }

            @Override
            public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
                if (!this.isSupportedBy(temporal)) {
                    throw new UnsupportedTemporalTypeException("Unsupported field: QuarterOfYear");
                }
                return super.rangeRefinedBy(temporal);
            }

            @Override
            public <R extends Temporal> R adjustInto(R temporal, long newValue) {
                long curValue = this.getFrom(temporal);
                this.range().checkValidValue(newValue, this);
                return (R)temporal.with(ChronoField.MONTH_OF_YEAR, temporal.getLong(ChronoField.MONTH_OF_YEAR) + (newValue - curValue) * 3L);
            }

            @Override
            public String toString() {
                return "QuarterOfYear";
            }
        }
        ,
        WEEK_OF_WEEK_BASED_YEAR{

            @Override
            public String getDisplayName(Locale locale) {
                Objects.requireNonNull(locale, "locale");
                LocaleResources lr = LocaleProviderAdapter.getResourceBundleBased().getLocaleResources(CalendarDataUtility.findRegionOverride(locale));
                ResourceBundle rb = lr.getJavaTimeFormatData();
                return rb.containsKey("field.week") ? rb.getString("field.week") : this.toString();
            }

            @Override
            public TemporalUnit getBaseUnit() {
                return ChronoUnit.WEEKS;
            }

            @Override
            public TemporalUnit getRangeUnit() {
                return WEEK_BASED_YEARS;
            }

            @Override
            public ValueRange range() {
                return ValueRange.of(1L, 52L, 53L);
            }

            @Override
            public boolean isSupportedBy(TemporalAccessor temporal) {
                return temporal.isSupported(ChronoField.EPOCH_DAY) && IsoFields.isIso(temporal);
            }

            @Override
            public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
                if (!this.isSupportedBy(temporal)) {
                    throw new UnsupportedTemporalTypeException("Unsupported field: WeekOfWeekBasedYear");
                }
                return Field.getWeekRange(LocalDate.from(temporal));
            }

            @Override
            public long getFrom(TemporalAccessor temporal) {
                if (!this.isSupportedBy(temporal)) {
                    throw new UnsupportedTemporalTypeException("Unsupported field: WeekOfWeekBasedYear");
                }
                return Field.getWeek(LocalDate.from(temporal));
            }

            @Override
            public <R extends Temporal> R adjustInto(R temporal, long newValue) {
                this.range().checkValidValue(newValue, this);
                return (R)temporal.plus(Math.subtractExact(newValue, this.getFrom(temporal)), ChronoUnit.WEEKS);
            }

            @Override
            public ChronoLocalDate resolve(Map<TemporalField, Long> fieldValues, TemporalAccessor partialTemporal, ResolverStyle resolverStyle) {
                Long wbyLong = fieldValues.get(WEEK_BASED_YEAR);
                Long dowLong = fieldValues.get(ChronoField.DAY_OF_WEEK);
                if (wbyLong == null || dowLong == null) {
                    return null;
                }
                int wby = WEEK_BASED_YEAR.range().checkValidIntValue(wbyLong, WEEK_BASED_YEAR);
                long wowby = fieldValues.get(WEEK_OF_WEEK_BASED_YEAR);
                IsoFields.ensureIso(partialTemporal);
                LocalDate date = LocalDate.of(wby, 1, 4);
                if (resolverStyle == ResolverStyle.LENIENT) {
                    long dow = dowLong;
                    if (dow > 7L) {
                        date = date.plusWeeks((dow - 1L) / 7L);
                        dow = (dow - 1L) % 7L + 1L;
                    } else if (dow < 1L) {
                        date = date.plusWeeks(Math.subtractExact(dow, 7L) / 7L);
                        dow = (dow + 6L) % 7L + 1L;
                    }
                    date = date.plusWeeks(Math.subtractExact(wowby, 1L)).with(ChronoField.DAY_OF_WEEK, dow);
                } else {
                    int dow = ChronoField.DAY_OF_WEEK.checkValidIntValue(dowLong);
                    if (wowby < 1L || wowby > 52L) {
                        if (resolverStyle == ResolverStyle.STRICT) {
                            Field.getWeekRange(date).checkValidValue(wowby, this);
                        } else {
                            this.range().checkValidValue(wowby, this);
                        }
                    }
                    date = date.plusWeeks(wowby - 1L).with(ChronoField.DAY_OF_WEEK, dow);
                }
                fieldValues.remove(this);
                fieldValues.remove(WEEK_BASED_YEAR);
                fieldValues.remove(ChronoField.DAY_OF_WEEK);
                return date;
            }

            @Override
            public String toString() {
                return "WeekOfWeekBasedYear";
            }
        }
        ,
        WEEK_BASED_YEAR{

            @Override
            public TemporalUnit getBaseUnit() {
                return WEEK_BASED_YEARS;
            }

            @Override
            public TemporalUnit getRangeUnit() {
                return ChronoUnit.FOREVER;
            }

            @Override
            public ValueRange range() {
                return ChronoField.YEAR.range();
            }

            @Override
            public boolean isSupportedBy(TemporalAccessor temporal) {
                return temporal.isSupported(ChronoField.EPOCH_DAY) && IsoFields.isIso(temporal);
            }

            @Override
            public long getFrom(TemporalAccessor temporal) {
                if (!this.isSupportedBy(temporal)) {
                    throw new UnsupportedTemporalTypeException("Unsupported field: WeekBasedYear");
                }
                return Field.getWeekBasedYear(LocalDate.from(temporal));
            }

            @Override
            public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
                if (!this.isSupportedBy(temporal)) {
                    throw new UnsupportedTemporalTypeException("Unsupported field: WeekBasedYear");
                }
                ValueRange range = super.rangeRefinedBy(temporal);
                ValueRange chronoRange = Chronology.from(temporal).range(ChronoField.YEAR);
                return ValueRange.of(Math.max(range.getMinimum(), chronoRange.getMinimum()), Math.min(range.getMaximum(), chronoRange.getMaximum()));
            }

            @Override
            public <R extends Temporal> R adjustInto(R temporal, long newValue) {
                if (!this.isSupportedBy(temporal)) {
                    throw new UnsupportedTemporalTypeException("Unsupported field: WeekBasedYear");
                }
                int newWby = this.range().checkValidIntValue(newValue, WEEK_BASED_YEAR);
                LocalDate date = LocalDate.from(temporal);
                int dow = date.get(ChronoField.DAY_OF_WEEK);
                int week = Field.getWeek(date);
                if (week == 53 && Field.getWeekRange(newWby) == 52) {
                    week = 52;
                }
                LocalDate resolved = LocalDate.of(newWby, 1, 4);
                int days = dow - resolved.get(ChronoField.DAY_OF_WEEK) + (week - 1) * 7;
                resolved = resolved.plusDays(days);
                return (R)temporal.with(resolved);
            }

            @Override
            public String toString() {
                return "WeekBasedYear";
            }
        };

        private static final int[] QUARTER_DAYS;

        @Override
        public boolean isDateBased() {
            return true;
        }

        @Override
        public boolean isTimeBased() {
            return false;
        }

        @Override
        public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
            return this.range();
        }

        private static ValueRange getWeekRange(LocalDate date) {
            int wby = Field.getWeekBasedYear(date);
            return ValueRange.of(1L, Field.getWeekRange(wby));
        }

        private static int getWeekRange(int wby) {
            LocalDate date = LocalDate.of(wby, 1, 1);
            if (date.getDayOfWeek() == DayOfWeek.THURSDAY || date.getDayOfWeek() == DayOfWeek.WEDNESDAY && date.isLeapYear()) {
                return 53;
            }
            return 52;
        }

        private static int getWeek(LocalDate date) {
            int alignedWeek;
            int dow0 = date.getDayOfWeek().ordinal();
            int doy0 = date.getDayOfYear() - 1;
            int doyThu0 = doy0 + (3 - dow0);
            int firstThuDoy0 = doyThu0 - (alignedWeek = doyThu0 / 7) * 7;
            int firstMonDoy0 = firstThuDoy0 - 3;
            if (firstMonDoy0 < -3) {
                firstMonDoy0 += 7;
            }
            if (doy0 < firstMonDoy0) {
                return (int)Field.getWeekRange(date.withDayOfYear(180).minusYears(1L)).getMaximum();
            }
            int week = (doy0 - firstMonDoy0) / 7 + 1;
            if (week == 53 && !(firstMonDoy0 == -3 || firstMonDoy0 == -2 && date.isLeapYear())) {
                week = 1;
            }
            return week;
        }

        private static int getWeekBasedYear(LocalDate date) {
            int year = date.getYear();
            int doy = date.getDayOfYear();
            if (doy <= 3) {
                int dow = date.getDayOfWeek().ordinal();
                if (doy - dow < -2) {
                    --year;
                }
            } else if (doy >= 363) {
                int dow = date.getDayOfWeek().ordinal();
                if ((doy = doy - 363 - (date.isLeapYear() ? 1 : 0)) - dow >= 0) {
                    ++year;
                }
            }
            return year;
        }

        static {
            QUARTER_DAYS = new int[]{0, 90, 181, 273, 0, 91, 182, 274};
        }
    }

    private static enum Unit implements TemporalUnit
    {
        WEEK_BASED_YEARS("WeekBasedYears", Duration.ofSeconds(31556952L)),
        QUARTER_YEARS("QuarterYears", Duration.ofSeconds(7889238L));

        private final String name;
        private final Duration duration;

        private Unit(String name, Duration estimatedDuration) {
            this.name = name;
            this.duration = estimatedDuration;
        }

        @Override
        public Duration getDuration() {
            return this.duration;
        }

        @Override
        public boolean isDurationEstimated() {
            return true;
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
        public boolean isSupportedBy(Temporal temporal) {
            return temporal.isSupported(ChronoField.EPOCH_DAY) && IsoFields.isIso(temporal);
        }

        @Override
        public <R extends Temporal> R addTo(R temporal, long amount) {
            return (R)(switch (this.ordinal()) {
                case 0 -> temporal.with(WEEK_BASED_YEAR, Math.addExact((long)temporal.get(WEEK_BASED_YEAR), amount));
                case 1 -> temporal.plus(amount / 4L, ChronoUnit.YEARS).plus(amount % 4L * 3L, ChronoUnit.MONTHS);
                default -> throw new IllegalStateException("Unreachable");
            });
        }

        @Override
        public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
            if (temporal1Inclusive.getClass() != temporal2Exclusive.getClass()) {
                return temporal1Inclusive.until(temporal2Exclusive, this);
            }
            return switch (this.ordinal()) {
                case 0 -> Math.subtractExact(temporal2Exclusive.getLong(WEEK_BASED_YEAR), temporal1Inclusive.getLong(WEEK_BASED_YEAR));
                case 1 -> temporal1Inclusive.until(temporal2Exclusive, ChronoUnit.MONTHS) / 3L;
                default -> throw new IllegalStateException("Unreachable");
            };
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}

