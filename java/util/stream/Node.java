/*
 * Decompiled with CFR 0.152.
 */
package java.util.stream;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import java.util.stream.Nodes;
import java.util.stream.Sink;
import java.util.stream.StreamShape;
import java.util.stream.Tripwire;

interface Node<T> {
    public Spliterator<T> spliterator();

    public void forEach(Consumer<? super T> var1);

    default public int getChildCount() {
        return 0;
    }

    default public Node<T> getChild(int i) {
        throw new IndexOutOfBoundsException();
    }

    default public Node<T> truncate(long from, long to, IntFunction<T[]> generator) {
        if (from == 0L && to == this.count()) {
            return this;
        }
        Spliterator<Object> spliterator = this.spliterator();
        long size = to - from;
        Builder nodeBuilder = Nodes.builder(size, generator);
        nodeBuilder.begin(size);
        int i = 0;
        while ((long)i < from && spliterator.tryAdvance(e -> {})) {
            ++i;
        }
        if (to == this.count()) {
            spliterator.forEachRemaining(nodeBuilder);
        } else {
            i = 0;
            while ((long)i < size && spliterator.tryAdvance(nodeBuilder)) {
                ++i;
            }
        }
        nodeBuilder.end();
        return nodeBuilder.build();
    }

    public T[] asArray(IntFunction<T[]> var1);

    public void copyInto(T[] var1, int var2);

    default public StreamShape getShape() {
        return StreamShape.REFERENCE;
    }

    public long count();

    public static interface Builder<T>
    extends Sink<T> {
        public Node<T> build();

        public static interface OfDouble
        extends Builder<Double>,
        Sink.OfDouble {
            public java.util.stream.Node$OfDouble build();
        }

        public static interface OfLong
        extends Builder<Long>,
        Sink.OfLong {
            public java.util.stream.Node$OfLong build();
        }

        public static interface OfInt
        extends Builder<Integer>,
        Sink.OfInt {
            public java.util.stream.Node$OfInt build();
        }
    }

    public static interface OfDouble
    extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, OfDouble> {
        @Override
        default public void forEach(Consumer<? super Double> consumer) {
            if (consumer instanceof DoubleConsumer) {
                this.forEach((DoubleConsumer)((Object)consumer));
            } else {
                if (Tripwire.ENABLED) {
                    Tripwire.trip(this.getClass(), "{0} calling Node.OfLong.forEachRemaining(Consumer)");
                }
                ((Spliterator.OfDouble)this.spliterator()).forEachRemaining(consumer);
            }
        }

        @Override
        default public void copyInto(Double[] boxed, int offset) {
            if (Tripwire.ENABLED) {
                Tripwire.trip(this.getClass(), "{0} calling Node.OfDouble.copyInto(Double[], int)");
            }
            double[] array = (double[])this.asPrimitiveArray();
            for (int i = 0; i < array.length; ++i) {
                boxed[offset + i] = array[i];
            }
        }

        @Override
        default public OfDouble truncate(long from, long to, IntFunction<Double[]> generator) {
            if (from == 0L && to == this.count()) {
                return this;
            }
            long size = to - from;
            Spliterator.OfDouble spliterator = (Spliterator.OfDouble)this.spliterator();
            Builder.OfDouble nodeBuilder = Nodes.doubleBuilder(size);
            nodeBuilder.begin(size);
            int i = 0;
            while ((long)i < from && spliterator.tryAdvance(e -> {})) {
                ++i;
            }
            if (to == this.count()) {
                spliterator.forEachRemaining(nodeBuilder);
            } else {
                i = 0;
                while ((long)i < size && spliterator.tryAdvance(nodeBuilder)) {
                    ++i;
                }
            }
            nodeBuilder.end();
            return nodeBuilder.build();
        }

        @Override
        default public double[] newArray(int count) {
            return new double[count];
        }

        @Override
        default public StreamShape getShape() {
            return StreamShape.DOUBLE_VALUE;
        }
    }

