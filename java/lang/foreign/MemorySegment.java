/*
 * Decompiled with CFR 0.152.
 */
package java.lang.foreign;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import jdk.internal.foreign.AbstractMemorySegmentImpl;
import jdk.internal.foreign.HeapMemorySegmentImpl;
import jdk.internal.foreign.MemorySessionImpl;
import jdk.internal.foreign.NativeMemorySegmentImpl;
import jdk.internal.foreign.Utils;
import jdk.internal.foreign.abi.SharedUtils;
import jdk.internal.foreign.layout.ValueLayouts;
import jdk.internal.javac.PreviewFeature;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.vm.annotation.ForceInline;

@PreviewFeature(feature=PreviewFeature.Feature.FOREIGN)
public sealed interface MemorySegment
permits AbstractMemorySegmentImpl {
    public static final MemorySegment NULL = new NativeMemorySegmentImpl();

    public long address();

    public Optional<Object> heapBase();

    public Spliterator<MemorySegment> spliterator(MemoryLayout var1);

    public Stream<MemorySegment> elements(MemoryLayout var1);

    public Scope scope();

    public boolean isAccessibleBy(Thread var1);

    public long byteSize();

    public MemorySegment asSlice(long var1, long var3);

    public MemorySegment asSlice(long var1, long var3, long var5);

    default public MemorySegment asSlice(long offset, MemoryLayout layout) {
        Objects.requireNonNull(layout);
        return this.asSlice(offset, layout.byteSize(), layout.byteAlignment());
    }

    public MemorySegment asSlice(long var1);

    @CallerSensitive
    public MemorySegment reinterpret(long var1);

    @CallerSensitive
    public MemorySegment reinterpret(Arena var1, Consumer<MemorySegment> var2);

    @CallerSensitive
    public MemorySegment reinterpret(long var1, Arena var3, Consumer<MemorySegment> var4);

    public boolean isReadOnly();

    public MemorySegment asReadOnly();

    public boolean isNative();

    public boolean isMapped();

    public Optional<MemorySegment> asOverlappingSlice(MemorySegment var1);

    public long segmentOffset(MemorySegment var1);

    public MemorySegment fill(byte var1);

    default public MemorySegment copyFrom(MemorySegment src) {
        MemorySegment.copy(src, 0L, this, 0L, src.byteSize());
        return this;
    }

    default public long mismatch(MemorySegment other) {
        Objects.requireNonNull(other);
        return MemorySegment.mismatch(this, 0L, this.byteSize(), other, 0L, other.byteSize());
    }

    public boolean isLoaded();

    public void load();

    public void unload();

    public void force();

    public ByteBuffer asByteBuffer();

    public byte[] toArray(ValueLayout.OfByte var1);

    public short[] toArray(ValueLayout.OfShort var1);

    public char[] toArray(ValueLayout.OfChar var1);

    public int[] toArray(ValueLayout.OfInt var1);

    public float[] toArray(ValueLayout.OfFloat var1);

    public long[] toArray(ValueLayout.OfLong var1);

    public double[] toArray(ValueLayout.OfDouble var1);

    default public String getUtf8String(long offset) {
        return SharedUtils.toJavaStringInternal((MemorySegment)this, (long)offset);
    }

    default public void setUtf8String(long offset, String str) {
        Utils.toCString((byte[])str.getBytes(StandardCharsets.UTF_8), (SegmentAllocator)SegmentAllocator.prefixAllocator(this.asSlice(offset)));
    }

    public static MemorySegment ofBuffer(Buffer buffer) {
        return AbstractMemorySegmentImpl.ofBuffer(buffer);
    }

    public static MemorySegment ofArray(byte[] byteArray) {
        return HeapMemorySegmentImpl.OfByte.fromArray((byte[])byteArray);
    }

    public static MemorySegment ofArray(char[] charArray) {
        return HeapMemorySegmentImpl.OfChar.fromArray((char[])charArray);
    }

    public static MemorySegment ofArray(short[] shortArray) {
        return HeapMemorySegmentImpl.OfShort.fromArray((short[])shortArray);
    }

    public static MemorySegment ofArray(int[] intArray) {
        return HeapMemorySegmentImpl.OfInt.fromArray((int[])intArray);
    }

    public static MemorySegment ofArray(float[] floatArray) {
        return HeapMemorySegmentImpl.OfFloat.fromArray((float[])floatArray);
    }

    public static MemorySegment ofArray(long[] longArray) {
        return HeapMemorySegmentImpl.OfLong.fromArray((long[])longArray);
    }

    public static MemorySegment ofArray(double[] doubleArray) {
        return HeapMemorySegmentImpl.OfDouble.fromArray((double[])doubleArray);
    }

    public static MemorySegment ofAddress(long address) {
        return NativeMemorySegmentImpl.makeNativeSegmentUnchecked((long)address, (long)0L);
    }

    @ForceInline
    public static void copy(MemorySegment srcSegment, long srcOffset, MemorySegment dstSegment, long dstOffset, long bytes) {
        MemorySegment.copy(srcSegment, ValueLayout.JAVA_BYTE, srcOffset, dstSegment, ValueLayout.JAVA_BYTE, dstOffset, bytes);
    }

    @ForceInline
    public static void copy(MemorySegment srcSegment, ValueLayout srcElementLayout, long srcOffset, MemorySegment dstSegment, ValueLayout dstElementLayout, long dstOffset, long elementCount) {
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(srcElementLayout);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(dstElementLayout);
        AbstractMemorySegmentImpl.copy(srcSegment, srcElementLayout, srcOffset, dstSegment, dstElementLayout, dstOffset, elementCount);
    }

    @ForceInline
    default public byte get(ValueLayout.OfByte layout, long offset) {
        return ((ValueLayouts.OfByteImpl)layout).accessHandle().get(this, offset);
    }

    @ForceInline
    default public void set(ValueLayout.OfByte layout, long offset, byte value) {
        ((ValueLayouts.OfByteImpl)layout).accessHandle().set(this, offset, value);
    }

    @ForceInline
    default public boolean get(ValueLayout.OfBoolean layout, long offset) {
        return ((ValueLayouts.OfBooleanImpl)layout).accessHandle().get(this, offset);
    }

    @ForceInline
    default public void set(ValueLayout.OfBoolean layout, long offset, boolean value) {
        ((ValueLayouts.OfBooleanImpl)layout).accessHandle().set(this, offset, value);
    }

    @ForceInline
    default public char get(ValueLayout.OfChar layout, long offset) {
        return ((ValueLayouts.OfCharImpl)layout).accessHandle().get(this, offset);
    }

    @ForceInline
    default public void set(ValueLayout.OfChar layout, long offset, char value) {
        ((ValueLayouts.OfCharImpl)layout).accessHandle().set(this, offset, value);
    }

    @ForceInline
    default public short get(ValueLayout.OfShort layout, long offset) {
        return ((ValueLayouts.OfShortImpl)layout).accessHandle().get(this, offset);
    }

    @ForceInline
    default public void set(ValueLayout.OfShort layout, long offset, short value) {
        ((ValueLayouts.OfShortImpl)layout).accessHandle().set(this, offset, value);
    }

    @ForceInline
    default public int get(ValueLayout.OfInt layout, long offset) {
        return ((ValueLayouts.OfIntImpl)layout).accessHandle().get(this, offset);
    }

    @ForceInline
    default public void set(ValueLayout.OfInt layout, long offset, int value) {
        ((ValueLayouts.OfIntImpl)layout).accessHandle().set(this, offset, value);
    }

    @ForceInline
    default public float get(ValueLayout.OfFloat layout, long offset) {
        return ((ValueLayouts.OfFloatImpl)layout).accessHandle().get(this, offset);
    }

    @ForceInline
    default public void set(ValueLayout.OfFloat layout, long offset, float value) {
        ((ValueLayouts.OfFloatImpl)layout).accessHandle().set(this, offset, value);
    }

    @ForceInline
    default public long get(ValueLayout.OfLong layout, long offset) {
        return ((ValueLayouts.OfLongImpl)layout).accessHandle().get(this, offset);
    }

    @ForceInline
    default public void set(ValueLayout.OfLong layout, long offset, long value) {
        ((ValueLayouts.OfLongImpl)layout).accessHandle().set(this, offset, value);
    }

    @ForceInline
    default public double get(ValueLayout.OfDouble layout, long offset) {
        return ((ValueLayouts.OfDoubleImpl)layout).accessHandle().get(this, offset);
    }

    @ForceInline
    default public void set(ValueLayout.OfDouble layout, long offset, double value) {
        ((ValueLayouts.OfDoubleImpl)layout).accessHandle().set(this, offset, value);
    }

    @ForceInline
    default public MemorySegment get(AddressLayout layout, long offset) {
        return ((ValueLayouts.OfAddressImpl)layout).accessHandle().get(this, offset);
    }

    @ForceInline
    default public void set(AddressLayout layout, long offset, MemorySegment value) {
        ((ValueLayouts.OfAddressImpl)layout).accessHandle().set(this, offset, value);
    }

    @ForceInline
    default public byte getAtIndex(ValueLayout.OfByte layout, long index) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        return ((ValueLayouts.OfByteImpl)layout).accessHandle().get(this, index * layout.byteSize());
    }

    @ForceInline
    default public boolean getAtIndex(ValueLayout.OfBoolean layout, long index) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        return ((ValueLayouts.OfBooleanImpl)layout).accessHandle().get(this, index * layout.byteSize());
    }

    @ForceInline
    default public char getAtIndex(ValueLayout.OfChar layout, long index) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        return ((ValueLayouts.OfCharImpl)layout).accessHandle().get(this, index * layout.byteSize());
    }

    @ForceInline
    default public void setAtIndex(ValueLayout.OfChar layout, long index, char value) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        ((ValueLayouts.OfCharImpl)layout).accessHandle().set(this, index * layout.byteSize(), value);
    }

    @ForceInline
    default public short getAtIndex(ValueLayout.OfShort layout, long index) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        return ((ValueLayouts.OfShortImpl)layout).accessHandle().get(this, index * layout.byteSize());
    }

    @ForceInline
    default public void setAtIndex(ValueLayout.OfByte layout, long index, byte value) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        ((ValueLayouts.OfByteImpl)layout).accessHandle().set(this, index * layout.byteSize(), value);
    }

    @ForceInline
    default public void setAtIndex(ValueLayout.OfBoolean layout, long index, boolean value) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        ((ValueLayouts.OfBooleanImpl)layout).accessHandle().set(this, index * layout.byteSize(), value);
    }

    @ForceInline
    default public void setAtIndex(ValueLayout.OfShort layout, long index, short value) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        ((ValueLayouts.OfShortImpl)layout).accessHandle().set(this, index * layout.byteSize(), value);
    }

    @ForceInline
    default public int getAtIndex(ValueLayout.OfInt layout, long index) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        return ((ValueLayouts.OfIntImpl)layout).accessHandle().get(this, index * layout.byteSize());
    }

    @ForceInline
    default public void setAtIndex(ValueLayout.OfInt layout, long index, int value) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        ((ValueLayouts.OfIntImpl)layout).accessHandle().set(this, index * layout.byteSize(), value);
    }

    @ForceInline
    default public float getAtIndex(ValueLayout.OfFloat layout, long index) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        return ((ValueLayouts.OfFloatImpl)layout).accessHandle().get(this, index * layout.byteSize());
    }

    @ForceInline
    default public void setAtIndex(ValueLayout.OfFloat layout, long index, float value) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        ((ValueLayouts.OfFloatImpl)layout).accessHandle().set(this, index * layout.byteSize(), value);
    }

    @ForceInline
    default public long getAtIndex(ValueLayout.OfLong layout, long index) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        return ((ValueLayouts.OfLongImpl)layout).accessHandle().get(this, index * layout.byteSize());
    }

    @ForceInline
    default public void setAtIndex(ValueLayout.OfLong layout, long index, long value) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        ((ValueLayouts.OfLongImpl)layout).accessHandle().set(this, index * layout.byteSize(), value);
    }

    @ForceInline
    default public double getAtIndex(ValueLayout.OfDouble layout, long index) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        return ((ValueLayouts.OfDoubleImpl)layout).accessHandle().get(this, index * layout.byteSize());
    }

    @ForceInline
    default public void setAtIndex(ValueLayout.OfDouble layout, long index, double value) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        ((ValueLayouts.OfDoubleImpl)layout).accessHandle().set(this, index * layout.byteSize(), value);
    }

    @ForceInline
    default public MemorySegment getAtIndex(AddressLayout layout, long index) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        return ((ValueLayouts.OfAddressImpl)layout).accessHandle().get(this, index * layout.byteSize());
    }

    @ForceInline
    default public void setAtIndex(AddressLayout layout, long index, MemorySegment value) {
        Utils.checkElementAlignment(layout, "Layout alignment greater than its size");
        ((ValueLayouts.OfAddressImpl)layout).accessHandle().set(this, index * layout.byteSize(), value);
    }

    public boolean equals(Object var1);

    public int hashCode();

    @ForceInline
    public static void copy(MemorySegment srcSegment, ValueLayout srcLayout, long srcOffset, Object dstArray, int dstIndex, int elementCount) {
        Objects.requireNonNull(srcSegment);
        Objects.requireNonNull(dstArray);
        Objects.requireNonNull(srcLayout);
        AbstractMemorySegmentImpl.copy(srcSegment, srcLayout, srcOffset, dstArray, dstIndex, elementCount);
    }

    @ForceInline
    public static void copy(Object srcArray, int srcIndex, MemorySegment dstSegment, ValueLayout dstLayout, long dstOffset, int elementCount) {
        Objects.requireNonNull(srcArray);
        Objects.requireNonNull(dstSegment);
        Objects.requireNonNull(dstLayout);
        AbstractMemorySegmentImpl.copy(srcArray, srcIndex, dstSegment, dstLayout, dstOffset, elementCount);
    }

    public static long mismatch(MemorySegment srcSegment, long srcFromOffset, long srcToOffset, MemorySegment dstSegment, long dstFromOffset, long dstToOffset) {
        return AbstractMemorySegmentImpl.mismatch(srcSegment, srcFromOffset, srcToOffset, dstSegment, dstFromOffset, dstToOffset);
    }

    @PreviewFeature(feature=PreviewFeature.Feature.FOREIGN)
    public static sealed interface Scope
    permits MemorySessionImpl {
        public boolean isAlive();

        public boolean equals(Object var1);

        public int hashCode();
    }
}

