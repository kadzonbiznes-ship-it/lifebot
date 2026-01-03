/*
 * Decompiled with CFR 0.152.
 */
package java.time.format;

import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.chrono.JapaneseChronology;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalField;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.util.locale.provider.CalendarDataUtility;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;

class DateTimeTextProvider {
    private static final ConcurrentMap<Map.Entry<TemporalField, Locale>, Object> CACHE = new ConcurrentHashMap<Map.Entry<TemporalField, Locale>, Object>(16, 0.75f, 2);
    private static final Comparator<Map.Entry<String, Long>> COMPARATOR = new Comparator<Map.Entry<String, Long>>(){

        @Override
        public int compare(Map.Entry<String, Long> obj1, Map.Entry<String, Long> obj2) {
            return obj2.getKey().length() - obj1.getKey().length();
        }
    };
    private static final DateTimeTextProvider INSTANCE = new DateTimeTextProvider();

    DateTimeTextProvider() {
    }

    static DateTimeTextProvider getInstance() {
        return INSTANCE;
    }

    public String getText(TemporalField field, long value, TextStyle style, Locale locale) {
        Object store = this.findStore(field, locale);
        if (store instanceof LocaleStore) {
            return ((LocaleStore)store).getText(value, style);
        }
        return null;
    }

    public String getText(Chronology chrono, TemporalField field, long value, TextStyle style, Locale locale) {
        int fieldValue;
        int fieldIndex;
        if (chrono == IsoChronology.INSTANCE || !(field instanceof ChronoField)) {
            return this.getText(field, value, style, locale);
        }
        if (field == ChronoField.ERA) {
            fieldIndex = 0;
            fieldValue = chrono == JapaneseChronology.INSTANCE ? (value == -999L ? 0 : (int)value + 2) : (int)value;
        } else if (field == ChronoField.MONTH_OF_YEAR) {
            fieldIndex = 2;
            fieldValue = (int)value - 1;
        } else if (field == ChronoField.DAY_OF_WEEK) {
            fieldIndex = 7;
            fieldValue = (int)value + 1;
            if (fieldValue > 7) {
                fieldValue = 1;
            }
        } else if (field == ChronoField.AMPM_OF_DAY) {
            fieldIndex = 9;
            fieldValue = (int)value;
        } else {
            return null;
        }
        return CalendarDataUtility.retrieveJavaTimeFieldValueName(chrono.getCalendarType(), fieldIndex, fieldValue, style.toCalendarStyle(), locale);
    }

    public Iterator<Map.Entry<String, Long>> getTextIterator(TemporalField field, TextStyle style, Locale locale) {
        Object store = this.findStore(field, locale);
        if (store instanceof LocaleStore) {
            return ((LocaleStore)store).getTextIterator(style);
        }
        return null;
    }

