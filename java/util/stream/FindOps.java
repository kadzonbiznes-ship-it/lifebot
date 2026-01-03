/*
 * Decompiled with CFR 0.152.
 */
package java.util.stream;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Spliterator;
import java.util.concurrent.CountedCompleter;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.AbstractShortCircuitTask;
import java.util.stream.PipelineHelper;
import java.util.stream.Sink;
import java.util.stream.StreamOpFlag;
import java.util.stream.StreamShape;
import java.util.stream.TerminalOp;
import java.util.stream.TerminalSink;

final class FindOps {
    private FindOps() {
    }

    public static <T> TerminalOp<T, Optional<T>> makeRef(boolean mustFindFirst) {
        return mustFindFirst ? FindSink.OfRef.OP_FIND_FIRST : FindSink.OfRef.OP_FIND_ANY;
    }

    public static TerminalOp<Integer, OptionalInt> makeInt(boolean mustFindFirst) {
        return mustFindFirst ? FindSink.OfInt.OP_FIND_FIRST : FindSink.OfInt.OP_FIND_ANY;
    }

    public static TerminalOp<Long, OptionalLong> makeLong(boolean mustFindFirst) {
        return mustFindFirst ? FindSink.OfLong.OP_FIND_FIRST : FindSink.OfLong.OP_FIND_ANY;
    }

    public static TerminalOp<Double, OptionalDouble> makeDouble(boolean mustFindFirst) {
        return mustFindFirst ? FindSink.OfDouble.OP_FIND_FIRST : FindSink.OfDouble.OP_FIND_ANY;
    }

    private static abstract class FindSink<T, O>
    implements TerminalSink<T, O> {
        boolean hasValue;
        T value;

        FindSink() {
        }

        @Override
        public void accept(T value) {
            if (!this.hasValue) {
                this.hasValue = true;
                this.value = value;
            }
        }

        @Override
        public boolean cancellationRequested() {
            return this.hasValue;
        }

        static final class OfDouble
        extends FindSink<Double, OptionalDouble>
        implements Sink.OfDouble {
            static final TerminalOp<Double, OptionalDouble> OP_FIND_FIRST = new FindOp<Double, OptionalDouble>(true, StreamShape.DOUBLE_VALUE, OptionalDouble.empty(), OptionalDouble::isPresent, OfDouble::new);
            static final TerminalOp<Double, OptionalDouble> OP_FIND_ANY = new FindOp<Double, OptionalDouble>(false, StreamShape.DOUBLE_VALUE, OptionalDouble.empty(), OptionalDouble::isPresent, OfDouble::new);

            OfDouble() {
            }

            @Override
            public void accept(double value) {
                this.accept(Double.valueOf(value));
            }

            @Override
            public OptionalDouble get() {
                return this.hasValue ? OptionalDouble.of((Double)this.value) : null;
            }
        }

        static final class OfLong
        extends FindSink<Long, OptionalLong>
        implements Sink.OfLong {
            static final TerminalOp<Long, OptionalLong> OP_FIND_FIRST = new FindOp<Long, OptionalLong>(true, StreamShape.LONG_VALUE, OptionalLong.empty(), OptionalLong::isPresent, OfLong::new);
            static final TerminalOp<Long, OptionalLong> OP_FIND_ANY = new FindOp<Long, OptionalLong>(false, StreamShape.LONG_VALUE, OptionalLong.empty(), OptionalLong::isPresent, OfLong::new);

            OfLong() {
            }

            @Override
            public void accept(long value) {
                this.accept(Long.valueOf(value));
            }

            @Override
            public OptionalLong get() {
                return this.hasValue ? OptionalLong.of((Long)this.value) : null;
            }
        }

        static final class OfInt
        extends FindSink<Integer, OptionalInt>
        implements Sink.OfInt {
            static final TerminalOp<Integer, OptionalInt> OP_FIND_FIRST = new FindOp<Integer, OptionalInt>(true, StreamShape.INT_VALUE, OptionalInt.empty(), OptionalInt::isPresent, OfInt::new);
            static final TerminalOp<Integer, OptionalInt> OP_FIND_ANY = new FindOp<Integer, OptionalInt>(false, StreamShape.INT_VALUE, OptionalInt.empty(), OptionalInt::isPresent, OfInt::new);

            OfInt() {
            }

            @Override
            public void accept(int value) {
                this.accept(Integer.valueOf(value));
            }

            @Override
            public OptionalInt get() {
                return this.hasValue ? OptionalInt.of((Integer)this.value) : null;
            }
        }

        static final class OfRef<T>
        extends FindSink<T, Optional<T>> {
            static final TerminalOp<?, ?> OP_FIND_FIRST = new FindOp(true, StreamShape.REFERENCE, Optional.empty(), Optional::isPresent, OfRef::new);
            static final TerminalOp<?, ?> OP_FIND_ANY = new FindOp(false, StreamShape.REFERENCE, Optional.empty(), Optional::isPresent, OfRef::new);

            OfRef() {
            }

            @Override
            public Optional<T> get() {
                return this.hasValue ? Optional.of(this.value) : null;
            }
        }
    }

