/*
 * Decompiled with CFR 0.152.
 */
package java.time.format;

import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.ParsePosition;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.chrono.Era;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseContext;
import java.time.format.DateTimePrintContext;
import java.time.format.DateTimeTextProvider;
import java.time.format.DecimalStyle;
import java.time.format.FormatStyle;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.time.format.TextStyle;
import java.time.format.ZoneName;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.JulianFields;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.ValueRange;
import java.time.temporal.WeekFields;
import java.time.zone.ZoneRulesProvider;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.text.spi.JavaTimeDateTimePatternProvider;
import sun.util.locale.provider.CalendarDataUtility;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;
import sun.util.locale.provider.TimeZoneNameUtility;

public final class DateTimeFormatterBuilder {
    private static final TemporalQuery<ZoneId> QUERY_REGION_ONLY = temporal -> {
        ZoneId zone = temporal.query(TemporalQueries.zoneId());
        return zone instanceof ZoneOffset ? null : zone;
    };
    private DateTimeFormatterBuilder active = this;
    private final DateTimeFormatterBuilder parent;
    private final List<DateTimePrinterParser> printerParsers = new ArrayList<DateTimePrinterParser>();
    private final boolean optional;
    private int padNextWidth;
    private char padNextChar;
    private int valueParserIndex = -1;
    private static final Pattern VALID_TEMPLATE_PATTERN = Pattern.compile("G{0,5}y*Q{0,5}M{0,5}w*E{0,5}d{0,2}B{0,5}[hHjC]{0,2}m{0,2}s{0,2}[vz]{0,4}");
    private static final Map<Character, TemporalField> FIELD_MAP = new HashMap<Character, TemporalField>();

    public static String getLocalizedDateTimePattern(FormatStyle dateStyle, FormatStyle timeStyle, Chronology chrono, Locale locale) {
        Objects.requireNonNull(locale, "locale");
        Objects.requireNonNull(chrono, "chrono");
        if (dateStyle == null && timeStyle == null) {
            throw new IllegalArgumentException("Either dateStyle or timeStyle must be non-null");
        }
        LocaleProviderAdapter adapter = LocaleProviderAdapter.getAdapter(JavaTimeDateTimePatternProvider.class, locale);
        JavaTimeDateTimePatternProvider provider = adapter.getJavaTimeDateTimePatternProvider();
        return provider.getJavaTimeDateTimePattern(DateTimeFormatterBuilder.convertStyle(timeStyle), DateTimeFormatterBuilder.convertStyle(dateStyle), chrono.getCalendarType(), CalendarDataUtility.findRegionOverride(locale));
    }

    public static String getLocalizedDateTimePattern(String requestedTemplate, Chronology chrono, Locale locale) {
        Objects.requireNonNull(requestedTemplate, "requestedTemplate");
        Objects.requireNonNull(chrono, "chrono");
        Objects.requireNonNull(locale, "locale");
        Locale override = CalendarDataUtility.findRegionOverride(locale);
        LocaleProviderAdapter adapter = LocaleProviderAdapter.getAdapter(JavaTimeDateTimePatternProvider.class, override);
        JavaTimeDateTimePatternProvider provider = adapter.getJavaTimeDateTimePatternProvider();
        return provider.getJavaTimeDateTimePattern(requestedTemplate, chrono.getCalendarType(), override);
    }

    private static int convertStyle(FormatStyle style) {
        if (style == null) {
            return -1;
        }
        return style.ordinal();
    }

    public DateTimeFormatterBuilder() {
        this.parent = null;
        this.optional = false;
    }

    private DateTimeFormatterBuilder(DateTimeFormatterBuilder parent, boolean optional) {
        this.parent = parent;
        this.optional = optional;
    }

    public DateTimeFormatterBuilder parseCaseSensitive() {
        this.appendInternal(SettingsParser.SENSITIVE);
        return this;
    }

    public DateTimeFormatterBuilder parseCaseInsensitive() {
        this.appendInternal(SettingsParser.INSENSITIVE);
        return this;
    }

    public DateTimeFormatterBuilder parseStrict() {
        this.appendInternal(SettingsParser.STRICT);
        return this;
    }

    public DateTimeFormatterBuilder parseLenient() {
        this.appendInternal(SettingsParser.LENIENT);
        return this;
    }

    public DateTimeFormatterBuilder parseDefaulting(TemporalField field, long value) {
        Objects.requireNonNull(field, "field");
        this.appendInternal(new DefaultValueParser(field, value));
        return this;
    }

    public DateTimeFormatterBuilder appendValue(TemporalField field) {
        Objects.requireNonNull(field, "field");
        this.appendValue(new NumberPrinterParser(field, 1, 19, SignStyle.NORMAL));
        return this;
    }

    public DateTimeFormatterBuilder appendValue(TemporalField field, int width) {
        Objects.requireNonNull(field, "field");
        if (width < 1 || width > 19) {
            throw new IllegalArgumentException("The width must be from 1 to 19 inclusive but was " + width);
        }
        NumberPrinterParser pp = new NumberPrinterParser(field, width, width, SignStyle.NOT_NEGATIVE);
        this.appendValue(pp);
        return this;
    }

    public DateTimeFormatterBuilder appendValue(TemporalField field, int minWidth, int maxWidth, SignStyle signStyle) {
        if (minWidth == maxWidth && signStyle == SignStyle.NOT_NEGATIVE) {
            return this.appendValue(field, maxWidth);
        }
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(signStyle, "signStyle");
        if (minWidth < 1 || minWidth > 19) {
            throw new IllegalArgumentException("The minimum width must be from 1 to 19 inclusive but was " + minWidth);
        }
        if (maxWidth < 1 || maxWidth > 19) {
            throw new IllegalArgumentException("The maximum width must be from 1 to 19 inclusive but was " + maxWidth);
        }
        if (maxWidth < minWidth) {
            throw new IllegalArgumentException("The maximum width must exceed or equal the minimum width but " + maxWidth + " < " + minWidth);
        }
        NumberPrinterParser pp = new NumberPrinterParser(field, minWidth, maxWidth, signStyle);
        this.appendValue(pp);
        return this;
    }

    public DateTimeFormatterBuilder appendValueReduced(TemporalField field, int width, int maxWidth, int baseValue) {
        Objects.requireNonNull(field, "field");
        ReducedPrinterParser pp = new ReducedPrinterParser(field, width, maxWidth, baseValue, null);
        this.appendValue(pp);
        return this;
    }

    public DateTimeFormatterBuilder appendValueReduced(TemporalField field, int width, int maxWidth, ChronoLocalDate baseDate) {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(baseDate, "baseDate");
        ReducedPrinterParser pp = new ReducedPrinterParser(field, width, maxWidth, 0, baseDate);
        this.appendValue(pp);
        return this;
    }

    private DateTimeFormatterBuilder appendValue(NumberPrinterParser pp) {
        if (this.active.valueParserIndex >= 0) {
            int activeValueParser = this.active.valueParserIndex;
            NumberPrinterParser basePP = (NumberPrinterParser)this.active.printerParsers.get(activeValueParser);
            if (pp.minWidth == pp.maxWidth && pp.signStyle == SignStyle.NOT_NEGATIVE) {
                basePP = basePP.withSubsequentWidth(pp.maxWidth);
                this.appendInternal(pp.withFixedWidth());
                this.active.valueParserIndex = activeValueParser;
            } else {
                basePP = basePP.withFixedWidth();
                this.active.valueParserIndex = this.appendInternal(pp);
            }
            this.active.printerParsers.set(activeValueParser, basePP);
        } else {
            this.active.valueParserIndex = this.appendInternal(pp);
        }
        return this;
    }

    public DateTimeFormatterBuilder appendFraction(TemporalField field, int minWidth, int maxWidth, boolean decimalPoint) {
        if (field == ChronoField.NANO_OF_SECOND) {
            if (minWidth == maxWidth && !decimalPoint) {
                this.appendValue(new NanosPrinterParser(minWidth, maxWidth, decimalPoint));
            } else {
                this.appendInternal(new NanosPrinterParser(minWidth, maxWidth, decimalPoint));
            }
        } else if (minWidth == maxWidth && !decimalPoint) {
            this.appendValue(new FractionPrinterParser(field, minWidth, maxWidth, decimalPoint));
        } else {
            this.appendInternal(new FractionPrinterParser(field, minWidth, maxWidth, decimalPoint));
        }
        return this;
    }

    public DateTimeFormatterBuilder appendText(TemporalField field) {
        return this.appendText(field, TextStyle.FULL);
    }

    public DateTimeFormatterBuilder appendText(TemporalField field, TextStyle textStyle) {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(textStyle, "textStyle");
        this.appendInternal(new TextPrinterParser(field, textStyle, DateTimeTextProvider.getInstance()));
        return this;
    }

    public DateTimeFormatterBuilder appendText(TemporalField field, Map<Long, String> textLookup) {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(textLookup, "textLookup");
        LinkedHashMap<Long, String> copy = new LinkedHashMap<Long, String>(textLookup);
        Map<TextStyle, Map<Long, String>> map = Collections.singletonMap(TextStyle.FULL, copy);
        final DateTimeTextProvider.LocaleStore store = new DateTimeTextProvider.LocaleStore(map);
        DateTimeTextProvider provider = new DateTimeTextProvider(this){

            @Override
            public String getText(Chronology chrono, TemporalField field, long value, TextStyle style, Locale locale) {
                return store.getText(value, style);
            }

            @Override
            public String getText(TemporalField field, long value, TextStyle style, Locale locale) {
                return store.getText(value, style);
            }

            @Override
            public Iterator<Map.Entry<String, Long>> getTextIterator(Chronology chrono, TemporalField field, TextStyle style, Locale locale) {
                return store.getTextIterator(style);
            }

            @Override
            public Iterator<Map.Entry<String, Long>> getTextIterator(TemporalField field, TextStyle style, Locale locale) {
                return store.getTextIterator(style);
            }
        };
        this.appendInternal(new TextPrinterParser(field, TextStyle.FULL, provider));
        return this;
    }

    public DateTimeFormatterBuilder appendInstant() {
        this.appendInternal(new InstantPrinterParser(-2));
        return this;
    }

    public DateTimeFormatterBuilder appendInstant(int fractionalDigits) {
        if (fractionalDigits < -1 || fractionalDigits > 9) {
            throw new IllegalArgumentException("The fractional digits must be from -1 to 9 inclusive but was " + fractionalDigits);
        }
        this.appendInternal(new InstantPrinterParser(fractionalDigits));
        return this;
    }

    public DateTimeFormatterBuilder appendOffsetId() {
        this.appendInternal(OffsetIdPrinterParser.INSTANCE_ID_Z);
        return this;
    }

    public DateTimeFormatterBuilder appendOffset(String pattern, String noOffsetText) {
        this.appendInternal(new OffsetIdPrinterParser(pattern, noOffsetText));
        return this;
    }

    public DateTimeFormatterBuilder appendLocalizedOffset(TextStyle style) {
        Objects.requireNonNull(style, "style");
        if (style != TextStyle.FULL && style != TextStyle.SHORT) {
            throw new IllegalArgumentException("Style must be either full or short");
        }
        this.appendInternal(new LocalizedOffsetIdPrinterParser(style));
        return this;
    }

    public DateTimeFormatterBuilder appendZoneId() {
        this.appendInternal(new ZoneIdPrinterParser(TemporalQueries.zoneId(), "ZoneId()"));
        return this;
    }

    public DateTimeFormatterBuilder appendZoneRegionId() {
        this.appendInternal(new ZoneIdPrinterParser(QUERY_REGION_ONLY, "ZoneRegionId()"));
        return this;
    }

    public DateTimeFormatterBuilder appendZoneOrOffsetId() {
        this.appendInternal(new ZoneIdPrinterParser(TemporalQueries.zone(), "ZoneOrOffsetId()"));
        return this;
    }

    public DateTimeFormatterBuilder appendZoneText(TextStyle textStyle) {
        this.appendInternal(new ZoneTextPrinterParser(textStyle, null, false));
        return this;
    }

    public DateTimeFormatterBuilder appendZoneText(TextStyle textStyle, Set<ZoneId> preferredZones) {
        Objects.requireNonNull(preferredZones, "preferredZones");
        this.appendInternal(new ZoneTextPrinterParser(textStyle, preferredZones, false));
        return this;
    }

    public DateTimeFormatterBuilder appendGenericZoneText(TextStyle textStyle) {
        this.appendInternal(new ZoneTextPrinterParser(textStyle, null, true));
        return this;
    }

    public DateTimeFormatterBuilder appendGenericZoneText(TextStyle textStyle, Set<ZoneId> preferredZones) {
        this.appendInternal(new ZoneTextPrinterParser(textStyle, preferredZones, true));
        return this;
    }

    public DateTimeFormatterBuilder appendChronologyId() {
        this.appendInternal(new ChronoPrinterParser(null));
        return this;
    }

    public DateTimeFormatterBuilder appendChronologyText(TextStyle textStyle) {
        Objects.requireNonNull(textStyle, "textStyle");
        this.appendInternal(new ChronoPrinterParser(textStyle));
        return this;
    }

    public DateTimeFormatterBuilder appendLocalized(FormatStyle dateStyle, FormatStyle timeStyle) {
        if (dateStyle == null && timeStyle == null) {
            throw new IllegalArgumentException("Either the date or time style must be non-null");
        }
        this.appendInternal(new LocalizedPrinterParser(dateStyle, timeStyle));
        return this;
    }

    public DateTimeFormatterBuilder appendLocalized(String requestedTemplate) {
        Objects.requireNonNull(requestedTemplate, "requestedTemplate");
        if (!VALID_TEMPLATE_PATTERN.matcher(requestedTemplate).matches()) {
            throw new IllegalArgumentException("Requested template is invalid: " + requestedTemplate);
        }
        this.appendInternal(new LocalizedPrinterParser(requestedTemplate));
        return this;
    }

    public DateTimeFormatterBuilder appendLiteral(char literal) {
        this.appendInternal(new CharLiteralPrinterParser(literal));
        return this;
    }

