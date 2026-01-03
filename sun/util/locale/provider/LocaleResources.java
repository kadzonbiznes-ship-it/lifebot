/*
 * Decompiled with CFR 0.152.
 */
package sun.util.locale.provider;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import sun.security.action.GetPropertyAction;
import sun.util.locale.provider.CalendarDataUtility;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.ResourceBundleBasedAdapter;
import sun.util.locale.provider.TimeZoneNameUtility;
import sun.util.resources.LocaleData;
import sun.util.resources.OpenListResourceBundle;
import sun.util.resources.ParallelListResourceBundle;
import sun.util.resources.TimeZoneNamesBundle;

public class LocaleResources {
    private final Locale locale;
    private final LocaleData localeData;
    private final LocaleProviderAdapter.Type type;
    private final ConcurrentMap<String, ResourceReference> cache = new ConcurrentHashMap<String, ResourceReference>();
    private final ReferenceQueue<Object> referenceQueue = new ReferenceQueue();
    private static final String BREAK_ITERATOR_INFO = "BII.";
    private static final String CALENDAR_DATA = "CALD.";
    private static final String COLLATION_DATA = "COLD.";
    private static final String DECIMAL_FORMAT_SYMBOLS_DATA_CACHEKEY = "DFSD";
    private static final String CURRENCY_NAMES = "CN.";
    private static final String LOCALE_NAMES = "LN.";
    private static final String TIME_ZONE_NAMES = "TZN.";
    private static final String ZONE_IDS_CACHEKEY = "ZID";
    private static final String CALENDAR_NAMES = "CALN.";
    private static final String NUMBER_PATTERNS_CACHEKEY = "NP";
    private static final String COMPACT_NUMBER_PATTERNS_CACHEKEY = "CNP";
    private static final String DATE_TIME_PATTERN = "DTP.";
    private static final String RULES_CACHEKEY = "RULE";
    private static final String SKELETON_PATTERN = "SP.";
    private static final String SKELETON_INPUT_REGIONS_KEY = "DateFormatItemInputRegions";
    private static final String TZNB_EXCITY_PREFIX = "timezone.excity.";
    private static final Object NULLOBJECT = new Object();
    private static final Pattern VALID_SKELETON_PATTERN = Pattern.compile("(?<date>G{0,5}y*Q{0,5}M{0,5}w*E{0,5}d{0,2})(?<time>B{0,5}[hHjC]{0,2}m{0,2}s{0,2}[vz]{0,4})");
    private static Map<String, Map<String, String>> inputSkeletons;
    private String jPattern;
    private String CPattern;
    private static final boolean TRACE_ON;

    LocaleResources(ResourceBundleBasedAdapter adapter, Locale locale) {
        this.locale = locale;
        this.localeData = adapter.getLocaleData();
        this.type = ((LocaleProviderAdapter)((Object)adapter)).getAdapterType();
    }

    private void removeEmptyReferences() {
        Reference<Object> ref;
        while ((ref = this.referenceQueue.poll()) != null) {
            this.cache.remove(((ResourceReference)ref).getCacheKey());
        }
    }

    Object getBreakIteratorInfo(String key) {
        Object biInfo;
        String cacheKey = BREAK_ITERATOR_INFO + key;
        this.removeEmptyReferences();
        ResourceReference data = (ResourceReference)this.cache.get(cacheKey);
        if (data == null || (biInfo = data.get()) == null) {
            biInfo = this.localeData.getBreakIteratorInfo(this.locale).getObject(key);
            this.cache.put(cacheKey, new ResourceReference(cacheKey, biInfo, this.referenceQueue));
        }
        return biInfo;
    }

    byte[] getBreakIteratorResources(String key) {
        return (byte[])this.localeData.getBreakIteratorResources(this.locale).getObject(key);
    }

    public String getCalendarData(String key) {
        String caldata = "";
        String cacheKey = CALENDAR_DATA + key;
        this.removeEmptyReferences();
        ResourceReference data = (ResourceReference)this.cache.get(cacheKey);
        if (data == null || (caldata = (String)data.get()) == null) {
            ResourceBundle rb = this.localeData.getCalendarData(this.locale);
            if (rb.containsKey(key)) {
                caldata = rb.getString(key);
            }
            this.cache.put(cacheKey, new ResourceReference(cacheKey, caldata, this.referenceQueue));
        }
        return caldata;
    }

