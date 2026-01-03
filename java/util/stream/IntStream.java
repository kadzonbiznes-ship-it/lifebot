/*
 * Decompiled with CFR 0.152.
 */
package java.util.stream;

import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;
import java.util.stream.SpinedBuffer;
import java.util.stream.Stream;
import java.util.stream.StreamSpliterators;
import java.util.stream.StreamSupport;
import java.util.stream.Streams;
import java.util.stream.WhileOps;

public interface IntStream
extends BaseStream<Integer, IntStream> {
    public IntStream filter(IntPredicate var1);

    public IntStream map(IntUnaryOperator var1);

    public <U> Stream<U> mapToObj(IntFunction<? extends U> var1);

    public LongStream mapToLong(IntToLongFunction var1);

    public DoubleStream mapToDouble(IntToDoubleFunction var1);

    public IntStream flatMap(IntFunction<? extends IntStream> var1);

    default public IntStream mapMulti(IntMapMultiConsumer mapper) {
        Objects.requireNonNull(mapper);
        return this.flatMap(e -> {
            SpinedBuffer.OfInt buffer = new SpinedBuffer.OfInt();
            mapper.accept(e, buffer);
            return StreamSupport.intStream(buffer.spliterator(), false);
        });
    }

    public IntStream distinct();

    public IntStream sorted();

    public IntStream peek(IntConsumer var1);

    public IntStream limit(long var1);

    public IntStream skip(long var1);

    default public IntStream takeWhile(IntPredicate predicate) {
        Objects.requireNonNull(predicate);
        return (IntStream)StreamSupport.intStream(new WhileOps.UnorderedWhileSpliterator.OfInt.Taking(this.spliterator(), true, predicate), this.isParallel()).onClose(this::close);
    }

    default public IntStream dropWhile(IntPredicate predicate) {
        Objects.requireNonNull(predicate);
        return (IntStream)StreamSupport.intStream(new WhileOps.UnorderedWhileSpliterator.OfInt.Dropping(this.spliterator(), true, predicate), this.isParallel()).onClose(this::close);
    }

    public void forEach(IntConsumer var1);

    public void forEachOrdered(IntConsumer var1);

    public int[] toArray();

    public int reduce(int var1, IntBinaryOperator var2);

    public OptionalInt reduce(IntBinaryOperator var1);

    public <R> R collect(Supplier<R> var1, ObjIntConsumer<R> var2, BiConsumer<R, R> var3);

    public int sum();

    public OptionalInt min();

    public OptionalInt max();

    public long count();

    public OptionalDouble average();

    public IntSummaryStatistics summaryStatistics();

    public boolean anyMatch(IntPredicate var1);

    public boolean allMatch(IntPredicate var1);

    public boolean noneMatch(IntPredicate var1);

    public OptionalInt findFirst();

    public OptionalInt findAny();

    public LongStream asLongStream();

    public DoubleStream asDoubleStream();

    public Stream<Integer> boxed();

    @Override
    public IntStream sequential();

    @Override
    public IntStream parallel();

    public PrimitiveIterator.OfInt iterator();

    public Spliterator.OfInt spliterator();

    public static Builder builder() {
        return new Streams.IntStreamBuilderImpl();
    }

    public static IntStream empty() {
        return StreamSupport.intStream(Spliterators.emptyIntSpliterator(), false);
    }

    public static IntStream of(int t) {
        return StreamSupport.intStream(new Streams.IntStreamBuilderImpl(t), false);
    }

    public static IntStream of(int ... values) {
        return Arrays.stream(values);
    }

    public static IntStream iterate(final int seed, final IntUnaryOperator f) {
        Objects.requireNonNull(f);
        Spliterators.AbstractIntSpliterator spliterator = new Spliterators.AbstractIntSpliterator(Long.MAX_VALUE, 1296){
            int prev;
            boolean started;

            @Override
            public boolean tryAdvance(IntConsumer action) {
                int t;
                Objects.requireNonNull(action);
                if (this.started) {
                    t = f.applyAsInt(this.prev);
                } else {
                    t = seed;
                    this.started = true;
                }
                this.prev = t;
                action.accept(this.prev);
                return true;
            }
        };
        return StreamSupport.intStream(spliterator, false);
    }

    public static IntStream iterate(final int seed, final IntPredicate hasNext, final IntUnaryOperator next) {
        Objects.requireNonNull(next);
        Objects.requireNonNull(hasNext);
        Spliterators.AbstractIntSpliterator spliterator = new Spliterators.AbstractIntSpliterator(Long.MAX_VALUE, 1296){
            int prev;
            boolean started;
            boolean finished;

            @Override
            public boolean tryAdvance(IntConsumer action) {
                int t;
                Objects.requireNonNull(action);
                if (this.finished) {
                    return false;
                }
                if (this.started) {
                    t = next.applyAsInt(this.prev);
                } else {
                    t = seed;
                    this.started = true;
                }
                if (!hasNext.test(t)) {
                    this.finished = true;
                    return false;
                }
                this.prev = t;
                action.accept(this.prev);
                return true;
            }

            @Override
            public void forEachRemaining(IntConsumer action) {
                int t;
                Objects.requireNonNull(action);
                if (this.finished) {
                    return;
                }
                this.finished = true;
                int n = t = this.started ? next.applyAsInt(this.prev) : seed;
                while (hasNext.test(t)) {
                    action.accept(t);
                    t = next.applyAsInt(t);
                }
            }
        };
        return StreamSupport.intStream(spliterator, false);
    }

    public static IntStream generate(IntSupplier s) {
        Objects.requireNonNull(s);
        return StreamSupport.intStream(new StreamSpliterators.InfiniteSupplyingSpliterator.OfInt(Long.MAX_VALUE, s), false);
    }

    public static IntStream range(int startInclusive, int endExclusive) {
        if (startInclusive >= endExclusive) {
            return IntStream.empty();
        }
        return StreamSupport.intStream(new Streams.RangeIntSpliterator(startInclusive, endExclusive, false), false);
    }

    public static IntStream rangeClosed(int startInclusive, int endInclusive) {
        if (startInclusive > endInclusive) {
            return IntStream.empty();
        }
        return StreamSupport.intStream(new Streams.RangeIntSpliterator(startInclusive, endInclusive, true), false);
    }

    public static IntStream concat(IntStream a, IntStream b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        Streams.ConcatSpliterator.OfInt split = new Streams.ConcatSpliterator.OfInt(a.spliterator(), b.spliterator());
        IntStream stream = StreamSupport.intStream(split, a.isParallel() || b.isParallel());
        return (IntStream)stream.onClose(Streams.composedClose(a, b));
    }

    @FunctionalInterface
    public static interface IntMapMultiConsumer {
        public void accept(int var1, IntConsumer var2);
    }

    public static interface Builder
    extends IntConsumer {
        @Override
        public void accept(int var1);

        default public Builder add(int t) {
            this.accept(t);
            return this;
        }

        public IntStream build();
    }
}

