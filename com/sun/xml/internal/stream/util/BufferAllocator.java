/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.internal.stream.util;

public class BufferAllocator {
    private static final int SMALL_SIZE_LIMIT = 128;
    private static final int MEDIUM_SIZE_LIMIT = 2048;
    private static final int LARGE_SIZE_LIMIT = 8192;
    char[] smallCharBuffer;
    char[] mediumCharBuffer;
    char[] largeCharBuffer;
    byte[] smallByteBuffer;
    byte[] mediumByteBuffer;
    byte[] largeByteBuffer;

    public char[] getCharBuffer(int size) {
        if (size <= 128) {
            char[] buffer = this.smallCharBuffer;
            this.smallCharBuffer = null;
            return buffer;
        }
        if (size <= 2048) {
            char[] buffer = this.mediumCharBuffer;
            this.mediumCharBuffer = null;
            return buffer;
        }
        if (size <= 8192) {
            char[] buffer = this.largeCharBuffer;
            this.largeCharBuffer = null;
            return buffer;
        }
        return null;
    }

    public void returnCharBuffer(char[] c) {
        if (c == null) {
            return;
        }
        if (c.length <= 128) {
            this.smallCharBuffer = c;
        } else if (c.length <= 2048) {
            this.mediumCharBuffer = c;
        } else if (c.length <= 8192) {
            this.largeCharBuffer = c;
        }
    }

    public byte[] getByteBuffer(int size) {
        if (size <= 128) {
            byte[] buffer = this.smallByteBuffer;
            this.smallByteBuffer = null;
            return buffer;
        }
        if (size <= 2048) {
            byte[] buffer = this.mediumByteBuffer;
            this.mediumByteBuffer = null;
            return buffer;
        }
        if (size <= 8192) {
            byte[] buffer = this.largeByteBuffer;
            this.largeByteBuffer = null;
            return buffer;
        }
        return null;
    }

    public void returnByteBuffer(byte[] b) {
        if (b == null) {
            return;
        }
        if (b.length <= 128) {
            this.smallByteBuffer = b;
        } else if (b.length <= 2048) {
            this.mediumByteBuffer = b;
        } else if (b.length <= 8192) {
            this.largeByteBuffer = b;
        }
    }
}