    public DateTimeFormatterBuilder appendLiteral(String literal) {
        Objects.requireNonNull(literal, "literal");
        if (!literal.isEmpty()) {
            if (literal.length() == 1) {
                this.appendInternal(new CharLiteralPrinterParser(literal.charAt(0)));
            } else {
                this.appendInternal(new StringLiteralPrinterParser(literal));
            }
        }
        return this;
    }

    public DateTimeFormatterBuilder appendDayPeriodText(TextStyle style) {
        Objects.requireNonNull(style, "style");
        switch (style) {
            case FULL_STANDALONE: {
                style = TextStyle.FULL;
                break;
            }
            case SHORT_STANDALONE: {
                style = TextStyle.SHORT;
                break;
            }
            case NARROW_STANDALONE: {
                style = TextStyle.NARROW;
            }
        }
        this.appendInternal(new DayPeriodPrinterParser(style));
        return this;
    }

    public DateTimeFormatterBuilder append(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        this.appendInternal(formatter.toPrinterParser(false));
        return this;
    }

    public DateTimeFormatterBuilder appendOptional(DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        this.appendInternal(formatter.toPrinterParser(true));
        return this;
    }

    public DateTimeFormatterBuilder appendPattern(String pattern) {
        Objects.requireNonNull(pattern, "pattern");
        this.parsePattern(pattern);
        return this;
    }

    private void parsePattern(String pattern) {
        for (int pos = 0; pos < pattern.length(); ++pos) {
            int start;
            char cur;
            block30: {
                block32: {
                    block50: {
                        int count;
                        block49: {
                            block48: {
                                block47: {
                                    block46: {
                                        block45: {
                                            block42: {
                                                block44: {
                                                    block43: {
                                                        block38: {
                                                            block41: {
                                                                block40: {
                                                                    block39: {
                                                                        block35: {
                                                                            block37: {
                                                                                block36: {
                                                                                    block34: {
                                                                                        block33: {
                                                                                            block31: {
                                                                                                TemporalField field;
                                                                                                cur = pattern.charAt(pos);
                                                                                                if ((cur < 'A' || cur > 'Z') && (cur < 'a' || cur > 'z')) break block30;
                                                                                                start = pos++;
                                                                                                while (pos < pattern.length() && pattern.charAt(pos) == cur) {
                                                                                                    ++pos;
                                                                                                }
                                                                                                count = pos - start;
                                                                                                if (cur == 'p') {
                                                                                                    int pad = 0;
                                                                                                    if (pos < pattern.length() && ((cur = pattern.charAt(pos)) >= 'A' && cur <= 'Z' || cur >= 'a' && cur <= 'z')) {
                                                                                                        pad = count;
                                                                                                        start = pos++;
                                                                                                        while (pos < pattern.length() && pattern.charAt(pos) == cur) {
                                                                                                            ++pos;
                                                                                                        }
                                                                                                        count = pos - start;
                                                                                                    }
                                                                                                    if (pad == 0) {
                                                                                                        throw new IllegalArgumentException("Pad letter 'p' must be followed by valid pad pattern: " + pattern);
                                                                                                    }
                                                                                                    this.padNext(pad);
                                                                                                }
                                                                                                if ((field = FIELD_MAP.get(Character.valueOf(cur))) == null) break block31;
                                                                                                this.parseField(cur, count, field);
                                                                                                break block32;
                                                                                            }
                                                                                            if (cur != 'z') break block33;
                                                                                            if (count > 4) {
                                                                                                throw new IllegalArgumentException("Too many pattern letters: " + cur);
                                                                                            }
                                                                                            if (count == 4) {
                                                                                                this.appendZoneText(TextStyle.FULL);
                                                                                            } else {
                                                                                                this.appendZoneText(TextStyle.SHORT);
                                                                                            }
                                                                                            break block32;
                                                                                        }
                                                                                        if (cur != 'V') break block34;
                                                                                        if (count != 2) {
                                                                                            throw new IllegalArgumentException("Pattern letter count must be 2: " + cur);
                                                                                        }
                                                                                        this.appendZoneId();
                                                                                        break block32;
                                                                                    }
                                                                                    if (cur != 'v') break block35;
                                                                                    if (count != 1) break block36;
                                                                                    this.appendGenericZoneText(TextStyle.SHORT);
                                                                                    break block32;
                                                                                }
                                                                                if (count != 4) break block37;
                                                                                this.appendGenericZoneText(TextStyle.FULL);
                                                                                break block32;
                                                                            }
                                                                            throw new IllegalArgumentException("Wrong number of pattern letters: " + cur);
                                                                        }
                                                                        if (cur != 'Z') break block38;
                                                                        if (count >= 4) break block39;
                                                                        this.appendOffset("+HHMM", "+0000");
                                                                        break block32;
                                                                    }
                                                                    if (count != 4) break block40;
                                                                    this.appendLocalizedOffset(TextStyle.FULL);
                                                                    break block32;
                                                                }
                                                                if (count != 5) break block41;
                                                                this.appendOffset("+HH:MM:ss", "Z");
                                                                break block32;
                                                            }
                                                            throw new IllegalArgumentException("Too many pattern letters: " + cur);
                                                        }
                                                        if (cur != 'O') break block42;
                                                        if (count != 1) break block43;
                                                        this.appendLocalizedOffset(TextStyle.SHORT);
                                                        break block32;
                                                    }
                                                    if (count != 4) break block44;
                                                    this.appendLocalizedOffset(TextStyle.FULL);
                                                    break block32;
                                                }
                                                throw new IllegalArgumentException("Pattern letter count must be 1 or 4: " + cur);
                                            }
                                            if (cur != 'X') break block45;
                                            if (count > 5) {
                                                throw new IllegalArgumentException("Too many pattern letters: " + cur);
                                            }
                                            this.appendOffset(OffsetIdPrinterParser.PATTERNS[count + (count == 1 ? 0 : 1)], "Z");
                                            break block32;
                                        }
                                        if (cur != 'x') break block46;
                                        if (count > 5) {
                                            throw new IllegalArgumentException("Too many pattern letters: " + cur);
                                        }
                                        String zero = count == 1 ? "+00" : (count % 2 == 0 ? "+0000" : "+00:00");
                                        this.appendOffset(OffsetIdPrinterParser.PATTERNS[count + (count == 1 ? 0 : 1)], zero);
                                        break block32;
                                    }
                                    if (cur != 'W') break block47;
                                    if (count > 1) {
                                        throw new IllegalArgumentException("Too many pattern letters: " + cur);
                                    }
                                    this.appendValue(new WeekBasedFieldPrinterParser(cur, count, count, count));
                                    break block32;
                                }
                                if (cur != 'w') break block48;
                                if (count > 2) {
                                    throw new IllegalArgumentException("Too many pattern letters: " + cur);
                                }
                                this.appendValue(new WeekBasedFieldPrinterParser(cur, count, count, 2));
                                break block32;
                            }
                            if (cur != 'Y') break block49;
                            if (count == 2) {
                                this.appendValue(new WeekBasedFieldPrinterParser(cur, count, count, 2));
                            } else {
                                this.appendValue(new WeekBasedFieldPrinterParser(cur, count, count, 19));
                            }
                            break block32;
                        }
                        if (cur != 'B') break block50;
                        switch (count) {
                            case 1: {
                                this.appendDayPeriodText(TextStyle.SHORT);
                                break block32;
                            }
                            case 4: {
                                this.appendDayPeriodText(TextStyle.FULL);
                                break block32;
                            }
                            case 5: {
                                this.appendDayPeriodText(TextStyle.NARROW);
                                break block32;
                            }
                            default: {
                                throw new IllegalArgumentException("Wrong number of pattern letters: " + cur);
                            }
                        }
                    }
                    throw new IllegalArgumentException("Unknown pattern letter: " + cur);
                }
                --pos;
                continue;
            }
            if (cur == '\'') {
                start = pos++;
                while (pos < pattern.length()) {
                    if (pattern.charAt(pos) == '\'') {
                        if (pos + 1 >= pattern.length() || pattern.charAt(pos + 1) != '\'') break;
                        ++pos;
                    }
                    ++pos;
                }
                if (pos >= pattern.length()) {
                    throw new IllegalArgumentException("Pattern ends with an incomplete string literal: " + pattern);
                }
                String str = pattern.substring(start + 1, pos);
                if (str.isEmpty()) {
                    this.appendLiteral('\'');
                    continue;
                }
                this.appendLiteral(str.replace("''", "'"));
                continue;
            }
            if (cur == '[') {
                this.optionalStart();
                continue;
            }
            if (cur == ']') {
                if (this.active.parent == null) {
                    throw new IllegalArgumentException("Pattern invalid as it contains ] without previous [");
                }
                this.optionalEnd();
                continue;
            }
            if (cur == '{' || cur == '}' || cur == '#') {
                throw new IllegalArgumentException("Pattern includes reserved character: '" + cur + "'");
            }
            this.appendLiteral(cur);
        }
    }

    private void parseField(char cur, int count, TemporalField field) {
        boolean standalone = false;
        block0 : switch (cur) {
            case 'u': 
            case 'y': {
                if (count == 2) {
                    this.appendValueReduced(field, 2, 2, ReducedPrinterParser.BASE_DATE);
                    break;
                }
                if (count < 4) {
                    this.appendValue(field, count, 19, SignStyle.NORMAL);
                    break;
                }
                this.appendValue(field, count, 19, SignStyle.EXCEEDS_PAD);
                break;
            }
            case 'c': {
                if (count == 1) {
                    this.appendValue(new WeekBasedFieldPrinterParser(cur, count, count, count));
                    break;
                }
                if (count == 2) {
                    throw new IllegalArgumentException("Invalid pattern \"cc\"");
                }
            }
            case 'L': 
            case 'q': {
                standalone = true;
            }
            case 'E': 
            case 'M': 
            case 'Q': 
            case 'e': {
                switch (count) {
                    case 1: 
                    case 2: {
                        if (cur == 'e') {
                            this.appendValue(new WeekBasedFieldPrinterParser(cur, count, count, count));
                            break block0;
                        }
                        if (cur == 'E') {
                            this.appendText(field, TextStyle.SHORT);
                            break block0;
                        }
                        if (count == 1) {
                            this.appendValue(field);
                            break block0;
                        }
                        this.appendValue(field, 2);
                        break block0;
                    }
                    case 3: {
                        this.appendText(field, standalone ? TextStyle.SHORT_STANDALONE : TextStyle.SHORT);
                        break block0;
                    }
                    case 4: {
                        this.appendText(field, standalone ? TextStyle.FULL_STANDALONE : TextStyle.FULL);
                        break block0;
                    }
                    case 5: {
                        this.appendText(field, standalone ? TextStyle.NARROW_STANDALONE : TextStyle.NARROW);
                        break block0;
                    }
                }
                throw new IllegalArgumentException("Too many pattern letters: " + cur);
            }
            case 'a': {
                if (count == 1) {
                    this.appendText(field, TextStyle.SHORT);
                    break;
                }
                throw new IllegalArgumentException("Too many pattern letters: " + cur);
            }
            case 'G': {
                switch (count) {
                    case 1: 
                    case 2: 
                    case 3: {
                        this.appendText(field, TextStyle.SHORT);
                        break block0;
                    }
                    case 4: {
                        this.appendText(field, TextStyle.FULL);
                        break block0;
                    }
                    case 5: {
                        this.appendText(field, TextStyle.NARROW);
                        break block0;
                    }
                }
                throw new IllegalArgumentException("Too many pattern letters: " + cur);
            }
            case 'S': {
                this.appendFraction(ChronoField.NANO_OF_SECOND, count, count, false);
                break;
            }
            case 'F': {
                if (count == 1) {
                    this.appendValue(field);
                    break;
                }
                throw new IllegalArgumentException("Too many pattern letters: " + cur);
            }
            case 'H': 
            case 'K': 
            case 'd': 
            case 'h': 
            case 'k': 
            case 'm': 
            case 's': {
                if (count == 1) {
                    this.appendValue(field);
                    break;
                }
                if (count == 2) {
                    this.appendValue(field, count);
                    break;
                }
                throw new IllegalArgumentException("Too many pattern letters: " + cur);
            }
            case 'D': {
                if (count == 1) {
                    this.appendValue(field);
                    break;
                }
                if (count == 2 || count == 3) {
                    this.appendValue(field, count, 3, SignStyle.NOT_NEGATIVE);
                    break;
                }
                throw new IllegalArgumentException("Too many pattern letters: " + cur);
            }
            case 'g': {
                this.appendValue(field, count, 19, SignStyle.NORMAL);
                break;
            }
            case 'A': 
            case 'N': 
            case 'n': {
                this.appendValue(field, count, 19, SignStyle.NOT_NEGATIVE);
                break;
            }
            default: {
                if (count == 1) {
                    this.appendValue(field);
                    break;
                }
                this.appendValue(field, count);
            }
        }
    }

    public DateTimeFormatterBuilder padNext(int padWidth) {
        return this.padNext(padWidth, ' ');
    }

    public DateTimeFormatterBuilder padNext(int padWidth, char padChar) {
        if (padWidth < 1) {
            throw new IllegalArgumentException("The pad width must be at least one but was " + padWidth);
        }
        this.active.padNextWidth = padWidth;
        this.active.padNextChar = padChar;
        this.active.valueParserIndex = -1;
        return this;
    }

    public DateTimeFormatterBuilder optionalStart() {
        this.active.valueParserIndex = -1;
        this.active = new DateTimeFormatterBuilder(this.active, true);
        return this;
    }

    public DateTimeFormatterBuilder optionalEnd() {
        if (this.active.parent == null) {
            throw new IllegalStateException("Cannot call optionalEnd() as there was no previous call to optionalStart()");
        }
        if (this.active.printerParsers.size() > 0) {
            CompositePrinterParser cpp = new CompositePrinterParser(this.active.printerParsers, this.active.optional);
            this.active = this.active.parent;
            this.appendInternal(cpp);
        } else {
            this.active = this.active.parent;
        }
        return this;
    }

    private int appendInternal(DateTimePrinterParser pp) {
        Objects.requireNonNull(pp, "pp");
        if (this.active.padNextWidth > 0) {
            pp = new PadPrinterParserDecorator(pp, this.active.padNextWidth, this.active.padNextChar);
            this.active.padNextWidth = 0;
            this.active.padNextChar = '\u0000';
        }
        this.active.printerParsers.add(pp);
        this.active.valueParserIndex = -1;
        return this.active.printerParsers.size() - 1;
    }

