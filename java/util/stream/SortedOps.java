/*
 * Decompiled with CFR 0.152.
 */
package java.util.stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.IntFunction;
import java.util.stream.AbstractPipeline;
import java.util.stream.DoublePipeline;
import java.util.stream.DoubleStream;
import java.util.stream.IntPipeline;
import java.util.stream.IntStream;
import java.util.stream.LongPipeline;
import java.util.stream.LongStream;
import java.util.stream.Node;
import java.util.stream.Nodes;
import java.util.stream.PipelineHelper;
import java.util.stream.ReferencePipeline;
import java.util.stream.Sink;
import java.util.stream.SpinedBuffer;
import java.util.stream.Stream;
import java.util.stream.StreamOpFlag;
import java.util.stream.StreamShape;

final class SortedOps {
    private SortedOps() {
    }

    static <T> Stream<T> makeRef(AbstractPipeline<?, T, ?> upstream) {
        return new OfRef<T>(upstream);
    }

    static <T> Stream<T> makeRef(AbstractPipeline<?, T, ?> upstream, Comparator<? super T> comparator) {
        return new OfRef<T>(upstream, comparator);
    }

    static <T> IntStream makeInt(AbstractPipeline<?, Integer, ?> upstream) {
        return new OfInt(upstream);
    }

    static <T> LongStream makeLong(AbstractPipeline<?, Long, ?> upstream) {
        return new OfLong(upstream);
    }

    static <T> DoubleStream makeDouble(AbstractPipeline<?, Double, ?> upstream) {
        return new OfDouble(upstream);
    }

