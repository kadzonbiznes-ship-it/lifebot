/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.StructBuffer$StructIterator
 *  org.lwjgl.system.StructBuffer$StructSpliterator
 */
package org.lwjgl.system;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Checks;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

public abstract class StructBuffer<T extends Struct<T>, SELF extends StructBuffer<T, SELF>>
extends CustomBuffer<SELF>
implements Iterable<T> {
    protected StructBuffer(ByteBuffer container, int remaining) {
        super(MemoryUtil.memAddress(container), container, -1, 0, remaining, remaining);
    }

    protected StructBuffer(long address, @Nullable ByteBuffer container, int mark, int position, int limit, int capacity) {
        super(address, container, mark, position, limit, capacity);
    }

    @Override
    public int sizeof() {
        return ((Struct)this.getElementFactory()).sizeof();
    }

    public T get() {
        T factory = this.getElementFactory();
        return (T)((Struct)factory).create(this.address + Integer.toUnsignedLong(this.nextGetIndex()) * (long)((Struct)factory).sizeof(), this.container);
    }

    public SELF get(T value) {
        int sizeof = ((Struct)this.getElementFactory()).sizeof();
        MemoryUtil.memCopy(this.address + Integer.toUnsignedLong(this.nextGetIndex()) * (long)sizeof, ((Pointer.Default)value).address(), sizeof);
        return (SELF)((StructBuffer)this.self());
    }

    @Override
    public SELF put(T value) {
        int sizeof = ((Struct)this.getElementFactory()).sizeof();
        MemoryUtil.memCopy(((Pointer.Default)value).address(), this.address + Integer.toUnsignedLong(this.nextPutIndex()) * (long)sizeof, sizeof);
        return (SELF)((StructBuffer)this.self());
    }

    public T get(int index) {
        T factory = this.getElementFactory();
        return (T)((Struct)factory).create(this.address + Integer.toUnsignedLong(StructBuffer.check(index, this.limit)) * (long)((Struct)factory).sizeof(), this.container);
    }

    public SELF get(int index, T value) {
        int sizeof = ((Struct)this.getElementFactory()).sizeof();
        MemoryUtil.memCopy(this.address + Checks.check(index, this.limit) * (long)sizeof, ((Pointer.Default)value).address(), sizeof);
        return (SELF)((StructBuffer)this.self());
    }

    public SELF put(int index, T value) {
        int sizeof = ((Struct)this.getElementFactory()).sizeof();
        MemoryUtil.memCopy(((Pointer.Default)value).address(), this.address + Checks.check(index, this.limit) * (long)sizeof, sizeof);
        return (SELF)((StructBuffer)this.self());
    }

    public SELF apply(Consumer<T> consumer) {
        consumer.accept(this.get());
        return (SELF)((StructBuffer)this.self());
    }

    public SELF apply(int index, Consumer<T> consumer) {
        consumer.accept(this.get(index));
        return (SELF)((StructBuffer)this.self());
    }

    @Override
    public Iterator<T> iterator() {
        return new StructIterator(this.address, this.container, this.getElementFactory(), this.position, this.limit);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        T factory = this.getElementFactory();
        int fence = this.limit;
        int sizeof = ((Struct)factory).sizeof();
        for (int i = this.position; i < fence; ++i) {
            action.accept(((Struct)factory).create(this.address + Integer.toUnsignedLong(i) * (long)this.sizeof(), this.container));
        }
    }

    @Override
    public Spliterator<T> spliterator() {
        return new StructSpliterator(this.address, this.container, this.getElementFactory(), this.position, this.limit);
    }

    public Stream<T> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    public Stream<T> parallelStream() {
        return StreamSupport.stream(this.spliterator(), true);
    }

    protected abstract T getElementFactory();

    private static int check(int index, int length) {
        if (Checks.CHECKS && (index < 0 || length <= index)) {
            throw new IndexOutOfBoundsException();
        }
        return index;
    }
}

