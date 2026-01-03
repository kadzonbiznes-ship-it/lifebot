/*
 * Decompiled with CFR 0.152.
 */
package sun.util.calendar;

import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.Era;
import sun.util.calendar.Gregorian;
import sun.util.calendar.LocalGregorianCalendar;

public abstract class CalendarSystem {
    private static volatile boolean initialized;
    private static ConcurrentMap<String, String> names;
    private static ConcurrentMap<String, CalendarSystem> calendars;
    private static final String PACKAGE_NAME = "sun.util.calendar.";
    private static final String[] namePairs;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void initNames() {
        ConcurrentHashMap<String, String> nameMap = new ConcurrentHashMap<String, String>();
        StringBuilder clName = new StringBuilder();
        for (int i = 0; i < namePairs.length; i += 2) {
            clName.setLength(0);
            String cl = clName.append(PACKAGE_NAME).append(namePairs[i + 1]).toString();
            nameMap.put(namePairs[i], cl);
        }
        Class<CalendarSystem> clazz = CalendarSystem.class;
        synchronized (CalendarSystem.class) {
            if (!initialized) {
                names = nameMap;
                calendars = new ConcurrentHashMap<String, CalendarSystem>();
                initialized = true;
            }
            // ** MonitorExit[var2_3] (shouldn't be in output)
            return;
        }
    }

    public static Gregorian getGregorianCalendar() {
        return GregorianHolder.GREGORIAN_INSTANCE;
    }

    public static CalendarSystem forName(String calendarName) {
        CalendarSystem cal;
        if ("gregorian".equals(calendarName)) {
            return GregorianHolder.GREGORIAN_INSTANCE;
        }
        if (!initialized) {
            CalendarSystem.initNames();
        }
        if ((cal = (CalendarSystem)calendars.get(calendarName)) != null) {
            return cal;
        }
        String className = (String)names.get(calendarName);
        if (className == null) {
            return null;
        }
        if (className.endsWith("LocalGregorianCalendar")) {
            cal = LocalGregorianCalendar.getLocalGregorianCalendar(calendarName);
        } else {
            try {
                Object tmp = Class.forName(className).newInstance();
                cal = (CalendarSystem)tmp;
            }
            catch (Exception e) {
                throw new InternalError(e);
            }
        }
        if (cal == null) {
            return null;
        }
        CalendarSystem cs = calendars.putIfAbsent(calendarName, cal);
        return cs == null ? cal : cs;
    }

    public abstract String getName();

    public abstract CalendarDate getCalendarDate();

    public abstract CalendarDate getCalendarDate(long var1);

    public abstract CalendarDate getCalendarDate(long var1, CalendarDate var3);

    public abstract CalendarDate getCalendarDate(long var1, TimeZone var3);

    public abstract CalendarDate newCalendarDate();

    public abstract CalendarDate newCalendarDate(TimeZone var1);

    public abstract long getTime(CalendarDate var1);

    public abstract int getYearLength(CalendarDate var1);

    public abstract int getMonthLength(CalendarDate var1);

    public abstract Era getEra(String var1);

    public abstract Era[] getEras();

    public abstract CalendarDate getNthDayOfWeek(int var1, int var2, CalendarDate var3);

    public abstract CalendarDate setTimeOfDay(CalendarDate var1, int var2);

    public abstract boolean validate(CalendarDate var1);

    public abstract boolean normalize(CalendarDate var1);

    static {
        namePairs = new String[]{"gregorian", "Gregorian", "japanese", "LocalGregorianCalendar", "julian", "JulianCalendar"};
    }

    private static final class GregorianHolder {
        private static final Gregorian GREGORIAN_INSTANCE = new Gregorian();

        private GregorianHolder() {
        }
    }
}

