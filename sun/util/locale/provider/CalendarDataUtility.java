/*
 * Decompiled with CFR 0.152.
 */
package sun.util.locale.provider;

import java.util.Locale;
import java.util.Map;
import java.util.spi.CalendarDataProvider;
import java.util.spi.CalendarNameProvider;
import sun.util.locale.provider.CalendarNameProviderImpl;
import sun.util.locale.provider.LocaleServiceProviderPool;

public class CalendarDataUtility {
    public static final String FIRST_DAY_OF_WEEK = "firstDayOfWeek";
    public static final String MINIMAL_DAYS_IN_FIRST_WEEK = "minimalDaysInFirstWeek";
    private static final Locale.Builder OVERRIDE_BUILDER = new Locale.Builder();

    private CalendarDataUtility() {
    }

    public static int retrieveFirstDayOfWeek(Locale locale) {
        LocaleServiceProviderPool pool;
        Integer value;
        String fw;
        if (locale.hasExtensions() && (fw = locale.getUnicodeLocaleType("fw")) != null) {
            switch (fw.toLowerCase(Locale.ROOT)) {
                case "mon": {
                    return 2;
                }
                case "tue": {
                    return 3;
                }
                case "wed": {
                    return 4;
                }
                case "thu": {
                    return 5;
                }
                case "fri": {
                    return 6;
                }
                case "sat": {
                    return 7;
                }
                case "sun": {
                    return 1;
                }
            }
        }
        return (value = (pool = LocaleServiceProviderPool.getPool(CalendarDataProvider.class)).getLocalizedObject(CalendarWeekParameterGetter.INSTANCE, CalendarDataUtility.findRegionOverride(locale), true, FIRST_DAY_OF_WEEK, new Object[0])) != null && value >= 1 && value <= 7 ? value : 1;
    }

    public static int retrieveMinimalDaysInFirstWeek(Locale locale) {
        LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(CalendarDataProvider.class);
        Integer value = pool.getLocalizedObject(CalendarWeekParameterGetter.INSTANCE, CalendarDataUtility.findRegionOverride(locale), true, MINIMAL_DAYS_IN_FIRST_WEEK, new Object[0]);
        return value != null && value >= 1 && value <= 7 ? value : 1;
    }

    public static String retrieveFieldValueName(String id, int field, int value, int style, Locale locale) {
        LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(CalendarNameProvider.class);
        return pool.getLocalizedObject(CalendarFieldValueNameGetter.INSTANCE, locale, CalendarDataUtility.normalizeCalendarType(id), field, value, style, false);
    }

    public static String retrieveJavaTimeFieldValueName(String id, int field, int value, int style, Locale locale) {
        LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(CalendarNameProvider.class);
        String name = pool.getLocalizedObject(CalendarFieldValueNameGetter.INSTANCE, locale, CalendarDataUtility.normalizeCalendarType(id), field, value, style, true);
        if (name == null) {
            name = pool.getLocalizedObject(CalendarFieldValueNameGetter.INSTANCE, locale, CalendarDataUtility.normalizeCalendarType(id), field, value, style, false);
        }
        return name;
    }

    public static Map<String, Integer> retrieveFieldValueNames(String id, int field, int style, Locale locale) {
        LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(CalendarNameProvider.class);
        return pool.getLocalizedObject(CalendarFieldValueNamesMapGetter.INSTANCE, locale, CalendarDataUtility.normalizeCalendarType(id), field, style, false);
    }

    public static Map<String, Integer> retrieveJavaTimeFieldValueNames(String id, int field, int style, Locale locale) {
        LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(CalendarNameProvider.class);
        Map<String, Integer> map = pool.getLocalizedObject(CalendarFieldValueNamesMapGetter.INSTANCE, locale, CalendarDataUtility.normalizeCalendarType(id), field, style, true);
        if (map == null) {
            map = pool.getLocalizedObject(CalendarFieldValueNamesMapGetter.INSTANCE, locale, CalendarDataUtility.normalizeCalendarType(id), field, style, false);
        }
        return map;
    }

