/*
 * Decompiled with CFR 0.152.
 */
package java.util.stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import java.util.stream.AbstractSpinedBuffer;
import java.util.stream.Tripwire;

class SpinedBuffer<E>
extends AbstractSpinedBuffer
implements Consumer<E>,
Iterable<E> {
    protected E[] curChunk;
    protected E[][] spine;
    private static final int SPLITERATOR_CHARACTERISTICS = 16464;

    SpinedBuffer(int initialCapacity) {
        super(initialCapacity);
        this.curChunk = new Object[1 << this.initialChunkPower];
    }

    SpinedBuffer() {
        this.curChunk = new Object[1 << this.initialChunkPower];
    }

    protected long capacity() {
        return this.spineIndex == 0 ? (long)this.curChunk.length : this.priorElementCount[this.spineIndex] + (long)this.spine[this.spineIndex].length;
    }

    private void inflateSpine() {
        if (this.spine == null) {
            this.spine = new Object[8][];
            this.priorElementCount = new long[8];
            this.spine[0] = this.curChunk;
        }
    }

    protected final void ensureCapacity(long targetSize) {
        long capacity = this.capacity();
        if (targetSize > capacity) {
            this.inflateSpine();
            int i = this.spineIndex + 1;
            while (targetSize > capacity) {
                if (i >= this.spine.length) {
                    int newSpineSize = this.spine.length * 2;
                    this.spine = (Object[][])Arrays.copyOf(this.spine, newSpineSize);
                    this.priorElementCount = Arrays.copyOf(this.priorElementCount, newSpineSize);
                }
                int nextChunkSize = this.chunkSize(i);
                this.spine[i] = new Object[nextChunkSize];
                this.priorElementCount[i] = this.priorElementCount[i - 1] + (long)this.spine[i - 1].length;
                capacity += (long)nextChunkSize;
                ++i;
            }
        }
    }

    protected void increaseCapacity() {
        this.ensureCapacity(this.capacity() + 1L);
    }

    public E get(long index) {
        if (this.spineIndex == 0) {
            if (index < (long)this.elementIndex) {
                return this.curChunk[(int)index];
            }
            throw new IndexOutOfBoundsException(Long.toString(index));
        }
        if (index >= this.count()) {
            throw new IndexOutOfBoundsException(Long.toString(index));
        }
        for (int j = 0; j <= this.spineIndex; ++j) {
            if (index >= this.priorElementCount[j] + (long)this.spine[j].length) continue;
            return this.spine[j][(int)(index - this.priorElementCount[j])];
        }
        throw new IndexOutOfBoundsException(Long.toString(index));
    }

    public void copyInto(E[] array, int offset) {
        long finalOffset = (long)offset + this.count();
        if (finalOffset > (long)array.length || finalOffset < (long)offset) {
            throw new IndexOutOfBoundsException("does not fit");
        }
        if (this.spineIndex == 0) {
            System.arraycopy(this.curChunk, 0, array, offset, this.elementIndex);
        } else {
            for (int i = 0; i < this.spineIndex; ++i) {
                System.arraycopy(this.spine[i], 0, array, offset, this.spine[i].length);
                offset += this.spine[i].length;
            }
            if (this.elementIndex > 0) {
                System.arraycopy(this.curChunk, 0, array, offset, this.elementIndex);
            }
        }
    }

    public E[] asArray(IntFunction<E[]> arrayFactory) {
        long size = this.count();
        if (size >= 0x7FFFFFF7L) {
            throw new IllegalArgumentException("Stream size exceeds max array size");
        }
        E[] result = arrayFactory.apply((int)size);
        this.copyInto(result, 0);
        return result;
    }

    @Override
    public void clear() {
        if (this.spine != null) {
            this.curChunk = this.spine[0];
            for (int i = 0; i < this.curChunk.length; ++i) {
                this.curChunk[i] = null;
            }
            this.spine = null;
            this.priorElementCount = null;
        } else {
            for (int i = 0; i < this.elementIndex; ++i) {
                this.curChunk[i] = null;
            }
        }
        this.elementIndex = 0;
        this.spineIndex = 0;
    }

    @Override
    public Iterator<E> iterator() {
        return Spliterators.iterator(this.spliterator());
    }

    @Override
    public void forEach(Consumer<? super E> consumer) {
        for (int j = 0; j < this.spineIndex; ++j) {
            for (E t : this.spine[j]) {
                consumer.accept(t);
            }
        }
        for (int i = 0; i < this.elementIndex; ++i) {
            consumer.accept(this.curChunk[i]);
        }
    }

    @Override
    public void accept(E e) {
        if (this.elementIndex == this.curChunk.length) {
            this.inflateSpine();
            if (this.spineIndex + 1 >= this.spine.length || this.spine[this.spineIndex + 1] == null) {
                this.increaseCapacity();
            }
            this.elementIndex = 0;
            ++this.spineIndex;
            this.curChunk = this.spine[this.spineIndex];
        }
        this.curChunk[this.elementIndex++] = e;
    }

    public String toString() {
        ArrayList list = new ArrayList();
        this.forEach((Consumer<? super E>)((Consumer<Object>)list::add));
        return "SpinedBuffer:" + ((Object)list).toString();
    }

    @Override
    public Spliterator<E> spliterator() {
        class Splitr
        implements Spliterator<E> {
            int splSpineIndex;
            final int lastSpineIndex;
            int splElementIndex;
            final int lastSpineElementFence;
            E[] splChunk;

            Splitr(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                this.splSpineIndex = firstSpineIndex;
                this.lastSpineIndex = lastSpineIndex;
                this.splElementIndex = firstSpineElementIndex;
                this.lastSpineElementFence = lastSpineElementFence;
                assert (SpinedBuffer.this.spine != null || firstSpineIndex == 0 && lastSpineIndex == 0);
                this.splChunk = SpinedBuffer.this.spine == null ? SpinedBuffer.this.curChunk : SpinedBuffer.this.spine[firstSpineIndex];
            }

            @Override
            public long estimateSize() {
                return this.splSpineIndex == this.lastSpineIndex ? (long)this.lastSpineElementFence - (long)this.splElementIndex : SpinedBuffer.this.priorElementCount[this.lastSpineIndex] + (long)this.lastSpineElementFence - SpinedBuffer.this.priorElementCount[this.splSpineIndex] - (long)this.splElementIndex;
            }

            @Override
            public int characteristics() {
                return 16464;
            }

            @Override
            public boolean tryAdvance(Consumer<? super E> consumer) {
                Objects.requireNonNull(consumer);
                if (this.splSpineIndex < this.lastSpineIndex || this.splSpineIndex == this.lastSpineIndex && this.splElementIndex < this.lastSpineElementFence) {
                    consumer.accept(this.splChunk[this.splElementIndex++]);
                    if (this.splElementIndex == this.splChunk.length) {
                        this.splElementIndex = 0;
                        ++this.splSpineIndex;
                        if (SpinedBuffer.this.spine != null && this.splSpineIndex <= this.lastSpineIndex) {
                            this.splChunk = SpinedBuffer.this.spine[this.splSpineIndex];
                        }
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void forEachRemaining(Consumer<? super E> consumer) {
                Objects.requireNonNull(consumer);
                if (this.splSpineIndex < this.lastSpineIndex || this.splSpineIndex == this.lastSpineIndex && this.splElementIndex < this.lastSpineElementFence) {
                    int i = this.splElementIndex;
                    for (int sp = this.splSpineIndex; sp < this.lastSpineIndex; ++sp) {
                        E[] chunk = SpinedBuffer.this.spine[sp];
                        while (i < chunk.length) {
                            consumer.accept(chunk[i]);
                            ++i;
                        }
                        i = 0;
                    }
                    E[] chunk = this.splSpineIndex == this.lastSpineIndex ? this.splChunk : SpinedBuffer.this.spine[this.lastSpineIndex];
                    int hElementIndex = this.lastSpineElementFence;
                    while (i < hElementIndex) {
                        consumer.accept(chunk[i]);
                        ++i;
                    }
                    this.splSpineIndex = this.lastSpineIndex;
                    this.splElementIndex = this.lastSpineElementFence;
                }
            }

            @Override
            public Spliterator<E> trySplit() {
                if (this.splSpineIndex < this.lastSpineIndex) {
                    Splitr ret = new Splitr(this.splSpineIndex, this.lastSpineIndex - 1, this.splElementIndex, SpinedBuffer.this.spine[this.lastSpineIndex - 1].length);
                    this.splSpineIndex = this.lastSpineIndex;
                    this.splElementIndex = 0;
                    this.splChunk = SpinedBuffer.this.spine[this.splSpineIndex];
                    return ret;
                }
                if (this.splSpineIndex == this.lastSpineIndex) {
                    int t = (this.lastSpineElementFence - this.splElementIndex) / 2;
                    if (t == 0) {
                        return null;
                    }
                    Spliterator ret = Arrays.spliterator(this.splChunk, this.splElementIndex, this.splElementIndex + t);
                    this.splElementIndex += t;
                    return ret;
                }
                return null;
            }
        }
        return new Splitr(0, this.spineIndex, 0, this.elementIndex);
    }

    static class OfDouble
    extends OfPrimitive<Double, double[], DoubleConsumer>
    implements DoubleConsumer {
        OfDouble() {
        }

        OfDouble(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public void forEach(Consumer<? super Double> consumer) {
            if (consumer instanceof DoubleConsumer) {
                this.forEach((DoubleConsumer)((Object)consumer));
            } else {
                if (Tripwire.ENABLED) {
                    Tripwire.trip(this.getClass(), "{0} calling SpinedBuffer.OfDouble.forEach(Consumer)");
                }
                this.spliterator().forEachRemaining(consumer);
            }
        }

        protected double[][] newArrayArray(int size) {
            return new double[size][];
        }

        @Override
        public double[] newArray(int size) {
            return new double[size];
        }

        @Override
        protected int arrayLength(double[] array) {
            return array.length;
        }

        @Override
        protected void arrayForEach(double[] array, int from, int to, DoubleConsumer consumer) {
            for (int i = from; i < to; ++i) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public void accept(double i) {
            this.preAccept();
            ((double[])this.curChunk)[this.elementIndex++] = i;
        }

        public double get(long index) {
            int ch = this.chunkFor(index);
            if (this.spineIndex == 0 && ch == 0) {
                return ((double[])this.curChunk)[(int)index];
            }
            return ((double[][])this.spine)[ch][(int)(index - this.priorElementCount[ch])];
        }

        public PrimitiveIterator.OfDouble iterator() {
            return Spliterators.iterator(this.spliterator());
        }

        public Spliterator.OfDouble spliterator() {
            class Splitr
            extends OfPrimitive.BaseSpliterator<Spliterator.OfDouble>
            implements Spliterator.OfDouble {
                Splitr(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    super(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                Splitr newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    return new Splitr(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                void arrayForOne(double[] array, int index, DoubleConsumer consumer) {
                    consumer.accept(array[index]);
                }

                @Override
                Spliterator.OfDouble arraySpliterator(double[] array, int offset, int len) {
                    return Arrays.spliterator(array, offset, offset + len);
                }
            }
            return new Splitr(0, this.spineIndex, 0, this.elementIndex);
        }

        public String toString() {
            double[] array = (double[])this.asPrimitiveArray();
            if (array.length < 200) {
                return String.format("%s[length=%d, chunks=%d]%s", this.getClass().getSimpleName(), array.length, this.spineIndex, Arrays.toString(array));
            }
            double[] array2 = Arrays.copyOf(array, 200);
            return String.format("%s[length=%d, chunks=%d]%s...", this.getClass().getSimpleName(), array.length, this.spineIndex, Arrays.toString(array2));
        }
    }

    static class OfLong
    extends OfPrimitive<Long, long[], LongConsumer>
    implements LongConsumer {
        OfLong() {
        }

        OfLong(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public void forEach(Consumer<? super Long> consumer) {
            if (consumer instanceof LongConsumer) {
                this.forEach((LongConsumer)((Object)consumer));
            } else {
                if (Tripwire.ENABLED) {
                    Tripwire.trip(this.getClass(), "{0} calling SpinedBuffer.OfLong.forEach(Consumer)");
                }
                this.spliterator().forEachRemaining(consumer);
            }
        }

        protected long[][] newArrayArray(int size) {
            return new long[size][];
        }

        @Override
        public long[] newArray(int size) {
            return new long[size];
        }

        @Override
        protected int arrayLength(long[] array) {
            return array.length;
        }

        @Override
        protected void arrayForEach(long[] array, int from, int to, LongConsumer consumer) {
            for (int i = from; i < to; ++i) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public void accept(long i) {
            this.preAccept();
            ((long[])this.curChunk)[this.elementIndex++] = i;
        }

        public long get(long index) {
            int ch = this.chunkFor(index);
            if (this.spineIndex == 0 && ch == 0) {
                return ((long[])this.curChunk)[(int)index];
            }
            return ((long[][])this.spine)[ch][(int)(index - this.priorElementCount[ch])];
        }

        public PrimitiveIterator.OfLong iterator() {
            return Spliterators.iterator(this.spliterator());
        }

        public Spliterator.OfLong spliterator() {
            class Splitr
            extends OfPrimitive.BaseSpliterator<Spliterator.OfLong>
            implements Spliterator.OfLong {
                Splitr(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    super(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                Splitr newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    return new Splitr(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                void arrayForOne(long[] array, int index, LongConsumer consumer) {
                    consumer.accept(array[index]);
                }

                @Override
                Spliterator.OfLong arraySpliterator(long[] array, int offset, int len) {
                    return Arrays.spliterator(array, offset, offset + len);
                }
            }
            return new Splitr(0, this.spineIndex, 0, this.elementIndex);
        }

        public String toString() {
            long[] array = (long[])this.asPrimitiveArray();
            if (array.length < 200) {
                return String.format("%s[length=%d, chunks=%d]%s", this.getClass().getSimpleName(), array.length, this.spineIndex, Arrays.toString(array));
            }
            long[] array2 = Arrays.copyOf(array, 200);
            return String.format("%s[length=%d, chunks=%d]%s...", this.getClass().getSimpleName(), array.length, this.spineIndex, Arrays.toString(array2));
        }
    }

    static class OfInt
    extends OfPrimitive<Integer, int[], IntConsumer>
    implements IntConsumer {
        OfInt() {
        }

        OfInt(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public void forEach(Consumer<? super Integer> consumer) {
            if (consumer instanceof IntConsumer) {
                this.forEach((IntConsumer)((Object)consumer));
            } else {
                if (Tripwire.ENABLED) {
                    Tripwire.trip(this.getClass(), "{0} calling SpinedBuffer.OfInt.forEach(Consumer)");
                }
                this.spliterator().forEachRemaining(consumer);
            }
        }

        protected int[][] newArrayArray(int size) {
            return new int[size][];
        }

        @Override
        public int[] newArray(int size) {
            return new int[size];
        }

        @Override
        protected int arrayLength(int[] array) {
            return array.length;
        }

        @Override
        protected void arrayForEach(int[] array, int from, int to, IntConsumer consumer) {
            for (int i = from; i < to; ++i) {
                consumer.accept(array[i]);
            }
        }

        @Override
        public void accept(int i) {
            this.preAccept();
            ((int[])this.curChunk)[this.elementIndex++] = i;
        }

        public int get(long index) {
            int ch = this.chunkFor(index);
            if (this.spineIndex == 0 && ch == 0) {
                return ((int[])this.curChunk)[(int)index];
            }
            return ((int[][])this.spine)[ch][(int)(index - this.priorElementCount[ch])];
        }

        public PrimitiveIterator.OfInt iterator() {
            return Spliterators.iterator(this.spliterator());
        }

        public Spliterator.OfInt spliterator() {
            class Splitr
            extends OfPrimitive.BaseSpliterator<Spliterator.OfInt>
            implements Spliterator.OfInt {
                Splitr(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    super(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                Splitr newSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                    return new Splitr(firstSpineIndex, lastSpineIndex, firstSpineElementIndex, lastSpineElementFence);
                }

                @Override
                void arrayForOne(int[] array, int index, IntConsumer consumer) {
                    consumer.accept(array[index]);
                }

                @Override
                Spliterator.OfInt arraySpliterator(int[] array, int offset, int len) {
                    return Arrays.spliterator(array, offset, offset + len);
                }
            }
            return new Splitr(0, this.spineIndex, 0, this.elementIndex);
        }

        public String toString() {
            int[] array = (int[])this.asPrimitiveArray();
            if (array.length < 200) {
                return String.format("%s[length=%d, chunks=%d]%s", this.getClass().getSimpleName(), array.length, this.spineIndex, Arrays.toString(array));
            }
            int[] array2 = Arrays.copyOf(array, 200);
            return String.format("%s[length=%d, chunks=%d]%s...", this.getClass().getSimpleName(), array.length, this.spineIndex, Arrays.toString(array2));
        }
    }

    static abstract class OfPrimitive<E, T_ARR, T_CONS>
    extends AbstractSpinedBuffer
    implements Iterable<E> {
        T_ARR curChunk;
        T_ARR[] spine;

        OfPrimitive(int initialCapacity) {
            super(initialCapacity);
            this.curChunk = this.newArray(1 << this.initialChunkPower);
        }

        OfPrimitive() {
            this.curChunk = this.newArray(1 << this.initialChunkPower);
        }

        @Override
        public abstract Iterator<E> iterator();

        @Override
        public abstract void forEach(Consumer<? super E> var1);

        protected abstract T_ARR[] newArrayArray(int var1);

        public abstract T_ARR newArray(int var1);

        protected abstract int arrayLength(T_ARR var1);

        protected abstract void arrayForEach(T_ARR var1, int var2, int var3, T_CONS var4);

        protected long capacity() {
            return this.spineIndex == 0 ? (long)this.arrayLength(this.curChunk) : this.priorElementCount[this.spineIndex] + (long)this.arrayLength(this.spine[this.spineIndex]);
        }

        private void inflateSpine() {
            if (this.spine == null) {
                this.spine = this.newArrayArray(8);
                this.priorElementCount = new long[8];
                this.spine[0] = this.curChunk;
            }
        }

        protected final void ensureCapacity(long targetSize) {
            long capacity = this.capacity();
            if (targetSize > capacity) {
                this.inflateSpine();
                int i = this.spineIndex + 1;
                while (targetSize > capacity) {
                    if (i >= this.spine.length) {
                        int newSpineSize = this.spine.length * 2;
                        this.spine = Arrays.copyOf(this.spine, newSpineSize);
                        this.priorElementCount = Arrays.copyOf(this.priorElementCount, newSpineSize);
                    }
                    int nextChunkSize = this.chunkSize(i);
                    this.spine[i] = this.newArray(nextChunkSize);
                    this.priorElementCount[i] = this.priorElementCount[i - 1] + (long)this.arrayLength(this.spine[i - 1]);
                    capacity += (long)nextChunkSize;
                    ++i;
                }
            }
        }

        protected void increaseCapacity() {
            this.ensureCapacity(this.capacity() + 1L);
        }

        protected int chunkFor(long index) {
            if (this.spineIndex == 0) {
                if (index < (long)this.elementIndex) {
                    return 0;
                }
                throw new IndexOutOfBoundsException(Long.toString(index));
            }
            if (index >= this.count()) {
                throw new IndexOutOfBoundsException(Long.toString(index));
            }
            for (int j = 0; j <= this.spineIndex; ++j) {
                if (index >= this.priorElementCount[j] + (long)this.arrayLength(this.spine[j])) continue;
                return j;
            }
            throw new IndexOutOfBoundsException(Long.toString(index));
        }

        public void copyInto(T_ARR array, int offset) {
            long finalOffset = (long)offset + this.count();
            if (finalOffset > (long)this.arrayLength(array) || finalOffset < (long)offset) {
                throw new IndexOutOfBoundsException("does not fit");
            }
            if (this.spineIndex == 0) {
                System.arraycopy(this.curChunk, 0, array, offset, this.elementIndex);
            } else {
                for (int i = 0; i < this.spineIndex; ++i) {
                    System.arraycopy(this.spine[i], 0, array, offset, this.arrayLength(this.spine[i]));
                    offset += this.arrayLength(this.spine[i]);
                }
                if (this.elementIndex > 0) {
                    System.arraycopy(this.curChunk, 0, array, offset, this.elementIndex);
                }
            }
        }

        public T_ARR asPrimitiveArray() {
            long size = this.count();
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            T_ARR result = this.newArray((int)size);
            this.copyInto(result, 0);
            return result;
        }

        protected void preAccept() {
            if (this.elementIndex == this.arrayLength(this.curChunk)) {
                this.inflateSpine();
                if (this.spineIndex + 1 >= this.spine.length || this.spine[this.spineIndex + 1] == null) {
                    this.increaseCapacity();
                }
                this.elementIndex = 0;
                ++this.spineIndex;
                this.curChunk = this.spine[this.spineIndex];
            }
        }

        @Override
        public void clear() {
            if (this.spine != null) {
                this.curChunk = this.spine[0];
                this.spine = null;
                this.priorElementCount = null;
            }
            this.elementIndex = 0;
            this.spineIndex = 0;
        }

        public void forEach(T_CONS consumer) {
            for (int j = 0; j < this.spineIndex; ++j) {
                this.arrayForEach(this.spine[j], 0, this.arrayLength(this.spine[j]), consumer);
            }
            this.arrayForEach(this.curChunk, 0, this.elementIndex, consumer);
        }

        abstract class BaseSpliterator<T_SPLITR extends Spliterator.OfPrimitive<E, T_CONS, T_SPLITR>>
        implements Spliterator.OfPrimitive<E, T_CONS, T_SPLITR> {
            int splSpineIndex;
            final int lastSpineIndex;
            int splElementIndex;
            final int lastSpineElementFence;
            T_ARR splChunk;

            BaseSpliterator(int firstSpineIndex, int lastSpineIndex, int firstSpineElementIndex, int lastSpineElementFence) {
                this.splSpineIndex = firstSpineIndex;
                this.lastSpineIndex = lastSpineIndex;
                this.splElementIndex = firstSpineElementIndex;
                this.lastSpineElementFence = lastSpineElementFence;
                assert (OfPrimitive.this.spine != null || firstSpineIndex == 0 && lastSpineIndex == 0);
                this.splChunk = OfPrimitive.this.spine == null ? OfPrimitive.this.curChunk : OfPrimitive.this.spine[firstSpineIndex];
            }

            abstract T_SPLITR newSpliterator(int var1, int var2, int var3, int var4);

            abstract void arrayForOne(T_ARR var1, int var2, T_CONS var3);

            abstract T_SPLITR arraySpliterator(T_ARR var1, int var2, int var3);

            @Override
            public long estimateSize() {
                return this.splSpineIndex == this.lastSpineIndex ? (long)this.lastSpineElementFence - (long)this.splElementIndex : OfPrimitive.this.priorElementCount[this.lastSpineIndex] + (long)this.lastSpineElementFence - OfPrimitive.this.priorElementCount[this.splSpineIndex] - (long)this.splElementIndex;
            }

            @Override
            public int characteristics() {
                return 16464;
            }

            @Override
            public boolean tryAdvance(T_CONS consumer) {
                Objects.requireNonNull(consumer);
                if (this.splSpineIndex < this.lastSpineIndex || this.splSpineIndex == this.lastSpineIndex && this.splElementIndex < this.lastSpineElementFence) {
                    this.arrayForOne(this.splChunk, this.splElementIndex++, consumer);
                    if (this.splElementIndex == OfPrimitive.this.arrayLength(this.splChunk)) {
                        this.splElementIndex = 0;
                        ++this.splSpineIndex;
                        if (OfPrimitive.this.spine != null && this.splSpineIndex <= this.lastSpineIndex) {
                            this.splChunk = OfPrimitive.this.spine[this.splSpineIndex];
                        }
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void forEachRemaining(T_CONS consumer) {
                Objects.requireNonNull(consumer);
                if (this.splSpineIndex < this.lastSpineIndex || this.splSpineIndex == this.lastSpineIndex && this.splElementIndex < this.lastSpineElementFence) {
                    int i = this.splElementIndex;
                    for (int sp = this.splSpineIndex; sp < this.lastSpineIndex; ++sp) {
                        Object chunk = OfPrimitive.this.spine[sp];
                        OfPrimitive.this.arrayForEach(chunk, i, OfPrimitive.this.arrayLength(chunk), consumer);
                        i = 0;
                    }
                    Object chunk = this.splSpineIndex == this.lastSpineIndex ? this.splChunk : OfPrimitive.this.spine[this.lastSpineIndex];
                    OfPrimitive.this.arrayForEach(chunk, i, this.lastSpineElementFence, consumer);
                    this.splSpineIndex = this.lastSpineIndex;
                    this.splElementIndex = this.lastSpineElementFence;
                }
            }

            @Override
            public T_SPLITR trySplit() {
                if (this.splSpineIndex < this.lastSpineIndex) {
                    T_SPLITR ret = this.newSpliterator(this.splSpineIndex, this.lastSpineIndex - 1, this.splElementIndex, OfPrimitive.this.arrayLength(OfPrimitive.this.spine[this.lastSpineIndex - 1]));
                    this.splSpineIndex = this.lastSpineIndex;
                    this.splElementIndex = 0;
                    this.splChunk = OfPrimitive.this.spine[this.splSpineIndex];
                    return ret;
                }
                if (this.splSpineIndex == this.lastSpineIndex) {
                    int t = (this.lastSpineElementFence - this.splElementIndex) / 2;
                    if (t == 0) {
                        return null;
                    }
                    T_SPLITR ret = this.arraySpliterator(this.splChunk, this.splElementIndex, t);
                    this.splElementIndex += t;
                    return ret;
                }
                return null;
            }
        }
    }
}

