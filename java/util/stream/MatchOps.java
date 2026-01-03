/*
 * Decompiled with CFR 0.152.
 */
package java.util.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.AbstractShortCircuitTask;
import java.util.stream.PipelineHelper;
import java.util.stream.Sink;
import java.util.stream.StreamOpFlag;
import java.util.stream.StreamShape;
import java.util.stream.TerminalOp;

final class MatchOps {
    private MatchOps() {
    }

    public static <T> TerminalOp<T, Boolean> makeRef(final Predicate<? super T> predicate, final MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        return new MatchOp(StreamShape.REFERENCE, matchKind, () -> {
            class MatchSink
            extends BooleanTerminalSink<T> {
                MatchSink() {
                    super(matchKind2);
                }

                @Override
                public void accept(T t) {
                    if (!this.stop && predicate.test(t) == matchKind.stopOnPredicateMatches) {
                        this.stop = true;
                        this.value = matchKind.shortCircuitResult;
                    }
                }
            }
            return new MatchSink();
        });
    }

    public static TerminalOp<Integer, Boolean> makeInt(final IntPredicate predicate, final MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        return new MatchOp<Integer>(StreamShape.INT_VALUE, matchKind, () -> {
            class MatchSink
            extends BooleanTerminalSink<Integer>
            implements Sink.OfInt {
                MatchSink() {
                    super(matchKind2);
                }

                @Override
                public void accept(int t) {
                    if (!this.stop && predicate.test(t) == matchKind.stopOnPredicateMatches) {
                        this.stop = true;
                        this.value = matchKind.shortCircuitResult;
                    }
                }
            }
            return new MatchSink();
        });
    }

    public static TerminalOp<Long, Boolean> makeLong(final LongPredicate predicate, final MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        return new MatchOp<Long>(StreamShape.LONG_VALUE, matchKind, () -> {
            class MatchSink
            extends BooleanTerminalSink<Long>
            implements Sink.OfLong {
                MatchSink() {
                    super(matchKind2);
                }

                @Override
                public void accept(long t) {
                    if (!this.stop && predicate.test(t) == matchKind.stopOnPredicateMatches) {
                        this.stop = true;
                        this.value = matchKind.shortCircuitResult;
                    }
                }
            }
            return new MatchSink();
        });
    }

    public static TerminalOp<Double, Boolean> makeDouble(final DoublePredicate predicate, final MatchKind matchKind) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(matchKind);
        return new MatchOp<Double>(StreamShape.DOUBLE_VALUE, matchKind, () -> {
            class MatchSink
            extends BooleanTerminalSink<Double>
            implements Sink.OfDouble {
                MatchSink() {
                    super(matchKind2);
                }

                @Override
                public void accept(double t) {
                    if (!this.stop && predicate.test(t) == matchKind.stopOnPredicateMatches) {
                        this.stop = true;
                        this.value = matchKind.shortCircuitResult;
                    }
                }
            }
            return new MatchSink();
        });
    }

    private static final class MatchOp<T>
    implements TerminalOp<T, Boolean> {
        private final StreamShape inputShape;
        final MatchKind matchKind;
        final Supplier<BooleanTerminalSink<T>> sinkSupplier;

        MatchOp(StreamShape shape, MatchKind matchKind, Supplier<BooleanTerminalSink<T>> sinkSupplier) {
            this.inputShape = shape;
            this.matchKind = matchKind;
            this.sinkSupplier = sinkSupplier;
        }

        @Override
        public int getOpFlags() {
            return StreamOpFlag.IS_SHORT_CIRCUIT | StreamOpFlag.NOT_ORDERED;
        }

        @Override
        public StreamShape inputShape() {
            return this.inputShape;
        }

        @Override
        public <S> Boolean evaluateSequential(PipelineHelper<T> helper, Spliterator<S> spliterator) {
            return helper.wrapAndCopyInto(this.sinkSupplier.get(), spliterator).getAndClearState();
        }

        @Override
        public <S> Boolean evaluateParallel(PipelineHelper<T> helper, Spliterator<S> spliterator) {
            return (Boolean)new MatchTask<S, T>(this, helper, spliterator).invoke();
        }
    }

    static enum MatchKind {
        ANY(true, true),
        ALL(false, false),
        NONE(true, false);

        private final boolean stopOnPredicateMatches;
        private final boolean shortCircuitResult;

        private MatchKind(boolean stopOnPredicateMatches, boolean shortCircuitResult) {
            this.stopOnPredicateMatches = stopOnPredicateMatches;
            this.shortCircuitResult = shortCircuitResult;
        }
    }

    private static final class MatchTask<P_IN, P_OUT>
    extends AbstractShortCircuitTask<P_IN, P_OUT, Boolean, MatchTask<P_IN, P_OUT>> {
        private final MatchOp<P_OUT> op;

        MatchTask(MatchOp<P_OUT> op, PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator) {
            super(helper, spliterator);
            this.op = op;
        }

        MatchTask(MatchTask<P_IN, P_OUT> parent, Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            this.op = parent.op;
        }

        @Override
        protected MatchTask<P_IN, P_OUT> makeChild(Spliterator<P_IN> spliterator) {
            return new MatchTask<P_IN, P_OUT>(this, spliterator);
        }

        @Override
        protected Boolean doLeaf() {
            boolean b = this.helper.wrapAndCopyInto(this.op.sinkSupplier.get(), this.spliterator).getAndClearState();
            if (b == this.op.matchKind.shortCircuitResult) {
                this.shortCircuit(b);
            }
            return null;
        }

        @Override
        protected Boolean getEmptyResult() {
            return !this.op.matchKind.shortCircuitResult;
        }
    }

    private static abstract class BooleanTerminalSink<T>
    implements Sink<T> {
        boolean stop;
        boolean value;

        BooleanTerminalSink(MatchKind matchKind) {
            this.value = !matchKind.shortCircuitResult;
        }

        public boolean getAndClearState() {
            return this.value;
        }

        @Override
        public boolean cancellationRequested() {
            return this.stop;
        }
    }
}

