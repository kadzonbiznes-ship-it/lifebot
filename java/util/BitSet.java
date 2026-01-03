/*
 * Decompiled with CFR 0.152.
 */
package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class BitSet
implements Cloneable,
Serializable {
    private static final int ADDRESS_BITS_PER_WORD = 6;
    private static final int BITS_PER_WORD = 64;
    private static final int BIT_INDEX_MASK = 63;
    private static final long WORD_MASK = -1L;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("bits", long[].class)};
    private long[] words;
    private transient int wordsInUse = 0;
    private transient boolean sizeIsSticky = false;
    private static final long serialVersionUID = 7997698588986878753L;

    private static int wordIndex(int bitIndex) {
        return bitIndex >> 6;
    }

    private void checkInvariants() {
        assert (this.wordsInUse == 0 || this.words[this.wordsInUse - 1] != 0L);
        assert (this.wordsInUse >= 0 && this.wordsInUse <= this.words.length);
        assert (this.wordsInUse == this.words.length || this.words[this.wordsInUse] == 0L);
    }

    private void recalculateWordsInUse() {
        int i;
        for (i = this.wordsInUse - 1; i >= 0 && this.words[i] == 0L; --i) {
        }
        this.wordsInUse = i + 1;
    }

    public BitSet() {
        this.initWords(64);
        this.sizeIsSticky = false;
    }

    public BitSet(int nbits) {
        if (nbits < 0) {
            throw new NegativeArraySizeException("nbits < 0: " + nbits);
        }
        this.initWords(nbits);
        this.sizeIsSticky = true;
    }

    private void initWords(int nbits) {
        this.words = new long[BitSet.wordIndex(nbits - 1) + 1];
    }

    private BitSet(long[] words) {
        this.words = words;
        this.wordsInUse = words.length;
        this.checkInvariants();
    }

    public static BitSet valueOf(long[] longs) {
        int n;
        for (n = longs.length; n > 0 && longs[n - 1] == 0L; --n) {
        }
        return new BitSet(Arrays.copyOf(longs, n));
    }

    public static BitSet valueOf(LongBuffer lb) {
        int n;
        lb = lb.slice();
        for (n = lb.remaining(); n > 0 && lb.get(n - 1) == 0L; --n) {
        }
        long[] words = new long[n];
        lb.get(words);
        return new BitSet(words);
    }

    public static BitSet valueOf(byte[] bytes) {
        return BitSet.valueOf(ByteBuffer.wrap(bytes));
    }

    public static BitSet valueOf(ByteBuffer bb) {
        int n;
        bb = bb.slice().order(ByteOrder.LITTLE_ENDIAN);
        for (n = bb.remaining(); n > 0 && bb.get(n - 1) == 0; --n) {
        }
        long[] words = new long[(n + 7) / 8];
        bb.limit(n);
        int i = 0;
        while (bb.remaining() >= 8) {
            words[i++] = bb.getLong();
        }
        int remaining = bb.remaining();
        for (int j = 0; j < remaining; ++j) {
            int n2 = i;
            words[n2] = words[n2] | ((long)bb.get() & 0xFFL) << 8 * j;
        }
        return new BitSet(words);
    }

    public byte[] toByteArray() {
        int n = this.wordsInUse;
        if (n == 0) {
            return new byte[0];
        }
        int len = 8 * (n - 1);
        for (long x = this.words[n - 1]; x != 0L; x >>>= 8) {
            ++len;
        }
        byte[] bytes = new byte[len];
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < n - 1; ++i) {
            bb.putLong(this.words[i]);
        }
        for (long x = this.words[n - 1]; x != 0L; x >>>= 8) {
            bb.put((byte)(x & 0xFFL));
        }
        return bytes;
    }

    public long[] toLongArray() {
        return Arrays.copyOf(this.words, this.wordsInUse);
    }

    private void ensureCapacity(int wordsRequired) {
        if (this.words.length < wordsRequired) {
            int request = Math.max(2 * this.words.length, wordsRequired);
            this.words = Arrays.copyOf(this.words, request);
            this.sizeIsSticky = false;
        }
    }

    private void expandTo(int wordIndex) {
        int wordsRequired = wordIndex + 1;
        if (this.wordsInUse < wordsRequired) {
            this.ensureCapacity(wordsRequired);
            this.wordsInUse = wordsRequired;
        }
    }

    private static void checkRange(int fromIndex, int toIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        }
        if (toIndex < 0) {
            throw new IndexOutOfBoundsException("toIndex < 0: " + toIndex);
        }
        if (fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " > toIndex: " + toIndex);
        }
    }

    public void flip(int bitIndex) {
        if (bitIndex < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        }
        int wordIndex = BitSet.wordIndex(bitIndex);
        this.expandTo(wordIndex);
        int n = wordIndex;
        this.words[n] = this.words[n] ^ 1L << bitIndex;
        this.recalculateWordsInUse();
        this.checkInvariants();
    }

    public void flip(int fromIndex, int toIndex) {
        BitSet.checkRange(fromIndex, toIndex);
        if (fromIndex == toIndex) {
            return;
        }
        int startWordIndex = BitSet.wordIndex(fromIndex);
        int endWordIndex = BitSet.wordIndex(toIndex - 1);
        this.expandTo(endWordIndex);
        long firstWordMask = -1L << fromIndex;
        long lastWordMask = -1L >>> -toIndex;
        if (startWordIndex == endWordIndex) {
            int n = startWordIndex;
            this.words[n] = this.words[n] ^ firstWordMask & lastWordMask;
        } else {
            int n = startWordIndex;
            this.words[n] = this.words[n] ^ firstWordMask;
            int i = startWordIndex + 1;
            while (i < endWordIndex) {
                int n2 = i++;
                this.words[n2] = this.words[n2] ^ 0xFFFFFFFFFFFFFFFFL;
            }
            int n3 = endWordIndex;
            this.words[n3] = this.words[n3] ^ lastWordMask;
        }
        this.recalculateWordsInUse();
        this.checkInvariants();
    }

    public void set(int bitIndex) {
        if (bitIndex < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        }
        int wordIndex = BitSet.wordIndex(bitIndex);
        this.expandTo(wordIndex);
        int n = wordIndex;
        this.words[n] = this.words[n] | 1L << bitIndex;
        this.checkInvariants();
    }

    public void set(int bitIndex, boolean value) {
        if (value) {
            this.set(bitIndex);
        } else {
            this.clear(bitIndex);
        }
    }

    public void set(int fromIndex, int toIndex) {
        BitSet.checkRange(fromIndex, toIndex);
        if (fromIndex == toIndex) {
            return;
        }
        int startWordIndex = BitSet.wordIndex(fromIndex);
        int endWordIndex = BitSet.wordIndex(toIndex - 1);
        this.expandTo(endWordIndex);
        long firstWordMask = -1L << fromIndex;
        long lastWordMask = -1L >>> -toIndex;
        if (startWordIndex == endWordIndex) {
            int n = startWordIndex;
            this.words[n] = this.words[n] | firstWordMask & lastWordMask;
        } else {
            int n = startWordIndex;
            this.words[n] = this.words[n] | firstWordMask;
            for (int i = startWordIndex + 1; i < endWordIndex; ++i) {
                this.words[i] = -1L;
            }
            int n2 = endWordIndex;
            this.words[n2] = this.words[n2] | lastWordMask;
        }
        this.checkInvariants();
    }

    public void set(int fromIndex, int toIndex, boolean value) {
        if (value) {
            this.set(fromIndex, toIndex);
        } else {
            this.clear(fromIndex, toIndex);
        }
    }

    public void clear(int bitIndex) {
        if (bitIndex < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        }
        int wordIndex = BitSet.wordIndex(bitIndex);
        if (wordIndex >= this.wordsInUse) {
            return;
        }
        int n = wordIndex;
        this.words[n] = this.words[n] & (1L << bitIndex ^ 0xFFFFFFFFFFFFFFFFL);
        this.recalculateWordsInUse();
        this.checkInvariants();
    }

    public void clear(int fromIndex, int toIndex) {
        BitSet.checkRange(fromIndex, toIndex);
        if (fromIndex == toIndex) {
            return;
        }
        int startWordIndex = BitSet.wordIndex(fromIndex);
        if (startWordIndex >= this.wordsInUse) {
            return;
        }
        int endWordIndex = BitSet.wordIndex(toIndex - 1);
        if (endWordIndex >= this.wordsInUse) {
            toIndex = this.length();
            endWordIndex = this.wordsInUse - 1;
        }
        long firstWordMask = -1L << fromIndex;
        long lastWordMask = -1L >>> -toIndex;
        if (startWordIndex == endWordIndex) {
            int n = startWordIndex;
            this.words[n] = this.words[n] & (firstWordMask & lastWordMask ^ 0xFFFFFFFFFFFFFFFFL);
        } else {
            int n = startWordIndex;
            this.words[n] = this.words[n] & (firstWordMask ^ 0xFFFFFFFFFFFFFFFFL);
            for (int i = startWordIndex + 1; i < endWordIndex; ++i) {
                this.words[i] = 0L;
            }
            int n2 = endWordIndex;
            this.words[n2] = this.words[n2] & (lastWordMask ^ 0xFFFFFFFFFFFFFFFFL);
        }
        this.recalculateWordsInUse();
        this.checkInvariants();
    }

    public void clear() {
        while (this.wordsInUse > 0) {
            this.words[--this.wordsInUse] = 0L;
        }
    }

    public boolean get(int bitIndex) {
        if (bitIndex < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        }
        this.checkInvariants();
        int wordIndex = BitSet.wordIndex(bitIndex);
        return wordIndex < this.wordsInUse && (this.words[wordIndex] & 1L << bitIndex) != 0L;
    }

    public BitSet get(int fromIndex, int toIndex) {
        BitSet.checkRange(fromIndex, toIndex);
        this.checkInvariants();
        int len = this.length();
        if (len <= fromIndex || fromIndex == toIndex) {
            return new BitSet(0);
        }
        if (toIndex > len) {
            toIndex = len;
        }
        BitSet result = new BitSet(toIndex - fromIndex);
        int targetWords = BitSet.wordIndex(toIndex - fromIndex - 1) + 1;
        int sourceIndex = BitSet.wordIndex(fromIndex);
        boolean wordAligned = (fromIndex & 0x3F) == 0;
        int i = 0;
        while (i < targetWords - 1) {
            result.words[i] = wordAligned ? this.words[sourceIndex] : this.words[sourceIndex] >>> fromIndex | this.words[sourceIndex + 1] << -fromIndex;
            ++i;
            ++sourceIndex;
        }
        long lastWordMask = -1L >>> -toIndex;
        result.words[targetWords - 1] = (toIndex - 1 & 0x3F) < (fromIndex & 0x3F) ? this.words[sourceIndex] >>> fromIndex | (this.words[sourceIndex + 1] & lastWordMask) << -fromIndex : (this.words[sourceIndex] & lastWordMask) >>> fromIndex;
        result.wordsInUse = targetWords;
        result.recalculateWordsInUse();
        result.checkInvariants();
        return result;
    }

    public int nextSetBit(int fromIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        }
        this.checkInvariants();
        int u = BitSet.wordIndex(fromIndex);
        if (u >= this.wordsInUse) {
            return -1;
        }
        long word = this.words[u] & -1L << fromIndex;
        while (word == 0L) {
            if (++u == this.wordsInUse) {
                return -1;
            }
            word = this.words[u];
        }
        return u * 64 + Long.numberOfTrailingZeros(word);
    }

    public int nextClearBit(int fromIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        }
        this.checkInvariants();
        int u = BitSet.wordIndex(fromIndex);
        if (u >= this.wordsInUse) {
            return fromIndex;
        }
        long word = (this.words[u] ^ 0xFFFFFFFFFFFFFFFFL) & -1L << fromIndex;
        while (word == 0L) {
            if (++u == this.wordsInUse) {
                return this.wordsInUse * 64;
            }
            word = this.words[u] ^ 0xFFFFFFFFFFFFFFFFL;
        }
        return u * 64 + Long.numberOfTrailingZeros(word);
    }

    public int previousSetBit(int fromIndex) {
        if (fromIndex < 0) {
            if (fromIndex == -1) {
                return -1;
            }
            throw new IndexOutOfBoundsException("fromIndex < -1: " + fromIndex);
        }
        this.checkInvariants();
        int u = BitSet.wordIndex(fromIndex);
        if (u >= this.wordsInUse) {
            return this.length() - 1;
        }
        long word = this.words[u] & -1L >>> -(fromIndex + 1);
        while (word == 0L) {
            if (u-- == 0) {
                return -1;
            }
            word = this.words[u];
        }
        return (u + 1) * 64 - 1 - Long.numberOfLeadingZeros(word);
    }

    public int previousClearBit(int fromIndex) {
        if (fromIndex < 0) {
            if (fromIndex == -1) {
                return -1;
            }
            throw new IndexOutOfBoundsException("fromIndex < -1: " + fromIndex);
        }
        this.checkInvariants();
        int u = BitSet.wordIndex(fromIndex);
        if (u >= this.wordsInUse) {
            return fromIndex;
        }
        long word = (this.words[u] ^ 0xFFFFFFFFFFFFFFFFL) & -1L >>> -(fromIndex + 1);
        while (word == 0L) {
            if (u-- == 0) {
                return -1;
            }
            word = this.words[u] ^ 0xFFFFFFFFFFFFFFFFL;
        }
        return (u + 1) * 64 - 1 - Long.numberOfLeadingZeros(word);
    }

    public int length() {
        if (this.wordsInUse == 0) {
            return 0;
        }
        return 64 * (this.wordsInUse - 1) + (64 - Long.numberOfLeadingZeros(this.words[this.wordsInUse - 1]));
    }

    public boolean isEmpty() {
        return this.wordsInUse == 0;
    }

    public boolean intersects(BitSet set) {
        for (int i = Math.min(this.wordsInUse, set.wordsInUse) - 1; i >= 0; --i) {
            if ((this.words[i] & set.words[i]) == 0L) continue;
            return true;
        }
        return false;
    }

    public int cardinality() {
        int sum = 0;
        for (int i = 0; i < this.wordsInUse; ++i) {
            sum += Long.bitCount(this.words[i]);
        }
        return sum;
    }

    public void and(BitSet set) {
        if (this == set) {
            return;
        }
        while (this.wordsInUse > set.wordsInUse) {
            this.words[--this.wordsInUse] = 0L;
        }
        for (int i = 0; i < this.wordsInUse; ++i) {
            int n = i;
            this.words[n] = this.words[n] & set.words[i];
        }
        this.recalculateWordsInUse();
        this.checkInvariants();
    }

    public void or(BitSet set) {
        if (this == set) {
            return;
        }
        int wordsInCommon = Math.min(this.wordsInUse, set.wordsInUse);
        if (this.wordsInUse < set.wordsInUse) {
            this.ensureCapacity(set.wordsInUse);
            this.wordsInUse = set.wordsInUse;
        }
        for (int i = 0; i < wordsInCommon; ++i) {
            int n = i;
            this.words[n] = this.words[n] | set.words[i];
        }
        if (wordsInCommon < set.wordsInUse) {
            System.arraycopy(set.words, wordsInCommon, this.words, wordsInCommon, this.wordsInUse - wordsInCommon);
        }
        this.checkInvariants();
    }

    public void xor(BitSet set) {
        int wordsInCommon = Math.min(this.wordsInUse, set.wordsInUse);
        if (this.wordsInUse < set.wordsInUse) {
            this.ensureCapacity(set.wordsInUse);
            this.wordsInUse = set.wordsInUse;
        }
        for (int i = 0; i < wordsInCommon; ++i) {
            int n = i;
            this.words[n] = this.words[n] ^ set.words[i];
        }
        if (wordsInCommon < set.wordsInUse) {
            System.arraycopy(set.words, wordsInCommon, this.words, wordsInCommon, set.wordsInUse - wordsInCommon);
        }
        this.recalculateWordsInUse();
        this.checkInvariants();
    }

    public void andNot(BitSet set) {
        for (int i = Math.min(this.wordsInUse, set.wordsInUse) - 1; i >= 0; --i) {
            int n = i;
            this.words[n] = this.words[n] & (set.words[i] ^ 0xFFFFFFFFFFFFFFFFL);
        }
        this.recalculateWordsInUse();
        this.checkInvariants();
    }

    public int hashCode() {
        long h = 1234L;
        int i = this.wordsInUse;
        while (--i >= 0) {
            h ^= this.words[i] * (long)(i + 1);
        }
        return (int)(h >> 32 ^ h);
    }

    public int size() {
        return this.words.length * 64;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BitSet)) {
            return false;
        }
        BitSet set = (BitSet)obj;
        if (this == obj) {
            return true;
        }
        this.checkInvariants();
        set.checkInvariants();
        if (this.wordsInUse != set.wordsInUse) {
            return false;
        }
        for (int i = 0; i < this.wordsInUse; ++i) {
            if (this.words[i] == set.words[i]) continue;
            return false;
        }
        return true;
    }

    public Object clone() {
        if (!this.sizeIsSticky) {
            this.trimToSize();
        }
        try {
            BitSet result = (BitSet)super.clone();
            result.words = (long[])this.words.clone();
            result.checkInvariants();
            return result;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    private void trimToSize() {
        if (this.wordsInUse != this.words.length) {
            this.words = Arrays.copyOf(this.words, this.wordsInUse);
            this.checkInvariants();
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        this.checkInvariants();
        if (!this.sizeIsSticky) {
            this.trimToSize();
        }
        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("bits", this.words);
        s.writeFields();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = s.readFields();
        this.words = (long[])fields.get("bits", null);
        this.wordsInUse = this.words.length;
        this.recalculateWordsInUse();
        this.sizeIsSticky = this.words.length > 0 && this.words[this.words.length - 1] == 0L;
        this.checkInvariants();
    }

    public String toString() {
        this.checkInvariants();
        int MAX_INITIAL_CAPACITY = 0x7FFFFFF7;
        int numBits = this.wordsInUse > 128 ? this.cardinality() : this.wordsInUse * 64;
        int initialCapacity = numBits <= 0x15555553 ? 6 * numBits + 2 : 0x7FFFFFF7;
        StringBuilder b = new StringBuilder(initialCapacity);
        b.append('{');
        int i = this.nextSetBit(0);
        if (i != -1) {
            b.append(i);
            while (++i >= 0 && (i = this.nextSetBit(i)) >= 0) {
                int endOfRun = this.nextClearBit(i);
                do {
                    b.append(", ").append(i);
                } while (++i != endOfRun);
            }
        }
        b.append('}');
        return b.toString();
    }

    public IntStream stream() {
        class BitSetSpliterator
        implements Spliterator.OfInt {
            private int index;
            private int fence;
            private int est;
            private boolean root;

            BitSetSpliterator(int origin, int fence, int est, boolean root) {
                this.index = origin;
                this.fence = fence;
                this.est = est;
                this.root = root;
            }

            private int getFence() {
                int hi = this.fence;
                if (hi < 0) {
                    this.fence = BitSet.this.wordsInUse >= BitSet.wordIndex(Integer.MAX_VALUE) ? Integer.MAX_VALUE : BitSet.this.wordsInUse << 6;
                    hi = this.fence;
                    this.est = BitSet.this.cardinality();
                    this.index = BitSet.this.nextSetBit(0);
                }
                return hi;
            }

            @Override
            public boolean tryAdvance(IntConsumer action) {
                Objects.requireNonNull(action);
                int hi = this.getFence();
                int i = this.index;
                if (i < 0 || i >= hi) {
                    if (i == Integer.MAX_VALUE && hi == Integer.MAX_VALUE) {
                        this.index = -1;
                        action.accept(Integer.MAX_VALUE);
                        return true;
                    }
                    return false;
                }
                this.index = BitSet.this.nextSetBit(i + 1, BitSet.wordIndex(hi - 1));
                action.accept(i);
                return true;
            }

            @Override
            public void forEachRemaining(IntConsumer action) {
                Objects.requireNonNull(action);
                int hi = this.getFence();
                int i = this.index;
                this.index = -1;
                if (i >= 0 && i < hi) {
                    action.accept(i++);
                    int u = BitSet.wordIndex(i);
                    int v = BitSet.wordIndex(hi - 1);
                    block0: while (u <= v && i <= hi) {
                        for (long word = BitSet.this.words[u] & -1L << i; word != 0L; word &= 1L << i ^ 0xFFFFFFFFFFFFFFFFL) {
                            i = (u << 6) + Long.numberOfTrailingZeros(word);
                            if (i >= hi) break block0;
                            action.accept(i);
                        }
                        i = ++u << 6;
                    }
                }
                if (i == Integer.MAX_VALUE && hi == Integer.MAX_VALUE) {
                    action.accept(Integer.MAX_VALUE);
                }
            }

            @Override
            public Spliterator.OfInt trySplit() {
                int hi = this.getFence();
                int lo = this.index;
                if (lo < 0) {
                    return null;
                }
                this.fence = hi < Integer.MAX_VALUE || !BitSet.this.get(Integer.MAX_VALUE) ? BitSet.this.previousSetBit(hi - 1) + 1 : Integer.MAX_VALUE;
                int mid = lo + (hi = this.fence) >>> 1;
                if (lo >= mid) {
                    return null;
                }
                this.index = BitSet.this.nextSetBit(mid, BitSet.wordIndex(hi - 1));
                this.root = false;
                return new BitSetSpliterator(lo, mid, this.est >>>= 1, false);
            }

            @Override
            public long estimateSize() {
                this.getFence();
                return this.est;
            }

            @Override
            public int characteristics() {
                return (this.root ? 64 : 0) | 0x10 | 1 | 4;
            }

            @Override
            public Comparator<? super Integer> getComparator() {
                return null;
            }
        }
        return StreamSupport.intStream(new BitSetSpliterator(0, -1, 0, true), false);
    }

    private int nextSetBit(int fromIndex, int toWordIndex) {
        int u = BitSet.wordIndex(fromIndex);
        if (u > toWordIndex) {
            return -1;
        }
        long word = this.words[u] & -1L << fromIndex;
        while (word == 0L) {
            if (++u > toWordIndex) {
                return -1;
            }
            word = this.words[u];
        }
        return u * 64 + Long.numberOfTrailingZeros(word);
    }
}

