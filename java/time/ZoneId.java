/*
 * Decompiled with CFR 0.152.
 */
package java.time;

import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.Ser;
import java.time.ZoneOffset;
import java.time.ZoneRegion;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import jdk.internal.ValueBased;

@ValueBased
public abstract sealed class ZoneId
implements Serializable
permits ZoneOffset, ZoneRegion {
    public static final Map<String, String> SHORT_IDS = Map.ofEntries(Map.entry("ACT", "Australia/Darwin"), Map.entry("AET", "Australia/Sydney"), Map.entry("AGT", "America/Argentina/Buenos_Aires"), Map.entry("ART", "Africa/Cairo"), Map.entry("AST", "America/Anchorage"), Map.entry("BET", "America/Sao_Paulo"), Map.entry("BST", "Asia/Dhaka"), Map.entry("CAT", "Africa/Harare"), Map.entry("CNT", "America/St_Johns"), Map.entry("CST", "America/Chicago"), Map.entry("CTT", "Asia/Shanghai"), Map.entry("EAT", "Africa/Addis_Ababa"), Map.entry("ECT", "Europe/Paris"), Map.entry("IET", "America/Indiana/Indianapolis"), Map.entry("IST", "Asia/Kolkata"), Map.entry("JST", "Asia/Tokyo"), Map.entry("MIT", "Pacific/Apia"), Map.entry("NET", "Asia/Yerevan"), Map.entry("NST", "Pacific/Auckland"), Map.entry("PLT", "Asia/Karachi"), Map.entry("PNT", "America/Phoenix"), Map.entry("PRT", "America/Puerto_Rico"), Map.entry("PST", "America/Los_Angeles"), Map.entry("SST", "Pacific/Guadalcanal"), Map.entry("VST", "Asia/Ho_Chi_Minh"), Map.entry("EST", "-05:00"), Map.entry("MST", "-07:00"), Map.entry("HST", "-10:00"));
    private static final long serialVersionUID = 8352817235686L;

    public static ZoneId systemDefault() {
        return TimeZone.getDefault().toZoneId();
    }

    public static Set<String> getAvailableZoneIds() {
        return new HashSet<String>(ZoneRulesProvider.getAvailableZoneIds());
    }

    public static ZoneId of(String zoneId, Map<String, String> aliasMap) {
        Objects.requireNonNull(zoneId, "zoneId");
        Objects.requireNonNull(aliasMap, "aliasMap");
        String id = Objects.requireNonNullElse(aliasMap.get(zoneId), zoneId);
        return ZoneId.of(id);
    }

    public static ZoneId of(String zoneId) {
        return ZoneId.of(zoneId, true);
    }

    public static ZoneId ofOffset(String prefix, ZoneOffset offset) {
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(offset, "offset");
        if (prefix.isEmpty()) {
            return offset;
        }
        if (!(prefix.equals("GMT") || prefix.equals("UTC") || prefix.equals("UT"))) {
            throw new IllegalArgumentException("prefix should be GMT, UTC or UT, is: " + prefix);
        }
        if (offset.getTotalSeconds() != 0) {
            prefix = prefix.concat(offset.getId());
        }
        return new ZoneRegion(prefix, offset.getRules());
    }

    static ZoneId of(String zoneId, boolean checkAvailable) {
        Objects.requireNonNull(zoneId, "zoneId");
        if (zoneId.length() <= 1 || zoneId.startsWith("+") || zoneId.startsWith("-")) {
            return ZoneOffset.of(zoneId);
        }
        if (zoneId.startsWith("UTC") || zoneId.startsWith("GMT")) {
            return ZoneId.ofWithPrefix(zoneId, 3, checkAvailable);
        }
        if (zoneId.startsWith("UT")) {
            return ZoneId.ofWithPrefix(zoneId, 2, checkAvailable);
        }
        return ZoneRegion.ofId(zoneId, checkAvailable);
    }

    private static ZoneId ofWithPrefix(String zoneId, int prefixLength, boolean checkAvailable) {
        String prefix = zoneId.substring(0, prefixLength);
        if (zoneId.length() == prefixLength) {
            return ZoneId.ofOffset(prefix, ZoneOffset.UTC);
        }
        if (zoneId.charAt(prefixLength) != '+' && zoneId.charAt(prefixLength) != '-') {
            return ZoneRegion.ofId(zoneId, checkAvailable);
        }
        try {
            ZoneOffset offset = ZoneOffset.of(zoneId.substring(prefixLength));
            if (offset == ZoneOffset.UTC) {
                return ZoneId.ofOffset(prefix, offset);
            }
            return ZoneId.ofOffset(prefix, offset);
        }
        catch (DateTimeException ex) {
            throw new DateTimeException("Invalid ID for offset-based ZoneId: " + zoneId, ex);
        }
    }

    public static ZoneId from(TemporalAccessor temporal) {
        ZoneId obj = temporal.query(TemporalQueries.zone());
        if (obj == null) {
            throw new DateTimeException("Unable to obtain ZoneId from TemporalAccessor: " + temporal + " of type " + temporal.getClass().getName());
        }
        return obj;
    }

    ZoneId() {
    }

    public abstract String getId();

    public String getDisplayName(TextStyle style, Locale locale) {
        return new DateTimeFormatterBuilder().appendZoneText(style).toFormatter(locale).format(this.toTemporal());
    }

    private TemporalAccessor toTemporal() {
        return new TemporalAccessor(){

            @Override
            public boolean isSupported(TemporalField field) {
                return false;
            }

            @Override
            public long getLong(TemporalField field) {
                throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            }

            @Override
            public <R> R query(TemporalQuery<R> query) {
                if (query == TemporalQueries.zoneId()) {
                    return (R)ZoneId.this;
                }
                return TemporalAccessor.super.query(query);
            }
        };
    }

    public abstract ZoneRules getRules();

    public ZoneId normalized() {
        try {
            ZoneRules rules = this.getRules();
            if (rules.isFixedOffset()) {
                return rules.getOffset(Instant.EPOCH);
            }
        }
        catch (ZoneRulesException zoneRulesException) {
            // empty catch block
        }
        return this;
    }

    abstract ZoneOffset getOffset(long var1);

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ZoneId)) return false;
        ZoneId other = (ZoneId)obj;
        if (!this.getId().equals(other.getId())) return false;
        return true;
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    public String toString() {
        return this.getId();
    }

    private Object writeReplace() {
        return new Ser(7, this);
    }

    abstract void write(DataOutput var1) throws IOException;
}