    public Iterator<Map.Entry<String, Long>> getTextIterator(Chronology chrono, TemporalField field, TextStyle style, Locale locale) {
        int fieldIndex;
        if (chrono == IsoChronology.INSTANCE || !(field instanceof ChronoField)) {
            return this.getTextIterator(field, style, locale);
        }
        switch ((ChronoField)field) {
            case ERA: {
                fieldIndex = 0;
                break;
            }
            case MONTH_OF_YEAR: {
                fieldIndex = 2;
                break;
            }
            case DAY_OF_WEEK: {
                fieldIndex = 7;
                break;
            }
            case AMPM_OF_DAY: {
                fieldIndex = 9;
                break;
            }
            default: {
                return null;
            }
        }
        int calendarStyle = style == null ? 0 : style.toCalendarStyle();
        Map<String, Integer> map = CalendarDataUtility.retrieveJavaTimeFieldValueNames(chrono.getCalendarType(), fieldIndex, calendarStyle, locale);
        if (map == null) {
            return null;
        }
        ArrayList<Map.Entry<String, Long>> list = new ArrayList<Map.Entry<String, Long>>(map.size());
        switch (fieldIndex) {
            case 0: {
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    int era = entry.getValue();
                    if (chrono == JapaneseChronology.INSTANCE) {
                        era = era == 0 ? -999 : (era -= 2);
                    }
                    list.add(DateTimeTextProvider.createEntry(entry.getKey(), Long.valueOf(era)));
                }
                break;
            }
            case 2: {
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    list.add(DateTimeTextProvider.createEntry(entry.getKey(), Long.valueOf(entry.getValue() + 1)));
                }
                break;
            }
            case 7: {
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    list.add(DateTimeTextProvider.createEntry(entry.getKey(), Long.valueOf(DateTimeTextProvider.toWeekDay(entry.getValue()))));
                }
                break;
            }
            default: {
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    list.add(DateTimeTextProvider.createEntry(entry.getKey(), Long.valueOf(entry.getValue().intValue())));
                }
            }
        }
        return list.iterator();
    }

    private Object findStore(TemporalField field, Locale locale) {
        Map.Entry<TemporalField, Locale> key = DateTimeTextProvider.createEntry(field, locale);
        return CACHE.computeIfAbsent(key, e -> this.createStore((TemporalField)e.getKey(), (Locale)e.getValue()));
    }

    private static int toWeekDay(int calWeekDay) {
        if (calWeekDay == 1) {
            return 7;
        }
        return calWeekDay - 1;
    }

    private Object createStore(TemporalField field, Locale locale) {
        HashMap<TextStyle, Map<Long, String>> styleMap = new HashMap<TextStyle, Map<Long, String>>();
        if (field == ChronoField.ERA) {
            for (TextStyle textStyle : TextStyle.values()) {
                Map<String, Integer> displayNames;
                if (textStyle.isStandalone() || (displayNames = CalendarDataUtility.retrieveJavaTimeFieldValueNames("gregory", 0, textStyle.toCalendarStyle(), locale)) == null) continue;
                HashMap<Long, String> map = new HashMap<Long, String>();
                for (Map.Entry<String, Integer> entry : displayNames.entrySet()) {
                    map.put(Long.valueOf(entry.getValue().intValue()), entry.getKey());
                }
                if (map.isEmpty()) continue;
                styleMap.put(textStyle, map);
            }
            return new LocaleStore(styleMap);
        }
        if (field == ChronoField.MONTH_OF_YEAR) {
            for (TextStyle textStyle : TextStyle.values()) {
                HashMap<Long, String> map = new HashMap<Long, String>();
                if (textStyle.equals((Object)TextStyle.NARROW) || textStyle.equals((Object)TextStyle.NARROW_STANDALONE)) {
                    String name;
                    for (int month = 0; month <= 11 && (name = CalendarDataUtility.retrieveJavaTimeFieldValueName("gregory", 2, month, textStyle.toCalendarStyle(), locale)) != null; ++month) {
                        map.put((long)month + 1L, name);
                    }
                } else {
                    Map<String, Integer> displayNames = CalendarDataUtility.retrieveJavaTimeFieldValueNames("gregory", 2, textStyle.toCalendarStyle(), locale);
                    if (displayNames != null) {
                        for (Map.Entry<String, Integer> entry : displayNames.entrySet()) {
                            map.put(Long.valueOf(entry.getValue() + 1), entry.getKey());
                        }
                    } else {
                        String name;
                        for (int month = 0; month <= 11 && (name = CalendarDataUtility.retrieveJavaTimeFieldValueName("gregory", 2, month, textStyle.toCalendarStyle(), locale)) != null; ++month) {
                            map.put((long)month + 1L, name);
                        }
                    }
                }
                if (map.isEmpty()) continue;
                styleMap.put(textStyle, map);
            }
            return new LocaleStore(styleMap);
        }
        if (field == ChronoField.DAY_OF_WEEK) {
            for (TextStyle textStyle : TextStyle.values()) {
                HashMap<Long, String> map = new HashMap<Long, String>();
                if (textStyle.equals((Object)TextStyle.NARROW) || textStyle.equals((Object)TextStyle.NARROW_STANDALONE)) {
                    String name;
                    for (int wday = 1; wday <= 7 && (name = CalendarDataUtility.retrieveJavaTimeFieldValueName("gregory", 7, wday, textStyle.toCalendarStyle(), locale)) != null; ++wday) {
                        map.put(Long.valueOf(DateTimeTextProvider.toWeekDay(wday)), name);
                    }
                } else {
                    Map<String, Integer> displayNames = CalendarDataUtility.retrieveJavaTimeFieldValueNames("gregory", 7, textStyle.toCalendarStyle(), locale);
                    if (displayNames != null) {
                        for (Map.Entry<String, Integer> entry : displayNames.entrySet()) {
                            map.put(Long.valueOf(DateTimeTextProvider.toWeekDay(entry.getValue())), entry.getKey());
                        }
                    } else {
                        String name;
                        for (int wday = 1; wday <= 7 && (name = CalendarDataUtility.retrieveJavaTimeFieldValueName("gregory", 7, wday, textStyle.toCalendarStyle(), locale)) != null; ++wday) {
                            map.put(Long.valueOf(DateTimeTextProvider.toWeekDay(wday)), name);
                        }
                    }
                }
                if (map.isEmpty()) continue;
                styleMap.put(textStyle, map);
            }
            return new LocaleStore(styleMap);
        }
        if (field == ChronoField.AMPM_OF_DAY) {
            for (TextStyle textStyle : TextStyle.values()) {
                Map<String, Integer> displayNames;
                if (textStyle.isStandalone() || (displayNames = CalendarDataUtility.retrieveJavaTimeFieldValueNames("gregory", 9, textStyle.toCalendarStyle(), locale)) == null) continue;
                HashMap<Long, String> map = new HashMap<Long, String>();
                for (Map.Entry<String, Integer> entry : displayNames.entrySet()) {
                    map.put(Long.valueOf(entry.getValue().intValue()), entry.getKey());
                }
                if (map.isEmpty()) continue;
                styleMap.put(textStyle, map);
            }
            return new LocaleStore(styleMap);
        }
        if (field == IsoFields.QUARTER_OF_YEAR) {
            String[] keys = new String[]{"QuarterNames", "standalone.QuarterNames", "QuarterAbbreviations", "standalone.QuarterAbbreviations", "QuarterNarrows", "standalone.QuarterNarrows"};
            for (int i = 0; i < keys.length; ++i) {
                String[] names = (String[])DateTimeTextProvider.getLocalizedResource(keys[i], locale);
                if (names == null) continue;
                HashMap<Long, String> map = new HashMap<Long, String>();
                for (int q = 0; q < names.length; ++q) {
                    map.put(Long.valueOf(q + 1), names[q]);
                }
                styleMap.put(TextStyle.values()[i], map);
            }
            return new LocaleStore(styleMap);
        }
        return "";
    }

    private static <A, B> Map.Entry<A, B> createEntry(A text, B field) {
        return new AbstractMap.SimpleImmutableEntry<A, B>(text, field);
    }

    static <T> T getLocalizedResource(String key, Locale locale) {
        LocaleResources lr = LocaleProviderAdapter.getResourceBundleBased().getLocaleResources(CalendarDataUtility.findRegionOverride(locale));
        ResourceBundle rb = lr.getJavaTimeFormatData();
        return (T)(rb.containsKey(key) ? rb.getObject(key) : null);
    }

    static final class LocaleStore {
        private final Map<TextStyle, Map<Long, String>> valueTextMap;
        private final Map<TextStyle, List<Map.Entry<String, Long>>> parsable;

        LocaleStore(Map<TextStyle, Map<Long, String>> valueTextMap) {
            this.valueTextMap = valueTextMap;
            HashMap<TextStyle, List<Map.Entry<String, Long>>> map = new HashMap<TextStyle, List<Map.Entry<String, Long>>>();
            ArrayList<Object> allList = new ArrayList<Object>();
            for (Map.Entry<TextStyle, Map<Long, String>> vtmEntry : valueTextMap.entrySet()) {
                HashMap<String, Map.Entry<String, Long>> reverse = new HashMap<String, Map.Entry<String, Long>>();
                for (Map.Entry<Long, String> entry : vtmEntry.getValue().entrySet()) {
                    if (reverse.put(entry.getValue(), DateTimeTextProvider.createEntry(entry.getValue(), entry.getKey())) == null) continue;
                }
                ArrayList list = new ArrayList(reverse.values());
                list.sort(COMPARATOR);
                map.put(vtmEntry.getKey(), list);
                allList.addAll(list);
                map.put(null, allList);
            }
            allList.sort(COMPARATOR);
            this.parsable = map;
        }

        String getText(long value, TextStyle style) {
            Map<Long, String> map = this.valueTextMap.get((Object)style);
            return map != null ? map.get(value) : null;
        }

        Iterator<Map.Entry<String, Long>> getTextIterator(TextStyle style) {
            List<Map.Entry<String, Long>> list = this.parsable.get((Object)style);
            return list != null ? list.iterator() : null;
        }
    }
}