    private static final class FindTask<P_IN, P_OUT, O>
    extends AbstractShortCircuitTask<P_IN, P_OUT, O, FindTask<P_IN, P_OUT, O>> {
        private final FindOp<P_OUT, O> op;
        private final boolean mustFindFirst;

        FindTask(FindOp<P_OUT, O> op, boolean mustFindFirst, PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator) {
            super(helper, spliterator);
            this.mustFindFirst = mustFindFirst;
            this.op = op;
        }

        FindTask(FindTask<P_IN, P_OUT, O> parent, Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            this.mustFindFirst = parent.mustFindFirst;
            this.op = parent.op;
        }

        @Override
        protected FindTask<P_IN, P_OUT, O> makeChild(Spliterator<P_IN> spliterator) {
            return new FindTask<P_IN, P_OUT, O>(this, spliterator);
        }

        @Override
        protected O getEmptyResult() {
            return this.op.emptyValue;
        }

        private void foundResult(O answer) {
            if (this.isLeftmostNode()) {
                this.shortCircuit(answer);
            } else {
                this.cancelLaterNodes();
            }
        }

        @Override
        protected O doLeaf() {
            Object result = this.helper.wrapAndCopyInto(this.op.sinkSupplier.get(), this.spliterator).get();
            if (!this.mustFindFirst) {
                if (result != null) {
                    this.shortCircuit(result);
                }
                return null;
            }
            if (result != null) {
                this.foundResult(result);
                return (O)result;
            }
            return null;
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            if (this.mustFindFirst) {
                FindTask child = (FindTask)this.leftChild;
                FindTask p = null;
                while (child != p) {
                    Object result = child.getLocalResult();
                    if (result != null && this.op.presentPredicate.test(result)) {
                        this.setLocalResult(result);
                        this.foundResult(result);
                        break;
                    }
                    p = child;
                    child = (FindTask)this.rightChild;
                }
            }
            super.onCompletion(caller);
        }
    }

    private static final class FindOp<T, O>
    implements TerminalOp<T, O> {
        private final StreamShape shape;
        final int opFlags;
        final O emptyValue;
        final Predicate<O> presentPredicate;
        final Supplier<TerminalSink<T, O>> sinkSupplier;

        FindOp(boolean mustFindFirst, StreamShape shape, O emptyValue, Predicate<O> presentPredicate, Supplier<TerminalSink<T, O>> sinkSupplier) {
            this.opFlags = StreamOpFlag.IS_SHORT_CIRCUIT | (mustFindFirst ? 0 : StreamOpFlag.NOT_ORDERED);
            this.shape = shape;
            this.emptyValue = emptyValue;
            this.presentPredicate = presentPredicate;
            this.sinkSupplier = sinkSupplier;
        }

        @Override
        public int getOpFlags() {
            return this.opFlags;
        }

        @Override
        public StreamShape inputShape() {
            return this.shape;
        }

        @Override
        public <S> O evaluateSequential(PipelineHelper<T> helper, Spliterator<S> spliterator) {
            Object result = helper.wrapAndCopyInto(this.sinkSupplier.get(), spliterator).get();
            return (O)(result != null ? result : this.emptyValue);
        }

        @Override
        public <P_IN> O evaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
            boolean mustFindFirst = StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags());
            return (O)new FindTask(this, mustFindFirst, helper, spliterator).invoke();
        }
    }
}

