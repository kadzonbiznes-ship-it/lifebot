/*
 * Decompiled with CFR 0.152.
 */
package com.sun.crypto.provider;

import javax.crypto.ShortBufferException;

interface Padding {
    public void padWithLen(byte[] var1, int var2, int var3) throws ShortBufferException;

    public int unpad(byte[] var1, int var2, int var3);

    public int padLength(int var1);
}