    public static Locale findRegionOverride(Locale l) {
        String rg = l.getUnicodeLocaleType("rg");
        Locale override = l;
        if (rg != null && rg.length() == 6 && (rg = rg.toUpperCase(Locale.ROOT)).charAt(0) >= 'A' && rg.charAt(0) <= 'Z' && rg.charAt(1) >= 'A' && rg.charAt(1) <= 'Z' && rg.substring(2).equals("ZZZZ")) {
            override = OVERRIDE_BUILDER.clear().setLocale(l).setRegion(rg.substring(0, 2)).build();
        }
        return override;
    }

    static String normalizeCalendarType(String requestID) {
        String type = requestID.equals("gregorian") || requestID.equals("iso8601") ? "gregory" : (requestID.startsWith("islamic") ? "islamic" : requestID);
        return type;
    }

    private static class CalendarWeekParameterGetter
    implements LocaleServiceProviderPool.LocalizedObjectGetter<CalendarDataProvider, Integer> {
        private static final CalendarWeekParameterGetter INSTANCE = new CalendarWeekParameterGetter();

        private CalendarWeekParameterGetter() {
        }

        @Override
        public Integer getObject(CalendarDataProvider calendarDataProvider, Locale locale, String requestID, Object ... params) {
            int value;
            assert (params.length == 0);
            switch (requestID) {
                case "firstDayOfWeek": {
                    value = calendarDataProvider.getFirstDayOfWeek(locale);
                    if (value != 0) break;
                    value = 2;
                    break;
                }
                case "minimalDaysInFirstWeek": {
                    value = calendarDataProvider.getMinimalDaysInFirstWeek(locale);
                    if (value != 0) break;
                    value = 1;
                    break;
                }
                default: {
                    throw new InternalError("invalid requestID: " + requestID);
                }
            }
            assert (value != 0);
            return value;
        }
    }

    private static class CalendarFieldValueNameGetter
    implements LocaleServiceProviderPool.LocalizedObjectGetter<CalendarNameProvider, String> {
        private static final CalendarFieldValueNameGetter INSTANCE = new CalendarFieldValueNameGetter();

        private CalendarFieldValueNameGetter() {
        }

        @Override
        public String getObject(CalendarNameProvider calendarNameProvider, Locale locale, String requestID, Object ... params) {
            assert (params.length == 4);
            int field = (Integer)params[0];
            int value = (Integer)params[1];
            int style = (Integer)params[2];
            boolean javatime = (Boolean)params[3];
            if (javatime && calendarNameProvider instanceof CalendarNameProviderImpl) {
                String name = ((CalendarNameProviderImpl)calendarNameProvider).getJavaTimeDisplayName(requestID, field, value, style, locale);
                return name;
            }
            return calendarNameProvider.getDisplayName(requestID, field, value, style, locale);
        }
    }

    private static class CalendarFieldValueNamesMapGetter
    implements LocaleServiceProviderPool.LocalizedObjectGetter<CalendarNameProvider, Map<String, Integer>> {
        private static final CalendarFieldValueNamesMapGetter INSTANCE = new CalendarFieldValueNamesMapGetter();

        private CalendarFieldValueNamesMapGetter() {
        }

        @Override
        public Map<String, Integer> getObject(CalendarNameProvider calendarNameProvider, Locale locale, String requestID, Object ... params) {
            assert (params.length == 3);
            int field = (Integer)params[0];
            int style = (Integer)params[1];
            boolean javatime = (Boolean)params[2];
            if (javatime && calendarNameProvider instanceof CalendarNameProviderImpl) {
                Map<String, Integer> map = ((CalendarNameProviderImpl)calendarNameProvider).getJavaTimeDisplayNames(requestID, field, style, locale);
                return map;
            }
            return calendarNameProvider.getDisplayNames(requestID, field, style, locale);
        }
    }
}

