/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.hid.uber.api;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface UberInput
extends Library {
    public int UberSendInit(int var1, int var2, Pointer var3, int var4);

    public void UberSendDestroy();

    public byte LoadUber(byte[] var1, int var2);
}

