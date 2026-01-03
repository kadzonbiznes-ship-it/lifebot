/*
 * Decompiled with CFR 0.152.
 */
package java.util.stream;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinTask;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.stream.AbstractTask;
import java.util.stream.Node;
import java.util.stream.PipelineHelper;
import java.util.stream.Sink;
import java.util.stream.SpinedBuffer;
import java.util.stream.StreamShape;

final class Nodes {
    static final long MAX_ARRAY_SIZE = 0x7FFFFFF7L;
    static final String BAD_SIZE = "Stream size exceeds max array size";
    private static final Node EMPTY_NODE = new EmptyNode.OfRef();
    private static final Node.OfInt EMPTY_INT_NODE = new EmptyNode.OfInt();
    private static final Node.OfLong EMPTY_LONG_NODE = new EmptyNode.OfLong();
    private static final Node.OfDouble EMPTY_DOUBLE_NODE = new EmptyNode.OfDouble();
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final long[] EMPTY_LONG_ARRAY = new long[0];
    private static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

    private Nodes() {
        throw new Error("no instances");
    }

    static <T> IntFunction<T[]> castingArray() {
        return Object[]::new;
    }

    static <T> Node<T> emptyNode(StreamShape shape) {
        return switch (shape) {
            default -> throw new MatchException(null, null);
            case StreamShape.REFERENCE -> EMPTY_NODE;
            case StreamShape.INT_VALUE -> EMPTY_INT_NODE;
            case StreamShape.LONG_VALUE -> EMPTY_LONG_NODE;
            case StreamShape.DOUBLE_VALUE -> EMPTY_DOUBLE_NODE;
        };
    }

    static <T> Node<T> conc(StreamShape shape, Node<T> left, Node<T> right) {
        return switch (shape) {
            default -> throw new MatchException(null, null);
            case StreamShape.REFERENCE -> new ConcNode<T>(left, right);
            case StreamShape.INT_VALUE -> new ConcNode.OfInt((Node.OfInt)left, (Node.OfInt)right);
            case StreamShape.LONG_VALUE -> new ConcNode.OfLong((Node.OfLong)left, (Node.OfLong)right);
            case StreamShape.DOUBLE_VALUE -> new ConcNode.OfDouble((Node.OfDouble)left, (Node.OfDouble)right);
        };
    }

    static <T> Node<T> node(T[] array) {
        return new ArrayNode<T>(array);
    }

    static <T> Node<T> node(Collection<T> c) {
        return new CollectionNode<T>(c);
    }

    static <T> Node.Builder<T> builder(long exactSizeIfKnown, IntFunction<T[]> generator) {
        return exactSizeIfKnown >= 0L && exactSizeIfKnown < 0x7FFFFFF7L ? new FixedNodeBuilder(exactSizeIfKnown, generator) : Nodes.builder();
    }

    static <T> Node.Builder<T> builder() {
        return new SpinedNodeBuilder();
    }

    static Node.OfInt node(int[] array) {
        return new IntArrayNode(array);
    }

    static Node.Builder.OfInt intBuilder(long exactSizeIfKnown) {
        return exactSizeIfKnown >= 0L && exactSizeIfKnown < 0x7FFFFFF7L ? new IntFixedNodeBuilder(exactSizeIfKnown) : Nodes.intBuilder();
    }

    static Node.Builder.OfInt intBuilder() {
        return new IntSpinedNodeBuilder();
    }

    static Node.OfLong node(long[] array) {
        return new LongArrayNode(array);
    }

    static Node.Builder.OfLong longBuilder(long exactSizeIfKnown) {
        return exactSizeIfKnown >= 0L && exactSizeIfKnown < 0x7FFFFFF7L ? new LongFixedNodeBuilder(exactSizeIfKnown) : Nodes.longBuilder();
    }

    static Node.Builder.OfLong longBuilder() {
        return new LongSpinedNodeBuilder();
    }

    static Node.OfDouble node(double[] array) {
        return new DoubleArrayNode(array);
    }

    static Node.Builder.OfDouble doubleBuilder(long exactSizeIfKnown) {
        return exactSizeIfKnown >= 0L && exactSizeIfKnown < 0x7FFFFFF7L ? new DoubleFixedNodeBuilder(exactSizeIfKnown) : Nodes.doubleBuilder();
    }

    static Node.Builder.OfDouble doubleBuilder() {
        return new DoubleSpinedNodeBuilder();
    }

