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
import java.time.Clock;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Ser;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.IsoChronology;
import java.time.chrono.IsoEra;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.Objects;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import jdk.internal.ValueBased;

@ValueBased
public final class LocalDate
implements Temporal,
TemporalAdjuster,
ChronoLocalDate,
Serializable {
    public static final LocalDate MIN = LocalDate.of(-999999999, 1, 1);
    public static final LocalDate MAX = LocalDate.of(999999999, 12, 31);
    public static final LocalDate EPOCH = LocalDate.of(1970, 1, 1);
    private static final long serialVersionUID = 2942565459149668126L;
    private static final int DAYS_PER_CYCLE = 146097;
    static final long DAYS_0000_TO_1970 = 719528L;
    private final int year;
    private final short month;
    private final short day;

    public static LocalDate now() {
        return LocalDate.now(Clock.systemDefaultZone());
    }

    public static LocalDate now(ZoneId zone) {
        return LocalDate.now(Clock.system(zone));
    }

    public static LocalDate now(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        Instant now = clock.instant();
        return LocalDate.ofInstant(now, clock.getZone());
    }

    public static LocalDate of(int year, Month month, int dayOfMonth) {
        ChronoField.YEAR.checkValidValue(year);
        Objects.requireNonNull(month, "month");
        ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth);
        return LocalDate.create(year, month.getValue(), dayOfMonth);
    }

    public static LocalDate of(int year, int month, int dayOfMonth) {
        ChronoField.YEAR.checkValidValue(year);
        ChronoField.MONTH_OF_YEAR.checkValidValue(month);
        ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth);
        return LocalDate.create(year, month, dayOfMonth);
    }

    public static LocalDate ofYearDay(int year, int dayOfYear) {
        ChronoField.YEAR.checkValidValue(year);
        ChronoField.DAY_OF_YEAR.checkValidValue(dayOfYear);
        boolean leap = IsoChronology.INSTANCE.isLeapYear(year);
        if (dayOfYear == 366 && !leap) {
            throw new DateTimeException("Invalid date 'DayOfYear 366' as '" + year + "' is not a leap year");
        }
        Month moy = Month.of((dayOfYear - 1) / 31 + 1);
        int monthEnd = moy.firstDayOfYear(leap) + moy.length(leap) - 1;
        if (dayOfYear > monthEnd) {
            moy = moy.plus(1L);
        }
        int dom = dayOfYear - moy.firstDayOfYear(leap) + 1;
        return new LocalDate(year, moy.getValue(), dom);
    }

    public static LocalDate ofInstant(Instant instant, ZoneId zone) {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(zone, "zone");
        ZoneRules rules = zone.getRules();
        ZoneOffset offset = rules.getOffset(instant);
        long localSecond = instant.getEpochSecond() + (long)offset.getTotalSeconds();
        long localEpochDay = Math.floorDiv(localSecond, 86400);
        return LocalDate.ofEpochDay(localEpochDay);
    }

    public static LocalDate ofEpochDay(long epochDay) {
        long yearEst;
        long doyEst;
        ChronoField.EPOCH_DAY.checkValidValue(epochDay);
        long zeroDay = epochDay + 719528L;
        long adjust = 0L;
        if ((zeroDay -= 60L) < 0L) {
            long adjustCycles = (zeroDay + 1L) / 146097L - 1L;
            adjust = adjustCycles * 400L;
            zeroDay += -adjustCycles * 146097L;
        }
        if ((doyEst = zeroDay - (365L * (yearEst = (400L * zeroDay + 591L) / 146097L) + yearEst / 4L - yearEst / 100L + yearEst / 400L)) < 0L) {
            doyEst = zeroDay - (365L * --yearEst + yearEst / 4L - yearEst / 100L + yearEst / 400L);
        }
        yearEst += adjust;
        int marchDoy0 = (int)doyEst;
        int marchMonth0 = (marchDoy0 * 5 + 2) / 153;
        int month = marchMonth0 + 3;
        if (month > 12) {
            month -= 12;
        }
        int dom = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1;
        if (marchDoy0 >= 306) {
            ++yearEst;
        }
        return new LocalDate((int)yearEst, month, dom);
    }

    public static LocalDate from(TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "temporal");
        LocalDate date = temporal.query(TemporalQueries.localDate());
        if (date == null) {
            throw new DateTimeException("Unable to obtain LocalDate from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName());
        }
        return date;
    }

    public static LocalDate parse(CharSequence text) {
        return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static LocalDate parse(CharSequence text, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.parse(text, LocalDate::from);
    }

    private static LocalDate create(int year, int month, int dayOfMonth) {
        if (dayOfMonth > 28) {
            int dom;
            switch (month) {
                case 2: {
                    int n;
                    if (IsoChronology.INSTANCE.isLeapYear(year)) {
                        n = 29;
                        break;
                    }
                    n = 28;
                    break;
                }
                case 4: 
                case 6: 
                case 9: 
                case 11: {
                    int n = 30;
                    break;
                }
                default: {
                    int n = dom = 31;
                }
            }
            if (dayOfMonth > dom) {
                if (dayOfMonth == 29) {
                    throw new DateTimeException("Invalid date 'February 29' as '" + year + "' is not a leap year");
                }
                throw new DateTimeException("Invalid date '" + Month.of(month).name() + " " + dayOfMonth + "'");
            }
        }
        return new LocalDate(year, month, dayOfMonth);
    }

    private static LocalDate resolvePreviousValid(int year, int month, int day) {
        switch (month) {
            case 2: {
                day = Math.min(day, IsoChronology.INSTANCE.isLeapYear(year) ? 29 : 28);
                break;
            }
            case 4: 
            case 6: 
            case 9: 
            case 11: {
                day = Math.min(day, 30);
            }
        }
        return new LocalDate(year, month, day);
    }

    private LocalDate(int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = (short)month;
        this.day = (short)dayOfMonth;
    }

    @Override
    public boolean isSupported(TemporalField field) {
        return ChronoLocalDate.super.isSupported(field);
    }

    @Override
    public boolean isSupported(TemporalUnit unit) {
        return ChronoLocalDate.super.isSupported(unit);
    }

    @Override
    public ValueRange range(TemporalField field) {
        if (field instanceof ChronoField) {
            ChronoField chronoField = (ChronoField)field;
            if (chronoField.isDateBased()) {
                return switch (chronoField) {
                    case ChronoField.DAY_OF_MONTH -> ValueRange.of(1L, this.lengthOfMonth());
                    case ChronoField.DAY_OF_YEAR -> ValueRange.of(1L, this.lengthOfYear());
                    case ChronoField.ALIGNED_WEEK_OF_MONTH -> ValueRange.of(1L, this.getMonth() == Month.FEBRUARY && !this.isLeapYear() ? 4L : 5L);
                    case ChronoField.YEAR_OF_ERA -> {
                        if (this.getYear() <= 0) {
                            yield ValueRange.of(1L, 1000000000L);
                        }
                        yield ValueRange.of(1L, 999999999L);
                    }
                    default -> field.range();
                };
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return field.rangeRefinedBy(this);
    }

    @Override
    public int get(TemporalField field) {
        if (field instanceof ChronoField) {
            return this.get0(field);
        }
        return ChronoLocalDate.super.get(field);
    }

    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            if (field == ChronoField.EPOCH_DAY) {
                return this.toEpochDay();
            }
            if (field == ChronoField.PROLEPTIC_MONTH) {
                return this.getProlepticMonth();
            }
            return this.get0(field);
        }
        return field.getFrom(this);
    }

    private int get0(TemporalField field) {
        return switch ((ChronoField)field) {
            case ChronoField.DAY_OF_WEEK -> this.getDayOfWeek().getValue();
            case ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH -> (this.day - 1) % 7 + 1;
            case ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR -> (this.getDayOfYear() - 1) % 7 + 1;
            case ChronoField.DAY_OF_MONTH -> this.day;
            case ChronoField.DAY_OF_YEAR -> this.getDayOfYear();
            case ChronoField.EPOCH_DAY -> throw new UnsupportedTemporalTypeException("Invalid field 'EpochDay' for get() method, use getLong() instead");
            case ChronoField.ALIGNED_WEEK_OF_MONTH -> (this.day - 1) / 7 + 1;
            case ChronoField.ALIGNED_WEEK_OF_YEAR -> (this.getDayOfYear() - 1) / 7 + 1;
            case ChronoField.MONTH_OF_YEAR -> this.month;
            case ChronoField.PROLEPTIC_MONTH -> throw new UnsupportedTemporalTypeException("Invalid field 'ProlepticMonth' for get() method, use getLong() instead");
            case ChronoField.YEAR_OF_ERA -> {
                if (this.year >= 1) {
                    yield this.year;
                }
                yield 1 - this.year;
            }
            case ChronoField.YEAR -> this.year;
            case ChronoField.ERA -> {
                if (this.year >= 1) {
                    yield 1;
                }
                yield 0;
            }
            default -> throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        };
    }

    private long getProlepticMonth() {
        return (long)this.year * 12L + (long)this.month - 1L;
    }

    @Override
    public IsoChronology getChronology() {
        return IsoChronology.INSTANCE;
    }

    @Override
    public IsoEra getEra() {
        return this.getYear() >= 1 ? IsoEra.CE : IsoEra.BCE;
    }

    public int getYear() {
        return this.year;
    }

    public int getMonthValue() {
        return this.month;
    }

    public Month getMonth() {
        return Month.of(this.month);
    }

    public int getDayOfMonth() {
        return this.day;
    }

    public int getDayOfYear() {
        return this.getMonth().firstDayOfYear(this.isLeapYear()) + this.day - 1;
    }

    public DayOfWeek getDayOfWeek() {
        int dow0 = Math.floorMod(this.toEpochDay() + 3L, 7);
        return DayOfWeek.of(dow0 + 1);
    }

    @Override
    public boolean isLeapYear() {
        return IsoChronology.INSTANCE.isLeapYear(this.year);
    }

    @Override
    public int lengthOfMonth() {
        return switch (this.month) {
            case 2 -> {
                if (this.isLeapYear()) {
                    yield 29;
                }
                yield 28;
            }
            case 4, 6, 9, 11 -> 30;
            default -> 31;
        };
    }

    @Override
    public int lengthOfYear() {
        return this.isLeapYear() ? 366 : 365;
    }

    @Override
    public LocalDate with(TemporalAdjuster adjuster) {
        if (adjuster instanceof LocalDate) {
            return (LocalDate)adjuster;
        }
        return (LocalDate)adjuster.adjustInto(this);
    }

    @Override
    public LocalDate with(TemporalField field, long newValue) {
        if (field instanceof ChronoField) {
            ChronoField chronoField = (ChronoField)field;
            chronoField.checkValidValue(newValue);
            return switch (chronoField) {
                case ChronoField.DAY_OF_WEEK -> this.plusDays(newValue - (long)this.getDayOfWeek().getValue());
                case ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH -> this.plusDays(newValue - this.getLong(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH));
                case ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR -> this.plusDays(newValue - this.getLong(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR));
                case ChronoField.DAY_OF_MONTH -> this.withDayOfMonth((int)newValue);
                case ChronoField.DAY_OF_YEAR -> this.withDayOfYear((int)newValue);
                case ChronoField.EPOCH_DAY -> LocalDate.ofEpochDay(newValue);
                case ChronoField.ALIGNED_WEEK_OF_MONTH -> this.plusWeeks(newValue - this.getLong(ChronoField.ALIGNED_WEEK_OF_MONTH));
                case ChronoField.ALIGNED_WEEK_OF_YEAR -> this.plusWeeks(newValue - this.getLong(ChronoField.ALIGNED_WEEK_OF_YEAR));
                case ChronoField.MONTH_OF_YEAR -> this.withMonth((int)newValue);
                case ChronoField.PROLEPTIC_MONTH -> this.plusMonths(newValue - this.getProlepticMonth());
                case ChronoField.YEAR_OF_ERA -> this.withYear((int)(this.year >= 1 ? newValue : 1L - newValue));
                case ChronoField.YEAR -> this.withYear((int)newValue);
                case ChronoField.ERA -> {
                    if (this.getLong(ChronoField.ERA) == newValue) {
                        yield this;
                    }
                    yield this.withYear(1 - this.year);
                }
                default -> throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            };
        }
        return field.adjustInto(this, newValue);
    }

    public LocalDate withYear(int year) {
        if (this.year == year) {
            return this;
        }
        ChronoField.YEAR.checkValidValue(year);
        return LocalDate.resolvePreviousValid(year, this.month, this.day);
    }

    public LocalDate withMonth(int month) {
        if (this.month == month) {
            return this;
        }
        ChronoField.MONTH_OF_YEAR.checkValidValue(month);
        return LocalDate.resolvePreviousValid(this.year, month, this.day);
    }

    public LocalDate withDayOfMonth(int dayOfMonth) {
        if (this.day == dayOfMonth) {
            return this;
        }
        return LocalDate.of(this.year, this.month, dayOfMonth);
    }

    public LocalDate withDayOfYear(int dayOfYear) {
        if (this.getDayOfYear() == dayOfYear) {
            return this;
        }
        return LocalDate.ofYearDay(this.year, dayOfYear);
    }

    @Override
    public LocalDate plus(TemporalAmount amountToAdd) {
        if (amountToAdd instanceof Period) {
            Period periodToAdd = (Period)amountToAdd;
            return this.plusMonths(periodToAdd.toTotalMonths()).plusDays(periodToAdd.getDays());
        }
        Objects.requireNonNull(amountToAdd, "amountToAdd");
        return (LocalDate)amountToAdd.addTo(this);
    }

    @Override
    public LocalDate plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            ChronoUnit chronoUnit = (ChronoUnit)unit;
            return switch (chronoUnit) {
                case ChronoUnit.DAYS -> this.plusDays(amountToAdd);
                case ChronoUnit.WEEKS -> this.plusWeeks(amountToAdd);
                case ChronoUnit.MONTHS -> this.plusMonths(amountToAdd);
                case ChronoUnit.YEARS -> this.plusYears(amountToAdd);
                case ChronoUnit.DECADES -> this.plusYears(Math.multiplyExact(amountToAdd, 10));
                case ChronoUnit.CENTURIES -> this.plusYears(Math.multiplyExact(amountToAdd, 100));
                case ChronoUnit.MILLENNIA -> this.plusYears(Math.multiplyExact(amountToAdd, 1000));
                case ChronoUnit.ERAS -> this.with(ChronoField.ERA, Math.addExact(this.getLong(ChronoField.ERA), amountToAdd));
                default -> throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
            };
        }
        return unit.addTo(this, amountToAdd);
    }

    public LocalDate plusYears(long yearsToAdd) {
        if (yearsToAdd == 0L) {
            return this;
        }
        int newYear = ChronoField.YEAR.checkValidIntValue((long)this.year + yearsToAdd);
        return LocalDate.resolvePreviousValid(newYear, this.month, this.day);
    }

    public LocalDate plusMonths(long monthsToAdd) {
        if (monthsToAdd == 0L) {
            return this;
        }
        long monthCount = (long)this.year * 12L + (long)(this.month - 1);
        long calcMonths = monthCount + monthsToAdd;
        int newYear = ChronoField.YEAR.checkValidIntValue(Math.floorDiv(calcMonths, 12));
        int newMonth = Math.floorMod(calcMonths, 12) + 1;
        return LocalDate.resolvePreviousValid(newYear, newMonth, this.day);
    }

    public LocalDate plusWeeks(long weeksToAdd) {
        return this.plusDays(Math.multiplyExact(weeksToAdd, 7));
    }

    public LocalDate plusDays(long daysToAdd) {
        if (daysToAdd == 0L) {
            return this;
        }
        long dom = (long)this.day + daysToAdd;
        if (dom > 0L) {
            if (dom <= 28L) {
                return new LocalDate(this.year, this.month, (int)dom);
            }
            if (dom <= 59L) {
                long monthLen = this.lengthOfMonth();
                if (dom <= monthLen) {
                    return new LocalDate(this.year, this.month, (int)dom);
                }
                if (this.month < 12) {
                    return new LocalDate(this.year, this.month + 1, (int)(dom - monthLen));
                }
                ChronoField.YEAR.checkValidValue(this.year + 1);
                return new LocalDate(this.year + 1, 1, (int)(dom - monthLen));
            }
        }
        long mjDay = Math.addExact(this.toEpochDay(), daysToAdd);
        return LocalDate.ofEpochDay(mjDay);
    }

    @Override
    public LocalDate minus(TemporalAmount amountToSubtract) {
        if (amountToSubtract instanceof Period) {
            Period periodToSubtract = (Period)amountToSubtract;
            return this.minusMonths(periodToSubtract.toTotalMonths()).minusDays(periodToSubtract.getDays());
        }
        Objects.requireNonNull(amountToSubtract, "amountToSubtract");
        return (LocalDate)amountToSubtract.subtractFrom(this);
    }

    @Override
    public LocalDate minus(long amountToSubtract, TemporalUnit unit) {
        return amountToSubtract == Long.MIN_VALUE ? this.plus(Long.MAX_VALUE, unit).plus(1L, unit) : this.plus(-amountToSubtract, unit);
    }

    public LocalDate minusYears(long yearsToSubtract) {
        return yearsToSubtract == Long.MIN_VALUE ? this.plusYears(Long.MAX_VALUE).plusYears(1L) : this.plusYears(-yearsToSubtract);
    }

    public LocalDate minusMonths(long monthsToSubtract) {
        return monthsToSubtract == Long.MIN_VALUE ? this.plusMonths(Long.MAX_VALUE).plusMonths(1L) : this.plusMonths(-monthsToSubtract);
    }

    public LocalDate minusWeeks(long weeksToSubtract) {
        return weeksToSubtract == Long.MIN_VALUE ? this.plusWeeks(Long.MAX_VALUE).plusWeeks(1L) : this.plusWeeks(-weeksToSubtract);
    }

    public LocalDate minusDays(long daysToSubtract) {
        return daysToSubtract == Long.MIN_VALUE ? this.plusDays(Long.MAX_VALUE).plusDays(1L) : this.plusDays(-daysToSubtract);
    }

    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.localDate()) {
            return (R)this;
        }
        return ChronoLocalDate.super.query(query);
    }

    @Override
    public Temporal adjustInto(Temporal temporal) {
        return ChronoLocalDate.super.adjustInto(temporal);
    }

    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        LocalDate end = LocalDate.from(endExclusive);
        if (unit instanceof ChronoUnit) {
            ChronoUnit chronoUnit = (ChronoUnit)unit;
            return switch (chronoUnit) {
                case ChronoUnit.DAYS -> this.daysUntil(end);
                case ChronoUnit.WEEKS -> this.daysUntil(end) / 7L;
                case ChronoUnit.MONTHS -> this.monthsUntil(end);
                case ChronoUnit.YEARS -> this.monthsUntil(end) / 12L;
                case ChronoUnit.DECADES -> this.monthsUntil(end) / 120L;
                case ChronoUnit.CENTURIES -> this.monthsUntil(end) / 1200L;
                case ChronoUnit.MILLENNIA -> this.monthsUntil(end) / 12000L;
                case ChronoUnit.ERAS -> end.getLong(ChronoField.ERA) - this.getLong(ChronoField.ERA);
                default -> throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
            };
        }
        return unit.between(this, end);
    }

    long daysUntil(LocalDate end) {
        return end.toEpochDay() - this.toEpochDay();
    }

    private long monthsUntil(LocalDate end) {
        long packed1 = this.getProlepticMonth() * 32L + (long)this.getDayOfMonth();
        long packed2 = end.getProlepticMonth() * 32L + (long)end.getDayOfMonth();
        return (packed2 - packed1) / 32L;
    }

    @Override
    public Period until(ChronoLocalDate endDateExclusive) {
        LocalDate end = LocalDate.from(endDateExclusive);
        long totalMonths = end.getProlepticMonth() - this.getProlepticMonth();
        int days = end.day - this.day;
        if (totalMonths > 0L && days < 0) {
            LocalDate calcDate = this.plusMonths(--totalMonths);
            days = (int)(end.toEpochDay() - calcDate.toEpochDay());
        } else if (totalMonths < 0L && days > 0) {
            ++totalMonths;
            days -= end.lengthOfMonth();
        }
        long years = totalMonths / 12L;
        int months = (int)(totalMonths % 12L);
        return Period.of(Math.toIntExact(years), months, days);
    }

    public Stream<LocalDate> datesUntil(LocalDate endExclusive) {
        long start;
        long end = endExclusive.toEpochDay();
        if (end < (start = this.toEpochDay())) {
            throw new IllegalArgumentException(endExclusive + " < " + this);
        }
        return LongStream.range(start, end).mapToObj(LocalDate::ofEpochDay);
    }

    public Stream<LocalDate> datesUntil(LocalDate endExclusive, Period step) {
        long maxAddMonths;
        if (step.isZero()) {
            throw new IllegalArgumentException("step is zero");
        }
        long end = endExclusive.toEpochDay();
        long start = this.toEpochDay();
        long until = end - start;
        long months = step.toTotalMonths();
        long days = step.getDays();
        if (months < 0L && days > 0L || months > 0L && days < 0L) {
            throw new IllegalArgumentException("period months and days are of opposite sign");
        }
        if (until == 0L) {
            return Stream.empty();
        }
        int sign = months > 0L || days > 0L ? 1 : -1;
        if (sign < 0 ^ until < 0L) {
            throw new IllegalArgumentException(endExclusive + (sign < 0 ? " > " : " < ") + this);
        }
        if (months == 0L) {
            long steps = (until - (long)sign) / days;
            return LongStream.rangeClosed(0L, steps).mapToObj(n -> LocalDate.ofEpochDay(start + n * days));
        }
        long steps = until * 1600L / (months * 48699L + days * 1600L) + 1L;
        long addMonths = months * steps;
        long addDays = days * steps;
        long l = maxAddMonths = months > 0L ? MAX.getProlepticMonth() - this.getProlepticMonth() : this.getProlepticMonth() - MIN.getProlepticMonth();
        if (addMonths * (long)sign > maxAddMonths || (this.plusMonths(addMonths).toEpochDay() + addDays) * (long)sign >= end * (long)sign) {
            --steps;
            if ((addMonths -= months) * (long)sign > maxAddMonths || (this.plusMonths(addMonths).toEpochDay() + (addDays -= days)) * (long)sign >= end * (long)sign) {
                --steps;
            }
        }
        return LongStream.rangeClosed(0L, steps).mapToObj(n -> this.plusMonths(months * n).plusDays(days * n));
    }

    @Override
    public String format(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.format(this);
    }

    public LocalDateTime atTime(LocalTime time) {
        return LocalDateTime.of(this, time);
    }

    public LocalDateTime atTime(int hour, int minute) {
        return this.atTime(LocalTime.of(hour, minute));
    }

    public LocalDateTime atTime(int hour, int minute, int second) {
        return this.atTime(LocalTime.of(hour, minute, second));
    }

    public LocalDateTime atTime(int hour, int minute, int second, int nanoOfSecond) {
        return this.atTime(LocalTime.of(hour, minute, second, nanoOfSecond));
    }

    public OffsetDateTime atTime(OffsetTime time) {
        return OffsetDateTime.of(LocalDateTime.of(this, time.toLocalTime()), time.getOffset());
    }

    public LocalDateTime atStartOfDay() {
        return LocalDateTime.of(this, LocalTime.MIDNIGHT);
    }

    public ZonedDateTime atStartOfDay(ZoneId zone) {
        ZoneRules rules;
        ZoneOffsetTransition trans;
        Objects.requireNonNull(zone, "zone");
        LocalDateTime ldt = this.atTime(LocalTime.MIDNIGHT);
        if (!(zone instanceof ZoneOffset) && (trans = (rules = zone.getRules()).getTransition(ldt)) != null && trans.isGap()) {
            ldt = trans.getDateTimeAfter();
        }
        return ZonedDateTime.of(ldt, zone);
    }

    @Override
    public long toEpochDay() {
        long y = this.year;
        long m = this.month;
        long total = 0L;
        total += 365L * y;
        total = y >= 0L ? (total += (y + 3L) / 4L - (y + 99L) / 100L + (y + 399L) / 400L) : (total -= y / -4L - y / -100L + y / -400L);
        total += (367L * m - 362L) / 12L;
        total += (long)(this.day - 1);
        if (m > 2L) {
            --total;
            if (!this.isLeapYear()) {
                --total;
            }
        }
        return total - 719528L;
    }

    public long toEpochSecond(LocalTime time, ZoneOffset offset) {
        Objects.requireNonNull(time, "time");
        Objects.requireNonNull(offset, "offset");
        long secs = this.toEpochDay() * 86400L + (long)time.toSecondOfDay();
        return secs -= (long)offset.getTotalSeconds();
    }

    @Override
    public int compareTo(ChronoLocalDate other) {
        if (other instanceof LocalDate) {
            return this.compareTo0((LocalDate)other);
        }
        return ChronoLocalDate.super.compareTo(other);
    }

    int compareTo0(LocalDate otherDate) {
        int cmp = this.year - otherDate.year;
        if (cmp == 0 && (cmp = this.month - otherDate.month) == 0) {
            cmp = this.day - otherDate.day;
        }
        return cmp;
    }

    @Override
    public boolean isAfter(ChronoLocalDate other) {
        if (other instanceof LocalDate) {
            return this.compareTo0((LocalDate)other) > 0;
        }
        return ChronoLocalDate.super.isAfter(other);
    }

    @Override
    public boolean isBefore(ChronoLocalDate other) {
        if (other instanceof LocalDate) {
            return this.compareTo0((LocalDate)other) < 0;
        }
        return ChronoLocalDate.super.isBefore(other);
    }

    @Override
    public boolean isEqual(ChronoLocalDate other) {
        if (other instanceof LocalDate) {
            return this.compareTo0((LocalDate)other) == 0;
        }
        return ChronoLocalDate.super.isEqual(other);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LocalDate) {
            return this.compareTo0((LocalDate)obj) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int yearValue = this.year;
        short monthValue = this.month;
        short dayValue = this.day;
        return yearValue & 0xFFFFF800 ^ (yearValue << 11) + (monthValue << 6) + dayValue;
    }

    @Override
    public String toString() {
        int yearValue = this.year;
        short monthValue = this.month;
        short dayValue = this.day;
        int absYear = Math.abs(yearValue);
        StringBuilder buf = new StringBuilder(10);
        if (absYear < 1000) {
            if (yearValue < 0) {
                buf.append(yearValue - 10000).deleteCharAt(1);
            } else {
                buf.append(yearValue + 10000).deleteCharAt(0);
            }
        } else {
            if (yearValue > 9999) {
                buf.append('+');
            }
            buf.append(yearValue);
        }
        return buf.append(monthValue < 10 ? "-0" : "-").append(monthValue).append(dayValue < 10 ? "-0" : "-").append(dayValue).toString();
    }

    private Object writeReplace() {
        return new Ser(3, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeInt(this.year);
        out.writeByte(this.month);
        out.writeByte(this.day);
    }

    static LocalDate readExternal(DataInput in) throws IOException {
        int year = in.readInt();
        byte month = in.readByte();
        byte dayOfMonth = in.readByte();
        return LocalDate.of(year, month, (int)dayOfMonth);
    }
}

