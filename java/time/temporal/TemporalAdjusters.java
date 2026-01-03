/*
 * Decompiled with CFR 0.152.
 */
package java.time.temporal;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.util.Objects;
import java.util.function.UnaryOperator;

public final class TemporalAdjusters {
    private TemporalAdjusters() {
    }

    public static TemporalAdjuster ofDateAdjuster(UnaryOperator<LocalDate> dateBasedAdjuster) {
        Objects.requireNonNull(dateBasedAdjuster, "dateBasedAdjuster");
        return temporal -> {
            LocalDate input = LocalDate.from(temporal);
            LocalDate output = (LocalDate)dateBasedAdjuster.apply(input);
            return temporal.with(output);
        };
    }

    public static TemporalAdjuster firstDayOfMonth() {
        return temporal -> temporal.with(ChronoField.DAY_OF_MONTH, 1L);
    }

    public static TemporalAdjuster lastDayOfMonth() {
        return temporal -> temporal.with(ChronoField.DAY_OF_MONTH, temporal.range(ChronoField.DAY_OF_MONTH).getMaximum());
    }

    public static TemporalAdjuster firstDayOfNextMonth() {
        return temporal -> temporal.with(ChronoField.DAY_OF_MONTH, 1L).plus(1L, ChronoUnit.MONTHS);
    }

    public static TemporalAdjuster firstDayOfYear() {
        return temporal -> temporal.with(ChronoField.DAY_OF_YEAR, 1L);
    }

    public static TemporalAdjuster lastDayOfYear() {
        return temporal -> temporal.with(ChronoField.DAY_OF_YEAR, temporal.range(ChronoField.DAY_OF_YEAR).getMaximum());
    }

    public static TemporalAdjuster firstDayOfNextYear() {
        return temporal -> temporal.with(ChronoField.DAY_OF_YEAR, 1L).plus(1L, ChronoUnit.YEARS);
    }

    public static TemporalAdjuster firstInMonth(DayOfWeek dayOfWeek) {
        return TemporalAdjusters.dayOfWeekInMonth(1, dayOfWeek);
    }

    public static TemporalAdjuster lastInMonth(DayOfWeek dayOfWeek) {
        return TemporalAdjusters.dayOfWeekInMonth(-1, dayOfWeek);
    }

    public static TemporalAdjuster dayOfWeekInMonth(int ordinal, DayOfWeek dayOfWeek) {
        Objects.requireNonNull(dayOfWeek, "dayOfWeek");
        int dowValue = dayOfWeek.getValue();
        if (ordinal >= 0) {
            return temporal -> {
                Temporal temp = temporal.with(ChronoField.DAY_OF_MONTH, 1L);
                int curDow = temp.get(ChronoField.DAY_OF_WEEK);
                int dowDiff = (dowValue - curDow + 7) % 7;
                return temp.plus(dowDiff += (int)(((long)ordinal - 1L) * 7L), ChronoUnit.DAYS);
            };
        }
        return temporal -> {
            Temporal temp = temporal.with(ChronoField.DAY_OF_MONTH, temporal.range(ChronoField.DAY_OF_MONTH).getMaximum());
            int curDow = temp.get(ChronoField.DAY_OF_WEEK);
            int daysDiff = dowValue - curDow;
            daysDiff = daysDiff == 0 ? 0 : (daysDiff > 0 ? daysDiff - 7 : daysDiff);
            return temp.plus(daysDiff -= (int)(((long)(-ordinal) - 1L) * 7L), ChronoUnit.DAYS);
        };
    }

    public static TemporalAdjuster next(DayOfWeek dayOfWeek) {
        int dowValue = dayOfWeek.getValue();
        return temporal -> {
            int calDow = temporal.get(ChronoField.DAY_OF_WEEK);
            int daysDiff = calDow - dowValue;
            return temporal.plus(daysDiff >= 0 ? (long)(7 - daysDiff) : (long)(-daysDiff), ChronoUnit.DAYS);
        };
    }

    public static TemporalAdjuster nextOrSame(DayOfWeek dayOfWeek) {
        int dowValue = dayOfWeek.getValue();
        return temporal -> {
            int calDow = temporal.get(ChronoField.DAY_OF_WEEK);
            if (calDow == dowValue) {
                return temporal;
            }
            int daysDiff = calDow - dowValue;
            return temporal.plus(daysDiff >= 0 ? (long)(7 - daysDiff) : (long)(-daysDiff), ChronoUnit.DAYS);
        };
    }

    public static TemporalAdjuster previous(DayOfWeek dayOfWeek) {
        int dowValue = dayOfWeek.getValue();
        return temporal -> {
            int calDow = temporal.get(ChronoField.DAY_OF_WEEK);
            int daysDiff = dowValue - calDow;
            return temporal.minus(daysDiff >= 0 ? (long)(7 - daysDiff) : (long)(-daysDiff), ChronoUnit.DAYS);
        };
    }

    public static TemporalAdjuster previousOrSame(DayOfWeek dayOfWeek) {
        int dowValue = dayOfWeek.getValue();
        return temporal -> {
            int calDow = temporal.get(ChronoField.DAY_OF_WEEK);
            if (calDow == dowValue) {
                return temporal;
            }
            int daysDiff = dowValue - calDow;
            return temporal.minus(daysDiff >= 0 ? (long)(7 - daysDiff) : (long)(-daysDiff), ChronoUnit.DAYS);
        };
    }
}

