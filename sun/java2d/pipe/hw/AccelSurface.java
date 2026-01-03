/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe.hw;

import java.awt.Rectangle;
import sun.java2d.Surface;
import sun.java2d.pipe.hw.BufferedContextProvider;

public interface AccelSurface
extends BufferedContextProvider,
Surface {
    public static final int UNDEFINED = 0;
    public static final int WINDOW = 1;
    public static final int RT_PLAIN = 2;
    public static final int TEXTURE = 3;
    public static final int FLIP_BACKBUFFER = 4;
    public static final int RT_TEXTURE = 5;

    public int getType();

    public long getNativeOps();

    public long getNativeResource(int var1);

    public void markDirty();

    public boolean isValid();

    public boolean isSurfaceLost();

    public Rectangle getBounds();

    public Rectangle getNativeBounds();
}