    public String getCollationData() {
        String key = "Rule";
        String cacheKey = COLLATION_DATA;
        String coldata = "";
        try {
            String type = this.locale.getUnicodeLocaleType("co");
            if (type != null && !type.isEmpty() && !type.equalsIgnoreCase("standard")) {
                key = key + "." + type;
                cacheKey = cacheKey + type;
            }
        }
        catch (IllegalArgumentException type) {
            // empty catch block
        }
        this.removeEmptyReferences();
        ResourceReference data = (ResourceReference)this.cache.get(cacheKey);
        if (data == null || (coldata = (String)data.get()) == null) {
            ResourceBundle rb = this.localeData.getCollationData(this.locale);
            if (rb.containsKey(key)) {
                coldata = rb.getString(key);
            }
            this.cache.put(cacheKey, new ResourceReference(cacheKey, coldata, this.referenceQueue));
        }
        return coldata;
    }

    public Object[] getDecimalFormatSymbolsData() {
        Object[] dfsdata;
        this.removeEmptyReferences();
        ResourceReference data = (ResourceReference)this.cache.get(DECIMAL_FORMAT_SYMBOLS_DATA_CACHEKEY);
        if (data == null || (dfsdata = (Object[])data.get()) == null) {
            ResourceBundle rb = this.localeData.getNumberFormatData(this.locale);
            dfsdata = new Object[3];
            dfsdata[0] = this.getNumberStrings(rb, "NumberElements");
            this.cache.put(DECIMAL_FORMAT_SYMBOLS_DATA_CACHEKEY, new ResourceReference(DECIMAL_FORMAT_SYMBOLS_DATA_CACHEKEY, dfsdata, this.referenceQueue));
        }
        return dfsdata;
    }

    private String[] getNumberStrings(ResourceBundle rb, String type) {
        String key;
        String[] ret = null;
        String numSys = this.locale.getUnicodeLocaleType("nu");
        if (numSys != null && rb.containsKey(key = numSys + "." + type)) {
            ret = rb.getStringArray(key);
        }
        if (ret == null && rb.containsKey("DefaultNumberingSystem") && rb.containsKey(key = rb.getString("DefaultNumberingSystem") + "." + type)) {
            ret = rb.getStringArray(key);
        }
        if (ret == null) {
            ret = rb.getStringArray(type);
        }
        return ret;
    }

    public String getCurrencyName(String key) {
        OpenListResourceBundle olrb;
        Object currencyName = null;
        String cacheKey = CURRENCY_NAMES + key;
        this.removeEmptyReferences();
        ResourceReference data = (ResourceReference)this.cache.get(cacheKey);
        if (data != null) {
            Object t = data.get();
            currencyName = t;
            if (t != null) {
                if (currencyName.equals(NULLOBJECT)) {
                    currencyName = null;
                }
                return (String)currencyName;
            }
        }
        if ((olrb = this.localeData.getCurrencyNames(this.locale)).containsKey(key)) {
            currencyName = olrb.getObject(key);
            this.cache.put(cacheKey, new ResourceReference(cacheKey, currencyName, this.referenceQueue));
        }
        return (String)currencyName;
    }

    public String getLocaleName(String key) {
        OpenListResourceBundle olrb;
        Object localeName = null;
        String cacheKey = LOCALE_NAMES + key;
        this.removeEmptyReferences();
        ResourceReference data = (ResourceReference)this.cache.get(cacheKey);
        if (data != null) {
            Object t = data.get();
            localeName = t;
            if (t != null) {
                if (localeName.equals(NULLOBJECT)) {
                    localeName = null;
                }
                return (String)localeName;
            }
        }
        if ((olrb = this.localeData.getLocaleNames(this.locale)).containsKey(key)) {
            localeName = olrb.getObject(key);
            this.cache.put(cacheKey, new ResourceReference(cacheKey, localeName, this.referenceQueue));
        }
        return (String)localeName;
    }

