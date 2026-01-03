/*
 * Decompiled with CFR 0.152.
 */
package sun.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorMap;
import java.util.LinkedHashSet;
import java.util.function.Supplier;

public interface DesktopDatatransferService {
    public void invokeOnEventThread(Runnable var1);

    public String getDefaultUnicodeEncoding();

    public FlavorMap getFlavorMap(Supplier<FlavorMap> var1);

    public boolean isDesktopPresent();

    public LinkedHashSet<DataFlavor> getPlatformMappingsForNative(String var1);

    public LinkedHashSet<String> getPlatformMappingsForFlavor(DataFlavor var1);

    public void registerTextFlavorProperties(String var1, String var2, String var3, String var4);
}