    public DateTimeFormatter toFormatter() {
        return this.toFormatter(Locale.getDefault(Locale.Category.FORMAT));
    }

    public DateTimeFormatter toFormatter(Locale locale) {
        return this.toFormatter(locale, ResolverStyle.SMART, null);
    }

    DateTimeFormatter toFormatter(ResolverStyle resolverStyle, Chronology chrono) {
        return this.toFormatter(Locale.getDefault(Locale.Category.FORMAT), resolverStyle, chrono);
    }

    private DateTimeFormatter toFormatter(Locale locale, ResolverStyle resolverStyle, Chronology chrono) {
        Objects.requireNonNull(locale, "locale");
        while (this.active.parent != null) {
            this.optionalEnd();
        }
        CompositePrinterParser pp = new CompositePrinterParser(this.printerParsers, false);
        return new DateTimeFormatter(pp, locale, DecimalStyle.STANDARD, resolverStyle, null, chrono, null);
    }

    static {
        FIELD_MAP.put(Character.valueOf('G'), ChronoField.ERA);
        FIELD_MAP.put(Character.valueOf('y'), ChronoField.YEAR_OF_ERA);
        FIELD_MAP.put(Character.valueOf('u'), ChronoField.YEAR);
        FIELD_MAP.put(Character.valueOf('Q'), IsoFields.QUARTER_OF_YEAR);
        FIELD_MAP.put(Character.valueOf('q'), IsoFields.QUARTER_OF_YEAR);
        FIELD_MAP.put(Character.valueOf('M'), ChronoField.MONTH_OF_YEAR);
        FIELD_MAP.put(Character.valueOf('L'), ChronoField.MONTH_OF_YEAR);
        FIELD_MAP.put(Character.valueOf('D'), ChronoField.DAY_OF_YEAR);
        FIELD_MAP.put(Character.valueOf('d'), ChronoField.DAY_OF_MONTH);
        FIELD_MAP.put(Character.valueOf('F'), ChronoField.ALIGNED_WEEK_OF_MONTH);
        FIELD_MAP.put(Character.valueOf('E'), ChronoField.DAY_OF_WEEK);
        FIELD_MAP.put(Character.valueOf('c'), ChronoField.DAY_OF_WEEK);
        FIELD_MAP.put(Character.valueOf('e'), ChronoField.DAY_OF_WEEK);
        FIELD_MAP.put(Character.valueOf('a'), ChronoField.AMPM_OF_DAY);
        FIELD_MAP.put(Character.valueOf('H'), ChronoField.HOUR_OF_DAY);
        FIELD_MAP.put(Character.valueOf('k'), ChronoField.CLOCK_HOUR_OF_DAY);
        FIELD_MAP.put(Character.valueOf('K'), ChronoField.HOUR_OF_AMPM);
        FIELD_MAP.put(Character.valueOf('h'), ChronoField.CLOCK_HOUR_OF_AMPM);
        FIELD_MAP.put(Character.valueOf('m'), ChronoField.MINUTE_OF_HOUR);
        FIELD_MAP.put(Character.valueOf('s'), ChronoField.SECOND_OF_MINUTE);
        FIELD_MAP.put(Character.valueOf('S'), ChronoField.NANO_OF_SECOND);
        FIELD_MAP.put(Character.valueOf('A'), ChronoField.MILLI_OF_DAY);
        FIELD_MAP.put(Character.valueOf('n'), ChronoField.NANO_OF_SECOND);
        FIELD_MAP.put(Character.valueOf('N'), ChronoField.NANO_OF_DAY);
        FIELD_MAP.put(Character.valueOf('g'), JulianFields.MODIFIED_JULIAN_DAY);
    }

    static enum SettingsParser implements DateTimePrinterParser
    {
        SENSITIVE,
        INSENSITIVE,
        STRICT,
        LENIENT;


        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            switch (this.ordinal()) {
                case 0: {
                    context.setCaseSensitive(true);
                    break;
                }
                case 1: {
                    context.setCaseSensitive(false);
                    break;
                }
                case 2: {
                    context.setStrict(true);
                    break;
                }
                case 3: {
                    context.setStrict(false);
                }
            }
            return position;
        }

