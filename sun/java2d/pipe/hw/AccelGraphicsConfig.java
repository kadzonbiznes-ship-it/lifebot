/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe.hw;

import java.awt.image.VolatileImage;
import sun.java2d.pipe.hw.BufferedContextProvider;
import sun.java2d.pipe.hw.ContextCapabilities;

public interface AccelGraphicsConfig
extends BufferedContextProvider {
    public VolatileImage createCompatibleVolatileImage(int var1, int var2, int var3, int var4);

    public ContextCapabilities getContextCapabilities();
}

