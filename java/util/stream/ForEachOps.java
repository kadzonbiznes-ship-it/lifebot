/*
 * Decompiled with CFR 0.152.
 */
package java.util.stream;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.CountedCompleter;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import java.util.stream.AbstractTask;
import java.util.stream.Node;
import java.util.stream.PipelineHelper;
import java.util.stream.Sink;
import java.util.stream.StreamOpFlag;
import java.util.stream.StreamShape;
import java.util.stream.TerminalOp;
import java.util.stream.TerminalSink;

final class ForEachOps {
    private ForEachOps() {
    }

    public static <T> TerminalOp<T, Void> makeRef(Consumer<? super T> action, boolean ordered) {
        Objects.requireNonNull(action);
        return new ForEachOp.OfRef<T>(action, ordered);
    }

    public static TerminalOp<Integer, Void> makeInt(IntConsumer action, boolean ordered) {
        Objects.requireNonNull(action);
        return new ForEachOp.OfInt(action, ordered);
    }

    public static TerminalOp<Long, Void> makeLong(LongConsumer action, boolean ordered) {
        Objects.requireNonNull(action);
        return new ForEachOp.OfLong(action, ordered);
    }

    public static TerminalOp<Double, Void> makeDouble(DoubleConsumer action, boolean ordered) {
        Objects.requireNonNull(action);
        return new ForEachOp.OfDouble(action, ordered);
    }

    static abstract class ForEachOp<T>
    implements TerminalOp<T, Void>,
    TerminalSink<T, Void> {
        private final boolean ordered;

        protected ForEachOp(boolean ordered) {
            this.ordered = ordered;
        }

        @Override
        public int getOpFlags() {
            return this.ordered ? 0 : StreamOpFlag.NOT_ORDERED;
        }

        @Override
        public <S> Void evaluateSequential(PipelineHelper<T> helper, Spliterator<S> spliterator) {
            return helper.wrapAndCopyInto(this, spliterator).get();
        }

        @Override
        public <S> Void evaluateParallel(PipelineHelper<T> helper, Spliterator<S> spliterator) {
            if (this.ordered) {
                new ForEachOrderedTask<S, T>(helper, spliterator, this).invoke();
            } else {
                new ForEachTask<S, T>(helper, spliterator, helper.wrapSink(this)).invoke();
            }
            return null;
        }

        @Override
        public Void get() {
            return null;
        }

        static final class OfDouble
        extends ForEachOp<Double>
        implements Sink.OfDouble {
            final DoubleConsumer consumer;

            OfDouble(DoubleConsumer consumer, boolean ordered) {
                super(ordered);
                this.consumer = consumer;
            }

            @Override
            public StreamShape inputShape() {
                return StreamShape.DOUBLE_VALUE;
            }

            @Override
            public void accept(double t) {
                this.consumer.accept(t);
            }
        }

        static final class OfLong
        extends ForEachOp<Long>
        implements Sink.OfLong {
            final LongConsumer consumer;

            OfLong(LongConsumer consumer, boolean ordered) {
                super(ordered);
                this.consumer = consumer;
            }

            @Override
            public StreamShape inputShape() {
                return StreamShape.LONG_VALUE;
            }

            @Override
            public void accept(long t) {
                this.consumer.accept(t);
            }
        }

        static final class OfInt
        extends ForEachOp<Integer>
        implements Sink.OfInt {
            final IntConsumer consumer;

            OfInt(IntConsumer consumer, boolean ordered) {
                super(ordered);
                this.consumer = consumer;
            }

            @Override
            public StreamShape inputShape() {
                return StreamShape.INT_VALUE;
            }

            @Override
            public void accept(int t) {
                this.consumer.accept(t);
            }
        }

        static final class OfRef<T>
        extends ForEachOp<T> {
            final Consumer<? super T> consumer;

            OfRef(Consumer<? super T> consumer, boolean ordered) {
                super(ordered);
                this.consumer = consumer;
            }

            @Override
            public void accept(T t) {
                this.consumer.accept(t);
            }
        }
    }

