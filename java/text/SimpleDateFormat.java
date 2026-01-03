/*
 * Decompiled with CFR 0.152.
 */
package java.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.text.AttributedCharacterIterator;
import java.text.CalendarBuilder;
import java.text.CharacterIteratorFieldDelegate;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DontCareFieldPosition;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.util.calendar.CalendarUtils;
import sun.util.calendar.ZoneInfoFile;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.TimeZoneNameUtility;

public class SimpleDateFormat
extends DateFormat {
    static final long serialVersionUID = 4774881970558875024L;
    static final int currentSerialVersion = 1;
    private int serialVersionOnStream = 1;
    private String pattern;
    private transient NumberFormat originalNumberFormat;
    private transient String originalNumberPattern;
    private transient char minusSign = (char)45;
    private transient boolean hasFollowingMinusSign = false;
    private transient boolean forceStandaloneForm = false;
    private transient char[] compiledPattern;
    private static final int TAG_QUOTE_ASCII_CHAR = 100;
    private static final int TAG_QUOTE_CHARS = 101;
    private transient char zeroDigit;
    private DateFormatSymbols formatData;
    private Date defaultCenturyStart;
    private transient int defaultCenturyStartYear;
    private static final int MILLIS_PER_MINUTE = 60000;
    private static final String GMT = "GMT";
    private static final ConcurrentMap<Locale, NumberFormat> cachedNumberFormatData = new ConcurrentHashMap<Locale, NumberFormat>(3);
    private Locale locale;
    transient boolean useDateFormatSymbols;
    private static final int[] PATTERN_INDEX_TO_CALENDAR_FIELD = new int[]{0, 1, 2, 5, 11, 11, 12, 13, 14, 7, 6, 8, 3, 4, 9, 10, 10, 15, 15, 17, 1000, 15, 2};
    private static final int[] PATTERN_INDEX_TO_DATE_FORMAT_FIELD = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 17, 1, 9, 17, 2};
    private static final DateFormat.Field[] PATTERN_INDEX_TO_DATE_FORMAT_FIELD_ID = new DateFormat.Field[]{DateFormat.Field.ERA, DateFormat.Field.YEAR, DateFormat.Field.MONTH, DateFormat.Field.DAY_OF_MONTH, DateFormat.Field.HOUR_OF_DAY1, DateFormat.Field.HOUR_OF_DAY0, DateFormat.Field.MINUTE, DateFormat.Field.SECOND, DateFormat.Field.MILLISECOND, DateFormat.Field.DAY_OF_WEEK, DateFormat.Field.DAY_OF_YEAR, DateFormat.Field.DAY_OF_WEEK_IN_MONTH, DateFormat.Field.WEEK_OF_YEAR, DateFormat.Field.WEEK_OF_MONTH, DateFormat.Field.AM_PM, DateFormat.Field.HOUR1, DateFormat.Field.HOUR0, DateFormat.Field.TIME_ZONE, DateFormat.Field.TIME_ZONE, DateFormat.Field.YEAR, DateFormat.Field.DAY_OF_WEEK, DateFormat.Field.TIME_ZONE, DateFormat.Field.MONTH};
    private static final int[] REST_OF_STYLES = new int[]{32769, 2, 32770};

    public SimpleDateFormat() {
        this("", Locale.getDefault(Locale.Category.FORMAT));
        this.applyPatternImpl(LocaleProviderAdapter.getResourceBundleBased().getLocaleResources(this.locale).getDateTimePattern(3, 3, this.calendar));
    }

    public SimpleDateFormat(String pattern) {
        this(pattern, Locale.getDefault(Locale.Category.FORMAT));
    }

    public SimpleDateFormat(String pattern, Locale locale) {
        if (pattern == null || locale == null) {
            throw new NullPointerException();
        }
        this.initializeCalendar(locale);
        this.pattern = pattern;
        this.formatData = DateFormatSymbols.getInstanceRef(locale);
        this.locale = locale;
        this.initialize(locale);
    }

    public SimpleDateFormat(String pattern, DateFormatSymbols formatSymbols) {
        if (pattern == null || formatSymbols == null) {
            throw new NullPointerException();
        }
        this.pattern = pattern;
        this.formatData = (DateFormatSymbols)formatSymbols.clone();
        this.locale = Locale.getDefault(Locale.Category.FORMAT);
        this.initializeCalendar(this.locale);
        this.initialize(this.locale);
        this.useDateFormatSymbols = true;
    }

    private void initialize(Locale loc) {
        this.compiledPattern = this.compile(this.pattern);
        this.numberFormat = (NumberFormat)cachedNumberFormatData.get(loc);
        if (this.numberFormat == null) {
            this.numberFormat = NumberFormat.getIntegerInstance(loc);
            this.numberFormat.setGroupingUsed(false);
            cachedNumberFormatData.putIfAbsent(loc, this.numberFormat);
        }
        this.numberFormat = (NumberFormat)this.numberFormat.clone();
        this.initializeDefaultCentury();
    }

    private void initializeCalendar(Locale loc) {
        if (this.calendar == null) {
            assert (loc != null);
            this.calendar = Calendar.getInstance(loc);
        }
    }

    private char[] compile(String pattern) {
        int length = pattern.length();
        boolean inQuote = false;
        StringBuilder compiledCode = new StringBuilder(length * 2);
        StringBuilder tmpBuffer = null;
        int count = 0;
        int tagcount = 0;
        int lastTag = -1;
        int prevTag = -1;
        for (int i = 0; i < length; ++i) {
            char c = pattern.charAt(i);
            if (c == '\'') {
                if (i + 1 < length && (c = pattern.charAt(i + 1)) == '\'') {
                    ++i;
                    if (count != 0) {
                        SimpleDateFormat.encode(lastTag, count, compiledCode);
                        ++tagcount;
                        prevTag = lastTag;
                        lastTag = -1;
                        count = 0;
                    }
                    if (inQuote) {
                        tmpBuffer.append(c);
                        continue;
                    }
                    compiledCode.append((char)(0x6400 | c));
                    continue;
                }
                if (!inQuote) {
                    if (count != 0) {
                        SimpleDateFormat.encode(lastTag, count, compiledCode);
                        ++tagcount;
                        prevTag = lastTag;
                        lastTag = -1;
                        count = 0;
                    }
                    if (tmpBuffer == null) {
                        tmpBuffer = new StringBuilder(length);
                    } else {
                        tmpBuffer.setLength(0);
                    }
                    inQuote = true;
                    continue;
                }
                int len = tmpBuffer.length();
                if (len == 1) {
                    char ch = tmpBuffer.charAt(0);
                    if (ch < '\u0080') {
                        compiledCode.append((char)(0x6400 | ch));
                    } else {
                        compiledCode.append('\u6501');
                        compiledCode.append(ch);
                    }
                } else {
                    SimpleDateFormat.encode(101, len, compiledCode);
                    compiledCode.append((CharSequence)tmpBuffer);
                }
                inQuote = false;
                continue;
            }
            if (inQuote) {
                tmpBuffer.append(c);
                continue;
            }
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z')) {
                char d;
                int j;
                if (count != 0) {
                    SimpleDateFormat.encode(lastTag, count, compiledCode);
                    ++tagcount;
                    prevTag = lastTag;
                    lastTag = -1;
                    count = 0;
                }
                if (c < '\u0080') {
                    compiledCode.append((char)(0x6400 | c));
                    continue;
                }
                for (j = i + 1; !(j >= length || (d = pattern.charAt(j)) == '\'' || d >= 'a' && d <= 'z' || d >= 'A' && d <= 'Z'); ++j) {
                }
                SimpleDateFormat.encode(101, j - i, compiledCode);
                while (i < j) {
                    compiledCode.append(pattern.charAt(i));
                    ++i;
                }
                --i;
                continue;
            }
            int tag = "GyMdkHmsSEDFwWahKzZYuXL".indexOf(c);
            if (tag == -1) {
                throw new IllegalArgumentException("Illegal pattern character '" + c + "'");
            }
            if (lastTag == -1 || lastTag == tag) {
                lastTag = tag;
                ++count;
                continue;
            }
            SimpleDateFormat.encode(lastTag, count, compiledCode);
            ++tagcount;
            prevTag = lastTag;
            lastTag = tag;
            count = 1;
        }
        if (inQuote) {
            throw new IllegalArgumentException("Unterminated quote");
        }
        if (count != 0) {
            SimpleDateFormat.encode(lastTag, count, compiledCode);
            ++tagcount;
            prevTag = lastTag;
        }
        this.forceStandaloneForm = tagcount == 1 && prevTag == 2;
        int len = compiledCode.length();
        char[] r = new char[len];
        compiledCode.getChars(0, len, r, 0);
        return r;
    }

    private static void encode(int tag, int length, StringBuilder buffer) {
        if (tag == 21 && length >= 4) {
            throw new IllegalArgumentException("invalid ISO 8601 format: length=" + length);
        }
        if (length < 255) {
            buffer.append((char)(tag << 8 | length));
        } else {
            buffer.append((char)(tag << 8 | 0xFF));
            buffer.append((char)(length >>> 16));
            buffer.append((char)(length & 0xFFFF));
        }
    }

    private void initializeDefaultCentury() {
        this.calendar.setTimeInMillis(System.currentTimeMillis());
        this.calendar.add(1, -80);
        this.parseAmbiguousDatesAsAfter(this.calendar.getTime());
    }

    private void parseAmbiguousDatesAsAfter(Date startDate) {
        this.defaultCenturyStart = startDate;
        this.calendar.setTime(startDate);
        this.defaultCenturyStartYear = this.calendar.get(1);
    }

    public void set2DigitYearStart(Date startDate) {
        this.parseAmbiguousDatesAsAfter(new Date(startDate.getTime()));
    }

    public Date get2DigitYearStart() {
        return (Date)this.defaultCenturyStart.clone();
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
        pos.endIndex = 0;
        pos.beginIndex = 0;
        return this.format(date, toAppendTo, pos.getFieldDelegate());
    }

    private StringBuffer format(Date date, StringBuffer toAppendTo, Format.FieldDelegate delegate) {
        this.calendar.setTime(date);
        boolean useDateFormatSymbols = this.useDateFormatSymbols();
        int i = 0;
        block4: while (i < this.compiledPattern.length) {
            int count;
            int tag = this.compiledPattern[i] >>> 8;
            if ((count = this.compiledPattern[i++] & 0xFF) == 255) {
                count = this.compiledPattern[i++] << 16;
                count |= this.compiledPattern[i++];
            }
            switch (tag) {
                case 100: {
                    toAppendTo.append((char)count);
                    continue block4;
                }
                case 101: {
                    toAppendTo.append(this.compiledPattern, i, count);
                    i += count;
                    continue block4;
                }
            }
            this.subFormat(tag, count, delegate, toAppendTo, useDateFormatSymbols);
        }
        return toAppendTo;
    }

    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        StringBuffer sb = new StringBuffer();
        CharacterIteratorFieldDelegate delegate = new CharacterIteratorFieldDelegate();
        if (obj instanceof Date) {
            this.format((Date)obj, sb, delegate);
        } else if (obj instanceof Number) {
            this.format(new Date(((Number)obj).longValue()), sb, delegate);
        } else {
            if (obj == null) {
                throw new NullPointerException("formatToCharacterIterator must be passed non-null object");
            }
            throw new IllegalArgumentException("Cannot format given Object as a Date");
        }
        return delegate.getIterator(sb.toString());
    }

    private void subFormat(int patternCharIndex, int count, Format.FieldDelegate delegate, StringBuffer buffer, boolean useDateFormatSymbols) {
        int style;
        int value;
        int maxIntCount = Integer.MAX_VALUE;
        String current = null;
        int beginOffset = buffer.length();
        int field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];
        if (field == 17) {
            if (this.calendar.isWeekDateSupported()) {
                value = this.calendar.getWeekYear();
            } else {
                patternCharIndex = 1;
                field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];
                value = this.calendar.get(field);
            }
        } else {
            value = field == 1000 ? CalendarBuilder.toISODayOfWeek(this.calendar.get(7)) : this.calendar.get(field);
        }
        int n = style = count >= 4 ? 2 : 1;
        if (!useDateFormatSymbols && field < 15 && patternCharIndex != 22) {
            current = this.calendar.getDisplayName(field, style, this.locale);
        }
        switch (patternCharIndex) {
            case 0: {
                String[] eras;
                if (useDateFormatSymbols && value < (eras = this.formatData.getEras()).length) {
                    current = eras[value];
                }
                if (current != null) break;
                current = "";
                break;
            }
            case 1: 
            case 19: {
                if (this.calendar instanceof GregorianCalendar) {
                    if (count != 2) {
                        this.zeroPaddingNumber(value, count, maxIntCount, buffer);
                        break;
                    }
                    this.zeroPaddingNumber(value, 2, 2, buffer);
                    break;
                }
                if (current != null) break;
                this.zeroPaddingNumber(value, style == 2 ? 1 : count, maxIntCount, buffer);
                break;
            }
            case 2: {
                if (useDateFormatSymbols) {
                    if (count >= 4) {
                        months = this.formatData.getMonths();
                        current = months[value];
                    } else if (count == 3) {
                        months = this.formatData.getShortMonths();
                        current = months[value];
                    }
                } else if (count < 3) {
                    current = null;
                } else if (this.forceStandaloneForm && (current = this.calendar.getDisplayName(field, style | 0x8000, this.locale)) == null) {
                    current = this.calendar.getDisplayName(field, style, this.locale);
                }
                if (current != null) break;
                this.zeroPaddingNumber(value + 1, count, maxIntCount, buffer);
                break;
            }
            case 22: {
                assert (current == null);
                if (this.locale == null) {
                    if (count >= 4) {
                        months = this.formatData.getMonths();
                        current = months[value];
                    } else if (count == 3) {
                        months = this.formatData.getShortMonths();
                        current = months[value];
                    }
                } else if (count >= 3) {
                    current = this.calendar.getDisplayName(field, style | 0x8000, this.locale);
                }
                if (current != null) break;
                this.zeroPaddingNumber(value + 1, count, maxIntCount, buffer);
                break;
            }
            case 4: {
                if (current != null) break;
                if (value == 0) {
                    this.zeroPaddingNumber(this.calendar.getMaximum(11) + 1, count, maxIntCount, buffer);
                    break;
                }
                this.zeroPaddingNumber(value, count, maxIntCount, buffer);
                break;
            }
            case 9: {
                String[] weekdays;
                if (!useDateFormatSymbols) break;
                if (count >= 4) {
                    weekdays = this.formatData.getWeekdays();
                    current = weekdays[value];
                    break;
                }
                weekdays = this.formatData.getShortWeekdays();
                current = weekdays[value];
                break;
            }
            case 14: {
                if (!useDateFormatSymbols) break;
                String[] ampm = this.formatData.getAmPmStrings();
                current = ampm[value];
                break;
            }
            case 15: {
                if (current != null) break;
                if (value == 0) {
                    this.zeroPaddingNumber(this.calendar.getLeastMaximum(10) + 1, count, maxIntCount, buffer);
                    break;
                }
                this.zeroPaddingNumber(value, count, maxIntCount, buffer);
                break;
            }
            case 17: {
                if (current != null) break;
                if (this.formatData.locale == null || this.formatData.isZoneStringsSet) {
                    int index;
                    int zoneIndex = this.formatData.getZoneIndex(this.calendar.getTimeZone().getID());
                    if (zoneIndex == -1) {
                        value = this.calendar.get(15) + this.calendar.get(16);
                        buffer.append(ZoneInfoFile.toCustomID(value));
                        break;
                    }
                    int n2 = index = this.calendar.get(16) == 0 ? 1 : 3;
                    if (count < 4) {
                        ++index;
                    }
                    String[][] zoneStrings = this.formatData.getZoneStringsWrapper();
                    buffer.append(zoneStrings[zoneIndex][index]);
                    break;
                }
                TimeZone tz = this.calendar.getTimeZone();
                boolean daylight = this.calendar.get(16) != 0;
                int tzstyle = count < 4 ? 0 : 1;
                buffer.append(tz.getDisplayName(daylight, tzstyle, this.formatData.locale));
                break;
            }
            case 18: {
                value = (this.calendar.get(15) + this.calendar.get(16)) / 60000;
                int width = 4;
                if (value >= 0) {
                    buffer.append('+');
                } else {
                    ++width;
                }
                int num = value / 60 * 100 + value % 60;
                CalendarUtils.sprintf0d(buffer, num, width);
                break;
            }
            case 21: {
                value = this.calendar.get(15) + this.calendar.get(16);
                if (value == 0) {
                    buffer.append('Z');
                    break;
                }
                if ((value /= 60000) >= 0) {
                    buffer.append('+');
                } else {
                    buffer.append('-');
                    value = -value;
                }
                CalendarUtils.sprintf0d(buffer, value / 60, 2);
                if (count == 1) break;
                if (count == 3) {
                    buffer.append(':');
                }
                CalendarUtils.sprintf0d(buffer, value % 60, 2);
                break;
            }
            default: {
                if (current != null) break;
                this.zeroPaddingNumber(value, count, maxIntCount, buffer);
            }
        }
        if (current != null) {
            buffer.append(current);
        }
        int fieldID = PATTERN_INDEX_TO_DATE_FORMAT_FIELD[patternCharIndex];
        DateFormat.Field f = PATTERN_INDEX_TO_DATE_FORMAT_FIELD_ID[patternCharIndex];
        delegate.formatted(fieldID, f, f, beginOffset, buffer.length(), buffer);
    }

    private void zeroPaddingNumber(int value, int minDigits, int maxDigits, StringBuffer buffer) {
        try {
            if (this.zeroDigit == '\u0000') {
                this.zeroDigit = ((DecimalFormat)this.numberFormat).getDecimalFormatSymbols().getZeroDigit();
            }
            if (value >= 0) {
                if (value < 100 && minDigits >= 1 && minDigits <= 2) {
                    if (value < 10) {
                        if (minDigits == 2) {
                            buffer.append(this.zeroDigit);
                        }
                        buffer.append((char)(this.zeroDigit + value));
                    } else {
                        buffer.append((char)(this.zeroDigit + value / 10));
                        buffer.append((char)(this.zeroDigit + value % 10));
                    }
                    return;
                }
                if (value >= 1000 && value < 10000) {
                    if (minDigits == 4) {
                        buffer.append((char)(this.zeroDigit + value / 1000));
                        buffer.append((char)(this.zeroDigit + (value %= 1000) / 100));
                        buffer.append((char)(this.zeroDigit + (value %= 100) / 10));
                        buffer.append((char)(this.zeroDigit + value % 10));
                        return;
                    }
                    if (minDigits == 2 && maxDigits == 2) {
                        this.zeroPaddingNumber(value % 100, 2, 2, buffer);
                        return;
                    }
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.numberFormat.setMinimumIntegerDigits(minDigits);
        this.numberFormat.setMaximumIntegerDigits(maxDigits);
        this.numberFormat.format(value, buffer, DontCareFieldPosition.INSTANCE);
    }

    @Override
    public Date parse(String text, ParsePosition pos) {
        Date parsedDate;
        int start;
        this.checkNegativeNumberExpression();
        int oldStart = start = pos.index;
        int textLength = text.length();
        boolean[] ambiguousYear = new boolean[]{false};
        CalendarBuilder calb = new CalendarBuilder();
        int i = 0;
        block6: while (i < this.compiledPattern.length) {
            int count;
            int tag = this.compiledPattern[i] >>> 8;
            if ((count = this.compiledPattern[i++] & 0xFF) == 255) {
                count = this.compiledPattern[i++] << 16;
                count |= this.compiledPattern[i++];
            }
            switch (tag) {
                case 100: {
                    if (start >= textLength || text.charAt(start) != (char)count) {
                        pos.index = oldStart;
                        pos.errorIndex = start;
                        return null;
                    }
                    ++start;
                    continue block6;
                }
                case 101: {
                    while (count-- > 0) {
                        if (start >= textLength || text.charAt(start) != this.compiledPattern[i++]) {
                            pos.index = oldStart;
                            pos.errorIndex = start;
                            return null;
                        }
                        ++start;
                    }
                    continue block6;
                }
            }
            boolean obeyCount = false;
            boolean useFollowingMinusSignAsDelimiter = false;
            if (i < this.compiledPattern.length) {
                int nextTag = this.compiledPattern[i] >>> 8;
                int nextCount = this.compiledPattern[i] & 0xFF;
                obeyCount = this.shouldObeyCount(nextTag, nextCount);
                if (this.hasFollowingMinusSign && (nextTag == 100 || nextTag == 101)) {
                    if (nextTag != 100) {
                        nextCount = this.compiledPattern[i + 1];
                    }
                    if (nextCount == this.minusSign) {
                        useFollowingMinusSignAsDelimiter = true;
                    }
                }
            }
            if ((start = this.subParse(text, start, tag, count, obeyCount, ambiguousYear, pos, useFollowingMinusSignAsDelimiter, calb)) >= 0) continue;
            pos.index = oldStart;
            return null;
        }
        pos.index = start;
        try {
            parsedDate = calb.establish(this.calendar).getTime();
            if (ambiguousYear[0] && parsedDate.before(this.defaultCenturyStart)) {
                parsedDate = calb.addYear(100).establish(this.calendar).getTime();
            }
        }
        catch (IllegalArgumentException e) {
            pos.errorIndex = start;
            pos.index = oldStart;
            return null;
        }
        return parsedDate;
    }

    private boolean shouldObeyCount(int tag, int count) {
        switch (tag) {
            case 2: 
            case 22: {
                return count <= 2;
            }
            case 1: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 7: 
            case 8: 
            case 10: 
            case 11: 
            case 12: 
            case 13: 
            case 15: 
            case 16: 
            case 19: 
            case 20: {
                return true;
            }
        }
        return false;
    }

    private int matchString(String text, int start, int field, String[] data, CalendarBuilder calb) {
        int i = 0;
        int count = data.length;
        if (field == 7) {
            i = 1;
        }
        int bestMatchLength = 0;
        int bestMatch = -1;
        while (i < count) {
            int length = data[i].length();
            if (length > bestMatchLength && text.regionMatches(true, start, data[i], 0, length)) {
                bestMatch = i;
                bestMatchLength = length;
            }
            ++i;
        }
        if (bestMatch >= 0) {
            calb.set(field, bestMatch);
            return start + bestMatchLength;
        }
        return -start;
    }

    private int matchString(String text, int start, int field, Map<String, Integer> data, CalendarBuilder calb) {
        if (data != null) {
            if (data instanceof SortedMap) {
                for (String name : data.keySet()) {
                    if (!text.regionMatches(true, start, name, 0, name.length())) continue;
                    calb.set(field, data.get(name));
                    return start + name.length();
                }
                return -start;
            }
            String bestMatch = null;
            for (String name : data.keySet()) {
                int length = name.length();
                if (bestMatch != null && length <= bestMatch.length() || !text.regionMatches(true, start, name, 0, length)) continue;
                bestMatch = name;
            }
            if (bestMatch != null) {
                calb.set(field, data.get(bestMatch));
                return start + bestMatch.length();
            }
        }
        return -start;
    }

    private int matchZoneString(String text, int start, String[] zoneNames) {
        for (int i = 1; i <= 4; ++i) {
            String zoneName = zoneNames[i];
            if (zoneName.isEmpty()) {
                zoneNames[i] = zoneName = TimeZoneNameUtility.retrieveDisplayName(zoneNames[0], i >= 3, i % 2, this.locale);
            }
            if (!text.regionMatches(true, start, zoneName, 0, zoneName.length())) continue;
            return i;
        }
        return -1;
    }

    private boolean matchDSTString(String text, int start, int zoneIndex, int standardIndex, String[][] zoneStrings) {
        int index = standardIndex + 2;
        String zoneName = zoneStrings[zoneIndex][index];
        return text.regionMatches(true, start, zoneName, 0, zoneName.length());
    }

    private int subParseZoneString(String text, int start, CalendarBuilder calb) {
        boolean useSameName = false;
        TimeZone currentTimeZone = this.getTimeZone();
        int zoneIndex = this.formatData.getZoneIndex(currentTimeZone.getID());
        Object tz = null;
        String[][] zoneStrings = this.formatData.getZoneStringsWrapper();
        String[] zoneNames2 = null;
        int nameIndex = 0;
        if (zoneIndex != -1 && (nameIndex = this.matchZoneString(text, start, zoneNames2 = zoneStrings[zoneIndex])) > 0) {
            if (nameIndex <= 2) {
                useSameName = zoneNames2[nameIndex].equalsIgnoreCase(zoneNames2[nameIndex + 2]);
            }
            tz = TimeZone.getTimeZone(zoneNames2[0]);
        }
        if (tz == null && (zoneIndex = this.formatData.getZoneIndex(TimeZone.getDefault().getID())) != -1 && (nameIndex = this.matchZoneString(text, start, zoneNames2 = zoneStrings[zoneIndex])) > 0) {
            if (nameIndex <= 2) {
                useSameName = zoneNames2[nameIndex].equalsIgnoreCase(zoneNames2[nameIndex + 2]);
            }
            tz = TimeZone.getTimeZone(zoneNames2[0]);
        }
        if (tz == null) {
            for (String[] zoneNames2 : zoneStrings) {
                nameIndex = this.matchZoneString(text, start, zoneNames2);
                if (nameIndex <= 0) continue;
                if (nameIndex <= 2) {
                    useSameName = zoneNames2[nameIndex].equalsIgnoreCase(zoneNames2[nameIndex + 2]);
                }
                tz = TimeZone.getTimeZone(zoneNames2[0]);
                break;
            }
        }
        if (tz != null) {
            int dstAmount;
            if (!tz.equals(currentTimeZone)) {
                this.setTimeZone((TimeZone)tz);
            }
            int n = dstAmount = nameIndex >= 3 ? ((TimeZone)tz).getDSTSavings() : 0;
            if (!(useSameName || nameIndex >= 3 && dstAmount == 0)) {
                calb.clear(15).set(16, dstAmount);
            }
            return start + zoneNames2[nameIndex].length();
        }
        return -start;
    }

    private int subParseNumericZone(String text, int start, int sign, int count, boolean colon, CalendarBuilder calb) {
        int index;
        block7: {
            index = start;
            try {
                char c = text.charAt(index++);
                if (!this.isDigit(c)) break block7;
                int hours = c - 48;
                if (this.isDigit(c = text.charAt(index++))) {
                    hours = hours * 10 + (c - 48);
                } else {
                    if (count > 0 || !colon) break block7;
                    --index;
                }
                if (hours > 23) break block7;
                int minutes = 0;
                if (count != 1) {
                    c = text.charAt(index++);
                    if (colon) {
                        if (c != ':') break block7;
                        c = text.charAt(index++);
                    }
                    if (!this.isDigit(c)) break block7;
                    minutes = c - 48;
                    if (!this.isDigit(c = text.charAt(index++)) || (minutes = minutes * 10 + (c - 48)) > 59) break block7;
                }
                calb.set(15, (minutes += hours * 60) * 60000 * sign).set(16, 0);
                return index;
            }
            catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                // empty catch block
            }
        }
        return 1 - index;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private int subParse(String text, int start, int patternCharIndex, int count, boolean obeyCount, boolean[] ambiguousYear, ParsePosition origPos, boolean useFollowingMinusSignAsDelimiter, CalendarBuilder calb) {
        ParsePosition pos;
        block56: {
            Number number;
            int actualStart;
            int field;
            int value;
            block54: {
                block58: {
                    block57: {
                        block55: {
                            value = 0;
                            pos = new ParsePosition(0);
                            pos.index = start;
                            if (patternCharIndex == 19 && !this.calendar.isWeekDateSupported()) {
                                patternCharIndex = 1;
                            }
                            field = PATTERN_INDEX_TO_CALENDAR_FIELD[patternCharIndex];
                            while (true) {
                                if (pos.index >= text.length()) {
                                    origPos.errorIndex = start;
                                    return -1;
                                }
                                char c = text.charAt(pos.index);
                                if (c != ' ' && c != '\t') break;
                                ++pos.index;
                            }
                            actualStart = pos.index;
                            if (!(patternCharIndex == 4 || patternCharIndex == 15 || patternCharIndex == 2 && count <= 2 || patternCharIndex == 22 && count <= 2 || patternCharIndex == 1) && patternCharIndex != 19) break block54;
                            if (!obeyCount) break block55;
                            if (start + count > text.length()) break block56;
                            number = this.numberFormat.parse(text.substring(0, start + count), pos);
                            break block57;
                        }
                        number = this.numberFormat.parse(text, pos);
                    }
                    if (number != null) break block58;
                    if (patternCharIndex == 1 && !(this.calendar instanceof GregorianCalendar)) break block54;
                    break block56;
                }
                value = number.intValue();
                if (useFollowingMinusSignAsDelimiter && value < 0 && (pos.index < text.length() && text.charAt(pos.index) != this.minusSign || pos.index == text.length() && text.charAt(pos.index - 1) == this.minusSign)) {
                    value = -value;
                    --pos.index;
                }
            }
            boolean useDateFormatSymbols = this.useDateFormatSymbols();
            switch (patternCharIndex) {
                case 0: {
                    if (useDateFormatSymbols) {
                        int index = this.matchString(text, start, 0, this.formatData.getEras(), calb);
                        if (index <= 0) break;
                        return index;
                    }
                    Map<String, Integer> map = this.getDisplayNamesMap(field, this.locale);
                    int index = this.matchString(text, start, field, map, calb);
                    if (index <= 0) break;
                    return index;
                }
                case 1: 
                case 19: {
                    if (!(this.calendar instanceof GregorianCalendar)) {
                        int index;
                        int style = count >= 4 ? 2 : 1;
                        Map<String, Integer> map = this.calendar.getDisplayNames(field, style, this.locale);
                        if (map != null && (index = this.matchString(text, start, field, map, calb)) > 0) {
                            return index;
                        }
                        calb.set(field, value);
                        return pos.index;
                    }
                    if (count <= 2 && pos.index - actualStart == 2 && Character.isDigit(text.charAt(actualStart)) && Character.isDigit(text.charAt(actualStart + 1))) {
                        int ambiguousTwoDigitYear = this.defaultCenturyStartYear % 100;
                        ambiguousYear[0] = value == ambiguousTwoDigitYear;
                        value += this.defaultCenturyStartYear / 100 * 100 + (value < ambiguousTwoDigitYear ? 100 : 0);
                    }
                    calb.set(field, value);
                    return pos.index;
                }
                case 2: {
                    if (count <= 2) {
                        calb.set(2, value - 1);
                        return pos.index;
                    }
                    if (useDateFormatSymbols) {
                        int newStart = this.matchString(text, start, 2, this.formatData.getMonths(), calb);
                        if (newStart > 0) {
                            return newStart;
                        }
                        int index = this.matchString(text, start, 2, this.formatData.getShortMonths(), calb);
                        if (index <= 0) break;
                        return index;
                    }
                    Map<String, Integer> map = this.getDisplayContextNamesMap(field, this.locale);
                    int index = this.matchString(text, start, field, map, calb);
                    if (index <= 0) break;
                    return index;
                }
                case 22: {
                    if (count <= 2) {
                        calb.set(2, value - 1);
                        return pos.index;
                    }
                    Map<String, Integer> maps = this.getDisplayNamesMap(field, this.locale);
                    int index = this.matchString(text, start, field, maps, calb);
                    if (index <= 0) break;
                    return index;
                }
                case 4: {
                    if (!this.isLenient() && (value < 1 || value > 24)) break;
                    if (value == this.calendar.getMaximum(11) + 1) {
                        value = 0;
                    }
                    calb.set(11, value);
                    return pos.index;
                }
                case 9: {
                    int[] styles;
                    if (useDateFormatSymbols) {
                        int newStart = this.matchString(text, start, 7, this.formatData.getWeekdays(), calb);
                        if (newStart > 0) {
                            return newStart;
                        }
                        int index = this.matchString(text, start, 7, this.formatData.getShortWeekdays(), calb);
                        if (index <= 0) break;
                        return index;
                    }
                    for (int style : styles = new int[]{2, 1}) {
                        Map<String, Integer> map = this.calendar.getDisplayNames(field, style, this.locale);
                        int index = this.matchString(text, start, field, map, calb);
                        if (index <= 0) continue;
                        return index;
                    }
                    break;
                }
                case 14: {
                    if (useDateFormatSymbols) {
                        int index = this.matchString(text, start, 9, this.formatData.getAmPmStrings(), calb);
                        if (index <= 0) break;
                        return index;
                    }
                    Map<String, Integer> map = this.getDisplayNamesMap(field, this.locale);
                    int index = this.matchString(text, start, field, map, calb);
                    if (index <= 0) break;
                    return index;
                }
                case 15: {
                    if (!this.isLenient() && (value < 1 || value > 12)) break;
                    if (value == this.calendar.getLeastMaximum(10) + 1) {
                        value = 0;
                    }
                    calb.set(10, value);
                    return pos.index;
                }
                case 17: 
                case 18: {
                    int sign = 0;
                    try {
                        int i;
                        char c = text.charAt(pos.index);
                        if (c == '+') {
                            sign = 1;
                        } else if (c == '-') {
                            sign = -1;
                        }
                        if (sign == 0) {
                            if ((c == 'G' || c == 'g') && text.length() - start >= GMT.length() && text.regionMatches(true, start, GMT, 0, GMT.length())) {
                                int i2;
                                pos.index = start + GMT.length();
                                if (text.length() - pos.index > 0) {
                                    c = text.charAt(pos.index);
                                    if (c == '+') {
                                        sign = 1;
                                    } else if (c == '-') {
                                        sign = -1;
                                    }
                                }
                                if (sign == 0) {
                                    calb.set(15, 0).set(16, 0);
                                    return pos.index;
                                }
                                if ((i2 = this.subParseNumericZone(text, ++pos.index, sign, 0, true, calb)) > 0) {
                                    return i2;
                                }
                                pos.index = -i2;
                                break;
                            }
                            int i3 = this.subParseZoneString(text, pos.index, calb);
                            if (i3 > 0) {
                                return i3;
                            }
                            pos.index = -i3;
                            break;
                        }
                        if ((i = this.subParseNumericZone(text, ++pos.index, sign, 0, false, calb)) > 0) {
                            return i;
                        }
                        pos.index = -i;
                    }
                    catch (IndexOutOfBoundsException c) {}
                    break;
                }
                case 21: {
                    int sign;
                    if (text.length() - pos.index <= 0) break;
                    char c = text.charAt(pos.index);
                    if (c == 'Z') {
                        calb.set(15, 0).set(16, 0);
                        return ++pos.index;
                    }
                    if (c == '+') {
                        sign = 1;
                    } else if (c == '-') {
                        sign = -1;
                    } else {
                        ++pos.index;
                        break;
                    }
                    int i = this.subParseNumericZone(text, ++pos.index, sign, count, count == 3, calb);
                    if (i > 0) {
                        return i;
                    }
                    pos.index = -i;
                    break;
                }
                default: {
                    if (obeyCount) {
                        if (start + count > text.length()) break;
                        number = this.numberFormat.parse(text.substring(0, start + count), pos);
                    } else {
                        number = this.numberFormat.parse(text, pos);
                    }
                    if (number == null) break;
                    value = number.intValue();
                    if (useFollowingMinusSignAsDelimiter && value < 0 && (pos.index < text.length() && text.charAt(pos.index) != this.minusSign || pos.index == text.length() && text.charAt(pos.index - 1) == this.minusSign)) {
                        value = -value;
                        --pos.index;
                    }
                    calb.set(field, value);
                    return pos.index;
                }
            }
        }
        origPos.errorIndex = pos.index;
        return -1;
    }

    private boolean useDateFormatSymbols() {
        return this.useDateFormatSymbols || this.locale == null;
    }

    private String translatePattern(String pattern, String from, String to) {
        StringBuilder result = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < pattern.length(); ++i) {
            char c = pattern.charAt(i);
            if (inQuote) {
                if (c == '\'') {
                    inQuote = false;
                }
            } else if (c == '\'') {
                inQuote = true;
            } else if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
                int ci = from.indexOf(c);
                if (ci >= 0) {
                    if (ci < to.length()) {
                        c = to.charAt(ci);
                    }
                } else {
                    throw new IllegalArgumentException("Illegal pattern  character '" + c + "'");
                }
            }
            result.append(c);
        }
        if (inQuote) {
            throw new IllegalArgumentException("Unfinished quote in pattern");
        }
        return result.toString();
    }

    public String toPattern() {
        return this.pattern;
    }

    public String toLocalizedPattern() {
        return this.translatePattern(this.pattern, "GyMdkHmsSEDFwWahKzZYuXL", this.formatData.getLocalPatternChars());
    }

    public void applyPattern(String pattern) {
        this.applyPatternImpl(pattern);
    }

    private void applyPatternImpl(String pattern) {
        this.compiledPattern = this.compile(pattern);
        this.pattern = pattern;
    }

    public void applyLocalizedPattern(String pattern) {
        String p = this.translatePattern(pattern, this.formatData.getLocalPatternChars(), "GyMdkHmsSEDFwWahKzZYuXL");
        this.compiledPattern = this.compile(p);
        this.pattern = p;
    }

    public DateFormatSymbols getDateFormatSymbols() {
        return (DateFormatSymbols)this.formatData.clone();
    }

    public void setDateFormatSymbols(DateFormatSymbols newFormatSymbols) {
        this.formatData = (DateFormatSymbols)newFormatSymbols.clone();
        this.useDateFormatSymbols = true;
    }

    @Override
    public Object clone() {
        SimpleDateFormat other = (SimpleDateFormat)super.clone();
        other.formatData = (DateFormatSymbols)this.formatData.clone();
        return other;
    }

    @Override
    public int hashCode() {
        return this.pattern.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        SimpleDateFormat that = (SimpleDateFormat)obj;
        return this.pattern.equals(that.pattern) && this.formatData.equals(that.formatData);
    }

    private Map<String, Integer> getDisplayNamesMap(int field, Locale locale) {
        Map<String, Integer> map = this.calendar.getDisplayNames(field, 1, locale);
        for (int style : REST_OF_STYLES) {
            Map<String, Integer> m = this.calendar.getDisplayNames(field, style, locale);
            if (m == null) continue;
            map.putAll(m);
        }
        return map;
    }

    private Map<String, Integer> getDisplayContextNamesMap(int field, Locale locale) {
        Map<String, Integer> map = this.calendar.getDisplayNames(field, this.forceStandaloneForm ? 32769 : 1, locale);
        Map<String, Integer> m = this.calendar.getDisplayNames(field, this.forceStandaloneForm ? 32770 : 2, locale);
        if (m != null) {
            map.putAll(m);
        }
        return map;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        String id;
        TimeZone zi;
        stream.defaultReadObject();
        try {
            this.compiledPattern = this.compile(this.pattern);
        }
        catch (Exception e) {
            throw new InvalidObjectException("invalid pattern");
        }
        if (this.serialVersionOnStream < 1) {
            this.initializeDefaultCentury();
        } else {
            this.parseAmbiguousDatesAsAfter(this.defaultCenturyStart);
        }
        this.serialVersionOnStream = 1;
        TimeZone tz = this.getTimeZone();
        if (tz instanceof SimpleTimeZone && (zi = TimeZone.getTimeZone(id = tz.getID())) != null && zi.hasSameRules(tz) && zi.getID().equals(id)) {
            this.setTimeZone(zi);
        }
    }

    private void checkNegativeNumberExpression() {
        if (this.numberFormat instanceof DecimalFormat && !this.numberFormat.equals(this.originalNumberFormat)) {
            String numberPattern = ((DecimalFormat)this.numberFormat).toPattern();
            if (!numberPattern.equals(this.originalNumberPattern)) {
                int minusIndex;
                this.hasFollowingMinusSign = false;
                int separatorIndex = numberPattern.indexOf(59);
                if (separatorIndex > -1 && (minusIndex = numberPattern.indexOf(45, separatorIndex)) > numberPattern.lastIndexOf(48) && minusIndex > numberPattern.lastIndexOf(35)) {
                    this.hasFollowingMinusSign = true;
                    this.minusSign = ((DecimalFormat)this.numberFormat).getDecimalFormatSymbols().getMinusSign();
                }
                this.originalNumberPattern = numberPattern;
            }
            this.originalNumberFormat = this.numberFormat;
        }
    }
}

