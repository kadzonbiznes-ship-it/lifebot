/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.spi.NumberFormatProvider;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.DuplicateFormatFlagsException;
import java.util.FormatFlagsConversionMismatchException;
import java.util.Formattable;
import java.util.FormatterClosedException;
import java.util.IllegalFormatArgumentIndexException;
import java.util.IllegalFormatCodePointException;
import java.util.IllegalFormatConversionException;
import java.util.IllegalFormatFlagsException;
import java.util.IllegalFormatPrecisionException;
import java.util.IllegalFormatWidthException;
import java.util.List;
import java.util.Locale;
import java.util.MissingFormatArgumentException;
import java.util.MissingFormatWidthException;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UnknownFormatConversionException;
import java.util.UnknownFormatFlagsException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jdk.internal.math.FormattedFPDecimal;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.ResourceBundleBasedAdapter;

public final class Formatter
implements Closeable,
Flushable {
    private static DecimalFormatSymbols DFS = null;
    private Appendable a;
    private final Locale l;
    private IOException lastException;
    static final String FORMAT_SPECIFIER = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";
    static final Pattern FORMAT_SPECIFIER_PATTERN = Pattern.compile("%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])");

    private static DecimalFormatSymbols getDecimalFormatSymbols(Locale locale) {
        DecimalFormatSymbols dfs = DFS;
        if (dfs != null && dfs.getLocale().equals(locale)) {
            return dfs;
        }
        DFS = dfs = DecimalFormatSymbols.getInstance(locale);
        return dfs;
    }

    private static char getZero(Locale locale) {
        return locale == null ? (char)'0' : Formatter.getDecimalFormatSymbols(locale).getZeroDigit();
    }

    private static char getDecimalSeparator(Locale locale) {
        return locale == null ? (char)'.' : Formatter.getDecimalFormatSymbols(locale).getDecimalSeparator();
    }

    private static char getGroupingSeparator(Locale locale) {
        return locale == null ? (char)',' : Formatter.getDecimalFormatSymbols(locale).getGroupingSeparator();
    }

    private static Charset toCharset(String csn) throws UnsupportedEncodingException {
        Objects.requireNonNull(csn, "charsetName");
        try {
            return Charset.forName(csn);
        }
        catch (IllegalCharsetNameException | UnsupportedCharsetException unused) {
            throw new UnsupportedEncodingException(csn);
        }
    }

    private static Appendable nonNullAppendable(Appendable a) {
        if (a == null) {
            return new StringBuilder();
        }
        return a;
    }

    private Formatter(Locale l, Appendable a) {
        this.a = a;
        this.l = l;
    }

    private Formatter(Charset charset, Locale l, File file) throws FileNotFoundException {
        this(l, new BufferedWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(file), charset)));
    }

    public Formatter() {
        this(Locale.getDefault(Locale.Category.FORMAT), new StringBuilder());
    }

    public Formatter(Appendable a) {
        this(Locale.getDefault(Locale.Category.FORMAT), Formatter.nonNullAppendable(a));
    }

    public Formatter(Locale l) {
        this(l, new StringBuilder());
    }

    public Formatter(Appendable a, Locale l) {
        this(l, Formatter.nonNullAppendable(a));
    }

    public Formatter(String fileName) throws FileNotFoundException {
        this(Locale.getDefault(Locale.Category.FORMAT), new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName))));
    }

    public Formatter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(fileName, csn, Locale.getDefault(Locale.Category.FORMAT));
    }

    public Formatter(String fileName, String csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
        this(Formatter.toCharset(csn), l, new File(fileName));
    }

    public Formatter(String fileName, Charset charset, Locale l) throws IOException {
        this(Objects.requireNonNull(charset, "charset"), l, new File(fileName));
    }

    public Formatter(File file) throws FileNotFoundException {
        this(Locale.getDefault(Locale.Category.FORMAT), new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))));
    }

    public Formatter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        this(file, csn, Locale.getDefault(Locale.Category.FORMAT));
    }

    public Formatter(File file, String csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
        this(Formatter.toCharset(csn), l, file);
    }

    public Formatter(File file, Charset charset, Locale l) throws IOException {
        this(Objects.requireNonNull(charset, "charset"), l, file);
    }

    public Formatter(PrintStream ps) {
        this(Locale.getDefault(Locale.Category.FORMAT), Objects.requireNonNull(ps));
    }

    public Formatter(OutputStream os) {
        this(Locale.getDefault(Locale.Category.FORMAT), new BufferedWriter(new OutputStreamWriter(os)));
    }

    public Formatter(OutputStream os, String csn) throws UnsupportedEncodingException {
        this(os, csn, Locale.getDefault(Locale.Category.FORMAT));
    }

    public Formatter(OutputStream os, String csn, Locale l) throws UnsupportedEncodingException {
        this(l, new BufferedWriter(new OutputStreamWriter(os, csn)));
    }

    public Formatter(OutputStream os, Charset charset, Locale l) {
        this(l, new BufferedWriter(new OutputStreamWriter(os, charset)));
    }

    public Locale locale() {
        this.ensureOpen();
        return this.l;
    }

    public Appendable out() {
        this.ensureOpen();
        return this.a;
    }

    public String toString() {
        this.ensureOpen();
        return this.a.toString();
    }

    @Override
    public void flush() {
        this.ensureOpen();
        if (this.a instanceof Flushable) {
            try {
                ((Flushable)((Object)this.a)).flush();
            }
            catch (IOException ioe) {
                this.lastException = ioe;
            }
        }
    }

    @Override
    public void close() {
        if (this.a == null) {
            return;
        }
        try {
            if (this.a instanceof Closeable) {
                ((Closeable)((Object)this.a)).close();
            }
        }
        catch (IOException ioe) {
            this.lastException = ioe;
        }
        finally {
            this.a = null;
        }
    }

    private void ensureOpen() {
        if (this.a == null) {
            throw new FormatterClosedException();
        }
    }

    public IOException ioException() {
        return this.lastException;
    }

    public Formatter format(String format, Object ... args) {
        return this.format(this.l, format, args);
    }

    public Formatter format(Locale l, String format, Object ... args) {
        this.ensureOpen();
        int last = -1;
        int lasto = -1;
        List<FormatString> fsa = Formatter.parse(format);
        for (FormatString fs : fsa) {
            int index = fs.index();
            try {
                switch (index) {
                    case -2: {
                        fs.print(this, null, l);
                        break;
                    }
                    case -1: {
                        if (last < 0 || args != null && last > args.length - 1) {
                            throw new MissingFormatArgumentException(fs.toString());
                        }
                        fs.print(this, args == null ? null : args[last], l);
                        break;
                    }
                    case 0: {
                        last = ++lasto;
                        if (args != null && lasto > args.length - 1) {
                            throw new MissingFormatArgumentException(fs.toString());
                        }
                        fs.print(this, args == null ? null : args[lasto], l);
                        break;
                    }
                    default: {
                        last = index - 1;
                        if (args != null && last > args.length - 1) {
                            throw new MissingFormatArgumentException(fs.toString());
                        }
                        fs.print(this, args == null ? null : args[last], l);
                        break;
                    }
                }
            }
            catch (IOException x) {
                this.lastException = x;
            }
        }
        return this;
    }

    static List<FormatString> parse(String s) {
        ArrayList<FormatString> al = new ArrayList<FormatString>();
        int i = 0;
        int max = s.length();
        Matcher m = null;
        while (i < max) {
            int n = s.indexOf(37, i);
            if (n < 0) {
                al.add(new FixedString(s, i, max));
                break;
            }
            if (i != n) {
                al.add(new FixedString(s, i, n));
            }
            if ((i = n + 1) >= max) {
                throw new UnknownFormatConversionException("%");
            }
            char c = s.charAt(i);
            if (Conversion.isValid(c)) {
                al.add(new FormatSpecifier(c));
                ++i;
                continue;
            }
            if (m == null) {
                m = FORMAT_SPECIFIER_PATTERN.matcher(s);
            }
            if (m.find(n) && m.start() == n) {
                al.add(new FormatSpecifier(s, m));
                i = m.end();
                continue;
            }
            throw new UnknownFormatConversionException(String.valueOf(c));
        }
        return al;
    }

    static interface FormatString {
        public int index();

        public void print(Formatter var1, Object var2, Locale var3) throws IOException;

        public String toString();
    }

    private static class FixedString
    implements FormatString {
        private final String s;
        private final int start;
        private final int end;

        FixedString(String s, int start, int end) {
            this.s = s;
            this.start = start;
            this.end = end;
        }

        @Override
        public int index() {
            return -2;
        }

        @Override
        public void print(Formatter fmt, Object arg, Locale l) throws IOException {
            fmt.a.append(this.s, this.start, this.end);
        }

        @Override
        public String toString() {
            return this.s.substring(this.start, this.end);
        }
    }

    static class Conversion {
        static final char DECIMAL_INTEGER = 'd';
        static final char OCTAL_INTEGER = 'o';
        static final char HEXADECIMAL_INTEGER = 'x';
        static final char HEXADECIMAL_INTEGER_UPPER = 'X';
        static final char SCIENTIFIC = 'e';
        static final char SCIENTIFIC_UPPER = 'E';
        static final char GENERAL = 'g';
        static final char GENERAL_UPPER = 'G';
        static final char DECIMAL_FLOAT = 'f';
        static final char HEXADECIMAL_FLOAT = 'a';
        static final char HEXADECIMAL_FLOAT_UPPER = 'A';
        static final char CHARACTER = 'c';
        static final char CHARACTER_UPPER = 'C';
        static final char DATE_TIME = 't';
        static final char DATE_TIME_UPPER = 'T';
        static final char BOOLEAN = 'b';
        static final char BOOLEAN_UPPER = 'B';
        static final char STRING = 's';
        static final char STRING_UPPER = 'S';
        static final char HASHCODE = 'h';
        static final char HASHCODE_UPPER = 'H';
        static final char LINE_SEPARATOR = 'n';
        static final char PERCENT_SIGN = '%';

        Conversion() {
        }

        static boolean isValid(char c) {
            return switch (c) {
                case '%', 'A', 'B', 'C', 'E', 'G', 'H', 'S', 'X', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'n', 'o', 's', 'x' -> true;
                default -> false;
            };
        }

        static boolean isGeneral(char c) {
            return switch (c) {
                case 'B', 'H', 'S', 'b', 'h', 's' -> true;
                default -> false;
            };
        }

        static boolean isCharacter(char c) {
            return switch (c) {
                case 'C', 'c' -> true;
                default -> false;
            };
        }

        static boolean isInteger(char c) {
            return switch (c) {
                case 'X', 'd', 'o', 'x' -> true;
                default -> false;
            };
        }

        static boolean isFloat(char c) {
            return switch (c) {
                case 'A', 'E', 'G', 'a', 'e', 'f', 'g' -> true;
                default -> false;
            };
        }

        static boolean isText(char c) {
            return switch (c) {
                case '%', 'n' -> true;
                default -> false;
            };
        }
    }

    static class FormatSpecifier
    implements FormatString {
        private static final double SCALEUP = Math.scalb(1.0, 54);
        int index = 0;
        int flags = 0;
        int width = -1;
        int precision = -1;
        boolean dt = false;
        char c;

        private void index(String s, int start, int end) {
            if (start >= 0) {
                try {
                    this.index = Integer.parseInt(s, start, end - 1, 10);
                    if (this.index <= 0) {
                        throw new IllegalFormatArgumentIndexException(this.index);
                    }
                }
                catch (NumberFormatException x) {
                    throw new IllegalFormatArgumentIndexException(Integer.MIN_VALUE);
                }
            }
        }

        @Override
        public int index() {
            return this.index;
        }

        private void flags(String s, int start, int end) {
            this.flags = Flags.parse(s, start, end);
            if (Flags.contains(this.flags, 256)) {
                this.index = -1;
            }
        }

        private void width(String s, int start, int end) {
            if (start >= 0) {
                try {
                    this.width = Integer.parseInt(s, start, end, 10);
                    if (this.width < 0) {
                        throw new IllegalFormatWidthException(this.width);
                    }
                }
                catch (NumberFormatException x) {
                    throw new IllegalFormatWidthException(Integer.MIN_VALUE);
                }
            }
        }

        private void precision(String s, int start, int end) {
            if (start >= 0) {
                try {
                    this.precision = Integer.parseInt(s, start + 1, end, 10);
                    if (this.precision < 0) {
                        throw new IllegalFormatPrecisionException(this.precision);
                    }
                }
                catch (NumberFormatException x) {
                    throw new IllegalFormatPrecisionException(Integer.MIN_VALUE);
                }
            }
        }

        private void conversion(char conv) {
            this.c = conv;
            if (!this.dt) {
                if (!Conversion.isValid(this.c)) {
                    throw new UnknownFormatConversionException(String.valueOf(this.c));
                }
                if (Character.isUpperCase(this.c)) {
                    this.flags = Flags.add(this.flags, 2);
                    this.c = Character.toLowerCase(this.c);
                }
                if (Conversion.isText(this.c)) {
                    this.index = -2;
                }
            }
        }

        FormatSpecifier(char conv) {
            this.c = conv;
            if (Character.isUpperCase(conv)) {
                this.flags = 2;
                this.c = Character.toLowerCase(conv);
            }
            if (Conversion.isText(conv)) {
                this.index = -2;
            }
        }

        FormatSpecifier(String s, Matcher m) {
            this.index(s, m.start(1), m.end(1));
            this.flags(s, m.start(2), m.end(2));
            this.width(s, m.start(3), m.end(3));
            this.precision(s, m.start(4), m.end(4));
            int tTStart = m.start(5);
            if (tTStart >= 0) {
                this.dt = true;
                if (s.charAt(tTStart) == 'T') {
                    this.flags = Flags.add(this.flags, 2);
                }
            }
            this.conversion(s.charAt(m.start(6)));
            if (this.dt) {
                this.checkDateTime();
            } else if (Conversion.isGeneral(this.c)) {
                this.checkGeneral();
            } else if (Conversion.isCharacter(this.c)) {
                this.checkCharacter();
            } else if (Conversion.isInteger(this.c)) {
                this.checkInteger();
            } else if (Conversion.isFloat(this.c)) {
                this.checkFloat();
            } else if (Conversion.isText(this.c)) {
                this.checkText();
            } else {
                throw new UnknownFormatConversionException(String.valueOf(this.c));
            }
        }

        @Override
        public void print(Formatter fmt, Object arg, Locale l) throws IOException {
            if (this.dt) {
                this.printDateTime(fmt, arg, l);
                return;
            }
            switch (this.c) {
                case 'd': 
                case 'o': 
                case 'x': {
                    this.printInteger(fmt, arg, l);
                    break;
                }
                case 'a': 
                case 'e': 
                case 'f': 
                case 'g': {
                    this.printFloat(fmt, arg, l);
                    break;
                }
                case 'c': {
                    this.printCharacter(fmt, arg, l);
                    break;
                }
                case 'b': {
                    this.printBoolean(fmt, arg, l);
                    break;
                }
                case 's': {
                    this.printString(fmt, arg, l);
                    break;
                }
                case 'h': {
                    this.printHashCode(fmt, arg, l);
                    break;
                }
                case 'n': {
                    fmt.a.append(System.lineSeparator());
                    break;
                }
                case '%': {
                    this.print(fmt, "%", l);
                    break;
                }
                default: {
                    assert (false);
                    break;
                }
            }
        }

        private void printInteger(Formatter fmt, Object arg, Locale l) throws IOException {
            if (arg == null) {
                this.print(fmt, "null", l);
            } else if (arg instanceof Byte) {
                this.print(fmt, (Byte)arg, l);
            } else if (arg instanceof Short) {
                this.print(fmt, (Short)arg, l);
            } else if (arg instanceof Integer) {
                this.print(fmt, (Integer)arg, l);
            } else if (arg instanceof Long) {
                this.print(fmt, (Long)arg, l);
            } else if (arg instanceof BigInteger) {
                this.print(fmt, (BigInteger)arg, l);
            } else {
                this.failConversion(this.c, arg);
            }
        }

        private void printFloat(Formatter fmt, Object arg, Locale l) throws IOException {
            if (arg == null) {
                this.print(fmt, "null", l);
            } else if (arg instanceof Float) {
                this.print(fmt, ((Float)arg).floatValue(), l);
            } else if (arg instanceof Double) {
                this.print(fmt, (Double)arg, l);
            } else if (arg instanceof BigDecimal) {
                this.print(fmt, (BigDecimal)arg, l);
            } else {
                this.failConversion(this.c, arg);
            }
        }

        private void printDateTime(Formatter fmt, Object arg, Locale l) throws IOException {
            if (arg == null) {
                this.print(fmt, "null", l);
                return;
            }
            Calendar cal = null;
            if (arg instanceof Long) {
                cal = Calendar.getInstance(l == null ? Locale.US : l);
                cal.setTimeInMillis((Long)arg);
            } else if (arg instanceof Date) {
                cal = Calendar.getInstance(l == null ? Locale.US : l);
                cal.setTime((Date)arg);
            } else if (arg instanceof Calendar) {
                cal = (Calendar)((Calendar)arg).clone();
                cal.setLenient(true);
            } else {
                if (arg instanceof TemporalAccessor) {
                    this.print(fmt, (TemporalAccessor)arg, this.c, l);
                    return;
                }
                this.failConversion(this.c, arg);
            }
            this.print(fmt, cal, this.c, l);
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        private void printCharacter(Formatter fmt, Object arg, Locale l) throws IOException {
            if (arg == null) {
                this.print(fmt, "null", l);
                return;
            }
            String s = null;
            if (arg instanceof Character) {
                s = ((Character)arg).toString();
            } else if (arg instanceof Byte) {
                byte i = (Byte)arg;
                if (!Character.isValidCodePoint(i)) throw new IllegalFormatCodePointException(i);
                s = new String(Character.toChars(i));
            } else if (arg instanceof Short) {
                short i = (Short)arg;
                if (!Character.isValidCodePoint(i)) throw new IllegalFormatCodePointException(i);
                s = new String(Character.toChars(i));
            } else if (arg instanceof Integer) {
                int i = (Integer)arg;
                if (!Character.isValidCodePoint(i)) throw new IllegalFormatCodePointException(i);
                s = new String(Character.toChars(i));
            } else {
                this.failConversion(this.c, arg);
            }
            this.print(fmt, s, l);
        }

        private void printString(Formatter fmt, Object arg, Locale l) throws IOException {
            if (arg instanceof Formattable) {
                if (fmt.locale() != l) {
                    fmt = new Formatter(fmt.out(), l);
                }
                ((Formattable)arg).formatTo(fmt, this.flags, this.width, this.precision);
            } else {
                if (Flags.contains(this.flags, 4)) {
                    this.failMismatch(4, 's');
                }
                if (arg == null) {
                    this.print(fmt, "null", l);
                } else {
                    this.print(fmt, arg.toString(), l);
                }
            }
        }

        private void printBoolean(Formatter fmt, Object arg, Locale l) throws IOException {
            String s = arg != null ? (arg instanceof Boolean ? ((Boolean)arg).toString() : Boolean.toString(true)) : Boolean.toString(false);
            this.print(fmt, s, l);
        }

        private void printHashCode(Formatter fmt, Object arg, Locale l) throws IOException {
            String s = arg == null ? "null" : Integer.toHexString(arg.hashCode());
            this.print(fmt, s, l);
        }

        private void print(Formatter fmt, String s, Locale l) throws IOException {
            if (this.precision != -1 && this.precision < s.length()) {
                s = s.substring(0, this.precision);
            }
            if (Flags.contains(this.flags, 2)) {
                s = this.toUpperCaseWithLocale(s, l);
            }
            this.appendJustified(fmt.a, s);
        }

        private String toUpperCaseWithLocale(String s, Locale l) {
            return s.toUpperCase(Objects.requireNonNullElse(l, Locale.getDefault(Locale.Category.FORMAT)));
        }

        private void appendJustified(Appendable a, CharSequence cs) throws IOException {
            if (this.width == -1) {
                a.append(cs);
                return;
            }
            boolean padRight = Flags.contains(this.flags, 1);
            int sp = this.width - cs.length();
            if (padRight) {
                a.append(cs);
            }
            for (int i = 0; i < sp; ++i) {
                a.append(' ');
            }
            if (!padRight) {
                a.append(cs);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("%");
            sb.append(Flags.toString(Flags.remove(this.flags, 2)));
            if (this.index > 0) {
                sb.append(this.index).append('$');
            }
            if (this.width != -1) {
                sb.append(this.width);
            }
            if (this.precision != -1) {
                sb.append('.').append(this.precision);
            }
            if (this.dt) {
                sb.append(Flags.contains(this.flags, 2) ? (char)'T' : 't');
            }
            sb.append(Flags.contains(this.flags, 2) ? Character.toUpperCase(this.c) : this.c);
            return sb.toString();
        }

        private void checkGeneral() {
            if ((this.c == 'b' || this.c == 'h') && Flags.contains(this.flags, 4)) {
                this.failMismatch(4, this.c);
            }
            if (this.width == -1 && Flags.contains(this.flags, 1)) {
                throw new MissingFormatWidthException(this.toString());
            }
            this.checkBadFlags(248);
        }

        private void checkDateTime() {
            if (this.precision != -1) {
                throw new IllegalFormatPrecisionException(this.precision);
            }
            if (!DateTime.isValid(this.c)) {
                throw new UnknownFormatConversionException("t" + this.c);
            }
            this.checkBadFlags(252);
            if (this.width == -1 && Flags.contains(this.flags, 1)) {
                throw new MissingFormatWidthException(this.toString());
            }
        }

        private void checkCharacter() {
            if (this.precision != -1) {
                throw new IllegalFormatPrecisionException(this.precision);
            }
            this.checkBadFlags(252);
            if (this.width == -1 && Flags.contains(this.flags, 1)) {
                throw new MissingFormatWidthException(this.toString());
            }
        }

        private void checkInteger() {
            this.checkNumeric();
            if (this.precision != -1) {
                throw new IllegalFormatPrecisionException(this.precision);
            }
            if (this.c == 'd') {
                this.checkBadFlags(4);
            } else if (this.c == 'o') {
                this.checkBadFlags(64);
            } else {
                this.checkBadFlags(64);
            }
        }

        private void checkBadFlags(int badFlags) {
            if ((this.flags & badFlags) != 0) {
                this.failMismatch(this.flags & badFlags, this.c);
            }
        }

        private void checkFloat() {
            this.checkNumeric();
            if (this.c != 'f') {
                if (this.c == 'a') {
                    this.checkBadFlags(192);
                } else if (this.c == 'e') {
                    this.checkBadFlags(64);
                } else if (this.c == 'g') {
                    this.checkBadFlags(4);
                }
            }
        }

        private void checkNumeric() {
            if (this.width != -1 && this.width < 0) {
                throw new IllegalFormatWidthException(this.width);
            }
            if (this.precision != -1 && this.precision < 0) {
                throw new IllegalFormatPrecisionException(this.precision);
            }
            if (this.width == -1 && Flags.containsAny(this.flags, 33)) {
                throw new MissingFormatWidthException(this.toString());
            }
            if (Flags.contains(this.flags, 24) || Flags.contains(this.flags, 33)) {
                throw new IllegalFormatFlagsException(Flags.toString(this.flags));
            }
        }

        private void checkText() {
            if (this.precision != -1) {
                throw new IllegalFormatPrecisionException(this.precision);
            }
            switch (this.c) {
                case '%': {
                    if (this.flags != 1 && this.flags != 0) {
                        throw new IllegalFormatFlagsException(Flags.toString(this.flags));
                    }
                    if (this.width != -1 || !Flags.contains(this.flags, 1)) break;
                    throw new MissingFormatWidthException(this.toString());
                }
                case 'n': {
                    if (this.width != -1) {
                        throw new IllegalFormatWidthException(this.width);
                    }
                    if (this.flags == 0) break;
                    throw new IllegalFormatFlagsException(Flags.toString(this.flags));
                }
                default: {
                    assert (false);
                    break;
                }
            }
        }

        private void print(Formatter fmt, byte value, Locale l) throws IOException {
            long v = value;
            if (value < 0 && (this.c == 'o' || this.c == 'x')) {
                v += 256L;
            }
            this.print(fmt, v, l);
        }

        private void print(Formatter fmt, short value, Locale l) throws IOException {
            long v = value;
            if (value < 0 && (this.c == 'o' || this.c == 'x')) assert ((v += 65536L) >= 0L) : v;
            this.print(fmt, v, l);
        }

        private void print(Formatter fmt, int value, Locale l) throws IOException {
            long v = value;
            if (value < 0 && (this.c == 'o' || this.c == 'x')) assert ((v += 0x100000000L) >= 0L) : v;
            this.print(fmt, v, l);
        }

        private void print(Formatter fmt, long value, Locale l) throws IOException {
            StringBuilder sb = new StringBuilder();
            if (this.c == 'd') {
                boolean neg = value < 0L;
                String valueStr = Long.toString(value, 10);
                this.leadingSign(sb, neg);
                this.localizedMagnitude(fmt, sb, valueStr, neg ? 1 : 0, this.flags, this.adjustWidth(this.width, this.flags, neg), l);
                this.trailingSign(sb, neg);
            } else if (this.c == 'o') {
                int len;
                this.checkBadFlags(152);
                String s = Long.toOctalString(value);
                int n = len = Flags.contains(this.flags, 4) ? s.length() + 1 : s.length();
                if (Flags.contains(this.flags, 4)) {
                    sb.append('0');
                }
                if (Flags.contains(this.flags, 32)) {
                    this.trailingZeros(sb, this.width - len);
                }
                sb.append(s);
            } else if (this.c == 'x') {
                int len;
                this.checkBadFlags(152);
                String s = Long.toHexString(value);
                int n = len = Flags.contains(this.flags, 4) ? s.length() + 2 : s.length();
                if (Flags.contains(this.flags, 4)) {
                    sb.append(Flags.contains(this.flags, 2) ? "0X" : "0x");
                }
                if (Flags.contains(this.flags, 32)) {
                    this.trailingZeros(sb, this.width - len);
                }
                if (Flags.contains(this.flags, 2)) {
                    s = this.toUpperCaseWithLocale(s, l);
                }
                sb.append(s);
            }
            this.appendJustified(fmt.a, sb);
        }

        private StringBuilder leadingSign(StringBuilder sb, boolean neg) {
            if (!neg) {
                if (Flags.contains(this.flags, 8)) {
                    sb.append('+');
                } else if (Flags.contains(this.flags, 16)) {
                    sb.append(' ');
                }
            } else if (Flags.contains(this.flags, 128)) {
                sb.append('(');
            } else {
                sb.append('-');
            }
            return sb;
        }

        private StringBuilder trailingSign(StringBuilder sb, boolean neg) {
            if (neg && Flags.contains(this.flags, 128)) {
                sb.append(')');
            }
            return sb;
        }

        private void print(Formatter fmt, BigInteger value, Locale l) throws IOException {
            StringBuilder sb = new StringBuilder();
            boolean neg = value.signum() == -1;
            BigInteger v = value.abs();
            this.leadingSign(sb, neg);
            if (this.c == 'd') {
                this.localizedMagnitude(fmt, sb, v.toString(), 0, this.flags, this.adjustWidth(this.width, this.flags, neg), l);
            } else if (this.c == 'o') {
                String s = v.toString(8);
                int len = s.length() + sb.length();
                if (neg && Flags.contains(this.flags, 128)) {
                    ++len;
                }
                if (Flags.contains(this.flags, 4)) {
                    ++len;
                    sb.append('0');
                }
                if (Flags.contains(this.flags, 32)) {
                    this.trailingZeros(sb, this.width - len);
                }
                sb.append(s);
            } else if (this.c == 'x') {
                String s = v.toString(16);
                int len = s.length() + sb.length();
                if (neg && Flags.contains(this.flags, 128)) {
                    ++len;
                }
                if (Flags.contains(this.flags, 4)) {
                    len += 2;
                    sb.append(Flags.contains(this.flags, 2) ? "0X" : "0x");
                }
                if (Flags.contains(this.flags, 32)) {
                    this.trailingZeros(sb, this.width - len);
                }
                if (Flags.contains(this.flags, 2)) {
                    s = this.toUpperCaseWithLocale(s, l);
                }
                sb.append(s);
            }
            this.trailingSign(sb, value.signum() == -1);
            this.appendJustified(fmt.a, sb);
        }

        private void print(Formatter fmt, float value, Locale l) throws IOException {
            this.print(fmt, (double)value, l);
        }

        private void print(Formatter fmt, double value, Locale l) throws IOException {
            boolean neg;
            StringBuilder sb = new StringBuilder();
            boolean bl = neg = Double.compare(value, 0.0) == -1;
            if (!Double.isNaN(value)) {
                double v = Math.abs(value);
                this.leadingSign(sb, neg);
                if (!Double.isInfinite(v)) {
                    this.print(fmt, sb, v, l, this.flags, this.c, this.precision, neg);
                } else {
                    sb.append(Flags.contains(this.flags, 2) ? "INFINITY" : "Infinity");
                }
                this.trailingSign(sb, neg);
            } else {
                sb.append(Flags.contains(this.flags, 2) ? "NAN" : "NaN");
            }
            this.appendJustified(fmt.a, sb);
        }

        private void print(Formatter fmt, StringBuilder sb, double value, Locale l, int flags, char c, int precision, boolean neg) {
            if (c == 'e') {
                char[] cArray;
                int prec = precision == -1 ? 6 : precision;
                FormattedFPDecimal fd = FormattedFPDecimal.valueOf(value, prec, 'e');
                StringBuilder mant = new StringBuilder().append(fd.getMantissa());
                this.addZeros(mant, prec);
                if (Flags.contains(flags, 4) && prec == 0) {
                    mant.append('.');
                }
                if (value == 0.0) {
                    char[] cArray2 = new char[3];
                    cArray2[0] = 43;
                    cArray2[1] = 48;
                    cArray = cArray2;
                    cArray2[2] = 48;
                } else {
                    cArray = fd.getExponent();
                }
                char[] exp = cArray;
                int newW = this.width;
                if (this.width != -1) {
                    newW = this.adjustWidth(this.width - exp.length - 1, flags, neg);
                }
                this.localizedMagnitude(fmt, sb, mant, 0, flags, newW, l);
                sb.append(Flags.contains(flags, 2) ? (char)'E' : 'e');
                char sign = exp[0];
                assert (sign == '+' || sign == '-');
                sb.append(sign);
                this.localizedMagnitudeExp(fmt, sb, exp, 1, l);
            } else if (c == 'f') {
                int prec = precision == -1 ? 6 : precision;
                FormattedFPDecimal fd = FormattedFPDecimal.valueOf(value, prec, 'f');
                StringBuilder mant = new StringBuilder().append(fd.getMantissa());
                this.addZeros(mant, prec);
                if (Flags.contains(flags, 4) && prec == 0) {
                    mant.append('.');
                }
                int newW = this.width;
                if (this.width != -1) {
                    newW = this.adjustWidth(this.width, flags, neg);
                }
                this.localizedMagnitude(fmt, sb, mant, 0, flags, newW, l);
            } else if (c == 'g') {
                int expRounded;
                char[] exp;
                int prec = precision;
                if (precision == -1) {
                    prec = 6;
                } else if (precision == 0) {
                    prec = 1;
                }
                StringBuilder mant = new StringBuilder();
                if (value == 0.0) {
                    exp = null;
                    mant.append('0');
                    expRounded = 0;
                } else {
                    FormattedFPDecimal fd = FormattedFPDecimal.valueOf(value, prec, 'g');
                    exp = fd.getExponent();
                    mant.append(fd.getMantissa());
                    expRounded = fd.getExponentRounded();
                }
                prec = exp != null ? --prec : (prec -= expRounded + 1);
                this.addZeros(mant, prec);
                if (Flags.contains(flags, 4) && prec == 0) {
                    mant.append('.');
                }
                int newW = this.width;
                if (this.width != -1) {
                    newW = exp != null ? this.adjustWidth(this.width - exp.length - 1, flags, neg) : this.adjustWidth(this.width, flags, neg);
                }
                this.localizedMagnitude(fmt, sb, mant, 0, flags, newW, l);
                if (exp != null) {
                    sb.append(Flags.contains(flags, 2) ? (char)'E' : 'e');
                    char sign = exp[0];
                    assert (sign == '+' || sign == '-');
                    sb.append(sign);
                    this.localizedMagnitudeExp(fmt, sb, exp, 1, l);
                }
            } else if (c == 'a') {
                int prec = precision;
                if (precision == -1) {
                    prec = 0;
                } else if (precision == 0) {
                    prec = 1;
                }
                String s = this.hexDouble(value, prec);
                StringBuilder va = new StringBuilder();
                boolean upper = Flags.contains(flags, 2);
                sb.append(upper ? "0X" : "0x");
                if (Flags.contains(flags, 32)) {
                    int leadingCharacters = 2;
                    if (Flags.contains(flags, 16) || Flags.contains(flags, 8) || neg) {
                        leadingCharacters = 3;
                    }
                    this.trailingZeros(sb, this.width - s.length() - leadingCharacters);
                }
                int idx = s.indexOf(112);
                if (upper) {
                    String tmp = s.substring(0, idx);
                    tmp = tmp.toUpperCase(Locale.ROOT);
                    va.append(tmp);
                } else {
                    va.append(s, 0, idx);
                }
                if (prec != 0) {
                    this.addZeros(va, prec);
                }
                sb.append((CharSequence)va);
                sb.append(upper ? (char)'P' : 'p');
                sb.append(s, idx + 1, s.length());
            }
        }

        private void addZeros(StringBuilder sb, int prec) {
            int i;
            int len = sb.length();
            for (i = 0; i < len && sb.charAt(i) != '.'; ++i) {
            }
            boolean needDot = false;
            if (i == len) {
                needDot = true;
            }
            int outPrec = len - i - (needDot ? 0 : 1);
            assert (outPrec <= prec);
            if (outPrec == prec) {
                return;
            }
            if (needDot) {
                sb.append('.');
            }
            this.trailingZeros(sb, prec - outPrec);
        }

        private String hexDouble(double d, int prec) {
            long signBit;
            double result;
            boolean sticky;
            boolean subnormal;
            if (!Double.isFinite(d) || d == 0.0 || prec == 0 || prec >= 13) {
                return Double.toHexString(d).substring(2);
            }
            assert (prec >= 1 && prec <= 12);
            int exponent = Math.getExponent(d);
            boolean bl = subnormal = exponent == -1023;
            if (subnormal) {
                exponent = Math.getExponent(d *= SCALEUP);
                assert (exponent >= -1022 && exponent <= 1023) : exponent;
            }
            int precision = 1 + prec * 4;
            int shiftDistance = 53 - precision;
            assert (shiftDistance >= 1 && shiftDistance < 53);
            long doppel = Double.doubleToLongBits(d);
            long newSignif = (doppel & Long.MAX_VALUE) >> shiftDistance;
            long roundingBits = doppel & (-1L << shiftDistance ^ 0xFFFFFFFFFFFFFFFFL);
            boolean leastZero = (newSignif & 1L) == 0L;
            boolean round = (1L << shiftDistance - 1 & roundingBits) != 0L;
            boolean bl2 = sticky = shiftDistance > 1 && ((1L << shiftDistance - 1 ^ 0xFFFFFFFFFFFFFFFFL) & roundingBits) != 0L;
            if (leastZero && round && sticky || !leastZero && round) {
                ++newSignif;
            }
            if (Double.isInfinite(result = Double.longBitsToDouble(newSignif = (signBit = doppel & Long.MIN_VALUE) | newSignif << shiftDistance))) {
                return "1.0p1024";
            }
            String res = Double.toHexString(result).substring(2);
            if (!subnormal) {
                return res;
            }
            int idx = res.indexOf(112);
            if (idx == -1) {
                assert (false);
                return null;
            }
            String exp = res.substring(idx + 1);
            int iexp = Integer.parseInt(exp) - 54;
            return res.substring(0, idx) + "p" + Integer.toString(iexp);
        }

        private void print(Formatter fmt, BigDecimal value, Locale l) throws IOException {
            if (this.c == 'a') {
                this.failConversion(this.c, value);
            }
            StringBuilder sb = new StringBuilder();
            boolean neg = value.signum() == -1;
            BigDecimal v = value.abs();
            this.leadingSign(sb, neg);
            this.print(fmt, sb, v, l, this.flags, this.c, this.precision, neg);
            this.trailingSign(sb, neg);
            this.appendJustified(fmt.a, sb);
        }

        private void print(Formatter fmt, StringBuilder sb, BigDecimal value, Locale l, int flags, char c, int precision, boolean neg) throws IOException {
            if (c == 'e') {
                int compPrec;
                int prec = precision == -1 ? 6 : precision;
                int scale = value.scale();
                int origPrec = value.precision();
                int nzeros = 0;
                if (prec > origPrec - 1) {
                    compPrec = origPrec;
                    nzeros = prec - (origPrec - 1);
                } else {
                    compPrec = prec + 1;
                }
                MathContext mc = new MathContext(compPrec);
                BigDecimal v = new BigDecimal(value.unscaledValue(), scale, mc);
                BigDecimalLayout bdl = new BigDecimalLayout(v.unscaledValue(), v.scale(), BigDecimalLayoutForm.SCIENTIFIC);
                StringBuilder mant = bdl.mantissa();
                if (!(origPrec != 1 && bdl.hasDot() || nzeros <= 0 && !Flags.contains(flags, 4))) {
                    mant.append('.');
                }
                this.trailingZeros(mant, nzeros);
                StringBuilder exp = bdl.exponent();
                int newW = this.width;
                if (this.width != -1) {
                    newW = this.adjustWidth(this.width - exp.length() - 1, flags, neg);
                }
                this.localizedMagnitude(fmt, sb, mant, 0, flags, newW, l);
                sb.append(Flags.contains(flags, 2) ? (char)'E' : 'e');
                int adaptedFlags = Flags.remove(flags, 64);
                char sign = exp.charAt(0);
                assert (sign == '+' || sign == '-');
                sb.append(sign);
                sb.append((CharSequence)this.localizedMagnitude(fmt, null, exp, 1, adaptedFlags, -1, l));
            } else if (c == 'f') {
                int nzeros;
                int prec = precision == -1 ? 6 : precision;
                int scale = value.scale();
                if (scale > prec) {
                    int compPrec = value.precision();
                    value = compPrec <= scale ? value.setScale(prec, RoundingMode.HALF_UP) : new BigDecimal(value.unscaledValue(), scale, new MathContext(compPrec -= scale - prec));
                }
                BigDecimalLayout bdl = new BigDecimalLayout(value.unscaledValue(), value.scale(), BigDecimalLayoutForm.DECIMAL_FLOAT);
                StringBuilder mant = bdl.mantissa();
                int n = nzeros = bdl.scale() < prec ? prec - bdl.scale() : 0;
                if (bdl.scale() == 0 && (Flags.contains(flags, 4) || nzeros > 0)) {
                    mant.append('.');
                }
                this.trailingZeros(mant, nzeros);
                this.localizedMagnitude(fmt, sb, mant, 0, flags, this.adjustWidth(this.width, flags, neg), l);
            } else if (c == 'g') {
                int prec = precision;
                if (precision == -1) {
                    prec = 6;
                } else if (precision == 0) {
                    prec = 1;
                }
                value = value.round(new MathContext(prec));
                if (value.equals(BigDecimal.ZERO) || value.compareTo(BigDecimal.valueOf(1L, 4)) != -1 && value.compareTo(BigDecimal.valueOf(1L, -prec)) == -1) {
                    int e = -value.scale() + (value.unscaledValue().toString().length() - 1);
                    prec = prec - e - 1;
                    this.print(fmt, sb, value, l, flags, 'f', prec, neg);
                } else {
                    this.print(fmt, sb, value, l, flags, 'e', prec - 1, neg);
                }
            } else if (c == 'a') assert (false);
        }

        private int adjustWidth(int width, int flags, boolean neg) {
            int newW = width;
            if (newW != -1 && neg && Flags.contains(flags, 128)) {
                --newW;
            }
            return newW;
        }

        private void trailingZeros(StringBuilder sb, int nzeros) {
            for (int i = 0; i < nzeros; ++i) {
                sb.append('0');
            }
        }

        private void print(Formatter fmt, Calendar t, char c, Locale l) throws IOException {
            StringBuilder sb = new StringBuilder();
            this.print(fmt, sb, t, c, l);
            if (Flags.contains(this.flags, 2)) {
                this.appendJustified(fmt.a, this.toUpperCaseWithLocale(sb.toString(), l));
            } else {
                this.appendJustified(fmt.a, sb);
            }
        }

        private Appendable print(Formatter fmt, StringBuilder sb, Calendar t, char c, Locale l) throws IOException {
            if (sb == null) {
                sb = new StringBuilder();
            }
            switch (c) {
                case 'H': 
                case 'I': 
                case 'k': 
                case 'l': {
                    int i = t.get(11);
                    if (c == 'I' || c == 'l') {
                        i = i == 0 || i == 12 ? 12 : i % 12;
                    }
                    int flags = c == 'H' || c == 'I' ? 32 : 0;
                    sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, flags, 2, l));
                    break;
                }
                case 'M': {
                    int i = t.get(12);
                    sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 32, 2, l));
                    break;
                }
                case 'N': {
                    int i = t.get(14) * 1000000;
                    sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 32, 9, l));
                    break;
                }
                case 'L': {
                    int i = t.get(14);
                    sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 32, 3, l));
                    break;
                }
                case 'Q': {
                    long i = t.getTimeInMillis();
                    sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 0, this.width, l));
                    break;
                }
                case 'p': {
                    String[] ampm = new String[]{"AM", "PM"};
                    if (l != null && l != Locale.US) {
                        DateFormatSymbols dfs = DateFormatSymbols.getInstance(l);
                        ampm = dfs.getAmPmStrings();
                    }
                    String s = ampm[t.get(9)];
                    sb.append(s.toLowerCase(Objects.requireNonNullElse(l, Locale.getDefault(Locale.Category.FORMAT))));
                    break;
                }
                case 's': {
                    long i = t.getTimeInMillis() / 1000L;
                    sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 0, this.width, l));
                    break;
                }
                case 'S': {
                    int i = t.get(13);
                    sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 32, 2, l));
                    break;
                }
                case 'z': {
                    int i = t.get(15) + t.get(16);
                    boolean neg = i < 0;
                    sb.append(neg ? (char)'-' : '+');
                    if (neg) {
                        i = -i;
                    }
                    int min = i / 60000;
                    int offset = min / 60 * 100 + min % 60;
                    sb.append((CharSequence)this.localizedMagnitude(fmt, null, offset, 32, 4, l));
                    break;
                }
                case 'Z': {
                    TimeZone tz = t.getTimeZone();
                    sb.append(tz.getDisplayName(t.get(16) != 0, 0, Objects.requireNonNullElse(l, Locale.US)));
                    break;
                }
                case 'A': 
                case 'a': {
                    int i = t.get(7);
                    Locale lt = Objects.requireNonNullElse(l, Locale.US);
                    DateFormatSymbols dfs = DateFormatSymbols.getInstance(lt);
                    if (c == 'A') {
                        sb.append(dfs.getWeekdays()[i]);
                        break;
                    }
                    sb.append(dfs.getShortWeekdays()[i]);
                    break;
                }
                case 'B': 
                case 'b': 
                case 'h': {
                    int i = t.get(2);
                    Locale lt = Objects.requireNonNullElse(l, Locale.US);
                    DateFormatSymbols dfs = DateFormatSymbols.getInstance(lt);
                    if (c == 'B') {
                        sb.append(dfs.getMonths()[i]);
                        break;
                    }
                    sb.append(dfs.getShortMonths()[i]);
                    break;
                }
                case 'C': 
                case 'Y': 
                case 'y': {
                    int i = t.get(1);
                    int size = 2;
                    switch (c) {
                        case 'C': {
                            i /= 100;
                            break;
                        }
                        case 'y': {
                            i %= 100;
                            break;
                        }
                        case 'Y': {
                            size = 4;
                        }
                    }
                    sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 32, size, l));
                    break;
                }
                case 'd': 
                case 'e': {
                    int i = t.get(5);
                    int flags = c == 'd' ? 32 : 0;
                    sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, flags, 2, l));
                    break;
                }
                case 'j': {
                    int i = t.get(6);
                    sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 32, 3, l));
                    break;
                }
                case 'm': {
                    int i = t.get(2) + 1;
                    sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 32, 2, l));
                    break;
                }
                case 'R': 
                case 'T': {
                    char sep = ':';
                    this.print(fmt, sb, t, 'H', l).append(sep);
                    this.print(fmt, sb, t, 'M', l);
                    if (c != 'T') break;
                    sb.append(sep);
                    this.print(fmt, sb, t, 'S', l);
                    break;
                }
                case 'r': {
                    char sep = ':';
                    this.print(fmt, sb, t, 'I', l).append(sep);
                    this.print(fmt, sb, t, 'M', l).append(sep);
                    this.print(fmt, sb, t, 'S', l).append(' ');
                    StringBuilder tsb = new StringBuilder();
                    this.print(fmt, tsb, t, 'p', l);
                    sb.append(this.toUpperCaseWithLocale(tsb.toString(), l));
                    break;
                }
                case 'c': {
                    char sep = ' ';
                    this.print(fmt, sb, t, 'a', l).append(sep);
                    this.print(fmt, sb, t, 'b', l).append(sep);
                    this.print(fmt, sb, t, 'd', l).append(sep);
                    this.print(fmt, sb, t, 'T', l).append(sep);
                    this.print(fmt, sb, t, 'Z', l).append(sep);
                    this.print(fmt, sb, t, 'Y', l);
                    break;
                }
                case 'D': {
                    char sep = '/';
                    this.print(fmt, sb, t, 'm', l).append(sep);
                    this.print(fmt, sb, t, 'd', l).append(sep);
                    this.print(fmt, sb, t, 'y', l);
                    break;
                }
                case 'F': {
                    char sep = '-';
                    this.print(fmt, sb, t, 'Y', l).append(sep);
                    this.print(fmt, sb, t, 'm', l).append(sep);
                    this.print(fmt, sb, t, 'd', l);
                    break;
                }
                default: {
                    assert (false);
                    break;
                }
            }
            return sb;
        }

        private void print(Formatter fmt, TemporalAccessor t, char c, Locale l) throws IOException {
            StringBuilder sb = new StringBuilder();
            this.print(fmt, sb, t, c, l);
            if (Flags.contains(this.flags, 2)) {
                this.appendJustified(fmt.a, this.toUpperCaseWithLocale(sb.toString(), l));
            } else {
                this.appendJustified(fmt.a, sb);
            }
        }

        private Appendable print(Formatter fmt, StringBuilder sb, TemporalAccessor t, char c, Locale l) throws IOException {
            if (sb == null) {
                sb = new StringBuilder();
            }
            try {
                switch (c) {
                    case 'H': {
                        int i = t.get(ChronoField.HOUR_OF_DAY);
                        sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 32, 2, l));
                        break;
                    }
                    case 'k': {
                        int i = t.get(ChronoField.HOUR_OF_DAY);
                        sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 0, 2, l));
                        break;
                    }
                    case 'I': {
                        int i = t.get(ChronoField.CLOCK_HOUR_OF_AMPM);
                        sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 32, 2, l));
                        break;
                    }
                    case 'l': {
                        int i = t.get(ChronoField.CLOCK_HOUR_OF_AMPM);
                        sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 0, 2, l));
                        break;
                    }
                    case 'M': {
                        int i = t.get(ChronoField.MINUTE_OF_HOUR);
                        sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 32, 2, l));
                        break;
                    }
                    case 'N': {
                        int i;
                        try {
                            i = t.get(ChronoField.NANO_OF_SECOND);
                        }
                        catch (UnsupportedTemporalTypeException u) {
                            i = t.get(ChronoField.MILLI_OF_SECOND) * 1000000;
                        }
                        sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 32, 9, l));
                        break;
                    }
                    case 'L': {
                        int i = t.get(ChronoField.MILLI_OF_SECOND);
                        sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 32, 3, l));
                        break;
                    }
                    case 'Q': {
                        long i = t.getLong(ChronoField.INSTANT_SECONDS) * 1000L + t.getLong(ChronoField.MILLI_OF_SECOND);
                        sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 0, this.width, l));
                        break;
                    }
                    case 'p': {
                        String[] ampm = new String[]{"AM", "PM"};
                        if (l != null && l != Locale.US) {
                            DateFormatSymbols dfs = DateFormatSymbols.getInstance(l);
                            ampm = dfs.getAmPmStrings();
                        }
                        String s = ampm[t.get(ChronoField.AMPM_OF_DAY)];
                        sb.append(s.toLowerCase(Objects.requireNonNullElse(l, Locale.getDefault(Locale.Category.FORMAT))));
                        break;
                    }
                    case 's': {
                        long i = t.getLong(ChronoField.INSTANT_SECONDS);
                        sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 0, this.width, l));
                        break;
                    }
                    case 'S': {
                        int i = t.get(ChronoField.SECOND_OF_MINUTE);
                        sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 32, 2, l));
                        break;
                    }
                    case 'z': {
                        int i = t.get(ChronoField.OFFSET_SECONDS);
                        boolean neg = i < 0;
                        sb.append(neg ? (char)'-' : '+');
                        if (neg) {
                            i = -i;
                        }
                        int min = i / 60;
                        int offset = min / 60 * 100 + min % 60;
                        sb.append((CharSequence)this.localizedMagnitude(fmt, null, offset, 32, 4, l));
                        break;
                    }
                    case 'Z': {
                        ZoneId zid = t.query(TemporalQueries.zone());
                        if (zid == null) {
                            throw new IllegalFormatConversionException(c, t.getClass());
                        }
                        if (!(zid instanceof ZoneOffset) && t.isSupported(ChronoField.INSTANT_SECONDS)) {
                            Instant instant = Instant.from(t);
                            sb.append(TimeZone.getTimeZone(zid.getId()).getDisplayName(zid.getRules().isDaylightSavings(instant), 0, Objects.requireNonNullElse(l, Locale.US)));
                            break;
                        }
                        sb.append(zid.getId());
                        break;
                    }
                    case 'A': 
                    case 'a': {
                        int i = t.get(ChronoField.DAY_OF_WEEK) % 7 + 1;
                        Locale lt = Objects.requireNonNullElse(l, Locale.US);
                        DateFormatSymbols dfs = DateFormatSymbols.getInstance(lt);
                        if (c == 'A') {
                            sb.append(dfs.getWeekdays()[i]);
                            break;
                        }
                        sb.append(dfs.getShortWeekdays()[i]);
                        break;
                    }
                    case 'B': 
                    case 'b': 
                    case 'h': {
                        int i = t.get(ChronoField.MONTH_OF_YEAR) - 1;
                        Locale lt = Objects.requireNonNullElse(l, Locale.US);
                        DateFormatSymbols dfs = DateFormatSymbols.getInstance(lt);
                        if (c == 'B') {
                            sb.append(dfs.getMonths()[i]);
                            break;
                        }
                        sb.append(dfs.getShortMonths()[i]);
                        break;
                    }
                    case 'C': 
                    case 'Y': 
                    case 'y': {
                        int i = t.get(ChronoField.YEAR_OF_ERA);
                        int size = 2;
                        switch (c) {
                            case 'C': {
                                i /= 100;
                                break;
                            }
                            case 'y': {
                                i %= 100;
                                break;
                            }
                            case 'Y': {
                                size = 4;
                            }
                        }
                        sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 32, size, l));
                        break;
                    }
                    case 'd': 
                    case 'e': {
                        int i = t.get(ChronoField.DAY_OF_MONTH);
                        int flags = c == 'd' ? 32 : 0;
                        sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, flags, 2, l));
                        break;
                    }
                    case 'j': {
                        int i = t.get(ChronoField.DAY_OF_YEAR);
                        sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 32, 3, l));
                        break;
                    }
                    case 'm': {
                        int i = t.get(ChronoField.MONTH_OF_YEAR);
                        sb.append((CharSequence)this.localizedMagnitude(fmt, null, i, 32, 2, l));
                        break;
                    }
                    case 'R': 
                    case 'T': {
                        char sep = ':';
                        this.print(fmt, sb, t, 'H', l).append(sep);
                        this.print(fmt, sb, t, 'M', l);
                        if (c == 'T') {
                            sb.append(sep);
                            this.print(fmt, sb, t, 'S', l);
                        }
                        break;
                    }
                    case 'r': {
                        char sep = ':';
                        this.print(fmt, sb, t, 'I', l).append(sep);
                        this.print(fmt, sb, t, 'M', l).append(sep);
                        this.print(fmt, sb, t, 'S', l).append(' ');
                        StringBuilder tsb = new StringBuilder();
                        this.print(fmt, tsb, t, 'p', l);
                        sb.append(this.toUpperCaseWithLocale(tsb.toString(), l));
                        break;
                    }
                    case 'c': {
                        char sep = ' ';
                        this.print(fmt, sb, t, 'a', l).append(sep);
                        this.print(fmt, sb, t, 'b', l).append(sep);
                        this.print(fmt, sb, t, 'd', l).append(sep);
                        this.print(fmt, sb, t, 'T', l).append(sep);
                        this.print(fmt, sb, t, 'Z', l).append(sep);
                        this.print(fmt, sb, t, 'Y', l);
                        break;
                    }
                    case 'D': {
                        char sep = '/';
                        this.print(fmt, sb, t, 'm', l).append(sep);
                        this.print(fmt, sb, t, 'd', l).append(sep);
                        this.print(fmt, sb, t, 'y', l);
                        break;
                    }
                    case 'F': {
                        char sep = '-';
                        this.print(fmt, sb, t, 'Y', l).append(sep);
                        this.print(fmt, sb, t, 'm', l).append(sep);
                        this.print(fmt, sb, t, 'd', l);
                        break;
                    }
                    default: {
                        assert (false);
                        break;
                    }
                }
            }
            catch (DateTimeException x) {
                throw new IllegalFormatConversionException(c, t.getClass());
            }
            return sb;
        }

        private void failMismatch(int f, char c) {
            String fs = Flags.toString(f);
            throw new FormatFlagsConversionMismatchException(fs, c);
        }

        private void failConversion(char c, Object arg) {
            throw new IllegalFormatConversionException(c, arg.getClass());
        }

        private StringBuilder localizedMagnitude(Formatter fmt, StringBuilder sb, long value, int flags, int width, Locale l) {
            return this.localizedMagnitude(fmt, sb, Long.toString(value, 10), 0, flags, width, l);
        }

        private StringBuilder localizedMagnitude(Formatter fmt, StringBuilder sb, CharSequence value, int offset, int f, int width, Locale l) {
            int j;
            int len;
            if (sb == null) {
                sb = new StringBuilder();
            }
            int begin = sb.length();
            char zero = Formatter.getZero(l);
            char grpSep = '\u0000';
            int grpSize = -1;
            char decSep = '\u0000';
            int dot = len = value.length();
            for (j = offset; j < len; ++j) {
                if (value.charAt(j) != '.') continue;
                dot = j;
                break;
            }
            if (dot < len) {
                decSep = Formatter.getDecimalSeparator(l);
            }
            if (Flags.contains(f, 64)) {
                grpSep = Formatter.getGroupingSeparator(l);
                if (l == null || l.equals(Locale.US)) {
                    grpSize = 3;
                } else {
                    DecimalFormat df = null;
                    NumberFormat nf = NumberFormat.getNumberInstance(l);
                    if (nf instanceof DecimalFormat) {
                        df = (DecimalFormat)nf;
                    } else {
                        LocaleProviderAdapter adapter = LocaleProviderAdapter.getAdapter(NumberFormatProvider.class, l);
                        if (!(adapter instanceof ResourceBundleBasedAdapter)) {
                            adapter = LocaleProviderAdapter.getResourceBundleBased();
                        }
                        String[] all = adapter.getLocaleResources(l).getNumberPatterns();
                        df = new DecimalFormat(all[0], Formatter.getDecimalFormatSymbols(l));
                    }
                    grpSize = df.getGroupingSize();
                    if (!df.isGroupingUsed() || grpSize == 0) {
                        grpSep = '\u0000';
                    }
                }
            }
            for (j = offset; j < len; ++j) {
                if (j == dot) {
                    sb.append(decSep);
                    grpSep = '\u0000';
                    continue;
                }
                char c = value.charAt(j);
                sb.append((char)(c - 48 + zero));
                if (grpSep == '\u0000' || j == dot - 1 || (dot - j) % grpSize != 1) continue;
                sb.append(grpSep);
            }
            if (width > sb.length() && Flags.contains(f, 32)) {
                String zeros = String.valueOf(zero).repeat(width - sb.length());
                sb.insert(begin, zeros);
            }
            return sb;
        }

        private void localizedMagnitudeExp(Formatter fmt, StringBuilder sb, char[] value, int offset, Locale l) {
            char zero = Formatter.getZero(l);
            int len = value.length;
            for (int j = offset; j < len; ++j) {
                char c = value[j];
                sb.append((char)(c - 48 + zero));
            }
        }

        private class BigDecimalLayout {
            private StringBuilder mant;
            private StringBuilder exp;
            private boolean dot = false;
            private int scale;

            public BigDecimalLayout(BigInteger intVal, int scale, BigDecimalLayoutForm form) {
                this.layout(intVal, scale, form);
            }

            public boolean hasDot() {
                return this.dot;
            }

            public int scale() {
                return this.scale;
            }

            public StringBuilder mantissa() {
                return this.mant;
            }

            public StringBuilder exponent() {
                return this.exp;
            }

            private void layout(BigInteger intVal, int scale, BigDecimalLayoutForm form) {
                String coeff = intVal.toString();
                this.scale = scale;
                int len = coeff.length();
                this.mant = new StringBuilder(len + 14);
                if (scale == 0) {
                    if (len > 1) {
                        this.mant.append(coeff.charAt(0));
                        if (form == BigDecimalLayoutForm.SCIENTIFIC) {
                            this.mant.append('.');
                            this.dot = true;
                            this.mant.append(coeff, 1, len);
                            this.exp = new StringBuilder("+");
                            if (len < 10) {
                                this.exp.append('0').append(len - 1);
                            } else {
                                this.exp.append(len - 1);
                            }
                        } else {
                            this.mant.append(coeff, 1, len);
                        }
                    } else {
                        this.mant.append(coeff);
                        if (form == BigDecimalLayoutForm.SCIENTIFIC) {
                            this.exp = new StringBuilder("+00");
                        }
                    }
                } else if (form == BigDecimalLayoutForm.DECIMAL_FLOAT) {
                    if (scale >= len) {
                        this.mant.append("0.");
                        this.dot = true;
                        FormatSpecifier.this.trailingZeros(this.mant, scale - len);
                        this.mant.append(coeff);
                    } else if (scale > 0) {
                        int pad = len - scale;
                        this.mant.append(coeff, 0, pad);
                        this.mant.append('.');
                        this.dot = true;
                        this.mant.append(coeff, pad, len);
                    } else {
                        this.mant.append(coeff, 0, len);
                        if (intVal.signum() != 0) {
                            FormatSpecifier.this.trailingZeros(this.mant, -scale);
                        }
                        this.scale = 0;
                    }
                } else {
                    this.mant.append(coeff.charAt(0));
                    if (len > 1) {
                        this.mant.append('.');
                        this.dot = true;
                        this.mant.append(coeff, 1, len);
                    }
                    this.exp = new StringBuilder();
                    long adjusted = -((long)scale) + (long)(len - 1);
                    if (adjusted != 0L) {
                        long abs = Math.abs(adjusted);
                        this.exp.append(adjusted < 0L ? (char)'-' : '+');
                        if (abs < 10L) {
                            this.exp.append('0');
                        }
                        this.exp.append(abs);
                    } else {
                        this.exp.append("+00");
                    }
                }
            }
        }
    }

    static class DateTime {
        static final char HOUR_OF_DAY_0 = 'H';
        static final char HOUR_0 = 'I';
        static final char HOUR_OF_DAY = 'k';
        static final char HOUR = 'l';
        static final char MINUTE = 'M';
        static final char NANOSECOND = 'N';
        static final char MILLISECOND = 'L';
        static final char MILLISECOND_SINCE_EPOCH = 'Q';
        static final char AM_PM = 'p';
        static final char SECONDS_SINCE_EPOCH = 's';
        static final char SECOND = 'S';
        static final char TIME = 'T';
        static final char ZONE_NUMERIC = 'z';
        static final char ZONE = 'Z';
        static final char NAME_OF_DAY_ABBREV = 'a';
        static final char NAME_OF_DAY = 'A';
        static final char NAME_OF_MONTH_ABBREV = 'b';
        static final char NAME_OF_MONTH = 'B';
        static final char CENTURY = 'C';
        static final char DAY_OF_MONTH_0 = 'd';
        static final char DAY_OF_MONTH = 'e';
        static final char NAME_OF_MONTH_ABBREV_X = 'h';
        static final char DAY_OF_YEAR = 'j';
        static final char MONTH = 'm';
        static final char YEAR_2 = 'y';
        static final char YEAR_4 = 'Y';
        static final char TIME_12_HOUR = 'r';
        static final char TIME_24_HOUR = 'R';
        static final char DATE_TIME = 'c';
        static final char DATE = 'D';
        static final char ISO_STANDARD_DATE = 'F';

        DateTime() {
        }

        static boolean isValid(char c) {
            return switch (c) {
                case 'H', 'I', 'L', 'M', 'N', 'Q', 'S', 'T', 'Z', 'k', 'l', 'p', 's', 'z' -> true;
                case 'A', 'B', 'C', 'Y', 'a', 'b', 'd', 'e', 'h', 'j', 'm', 'y' -> true;
                case 'D', 'F', 'R', 'c', 'r' -> true;
                default -> false;
            };
        }
    }

    static class Flags {
        static final int NONE = 0;
        static final int LEFT_JUSTIFY = 1;
        static final int UPPERCASE = 2;
        static final int ALTERNATE = 4;
        static final int PLUS = 8;
        static final int LEADING_SPACE = 16;
        static final int ZERO_PAD = 32;
        static final int GROUP = 64;
        static final int PARENTHESES = 128;
        static final int PREVIOUS = 256;

        Flags() {
        }

        public static boolean contains(int flags, int f) {
            return (flags & f) == f;
        }

        public static boolean containsAny(int flags, int f) {
            return (flags & f) != 0;
        }

        private static int add(int flags, int f) {
            return flags | f;
        }

        public static int remove(int flags, int f) {
            return flags & ~f;
        }

        public static int parse(String s, int start, int end) {
            int f = 0;
            for (int i = start; i < end; ++i) {
                char c = s.charAt(i);
                int v = Flags.parse(c);
                if (Flags.contains(f, v)) {
                    throw new DuplicateFormatFlagsException(Flags.toString(v));
                }
                f = Flags.add(f, v);
            }
            return f;
        }

        private static int parse(char c) {
            return switch (c) {
                case '-' -> 1;
                case '#' -> 4;
                case '+' -> 8;
                case ' ' -> 16;
                case '0' -> 32;
                case ',' -> 64;
                case '(' -> 128;
                case '<' -> 256;
                default -> throw new UnknownFormatFlagsException(String.valueOf(c));
            };
        }

        public static String toString(int f) {
            StringBuilder sb = new StringBuilder();
            if (Flags.contains(f, 1)) {
                sb.append('-');
            }
            if (Flags.contains(f, 2)) {
                sb.append('^');
            }
            if (Flags.contains(f, 4)) {
                sb.append('#');
            }
            if (Flags.contains(f, 8)) {
                sb.append('+');
            }
            if (Flags.contains(f, 16)) {
                sb.append(' ');
            }
            if (Flags.contains(f, 32)) {
                sb.append('0');
            }
            if (Flags.contains(f, 64)) {
                sb.append(',');
            }
            if (Flags.contains(f, 128)) {
                sb.append('(');
            }
            if (Flags.contains(f, 256)) {
                sb.append('<');
            }
            return sb.toString();
        }
    }

    public static enum BigDecimalLayoutForm {
        SCIENTIFIC,
        DECIMAL_FLOAT;

    }
}