    public static <P_IN, P_OUT> Node<P_OUT> collect(PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<P_OUT[]> generator) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0L && spliterator.hasCharacteristics(16384)) {
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException(BAD_SIZE);
            }
            P_OUT[] array = generator.apply((int)size);
            new SizedCollectorTask.OfRef<P_IN, P_OUT>(spliterator, helper, array).invoke();
            return Nodes.node(array);
        }
        Node node = (Node)new CollectorTask.OfRef<P_IN, P_OUT>(helper, generator, spliterator).invoke();
        return flattenTree ? Nodes.flatten(node, generator) : node;
    }

    public static <P_IN> Node.OfInt collectInt(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator, boolean flattenTree) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0L && spliterator.hasCharacteristics(16384)) {
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException(BAD_SIZE);
            }
            int[] array = new int[(int)size];
            new SizedCollectorTask.OfInt<P_IN>(spliterator, helper, array).invoke();
            return Nodes.node(array);
        }
        Node.OfInt node = (Node.OfInt)new CollectorTask.OfInt<P_IN>(helper, spliterator).invoke();
        return flattenTree ? Nodes.flattenInt(node) : node;
    }

    public static <P_IN> Node.OfLong collectLong(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator, boolean flattenTree) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0L && spliterator.hasCharacteristics(16384)) {
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException(BAD_SIZE);
            }
            long[] array = new long[(int)size];
            new SizedCollectorTask.OfLong<P_IN>(spliterator, helper, array).invoke();
            return Nodes.node(array);
        }
        Node.OfLong node = (Node.OfLong)new CollectorTask.OfLong<P_IN>(helper, spliterator).invoke();
        return flattenTree ? Nodes.flattenLong(node) : node;
    }

    public static <P_IN> Node.OfDouble collectDouble(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator, boolean flattenTree) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size >= 0L && spliterator.hasCharacteristics(16384)) {
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException(BAD_SIZE);
            }
            double[] array = new double[(int)size];
            new SizedCollectorTask.OfDouble<P_IN>(spliterator, helper, array).invoke();
            return Nodes.node(array);
        }
        Node.OfDouble node = (Node.OfDouble)new CollectorTask.OfDouble<P_IN>(helper, spliterator).invoke();
        return flattenTree ? Nodes.flattenDouble(node) : node;
    }

    public static <T> Node<T> flatten(Node<T> node, IntFunction<T[]> generator) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException(BAD_SIZE);
            }
            T[] array = generator.apply((int)size);
            new ToArrayTask.OfRef<T>(node, array, 0).invoke();
            return Nodes.node(array);
        }
        return node;
    }

    public static Node.OfInt flattenInt(Node.OfInt node) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException(BAD_SIZE);
            }
            int[] array = new int[(int)size];
            new ToArrayTask.OfInt(node, array, 0).invoke();
            return Nodes.node(array);
        }
        return node;
    }

    public static Node.OfLong flattenLong(Node.OfLong node) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException(BAD_SIZE);
            }
            long[] array = new long[(int)size];
            new ToArrayTask.OfLong(node, array, 0).invoke();
            return Nodes.node(array);
        }
        return node;
    }

    public static Node.OfDouble flattenDouble(Node.OfDouble node) {
        if (node.getChildCount() > 0) {
            long size = node.count();
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException(BAD_SIZE);
            }
            double[] array = new double[(int)size];
            new ToArrayTask.OfDouble(node, array, 0).invoke();
            return Nodes.node(array);
        }
        return node;
    }

    static final class ConcNode<T>
    extends AbstractConcNode<T, Node<T>>
    implements Node<T> {
        ConcNode(Node<T> left, Node<T> right) {
            super(left, right);
        }

        @Override
        public Spliterator<T> spliterator() {
            return new InternalNodeSpliterator.OfRef(this);
        }

        @Override
        public void copyInto(T[] array, int offset) {
            Objects.requireNonNull(array);
            this.left.copyInto(array, offset);
            this.right.copyInto(array, offset + (int)this.left.count());
        }

        @Override
        public T[] asArray(IntFunction<T[]> generator) {
            long size = this.count();
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }
            T[] array = generator.apply((int)size);
            this.copyInto(array, 0);
            return array;
        }

        @Override
        public void forEach(Consumer<? super T> consumer) {
            this.left.forEach(consumer);
            this.right.forEach(consumer);
        }

        @Override
        public Node<T> truncate(long from, long to, IntFunction<T[]> generator) {
            if (from == 0L && to == this.count()) {
                return this;
            }
            long leftCount = this.left.count();
            if (from >= leftCount) {
                return this.right.truncate(from - leftCount, to - leftCount, generator);
            }
            if (to <= leftCount) {
                return this.left.truncate(from, to, generator);
            }
            return Nodes.conc(this.getShape(), this.left.truncate(from, leftCount, generator), this.right.truncate(0L, to - leftCount, generator));
        }

        public String toString() {
            if (this.count() < 32L) {
                return String.format("ConcNode[%s.%s]", this.left, this.right);
            }
            return String.format("ConcNode[size=%d]", this.count());
        }

        static final class OfDouble
        extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble>
        implements Node.OfDouble {
            OfDouble(Node.OfDouble left, Node.OfDouble right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfDouble spliterator() {
                return new InternalNodeSpliterator.OfDouble(this);
            }
        }

        static final class OfLong
        extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, Node.OfLong>
        implements Node.OfLong {
            OfLong(Node.OfLong left, Node.OfLong right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfLong spliterator() {
                return new InternalNodeSpliterator.OfLong(this);
            }
        }

        static final class OfInt
        extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt>
        implements Node.OfInt {
            OfInt(Node.OfInt left, Node.OfInt right) {
                super(left, right);
            }

            @Override
            public Spliterator.OfInt spliterator() {
                return new InternalNodeSpliterator.OfInt(this);
            }
        }

        private static abstract class OfPrimitive<E, T_CONS, T_ARR, T_SPLITR extends Spliterator.OfPrimitive<E, T_CONS, T_SPLITR>, T_NODE extends Node.OfPrimitive<E, T_CONS, T_ARR, T_SPLITR, T_NODE>>
        extends AbstractConcNode<E, T_NODE>
        implements Node.OfPrimitive<E, T_CONS, T_ARR, T_SPLITR, T_NODE> {
            OfPrimitive(T_NODE left, T_NODE right) {
                super(left, right);
            }

            @Override
            public void forEach(T_CONS consumer) {
                ((Node.OfPrimitive)this.left).forEach(consumer);
                ((Node.OfPrimitive)this.right).forEach(consumer);
            }

            @Override
            public void copyInto(T_ARR array, int offset) {
                ((Node.OfPrimitive)this.left).copyInto(array, offset);
                ((Node.OfPrimitive)this.right).copyInto(array, offset + (int)((Node.OfPrimitive)this.left).count());
            }

            @Override
            public T_ARR asPrimitiveArray() {
                long size = this.count();
                if (size >= 0x7FFFFFF7L) {
                    throw new IllegalArgumentException(Nodes.BAD_SIZE);
                }
                Object array = this.newArray((int)size);
                this.copyInto(array, 0);
                return array;
            }

            public String toString() {
                if (this.count() < 32L) {
                    return String.format("%s[%s.%s]", this.getClass().getName(), this.left, this.right);
                }
                return String.format("%s[size=%d]", this.getClass().getName(), this.count());
            }
        }
    }

    private static class ArrayNode<T>
    implements Node<T> {
        final T[] array;
        int curSize;

        ArrayNode(long size, IntFunction<T[]> generator) {
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }
            this.array = generator.apply((int)size);
            this.curSize = 0;
        }

        ArrayNode(T[] array) {
            this.array = array;
            this.curSize = array.length;
        }

        @Override
        public Spliterator<T> spliterator() {
            return Arrays.spliterator(this.array, 0, this.curSize);
        }

        @Override
        public void copyInto(T[] dest, int destOffset) {
            System.arraycopy(this.array, 0, dest, destOffset, this.curSize);
        }

        @Override
        public T[] asArray(IntFunction<T[]> generator) {
            if (this.array.length == this.curSize) {
                return this.array;
            }
            throw new IllegalStateException();
        }

        @Override
        public long count() {
            return this.curSize;
        }

        @Override
        public void forEach(Consumer<? super T> consumer) {
            for (int i = 0; i < this.curSize; ++i) {
                consumer.accept(this.array[i]);
            }
        }

        public String toString() {
            return String.format("ArrayNode[%d][%s]", this.array.length - this.curSize, Arrays.toString(this.array));
        }
    }

    private static final class CollectionNode<T>
    implements Node<T> {
        private final Collection<T> c;

        CollectionNode(Collection<T> c) {
            this.c = c;
        }

        @Override
        public Spliterator<T> spliterator() {
            return this.c.stream().spliterator();
        }

        @Override
        public void copyInto(T[] array, int offset) {
            for (T t : this.c) {
                array[offset++] = t;
            }
        }

        @Override
        public T[] asArray(IntFunction<T[]> generator) {
            return this.c.toArray(generator.apply(this.c.size()));
        }

        @Override
        public long count() {
            return this.c.size();
        }

        @Override
        public void forEach(Consumer<? super T> consumer) {
            this.c.forEach(consumer);
        }

        public String toString() {
            return String.format("CollectionNode[%d][%s]", this.c.size(), this.c);
        }
    }

    private static final class FixedNodeBuilder<T>
    extends ArrayNode<T>
    implements Node.Builder<T> {
        FixedNodeBuilder(long size, IntFunction<T[]> generator) {
            super(size, generator);
            assert (size < 0x7FFFFFF7L);
        }

        @Override
        public Node<T> build() {
            if (this.curSize < this.array.length) {
                throw new IllegalStateException(String.format("Current size %d is less than fixed size %d", this.curSize, this.array.length));
            }
            return this;
        }

        @Override
        public void begin(long size) {
            if (size != (long)this.array.length) {
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d", size, this.array.length));
            }
            this.curSize = 0;
        }

        @Override
        public void accept(T t) {
            if (this.curSize >= this.array.length) {
                throw new IllegalStateException(String.format("Accept exceeded fixed size of %d", this.array.length));
            }
            this.array[this.curSize++] = t;
        }

        @Override
        public void end() {
            if (this.curSize < this.array.length) {
                throw new IllegalStateException(String.format("End size %d is less than fixed size %d", this.curSize, this.array.length));
            }
        }

        @Override
        public String toString() {
            return String.format("FixedNodeBuilder[%d][%s]", this.array.length - this.curSize, Arrays.toString(this.array));
        }
    }

    private static final class SpinedNodeBuilder<T>
    extends SpinedBuffer<T>
    implements Node<T>,
    Node.Builder<T> {
        private boolean building = false;

        SpinedNodeBuilder() {
        }

        @Override
        public Spliterator<T> spliterator() {
            assert (!this.building) : "during building";
            return super.spliterator();
        }

        @Override
        public void forEach(Consumer<? super T> consumer) {
            assert (!this.building) : "during building";
            super.forEach(consumer);
        }

        @Override
        public void begin(long size) {
            assert (!this.building) : "was already building";
            this.building = true;
            this.clear();
            this.ensureCapacity(size);
        }

        @Override
        public void accept(T t) {
            assert (this.building) : "not building";
            super.accept(t);
        }

        @Override
        public void end() {
            assert (this.building) : "was not building";
            this.building = false;
        }

        @Override
        public void copyInto(T[] array, int offset) {
            assert (!this.building) : "during building";
            super.copyInto(array, offset);
        }

        @Override
        public T[] asArray(IntFunction<T[]> arrayFactory) {
            assert (!this.building) : "during building";
            return super.asArray(arrayFactory);
        }

        @Override
        public Node<T> build() {
            assert (!this.building) : "during building";
            return this;
        }
    }

    private static class IntArrayNode
    implements Node.OfInt {
        final int[] array;
        int curSize;

        IntArrayNode(long size) {
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }
            this.array = new int[(int)size];
            this.curSize = 0;
        }

        IntArrayNode(int[] array) {
            this.array = array;
            this.curSize = array.length;
        }

        @Override
        public Spliterator.OfInt spliterator() {
            return Arrays.spliterator(this.array, 0, this.curSize);
        }

        @Override
        public int[] asPrimitiveArray() {
            if (this.array.length == this.curSize) {
                return this.array;
            }
            return Arrays.copyOf(this.array, this.curSize);
        }

        @Override
        public void copyInto(int[] dest, int destOffset) {
            System.arraycopy(this.array, 0, dest, destOffset, this.curSize);
        }

        @Override
        public long count() {
            return this.curSize;
        }

        @Override
        public void forEach(IntConsumer consumer) {
            for (int i = 0; i < this.curSize; ++i) {
                consumer.accept(this.array[i]);
            }
        }

        public String toString() {
            return String.format("IntArrayNode[%d][%s]", this.array.length - this.curSize, Arrays.toString(this.array));
        }
    }

    private static final class IntFixedNodeBuilder
    extends IntArrayNode
    implements Node.Builder.OfInt {
        IntFixedNodeBuilder(long size) {
            super(size);
            assert (size < 0x7FFFFFF7L);
        }

        @Override
        public Node.OfInt build() {
            if (this.curSize < this.array.length) {
                throw new IllegalStateException(String.format("Current size %d is less than fixed size %d", this.curSize, this.array.length));
            }
            return this;
        }

        @Override
        public void begin(long size) {
            if (size != (long)this.array.length) {
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d", size, this.array.length));
            }
            this.curSize = 0;
        }

        @Override
        public void accept(int i) {
            if (this.curSize >= this.array.length) {
                throw new IllegalStateException(String.format("Accept exceeded fixed size of %d", this.array.length));
            }
            this.array[this.curSize++] = i;
        }

        @Override
        public void end() {
            if (this.curSize < this.array.length) {
                throw new IllegalStateException(String.format("End size %d is less than fixed size %d", this.curSize, this.array.length));
            }
        }

        @Override
        public String toString() {
            return String.format("IntFixedNodeBuilder[%d][%s]", this.array.length - this.curSize, Arrays.toString(this.array));
        }
    }

    private static final class IntSpinedNodeBuilder
    extends SpinedBuffer.OfInt
    implements Node.OfInt,
    Node.Builder.OfInt {
        private boolean building = false;

        IntSpinedNodeBuilder() {
        }

        @Override
        public Spliterator.OfInt spliterator() {
            assert (!this.building) : "during building";
            return super.spliterator();
        }

        @Override
        public void forEach(IntConsumer consumer) {
            assert (!this.building) : "during building";
            super.forEach(consumer);
        }

        @Override
        public void begin(long size) {
            assert (!this.building) : "was already building";
            this.building = true;
            this.clear();
            this.ensureCapacity(size);
        }

        @Override
        public void accept(int i) {
            assert (this.building) : "not building";
            super.accept(i);
        }

        @Override
        public void end() {
            assert (this.building) : "was not building";
            this.building = false;
        }

        @Override
        public void copyInto(int[] array, int offset) throws IndexOutOfBoundsException {
            assert (!this.building) : "during building";
            super.copyInto(array, offset);
        }

        @Override
        public int[] asPrimitiveArray() {
            assert (!this.building) : "during building";
            return (int[])super.asPrimitiveArray();
        }

        @Override
        public Node.OfInt build() {
            assert (!this.building) : "during building";
            return this;
        }
    }

    private static class LongArrayNode
    implements Node.OfLong {
        final long[] array;
        int curSize;

        LongArrayNode(long size) {
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }
            this.array = new long[(int)size];
            this.curSize = 0;
        }

        LongArrayNode(long[] array) {
            this.array = array;
            this.curSize = array.length;
        }

        @Override
        public Spliterator.OfLong spliterator() {
            return Arrays.spliterator(this.array, 0, this.curSize);
        }

        @Override
        public long[] asPrimitiveArray() {
            if (this.array.length == this.curSize) {
                return this.array;
            }
            return Arrays.copyOf(this.array, this.curSize);
        }

        @Override
        public void copyInto(long[] dest, int destOffset) {
            System.arraycopy(this.array, 0, dest, destOffset, this.curSize);
        }

        @Override
        public long count() {
            return this.curSize;
        }

        @Override
        public void forEach(LongConsumer consumer) {
            for (int i = 0; i < this.curSize; ++i) {
                consumer.accept(this.array[i]);
            }
        }

        public String toString() {
            return String.format("LongArrayNode[%d][%s]", this.array.length - this.curSize, Arrays.toString(this.array));
        }
    }

    private static final class LongFixedNodeBuilder
    extends LongArrayNode
    implements Node.Builder.OfLong {
        LongFixedNodeBuilder(long size) {
            super(size);
            assert (size < 0x7FFFFFF7L);
        }

        @Override
        public Node.OfLong build() {
            if (this.curSize < this.array.length) {
                throw new IllegalStateException(String.format("Current size %d is less than fixed size %d", this.curSize, this.array.length));
            }
            return this;
        }

        @Override
        public void begin(long size) {
            if (size != (long)this.array.length) {
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d", size, this.array.length));
            }
            this.curSize = 0;
        }

        @Override
        public void accept(long i) {
            if (this.curSize >= this.array.length) {
                throw new IllegalStateException(String.format("Accept exceeded fixed size of %d", this.array.length));
            }
            this.array[this.curSize++] = i;
        }

        @Override
        public void end() {
            if (this.curSize < this.array.length) {
                throw new IllegalStateException(String.format("End size %d is less than fixed size %d", this.curSize, this.array.length));
            }
        }

        @Override
        public String toString() {
            return String.format("LongFixedNodeBuilder[%d][%s]", this.array.length - this.curSize, Arrays.toString(this.array));
        }
    }

    private static final class LongSpinedNodeBuilder
    extends SpinedBuffer.OfLong
    implements Node.OfLong,
    Node.Builder.OfLong {
        private boolean building = false;

        LongSpinedNodeBuilder() {
        }

        @Override
        public Spliterator.OfLong spliterator() {
            assert (!this.building) : "during building";
            return super.spliterator();
        }

        @Override
        public void forEach(LongConsumer consumer) {
            assert (!this.building) : "during building";
            super.forEach(consumer);
        }

        @Override
        public void begin(long size) {
            assert (!this.building) : "was already building";
            this.building = true;
            this.clear();
            this.ensureCapacity(size);
        }

        @Override
        public void accept(long i) {
            assert (this.building) : "not building";
            super.accept(i);
        }

        @Override
        public void end() {
            assert (this.building) : "was not building";
            this.building = false;
        }

        @Override
        public void copyInto(long[] array, int offset) {
            assert (!this.building) : "during building";
            super.copyInto(array, offset);
        }

        @Override
        public long[] asPrimitiveArray() {
            assert (!this.building) : "during building";
            return (long[])super.asPrimitiveArray();
        }

        @Override
        public Node.OfLong build() {
            assert (!this.building) : "during building";
            return this;
        }
    }

    private static class DoubleArrayNode
    implements Node.OfDouble {
        final double[] array;
        int curSize;

        DoubleArrayNode(long size) {
            if (size >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }
            this.array = new double[(int)size];
            this.curSize = 0;
        }

        DoubleArrayNode(double[] array) {
            this.array = array;
            this.curSize = array.length;
        }

        @Override
        public Spliterator.OfDouble spliterator() {
            return Arrays.spliterator(this.array, 0, this.curSize);
        }

        @Override
        public double[] asPrimitiveArray() {
            if (this.array.length == this.curSize) {
                return this.array;
            }
            return Arrays.copyOf(this.array, this.curSize);
        }

        @Override
        public void copyInto(double[] dest, int destOffset) {
            System.arraycopy(this.array, 0, dest, destOffset, this.curSize);
        }

        @Override
        public long count() {
            return this.curSize;
        }

        @Override
        public void forEach(DoubleConsumer consumer) {
            for (int i = 0; i < this.curSize; ++i) {
                consumer.accept(this.array[i]);
            }
        }

        public String toString() {
            return String.format("DoubleArrayNode[%d][%s]", this.array.length - this.curSize, Arrays.toString(this.array));
        }
    }

    private static final class DoubleFixedNodeBuilder
    extends DoubleArrayNode
    implements Node.Builder.OfDouble {
        DoubleFixedNodeBuilder(long size) {
            super(size);
            assert (size < 0x7FFFFFF7L);
        }

        @Override
        public Node.OfDouble build() {
            if (this.curSize < this.array.length) {
                throw new IllegalStateException(String.format("Current size %d is less than fixed size %d", this.curSize, this.array.length));
            }
            return this;
        }

        @Override
        public void begin(long size) {
            if (size != (long)this.array.length) {
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d", size, this.array.length));
            }
            this.curSize = 0;
        }

        @Override
        public void accept(double i) {
            if (this.curSize >= this.array.length) {
                throw new IllegalStateException(String.format("Accept exceeded fixed size of %d", this.array.length));
            }
            this.array[this.curSize++] = i;
        }

        @Override
        public void end() {
            if (this.curSize < this.array.length) {
                throw new IllegalStateException(String.format("End size %d is less than fixed size %d", this.curSize, this.array.length));
            }
        }

        @Override
        public String toString() {
            return String.format("DoubleFixedNodeBuilder[%d][%s]", this.array.length - this.curSize, Arrays.toString(this.array));
        }
    }

    private static final class DoubleSpinedNodeBuilder
    extends SpinedBuffer.OfDouble
    implements Node.OfDouble,
    Node.Builder.OfDouble {
        private boolean building = false;

        DoubleSpinedNodeBuilder() {
        }

        @Override
        public Spliterator.OfDouble spliterator() {
            assert (!this.building) : "during building";
            return super.spliterator();
        }

        @Override
        public void forEach(DoubleConsumer consumer) {
            assert (!this.building) : "during building";
            super.forEach(consumer);
        }

        @Override
        public void begin(long size) {
            assert (!this.building) : "was already building";
            this.building = true;
            this.clear();
            this.ensureCapacity(size);
        }

        @Override
        public void accept(double i) {
            assert (this.building) : "not building";
            super.accept(i);
        }

        @Override
        public void end() {
            assert (this.building) : "was not building";
            this.building = false;
        }

        @Override
        public void copyInto(double[] array, int offset) {
            assert (!this.building) : "during building";
            super.copyInto(array, offset);
        }

        @Override
        public double[] asPrimitiveArray() {
            assert (!this.building) : "during building";
            return (double[])super.asPrimitiveArray();
        }

        @Override
        public Node.OfDouble build() {
            assert (!this.building) : "during building";
            return this;
        }
    }

    private static abstract class SizedCollectorTask<P_IN, P_OUT, T_SINK extends Sink<P_OUT>, K extends SizedCollectorTask<P_IN, P_OUT, T_SINK, K>>
    extends CountedCompleter<Void>
    implements Sink<P_OUT> {
        protected final Spliterator<P_IN> spliterator;
        protected final PipelineHelper<P_OUT> helper;
        protected final long targetSize;
        protected long offset;
        protected long length;
        protected int index;
        protected int fence;

        SizedCollectorTask(Spliterator<P_IN> spliterator, PipelineHelper<P_OUT> helper, int arrayLength) {
            assert (spliterator.hasCharacteristics(16384));
            this.spliterator = spliterator;
            this.helper = helper;
            this.targetSize = AbstractTask.suggestTargetSize(spliterator.estimateSize());
            this.offset = 0L;
            this.length = arrayLength;
        }

        SizedCollectorTask(K parent, Spliterator<P_IN> spliterator, long offset, long length, int arrayLength) {
            super((CountedCompleter<?>)parent);
            assert (spliterator.hasCharacteristics(16384));
            this.spliterator = spliterator;
            this.helper = ((SizedCollectorTask)parent).helper;
            this.targetSize = ((SizedCollectorTask)parent).targetSize;
            this.offset = offset;
            this.length = length;
            if (offset < 0L || length < 0L || offset + length - 1L >= (long)arrayLength) {
                throw new IllegalArgumentException(String.format("offset and length interval [%d, %d + %d) is not within array size interval [0, %d)", offset, offset, length, arrayLength));
            }
        }

        @Override
        public void compute() {
            Spliterator<P_IN> leftSplit;
            SizedCollectorTask<P_IN, P_OUT, T_SINK, K> task = this;
            Spliterator<P_IN> rightSplit = this.spliterator;
            while (rightSplit.estimateSize() > task.targetSize && (leftSplit = rightSplit.trySplit()) != null) {
                task.setPendingCount(1);
                long leftSplitSize = leftSplit.estimateSize();
                ((ForkJoinTask)task.makeChild(leftSplit, task.offset, leftSplitSize)).fork();
                task = task.makeChild(rightSplit, task.offset + leftSplitSize, task.length - leftSplitSize);
            }
            assert (task.offset + task.length < 0x7FFFFFF7L);
            SizedCollectorTask sink = task;
            task.helper.wrapAndCopyInto(sink, rightSplit);
            task.propagateCompletion();
        }

        abstract K makeChild(Spliterator<P_IN> var1, long var2, long var4);

        @Override
        public void begin(long size) {
            if (size > this.length) {
                throw new IllegalStateException("size passed to Sink.begin exceeds array length");
            }
            this.index = (int)this.offset;
            this.fence = this.index + (int)this.length;
        }

        static final class OfDouble<P_IN>
        extends SizedCollectorTask<P_IN, Double, Sink.OfDouble, OfDouble<P_IN>>
        implements Sink.OfDouble {
            private final double[] array;

            OfDouble(Spliterator<P_IN> spliterator, PipelineHelper<Double> helper, double[] array) {
                super(spliterator, helper, array.length);
                this.array = array;
            }

            OfDouble(OfDouble<P_IN> parent, Spliterator<P_IN> spliterator, long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            @Override
            OfDouble<P_IN> makeChild(Spliterator<P_IN> spliterator, long offset, long size) {
                return new OfDouble<P_IN>(this, spliterator, offset, size);
            }

            @Override
            public void accept(double value) {
                if (this.index >= this.fence) {
                    throw new IndexOutOfBoundsException(Integer.toString(this.index));
                }
                this.array[this.index++] = value;
            }
        }

        static final class OfLong<P_IN>
        extends SizedCollectorTask<P_IN, Long, Sink.OfLong, OfLong<P_IN>>
        implements Sink.OfLong {
            private final long[] array;

            OfLong(Spliterator<P_IN> spliterator, PipelineHelper<Long> helper, long[] array) {
                super(spliterator, helper, array.length);
                this.array = array;
            }

            OfLong(OfLong<P_IN> parent, Spliterator<P_IN> spliterator, long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            @Override
            OfLong<P_IN> makeChild(Spliterator<P_IN> spliterator, long offset, long size) {
                return new OfLong<P_IN>(this, spliterator, offset, size);
            }

            @Override
            public void accept(long value) {
                if (this.index >= this.fence) {
                    throw new IndexOutOfBoundsException(Integer.toString(this.index));
                }
                this.array[this.index++] = value;
            }
        }

        static final class OfInt<P_IN>
        extends SizedCollectorTask<P_IN, Integer, Sink.OfInt, OfInt<P_IN>>
        implements Sink.OfInt {
            private final int[] array;

            OfInt(Spliterator<P_IN> spliterator, PipelineHelper<Integer> helper, int[] array) {
                super(spliterator, helper, array.length);
                this.array = array;
            }

            OfInt(OfInt<P_IN> parent, Spliterator<P_IN> spliterator, long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            @Override
            OfInt<P_IN> makeChild(Spliterator<P_IN> spliterator, long offset, long size) {
                return new OfInt<P_IN>(this, spliterator, offset, size);
            }

            @Override
            public void accept(int value) {
                if (this.index >= this.fence) {
                    throw new IndexOutOfBoundsException(Integer.toString(this.index));
                }
                this.array[this.index++] = value;
            }
        }

        static final class OfRef<P_IN, P_OUT>
        extends SizedCollectorTask<P_IN, P_OUT, Sink<P_OUT>, OfRef<P_IN, P_OUT>>
        implements Sink<P_OUT> {
            private final P_OUT[] array;

            OfRef(Spliterator<P_IN> spliterator, PipelineHelper<P_OUT> helper, P_OUT[] array) {
                super(spliterator, helper, array.length);
                this.array = array;
            }

            OfRef(OfRef<P_IN, P_OUT> parent, Spliterator<P_IN> spliterator, long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            @Override
            OfRef<P_IN, P_OUT> makeChild(Spliterator<P_IN> spliterator, long offset, long size) {
                return new OfRef<P_IN, P_OUT>(this, spliterator, offset, size);
            }

            @Override
            public void accept(P_OUT value) {
                if (this.index >= this.fence) {
                    throw new IndexOutOfBoundsException(Integer.toString(this.index));
                }
                this.array[this.index++] = value;
            }
        }
    }

    private static class CollectorTask<P_IN, P_OUT, T_NODE extends Node<P_OUT>, T_BUILDER extends Node.Builder<P_OUT>>
    extends AbstractTask<P_IN, P_OUT, T_NODE, CollectorTask<P_IN, P_OUT, T_NODE, T_BUILDER>> {
        protected final PipelineHelper<P_OUT> helper;
        protected final LongFunction<T_BUILDER> builderFactory;
        protected final BinaryOperator<T_NODE> concFactory;

        CollectorTask(PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator, LongFunction<T_BUILDER> builderFactory, BinaryOperator<T_NODE> concFactory) {
            super(helper, spliterator);
            this.helper = helper;
            this.builderFactory = builderFactory;
            this.concFactory = concFactory;
        }

        CollectorTask(CollectorTask<P_IN, P_OUT, T_NODE, T_BUILDER> parent, Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            this.helper = parent.helper;
            this.builderFactory = parent.builderFactory;
            this.concFactory = parent.concFactory;
        }

        @Override
        protected CollectorTask<P_IN, P_OUT, T_NODE, T_BUILDER> makeChild(Spliterator<P_IN> spliterator) {
            return new CollectorTask<P_IN, P_OUT, T_NODE, T_BUILDER>(this, spliterator);
        }

        @Override
        protected T_NODE doLeaf() {
            Node.Builder builder = (Node.Builder)this.builderFactory.apply(this.helper.exactOutputSizeIfKnown(this.spliterator));
            return (T_NODE)this.helper.wrapAndCopyInto(builder, this.spliterator).build();
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            if (!this.isLeaf()) {
                this.setLocalResult((Node)this.concFactory.apply((Node)((CollectorTask)this.leftChild).getLocalResult(), (Node)((CollectorTask)this.rightChild).getLocalResult()));
            }
            super.onCompletion(caller);
        }

        private static final class OfDouble<P_IN>
        extends CollectorTask<P_IN, Double, Node.OfDouble, Node.Builder.OfDouble> {
            OfDouble(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, Nodes::doubleBuilder, ConcNode.OfDouble::new);
            }
        }

        private static final class OfLong<P_IN>
        extends CollectorTask<P_IN, Long, Node.OfLong, Node.Builder.OfLong> {
            OfLong(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, Nodes::longBuilder, ConcNode.OfLong::new);
            }
        }

        private static final class OfInt<P_IN>
        extends CollectorTask<P_IN, Integer, Node.OfInt, Node.Builder.OfInt> {
            OfInt(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, Nodes::intBuilder, ConcNode.OfInt::new);
            }
        }

        private static final class OfRef<P_IN, P_OUT>
        extends CollectorTask<P_IN, P_OUT, Node<P_OUT>, Node.Builder<P_OUT>> {
            OfRef(PipelineHelper<P_OUT> helper, IntFunction<P_OUT[]> generator, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, s -> Nodes.builder(s, generator), ConcNode::new);
            }
        }
    }

    private static abstract class ToArrayTask<T, T_NODE extends Node<T>, K extends ToArrayTask<T, T_NODE, K>>
    extends CountedCompleter<Void> {
        protected final T_NODE node;
        protected final int offset;

        ToArrayTask(T_NODE node, int offset) {
            this.node = node;
            this.offset = offset;
        }

        ToArrayTask(K parent, T_NODE node, int offset) {
            super((CountedCompleter<?>)parent);
            this.node = node;
            this.offset = offset;
        }

        abstract void copyNodeToArray();

        abstract K makeChild(int var1, int var2);

        @Override
        public void compute() {
            ToArrayTask<T, T_NODE, K> task = this;
            while (true) {
                int i;
                if (task.node.getChildCount() == 0) {
                    task.copyNodeToArray();
                    task.propagateCompletion();
                    return;
                }
                task.setPendingCount(task.node.getChildCount() - 1);
                long size = 0L;
                for (i = 0; i < task.node.getChildCount() - 1; ++i) {
                    K leftTask = task.makeChild(i, (int)((long)task.offset + size));
                    size += ((ToArrayTask)leftTask).node.count();
                    ((ForkJoinTask)leftTask).fork();
                }
                task = task.makeChild(i, (int)((long)task.offset + size));
            }
        }

        private static final class OfDouble
        extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble> {
            private OfDouble(Node.OfDouble node, double[] array, int offset) {
                super(node, array, offset);
            }
        }

        private static final class OfLong
        extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, Node.OfLong> {
            private OfLong(Node.OfLong node, long[] array, int offset) {
                super(node, array, offset);
            }
        }

        private static final class OfInt
        extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt> {
            private OfInt(Node.OfInt node, int[] array, int offset) {
                super(node, array, offset);
            }
        }

        private static class OfPrimitive<T, T_CONS, T_ARR, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>, T_NODE extends Node.OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>>
        extends ToArrayTask<T, T_NODE, OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>> {
            private final T_ARR array;

            private OfPrimitive(T_NODE node, T_ARR array, int offset) {
                super(node, offset);
                this.array = array;
            }

            private OfPrimitive(OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE> parent, T_NODE node, int offset) {
                super(parent, node, offset);
                this.array = parent.array;
            }

            @Override
            OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE> makeChild(int childIndex, int offset) {
                return new OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, Node>(this, ((Node.OfPrimitive)this.node).getChild(childIndex), offset);
            }

            @Override
            void copyNodeToArray() {
                ((Node.OfPrimitive)this.node).copyInto(this.array, this.offset);
            }
        }

        private static final class OfRef<T>
        extends ToArrayTask<T, Node<T>, OfRef<T>> {
            private final T[] array;

            private OfRef(Node<T> node, T[] array, int offset) {
                super(node, offset);
                this.array = array;
            }

            private OfRef(OfRef<T> parent, Node<T> node, int offset) {
                super(parent, node, offset);
                this.array = parent.array;
            }

            @Override
            OfRef<T> makeChild(int childIndex, int offset) {
                return new OfRef(this, this.node.getChild(childIndex), offset);
            }

            @Override
            void copyNodeToArray() {
                this.node.copyInto(this.array, this.offset);
            }
        }
    }

    private static abstract class EmptyNode<T, T_ARR, T_CONS>
    implements Node<T> {
        EmptyNode() {
        }

        @Override
        public T[] asArray(IntFunction<T[]> generator) {
            return generator.apply(0);
        }

        public void copyInto(T_ARR array, int offset) {
        }

        @Override
        public long count() {
            return 0L;
        }

        public void forEach(T_CONS consumer) {
        }

        private static final class OfDouble
        extends EmptyNode<Double, double[], DoubleConsumer>
        implements Node.OfDouble {
            OfDouble() {
            }

            @Override
            public Spliterator.OfDouble spliterator() {
                return Spliterators.emptyDoubleSpliterator();
            }

            @Override
            public double[] asPrimitiveArray() {
                return EMPTY_DOUBLE_ARRAY;
            }
        }

        private static final class OfLong
        extends EmptyNode<Long, long[], LongConsumer>
        implements Node.OfLong {
            OfLong() {
            }

            @Override
            public Spliterator.OfLong spliterator() {
                return Spliterators.emptyLongSpliterator();
            }

            @Override
            public long[] asPrimitiveArray() {
                return EMPTY_LONG_ARRAY;
            }
        }

        private static final class OfInt
        extends EmptyNode<Integer, int[], IntConsumer>
        implements Node.OfInt {
            OfInt() {
            }

            @Override
            public Spliterator.OfInt spliterator() {
                return Spliterators.emptyIntSpliterator();
            }

            @Override
            public int[] asPrimitiveArray() {
                return EMPTY_INT_ARRAY;
            }
        }

        private static class OfRef<T>
        extends EmptyNode<T, T[], Consumer<? super T>> {
            private OfRef() {
            }

            @Override
            public Spliterator<T> spliterator() {
                return Spliterators.emptySpliterator();
            }
        }
    }

    private static abstract class InternalNodeSpliterator<T, S extends Spliterator<T>, N extends Node<T>>
    implements Spliterator<T> {
        N curNode;
        int curChildIndex;
        S lastNodeSpliterator;
        S tryAdvanceSpliterator;
        Deque<N> tryAdvanceStack;

        InternalNodeSpliterator(N curNode) {
            this.curNode = curNode;
        }

        protected final Deque<N> initStack() {
            ArrayDeque stack = new ArrayDeque(8);
            for (int i = this.curNode.getChildCount() - 1; i >= this.curChildIndex; --i) {
                stack.addFirst(this.curNode.getChild(i));
            }
            return stack;
        }

        protected final N findNextLeafNode(Deque<N> stack) {
            Node n = null;
            while ((n = (Node)stack.pollFirst()) != null) {
                if (n.getChildCount() == 0) {
                    if (n.count() <= 0L) continue;
                    return (N)n;
                }
                for (int i = n.getChildCount() - 1; i >= 0; --i) {
                    stack.addFirst(n.getChild(i));
                }
            }
            return null;
        }

        /*
         * Enabled aggressive block sorting
         */
        protected final boolean initTryAdvance() {
            if (this.curNode == null) {
                return false;
            }
            if (this.tryAdvanceSpliterator != null) return true;
            if (this.lastNodeSpliterator != null) {
                this.tryAdvanceSpliterator = this.lastNodeSpliterator;
                return true;
            }
            this.tryAdvanceStack = this.initStack();
            N leaf = this.findNextLeafNode(this.tryAdvanceStack);
            if (leaf != null) {
                this.tryAdvanceSpliterator = leaf.spliterator();
                return true;
            }
            this.curNode = null;
            return false;
        }

        public final S trySplit() {
            if (this.curNode == null || this.tryAdvanceSpliterator != null) {
                return null;
            }
            if (this.lastNodeSpliterator != null) {
                return (S)this.lastNodeSpliterator.trySplit();
            }
            if (this.curChildIndex < this.curNode.getChildCount() - 1) {
                return (S)this.curNode.getChild(this.curChildIndex++).spliterator();
            }
            this.curNode = this.curNode.getChild(this.curChildIndex);
            if (this.curNode.getChildCount() == 0) {
                this.lastNodeSpliterator = this.curNode.spliterator();
                return (S)this.lastNodeSpliterator.trySplit();
            }
            this.curChildIndex = 0;
            return (S)this.curNode.getChild(this.curChildIndex++).spliterator();
        }

        @Override
        public final long estimateSize() {
            if (this.curNode == null) {
                return 0L;
            }
            if (this.lastNodeSpliterator != null) {
                return this.lastNodeSpliterator.estimateSize();
            }
            long size = 0L;
            for (int i = this.curChildIndex; i < this.curNode.getChildCount(); ++i) {
                size += this.curNode.getChild(i).count();
            }
            return size;
        }

        @Override
        public final int characteristics() {
            return 64;
        }

        private static final class OfDouble
        extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble>
        implements Spliterator.OfDouble {
            OfDouble(Node.OfDouble cur) {
                super(cur);
            }
        }

        private static final class OfLong
        extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, Node.OfLong>
        implements Spliterator.OfLong {
            OfLong(Node.OfLong cur) {
                super(cur);
            }
        }

        private static final class OfInt
        extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt>
        implements Spliterator.OfInt {
            OfInt(Node.OfInt cur) {
                super(cur);
            }
        }

        private static abstract class OfPrimitive<T, T_CONS, T_ARR, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>, N extends Node.OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, N>>
        extends InternalNodeSpliterator<T, T_SPLITR, N>
        implements Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {
            OfPrimitive(N cur) {
                super(cur);
            }

            @Override
            public boolean tryAdvance(T_CONS consumer) {
                if (!this.initTryAdvance()) {
                    return false;
                }
                boolean hasNext = ((Spliterator.OfPrimitive)this.tryAdvanceSpliterator).tryAdvance(consumer);
                if (!hasNext) {
                    Node.OfPrimitive leaf;
                    if (this.lastNodeSpliterator == null && (leaf = (Node.OfPrimitive)this.findNextLeafNode(this.tryAdvanceStack)) != null) {
                        this.tryAdvanceSpliterator = leaf.spliterator();
                        return ((Spliterator.OfPrimitive)this.tryAdvanceSpliterator).tryAdvance(consumer);
                    }
                    this.curNode = null;
                }
                return hasNext;
            }

            @Override
            public void forEachRemaining(T_CONS consumer) {
                if (this.curNode == null) {
                    return;
                }
                if (this.tryAdvanceSpliterator == null) {
                    if (this.lastNodeSpliterator == null) {
                        Node.OfPrimitive leaf;
                        Deque stack = this.initStack();
                        while ((leaf = (Node.OfPrimitive)this.findNextLeafNode(stack)) != null) {
                            leaf.forEach(consumer);
                        }
                        this.curNode = null;
                    } else {
                        ((Spliterator.OfPrimitive)this.lastNodeSpliterator).forEachRemaining(consumer);
                    }
                } else {
                    while (this.tryAdvance(consumer)) {
                    }
                }
            }
        }

        private static final class OfRef<T>
        extends InternalNodeSpliterator<T, Spliterator<T>, Node<T>> {
            OfRef(Node<T> curNode) {
                super(curNode);
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> consumer) {
                if (!this.initTryAdvance()) {
                    return false;
                }
                boolean hasNext = this.tryAdvanceSpliterator.tryAdvance(consumer);
                if (!hasNext) {
                    Object leaf;
                    if (this.lastNodeSpliterator == null && (leaf = this.findNextLeafNode(this.tryAdvanceStack)) != null) {
                        this.tryAdvanceSpliterator = leaf.spliterator();
                        return this.tryAdvanceSpliterator.tryAdvance(consumer);
                    }
                    this.curNode = null;
                }
                return hasNext;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> consumer) {
                if (this.curNode == null) {
                    return;
                }
                if (this.tryAdvanceSpliterator == null) {
                    if (this.lastNodeSpliterator == null) {
                        Object leaf;
                        Deque stack = this.initStack();
                        while ((leaf = this.findNextLeafNode(stack)) != null) {
                            leaf.forEach(consumer);
                        }
                        this.curNode = null;
                    } else {
                        this.lastNodeSpliterator.forEachRemaining(consumer);
                    }
                } else {
                    while (this.tryAdvance(consumer)) {
                    }
                }
            }
        }
    }

    private static abstract class AbstractConcNode<T, T_NODE extends Node<T>>
    implements Node<T> {
        protected final T_NODE left;
        protected final T_NODE right;
        private final long size;

        AbstractConcNode(T_NODE left, T_NODE right) {
            this.left = left;
            this.right = right;
            this.size = left.count() + right.count();
        }

        @Override
        public int getChildCount() {
            return 2;
        }

        public T_NODE getChild(int i) {
            if (i == 0) {
                return this.left;
            }
            if (i == 1) {
                return this.right;
            }
            throw new IndexOutOfBoundsException();
        }

        @Override
        public long count() {
            return this.size;
        }
    }
}

