/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import java.awt.Rectangle;
import java.awt.Shape;
import sun.java2d.SunGraphics2D;

public interface CompositePipe {
    public Object startSequence(SunGraphics2D var1, Shape var2, Rectangle var3, int[] var4);

    public boolean needTile(Object var1, int var2, int var3, int var4, int var5);

    public void renderPathTile(Object var1, byte[] var2, int var3, int var4, int var5, int var6, int var7, int var8);

    public void skipTile(Object var1, int var2, int var3);

    public void endSequence(Object var1);
}

