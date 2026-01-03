/*
 * Decompiled with CFR 0.152.
 */
package java.time;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Locale;

public enum Month implements TemporalAccessor,
TemporalAdjuster
{
    JANUARY,
    FEBRUARY,
    MARCH,
    APRIL,
    MAY,
    JUNE,
    JULY,
    AUGUST,
    SEPTEMBER,
    OCTOBER,
    NOVEMBER,
    DECEMBER;

    private static final Month[] ENUMS;

    public static Month of(int month) {
        if (month < 1 || month > 12) {
            throw new DateTimeException("Invalid value for MonthOfYear: " + month);
        }
        return ENUMS[month - 1];
    }

    public static Month from(TemporalAccessor temporal) {
        if (temporal instanceof Month) {
            return (Month)temporal;
        }
        try {
            if (!IsoChronology.INSTANCE.equals(Chronology.from(temporal))) {
                temporal = LocalDate.from(temporal);
            }
            return Month.of(temporal.get(ChronoField.MONTH_OF_YEAR));
        }
        catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain Month from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName(), ex);
        }
    }

    public int getValue() {
        return this.ordinal() + 1;
    }

    public String getDisplayName(TextStyle style, Locale locale) {
        return new DateTimeFormatterBuilder().appendText((TemporalField)ChronoField.MONTH_OF_YEAR, style).toFormatter(locale).format(this);
    }

    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            return field == ChronoField.MONTH_OF_YEAR;
        }
        return field != null && field.isSupportedBy(this);
    }

    @Override
    public ValueRange range(TemporalField field) {
        if (field == ChronoField.MONTH_OF_YEAR) {
            return field.range();
        }
        return TemporalAccessor.super.range(field);
    }

    @Override
    public int get(TemporalField field) {
        if (field == ChronoField.MONTH_OF_YEAR) {
            return this.getValue();
        }
        return TemporalAccessor.super.get(field);
    }

    @Override
    public long getLong(TemporalField field) {
        if (field == ChronoField.MONTH_OF_YEAR) {
            return this.getValue();
        }
        if (field instanceof ChronoField) {
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return field.getFrom(this);
    }

    public Month plus(long months) {
        int amount = (int)(months % 12L);
        return ENUMS[(this.ordinal() + (amount + 12)) % 12];
    }

    public Month minus(long months) {
        return this.plus(-(months % 12L));
    }

    public int length(boolean leapYear) {
        return switch (this.ordinal()) {
            case 1 -> {
                if (leapYear) {
                    yield 29;
                }
                yield 28;
            }
            case 3, 5, 8, 10 -> 30;
            default -> 31;
        };
    }

    public int minLength() {
        return switch (this.ordinal()) {
            case 1 -> 28;
            case 3, 5, 8, 10 -> 30;
            default -> 31;
        };
    }

    public int maxLength() {
        return switch (this.ordinal()) {
            case 1 -> 29;
            case 3, 5, 8, 10 -> 30;
            default -> 31;
        };
    }

    public int firstDayOfYear(boolean leapYear) {
        int leap = leapYear ? 1 : 0;
        return switch (this.ordinal()) {
            case 0 -> 1;
            case 1 -> 32;
            case 2 -> 60 + leap;
            case 3 -> 91 + leap;
            case 4 -> 121 + leap;
            case 5 -> 152 + leap;
            case 6 -> 182 + leap;
            case 7 -> 213 + leap;
            case 8 -> 244 + leap;
            case 9 -> 274 + leap;
            case 10 -> 305 + leap;
            default -> 335 + leap;
        };
    }

    public Month firstMonthOfQuarter() {
        return ENUMS[this.ordinal() / 3 * 3];
    }

    @Override
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.chronology()) {
            return (R)IsoChronology.INSTANCE;
        }
        if (query == TemporalQueries.precision()) {
            return (R)ChronoUnit.MONTHS;
        }
        return TemporalAccessor.super.query(query);
    }

    @Override
    public Temporal adjustInto(Temporal temporal) {
        if (!Chronology.from(temporal).equals(IsoChronology.INSTANCE)) {
            throw new DateTimeException("Adjustment only supported on ISO date-time");
        }
        return temporal.with(ChronoField.MONTH_OF_YEAR, this.getValue());
    }

    static {
        ENUMS = Month.values();
    }
}

