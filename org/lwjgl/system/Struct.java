/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package org.lwjgl.system;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Checks;
import org.lwjgl.system.Library;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Platform;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.StructBuffer;

public abstract class Struct<SELF extends Struct<SELF>>
extends Pointer.Default {
    protected static final int DEFAULT_PACK_ALIGNMENT = Platform.get() == Platform.WINDOWS ? 8 : 0x40000000;
    protected static final int DEFAULT_ALIGN_AS = 0;
    protected @Nullable ByteBuffer container;

    protected Struct(long address, @Nullable ByteBuffer container) {
        super(address);
        this.container = container;
    }

    protected abstract SELF create(long var1, @Nullable ByteBuffer var3);

    public abstract int sizeof();

    public void clear() {
        MemoryUtil.memSet(this.address(), 0, this.sizeof());
    }

    public void free() {
        MemoryUtil.nmemFree(this.address());
    }

    public boolean isNull(int memberOffset) {
        if (Checks.DEBUG) {
            this.checkMemberOffset(memberOffset);
        }
        return MemoryUtil.memGetAddress(this.address() + (long)memberOffset) == 0L;
    }

    private void checkMemberOffset(int memberOffset) {
        if (memberOffset < 0 || this.sizeof() - memberOffset < POINTER_SIZE) {
            throw new IllegalArgumentException("Invalid member offset.");
        }
    }

    protected static ByteBuffer __checkContainer(ByteBuffer container, int sizeof) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)container, sizeof);
        }
        return container;
    }

    private static long getBytes(int elements, int elementSize) {
        return ((long)elements & 0xFFFFFFFFL) * (long)elementSize;
    }

    protected static long __checkMalloc(int elements, int elementSize) {
        long bytes = ((long)elements & 0xFFFFFFFFL) * (long)elementSize;
        if (Checks.DEBUG) {
            if (elements < 0) {
                throw new IllegalArgumentException("Invalid number of elements");
            }
            if (BITS32 && 0xFFFFFFFFL < bytes) {
                throw new IllegalArgumentException("The request allocation is too large");
            }
        }
        return bytes;
    }

    protected static ByteBuffer __create(int elements, int elementSize) {
        APIUtil.apiCheckAllocation(elements, Struct.getBytes(elements, elementSize), Integer.MAX_VALUE);
        return ByteBuffer.allocateDirect(elements * elementSize).order(ByteOrder.nativeOrder());
    }

    protected static <T extends Struct<T>> @Nullable ByteBuffer __getContainer(T struct) {
        return struct.container;
    }

    protected static @Nullable ByteBuffer __getContainer(StructBuffer<?, ?> struct) {
        return struct.container;
    }

    public static void validate(long array, int count, int SIZEOF, StructValidation validation) {
        for (int i = 0; i < count; ++i) {
            validation.validate(array + Integer.toUnsignedLong(i) * (long)SIZEOF);
        }
    }

    public static void validate(long array, long count, int SIZEOF, StructValidation validation) {
        Struct.validate(array, (int)count, SIZEOF, validation);
    }

    protected static Member __padding(int num, boolean condition) {
        return Struct.__padding(num, 1, condition);
    }

    protected static Member __padding(int num, int size, boolean condition) {
        return Struct.__member(condition ? num * size : 0, size);
    }

    protected static Member __member(int size) {
        return Struct.__member(size, size);
    }

    protected static Member __member(int size, int alignment) {
        return Struct.__member(size, alignment, false);
    }

    protected static Member __member(int size, int alignment, boolean forceAlignment) {
        return new Member(size, alignment, forceAlignment);
    }

    protected static Member __array(int size, int length) {
        return Struct.__array(size, size, length);
    }

    protected static Member __array(int size, int alignment, int length) {
        return new Member(size * length, alignment, false);
    }

    protected static Member __array(int size, int alignment, boolean forceAlignment, int length) {
        return new Member(size * length, alignment, forceAlignment);
    }

    protected static Layout __union(Member ... members) {
        return Struct.__union(DEFAULT_PACK_ALIGNMENT, 0, members);
    }

    protected static Layout __union(int packAlignment, int alignas, Member ... members) {
        ArrayList<Member> union = new ArrayList<Member>(members.length);
        int size = 0;
        int alignment = alignas;
        for (Member m : members) {
            size = Math.max(size, m.size);
            alignment = Math.max(alignment, m.getAlignment(packAlignment));
            m.offset = 0;
            union.add(m);
            if (!(m instanceof Layout)) continue;
            Struct.addNestedMembers(m, union, 0);
        }
        return new Layout(size, alignment, alignas != 0, union.toArray(new Member[0]));
    }

    protected static Layout __struct(Member ... members) {
        return Struct.__struct(DEFAULT_PACK_ALIGNMENT, 0, members);
    }

    protected static Layout __struct(int packAlignment, int alignas, Member ... members) {
        ArrayList<Member> struct = new ArrayList<Member>(members.length);
        int size = 0;
        int alignment = alignas;
        for (Member m : members) {
            int memberAlignment = m.getAlignment(packAlignment);
            m.offset = Struct.align(size, memberAlignment);
            size = m.offset + m.size;
            alignment = Math.max(alignment, memberAlignment);
            struct.add(m);
            if (!(m instanceof Layout)) continue;
            Struct.addNestedMembers(m, struct, m.offset);
        }
        size = Struct.align(size, alignment);
        return new Layout(size, alignment, alignas != 0, struct.toArray(new Member[0]));
    }

    private static void addNestedMembers(Member nested, List<Member> members, int offset) {
        Layout layout = (Layout)nested;
        for (Member m : layout.members) {
            m.offset += offset;
            members.add(m);
        }
    }

    private static int align(int offset, int alignment) {
        return (offset - 1 | alignment - 1) + 1;
    }

    static {
        Library.initialize();
    }

    protected static class Layout
    extends Member {
        final Member[] members;

        Layout(int size, int alignment, boolean forceAlignment, Member[] members) {
            super(size, alignment, forceAlignment);
            this.members = members;
        }

        public int offsetof(int member) {
            return this.members[member].offset;
        }
    }

    protected static class Member {
        final int size;
        final int alignment;
        final boolean forcedAlignment;
        int offset;

        Member(int size, int alignment, boolean forcedAlignment) {
            this.size = size;
            this.alignment = alignment;
            this.forcedAlignment = forcedAlignment;
        }

        public int getSize() {
            return this.size;
        }

        public int getAlignment() {
            return this.alignment;
        }

        public int getAlignment(int packAlignment) {
            return this.forcedAlignment ? this.alignment : Math.min(this.alignment, packAlignment);
        }
    }

    @FunctionalInterface
    public static interface StructValidation {
        public void validate(long var1);
    }
}

