/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import sun.java2d.SunGraphics2D;

public interface PixelFillPipe {
    public void fillRect(SunGraphics2D var1, int var2, int var3, int var4, int var5);

    public void fillRoundRect(SunGraphics2D var1, int var2, int var3, int var4, int var5, int var6, int var7);

    public void fillOval(SunGraphics2D var1, int var2, int var3, int var4, int var5);

    public void fillArc(SunGraphics2D var1, int var2, int var3, int var4, int var5, int var6, int var7);

    public void fillPolygon(SunGraphics2D var1, int[] var2, int[] var3, int var4);
}

