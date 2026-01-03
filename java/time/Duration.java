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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.Ser;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jdk.internal.ValueBased;

@ValueBased
public final class Duration
implements TemporalAmount,
Comparable<Duration>,
Serializable {
    public static final Duration ZERO = new Duration(0L, 0);
    private static final long serialVersionUID = 3078945930695997490L;
    private static final BigInteger BI_NANOS_PER_SECOND = BigInteger.valueOf(1000000000L);
    private final long seconds;
    private final int nanos;

    public static Duration ofDays(long days) {
        return Duration.create(Math.multiplyExact(days, 86400), 0);
    }

    public static Duration ofHours(long hours) {
        return Duration.create(Math.multiplyExact(hours, 3600), 0);
    }

    public static Duration ofMinutes(long minutes) {
        return Duration.create(Math.multiplyExact(minutes, 60), 0);
    }

    public static Duration ofSeconds(long seconds) {
        return Duration.create(seconds, 0);
    }

    public static Duration ofSeconds(long seconds, long nanoAdjustment) {
        long secs = Math.addExact(seconds, Math.floorDiv(nanoAdjustment, 1000000000L));
        int nos = (int)Math.floorMod(nanoAdjustment, 1000000000L);
        return Duration.create(secs, nos);
    }

    public static Duration ofMillis(long millis) {
        long secs = millis / 1000L;
        int mos = (int)(millis % 1000L);
        if (mos < 0) {
            mos += 1000;
            --secs;
        }
        return Duration.create(secs, mos * 1000000);
    }

    public static Duration ofNanos(long nanos) {
        long secs = nanos / 1000000000L;
        int nos = (int)(nanos % 1000000000L);
        if (nos < 0) {
            nos += 1000000000;
            --secs;
        }
        return Duration.create(secs, nos);
    }

    public static Duration of(long amount, TemporalUnit unit) {
        return ZERO.plus(amount, unit);
    }

    public static Duration from(TemporalAmount amount) {
        Objects.requireNonNull(amount, "amount");
        Duration duration = ZERO;
        for (TemporalUnit unit : amount.getUnits()) {
            duration = duration.plus(amount.get(unit), unit);
        }
        return duration;
    }

    public static Duration parse(CharSequence text) {
        Objects.requireNonNull(text, "text");
        Matcher matcher = Lazy.PATTERN.matcher(text);
        if (matcher.matches() && !Duration.charMatch(text, matcher.start(3), matcher.end(3), 'T')) {
            boolean negate = Duration.charMatch(text, matcher.start(1), matcher.end(1), '-');
            int dayStart = matcher.start(2);
            int dayEnd = matcher.end(2);
            int hourStart = matcher.start(4);
            int hourEnd = matcher.end(4);
            int minuteStart = matcher.start(5);
            int minuteEnd = matcher.end(5);
            int secondStart = matcher.start(6);
            int secondEnd = matcher.end(6);
            int fractionStart = matcher.start(7);
            int fractionEnd = matcher.end(7);
            if (dayStart >= 0 || hourStart >= 0 || minuteStart >= 0 || secondStart >= 0) {
                long daysAsSecs = Duration.parseNumber(text, dayStart, dayEnd, 86400, "days");
                long hoursAsSecs = Duration.parseNumber(text, hourStart, hourEnd, 3600, "hours");
                long minsAsSecs = Duration.parseNumber(text, minuteStart, minuteEnd, 60, "minutes");
                long seconds = Duration.parseNumber(text, secondStart, secondEnd, 1, "seconds");
                boolean negativeSecs = secondStart >= 0 && text.charAt(secondStart) == '-';
                int nanos = Duration.parseFraction(text, fractionStart, fractionEnd, negativeSecs ? -1 : 1);
                try {
                    return Duration.create(negate, daysAsSecs, hoursAsSecs, minsAsSecs, seconds, nanos);
                }
                catch (ArithmeticException ex) {
                    throw new DateTimeParseException("Text cannot be parsed to a Duration: overflow", text, 0, ex);
                }
            }
        }
        throw new DateTimeParseException("Text cannot be parsed to a Duration", text, 0);
    }

    private static boolean charMatch(CharSequence text, int start, int end, char c) {
        return start >= 0 && end == start + 1 && text.charAt(start) == c;
    }

    private static long parseNumber(CharSequence text, int start, int end, int multiplier, String errorText) {
        if (start < 0 || end < 0) {
            return 0L;
        }
        try {
            long val = Long.parseLong(text, start, end, 10);
            return Math.multiplyExact(val, multiplier);
        }
        catch (ArithmeticException | NumberFormatException ex) {
            throw new DateTimeParseException("Text cannot be parsed to a Duration: " + errorText, text, 0, ex);
        }
    }

    private static int parseFraction(CharSequence text, int start, int end, int negate) {
        if (start < 0 || end < 0 || end - start == 0) {
            return 0;
        }
        try {
            int fraction = Integer.parseInt(text, start, end, 10);
            for (int i = end - start; i < 9; ++i) {
                fraction *= 10;
            }
            return fraction * negate;
        }
        catch (ArithmeticException | NumberFormatException ex) {
            throw new DateTimeParseException("Text cannot be parsed to a Duration: fraction", text, 0, ex);
        }
    }

    private static Duration create(boolean negate, long daysAsSecs, long hoursAsSecs, long minsAsSecs, long secs, int nanos) {
        long seconds = Math.addExact(daysAsSecs, Math.addExact(hoursAsSecs, Math.addExact(minsAsSecs, secs)));
        if (negate) {
            return Duration.ofSeconds(seconds, nanos).negated();
        }
        return Duration.ofSeconds(seconds, nanos);
    }

    public static Duration between(Temporal startInclusive, Temporal endExclusive) {
        try {
            return Duration.ofNanos(startInclusive.until(endExclusive, ChronoUnit.NANOS));
        }
        catch (ArithmeticException | DateTimeException ex) {
            long nanos;
            long secs = startInclusive.until(endExclusive, ChronoUnit.SECONDS);
            try {
                nanos = endExclusive.getLong(ChronoField.NANO_OF_SECOND) - startInclusive.getLong(ChronoField.NANO_OF_SECOND);
                if (secs > 0L && nanos < 0L) {
                    ++secs;
                } else if (secs < 0L && nanos > 0L) {
                    --secs;
                }
            }
            catch (DateTimeException ex2) {
                nanos = 0L;
            }
            return Duration.ofSeconds(secs, nanos);
        }
    }

    private static Duration create(long seconds, int nanoAdjustment) {
        if ((seconds | (long)nanoAdjustment) == 0L) {
            return ZERO;
        }
        return new Duration(seconds, nanoAdjustment);
    }

    private Duration(long seconds, int nanos) {
        this.seconds = seconds;
        this.nanos = nanos;
    }

    @Override
    public long get(TemporalUnit unit) {
        if (unit == ChronoUnit.SECONDS) {
            return this.seconds;
        }
        if (unit == ChronoUnit.NANOS) {
            return this.nanos;
        }
        throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return DurationUnits.UNITS;
    }

    public boolean isPositive() {
        return (this.seconds | (long)this.nanos) > 0L;
    }

    public boolean isZero() {
        return (this.seconds | (long)this.nanos) == 0L;
    }

    public boolean isNegative() {
        return this.seconds < 0L;
    }

    public long getSeconds() {
        return this.seconds;
    }

    public int getNano() {
        return this.nanos;
    }

    public Duration withSeconds(long seconds) {
        return Duration.create(seconds, this.nanos);
    }

    public Duration withNanos(int nanoOfSecond) {
        ChronoField.NANO_OF_SECOND.checkValidIntValue(nanoOfSecond);
        return Duration.create(this.seconds, nanoOfSecond);
    }

    public Duration plus(Duration duration) {
        return this.plus(duration.getSeconds(), duration.getNano());
    }

    public Duration plus(long amountToAdd, TemporalUnit unit) {
        Objects.requireNonNull(unit, "unit");
        if (unit == ChronoUnit.DAYS) {
            return this.plus(Math.multiplyExact(amountToAdd, 86400), 0L);
        }
        if (unit.isDurationEstimated()) {
            throw new UnsupportedTemporalTypeException("Unit must not have an estimated duration");
        }
        if (amountToAdd == 0L) {
            return this;
        }
        if (unit instanceof ChronoUnit) {
            ChronoUnit chronoUnit = (ChronoUnit)unit;
            return switch (chronoUnit) {
                case ChronoUnit.NANOS -> this.plusNanos(amountToAdd);
                case ChronoUnit.MICROS -> this.plusSeconds(amountToAdd / 1000000000L * 1000L).plusNanos(amountToAdd % 1000000000L * 1000L);
                case ChronoUnit.MILLIS -> this.plusMillis(amountToAdd);
                case ChronoUnit.SECONDS -> this.plusSeconds(amountToAdd);
                default -> this.plusSeconds(Math.multiplyExact(unit.getDuration().seconds, amountToAdd));
            };
        }
        Duration duration = unit.getDuration().multipliedBy(amountToAdd);
        return this.plusSeconds(duration.getSeconds()).plusNanos(duration.getNano());
    }

    public Duration plusDays(long daysToAdd) {
        return this.plus(Math.multiplyExact(daysToAdd, 86400), 0L);
    }

    public Duration plusHours(long hoursToAdd) {
        return this.plus(Math.multiplyExact(hoursToAdd, 3600), 0L);
    }

    public Duration plusMinutes(long minutesToAdd) {
        return this.plus(Math.multiplyExact(minutesToAdd, 60), 0L);
    }

    public Duration plusSeconds(long secondsToAdd) {
        return this.plus(secondsToAdd, 0L);
    }

    public Duration plusMillis(long millisToAdd) {
        return this.plus(millisToAdd / 1000L, millisToAdd % 1000L * 1000000L);
    }

    public Duration plusNanos(long nanosToAdd) {
        return this.plus(0L, nanosToAdd);
    }

    private Duration plus(long secondsToAdd, long nanosToAdd) {
        if ((secondsToAdd | nanosToAdd) == 0L) {
            return this;
        }
        long epochSec = Math.addExact(this.seconds, secondsToAdd);
        epochSec = Math.addExact(epochSec, nanosToAdd / 1000000000L);
        long nanoAdjustment = (long)this.nanos + (nanosToAdd %= 1000000000L);
        return Duration.ofSeconds(epochSec, nanoAdjustment);
    }

    public Duration minus(Duration duration) {
        long secsToSubtract = duration.getSeconds();
        int nanosToSubtract = duration.getNano();
        if (secsToSubtract == Long.MIN_VALUE) {
            return this.plus(Long.MAX_VALUE, -nanosToSubtract).plus(1L, 0L);
        }
        return this.plus(-secsToSubtract, -nanosToSubtract);
    }

    public Duration minus(long amountToSubtract, TemporalUnit unit) {
        return amountToSubtract == Long.MIN_VALUE ? this.plus(Long.MAX_VALUE, unit).plus(1L, unit) : this.plus(-amountToSubtract, unit);
    }

    public Duration minusDays(long daysToSubtract) {
        return daysToSubtract == Long.MIN_VALUE ? this.plusDays(Long.MAX_VALUE).plusDays(1L) : this.plusDays(-daysToSubtract);
    }

    public Duration minusHours(long hoursToSubtract) {
        return hoursToSubtract == Long.MIN_VALUE ? this.plusHours(Long.MAX_VALUE).plusHours(1L) : this.plusHours(-hoursToSubtract);
    }

    public Duration minusMinutes(long minutesToSubtract) {
        return minutesToSubtract == Long.MIN_VALUE ? this.plusMinutes(Long.MAX_VALUE).plusMinutes(1L) : this.plusMinutes(-minutesToSubtract);
    }

    public Duration minusSeconds(long secondsToSubtract) {
        return secondsToSubtract == Long.MIN_VALUE ? this.plusSeconds(Long.MAX_VALUE).plusSeconds(1L) : this.plusSeconds(-secondsToSubtract);
    }

    public Duration minusMillis(long millisToSubtract) {
        return millisToSubtract == Long.MIN_VALUE ? this.plusMillis(Long.MAX_VALUE).plusMillis(1L) : this.plusMillis(-millisToSubtract);
    }

    public Duration minusNanos(long nanosToSubtract) {
        return nanosToSubtract == Long.MIN_VALUE ? this.plusNanos(Long.MAX_VALUE).plusNanos(1L) : this.plusNanos(-nanosToSubtract);
    }

    public Duration multipliedBy(long multiplicand) {
        if (multiplicand == 0L) {
            return ZERO;
        }
        if (multiplicand == 1L) {
            return this;
        }
        return Duration.create(this.toBigDecimalSeconds().multiply(BigDecimal.valueOf(multiplicand)));
    }

    public Duration dividedBy(long divisor) {
        if (divisor == 0L) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        if (divisor == 1L) {
            return this;
        }
        return Duration.create(this.toBigDecimalSeconds().divide(BigDecimal.valueOf(divisor), RoundingMode.DOWN));
    }

    public long dividedBy(Duration divisor) {
        Objects.requireNonNull(divisor, "divisor");
        BigDecimal dividendBigD = this.toBigDecimalSeconds();
        BigDecimal divisorBigD = divisor.toBigDecimalSeconds();
        return dividendBigD.divideToIntegralValue(divisorBigD).longValueExact();
    }

    private BigDecimal toBigDecimalSeconds() {
        return BigDecimal.valueOf(this.seconds).add(BigDecimal.valueOf(this.nanos, 9));
    }

    private static Duration create(BigDecimal seconds) {
        BigInteger nanos = seconds.movePointRight(9).toBigIntegerExact();
        BigInteger[] divRem = nanos.divideAndRemainder(BI_NANOS_PER_SECOND);
        if (divRem[0].bitLength() > 63) {
            throw new ArithmeticException("Exceeds capacity of Duration: " + nanos);
        }
        return Duration.ofSeconds(divRem[0].longValue(), divRem[1].intValue());
    }

    public Duration negated() {
        return this.multipliedBy(-1L);
    }

    public Duration abs() {
        return this.isNegative() ? this.negated() : this;
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        if (this.seconds != 0L) {
            temporal = temporal.plus(this.seconds, ChronoUnit.SECONDS);
        }
        if (this.nanos != 0) {
            temporal = temporal.plus(this.nanos, ChronoUnit.NANOS);
        }
        return temporal;
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        if (this.seconds != 0L) {
            temporal = temporal.minus(this.seconds, ChronoUnit.SECONDS);
        }
        if (this.nanos != 0) {
            temporal = temporal.minus(this.nanos, ChronoUnit.NANOS);
        }
        return temporal;
    }

    public long toDays() {
        return this.seconds / 86400L;
    }

    public long toHours() {
        return this.seconds / 3600L;
    }

    public long toMinutes() {
        return this.seconds / 60L;
    }

    public long toSeconds() {
        return this.seconds;
    }

    public long toMillis() {
        long tempSeconds = this.seconds;
        long tempNanos = this.nanos;
        if (tempSeconds < 0L) {
            ++tempSeconds;
            tempNanos -= 1000000000L;
        }
        long millis = Math.multiplyExact(tempSeconds, 1000);
        millis = Math.addExact(millis, tempNanos / 1000000L);
        return millis;
    }

    public long toNanos() {
        long tempSeconds = this.seconds;
        long tempNanos = this.nanos;
        if (tempSeconds < 0L) {
            ++tempSeconds;
            tempNanos -= 1000000000L;
        }
        long totalNanos = Math.multiplyExact(tempSeconds, 1000000000L);
        totalNanos = Math.addExact(totalNanos, tempNanos);
        return totalNanos;
    }

    public long toDaysPart() {
        return this.seconds / 86400L;
    }

    public int toHoursPart() {
        return (int)(this.toHours() % 24L);
    }

    public int toMinutesPart() {
        return (int)(this.toMinutes() % 60L);
    }

    public int toSecondsPart() {
        return (int)(this.seconds % 60L);
    }

    public int toMillisPart() {
        return this.nanos / 1000000;
    }

    public int toNanosPart() {
        return this.nanos;
    }

    public Duration truncatedTo(TemporalUnit unit) {
        Objects.requireNonNull(unit, "unit");
        if (unit == ChronoUnit.SECONDS && (this.seconds >= 0L || this.nanos == 0)) {
            return new Duration(this.seconds, 0);
        }
        if (unit == ChronoUnit.NANOS) {
            return this;
        }
        Duration unitDur = unit.getDuration();
        if (unitDur.getSeconds() > 86400L) {
            throw new UnsupportedTemporalTypeException("Unit is too large to be used for truncation");
        }
        long dur = unitDur.toNanos();
        if (86400000000000L % dur != 0L) {
            throw new UnsupportedTemporalTypeException("Unit must divide into a standard day without remainder");
        }
        long nod = this.seconds % 86400L * 1000000000L + (long)this.nanos;
        long result = nod / dur * dur;
        return this.plusNanos(result - nod);
    }

    @Override
    public int compareTo(Duration otherDuration) {
        int cmp = Long.compare(this.seconds, otherDuration.seconds);
        if (cmp != 0) {
            return cmp;
        }
        return this.nanos - otherDuration.nanos;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Duration)) return false;
        Duration otherDuration = (Duration)other;
        if (this.seconds != otherDuration.seconds) return false;
        if (this.nanos != otherDuration.nanos) return false;
        return true;
    }

    public int hashCode() {
        return (int)(this.seconds ^ this.seconds >>> 32) + 51 * this.nanos;
    }

    public String toString() {
        if (this == ZERO) {
            return "PT0S";
        }
        long effectiveTotalSecs = this.seconds;
        if (this.seconds < 0L && this.nanos > 0) {
            ++effectiveTotalSecs;
        }
        long hours = effectiveTotalSecs / 3600L;
        int minutes = (int)(effectiveTotalSecs % 3600L / 60L);
        int secs = (int)(effectiveTotalSecs % 60L);
        StringBuilder buf = new StringBuilder(24);
        buf.append("PT");
        if (hours != 0L) {
            buf.append(hours).append('H');
        }
        if (minutes != 0) {
            buf.append(minutes).append('M');
        }
        if (secs == 0 && this.nanos == 0 && buf.length() > 2) {
            return buf.toString();
        }
        if (this.seconds < 0L && this.nanos > 0) {
            if (secs == 0) {
                buf.append("-0");
            } else {
                buf.append(secs);
            }
        } else {
            buf.append(secs);
        }
        if (this.nanos > 0) {
            int pos = buf.length();
            if (this.seconds < 0L) {
                buf.append(2000000000L - (long)this.nanos);
            } else {
                buf.append((long)this.nanos + 1000000000L);
            }
            while (buf.charAt(buf.length() - 1) == '0') {
                buf.setLength(buf.length() - 1);
            }
            buf.setCharAt(pos, '.');
        }
        buf.append('S');
        return buf.toString();
    }

    private Object writeReplace() {
        return new Ser(1, this);
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeLong(this.seconds);
        out.writeInt(this.nanos);
    }

    static Duration readExternal(DataInput in) throws IOException {
        long seconds = in.readLong();
        int nanos = in.readInt();
        return Duration.ofSeconds(seconds, nanos);
    }

    private static class Lazy {
        static final Pattern PATTERN = Pattern.compile("([-+]?)P(?:([-+]?[0-9]+)D)?(T(?:([-+]?[0-9]+)H)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)(?:[.,]([0-9]{0,9}))?S)?)?", 2);

        private Lazy() {
        }
    }

    private static class DurationUnits {
        static final List<TemporalUnit> UNITS = List.of(ChronoUnit.SECONDS, ChronoUnit.NANOS);

        private DurationUnits() {
        }
    }
}