    public Object getTimeZoneNames(String key) {
        String[] val;
        block13: {
            String cacheKey;
            block12: {
                val = null;
                cacheKey = TIME_ZONE_NAMES + key;
                this.removeEmptyReferences();
                ResourceReference data = (ResourceReference)this.cache.get(cacheKey);
                if (Objects.isNull(data)) break block12;
                Object t = data.get();
                val = (String[])t;
                if (!Objects.isNull(t)) break block13;
            }
            TimeZoneNamesBundle tznb = this.localeData.getTimeZoneNames(this.locale);
            if (key.startsWith(TZNB_EXCITY_PREFIX)) {
                if (tznb.containsKey(key)) {
                    val = tznb.getString(key);
                    assert (val instanceof String);
                    LocaleResources.trace("tznb: %s key: %s, val: %s\n", tznb, key, val);
                }
            } else {
                String[] names = null;
                if (tznb.containsKey(key)) {
                    names = tznb.getStringArray(key);
                } else {
                    String tz = TimeZoneNameUtility.canonicalTZID(key).orElse(key);
                    if (tznb.containsKey(tz)) {
                        names = tznb.getStringArray(tz);
                    }
                }
                if (names != null) {
                    names[0] = key;
                    LocaleResources.trace("tznb: %s key: %s, names: %s, %s, %s, %s, %s, %s, %s\n", tznb, key, names[0], names[1], names[2], names[3], names[4], names[5], names[6]);
                    val = names;
                }
            }
            if (val != null) {
                this.cache.put(cacheKey, new ResourceReference(cacheKey, val, this.referenceQueue));
            }
        }
        return val;
    }

    Set<String> getZoneIDs() {
        Set<String> zoneIDs;
        this.removeEmptyReferences();
        ResourceReference data = (ResourceReference)this.cache.get(ZONE_IDS_CACHEKEY);
        if (data == null || (zoneIDs = (Set<String>)data.get()) == null) {
            TimeZoneNamesBundle rb = this.localeData.getTimeZoneNames(this.locale);
            zoneIDs = rb.keySet();
            this.cache.put(ZONE_IDS_CACHEKEY, new ResourceReference(ZONE_IDS_CACHEKEY, zoneIDs, this.referenceQueue));
        }
        return zoneIDs;
    }

    String[][] getZoneStrings() {
        TimeZoneNamesBundle rb = this.localeData.getTimeZoneNames(this.locale);
        Set<String> keyset = this.getZoneIDs();
        LinkedHashSet<String[]> value = new LinkedHashSet<String[]>();
        HashSet<String> tzIds = new HashSet<String>(Arrays.asList(TimeZone.getAvailableIDs()));
        for (String key : keyset) {
            if (key.startsWith(TZNB_EXCITY_PREFIX)) continue;
            value.add(rb.getStringArray(key));
            tzIds.remove(key);
        }
        if (this.type == LocaleProviderAdapter.Type.CLDR) {
            tzIds.stream().filter(i -> !i.startsWith("Etc/GMT") && !i.startsWith("GMT") && !i.startsWith("SystemV")).forEach(tzid -> {
                String[] val = new String[7];
                if (keyset.contains(tzid)) {
                    val = rb.getStringArray((String)tzid);
                } else {
                    String canonID = TimeZoneNameUtility.canonicalTZID(tzid).orElse((String)tzid);
                    if (keyset.contains(canonID)) {
                        val = rb.getStringArray(canonID);
                    }
                }
                val[0] = tzid;
                value.add(val);
            });
        }
        return (String[][])value.toArray((T[])new String[0][]);
    }

    String[] getCalendarNames(String key) {
        ResourceBundle rb;
        String[] names = null;
        String cacheKey = CALENDAR_NAMES + key;
        this.removeEmptyReferences();
        ResourceReference data = (ResourceReference)this.cache.get(cacheKey);
        if ((data == null || (names = (String[])data.get()) == null) && (rb = this.localeData.getDateFormatData(this.locale)).containsKey(key)) {
            names = rb.getStringArray(key);
            this.cache.put(cacheKey, new ResourceReference(cacheKey, names, this.referenceQueue));
        }
        return names;
    }

    String[] getJavaTimeNames(String key) {
        ResourceBundle rb;
        String[] names = null;
        String cacheKey = CALENDAR_NAMES + key;
        this.removeEmptyReferences();
        ResourceReference data = (ResourceReference)this.cache.get(cacheKey);
        if ((data == null || (names = (String[])data.get()) == null) && (rb = this.getJavaTimeFormatData()).containsKey(key)) {
            names = rb.getStringArray(key);
            this.cache.put(cacheKey, new ResourceReference(cacheKey, names, this.referenceQueue));
        }
        return names;
    }