    private static final class OfRef<T>
    extends ReferencePipeline.StatefulOp<T, T> {
        private final boolean isNaturalSort;
        private final Comparator<? super T> comparator;

        OfRef(AbstractPipeline<?, T, ?> upstream) {
            super(upstream, StreamShape.REFERENCE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
            this.isNaturalSort = true;
            Comparator comp = Comparator.naturalOrder();
            this.comparator = comp;
        }

        OfRef(AbstractPipeline<?, T, ?> upstream, Comparator<? super T> comparator) {
            super(upstream, StreamShape.REFERENCE, StreamOpFlag.IS_ORDERED | StreamOpFlag.NOT_SORTED);
            this.isNaturalSort = false;
            this.comparator = Objects.requireNonNull(comparator);
        }

        @Override
        public Sink<T> opWrapSink(int flags, Sink<T> sink) {
            Objects.requireNonNull(sink);
            if (StreamOpFlag.SORTED.isKnown(flags) && this.isNaturalSort) {
                return sink;
            }
            if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedRefSortingSink<T>(sink, this.comparator);
            }
            return new RefSortingSink<T>(sink, this.comparator);
        }

        @Override
        public <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> spliterator, IntFunction<T[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags()) && this.isNaturalSort) {
                return helper.evaluate(spliterator, false, generator);
            }
            T[] flattenedData = helper.evaluate(spliterator, true, generator).asArray(generator);
            Arrays.parallelSort(flattenedData, this.comparator);
            return Nodes.node(flattenedData);
        }
    }

    private static final class OfInt
    extends IntPipeline.StatefulOp<Integer> {
        OfInt(AbstractPipeline<?, Integer, ?> upstream) {
            super(upstream, StreamShape.INT_VALUE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
            Objects.requireNonNull(sink);
            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            }
            if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedIntSortingSink((Sink<? super Integer>)sink);
            }
            return new IntSortingSink((Sink<? super Integer>)sink);
        }

        @Override
        public <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator, IntFunction<Integer[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            Node.OfInt n = (Node.OfInt)helper.evaluate(spliterator, true, generator);
            int[] content = (int[])n.asPrimitiveArray();
            Arrays.parallelSort(content);
            return Nodes.node(content);
        }
    }

    private static final class OfLong
    extends LongPipeline.StatefulOp<Long> {
        OfLong(AbstractPipeline<?, Long, ?> upstream) {
            super(upstream, StreamShape.LONG_VALUE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Long> opWrapSink(int flags, Sink<Long> sink) {
            Objects.requireNonNull(sink);
            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            }
            if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedLongSortingSink((Sink<? super Long>)sink);
            }
            return new LongSortingSink((Sink<? super Long>)sink);
        }

        @Override
        public <P_IN> Node<Long> opEvaluateParallel(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator, IntFunction<Long[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            Node.OfLong n = (Node.OfLong)helper.evaluate(spliterator, true, generator);
            long[] content = (long[])n.asPrimitiveArray();
            Arrays.parallelSort(content);
            return Nodes.node(content);
        }
    }

    private static final class OfDouble
    extends DoublePipeline.StatefulOp<Double> {
        OfDouble(AbstractPipeline<?, Double, ?> upstream) {
            super(upstream, StreamShape.DOUBLE_VALUE, StreamOpFlag.IS_ORDERED | StreamOpFlag.IS_SORTED);
        }

        @Override
        public Sink<Double> opWrapSink(int flags, Sink<Double> sink) {
            Objects.requireNonNull(sink);
            if (StreamOpFlag.SORTED.isKnown(flags)) {
                return sink;
            }
            if (StreamOpFlag.SIZED.isKnown(flags)) {
                return new SizedDoubleSortingSink((Sink<? super Double>)sink);
            }
            return new DoubleSortingSink((Sink<? super Double>)sink);
        }

        @Override
        public <P_IN> Node<Double> opEvaluateParallel(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator, IntFunction<Double[]> generator) {
            if (StreamOpFlag.SORTED.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            Node.OfDouble n = (Node.OfDouble)helper.evaluate(spliterator, true, generator);
            double[] content = (double[])n.asPrimitiveArray();
            Arrays.parallelSort(content);
            return Nodes.node(content);
        }
    }

    private static final class DoubleSortingSink
    extends AbstractDoubleSortingSink {
        private SpinedBuffer.OfDouble b;

        DoubleSortingSink(Sink<? super Double> sink) {
            super(sink);
        }

        @Override
        public void begin(long size) {
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            this.b = size > 0L ? new SpinedBuffer.OfDouble((int)size) : new SpinedBuffer.OfDouble();
        }

        @Override
        public void end() {
            double[] doubles = (double[])this.b.asPrimitiveArray();
            Arrays.sort(doubles);
            this.downstream.begin(doubles.length);
            if (!this.cancellationRequestedCalled) {
                for (double aDouble : doubles) {
                    this.downstream.accept(aDouble);
                }
            } else {
                for (double aDouble : doubles) {
                    if (!this.downstream.cancellationRequested()) {
                        this.downstream.accept(aDouble);
                        continue;
                    }
                    break;
                }
            }
            this.downstream.end();
        }

        @Override
        public void accept(double t) {
            this.b.accept(t);
        }
    }

    private static final class SizedDoubleSortingSink
    extends AbstractDoubleSortingSink {
        private double[] array;
        private int offset;

        SizedDoubleSortingSink(Sink<? super Double> downstream) {
            super(downstream);
        }

        @Override
        public void begin(long size) {
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            this.array = new double[(int)size];
        }

        @Override
        public void end() {
            Arrays.sort(this.array, 0, this.offset);
            this.downstream.begin(this.offset);
            if (!this.cancellationRequestedCalled) {
                for (int i = 0; i < this.offset; ++i) {
                    this.downstream.accept(this.array[i]);
                }
            } else {
                for (int i = 0; i < this.offset && !this.downstream.cancellationRequested(); ++i) {
                    this.downstream.accept(this.array[i]);
                }
            }
            this.downstream.end();
            this.array = null;
        }

        @Override
        public void accept(double t) {
            this.array[this.offset++] = t;
        }
    }

    private static abstract class AbstractDoubleSortingSink
    extends Sink.ChainedDouble<Double> {
        protected boolean cancellationRequestedCalled;

        AbstractDoubleSortingSink(Sink<? super Double> downstream) {
            super(downstream);
        }

        @Override
        public final boolean cancellationRequested() {
            this.cancellationRequestedCalled = true;
            return false;
        }
    }

    private static final class LongSortingSink
    extends AbstractLongSortingSink {
        private SpinedBuffer.OfLong b;

        LongSortingSink(Sink<? super Long> sink) {
            super(sink);
        }

        @Override
        public void begin(long size) {
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            this.b = size > 0L ? new SpinedBuffer.OfLong((int)size) : new SpinedBuffer.OfLong();
        }

        @Override
        public void end() {
            long[] longs = (long[])this.b.asPrimitiveArray();
            Arrays.sort(longs);
            this.downstream.begin(longs.length);
            if (!this.cancellationRequestedCalled) {
                for (long aLong : longs) {
                    this.downstream.accept(aLong);
                }
            } else {
                for (long aLong : longs) {
                    if (!this.downstream.cancellationRequested()) {
                        this.downstream.accept(aLong);
                        continue;
                    }
                    break;
                }
            }
            this.downstream.end();
        }

        @Override
        public void accept(long t) {
            this.b.accept(t);
        }
    }

    private static final class SizedLongSortingSink
    extends AbstractLongSortingSink {
        private long[] array;
        private int offset;

        SizedLongSortingSink(Sink<? super Long> downstream) {
            super(downstream);
        }

        @Override
        public void begin(long size) {
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            this.array = new long[(int)size];
        }

        @Override
        public void end() {
            Arrays.sort(this.array, 0, this.offset);
            this.downstream.begin(this.offset);
            if (!this.cancellationRequestedCalled) {
                for (int i = 0; i < this.offset; ++i) {
                    this.downstream.accept(this.array[i]);
                }
            } else {
                for (int i = 0; i < this.offset && !this.downstream.cancellationRequested(); ++i) {
                    this.downstream.accept(this.array[i]);
                }
            }
            this.downstream.end();
            this.array = null;
        }

        @Override
        public void accept(long t) {
            this.array[this.offset++] = t;
        }
    }

    private static abstract class AbstractLongSortingSink
    extends Sink.ChainedLong<Long> {
        protected boolean cancellationRequestedCalled;

        AbstractLongSortingSink(Sink<? super Long> downstream) {
            super(downstream);
        }

        @Override
        public final boolean cancellationRequested() {
            this.cancellationRequestedCalled = true;
            return false;
        }
    }

    private static final class IntSortingSink
    extends AbstractIntSortingSink {
        private SpinedBuffer.OfInt b;

        IntSortingSink(Sink<? super Integer> sink) {
            super(sink);
        }

        @Override
        public void begin(long size) {
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            this.b = size > 0L ? new SpinedBuffer.OfInt((int)size) : new SpinedBuffer.OfInt();
        }

        @Override
        public void end() {
            int[] ints = (int[])this.b.asPrimitiveArray();
            Arrays.sort(ints);
            this.downstream.begin(ints.length);
            if (!this.cancellationRequestedCalled) {
                for (int anInt : ints) {
                    this.downstream.accept(anInt);
                }
            } else {
                for (int anInt : ints) {
                    if (!this.downstream.cancellationRequested()) {
                        this.downstream.accept(anInt);
                        continue;
                    }
                    break;
                }
            }
            this.downstream.end();
        }

        @Override
        public void accept(int t) {
            this.b.accept(t);
        }
    }

    private static final class SizedIntSortingSink
    extends AbstractIntSortingSink {
        private int[] array;
        private int offset;

        SizedIntSortingSink(Sink<? super Integer> downstream) {
            super(downstream);
        }

        @Override
        public void begin(long size) {
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            this.array = new int[(int)size];
        }

        @Override
        public void end() {
            Arrays.sort(this.array, 0, this.offset);
            this.downstream.begin(this.offset);
            if (!this.cancellationRequestedCalled) {
                for (int i = 0; i < this.offset; ++i) {
                    this.downstream.accept(this.array[i]);
                }
            } else {
                for (int i = 0; i < this.offset && !this.downstream.cancellationRequested(); ++i) {
                    this.downstream.accept(this.array[i]);
                }
            }
            this.downstream.end();
            this.array = null;
        }

        @Override
        public void accept(int t) {
            this.array[this.offset++] = t;
        }
    }

    private static abstract class AbstractIntSortingSink
    extends Sink.ChainedInt<Integer> {
        protected boolean cancellationRequestedCalled;

        AbstractIntSortingSink(Sink<? super Integer> downstream) {
            super(downstream);
        }

        @Override
        public final boolean cancellationRequested() {
            this.cancellationRequestedCalled = true;
            return false;
        }
    }

    private static final class RefSortingSink<T>
    extends AbstractRefSortingSink<T> {
        private ArrayList<T> list;

        RefSortingSink(Sink<? super T> sink, Comparator<? super T> comparator) {
            super(sink, comparator);
        }

        @Override
        public void begin(long size) {
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            this.list = size >= 0L ? new ArrayList((int)size) : new ArrayList();
        }

        @Override
        public void end() {
            this.list.sort(this.comparator);
            this.downstream.begin(this.list.size());
            if (!this.cancellationRequestedCalled) {
                this.list.forEach(this.downstream::accept);
            } else {
                for (T t : this.list) {
                    if (this.downstream.cancellationRequested()) break;
                    this.downstream.accept(t);
                }
            }
            this.downstream.end();
            this.list = null;
        }

        @Override
        public void accept(T t) {
            this.list.add(t);
        }
    }

    private static final class SizedRefSortingSink<T>
    extends AbstractRefSortingSink<T> {
        private T[] array;
        private int offset;

        SizedRefSortingSink(Sink<? super T> sink, Comparator<? super T> comparator) {
            super(sink, comparator);
        }

        @Override
        public void begin(long size) {
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            this.array = new Object[(int)size];
        }

        @Override
        public void end() {
            Arrays.sort(this.array, 0, this.offset, this.comparator);
            this.downstream.begin(this.offset);
            if (!this.cancellationRequestedCalled) {
                for (int i = 0; i < this.offset; ++i) {
                    this.downstream.accept(this.array[i]);
                }
            } else {
                for (int i = 0; i < this.offset && !this.downstream.cancellationRequested(); ++i) {
                    this.downstream.accept(this.array[i]);
                }
            }
            this.downstream.end();
            this.array = null;
        }

        @Override
        public void accept(T t) {
            this.array[this.offset++] = t;
        }
    }

    private static abstract class AbstractRefSortingSink<T>
    extends Sink.ChainedReference<T, T> {
        protected final Comparator<? super T> comparator;
        protected boolean cancellationRequestedCalled;

        AbstractRefSortingSink(Sink<? super T> downstream, Comparator<? super T> comparator) {
            super(downstream);
            this.comparator = comparator;
        }

        @Override
        public final boolean cancellationRequested() {
            this.cancellationRequestedCalled = true;
            return false;
        }
    }
}