    static final class ForEachOrderedTask<S, T>
    extends CountedCompleter<Void> {
        private final PipelineHelper<T> helper;
        private Spliterator<S> spliterator;
        private final long targetSize;
        private final Sink<T> action;
        private final ForEachOrderedTask<S, T> leftPredecessor;
        private Node<T> node;
        private ForEachOrderedTask<S, T> next;
        private static final VarHandle NEXT;

        protected ForEachOrderedTask(PipelineHelper<T> helper, Spliterator<S> spliterator, Sink<T> action) {
            super(null);
            this.helper = helper;
            this.spliterator = spliterator;
            this.targetSize = AbstractTask.suggestTargetSize(spliterator.estimateSize());
            this.action = action;
            this.leftPredecessor = null;
        }

        ForEachOrderedTask(ForEachOrderedTask<S, T> parent, Spliterator<S> spliterator, ForEachOrderedTask<S, T> leftPredecessor) {
            super(parent);
            this.helper = parent.helper;
            this.spliterator = spliterator;
            this.targetSize = parent.targetSize;
            this.action = parent.action;
            this.leftPredecessor = leftPredecessor;
        }

        @Override
        public final void compute() {
            ForEachOrderedTask.doCompute(this);
        }

        private static <S, T> void doCompute(ForEachOrderedTask<S, T> task) {
            Spliterator<S> leftSplit;
            Spliterator<S> rightSplit = task.spliterator;
            long sizeThreshold = task.targetSize;
            boolean forkRight = false;
            while (rightSplit.estimateSize() > sizeThreshold && (leftSplit = rightSplit.trySplit()) != null) {
                ForEachOrderedTask<S, T> taskToFork;
                ForEachOrderedTask<S, T> leftChild = new ForEachOrderedTask<S, T>(task, leftSplit, task.leftPredecessor);
                ForEachOrderedTask<S, T> rightChild = new ForEachOrderedTask<S, T>(task, rightSplit, leftChild);
                leftChild.next = rightChild;
                task.addToPendingCount(1);
                rightChild.addToPendingCount(1);
                if (task.leftPredecessor != null) {
                    leftChild.addToPendingCount(1);
                    if (NEXT.compareAndSet(task.leftPredecessor, task, leftChild)) {
                        task.addToPendingCount(-1);
                    } else {
                        leftChild.addToPendingCount(-1);
                    }
                }
                if (forkRight) {
                    forkRight = false;
                    rightSplit = leftSplit;
                    task = leftChild;
                    taskToFork = rightChild;
                } else {
                    forkRight = true;
                    task = rightChild;
                    taskToFork = leftChild;
                }
                taskToFork.fork();
            }
            if (task.getPendingCount() > 0) {
                IntFunction<P_OUT[]> generator = Object[]::new;
                Node.Builder<T> nb = task.helper.makeNodeBuilder(task.helper.exactOutputSizeIfKnown(rightSplit), generator);
                task.node = task.helper.wrapAndCopyInto(nb, rightSplit).build();
                task.spliterator = null;
            }
            task.tryComplete();
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            if (this.node != null) {
                this.node.forEach(this.action);
                this.node = null;
            } else if (this.spliterator != null) {
                this.helper.wrapAndCopyInto(this.action, this.spliterator);
                this.spliterator = null;
            }
            ForEachOrderedTask leftDescendant = NEXT.getAndSet(this, null);
            if (leftDescendant != null) {
                leftDescendant.tryComplete();
            }
        }

        static {
            try {
                MethodHandles.Lookup l = MethodHandles.lookup();
                NEXT = l.findVarHandle(ForEachOrderedTask.class, "next", ForEachOrderedTask.class);
            }
            catch (Exception e) {
                throw new InternalError(e);
            }
        }
    }

    static final class ForEachTask<S, T>
    extends CountedCompleter<Void> {
        private Spliterator<S> spliterator;
        private final Sink<S> sink;
        private final PipelineHelper<T> helper;
        private long targetSize;

        ForEachTask(PipelineHelper<T> helper, Spliterator<S> spliterator, Sink<S> sink) {
            super(null);
            this.sink = sink;
            this.helper = helper;
            this.spliterator = spliterator;
            this.targetSize = 0L;
        }

        ForEachTask(ForEachTask<S, T> parent, Spliterator<S> spliterator) {
            super(parent);
            this.spliterator = spliterator;
            this.sink = parent.sink;
            this.targetSize = parent.targetSize;
            this.helper = parent.helper;
        }

        @Override
        public void compute() {
            Spliterator<S> rightSplit = this.spliterator;
            long sizeEstimate = rightSplit.estimateSize();
            long sizeThreshold = this.targetSize;
            if (sizeThreshold == 0L) {
                this.targetSize = sizeThreshold = AbstractTask.suggestTargetSize(sizeEstimate);
            }
            boolean isShortCircuit = StreamOpFlag.SHORT_CIRCUIT.isKnown(this.helper.getStreamAndOpFlags());
            boolean forkRight = false;
            Sink<S> taskSink = this.sink;
            ForEachTask task = this;
            while (!isShortCircuit || !taskSink.cancellationRequested()) {
                ForEachTask taskToFork;
                Spliterator<S> leftSplit;
                if (sizeEstimate <= sizeThreshold || (leftSplit = rightSplit.trySplit()) == null) {
                    task.helper.copyInto(taskSink, rightSplit);
                    break;
                }
                ForEachTask leftTask = new ForEachTask(task, leftSplit);
                task.addToPendingCount(1);
                if (forkRight) {
                    forkRight = false;
                    rightSplit = leftSplit;
                    taskToFork = task;
                    task = leftTask;
                } else {
                    forkRight = true;
                    taskToFork = leftTask;
                }
                taskToFork.fork();
                sizeEstimate = rightSplit.estimateSize();
            }
            task.spliterator = null;
            task.propagateCompletion();
        }
    }
}

