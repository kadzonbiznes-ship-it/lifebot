/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.cmm;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public interface ColorTransform {
    public static final int Any = -1;

    public int getNumInComponents();

    public int getNumOutComponents();

    public void colorConvert(BufferedImage var1, BufferedImage var2);

    public void colorConvert(Raster var1, WritableRaster var2, float[] var3, float[] var4, float[] var5, float[] var6);

    public void colorConvert(Raster var1, WritableRaster var2);

    public short[] colorConvert(short[] var1, short[] var2);

    public byte[] colorConvert(byte[] var1, byte[] var2);
}

