/*
 * Decompiled with CFR 0.152.
 */
package java.util.regex;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.regex.ASCII;
import java.util.regex.CharPredicates;
import java.util.regex.IntHashSet;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jdk.internal.util.ArraysSupport;
import jdk.internal.util.regex.Grapheme;
import sun.text.Normalizer;

public final class Pattern
implements Serializable {
    public static final int UNIX_LINES = 1;
    public static final int CASE_INSENSITIVE = 2;
    public static final int COMMENTS = 4;
    public static final int MULTILINE = 8;
    public static final int LITERAL = 16;
    public static final int DOTALL = 32;
    public static final int UNICODE_CASE = 64;
    public static final int CANON_EQ = 128;
    public static final int UNICODE_CHARACTER_CLASS = 256;
    private static final int ALL_FLAGS = 511;
    private static final long serialVersionUID = 5073258162644648461L;
    private String pattern;
    private int flags;
    private transient int flags0;
    private volatile transient boolean compiled;
    private transient String normalizedPattern;
    transient Node root;
    transient Node matchRoot;
    transient int[] buffer;
    transient CharPredicate predicate;
    volatile transient Map<String, Integer> namedGroups;
    transient GroupHead[] groupNodes;
    transient List<Node> topClosureNodes;
    transient int localTCNCount;
    transient boolean hasGroupRef;
    private transient int[] temp;
    transient int capturingGroupCount;
    transient int localCount;
    private transient int cursor;
    private transient int patternLength;
    private transient boolean hasSupplementary;
    static final int MAX_REPS = Integer.MAX_VALUE;
    static final Node accept = new Node();
    static final Node lastAccept = new LastNode();

    public static Pattern compile(String regex) {
        return new Pattern(regex, 0);
    }

    public static Pattern compile(String regex, int flags) {
        return new Pattern(regex, flags);
    }

    public String pattern() {
        return this.pattern;
    }

    public String toString() {
        return this.pattern;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Matcher matcher(CharSequence input) {
        if (!this.compiled) {
            Pattern pattern = this;
            synchronized (pattern) {
                if (!this.compiled) {
                    this.compile();
                }
            }
        }
        Matcher m = new Matcher(this, input);
        return m;
    }

    public int flags() {
        return this.flags0;
    }

    public static boolean matches(String regex, CharSequence input) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        return m.matches();
    }

    public String[] split(CharSequence input, int limit) {
        return this.split(input, limit, false);
    }

    public String[] splitWithDelimiters(CharSequence input, int limit) {
        return this.split(input, limit, true);
    }

    private String[] split(CharSequence input, int limit, boolean withDelimiters) {
        int resultSize;
        int matchCount = 0;
        int index = 0;
        boolean matchLimited = limit > 0;
        ArrayList<String> matchList = new ArrayList<String>();
        Matcher m = this.matcher(input);
        while (m.find()) {
            String match;
            if (!matchLimited || matchCount < limit - 1) {
                if (index == 0 && index == m.start() && m.start() == m.end()) continue;
                match = input.subSequence(index, m.start()).toString();
                matchList.add(match);
                index = m.end();
                if (withDelimiters) {
                    matchList.add(input.subSequence(m.start(), index).toString());
                }
                ++matchCount;
                continue;
            }
            if (matchCount != limit - 1) continue;
            match = input.subSequence(index, input.length()).toString();
            matchList.add(match);
            index = m.end();
            ++matchCount;
        }
        if (index == 0) {
            return new String[]{input.toString()};
        }
        if (!matchLimited || matchCount < limit) {
            matchList.add(input.subSequence(index, input.length()).toString());
        }
        if (limit == 0) {
            for (resultSize = matchList.size(); resultSize > 0 && ((String)matchList.get(resultSize - 1)).isEmpty(); --resultSize) {
            }
        }
        String[] result = new String[resultSize];
        return matchList.subList(0, resultSize).toArray(result);
    }

    public String[] split(CharSequence input) {
        return this.split(input, 0, false);
    }

    public static String quote(String s) {
        int slashEIndex = s.indexOf("\\E");
        if (slashEIndex == -1) {
            return "\\Q" + s + "\\E";
        }
        int lenHint = s.length();
        lenHint = lenHint < 0x7FFFFFF7 - lenHint ? lenHint << 1 : 0x7FFFFFF7;
        StringBuilder sb = new StringBuilder(lenHint);
        sb.append("\\Q");
        int current = 0;
        do {
            sb.append(s, current, slashEIndex).append("\\E\\\\E\\Q");
        } while ((slashEIndex = s.indexOf("\\E", current = slashEIndex + 2)) != -1);
        return sb.append(s, current, s.length()).append("\\E").toString();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.flags0 = this.flags;
        this.capturingGroupCount = 1;
        this.localCount = 0;
        this.localTCNCount = 0;
        if (this.pattern.isEmpty()) {
            this.root = new Start(lastAccept);
            this.matchRoot = lastAccept;
            this.compiled = true;
        }
    }

    private Pattern(String p, int f) {
        if ((f & 0xFFFFFE00) != 0) {
            throw new IllegalArgumentException("Unknown flag 0x" + Integer.toHexString(f));
        }
        this.pattern = p;
        this.flags = f;
        if ((this.flags & 0x100) != 0) {
            this.flags |= 0x40;
        }
        this.flags0 = this.flags;
        this.capturingGroupCount = 1;
        this.localCount = 0;
        this.localTCNCount = 0;
        if (!this.pattern.isEmpty()) {
            try {
                this.compile();
            }
            catch (StackOverflowError soe) {
                throw this.error("Stack overflow during pattern compilation");
            }
        } else {
            this.root = new Start(lastAccept);
            this.matchRoot = lastAccept;
        }
    }

    private static String normalize(String pattern) {
        int plen = pattern.length();
        StringBuilder pbuf = new StringBuilder(plen);
        int last = 0;
        int lastStart = 0;
        int cc = 0;
        int i = 0;
        while (i < plen) {
            char c = pattern.charAt(i);
            if (cc == 0 && c == '\\' && i + 1 < plen && pattern.charAt(i + 1) == '\\') {
                i += 2;
                last = 0;
                continue;
            }
            if (c == '[' && last != 92) {
                if (cc == 0) {
                    if (lastStart < i) {
                        Pattern.normalizeSlice(pattern, lastStart, i, pbuf);
                    }
                    lastStart = i;
                }
                cc = (char)(cc + 1);
            } else if (c == ']' && last != 92 && (cc = (int)((char)(cc - 1))) == 0) {
                Pattern.normalizeClazz(pattern, lastStart, i + 1, pbuf);
                lastStart = i + 1;
            }
            last = c;
            ++i;
        }
        assert (cc == 0);
        if (lastStart < plen) {
            Pattern.normalizeSlice(pattern, lastStart, plen, pbuf);
        }
        return pbuf.toString();
    }

    private static void normalizeSlice(String src, int off, int limit, StringBuilder dst) {
        int len = src.length();
        int off0 = off;
        while (off < limit && ASCII.isAscii(src.charAt(off))) {
            ++off;
        }
        if (off == limit) {
            dst.append(src, off0, limit);
            return;
        }
        if (--off < off0) {
            off = off0;
        } else {
            dst.append(src, off0, off);
        }
        while (off < limit) {
            int ch1;
            int ch0 = src.codePointAt(off);
            if (".$|()[]{}^?*+\\".indexOf(ch0) != -1) {
                dst.append((char)ch0);
                ++off;
                continue;
            }
            int j = Grapheme.nextBoundary(src, off, limit);
            String seq = src.substring(off, j);
            String nfd = java.text.Normalizer.normalize(seq, Normalizer.Form.NFD);
            off = j;
            if (nfd.codePointCount(0, nfd.length()) > 1 && Character.getType(ch1 = nfd.codePointAt(Character.charCount(ch0 = nfd.codePointAt(0)))) == 6) {
                LinkedHashSet<String> altns = new LinkedHashSet<String>();
                altns.add(seq);
                Pattern.produceEquivalentAlternation(nfd, altns);
                dst.append("(?:");
                altns.forEach(s -> dst.append((String)s).append('|'));
                dst.delete(dst.length() - 1, dst.length());
                dst.append(")");
                continue;
            }
            String nfc = java.text.Normalizer.normalize(seq, Normalizer.Form.NFC);
            if (!seq.equals(nfc) && !nfd.equals(nfc)) {
                dst.append("(?:" + seq + "|" + nfd + "|" + nfc + ")");
                continue;
            }
            if (!seq.equals(nfd)) {
                dst.append("(?:" + seq + "|" + nfd + ")");
                continue;
            }
            dst.append(seq);
        }
    }

    private static void normalizeClazz(String src, int off, int limit, StringBuilder dst) {
        dst.append(java.text.Normalizer.normalize(src.substring(off, limit), Normalizer.Form.NFC));
    }

    private static void produceEquivalentAlternation(String src, Set<String> dst) {
        int len = Pattern.countChars(src, 0, 1);
        if (src.length() == len) {
            dst.add(src);
            return;
        }
        String base = src.substring(0, len);
        String combiningMarks = src.substring(len);
        String[] perms = Pattern.producePermutations(combiningMarks);
        for (int x = 0; x < perms.length; ++x) {
            String next = base + perms[x];
            dst.add(next);
            next = Pattern.composeOneStep(next);
            if (next == null) continue;
            Pattern.produceEquivalentAlternation(next, dst);
        }
    }

    private static String[] producePermutations(String input) {
        if (input.length() == Pattern.countChars(input, 0, 1)) {
            return new String[]{input};
        }
        if (input.length() == Pattern.countChars(input, 0, 2)) {
            int c0 = Character.codePointAt(input, 0);
            int c1 = Character.codePointAt(input, Character.charCount(c0));
            if (Pattern.getClass(c1) == Pattern.getClass(c0)) {
                return new String[]{input};
            }
            String[] result = new String[2];
            result[0] = input;
            StringBuilder sb = new StringBuilder(2);
            sb.appendCodePoint(c1);
            sb.appendCodePoint(c0);
            result[1] = sb.toString();
            return result;
        }
        int nCodePoints = Pattern.countCodePoints(input);
        if (nCodePoints > 12) {
            throw new OutOfMemoryError("Pattern too complex");
        }
        int length = 1;
        for (int x = 2; x <= nCodePoints; ++x) {
            length *= x;
        }
        String[] temp = new String[length];
        int[] combClass = new int[nCodePoints];
        int i = 0;
        for (int x = 0; x < nCodePoints; ++x) {
            int c = Character.codePointAt(input, i);
            combClass[x] = Pattern.getClass(c);
            i += Character.charCount(c);
        }
        int index = 0;
        int x = 0;
        int offset = 0;
        while (x < nCodePoints) {
            int len;
            block10: {
                len = Pattern.countChars(input, offset, 1);
                for (int y = x - 1; y >= 0; --y) {
                    if (combClass[y] != combClass[x]) {
                        continue;
                    }
                    break block10;
                }
                StringBuilder sb = new StringBuilder(input);
                String otherChars = sb.delete(offset, offset + len).toString();
                String[] subResult = Pattern.producePermutations(otherChars);
                String prefix = input.substring(offset, offset + len);
                for (String sre : subResult) {
                    temp[index++] = prefix + sre;
                }
            }
            ++x;
            offset += len;
        }
        String[] result = new String[index];
        System.arraycopy(temp, 0, result, 0, index);
        return result;
    }

    private static int getClass(int c) {
        return Normalizer.getCombiningClass(c);
    }

    private static String composeOneStep(String input) {
        int len = Pattern.countChars(input, 0, 2);
        String firstTwoCharacters = input.substring(0, len);
        String result = java.text.Normalizer.normalize(firstTwoCharacters, Normalizer.Form.NFC);
        if (result.equals(firstTwoCharacters)) {
            return null;
        }
        String remainder = input.substring(len);
        return result + remainder;
    }

    private void RemoveQEQuoting() {
        int newTempLen;
        int pLen = this.patternLength;
        int i = 0;
        while (i < pLen - 1) {
            if (this.temp[i] != 92) {
                ++i;
                continue;
            }
            if (this.temp[i + 1] == 81) break;
            i += 2;
        }
        if (i >= pLen - 1) {
            return;
        }
        int j = i;
        i += 2;
        try {
            newTempLen = Math.addExact(j + 2, Math.multiplyExact(3, pLen - i));
        }
        catch (ArithmeticException ae) {
            throw new OutOfMemoryError("Required pattern length too large");
        }
        int[] newtemp = new int[newTempLen];
        System.arraycopy(this.temp, 0, newtemp, 0, j);
        boolean inQuote = true;
        boolean beginQuote = true;
        while (i < pLen) {
            int c;
            if (!ASCII.isAscii(c = this.temp[i++]) || ASCII.isAlpha(c)) {
                newtemp[j++] = c;
            } else if (ASCII.isDigit(c)) {
                if (beginQuote) {
                    newtemp[j++] = 92;
                    newtemp[j++] = 120;
                    newtemp[j++] = 51;
                }
                newtemp[j++] = c;
            } else if (c != 92) {
                if (inQuote) {
                    newtemp[j++] = 92;
                }
                newtemp[j++] = c;
            } else if (inQuote) {
                if (this.temp[i] == 69) {
                    ++i;
                    inQuote = false;
                } else {
                    newtemp[j++] = 92;
                    newtemp[j++] = 92;
                }
            } else {
                if (this.temp[i] == 81) {
                    ++i;
                    inQuote = true;
                    beginQuote = true;
                    continue;
                }
                newtemp[j++] = c;
                if (i != pLen) {
                    newtemp[j++] = this.temp[i++];
                }
            }
            beginQuote = false;
        }
        this.patternLength = j;
        this.temp = Arrays.copyOf(newtemp, j + 2);
    }

    private void compile() {
        int c;
        this.normalizedPattern = this.has(128) && !this.has(16) ? Pattern.normalize(this.pattern) : this.pattern;
        this.patternLength = this.normalizedPattern.length();
        this.temp = new int[this.patternLength + 2];
        this.hasSupplementary = false;
        int count = 0;
        for (int x = 0; x < this.patternLength; x += Character.charCount(c)) {
            c = this.normalizedPattern.codePointAt(x);
            if (Pattern.isSupplementary(c)) {
                this.hasSupplementary = true;
            }
            this.temp[count++] = c;
        }
        this.patternLength = count;
        if (!this.has(16)) {
            this.RemoveQEQuoting();
        }
        this.buffer = new int[32];
        this.groupNodes = new GroupHead[10];
        this.namedGroups = null;
        this.topClosureNodes = new ArrayList<Node>(10);
        if (this.has(16)) {
            this.matchRoot = this.newSlice(this.temp, this.patternLength, this.hasSupplementary);
            this.matchRoot.next = lastAccept;
        } else {
            this.matchRoot = this.expr(lastAccept);
            if (this.patternLength != this.cursor) {
                if (this.peek() == 41) {
                    throw this.error("Unmatched closing ')'");
                }
                if (this.cursor == this.patternLength + 1 && this.temp[this.patternLength - 1] == 92) {
                    throw this.error("Unescaped trailing backslash");
                }
                throw this.error("Unexpected internal error");
            }
        }
        if (this.matchRoot instanceof Slice) {
            this.root = BnM.optimize(this.matchRoot);
            if (this.root == this.matchRoot) {
                this.root = this.hasSupplementary ? new StartS(this.matchRoot) : new Start(this.matchRoot);
            }
        } else if (this.matchRoot instanceof Begin || this.matchRoot instanceof First) {
            this.root = this.matchRoot;
        } else {
            Node node = this.root = this.hasSupplementary ? new StartS(this.matchRoot) : new Start(this.matchRoot);
        }
        if (!this.hasGroupRef) {
            for (Node node : this.topClosureNodes) {
                if (!(node instanceof Loop)) continue;
                ++this.localTCNCount;
                ((Loop)node).posIndex = ((Loop)node).posIndex;
            }
        }
        this.temp = null;
        this.buffer = null;
        this.groupNodes = null;
        this.patternLength = 0;
        this.compiled = true;
        this.topClosureNodes = null;
    }

    private Map<String, Integer> namedGroupsMap() {
        Map<String, Integer> groups = this.namedGroups;
        if (groups == null) {
            this.namedGroups = groups = new HashMap<String, Integer>(2);
        }
        return groups;
    }

    public Map<String, Integer> namedGroups() {
        return Map.copyOf(this.namedGroupsMap());
    }

    private boolean has(int f) {
        return (this.flags0 & f) != 0;
    }

    private void accept(int ch, String s) {
        int testChar = this.temp[this.cursor++];
        if (this.has(4)) {
            testChar = this.parsePastWhitespace(testChar);
        }
        if (ch != testChar) {
            throw this.error(s);
        }
    }

    private void mark(int c) {
        this.temp[this.patternLength] = c;
    }

    private int peek() {
        int ch = this.temp[this.cursor];
        if (this.has(4)) {
            ch = this.peekPastWhitespace(ch);
        }
        return ch;
    }

    private int read() {
        int ch = this.temp[this.cursor++];
        if (this.has(4)) {
            ch = this.parsePastWhitespace(ch);
        }
        return ch;
    }

    private int readEscaped() {
        int ch = this.temp[this.cursor++];
        return ch;
    }

    private int next() {
        int ch = this.temp[++this.cursor];
        if (this.has(4)) {
            ch = this.peekPastWhitespace(ch);
        }
        return ch;
    }

    private int nextEscaped() {
        int ch = this.temp[++this.cursor];
        return ch;
    }

    private int peekPastWhitespace(int ch) {
        while (ASCII.isSpace(ch) || ch == 35) {
            while (ASCII.isSpace(ch)) {
                ch = this.temp[++this.cursor];
            }
            if (ch != 35) continue;
            ch = this.peekPastLine();
        }
        return ch;
    }

    private int parsePastWhitespace(int ch) {
        while (ASCII.isSpace(ch) || ch == 35) {
            while (ASCII.isSpace(ch)) {
                ch = this.temp[this.cursor++];
            }
            if (ch != 35) continue;
            ch = this.parsePastLine();
        }
        return ch;
    }

    private int parsePastLine() {
        int ch = this.temp[this.cursor++];
        while (ch != 0 && !this.isLineSeparator(ch)) {
            ch = this.temp[this.cursor++];
        }
        if (ch == 0 && this.cursor > this.patternLength) {
            this.cursor = this.patternLength;
            ch = this.temp[this.cursor++];
        }
        return ch;
    }

    private int peekPastLine() {
        int ch = this.temp[++this.cursor];
        while (ch != 0 && !this.isLineSeparator(ch)) {
            ch = this.temp[++this.cursor];
        }
        if (ch == 0 && this.cursor > this.patternLength) {
            this.cursor = this.patternLength;
            ch = this.temp[this.cursor];
        }
        return ch;
    }

    private boolean isLineSeparator(int ch) {
        if (this.has(1)) {
            return ch == 10;
        }
        return ch == 10 || ch == 13 || (ch | 1) == 8233 || ch == 133;
    }

    private int skip() {
        int i = this.cursor;
        int ch = this.temp[i + 1];
        this.cursor = i + 2;
        return ch;
    }

    private void unread() {
        --this.cursor;
    }

    private PatternSyntaxException error(String s) {
        return new PatternSyntaxException(s, this.normalizedPattern, this.cursor - 1);
    }

    private boolean findSupplementary(int start, int end) {
        for (int i = start; i < end; ++i) {
            if (!Pattern.isSupplementary(this.temp[i])) continue;
            return true;
        }
        return false;
    }

    private static final boolean isSupplementary(int ch) {
        return ch >= 65536 || Character.isSurrogate((char)ch);
    }

    private Node expr(Node end) {
        Node prev = null;
        Node firstTail = null;
        Branch branch = null;
        BranchConn branchConn = null;
        while (true) {
            Node node = this.sequence(end);
            Node nodeTail = this.root;
            if (prev == null) {
                prev = node;
                firstTail = nodeTail;
            } else {
                if (branchConn == null) {
                    branchConn = new BranchConn();
                    branchConn.next = end;
                }
                if (node == end) {
                    node = null;
                } else {
                    nodeTail.next = branchConn;
                }
                if (prev == branch) {
                    branch.add(node);
                } else {
                    if (prev == end) {
                        prev = null;
                    } else {
                        firstTail.next = branchConn;
                    }
                    branch = new Branch(prev, node, branchConn);
                    prev = branch;
                }
            }
            if (this.peek() != 124) {
                return prev;
            }
            this.next();
        }
    }

    private Node sequence(Node end) {
        Node head = null;
        Node tail = null;
        block12: while (true) {
            Node node;
            int ch = this.peek();
            switch (ch) {
                case 40: {
                    node = this.group0();
                    if (node == null) continue block12;
                    if (head == null) {
                        head = node;
                    } else {
                        tail.next = node;
                    }
                    tail = this.root;
                    continue block12;
                }
                case 91: {
                    if (this.has(128) && !this.has(16)) {
                        node = new NFCCharProperty(this.clazz(true));
                        break;
                    }
                    node = this.newCharProperty(this.clazz(true));
                    break;
                }
                case 92: {
                    ch = this.nextEscaped();
                    if (ch == 112 || ch == 80) {
                        boolean oneLetter = true;
                        boolean comp = ch == 80;
                        ch = this.next();
                        if (ch != 123) {
                            this.unread();
                        } else {
                            oneLetter = false;
                        }
                        if (this.has(128) && !this.has(16)) {
                            node = new NFCCharProperty(this.family(oneLetter, comp));
                            break;
                        }
                        node = this.newCharProperty(this.family(oneLetter, comp));
                        break;
                    }
                    this.unread();
                    node = this.atom();
                    break;
                }
                case 94: {
                    this.next();
                    if (this.has(8)) {
                        if (this.has(1)) {
                            node = new UnixCaret();
                            break;
                        }
                        node = new Caret();
                        break;
                    }
                    node = new Begin();
                    break;
                }
                case 36: {
                    this.next();
                    if (this.has(1)) {
                        node = new UnixDollar(this.has(8));
                        break;
                    }
                    node = new Dollar(this.has(8));
                    break;
                }
                case 46: {
                    this.next();
                    if (this.has(32)) {
                        node = new CharProperty(Pattern.ALL());
                        break;
                    }
                    if (this.has(1)) {
                        node = new CharProperty(Pattern.UNIXDOT());
                        break;
                    }
                    node = new CharProperty(Pattern.DOT());
                    break;
                }
                case 41: 
                case 124: {
                    break block12;
                }
                case 93: 
                case 125: {
                    node = this.atom();
                    break;
                }
                case 42: 
                case 43: 
                case 63: {
                    this.next();
                    throw this.error("Dangling meta character '" + (char)ch + "'");
                }
                case 0: {
                    if (this.cursor >= this.patternLength) break block12;
                }
                default: {
                    node = this.atom();
                }
            }
            node = this.closure(node);
            if (head == null) {
                head = tail = node;
                continue;
            }
            tail.next = node;
            tail = node;
        }
        if (head == null) {
            return end;
        }
        tail.next = end;
        this.root = tail;
        return head;
    }

    private Node atom() {
        int first = 0;
        int prev = -1;
        boolean hasSupplementary = false;
        int ch = this.peek();
        block6: while (true) {
            switch (ch) {
                case 42: 
                case 43: 
                case 63: 
                case 123: {
                    if (first <= true) break block6;
                    this.cursor = prev;
                    --first;
                    break block6;
                }
                case 36: 
                case 40: 
                case 41: 
                case 46: 
                case 91: 
                case 94: 
                case 124: {
                    break block6;
                }
                case 92: {
                    ch = this.nextEscaped();
                    if (ch == 112 || ch == 80) {
                        if (first > 0) {
                            this.unread();
                            break block6;
                        }
                        boolean comp = ch == 80;
                        boolean oneLetter = true;
                        ch = this.next();
                        if (ch != 123) {
                            this.unread();
                        } else {
                            oneLetter = false;
                        }
                        if (this.has(128) && !this.has(16)) {
                            return new NFCCharProperty(this.family(oneLetter, comp));
                        }
                        return this.newCharProperty(this.family(oneLetter, comp));
                    }
                    this.unread();
                    prev = this.cursor;
                    ch = this.escape(false, first == 0, false);
                    if (ch >= 0) {
                        this.append(ch, first);
                        ++first;
                        if (Pattern.isSupplementary(ch)) {
                            hasSupplementary = true;
                        }
                        ch = this.peek();
                        continue block6;
                    }
                    if (first == 0) {
                        return this.root;
                    }
                    this.cursor = prev;
                    break block6;
                }
                case 0: {
                    if (this.cursor >= this.patternLength) break block6;
                }
                default: {
                    prev = this.cursor;
                    this.append(ch, first);
                    ++first;
                    if (Pattern.isSupplementary(ch)) {
                        hasSupplementary = true;
                    }
                    ch = this.next();
                    continue block6;
                }
            }
            break;
        }
        if (first == 1) {
            return this.newCharProperty(this.single(this.buffer[0]));
        }
        return this.newSlice(this.buffer, first, hasSupplementary);
    }

    private void append(int ch, int index) {
        int len = this.buffer.length;
        if (index - len >= 0) {
            len = ArraysSupport.newLength(len, 1 + index - len, len);
            this.buffer = Arrays.copyOf(this.buffer, len);
        }
        this.buffer[index] = ch;
    }

    private Node ref(int refNum) {
        boolean done = false;
        block3: while (!done) {
            int ch = this.peek();
            switch (ch) {
                case 48: 
                case 49: 
                case 50: 
                case 51: 
                case 52: 
                case 53: 
                case 54: 
                case 55: 
                case 56: 
                case 57: {
                    int newRefNum = refNum * 10 + (ch - 48);
                    if (this.capturingGroupCount - 1 < newRefNum) {
                        done = true;
                        continue block3;
                    }
                    refNum = newRefNum;
                    this.read();
                    continue block3;
                }
            }
            done = true;
        }
        this.hasGroupRef = true;
        if (this.has(2)) {
            return new CIBackRef(refNum, this.has(64));
        }
        return new BackRef(refNum);
    }

    private int escape(boolean inclass, boolean create, boolean isrange) {
        int ch = this.skip();
        switch (ch) {
            case 48: {
                return this.o();
            }
            case 49: 
            case 50: 
            case 51: 
            case 52: 
            case 53: 
            case 54: 
            case 55: 
            case 56: 
            case 57: {
                if (inclass) break;
                if (create) {
                    this.root = this.ref(ch - 48);
                }
                return -1;
            }
            case 65: {
                if (inclass) break;
                if (create) {
                    this.root = new Begin();
                }
                return -1;
            }
            case 66: {
                if (inclass) break;
                if (create) {
                    this.root = new Bound(Bound.NONE, this.has(256));
                }
                return -1;
            }
            case 67: {
                break;
            }
            case 68: {
                if (create) {
                    this.predicate = this.has(256) ? CharPredicates.DIGIT() : CharPredicates.ASCII_DIGIT();
                    this.predicate = this.predicate.negate();
                    if (!inclass) {
                        this.root = this.newCharProperty(this.predicate);
                    }
                }
                return -1;
            }
            case 69: 
            case 70: {
                break;
            }
            case 71: {
                if (inclass) break;
                if (create) {
                    this.root = new LastMatch();
                }
                return -1;
            }
            case 72: {
                if (create) {
                    this.predicate = Pattern.HorizWS().negate();
                    if (!inclass) {
                        this.root = this.newCharProperty(this.predicate);
                    }
                }
                return -1;
            }
            case 73: 
            case 74: 
            case 75: 
            case 76: 
            case 77: {
                break;
            }
            case 78: {
                return this.N();
            }
            case 79: 
            case 80: 
            case 81: {
                break;
            }
            case 82: {
                if (inclass) break;
                if (create) {
                    this.root = new LineEnding();
                }
                return -1;
            }
            case 83: {
                if (create) {
                    this.predicate = this.has(256) ? CharPredicates.WHITE_SPACE() : CharPredicates.ASCII_SPACE();
                    this.predicate = this.predicate.negate();
                    if (!inclass) {
                        this.root = this.newCharProperty(this.predicate);
                    }
                }
                return -1;
            }
            case 84: 
            case 85: {
                break;
            }
            case 86: {
                if (create) {
                    this.predicate = Pattern.VertWS().negate();
                    if (!inclass) {
                        this.root = this.newCharProperty(this.predicate);
                    }
                }
                return -1;
            }
            case 87: {
                if (create) {
                    this.predicate = this.has(256) ? CharPredicates.WORD() : CharPredicates.ASCII_WORD();
                    this.predicate = this.predicate.negate();
                    if (!inclass) {
                        this.root = this.newCharProperty(this.predicate);
                    }
                }
                return -1;
            }
            case 88: {
                if (inclass) break;
                if (create) {
                    this.root = new XGrapheme();
                }
                return -1;
            }
            case 89: {
                break;
            }
            case 90: {
                if (inclass) break;
                if (create) {
                    this.root = this.has(1) ? new UnixDollar(false) : new Dollar(false);
                }
                return -1;
            }
            case 97: {
                return 7;
            }
            case 98: {
                if (inclass) break;
                if (create) {
                    if (this.peek() == 123) {
                        if (this.skip() == 103) {
                            if (this.read() != 125) break;
                            this.root = new GraphemeBound();
                            return -1;
                        }
                        this.unread();
                        this.unread();
                    }
                    this.root = new Bound(Bound.BOTH, this.has(256));
                }
                return -1;
            }
            case 99: {
                return this.c();
            }
            case 100: {
                if (create) {
                    CharPredicate charPredicate = this.predicate = this.has(256) ? CharPredicates.DIGIT() : CharPredicates.ASCII_DIGIT();
                    if (!inclass) {
                        this.root = this.newCharProperty(this.predicate);
                    }
                }
                return -1;
            }
            case 101: {
                return 27;
            }
            case 102: {
                return 12;
            }
            case 103: {
                break;
            }
            case 104: {
                if (create) {
                    this.predicate = Pattern.HorizWS();
                    if (!inclass) {
                        this.root = this.newCharProperty(this.predicate);
                    }
                }
                return -1;
            }
            case 105: 
            case 106: {
                break;
            }
            case 107: {
                if (inclass) break;
                if (this.read() != 60) {
                    throw this.error("\\k is not followed by '<' for named capturing group");
                }
                String name = this.groupname(this.read());
                Integer number = this.namedGroupsMap().get(name);
                if (number == null) {
                    throw this.error("named capturing group <" + name + "> does not exist");
                }
                if (create) {
                    this.hasGroupRef = true;
                    this.root = this.has(2) ? new CIBackRef(number, this.has(64)) : new BackRef(number);
                }
                return -1;
            }
            case 108: 
            case 109: {
                break;
            }
            case 110: {
                return 10;
            }
            case 111: 
            case 112: 
            case 113: {
                break;
            }
            case 114: {
                return 13;
            }
            case 115: {
                if (create) {
                    CharPredicate charPredicate = this.predicate = this.has(256) ? CharPredicates.WHITE_SPACE() : CharPredicates.ASCII_SPACE();
                    if (!inclass) {
                        this.root = this.newCharProperty(this.predicate);
                    }
                }
                return -1;
            }
            case 116: {
                return 9;
            }
            case 117: {
                return this.u();
            }
            case 118: {
                if (isrange) {
                    return 11;
                }
                if (create) {
                    this.predicate = Pattern.VertWS();
                    if (!inclass) {
                        this.root = this.newCharProperty(this.predicate);
                    }
                }
                return -1;
            }
            case 119: {
                if (create) {
                    CharPredicate charPredicate = this.predicate = this.has(256) ? CharPredicates.WORD() : CharPredicates.ASCII_WORD();
                    if (!inclass) {
                        this.root = this.newCharProperty(this.predicate);
                    }
                }
                return -1;
            }
            case 120: {
                return this.x();
            }
            case 121: {
                break;
            }
            case 122: {
                if (inclass) break;
                if (create) {
                    this.root = new End();
                }
                return -1;
            }
            default: {
                return ch;
            }
        }
        throw this.error("Illegal/unsupported escape sequence");
    }

    private CharPredicate clazz(boolean consume) {
        CharPredicate prev = null;
        CharPredicate curr = null;
        BitClass bits = new BitClass();
        boolean isNeg = false;
        boolean hasBits = false;
        int ch = this.next();
        if (ch == 94 && this.temp[this.cursor - 1] == 91) {
            ch = this.next();
            isNeg = true;
        }
        block6: while (true) {
            switch (ch) {
                case 91: {
                    curr = this.clazz(true);
                    prev = prev == null ? curr : prev.union(curr);
                    ch = this.peek();
                    continue block6;
                }
                case 38: {
                    ch = this.next();
                    if (ch == 38) {
                        ch = this.next();
                        CharPredicate right = null;
                        while (ch != 93 && ch != 38) {
                            if (ch == 91) {
                                right = right == null ? this.clazz(true) : right.union(this.clazz(true));
                            } else {
                                this.unread();
                                right = right == null ? this.clazz(false) : right.union(this.clazz(false));
                            }
                            ch = this.peek();
                        }
                        if (hasBits) {
                            prev = prev == null ? (curr = bits) : prev.union(bits);
                            hasBits = false;
                        }
                        if (right != null) {
                            curr = right;
                        }
                        if (prev == null) {
                            if (right == null) {
                                throw this.error("Bad class syntax");
                            }
                            prev = right;
                            continue block6;
                        }
                        if (curr == null) {
                            throw this.error("Bad intersection syntax");
                        }
                        prev = prev.and(curr);
                        continue block6;
                    }
                    this.unread();
                    break;
                }
                case 0: {
                    if (this.cursor < this.patternLength) break;
                    throw this.error("Unclosed character class");
                }
                case 93: {
                    if (prev == null && !hasBits) break;
                    if (consume) {
                        this.next();
                    }
                    if (prev == null) {
                        prev = bits;
                    } else if (hasBits) {
                        prev = prev.union(bits);
                    }
                    if (isNeg) {
                        return prev.negate();
                    }
                    return prev;
                }
            }
            curr = this.range(bits);
            if (curr == null) {
                hasBits = true;
            } else if (prev == null) {
                prev = curr;
            } else if (prev != curr) {
                prev = prev.union(curr);
            }
            ch = this.peek();
        }
    }

    private CharPredicate bitsOrSingle(BitClass bits, int ch) {
        if (ch < 256 && (!this.has(2) || !this.has(64) || ch != 255 && ch != 181 && ch != 73 && ch != 105 && ch != 83 && ch != 115 && ch != 75 && ch != 107 && ch != 197 && ch != 229)) {
            bits.add(ch, this.flags0);
            return null;
        }
        return this.single(ch);
    }

    private CharPredicate single(int ch) {
        if (this.has(2)) {
            int upper;
            int lower;
            if (this.has(64)) {
                int lower2;
                int upper2 = Character.toUpperCase(ch);
                if (upper2 != (lower2 = Character.toLowerCase(upper2))) {
                    return Pattern.SingleU(lower2);
                }
            } else if (ASCII.isAscii(ch) && (lower = ASCII.toLower(ch)) != (upper = ASCII.toUpper(ch))) {
                return Pattern.SingleI(lower, upper);
            }
        }
        if (Pattern.isSupplementary(ch)) {
            return Pattern.SingleS(ch);
        }
        return Pattern.Single(ch);
    }

    private CharPredicate range(BitClass bits) {
        int ch = this.peek();
        if (ch == 92) {
            ch = this.nextEscaped();
            if (ch == 112 || ch == 80) {
                boolean comp = ch == 80;
                boolean oneLetter = true;
                ch = this.next();
                if (ch != 123) {
                    this.unread();
                } else {
                    oneLetter = false;
                }
                return this.family(oneLetter, comp);
            }
            boolean isrange = this.temp[this.cursor + 1] == 45;
            this.unread();
            ch = this.escape(true, true, isrange);
            if (ch == -1) {
                return this.predicate;
            }
        } else {
            this.next();
        }
        if (ch >= 0) {
            if (this.peek() == 45) {
                int endRange = this.temp[this.cursor + 1];
                if (endRange == 91) {
                    return this.bitsOrSingle(bits, ch);
                }
                if (endRange != 93) {
                    this.next();
                    int m = this.peek();
                    if (m == 92) {
                        m = this.escape(true, false, true);
                    } else {
                        this.next();
                    }
                    if (m < ch) {
                        throw this.error("Illegal character range");
                    }
                    if (this.has(2)) {
                        if (this.has(64)) {
                            return Pattern.CIRangeU(ch, m);
                        }
                        return Pattern.CIRange(ch, m);
                    }
                    return Pattern.Range(ch, m);
                }
            }
            return this.bitsOrSingle(bits, ch);
        }
        throw this.error("Unexpected character '" + (char)ch + "'");
    }

    private CharPredicate family(boolean singleLetter, boolean isComplement) {
        int i;
        String name;
        this.next();
        CharPredicate p = null;
        if (singleLetter) {
            int c = this.temp[this.cursor];
            name = !Character.isSupplementaryCodePoint(c) ? String.valueOf((char)c) : new String(this.temp, this.cursor, 1);
            this.read();
        } else {
            i = this.cursor;
            this.mark(125);
            while (this.read() != 125) {
            }
            this.mark(0);
            int j = this.cursor;
            if (j > this.patternLength) {
                throw this.error("Unclosed character family");
            }
            if (i + 1 >= j) {
                throw this.error("Empty character family");
            }
            name = new String(this.temp, i, j - i - 1);
        }
        i = name.indexOf(61);
        if (i != -1) {
            String value = name.substring(i + 1);
            switch (name = name.substring(0, i).toLowerCase(Locale.ENGLISH)) {
                case "sc": 
                case "script": {
                    p = CharPredicates.forUnicodeScript(value);
                    break;
                }
                case "blk": 
                case "block": {
                    p = CharPredicates.forUnicodeBlock(value);
                    break;
                }
                case "gc": 
                case "general_category": {
                    p = CharPredicates.forProperty(value, this.has(2));
                    break;
                }
            }
            if (p == null) {
                throw this.error("Unknown Unicode property {name=<" + name + ">, value=<" + value + ">}");
            }
        } else {
            if (name.startsWith("In")) {
                p = CharPredicates.forUnicodeBlock(name.substring(2));
            } else if (name.startsWith("Is")) {
                String shortName = name.substring(2);
                p = CharPredicates.forUnicodeProperty(shortName, this.has(2));
                if (p == null) {
                    p = CharPredicates.forProperty(shortName, this.has(2));
                }
                if (p == null) {
                    p = CharPredicates.forUnicodeScript(shortName);
                }
            } else {
                if (this.has(256)) {
                    p = CharPredicates.forPOSIXName(name, this.has(2));
                }
                if (p == null) {
                    p = CharPredicates.forProperty(name, this.has(2));
                }
            }
            if (p == null) {
                throw this.error("Unknown character property name {" + name + "}");
            }
        }
        if (isComplement) {
            this.hasSupplementary = true;
            p = p.negate();
        }
        return p;
    }

    private CharProperty newCharProperty(CharPredicate p) {
        if (p == null) {
            return null;
        }
        if (p instanceof BmpCharPredicate) {
            return new BmpCharProperty((BmpCharPredicate)p);
        }
        this.hasSupplementary = true;
        return new CharProperty(p);
    }

    private String groupname(int ch) {
        StringBuilder sb = new StringBuilder();
        if (!ASCII.isAlpha(ch)) {
            throw this.error("capturing group name does not start with a Latin letter");
        }
        do {
            sb.append((char)ch);
        } while (ASCII.isAlnum(ch = this.read()));
        if (ch != 62) {
            throw this.error("named capturing group is missing trailing '>'");
        }
        return sb.toString();
    }

    private Node group0() {
        Node tail;
        Node head;
        int saveTCNCount;
        int save;
        boolean capturingGroup;
        block28: {
            block27: {
                capturingGroup = false;
                save = this.flags0;
                saveTCNCount = this.topClosureNodes.size();
                this.root = null;
                int ch = this.next();
                if (ch != 63) break block27;
                ch = this.skip();
                switch (ch) {
                    case 58: {
                        head = this.createGroup(true);
                        tail = this.root;
                        head.next = this.expr(tail);
                        break;
                    }
                    case 33: 
                    case 61: {
                        head = this.createGroup(true);
                        tail = this.root;
                        head.next = this.expr(tail);
                        if (ch == 61) {
                            head = tail = new Pos(head);
                            break;
                        }
                        head = tail = new Neg(head);
                        break;
                    }
                    case 62: {
                        head = this.createGroup(true);
                        tail = this.root;
                        head.next = this.expr(tail);
                        head = tail = new Ques(head, Qtype.INDEPENDENT);
                        break;
                    }
                    case 60: {
                        ch = this.read();
                        if (ch != 61 && ch != 33) {
                            String name = this.groupname(ch);
                            if (this.namedGroupsMap().containsKey(name)) {
                                throw this.error("Named capturing group <" + name + "> is already defined");
                            }
                            capturingGroup = true;
                            head = this.createGroup(false);
                            tail = this.root;
                            this.namedGroupsMap().put(name, this.capturingGroupCount - 1);
                            head.next = this.expr(tail);
                            break;
                        }
                        int start = this.cursor;
                        head = this.createGroup(true);
                        tail = this.root;
                        head.next = this.expr(tail);
                        tail.next = LookBehindEndNode.INSTANCE;
                        TreeInfo info = new TreeInfo();
                        head.study(info);
                        if (!info.maxValid) {
                            throw this.error("Look-behind group does not have an obvious maximum length");
                        }
                        boolean hasSupplementary = this.findSupplementary(start, this.patternLength);
                        if (ch == 61) {
                            tail = hasSupplementary ? new BehindS(head, info.maxLength, info.minLength) : new Behind(head, info.maxLength, info.minLength);
                            head = tail;
                        } else {
                            tail = hasSupplementary ? new NotBehindS(head, info.maxLength, info.minLength) : new NotBehind(head, info.maxLength, info.minLength);
                            head = tail;
                        }
                        if (saveTCNCount < this.topClosureNodes.size()) {
                            this.topClosureNodes.subList(saveTCNCount, this.topClosureNodes.size()).clear();
                            break;
                        }
                        break block28;
                    }
                    case 36: 
                    case 64: {
                        throw this.error("Unknown group type");
                    }
                    default: {
                        this.unread();
                        this.addFlag();
                        ch = this.read();
                        if (ch == 41) {
                            return null;
                        }
                        if (ch != 58) {
                            throw this.error("Unknown inline modifier");
                        }
                        head = this.createGroup(true);
                        tail = this.root;
                        head.next = this.expr(tail);
                        break;
                    }
                }
                break block28;
            }
            capturingGroup = true;
            head = this.createGroup(false);
            tail = this.root;
            head.next = this.expr(tail);
        }
        this.accept(41, "Unclosed group");
        this.flags0 = save;
        Node node = this.closure(head);
        if (node == head) {
            this.root = tail;
            return node;
        }
        if (head == tail) {
            this.root = node;
            return node;
        }
        if (saveTCNCount < this.topClosureNodes.size()) {
            this.topClosureNodes.subList(saveTCNCount, this.topClosureNodes.size()).clear();
        }
        if (node instanceof Ques) {
            Ques ques = (Ques)node;
            if (ques.type == Qtype.POSSESSIVE) {
                this.root = node;
                return node;
            }
            tail = tail.next = new BranchConn();
            head = ques.type == Qtype.GREEDY ? new Branch(head, null, tail) : new Branch(null, head, tail);
            this.root = tail;
            return head;
        }
        if (node instanceof Curly) {
            Loop loop;
            Curly curly = (Curly)node;
            if (curly.type == Qtype.POSSESSIVE) {
                this.root = node;
                return node;
            }
            TreeInfo info = new TreeInfo();
            if (head.study(info)) {
                GroupTail temp = (GroupTail)tail;
                head = this.root = new GroupCurly(head.next, curly.cmin, curly.cmax, curly.type, ((GroupTail)tail).localIndex, ((GroupTail)tail).groupIndex, capturingGroup);
                return head;
            }
            int temp = ((GroupHead)head).localIndex;
            if (curly.type == Qtype.GREEDY) {
                loop = new Loop(this.localCount, temp);
                if (curly.cmax == Integer.MAX_VALUE) {
                    this.topClosureNodes.add(loop);
                }
            } else {
                loop = new LazyLoop(this.localCount, temp);
            }
            Prolog prolog = new Prolog(loop);
            ++this.localCount;
            loop.cmin = curly.cmin;
            loop.cmax = curly.cmax;
            loop.body = head;
            tail.next = loop;
            this.root = loop;
            return prolog;
        }
        throw this.error("Internal logic error");
    }

    private Node createGroup(boolean anonymous) {
        int localIndex = this.localCount++;
        int groupIndex = 0;
        if (!anonymous) {
            groupIndex = this.capturingGroupCount++;
        }
        GroupHead head = new GroupHead(localIndex);
        this.root = new GroupTail(localIndex, groupIndex);
        head.tail = (GroupTail)this.root;
        if (!anonymous && groupIndex < 10) {
            this.groupNodes[groupIndex] = head;
        }
        return head;
    }

    private void addFlag() {
        int ch = this.peek();
        while (true) {
            switch (ch) {
                case 105: {
                    this.flags0 |= 2;
                    break;
                }
                case 109: {
                    this.flags0 |= 8;
                    break;
                }
                case 115: {
                    this.flags0 |= 0x20;
                    break;
                }
                case 100: {
                    this.flags0 |= 1;
                    break;
                }
                case 117: {
                    this.flags0 |= 0x40;
                    break;
                }
                case 99: {
                    this.flags0 |= 0x80;
                    break;
                }
                case 120: {
                    this.flags0 |= 4;
                    break;
                }
                case 85: {
                    this.flags0 |= 0x140;
                    break;
                }
                case 45: {
                    ch = this.next();
                    this.subFlag();
                }
                default: {
                    return;
                }
            }
            ch = this.next();
        }
    }

    private void subFlag() {
        int ch = this.peek();
        while (true) {
            switch (ch) {
                case 105: {
                    this.flags0 &= 0xFFFFFFFD;
                    break;
                }
                case 109: {
                    this.flags0 &= 0xFFFFFFF7;
                    break;
                }
                case 115: {
                    this.flags0 &= 0xFFFFFFDF;
                    break;
                }
                case 100: {
                    this.flags0 &= 0xFFFFFFFE;
                    break;
                }
                case 117: {
                    this.flags0 &= 0xFFFFFFBF;
                    break;
                }
                case 99: {
                    this.flags0 &= 0xFFFFFF7F;
                    break;
                }
                case 120: {
                    this.flags0 &= 0xFFFFFFFB;
                    break;
                }
                case 85: {
                    this.flags0 &= 0xFFFFFEBF;
                    break;
                }
                default: {
                    return;
                }
            }
            ch = this.next();
        }
    }

    private Qtype qtype() {
        int ch = this.next();
        if (ch == 63) {
            this.next();
            return Qtype.LAZY;
        }
        if (ch == 43) {
            this.next();
            return Qtype.POSSESSIVE;
        }
        return Qtype.GREEDY;
    }

    private Node curly(Node prev, int cmin) {
        Qtype qtype = this.qtype();
        if (qtype == Qtype.GREEDY) {
            if (prev instanceof BmpCharProperty) {
                return new BmpCharPropertyGreedy((BmpCharProperty)prev, cmin);
            }
            if (prev instanceof CharProperty) {
                return new CharPropertyGreedy((CharProperty)prev, cmin);
            }
        }
        return new Curly(prev, cmin, Integer.MAX_VALUE, qtype);
    }

    private Node closure(Node prev) {
        int ch = this.peek();
        switch (ch) {
            case 63: {
                return new Ques(prev, this.qtype());
            }
            case 42: {
                return this.curly(prev, 0);
            }
            case 43: {
                return this.curly(prev, 1);
            }
            case 123: {
                ch = this.skip();
                if (ASCII.isDigit(ch)) {
                    int cmax;
                    int cmin = 0;
                    try {
                        do {
                            cmin = Math.addExact(Math.multiplyExact(cmin, 10), ch - 48);
                        } while (ASCII.isDigit(ch = this.read()));
                        if (ch == 44) {
                            ch = this.read();
                            if (ch == 125) {
                                this.unread();
                                return this.curly(prev, cmin);
                            }
                            cmax = 0;
                            while (ASCII.isDigit(ch)) {
                                cmax = Math.addExact(Math.multiplyExact(cmax, 10), ch - 48);
                                ch = this.read();
                            }
                        } else {
                            cmax = cmin;
                        }
                    }
                    catch (ArithmeticException ae) {
                        throw this.error("Illegal repetition range");
                    }
                    if (ch != 125) {
                        throw this.error("Unclosed counted closure");
                    }
                    if (cmax < cmin) {
                        throw this.error("Illegal repetition range");
                    }
                    this.unread();
                    return cmin == 0 && cmax == 1 ? new Ques(prev, this.qtype()) : new Curly(prev, cmin, cmax, this.qtype());
                }
                throw this.error("Illegal repetition");
            }
        }
        return prev;
    }

    private int c() {
        if (this.cursor < this.patternLength) {
            return this.read() ^ 0x40;
        }
        throw this.error("Illegal control escape sequence");
    }

    private int o() {
        int n = this.read();
        if ((n - 48 | 55 - n) >= 0) {
            int m = this.read();
            if ((m - 48 | 55 - m) >= 0) {
                int o = this.read();
                if ((o - 48 | 55 - o) >= 0 && (n - 48 | 51 - n) >= 0) {
                    return (n - 48) * 64 + (m - 48) * 8 + (o - 48);
                }
                this.unread();
                return (n - 48) * 8 + (m - 48);
            }
            this.unread();
            return n - 48;
        }
        throw this.error("Illegal octal escape sequence");
    }

    private int x() {
        int n = this.read();
        if (ASCII.isHexDigit(n)) {
            int m = this.read();
            if (ASCII.isHexDigit(m)) {
                return ASCII.toDigit(n) * 16 + ASCII.toDigit(m);
            }
        } else if (n == 123 && ASCII.isHexDigit(this.peek())) {
            int ch = 0;
            while (ASCII.isHexDigit(n = this.read())) {
                if ((ch = (ch << 4) + ASCII.toDigit(n)) <= 0x10FFFF) continue;
                throw this.error("Hexadecimal codepoint is too big");
            }
            if (n != 125) {
                throw this.error("Unclosed hexadecimal escape sequence");
            }
            return ch;
        }
        throw this.error("Illegal hexadecimal escape sequence");
    }

    private int cursor() {
        return this.cursor;
    }

    private void setcursor(int pos) {
        this.cursor = pos;
    }

    private int uxxxx() {
        int n = 0;
        for (int i = 0; i < 4; ++i) {
            int ch = this.read();
            if (!ASCII.isHexDigit(ch)) {
                throw this.error("Illegal Unicode escape sequence");
            }
            n = n * 16 + ASCII.toDigit(ch);
        }
        return n;
    }

    private int u() {
        int n = this.uxxxx();
        if (Character.isHighSurrogate((char)n)) {
            int n2;
            int cur = this.cursor();
            if (this.read() == 92 && this.read() == 117 && Character.isLowSurrogate((char)(n2 = this.uxxxx()))) {
                return Character.toCodePoint((char)n, (char)n2);
            }
            this.setcursor(cur);
        }
        return n;
    }

    private int N() {
        if (this.read() == 123) {
            int i = this.cursor;
            while (this.read() != 125) {
                if (this.cursor < this.patternLength) continue;
                throw this.error("Unclosed character name escape sequence");
            }
            String name = new String(this.temp, i, this.cursor - i - 1);
            try {
                return Character.codePointOf(name);
            }
            catch (IllegalArgumentException x) {
                throw this.error("Unknown character name [" + name + "]");
            }
        }
        throw this.error("Illegal character name escape sequence");
    }

    private static final int countChars(CharSequence seq, int index, int lengthInCodePoints) {
        if (lengthInCodePoints == 1 && index >= 0 && index < seq.length() && !Character.isHighSurrogate(seq.charAt(index))) {
            return 1;
        }
        int length = seq.length();
        int x = index;
        if (lengthInCodePoints >= 0) {
            assert (length == 0 && index == 0 || index >= 0 && index < length);
            for (int i = 0; x < length && i < lengthInCodePoints; ++i) {
                if (!Character.isHighSurrogate(seq.charAt(x++)) || x >= length || !Character.isLowSurrogate(seq.charAt(x))) continue;
                ++x;
            }
            return x - index;
        }
        assert (index >= 0 && index <= length);
        if (index == 0) {
            return 0;
        }
        int len = -lengthInCodePoints;
        for (int i = 0; x > 0 && i < len; ++i) {
            if (!Character.isLowSurrogate(seq.charAt(--x)) || x <= 0 || !Character.isHighSurrogate(seq.charAt(x - 1))) continue;
            --x;
        }
        return index - x;
    }

    private static final int countCodePoints(CharSequence seq) {
        int length = seq.length();
        int n = 0;
        int i = 0;
        while (i < length) {
            ++n;
            if (!Character.isHighSurrogate(seq.charAt(i++)) || i >= length || !Character.isLowSurrogate(seq.charAt(i))) continue;
            ++i;
        }
        return n;
    }

    private Node newSlice(int[] buf, int count, boolean hasSupplementary) {
        int[] tmp = new int[count];
        if (this.has(2)) {
            if (this.has(64)) {
                for (int i = 0; i < count; ++i) {
                    tmp[i] = Character.toLowerCase(Character.toUpperCase(buf[i]));
                }
                return hasSupplementary ? new SliceUS(tmp) : new SliceU(tmp);
            }
            for (int i = 0; i < count; ++i) {
                tmp[i] = ASCII.toLower(buf[i]);
            }
            return hasSupplementary ? new SliceIS(tmp) : new SliceI(tmp);
        }
        for (int i = 0; i < count; ++i) {
            tmp[i] = buf[i];
        }
        return hasSupplementary ? new SliceS(tmp) : new Slice(tmp);
    }

    private static boolean hasBaseCharacter(Matcher matcher, int i, CharSequence seq) {
        int start = !matcher.transparentBounds ? matcher.from : 0;
        for (int x = i; x >= start; --x) {
            int ch = Character.codePointAt(seq, x);
            if (Character.isLetterOrDigit(ch)) {
                return true;
            }
            if (Character.getType(ch) == 6) continue;
            return false;
        }
        return false;
    }

    private static CharPredicate and(CharPredicate p1, CharPredicate p2, boolean bmpChar) {
        if (bmpChar) {
            return ch -> p1.is(ch) && p2.is(ch);
        }
        return ch -> p1.is(ch) && p2.is(ch);
    }

    private static CharPredicate union(CharPredicate p1, CharPredicate p2, boolean bmpChar) {
        if (bmpChar) {
            return ch -> p1.is(ch) || p2.is(ch);
        }
        return ch -> p1.is(ch) || p2.is(ch);
    }

    private static CharPredicate union(CharPredicate p1, CharPredicate p2, CharPredicate p3, boolean bmpChar) {
        if (bmpChar) {
            return ch -> p1.is(ch) || p2.is(ch) || p3.is(ch);
        }
        return ch -> p1.is(ch) || p2.is(ch) || p3.is(ch);
    }

    private static CharPredicate negate(CharPredicate p1) {
        return ch -> !p1.is(ch);
    }

    static BmpCharPredicate VertWS() {
        return cp -> cp >= 10 && cp <= 13 || cp == 133 || cp == 8232 || cp == 8233;
    }

    static BmpCharPredicate HorizWS() {
        return cp -> cp == 9 || cp == 32 || cp == 160 || cp == 5760 || cp == 6158 || cp >= 8192 && cp <= 8202 || cp == 8239 || cp == 8287 || cp == 12288;
    }

    static CharPredicate ALL() {
        return ch -> true;
    }

    static CharPredicate DOT() {
        return ch -> ch != 10 && ch != 13 && (ch | 1) != 8233 && ch != 133;
    }

    static CharPredicate UNIXDOT() {
        return ch -> ch != 10;
    }

    static CharPredicate SingleS(int c) {
        return ch -> ch == c;
    }

    static BmpCharPredicate Single(int c) {
        return ch -> ch == c;
    }

    static BmpCharPredicate SingleI(int lower, int upper) {
        return ch -> ch == lower || ch == upper;
    }

    static CharPredicate SingleU(int lower) {
        return ch -> lower == ch || lower == Character.toLowerCase(Character.toUpperCase(ch));
    }

    private static boolean inRange(int lower, int ch, int upper) {
        return lower <= ch && ch <= upper;
    }

    static CharPredicate Range(int lower, int upper) {
        if (upper < 55296 || lower > 57343 && upper < 65536) {
            return ch -> Pattern.inRange(lower, ch, upper);
        }
        return ch -> Pattern.inRange(lower, ch, upper);
    }

    static CharPredicate CIRange(int lower, int upper) {
        return ch -> Pattern.inRange(lower, ch, upper) || ASCII.isAscii(ch) && (Pattern.inRange(lower, ASCII.toUpper(ch), upper) || Pattern.inRange(lower, ASCII.toLower(ch), upper));
    }

    static CharPredicate CIRangeU(int lower, int upper) {
        return ch -> {
            if (Pattern.inRange(lower, ch, upper)) {
                return true;
            }
            int up = Character.toUpperCase(ch);
            return Pattern.inRange(lower, up, upper) || Pattern.inRange(lower, Character.toLowerCase(up), upper);
        };
    }

    public Predicate<String> asPredicate() {
        return s -> this.matcher((CharSequence)s).find();
    }

    public Predicate<String> asMatchPredicate() {
        return s -> this.matcher((CharSequence)s).matches();
    }

    public Stream<String> splitAsStream(final CharSequence input) {
        class MatcherIterator
        implements Iterator<String> {
            private Matcher matcher;
            private int current;
            private String nextElement;
            private int emptyElementCount;
            final /* synthetic */ Pattern this$0;

            MatcherIterator() {
                this.this$0 = this$0;
            }

            @Override
            public String next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                if (this.emptyElementCount == 0) {
                    String n = this.nextElement;
                    this.nextElement = null;
                    return n;
                }
                --this.emptyElementCount;
                return "";
            }

            @Override
            public boolean hasNext() {
                if (this.matcher == null) {
                    this.matcher = this.this$0.matcher(input);
                    int n = this.emptyElementCount = input.length() == 0 ? 1 : 0;
                }
                if (this.nextElement != null || this.emptyElementCount > 0) {
                    return true;
                }
                if (this.current == input.length()) {
                    return false;
                }
                while (this.matcher.find()) {
                    this.nextElement = input.subSequence(this.current, this.matcher.start()).toString();
                    this.current = this.matcher.end();
                    if (!this.nextElement.isEmpty()) {
                        return true;
                    }
                    if (this.current <= 0) continue;
                    ++this.emptyElementCount;
                }
                this.nextElement = input.subSequence(this.current, input.length()).toString();
                this.current = input.length();
                if (!this.nextElement.isEmpty()) {
                    return true;
                }
                this.emptyElementCount = 0;
                this.nextElement = null;
                return false;
            }
        }
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new MatcherIterator(), 272), false);
    }

    static class Start
    extends Node {
        int minLength;

        Start(Node node) {
            this.next = node;
            TreeInfo info = new TreeInfo();
            this.next.study(info);
            this.minLength = info.minLength;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            if (i > matcher.to - this.minLength) {
                matcher.hitEnd = true;
                return false;
            }
            int guard = matcher.to - this.minLength;
            while (i <= guard) {
                if (this.next.match(matcher, i, seq)) {
                    matcher.groups[0] = matcher.first = i;
                    matcher.groups[1] = matcher.last;
                    return true;
                }
                ++i;
            }
            matcher.hitEnd = true;
            return false;
        }

        @Override
        boolean study(TreeInfo info) {
            this.next.study(info);
            info.maxValid = false;
            info.deterministic = false;
            return false;
        }
    }

    static class Node {
        Node next = accept;

        Node() {
        }

        boolean match(Matcher matcher, int i, CharSequence seq) {
            matcher.last = i;
            matcher.groups[0] = matcher.first;
            matcher.groups[1] = matcher.last;
            return true;
        }

        boolean study(TreeInfo info) {
            if (this.next != null) {
                return this.next.study(info);
            }
            return info.deterministic;
        }
    }

    static final class GroupHead
    extends Node {
        int localIndex;
        GroupTail tail;

        GroupHead(int localCount) {
            this.localIndex = localCount;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int save = matcher.locals[this.localIndex];
            matcher.locals[this.localIndex] = i;
            boolean ret = this.next.match(matcher, i, seq);
            matcher.locals[this.localIndex] = save;
            return ret;
        }
    }

    static class Slice
    extends SliceNode {
        Slice(int[] buf) {
            super(buf);
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int[] buf = this.buffer;
            int len = buf.length;
            for (int j = 0; j < len; ++j) {
                if (i + j >= matcher.to) {
                    matcher.hitEnd = true;
                    return false;
                }
                if (buf[j] == seq.charAt(i + j)) continue;
                return false;
            }
            return this.next.match(matcher, i + len, seq);
        }
    }

    static class BnM
    extends Node {
        int[] buffer;
        int[] lastOcc;
        int[] optoSft;

        static Node optimize(Node node) {
            int i;
            if (!(node instanceof Slice)) {
                return node;
            }
            int[] src = ((Slice)node).buffer;
            int patternLength = src.length;
            if (patternLength < 4) {
                return node;
            }
            int[] lastOcc = new int[128];
            int[] optoSft = new int[patternLength];
            for (i = 0; i < patternLength; ++i) {
                lastOcc[src[i] & 0x7F] = i + 1;
            }
            block1: for (i = patternLength; i > 0; --i) {
                int j;
                for (j = patternLength - 1; j >= i; --j) {
                    if (src[j] != src[j - i]) continue block1;
                    optoSft[j - 1] = i;
                }
                while (j > 0) {
                    optoSft[--j] = i;
                }
            }
            optoSft[patternLength - 1] = 1;
            if (node instanceof SliceS) {
                return new BnMS(src, lastOcc, optoSft, node.next);
            }
            return new BnM(src, lastOcc, optoSft, node.next);
        }

        BnM(int[] src, int[] lastOcc, int[] optoSft, Node next) {
            this.buffer = src;
            this.lastOcc = lastOcc;
            this.optoSft = optoSft;
            this.next = next;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int[] src = this.buffer;
            int patternLength = src.length;
            int last = matcher.to - patternLength;
            block0: while (i <= last) {
                for (int j = patternLength - 1; j >= 0; --j) {
                    char ch = seq.charAt(i + j);
                    if (ch == src[j]) continue;
                    i += Math.max(j + 1 - this.lastOcc[ch & 0x7F], this.optoSft[j]);
                    continue block0;
                }
                matcher.first = i;
                boolean ret = this.next.match(matcher, i + patternLength, seq);
                if (ret) {
                    matcher.groups[0] = matcher.first = i;
                    matcher.groups[1] = matcher.last;
                    return true;
                }
                ++i;
            }
            matcher.hitEnd = true;
            return false;
        }

        @Override
        boolean study(TreeInfo info) {
            info.minLength += this.buffer.length;
            info.maxValid = false;
            return this.next.study(info);
        }
    }

    static final class StartS
    extends Start {
        StartS(Node node) {
            super(node);
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            if (i > matcher.to - this.minLength) {
                matcher.hitEnd = true;
                return false;
            }
            int guard = matcher.to - this.minLength;
            while (i <= guard) {
                if (this.next.match(matcher, i, seq)) {
                    matcher.groups[0] = matcher.first = i;
                    matcher.groups[1] = matcher.last;
                    return true;
                }
                if (i == guard) break;
                if (!Character.isHighSurrogate(seq.charAt(i++)) || i >= seq.length() || !Character.isLowSurrogate(seq.charAt(i))) continue;
                ++i;
            }
            matcher.hitEnd = true;
            return false;
        }
    }

    static final class Begin
    extends Node {
        Begin() {
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int fromIndex;
            int n = fromIndex = matcher.anchoringBounds ? matcher.from : 0;
            if (i == fromIndex && this.next.match(matcher, i, seq)) {
                matcher.first = i;
                matcher.groups[0] = i;
                matcher.groups[1] = matcher.last;
                return true;
            }
            return false;
        }
    }

    static final class First
    extends Node {
        Node atom;

        First(Node node) {
            this.atom = BnM.optimize(node);
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            if (this.atom instanceof BnM) {
                return this.atom.match(matcher, i, seq) && this.next.match(matcher, matcher.last, seq);
            }
            while (true) {
                if (i > matcher.to) {
                    matcher.hitEnd = true;
                    return false;
                }
                if (this.atom.match(matcher, i, seq)) {
                    return this.next.match(matcher, matcher.last, seq);
                }
                i += Pattern.countChars(seq, i, 1);
                ++matcher.first;
            }
        }

        @Override
        boolean study(TreeInfo info) {
            this.atom.study(info);
            info.maxValid = false;
            info.deterministic = false;
            return this.next.study(info);
        }
    }

    static class Loop
    extends Node {
        Node body;
        int countIndex;
        int beginIndex;
        int cmin;
        int cmax;
        int posIndex;

        Loop(int countIndex, int beginIndex) {
            this.countIndex = countIndex;
            this.beginIndex = beginIndex;
            this.posIndex = -1;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            if (i > matcher.locals[this.beginIndex]) {
                int count = matcher.locals[this.countIndex];
                if (count < this.cmin) {
                    matcher.locals[this.countIndex] = count + 1;
                    boolean b = this.body.match(matcher, i, seq);
                    if (!b) {
                        matcher.locals[this.countIndex] = count;
                    }
                    return b;
                }
                if (count < this.cmax) {
                    if (this.posIndex != -1 && matcher.localsPos[this.posIndex].contains(i)) {
                        return this.next.match(matcher, i, seq);
                    }
                    matcher.locals[this.countIndex] = count + 1;
                    boolean b = this.body.match(matcher, i, seq);
                    if (b) {
                        return true;
                    }
                    matcher.locals[this.countIndex] = count;
                    if (this.posIndex != -1) {
                        matcher.localsPos[this.posIndex].add(i);
                    }
                }
            }
            return this.next.match(matcher, i, seq);
        }

        boolean matchInit(Matcher matcher, int i, CharSequence seq) {
            boolean ret;
            int save = matcher.locals[this.countIndex];
            if (this.posIndex != -1 && matcher.localsPos[this.posIndex] == null) {
                matcher.localsPos[this.posIndex] = new IntHashSet();
            }
            if (0 < this.cmin) {
                matcher.locals[this.countIndex] = 1;
                ret = this.body.match(matcher, i, seq);
            } else if (0 < this.cmax) {
                matcher.locals[this.countIndex] = 1;
                ret = this.body.match(matcher, i, seq);
                if (!ret) {
                    ret = this.next.match(matcher, i, seq);
                }
            } else {
                ret = this.next.match(matcher, i, seq);
            }
            matcher.locals[this.countIndex] = save;
            return ret;
        }

        @Override
        boolean study(TreeInfo info) {
            info.maxValid = false;
            info.deterministic = false;
            return false;
        }
    }

    static final class BranchConn
    extends Node {
        BranchConn() {
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            return this.next.match(matcher, i, seq);
        }

        @Override
        boolean study(TreeInfo info) {
            return info.deterministic;
        }
    }

    static final class Branch
    extends Node {
        Node[] atoms = new Node[2];
        int size = 2;
        Node conn;

        Branch(Node first, Node second, Node branchConn) {
            this.conn = branchConn;
            this.atoms[0] = first;
            this.atoms[1] = second;
        }

        void add(Node node) {
            if (this.size >= this.atoms.length) {
                Node[] tmp = new Node[this.atoms.length * 2];
                System.arraycopy(this.atoms, 0, tmp, 0, this.atoms.length);
                this.atoms = tmp;
            }
            this.atoms[this.size++] = node;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            for (int n = 0; n < this.size; ++n) {
                if (!(this.atoms[n] == null ? this.conn.next.match(matcher, i, seq) : this.atoms[n].match(matcher, i, seq))) continue;
                return true;
            }
            return false;
        }

        @Override
        boolean study(TreeInfo info) {
            int minL = info.minLength;
            int maxL = info.maxLength;
            boolean maxV = info.maxValid;
            int minL2 = Integer.MAX_VALUE;
            int maxL2 = -1;
            for (int n = 0; n < this.size; ++n) {
                info.reset();
                if (this.atoms[n] != null) {
                    this.atoms[n].study(info);
                }
                minL2 = Math.min(minL2, info.minLength);
                maxL2 = Math.max(maxL2, info.maxLength);
                maxV &= info.maxValid;
            }
            info.reset();
            this.conn.next.study(info);
            info.minLength += (minL += minL2);
            info.maxLength += (maxL += maxL2);
            info.maxValid &= maxV;
            info.deterministic = false;
            return false;
        }
    }

    private static class NFCCharProperty
    extends Node {
        CharPredicate predicate;

        NFCCharProperty(CharPredicate predicate) {
            this.predicate = predicate;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            if (i < matcher.to) {
                int j;
                int ch0 = Character.codePointAt(seq, i);
                int n = Character.charCount(ch0);
                if (i + n == (j = Grapheme.nextBoundary(seq, i, matcher.to))) {
                    if (this.predicate.is(ch0)) {
                        return this.next.match(matcher, j, seq);
                    }
                } else {
                    while (i + n < j) {
                        String nfc = java.text.Normalizer.normalize(seq.toString().substring(i, j), Normalizer.Form.NFC);
                        if (nfc.codePointCount(0, nfc.length()) == 1 && this.predicate.is(nfc.codePointAt(0)) && this.next.match(matcher, j, seq)) {
                            return true;
                        }
                        ch0 = Character.codePointBefore(seq, j);
                        j -= Character.charCount(ch0);
                    }
                }
                if (j < matcher.to) {
                    return false;
                }
            } else {
                matcher.hitEnd = true;
            }
            return false;
        }

        @Override
        boolean study(TreeInfo info) {
            ++info.minLength;
            info.deterministic = false;
            return this.next.study(info);
        }
    }

    @FunctionalInterface
    static interface CharPredicate {
        public boolean is(int var1);

        default public CharPredicate and(CharPredicate p) {
            return Pattern.and(this, p, false);
        }

        default public CharPredicate union(CharPredicate p) {
            return Pattern.union(this, p, false);
        }

        default public CharPredicate union(CharPredicate p1, CharPredicate p2) {
            return Pattern.union(this, p1, p2, false);
        }

        default public CharPredicate negate() {
            return Pattern.negate(this);
        }
    }

    static class CharProperty
    extends Node {
        final CharPredicate predicate;

        CharProperty(CharPredicate predicate) {
            this.predicate = predicate;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int ch;
            if (i < matcher.to && (i += Character.charCount(ch = Character.codePointAt(seq, i))) <= matcher.to) {
                return this.predicate.is(ch) && this.next.match(matcher, i, seq);
            }
            matcher.hitEnd = true;
            return false;
        }

        @Override
        boolean study(TreeInfo info) {
            ++info.minLength;
            ++info.maxLength;
            return this.next.study(info);
        }
    }

    static final class UnixCaret
    extends Node {
        UnixCaret() {
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            char ch;
            int startIndex = matcher.from;
            int endIndex = matcher.to;
            if (!matcher.anchoringBounds) {
                startIndex = 0;
                endIndex = matcher.getTextLength();
            }
            if (i == endIndex) {
                matcher.hitEnd = true;
                return false;
            }
            if (i > startIndex && (ch = seq.charAt(i - 1)) != '\n') {
                return false;
            }
            return this.next.match(matcher, i, seq);
        }
    }

    static final class Caret
    extends Node {
        Caret() {
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int startIndex = matcher.from;
            int endIndex = matcher.to;
            if (!matcher.anchoringBounds) {
                startIndex = 0;
                endIndex = matcher.getTextLength();
            }
            if (i == endIndex) {
                matcher.hitEnd = true;
                return false;
            }
            if (i > startIndex) {
                char ch = seq.charAt(i - 1);
                if (ch != '\n' && ch != '\r' && (ch | '\u0001') != 8233 && ch != '\u0085') {
                    return false;
                }
                if (ch == '\r' && seq.charAt(i) == '\n') {
                    return false;
                }
            }
            return this.next.match(matcher, i, seq);
        }
    }

    static final class UnixDollar
    extends Node {
        boolean multiline;

        UnixDollar(boolean mul) {
            this.multiline = mul;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int endIndex;
            int n = endIndex = matcher.anchoringBounds ? matcher.to : matcher.getTextLength();
            if (i < endIndex) {
                char ch = seq.charAt(i);
                if (ch == '\n') {
                    if (!this.multiline && i != endIndex - 1) {
                        return false;
                    }
                    if (this.multiline) {
                        return this.next.match(matcher, i, seq);
                    }
                } else {
                    return false;
                }
            }
            matcher.hitEnd = true;
            matcher.requireEnd = true;
            return this.next.match(matcher, i, seq);
        }

        @Override
        boolean study(TreeInfo info) {
            this.next.study(info);
            return info.deterministic;
        }
    }

    static final class Dollar
    extends Node {
        boolean multiline;

        Dollar(boolean mul) {
            this.multiline = mul;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            char ch;
            int endIndex;
            int n = endIndex = matcher.anchoringBounds ? matcher.to : matcher.getTextLength();
            if (!this.multiline) {
                if (i < endIndex - 2) {
                    return false;
                }
                if (i == endIndex - 2) {
                    ch = seq.charAt(i);
                    if (ch != '\r') {
                        return false;
                    }
                    ch = seq.charAt(i + 1);
                    if (ch != '\n') {
                        return false;
                    }
                }
            }
            if (i < endIndex) {
                ch = seq.charAt(i);
                if (ch == '\n') {
                    if (i > 0 && seq.charAt(i - 1) == '\r') {
                        return false;
                    }
                    if (this.multiline) {
                        return this.next.match(matcher, i, seq);
                    }
                } else if (ch == '\r' || ch == '\u0085' || (ch | '\u0001') == 8233) {
                    if (this.multiline) {
                        return this.next.match(matcher, i, seq);
                    }
                } else {
                    return false;
                }
            }
            matcher.hitEnd = true;
            matcher.requireEnd = true;
            return this.next.match(matcher, i, seq);
        }

        @Override
        boolean study(TreeInfo info) {
            this.next.study(info);
            return info.deterministic;
        }
    }

    static class CIBackRef
    extends Node {
        int groupIndex;
        boolean doUnicodeCase;

        CIBackRef(int groupCount, boolean doUnicodeCase) {
            this.groupIndex = groupCount + groupCount;
            this.doUnicodeCase = doUnicodeCase;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            if (this.groupIndex >= matcher.groups.length) {
                return false;
            }
            int j = matcher.groups[this.groupIndex];
            int k = matcher.groups[this.groupIndex + 1];
            int groupSizeChars = k - j;
            if (j < 0) {
                return false;
            }
            if (i + groupSizeChars > matcher.to) {
                matcher.hitEnd = true;
                return false;
            }
            int x = i;
            int groupCodepoints = groupSizeChars;
            for (int index = 0; index < groupCodepoints; ++index) {
                int cc2;
                int cc1;
                int c2;
                int c1 = Character.codePointAt(seq, x);
                if (c1 != (c2 = Character.codePointAt(seq, j)) && (this.doUnicodeCase ? (cc1 = Character.toUpperCase(c1)) != (cc2 = Character.toUpperCase(c2)) && Character.toLowerCase(cc1) != Character.toLowerCase(cc2) : ASCII.toLower(c1) != ASCII.toLower(c2))) {
                    return false;
                }
                x += Character.charCount(c1);
                j += Character.charCount(c2);
                if (c1 < 65536) continue;
                --groupCodepoints;
            }
            return this.next.match(matcher, i + groupSizeChars, seq);
        }

        @Override
        boolean study(TreeInfo info) {
            info.maxValid = false;
            return this.next.study(info);
        }
    }

    static class BackRef
    extends Node {
        int groupIndex;

        BackRef(int groupCount) {
            this.groupIndex = groupCount + groupCount;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            if (this.groupIndex >= matcher.groups.length) {
                return false;
            }
            int j = matcher.groups[this.groupIndex];
            int k = matcher.groups[this.groupIndex + 1];
            int groupSize = k - j;
            if (j < 0) {
                return false;
            }
            if (i + groupSize > matcher.to) {
                matcher.hitEnd = true;
                return false;
            }
            for (int index = 0; index < groupSize; ++index) {
                if (seq.charAt(i + index) == seq.charAt(j + index)) continue;
                return false;
            }
            return this.next.match(matcher, i + groupSize, seq);
        }

        @Override
        boolean study(TreeInfo info) {
            info.maxValid = false;
            return this.next.study(info);
        }
    }

    static final class Bound
    extends Node {
        static final int LEFT = 1;
        static final int RIGHT = 2;
        static final int BOTH = 3;
        static final int NONE = 4;
        int type;
        boolean useUWORD;

        Bound(int n, boolean useUWORD) {
            this.type = n;
            this.useUWORD = useUWORD;
        }

        boolean isWord(int ch) {
            return this.useUWORD ? CharPredicates.WORD().is(ch) : CharPredicates.ASCII_WORD().is(ch);
        }

        int check(Matcher matcher, int i, CharSequence seq) {
            int ch;
            boolean left = false;
            int startIndex = matcher.from;
            int endIndex = matcher.to;
            if (matcher.transparentBounds) {
                startIndex = 0;
                endIndex = matcher.getTextLength();
            }
            if (i > startIndex) {
                ch = Character.codePointBefore(seq, i);
                left = this.isWord(ch) || Character.getType(ch) == 6 && Pattern.hasBaseCharacter(matcher, i - 1, seq);
            }
            boolean right = false;
            if (i < endIndex) {
                ch = Character.codePointAt(seq, i);
                right = this.isWord(ch) || Character.getType(ch) == 6 && Pattern.hasBaseCharacter(matcher, i, seq);
            } else {
                matcher.hitEnd = true;
                matcher.requireEnd = true;
            }
            return left ^ right ? (right ? 1 : 2) : 4;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            return (this.check(matcher, i, seq) & this.type) > 0 && this.next.match(matcher, i, seq);
        }
    }

    static interface BmpCharPredicate
    extends CharPredicate {
        @Override
        default public CharPredicate and(CharPredicate p) {
            return Pattern.and(this, p, p instanceof BmpCharPredicate);
        }

        @Override
        default public CharPredicate union(CharPredicate p) {
            return Pattern.union(this, p, p instanceof BmpCharPredicate);
        }

        @Override
        default public CharPredicate union(CharPredicate p1, CharPredicate p2) {
            return Pattern.union(this, p1, p2, p1 instanceof BmpCharPredicate && p2 instanceof BmpCharPredicate);
        }
    }

    static final class LastMatch
    extends Node {
        LastMatch() {
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            if (i != matcher.oldLast) {
                return false;
            }
            return this.next.match(matcher, i, seq);
        }
    }

    static final class LineEnding
    extends Node {
        LineEnding() {
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            if (i < matcher.to) {
                char ch = seq.charAt(i);
                if (ch == '\n' || ch == '\u000b' || ch == '\f' || ch == '\u0085' || ch == '\u2028' || ch == '\u2029') {
                    return this.next.match(matcher, i + 1, seq);
                }
                if (ch == '\r') {
                    if (++i < matcher.to) {
                        if (seq.charAt(i) == '\n' && this.next.match(matcher, i + 1, seq)) {
                            return true;
                        }
                    } else {
                        matcher.hitEnd = true;
                    }
                    return this.next.match(matcher, i, seq);
                }
            } else {
                matcher.hitEnd = true;
            }
            return false;
        }

        @Override
        boolean study(TreeInfo info) {
            ++info.minLength;
            info.maxLength += 2;
            return this.next.study(info);
        }
    }

    static class XGrapheme
    extends Node {
        XGrapheme() {
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            if (i < matcher.to) {
                i = Grapheme.nextBoundary(seq, i, matcher.to);
                return this.next.match(matcher, i, seq);
            }
            matcher.hitEnd = true;
            return false;
        }

        @Override
        boolean study(TreeInfo info) {
            ++info.minLength;
            info.deterministic = false;
            return this.next.study(info);
        }
    }

    static class GraphemeBound
    extends Node {
        GraphemeBound() {
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int startIndex = matcher.from;
            int endIndex = matcher.to;
            if (matcher.transparentBounds) {
                startIndex = 0;
                endIndex = matcher.getTextLength();
            }
            if (i != startIndex) {
                if (i < endIndex) {
                    if (Character.isSurrogatePair(seq.charAt(i - 1), seq.charAt(i))) {
                        return false;
                    }
                    if (Grapheme.nextBoundary(seq, matcher.last, endIndex) > i) {
                        return false;
                    }
                } else {
                    matcher.hitEnd = true;
                    matcher.requireEnd = true;
                }
            }
            return this.next.match(matcher, i, seq);
        }
    }

    static final class End
    extends Node {
        End() {
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int endIndex;
            int n = endIndex = matcher.anchoringBounds ? matcher.to : matcher.getTextLength();
            if (i == endIndex) {
                matcher.hitEnd = true;
                return this.next.match(matcher, i, seq);
            }
            return false;
        }
    }

    static final class BitClass
    implements BmpCharPredicate {
        final boolean[] bits = new boolean[256];

        BitClass() {
        }

        BitClass add(int c, int flags) {
            assert (c >= 0 && c <= 255);
            if ((flags & 2) != 0) {
                if (ASCII.isAscii(c)) {
                    this.bits[ASCII.toUpper((int)c)] = true;
                    this.bits[ASCII.toLower((int)c)] = true;
                } else if ((flags & 0x40) != 0) {
                    this.bits[Character.toLowerCase((int)c)] = true;
                    this.bits[Character.toUpperCase((int)c)] = true;
                }
            }
            this.bits[c] = true;
            return this;
        }

        @Override
        public boolean is(int ch) {
            return ch < 256 && this.bits[ch];
        }
    }

    private static class BmpCharProperty
    extends CharProperty {
        BmpCharProperty(BmpCharPredicate predicate) {
            super(predicate);
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            if (i < matcher.to) {
                return this.predicate.is(seq.charAt(i)) && this.next.match(matcher, i + 1, seq);
            }
            matcher.hitEnd = true;
            return false;
        }
    }

    static final class Pos
    extends Node {
        Node cond;

        Pos(Node cond) {
            this.cond = cond;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            boolean conditionMatched;
            int savedTo = matcher.to;
            if (matcher.transparentBounds) {
                matcher.to = matcher.getTextLength();
            }
            try {
                conditionMatched = this.cond.match(matcher, i, seq);
            }
            finally {
                matcher.to = savedTo;
            }
            return conditionMatched && this.next.match(matcher, i, seq);
        }
    }

    static final class Neg
    extends Node {
        Node cond;

        Neg(Node cond) {
            this.cond = cond;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            boolean conditionMatched;
            int savedTo = matcher.to;
            if (matcher.transparentBounds) {
                matcher.to = matcher.getTextLength();
            }
            try {
                if (i < matcher.to) {
                    conditionMatched = !this.cond.match(matcher, i, seq);
                } else {
                    matcher.requireEnd = true;
                    conditionMatched = !this.cond.match(matcher, i, seq);
                }
            }
            finally {
                matcher.to = savedTo;
            }
            return conditionMatched && this.next.match(matcher, i, seq);
        }
    }

    static final class Ques
    extends Node {
        Node atom;
        Qtype type;

        Ques(Node node, Qtype type) {
            this.atom = node;
            this.type = type;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            switch (this.type.ordinal()) {
                case 0: {
                    return this.atom.match(matcher, i, seq) && this.next.match(matcher, matcher.last, seq) || this.next.match(matcher, i, seq);
                }
                case 1: {
                    return this.next.match(matcher, i, seq) || this.atom.match(matcher, i, seq) && this.next.match(matcher, matcher.last, seq);
                }
                case 2: {
                    if (this.atom.match(matcher, i, seq)) {
                        i = matcher.last;
                    }
                    return this.next.match(matcher, i, seq);
                }
            }
            return this.atom.match(matcher, i, seq) && this.next.match(matcher, matcher.last, seq);
        }

        @Override
        boolean study(TreeInfo info) {
            if (this.type != Qtype.INDEPENDENT) {
                int minL = info.minLength;
                this.atom.study(info);
                info.minLength = minL;
                info.deterministic = false;
                return this.next.study(info);
            }
            this.atom.study(info);
            return this.next.study(info);
        }
    }

    static enum Qtype {
        GREEDY,
        LAZY,
        POSSESSIVE,
        INDEPENDENT;

    }

    static class LookBehindEndNode
    extends Node {
        static LookBehindEndNode INSTANCE = new LookBehindEndNode();

        private LookBehindEndNode() {
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            return i == matcher.lookbehindTo;
        }
    }

    static final class TreeInfo {
        int minLength;
        int maxLength;
        boolean maxValid;
        boolean deterministic;

        TreeInfo() {
            this.reset();
        }

        void reset() {
            this.minLength = 0;
            this.maxLength = 0;
            this.maxValid = true;
            this.deterministic = true;
        }
    }

    static final class BehindS
    extends Behind {
        BehindS(Node cond, int rmax, int rmin) {
            super(cond, rmax, rmin);
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int rmaxChars = Pattern.countChars(seq, i, -this.rmax);
            int rminChars = Pattern.countChars(seq, i, -this.rmin);
            int savedFrom = matcher.from;
            int startIndex = !matcher.transparentBounds ? matcher.from : 0;
            boolean conditionMatched = false;
            int from = Math.max(i - rmaxChars, startIndex);
            int savedLBT = matcher.lookbehindTo;
            matcher.lookbehindTo = i;
            if (matcher.transparentBounds) {
                matcher.from = 0;
            }
            for (int j = i - rminChars; !conditionMatched && j >= from; j -= j > from ? Pattern.countChars(seq, j, -1) : 1) {
                conditionMatched = this.cond.match(matcher, j, seq);
            }
            matcher.from = savedFrom;
            matcher.lookbehindTo = savedLBT;
            return conditionMatched && this.next.match(matcher, i, seq);
        }
    }

    static class Behind
    extends Node {
        Node cond;
        int rmax;
        int rmin;

        Behind(Node cond, int rmax, int rmin) {
            this.cond = cond;
            this.rmax = rmax;
            this.rmin = rmin;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int savedFrom = matcher.from;
            boolean conditionMatched = false;
            int startIndex = !matcher.transparentBounds ? matcher.from : 0;
            int from = Math.max(i - this.rmax, startIndex);
            int savedLBT = matcher.lookbehindTo;
            matcher.lookbehindTo = i;
            if (matcher.transparentBounds) {
                matcher.from = 0;
            }
            for (int j = i - this.rmin; !conditionMatched && j >= from; --j) {
                conditionMatched = this.cond.match(matcher, j, seq);
            }
            matcher.from = savedFrom;
            matcher.lookbehindTo = savedLBT;
            return conditionMatched && this.next.match(matcher, i, seq);
        }
    }

    static final class NotBehindS
    extends NotBehind {
        NotBehindS(Node cond, int rmax, int rmin) {
            super(cond, rmax, rmin);
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int rmaxChars = Pattern.countChars(seq, i, -this.rmax);
            int rminChars = Pattern.countChars(seq, i, -this.rmin);
            int savedFrom = matcher.from;
            int savedLBT = matcher.lookbehindTo;
            boolean conditionMatched = false;
            int startIndex = !matcher.transparentBounds ? matcher.from : 0;
            int from = Math.max(i - rmaxChars, startIndex);
            matcher.lookbehindTo = i;
            if (matcher.transparentBounds) {
                matcher.from = 0;
            }
            for (int j = i - rminChars; !conditionMatched && j >= from; j -= j > from ? Pattern.countChars(seq, j, -1) : 1) {
                conditionMatched = this.cond.match(matcher, j, seq);
            }
            matcher.from = savedFrom;
            matcher.lookbehindTo = savedLBT;
            return !conditionMatched && this.next.match(matcher, i, seq);
        }
    }

    static class NotBehind
    extends Node {
        Node cond;
        int rmax;
        int rmin;

        NotBehind(Node cond, int rmax, int rmin) {
            this.cond = cond;
            this.rmax = rmax;
            this.rmin = rmin;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int savedLBT = matcher.lookbehindTo;
            int savedFrom = matcher.from;
            boolean conditionMatched = false;
            int startIndex = !matcher.transparentBounds ? matcher.from : 0;
            int from = Math.max(i - this.rmax, startIndex);
            matcher.lookbehindTo = i;
            if (matcher.transparentBounds) {
                matcher.from = 0;
            }
            for (int j = i - this.rmin; !conditionMatched && j >= from; --j) {
                conditionMatched = this.cond.match(matcher, j, seq);
            }
            matcher.from = savedFrom;
            matcher.lookbehindTo = savedLBT;
            return !conditionMatched && this.next.match(matcher, i, seq);
        }
    }

    static final class Curly
    extends Node {
        Node atom;
        Qtype type;
        int cmin;
        int cmax;

        Curly(Node node, int cmin, int cmax, Qtype type) {
            this.atom = node;
            this.type = type;
            this.cmin = cmin;
            this.cmax = cmax;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int j;
            for (j = 0; j < this.cmin; ++j) {
                if (!this.atom.match(matcher, i, seq)) {
                    return false;
                }
                i = matcher.last;
            }
            if (this.type == Qtype.GREEDY) {
                return this.match0(matcher, i, j, seq);
            }
            if (this.type == Qtype.LAZY) {
                return this.match1(matcher, i, j, seq);
            }
            return this.match2(matcher, i, j, seq);
        }

        boolean match0(Matcher matcher, int i, int j, CharSequence seq) {
            int k;
            if (j >= this.cmax) {
                return this.next.match(matcher, i, seq);
            }
            int backLimit = j++;
            if (this.atom.match(matcher, i, seq) && (k = matcher.last - i) != 0) {
                i = matcher.last;
                while (j < this.cmax && this.atom.match(matcher, i, seq)) {
                    if (i + k != matcher.last) {
                        if (!this.match0(matcher, matcher.last, j + 1, seq)) break;
                        return true;
                    }
                    i += k;
                    ++j;
                }
                while (j >= backLimit) {
                    if (this.next.match(matcher, i, seq)) {
                        return true;
                    }
                    i -= k;
                    --j;
                }
                return false;
            }
            return this.next.match(matcher, i, seq);
        }

        boolean match1(Matcher matcher, int i, int j, CharSequence seq) {
            while (!this.next.match(matcher, i, seq)) {
                if (j >= this.cmax) {
                    return false;
                }
                if (!this.atom.match(matcher, i, seq)) {
                    return false;
                }
                if (i == matcher.last) {
                    return false;
                }
                i = matcher.last;
                ++j;
            }
            return true;
        }

        boolean match2(Matcher matcher, int i, int j, CharSequence seq) {
            while (j < this.cmax && this.atom.match(matcher, i, seq) && i != matcher.last) {
                i = matcher.last;
                ++j;
            }
            return this.next.match(matcher, i, seq);
        }

        @Override
        boolean study(TreeInfo info) {
            int minL = info.minLength;
            int maxL = info.maxLength;
            boolean maxV = info.maxValid;
            boolean detm = info.deterministic;
            info.reset();
            this.atom.study(info);
            int temp = info.minLength * this.cmin + minL;
            if (temp < minL) {
                temp = 0xFFFFFFF;
            }
            info.minLength = temp;
            if (maxV & info.maxValid) {
                info.maxLength = temp = info.maxLength * this.cmax + maxL;
                if (temp < maxL) {
                    info.maxValid = false;
                }
            } else {
                info.maxValid = false;
            }
            info.deterministic = info.deterministic && this.cmin == this.cmax ? detm : false;
            return this.next.study(info);
        }
    }

    static final class GroupTail
    extends Node {
        int localIndex;
        int groupIndex;

        GroupTail(int localCount, int groupCount) {
            this.localIndex = localCount;
            this.groupIndex = groupCount + groupCount;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int tmp = matcher.locals[this.localIndex];
            if (tmp >= 0) {
                int groupStart = matcher.groups[this.groupIndex];
                int groupEnd = matcher.groups[this.groupIndex + 1];
                matcher.groups[this.groupIndex] = tmp;
                matcher.groups[this.groupIndex + 1] = i;
                if (this.next.match(matcher, i, seq)) {
                    return true;
                }
                matcher.groups[this.groupIndex] = groupStart;
                matcher.groups[this.groupIndex + 1] = groupEnd;
                return false;
            }
            matcher.last = i;
            return true;
        }
    }

    static final class GroupCurly
    extends Node {
        Node atom;
        Qtype type;
        int cmin;
        int cmax;
        int localIndex;
        int groupIndex;
        boolean capture;

        GroupCurly(Node node, int cmin, int cmax, Qtype type, int local, int group, boolean capture) {
            this.atom = node;
            this.type = type;
            this.cmin = cmin;
            this.cmax = cmax;
            this.localIndex = local;
            this.groupIndex = group;
            this.capture = capture;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int[] groups = matcher.groups;
            int[] locals = matcher.locals;
            int save0 = locals[this.localIndex];
            int save1 = 0;
            int save2 = 0;
            if (this.capture) {
                save1 = groups[this.groupIndex];
                save2 = groups[this.groupIndex + 1];
            }
            locals[this.localIndex] = -1;
            boolean ret = true;
            for (int j = 0; j < this.cmin; ++j) {
                if (this.atom.match(matcher, i, seq)) {
                    if (this.capture) {
                        groups[this.groupIndex] = i;
                        groups[this.groupIndex + 1] = matcher.last;
                    }
                } else {
                    ret = false;
                    break;
                }
                i = matcher.last;
            }
            if (ret) {
                ret = this.type == Qtype.GREEDY ? this.match0(matcher, i, this.cmin, seq) : (this.type == Qtype.LAZY ? this.match1(matcher, i, this.cmin, seq) : this.match2(matcher, i, this.cmin, seq));
            }
            if (!ret) {
                locals[this.localIndex] = save0;
                if (this.capture) {
                    groups[this.groupIndex] = save1;
                    groups[this.groupIndex + 1] = save2;
                }
            }
            return ret;
        }

        boolean match0(Matcher matcher, int i, int j, CharSequence seq) {
            int min = j;
            int[] groups = matcher.groups;
            int save0 = 0;
            int save1 = 0;
            if (this.capture) {
                save0 = groups[this.groupIndex];
                save1 = groups[this.groupIndex + 1];
            }
            if (j < this.cmax && this.atom.match(matcher, i, seq)) {
                int k = matcher.last - i;
                if (k <= 0) {
                    if (this.capture) {
                        groups[this.groupIndex] = i;
                        groups[this.groupIndex + 1] = i + k;
                    }
                    i += k;
                } else {
                    block13: {
                        do {
                            if (this.capture) {
                                groups[this.groupIndex] = i;
                                groups[this.groupIndex + 1] = i + k;
                            }
                            if (++j >= this.cmax || !this.atom.match(matcher, i += k, seq)) break block13;
                        } while (i + k == matcher.last);
                        if (this.match0(matcher, i, j, seq)) {
                            return true;
                        }
                    }
                    while (j > min) {
                        if (this.next.match(matcher, i, seq)) {
                            if (this.capture) {
                                groups[this.groupIndex + 1] = i;
                                groups[this.groupIndex] = i - k;
                            }
                            return true;
                        }
                        i -= k;
                        if (this.capture) {
                            groups[this.groupIndex + 1] = i;
                            groups[this.groupIndex] = i - k;
                        }
                        --j;
                    }
                }
            }
            if (this.capture) {
                groups[this.groupIndex] = save0;
                groups[this.groupIndex + 1] = save1;
            }
            return this.next.match(matcher, i, seq);
        }

        boolean match1(Matcher matcher, int i, int j, CharSequence seq) {
            while (!this.next.match(matcher, i, seq)) {
                if (j >= this.cmax) {
                    return false;
                }
                if (!this.atom.match(matcher, i, seq)) {
                    return false;
                }
                if (i == matcher.last) {
                    return false;
                }
                if (this.capture) {
                    matcher.groups[this.groupIndex] = i;
                    matcher.groups[this.groupIndex + 1] = matcher.last;
                }
                i = matcher.last;
                ++j;
            }
            return true;
        }

        boolean match2(Matcher matcher, int i, int j, CharSequence seq) {
            while (j < this.cmax && this.atom.match(matcher, i, seq)) {
                if (this.capture) {
                    matcher.groups[this.groupIndex] = i;
                    matcher.groups[this.groupIndex + 1] = matcher.last;
                }
                if (i == matcher.last) break;
                i = matcher.last;
                ++j;
            }
            return this.next.match(matcher, i, seq);
        }

        @Override
        boolean study(TreeInfo info) {
            int minL = info.minLength;
            int maxL = info.maxLength;
            boolean maxV = info.maxValid;
            boolean detm = info.deterministic;
            info.reset();
            this.atom.study(info);
            int temp = info.minLength * this.cmin + minL;
            if (temp < minL) {
                temp = 0xFFFFFFF;
            }
            info.minLength = temp;
            if (maxV & info.maxValid) {
                info.maxLength = temp = info.maxLength * this.cmax + maxL;
                if (temp < maxL) {
                    info.maxValid = false;
                }
            } else {
                info.maxValid = false;
            }
            info.deterministic = info.deterministic && this.cmin == this.cmax ? detm : false;
            return this.next.study(info);
        }
    }

    static final class LazyLoop
    extends Loop {
        LazyLoop(int countIndex, int beginIndex) {
            super(countIndex, beginIndex);
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            if (i > matcher.locals[this.beginIndex]) {
                int count = matcher.locals[this.countIndex];
                if (count < this.cmin) {
                    matcher.locals[this.countIndex] = count + 1;
                    boolean result = this.body.match(matcher, i, seq);
                    if (!result) {
                        matcher.locals[this.countIndex] = count;
                    }
                    return result;
                }
                if (this.next.match(matcher, i, seq)) {
                    return true;
                }
                if (count < this.cmax) {
                    matcher.locals[this.countIndex] = count + 1;
                    boolean result = this.body.match(matcher, i, seq);
                    if (!result) {
                        matcher.locals[this.countIndex] = count;
                    }
                    return result;
                }
                return false;
            }
            return this.next.match(matcher, i, seq);
        }

        @Override
        boolean matchInit(Matcher matcher, int i, CharSequence seq) {
            int save = matcher.locals[this.countIndex];
            boolean ret = false;
            if (0 < this.cmin) {
                matcher.locals[this.countIndex] = 1;
                ret = this.body.match(matcher, i, seq);
            } else if (this.next.match(matcher, i, seq)) {
                ret = true;
            } else if (0 < this.cmax) {
                matcher.locals[this.countIndex] = 1;
                ret = this.body.match(matcher, i, seq);
            }
            matcher.locals[this.countIndex] = save;
            return ret;
        }

        @Override
        boolean study(TreeInfo info) {
            info.maxValid = false;
            info.deterministic = false;
            return false;
        }
    }

    static final class Prolog
    extends Node {
        Loop loop;

        Prolog(Loop loop) {
            this.loop = loop;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            return this.loop.matchInit(matcher, i, seq);
        }

        @Override
        boolean study(TreeInfo info) {
            return this.loop.study(info);
        }
    }

    static final class BmpCharPropertyGreedy
    extends CharPropertyGreedy {
        BmpCharPropertyGreedy(BmpCharProperty bcp, int cmin) {
            super(bcp, cmin);
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int n = 0;
            int to = matcher.to;
            while (i < to && this.predicate.is(seq.charAt(i))) {
                ++i;
                ++n;
            }
            if (i >= to) {
                matcher.hitEnd = true;
            }
            while (n >= this.cmin) {
                if (this.next.match(matcher, i, seq)) {
                    return true;
                }
                --i;
                --n;
            }
            return false;
        }
    }

    static class CharPropertyGreedy
    extends Node {
        final CharPredicate predicate;
        final int cmin;

        CharPropertyGreedy(CharProperty cp, int cmin) {
            this.predicate = cp.predicate;
            this.cmin = cmin;
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int ch;
            int starti = i;
            int n = 0;
            int to = matcher.to;
            while (i < to) {
                ch = Character.codePointAt(seq, i);
                int len = Character.charCount(ch);
                if (i + len > to) {
                    matcher.hitEnd = true;
                    ch = seq.charAt(i);
                    len = 1;
                }
                if (!this.predicate.is(ch)) break;
                i += len;
                ++n;
            }
            if (i >= to) {
                matcher.hitEnd = true;
            }
            while (n >= this.cmin) {
                if (this.next.match(matcher, i, seq)) {
                    return true;
                }
                if (n == this.cmin) {
                    return false;
                }
                ch = Character.codePointBefore(seq, i);
                i = Math.max(starti, i - Character.charCount(ch));
                --n;
            }
            return false;
        }

        @Override
        boolean study(TreeInfo info) {
            info.minLength += this.cmin;
            if (info.maxValid) {
                info.maxLength += Integer.MAX_VALUE;
            }
            info.deterministic = false;
            return this.next.study(info);
        }
    }

    static final class SliceUS
    extends SliceIS {
        SliceUS(int[] buf) {
            super(buf);
        }

        @Override
        int toLower(int c) {
            return Character.toLowerCase(Character.toUpperCase(c));
        }
    }

    static final class SliceU
    extends SliceNode {
        SliceU(int[] buf) {
            super(buf);
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int[] buf = this.buffer;
            int len = buf.length;
            for (int j = 0; j < len; ++j) {
                if (i + j >= matcher.to) {
                    matcher.hitEnd = true;
                    return false;
                }
                char c = seq.charAt(i + j);
                if (buf[j] == c || buf[j] == Character.toLowerCase(Character.toUpperCase((int)c))) continue;
                return false;
            }
            return this.next.match(matcher, i + len, seq);
        }
    }

    static class SliceIS
    extends SliceNode {
        SliceIS(int[] buf) {
            super(buf);
        }

        int toLower(int c) {
            return ASCII.toLower(c);
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int[] buf = this.buffer;
            int x = i;
            for (int j = 0; j < buf.length; ++j) {
                if (x >= matcher.to) {
                    matcher.hitEnd = true;
                    return false;
                }
                int c = Character.codePointAt(seq, x);
                if (buf[j] != c && buf[j] != this.toLower(c)) {
                    return false;
                }
                if ((x += Character.charCount(c)) <= matcher.to) continue;
                matcher.hitEnd = true;
                return false;
            }
            return this.next.match(matcher, x, seq);
        }
    }

    static class SliceI
    extends SliceNode {
        SliceI(int[] buf) {
            super(buf);
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int[] buf = this.buffer;
            int len = buf.length;
            for (int j = 0; j < len; ++j) {
                if (i + j >= matcher.to) {
                    matcher.hitEnd = true;
                    return false;
                }
                char c = seq.charAt(i + j);
                if (buf[j] == c || buf[j] == ASCII.toLower(c)) continue;
                return false;
            }
            return this.next.match(matcher, i + len, seq);
        }
    }

    static final class SliceS
    extends Slice {
        SliceS(int[] buf) {
            super(buf);
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int[] buf = this.buffer;
            int x = i;
            for (int j = 0; j < buf.length; ++j) {
                if (x >= matcher.to) {
                    matcher.hitEnd = true;
                    return false;
                }
                int c = Character.codePointAt(seq, x);
                if (buf[j] != c) {
                    return false;
                }
                if ((x += Character.charCount(c)) <= matcher.to) continue;
                matcher.hitEnd = true;
                return false;
            }
            return this.next.match(matcher, x, seq);
        }
    }

    static class LastNode
    extends Node {
        LastNode() {
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            if (matcher.acceptMode == 1 && i != matcher.to) {
                return false;
            }
            matcher.last = i;
            matcher.groups[0] = matcher.first;
            matcher.groups[1] = matcher.last;
            return true;
        }
    }

    static final class BnMS
    extends BnM {
        int lengthInChars;

        BnMS(int[] src, int[] lastOcc, int[] optoSft, Node next) {
            super(src, lastOcc, optoSft, next);
            for (int cp : this.buffer) {
                this.lengthInChars += Character.charCount(cp);
            }
        }

        @Override
        boolean match(Matcher matcher, int i, CharSequence seq) {
            int[] src = this.buffer;
            int patternLength = src.length;
            int last = matcher.to - this.lengthInChars;
            block0: while (i <= last) {
                int j = Pattern.countChars(seq, i, patternLength);
                int x = patternLength - 1;
                while (j > 0) {
                    int ch = Character.codePointBefore(seq, i + j);
                    if (ch != src[x]) {
                        int n = Math.max(x + 1 - this.lastOcc[ch & 0x7F], this.optoSft[x]);
                        i += Pattern.countChars(seq, i, n);
                        continue block0;
                    }
                    j -= Character.charCount(ch);
                    --x;
                }
                matcher.first = i;
                boolean ret = this.next.match(matcher, i + this.lengthInChars, seq);
                if (ret) {
                    matcher.groups[0] = matcher.first = i;
                    matcher.groups[1] = matcher.last;
                    return true;
                }
                i += Pattern.countChars(seq, i, 1);
            }
            matcher.hitEnd = true;
            return false;
        }
    }

    static class SliceNode
    extends Node {
        int[] buffer;

        SliceNode(int[] buf) {
            this.buffer = buf;
        }

        @Override
        boolean study(TreeInfo info) {
            info.minLength += this.buffer.length;
            info.maxLength += this.buffer.length;
            return this.next.study(info);
        }
    }
}