    public static interface OfLong
    extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, OfLong> {
        @Override
        default public void forEach(Consumer<? super Long> consumer) {
            if (consumer instanceof LongConsumer) {
                this.forEach((LongConsumer)((Object)consumer));
            } else {
                if (Tripwire.ENABLED) {
                    Tripwire.trip(this.getClass(), "{0} calling Node.OfLong.forEachRemaining(Consumer)");
                }
                ((Spliterator.OfLong)this.spliterator()).forEachRemaining(consumer);
            }
        }

        @Override
        default public void copyInto(Long[] boxed, int offset) {
            if (Tripwire.ENABLED) {
                Tripwire.trip(this.getClass(), "{0} calling Node.OfInt.copyInto(Long[], int)");
            }
            long[] array = (long[])this.asPrimitiveArray();
            for (int i = 0; i < array.length; ++i) {
                boxed[offset + i] = array[i];
            }
        }

        @Override
        default public OfLong truncate(long from, long to, IntFunction<Long[]> generator) {
            if (from == 0L && to == this.count()) {
                return this;
            }
            long size = to - from;
            Spliterator.OfLong spliterator = (Spliterator.OfLong)this.spliterator();
            Builder.OfLong nodeBuilder = Nodes.longBuilder(size);
            nodeBuilder.begin(size);
            int i = 0;
            while ((long)i < from && spliterator.tryAdvance(e -> {})) {
                ++i;
            }
            if (to == this.count()) {
                spliterator.forEachRemaining(nodeBuilder);
            } else {
                i = 0;
                while ((long)i < size && spliterator.tryAdvance(nodeBuilder)) {
                    ++i;
                }
            }
            nodeBuilder.end();
            return nodeBuilder.build();
        }

        @Override
        default public long[] newArray(int count) {
            return new long[count];
        }

        @Override
        default public StreamShape getShape() {
            return StreamShape.LONG_VALUE;
        }
    }

    public static interface OfInt
    extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, OfInt> {
        @Override
        default public void forEach(Consumer<? super Integer> consumer) {
            if (consumer instanceof IntConsumer) {
                this.forEach((IntConsumer)((Object)consumer));
            } else {
                if (Tripwire.ENABLED) {
                    Tripwire.trip(this.getClass(), "{0} calling Node.OfInt.forEachRemaining(Consumer)");
                }
                ((Spliterator.OfInt)this.spliterator()).forEachRemaining(consumer);
            }
        }

        @Override
        default public void copyInto(Integer[] boxed, int offset) {
            if (Tripwire.ENABLED) {
                Tripwire.trip(this.getClass(), "{0} calling Node.OfInt.copyInto(Integer[], int)");
            }
            int[] array = (int[])this.asPrimitiveArray();
            for (int i = 0; i < array.length; ++i) {
                boxed[offset + i] = array[i];
            }
        }

        @Override
        default public OfInt truncate(long from, long to, IntFunction<Integer[]> generator) {
            if (from == 0L && to == this.count()) {
                return this;
            }
            long size = to - from;
            Spliterator.OfInt spliterator = (Spliterator.OfInt)this.spliterator();
            Builder.OfInt nodeBuilder = Nodes.intBuilder(size);
            nodeBuilder.begin(size);
            int i = 0;
            while ((long)i < from && spliterator.tryAdvance(e -> {})) {
                ++i;
            }
            if (to == this.count()) {
                spliterator.forEachRemaining(nodeBuilder);
            } else {
                i = 0;
                while ((long)i < size && spliterator.tryAdvance(nodeBuilder)) {
                    ++i;
                }
            }
            nodeBuilder.end();
            return nodeBuilder.build();
        }

        @Override
        default public int[] newArray(int count) {
            return new int[count];
        }

        @Override
        default public StreamShape getShape() {
            return StreamShape.INT_VALUE;
        }
    }

    public static interface OfPrimitive<T, T_CONS, T_ARR, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>, T_NODE extends OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>>
    extends Node<T> {
        public T_SPLITR spliterator();

        public void forEach(T_CONS var1);

        default public T_NODE getChild(int i) {
            throw new IndexOutOfBoundsException();
        }

        public T_NODE truncate(long var1, long var3, IntFunction<T[]> var5);

        @Override
        default public T[] asArray(IntFunction<T[]> generator) {
            long size;
            if (Tripwire.ENABLED) {
                Tripwire.trip(this.getClass(), "{0} calling Node.OfPrimitive.asArray");
            }
            if ((size = this.count()) >= 0x7FFFFFF7L) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            T[] boxed = generator.apply((int)this.count());
            this.copyInto((T_ARR)boxed, 0);
            return boxed;
        }

        public T_ARR asPrimitiveArray();

        public T_ARR newArray(int var1);

        public void copyInto(T_ARR var1, int var2);
    }
}

