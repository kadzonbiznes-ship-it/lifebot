/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.glfw.GLFWVidMode$Buffer
 *  org.lwjgl.system.NativeType
 */
package org.lwjgl.glfw;

import java.nio.ByteBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;

@NativeType(value="struct GLFWvidmode")
public class GLFWVidMode
extends Struct<GLFWVidMode> {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int WIDTH;
    public static final int HEIGHT;
    public static final int REDBITS;
    public static final int GREENBITS;
    public static final int BLUEBITS;
    public static final int REFRESHRATE;

    protected GLFWVidMode(long address, @Nullable ByteBuffer container) {
        super(address, container);
    }

    @Override
    protected GLFWVidMode create(long address, @Nullable ByteBuffer container) {
        return new GLFWVidMode(address, container);
    }

    public GLFWVidMode(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), GLFWVidMode.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public int width() {
        return GLFWVidMode.nwidth(this.address());
    }

    public int height() {
        return GLFWVidMode.nheight(this.address());
    }

    public int redBits() {
        return GLFWVidMode.nredBits(this.address());
    }

    public int greenBits() {
        return GLFWVidMode.ngreenBits(this.address());
    }

    public int blueBits() {
        return GLFWVidMode.nblueBits(this.address());
    }

    public int refreshRate() {
        return GLFWVidMode.nrefreshRate(this.address());
    }

    public static GLFWVidMode create(long address) {
        return new GLFWVidMode(address, null);
    }

    public static @Nullable GLFWVidMode createSafe(long address) {
        return address == 0L ? null : new GLFWVidMode(address, null);
    }

    public static Buffer create(long address, int capacity) {
        return new Buffer(address, capacity);
    }

    public static // Could not load outer class - annotation placement on inner may be incorrect
    @Nullable GLFWVidMode.Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : new Buffer(address, capacity);
    }

    public static int nwidth(long struct) {
        return MemoryUtil.memGetInt(struct + (long)WIDTH);
    }

    public static int nheight(long struct) {
        return MemoryUtil.memGetInt(struct + (long)HEIGHT);
    }

    public static int nredBits(long struct) {
        return MemoryUtil.memGetInt(struct + (long)REDBITS);
    }

    public static int ngreenBits(long struct) {
        return MemoryUtil.memGetInt(struct + (long)GREENBITS);
    }

    public static int nblueBits(long struct) {
        return MemoryUtil.memGetInt(struct + (long)BLUEBITS);
    }

    public static int nrefreshRate(long struct) {
        return MemoryUtil.memGetInt(struct + (long)REFRESHRATE);
    }

    static {
        Struct.Layout layout = GLFWVidMode.__struct(GLFWVidMode.__member(4), GLFWVidMode.__member(4), GLFWVidMode.__member(4), GLFWVidMode.__member(4), GLFWVidMode.__member(4), GLFWVidMode.__member(4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        WIDTH = layout.offsetof(0);
        HEIGHT = layout.offsetof(1);
        REDBITS = layout.offsetof(2);
        GREENBITS = layout.offsetof(3);
        BLUEBITS = layout.offsetof(4);
        REFRESHRATE = layout.offsetof(5);
    }
}

