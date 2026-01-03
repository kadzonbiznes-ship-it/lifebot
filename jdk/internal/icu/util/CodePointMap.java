/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.icu.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class CodePointMap
implements Iterable<Range> {
    protected CodePointMap() {
    }

    public abstract int get(int var1);

    public abstract boolean getRange(int var1, ValueFilter var2, Range var3);

    public boolean getRange(int start, RangeOption option, int surrogateValue, ValueFilter filter, Range range) {
        assert (option != null);
        if (!this.getRange(start, filter, range)) {
            return false;
        }
        if (option == RangeOption.NORMAL) {
            return true;
        }
        int surrEnd = option == RangeOption.FIXED_ALL_SURROGATES ? 57343 : 56319;
        int end = range.end;
        if (end < 55295 || start > surrEnd) {
            return true;
        }
        if (range.value == surrogateValue) {
            if (end >= surrEnd) {
                return true;
            }
        } else {
            if (start <= 55295) {
                range.end = 55295;
                return true;
            }
            range.value = surrogateValue;
            if (end > surrEnd) {
                range.end = surrEnd;
                return true;
            }
        }
        if (this.getRange(surrEnd + 1, filter, range) && range.value == surrogateValue) {
            range.start = start;
            return true;
        }
        range.start = start;
        range.end = surrEnd;
        range.value = surrogateValue;
        return true;
    }

    @Override
    public Iterator<Range> iterator() {
        return new RangeIterator();
    }

    public StringIterator stringIterator(CharSequence s, int sIndex) {
        return new StringIterator(s, sIndex);
    }

    public static interface ValueFilter {
        public int apply(int var1);
    }

    public static final class Range {
        private int start = -1;
        private int end = -1;
        private int value = 0;

        public int getStart() {
            return this.start;
        }

        public int getEnd() {
            return this.end;
        }

        public int getValue() {
            return this.value;
        }

        public void set(int start, int end, int value) {
            this.start = start;
            this.end = end;
            this.value = value;
        }
    }

    public static enum RangeOption {
        NORMAL,
        FIXED_LEAD_SURROGATES,
        FIXED_ALL_SURROGATES;

    }

    private final class RangeIterator
    implements Iterator<Range> {
        private Range range = new Range();

        private RangeIterator() {
        }

        @Override
        public boolean hasNext() {
            return -1 <= this.range.end && this.range.end < 0x10FFFF;
        }

        @Override
        public Range next() {
            if (CodePointMap.this.getRange(this.range.end + 1, null, this.range)) {
                return this.range;
            }
            throw new NoSuchElementException();
        }

        @Override
        public final void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public class StringIterator {
        @Deprecated
        protected CharSequence s;
        @Deprecated
        protected int sIndex;
        @Deprecated
        protected int c;
        @Deprecated
        protected int value;

        @Deprecated
        protected StringIterator(CharSequence s, int sIndex) {
            this.s = s;
            this.sIndex = sIndex;
            this.c = -1;
            this.value = 0;
        }

        public void reset(CharSequence s, int sIndex) {
            this.s = s;
            this.sIndex = sIndex;
            this.c = -1;
            this.value = 0;
        }

        public boolean next() {
            if (this.sIndex >= this.s.length()) {
                return false;
            }
            this.c = Character.codePointAt(this.s, this.sIndex);
            this.sIndex += Character.charCount(this.c);
            this.value = CodePointMap.this.get(this.c);
            return true;
        }

        public boolean previous() {
            if (this.sIndex <= 0) {
                return false;
            }
            this.c = Character.codePointBefore(this.s, this.sIndex);
            this.sIndex -= Character.charCount(this.c);
            this.value = CodePointMap.this.get(this.c);
            return true;
        }

        public final int getIndex() {
            return this.sIndex;
        }

        public final int getCodePoint() {
            return this.c;
        }

        public final int getValue() {
            return this.value;
        }
    }
}

