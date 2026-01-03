/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.loops;

import java.awt.Color;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import sun.java2d.SunCompositeContext;
import sun.java2d.SurfaceData;
import sun.java2d.loops.SurfaceType;

public final class XORComposite
implements Composite {
    Color xorColor;
    int xorPixel;
    int alphaMask;

    public XORComposite(Color xorColor, SurfaceData sd) {
        this.xorColor = xorColor;
        SurfaceType sType = sd.getSurfaceType();
        this.xorPixel = sd.pixelFor(xorColor.getRGB());
        this.alphaMask = sType.getAlphaMask();
    }

    public Color getXorColor() {
        return this.xorColor;
    }

    public int getXorPixel() {
        return this.xorPixel;
    }

    public int getAlphaMask() {
        return this.alphaMask;
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return new SunCompositeContext(this, srcColorModel, dstColorModel);
    }
}

