/*
 * Decompiled with CFR 0.152.
 */
package java.util.stream;

import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.SpinedBuffer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jdk.internal.vm.annotation.IntrinsicCandidate;

final class Streams {
    private Streams() {
        throw new Error("no instances");
    }

    static Runnable composeWithExceptions(final Runnable a, final Runnable b) {
        return new Runnable(){

            @Override
            public void run() {
                try {
                    a.run();
                }
                catch (Throwable e1) {
                    try {
                        b.run();
                    }
                    catch (Throwable e2) {
                        try {
                            e1.addSuppressed(e2);
                        }
                        catch (Throwable throwable) {
                            // empty catch block
                        }
                    }
                    throw e1;
                }
                b.run();
            }
        };
    }

    static Runnable composedClose(final BaseStream<?, ?> a, final BaseStream<?, ?> b) {
        return new Runnable(){

            @Override
            public void run() {
                try {
                    a.close();
                }
                catch (Throwable e1) {
                    try {
                        b.close();
                    }
                    catch (Throwable e2) {
                        try {
                            e1.addSuppressed(e2);
                        }
                        catch (Throwable throwable) {
                            // empty catch block
                        }
                    }
                    throw e1;
                }
                b.close();
            }
        };
    }

    static abstract class ConcatSpliterator<T, T_SPLITR extends Spliterator<T>>
    implements Spliterator<T> {
        protected final T_SPLITR aSpliterator;
        protected final T_SPLITR bSpliterator;
        boolean beforeSplit;
        final boolean unsized;

        public ConcatSpliterator(T_SPLITR aSpliterator, T_SPLITR bSpliterator) {
            this.aSpliterator = aSpliterator;
            this.bSpliterator = bSpliterator;
            this.beforeSplit = true;
            this.unsized = aSpliterator.estimateSize() + bSpliterator.estimateSize() < 0L;
        }

        public T_SPLITR trySplit() {
            Object ret = this.beforeSplit ? this.aSpliterator : this.bSpliterator.trySplit();
            this.beforeSplit = false;
            return ret;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> consumer) {
            boolean hasNext;
            if (this.beforeSplit) {
                hasNext = this.aSpliterator.tryAdvance(consumer);
                if (!hasNext) {
                    this.beforeSplit = false;
                    hasNext = this.bSpliterator.tryAdvance(consumer);
                }
            } else {
                hasNext = this.bSpliterator.tryAdvance(consumer);
            }
            return hasNext;
        }

        @Override
        public void forEachRemaining(Consumer<? super T> consumer) {
            if (this.beforeSplit) {
                this.aSpliterator.forEachRemaining(consumer);
            }
            this.bSpliterator.forEachRemaining(consumer);
        }

        @Override
        public long estimateSize() {
            if (this.beforeSplit) {
                long size = this.aSpliterator.estimateSize() + this.bSpliterator.estimateSize();
                return size >= 0L ? size : Long.MAX_VALUE;
            }
            return this.bSpliterator.estimateSize();
        }

        @Override
        public int characteristics() {
            if (this.beforeSplit) {
                return this.aSpliterator.characteristics() & this.bSpliterator.characteristics() & ~(5 | (this.unsized ? 16448 : 0));
            }
            return this.bSpliterator.characteristics();
        }

        @Override
        public Comparator<? super T> getComparator() {
            if (this.beforeSplit) {
                throw new IllegalStateException();
            }
            return this.bSpliterator.getComparator();
        }

        static class OfDouble
        extends OfPrimitive<Double, DoubleConsumer, Spliterator.OfDouble>
        implements Spliterator.OfDouble {
            OfDouble(Spliterator.OfDouble aSpliterator, Spliterator.OfDouble bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        static class OfLong
        extends OfPrimitive<Long, LongConsumer, Spliterator.OfLong>
        implements Spliterator.OfLong {
            OfLong(Spliterator.OfLong aSpliterator, Spliterator.OfLong bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        static class OfInt
        extends OfPrimitive<Integer, IntConsumer, Spliterator.OfInt>
        implements Spliterator.OfInt {
            OfInt(Spliterator.OfInt aSpliterator, Spliterator.OfInt bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        private static abstract class OfPrimitive<T, T_CONS, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>>
        extends ConcatSpliterator<T, T_SPLITR>
        implements Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {
            private OfPrimitive(T_SPLITR aSpliterator, T_SPLITR bSpliterator) {
                super(aSpliterator, bSpliterator);
            }

            @Override
            public boolean tryAdvance(T_CONS action) {
                boolean hasNext;
                if (this.beforeSplit) {
                    hasNext = ((Spliterator.OfPrimitive)this.aSpliterator).tryAdvance(action);
                    if (!hasNext) {
                        this.beforeSplit = false;
                        hasNext = ((Spliterator.OfPrimitive)this.bSpliterator).tryAdvance(action);
                    }
                } else {
                    hasNext = ((Spliterator.OfPrimitive)this.bSpliterator).tryAdvance(action);
                }
                return hasNext;
            }

            @Override
            public void forEachRemaining(T_CONS action) {
                if (this.beforeSplit) {
                    ((Spliterator.OfPrimitive)this.aSpliterator).forEachRemaining(action);
                }
                ((Spliterator.OfPrimitive)this.bSpliterator).forEachRemaining(action);
            }
        }

        static class OfRef<T>
        extends ConcatSpliterator<T, Spliterator<T>> {
            OfRef(Spliterator<T> aSpliterator, Spliterator<T> bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }
    }

    static final class DoubleStreamBuilderImpl
    extends AbstractStreamBuilderImpl<Double, Spliterator.OfDouble>
    implements DoubleStream.Builder,
    Spliterator.OfDouble {
        double first;
        SpinedBuffer.OfDouble buffer;

        DoubleStreamBuilderImpl() {
        }

        DoubleStreamBuilderImpl(double t) {
            this.first = t;
            this.count = -2;
        }

        @Override
        public void accept(double t) {
            if (this.count == 0) {
                this.first = t;
                ++this.count;
            } else if (this.count > 0) {
                if (this.buffer == null) {
                    this.buffer = new SpinedBuffer.OfDouble();
                    this.buffer.accept(this.first);
                    ++this.count;
                }
                this.buffer.accept(t);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public DoubleStream build() {
            int c = this.count;
            if (c >= 0) {
                this.count = -this.count - 1;
                return c < 2 ? StreamSupport.doubleStream(this, false) : StreamSupport.doubleStream(this.buffer.spliterator(), false);
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean tryAdvance(DoubleConsumer action) {
            Objects.requireNonNull(action);
            if (this.count == -2) {
                action.accept(this.first);
                this.count = -1;
                return true;
            }
            return false;
        }

        @Override
        public void forEachRemaining(DoubleConsumer action) {
            Objects.requireNonNull(action);
            if (this.count == -2) {
                action.accept(this.first);
                this.count = -1;
            }
        }
    }

    static final class LongStreamBuilderImpl
    extends AbstractStreamBuilderImpl<Long, Spliterator.OfLong>
    implements LongStream.Builder,
    Spliterator.OfLong {
        long first;
        SpinedBuffer.OfLong buffer;

        LongStreamBuilderImpl() {
        }

        LongStreamBuilderImpl(long t) {
            this.first = t;
            this.count = -2;
        }

        @Override
        public void accept(long t) {
            if (this.count == 0) {
                this.first = t;
                ++this.count;
            } else if (this.count > 0) {
                if (this.buffer == null) {
                    this.buffer = new SpinedBuffer.OfLong();
                    this.buffer.accept(this.first);
                    ++this.count;
                }
                this.buffer.accept(t);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public LongStream build() {
            int c = this.count;
            if (c >= 0) {
                this.count = -this.count - 1;
                return c < 2 ? StreamSupport.longStream(this, false) : StreamSupport.longStream(this.buffer.spliterator(), false);
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean tryAdvance(LongConsumer action) {
            Objects.requireNonNull(action);
            if (this.count == -2) {
                action.accept(this.first);
                this.count = -1;
                return true;
            }
            return false;
        }

        @Override
        public void forEachRemaining(LongConsumer action) {
            Objects.requireNonNull(action);
            if (this.count == -2) {
                action.accept(this.first);
                this.count = -1;
            }
        }
    }

    static final class IntStreamBuilderImpl
    extends AbstractStreamBuilderImpl<Integer, Spliterator.OfInt>
    implements IntStream.Builder,
    Spliterator.OfInt {
        int first;
        SpinedBuffer.OfInt buffer;

        IntStreamBuilderImpl() {
        }

        IntStreamBuilderImpl(int t) {
            this.first = t;
            this.count = -2;
        }

        @Override
        public void accept(int t) {
            if (this.count == 0) {
                this.first = t;
                ++this.count;
            } else if (this.count > 0) {
                if (this.buffer == null) {
                    this.buffer = new SpinedBuffer.OfInt();
                    this.buffer.accept(this.first);
                    ++this.count;
                }
                this.buffer.accept(t);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public IntStream build() {
            int c = this.count;
            if (c >= 0) {
                this.count = -this.count - 1;
                return c < 2 ? StreamSupport.intStream(this, false) : StreamSupport.intStream(this.buffer.spliterator(), false);
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean tryAdvance(IntConsumer action) {
            Objects.requireNonNull(action);
            if (this.count == -2) {
                action.accept(this.first);
                this.count = -1;
                return true;
            }
            return false;
        }

        @Override
        public void forEachRemaining(IntConsumer action) {
            Objects.requireNonNull(action);
            if (this.count == -2) {
                action.accept(this.first);
                this.count = -1;
            }
        }
    }

    static final class StreamBuilderImpl<T>
    extends AbstractStreamBuilderImpl<T, Spliterator<T>>
    implements Stream.Builder<T> {
        T first;
        SpinedBuffer<T> buffer;

        StreamBuilderImpl() {
        }

        StreamBuilderImpl(T t) {
            this.first = t;
            this.count = -2;
        }

        @Override
        public void accept(T t) {
            if (this.count == 0) {
                this.first = t;
                ++this.count;
            } else if (this.count > 0) {
                if (this.buffer == null) {
                    this.buffer = new SpinedBuffer();
                    this.buffer.accept(this.first);
                    ++this.count;
                }
                this.buffer.accept(t);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public Stream.Builder<T> add(T t) {
            this.accept(t);
            return this;
        }

        @Override
        public Stream<T> build() {
            int c = this.count;
            if (c >= 0) {
                this.count = -this.count - 1;
                return c < 2 ? StreamSupport.stream(this, false) : StreamSupport.stream(this.buffer.spliterator(), false);
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            Objects.requireNonNull(action);
            if (this.count == -2) {
                action.accept(this.first);
                this.count = -1;
                return true;
            }
            return false;
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            Objects.requireNonNull(action);
            if (this.count == -2) {
                action.accept(this.first);
                this.count = -1;
            }
        }
    }

    private static abstract class AbstractStreamBuilderImpl<T, S extends Spliterator<T>>
    implements Spliterator<T> {
        int count;

        private AbstractStreamBuilderImpl() {
        }

        public S trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return -this.count - 1;
        }

        @Override
        public int characteristics() {
            return 17488;
        }
    }

    static final class RangeLongSpliterator
    implements Spliterator.OfLong {
        private long from;
        private final long upTo;
        private int last;
        private static final long BALANCED_SPLIT_THRESHOLD = 0x1000000L;
        private static final long RIGHT_BALANCED_SPLIT_RATIO = 8L;

        RangeLongSpliterator(long from, long upTo, boolean closed) {
            this(from, upTo, closed ? 1 : 0);
        }

        private RangeLongSpliterator(long from, long upTo, int last) {
            assert (upTo - from + (long)last > 0L);
            this.from = from;
            this.upTo = upTo;
            this.last = last;
        }

        @Override
        public boolean tryAdvance(LongConsumer consumer) {
            Objects.requireNonNull(consumer);
            long i = this.from++;
            if (i < this.upTo) {
                consumer.accept(i);
                return true;
            }
            if (this.last > 0) {
                this.last = 0;
                consumer.accept(i);
                return true;
            }
            return false;
        }

        @Override
        public void forEachRemaining(LongConsumer consumer) {
            Objects.requireNonNull(consumer);
            long i = this.from;
            long hUpTo = this.upTo;
            int hLast = this.last;
            this.from = this.upTo;
            this.last = 0;
            while (i < hUpTo) {
                consumer.accept(i++);
            }
            if (hLast > 0) {
                consumer.accept(i);
            }
        }

        @Override
        public long estimateSize() {
            return this.upTo - this.from + (long)this.last;
        }

        @Override
        public int characteristics() {
            return 17749;
        }

        @Override
        public Comparator<? super Long> getComparator() {
            return null;
        }

        @Override
        public Spliterator.OfLong trySplit() {
            long size = this.estimateSize();
            return size <= 1L ? null : new RangeLongSpliterator(this.from, this.from += this.splitPoint(size), 0);
        }

        private long splitPoint(long size) {
            long d = size < 0x1000000L ? 2L : 8L;
            return size / d;
        }
    }

    static final class RangeIntSpliterator
    implements Spliterator.OfInt {
        private int from;
        private final int upTo;
        private int last;
        private static final int BALANCED_SPLIT_THRESHOLD = 0x1000000;
        private static final int RIGHT_BALANCED_SPLIT_RATIO = 8;

        RangeIntSpliterator(int from, int upTo, boolean closed) {
            this(from, upTo, closed ? 1 : 0);
        }

        private RangeIntSpliterator(int from, int upTo, int last) {
            this.from = from;
            this.upTo = upTo;
            this.last = last;
        }

        @Override
        public boolean tryAdvance(IntConsumer consumer) {
            Objects.requireNonNull(consumer);
            int i = this.from++;
            if (i < this.upTo) {
                consumer.accept(i);
                return true;
            }
            if (this.last > 0) {
                this.last = 0;
                consumer.accept(i);
                return true;
            }
            return false;
        }

        @Override
        @IntrinsicCandidate
        public void forEachRemaining(IntConsumer consumer) {
            Objects.requireNonNull(consumer);
            int i = this.from;
            int hUpTo = this.upTo;
            int hLast = this.last;
            this.from = this.upTo;
            this.last = 0;
            while (i < hUpTo) {
                consumer.accept(i++);
            }
            if (hLast > 0) {
                consumer.accept(i);
            }
        }

        @Override
        public long estimateSize() {
            return (long)this.upTo - (long)this.from + (long)this.last;
        }

        @Override
        public int characteristics() {
            return 17749;
        }

        @Override
        public Comparator<? super Integer> getComparator() {
            return null;
        }

        @Override
        public Spliterator.OfInt trySplit() {
            long size = this.estimateSize();
            return size <= 1L ? null : new RangeIntSpliterator(this.from, this.from += this.splitPoint(size), 0);
        }

        private int splitPoint(long size) {
            int d = size < 0x1000000L ? 2 : 8;
            return (int)(size / (long)d);
        }
    }
}

