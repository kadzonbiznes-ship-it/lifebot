/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import sun.java2d.cmm.CMSManager;
import sun.java2d.cmm.ColorTransform;
import sun.java2d.cmm.PCMM;

public abstract class ColorModel
implements Transparency {
    protected int pixel_bits;
    int[] nBits;
    int transparency = 3;
    boolean supportsAlpha = true;
    boolean isAlphaPremultiplied = false;
    int numComponents = -1;
    int numColorComponents = -1;
    ColorSpace colorSpace = ColorSpace.getInstance(1000);
    int colorSpaceType = 5;
    int maxBits;
    boolean is_sRGB = true;
    protected int transferType;
    private static boolean loaded = false;
    static byte[] l8Tos8;
    static byte[] s8Tol8;
    static byte[] l16Tos8;
    static short[] s8Tol16;
    static Map<ICC_ColorSpace, byte[]> g8Tos8Map;
    static Map<ICC_ColorSpace, byte[]> lg16Toog8Map;
    static Map<ICC_ColorSpace, byte[]> g16Tos8Map;
    static Map<ICC_ColorSpace, short[]> lg16Toog16Map;

    static void loadLibraries() {
        if (!loaded) {
            AccessController.doPrivileged(new PrivilegedAction<Void>(){

                @Override
                public Void run() {
                    System.loadLibrary("awt");
                    return null;
                }
            });
            loaded = true;
        }
    }

    private static native void initIDs();

    public static ColorModel getRGBdefault() {
        static interface RGBdefault {
            public static final ColorModel INSTANCE = new DirectColorModel(32, 0xFF0000, 65280, 255, -16777216);
        }
        return RGBdefault.INSTANCE;
    }

    public ColorModel(int bits) {
        this.pixel_bits = bits;
        if (bits < 1) {
            throw new IllegalArgumentException("Number of bits must be > 0");
        }
        this.numComponents = 4;
        this.numColorComponents = 3;
        this.maxBits = bits;
        this.transferType = ColorModel.getDefaultTransferType(bits);
    }

    protected ColorModel(int pixel_bits, int[] bits, ColorSpace cspace, boolean hasAlpha, boolean isAlphaPremultiplied, int transparency, int transferType) {
        this.colorSpace = cspace;
        this.colorSpaceType = cspace.getType();
        this.numColorComponents = cspace.getNumComponents();
        this.numComponents = this.numColorComponents + (hasAlpha ? 1 : 0);
        this.supportsAlpha = hasAlpha;
        if (bits.length < this.numComponents) {
            throw new IllegalArgumentException("Number of color/alpha components should be " + this.numComponents + " but length of bits array is " + bits.length);
        }
        if (transparency < 1 || transparency > 3) {
            throw new IllegalArgumentException("Unknown transparency: " + transparency);
        }
        if (!this.supportsAlpha) {
            this.isAlphaPremultiplied = false;
            this.transparency = 1;
        } else {
            this.isAlphaPremultiplied = isAlphaPremultiplied;
            this.transparency = transparency;
        }
        this.nBits = Arrays.copyOf(bits, this.numComponents);
        this.pixel_bits = pixel_bits;
        if (pixel_bits <= 0) {
            throw new IllegalArgumentException("Number of pixel bits must be > 0");
        }
        this.maxBits = 0;
        for (int i = 0; i < bits.length; ++i) {
            if (bits[i] < 0) {
                throw new IllegalArgumentException("Number of bits must be >= 0");
            }
            if (this.maxBits >= bits[i]) continue;
            this.maxBits = bits[i];
        }
        if (this.maxBits == 0) {
            throw new IllegalArgumentException("There must be at least one component with > 0 pixel bits.");
        }
        if (cspace != ColorSpace.getInstance(1000)) {
            this.is_sRGB = false;
        }
        this.transferType = transferType;
    }

    public final boolean hasAlpha() {
        return this.supportsAlpha;
    }

    public final boolean isAlphaPremultiplied() {
        return this.isAlphaPremultiplied;
    }

    public final int getTransferType() {
        return this.transferType;
    }

    public int getPixelSize() {
        return this.pixel_bits;
    }

    public int getComponentSize(int componentIdx) {
        if (this.nBits == null) {
            throw new NullPointerException("Number of bits array is null.");
        }
        return this.nBits[componentIdx];
    }

    public int[] getComponentSize() {
        if (this.nBits != null) {
            return (int[])this.nBits.clone();
        }
        return null;
    }

    @Override
    public int getTransparency() {
        return this.transparency;
    }

    public int getNumComponents() {
        return this.numComponents;
    }

    public int getNumColorComponents() {
        return this.numColorComponents;
    }

    public abstract int getRed(int var1);

    public abstract int getGreen(int var1);

    public abstract int getBlue(int var1);

    public abstract int getAlpha(int var1);

    public int getRGB(int pixel) {
        return this.getAlpha(pixel) << 24 | this.getRed(pixel) << 16 | this.getGreen(pixel) << 8 | this.getBlue(pixel) << 0;
    }

    public int getRed(Object inData) {
        int pixel = 0;
        int length = 0;
        switch (this.transferType) {
            case 0: {
                byte[] bdata = (byte[])inData;
                pixel = bdata[0] & 0xFF;
                length = bdata.length;
                break;
            }
            case 1: {
                short[] sdata = (short[])inData;
                pixel = sdata[0] & 0xFFFF;
                length = sdata.length;
                break;
            }
            case 3: {
                int[] idata = (int[])inData;
                pixel = idata[0];
                length = idata.length;
                break;
            }
            default: {
                throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
            }
        }
        if (length == 1) {
            return this.getRed(pixel);
        }
        throw new UnsupportedOperationException("This method is not supported by this color model");
    }

    public int getGreen(Object inData) {
        int pixel = 0;
        int length = 0;
        switch (this.transferType) {
            case 0: {
                byte[] bdata = (byte[])inData;
                pixel = bdata[0] & 0xFF;
                length = bdata.length;
                break;
            }
            case 1: {
                short[] sdata = (short[])inData;
                pixel = sdata[0] & 0xFFFF;
                length = sdata.length;
                break;
            }
            case 3: {
                int[] idata = (int[])inData;
                pixel = idata[0];
                length = idata.length;
                break;
            }
            default: {
                throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
            }
        }
        if (length == 1) {
            return this.getGreen(pixel);
        }
        throw new UnsupportedOperationException("This method is not supported by this color model");
    }

    public int getBlue(Object inData) {
        int pixel = 0;
        int length = 0;
        switch (this.transferType) {
            case 0: {
                byte[] bdata = (byte[])inData;
                pixel = bdata[0] & 0xFF;
                length = bdata.length;
                break;
            }
            case 1: {
                short[] sdata = (short[])inData;
                pixel = sdata[0] & 0xFFFF;
                length = sdata.length;
                break;
            }
            case 3: {
                int[] idata = (int[])inData;
                pixel = idata[0];
                length = idata.length;
                break;
            }
            default: {
                throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
            }
        }
        if (length == 1) {
            return this.getBlue(pixel);
        }
        throw new UnsupportedOperationException("This method is not supported by this color model");
    }

    public int getAlpha(Object inData) {
        int pixel = 0;
        int length = 0;
        switch (this.transferType) {
            case 0: {
                byte[] bdata = (byte[])inData;
                pixel = bdata[0] & 0xFF;
                length = bdata.length;
                break;
            }
            case 1: {
                short[] sdata = (short[])inData;
                pixel = sdata[0] & 0xFFFF;
                length = sdata.length;
                break;
            }
            case 3: {
                int[] idata = (int[])inData;
                pixel = idata[0];
                length = idata.length;
                break;
            }
            default: {
                throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
            }
        }
        if (length == 1) {
            return this.getAlpha(pixel);
        }
        throw new UnsupportedOperationException("This method is not supported by this color model");
    }

    public int getRGB(Object inData) {
        return this.getAlpha(inData) << 24 | this.getRed(inData) << 16 | this.getGreen(inData) << 8 | this.getBlue(inData) << 0;
    }

    public Object getDataElements(int rgb, Object pixel) {
        throw new UnsupportedOperationException("This method is not supported by this color model.");
    }

    public int[] getComponents(int pixel, int[] components, int offset) {
        throw new UnsupportedOperationException("This method is not supported by this color model.");
    }

    public int[] getComponents(Object pixel, int[] components, int offset) {
        throw new UnsupportedOperationException("This method is not supported by this color model.");
    }

    public int[] getUnnormalizedComponents(float[] normComponents, int normOffset, int[] components, int offset) {
        if (this.colorSpace == null) {
            throw new UnsupportedOperationException("This method is not supported by this color model.");
        }
        if (this.nBits == null) {
            throw new UnsupportedOperationException("This method is not supported.  Unable to determine #bits per component.");
        }
        if (normComponents.length - normOffset < this.numComponents) {
            throw new IllegalArgumentException("Incorrect number of components.  Expecting " + this.numComponents);
        }
        if (components == null) {
            components = new int[offset + this.numComponents];
        }
        if (this.supportsAlpha && this.isAlphaPremultiplied) {
            float normAlpha = normComponents[normOffset + this.numColorComponents];
            for (int i = 0; i < this.numColorComponents; ++i) {
                components[offset + i] = (int)(normComponents[normOffset + i] * (float)((1 << this.nBits[i]) - 1) * normAlpha + 0.5f);
            }
            components[offset + this.numColorComponents] = (int)(normAlpha * (float)((1 << this.nBits[this.numColorComponents]) - 1) + 0.5f);
        } else {
            for (int i = 0; i < this.numComponents; ++i) {
                components[offset + i] = (int)(normComponents[normOffset + i] * (float)((1 << this.nBits[i]) - 1) + 0.5f);
            }
        }
        return components;
    }

    public float[] getNormalizedComponents(int[] components, int offset, float[] normComponents, int normOffset) {
        if (this.colorSpace == null) {
            throw new UnsupportedOperationException("This method is not supported by this color model.");
        }
        if (this.nBits == null) {
            throw new UnsupportedOperationException("This method is not supported.  Unable to determine #bits per component.");
        }
        if (components.length - offset < this.numComponents) {
            throw new IllegalArgumentException("Incorrect number of components.  Expecting " + this.numComponents);
        }
        if (normComponents == null) {
            normComponents = new float[this.numComponents + normOffset];
        }
        if (this.supportsAlpha && this.isAlphaPremultiplied) {
            float normAlpha = components[offset + this.numColorComponents];
            if ((normAlpha /= (float)((1 << this.nBits[this.numColorComponents]) - 1)) != 0.0f) {
                for (int i = 0; i < this.numColorComponents; ++i) {
                    normComponents[normOffset + i] = (float)components[offset + i] / (normAlpha * (float)((1 << this.nBits[i]) - 1));
                }
            } else {
                for (int i = 0; i < this.numColorComponents; ++i) {
                    normComponents[normOffset + i] = 0.0f;
                }
            }
            normComponents[normOffset + this.numColorComponents] = normAlpha;
        } else {
            for (int i = 0; i < this.numComponents; ++i) {
                normComponents[normOffset + i] = (float)components[offset + i] / (float)((1 << this.nBits[i]) - 1);
            }
        }
        return normComponents;
    }

    public int getDataElement(int[] components, int offset) {
        throw new UnsupportedOperationException("This method is not supported by this color model.");
    }

    public Object getDataElements(int[] components, int offset, Object obj) {
        throw new UnsupportedOperationException("This method has not been implemented for this color model.");
    }

    public int getDataElement(float[] normComponents, int normOffset) {
        int[] components = this.getUnnormalizedComponents(normComponents, normOffset, null, 0);
        return this.getDataElement(components, 0);
    }

    public Object getDataElements(float[] normComponents, int normOffset, Object obj) {
        int[] components = this.getUnnormalizedComponents(normComponents, normOffset, null, 0);
        return this.getDataElements(components, 0, obj);
    }

    public float[] getNormalizedComponents(Object pixel, float[] normComponents, int normOffset) {
        int[] components = this.getComponents(pixel, null, 0);
        return this.getNormalizedComponents(components, 0, normComponents, normOffset);
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public final ColorSpace getColorSpace() {
        return this.colorSpace;
    }

    public ColorModel coerceData(WritableRaster raster, boolean isAlphaPremultiplied) {
        throw new UnsupportedOperationException("This method is not supported by this color model");
    }

    public boolean isCompatibleRaster(Raster raster) {
        throw new UnsupportedOperationException("This method has not been implemented for this ColorModel.");
    }

    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        throw new UnsupportedOperationException("This method is not supported by this color model");
    }

    public SampleModel createCompatibleSampleModel(int w, int h) {
        throw new UnsupportedOperationException("This method is not supported by this color model");
    }

    public boolean isCompatibleSampleModel(SampleModel sm) {
        throw new UnsupportedOperationException("This method is not supported by this color model");
    }

    public WritableRaster getAlphaRaster(WritableRaster raster) {
        return null;
    }

    public String toString() {
        return "ColorModel: #pixelBits = " + this.pixel_bits + " numComponents = " + this.numComponents + " color space = " + String.valueOf(this.colorSpace) + " transparency = " + this.transparency + " has alpha = " + this.supportsAlpha + " isAlphaPre = " + this.isAlphaPremultiplied;
    }

    static int getDefaultTransferType(int pixel_bits) {
        if (pixel_bits <= 8) {
            return 0;
        }
        if (pixel_bits <= 16) {
            return 1;
        }
        if (pixel_bits <= 32) {
            return 3;
        }
        return 32;
    }

    static boolean isLinearRGBspace(ColorSpace cs) {
        return cs == ColorSpace.getInstance(1004);
    }

    static boolean isLinearGRAYspace(ColorSpace cs) {
        return cs == ColorSpace.getInstance(1003);
    }

    static byte[] getLinearRGB8TosRGB8LUT() {
        if (l8Tos8 == null) {
            l8Tos8 = new byte[256];
            for (int i = 0; i <= 255; ++i) {
                float input = (float)i / 255.0f;
                float output = input <= 0.0031308f ? input * 12.92f : 1.055f * (float)Math.pow(input, 0.4166666666666667) - 0.055f;
                ColorModel.l8Tos8[i] = (byte)Math.round(output * 255.0f);
            }
        }
        return l8Tos8;
    }

    static byte[] getsRGB8ToLinearRGB8LUT() {
        if (s8Tol8 == null) {
            s8Tol8 = new byte[256];
            for (int i = 0; i <= 255; ++i) {
                float input = (float)i / 255.0f;
                float output = input <= 0.04045f ? input / 12.92f : (float)Math.pow((input + 0.055f) / 1.055f, 2.4);
                ColorModel.s8Tol8[i] = (byte)Math.round(output * 255.0f);
            }
        }
        return s8Tol8;
    }

    static byte[] getLinearRGB16TosRGB8LUT() {
        if (l16Tos8 == null) {
            l16Tos8 = new byte[65536];
            for (int i = 0; i <= 65535; ++i) {
                float input = (float)i / 65535.0f;
                float output = input <= 0.0031308f ? input * 12.92f : 1.055f * (float)Math.pow(input, 0.4166666666666667) - 0.055f;
                ColorModel.l16Tos8[i] = (byte)Math.round(output * 255.0f);
            }
        }
        return l16Tos8;
    }

    static short[] getsRGB8ToLinearRGB16LUT() {
        if (s8Tol16 == null) {
            s8Tol16 = new short[256];
            for (int i = 0; i <= 255; ++i) {
                float input = (float)i / 255.0f;
                float output = input <= 0.04045f ? input / 12.92f : (float)Math.pow((input + 0.055f) / 1.055f, 2.4);
                ColorModel.s8Tol16[i] = (short)Math.round(output * 65535.0f);
            }
        }
        return s8Tol16;
    }

    static byte[] getGray8TosRGB8LUT(ICC_ColorSpace grayCS) {
        byte[] g8Tos8LUT;
        if (ColorModel.isLinearGRAYspace(grayCS)) {
            return ColorModel.getLinearRGB8TosRGB8LUT();
        }
        if (g8Tos8Map != null && (g8Tos8LUT = g8Tos8Map.get(grayCS)) != null) {
            return g8Tos8LUT;
        }
        g8Tos8LUT = new byte[256];
        for (int i = 0; i <= 255; ++i) {
            g8Tos8LUT[i] = (byte)i;
        }
        ICC_Profile srgb = ICC_Profile.getInstance(1000);
        PCMM mdl = CMSManager.getModule();
        ColorTransform t = mdl.createTransform(-1, grayCS.getProfile(), srgb);
        byte[] tmp = t.colorConvert(g8Tos8LUT, (byte[])null);
        int i = 0;
        int j = 2;
        while (i <= 255) {
            g8Tos8LUT[i] = tmp[j];
            ++i;
            j += 3;
        }
        if (g8Tos8Map == null) {
            g8Tos8Map = Collections.synchronizedMap(new WeakHashMap(2));
        }
        g8Tos8Map.put(grayCS, g8Tos8LUT);
        return g8Tos8LUT;
    }

    static byte[] getLinearGray16ToOtherGray8LUT(ICC_ColorSpace grayCS) {
        byte[] lg16Toog8LUT;
        if (lg16Toog8Map != null && (lg16Toog8LUT = lg16Toog8Map.get(grayCS)) != null) {
            return lg16Toog8LUT;
        }
        short[] tmp = new short[65536];
        for (int i = 0; i <= 65535; ++i) {
            tmp[i] = (short)i;
        }
        ICC_Profile lg = ICC_Profile.getInstance(1003);
        PCMM mdl = CMSManager.getModule();
        ColorTransform t = mdl.createTransform(-1, lg, grayCS.getProfile());
        tmp = t.colorConvert(tmp, null);
        byte[] lg16Toog8LUT2 = new byte[65536];
        for (int i = 0; i <= 65535; ++i) {
            lg16Toog8LUT2[i] = (byte)((float)(tmp[i] & 0xFFFF) * 0.0038910506f + 0.5f);
        }
        if (lg16Toog8Map == null) {
            lg16Toog8Map = Collections.synchronizedMap(new WeakHashMap(2));
        }
        lg16Toog8Map.put(grayCS, lg16Toog8LUT2);
        return lg16Toog8LUT2;
    }

    static byte[] getGray16TosRGB8LUT(ICC_ColorSpace grayCS) {
        byte[] g16Tos8LUT;
        if (ColorModel.isLinearGRAYspace(grayCS)) {
            return ColorModel.getLinearRGB16TosRGB8LUT();
        }
        if (g16Tos8Map != null && (g16Tos8LUT = g16Tos8Map.get(grayCS)) != null) {
            return g16Tos8LUT;
        }
        short[] tmp = new short[65536];
        for (int i = 0; i <= 65535; ++i) {
            tmp[i] = (short)i;
        }
        ICC_Profile srgb = ICC_Profile.getInstance(1000);
        PCMM mdl = CMSManager.getModule();
        ColorTransform t = mdl.createTransform(-1, grayCS.getProfile(), srgb);
        tmp = t.colorConvert(tmp, null);
        byte[] g16Tos8LUT2 = new byte[65536];
        int i = 0;
        int j = 2;
        while (i <= 65535) {
            g16Tos8LUT2[i] = (byte)((float)(tmp[j] & 0xFFFF) * 0.0038910506f + 0.5f);
            ++i;
            j += 3;
        }
        if (g16Tos8Map == null) {
            g16Tos8Map = Collections.synchronizedMap(new WeakHashMap(2));
        }
        g16Tos8Map.put(grayCS, g16Tos8LUT2);
        return g16Tos8LUT2;
    }

    static short[] getLinearGray16ToOtherGray16LUT(ICC_ColorSpace grayCS) {
        short[] lg16Toog16LUT;
        if (lg16Toog16Map != null && (lg16Toog16LUT = lg16Toog16Map.get(grayCS)) != null) {
            return lg16Toog16LUT;
        }
        short[] tmp = new short[65536];
        for (int i = 0; i <= 65535; ++i) {
            tmp[i] = (short)i;
        }
        ICC_Profile lg = ICC_Profile.getInstance(1003);
        PCMM mdl = CMSManager.getModule();
        ColorTransform t = mdl.createTransform(-1, lg, grayCS.getProfile());
        short[] lg16Toog16LUT2 = t.colorConvert(tmp, null);
        if (lg16Toog16Map == null) {
            lg16Toog16Map = Collections.synchronizedMap(new WeakHashMap(2));
        }
        lg16Toog16Map.put(grayCS, lg16Toog16LUT2);
        return lg16Toog16LUT2;
    }

    static {
        ColorModel.loadLibraries();
        ColorModel.initIDs();
        l8Tos8 = null;
        s8Tol8 = null;
        l16Tos8 = null;
        s8Tol16 = null;
        g8Tos8Map = null;
        lg16Toog8Map = null;
        g16Tos8Map = null;
        lg16Toog16Map = null;
    }
}

