/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.image.ColorModel;
import java.util.Hashtable;

public interface ImageConsumer {
    public static final int RANDOMPIXELORDER = 1;
    public static final int TOPDOWNLEFTRIGHT = 2;
    public static final int COMPLETESCANLINES = 4;
    public static final int SINGLEPASS = 8;
    public static final int SINGLEFRAME = 16;
    public static final int IMAGEERROR = 1;
    public static final int SINGLEFRAMEDONE = 2;
    public static final int STATICIMAGEDONE = 3;
    public static final int IMAGEABORTED = 4;

    public void setDimensions(int var1, int var2);

    public void setProperties(Hashtable<?, ?> var1);

    public void setColorModel(ColorModel var1);

    public void setHints(int var1);

    public void setPixels(int var1, int var2, int var3, int var4, ColorModel var5, byte[] var6, int var7, int var8);

    public void setPixels(int var1, int var2, int var3, int var4, ColorModel var5, int[] var6, int var7, int var8);

    public void imageComplete(int var1);
}

