/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.PackedColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

public class DirectColorModel
extends PackedColorModel {
    private int red_mask;
    private int green_mask;
    private int blue_mask;
    private int alpha_mask;
    private int red_offset;
    private int green_offset;
    private int blue_offset;
    private int alpha_offset;
    private int red_scale;
    private int green_scale;
    private int blue_scale;
    private int alpha_scale;
    private boolean is_LinearRGB;
    private int lRGBprecision;
    private byte[] tosRGB8LUT;
    private byte[] fromsRGB8LUT8;
    private short[] fromsRGB8LUT16;

    public DirectColorModel(int bits, int rmask, int gmask, int bmask) {
        this(bits, rmask, gmask, bmask, 0);
    }

    public DirectColorModel(int bits, int rmask, int gmask, int bmask, int amask) {
        super(ColorSpace.getInstance(1000), bits, rmask, gmask, bmask, amask, false, amask == 0 ? 1 : 3, ColorModel.getDefaultTransferType(bits));
        this.setFields();
    }

    public DirectColorModel(ColorSpace space, int bits, int rmask, int gmask, int bmask, int amask, boolean isAlphaPremultiplied, int transferType) {
        super(space, bits, rmask, gmask, bmask, amask, isAlphaPremultiplied, amask == 0 ? 1 : 3, transferType);
        if (ColorModel.isLinearRGBspace(this.colorSpace)) {
            this.is_LinearRGB = true;
            if (this.maxBits <= 8) {
                this.lRGBprecision = 8;
                this.tosRGB8LUT = ColorModel.getLinearRGB8TosRGB8LUT();
                this.fromsRGB8LUT8 = ColorModel.getsRGB8ToLinearRGB8LUT();
            } else {
                this.lRGBprecision = 16;
                this.tosRGB8LUT = ColorModel.getLinearRGB16TosRGB8LUT();
                this.fromsRGB8LUT16 = ColorModel.getsRGB8ToLinearRGB16LUT();
            }
        } else if (!this.is_sRGB) {
            for (int i = 0; i < 3; ++i) {
                if (space.getMinValue(i) == 0.0f && space.getMaxValue(i) == 1.0f) continue;
                throw new IllegalArgumentException("Illegal min/max RGB component value");
            }
        }
        this.setFields();
    }

    public final int getRedMask() {
        return this.maskArray[0];
    }

    public final int getGreenMask() {
        return this.maskArray[1];
    }

    public final int getBlueMask() {
        return this.maskArray[2];
    }

    public final int getAlphaMask() {
        if (this.supportsAlpha) {
            return this.maskArray[3];
        }
        return 0;
    }

    private float[] getDefaultRGBComponents(int pixel) {
        int[] components = this.getComponents(pixel, (int[])null, 0);
        float[] norm = this.getNormalizedComponents(components, 0, null, 0);
        return this.colorSpace.toRGB(norm);
    }

    private int getsRGBComponentFromsRGB(int pixel, int idx) {
        int c = (pixel & this.maskArray[idx]) >>> this.maskOffsets[idx];
        if (this.isAlphaPremultiplied) {
            int a = (pixel & this.maskArray[3]) >>> this.maskOffsets[3];
            c = a == 0 ? 0 : (int)((float)c * this.scaleFactors[idx] * 255.0f / ((float)a * this.scaleFactors[3]) + 0.5f);
        } else if (this.scaleFactors[idx] != 1.0f) {
            c = (int)((float)c * this.scaleFactors[idx] + 0.5f);
        }
        return c;
    }

    private int getsRGBComponentFromLinearRGB(int pixel, int idx) {
        int c = (pixel & this.maskArray[idx]) >>> this.maskOffsets[idx];
        if (this.isAlphaPremultiplied) {
            float factor = (1 << this.lRGBprecision) - 1;
            int a = (pixel & this.maskArray[3]) >>> this.maskOffsets[3];
            c = a == 0 ? 0 : (int)((float)c * this.scaleFactors[idx] * factor / ((float)a * this.scaleFactors[3]) + 0.5f);
        } else if (this.nBits[idx] != this.lRGBprecision) {
            c = this.lRGBprecision == 16 ? (int)((float)c * this.scaleFactors[idx] * 257.0f + 0.5f) : (int)((float)c * this.scaleFactors[idx] + 0.5f);
        }
        return this.tosRGB8LUT[c] & 0xFF;
    }

    @Override
    public final int getRed(int pixel) {
        if (this.is_sRGB) {
            return this.getsRGBComponentFromsRGB(pixel, 0);
        }
        if (this.is_LinearRGB) {
            return this.getsRGBComponentFromLinearRGB(pixel, 0);
        }
        float[] rgb = this.getDefaultRGBComponents(pixel);
        return (int)(rgb[0] * 255.0f + 0.5f);
    }

    @Override
    public final int getGreen(int pixel) {
        if (this.is_sRGB) {
            return this.getsRGBComponentFromsRGB(pixel, 1);
        }
        if (this.is_LinearRGB) {
            return this.getsRGBComponentFromLinearRGB(pixel, 1);
        }
        float[] rgb = this.getDefaultRGBComponents(pixel);
        return (int)(rgb[1] * 255.0f + 0.5f);
    }

    @Override
    public final int getBlue(int pixel) {
        if (this.is_sRGB) {
            return this.getsRGBComponentFromsRGB(pixel, 2);
        }
        if (this.is_LinearRGB) {
            return this.getsRGBComponentFromLinearRGB(pixel, 2);
        }
        float[] rgb = this.getDefaultRGBComponents(pixel);
        return (int)(rgb[2] * 255.0f + 0.5f);
    }

    @Override
    public final int getAlpha(int pixel) {
        if (!this.supportsAlpha) {
            return 255;
        }
        int a = (pixel & this.maskArray[3]) >>> this.maskOffsets[3];
        if (this.scaleFactors[3] != 1.0f) {
            a = (int)((float)a * this.scaleFactors[3] + 0.5f);
        }
        return a;
    }

    @Override
    public final int getRGB(int pixel) {
        if (this.is_sRGB || this.is_LinearRGB) {
            return this.getAlpha(pixel) << 24 | this.getRed(pixel) << 16 | this.getGreen(pixel) << 8 | this.getBlue(pixel) << 0;
        }
        float[] rgb = this.getDefaultRGBComponents(pixel);
        return this.getAlpha(pixel) << 24 | (int)(rgb[0] * 255.0f + 0.5f) << 16 | (int)(rgb[1] * 255.0f + 0.5f) << 8 | (int)(rgb[2] * 255.0f + 0.5f) << 0;
    }

    @Override
    public int getRed(Object inData) {
        int pixel = 0;
        switch (this.transferType) {
            case 0: {
                byte[] bdata = (byte[])inData;
                pixel = bdata[0] & 0xFF;
                break;
            }
            case 1: {
                short[] sdata = (short[])inData;
                pixel = sdata[0] & 0xFFFF;
                break;
            }
            case 3: {
                int[] idata = (int[])inData;
                pixel = idata[0];
                break;
            }
            default: {
                throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
            }
        }
        return this.getRed(pixel);
    }

    @Override
    public int getGreen(Object inData) {
        int pixel = 0;
        switch (this.transferType) {
            case 0: {
                byte[] bdata = (byte[])inData;
                pixel = bdata[0] & 0xFF;
                break;
            }
            case 1: {
                short[] sdata = (short[])inData;
                pixel = sdata[0] & 0xFFFF;
                break;
            }
            case 3: {
                int[] idata = (int[])inData;
                pixel = idata[0];
                break;
            }
            default: {
                throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
            }
        }
        return this.getGreen(pixel);
    }

    @Override
    public int getBlue(Object inData) {
        int pixel = 0;
        switch (this.transferType) {
            case 0: {
                byte[] bdata = (byte[])inData;
                pixel = bdata[0] & 0xFF;
                break;
            }
            case 1: {
                short[] sdata = (short[])inData;
                pixel = sdata[0] & 0xFFFF;
                break;
            }
            case 3: {
                int[] idata = (int[])inData;
                pixel = idata[0];
                break;
            }
            default: {
                throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
            }
        }
        return this.getBlue(pixel);
    }

    @Override
    public int getAlpha(Object inData) {
        int pixel = 0;
        switch (this.transferType) {
            case 0: {
                byte[] bdata = (byte[])inData;
                pixel = bdata[0] & 0xFF;
                break;
            }
            case 1: {
                short[] sdata = (short[])inData;
                pixel = sdata[0] & 0xFFFF;
                break;
            }
            case 3: {
                int[] idata = (int[])inData;
                pixel = idata[0];
                break;
            }
            default: {
                throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
            }
        }
        return this.getAlpha(pixel);
    }

    @Override
    public int getRGB(Object inData) {
        int pixel = 0;
        switch (this.transferType) {
            case 0: {
                byte[] bdata = (byte[])inData;
                pixel = bdata[0] & 0xFF;
                break;
            }
            case 1: {
                short[] sdata = (short[])inData;
                pixel = sdata[0] & 0xFFFF;
                break;
            }
            case 3: {
                int[] idata = (int[])inData;
                pixel = idata[0];
                break;
            }
            default: {
                throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
            }
        }
        return this.getRGB(pixel);
    }

    @Override
    public Object getDataElements(int rgb, Object pixel) {
        int[] intpixel = null;
        if (this.transferType == 3 && pixel != null) {
            intpixel = (int[])pixel;
            intpixel[0] = 0;
        } else {
            intpixel = new int[1];
        }
        ColorModel defaultCM = ColorModel.getRGBdefault();
        if (this == defaultCM || this.equals(defaultCM)) {
            intpixel[0] = rgb;
            return intpixel;
        }
        int red = rgb >> 16 & 0xFF;
        int grn = rgb >> 8 & 0xFF;
        int blu = rgb & 0xFF;
        if (this.is_sRGB || this.is_LinearRGB) {
            float factor;
            int precision;
            if (this.is_LinearRGB) {
                if (this.lRGBprecision == 8) {
                    red = this.fromsRGB8LUT8[red] & 0xFF;
                    grn = this.fromsRGB8LUT8[grn] & 0xFF;
                    blu = this.fromsRGB8LUT8[blu] & 0xFF;
                    precision = 8;
                    factor = 0.003921569f;
                } else {
                    red = this.fromsRGB8LUT16[red] & 0xFFFF;
                    grn = this.fromsRGB8LUT16[grn] & 0xFFFF;
                    blu = this.fromsRGB8LUT16[blu] & 0xFFFF;
                    precision = 16;
                    factor = 1.5259022E-5f;
                }
            } else {
                precision = 8;
                factor = 0.003921569f;
            }
            if (this.supportsAlpha) {
                int alp = rgb >> 24 & 0xFF;
                if (this.isAlphaPremultiplied) {
                    factor *= (float)alp * 0.003921569f;
                    precision = -1;
                }
                if (this.nBits[3] != 8 && (alp = (int)((float)alp * 0.003921569f * (float)((1 << this.nBits[3]) - 1) + 0.5f)) > (1 << this.nBits[3]) - 1) {
                    alp = (1 << this.nBits[3]) - 1;
                }
                intpixel[0] = alp << this.maskOffsets[3];
            }
            if (this.nBits[0] != precision) {
                red = (int)((float)red * factor * (float)((1 << this.nBits[0]) - 1) + 0.5f);
            }
            if (this.nBits[1] != precision) {
                grn = (int)((float)grn * factor * (float)((1 << this.nBits[1]) - 1) + 0.5f);
            }
            if (this.nBits[2] != precision) {
                blu = (int)((float)blu * factor * (float)((1 << this.nBits[2]) - 1) + 0.5f);
            }
        } else {
            float[] norm = new float[3];
            float factor = 0.003921569f;
            norm[0] = (float)red * factor;
            norm[1] = (float)grn * factor;
            norm[2] = (float)blu * factor;
            norm = this.colorSpace.fromRGB(norm);
            if (this.supportsAlpha) {
                int alp = rgb >> 24 & 0xFF;
                if (this.isAlphaPremultiplied) {
                    factor *= (float)alp;
                    int i = 0;
                    while (i < 3) {
                        int n = i++;
                        norm[n] = norm[n] * factor;
                    }
                }
                if (this.nBits[3] != 8 && (alp = (int)((float)alp * 0.003921569f * (float)((1 << this.nBits[3]) - 1) + 0.5f)) > (1 << this.nBits[3]) - 1) {
                    alp = (1 << this.nBits[3]) - 1;
                }
                intpixel[0] = alp << this.maskOffsets[3];
            }
            red = (int)(norm[0] * (float)((1 << this.nBits[0]) - 1) + 0.5f);
            grn = (int)(norm[1] * (float)((1 << this.nBits[1]) - 1) + 0.5f);
            blu = (int)(norm[2] * (float)((1 << this.nBits[2]) - 1) + 0.5f);
        }
        if (this.maxBits > 23) {
            if (red > (1 << this.nBits[0]) - 1) {
                red = (1 << this.nBits[0]) - 1;
            }
            if (grn > (1 << this.nBits[1]) - 1) {
                grn = (1 << this.nBits[1]) - 1;
            }
            if (blu > (1 << this.nBits[2]) - 1) {
                blu = (1 << this.nBits[2]) - 1;
            }
        }
        intpixel[0] = intpixel[0] | (red << this.maskOffsets[0] | grn << this.maskOffsets[1] | blu << this.maskOffsets[2]);
        switch (this.transferType) {
            case 0: {
                byte[] bdata = pixel == null ? new byte[1] : (byte[])pixel;
                bdata[0] = (byte)(0xFF & intpixel[0]);
                return bdata;
            }
            case 1: {
                short[] sdata = pixel == null ? new short[1] : (short[])pixel;
                sdata[0] = (short)(intpixel[0] & 0xFFFF);
                return sdata;
            }
            case 3: {
                return intpixel;
            }
        }
        throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
    }

    @Override
    public final int[] getComponents(int pixel, int[] components, int offset) {
        if (components == null) {
            components = new int[offset + this.numComponents];
        }
        for (int i = 0; i < this.numComponents; ++i) {
            components[offset + i] = (pixel & this.maskArray[i]) >>> this.maskOffsets[i];
        }
        return components;
    }

    @Override
    public final int[] getComponents(Object pixel, int[] components, int offset) {
        int intpixel = 0;
        switch (this.transferType) {
            case 0: {
                byte[] bdata = (byte[])pixel;
                intpixel = bdata[0] & 0xFF;
                break;
            }
            case 1: {
                short[] sdata = (short[])pixel;
                intpixel = sdata[0] & 0xFFFF;
                break;
            }
            case 3: {
                int[] idata = (int[])pixel;
                intpixel = idata[0];
                break;
            }
            default: {
                throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
            }
        }
        return this.getComponents(intpixel, components, offset);
    }

    @Override
    public final WritableRaster createCompatibleWritableRaster(int w, int h) {
        int[] bandmasks;
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("Width (" + w + ") and height (" + h + ") cannot be <= 0");
        }
        if (this.supportsAlpha) {
            bandmasks = new int[4];
            bandmasks[3] = this.alpha_mask;
        } else {
            bandmasks = new int[]{this.red_mask, this.green_mask, this.blue_mask};
        }
        if (this.pixel_bits > 16) {
            return Raster.createPackedRaster(3, w, h, bandmasks, null);
        }
        if (this.pixel_bits > 8) {
            return Raster.createPackedRaster(1, w, h, bandmasks, null);
        }
        return Raster.createPackedRaster(0, w, h, bandmasks, null);
    }

    @Override
    public int getDataElement(int[] components, int offset) {
        int pixel = 0;
        for (int i = 0; i < this.numComponents; ++i) {
            pixel |= components[offset + i] << this.maskOffsets[i] & this.maskArray[i];
        }
        return pixel;
    }

    @Override
    public Object getDataElements(int[] components, int offset, Object obj) {
        int pixel = 0;
        for (int i = 0; i < this.numComponents; ++i) {
            pixel |= components[offset + i] << this.maskOffsets[i] & this.maskArray[i];
        }
        switch (this.transferType) {
            case 0: {
                if (obj instanceof byte[]) {
                    byte[] bdata = (byte[])obj;
                    bdata[0] = (byte)(pixel & 0xFF);
                    return bdata;
                }
                byte[] bdata = new byte[]{(byte)(pixel & 0xFF)};
                return bdata;
            }
            case 1: {
                if (obj instanceof short[]) {
                    short[] sdata = (short[])obj;
                    sdata[0] = (short)(pixel & 0xFFFF);
                    return sdata;
                }
                short[] sdata = new short[]{(short)(pixel & 0xFFFF)};
                return sdata;
            }
            case 3: {
                if (obj instanceof int[]) {
                    int[] idata = (int[])obj;
                    idata[0] = pixel;
                    return idata;
                }
                int[] idata = new int[]{pixel};
                return idata;
            }
        }
        throw new ClassCastException("This method has not been implemented for transferType " + this.transferType);
    }

    @Override
    public final ColorModel coerceData(WritableRaster raster, boolean isAlphaPremultiplied) {
        block42: {
            int[] pixel;
            int rY;
            int rminX;
            float alphaScale;
            int aIdx;
            int h;
            int w;
            block41: {
                if (!this.supportsAlpha || this.isAlphaPremultiplied() == isAlphaPremultiplied) {
                    return this;
                }
                w = raster.getWidth();
                h = raster.getHeight();
                aIdx = this.numColorComponents;
                alphaScale = 1.0f / (float)((1 << this.nBits[aIdx]) - 1);
                rminX = raster.getMinX();
                rY = raster.getMinY();
                pixel = null;
                int[] zpixel = null;
                if (!isAlphaPremultiplied) break block41;
                switch (this.transferType) {
                    case 0: {
                        int y = 0;
                        while (y < h) {
                            int rX = rminX;
                            int x = 0;
                            while (x < w) {
                                float normAlpha = (float)(pixel = raster.getPixel(rX, rY, pixel))[aIdx] * alphaScale;
                                if (normAlpha != 0.0f) {
                                    for (int c = 0; c < aIdx; ++c) {
                                        pixel[c] = (int)((float)pixel[c] * normAlpha + 0.5f);
                                    }
                                    raster.setPixel(rX, rY, pixel);
                                } else {
                                    if (zpixel == null) {
                                        zpixel = new int[this.numComponents];
                                    }
                                    raster.setPixel(rX, rY, zpixel);
                                }
                                ++x;
                                ++rX;
                            }
                            ++y;
                            ++rY;
                        }
                        break block42;
                    }
                    case 1: {
                        int y = 0;
                        while (y < h) {
                            int rX = rminX;
                            int x = 0;
                            while (x < w) {
                                float normAlpha = (float)(pixel = raster.getPixel(rX, rY, pixel))[aIdx] * alphaScale;
                                if (normAlpha != 0.0f) {
                                    for (int c = 0; c < aIdx; ++c) {
                                        pixel[c] = (int)((float)pixel[c] * normAlpha + 0.5f);
                                    }
                                    raster.setPixel(rX, rY, pixel);
                                } else {
                                    if (zpixel == null) {
                                        zpixel = new int[this.numComponents];
                                    }
                                    raster.setPixel(rX, rY, zpixel);
                                }
                                ++x;
                                ++rX;
                            }
                            ++y;
                            ++rY;
                        }
                        break block42;
                    }
                    case 3: {
                        int y = 0;
                        while (y < h) {
                            int rX = rminX;
                            int x = 0;
                            while (x < w) {
                                float normAlpha = (float)(pixel = raster.getPixel(rX, rY, pixel))[aIdx] * alphaScale;
                                if (normAlpha != 0.0f) {
                                    for (int c = 0; c < aIdx; ++c) {
                                        pixel[c] = (int)((float)pixel[c] * normAlpha + 0.5f);
                                    }
                                    raster.setPixel(rX, rY, pixel);
                                } else {
                                    if (zpixel == null) {
                                        zpixel = new int[this.numComponents];
                                    }
                                    raster.setPixel(rX, rY, zpixel);
                                }
                                ++x;
                                ++rX;
                            }
                            ++y;
                            ++rY;
                        }
                        break block42;
                    }
                    default: {
                        throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
                    }
                }
            }
            switch (this.transferType) {
                case 0: {
                    int y = 0;
                    while (y < h) {
                        int rX = rminX;
                        int x = 0;
                        while (x < w) {
                            float normAlpha = (float)(pixel = raster.getPixel(rX, rY, pixel))[aIdx] * alphaScale;
                            if (normAlpha != 0.0f) {
                                float invAlpha = 1.0f / normAlpha;
                                for (int c = 0; c < aIdx; ++c) {
                                    pixel[c] = (int)((float)pixel[c] * invAlpha + 0.5f);
                                }
                                raster.setPixel(rX, rY, pixel);
                            }
                            ++x;
                            ++rX;
                        }
                        ++y;
                        ++rY;
                    }
                    break;
                }
                case 1: {
                    int y = 0;
                    while (y < h) {
                        int rX = rminX;
                        int x = 0;
                        while (x < w) {
                            float normAlpha = (float)(pixel = raster.getPixel(rX, rY, pixel))[aIdx] * alphaScale;
                            if (normAlpha != 0.0f) {
                                float invAlpha = 1.0f / normAlpha;
                                for (int c = 0; c < aIdx; ++c) {
                                    pixel[c] = (int)((float)pixel[c] * invAlpha + 0.5f);
                                }
                                raster.setPixel(rX, rY, pixel);
                            }
                            ++x;
                            ++rX;
                        }
                        ++y;
                        ++rY;
                    }
                    break;
                }
                case 3: {
                    int y = 0;
                    while (y < h) {
                        int rX = rminX;
                        int x = 0;
                        while (x < w) {
                            float normAlpha = (float)(pixel = raster.getPixel(rX, rY, pixel))[aIdx] * alphaScale;
                            if (normAlpha != 0.0f) {
                                float invAlpha = 1.0f / normAlpha;
                                for (int c = 0; c < aIdx; ++c) {
                                    pixel[c] = (int)((float)pixel[c] * invAlpha + 0.5f);
                                }
                                raster.setPixel(rX, rY, pixel);
                            }
                            ++x;
                            ++rX;
                        }
                        ++y;
                        ++rY;
                    }
                    break;
                }
                default: {
                    throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
                }
            }
        }
        return new DirectColorModel(this.colorSpace, this.pixel_bits, this.maskArray[0], this.maskArray[1], this.maskArray[2], this.maskArray[3], isAlphaPremultiplied, this.transferType);
    }

    @Override
    public boolean isCompatibleRaster(Raster raster) {
        SampleModel sm = raster.getSampleModel();
        if (!(sm instanceof SinglePixelPackedSampleModel)) {
            return false;
        }
        SinglePixelPackedSampleModel spsm = (SinglePixelPackedSampleModel)sm;
        if (spsm.getNumBands() != this.getNumComponents()) {
            return false;
        }
        int[] bitMasks = spsm.getBitMasks();
        for (int i = 0; i < this.numComponents; ++i) {
            if (bitMasks[i] == this.maskArray[i]) continue;
            return false;
        }
        return raster.getTransferType() == this.transferType;
    }

    private void setFields() {
        this.red_mask = this.maskArray[0];
        this.red_offset = this.maskOffsets[0];
        this.green_mask = this.maskArray[1];
        this.green_offset = this.maskOffsets[1];
        this.blue_mask = this.maskArray[2];
        this.blue_offset = this.maskOffsets[2];
        if (this.nBits[0] < 8) {
            this.red_scale = (1 << this.nBits[0]) - 1;
        }
        if (this.nBits[1] < 8) {
            this.green_scale = (1 << this.nBits[1]) - 1;
        }
        if (this.nBits[2] < 8) {
            this.blue_scale = (1 << this.nBits[2]) - 1;
        }
        if (this.supportsAlpha) {
            this.alpha_mask = this.maskArray[3];
            this.alpha_offset = this.maskOffsets[3];
            if (this.nBits[3] < 8) {
                this.alpha_scale = (1 << this.nBits[3]) - 1;
            }
        }
    }

    @Override
    public String toString() {
        return "DirectColorModel: rmask=" + Integer.toHexString(this.red_mask) + " gmask=" + Integer.toHexString(this.green_mask) + " bmask=" + Integer.toHexString(this.blue_mask) + " amask=" + Integer.toHexString(this.alpha_mask);
    }
}

