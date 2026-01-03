/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import java.nio.ByteBuffer;

public interface GCM {
    public int update(byte[] var1, int var2, int var3, byte[] var4, int var5);

    public int update(byte[] var1, int var2, int var3, ByteBuffer var4);

    public int update(ByteBuffer var1, ByteBuffer var2);

    public int doFinal(byte[] var1, int var2, int var3, byte[] var4, int var5);

    public int doFinal(ByteBuffer var1, ByteBuffer var2);
}