    public String getDateTimePattern(int timeStyle, int dateStyle, Calendar cal) {
        if (cal == null) {
            cal = Calendar.getInstance(this.locale);
        }
        return this.getDateTimePattern(null, timeStyle, dateStyle, cal.getCalendarType());
    }

    public String getJavaTimeDateTimePattern(int timeStyle, int dateStyle, String calType) {
        String pattern = this.getDateTimePattern("java.time.", timeStyle, dateStyle, calType = CalendarDataUtility.normalizeCalendarType(calType));
        if (pattern == null) {
            pattern = this.getDateTimePattern(null, timeStyle, dateStyle, calType);
        }
        return pattern;
    }

    private String getDateTimePattern(String prefix, int timeStyle, int dateStyle, String calType) {
        String pattern;
        String timePattern = null;
        String datePattern = null;
        if (timeStyle >= 0) {
            if (prefix != null) {
                timePattern = this.getDateTimePattern(prefix, "TimePatterns", timeStyle, calType);
            }
            if (timePattern == null) {
                timePattern = this.getDateTimePattern(null, "TimePatterns", timeStyle, calType);
            }
        }
        if (dateStyle >= 0) {
            if (prefix != null) {
                datePattern = this.getDateTimePattern(prefix, "DatePatterns", dateStyle, calType);
            }
            if (datePattern == null) {
                datePattern = this.getDateTimePattern(null, "DatePatterns", dateStyle, calType);
            }
        }
        if (timeStyle >= 0) {
            if (dateStyle >= 0) {
                String dateTimePattern = null;
                int dateTimeStyle = Math.max(dateStyle, timeStyle);
                if (prefix != null) {
                    dateTimePattern = this.getDateTimePattern(prefix, "DateTimePatterns", dateTimeStyle, calType);
                }
                if (dateTimePattern == null) {
                    dateTimePattern = this.getDateTimePattern(null, "DateTimePatterns", dateTimeStyle, calType);
                }
                pattern = switch (Objects.requireNonNull(dateTimePattern)) {
                    case "{1} {0}" -> datePattern + " " + timePattern;
                    case "{0} {1}" -> timePattern + " " + datePattern;
                    default -> MessageFormat.format(dateTimePattern.replaceAll("'", "''"), timePattern, datePattern);
                };
            } else {
                pattern = timePattern;
            }
        } else if (dateStyle >= 0) {
            pattern = datePattern;
        } else {
            throw new IllegalArgumentException("No date or time style specified");
        }
        return pattern;
    }

    public String[] getNumberPatterns() {
        String[] numberPatterns;
        this.removeEmptyReferences();
        ResourceReference data = (ResourceReference)this.cache.get(NUMBER_PATTERNS_CACHEKEY);
        if (data == null || (numberPatterns = (String[])data.get()) == null) {
            ResourceBundle resource = this.localeData.getNumberFormatData(this.locale);
            numberPatterns = this.getNumberStrings(resource, "NumberPatterns");
            this.cache.put(NUMBER_PATTERNS_CACHEKEY, new ResourceReference(NUMBER_PATTERNS_CACHEKEY, numberPatterns, this.referenceQueue));
        }
        return numberPatterns;
    }

    public String[] getCNPatterns(NumberFormat.Style formatStyle) {
        String[] compactNumberPatterns;
        Objects.requireNonNull(formatStyle);
        this.removeEmptyReferences();
        String width = formatStyle == NumberFormat.Style.LONG ? "long" : "short";
        String cacheKey = width + "." + COMPACT_NUMBER_PATTERNS_CACHEKEY;
        ResourceReference data = (ResourceReference)this.cache.get(cacheKey);
        if (data == null || (compactNumberPatterns = (String[])data.get()) == null) {
            ResourceBundle resource = this.localeData.getNumberFormatData(this.locale);
            compactNumberPatterns = (String[])resource.getObject(width + ".CompactNumberPatterns");
            this.cache.put(cacheKey, new ResourceReference(cacheKey, compactNumberPatterns, this.referenceQueue));
        }
        return compactNumberPatterns;
    }

    public ResourceBundle getJavaTimeFormatData() {
        ResourceBundle rb = this.localeData.getDateFormatData(this.locale);
        if (rb instanceof ParallelListResourceBundle) {
            this.localeData.setSupplementary((ParallelListResourceBundle)rb);
        }
        return rb;
    }

