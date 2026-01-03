/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.pipe;

import sun.java2d.SunGraphics2D;

public interface PixelDrawPipe {
    public void drawLine(SunGraphics2D var1, int var2, int var3, int var4, int var5);

    public void drawRect(SunGraphics2D var1, int var2, int var3, int var4, int var5);

    public void drawRoundRect(SunGraphics2D var1, int var2, int var3, int var4, int var5, int var6, int var7);

    public void drawOval(SunGraphics2D var1, int var2, int var3, int var4, int var5);

    public void drawArc(SunGraphics2D var1, int var2, int var3, int var4, int var5, int var6, int var7);

    public void drawPolyline(SunGraphics2D var1, int[] var2, int[] var3, int var4);

    public void drawPolygon(SunGraphics2D var1, int[] var2, int[] var3, int var4);
}

