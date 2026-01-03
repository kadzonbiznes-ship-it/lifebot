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
import java.time.LocalDate;
import java.time.Ser;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jdk.internal.ValueBased;

@ValueBased
public final class Period
implements ChronoPeriod,
Serializable {
    public static final Period ZERO = new Period(0, 0, 0);
    private static final long serialVersionUID = -3587258372562876L;
    private static final Pattern PATTERN = Pattern.compile("([-+]?)P(?:([-+]?[0-9]+)Y)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)W)?(?:([-+]?[0-9]+)D)?", 2);
    private static final List<TemporalUnit> SUPPORTED_UNITS = List.of(ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.DAYS);
    private final int years;
    private final int months;
    private final int days;

    public static Period ofYears(int years) {
        return Period.create(years, 0, 0);
    }

    public static Period ofMonths(int months) {
        return Period.create(0, months, 0);
    }

    public static Period ofWeeks(int weeks) {
        return Period.create(0, 0, Math.multiplyExact(weeks, 7));
    }

    public static Period ofDays(int days) {
        return Period.create(0, 0, days);
    }

    public static Period of(int years, int months, int days) {
        return Period.create(years, months, days);
    }

    public static Period from(TemporalAmount amount) {
        if (amount instanceof Period) {
            return (Period)amount;
        }
        if (amount instanceof ChronoPeriod && !IsoChronology.INSTANCE.equals(((ChronoPeriod)amount).getChronology())) {
            throw new DateTimeException("Period requires ISO chronology: " + amount);
        }
        Objects.requireNonNull(amount, "amount");
        int years = 0;
        int months = 0;
        int days = 0;
        for (TemporalUnit unit : amount.getUnits()) {
            long unitAmount = amount.get(unit);
            if (unit == ChronoUnit.YEARS) {
                years = Math.toIntExact(unitAmount);
                continue;
            }
            if (unit == ChronoUnit.MONTHS) {
                months = Math.toIntExact(unitAmount);
                continue;
            }
            if (unit == ChronoUnit.DAYS) {
                days = Math.toIntExact(unitAmount);
                continue;
            }
            throw new DateTimeException("Unit must be Years, Months or Days, but was " + unit);
        }
        return Period.create(years, months, days);
    }

    public static Period parse(CharSequence text) {
        Objects.requireNonNull(text, "text");
        Matcher matcher = PATTERN.matcher(text);
        if (matcher.matches()) {
            int negate = Period.charMatch(text, matcher.start(1), matcher.end(1), '-') ? -1 : 1;
            int yearStart = matcher.start(2);
            int yearEnd = matcher.end(2);
            int monthStart = matcher.start(3);
            int monthEnd = matcher.end(3);
            int weekStart = matcher.start(4);
            int weekEnd = matcher.end(4);
            int dayStart = matcher.start(5);
            int dayEnd = matcher.end(5);
            if (yearStart >= 0 || monthStart >= 0 || weekStart >= 0 || dayStart >= 0) {
                try {
                    int years = Period.parseNumber(text, yearStart, yearEnd, negate);
                    int months = Period.parseNumber(text, monthStart, monthEnd, negate);
                    int weeks = Period.parseNumber(text, weekStart, weekEnd, negate);
                    int days = Period.parseNumber(text, dayStart, dayEnd, negate);
                    days = Math.addExact(days, Math.multiplyExact(weeks, 7));
                    return Period.create(years, months, days);
                }
                catch (NumberFormatException ex) {
                    throw new DateTimeParseException("Text cannot be parsed to a Period", text, 0, ex);
                }
            }
        }
        throw new DateTimeParseException("Text cannot be parsed to a Period", text, 0);
    }

    private static boolean charMatch(CharSequence text, int start, int end, char c) {
        return start >= 0 && end == start + 1 && text.charAt(start) == c;
    }

    private static int parseNumber(CharSequence text, int start, int end, int negate) {
        if (start < 0 || end < 0) {
            return 0;
        }
        int val = Integer.parseInt(text, start, end, 10);
        try {
            return Math.multiplyExact(val, negate);
        }
        catch (ArithmeticException ex) {
            throw new DateTimeParseException("Text cannot be parsed to a Period", text, 0, ex);
        }
    }

    public static Period between(LocalDate startDateInclusive, LocalDate endDateExclusive) {
        return startDateInclusive.until(endDateExclusive);
    }

    private static Period create(int years, int months, int days) {
        if ((years | months | days) == 0) {
            return ZERO;
        }
        return new Period(years, months, days);
    }

    private Period(int years, int months, int days) {
        this.years = years;
        this.months = months;
        this.days = days;
    }

    @Override
    public long get(TemporalUnit unit) {
        if (unit == ChronoUnit.YEARS) {
            return this.getYears();
        }
        if (unit == ChronoUnit.MONTHS) {
            return this.getMonths();
        }
        if (unit == ChronoUnit.DAYS) {
            return this.getDays();
        }
        throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return SUPPORTED_UNITS;
    }

    @Override
    public IsoChronology getChronology() {
        return IsoChronology.INSTANCE;
    }

    @Override
    public boolean isZero() {
        return this == ZERO;
    }

    @Override
    public boolean isNegative() {
        return this.years < 0 || this.months < 0 || this.days < 0;
    }

    public int getYears() {
        return this.years;
    }

    public int getMonths() {
        return this.months;
    }

    public int getDays() {
        return this.days;
    }

    public Period withYears(int years) {
        if (years == this.years) {
            return this;
        }
        return Period.create(years, this.months, this.days);
    }

    public Period withMonths(int months) {
        if (months == this.months) {
            return this;
        }
        return Period.create(this.years, months, this.days);
    }

    public Period withDays(int days) {
        if (days == this.days) {
            return this;
        }
        return Period.create(this.years, this.months, days);
    }

    @Override
    public Period plus(TemporalAmount amountToAdd) {
        Period isoAmount = Period.from(amountToAdd);
        return Period.create(Math.addExact(this.years, isoAmount.years), Math.addExact(this.months, isoAmount.months), Math.addExact(this.days, isoAmount.days));
    }

    public Period plusYears(long yearsToAdd) {
        if (yearsToAdd == 0L) {
            return this;
        }
        return Period.create(Math.toIntExact(Math.addExact((long)this.years, yearsToAdd)), this.months, this.days);
    }

    public Period plusMonths(long monthsToAdd) {
        if (monthsToAdd == 0L) {
            return this;
        }
        return Period.create(this.years, Math.toIntExact(Math.addExact((long)this.months, monthsToAdd)), this.days);
    }

    public Period plusDays(long daysToAdd) {
        if (daysToAdd == 0L) {
            return this;
        }
        return Period.create(this.years, this.months, Math.toIntExact(Math.addExact((long)this.days, daysToAdd)));
    }

    @Override
    public Period minus(TemporalAmount amountToSubtract) {
        Period isoAmount = Period.from(amountToSubtract);
        return Period.create(Math.subtractExact(this.years, isoAmount.years), Math.subtractExact(this.months, isoAmount.months), Math.subtractExact(this.days, isoAmount.days));
    }

    public Period minusYears(long yearsToSubtract) {
        return yearsToSubtract == Long.MIN_VALUE ? this.plusYears(Long.MAX_VALUE).plusYears(1L) : this.plusYears(-yearsToSubtract);
    }

    public Period minusMonths(long monthsToSubtract) {
        return monthsToSubtract == Long.MIN_VALUE ? this.plusMonths(Long.MAX_VALUE).plusMonths(1L) : this.plusMonths(-monthsToSubtract);
    }

    public Period minusDays(long daysToSubtract) {
        return daysToSubtract == Long.MIN_VALUE ? this.plusDays(Long.MAX_VALUE).plusDays(1L) : this.plusDays(-daysToSubtract);
    }

    @Override
    public Period multipliedBy(int scalar) {
        if (this == ZERO || scalar == 1) {
            return this;
        }
        return Period.create(Math.multiplyExact(this.years, scalar), Math.multiplyExact(this.months, scalar), Math.multiplyExact(this.days, scalar));
    }

    @Override
    public Period negated() {
        return this.multipliedBy(-1);
    }

    @Override
    public Period normalized() {
        long totalMonths = this.toTotalMonths();
        long splitYears = totalMonths / 12L;
        int splitMonths = (int)(totalMonths % 12L);
        if (splitYears == (long)this.years && splitMonths == this.months) {
            return this;
        }
        return Period.create(Math.toIntExact(splitYears), splitMonths, this.days);
    }

    public long toTotalMonths() {
        return (long)this.years * 12L + (long)this.months;
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        this.validateChrono(temporal);
        if (this.months == 0) {
            if (this.years != 0) {
                temporal = temporal.plus(this.years, ChronoUnit.YEARS);
            }
        } else {
            long totalMonths = this.toTotalMonths();
            if (totalMonths != 0L) {
                temporal = temporal.plus(totalMonths, ChronoUnit.MONTHS);
            }
        }
        if (this.days != 0) {
            temporal = temporal.plus(this.days, ChronoUnit.DAYS);
        }
        return temporal;
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        this.validateChrono(temporal);
        if (this.months == 0) {
            if (this.years != 0) {
                temporal = temporal.minus(this.years, ChronoUnit.YEARS);
            }
        } else {
            long totalMonths = this.toTotalMonths();
            if (totalMonths != 0L) {
                temporal = temporal.minus(totalMonths, ChronoUnit.MONTHS);
            }
        }
        if (this.days != 0) {
            temporal = temporal.minus(this.days, ChronoUnit.DAYS);
        }
        return temporal;
    }

    private void validateChrono(TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "temporal");
        Chronology temporalChrono = temporal.query(TemporalQueries.chronology());
        if (temporalChrono != null && !IsoChronology.INSTANCE.equals(temporalChrono)) {
            throw new DateTimeException("Chronology mismatch, expected: ISO, actual: " + temporalChrono.getId());
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Period)) return false;
        Period other = (Period)obj;
        if (this.years != other.years) return false;
        if (this.months != other.months) return false;
        if (this.days != other.days) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.years + Integer.rotateLeft(this.months, 8) + Integer.rotateLeft(this.days, 16);
    }

    @Override
    public String toString() {
        if (this == ZERO) {
            return "P0D";
        }
        StringBuilder buf = new StringBuilder();
        buf.append('P');
        if (this.years != 0) {
            buf.append(this.years).append('Y');
        }
        if (this.months != 0) {
            buf.append(this.months).append('M');
        }
        if (this.days != 0) {
            buf.append(this.days).append('D');
        }
        return buf.toString();
    }

    private Object writeReplace() {
        return new Ser(14, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeInt(this.years);
        out.writeInt(this.months);
        out.writeInt(this.days);
    }

    static Period readExternal(DataInput in) throws IOException {
        int years = in.readInt();
        int months = in.readInt();
        int days = in.readInt();
        return Period.of(years, months, days);
    }
}

