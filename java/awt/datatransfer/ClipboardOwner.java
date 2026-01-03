/*
 * Decompiled with CFR 0.152.
 */
package java.awt.datatransfer;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;

public interface ClipboardOwner {
    public void lostOwnership(Clipboard var1, Transferable var2);
}

