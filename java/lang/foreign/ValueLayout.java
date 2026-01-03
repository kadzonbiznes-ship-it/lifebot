/*
 * Decompiled with CFR 0.152.
 */
package java.lang.foreign;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import jdk.internal.foreign.layout.ValueLayouts;
import jdk.internal.javac.PreviewFeature;

@PreviewFeature(feature=PreviewFeature.Feature.FOREIGN)
public sealed interface ValueLayout
extends MemoryLayout
permits OfBoolean, OfByte, OfChar, OfShort, OfInt, OfFloat, OfLong, OfDouble, AddressLayout {
    public static final AddressLayout ADDRESS = ValueLayouts.OfAddressImpl.of(ByteOrder.nativeOrder());
    public static final OfByte JAVA_BYTE = ValueLayouts.OfByteImpl.of(ByteOrder.nativeOrder());
    public static final OfBoolean JAVA_BOOLEAN = ValueLayouts.OfBooleanImpl.of(ByteOrder.nativeOrder());
    public static final OfChar JAVA_CHAR = ValueLayouts.OfCharImpl.of(ByteOrder.nativeOrder());
    public static final OfShort JAVA_SHORT = ValueLayouts.OfShortImpl.of(ByteOrder.nativeOrder());
    public static final OfInt JAVA_INT = ValueLayouts.OfIntImpl.of(ByteOrder.nativeOrder());
    public static final OfLong JAVA_LONG = ValueLayouts.OfLongImpl.of(ByteOrder.nativeOrder());
    public static final OfFloat JAVA_FLOAT = ValueLayouts.OfFloatImpl.of(ByteOrder.nativeOrder());
    public static final OfDouble JAVA_DOUBLE = ValueLayouts.OfDoubleImpl.of(ByteOrder.nativeOrder());
    public static final AddressLayout ADDRESS_UNALIGNED = ADDRESS.withByteAlignment(1L);
    public static final OfChar JAVA_CHAR_UNALIGNED = JAVA_CHAR.withByteAlignment(1L);
    public static final OfShort JAVA_SHORT_UNALIGNED = JAVA_SHORT.withByteAlignment(1L);
    public static final OfInt JAVA_INT_UNALIGNED = JAVA_INT.withByteAlignment(1L);
    public static final OfLong JAVA_LONG_UNALIGNED = JAVA_LONG.withByteAlignment(1L);
    public static final OfFloat JAVA_FLOAT_UNALIGNED = JAVA_FLOAT.withByteAlignment(1L);
    public static final OfDouble JAVA_DOUBLE_UNALIGNED = JAVA_DOUBLE.withByteAlignment(1L);

    public ByteOrder order();

    public ValueLayout withOrder(ByteOrder var1);

    @Override
    public ValueLayout withoutName();

    public VarHandle arrayElementVarHandle(int ... var1);

    public Class<?> carrier();

    @Override
    public ValueLayout withName(String var1);

    @Override
    public ValueLayout withByteAlignment(long var1);

    public static sealed interface OfByte
    extends ValueLayout
    permits ValueLayouts.OfByteImpl {
        @Override
        public OfByte withName(String var1);

        @Override
        public OfByte withoutName();

        @Override
        public OfByte withByteAlignment(long var1);

        @Override
        public OfByte withOrder(ByteOrder var1);
    }

    public static sealed interface OfBoolean
    extends ValueLayout
    permits ValueLayouts.OfBooleanImpl {
        @Override
        public OfBoolean withName(String var1);

        @Override
        public OfBoolean withoutName();

        @Override
        public OfBoolean withByteAlignment(long var1);

        @Override
        public OfBoolean withOrder(ByteOrder var1);
    }

    public static sealed interface OfChar
    extends ValueLayout
    permits ValueLayouts.OfCharImpl {
        @Override
        public OfChar withName(String var1);

        @Override
        public OfChar withoutName();

        @Override
        public OfChar withByteAlignment(long var1);

        @Override
        public OfChar withOrder(ByteOrder var1);
    }

    public static sealed interface OfShort
    extends ValueLayout
    permits ValueLayouts.OfShortImpl {
        @Override
        public OfShort withName(String var1);

        @Override
        public OfShort withoutName();

        @Override
        public OfShort withByteAlignment(long var1);

        @Override
        public OfShort withOrder(ByteOrder var1);
    }

    public static sealed interface OfInt
    extends ValueLayout
    permits ValueLayouts.OfIntImpl {
        @Override
        public OfInt withName(String var1);

        @Override
        public OfInt withoutName();

        @Override
        public OfInt withByteAlignment(long var1);

        @Override
        public OfInt withOrder(ByteOrder var1);
    }

    public static sealed interface OfLong
    extends ValueLayout
    permits ValueLayouts.OfLongImpl {
        @Override
        public OfLong withName(String var1);

        @Override
        public OfLong withoutName();

        @Override
        public OfLong withByteAlignment(long var1);

        @Override
        public OfLong withOrder(ByteOrder var1);
    }

    public static sealed interface OfFloat
    extends ValueLayout
    permits ValueLayouts.OfFloatImpl {
        @Override
        public OfFloat withName(String var1);

        @Override
        public OfFloat withoutName();

        @Override
        public OfFloat withByteAlignment(long var1);

        @Override
        public OfFloat withOrder(ByteOrder var1);
    }

    public static sealed interface OfDouble
    extends ValueLayout
    permits ValueLayouts.OfDoubleImpl {
        @Override
        public OfDouble withName(String var1);

        @Override
        public OfDouble withoutName();

        @Override
        public OfDouble withByteAlignment(long var1);

        @Override
        public OfDouble withOrder(ByteOrder var1);
    }
}

