/*
 * Decompiled with CFR 0.152.
 */
package java.awt.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorMap;
import java.util.List;

public interface FlavorTable
extends FlavorMap {
    public List<String> getNativesForFlavor(DataFlavor var1);

    public List<DataFlavor> getFlavorsForNative(String var1);
}

