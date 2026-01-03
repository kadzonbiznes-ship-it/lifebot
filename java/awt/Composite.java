/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;

public interface Composite {
    public CompositeContext createContext(ColorModel var1, ColorModel var2, RenderingHints var3);
}

