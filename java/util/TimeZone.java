/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyPermission;
import jdk.internal.util.StaticProperty;
import sun.security.action.GetPropertyAction;
import sun.util.calendar.ZoneInfo;
import sun.util.calendar.ZoneInfoFile;
import sun.util.locale.provider.TimeZoneNameUtility;

public abstract class TimeZone
implements Serializable,
Cloneable {
    public static final int SHORT = 0;
    public static final int LONG = 1;
    private static final int ONE_MINUTE = 60000;
    private static final int ONE_HOUR = 3600000;
    private static final int ONE_DAY = 86400000;
    static final long serialVersionUID = 3581463369166924961L;
    static final TimeZone NO_TIMEZONE = null;
    private String ID;
    private transient ZoneId zoneId;
    private static volatile TimeZone defaultTimeZone;
    static final String GMT_ID = "GMT";
    private static final int GMT_ID_LENGTH = 3;

    public abstract int getOffset(int var1, int var2, int var3, int var4, int var5, int var6);

    public int getOffset(long date) {
        if (this.inDaylightTime(new Date(date))) {
            return this.getRawOffset() + this.getDSTSavings();
        }
        return this.getRawOffset();
    }

    int getOffsets(long date, int[] offsets) {
        int rawoffset = this.getRawOffset();
        int dstoffset = 0;
        if (this.inDaylightTime(new Date(date))) {
            dstoffset = this.getDSTSavings();
        }
        if (offsets != null) {
            offsets[0] = rawoffset;
            offsets[1] = dstoffset;
        }
        return rawoffset + dstoffset;
    }

    public abstract void setRawOffset(int var1);

    public abstract int getRawOffset();

    public String getID() {
        return this.ID;
    }

    public void setID(String ID) {
        if (ID == null) {
            throw new NullPointerException();
        }
        this.ID = ID;
        this.zoneId = null;
    }

    public final String getDisplayName() {
        return this.getDisplayName(false, 1, Locale.getDefault(Locale.Category.DISPLAY));
    }

    public final String getDisplayName(Locale locale) {
        return this.getDisplayName(false, 1, locale);
    }

    public final String getDisplayName(boolean daylight, int style) {
        return this.getDisplayName(daylight, style, Locale.getDefault(Locale.Category.DISPLAY));
    }

    public String getDisplayName(boolean daylight, int style, Locale locale) {
        char sign;
        if (style != 0 && style != 1) {
            throw new IllegalArgumentException("Illegal style: " + style);
        }
        String id = this.getID();
        String name = TimeZoneNameUtility.retrieveDisplayName(id, daylight, style, locale);
        if (name != null) {
            return name;
        }
        if (id.startsWith(GMT_ID) && id.length() > 3 && ((sign = id.charAt(3)) == '+' || sign == '-')) {
            return id;
        }
        int offset = this.getRawOffset();
        if (daylight) {
            offset += this.getDSTSavings();
        }
        return ZoneInfoFile.toCustomID(offset);
    }

    private static String[] getDisplayNames(String id, Locale locale) {
        return TimeZoneNameUtility.retrieveDisplayNames(id, locale);
    }

    public int getDSTSavings() {
        if (this.useDaylightTime()) {
            return 3600000;
        }
        return 0;
    }

    public abstract boolean useDaylightTime();

    public boolean observesDaylightTime() {
        return this.useDaylightTime() || this.inDaylightTime(new Date());
    }

    public abstract boolean inDaylightTime(Date var1);

    public static synchronized TimeZone getTimeZone(String ID) {
        return TimeZone.getTimeZone(ID, true);
    }

    public static TimeZone getTimeZone(ZoneId zoneId) {
        String tzid = zoneId.getId();
        if (zoneId instanceof ZoneOffset) {
            ZoneOffset zo = (ZoneOffset)zoneId;
            int totalMillis = zo.getTotalSeconds() * 1000;
            return new ZoneInfo(totalMillis == 0 ? "UTC" : GMT_ID + tzid, totalMillis);
        }
        if (tzid.startsWith("UT") && !tzid.equals("UTC")) {
            tzid = tzid.replaceFirst("(UTC|UT)(.*)", "GMT$2");
        }
        return TimeZone.getTimeZone(tzid, true);
    }

    public ZoneId toZoneId() {
        ZoneId zId = this.zoneId;
        if (zId == null) {
            this.zoneId = zId = this.toZoneId0();
        }
        return zId;
    }

    private ZoneId toZoneId0() {
        String id = this.getID();
        TimeZone defaultZone = defaultTimeZone;
        if (defaultZone != this && defaultZone != null && id.equals(defaultZone.getID())) {
            return defaultZone.toZoneId();
        }
        if (ZoneInfoFile.useOldMapping() && id.length() == 3) {
            if ("EST".equals(id)) {
                return ZoneId.of("America/New_York");
            }
            if ("MST".equals(id)) {
                return ZoneId.of("America/Denver");
            }
            if ("HST".equals(id)) {
                return ZoneId.of("America/Honolulu");
            }
        }
        return ZoneId.of(id, ZoneId.SHORT_IDS);
    }

    private static TimeZone getTimeZone(String ID, boolean fallback) {
        TimeZone tz = ZoneInfo.getTimeZone(ID);
        if (tz == null && (tz = TimeZone.parseCustomTimeZone(ID)) == null && fallback) {
            tz = new ZoneInfo(GMT_ID, 0);
        }
        return tz;
    }

    public static synchronized String[] getAvailableIDs(int rawOffset) {
        return ZoneInfo.getAvailableIDs(rawOffset);
    }

    public static synchronized String[] getAvailableIDs() {
        return ZoneInfo.getAvailableIDs();
    }

    private static native String getSystemTimeZoneID(String var0);

    private static native String getSystemGMTOffsetID();

    public static TimeZone getDefault() {
        return (TimeZone)TimeZone.getDefaultRef().clone();
    }

    static TimeZone getDefaultRef() {
        TimeZone defaultZone = defaultTimeZone;
        if (defaultZone == null) {
            defaultZone = TimeZone.setDefaultZone();
            assert (defaultZone != null);
        }
        return defaultZone;
    }

    private static synchronized TimeZone setDefaultZone() {
        TimeZone tz;
        Properties props = GetPropertyAction.privilegedGetProperties();
        String zoneID = props.getProperty("user.timezone");
        if ((zoneID == null || zoneID.isEmpty()) && (zoneID = TimeZone.getSystemTimeZoneID(StaticProperty.javaHome())) == null) {
            zoneID = GMT_ID;
        }
        if ((tz = TimeZone.getTimeZone(zoneID, false)) == null) {
            String gmtOffsetID = TimeZone.getSystemGMTOffsetID();
            if (gmtOffsetID != null) {
                zoneID = gmtOffsetID;
            }
            tz = TimeZone.getTimeZone(zoneID, true);
        }
        assert (tz != null);
        String id = zoneID;
        props.setProperty("user.timezone", id);
        defaultTimeZone = tz;
        return tz;
    }

    public static void setDefault(TimeZone zone) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new PropertyPermission("user.timezone", "write"));
        }
        defaultTimeZone = zone == null ? null : (TimeZone)zone.clone();
    }

    public boolean hasSameRules(TimeZone other) {
        return other != null && this.getRawOffset() == other.getRawOffset() && this.useDaylightTime() == other.useDaylightTime();
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static final TimeZone parseCustomTimeZone(String id) {
        char c;
        int length = id.length();
        if (length < 5) return null;
        if (id.indexOf(GMT_ID) != 0) {
            return null;
        }
        ZoneInfo zi = ZoneInfoFile.getZoneInfo(id);
        if (zi != null) {
            return zi;
        }
        int index = 3;
        boolean negative = false;
        if ((c = id.charAt(index++)) == '-') {
            negative = true;
        } else if (c != '+') {
            return null;
        }
        int hours = 0;
        int minutes = 0;
        int num = 0;
        int countDelim = 0;
        int len = 0;
        while (index < length) {
            if ((c = id.charAt(index++)) == ':') {
                if (countDelim > 1) {
                    return null;
                }
                if (len == 0) return null;
                if (len > 2) {
                    return null;
                }
                if (countDelim == 0) {
                    hours = num;
                } else if (countDelim == 1) {
                    minutes = num;
                }
                ++countDelim;
                num = 0;
                len = 0;
                continue;
            }
            if (c < '0') return null;
            if (c > '9') {
                return null;
            }
            num = num * 10 + (c - 48);
            ++len;
        }
        if (index != length) {
            return null;
        }
        if (countDelim == 0) {
            if (len <= 2) {
                hours = num;
                minutes = 0;
                num = 0;
            } else {
                if (len > 4) return null;
                hours = num / 100;
                minutes = num % 100;
                num = 0;
            }
        } else if (countDelim == 1) {
            if (len != 2) return null;
            minutes = num;
            num = 0;
        } else if (len != 2) {
            return null;
        }
        if (hours > 23) return null;
        if (minutes > 59) return null;
        if (num > 59) {
            return null;
        }
        int gmtOffset = (hours * 3600 + minutes * 60 + num) * 1000;
        if (gmtOffset != 0) return ZoneInfoFile.getCustomTimeZone(id, negative ? -gmtOffset : gmtOffset);
        zi = ZoneInfoFile.getZoneInfo(GMT_ID);
        if (negative) {
            zi.setID("GMT-00:00");
            return zi;
        } else {
            zi.setID("GMT+00:00");
        }
        return zi;
    }
}

