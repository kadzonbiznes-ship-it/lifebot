/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.NativeType
 */
package org.lwjgl.glfw;

import java.nio.ByteBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType(value="struct GLFWimage")
public class GLFWImage
extends Struct<GLFWImage>
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int WIDTH;
    public static final int HEIGHT;
    public static final int PIXELS;

    protected GLFWImage(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected GLFWImage create(long address, @Nullable ByteBuffer container) {
        return new GLFWImage(address, container);
    }

    public GLFWImage(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), GLFWImage.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public int width() {
        return GLFWImage.nwidth(this.address());
    }

    public int height() {
        return GLFWImage.nheight(this.address());
    }

    @NativeType(value="unsigned char *")
    public ByteBuffer pixels(int capacity) {
        return GLFWImage.npixels(this.address(), capacity);
    }

    public GLFWImage width(int value) {
        GLFWImage.nwidth(this.address(), value);
        return this;
    }

    public GLFWImage height(int value) {
        GLFWImage.nheight(this.address(), value);
        return this;
    }

    public GLFWImage pixels(@NativeType(value="unsigned char *") ByteBuffer value) {
        GLFWImage.npixels(this.address(), value);
        return this;
    }

    public GLFWImage set(int width, int height, ByteBuffer pixels) {
        this.width(width);
        this.height(height);
        this.pixels(pixels);
        return this;
    }

    public GLFWImage set(GLFWImage src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static GLFWImage malloc() {
        return new GLFWImage(MemoryUtil.nmemAllocChecked(SIZEOF), null);
    }

    public static GLFWImage calloc() {
        return new GLFWImage(MemoryUtil.nmemCallocChecked(1L, SIZEOF), null);
    }

    public static GLFWImage create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return new GLFWImage(MemoryUtil.memAddress(container), container);
    }

    public static GLFWImage create(long address) {
        return new GLFWImage(address, null);
    }

    public static @Nullable GLFWImage createSafe(long address) {
        return address == 0L ? null : new GLFWImage(address, null);
    }

    public static Buffer malloc(int capacity) {
        return new Buffer(MemoryUtil.nmemAllocChecked(GLFWImage.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return new Buffer(MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = GLFWImage.__create(capacity, SIZEOF);
        return new Buffer(MemoryUtil.memAddress(container), container, -1, 0, capacity, capacity);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static @Nullable Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    @Deprecated
    public static GLFWImage mallocStack() {
        return GLFWImage.malloc(MemoryStack.stackGet());
    }

    @Deprecated
    public static GLFWImage callocStack() {
        return GLFWImage.calloc(MemoryStack.stackGet());
    }

    @Deprecated
    public static GLFWImage mallocStack(MemoryStack stack) {
        return GLFWImage.malloc(stack);
    }

    @Deprecated
    public static GLFWImage callocStack(MemoryStack stack) {
        return GLFWImage.calloc(stack);
    }

    @Deprecated
    public static Buffer mallocStack(int capacity) {
        return GLFWImage.malloc(capacity, MemoryStack.stackGet());
    }

    @Deprecated
    public static Buffer callocStack(int capacity) {
        return GLFWImage.calloc(capacity, MemoryStack.stackGet());
    }

    @Deprecated
    public static Buffer mallocStack(int capacity, MemoryStack stack) {
        return GLFWImage.malloc(capacity, stack);
    }

    @Deprecated
    public static Buffer callocStack(int capacity, MemoryStack stack) {
        return GLFWImage.calloc(capacity, stack);
    }

    public static GLFWImage malloc(MemoryStack stack) {
        return new GLFWImage(stack.nmalloc(ALIGNOF, SIZEOF), null);
    }

    public static GLFWImage calloc(MemoryStack stack) {
        return new GLFWImage(stack.ncalloc(ALIGNOF, 1, SIZEOF), null);
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return new Buffer(stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int nwidth(long struct) {
        return MemoryUtil.memGetInt(struct + (long)WIDTH);
    }

    public static int nheight(long struct) {
        return MemoryUtil.memGetInt(struct + (long)HEIGHT);
    }

    public static ByteBuffer npixels(long struct, int capacity) {
        return MemoryUtil.memByteBuffer(MemoryUtil.memGetAddress(struct + (long)PIXELS), capacity);
    }

    public static void nwidth(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)WIDTH, value);
    }

    public static void nheight(long struct, int value) {
        MemoryUtil.memPutInt(struct + (long)HEIGHT, value);
    }

    public static void npixels(long struct, ByteBuffer value) {
        MemoryUtil.memPutAddress(struct + (long)PIXELS, MemoryUtil.memAddress(value));
    }

    public static void validate(long struct) {
        Checks.check(MemoryUtil.memGetAddress(struct + (long)PIXELS));
    }

    static {
        Struct.Layout layout = GLFWImage.__struct(GLFWImage.__member(4), GLFWImage.__member(4), GLFWImage.__member(POINTER_SIZE));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        WIDTH = layout.offsetof(0);
        HEIGHT = layout.offsetof(1);
        PIXELS = layout.offsetof(2);
    }

    public static class Buffer
    extends StructBuffer<GLFWImage, Buffer>
    implements NativeResource {
        private static final GLFWImage ELEMENT_FACTORY = GLFWImage.create(-1L);

        public Buffer(ByteBuffer container) {
            super(container, container.remaining() / SIZEOF);
        }

        public Buffer(long address, int cap) {
            super(address, null, -1, 0, cap, cap);
        }

        Buffer(long address, @Nullable ByteBuffer container, int mark, int pos, int lim, int cap) {
            super(address, container, mark, pos, lim, cap);
        }

        @Override
        protected Buffer self() {
            return this;
        }

        @Override
        protected Buffer create(long address, @Nullable ByteBuffer container, int mark, int position, int limit, int capacity) {
            return new Buffer(address, container, mark, position, limit, capacity);
        }

        @Override
        protected GLFWImage getElementFactory() {
            return ELEMENT_FACTORY;
        }

        public int width() {
            return GLFWImage.nwidth(this.address());
        }

        public int height() {
            return GLFWImage.nheight(this.address());
        }

        @NativeType(value="unsigned char *")
        public ByteBuffer pixels(int capacity) {
            return GLFWImage.npixels(this.address(), capacity);
        }

        public Buffer width(int value) {
            GLFWImage.nwidth(this.address(), value);
            return this;
        }

        public Buffer height(int value) {
            GLFWImage.nheight(this.address(), value);
            return this;
        }

        public Buffer pixels(@NativeType(value="unsigned char *") ByteBuffer value) {
            GLFWImage.npixels(this.address(), value);
            return this;
        }
    }
}

