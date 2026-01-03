/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package org.lwjgl.system;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;

public abstract class CustomBuffer<SELF extends CustomBuffer<SELF>>
extends Pointer.Default {
    protected @Nullable ByteBuffer container;
    protected int mark;
    protected int position;
    protected int limit;
    protected int capacity;

    protected CustomBuffer(long address, @Nullable ByteBuffer container, int mark, int position, int limit, int capacity) {
        super(address);
        this.container = container;
        this.mark = mark;
        this.position = position;
        this.limit = limit;
        this.capacity = capacity;
    }

    public abstract int sizeof();

    public long address0() {
        return this.address;
    }

    @Override
    public long address() {
        return this.address + Integer.toUnsignedLong(this.position) * (long)this.sizeof();
    }

    public long address(int position) {
        return this.address + Integer.toUnsignedLong(position) * (long)this.sizeof();
    }

    public void free() {
        MemoryUtil.nmemFree(this.address);
    }

    public int capacity() {
        return this.capacity;
    }

    public int position() {
        return this.position;
    }

    public SELF position(int position) {
        if (position < 0 || this.limit < position) {
            throw new IllegalArgumentException();
        }
        this.position = position;
        if (position < this.mark) {
            this.mark = -1;
        }
        return this.self();
    }

    public int limit() {
        return this.limit;
    }

    public SELF limit(int limit) {
        if (limit < 0 || this.capacity < limit) {
            throw new IllegalArgumentException();
        }
        this.limit = limit;
        if (limit < this.position) {
            this.position = limit;
        }
        if (limit < this.mark) {
            this.mark = -1;
        }
        return this.self();
    }

    public SELF mark() {
        this.mark = this.position;
        return this.self();
    }

    public SELF reset() {
        int m = this.mark;
        if (m < 0) {
            throw new InvalidMarkException();
        }
        this.position = m;
        return this.self();
    }

    public SELF clear() {
        this.position = 0;
        this.limit = this.capacity;
        this.mark = -1;
        return this.self();
    }

    public SELF flip() {
        this.limit = this.position;
        this.position = 0;
        this.mark = -1;
        return this.self();
    }

    public SELF rewind() {
        this.position = 0;
        this.mark = -1;
        return this.self();
    }

    public int remaining() {
        return this.limit - this.position;
    }

    public boolean hasRemaining() {
        return this.position < this.limit;
    }

    public SELF slice() {
        return this.create(this.address + Integer.toUnsignedLong(this.position) * (long)this.sizeof(), this.container, -1, 0, this.remaining(), this.remaining());
    }

    public SELF slice(int offset, int capacity) {
        int position = this.position + offset;
        if (offset < 0 || this.limit < offset) {
            throw new IllegalArgumentException();
        }
        if (capacity < 0 || this.capacity - position < capacity) {
            throw new IllegalArgumentException();
        }
        return this.create(this.address + Integer.toUnsignedLong(position) * (long)this.sizeof(), this.container, -1, 0, capacity, capacity);
    }

    public SELF duplicate() {
        return this.create(this.address, this.container, this.mark, this.position, this.limit, this.capacity);
    }

    public SELF put(SELF src) {
        if (src == this) {
            throw new IllegalArgumentException();
        }
        int n = ((CustomBuffer)src).remaining();
        if (this.remaining() < n) {
            throw new BufferOverflowException();
        }
        MemoryUtil.memCopy(((CustomBuffer)src).address(), this.address(), Integer.toUnsignedLong(n) * (long)this.sizeof());
        this.position += n;
        return this.self();
    }

    public SELF compact() {
        MemoryUtil.memCopy(this.address(), this.address, Integer.toUnsignedLong(this.remaining()) * (long)this.sizeof());
        this.position(this.remaining());
        this.limit(this.capacity());
        this.mark = -1;
        return this.self();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[pos=" + this.position() + " lim=" + this.limit() + " cap=" + this.capacity() + "]";
    }

    protected abstract SELF self();

    protected abstract SELF create(long var1, @Nullable ByteBuffer var3, int var4, int var5, int var6, int var7);

    protected final int nextGetIndex() {
        if (this.position < this.limit) {
            return this.position++;
        }
        throw new BufferUnderflowException();
    }

    protected final int nextPutIndex() {
        if (this.position < this.limit) {
            return this.position++;
        }
        throw new BufferOverflowException();
    }
}

