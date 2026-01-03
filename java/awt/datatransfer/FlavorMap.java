/*
 * Decompiled with CFR 0.152.
 */
package java.awt.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.util.Map;

public interface FlavorMap {
    public Map<DataFlavor, String> getNativesForFlavors(DataFlavor[] var1);

    public Map<String, DataFlavor> getFlavorsForNatives(String[] var1);
}

