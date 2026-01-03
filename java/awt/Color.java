/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.ColorPaintContext;
import java.awt.GraphicsEnvironment;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.beans.ConstructorProperties;
import java.io.Serializable;

public class Color
implements Paint,
Serializable {
    public static final Color white;
    public static final Color WHITE;
    public static final Color lightGray;
    public static final Color LIGHT_GRAY;
    public static final Color gray;
    public static final Color GRAY;
    public static final Color darkGray;
    public static final Color DARK_GRAY;
    public static final Color black;
    public static final Color BLACK;
    public static final Color red;
    public static final Color RED;
    public static final Color pink;
    public static final Color PINK;
    public static final Color orange;
    public static final Color ORANGE;
    public static final Color yellow;
    public static final Color YELLOW;
    public static final Color green;
    public static final Color GREEN;
    public static final Color magenta;
    public static final Color MAGENTA;
    public static final Color cyan;
    public static final Color CYAN;
    public static final Color blue;
    public static final Color BLUE;
    int value;
    private float[] frgbvalue = null;
    private float[] fvalue = null;
    private float falpha = 0.0f;
    private ColorSpace cs = null;
    private static final long serialVersionUID = 118526816881161077L;
    private static final double FACTOR = 0.7;

    private static native void initIDs();

    private static void testColorValueRange(int r, int g, int b, int a) {
        boolean rangeError = false;
        Object badComponentString = "";
        if (a < 0 || a > 255) {
            rangeError = true;
            badComponentString = (String)badComponentString + " Alpha";
        }
        if (r < 0 || r > 255) {
            rangeError = true;
            badComponentString = (String)badComponentString + " Red";
        }
        if (g < 0 || g > 255) {
            rangeError = true;
            badComponentString = (String)badComponentString + " Green";
        }
        if (b < 0 || b > 255) {
            rangeError = true;
            badComponentString = (String)badComponentString + " Blue";
        }
        if (rangeError) {
            throw new IllegalArgumentException("Color parameter outside of expected range:" + (String)badComponentString);
        }
    }

    private static void testColorValueRange(float r, float g, float b, float a) {
        boolean rangeError = false;
        Object badComponentString = "";
        if ((double)a < 0.0 || (double)a > 1.0) {
            rangeError = true;
            badComponentString = (String)badComponentString + " Alpha";
        }
        if ((double)r < 0.0 || (double)r > 1.0) {
            rangeError = true;
            badComponentString = (String)badComponentString + " Red";
        }
        if ((double)g < 0.0 || (double)g > 1.0) {
            rangeError = true;
            badComponentString = (String)badComponentString + " Green";
        }
        if ((double)b < 0.0 || (double)b > 1.0) {
            rangeError = true;
            badComponentString = (String)badComponentString + " Blue";
        }
        if (rangeError) {
            throw new IllegalArgumentException("Color parameter outside of expected range:" + (String)badComponentString);
        }
    }

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    @ConstructorProperties(value={"red", "green", "blue", "alpha"})
    public Color(int r, int g, int b, int a) {
        this.value = (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF) << 0;
        Color.testColorValueRange(r, g, b, a);
    }

    public Color(int rgb) {
        this.value = 0xFF000000 | rgb;
    }

    public Color(int rgba, boolean hasalpha) {
        this.value = hasalpha ? rgba : 0xFF000000 | rgba;
    }

    public Color(float r, float g, float b) {
        this((int)((double)(r * 255.0f) + 0.5), (int)((double)(g * 255.0f) + 0.5), (int)((double)(b * 255.0f) + 0.5));
        Color.testColorValueRange(r, g, b, 1.0f);
        this.frgbvalue = new float[3];
        this.frgbvalue[0] = r;
        this.frgbvalue[1] = g;
        this.frgbvalue[2] = b;
        this.falpha = 1.0f;
        this.fvalue = this.frgbvalue;
    }

    public Color(float r, float g, float b, float a) {
        this((int)((double)(r * 255.0f) + 0.5), (int)((double)(g * 255.0f) + 0.5), (int)((double)(b * 255.0f) + 0.5), (int)((double)(a * 255.0f) + 0.5));
        this.frgbvalue = new float[3];
        this.frgbvalue[0] = r;
        this.frgbvalue[1] = g;
        this.frgbvalue[2] = b;
        this.falpha = a;
        this.fvalue = this.frgbvalue;
    }

    public Color(ColorSpace cspace, float[] components, float alpha) {
        boolean rangeError = false;
        Object badComponentString = "";
        int n = cspace.getNumComponents();
        this.fvalue = new float[n];
        for (int i = 0; i < n; ++i) {
            if ((double)components[i] < 0.0 || (double)components[i] > 1.0) {
                rangeError = true;
                badComponentString = (String)badComponentString + "Component " + i + " ";
                continue;
            }
            this.fvalue[i] = components[i];
        }
        if ((double)alpha < 0.0 || (double)alpha > 1.0) {
            rangeError = true;
            badComponentString = (String)badComponentString + "Alpha";
        } else {
            this.falpha = alpha;
        }
        if (rangeError) {
            throw new IllegalArgumentException("Color parameter outside of expected range: " + (String)badComponentString);
        }
        this.frgbvalue = cspace.toRGB(this.fvalue);
        this.cs = cspace;
        this.value = ((int)(this.falpha * 255.0f) & 0xFF) << 24 | ((int)(this.frgbvalue[0] * 255.0f) & 0xFF) << 16 | ((int)(this.frgbvalue[1] * 255.0f) & 0xFF) << 8 | ((int)(this.frgbvalue[2] * 255.0f) & 0xFF) << 0;
    }

    public int getRed() {
        return this.getRGB() >> 16 & 0xFF;
    }

    public int getGreen() {
        return this.getRGB() >> 8 & 0xFF;
    }

    public int getBlue() {
        return this.getRGB() >> 0 & 0xFF;
    }

    public int getAlpha() {
        return this.getRGB() >> 24 & 0xFF;
    }

    public int getRGB() {
        return this.value;
    }

    public Color brighter() {
        int r = this.getRed();
        int g = this.getGreen();
        int b = this.getBlue();
        int alpha = this.getAlpha();
        int i = 3;
        if (r == 0 && g == 0 && b == 0) {
            return new Color(i, i, i, alpha);
        }
        if (r > 0 && r < i) {
            r = i;
        }
        if (g > 0 && g < i) {
            g = i;
        }
        if (b > 0 && b < i) {
            b = i;
        }
        return new Color(Math.min((int)((double)r / 0.7), 255), Math.min((int)((double)g / 0.7), 255), Math.min((int)((double)b / 0.7), 255), alpha);
    }

    public Color darker() {
        return new Color(Math.max((int)((double)this.getRed() * 0.7), 0), Math.max((int)((double)this.getGreen() * 0.7), 0), Math.max((int)((double)this.getBlue() * 0.7), 0), this.getAlpha());
    }

    public int hashCode() {
        return this.value;
    }

    public boolean equals(Object obj) {
        return obj instanceof Color && ((Color)obj).getRGB() == this.getRGB();
    }

    public String toString() {
        return this.getClass().getName() + "[r=" + this.getRed() + ",g=" + this.getGreen() + ",b=" + this.getBlue() + "]";
    }

    public static Color decode(String nm) throws NumberFormatException {
        Integer intval = Integer.decode(nm);
        int i = intval;
        return new Color(i >> 16 & 0xFF, i >> 8 & 0xFF, i & 0xFF);
    }

    public static Color getColor(String nm) {
        return Color.getColor(nm, null);
    }

    public static Color getColor(String nm, Color v) {
        Integer intval = Integer.getInteger(nm);
        if (intval == null) {
            return v;
        }
        int i = intval;
        return new Color(i >> 16 & 0xFF, i >> 8 & 0xFF, i & 0xFF);
    }

    public static Color getColor(String nm, int v) {
        Integer intval = Integer.getInteger(nm);
        int i = intval != null ? intval : v;
        return new Color(i >> 16 & 0xFF, i >> 8 & 0xFF, i >> 0 & 0xFF);
    }

    public static int HSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0;
        int g = 0;
        int b = 0;
        if (saturation == 0.0f) {
            g = b = (int)(brightness * 255.0f + 0.5f);
            r = b;
        } else {
            float h = (hue - (float)Math.floor(hue)) * 6.0f;
            float f = h - (float)Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - saturation * (1.0f - f));
            switch ((int)h) {
                case 0: {
                    r = (int)(brightness * 255.0f + 0.5f);
                    g = (int)(t * 255.0f + 0.5f);
                    b = (int)(p * 255.0f + 0.5f);
                    break;
                }
                case 1: {
                    r = (int)(q * 255.0f + 0.5f);
                    g = (int)(brightness * 255.0f + 0.5f);
                    b = (int)(p * 255.0f + 0.5f);
                    break;
                }
                case 2: {
                    r = (int)(p * 255.0f + 0.5f);
                    g = (int)(brightness * 255.0f + 0.5f);
                    b = (int)(t * 255.0f + 0.5f);
                    break;
                }
                case 3: {
                    r = (int)(p * 255.0f + 0.5f);
                    g = (int)(q * 255.0f + 0.5f);
                    b = (int)(brightness * 255.0f + 0.5f);
                    break;
                }
                case 4: {
                    r = (int)(t * 255.0f + 0.5f);
                    g = (int)(p * 255.0f + 0.5f);
                    b = (int)(brightness * 255.0f + 0.5f);
                    break;
                }
                case 5: {
                    r = (int)(brightness * 255.0f + 0.5f);
                    g = (int)(p * 255.0f + 0.5f);
                    b = (int)(q * 255.0f + 0.5f);
                }
            }
        }
        return 0xFF000000 | r << 16 | g << 8 | b << 0;
    }

    public static float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
        float hue;
        int cmin;
        int cmax;
        if (hsbvals == null) {
            hsbvals = new float[3];
        }
        int n = cmax = r > g ? r : g;
        if (b > cmax) {
            cmax = b;
        }
        int n2 = cmin = r < g ? r : g;
        if (b < cmin) {
            cmin = b;
        }
        float brightness = (float)cmax / 255.0f;
        float saturation = cmax != 0 ? (float)(cmax - cmin) / (float)cmax : 0.0f;
        if (saturation == 0.0f) {
            hue = 0.0f;
        } else {
            float redc = (float)(cmax - r) / (float)(cmax - cmin);
            float greenc = (float)(cmax - g) / (float)(cmax - cmin);
            float bluec = (float)(cmax - b) / (float)(cmax - cmin);
            hue = r == cmax ? bluec - greenc : (g == cmax ? 2.0f + redc - bluec : 4.0f + greenc - redc);
            if ((hue /= 6.0f) < 0.0f) {
                hue += 1.0f;
            }
        }
        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }

    public static Color getHSBColor(float h, float s, float b) {
        return new Color(Color.HSBtoRGB(h, s, b));
    }

    public float[] getRGBComponents(float[] compArray) {
        float[] f = compArray == null ? new float[4] : compArray;
        if (this.frgbvalue == null) {
            f[0] = (float)this.getRed() / 255.0f;
            f[1] = (float)this.getGreen() / 255.0f;
            f[2] = (float)this.getBlue() / 255.0f;
            f[3] = (float)this.getAlpha() / 255.0f;
        } else {
            f[0] = this.frgbvalue[0];
            f[1] = this.frgbvalue[1];
            f[2] = this.frgbvalue[2];
            f[3] = this.falpha;
        }
        return f;
    }

    public float[] getRGBColorComponents(float[] compArray) {
        float[] f = compArray == null ? new float[3] : compArray;
        if (this.frgbvalue == null) {
            f[0] = (float)this.getRed() / 255.0f;
            f[1] = (float)this.getGreen() / 255.0f;
            f[2] = (float)this.getBlue() / 255.0f;
        } else {
            f[0] = this.frgbvalue[0];
            f[1] = this.frgbvalue[1];
            f[2] = this.frgbvalue[2];
        }
        return f;
    }

    public float[] getComponents(float[] compArray) {
        if (this.fvalue == null) {
            return this.getRGBComponents(compArray);
        }
        int n = this.fvalue.length;
        float[] f = compArray == null ? new float[n + 1] : compArray;
        for (int i = 0; i < n; ++i) {
            f[i] = this.fvalue[i];
        }
        f[n] = this.falpha;
        return f;
    }

    public float[] getColorComponents(float[] compArray) {
        if (this.fvalue == null) {
            return this.getRGBColorComponents(compArray);
        }
        int n = this.fvalue.length;
        float[] f = compArray == null ? new float[n] : compArray;
        for (int i = 0; i < n; ++i) {
            f[i] = this.fvalue[i];
        }
        return f;
    }

    public float[] getComponents(ColorSpace cspace, float[] compArray) {
        if (this.cs == null) {
            this.cs = ColorSpace.getInstance(1000);
        }
        float[] f = this.fvalue == null ? new float[]{(float)this.getRed() / 255.0f, (float)this.getGreen() / 255.0f, (float)this.getBlue() / 255.0f} : this.fvalue;
        float[] tmp = this.cs.toCIEXYZ(f);
        float[] tmpout = cspace.fromCIEXYZ(tmp);
        if (compArray == null) {
            compArray = new float[tmpout.length + 1];
        }
        for (int i = 0; i < tmpout.length; ++i) {
            compArray[i] = tmpout[i];
        }
        compArray[tmpout.length] = this.fvalue == null ? (float)this.getAlpha() / 255.0f : this.falpha;
        return compArray;
    }

    public float[] getColorComponents(ColorSpace cspace, float[] compArray) {
        if (this.cs == null) {
            this.cs = ColorSpace.getInstance(1000);
        }
        float[] f = this.fvalue == null ? new float[]{(float)this.getRed() / 255.0f, (float)this.getGreen() / 255.0f, (float)this.getBlue() / 255.0f} : this.fvalue;
        float[] tmp = this.cs.toCIEXYZ(f);
        float[] tmpout = cspace.fromCIEXYZ(tmp);
        if (compArray == null) {
            return tmpout;
        }
        for (int i = 0; i < tmpout.length; ++i) {
            compArray[i] = tmpout[i];
        }
        return compArray;
    }

    public ColorSpace getColorSpace() {
        if (this.cs == null) {
            this.cs = ColorSpace.getInstance(1000);
        }
        return this.cs;
    }

    @Override
    public synchronized PaintContext createContext(ColorModel cm, Rectangle r, Rectangle2D r2d, AffineTransform xform, RenderingHints hints) {
        return new ColorPaintContext(this.getRGB(), cm);
    }

    @Override
    public int getTransparency() {
        int alpha = this.getAlpha();
        if (alpha == 255) {
            return 1;
        }
        if (alpha == 0) {
            return 2;
        }
        return 3;
    }

    static {
        WHITE = white = new Color(255, 255, 255);
        LIGHT_GRAY = lightGray = new Color(192, 192, 192);
        GRAY = gray = new Color(128, 128, 128);
        DARK_GRAY = darkGray = new Color(64, 64, 64);
        BLACK = black = new Color(0, 0, 0);
        RED = red = new Color(255, 0, 0);
        PINK = pink = new Color(255, 175, 175);
        ORANGE = orange = new Color(255, 200, 0);
        YELLOW = yellow = new Color(255, 255, 0);
        GREEN = green = new Color(0, 255, 0);
        MAGENTA = magenta = new Color(255, 0, 255);
        CYAN = cyan = new Color(0, 255, 255);
        BLUE = blue = new Color(0, 0, 255);
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            Color.initIDs();
        }
    }
}