        public String toString() {
            return switch (this.ordinal()) {
                case 0 -> "ParseCaseSensitive(true)";
                case 1 -> "ParseCaseSensitive(false)";
                case 2 -> "ParseStrict(true)";
                case 3 -> "ParseStrict(false)";
                default -> throw new IllegalStateException("Unreachable");
            };
        }
    }

    static interface DateTimePrinterParser {
        public boolean format(DateTimePrintContext var1, StringBuilder var2);

        public int parse(DateTimeParseContext var1, CharSequence var2, int var3);
    }

    static class DefaultValueParser
    implements DateTimePrinterParser {
        private final TemporalField field;
        private final long value;

        private DefaultValueParser(TemporalField field, long value) {
            this.field = field;
            this.value = value;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            if (context.getParsed(this.field) == null) {
                context.setParsedField(this.field, this.value, position, position);
            }
            return position;
        }
    }

    static class NumberPrinterParser
    implements DateTimePrinterParser {
        static final long[] EXCEED_POINTS = new long[]{0L, 10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L, 1000000000L, 10000000000L};
        final TemporalField field;
        final int minWidth;
        final int maxWidth;
        private final SignStyle signStyle;
        final int subsequentWidth;

        private NumberPrinterParser(TemporalField field, int minWidth, int maxWidth, SignStyle signStyle) {
            this.field = field;
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
            this.signStyle = signStyle;
            this.subsequentWidth = 0;
        }

        protected NumberPrinterParser(TemporalField field, int minWidth, int maxWidth, SignStyle signStyle, int subsequentWidth) {
            this.field = field;
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
            this.signStyle = signStyle;
            this.subsequentWidth = subsequentWidth;
        }

        NumberPrinterParser withFixedWidth() {
            if (this.subsequentWidth == -1) {
                return this;
            }
            return new NumberPrinterParser(this.field, this.minWidth, this.maxWidth, this.signStyle, -1);
        }

        NumberPrinterParser withSubsequentWidth(int subsequentWidth) {
            return new NumberPrinterParser(this.field, this.minWidth, this.maxWidth, this.signStyle, this.subsequentWidth + subsequentWidth);
        }

        private static int stringSize(long x) {
            int d = 1;
            if (x >= 0L) {
                d = 0;
                x = -x;
            }
            long p = -10L;
            for (int i = 1; i < 19; ++i) {
                if (x > p) {
                    return i + d;
                }
                p = 10L * p;
            }
            return 19 + d;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long valueLong = context.getValue(this.field);
            if (valueLong == null) {
                return false;
            }
            long value = this.getValue(context, valueLong);
            DecimalStyle decimalStyle = context.getDecimalStyle();
            int size = NumberPrinterParser.stringSize(value);
            if (value < 0L) {
                --size;
            }
            if (size > this.maxWidth) {
                throw new DateTimeException("Field " + this.field + " cannot be printed as the value " + value + " exceeds the maximum print width of " + this.maxWidth);
            }
            if (value >= 0L) {
                switch (this.signStyle) {
                    case EXCEEDS_PAD: {
                        if (this.minWidth >= 19 || size <= this.minWidth) break;
                        buf.append(decimalStyle.getPositiveSign());
                        break;
                    }
                    case ALWAYS: {
                        buf.append(decimalStyle.getPositiveSign());
                    }
                }
            } else {
                switch (this.signStyle) {
                    case EXCEEDS_PAD: 
                    case ALWAYS: 
                    case NORMAL: {
                        buf.append(decimalStyle.getNegativeSign());
                        break;
                    }
                    case NOT_NEGATIVE: {
                        throw new DateTimeException("Field " + this.field + " cannot be printed as the value " + value + " cannot be negative according to the SignStyle");
                    }
                }
            }
            char zeroDigit = decimalStyle.getZeroDigit();
            for (int i = 0; i < this.minWidth - size; ++i) {
                buf.append(zeroDigit);
            }
            if (zeroDigit == '0' && value != Long.MIN_VALUE) {
                buf.append(Math.abs(value));
            } else {
                String str = value == Long.MIN_VALUE ? "9223372036854775808" : Long.toString(Math.abs(value));
                buf.append(decimalStyle.convertNumberToI18N(str));
            }
            return true;
        }

        long getValue(DateTimePrintContext context, long value) {
            return value;
        }

        boolean isFixedWidth(DateTimeParseContext context) {
            return this.subsequentWidth == -1 || this.subsequentWidth > 0 && this.minWidth == this.maxWidth && this.signStyle == SignStyle.NOT_NEGATIVE;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int length = text.length();
            if (position == length) {
                return ~position;
            }
            char sign = text.charAt(position);
            boolean negative = false;
            boolean positive = false;
            if (sign == context.getDecimalStyle().getPositiveSign()) {
                if (!this.signStyle.parse(true, context.isStrict(), this.minWidth == this.maxWidth)) {
                    return ~position;
                }
                positive = true;
                ++position;
            } else if (sign == context.getDecimalStyle().getNegativeSign()) {
                if (!this.signStyle.parse(false, context.isStrict(), this.minWidth == this.maxWidth)) {
                    return ~position;
                }
                negative = true;
                ++position;
            } else if (this.signStyle == SignStyle.ALWAYS && context.isStrict()) {
                return ~position;
            }
            int effMinWidth = context.isStrict() || this.isFixedWidth(context) ? this.minWidth : 1;
            int minEndPos = position + effMinWidth;
            if (minEndPos > length) {
                return ~position;
            }
            int effMaxWidth = (context.isStrict() || this.isFixedWidth(context) ? this.maxWidth : 9) + Math.max(this.subsequentWidth, 0);
            long total = 0L;
            BigInteger totalBig = null;
            int pos = position;
            for (int pass = 0; pass < 2; ++pass) {
                int maxEndPos = Math.min(pos + effMaxWidth, length);
                while (pos < maxEndPos) {
                    char ch = text.charAt(pos++);
                    int digit = context.getDecimalStyle().convertToDigit(ch);
                    if (digit < 0) {
                        if (--pos >= minEndPos) break;
                        return ~position;
                    }
                    if (pos - position > 18) {
                        if (totalBig == null) {
                            totalBig = BigInteger.valueOf(total);
                        }
                        totalBig = totalBig.multiply(BigInteger.TEN).add(BigInteger.valueOf(digit));
                        continue;
                    }
                    total = total * 10L + (long)digit;
                }
                if (this.subsequentWidth <= 0 || pass != 0) break;
                int parseLen = pos - position;
                effMaxWidth = Math.max(effMinWidth, parseLen - this.subsequentWidth);
                pos = position;
                total = 0L;
                totalBig = null;
            }
            if (negative) {
                if (totalBig != null) {
                    if (totalBig.equals(BigInteger.ZERO) && context.isStrict()) {
                        return ~(position - 1);
                    }
                    totalBig = totalBig.negate();
                } else {
                    if (total == 0L && context.isStrict()) {
                        return ~(position - 1);
                    }
                    total = -total;
                }
            } else if (this.signStyle == SignStyle.EXCEEDS_PAD && context.isStrict()) {
                int parseLen = pos - position;
                if (positive) {
                    if (parseLen <= this.minWidth) {
                        return ~(position - 1);
                    }
                } else if (parseLen > this.minWidth) {
                    return ~position;
                }
            }
            if (totalBig != null) {
                if (totalBig.bitLength() > 63) {
                    totalBig = totalBig.divide(BigInteger.TEN);
                    --pos;
                }
                return this.setValue(context, totalBig.longValue(), position, pos);
            }
            return this.setValue(context, total, position, pos);
        }

        int setValue(DateTimeParseContext context, long value, int errorPos, int successPos) {
            return context.setParsedField(this.field, value, errorPos, successPos);
        }

        public String toString() {
            if (this.minWidth == 1 && this.maxWidth == 19 && this.signStyle == SignStyle.NORMAL) {
                return "Value(" + this.field + ")";
            }
            if (this.minWidth == this.maxWidth && this.signStyle == SignStyle.NOT_NEGATIVE) {
                return "Value(" + this.field + "," + this.minWidth + ")";
            }
            return "Value(" + this.field + "," + this.minWidth + "," + this.maxWidth + "," + (Object)((Object)this.signStyle) + ")";
        }
    }

    static final class ReducedPrinterParser
    extends NumberPrinterParser {
        static final LocalDate BASE_DATE = LocalDate.of(2000, 1, 1);
        private final int baseValue;
        private final ChronoLocalDate baseDate;

        private ReducedPrinterParser(TemporalField field, int minWidth, int maxWidth, int baseValue, ChronoLocalDate baseDate) {
            this(field, minWidth, maxWidth, baseValue, baseDate, 0);
            if (minWidth < 1 || minWidth > 10) {
                throw new IllegalArgumentException("The minWidth must be from 1 to 10 inclusive but was " + minWidth);
            }
            if (maxWidth < 1 || maxWidth > 10) {
                throw new IllegalArgumentException("The maxWidth must be from 1 to 10 inclusive but was " + minWidth);
            }
            if (maxWidth < minWidth) {
                throw new IllegalArgumentException("Maximum width must exceed or equal the minimum width but " + maxWidth + " < " + minWidth);
            }
            if (baseDate == null) {
                if (!field.range().isValidValue(baseValue)) {
                    throw new IllegalArgumentException("The base value must be within the range of the field");
                }
                if ((long)baseValue + EXCEED_POINTS[maxWidth] > Integer.MAX_VALUE) {
                    throw new DateTimeException("Unable to add printer-parser as the range exceeds the capacity of an int");
                }
            }
        }

        private ReducedPrinterParser(TemporalField field, int minWidth, int maxWidth, int baseValue, ChronoLocalDate baseDate, int subsequentWidth) {
            super(field, minWidth, maxWidth, SignStyle.NOT_NEGATIVE, subsequentWidth);
            this.baseValue = baseValue;
            this.baseDate = baseDate;
        }

        @Override
        long getValue(DateTimePrintContext context, long value) {
            long absValue = Math.abs(value);
            int baseValue = this.baseValue;
            if (this.baseDate != null) {
                Chronology chrono = Chronology.from(context.getTemporal());
                baseValue = chrono.date(this.baseDate).get(this.field);
            }
            if (value >= (long)baseValue && value < (long)baseValue + EXCEED_POINTS[this.minWidth]) {
                return absValue % EXCEED_POINTS[this.minWidth];
            }
            return absValue % EXCEED_POINTS[this.maxWidth];
        }

        @Override
        int setValue(DateTimeParseContext context, long value, int errorPos, int successPos) {
            int parseLen;
            int baseValue = this.baseValue;
            if (this.baseDate != null) {
                Chronology chrono = context.getEffectiveChronology();
                baseValue = chrono.date(this.baseDate).get(this.field);
                long initialValue = value;
                context.addChronoChangedListener(_unused -> this.setValue(context, initialValue, errorPos, successPos));
            }
            if ((parseLen = successPos - errorPos) == this.minWidth && value >= 0L) {
                long range = EXCEED_POINTS[this.minWidth];
                long lastPart = (long)baseValue % range;
                long basePart = (long)baseValue - lastPart;
                value = baseValue > 0 ? basePart + value : basePart - value;
                if (value < (long)baseValue) {
                    value += range;
                }
            }
            return context.setParsedField(this.field, value, errorPos, successPos);
        }

        @Override
        ReducedPrinterParser withFixedWidth() {
            if (this.subsequentWidth == -1) {
                return this;
            }
            return new ReducedPrinterParser(this.field, this.minWidth, this.maxWidth, this.baseValue, this.baseDate, -1);
        }

        @Override
        ReducedPrinterParser withSubsequentWidth(int subsequentWidth) {
            return new ReducedPrinterParser(this.field, this.minWidth, this.maxWidth, this.baseValue, this.baseDate, this.subsequentWidth + subsequentWidth);
        }

        @Override
        boolean isFixedWidth(DateTimeParseContext context) {
            if (!context.isStrict()) {
                return false;
            }
            return super.isFixedWidth(context);
        }

        @Override
        public String toString() {
            return "ReducedValue(" + this.field + "," + this.minWidth + "," + this.maxWidth + "," + Objects.requireNonNullElse(this.baseDate, this.baseValue) + ")";
        }
    }

    static final class NanosPrinterParser
    extends NumberPrinterParser {
        private final boolean decimalPoint;
        private static final int[] TENS = new int[]{1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};

        private NanosPrinterParser(int minWidth, int maxWidth, boolean decimalPoint) {
            this(minWidth, maxWidth, decimalPoint, 0);
            if (minWidth < 0 || minWidth > 9) {
                throw new IllegalArgumentException("Minimum width must be from 0 to 9 inclusive but was " + minWidth);
            }
            if (maxWidth < 1 || maxWidth > 9) {
                throw new IllegalArgumentException("Maximum width must be from 1 to 9 inclusive but was " + maxWidth);
            }
            if (maxWidth < minWidth) {
                throw new IllegalArgumentException("Maximum width must exceed or equal the minimum width but " + maxWidth + " < " + minWidth);
            }
        }

        private NanosPrinterParser(int minWidth, int maxWidth, boolean decimalPoint, int subsequentWidth) {
            super(ChronoField.NANO_OF_SECOND, minWidth, maxWidth, SignStyle.NOT_NEGATIVE, subsequentWidth);
            this.decimalPoint = decimalPoint;
        }

        @Override
        NanosPrinterParser withFixedWidth() {
            if (this.subsequentWidth == -1) {
                return this;
            }
            return new NanosPrinterParser(this.minWidth, this.maxWidth, this.decimalPoint, -1);
        }

        @Override
        NanosPrinterParser withSubsequentWidth(int subsequentWidth) {
            return new NanosPrinterParser(this.minWidth, this.maxWidth, this.decimalPoint, this.subsequentWidth + subsequentWidth);
        }

        @Override
        boolean isFixedWidth(DateTimeParseContext context) {
            return context.isStrict() && this.minWidth == this.maxWidth && !this.decimalPoint;
        }

        private static int stringSize(int x) {
            int p = 10;
            for (int i = 1; i < 10; ++i) {
                if (x < p) {
                    return i;
                }
                p = 10 * p;
            }
            return 10;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long value = context.getValue(this.field);
            if (value == null) {
                return false;
            }
            int val = this.field.range().checkValidIntValue(value, this.field);
            DecimalStyle decimalStyle = context.getDecimalStyle();
            int stringSize = NanosPrinterParser.stringSize(val);
            char zero = decimalStyle.getZeroDigit();
            if (val == 0 || stringSize < 10 - this.maxWidth) {
                int width;
                int n = width = val == 0 ? this.minWidth : this.maxWidth;
                if (width > 0) {
                    if (this.decimalPoint) {
                        buf.append(decimalStyle.getDecimalSeparator());
                    }
                    for (int i = 0; i < width; ++i) {
                        buf.append(zero);
                    }
                }
            } else {
                int i;
                if (this.decimalPoint) {
                    buf.append(decimalStyle.getDecimalSeparator());
                }
                for (i = 9 - stringSize; i > 0; --i) {
                    buf.append(zero);
                }
                if (this.maxWidth < 9) {
                    val /= TENS[9 - this.maxWidth];
                }
                for (i = this.maxWidth; i > this.minWidth && val % 10 == 0; --i) {
                    val /= 10;
                }
                if (zero == '0') {
                    buf.append(val);
                } else {
                    buf.append(decimalStyle.convertNumberToI18N(Integer.toString(val)));
                }
            }
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int pos;
            int minEndPos;
            int effectiveMin = context.isStrict() || this.isFixedWidth(context) ? this.minWidth : 0;
            int effectiveMax = context.isStrict() || this.isFixedWidth(context) ? this.maxWidth : 9;
            int length = text.length();
            if (position == length) {
                return effectiveMin > 0 ? ~position : position;
            }
            if (this.decimalPoint) {
                if (text.charAt(position) != context.getDecimalStyle().getDecimalSeparator()) {
                    return effectiveMin > 0 ? ~position : position;
                }
                ++position;
            }
            if ((minEndPos = position + effectiveMin) > length) {
                return ~position;
            }
            int maxEndPos = Math.min(position + effectiveMax, length);
            int total = 0;
            for (pos = position; pos < maxEndPos; ++pos) {
                char ch = text.charAt(pos);
                int digit = context.getDecimalStyle().convertToDigit(ch);
                if (digit < 0) {
                    if (pos >= minEndPos) break;
                    return ~position;
                }
                total = total * 10 + digit;
            }
            for (int i = 9 - (pos - position); i > 0; --i) {
                total *= 10;
            }
            return context.setParsedField(this.field, total, position, pos);
        }

        @Override
        public String toString() {
            String decimal = this.decimalPoint ? ",DecimalPoint" : "";
            return "Fraction(" + this.field + "," + this.minWidth + "," + this.maxWidth + decimal + ")";
        }
    }

    static final class FractionPrinterParser
    extends NumberPrinterParser {
        private final boolean decimalPoint;
        private final BigDecimal minBD;
        private final BigDecimal rangeBD;

        private FractionPrinterParser(TemporalField field, int minWidth, int maxWidth, boolean decimalPoint) {
            this(field, minWidth, maxWidth, decimalPoint, 0);
            Objects.requireNonNull(field, "field");
            if (!field.range().isFixed()) {
                throw new IllegalArgumentException("Field must have a fixed set of values: " + field);
            }
            if (minWidth < 0 || minWidth > 9) {
                throw new IllegalArgumentException("Minimum width must be from 0 to 9 inclusive but was " + minWidth);
            }
            if (maxWidth < 1 || maxWidth > 9) {
                throw new IllegalArgumentException("Maximum width must be from 1 to 9 inclusive but was " + maxWidth);
            }
            if (maxWidth < minWidth) {
                throw new IllegalArgumentException("Maximum width must exceed or equal the minimum width but " + maxWidth + " < " + minWidth);
            }
        }

        private FractionPrinterParser(TemporalField field, int minWidth, int maxWidth, boolean decimalPoint, int subsequentWidth) {
            super(field, minWidth, maxWidth, SignStyle.NOT_NEGATIVE, subsequentWidth);
            this.decimalPoint = decimalPoint;
            ValueRange range = field.range();
            this.minBD = BigDecimal.valueOf(range.getMinimum());
            this.rangeBD = BigDecimal.valueOf(range.getMaximum()).subtract(this.minBD).add(BigDecimal.ONE);
        }

        @Override
        FractionPrinterParser withFixedWidth() {
            if (this.subsequentWidth == -1) {
                return this;
            }
            return new FractionPrinterParser(this.field, this.minWidth, this.maxWidth, this.decimalPoint, -1);
        }

        @Override
        FractionPrinterParser withSubsequentWidth(int subsequentWidth) {
            return new FractionPrinterParser(this.field, this.minWidth, this.maxWidth, this.decimalPoint, this.subsequentWidth + subsequentWidth);
        }

        @Override
        boolean isFixedWidth(DateTimeParseContext context) {
            return context.isStrict() && this.minWidth == this.maxWidth && !this.decimalPoint;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long value = context.getValue(this.field);
            if (value == null) {
                return false;
            }
            DecimalStyle decimalStyle = context.getDecimalStyle();
            BigDecimal fraction = this.convertToFraction(value);
            if (fraction.scale() == 0) {
                if (this.minWidth > 0) {
                    if (this.decimalPoint) {
                        buf.append(decimalStyle.getDecimalSeparator());
                    }
                    for (int i = 0; i < this.minWidth; ++i) {
                        buf.append(decimalStyle.getZeroDigit());
                    }
                }
            } else {
                int outputScale = Math.clamp((long)fraction.scale(), this.minWidth, this.maxWidth);
                fraction = fraction.setScale(outputScale, RoundingMode.FLOOR);
                if (this.decimalPoint) {
                    buf.append(decimalStyle.getDecimalSeparator());
                }
                String str = fraction.toPlainString();
                str = decimalStyle.convertNumberToI18N(str);
                buf.append(str, 2, str.length());
            }
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int minEndPos;
            int effectiveMin = context.isStrict() || this.isFixedWidth(context) ? this.minWidth : 0;
            int effectiveMax = context.isStrict() || this.isFixedWidth(context) ? this.maxWidth : 9;
            int length = text.length();
            if (position == length) {
                return effectiveMin > 0 ? ~position : position;
            }
            if (this.decimalPoint) {
                if (text.charAt(position) != context.getDecimalStyle().getDecimalSeparator()) {
                    return effectiveMin > 0 ? ~position : position;
                }
                ++position;
            }
            if ((minEndPos = position + effectiveMin) > length) {
                return ~position;
            }
            int maxEndPos = Math.min(position + effectiveMax, length);
            int total = 0;
            int pos = position;
            while (pos < maxEndPos) {
                char ch = text.charAt(pos++);
                int digit = context.getDecimalStyle().convertToDigit(ch);
                if (digit < 0) {
                    if (pos <= minEndPos) {
                        return ~position;
                    }
                    --pos;
                    break;
                }
                total = total * 10 + digit;
            }
            BigDecimal fraction = new BigDecimal(total).movePointLeft(pos - position);
            long value = this.convertFromFraction(fraction);
            return context.setParsedField(this.field, value, position, pos);
        }

        private BigDecimal convertToFraction(long value) {
            this.field.range().checkValidValue(value, this.field);
            BigDecimal valueBD = BigDecimal.valueOf(value).subtract(this.minBD);
            BigDecimal fraction = valueBD.divide(this.rangeBD, 9, RoundingMode.FLOOR);
            return fraction.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : fraction.stripTrailingZeros();
        }

        private long convertFromFraction(BigDecimal fraction) {
            BigDecimal valueBD = fraction.multiply(this.rangeBD).setScale(0, RoundingMode.FLOOR).add(this.minBD);
            return valueBD.longValueExact();
        }

        @Override
        public String toString() {
            String decimal = this.decimalPoint ? ",DecimalPoint" : "";
            return "Fraction(" + this.field + "," + this.minWidth + "," + this.maxWidth + decimal + ")";
        }
    }

    static final class TextPrinterParser
    implements DateTimePrinterParser {
        private final TemporalField field;
        private final TextStyle textStyle;
        private final DateTimeTextProvider provider;
        private volatile NumberPrinterParser numberPrinterParser;

        private TextPrinterParser(TemporalField field, TextStyle textStyle, DateTimeTextProvider provider) {
            this.field = field;
            this.textStyle = textStyle;
            this.provider = provider;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long value = context.getValue(this.field);
            if (value == null) {
                return false;
            }
            Chronology chrono = context.getTemporal().query(TemporalQueries.chronology());
            String text = chrono == null || chrono == IsoChronology.INSTANCE ? this.provider.getText(this.field, value, this.textStyle, context.getLocale()) : this.provider.getText(chrono, this.field, value, this.textStyle, context.getLocale());
            if (text == null) {
                return this.numberPrinterParser().format(context, buf);
            }
            buf.append(text);
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence parseText, int position) {
            int length = parseText.length();
            if (position < 0 || position > length) {
                throw new IndexOutOfBoundsException();
            }
            TextStyle style = context.isStrict() ? this.textStyle : null;
            Chronology chrono = context.getEffectiveChronology();
            Iterator<Map.Entry<String, Long>> it = chrono == null || chrono == IsoChronology.INSTANCE ? this.provider.getTextIterator(this.field, style, context.getLocale()) : this.provider.getTextIterator(chrono, this.field, style, context.getLocale());
            if (it != null) {
                while (it.hasNext()) {
                    Map.Entry<String, Long> entry = it.next();
                    String itText = entry.getKey();
                    if (!context.subSequenceEquals(itText, 0, parseText, position, itText.length())) continue;
                    return context.setParsedField(this.field, entry.getValue(), position, position + itText.length());
                }
                if (this.field == ChronoField.ERA && !context.isStrict()) {
                    List<Era> eras = chrono.eras();
                    for (Era era : eras) {
                        String name = era.toString();
                        if (!context.subSequenceEquals(name, 0, parseText, position, name.length())) continue;
                        return context.setParsedField(this.field, era.getValue(), position, position + name.length());
                    }
                }
                if (context.isStrict()) {
                    return ~position;
                }
            }
            return this.numberPrinterParser().parse(context, parseText, position);
        }

        private NumberPrinterParser numberPrinterParser() {
            if (this.numberPrinterParser == null) {
                this.numberPrinterParser = new NumberPrinterParser(this.field, 1, 19, SignStyle.NORMAL);
            }
            return this.numberPrinterParser;
        }

        public String toString() {
            if (this.textStyle == TextStyle.FULL) {
                return "Text(" + this.field + ")";
            }
            return "Text(" + this.field + "," + (Object)((Object)this.textStyle) + ")";
        }
    }

    static final class InstantPrinterParser
    implements DateTimePrinterParser {
        private static final long SECONDS_PER_10000_YEARS = 315569520000L;
        private static final long SECONDS_0000_TO_1970 = 62167219200L;
        private final int fractionalDigits;

        private InstantPrinterParser(int fractionalDigits) {
            this.fractionalDigits = fractionalDigits;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long inSecs = context.getValue(ChronoField.INSTANT_SECONDS);
            Long inNanos = null;
            if (context.getTemporal().isSupported(ChronoField.NANO_OF_SECOND)) {
                inNanos = context.getTemporal().getLong(ChronoField.NANO_OF_SECOND);
            }
            if (inSecs == null) {
                return false;
            }
            long inSec = inSecs;
            int inNano = ChronoField.NANO_OF_SECOND.checkValidIntValue(inNanos != null ? inNanos : 0L);
            if (inSec >= -62167219200L) {
                zeroSecs = inSec - 315569520000L + 62167219200L;
                hi = Math.floorDiv(zeroSecs, 315569520000L) + 1L;
                lo = Math.floorMod(zeroSecs, 315569520000L);
                ldt = LocalDateTime.ofEpochSecond(lo - 62167219200L, 0, ZoneOffset.UTC);
                if (hi > 0L) {
                    buf.append('+').append(hi);
                }
                buf.append(ldt);
                if (ldt.getSecond() == 0) {
                    buf.append(":00");
                }
            } else {
                zeroSecs = inSec + 62167219200L;
                hi = zeroSecs / 315569520000L;
                lo = zeroSecs % 315569520000L;
                ldt = LocalDateTime.ofEpochSecond(lo - 62167219200L, 0, ZoneOffset.UTC);
                int pos = buf.length();
                buf.append(ldt);
                if (ldt.getSecond() == 0) {
                    buf.append(":00");
                }
                if (hi < 0L) {
                    if (ldt.getYear() == -10000) {
                        buf.replace(pos, pos + 2, Long.toString(hi - 1L));
                    } else if (lo == 0L) {
                        buf.insert(pos, hi);
                    } else {
                        buf.insert(pos + 1, Math.abs(hi));
                    }
                }
            }
            if (this.fractionalDigits < 0 && inNano > 0 || this.fractionalDigits > 0) {
                buf.append('.');
                int div = 100000000;
                for (int i = 0; this.fractionalDigits == -1 && inNano > 0 || this.fractionalDigits == -2 && (inNano > 0 || i % 3 != 0) || i < this.fractionalDigits; ++i) {
                    int digit = inNano / div;
                    buf.append((char)(digit + 48));
                    inNano -= digit * div;
                    div /= 10;
                }
            }
            buf.append('Z');
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            DateTimeParseContext newContext;
            int minDigits = this.fractionalDigits < 0 ? 0 : this.fractionalDigits;
            int maxDigits = this.fractionalDigits < 0 ? 9 : this.fractionalDigits;
            CompositePrinterParser parser = new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE).appendLiteral('T').appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2).appendFraction(ChronoField.NANO_OF_SECOND, minDigits, maxDigits, true).appendOffsetId().toFormatter().toPrinterParser(false);
            int pos = parser.parse(newContext = context.copy(), text, position);
            if (pos < 0) {
                return pos;
            }
            long yearParsed = newContext.getParsed(ChronoField.YEAR);
            int month = newContext.getParsed(ChronoField.MONTH_OF_YEAR).intValue();
            int day = newContext.getParsed(ChronoField.DAY_OF_MONTH).intValue();
            int hour = newContext.getParsed(ChronoField.HOUR_OF_DAY).intValue();
            int min = newContext.getParsed(ChronoField.MINUTE_OF_HOUR).intValue();
            Long secVal = newContext.getParsed(ChronoField.SECOND_OF_MINUTE);
            Long nanoVal = newContext.getParsed(ChronoField.NANO_OF_SECOND);
            int sec = secVal != null ? secVal.intValue() : 0;
            int nano = nanoVal != null ? nanoVal.intValue() : 0;
            int offset = newContext.getParsed(ChronoField.OFFSET_SECONDS).intValue();
            int days = 0;
            if (hour == 24 && min == 0 && sec == 0 && nano == 0) {
                hour = 0;
                days = 1;
            } else if (hour == 23 && min == 59 && sec == 60) {
                context.setParsedLeapSecond();
                sec = 59;
            }
            int year = (int)yearParsed % 10000;
            try {
                LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, min, sec, 0).plusDays(days);
                long instantSecs = ldt.toEpochSecond(ZoneOffset.ofTotalSeconds(offset));
            }
            catch (RuntimeException ex) {
                return ~position;
            }
            int successPos = pos;
            successPos = context.setParsedField(ChronoField.INSTANT_SECONDS, instantSecs += Math.multiplyExact(yearParsed / 10000L, 315569520000L), position, successPos);
            return context.setParsedField(ChronoField.NANO_OF_SECOND, nano, position, successPos);
        }

        public String toString() {
            return "Instant()";
        }
    }

    static final class OffsetIdPrinterParser
    implements DateTimePrinterParser {
        static final String[] PATTERNS = new String[]{"+HH", "+HHmm", "+HH:mm", "+HHMM", "+HH:MM", "+HHMMss", "+HH:MM:ss", "+HHMMSS", "+HH:MM:SS", "+HHmmss", "+HH:mm:ss", "+H", "+Hmm", "+H:mm", "+HMM", "+H:MM", "+HMMss", "+H:MM:ss", "+HMMSS", "+H:MM:SS", "+Hmmss", "+H:mm:ss"};
        static final OffsetIdPrinterParser INSTANCE_ID_Z = new OffsetIdPrinterParser("+HH:MM:ss", "Z");
        static final OffsetIdPrinterParser INSTANCE_ID_ZERO = new OffsetIdPrinterParser("+HH:MM:ss", "0");
        private final String noOffsetText;
        private final int type;
        private final int style;

        private OffsetIdPrinterParser(String pattern, String noOffsetText) {
            Objects.requireNonNull(pattern, "pattern");
            Objects.requireNonNull(noOffsetText, "noOffsetText");
            this.type = this.checkPattern(pattern);
            this.style = this.type % 11;
            this.noOffsetText = noOffsetText;
        }

        private int checkPattern(String pattern) {
            for (int i = 0; i < PATTERNS.length; ++i) {
                if (!PATTERNS[i].equals(pattern)) continue;
                return i;
            }
            throw new IllegalArgumentException("Invalid zone offset pattern: " + pattern);
        }

        private boolean isPaddedHour() {
            return this.type < 11;
        }

        private boolean isColon() {
            return this.style > 0 && this.style % 2 == 0;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long offsetSecs = context.getValue(ChronoField.OFFSET_SECONDS);
            if (offsetSecs == null) {
                return false;
            }
            int totalSecs = Math.toIntExact(offsetSecs);
            if (totalSecs == 0) {
                buf.append(this.noOffsetText);
            } else {
                int absHours = Math.abs(totalSecs / 3600 % 100);
                int absMinutes = Math.abs(totalSecs / 60 % 60);
                int absSeconds = Math.abs(totalSecs % 60);
                int bufPos = buf.length();
                int output = absHours;
                buf.append(totalSecs < 0 ? "-" : "+");
                if (this.isPaddedHour() || absHours >= 10) {
                    this.formatZeroPad(false, absHours, buf);
                } else {
                    buf.append((char)(absHours + 48));
                }
                if (this.style >= 3 && this.style <= 8 || this.style >= 9 && absSeconds > 0 || this.style >= 1 && absMinutes > 0) {
                    this.formatZeroPad(this.isColon(), absMinutes, buf);
                    output += absMinutes;
                    if (this.style == 7 || this.style == 8 || this.style >= 5 && absSeconds > 0) {
                        this.formatZeroPad(this.isColon(), absSeconds, buf);
                        output += absSeconds;
                    }
                }
                if (output == 0) {
                    buf.setLength(bufPos);
                    buf.append(this.noOffsetText);
                }
            }
            return true;
        }

        private void formatZeroPad(boolean colon, int value, StringBuilder buf) {
            buf.append(colon ? ":" : "").append((char)(value / 10 + 48)).append((char)(value % 10 + 48));
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            char sign;
            int length = text.length();
            int noOffsetLen = this.noOffsetText.length();
            if (noOffsetLen == 0) {
                if (position == length) {
                    return context.setParsedField(ChronoField.OFFSET_SECONDS, 0L, position, position);
                }
            } else {
                if (position == length) {
                    return ~position;
                }
                if (context.subSequenceEquals(text, position, this.noOffsetText, 0, noOffsetLen)) {
                    return context.setParsedField(ChronoField.OFFSET_SECONDS, 0L, position, position + noOffsetLen);
                }
            }
            if ((sign = text.charAt(position)) == '+' || sign == '-') {
                int negative = sign == '-' ? -1 : 1;
                boolean isColon = this.isColon();
                boolean paddedHour = this.isPaddedHour();
                int[] array = new int[4];
                array[0] = position + 1;
                int parseType = this.type;
                if (!context.isStrict()) {
                    if (paddedHour) {
                        if (isColon || parseType == 0 && length > position + 3 && text.charAt(position + 3) == ':') {
                            isColon = true;
                            parseType = 10;
                        } else {
                            parseType = 9;
                        }
                    } else if (isColon || parseType == 11 && length > position + 3 && (text.charAt(position + 2) == ':' || text.charAt(position + 3) == ':')) {
                        isColon = true;
                        parseType = 21;
                    } else {
                        parseType = 20;
                    }
                }
                switch (parseType) {
                    case 0: 
                    case 11: {
                        this.parseHour(text, paddedHour, array);
                        break;
                    }
                    case 1: 
                    case 2: 
                    case 13: {
                        this.parseHour(text, paddedHour, array);
                        this.parseMinute(text, isColon, false, array);
                        break;
                    }
                    case 3: 
                    case 4: 
                    case 15: {
                        this.parseHour(text, paddedHour, array);
                        this.parseMinute(text, isColon, true, array);
                        break;
                    }
                    case 5: 
                    case 6: 
                    case 17: {
                        this.parseHour(text, paddedHour, array);
                        this.parseMinute(text, isColon, true, array);
                        this.parseSecond(text, isColon, false, array);
                        break;
                    }
                    case 7: 
                    case 8: 
                    case 19: {
                        this.parseHour(text, paddedHour, array);
                        this.parseMinute(text, isColon, true, array);
                        this.parseSecond(text, isColon, true, array);
                        break;
                    }
                    case 9: 
                    case 10: 
                    case 21: {
                        this.parseHour(text, paddedHour, array);
                        this.parseOptionalMinuteSecond(text, isColon, array);
                        break;
                    }
                    case 12: {
                        this.parseVariableWidthDigits(text, 1, 4, array);
                        break;
                    }
                    case 14: {
                        this.parseVariableWidthDigits(text, 3, 4, array);
                        break;
                    }
                    case 16: {
                        this.parseVariableWidthDigits(text, 3, 6, array);
                        break;
                    }
                    case 18: {
                        this.parseVariableWidthDigits(text, 5, 6, array);
                        break;
                    }
                    case 20: {
                        this.parseVariableWidthDigits(text, 1, 6, array);
                    }
                }
                if (array[0] > 0) {
                    if (array[1] > 23 || array[2] > 59 || array[3] > 59) {
                        throw new DateTimeException("Value out of range: Hour[0-23], Minute[0-59], Second[0-59]");
                    }
                    long offsetSecs = (long)negative * ((long)array[1] * 3600L + (long)array[2] * 60L + (long)array[3]);
                    return context.setParsedField(ChronoField.OFFSET_SECONDS, offsetSecs, position, array[0]);
                }
            }
            if (noOffsetLen == 0) {
                return context.setParsedField(ChronoField.OFFSET_SECONDS, 0L, position, position);
            }
            return ~position;
        }

        private void parseHour(CharSequence parseText, boolean paddedHour, int[] array) {
            if (paddedHour) {
                if (!this.parseDigits(parseText, false, 1, array)) {
                    array[0] = ~array[0];
                }
            } else {
                this.parseVariableWidthDigits(parseText, 1, 2, array);
            }
        }

        private void parseMinute(CharSequence parseText, boolean isColon, boolean mandatory, int[] array) {
            if (!this.parseDigits(parseText, isColon, 2, array) && mandatory) {
                array[0] = ~array[0];
            }
        }

        private void parseSecond(CharSequence parseText, boolean isColon, boolean mandatory, int[] array) {
            if (!this.parseDigits(parseText, isColon, 3, array) && mandatory) {
                array[0] = ~array[0];
            }
        }

        private void parseOptionalMinuteSecond(CharSequence parseText, boolean isColon, int[] array) {
            if (this.parseDigits(parseText, isColon, 2, array)) {
                this.parseDigits(parseText, isColon, 3, array);
            }
        }

        private boolean parseDigits(CharSequence parseText, boolean isColon, int arrayIndex, int[] array) {
            int pos = array[0];
            if (pos < 0) {
                return true;
            }
            if (isColon && arrayIndex != 1) {
                if (pos + 1 > parseText.length() || parseText.charAt(pos) != ':') {
                    return false;
                }
                ++pos;
            }
            if (pos + 2 > parseText.length()) {
                return false;
            }
            char ch1 = parseText.charAt(pos++);
            char ch2 = parseText.charAt(pos++);
            if (ch1 < '0' || ch1 > '9' || ch2 < '0' || ch2 > '9') {
                return false;
            }
            int value = (ch1 - 48) * 10 + (ch2 - 48);
            if (value < 0 || value > 59) {
                return false;
            }
            array[arrayIndex] = value;
            array[0] = pos;
            return true;
        }

        private void parseVariableWidthDigits(CharSequence parseText, int minDigits, int maxDigits, int[] array) {
            int pos = array[0];
            int available = 0;
            char[] chars = new char[maxDigits];
            for (int i = 0; i < maxDigits && pos + 1 <= parseText.length(); ++i) {
                char ch;
                if ((ch = parseText.charAt(pos++)) < '0' || ch > '9') {
                    --pos;
                    break;
                }
                chars[i] = ch;
                ++available;
            }
            if (available < minDigits) {
                array[0] = ~array[0];
                return;
            }
            switch (available) {
                case 1: {
                    array[1] = chars[0] - 48;
                    break;
                }
                case 2: {
                    array[1] = (chars[0] - 48) * 10 + (chars[1] - 48);
                    break;
                }
                case 3: {
                    array[1] = chars[0] - 48;
                    array[2] = (chars[1] - 48) * 10 + (chars[2] - 48);
                    break;
                }
                case 4: {
                    array[1] = (chars[0] - 48) * 10 + (chars[1] - 48);
                    array[2] = (chars[2] - 48) * 10 + (chars[3] - 48);
                    break;
                }
                case 5: {
                    array[1] = chars[0] - 48;
                    array[2] = (chars[1] - 48) * 10 + (chars[2] - 48);
                    array[3] = (chars[3] - 48) * 10 + (chars[4] - 48);
                    break;
                }
                case 6: {
                    array[1] = (chars[0] - 48) * 10 + (chars[1] - 48);
                    array[2] = (chars[2] - 48) * 10 + (chars[3] - 48);
                    array[3] = (chars[4] - 48) * 10 + (chars[5] - 48);
                }
            }
            array[0] = pos;
        }

        public String toString() {
            String converted = this.noOffsetText.replace("'", "''");
            return "Offset(" + PATTERNS[this.type] + ",'" + converted + "')";
        }
    }

    static final class LocalizedOffsetIdPrinterParser
    implements DateTimePrinterParser {
        private final TextStyle style;

        LocalizedOffsetIdPrinterParser(TextStyle style) {
            this.style = style;
        }

        private static StringBuilder appendHMS(StringBuilder buf, int t) {
            return buf.append((char)(t / 10 + 48)).append((char)(t % 10 + 48));
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long offsetSecs = context.getValue(ChronoField.OFFSET_SECONDS);
            if (offsetSecs == null) {
                return false;
            }
            String key = "timezone.gmtZeroFormat";
            String gmtText = (String)DateTimeTextProvider.getLocalizedResource(key, context.getLocale());
            if (gmtText == null) {
                gmtText = "GMT";
            }
            buf.append(gmtText);
            int totalSecs = Math.toIntExact(offsetSecs);
            if (totalSecs != 0) {
                int absHours = Math.abs(totalSecs / 3600 % 100);
                int absMinutes = Math.abs(totalSecs / 60 % 60);
                int absSeconds = Math.abs(totalSecs % 60);
                buf.append(totalSecs < 0 ? "-" : "+");
                if (this.style == TextStyle.FULL) {
                    LocalizedOffsetIdPrinterParser.appendHMS(buf, absHours);
                    buf.append(':');
                    LocalizedOffsetIdPrinterParser.appendHMS(buf, absMinutes);
                    if (absSeconds != 0) {
                        buf.append(':');
                        LocalizedOffsetIdPrinterParser.appendHMS(buf, absSeconds);
                    }
                } else {
                    if (absHours >= 10) {
                        buf.append((char)(absHours / 10 + 48));
                    }
                    buf.append((char)(absHours % 10 + 48));
                    if (absMinutes != 0 || absSeconds != 0) {
                        buf.append(':');
                        LocalizedOffsetIdPrinterParser.appendHMS(buf, absMinutes);
                        if (absSeconds != 0) {
                            buf.append(':');
                            LocalizedOffsetIdPrinterParser.appendHMS(buf, absSeconds);
                        }
                    }
                }
            }
            return true;
        }

        int getDigit(CharSequence text, int position) {
            char c = text.charAt(position);
            if (c < '0' || c > '9') {
                return -1;
            }
            return c - 48;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int pos = position;
            int end = text.length();
            String key = "timezone.gmtZeroFormat";
            String gmtText = (String)DateTimeTextProvider.getLocalizedResource(key, context.getLocale());
            if (gmtText == null) {
                gmtText = "GMT";
            }
            if (!context.subSequenceEquals(text, pos, gmtText, 0, gmtText.length())) {
                return ~position;
            }
            int negative = 0;
            if ((pos += gmtText.length()) == end) {
                return context.setParsedField(ChronoField.OFFSET_SECONDS, 0L, position, pos);
            }
            char sign = text.charAt(pos);
            if (sign == '+') {
                negative = 1;
            } else if (sign == '-') {
                negative = -1;
            } else {
                return context.setParsedField(ChronoField.OFFSET_SECONDS, 0L, position, pos);
            }
            ++pos;
            int h = 0;
            int m = 0;
            int s = 0;
            if (this.style == TextStyle.FULL) {
                int h1 = this.getDigit(text, pos++);
                int h2 = this.getDigit(text, pos++);
                if (h1 < 0 || h2 < 0 || text.charAt(pos++) != ':') {
                    return ~position;
                }
                h = h1 * 10 + h2;
                int m1 = this.getDigit(text, pos++);
                int m2 = this.getDigit(text, pos++);
                if (m1 < 0 || m2 < 0) {
                    return ~position;
                }
                m = m1 * 10 + m2;
                if (pos + 2 < end && text.charAt(pos) == ':') {
                    int s1 = this.getDigit(text, pos + 1);
                    int s2 = this.getDigit(text, pos + 2);
                    if (s1 >= 0 && s2 >= 0) {
                        s = s1 * 10 + s2;
                        pos += 3;
                    }
                }
            } else {
                if ((h = this.getDigit(text, pos++)) < 0) {
                    return ~position;
                }
                if (pos < end) {
                    int h2 = this.getDigit(text, pos);
                    if (h2 >= 0) {
                        h = h * 10 + h2;
                        ++pos;
                    }
                    if (pos + 2 < end && text.charAt(pos) == ':' && pos + 2 < end && text.charAt(pos) == ':') {
                        int m1 = this.getDigit(text, pos + 1);
                        int m2 = this.getDigit(text, pos + 2);
                        if (m1 >= 0 && m2 >= 0) {
                            m = m1 * 10 + m2;
                            if ((pos += 3) + 2 < end && text.charAt(pos) == ':') {
                                int s1 = this.getDigit(text, pos + 1);
                                int s2 = this.getDigit(text, pos + 2);
                                if (s1 >= 0 && s2 >= 0) {
                                    s = s1 * 10 + s2;
                                    pos += 3;
                                }
                            }
                        }
                    }
                }
            }
            long offsetSecs = (long)negative * ((long)h * 3600L + (long)m * 60L + (long)s);
            return context.setParsedField(ChronoField.OFFSET_SECONDS, offsetSecs, position, pos);
        }

        public String toString() {
            return "LocalizedOffset(" + (Object)((Object)this.style) + ")";
        }
    }

    static class ZoneIdPrinterParser
    implements DateTimePrinterParser {
        private final TemporalQuery<ZoneId> query;
        private final String description;
        private static volatile Map.Entry<Integer, PrefixTree> cachedPrefixTree;
        private static volatile Map.Entry<Integer, PrefixTree> cachedPrefixTreeCI;

        private ZoneIdPrinterParser(TemporalQuery<ZoneId> query, String description) {
            this.query = query;
            this.description = description;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            ZoneId zone = context.getValue(this.query);
            if (zone == null) {
                return false;
            }
            buf.append(zone.getId());
            return true;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        protected PrefixTree getTree(DateTimeParseContext context) {
            Map.Entry<Integer, PrefixTree> cached;
            Set<String> regionIds = ZoneRulesProvider.getAvailableZoneIds();
            int regionIdsSize = regionIds.size();
            Map.Entry<Integer, PrefixTree> entry = cached = context.isCaseSensitive() ? cachedPrefixTree : cachedPrefixTreeCI;
            if (cached == null || cached.getKey() != regionIdsSize) {
                ZoneIdPrinterParser zoneIdPrinterParser = this;
                synchronized (zoneIdPrinterParser) {
                    Map.Entry<Integer, PrefixTree> entry2 = cached = context.isCaseSensitive() ? cachedPrefixTree : cachedPrefixTreeCI;
                    if (cached == null || cached.getKey() != regionIdsSize) {
                        cached = new AbstractMap.SimpleImmutableEntry<Integer, PrefixTree>(regionIdsSize, PrefixTree.newTree(regionIds, context));
                        if (context.isCaseSensitive()) {
                            cachedPrefixTree = cached;
                        } else {
                            cachedPrefixTreeCI = cached;
                        }
                    }
                }
            }
            return cached.getValue();
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int length = text.length();
            if (position > length) {
                throw new IndexOutOfBoundsException();
            }
            if (position == length) {
                return ~position;
            }
            char nextChar = text.charAt(position);
            if (nextChar == '+' || nextChar == '-') {
                return this.parseOffsetBased(context, text, position, position, OffsetIdPrinterParser.INSTANCE_ID_Z);
            }
            if (length >= position + 2) {
                char nextNextChar = text.charAt(position + 1);
                if (context.charEquals(nextChar, 'U') && context.charEquals(nextNextChar, 'T')) {
                    if (length < position + 3 || !context.charEquals(text.charAt(position + 2), 'C')) return this.parseOffsetBased(context, text, position, position + 2, OffsetIdPrinterParser.INSTANCE_ID_ZERO);
                    if (length == position + 3 || context.charEquals(text.charAt(position + 3), '+') || context.charEquals(text.charAt(position + 3), '-')) {
                        return this.parseOffsetBased(context, text, position, position + 3, OffsetIdPrinterParser.INSTANCE_ID_ZERO);
                    }
                } else if (context.charEquals(nextChar, 'G') && length >= position + 3 && context.charEquals(nextNextChar, 'M') && context.charEquals(text.charAt(position + 2), 'T')) {
                    if (length < position + 4 || !context.charEquals(text.charAt(position + 3), '0')) return this.parseOffsetBased(context, text, position, position + 3, OffsetIdPrinterParser.INSTANCE_ID_ZERO);
                    context.setParsed(ZoneId.of("GMT0"));
                    return position + 4;
                }
            }
            PrefixTree tree = this.getTree(context);
            ParsePosition ppos = new ParsePosition(position);
            PrefixTree parsedZoneId = tree.match(text, ppos);
            if (parsedZoneId.value == null) {
                if (!context.charEquals(nextChar, 'Z')) return ~position;
                context.setParsed(ZoneOffset.UTC);
                return position + 1;
            }
            context.setParsed(ZoneId.of(parsedZoneId.value));
            context.setParsedZoneNameType(parsedZoneId.type);
            return ppos.getIndex();
        }

        private int parseOffsetBased(DateTimeParseContext context, CharSequence text, int prefixPos, int position, OffsetIdPrinterParser parser) {
            String prefix = text.subSequence(prefixPos, position).toString().toUpperCase();
            if (position >= text.length()) {
                context.setParsed(ZoneId.of(prefix));
                return position;
            }
            if (text.charAt(position) == '0' || context.charEquals(text.charAt(position), 'Z')) {
                context.setParsed(ZoneId.of(prefix));
                return position;
            }
            DateTimeParseContext newContext = context.copy();
            int endPos = parser.parse(newContext, text, position);
            try {
                if (endPos < 0) {
                    if (parser == OffsetIdPrinterParser.INSTANCE_ID_Z) {
                        return ~prefixPos;
                    }
                    context.setParsed(ZoneId.of(prefix));
                    return position;
                }
                int offset = (int)newContext.getParsed(ChronoField.OFFSET_SECONDS).longValue();
                ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(offset);
                context.setParsed(ZoneId.ofOffset(prefix, zoneOffset));
                return endPos;
            }
            catch (DateTimeException dte) {
                return ~prefixPos;
            }
        }

        public String toString() {
            return this.description;
        }
    }

    static final class ZoneTextPrinterParser
    extends ZoneIdPrinterParser {
        private final TextStyle textStyle;
        private Set<String> preferredZones;
        private final boolean isGeneric;
        static final int UNDEFINED = -1;
        static final int STD = 0;
        static final int DST = 1;
        static final int GENERIC = 2;
        private static final Map<String, SoftReference<Map<Locale, String[]>>> cache = new ConcurrentHashMap<String, SoftReference<Map<Locale, String[]>>>();
        private final Map<Locale, Map.Entry<Integer, SoftReference<PrefixTree>>> cachedTree = HashMap.newHashMap(1);
        private final Map<Locale, Map.Entry<Integer, SoftReference<PrefixTree>>> cachedTreeCI = HashMap.newHashMap(1);

        private ZoneTextPrinterParser(TextStyle textStyle, Set<ZoneId> preferredZones, boolean isGeneric) {
            super(TemporalQueries.zone(), "ZoneText(" + (Object)((Object)textStyle) + ")");
            this.textStyle = Objects.requireNonNull(textStyle, "textStyle");
            this.isGeneric = isGeneric;
            if (preferredZones != null && preferredZones.size() != 0) {
                this.preferredZones = new HashSet<String>();
                for (ZoneId id : preferredZones) {
                    this.preferredZones.add(id.getId());
                }
            }
        }

        private String getDisplayName(String id, int type, Locale locale) {
            String[] names;
            if (this.textStyle == TextStyle.NARROW) {
                return null;
            }
            SoftReference<Map<Locale, String[]>> ref = cache.get(id);
            Map<Locale, String[]> perLocale = null;
            if (ref == null || (perLocale = ref.get()) == null || (names = perLocale.get(locale)) == null) {
                names = TimeZoneNameUtility.retrieveDisplayNames(id, locale);
                if (names == null) {
                    return null;
                }
                names = Arrays.copyOfRange(names, 0, 7);
                names[5] = TimeZoneNameUtility.retrieveGenericDisplayName(id, 1, locale);
                if (names[5] == null) {
                    names[5] = names[0];
                }
                names[6] = TimeZoneNameUtility.retrieveGenericDisplayName(id, 0, locale);
                if (names[6] == null) {
                    names[6] = names[0];
                }
                if (perLocale == null) {
                    perLocale = new ConcurrentHashMap<Locale, String[]>();
                }
                perLocale.put(locale, names);
                cache.put(id, new SoftReference<Map<Locale, String[]>>(perLocale));
            }
            return switch (type) {
                case 0 -> names[this.textStyle.zoneNameStyleIndex() + 1];
                case 1 -> names[this.textStyle.zoneNameStyleIndex() + 3];
                default -> names[this.textStyle.zoneNameStyleIndex() + 5];
            };
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            ZoneId zone = context.getValue(TemporalQueries.zoneId());
            if (zone == null) {
                return false;
            }
            String zname = zone.getId();
            if (!(zone instanceof ZoneOffset)) {
                String name;
                TemporalAccessor dt = context.getTemporal();
                int type = 2;
                if (!this.isGeneric) {
                    if (dt.isSupported(ChronoField.INSTANT_SECONDS)) {
                        type = zone.getRules().isDaylightSavings(Instant.from(dt)) ? 1 : 0;
                    } else if (dt.isSupported(ChronoField.EPOCH_DAY) && dt.isSupported(ChronoField.NANO_OF_DAY)) {
                        LocalDate date = LocalDate.ofEpochDay(dt.getLong(ChronoField.EPOCH_DAY));
                        LocalTime time = LocalTime.ofNanoOfDay(dt.getLong(ChronoField.NANO_OF_DAY));
                        LocalDateTime ldt = date.atTime(time);
                        if (zone.getRules().getTransition(ldt) == null) {
                            int n = type = zone.getRules().isDaylightSavings(ldt.atZone(zone).toInstant()) ? 1 : 0;
                        }
                    }
                }
                if ((name = this.getDisplayName(zname, type, context.getLocale())) != null) {
                    zname = name;
                }
            }
            buf.append(zname);
            return true;
        }

        @Override
        protected PrefixTree getTree(DateTimeParseContext context) {
            if (this.textStyle == TextStyle.NARROW) {
                return super.getTree(context);
            }
            Locale locale = context.getLocale();
            boolean isCaseSensitive = context.isCaseSensitive();
            Set<String> availableZoneIds = ZoneRulesProvider.getAvailableZoneIds();
            int regionIdsSize = availableZoneIds.size();
            Map<Locale, Map.Entry<Integer, SoftReference<PrefixTree>>> cached = isCaseSensitive ? this.cachedTree : this.cachedTreeCI;
            Map.Entry<Integer, SoftReference<PrefixTree>> entry = null;
            PrefixTree tree = null;
            String[][] zoneStrings = null;
            entry = cached.get(locale);
            if (entry == null || entry.getKey() != regionIdsSize || (tree = entry.getValue().get()) == null) {
                tree = PrefixTree.newTree(context);
                zoneStrings = TimeZoneNameUtility.getZoneStrings(locale);
                HashSet nonRegionIds = HashSet.newHashSet(64);
                HashSet<String> regionIds = new HashSet<String>(availableZoneIds);
                for (String[] names : zoneStrings) {
                    int i;
                    String zid2 = names[0];
                    if (!regionIds.remove(zid2)) {
                        nonRegionIds.add(zid2);
                        continue;
                    }
                    tree.add(zid2, zid2, -1);
                    zid2 = ZoneName.toZid(zid2, locale);
                    int n = i = this.textStyle == TextStyle.FULL ? 1 : 2;
                    while (i < names.length) {
                        tree.add(names[i], zid2, (i - 1) / 2);
                        i += 2;
                    }
                }
                PrefixTree t = tree;
                regionIds.stream().filter(zid -> !zid.startsWith("Etc") && !zid.startsWith("GMT")).forEach(cid -> {
                    int i;
                    String[] cidNames = TimeZoneNameUtility.retrieveDisplayNames(cid, locale);
                    int n = i = this.textStyle == TextStyle.FULL ? 1 : 2;
                    while (i < cidNames.length) {
                        if (cidNames[i] != null && !cidNames[i].isEmpty()) {
                            t.add(cidNames[i], (String)cid, (i - 1) / 2);
                        }
                        i += 2;
                    }
                });
                if (this.preferredZones != null) {
                    for (String[] names : zoneStrings) {
                        int i;
                        String zid3 = names[0];
                        if (!this.preferredZones.contains(zid3) || nonRegionIds.contains(zid3)) continue;
                        int n = i = this.textStyle == TextStyle.FULL ? 1 : 2;
                        while (i < names.length) {
                            tree.add(names[i], zid3, (i - 1) / 2);
                            i += 2;
                        }
                    }
                }
                cached.put(locale, new AbstractMap.SimpleImmutableEntry<Integer, SoftReference<PrefixTree>>(regionIdsSize, new SoftReference<PrefixTree>(tree)));
            }
            return tree;
        }
    }

    static final class ChronoPrinterParser
    implements DateTimePrinterParser {
        private final TextStyle textStyle;

        private ChronoPrinterParser(TextStyle textStyle) {
            this.textStyle = textStyle;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Chronology chrono = context.getValue(TemporalQueries.chronology());
            if (chrono == null) {
                return false;
            }
            if (this.textStyle == null) {
                buf.append(chrono.getId());
            } else {
                buf.append(this.getChronologyName(chrono, context.getLocale()));
            }
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            if (position < 0 || position > text.length()) {
                throw new IndexOutOfBoundsException();
            }
            Set<Chronology> chronos = Chronology.getAvailableChronologies();
            Chronology bestMatch = null;
            int matchLen = -1;
            for (Chronology chrono : chronos) {
                String name = this.textStyle == null ? chrono.getId() : this.getChronologyName(chrono, context.getLocale());
                int nameLen = name.length();
                if (nameLen <= matchLen || !context.subSequenceEquals(text, position, name, 0, nameLen)) continue;
                bestMatch = chrono;
                matchLen = nameLen;
            }
            if (bestMatch == null) {
                return ~position;
            }
            context.setParsed(bestMatch);
            return position + matchLen;
        }

        private String getChronologyName(Chronology chrono, Locale locale) {
            String key = "calendarname." + chrono.getCalendarType();
            String name = (String)DateTimeTextProvider.getLocalizedResource(key, locale);
            return Objects.requireNonNullElseGet(name, () -> chrono.getId());
        }
    }

    static final class LocalizedPrinterParser
    implements DateTimePrinterParser {
        private static final ConcurrentMap<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<String, DateTimeFormatter>(16, 0.75f, 2);
        private final FormatStyle dateStyle;
        private final FormatStyle timeStyle;
        private final String requestedTemplate;

        private LocalizedPrinterParser(FormatStyle dateStyle, FormatStyle timeStyle) {
            this.dateStyle = dateStyle;
            this.timeStyle = timeStyle;
            this.requestedTemplate = null;
        }

        private LocalizedPrinterParser(String requestedTemplate) {
            this.dateStyle = null;
            this.timeStyle = null;
            this.requestedTemplate = requestedTemplate;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Chronology chrono = Chronology.from(context.getTemporal());
            return this.formatter(context.getLocale(), chrono).toPrinterParser(false).format(context, buf);
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            Chronology chrono = context.getEffectiveChronology();
            return this.formatter(context.getLocale(), chrono).toPrinterParser(false).parse(context, text, position);
        }

        private DateTimeFormatter formatter(Locale locale, Chronology chrono) {
            String key = chrono.getId() + '|' + locale.toString() + '|' + (this.requestedTemplate != null ? this.requestedTemplate : Objects.toString((Object)this.dateStyle) + (Object)((Object)this.timeStyle));
            return FORMATTER_CACHE.computeIfAbsent(key, k -> new DateTimeFormatterBuilder().appendPattern(this.requestedTemplate != null ? DateTimeFormatterBuilder.getLocalizedDateTimePattern(this.requestedTemplate, chrono, locale) : DateTimeFormatterBuilder.getLocalizedDateTimePattern(this.dateStyle, this.timeStyle, chrono, locale)).toFormatter(locale));
        }

        public String toString() {
            return "Localized(" + (this.requestedTemplate != null ? this.requestedTemplate : (this.dateStyle != null ? this.dateStyle : "") + "," + (this.timeStyle != null ? this.timeStyle : "")) + ")";
        }
    }

    static final class CharLiteralPrinterParser
    implements DateTimePrinterParser {
        private final char literal;

        private CharLiteralPrinterParser(char literal) {
            this.literal = literal;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            buf.append(this.literal);
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int length = text.length();
            if (position == length) {
                return ~position;
            }
            char ch = text.charAt(position);
            if (ch != this.literal && (context.isCaseSensitive() || Character.toUpperCase(ch) != Character.toUpperCase(this.literal) && Character.toLowerCase(ch) != Character.toLowerCase(this.literal))) {
                return ~position;
            }
            return position + 1;
        }

        public String toString() {
            if (this.literal == '\'') {
                return "''";
            }
            return "'" + this.literal + "'";
        }
    }

    static final class StringLiteralPrinterParser
    implements DateTimePrinterParser {
        private final String literal;

        private StringLiteralPrinterParser(String literal) {
            this.literal = literal;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            buf.append(this.literal);
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int length = text.length();
            if (position > length || position < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (!context.subSequenceEquals(text, position, this.literal, 0, this.literal.length())) {
                return ~position;
            }
            return position + this.literal.length();
        }

        public String toString() {
            String converted = this.literal.replace("'", "''");
            return "'" + converted + "'";
        }
    }

    static final class DayPeriodPrinterParser
    implements DateTimePrinterParser {
        private final TextStyle textStyle;
        private static final ConcurrentMap<Locale, DateTimeTextProvider.LocaleStore> DAYPERIOD_LOCALESTORE = new ConcurrentHashMap<Locale, DateTimeTextProvider.LocaleStore>();

        private DayPeriodPrinterParser(TextStyle textStyle) {
            this.textStyle = textStyle;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            Long hod = context.getValue(ChronoField.HOUR_OF_DAY);
            if (hod == null) {
                return false;
            }
            Long moh = context.getValue(ChronoField.MINUTE_OF_HOUR);
            long value = Math.floorMod((long)hod, 24) * 60 + (moh != null ? Math.floorMod((long)moh, 60) : 0);
            Locale locale = context.getLocale();
            DateTimeTextProvider.LocaleStore store = DayPeriodPrinterParser.findDayPeriodStore(locale);
            long val = value;
            Map<DayPeriod, Long> map = DayPeriod.getDayPeriodMap(locale);
            value = map.keySet().stream().filter(k -> k.includes(val)).min(DayPeriod.DPCOMPARATOR).map(map::get).orElse(val / 720L);
            String text = store.getText(value, this.textStyle);
            buf.append(text);
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence parseText, int position) {
            int length = parseText.length();
            if (position < 0 || position > length) {
                throw new IndexOutOfBoundsException();
            }
            TextStyle style = context.isStrict() ? this.textStyle : null;
            DateTimeTextProvider.LocaleStore store = DayPeriodPrinterParser.findDayPeriodStore(context.getLocale());
            Iterator<Map.Entry<String, Long>> it = store.getTextIterator(style);
            if (it != null) {
                while (it.hasNext()) {
                    Map.Entry<String, Long> entry = it.next();
                    String itText = entry.getKey();
                    if (!context.subSequenceEquals(itText, 0, parseText, position, itText.length())) continue;
                    context.setParsedDayPeriod(DayPeriod.ofLocale(context.getLocale(), entry.getValue()));
                    return position + itText.length();
                }
            }
            return ~position;
        }

        public String toString() {
            return "DayPeriod(" + (Object)((Object)this.textStyle) + ")";
        }

        private static DateTimeTextProvider.LocaleStore findDayPeriodStore(Locale locale) {
            return DAYPERIOD_LOCALESTORE.computeIfAbsent(locale, loc -> {
                HashMap<TextStyle, Map<Long, String>> styleMap = new HashMap<TextStyle, Map<Long, String>>();
                for (TextStyle textStyle : TextStyle.values()) {
                    if (textStyle.isStandalone()) continue;
                    HashMap map = new HashMap();
                    int calStyle = textStyle.toCalendarStyle();
                    Map<DayPeriod, Long> periodMap = DayPeriod.getDayPeriodMap(loc);
                    periodMap.forEach((key, value) -> {
                        String displayName = CalendarDataUtility.retrieveJavaTimeFieldValueName("gregory", 9, value.intValue(), calStyle, loc);
                        if (displayName != null) {
                            map.put(value, displayName);
                        } else {
                            periodMap.remove(key);
                        }
                    });
                    if (map.isEmpty()) continue;
                    styleMap.put(textStyle, map);
                }
                return new DateTimeTextProvider.LocaleStore(styleMap);
            });
        }
    }

    static final class CompositePrinterParser
    implements DateTimePrinterParser {
        private final DateTimePrinterParser[] printerParsers;
        private final boolean optional;

        private CompositePrinterParser(List<DateTimePrinterParser> printerParsers, boolean optional) {
            this(printerParsers.toArray(new DateTimePrinterParser[0]), optional);
        }

        private CompositePrinterParser(DateTimePrinterParser[] printerParsers, boolean optional) {
            this.printerParsers = printerParsers;
            this.optional = optional;
        }

        public CompositePrinterParser withOptional(boolean optional) {
            if (optional == this.optional) {
                return this;
            }
            return new CompositePrinterParser(this.printerParsers, optional);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            int length = buf.length();
            if (this.optional) {
                context.startOptional();
            }
            try {
                for (DateTimePrinterParser pp : this.printerParsers) {
                    if (pp.format(context, buf)) continue;
                    buf.setLength(length);
                    boolean bl = true;
                    return bl;
                }
            }
            finally {
                if (this.optional) {
                    context.endOptional();
                }
            }
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            DateTimePrinterParser pp;
            if (this.optional) {
                context.startOptional();
                int pos = position;
                for (DateTimePrinterParser pp2 : this.printerParsers) {
                    if ((pos = pp2.parse(context, text, pos)) >= 0) continue;
                    context.endOptional(false);
                    return position;
                }
                context.endOptional(true);
                return pos;
            }
            DateTimePrinterParser[] dateTimePrinterParserArray = this.printerParsers;
            int n = dateTimePrinterParserArray.length;
            for (int i = 0; i < n && (position = (pp = dateTimePrinterParserArray[i]).parse(context, text, position)) >= 0; ++i) {
            }
            return position;
        }

        public String toString() {
            StringBuilder buf = new StringBuilder();
            if (this.printerParsers != null) {
                buf.append(this.optional ? "[" : "(");
                for (DateTimePrinterParser pp : this.printerParsers) {
                    buf.append(pp);
                }
                buf.append(this.optional ? "]" : ")");
            }
            return buf.toString();
        }
    }

    static final class WeekBasedFieldPrinterParser
    extends NumberPrinterParser {
        private char chr;
        private int count;

        private WeekBasedFieldPrinterParser(char chr, int count, int minWidth, int maxWidth) {
            this(chr, count, minWidth, maxWidth, 0);
        }

        private WeekBasedFieldPrinterParser(char chr, int count, int minWidth, int maxWidth, int subsequentWidth) {
            super(null, minWidth, maxWidth, SignStyle.NOT_NEGATIVE, subsequentWidth);
            this.chr = chr;
            this.count = count;
        }

        @Override
        WeekBasedFieldPrinterParser withFixedWidth() {
            if (this.subsequentWidth == -1) {
                return this;
            }
            return new WeekBasedFieldPrinterParser(this.chr, this.count, this.minWidth, this.maxWidth, -1);
        }

        @Override
        WeekBasedFieldPrinterParser withSubsequentWidth(int subsequentWidth) {
            return new WeekBasedFieldPrinterParser(this.chr, this.count, this.minWidth, this.maxWidth, this.subsequentWidth + subsequentWidth);
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            return this.printerParser(context.getLocale()).format(context, buf);
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            return this.printerParser(context.getLocale()).parse(context, text, position);
        }

        private DateTimePrinterParser printerParser(Locale locale) {
            WeekFields weekDef = WeekFields.of(locale);
            TemporalField field = null;
            switch (this.chr) {
                case 'Y': {
                    field = weekDef.weekBasedYear();
                    if (this.count == 2) {
                        return new ReducedPrinterParser(field, 2, 2, 0, ReducedPrinterParser.BASE_DATE, this.subsequentWidth);
                    }
                    return new NumberPrinterParser(field, this.count, 19, this.count < 4 ? SignStyle.NORMAL : SignStyle.EXCEEDS_PAD, this.subsequentWidth);
                }
                case 'c': 
                case 'e': {
                    field = weekDef.dayOfWeek();
                    break;
                }
                case 'w': {
                    field = weekDef.weekOfWeekBasedYear();
                    break;
                }
                case 'W': {
                    field = weekDef.weekOfMonth();
                    break;
                }
                default: {
                    throw new IllegalStateException("unreachable");
                }
            }
            return new NumberPrinterParser(field, this.minWidth, this.maxWidth, SignStyle.NOT_NEGATIVE, this.subsequentWidth);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(30);
            sb.append("Localized(");
            if (this.chr == 'Y') {
                if (this.count == 1) {
                    sb.append("WeekBasedYear");
                } else if (this.count == 2) {
                    sb.append("ReducedValue(WeekBasedYear,2,2,2000-01-01)");
                } else {
                    sb.append("WeekBasedYear,").append(this.count).append(",").append(19).append(",").append((Object)(this.count < 4 ? SignStyle.NORMAL : SignStyle.EXCEEDS_PAD));
                }
            } else {
                switch (this.chr) {
                    case 'c': 
                    case 'e': {
                        sb.append("DayOfWeek");
                        break;
                    }
                    case 'w': {
                        sb.append("WeekOfWeekBasedYear");
                        break;
                    }
                    case 'W': {
                        sb.append("WeekOfMonth");
                        break;
                    }
                }
                sb.append(",");
                sb.append(this.count);
            }
            sb.append(")");
            return sb.toString();
        }
    }

    static final class PadPrinterParserDecorator
    implements DateTimePrinterParser {
        private final DateTimePrinterParser printerParser;
        private final int padWidth;
        private final char padChar;

        private PadPrinterParserDecorator(DateTimePrinterParser printerParser, int padWidth, char padChar) {
            this.printerParser = printerParser;
            this.padWidth = padWidth;
            this.padChar = padChar;
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            int preLen = buf.length();
            if (!this.printerParser.format(context, buf)) {
                return false;
            }
            int len = buf.length() - preLen;
            if (len > this.padWidth) {
                throw new DateTimeException("Cannot print as output of " + len + " characters exceeds pad width of " + this.padWidth);
            }
            int count = this.padWidth - len;
            if (count == 0) {
                return true;
            }
            if (count == 1) {
                buf.insert(preLen, this.padChar);
                return true;
            }
            buf.insert(preLen, String.valueOf(this.padChar).repeat(count));
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            int pos;
            boolean strict = context.isStrict();
            if (position > text.length()) {
                throw new IndexOutOfBoundsException();
            }
            if (position == text.length()) {
                return ~position;
            }
            int endPos = position + this.padWidth;
            if (endPos > text.length()) {
                if (strict) {
                    return ~position;
                }
                endPos = text.length();
            }
            for (pos = position; pos < endPos && context.charEquals(text.charAt(pos), this.padChar); ++pos) {
            }
            int resultPos = this.printerParser.parse(context, text = text.subSequence(0, endPos), pos);
            if (resultPos != endPos && strict) {
                return ~(position + pos);
            }
            return resultPos;
        }

        public String toString() {
            return "Pad(" + this.printerParser + "," + this.padWidth + (this.padChar == ' ' ? ")" : ",'" + this.padChar + "')");
        }
    }

    static final class DayPeriod {
        private static final Map<Locale, Map<DayPeriod, Long>> DAYPERIOD_CACHE = new ConcurrentHashMap<Locale, Map<DayPeriod, Long>>();
        private static final Comparator<DayPeriod> DPCOMPARATOR = (dp1, dp2) -> (int)(dp1.duration() - dp2.duration());
        private static final Pattern RULE = Pattern.compile("(?<type>[a-z12]+):(?<from>\\d{2}):00(-(?<to>\\d{2}))*");
        private final long from;
        private final long to;
        private final long index;

        private DayPeriod(long from, long to, long index) {
            this.from = from;
            this.to = to;
            this.index = index;
        }

        long getIndex() {
            return this.index;
        }

        long mid() {
            return (this.from + this.duration() / 2L) % 1440L;
        }

        boolean includes(long mod) {
            if (this.from == 0L && this.to == 0L && mod == 1440L) {
                return true;
            }
            return this.from == mod && this.to == mod || this.from <= mod && mod < this.to || this.from > this.to && (this.from <= mod || this.to > mod);
        }

        private long duration() {
            return this.from > this.to ? 1440L - this.from + this.to : this.to - this.from;
        }

        static long mapToIndex(String type) {
            return switch (type) {
                case "am" -> 0L;
                case "pm" -> 1L;
                case "midnight" -> 2L;
                case "noon" -> 3L;
                case "morning1" -> 4L;
                case "morning2" -> 5L;
                case "afternoon1" -> 6L;
                case "afternoon2" -> 7L;
                case "evening1" -> 8L;
                case "evening2" -> 9L;
                case "night1" -> 10L;
                case "night2" -> 11L;
                default -> throw new InternalError("invalid day period type");
            };
        }

        static Map<DayPeriod, Long> getDayPeriodMap(Locale locale) {
            return DAYPERIOD_CACHE.computeIfAbsent(locale, l -> {
                LocaleResources lr = LocaleProviderAdapter.getResourceBundleBased().getLocaleResources(CalendarDataUtility.findRegionOverride(l));
                String dayPeriodRules = lr.getRules()[1];
                ConcurrentHashMap<DayPeriod, Long> periodMap = new ConcurrentHashMap<DayPeriod, Long>();
                Arrays.stream(dayPeriodRules.split(";")).forEach(rule -> {
                    Matcher m = RULE.matcher((CharSequence)rule);
                    if (m.find()) {
                        String from = m.group("from");
                        String to = m.group("to");
                        long index = DayPeriod.mapToIndex(m.group("type"));
                        if (to == null) {
                            to = from;
                        }
                        periodMap.putIfAbsent(new DayPeriod(Long.parseLong(from) * 60L, Long.parseLong(to) * 60L, index), index);
                    }
                });
                periodMap.putIfAbsent(new DayPeriod(0L, 720L, 0L), 0L);
                periodMap.putIfAbsent(new DayPeriod(720L, 1440L, 1L), 1L);
                return periodMap;
            });
        }

        static DayPeriod ofLocale(Locale locale, long index) {
            return DayPeriod.getDayPeriodMap(locale).keySet().stream().filter(dp -> dp.getIndex() == index).findAny().orElseThrow(() -> new DateTimeException("DayPeriod could not be determined for the locale " + locale + " at type index " + index));
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            DayPeriod dayPeriod = (DayPeriod)o;
            return this.from == dayPeriod.from && this.to == dayPeriod.to && this.index == dayPeriod.index;
        }

        public int hashCode() {
            return Objects.hash(this.from, this.to, this.index);
        }

        public String toString() {
            return "DayPeriod(%02d:%02d".formatted(this.from / 60L, this.from % 60L) + (this.from == this.to ? ")" : "-%02d:%02d)".formatted(this.to / 60L, this.to % 60L));
        }
    }

    static class PrefixTree {
        protected String key;
        protected String value;
        protected int type;
        protected char c0;
        protected PrefixTree child;
        protected PrefixTree sibling;

        private PrefixTree(String k, String v, int type, PrefixTree child) {
            this.key = k;
            this.value = v;
            this.type = type;
            this.child = child;
            this.c0 = k.isEmpty() ? (char)65535 : this.key.charAt(0);
        }

        public static PrefixTree newTree(DateTimeParseContext context) {
            if (context.isCaseSensitive()) {
                return new PrefixTree("", null, -1, null);
            }
            return new CI("", null, -1, null);
        }

        public static PrefixTree newTree(Set<String> keys, DateTimeParseContext context) {
            PrefixTree tree = PrefixTree.newTree(context);
            for (String k : keys) {
                tree.add0(k, k, -1);
            }
            return tree;
        }

        public PrefixTree copyTree() {
            PrefixTree copy = new PrefixTree(this.key, this.value, this.type, null);
            if (this.child != null) {
                copy.child = this.child.copyTree();
            }
            if (this.sibling != null) {
                copy.sibling = this.sibling.copyTree();
            }
            return copy;
        }

        public boolean add(String k, String v, int t) {
            return this.add0(k, v, t);
        }

        private boolean add0(String k, String v, int t) {
            int prefixLen = this.prefixLength(k = this.toKey(k));
            if (prefixLen == this.key.length()) {
                if (prefixLen < k.length()) {
                    String subKey = k.substring(prefixLen);
                    PrefixTree c = this.child;
                    while (c != null) {
                        if (this.isEqual(c.c0, subKey.charAt(0))) {
                            return c.add0(subKey, v, t);
                        }
                        c = c.sibling;
                    }
                    c = this.newNode(subKey, v, t, null);
                    c.sibling = this.child;
                    this.child = c;
                    return true;
                }
                this.value = v;
                this.type = t;
                return true;
            }
            PrefixTree n1 = this.newNode(this.key.substring(prefixLen), this.value, this.type, this.child);
            this.key = k.substring(0, prefixLen);
            this.child = n1;
            if (prefixLen < k.length()) {
                PrefixTree n2;
                this.child.sibling = n2 = this.newNode(k.substring(prefixLen), v, t, null);
                this.value = null;
            } else {
                this.value = v;
                this.type = t;
            }
            return true;
        }

        public PrefixTree match(CharSequence text, int off, int end) {
            if (!this.prefixOf(text, off, end)) {
                return null;
            }
            if (this.child != null && (off += this.key.length()) != end) {
                PrefixTree c = this.child;
                do {
                    if (!this.isEqual(c.c0, text.charAt(off))) continue;
                    PrefixTree found = c.match(text, off, end);
                    if (found != null) {
                        return found;
                    }
                    return this;
                } while ((c = c.sibling) != null);
            }
            return this;
        }

        public PrefixTree match(CharSequence text, ParsePosition pos) {
            int end;
            int off = pos.getIndex();
            if (!this.prefixOf(text, off, end = text.length())) {
                return null;
            }
            if (this.child != null && (off += this.key.length()) != end) {
                PrefixTree c = this.child;
                do {
                    if (!this.isEqual(c.c0, text.charAt(off))) continue;
                    pos.setIndex(off);
                    PrefixTree found = c.match(text, pos);
                    if (found == null) break;
                    return found;
                } while ((c = c.sibling) != null);
            }
            pos.setIndex(off);
            return this;
        }

        protected String toKey(String k) {
            return k;
        }

        protected PrefixTree newNode(String k, String v, int t, PrefixTree child) {
            return new PrefixTree(k, v, t, child);
        }

        protected boolean isEqual(char c1, char c2) {
            return c1 == c2;
        }

        protected boolean prefixOf(CharSequence text, int off, int end) {
            if (text instanceof String) {
                return ((String)text).startsWith(this.key, off);
            }
            int len = this.key.length();
            if (len > end - off) {
                return false;
            }
            int off0 = 0;
            while (len-- > 0) {
                if (this.isEqual(this.key.charAt(off0++), text.charAt(off++))) continue;
                return false;
            }
            return true;
        }

        private int prefixLength(String k) {
            int off;
            for (off = 0; off < k.length() && off < this.key.length(); ++off) {
                if (this.isEqual(k.charAt(off), this.key.charAt(off))) continue;
                return off;
            }
            return off;
        }

        private static class CI
        extends PrefixTree {
            private CI(String k, String v, int t, PrefixTree child) {
                super(k, v, t, child);
            }

            @Override
            protected CI newNode(String k, String v, int t, PrefixTree child) {
                return new CI(k, v, t, child);
            }

            @Override
            protected boolean isEqual(char c1, char c2) {
                return DateTimeParseContext.charEqualsIgnoreCase(c1, c2);
            }

            @Override
            protected boolean prefixOf(CharSequence text, int off, int end) {
                int len = this.key.length();
                if (len > end - off) {
                    return false;
                }
                int off0 = 0;
                while (len-- > 0) {
                    if (this.isEqual(this.key.charAt(off0++), text.charAt(off++))) continue;
                    return false;
                }
                return true;
            }
        }
    }
}

