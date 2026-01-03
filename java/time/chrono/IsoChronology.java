/*
 * Decompiled with CFR 0.152.
 */
package java.time.chrono;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.AbstractChronology;
import java.time.chrono.Era;
import java.time.chrono.IsoEra;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.ValueRange;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class IsoChronology
extends AbstractChronology
implements Serializable {
    public static final IsoChronology INSTANCE = new IsoChronology();
    private static final long serialVersionUID = -1440403870442975015L;
    private static final long DAYS_0000_TO_1970 = 719528L;

    private IsoChronology() {
    }

    @Override
    public String getId() {
        return "ISO";
    }

    @Override
    public String getCalendarType() {
        return "iso8601";
    }

    @Override
    public LocalDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
        return this.date(this.prolepticYear(era, yearOfEra), month, dayOfMonth);
    }

    @Override
    public LocalDate date(int prolepticYear, int month, int dayOfMonth) {
        return LocalDate.of(prolepticYear, month, dayOfMonth);
    }

    @Override
    public LocalDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
        return this.dateYearDay(this.prolepticYear(era, yearOfEra), dayOfYear);
    }

    @Override
    public LocalDate dateYearDay(int prolepticYear, int dayOfYear) {
        return LocalDate.ofYearDay(prolepticYear, dayOfYear);
    }

    @Override
    public LocalDate dateEpochDay(long epochDay) {
        return LocalDate.ofEpochDay(epochDay);
    }

    @Override
    public LocalDate date(TemporalAccessor temporal) {
        return LocalDate.from(temporal);
    }

    @Override
    public long epochSecond(int prolepticYear, int month, int dayOfMonth, int hour, int minute, int second, ZoneOffset zoneOffset) {
        int dom;
        ChronoField.YEAR.checkValidValue(prolepticYear);
        ChronoField.MONTH_OF_YEAR.checkValidValue(month);
        ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth);
        ChronoField.HOUR_OF_DAY.checkValidValue(hour);
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
        ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
        Objects.requireNonNull(zoneOffset, "zoneOffset");
        if (dayOfMonth > 28 && dayOfMonth > (dom = this.numberOfDaysOfMonth(prolepticYear, month))) {
            if (dayOfMonth == 29) {
                throw new DateTimeException("Invalid date 'February 29' as '" + prolepticYear + "' is not a leap year");
            }
            throw new DateTimeException("Invalid date '" + Month.of(month).name() + " " + dayOfMonth + "'");
        }
        long totalDays = 0L;
        int timeinSec = 0;
        totalDays += 365L * (long)prolepticYear;
        totalDays = prolepticYear >= 0 ? (totalDays += ((long)prolepticYear + 3L) / 4L - ((long)prolepticYear + 99L) / 100L + ((long)prolepticYear + 399L) / 400L) : (totalDays -= (long)(prolepticYear / -4 - prolepticYear / -100 + prolepticYear / -400));
        totalDays += (long)((367 * month - 362) / 12);
        totalDays += (long)(dayOfMonth - 1);
        if (month > 2) {
            --totalDays;
            if (!INSTANCE.isLeapYear(prolepticYear)) {
                --totalDays;
            }
        }
        timeinSec = (hour * 60 + minute) * 60 + second;
        return Math.addExact(Math.multiplyExact(totalDays -= 719528L, 86400L), (long)(timeinSec - zoneOffset.getTotalSeconds()));
    }

    private int numberOfDaysOfMonth(int year, int month) {
        return switch (month) {
            case 2 -> INSTANCE.isLeapYear(year) ? 29 : 28;
            case 4, 6, 9, 11 -> 30;
            default -> 31;
        };
    }

    public LocalDateTime localDateTime(TemporalAccessor temporal) {
        return LocalDateTime.from(temporal);
    }

    public ZonedDateTime zonedDateTime(TemporalAccessor temporal) {
        return ZonedDateTime.from(temporal);
    }

    public ZonedDateTime zonedDateTime(Instant instant, ZoneId zone) {
        return ZonedDateTime.ofInstant(instant, zone);
    }

    @Override
    public LocalDate dateNow() {
        return this.dateNow(Clock.systemDefaultZone());
    }

    @Override
    public LocalDate dateNow(ZoneId zone) {
        return this.dateNow(Clock.system(zone));
    }

    @Override
    public LocalDate dateNow(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        return this.date(LocalDate.now(clock));
    }

    @Override
    public boolean isLeapYear(long prolepticYear) {
        return (prolepticYear & 3L) == 0L && (prolepticYear % 100L != 0L || prolepticYear % 400L == 0L);
    }

    @Override
    public int prolepticYear(Era era, int yearOfEra) {
        if (!(era instanceof IsoEra)) {
            throw new ClassCastException("Era must be IsoEra");
        }
        return era == IsoEra.CE ? yearOfEra : 1 - yearOfEra;
    }

    @Override
    public IsoEra eraOf(int eraValue) {
        return IsoEra.of(eraValue);
    }

    @Override
    public List<Era> eras() {
        return List.of(IsoEra.values());
    }

    @Override
    public LocalDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        return (LocalDate)super.resolveDate(fieldValues, resolverStyle);
    }

    @Override
    void resolveProlepticMonth(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        Long pMonth = fieldValues.remove(ChronoField.PROLEPTIC_MONTH);
        if (pMonth != null) {
            if (resolverStyle != ResolverStyle.LENIENT) {
                ChronoField.PROLEPTIC_MONTH.checkValidValue(pMonth);
            }
            this.addFieldValue(fieldValues, ChronoField.MONTH_OF_YEAR, Math.floorMod((long)pMonth, 12) + 1);
            this.addFieldValue(fieldValues, ChronoField.YEAR, Math.floorDiv((long)pMonth, 12));
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    LocalDate resolveYearOfEra(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        Long yoeLong = fieldValues.remove(ChronoField.YEAR_OF_ERA);
        if (yoeLong != null) {
            Long era;
            if (resolverStyle != ResolverStyle.LENIENT) {
                ChronoField.YEAR_OF_ERA.checkValidValue(yoeLong);
            }
            if ((era = fieldValues.remove(ChronoField.ERA)) == null) {
                Long year = fieldValues.get(ChronoField.YEAR);
                if (resolverStyle == ResolverStyle.STRICT) {
                    if (year != null) {
                        this.addFieldValue(fieldValues, ChronoField.YEAR, year > 0L ? yoeLong : Math.subtractExact(1L, yoeLong));
                        return null;
                    } else {
                        fieldValues.put(ChronoField.YEAR_OF_ERA, yoeLong);
                    }
                    return null;
                } else {
                    this.addFieldValue(fieldValues, ChronoField.YEAR, year == null || year > 0L ? yoeLong : Math.subtractExact(1L, yoeLong));
                }
                return null;
            } else if (era == 1L) {
                this.addFieldValue(fieldValues, ChronoField.YEAR, yoeLong);
                return null;
            } else {
                if (era != 0L) throw new DateTimeException("Invalid value for era: " + era);
                this.addFieldValue(fieldValues, ChronoField.YEAR, Math.subtractExact(1L, yoeLong));
            }
            return null;
        } else {
            if (!fieldValues.containsKey(ChronoField.ERA)) return null;
            ChronoField.ERA.checkValidValue(fieldValues.get(ChronoField.ERA));
        }
        return null;
    }

    @Override
    LocalDate resolveYMD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = ChronoField.YEAR.checkValidIntValue(fieldValues.remove(ChronoField.YEAR));
        if (resolverStyle == ResolverStyle.LENIENT) {
            long months = Math.subtractExact(fieldValues.remove(ChronoField.MONTH_OF_YEAR), 1L);
            long days = Math.subtractExact(fieldValues.remove(ChronoField.DAY_OF_MONTH), 1L);
            return LocalDate.of(y, 1, 1).plusMonths(months).plusDays(days);
        }
        int moy = ChronoField.MONTH_OF_YEAR.checkValidIntValue(fieldValues.remove(ChronoField.MONTH_OF_YEAR));
        int dom = ChronoField.DAY_OF_MONTH.checkValidIntValue(fieldValues.remove(ChronoField.DAY_OF_MONTH));
        if (resolverStyle == ResolverStyle.SMART) {
            if (moy == 4 || moy == 6 || moy == 9 || moy == 11) {
                dom = Math.min(dom, 30);
            } else if (moy == 2) {
                dom = Math.min(dom, Month.FEBRUARY.length(Year.isLeap(y)));
            }
        }
        return LocalDate.of(y, moy, dom);
    }

    @Override
    public ValueRange range(ChronoField field) {
        return field.range();
    }

    @Override
    public Period period(int years, int months, int days) {
        return Period.of(years, months, days);
    }

    @Override
    public boolean isIsoBased() {
        return true;
    }

    @Override
    Object writeReplace() {
        return super.writeReplace();
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }
}

