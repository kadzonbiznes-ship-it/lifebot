/*
 * Decompiled with CFR 0.152.
 */
package java.time.chrono;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.Era;
import java.time.chrono.HijrahChronology;
import java.time.chrono.IsoChronology;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.MinguoChronology;
import java.time.chrono.Ser;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.ValueRange;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import sun.util.logging.PlatformLogger;

public abstract class AbstractChronology
implements Chronology {
    private static final ConcurrentHashMap<String, Chronology> CHRONOS_BY_ID = new ConcurrentHashMap();
    private static final ConcurrentHashMap<String, Chronology> CHRONOS_BY_TYPE = new ConcurrentHashMap();

    static Chronology registerChrono(Chronology chrono) {
        return AbstractChronology.registerChrono(chrono, chrono.getId());
    }

    static Chronology registerChrono(Chronology chrono, String id) {
        String type;
        Chronology prev = CHRONOS_BY_ID.putIfAbsent(id, chrono);
        if (prev == null && (type = chrono.getCalendarType()) != null) {
            CHRONOS_BY_TYPE.putIfAbsent(type, chrono);
        }
        return prev;
    }

    private static boolean initCache() {
        if (CHRONOS_BY_ID.get("ISO") == null) {
            AbstractChronology.registerChrono(HijrahChronology.INSTANCE);
            AbstractChronology.registerChrono(JapaneseChronology.INSTANCE);
            AbstractChronology.registerChrono(MinguoChronology.INSTANCE);
            AbstractChronology.registerChrono(ThaiBuddhistChronology.INSTANCE);
            ServiceLoader<AbstractChronology> loader = ServiceLoader.load(AbstractChronology.class, null);
            for (AbstractChronology chrono : loader) {
                String id = chrono.getId();
                if (!id.equals("ISO") && AbstractChronology.registerChrono(chrono) == null) continue;
                PlatformLogger logger = PlatformLogger.getLogger("java.time.chrono");
                logger.warning("Ignoring duplicate Chronology, from ServiceLoader configuration " + id);
            }
            AbstractChronology.registerChrono(IsoChronology.INSTANCE);
            return true;
        }
        return false;
    }

    static Chronology ofLocale(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        String type = locale.getUnicodeLocaleType("ca");
        if (type == null || "iso".equals(type) || "iso8601".equals(type)) {
            return IsoChronology.INSTANCE;
        }
        do {
            Chronology chrono;
            if ((chrono = CHRONOS_BY_TYPE.get(type)) == null) continue;
            return chrono;
        } while (AbstractChronology.initCache());
        ServiceLoader<Chronology> loader = ServiceLoader.load(Chronology.class);
        for (Chronology chrono : loader) {
            if (!type.equals(chrono.getCalendarType())) continue;
            return chrono;
        }
        throw new DateTimeException("Unknown calendar system: " + type);
    }

    static Chronology of(String id) {
        Objects.requireNonNull(id, "id");
        do {
            Chronology chrono;
            if ((chrono = AbstractChronology.of0(id)) == null) continue;
            return chrono;
        } while (AbstractChronology.initCache());
        ServiceLoader<Chronology> loader = ServiceLoader.load(Chronology.class);
        for (Chronology chrono : loader) {
            if (!id.equals(chrono.getId()) && !id.equals(chrono.getCalendarType())) continue;
            return chrono;
        }
        throw new DateTimeException("Unknown chronology: " + id);
    }

    private static Chronology of0(String id) {
        Chronology chrono = CHRONOS_BY_ID.get(id);
        if (chrono == null) {
            chrono = CHRONOS_BY_TYPE.get(id);
        }
        return chrono;
    }

    static Set<Chronology> getAvailableChronologies() {
        AbstractChronology.initCache();
        HashSet<Chronology> chronos = new HashSet<Chronology>(CHRONOS_BY_ID.values());
        ServiceLoader<Chronology> loader = ServiceLoader.load(Chronology.class);
        for (Chronology chrono : loader) {
            chronos.add(chrono);
        }
        return chronos;
    }

    protected AbstractChronology() {
    }

    @Override
    public ChronoLocalDate resolveDate(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        if (fieldValues.containsKey(ChronoField.EPOCH_DAY)) {
            return this.dateEpochDay(fieldValues.remove(ChronoField.EPOCH_DAY));
        }
        this.resolveProlepticMonth(fieldValues, resolverStyle);
        ChronoLocalDate resolved = this.resolveYearOfEra(fieldValues, resolverStyle);
        if (resolved != null) {
            return resolved;
        }
        if (fieldValues.containsKey(ChronoField.YEAR)) {
            if (fieldValues.containsKey(ChronoField.MONTH_OF_YEAR)) {
                if (fieldValues.containsKey(ChronoField.DAY_OF_MONTH)) {
                    return this.resolveYMD(fieldValues, resolverStyle);
                }
                if (fieldValues.containsKey(ChronoField.ALIGNED_WEEK_OF_MONTH)) {
                    if (fieldValues.containsKey(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH)) {
                        return this.resolveYMAA(fieldValues, resolverStyle);
                    }
                    if (fieldValues.containsKey(ChronoField.DAY_OF_WEEK)) {
                        return this.resolveYMAD(fieldValues, resolverStyle);
                    }
                }
            }
            if (fieldValues.containsKey(ChronoField.DAY_OF_YEAR)) {
                return this.resolveYD(fieldValues, resolverStyle);
            }
            if (fieldValues.containsKey(ChronoField.ALIGNED_WEEK_OF_YEAR)) {
                if (fieldValues.containsKey(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR)) {
                    return this.resolveYAA(fieldValues, resolverStyle);
                }
                if (fieldValues.containsKey(ChronoField.DAY_OF_WEEK)) {
                    return this.resolveYAD(fieldValues, resolverStyle);
                }
            }
        }
        return null;
    }

    void resolveProlepticMonth(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        Long pMonth = fieldValues.remove(ChronoField.PROLEPTIC_MONTH);
        if (pMonth != null) {
            if (resolverStyle != ResolverStyle.LENIENT) {
                ChronoField.PROLEPTIC_MONTH.checkValidValue(pMonth);
            }
            ChronoLocalDate chronoDate = this.dateNow().with(ChronoField.DAY_OF_MONTH, 1L).with(ChronoField.PROLEPTIC_MONTH, pMonth);
            this.addFieldValue(fieldValues, ChronoField.MONTH_OF_YEAR, chronoDate.get(ChronoField.MONTH_OF_YEAR));
            this.addFieldValue(fieldValues, ChronoField.YEAR, chronoDate.get(ChronoField.YEAR));
        }
    }

    ChronoLocalDate resolveYearOfEra(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        Long yoeLong = fieldValues.remove(ChronoField.YEAR_OF_ERA);
        if (yoeLong != null) {
            Long eraLong = fieldValues.remove(ChronoField.ERA);
            int yoe = resolverStyle != ResolverStyle.LENIENT ? this.range(ChronoField.YEAR_OF_ERA).checkValidIntValue(yoeLong, ChronoField.YEAR_OF_ERA) : Math.toIntExact(yoeLong);
            if (eraLong != null) {
                Era eraObj = this.eraOf(this.range(ChronoField.ERA).checkValidIntValue(eraLong, ChronoField.ERA));
                this.addFieldValue(fieldValues, ChronoField.YEAR, this.prolepticYear(eraObj, yoe));
            } else if (fieldValues.containsKey(ChronoField.YEAR)) {
                int year = this.range(ChronoField.YEAR).checkValidIntValue(fieldValues.get(ChronoField.YEAR), ChronoField.YEAR);
                ChronoLocalDate chronoDate = this.dateYearDay(year, 1);
                this.addFieldValue(fieldValues, ChronoField.YEAR, this.prolepticYear(chronoDate.getEra(), yoe));
            } else if (resolverStyle == ResolverStyle.STRICT) {
                fieldValues.put(ChronoField.YEAR_OF_ERA, yoeLong);
            } else {
                List<Era> eras = this.eras();
                if (eras.isEmpty()) {
                    this.addFieldValue(fieldValues, ChronoField.YEAR, yoe);
                } else {
                    Era eraObj = eras.get(eras.size() - 1);
                    this.addFieldValue(fieldValues, ChronoField.YEAR, this.prolepticYear(eraObj, yoe));
                }
            }
        } else if (fieldValues.containsKey(ChronoField.ERA)) {
            this.range(ChronoField.ERA).checkValidValue(fieldValues.get(ChronoField.ERA), ChronoField.ERA);
        }
        return null;
    }

    ChronoLocalDate resolveYMD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = this.range(ChronoField.YEAR).checkValidIntValue(fieldValues.remove(ChronoField.YEAR), ChronoField.YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long months = Math.subtractExact(fieldValues.remove(ChronoField.MONTH_OF_YEAR), 1L);
            long days = Math.subtractExact(fieldValues.remove(ChronoField.DAY_OF_MONTH), 1L);
            return this.date(y, 1, 1).plus(months, ChronoUnit.MONTHS).plus(days, ChronoUnit.DAYS);
        }
        int moy = this.range(ChronoField.MONTH_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.MONTH_OF_YEAR), ChronoField.MONTH_OF_YEAR);
        ValueRange domRange = this.range(ChronoField.DAY_OF_MONTH);
        int dom = domRange.checkValidIntValue(fieldValues.remove(ChronoField.DAY_OF_MONTH), ChronoField.DAY_OF_MONTH);
        if (resolverStyle == ResolverStyle.SMART) {
            try {
                return this.date(y, moy, dom);
            }
            catch (DateTimeException ex) {
                return this.date(y, moy, 1).with(TemporalAdjusters.lastDayOfMonth());
            }
        }
        return this.date(y, moy, dom);
    }

    ChronoLocalDate resolveYD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = this.range(ChronoField.YEAR).checkValidIntValue(fieldValues.remove(ChronoField.YEAR), ChronoField.YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long days = Math.subtractExact(fieldValues.remove(ChronoField.DAY_OF_YEAR), 1L);
            return this.dateYearDay(y, 1).plus(days, ChronoUnit.DAYS);
        }
        int doy = this.range(ChronoField.DAY_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.DAY_OF_YEAR), ChronoField.DAY_OF_YEAR);
        return this.dateYearDay(y, doy);
    }

    ChronoLocalDate resolveYMAA(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = this.range(ChronoField.YEAR).checkValidIntValue(fieldValues.remove(ChronoField.YEAR), ChronoField.YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long months = Math.subtractExact(fieldValues.remove(ChronoField.MONTH_OF_YEAR), 1L);
            long weeks = Math.subtractExact(fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_MONTH), 1L);
            long days = Math.subtractExact(fieldValues.remove(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH), 1L);
            return this.date(y, 1, 1).plus(months, ChronoUnit.MONTHS).plus(weeks, ChronoUnit.WEEKS).plus(days, ChronoUnit.DAYS);
        }
        int moy = this.range(ChronoField.MONTH_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.MONTH_OF_YEAR), ChronoField.MONTH_OF_YEAR);
        int aw = this.range(ChronoField.ALIGNED_WEEK_OF_MONTH).checkValidIntValue(fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_MONTH), ChronoField.ALIGNED_WEEK_OF_MONTH);
        int ad = this.range(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH).checkValidIntValue(fieldValues.remove(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH), ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH);
        ChronoLocalDate date = this.date(y, moy, 1).plus((aw - 1) * 7 + (ad - 1), ChronoUnit.DAYS);
        if (resolverStyle == ResolverStyle.STRICT && date.get(ChronoField.MONTH_OF_YEAR) != moy) {
            throw new DateTimeException("Strict mode rejected resolved date as it is in a different month");
        }
        return date;
    }

    ChronoLocalDate resolveYMAD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = this.range(ChronoField.YEAR).checkValidIntValue(fieldValues.remove(ChronoField.YEAR), ChronoField.YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long months = Math.subtractExact(fieldValues.remove(ChronoField.MONTH_OF_YEAR), 1L);
            long weeks = Math.subtractExact(fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_MONTH), 1L);
            long dow = Math.subtractExact(fieldValues.remove(ChronoField.DAY_OF_WEEK), 1L);
            return this.resolveAligned(this.date(y, 1, 1), months, weeks, dow);
        }
        int moy = this.range(ChronoField.MONTH_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.MONTH_OF_YEAR), ChronoField.MONTH_OF_YEAR);
        int aw = this.range(ChronoField.ALIGNED_WEEK_OF_MONTH).checkValidIntValue(fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_MONTH), ChronoField.ALIGNED_WEEK_OF_MONTH);
        int dow = this.range(ChronoField.DAY_OF_WEEK).checkValidIntValue(fieldValues.remove(ChronoField.DAY_OF_WEEK), ChronoField.DAY_OF_WEEK);
        ChronoLocalDate date = this.date(y, moy, 1).plus((aw - 1) * 7, ChronoUnit.DAYS).with(TemporalAdjusters.nextOrSame(DayOfWeek.of(dow)));
        if (resolverStyle == ResolverStyle.STRICT && date.get(ChronoField.MONTH_OF_YEAR) != moy) {
            throw new DateTimeException("Strict mode rejected resolved date as it is in a different month");
        }
        return date;
    }

    ChronoLocalDate resolveYAA(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = this.range(ChronoField.YEAR).checkValidIntValue(fieldValues.remove(ChronoField.YEAR), ChronoField.YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long weeks = Math.subtractExact(fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_YEAR), 1L);
            long days = Math.subtractExact(fieldValues.remove(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR), 1L);
            return this.dateYearDay(y, 1).plus(weeks, ChronoUnit.WEEKS).plus(days, ChronoUnit.DAYS);
        }
        int aw = this.range(ChronoField.ALIGNED_WEEK_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_YEAR), ChronoField.ALIGNED_WEEK_OF_YEAR);
        int ad = this.range(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR), ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR);
        ChronoLocalDate date = this.dateYearDay(y, 1).plus((aw - 1) * 7 + (ad - 1), ChronoUnit.DAYS);
        if (resolverStyle == ResolverStyle.STRICT && date.get(ChronoField.YEAR) != y) {
            throw new DateTimeException("Strict mode rejected resolved date as it is in a different year");
        }
        return date;
    }

    ChronoLocalDate resolveYAD(Map<TemporalField, Long> fieldValues, ResolverStyle resolverStyle) {
        int y = this.range(ChronoField.YEAR).checkValidIntValue(fieldValues.remove(ChronoField.YEAR), ChronoField.YEAR);
        if (resolverStyle == ResolverStyle.LENIENT) {
            long weeks = Math.subtractExact(fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_YEAR), 1L);
            long dow = Math.subtractExact(fieldValues.remove(ChronoField.DAY_OF_WEEK), 1L);
            return this.resolveAligned(this.dateYearDay(y, 1), 0L, weeks, dow);
        }
        int aw = this.range(ChronoField.ALIGNED_WEEK_OF_YEAR).checkValidIntValue(fieldValues.remove(ChronoField.ALIGNED_WEEK_OF_YEAR), ChronoField.ALIGNED_WEEK_OF_YEAR);
        int dow = this.range(ChronoField.DAY_OF_WEEK).checkValidIntValue(fieldValues.remove(ChronoField.DAY_OF_WEEK), ChronoField.DAY_OF_WEEK);
        ChronoLocalDate date = this.dateYearDay(y, 1).plus((aw - 1) * 7, ChronoUnit.DAYS).with(TemporalAdjusters.nextOrSame(DayOfWeek.of(dow)));
        if (resolverStyle == ResolverStyle.STRICT && date.get(ChronoField.YEAR) != y) {
            throw new DateTimeException("Strict mode rejected resolved date as it is in a different year");
        }
        return date;
    }

    ChronoLocalDate resolveAligned(ChronoLocalDate base, long months, long weeks, long dow) {
        ChronoLocalDate date = base.plus(months, ChronoUnit.MONTHS).plus(weeks, ChronoUnit.WEEKS);
        if (dow > 7L) {
            date = date.plus((dow - 1L) / 7L, ChronoUnit.WEEKS);
            dow = (dow - 1L) % 7L + 1L;
        } else if (dow < 1L) {
            date = date.plus(Math.subtractExact(dow, 7L) / 7L, ChronoUnit.WEEKS);
            dow = (dow + 6L) % 7L + 1L;
        }
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.of((int)dow)));
    }

    void addFieldValue(Map<TemporalField, Long> fieldValues, ChronoField field, long value) {
        Long old = fieldValues.get(field);
        if (old != null && old != value) {
            throw new DateTimeException("Conflict found: " + field + " " + old + " differs from " + field + " " + value);
        }
        fieldValues.put(field, value);
    }

    @Override
    public int compareTo(Chronology other) {
        return this.getId().compareTo(other.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AbstractChronology) {
            return this.compareTo((AbstractChronology)obj) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode() ^ this.getId().hashCode();
    }

    @Override
    public String toString() {
        return this.getId();
    }

    Object writeReplace() {
        return new Ser(1, (Serializable)((Object)this));
    }

    private void readObject(ObjectInputStream s) throws ObjectStreamException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeUTF(this.getId());
    }

    static Chronology readExternal(DataInput in) throws IOException {
        String id = in.readUTF();
        return Chronology.of(id);
    }
}

