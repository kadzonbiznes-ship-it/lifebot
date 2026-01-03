/*
 * Decompiled with CFR 0.152.
 */
package java.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageFormat
extends Format {
    private static final long serialVersionUID = 6479157306784022952L;
    private Locale locale;
    private String pattern = "";
    private static final int INITIAL_FORMATS = 10;
    private Format[] formats = new Format[10];
    private int[] offsets = new int[10];
    private int[] argumentNumbers = new int[10];
    private static final int MAX_ARGUMENT_INDEX = 10000;
    private int maxOffset = -1;
    private static final int SEG_RAW = 0;
    private static final int SEG_INDEX = 1;
    private static final int SEG_TYPE = 2;
    private static final int SEG_MODIFIER = 3;
    private static final int TYPE_NULL = 0;
    private static final int TYPE_NUMBER = 1;
    private static final int TYPE_DATE = 2;
    private static final int TYPE_TIME = 3;
    private static final int TYPE_CHOICE = 4;
    private static final String[] TYPE_KEYWORDS = new String[]{"", "number", "date", "time", "choice"};
    private static final int MODIFIER_DEFAULT = 0;
    private static final int MODIFIER_CURRENCY = 1;
    private static final int MODIFIER_PERCENT = 2;
    private static final int MODIFIER_INTEGER = 3;
    private static final String[] NUMBER_MODIFIER_KEYWORDS = new String[]{"", "currency", "percent", "integer"};
    private static final int MODIFIER_SHORT = 1;
    private static final int MODIFIER_MEDIUM = 2;
    private static final int MODIFIER_LONG = 3;
    private static final int MODIFIER_FULL = 4;
    private static final String[] DATE_TIME_MODIFIER_KEYWORDS = new String[]{"", "short", "medium", "long", "full"};
    private static final int[] DATE_TIME_MODIFIERS = new int[]{2, 3, 2, 1, 0};

    public MessageFormat(String pattern) {
        this.locale = Locale.getDefault(Locale.Category.FORMAT);
        this.applyPattern(pattern);
    }

    public MessageFormat(String pattern, Locale locale) {
        this.locale = locale;
        this.applyPattern(pattern);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void applyPattern(String pattern) {
        StringBuilder[] segments = new StringBuilder[4];
        segments[0] = new StringBuilder();
        int part = 0;
        int formatNumber = 0;
        boolean inQuote = false;
        int braceStack = 0;
        this.maxOffset = -1;
        block7: for (int i = 0; i < pattern.length(); ++i) {
            char ch = pattern.charAt(i);
            if (part == 0) {
                if (ch == '\'') {
                    if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '\'') {
                        segments[part].append(ch);
                        ++i;
                        continue;
                    }
                    inQuote = !inQuote;
                    continue;
                }
                if (ch == '{' && !inQuote) {
                    part = 1;
                    if (segments[1] != null) continue;
                    segments[1] = new StringBuilder();
                    continue;
                }
                segments[part].append(ch);
                continue;
            }
            if (inQuote) {
                segments[part].append(ch);
                if (ch != '\'') continue;
                inQuote = false;
                continue;
            }
            switch (ch) {
                case ',': {
                    if (part < 3) {
                        if (segments[++part] != null) continue block7;
                        segments[part] = new StringBuilder();
                        continue block7;
                    }
                    segments[part].append(ch);
                    continue block7;
                }
                case '{': {
                    ++braceStack;
                    segments[part].append(ch);
                    continue block7;
                }
                case '}': {
                    if (braceStack == 0) {
                        part = 0;
                        this.makeFormat(i, formatNumber, segments);
                        ++formatNumber;
                        segments[1] = null;
                        segments[2] = null;
                        segments[3] = null;
                        continue block7;
                    }
                    --braceStack;
                    segments[part].append(ch);
                    continue block7;
                }
                case ' ': {
                    if (part == 2 && segments[2].length() <= 0) continue block7;
                    segments[part].append(ch);
                    continue block7;
                }
                case '\'': {
                    inQuote = true;
                }
                default: {
                    segments[part].append(ch);
                }
            }
        }
        if (braceStack == 0 && part != 0) {
            this.maxOffset = -1;
            throw new IllegalArgumentException("Unmatched braces in the pattern.");
        }
        this.pattern = segments[0].toString();
    }

    public String toPattern() {
        int lastOffset = 0;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i <= this.maxOffset; ++i) {
            MessageFormat.copyAndFixQuotes(this.pattern, lastOffset, this.offsets[i], result);
            lastOffset = this.offsets[i];
            result.append('{').append(this.argumentNumbers[i]);
            Format fmt = this.formats[i];
            if (fmt != null) {
                if (fmt instanceof NumberFormat) {
                    if (fmt.equals(NumberFormat.getInstance(this.locale))) {
                        result.append(",number");
                    } else if (fmt.equals(NumberFormat.getCurrencyInstance(this.locale))) {
                        result.append(",number,currency");
                    } else if (fmt.equals(NumberFormat.getPercentInstance(this.locale))) {
                        result.append(",number,percent");
                    } else if (fmt.equals(NumberFormat.getIntegerInstance(this.locale))) {
                        result.append(",number,integer");
                    } else if (fmt instanceof DecimalFormat) {
                        result.append(",number,").append(((DecimalFormat)fmt).toPattern());
                    } else if (fmt instanceof ChoiceFormat) {
                        result.append(",choice,").append(((ChoiceFormat)fmt).toPattern());
                    }
                } else if (fmt instanceof DateFormat) {
                    int index;
                    for (index = 0; index < DATE_TIME_MODIFIERS.length; ++index) {
                        DateFormat df = DateFormat.getDateInstance(DATE_TIME_MODIFIERS[index], this.locale);
                        if (fmt.equals(df)) {
                            result.append(",date");
                            break;
                        }
                        df = DateFormat.getTimeInstance(DATE_TIME_MODIFIERS[index], this.locale);
                        if (!fmt.equals(df)) continue;
                        result.append(",time");
                        break;
                    }
                    if (index >= DATE_TIME_MODIFIERS.length) {
                        if (fmt instanceof SimpleDateFormat) {
                            result.append(",date,").append(((SimpleDateFormat)fmt).toPattern());
                        }
                    } else if (index != 0) {
                        result.append(',').append(DATE_TIME_MODIFIER_KEYWORDS[index]);
                    }
                }
            }
            result.append('}');
        }
        MessageFormat.copyAndFixQuotes(this.pattern, lastOffset, this.pattern.length(), result);
        return result.toString();
    }

    public void setFormatsByArgumentIndex(Format[] newFormats) {
        for (int i = 0; i <= this.maxOffset; ++i) {
            int j = this.argumentNumbers[i];
            if (j >= newFormats.length) continue;
            this.formats[i] = newFormats[j];
        }
    }

    public void setFormats(Format[] newFormats) {
        int runsToCopy = newFormats.length;
        if (runsToCopy > this.maxOffset + 1) {
            runsToCopy = this.maxOffset + 1;
        }
        for (int i = 0; i < runsToCopy; ++i) {
            this.formats[i] = newFormats[i];
        }
    }

    public void setFormatByArgumentIndex(int argumentIndex, Format newFormat) {
        for (int j = 0; j <= this.maxOffset; ++j) {
            if (this.argumentNumbers[j] != argumentIndex) continue;
            this.formats[j] = newFormat;
        }
    }

    public void setFormat(int formatElementIndex, Format newFormat) {
        if (formatElementIndex > this.maxOffset) {
            throw new ArrayIndexOutOfBoundsException(formatElementIndex);
        }
        this.formats[formatElementIndex] = newFormat;
    }

    public Format[] getFormatsByArgumentIndex() {
        int maximumArgumentNumber = -1;
        for (int i = 0; i <= this.maxOffset; ++i) {
            if (this.argumentNumbers[i] <= maximumArgumentNumber) continue;
            maximumArgumentNumber = this.argumentNumbers[i];
        }
        Format[] resultArray = new Format[maximumArgumentNumber + 1];
        for (int i = 0; i <= this.maxOffset; ++i) {
            resultArray[this.argumentNumbers[i]] = this.formats[i];
        }
        return resultArray;
    }

    public Format[] getFormats() {
        Format[] resultArray = new Format[this.maxOffset + 1];
        System.arraycopy(this.formats, 0, resultArray, 0, this.maxOffset + 1);
        return resultArray;
    }

    public final StringBuffer format(Object[] arguments, StringBuffer result, FieldPosition pos) {
        return this.subformat(arguments, result, pos, null);
    }

    public static String format(String pattern, Object ... arguments) {
        MessageFormat temp = new MessageFormat(pattern);
        return temp.format(arguments);
    }

    @Override
    public final StringBuffer format(Object arguments, StringBuffer result, FieldPosition pos) {
        return this.subformat((Object[])arguments, result, pos, null);
    }

    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object arguments) {
        StringBuffer result = new StringBuffer();
        ArrayList<AttributedCharacterIterator> iterators = new ArrayList<AttributedCharacterIterator>();
        if (arguments == null) {
            throw new NullPointerException("formatToCharacterIterator must be passed non-null object");
        }
        this.subformat((Object[])arguments, result, null, iterators);
        if (iterators.size() == 0) {
            return this.createAttributedCharacterIterator("");
        }
        return this.createAttributedCharacterIterator(iterators.toArray(new AttributedCharacterIterator[iterators.size()]));
    }

    public Object[] parse(String source, ParsePosition pos) {
        if (source == null) {
            Object[] empty = new Object[]{};
            return empty;
        }
        int maximumArgumentNumber = -1;
        for (int i = 0; i <= this.maxOffset; ++i) {
            if (this.argumentNumbers[i] <= maximumArgumentNumber) continue;
            maximumArgumentNumber = this.argumentNumbers[i];
        }
        Object[] resultArray = new Object[maximumArgumentNumber + 1];
        int patternOffset = 0;
        int sourceOffset = pos.index;
        ParsePosition tempStatus = new ParsePosition(0);
        for (int i = 0; i <= this.maxOffset; ++i) {
            int len = this.offsets[i] - patternOffset;
            if (len == 0 || this.pattern.regionMatches(patternOffset, source, sourceOffset, len)) {
                sourceOffset += len;
                patternOffset += len;
            } else {
                pos.errorIndex = sourceOffset;
                return null;
            }
            if (this.formats[i] == null) {
                int tempLength = i != this.maxOffset ? this.offsets[i + 1] : this.pattern.length();
                int next = patternOffset >= tempLength ? source.length() : source.indexOf(this.pattern.substring(patternOffset, tempLength), sourceOffset);
                if (next < 0) {
                    pos.errorIndex = sourceOffset;
                    return null;
                }
                String strValue = source.substring(sourceOffset, next);
                if (!strValue.equals("{" + this.argumentNumbers[i] + "}")) {
                    resultArray[this.argumentNumbers[i]] = source.substring(sourceOffset, next);
                }
                sourceOffset = next;
                continue;
            }
            tempStatus.index = sourceOffset;
            resultArray[this.argumentNumbers[i]] = this.formats[i].parseObject(source, tempStatus);
            if (tempStatus.index == sourceOffset) {
                pos.errorIndex = sourceOffset;
                return null;
            }
            sourceOffset = tempStatus.index;
        }
        int len = this.pattern.length() - patternOffset;
        if (len != 0 && !this.pattern.regionMatches(patternOffset, source, sourceOffset, len)) {
            pos.errorIndex = sourceOffset;
            return null;
        }
        pos.index = sourceOffset + len;
        return resultArray;
    }

    public Object[] parse(String source) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Object[] result = this.parse(source, pos);
        if (pos.index == 0) {
            throw new ParseException("MessageFormat parse error!", pos.errorIndex);
        }
        return result;
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        return this.parse(source, pos);
    }

    @Override
    public Object clone() {
        MessageFormat other = (MessageFormat)super.clone();
        other.formats = (Format[])this.formats.clone();
        for (int i = 0; i < this.formats.length; ++i) {
            if (this.formats[i] == null) continue;
            other.formats[i] = (Format)this.formats[i].clone();
        }
        other.offsets = (int[])this.offsets.clone();
        other.argumentNumbers = (int[])this.argumentNumbers.clone();
        return other;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        MessageFormat other = (MessageFormat)obj;
        return this.maxOffset == other.maxOffset && this.pattern.equals(other.pattern) && (this.locale != null && this.locale.equals(other.locale) || this.locale == null && other.locale == null) && Arrays.equals(this.offsets, other.offsets) && Arrays.equals(this.argumentNumbers, other.argumentNumbers) && Arrays.equals(this.formats, other.formats);
    }

    public int hashCode() {
        return this.pattern.hashCode();
    }

    private StringBuffer subformat(Object[] arguments, StringBuffer result, FieldPosition fp, List<AttributedCharacterIterator> characterIterators) {
        int lastOffset = 0;
        int last = result.length();
        for (int i = 0; i <= this.maxOffset; ++i) {
            result.append(this.pattern, lastOffset, this.offsets[i]);
            lastOffset = this.offsets[i];
            int argumentNumber = this.argumentNumbers[i];
            if (arguments == null || argumentNumber >= arguments.length) {
                result.append('{').append(argumentNumber).append('}');
                continue;
            }
            Object[] obj = arguments[argumentNumber];
            String arg = null;
            Format subFormatter = null;
            if (obj == null) {
                arg = "null";
            } else if (this.formats[i] != null) {
                subFormatter = this.formats[i];
                if (subFormatter instanceof ChoiceFormat && (arg = this.formats[i].format(obj)).indexOf(123) >= 0) {
                    subFormatter = new MessageFormat(arg, this.locale);
                    obj = arguments;
                    arg = null;
                }
            } else if (obj instanceof Number) {
                subFormatter = NumberFormat.getInstance(this.locale);
            } else if (obj instanceof Date) {
                subFormatter = DateFormat.getDateTimeInstance(3, 3, this.locale);
            } else if (obj instanceof String) {
                arg = (String)obj;
            } else {
                arg = obj.toString();
                if (arg == null) {
                    arg = "null";
                }
            }
            if (characterIterators != null) {
                if (last != result.length()) {
                    characterIterators.add(this.createAttributedCharacterIterator(result.substring(last)));
                    last = result.length();
                }
                if (subFormatter != null) {
                    AttributedCharacterIterator subIterator = subFormatter.formatToCharacterIterator(obj);
                    this.append(result, subIterator);
                    if (last != result.length()) {
                        characterIterators.add(this.createAttributedCharacterIterator(subIterator, (AttributedCharacterIterator.Attribute)Field.ARGUMENT, (Object)argumentNumber));
                        last = result.length();
                    }
                    arg = null;
                }
                if (arg == null || arg.isEmpty()) continue;
                result.append(arg);
                characterIterators.add(this.createAttributedCharacterIterator(arg, (AttributedCharacterIterator.Attribute)Field.ARGUMENT, (Object)argumentNumber));
                last = result.length();
                continue;
            }
            if (subFormatter != null) {
                arg = subFormatter.format(obj);
            }
            last = result.length();
            result.append(arg);
            if (i == 0 && fp != null && Field.ARGUMENT.equals(fp.getFieldAttribute())) {
                fp.setBeginIndex(last);
                fp.setEndIndex(result.length());
            }
            last = result.length();
        }
        result.append(this.pattern, lastOffset, this.pattern.length());
        if (characterIterators != null && last != result.length()) {
            characterIterators.add(this.createAttributedCharacterIterator(result.substring(last)));
        }
        return result;
    }

    private void append(StringBuffer result, CharacterIterator iterator) {
        if (iterator.first() != '\uffff') {
            char aChar;
            result.append(iterator.first());
            while ((aChar = iterator.next()) != '\uffff') {
                result.append(aChar);
            }
        }
    }

    private void makeFormat(int position, int offsetNumber, StringBuilder[] textSegments) {
        int argumentNumber;
        String[] segments = new String[textSegments.length];
        for (int i = 0; i < textSegments.length; ++i) {
            StringBuilder oneseg = textSegments[i];
            segments[i] = oneseg != null ? oneseg.toString() : "";
        }
        try {
            argumentNumber = Integer.parseInt(segments[1]);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("can't parse argument number: " + segments[1], e);
        }
        if (argumentNumber < 0) {
            throw new IllegalArgumentException("negative argument number: " + argumentNumber);
        }
        if (argumentNumber >= 10000) {
            throw new IllegalArgumentException(argumentNumber + " exceeds the ArgumentIndex implementation limit");
        }
        if (offsetNumber >= this.formats.length) {
            int newLength = this.formats.length * 2;
            Format[] newFormats = new Format[newLength];
            int[] newOffsets = new int[newLength];
            int[] newArgumentNumbers = new int[newLength];
            System.arraycopy(this.formats, 0, newFormats, 0, this.maxOffset + 1);
            System.arraycopy(this.offsets, 0, newOffsets, 0, this.maxOffset + 1);
            System.arraycopy(this.argumentNumbers, 0, newArgumentNumbers, 0, this.maxOffset + 1);
            this.formats = newFormats;
            this.offsets = newOffsets;
            this.argumentNumbers = newArgumentNumbers;
        }
        int oldMaxOffset = this.maxOffset;
        this.maxOffset = offsetNumber;
        this.offsets[offsetNumber] = segments[0].length();
        this.argumentNumbers[offsetNumber] = argumentNumber;
        Format newFormat = null;
        if (!segments[2].isEmpty()) {
            int type = MessageFormat.findKeyword(segments[2], TYPE_KEYWORDS);
            block4 : switch (type) {
                case 0: {
                    break;
                }
                case 1: {
                    switch (MessageFormat.findKeyword(segments[3], NUMBER_MODIFIER_KEYWORDS)) {
                        case 0: {
                            newFormat = NumberFormat.getInstance(this.locale);
                            break block4;
                        }
                        case 1: {
                            newFormat = NumberFormat.getCurrencyInstance(this.locale);
                            break block4;
                        }
                        case 2: {
                            newFormat = NumberFormat.getPercentInstance(this.locale);
                            break block4;
                        }
                        case 3: {
                            newFormat = NumberFormat.getIntegerInstance(this.locale);
                            break block4;
                        }
                    }
                    try {
                        newFormat = new DecimalFormat(segments[3], DecimalFormatSymbols.getInstance(this.locale));
                        break;
                    }
                    catch (IllegalArgumentException e) {
                        this.maxOffset = oldMaxOffset;
                        throw e;
                    }
                }
                case 2: 
                case 3: {
                    int mod = MessageFormat.findKeyword(segments[3], DATE_TIME_MODIFIER_KEYWORDS);
                    if (mod >= 0 && mod < DATE_TIME_MODIFIER_KEYWORDS.length) {
                        if (type == 2) {
                            newFormat = DateFormat.getDateInstance(DATE_TIME_MODIFIERS[mod], this.locale);
                            break;
                        }
                        newFormat = DateFormat.getTimeInstance(DATE_TIME_MODIFIERS[mod], this.locale);
                        break;
                    }
                    try {
                        newFormat = new SimpleDateFormat(segments[3], this.locale);
                        break;
                    }
                    catch (IllegalArgumentException e) {
                        this.maxOffset = oldMaxOffset;
                        throw e;
                    }
                }
                case 4: {
                    try {
                        newFormat = new ChoiceFormat(segments[3]);
                        break;
                    }
                    catch (Exception e) {
                        this.maxOffset = oldMaxOffset;
                        throw new IllegalArgumentException("Choice Pattern incorrect: " + segments[3], e);
                    }
                }
                default: {
                    this.maxOffset = oldMaxOffset;
                    throw new IllegalArgumentException("unknown format type: " + segments[2]);
                }
            }
        }
        this.formats[offsetNumber] = newFormat;
    }

    private static final int findKeyword(String s, String[] list) {
        for (int i = 0; i < list.length; ++i) {
            if (!s.equals(list[i])) continue;
            return i;
        }
        String ls = s.trim().toLowerCase(Locale.ROOT);
        if (ls != s) {
            for (int i = 0; i < list.length; ++i) {
                if (!ls.equals(list[i])) continue;
                return i;
            }
        }
        return -1;
    }

    private static final void copyAndFixQuotes(String source, int start, int end, StringBuilder target) {
        boolean quoted = false;
        for (int i = start; i < end; ++i) {
            char ch = source.charAt(i);
            if (ch == '{') {
                if (!quoted) {
                    target.append('\'');
                    quoted = true;
                }
                target.append(ch);
                continue;
            }
            if (ch == '\'') {
                target.append("''");
                continue;
            }
            if (quoted) {
                target.append('\'');
                quoted = false;
            }
            target.append(ch);
        }
        if (quoted) {
            target.append('\'');
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        boolean isValid;
        ObjectInputStream.GetField fields = in.readFields();
        if (fields.defaulted("argumentNumbers") || fields.defaulted("offsets") || fields.defaulted("formats") || fields.defaulted("locale") || fields.defaulted("pattern") || fields.defaulted("maxOffset")) {
            throw new InvalidObjectException("Stream has missing data");
        }
        this.locale = (Locale)fields.get("locale", null);
        String patt = (String)fields.get("pattern", null);
        int maxOff = fields.get("maxOffset", -2);
        int[] argNums = (int[])((int[])fields.get("argumentNumbers", null)).clone();
        int[] offs = (int[])((int[])fields.get("offsets", null)).clone();
        Format[] fmts = (Format[])((Format[])fields.get("formats", null)).clone();
        boolean bl = isValid = maxOff >= -1 && argNums.length > maxOff && offs.length > maxOff && fmts.length > maxOff;
        if (isValid) {
            int lastOffset = patt.length();
            for (int i = maxOff; i >= 0; --i) {
                if (argNums[i] < 0 || argNums[i] >= 10000 || offs[i] < 0 || offs[i] > lastOffset) {
                    isValid = false;
                    break;
                }
                lastOffset = offs[i];
            }
        }
        if (!isValid) {
            throw new InvalidObjectException("Stream has invalid data");
        }
        this.maxOffset = maxOff;
        this.pattern = patt;
        this.offsets = offs;
        this.formats = fmts;
        this.argumentNumbers = argNums;
    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("Deserialized MessageFormat objects need data");
    }

    public static class Field
    extends Format.Field {
        private static final long serialVersionUID = 7899943957617360810L;
        public static final Field ARGUMENT = new Field("message argument field");

        protected Field(String name) {
            super(name);
        }

        @Override
        protected Object readResolve() throws InvalidObjectException {
            if (this.getClass() != Field.class) {
                throw new InvalidObjectException("subclass didn't correctly implement readResolve");
            }
            return ARGUMENT;
        }
    }
}