    public String getLocalizedPattern(String requestedTemplate, String calType) {
        String pattern;
        String cacheKey = SKELETON_PATTERN + calType + "." + requestedTemplate;
        this.removeEmptyReferences();
        ResourceReference data = (ResourceReference)this.cache.get(cacheKey);
        if (data == null || (pattern = (String)data.get()) == null) {
            pattern = this.getLocalizedPatternImpl(requestedTemplate, calType);
            this.cache.put(cacheKey, new ResourceReference(cacheKey, pattern != null ? pattern : "", this.referenceQueue));
        } else if ("".equals(pattern)) {
            pattern = null;
        }
        return pattern;
    }

    private String getLocalizedPatternImpl(String requestedTemplate, String calType) {
        this.initSkeletonIfNeeded();
        String skeleton = this.substituteInputSkeletons(requestedTemplate);
        Matcher matcher = VALID_SKELETON_PATTERN.matcher(skeleton);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Requested template \"%s\" is invalid".formatted(requestedTemplate) + (requestedTemplate.equals(skeleton) ? "." : ", which translated into \"%s\"".formatted(skeleton) + " after the 'j' or 'C' substitution."));
        }
        String matched = this.matchSkeleton(skeleton, calType);
        if (matched == null) {
            String dateMatched = this.matchSkeleton(matcher.group("date"), calType);
            String timeMatched = this.matchSkeleton(matcher.group("time"), calType);
            if (dateMatched != null && timeMatched != null) {
                int style = switch (requestedTemplate.replaceAll("[^M]+", "").length()) {
                    case 4 -> {
                        if (requestedTemplate.indexOf(69) >= 0) {
                            yield 0;
                        }
                        yield 1;
                    }
                    case 3 -> 2;
                    default -> 3;
                };
                String dateTimePattern = this.getDateTimePattern(null, "DateTimePatterns", style, calType);
                matched = MessageFormat.format(dateTimePattern.replaceAll("'", "''"), timeMatched, dateMatched);
            }
        }
        LocaleResources.trace("requested: %s, locale: %s, calType: %s, matched: %s\n", requestedTemplate, this.locale, calType, matched);
        return matched;
    }

    private String matchSkeleton(String skeleton, String calType) {
        Stream<String> inferred = this.possibleInferred(skeleton);
        ResourceBundle r = this.localeData.getDateFormatData(this.locale);
        return inferred.map(s -> ("gregory".equals(calType) ? "" : calType + ".") + "DateFormatItem." + s).map(key -> r.containsKey((String)key) ? r.getString((String)key) : null).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private void initSkeletonIfNeeded() {
        if (inputSkeletons == null) {
            inputSkeletons = new HashMap<String, Map<String, String>>();
            Pattern p = Pattern.compile("([^:]+):([^;]+);");
            ResourceBundle r = this.localeData.getDateFormatData(Locale.ROOT);
            Stream.of("preferred", "allowed").forEach(type -> {
                String inputRegionsKey = "DateFormatItemInputRegions." + type;
                HashMap typeMap = new HashMap();
                if (r.containsKey(inputRegionsKey)) {
                    p.matcher(r.getString(inputRegionsKey)).results().forEach(mr -> Arrays.stream(mr.group(2).split(" ")).forEach(region -> typeMap.put(region, mr.group(1))));
                }
                inputSkeletons.put((String)type, typeMap);
            });
        }
        if (this.jPattern == null) {
            this.jPattern = this.resolveInputSkeleton("preferred");
            this.CPattern = this.resolveInputSkeleton("allowed");
            if (this.CPattern.length() == 2) {
                byte[] ba = new byte[]{(byte)this.CPattern.charAt(1), (byte)this.CPattern.charAt(0)};
                this.CPattern = new String(ba);
            }
        }
    }

    private String resolveInputSkeleton(String type) {
        Map<String, String> regionToSkeletonMap = inputSkeletons.get(type);
        return regionToSkeletonMap.getOrDefault(this.locale.getLanguage() + "-" + this.locale.getCountry(), regionToSkeletonMap.getOrDefault(this.locale.getCountry(), regionToSkeletonMap.getOrDefault(this.locale.getLanguage() + "-001", regionToSkeletonMap.getOrDefault("001", "h"))));
    }

    private String substituteInputSkeletons(String requestedTemplate) {
        long cCount = requestedTemplate.chars().filter(c -> c == 67).count();
        return requestedTemplate.replaceAll("j", this.jPattern).replaceFirst("C+", this.CPattern.replaceAll("([hkHK])", "$1".repeat((int)cCount)));
    }

    private Stream<String> possibleInferred(String skeleton) {
        return this.priorityList(skeleton, "M", "L").stream().flatMap(s -> this.priorityList((String)s, "E", "c").stream()).distinct();
    }

    private List<String> priorityList(String skeleton, String pChar, String subChar) {
        int first = skeleton.indexOf(pChar);
        int last = skeleton.lastIndexOf(pChar);
        if (first >= 0) {
            String prefix = skeleton.substring(0, first);
            String suffix = skeleton.substring(last + 1);
            String o1 = prefix + pChar + suffix;
            String o2 = prefix + pChar.repeat(2) + suffix;
            String o3 = prefix + pChar.repeat(3) + suffix;
            String o4 = prefix + pChar.repeat(4) + suffix;
            String s1 = prefix + subChar + suffix;
            String s2 = prefix + subChar.repeat(2) + suffix;
            String s3 = prefix + subChar.repeat(3) + suffix;
            String s4 = prefix + subChar.repeat(4) + suffix;
            return switch (last - first) {
                case 1 -> List.of(skeleton, o1, o2, o3, o4, s1, s2, s3, s4);
                case 2 -> List.of(skeleton, o2, o1, o3, o4, s2, s1, s3, s4);
                case 3 -> List.of(skeleton, o3, o4, o2, o1, s3, s4, s2, s1);
                default -> List.of(skeleton, o4, o3, o2, o1, s4, s3, s2, s1);
            };
        }
        return List.of(skeleton);
    }

    private String getDateTimePattern(String prefix, String key, int styleIndex, String calendarType) {
        StringBuilder sb = new StringBuilder();
        if (prefix != null) {
            sb.append(prefix);
        }
        if (!"gregory".equals(calendarType)) {
            sb.append(calendarType).append('.');
        }
        sb.append(key);
        String resourceKey = sb.toString();
        String cacheKey = sb.insert(0, DATE_TIME_PATTERN).toString();
        this.removeEmptyReferences();
        ResourceReference data = (ResourceReference)this.cache.get(cacheKey);
        String[] value = NULLOBJECT;
        if (data == null || (value = data.get()) == null) {
            ResourceBundle r;
            ResourceBundle resourceBundle = r = prefix != null ? this.getJavaTimeFormatData() : this.localeData.getDateFormatData(this.locale);
            if (r.containsKey(resourceKey)) {
                value = r.getStringArray(resourceKey);
            } else {
                assert (!resourceKey.equals(key));
                if (r.containsKey(key)) {
                    value = r.getStringArray(key);
                }
            }
            this.cache.put(cacheKey, new ResourceReference(cacheKey, value, this.referenceQueue));
        }
        if (value == NULLOBJECT) {
            assert (prefix != null);
            return null;
        }
        String[] styles = value;
        return styles.length > 1 ? styles[styleIndex] : styles[0];
    }

    public String[] getRules() {
        String[] rules;
        this.removeEmptyReferences();
        ResourceReference data = (ResourceReference)this.cache.get(RULES_CACHEKEY);
        if (data == null || (rules = (String[])data.get()) == null) {
            ResourceBundle rb = this.localeData.getDateFormatData(this.locale);
            rules = new String[2];
            rules[1] = "";
            rules[0] = "";
            if (rb.containsKey("PluralRules")) {
                rules[0] = rb.getString("PluralRules");
            }
            if (rb.containsKey("DayPeriodRules")) {
                rules[1] = rb.getString("DayPeriodRules");
            }
            this.cache.put(RULES_CACHEKEY, new ResourceReference(RULES_CACHEKEY, rules, this.referenceQueue));
        }
        return rules;
    }

    public static void trace(String format, Object ... params) {
        if (TRACE_ON) {
            System.out.format(format, params);
        }
    }

    static {
        TRACE_ON = Boolean.parseBoolean(GetPropertyAction.privilegedGetProperty("locale.resources.debug", "false"));
    }

    private static class ResourceReference
    extends SoftReference<Object> {
        private final String cacheKey;

        ResourceReference(String cacheKey, Object o, ReferenceQueue<Object> q) {
            super(o, q);
            this.cacheKey = cacheKey;
        }

        String getCacheKey() {
            return this.cacheKey;
        }
    }
}

