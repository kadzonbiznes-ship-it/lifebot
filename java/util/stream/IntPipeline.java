/*
 * Decompiled with CFR 0.152.
 */
package java.util.stream;

import java.util.IntSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.AbstractPipeline;
import java.util.stream.DoublePipeline;
import java.util.stream.DoubleStream;
import java.util.stream.FindOps;
import java.util.stream.ForEachOps;
import java.util.stream.IntStream;
import java.util.stream.LongPipeline;
import java.util.stream.LongStream;
import java.util.stream.MatchOps;
import java.util.stream.Node;
import java.util.stream.Nodes;
import java.util.stream.PipelineHelper;
import java.util.stream.ReduceOps;
import java.util.stream.ReferencePipeline;
import java.util.stream.Sink;
import java.util.stream.SliceOps;
import java.util.stream.SortedOps;
import java.util.stream.Stream;
import java.util.stream.StreamOpFlag;
import java.util.stream.StreamShape;
import java.util.stream.StreamSpliterators;
import java.util.stream.Tripwire;
import java.util.stream.WhileOps;

abstract class IntPipeline<E_IN>
extends AbstractPipeline<E_IN, Integer, IntStream>
implements IntStream {
    IntPipeline(Supplier<? extends Spliterator<Integer>> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    IntPipeline(Spliterator<Integer> source, int sourceFlags, boolean parallel) {
        super(source, sourceFlags, parallel);
    }

    IntPipeline(AbstractPipeline<?, E_IN, ?> upstream, int opFlags) {
        super(upstream, opFlags);
    }

    private static IntConsumer adapt(Sink<Integer> sink) {
        if (sink instanceof IntConsumer) {
            return (IntConsumer)((Object)sink);
        }
        if (Tripwire.ENABLED) {
            Tripwire.trip(AbstractPipeline.class, "using IntStream.adapt(Sink<Integer> s)");
        }
        return sink::accept;
    }

    private static Spliterator.OfInt adapt(Spliterator<Integer> s) {
        if (s instanceof Spliterator.OfInt) {
            return (Spliterator.OfInt)s;
        }
        if (Tripwire.ENABLED) {
            Tripwire.trip(AbstractPipeline.class, "using IntStream.adapt(Spliterator<Integer> s)");
        }
        throw new UnsupportedOperationException("IntStream.adapt(Spliterator<Integer> s)");
    }

    @Override
    final StreamShape getOutputShape() {
        return StreamShape.INT_VALUE;
    }

    @Override
    final <P_IN> Node<Integer> evaluateToNode(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<Integer[]> generator) {
        return Nodes.collectInt(helper, spliterator, flattenTree);
    }

    @Override
    final <P_IN> Spliterator<Integer> wrap(PipelineHelper<Integer> ph, Supplier<Spliterator<P_IN>> supplier, boolean isParallel) {
        return new StreamSpliterators.IntWrappingSpliterator<P_IN>(ph, supplier, isParallel);
    }

    final Spliterator.OfInt lazySpliterator(Supplier<? extends Spliterator<Integer>> supplier) {
        return new StreamSpliterators.DelegatingSpliterator.OfInt((Supplier<Spliterator.OfInt>)supplier);
    }

    @Override
    final boolean forEachWithCancel(Spliterator<Integer> spliterator, Sink<Integer> sink) {
        boolean cancelled;
        Spliterator.OfInt spl = IntPipeline.adapt(spliterator);
        IntConsumer adaptedSink = IntPipeline.adapt(sink);
        while (!(cancelled = sink.cancellationRequested()) && spl.tryAdvance(adaptedSink)) {
        }
        return cancelled;
    }

    @Override
    final Node.Builder<Integer> makeNodeBuilder(long exactSizeIfKnown, IntFunction<Integer[]> generator) {
        return Nodes.intBuilder(exactSizeIfKnown);
    }

    private <U> Stream<U> mapToObj(final IntFunction<? extends U> mapper, int opFlags) {
        return new ReferencePipeline.StatelessOp<Integer, U>(this, this, StreamShape.INT_VALUE, opFlags){

            @Override
            Sink<Integer> opWrapSink(int flags, Sink<U> sink) {
                return new Sink.ChainedInt<U>(sink){

                    @Override
                    public void accept(int t) {
                        this.downstream.accept(mapper.apply(t));
                    }
                };
            }
        };
    }

    @Override
    public final PrimitiveIterator.OfInt iterator() {
        return Spliterators.iterator(this.spliterator());
    }

    @Override
    public final Spliterator.OfInt spliterator() {
        return IntPipeline.adapt(super.spliterator());
    }

    @Override
    public final LongStream asLongStream() {
        return new LongPipeline.StatelessOp<Integer>(this, (AbstractPipeline)this, StreamShape.INT_VALUE, 0){

            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedInt<Long>(this, sink){

                    @Override
                    public void accept(int t) {
                        this.downstream.accept((long)t);
                    }
                };
            }
        };
    }

    @Override
    public final DoubleStream asDoubleStream() {
        return new DoublePipeline.StatelessOp<Integer>(this, (AbstractPipeline)this, StreamShape.INT_VALUE, 0){

            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedInt<Double>(this, sink){

                    @Override
                    public void accept(int t) {
                        this.downstream.accept((double)t);
                    }
                };
            }
        };
    }

    @Override
    public final Stream<Integer> boxed() {
        return this.mapToObj(Integer::valueOf, 0);
    }

    @Override
    public final IntStream map(final IntUnaryOperator mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<Integer>(this, (AbstractPipeline)this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT){

            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedInt<Integer>(sink){

                    @Override
                    public void accept(int t) {
                        this.downstream.accept(mapper.applyAsInt(t));
                    }
                };
            }
        };
    }

    @Override
    public final <U> Stream<U> mapToObj(IntFunction<? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return this.mapToObj(mapper, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT);
    }

    @Override
    public final LongStream mapToLong(final IntToLongFunction mapper) {
        Objects.requireNonNull(mapper);
        return new LongPipeline.StatelessOp<Integer>(this, (AbstractPipeline)this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT){

            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Long> sink) {
                return new Sink.ChainedInt<Long>(sink){

                    @Override
                    public void accept(int t) {
                        this.downstream.accept(mapper.applyAsLong(t));
                    }
                };
            }
        };
    }

    @Override
    public final DoubleStream mapToDouble(final IntToDoubleFunction mapper) {
        Objects.requireNonNull(mapper);
        return new DoublePipeline.StatelessOp<Integer>(this, (AbstractPipeline)this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT){

            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Double> sink) {
                return new Sink.ChainedInt<Double>(sink){

                    @Override
                    public void accept(int t) {
                        this.downstream.accept(mapper.applyAsDouble(t));
                    }
                };
            }
        };
    }

    @Override
    public final IntStream flatMap(final IntFunction<? extends IntStream> mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<Integer>(this, (AbstractPipeline)this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED){

            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedInt<Integer>(sink){
                    boolean cancellationRequestedCalled;
                    IntConsumer downstreamAsInt;
                    {
                        this.downstreamAsInt = this.downstream::accept;
                    }

                    @Override
                    public void begin(long size) {
                        this.downstream.begin(-1L);
                    }

                    @Override
                    public void accept(int t) {
                        try (IntStream result = (IntStream)mapper.apply(t);){
                            if (result != null) {
                                if (!this.cancellationRequestedCalled) {
                                    result.sequential().forEach(this.downstreamAsInt);
                                } else {
                                    Spliterator.OfInt s = result.sequential().spliterator();
                                    while (!this.downstream.cancellationRequested() && s.tryAdvance(this.downstreamAsInt)) {
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public boolean cancellationRequested() {
                        this.cancellationRequestedCalled = true;
                        return this.downstream.cancellationRequested();
                    }
                };
            }
        };
    }

    @Override
    public final IntStream mapMulti(final IntStream.IntMapMultiConsumer mapper) {
        Objects.requireNonNull(mapper);
        return new StatelessOp<Integer>(this, (AbstractPipeline)this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SORTED | StreamOpFlag.NOT_DISTINCT | StreamOpFlag.NOT_SIZED){

            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedInt<Integer>(sink){

                    @Override
                    public void begin(long size) {
                        this.downstream.begin(-1L);
                    }

                    @Override
                    public void accept(int t) {
                        mapper.accept(t, (IntConsumer)((Object)this.downstream));
                    }
                };
            }
        };
    }

    @Override
    public IntStream unordered() {
        if (!this.isOrdered()) {
            return this;
        }
        return new StatelessOp<Integer>(this, (AbstractPipeline)this, StreamShape.INT_VALUE, StreamOpFlag.NOT_ORDERED){

            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return sink;
            }
        };
    }

    @Override
    public final IntStream filter(final IntPredicate predicate) {
        Objects.requireNonNull(predicate);
        return new StatelessOp<Integer>(this, (AbstractPipeline)this, StreamShape.INT_VALUE, StreamOpFlag.NOT_SIZED){

            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedInt<Integer>(sink){

                    @Override
                    public void begin(long size) {
                        this.downstream.begin(-1L);
                    }

                    @Override
                    public void accept(int t) {
                        if (predicate.test(t)) {
                            this.downstream.accept(t);
                        }
                    }
                };
            }
        };
    }

    @Override
    public final IntStream peek(final IntConsumer action) {
        Objects.requireNonNull(action);
        return new StatelessOp<Integer>(this, (AbstractPipeline)this, StreamShape.INT_VALUE, 0){

            @Override
            Sink<Integer> opWrapSink(int flags, Sink<Integer> sink) {
                return new Sink.ChainedInt<Integer>(sink){

                    @Override
                    public void accept(int t) {
                        action.accept(t);
                        this.downstream.accept(t);
                    }
                };
            }
        };
    }

    @Override
    public final IntStream limit(long maxSize) {
        if (maxSize < 0L) {
            throw new IllegalArgumentException(Long.toString(maxSize));
        }
        return SliceOps.makeInt(this, 0L, maxSize);
    }

    @Override
    public final IntStream skip(long n) {
        if (n < 0L) {
            throw new IllegalArgumentException(Long.toString(n));
        }
        if (n == 0L) {
            return this;
        }
        return SliceOps.makeInt(this, n, -1L);
    }

    @Override
    public final IntStream takeWhile(IntPredicate predicate) {
        return WhileOps.makeTakeWhileInt(this, predicate);
    }

    @Override
    public final IntStream dropWhile(IntPredicate predicate) {
        return WhileOps.makeDropWhileInt(this, predicate);
    }

    @Override
    public final IntStream sorted() {
        return SortedOps.makeInt(this);
    }

    @Override
    public final IntStream distinct() {
        return this.boxed().distinct().mapToInt(i -> i);
    }

    @Override
    public void forEach(IntConsumer action) {
        this.evaluate(ForEachOps.makeInt(action, false));
    }

    @Override
    public void forEachOrdered(IntConsumer action) {
        this.evaluate(ForEachOps.makeInt(action, true));
    }

    @Override
    public final int sum() {
        return this.reduce(0, Integer::sum);
    }

    @Override
    public final OptionalInt min() {
        return this.reduce(Math::min);
    }

    @Override
    public final OptionalInt max() {
        return this.reduce(Math::max);
    }

    @Override
    public final long count() {
        return this.evaluate(ReduceOps.makeIntCounting());
    }

    @Override
    public final OptionalDouble average() {
        long[] avg = this.collect(() -> new long[2], (ll, i) -> {
            ll[0] = ll[0] + 1L;
            ll[1] = ll[1] + (long)i;
        }, (ll, rr) -> {
            ll[0] = ll[0] + rr[0];
            ll[1] = ll[1] + rr[1];
        });
        return avg[0] > 0L ? OptionalDouble.of((double)avg[1] / (double)avg[0]) : OptionalDouble.empty();
    }

    @Override
    public final IntSummaryStatistics summaryStatistics() {
        return this.collect(IntSummaryStatistics::new, IntSummaryStatistics::accept, IntSummaryStatistics::combine);
    }

    @Override
    public final int reduce(int identity, IntBinaryOperator op) {
        return this.evaluate(ReduceOps.makeInt(identity, op));
    }

    @Override
    public final OptionalInt reduce(IntBinaryOperator op) {
        return this.evaluate(ReduceOps.makeInt(op));
    }

    @Override
    public final <R> R collect(Supplier<R> supplier, ObjIntConsumer<R> accumulator, BiConsumer<R, R> combiner) {
        Objects.requireNonNull(combiner);
        BinaryOperator operator = (left, right) -> {
            combiner.accept(left, right);
            return left;
        };
        return this.evaluate(ReduceOps.makeInt(supplier, accumulator, operator));
    }

    @Override
    public final boolean anyMatch(IntPredicate predicate) {
        return this.evaluate(MatchOps.makeInt(predicate, MatchOps.MatchKind.ANY));
    }

    @Override
    public final boolean allMatch(IntPredicate predicate) {
        return this.evaluate(MatchOps.makeInt(predicate, MatchOps.MatchKind.ALL));
    }

    @Override
    public final boolean noneMatch(IntPredicate predicate) {
        return this.evaluate(MatchOps.makeInt(predicate, MatchOps.MatchKind.NONE));
    }

    @Override
    public final OptionalInt findFirst() {
        return this.evaluate(FindOps.makeInt(true));
    }

    @Override
    public final OptionalInt findAny() {
        return this.evaluate(FindOps.makeInt(false));
    }

    @Override
    public final int[] toArray() {
        return (int[])Nodes.flattenInt((Node.OfInt)this.evaluateToArrayNode(Integer[]::new)).asPrimitiveArray();
    }

    static abstract class StatefulOp<E_IN>
    extends IntPipeline<E_IN> {
        StatefulOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
            assert (upstream.getOutputShape() == inputShape);
        }

        @Override
        final boolean opIsStateful() {
            return true;
        }

        @Override
        abstract <P_IN> Node<Integer> opEvaluateParallel(PipelineHelper<Integer> var1, Spliterator<P_IN> var2, IntFunction<Integer[]> var3);
    }

    static abstract class StatelessOp<E_IN>
    extends IntPipeline<E_IN> {
        StatelessOp(AbstractPipeline<?, E_IN, ?> upstream, StreamShape inputShape, int opFlags) {
            super(upstream, opFlags);
            assert (upstream.getOutputShape() == inputShape);
        }

        @Override
        final boolean opIsStateful() {
            return false;
        }
    }

    static class Head<E_IN>
    extends IntPipeline<E_IN> {
        Head(Supplier<? extends Spliterator<Integer>> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        Head(Spliterator<Integer> source, int sourceFlags, boolean parallel) {
            super(source, sourceFlags, parallel);
        }

        @Override
        final boolean opIsStateful() {
            throw new UnsupportedOperationException();
        }

        @Override
        final Sink<E_IN> opWrapSink(int flags, Sink<Integer> sink) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void forEach(IntConsumer action) {
            if (!this.isParallel()) {
                IntPipeline.adapt(this.sourceStageSpliterator()).forEachRemaining(action);
            } else {
                super.forEach(action);
            }
        }

        @Override
        public void forEachOrdered(IntConsumer action) {
            if (!this.isParallel()) {
                IntPipeline.adapt(this.sourceStageSpliterator()).forEachRemaining(action);
            } else {
                super.forEachOrdered(action);
            }
        }
    }
}

