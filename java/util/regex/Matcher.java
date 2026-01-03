/*
 * Decompiled with CFR 0.152.
 */
package java.util.regex;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.ASCII;
import java.util.regex.IntHashSet;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Matcher
implements MatchResult {
    Pattern parentPattern;
    int[] groups;
    int from;
    int to;
    int lookbehindTo;
    CharSequence text;
    static final int ENDANCHOR = 1;
    static final int NOANCHOR = 0;
    int acceptMode = 0;
    int first = -1;
    int last = 0;
    int oldLast = -1;
    int lastAppendPosition = 0;
    int[] locals;
    IntHashSet[] localsPos;
    boolean hitEnd;
    boolean requireEnd;
    boolean transparentBounds = false;
    boolean anchoringBounds = true;
    int modCount;
    private Map<String, Integer> namedGroups;

    Matcher() {
    }

    Matcher(Pattern parent, CharSequence text) {
        this.parentPattern = parent;
        this.text = text;
        int parentGroupCount = Math.max(parent.capturingGroupCount, 10);
        this.groups = new int[parentGroupCount * 2];
        this.locals = new int[parent.localCount];
        this.localsPos = new IntHashSet[parent.localTCNCount];
        this.reset();
    }

    public Pattern pattern() {
        return this.parentPattern;
    }

    public MatchResult toMatchResult() {
        String capturedText;
        int minStart;
        if (this.hasMatch()) {
            minStart = this.minStart();
            capturedText = this.text.subSequence(minStart, this.maxEnd()).toString();
        } else {
            minStart = -1;
            capturedText = null;
        }
        return new ImmutableMatchResult(this.first, this.last, this.groupCount(), (int[])this.groups.clone(), capturedText, this.namedGroups(), minStart);
    }

    private int minStart() {
        int r = this.text.length();
        for (int group = 0; group <= this.groupCount(); ++group) {
            int start = this.groups[group * 2];
            if (start < 0) continue;
            r = Math.min(r, start);
        }
        return r;
    }

    private int maxEnd() {
        int r = 0;
        for (int group = 0; group <= this.groupCount(); ++group) {
            int end = this.groups[group * 2 + 1];
            if (end < 0) continue;
            r = Math.max(r, end);
        }
        return r;
    }

    public Matcher usePattern(Pattern newPattern) {
        int i;
        if (newPattern == null) {
            throw new IllegalArgumentException("Pattern cannot be null");
        }
        this.parentPattern = newPattern;
        this.namedGroups = null;
        int parentGroupCount = Math.max(newPattern.capturingGroupCount, 10);
        this.groups = new int[parentGroupCount * 2];
        this.locals = new int[newPattern.localCount];
        for (i = 0; i < this.groups.length; ++i) {
            this.groups[i] = -1;
        }
        for (i = 0; i < this.locals.length; ++i) {
            this.locals[i] = -1;
        }
        this.localsPos = new IntHashSet[this.parentPattern.localTCNCount];
        ++this.modCount;
        return this;
    }

    public Matcher reset() {
        int i;
        this.first = -1;
        this.last = 0;
        this.oldLast = -1;
        for (i = 0; i < this.groups.length; ++i) {
            this.groups[i] = -1;
        }
        for (i = 0; i < this.locals.length; ++i) {
            this.locals[i] = -1;
        }
        for (i = 0; i < this.localsPos.length; ++i) {
            if (this.localsPos[i] == null) continue;
            this.localsPos[i].clear();
        }
        this.lastAppendPosition = 0;
        this.from = 0;
        this.to = this.getTextLength();
        ++this.modCount;
        return this;
    }

    public Matcher reset(CharSequence input) {
        this.text = input;
        return this.reset();
    }

    @Override
    public int start() {
        this.checkMatch();
        return this.first;
    }

    @Override
    public int start(int group) {
        this.checkMatch();
        this.checkGroup(group);
        return this.groups[group * 2];
    }

    @Override
    public int start(String name) {
        return this.groups[this.getMatchedGroupIndex(name) * 2];
    }

    @Override
    public int end() {
        this.checkMatch();
        return this.last;
    }

    @Override
    public int end(int group) {
        this.checkMatch();
        this.checkGroup(group);
        return this.groups[group * 2 + 1];
    }

    @Override
    public int end(String name) {
        return this.groups[this.getMatchedGroupIndex(name) * 2 + 1];
    }

    @Override
    public String group() {
        return this.group(0);
    }

    @Override
    public String group(int group) {
        this.checkMatch();
        this.checkGroup(group);
        if (this.groups[group * 2] == -1 || this.groups[group * 2 + 1] == -1) {
            return null;
        }
        return this.getSubSequence(this.groups[group * 2], this.groups[group * 2 + 1]).toString();
    }

    @Override
    public String group(String name) {
        int group = this.getMatchedGroupIndex(name);
        if (this.groups[group * 2] == -1 || this.groups[group * 2 + 1] == -1) {
            return null;
        }
        return this.getSubSequence(this.groups[group * 2], this.groups[group * 2 + 1]).toString();
    }

    @Override
    public int groupCount() {
        return this.parentPattern.capturingGroupCount - 1;
    }

    public boolean matches() {
        return this.match(this.from, 1);
    }

    public boolean find() {
        int nextSearchIndex = this.last;
        if (nextSearchIndex == this.first) {
            ++nextSearchIndex;
        }
        if (nextSearchIndex < this.from) {
            nextSearchIndex = this.from;
        }
        if (nextSearchIndex > this.to) {
            for (int i = 0; i < this.groups.length; ++i) {
                this.groups[i] = -1;
            }
            return false;
        }
        return this.search(nextSearchIndex);
    }

    public boolean find(int start) {
        int limit = this.getTextLength();
        if (start < 0 || start > limit) {
            throw new IndexOutOfBoundsException("Illegal start index");
        }
        this.reset();
        return this.search(start);
    }

    public boolean lookingAt() {
        return this.match(this.from, 0);
    }

    public static String quoteReplacement(String s) {
        if (s.indexOf(92) == -1 && s.indexOf(36) == -1) {
            return s;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == '\\' || c == '$') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public Matcher appendReplacement(StringBuffer sb, String replacement) {
        this.checkMatch();
        int curLen = sb.length();
        try {
            sb.append(this.text, this.lastAppendPosition, this.first);
            this.appendExpandedReplacement(sb, replacement);
        }
        catch (IllegalArgumentException e) {
            sb.setLength(curLen);
            throw e;
        }
        this.lastAppendPosition = this.last;
        ++this.modCount;
        return this;
    }

    public Matcher appendReplacement(StringBuilder sb, String replacement) {
        this.checkMatch();
        int curLen = sb.length();
        try {
            sb.append(this.text, this.lastAppendPosition, this.first);
            this.appendExpandedReplacement(sb, replacement);
        }
        catch (IllegalArgumentException e) {
            sb.setLength(curLen);
            throw e;
        }
        this.lastAppendPosition = this.last;
        ++this.modCount;
        return this;
    }

    private void appendExpandedReplacement(Appendable app, String replacement) {
        try {
            int cursor = 0;
            while (cursor < replacement.length()) {
                char nextChar = replacement.charAt(cursor);
                if (nextChar == '\\') {
                    if (++cursor == replacement.length()) {
                        throw new IllegalArgumentException("character to be escaped is missing");
                    }
                    nextChar = replacement.charAt(cursor);
                    app.append(nextChar);
                    ++cursor;
                    continue;
                }
                if (nextChar == '$') {
                    if (++cursor == replacement.length()) {
                        throw new IllegalArgumentException("Illegal group reference: group index is missing");
                    }
                    nextChar = replacement.charAt(cursor);
                    int refNum = -1;
                    if (nextChar == '{') {
                        int begin = ++cursor;
                        while (cursor < replacement.length() && (ASCII.isLower(nextChar = replacement.charAt(cursor)) || ASCII.isUpper(nextChar) || ASCII.isDigit(nextChar))) {
                            ++cursor;
                        }
                        if (begin == cursor) {
                            throw new IllegalArgumentException("named capturing group has 0 length name");
                        }
                        if (nextChar != '}') {
                            throw new IllegalArgumentException("named capturing group is missing trailing '}'");
                        }
                        String gname = replacement.substring(begin, cursor);
                        if (ASCII.isDigit(gname.charAt(0))) {
                            throw new IllegalArgumentException("capturing group name {" + gname + "} starts with digit character");
                        }
                        Integer number = this.namedGroups().get(gname);
                        if (number == null) {
                            throw new IllegalArgumentException("No group with name {" + gname + "}");
                        }
                        refNum = number;
                        ++cursor;
                    } else {
                        int nextDigit;
                        refNum = nextChar - 48;
                        if (refNum < 0 || refNum > 9) {
                            throw new IllegalArgumentException("Illegal group reference");
                        }
                        ++cursor;
                        boolean done = false;
                        while (!done && cursor < replacement.length() && (nextDigit = replacement.charAt(cursor) - 48) >= 0 && nextDigit <= 9) {
                            int newRefNum = refNum * 10 + nextDigit;
                            if (this.groupCount() < newRefNum) {
                                done = true;
                                continue;
                            }
                            refNum = newRefNum;
                            ++cursor;
                        }
                    }
                    if (this.start(refNum) == -1 || this.end(refNum) == -1) continue;
                    app.append(this.text, this.start(refNum), this.end(refNum));
                    continue;
                }
                app.append(nextChar);
                ++cursor;
            }
        }
        catch (IOException e) {
            throw new AssertionError((Object)e.getMessage());
        }
    }

    public StringBuffer appendTail(StringBuffer sb) {
        sb.append(this.text, this.lastAppendPosition, this.getTextLength());
        return sb;
    }

    public StringBuilder appendTail(StringBuilder sb) {
        sb.append(this.text, this.lastAppendPosition, this.getTextLength());
        return sb;
    }

    public String replaceAll(String replacement) {
        this.reset();
        boolean result = this.find();
        if (result) {
            StringBuilder sb = new StringBuilder();
            do {
                this.appendReplacement(sb, replacement);
            } while (result = this.find());
            this.appendTail(sb);
            return sb.toString();
        }
        return this.text.toString();
    }

    public String replaceAll(Function<MatchResult, String> replacer) {
        Objects.requireNonNull(replacer);
        this.reset();
        boolean result = this.find();
        if (result) {
            StringBuilder sb = new StringBuilder();
            do {
                int ec = this.modCount;
                String replacement = replacer.apply(this);
                if (ec != this.modCount) {
                    throw new ConcurrentModificationException();
                }
                this.appendReplacement(sb, replacement);
            } while (result = this.find());
            this.appendTail(sb);
            return sb.toString();
        }
        return this.text.toString();
    }

    public Stream<MatchResult> results() {
        class MatchResultIterator
        implements Iterator<MatchResult> {
            int state = -1;
            int expectedCount = -1;

            MatchResultIterator() {
            }

            @Override
            public MatchResult next() {
                if (this.expectedCount >= 0 && this.expectedCount != Matcher.this.modCount) {
                    throw new ConcurrentModificationException();
                }
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                this.state = -1;
                return Matcher.this.toMatchResult();
            }

            @Override
            public boolean hasNext() {
                if (this.state >= 0) {
                    return this.state == 1;
                }
                if (this.expectedCount >= 0 && this.expectedCount != Matcher.this.modCount) {
                    return true;
                }
                boolean found = Matcher.this.find();
                this.state = found ? 1 : 0;
                this.expectedCount = Matcher.this.modCount;
                return found;
            }

            @Override
            public void forEachRemaining(Consumer<? super MatchResult> action) {
                if (this.expectedCount >= 0 && this.expectedCount != Matcher.this.modCount) {
                    throw new ConcurrentModificationException();
                }
                int s = this.state;
                if (s == 0) {
                    return;
                }
                this.state = 0;
                this.expectedCount = -1;
                if (s < 0 && !Matcher.this.find()) {
                    return;
                }
                do {
                    int ec = Matcher.this.modCount;
                    action.accept(Matcher.this.toMatchResult());
                    if (ec == Matcher.this.modCount) continue;
                    throw new ConcurrentModificationException();
                } while (Matcher.this.find());
            }
        }
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new MatchResultIterator(), 272), false);
    }

    public String replaceFirst(String replacement) {
        if (replacement == null) {
            throw new NullPointerException("replacement");
        }
        this.reset();
        if (!this.find()) {
            return this.text.toString();
        }
        StringBuilder sb = new StringBuilder();
        this.appendReplacement(sb, replacement);
        this.appendTail(sb);
        return sb.toString();
    }

    public String replaceFirst(Function<MatchResult, String> replacer) {
        Objects.requireNonNull(replacer);
        this.reset();
        if (!this.find()) {
            return this.text.toString();
        }
        StringBuilder sb = new StringBuilder();
        int ec = this.modCount;
        String replacement = replacer.apply(this);
        if (ec != this.modCount) {
            throw new ConcurrentModificationException();
        }
        this.appendReplacement(sb, replacement);
        this.appendTail(sb);
        return sb.toString();
    }

    public Matcher region(int start, int end) {
        if (start < 0 || start > this.getTextLength()) {
            throw new IndexOutOfBoundsException("start");
        }
        if (end < 0 || end > this.getTextLength()) {
            throw new IndexOutOfBoundsException("end");
        }
        if (start > end) {
            throw new IndexOutOfBoundsException("start > end");
        }
        this.reset();
        this.from = start;
        this.to = end;
        return this;
    }

    public int regionStart() {
        return this.from;
    }

    public int regionEnd() {
        return this.to;
    }

    public boolean hasTransparentBounds() {
        return this.transparentBounds;
    }

    public Matcher useTransparentBounds(boolean b) {
        this.transparentBounds = b;
        return this;
    }

    public boolean hasAnchoringBounds() {
        return this.anchoringBounds;
    }

    public Matcher useAnchoringBounds(boolean b) {
        this.anchoringBounds = b;
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("java.util.regex.Matcher").append("[pattern=").append(this.pattern()).append(" region=").append(this.regionStart()).append(',').append(this.regionEnd()).append(" lastmatch=");
        if (this.first >= 0 && this.group() != null) {
            sb.append(this.group());
        }
        sb.append(']');
        return sb.toString();
    }

    public boolean hitEnd() {
        return this.hitEnd;
    }

    public boolean requireEnd() {
        return this.requireEnd;
    }

    boolean search(int from) {
        int i;
        this.hitEnd = false;
        this.requireEnd = false;
        this.first = from = from < 0 ? 0 : from;
        this.oldLast = this.oldLast < 0 ? from : this.oldLast;
        for (i = 0; i < this.groups.length; ++i) {
            this.groups[i] = -1;
        }
        for (i = 0; i < this.localsPos.length; ++i) {
            if (this.localsPos[i] == null) continue;
            this.localsPos[i].clear();
        }
        this.acceptMode = 0;
        boolean result = this.parentPattern.root.match(this, from, this.text);
        if (!result) {
            this.first = -1;
        }
        this.oldLast = this.last;
        ++this.modCount;
        return result;
    }

    boolean match(int from, int anchor) {
        int i;
        this.hitEnd = false;
        this.requireEnd = false;
        this.first = from = from < 0 ? 0 : from;
        this.oldLast = this.oldLast < 0 ? from : this.oldLast;
        for (i = 0; i < this.groups.length; ++i) {
            this.groups[i] = -1;
        }
        for (i = 0; i < this.localsPos.length; ++i) {
            if (this.localsPos[i] == null) continue;
            this.localsPos[i].clear();
        }
        this.acceptMode = anchor;
        boolean result = this.parentPattern.matchRoot.match(this, from, this.text);
        if (!result) {
            this.first = -1;
        }
        this.oldLast = this.last;
        ++this.modCount;
        return result;
    }

    int getTextLength() {
        return this.text.length();
    }

    CharSequence getSubSequence(int beginIndex, int endIndex) {
        return this.text.subSequence(beginIndex, endIndex);
    }

    char charAt(int i) {
        return this.text.charAt(i);
    }

    int getMatchedGroupIndex(String name) {
        Objects.requireNonNull(name, "Group name");
        this.checkMatch();
        Integer number = this.namedGroups().get(name);
        if (number == null) {
            throw new IllegalArgumentException("No group with name <" + name + ">");
        }
        return number;
    }

    private void checkGroup(int group) {
        if (group < 0 || group > this.groupCount()) {
            throw new IndexOutOfBoundsException("No group " + group);
        }
    }

    private void checkMatch() {
        if (!this.hasMatch()) {
            throw new IllegalStateException("No match found");
        }
    }

    @Override
    public Map<String, Integer> namedGroups() {
        if (this.namedGroups == null) {
            this.namedGroups = this.parentPattern.namedGroups();
            return this.namedGroups;
        }
        return this.namedGroups;
    }

    @Override
    public boolean hasMatch() {
        return this.first >= 0;
    }

    private static class ImmutableMatchResult
    implements MatchResult {
        private final int first;
        private final int last;
        private final int groupCount;
        private final int[] groups;
        private final String text;
        private final Map<String, Integer> namedGroups;
        private final int minStart;

        ImmutableMatchResult(int first, int last, int groupCount, int[] groups, String text, Map<String, Integer> namedGroups, int minStart) {
            this.first = first;
            this.last = last;
            this.groupCount = groupCount;
            this.groups = groups;
            this.text = text;
            this.namedGroups = namedGroups;
            this.minStart = minStart;
        }

        @Override
        public int start() {
            this.checkMatch();
            return this.first;
        }

        @Override
        public int start(int group) {
            this.checkMatch();
            this.checkGroup(group);
            return this.groups[group * 2];
        }

        @Override
        public int end() {
            this.checkMatch();
            return this.last;
        }

        @Override
        public int end(int group) {
            this.checkMatch();
            this.checkGroup(group);
            return this.groups[group * 2 + 1];
        }

        @Override
        public int groupCount() {
            return this.groupCount;
        }

        @Override
        public String group() {
            this.checkMatch();
            return this.group(0);
        }

        @Override
        public String group(int group) {
            this.checkMatch();
            this.checkGroup(group);
            if (this.groups[group * 2] == -1 || this.groups[group * 2 + 1] == -1) {
                return null;
            }
            return this.text.substring(this.groups[group * 2] - this.minStart, this.groups[group * 2 + 1] - this.minStart);
        }

        @Override
        public Map<String, Integer> namedGroups() {
            return this.namedGroups;
        }

        @Override
        public boolean hasMatch() {
            return this.first >= 0;
        }

        private void checkGroup(int group) {
            if (group < 0 || group > this.groupCount) {
                throw new IndexOutOfBoundsException("No group " + group);
            }
        }

        private void checkMatch() {
            if (!this.hasMatch()) {
                throw new IllegalStateException("No match found");
            }
        }
    }
}

