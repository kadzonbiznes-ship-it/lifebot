/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;

public class ComponentColorModel
extends ColorModel {
    private boolean signed;
    private boolean is_sRGB_stdScale;
    private boolean is_LinearRGB_stdScale;
    private boolean is_LinearGray_stdScale;
    private boolean is_ICCGray_stdScale;
    private byte[] tosRGB8LUT;
    private byte[] fromsRGB8LUT8;
    private short[] fromsRGB8LUT16;
    private byte[] fromLinearGray16ToOtherGray8LUT;
    private short[] fromLinearGray16ToOtherGray16LUT;
    private boolean needScaleInit;
    private boolean noUnnorm;
    private boolean nonStdScale;
    private float[] min;
    private float[] diffMinMax;
    private float[] compOffset;
    private float[] compScale;
    private volatile int hashCode;

    public ComponentColorModel(ColorSpace colorSpace, int[] bits, boolean hasAlpha, boolean isAlphaPremultiplied, int transparency, int transferType) {
        super(ComponentColorModel.bitsHelper(transferType, colorSpace, hasAlpha), ComponentColorModel.bitsArrayHelper(bits, transferType, colorSpace, hasAlpha), colorSpace, hasAlpha, isAlphaPremultiplied, transparency, transferType);
        switch (transferType) {
            case 0: 
            case 1: 
            case 3: {
                this.signed = false;
                this.needScaleInit = true;
                break;
            }
            case 2: {
                this.signed = true;
                this.needScaleInit = true;
                break;
            }
            case 4: 
            case 5: {
                this.signed = true;
                this.needScaleInit = false;
                this.noUnnorm = true;
                this.nonStdScale = false;
                break;
            }
            default: {
                throw new IllegalArgumentException("This constructor is not compatible with transferType " + transferType);
            }
        }
        this.setupLUTs();
    }

    public ComponentColorModel(ColorSpace colorSpace, boolean hasAlpha, boolean isAlphaPremultiplied, int transparency, int transferType) {
        this(colorSpace, null, hasAlpha, isAlphaPremultiplied, transparency, transferType);
    }

    private static int bitsHelper(int transferType, ColorSpace colorSpace, boolean hasAlpha) {
        int numBits = DataBuffer.getDataTypeSize(transferType);
        int numComponents = colorSpace.getNumComponents();
        if (hasAlpha) {
            ++numComponents;
        }
        return numBits * numComponents;
    }

    private static int[] bitsArrayHelper(int[] origBits, int transferType, ColorSpace colorSpace, boolean hasAlpha) {
        switch (transferType) {
            case 0: 
            case 1: 
            case 3: {
                if (origBits == null) break;
                return origBits;
            }
        }
        int numBits = DataBuffer.getDataTypeSize(transferType);
        int numComponents = colorSpace.getNumComponents();
        if (hasAlpha) {
            ++numComponents;
        }
        int[] bits = new int[numComponents];
        for (int i = 0; i < numComponents; ++i) {
            bits[i] = numBits;
        }
        return bits;
    }

    private void setupLUTs() {
        if (this.is_sRGB) {
            this.is_sRGB_stdScale = true;
            this.nonStdScale = false;
        } else if (ColorModel.isLinearRGBspace(this.colorSpace)) {
            this.is_LinearRGB_stdScale = true;
            this.nonStdScale = false;
            if (this.transferType == 0) {
                this.tosRGB8LUT = ColorModel.getLinearRGB8TosRGB8LUT();
                this.fromsRGB8LUT8 = ColorModel.getsRGB8ToLinearRGB8LUT();
            } else {
                this.tosRGB8LUT = ColorModel.getLinearRGB16TosRGB8LUT();
                this.fromsRGB8LUT16 = ColorModel.getsRGB8ToLinearRGB16LUT();
            }
        } else if (this.colorSpaceType == 6 && this.colorSpace instanceof ICC_ColorSpace && this.colorSpace.getMinValue(0) == 0.0f && this.colorSpace.getMaxValue(0) == 1.0f) {
            ICC_ColorSpace ics = (ICC_ColorSpace)this.colorSpace;
            this.is_ICCGray_stdScale = true;
            this.nonStdScale = false;
            this.fromsRGB8LUT16 = ColorModel.getsRGB8ToLinearRGB16LUT();
            if (ColorModel.isLinearGRAYspace(ics)) {
                this.is_LinearGray_stdScale = true;
                this.tosRGB8LUT = this.transferType == 0 ? ColorModel.getGray8TosRGB8LUT(ics) : ColorModel.getGray16TosRGB8LUT(ics);
            } else if (this.transferType == 0) {
                this.tosRGB8LUT = ColorModel.getGray8TosRGB8LUT(ics);
                this.fromLinearGray16ToOtherGray8LUT = ColorModel.getLinearGray16ToOtherGray8LUT(ics);
            } else {
                this.tosRGB8LUT = ColorModel.getGray16TosRGB8LUT(ics);
                this.fromLinearGray16ToOtherGray16LUT = ColorModel.getLinearGray16ToOtherGray16LUT(ics);
            }
        } else if (this.needScaleInit) {
            int i;
            this.nonStdScale = false;
            for (i = 0; i < this.numColorComponents; ++i) {
                if (this.colorSpace.getMinValue(i) == 0.0f && this.colorSpace.getMaxValue(i) == 1.0f) continue;
                this.nonStdScale = true;
                break;
            }
            if (this.nonStdScale) {
                this.min = new float[this.numColorComponents];
                this.diffMinMax = new float[this.numColorComponents];
                for (i = 0; i < this.numColorComponents; ++i) {
                    this.min[i] = this.colorSpace.getMinValue(i);
                    this.diffMinMax[i] = this.colorSpace.getMaxValue(i) - this.min[i];
                }
            }
        }
    }

    private void initScale() {
        int i;
        float[] highVal;
        float[] lowVal;
        this.needScaleInit = false;
        this.noUnnorm = this.nonStdScale || this.signed;
        switch (this.transferType) {
            case 0: {
                int i2;
                byte[] bpixel = new byte[this.numComponents];
                if (this.supportsAlpha) {
                    bpixel[this.numColorComponents] = (byte)((1 << this.nBits[this.numColorComponents]) - 1);
                }
                lowVal = this.getNormalizedComponents(bpixel, null, 0);
                for (i2 = 0; i2 < this.numColorComponents; ++i2) {
                    bpixel[i2] = (byte)((1 << this.nBits[i2]) - 1);
                }
                highVal = this.getNormalizedComponents(bpixel, null, 0);
                break;
            }
            case 1: {
                int i2;
                short[] uspixel = new short[this.numComponents];
                if (this.supportsAlpha) {
                    uspixel[this.numColorComponents] = (short)((1 << this.nBits[this.numColorComponents]) - 1);
                }
                lowVal = this.getNormalizedComponents(uspixel, null, 0);
                for (i2 = 0; i2 < this.numColorComponents; ++i2) {
                    uspixel[i2] = (short)((1 << this.nBits[i2]) - 1);
                }
                highVal = this.getNormalizedComponents(uspixel, null, 0);
                break;
            }
            case 3: {
                int i2;
                int[] ipixel = new int[this.numComponents];
                if (this.supportsAlpha) {
                    ipixel[this.numColorComponents] = (1 << this.nBits[this.numColorComponents]) - 1;
                }
                lowVal = this.getNormalizedComponents(ipixel, null, 0);
                for (i2 = 0; i2 < this.numColorComponents; ++i2) {
                    ipixel[i2] = (1 << this.nBits[i2]) - 1;
                }
                highVal = this.getNormalizedComponents(ipixel, null, 0);
                break;
            }
            case 2: {
                int i2;
                short[] spixel = new short[this.numComponents];
                if (this.supportsAlpha) {
                    spixel[this.numColorComponents] = Short.MAX_VALUE;
                }
                lowVal = this.getNormalizedComponents(spixel, null, 0);
                for (i2 = 0; i2 < this.numColorComponents; ++i2) {
                    spixel[i2] = Short.MAX_VALUE;
                }
                highVal = this.getNormalizedComponents(spixel, null, 0);
                break;
            }
            default: {
                highVal = null;
                lowVal = null;
            }
        }
        this.nonStdScale = false;
        for (i = 0; i < this.numColorComponents; ++i) {
            if (lowVal[i] == 0.0f && highVal[i] == 1.0f) continue;
            this.nonStdScale = true;
            break;
        }
        if (this.nonStdScale) {
            this.noUnnorm = true;
            this.is_sRGB_stdScale = false;
            this.is_LinearRGB_stdScale = false;
            this.is_LinearGray_stdScale = false;
            this.is_ICCGray_stdScale = false;
            this.compOffset = new float[this.numColorComponents];
            this.compScale = new float[this.numColorComponents];
            for (i = 0; i < this.numColorComponents; ++i) {
                this.compOffset[i] = lowVal[i];
                this.compScale[i] = 1.0f / (highVal[i] - lowVal[i]);
            }
        }
    }

    private int getRGBComponent(int pixel, int idx) {
        if (this.numComponents > 1) {
            throw new IllegalArgumentException("More than one component per pixel");
        }
        if (this.signed) {
            throw new IllegalArgumentException("Component value is signed");
        }
        if (this.needScaleInit) {
            this.initScale();
        }
        Object[] opixel = null;
        switch (this.transferType) {
            case 0: {
                byte[] bpixel = new byte[]{(byte)pixel};
                opixel = bpixel;
                break;
            }
            case 1: {
                short[] spixel = new short[]{(short)pixel};
                opixel = spixel;
                break;
            }
            case 3: {
                int[] ipixel;
                opixel = ipixel = new int[]{pixel};
            }
        }
        float[] norm = this.getNormalizedComponents(opixel, null, 0);
        float[] rgb = this.colorSpace.toRGB(norm);
        return (int)(rgb[idx] * 255.0f + 0.5f);
    }

    @Override
    public int getRed(int pixel) {
        return this.getRGBComponent(pixel, 0);
    }

    @Override
    public int getGreen(int pixel) {
        return this.getRGBComponent(pixel, 1);
    }

    @Override
    public int getBlue(int pixel) {
        return this.getRGBComponent(pixel, 2);
    }

    @Override
    public int getAlpha(int pixel) {
        if (!this.supportsAlpha) {
            return 255;
        }
        if (this.numComponents > 1) {
            throw new IllegalArgumentException("More than one component per pixel");
        }
        if (this.signed) {
            throw new IllegalArgumentException("Component value is signed");
        }
        return (int)((float)pixel / (float)((1 << this.nBits[0]) - 1) * 255.0f + 0.5f);
    }

    @Override
    public int getRGB(int pixel) {
        if (this.numComponents > 1) {
            throw new IllegalArgumentException("More than one component per pixel");
        }
        if (this.signed) {
            throw new IllegalArgumentException("Component value is signed");
        }
        return this.getAlpha(pixel) << 24 | this.getRed(pixel) << 16 | this.getGreen(pixel) << 8 | this.getBlue(pixel) << 0;
    }

    private int extractComponent(Object inData, int idx, int precision) {
        int comp;
        boolean needAlpha = this.supportsAlpha && this.isAlphaPremultiplied;
        int alp = 0;
        int mask = (1 << this.nBits[idx]) - 1;
        switch (this.transferType) {
            case 2: {
                short[] sdata = (short[])inData;
                float scalefactor = (1 << precision) - 1;
                if (needAlpha) {
                    short s = sdata[this.numColorComponents];
                    if (s != 0) {
                        return (int)((float)sdata[idx] / (float)s * scalefactor + 0.5f);
                    }
                    return 0;
                }
                return (int)((float)sdata[idx] / 32767.0f * scalefactor + 0.5f);
            }
            case 4: {
                float[] fdata = (float[])inData;
                float scalefactor = (1 << precision) - 1;
                if (needAlpha) {
                    float f = fdata[this.numColorComponents];
                    if (f != 0.0f) {
                        return (int)(fdata[idx] / f * scalefactor + 0.5f);
                    }
                    return 0;
                }
                return (int)(fdata[idx] * scalefactor + 0.5f);
            }
            case 5: {
                double[] ddata = (double[])inData;
                double scalefactor = (1 << precision) - 1;
                if (needAlpha) {
                    double d = ddata[this.numColorComponents];
                    if (d != 0.0) {
                        return (int)(ddata[idx] / d * scalefactor + 0.5);
                    }
                    return 0;
                }
                return (int)(ddata[idx] * scalefactor + 0.5);
            }
            case 0: {
                byte[] bdata = (byte[])inData;
                comp = bdata[idx] & mask;
                precision = 8;
                if (!needAlpha) break;
                alp = bdata[this.numColorComponents] & mask;
                break;
            }
            case 1: {
                short[] usdata = (short[])inData;
                comp = usdata[idx] & mask;
                if (!needAlpha) break;
                alp = usdata[this.numColorComponents] & mask;
                break;
            }
            case 3: {
                int[] idata = (int[])inData;
                comp = idata[idx];
                if (!needAlpha) break;
                alp = idata[this.numColorComponents];
                break;
            }
            default: {
                throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
            }
        }
        if (needAlpha) {
            if (alp != 0) {
                float scalefactor = (1 << precision) - 1;
                float fcomp = (float)comp / (float)mask;
                float invalp = (float)((1 << this.nBits[this.numColorComponents]) - 1) / (float)alp;
                return (int)(fcomp * invalp * scalefactor + 0.5f);
            }
            return 0;
        }
        if (this.nBits[idx] != precision) {
            float scalefactor = (1 << precision) - 1;
            float fcomp = (float)comp / (float)mask;
            return (int)(fcomp * scalefactor + 0.5f);
        }
        return comp;
    }

    private int getRGBComponent(Object inData, int idx) {
        if (this.needScaleInit) {
            this.initScale();
        }
        if (this.is_sRGB_stdScale) {
            return this.extractComponent(inData, idx, 8);
        }
        if (this.is_LinearRGB_stdScale) {
            int lutidx = this.extractComponent(inData, idx, 16);
            return this.tosRGB8LUT[lutidx] & 0xFF;
        }
        if (this.is_ICCGray_stdScale) {
            int lutidx = this.extractComponent(inData, 0, 16);
            return this.tosRGB8LUT[lutidx] & 0xFF;
        }
        float[] norm = this.getNormalizedComponents(inData, null, 0);
        float[] rgb = this.colorSpace.toRGB(norm);
        return (int)(rgb[idx] * 255.0f + 0.5f);
    }

    @Override
    public int getRed(Object inData) {
        return this.getRGBComponent(inData, 0);
    }

    @Override
    public int getGreen(Object inData) {
        return this.getRGBComponent(inData, 1);
    }

    @Override
    public int getBlue(Object inData) {
        return this.getRGBComponent(inData, 2);
    }

    @Override
    public int getAlpha(Object inData) {
        if (!this.supportsAlpha) {
            return 255;
        }
        int alpha = 0;
        int aIdx = this.numColorComponents;
        int mask = (1 << this.nBits[aIdx]) - 1;
        switch (this.transferType) {
            case 2: {
                short[] sdata = (short[])inData;
                alpha = (int)((float)sdata[aIdx] / 32767.0f * 255.0f + 0.5f);
                return alpha;
            }
            case 4: {
                float[] fdata = (float[])inData;
                alpha = (int)(fdata[aIdx] * 255.0f + 0.5f);
                return alpha;
            }
            case 5: {
                double[] ddata = (double[])inData;
                alpha = (int)(ddata[aIdx] * 255.0 + 0.5);
                return alpha;
            }
            case 0: {
                byte[] bdata = (byte[])inData;
                alpha = bdata[aIdx] & mask;
                break;
            }
            case 1: {
                short[] usdata = (short[])inData;
                alpha = usdata[aIdx] & mask;
                break;
            }
            case 3: {
                int[] idata = (int[])inData;
                alpha = idata[aIdx];
                break;
            }
            default: {
                throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
            }
        }
        if (this.nBits[aIdx] == 8) {
            return alpha;
        }
        return (int)((float)alpha / (float)((1 << this.nBits[aIdx]) - 1) * 255.0f + 0.5f);
    }

    @Override
    public int getRGB(Object inData) {
        if (this.needScaleInit) {
            this.initScale();
        }
        if (this.is_sRGB_stdScale || this.is_LinearRGB_stdScale) {
            return this.getAlpha(inData) << 24 | this.getRed(inData) << 16 | this.getGreen(inData) << 8 | this.getBlue(inData);
        }
        if (this.colorSpaceType == 6) {
            int gray = this.getRed(inData);
            return this.getAlpha(inData) << 24 | gray << 16 | gray << 8 | gray;
        }
        float[] norm = this.getNormalizedComponents(inData, null, 0);
        float[] rgb = this.colorSpace.toRGB(norm);
        return this.getAlpha(inData) << 24 | (int)(rgb[0] * 255.0f + 0.5f) << 16 | (int)(rgb[1] * 255.0f + 0.5f) << 8 | (int)(rgb[2] * 255.0f + 0.5f) << 0;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public Object getDataElements(int rgb, Object pixel) {
        int red = rgb >> 16 & 0xFF;
        int grn = rgb >> 8 & 0xFF;
        int blu = rgb & 0xFF;
        if (this.needScaleInit) {
            this.initScale();
        }
        if (this.signed) {
            switch (this.transferType) {
                case 2: {
                    short[] sdata = pixel == null ? new short[this.numComponents] : (short[])pixel;
                    if (this.is_sRGB_stdScale || this.is_LinearRGB_stdScale) {
                        float factor = 128.49803f;
                        if (this.is_LinearRGB_stdScale) {
                            red = this.fromsRGB8LUT16[red] & 0xFFFF;
                            grn = this.fromsRGB8LUT16[grn] & 0xFFFF;
                            blu = this.fromsRGB8LUT16[blu] & 0xFFFF;
                            factor = 0.49999237f;
                        }
                        if (this.supportsAlpha) {
                            int alp = rgb >> 24 & 0xFF;
                            sdata[3] = (short)((float)alp * 128.49803f + 0.5f);
                            if (this.isAlphaPremultiplied) {
                                factor = (float)alp * factor * 0.003921569f;
                            }
                        }
                        sdata[0] = (short)((float)red * factor + 0.5f);
                        sdata[1] = (short)((float)grn * factor + 0.5f);
                        sdata[2] = (short)((float)blu * factor + 0.5f);
                        return sdata;
                    } else if (this.is_LinearGray_stdScale) {
                        red = this.fromsRGB8LUT16[red] & 0xFFFF;
                        grn = this.fromsRGB8LUT16[grn] & 0xFFFF;
                        blu = this.fromsRGB8LUT16[blu] & 0xFFFF;
                        float gray = (0.2125f * (float)red + 0.7154f * (float)grn + 0.0721f * (float)blu) / 65535.0f;
                        float factor = 32767.0f;
                        if (this.supportsAlpha) {
                            int alp = rgb >> 24 & 0xFF;
                            sdata[1] = (short)((float)alp * 128.49803f + 0.5f);
                            if (this.isAlphaPremultiplied) {
                                factor = (float)alp * factor * 0.003921569f;
                            }
                        }
                        sdata[0] = (short)(gray * factor + 0.5f);
                        return sdata;
                    } else if (this.is_ICCGray_stdScale) {
                        red = this.fromsRGB8LUT16[red] & 0xFFFF;
                        grn = this.fromsRGB8LUT16[grn] & 0xFFFF;
                        blu = this.fromsRGB8LUT16[blu] & 0xFFFF;
                        int gray = (int)(0.2125f * (float)red + 0.7154f * (float)grn + 0.0721f * (float)blu + 0.5f);
                        gray = this.fromLinearGray16ToOtherGray16LUT[gray] & 0xFFFF;
                        float factor = 0.49999237f;
                        if (this.supportsAlpha) {
                            int alp = rgb >> 24 & 0xFF;
                            sdata[1] = (short)((float)alp * 128.49803f + 0.5f);
                            if (this.isAlphaPremultiplied) {
                                factor = (float)alp * factor * 0.003921569f;
                            }
                        }
                        sdata[0] = (short)((float)gray * factor + 0.5f);
                        return sdata;
                    } else {
                        int i;
                        float factor = 0.003921569f;
                        float[] norm = new float[]{(float)red * factor, (float)grn * factor, (float)blu * factor};
                        norm = this.colorSpace.fromRGB(norm);
                        if (this.nonStdScale) {
                            for (i = 0; i < this.numColorComponents; ++i) {
                                norm[i] = (norm[i] - this.compOffset[i]) * this.compScale[i];
                                if (norm[i] < 0.0f) {
                                    norm[i] = 0.0f;
                                }
                                if (!(norm[i] > 1.0f)) continue;
                                norm[i] = 1.0f;
                            }
                        }
                        factor = 32767.0f;
                        if (this.supportsAlpha) {
                            int alp = rgb >> 24 & 0xFF;
                            sdata[this.numColorComponents] = (short)((float)alp * 128.49803f + 0.5f);
                            if (this.isAlphaPremultiplied) {
                                factor *= (float)alp * 0.003921569f;
                            }
                        }
                        for (i = 0; i < this.numColorComponents; ++i) {
                            sdata[i] = (short)(norm[i] * factor + 0.5f);
                        }
                    }
                    return sdata;
                }
                case 4: {
                    float[] fdata = pixel == null ? new float[this.numComponents] : (float[])pixel;
                    if (this.is_sRGB_stdScale || this.is_LinearRGB_stdScale) {
                        float factor;
                        if (this.is_LinearRGB_stdScale) {
                            red = this.fromsRGB8LUT16[red] & 0xFFFF;
                            grn = this.fromsRGB8LUT16[grn] & 0xFFFF;
                            blu = this.fromsRGB8LUT16[blu] & 0xFFFF;
                            factor = 1.5259022E-5f;
                        } else {
                            factor = 0.003921569f;
                        }
                        if (this.supportsAlpha) {
                            int alp = rgb >> 24 & 0xFF;
                            fdata[3] = (float)alp * 0.003921569f;
                            if (this.isAlphaPremultiplied) {
                                factor *= fdata[3];
                            }
                        }
                        fdata[0] = (float)red * factor;
                        fdata[1] = (float)grn * factor;
                        fdata[2] = (float)blu * factor;
                        return fdata;
                    } else if (this.is_LinearGray_stdScale) {
                        red = this.fromsRGB8LUT16[red] & 0xFFFF;
                        grn = this.fromsRGB8LUT16[grn] & 0xFFFF;
                        blu = this.fromsRGB8LUT16[blu] & 0xFFFF;
                        fdata[0] = (0.2125f * (float)red + 0.7154f * (float)grn + 0.0721f * (float)blu) / 65535.0f;
                        if (!this.supportsAlpha) return fdata;
                        int alp = rgb >> 24 & 0xFF;
                        fdata[1] = (float)alp * 0.003921569f;
                        if (!this.isAlphaPremultiplied) return fdata;
                        fdata[0] = fdata[0] * fdata[1];
                        return fdata;
                    } else if (this.is_ICCGray_stdScale) {
                        red = this.fromsRGB8LUT16[red] & 0xFFFF;
                        grn = this.fromsRGB8LUT16[grn] & 0xFFFF;
                        blu = this.fromsRGB8LUT16[blu] & 0xFFFF;
                        int gray = (int)(0.2125f * (float)red + 0.7154f * (float)grn + 0.0721f * (float)blu + 0.5f);
                        fdata[0] = (float)(this.fromLinearGray16ToOtherGray16LUT[gray] & 0xFFFF) / 65535.0f;
                        if (!this.supportsAlpha) return fdata;
                        int alp = rgb >> 24 & 0xFF;
                        fdata[1] = (float)alp * 0.003921569f;
                        if (!this.isAlphaPremultiplied) return fdata;
                        fdata[0] = fdata[0] * fdata[1];
                        return fdata;
                    } else {
                        int i;
                        float[] norm = new float[3];
                        float factor = 0.003921569f;
                        norm[0] = (float)red * factor;
                        norm[1] = (float)grn * factor;
                        norm[2] = (float)blu * factor;
                        norm = this.colorSpace.fromRGB(norm);
                        if (this.supportsAlpha) {
                            int alp = rgb >> 24 & 0xFF;
                            fdata[this.numColorComponents] = (float)alp * factor;
                            if (this.isAlphaPremultiplied) {
                                factor *= (float)alp;
                                i = 0;
                                while (i < this.numColorComponents) {
                                    int n = i++;
                                    norm[n] = norm[n] * factor;
                                }
                            }
                        }
                        for (i = 0; i < this.numColorComponents; ++i) {
                            fdata[i] = norm[i];
                        }
                    }
                    return fdata;
                }
                case 5: {
                    double[] ddata = pixel == null ? new double[this.numComponents] : (double[])pixel;
                    if (this.is_sRGB_stdScale || this.is_LinearRGB_stdScale) {
                        double factor;
                        if (this.is_LinearRGB_stdScale) {
                            red = this.fromsRGB8LUT16[red] & 0xFFFF;
                            grn = this.fromsRGB8LUT16[grn] & 0xFFFF;
                            blu = this.fromsRGB8LUT16[blu] & 0xFFFF;
                            factor = 1.5259021896696422E-5;
                        } else {
                            factor = 0.00392156862745098;
                        }
                        if (this.supportsAlpha) {
                            int alp = rgb >> 24 & 0xFF;
                            ddata[3] = (double)alp * 0.00392156862745098;
                            if (this.isAlphaPremultiplied) {
                                factor *= ddata[3];
                            }
                        }
                        ddata[0] = (double)red * factor;
                        ddata[1] = (double)grn * factor;
                        ddata[2] = (double)blu * factor;
                        return ddata;
                    } else if (this.is_LinearGray_stdScale) {
                        red = this.fromsRGB8LUT16[red] & 0xFFFF;
                        grn = this.fromsRGB8LUT16[grn] & 0xFFFF;
                        blu = this.fromsRGB8LUT16[blu] & 0xFFFF;
                        ddata[0] = (0.2125 * (double)red + 0.7154 * (double)grn + 0.0721 * (double)blu) / 65535.0;
                        if (!this.supportsAlpha) return ddata;
                        int alp = rgb >> 24 & 0xFF;
                        ddata[1] = (double)alp * 0.00392156862745098;
                        if (!this.isAlphaPremultiplied) return ddata;
                        ddata[0] = ddata[0] * ddata[1];
                        return ddata;
                    } else if (this.is_ICCGray_stdScale) {
                        red = this.fromsRGB8LUT16[red] & 0xFFFF;
                        grn = this.fromsRGB8LUT16[grn] & 0xFFFF;
                        blu = this.fromsRGB8LUT16[blu] & 0xFFFF;
                        int gray = (int)(0.2125f * (float)red + 0.7154f * (float)grn + 0.0721f * (float)blu + 0.5f);
                        ddata[0] = (double)(this.fromLinearGray16ToOtherGray16LUT[gray] & 0xFFFF) / 65535.0;
                        if (!this.supportsAlpha) return ddata;
                        int alp = rgb >> 24 & 0xFF;
                        ddata[1] = (double)alp * 0.00392156862745098;
                        if (!this.isAlphaPremultiplied) return ddata;
                        ddata[0] = ddata[0] * ddata[1];
                        return ddata;
                    } else {
                        int i;
                        float factor = 0.003921569f;
                        float[] norm = new float[]{(float)red * factor, (float)grn * factor, (float)blu * factor};
                        norm = this.colorSpace.fromRGB(norm);
                        if (this.supportsAlpha) {
                            int alp = rgb >> 24 & 0xFF;
                            ddata[this.numColorComponents] = (double)alp * 0.00392156862745098;
                            if (this.isAlphaPremultiplied) {
                                factor *= (float)alp;
                                i = 0;
                                while (i < this.numColorComponents) {
                                    int n = i++;
                                    norm[n] = norm[n] * factor;
                                }
                            }
                        }
                        for (i = 0; i < this.numColorComponents; ++i) {
                            ddata[i] = norm[i];
                        }
                    }
                    return ddata;
                }
            }
        }
        int[] intpixel = this.transferType == 3 && pixel != null ? (int[])pixel : new int[this.numComponents];
        if (this.is_sRGB_stdScale || this.is_LinearRGB_stdScale) {
            int precision;
            if (this.is_LinearRGB_stdScale) {
                if (this.transferType == 0) {
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
                alp = rgb >> 24 & 0xFF;
                intpixel[3] = this.nBits[3] == 8 ? alp : (int)((float)alp * 0.003921569f * (float)((1 << this.nBits[3]) - 1) + 0.5f);
                if (this.isAlphaPremultiplied) {
                    factor *= (float)alp * 0.003921569f;
                    precision = -1;
                }
            }
            intpixel[0] = this.nBits[0] == precision ? red : (int)((float)red * factor * (float)((1 << this.nBits[0]) - 1) + 0.5f);
            intpixel[1] = this.nBits[1] == precision ? grn : (int)((float)grn * factor * (float)((1 << this.nBits[1]) - 1) + 0.5f);
            intpixel[2] = this.nBits[2] == precision ? blu : (int)((float)blu * factor * (float)((1 << this.nBits[2]) - 1) + 0.5f);
        } else if (this.is_LinearGray_stdScale) {
            red = this.fromsRGB8LUT16[red] & 0xFFFF;
            grn = this.fromsRGB8LUT16[grn] & 0xFFFF;
            blu = this.fromsRGB8LUT16[blu] & 0xFFFF;
            float gray = (0.2125f * (float)red + 0.7154f * (float)grn + 0.0721f * (float)blu) / 65535.0f;
            if (this.supportsAlpha) {
                alp = rgb >> 24 & 0xFF;
                intpixel[1] = this.nBits[1] == 8 ? alp : (int)((float)alp * 0.003921569f * (float)((1 << this.nBits[1]) - 1) + 0.5f);
                if (this.isAlphaPremultiplied) {
                    gray *= (float)alp * 0.003921569f;
                }
            }
            intpixel[0] = (int)(gray * (float)((1 << this.nBits[0]) - 1) + 0.5f);
        } else if (this.is_ICCGray_stdScale) {
            red = this.fromsRGB8LUT16[red] & 0xFFFF;
            grn = this.fromsRGB8LUT16[grn] & 0xFFFF;
            blu = this.fromsRGB8LUT16[blu] & 0xFFFF;
            int gray16 = (int)(0.2125f * (float)red + 0.7154f * (float)grn + 0.0721f * (float)blu + 0.5f);
            float gray = (float)(this.fromLinearGray16ToOtherGray16LUT[gray16] & 0xFFFF) / 65535.0f;
            if (this.supportsAlpha) {
                alp = rgb >> 24 & 0xFF;
                intpixel[1] = this.nBits[1] == 8 ? alp : (int)((float)alp * 0.003921569f * (float)((1 << this.nBits[1]) - 1) + 0.5f);
                if (this.isAlphaPremultiplied) {
                    gray *= (float)alp * 0.003921569f;
                }
            }
            intpixel[0] = (int)(gray * (float)((1 << this.nBits[0]) - 1) + 0.5f);
        } else {
            int i;
            float[] norm = new float[3];
            factor = 0.003921569f;
            norm[0] = (float)red * factor;
            norm[1] = (float)grn * factor;
            norm[2] = (float)blu * factor;
            norm = this.colorSpace.fromRGB(norm);
            if (this.nonStdScale) {
                for (i = 0; i < this.numColorComponents; ++i) {
                    norm[i] = (norm[i] - this.compOffset[i]) * this.compScale[i];
                    if (norm[i] < 0.0f) {
                        norm[i] = 0.0f;
                    }
                    if (!(norm[i] > 1.0f)) continue;
                    norm[i] = 1.0f;
                }
            }
            if (this.supportsAlpha) {
                alp = rgb >> 24 & 0xFF;
                intpixel[this.numColorComponents] = this.nBits[this.numColorComponents] == 8 ? alp : (int)((float)alp * factor * (float)((1 << this.nBits[this.numColorComponents]) - 1) + 0.5f);
                if (this.isAlphaPremultiplied) {
                    factor *= (float)alp;
                    i = 0;
                    while (i < this.numColorComponents) {
                        int n = i++;
                        norm[n] = norm[n] * factor;
                    }
                }
            }
            for (i = 0; i < this.numColorComponents; ++i) {
                intpixel[i] = (int)(norm[i] * (float)((1 << this.nBits[i]) - 1) + 0.5f);
            }
        }
        switch (this.transferType) {
            case 0: {
                byte[] bdata = pixel == null ? new byte[this.numComponents] : (byte[])pixel;
                for (int i = 0; i < this.numComponents; ++i) {
                    bdata[i] = (byte)(0xFF & intpixel[i]);
                }
                return bdata;
            }
            case 1: {
                short[] sdata = pixel == null ? new short[this.numComponents] : (short[])pixel;
                for (int i = 0; i < this.numComponents; ++i) {
                    sdata[i] = (short)(intpixel[i] & 0xFFFF);
                }
                return sdata;
            }
            case 3: {
                if (this.maxBits <= 23) return intpixel;
                for (int i = 0; i < this.numComponents; ++i) {
                    if (intpixel[i] <= (1 << this.nBits[i]) - 1) continue;
                    intpixel[i] = (1 << this.nBits[i]) - 1;
                }
                return intpixel;
            }
        }
        throw new IllegalArgumentException("This method has not been implemented for transferType " + this.transferType);
    }

    @Override
    public int[] getComponents(int pixel, int[] components, int offset) {
        if (this.numComponents > 1) {
            throw new IllegalArgumentException("More than one component per pixel");
        }
        if (this.needScaleInit) {
            this.initScale();
        }
        if (this.noUnnorm) {
            throw new IllegalArgumentException("This ColorModel does not support the unnormalized form");
        }
        if (components == null) {
            components = new int[offset + 1];
        }
        components[offset + 0] = pixel & (1 << this.nBits[0]) - 1;
        return components;
    }

    @Override
    public int[] getComponents(Object pixel, int[] components, int offset) {
        int[] intpixel;
        if (this.needScaleInit) {
            this.initScale();
        }
        if (this.noUnnorm) {
            throw new IllegalArgumentException("This ColorModel does not support the unnormalized form");
        }
        if (pixel instanceof int[]) {
            intpixel = (int[])pixel;
        } else {
            intpixel = DataBuffer.toIntArray(pixel);
            if (intpixel == null) {
                throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
            }
        }
        if (intpixel.length < this.numComponents) {
            throw new IllegalArgumentException("Length of pixel array < number of components in model");
        }
        if (components == null) {
            components = new int[offset + this.numComponents];
        } else if (components.length - offset < this.numComponents) {
            throw new IllegalArgumentException("Length of components array < number of components in model");
        }
        System.arraycopy(intpixel, 0, components, offset, this.numComponents);
        return components;
    }

    @Override
    public int[] getUnnormalizedComponents(float[] normComponents, int normOffset, int[] components, int offset) {
        if (this.needScaleInit) {
            this.initScale();
        }
        if (this.noUnnorm) {
            throw new IllegalArgumentException("This ColorModel does not support the unnormalized form");
        }
        return super.getUnnormalizedComponents(normComponents, normOffset, components, offset);
    }

    @Override
    public float[] getNormalizedComponents(int[] components, int offset, float[] normComponents, int normOffset) {
        if (this.needScaleInit) {
            this.initScale();
        }
        if (this.noUnnorm) {
            throw new IllegalArgumentException("This ColorModel does not support the unnormalized form");
        }
        return super.getNormalizedComponents(components, offset, normComponents, normOffset);
    }

    @Override
    public int getDataElement(int[] components, int offset) {
        if (this.needScaleInit) {
            this.initScale();
        }
        if (this.numComponents == 1) {
            if (this.noUnnorm) {
                throw new IllegalArgumentException("This ColorModel does not support the unnormalized form");
            }
            return components[offset + 0];
        }
        throw new IllegalArgumentException("This model returns " + this.numComponents + " elements in the pixel array.");
    }

    @Override
    public Object getDataElements(int[] components, int offset, Object obj) {
        if (this.needScaleInit) {
            this.initScale();
        }
        if (this.noUnnorm) {
            throw new IllegalArgumentException("This ColorModel does not support the unnormalized form");
        }
        if (components.length - offset < this.numComponents) {
            throw new IllegalArgumentException("Component array too small (should be " + this.numComponents);
        }
        switch (this.transferType) {
            case 3: {
                int[] pixel = obj == null ? new int[this.numComponents] : (int[])obj;
                System.arraycopy(components, offset, pixel, 0, this.numComponents);
                return pixel;
            }
            case 0: {
                byte[] pixel = obj == null ? new byte[this.numComponents] : (byte[])obj;
                for (int i = 0; i < this.numComponents; ++i) {
                    pixel[i] = (byte)(components[offset + i] & 0xFF);
                }
                return pixel;
            }
            case 1: {
                short[] pixel = obj == null ? new short[this.numComponents] : (short[])obj;
                for (int i = 0; i < this.numComponents; ++i) {
                    pixel[i] = (short)(components[offset + i] & 0xFFFF);
                }
                return pixel;
            }
        }
        throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
    }

    @Override
    public int getDataElement(float[] normComponents, int normOffset) {
        if (this.numComponents > 1) {
            throw new IllegalArgumentException("More than one component per pixel");
        }
        if (this.signed) {
            throw new IllegalArgumentException("Component value is signed");
        }
        if (this.needScaleInit) {
            this.initScale();
        }
        Object pixel = this.getDataElements(normComponents, normOffset, null);
        switch (this.transferType) {
            case 0: {
                byte[] bpixel = (byte[])pixel;
                return bpixel[0] & 0xFF;
            }
            case 1: {
                short[] uspixel = (short[])pixel;
                return uspixel[0] & 0xFFFF;
            }
            case 3: {
                int[] ipixel = (int[])pixel;
                return ipixel[0];
            }
        }
        throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
    }

    @Override
    public Object getDataElements(float[] normComponents, int normOffset, Object obj) {
        float[] stdNormComponents;
        boolean needAlpha;
        boolean bl = needAlpha = this.supportsAlpha && this.isAlphaPremultiplied;
        if (this.needScaleInit) {
            this.initScale();
        }
        if (this.nonStdScale) {
            stdNormComponents = new float[this.numComponents];
            int c = 0;
            int nc = normOffset;
            while (c < this.numColorComponents) {
                stdNormComponents[c] = (normComponents[nc] - this.compOffset[c]) * this.compScale[c];
                if (stdNormComponents[c] < 0.0f) {
                    stdNormComponents[c] = 0.0f;
                }
                if (stdNormComponents[c] > 1.0f) {
                    stdNormComponents[c] = 1.0f;
                }
                ++c;
                ++nc;
            }
            if (this.supportsAlpha) {
                stdNormComponents[this.numColorComponents] = normComponents[this.numColorComponents + normOffset];
            }
            normOffset = 0;
        } else {
            stdNormComponents = normComponents;
        }
        switch (this.transferType) {
            case 0: {
                byte[] bpixel = obj == null ? new byte[this.numComponents] : (byte[])obj;
                if (needAlpha) {
                    float alpha = stdNormComponents[this.numColorComponents + normOffset];
                    int c = 0;
                    int nc = normOffset;
                    while (c < this.numColorComponents) {
                        bpixel[c] = (byte)(stdNormComponents[nc] * alpha * (float)((1 << this.nBits[c]) - 1) + 0.5f);
                        ++c;
                        ++nc;
                    }
                    bpixel[this.numColorComponents] = (byte)(alpha * (float)((1 << this.nBits[this.numColorComponents]) - 1) + 0.5f);
                } else {
                    int c = 0;
                    int nc = normOffset;
                    while (c < this.numComponents) {
                        bpixel[c] = (byte)(stdNormComponents[nc] * (float)((1 << this.nBits[c]) - 1) + 0.5f);
                        ++c;
                        ++nc;
                    }
                }
                return bpixel;
            }
            case 1: {
                short[] uspixel = obj == null ? new short[this.numComponents] : (short[])obj;
                if (needAlpha) {
                    float alpha = stdNormComponents[this.numColorComponents + normOffset];
                    int c = 0;
                    int nc = normOffset;
                    while (c < this.numColorComponents) {
                        uspixel[c] = (short)(stdNormComponents[nc] * alpha * (float)((1 << this.nBits[c]) - 1) + 0.5f);
                        ++c;
                        ++nc;
                    }
                    uspixel[this.numColorComponents] = (short)(alpha * (float)((1 << this.nBits[this.numColorComponents]) - 1) + 0.5f);
                } else {
                    int c = 0;
                    int nc = normOffset;
                    while (c < this.numComponents) {
                        uspixel[c] = (short)(stdNormComponents[nc] * (float)((1 << this.nBits[c]) - 1) + 0.5f);
                        ++c;
                        ++nc;
                    }
                }
                return uspixel;
            }
            case 3: {
                int[] ipixel = obj == null ? new int[this.numComponents] : (int[])obj;
                if (needAlpha) {
                    float alpha = stdNormComponents[this.numColorComponents + normOffset];
                    int c = 0;
                    int nc = normOffset;
                    while (c < this.numColorComponents) {
                        ipixel[c] = (int)(stdNormComponents[nc] * alpha * (float)((1 << this.nBits[c]) - 1) + 0.5f);
                        ++c;
                        ++nc;
                    }
                    ipixel[this.numColorComponents] = (int)(alpha * (float)((1 << this.nBits[this.numColorComponents]) - 1) + 0.5f);
                } else {
                    int c = 0;
                    int nc = normOffset;
                    while (c < this.numComponents) {
                        ipixel[c] = (int)(stdNormComponents[nc] * (float)((1 << this.nBits[c]) - 1) + 0.5f);
                        ++c;
                        ++nc;
                    }
                }
                return ipixel;
            }
            case 2: {
                short[] spixel = obj == null ? new short[this.numComponents] : (short[])obj;
                if (needAlpha) {
                    float alpha = stdNormComponents[this.numColorComponents + normOffset];
                    int c = 0;
                    int nc = normOffset;
                    while (c < this.numColorComponents) {
                        spixel[c] = (short)(stdNormComponents[nc] * alpha * 32767.0f + 0.5f);
                        ++c;
                        ++nc;
                    }
                    spixel[this.numColorComponents] = (short)(alpha * 32767.0f + 0.5f);
                } else {
                    int c = 0;
                    int nc = normOffset;
                    while (c < this.numComponents) {
                        spixel[c] = (short)(stdNormComponents[nc] * 32767.0f + 0.5f);
                        ++c;
                        ++nc;
                    }
                }
                return spixel;
            }
            case 4: {
                float[] fpixel = obj == null ? new float[this.numComponents] : (float[])obj;
                if (needAlpha) {
                    float alpha = normComponents[this.numColorComponents + normOffset];
                    int c = 0;
                    int nc = normOffset;
                    while (c < this.numColorComponents) {
                        fpixel[c] = normComponents[nc] * alpha;
                        ++c;
                        ++nc;
                    }
                    fpixel[this.numColorComponents] = alpha;
                } else {
                    int c = 0;
                    int nc = normOffset;
                    while (c < this.numComponents) {
                        fpixel[c] = normComponents[nc];
                        ++c;
                        ++nc;
                    }
                }
                return fpixel;
            }
            case 5: {
                double[] dpixel = obj == null ? new double[this.numComponents] : (double[])obj;
                if (needAlpha) {
                    double alpha = normComponents[this.numColorComponents + normOffset];
                    int c = 0;
                    int nc = normOffset;
                    while (c < this.numColorComponents) {
                        dpixel[c] = (double)normComponents[nc] * alpha;
                        ++c;
                        ++nc;
                    }
                    dpixel[this.numColorComponents] = alpha;
                } else {
                    int c = 0;
                    int nc = normOffset;
                    while (c < this.numComponents) {
                        dpixel[c] = normComponents[nc];
                        ++c;
                        ++nc;
                    }
                }
                return dpixel;
            }
        }
        throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
    }

    @Override
    public float[] getNormalizedComponents(Object pixel, float[] normComponents, int normOffset) {
        float alpha;
        if (normComponents == null) {
            normComponents = new float[this.numComponents + normOffset];
        }
        switch (this.transferType) {
            case 0: {
                byte[] bpixel = (byte[])pixel;
                int c = 0;
                int nc = normOffset;
                while (c < this.numComponents) {
                    normComponents[nc] = (float)(bpixel[c] & 0xFF) / (float)((1 << this.nBits[c]) - 1);
                    ++c;
                    ++nc;
                }
                break;
            }
            case 1: {
                short[] uspixel = (short[])pixel;
                int c = 0;
                int nc = normOffset;
                while (c < this.numComponents) {
                    normComponents[nc] = (float)(uspixel[c] & 0xFFFF) / (float)((1 << this.nBits[c]) - 1);
                    ++c;
                    ++nc;
                }
                break;
            }
            case 3: {
                int[] ipixel = (int[])pixel;
                int c = 0;
                int nc = normOffset;
                while (c < this.numComponents) {
                    normComponents[nc] = (float)ipixel[c] / (float)((1 << this.nBits[c]) - 1);
                    ++c;
                    ++nc;
                }
                break;
            }
            case 2: {
                short[] spixel = (short[])pixel;
                int c = 0;
                int nc = normOffset;
                while (c < this.numComponents) {
                    normComponents[nc] = (float)spixel[c] / 32767.0f;
                    ++c;
                    ++nc;
                }
                break;
            }
            case 4: {
                float[] fpixel = (float[])pixel;
                int c = 0;
                int nc = normOffset;
                while (c < this.numComponents) {
                    normComponents[nc] = fpixel[c];
                    ++c;
                    ++nc;
                }
                break;
            }
            case 5: {
                double[] dpixel = (double[])pixel;
                int c = 0;
                int nc = normOffset;
                while (c < this.numComponents) {
                    normComponents[nc] = (float)dpixel[c];
                    ++c;
                    ++nc;
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
            }
        }
        if (this.supportsAlpha && this.isAlphaPremultiplied && (alpha = normComponents[this.numColorComponents + normOffset]) != 0.0f) {
            float invAlpha = 1.0f / alpha;
            int c = normOffset;
            while (c < this.numColorComponents + normOffset) {
                int n = c++;
                normComponents[n] = normComponents[n] * invAlpha;
            }
        }
        if (this.min != null) {
            for (int c = 0; c < this.numColorComponents; ++c) {
                normComponents[c + normOffset] = this.min[c] + this.diffMinMax[c] * normComponents[c + normOffset];
            }
        }
        return normComponents;
    }

    @Override
    public ColorModel coerceData(WritableRaster raster, boolean isAlphaPremultiplied) {
        block79: {
            int rY;
            int rminX;
            int aIdx;
            int h;
            int w;
            block78: {
                if (!this.supportsAlpha || this.isAlphaPremultiplied == isAlphaPremultiplied) {
                    return this;
                }
                w = raster.getWidth();
                h = raster.getHeight();
                aIdx = raster.getNumBands() - 1;
                rminX = raster.getMinX();
                rY = raster.getMinY();
                if (!isAlphaPremultiplied) break block78;
                switch (this.transferType) {
                    case 0: {
                        byte[] pixel = null;
                        byte[] zpixel = null;
                        float alphaScale = 1.0f / (float)((1 << this.nBits[aIdx]) - 1);
                        int y = 0;
                        while (y < h) {
                            int rX = rminX;
                            int x = 0;
                            while (x < w) {
                                float normAlpha = (float)((pixel = (byte[])raster.getDataElements(rX, rY, pixel))[aIdx] & 0xFF) * alphaScale;
                                if (normAlpha != 0.0f) {
                                    for (int c = 0; c < aIdx; ++c) {
                                        pixel[c] = (byte)((float)(pixel[c] & 0xFF) * normAlpha + 0.5f);
                                    }
                                    raster.setDataElements(rX, rY, pixel);
                                } else {
                                    if (zpixel == null) {
                                        zpixel = new byte[this.numComponents];
                                    }
                                    raster.setDataElements(rX, rY, zpixel);
                                }
                                ++x;
                                ++rX;
                            }
                            ++y;
                            ++rY;
                        }
                        break block79;
                    }
                    case 1: {
                        short[] pixel = null;
                        short[] zpixel = null;
                        float alphaScale = 1.0f / (float)((1 << this.nBits[aIdx]) - 1);
                        int y = 0;
                        while (y < h) {
                            int rX = rminX;
                            int x = 0;
                            while (x < w) {
                                float normAlpha = (float)((pixel = (short[])raster.getDataElements(rX, rY, pixel))[aIdx] & 0xFFFF) * alphaScale;
                                if (normAlpha != 0.0f) {
                                    for (int c = 0; c < aIdx; ++c) {
                                        pixel[c] = (short)((float)(pixel[c] & 0xFFFF) * normAlpha + 0.5f);
                                    }
                                    raster.setDataElements(rX, rY, pixel);
                                } else {
                                    if (zpixel == null) {
                                        zpixel = new short[this.numComponents];
                                    }
                                    raster.setDataElements(rX, rY, zpixel);
                                }
                                ++x;
                                ++rX;
                            }
                            ++y;
                            ++rY;
                        }
                        break block79;
                    }
                    case 3: {
                        int[] pixel = null;
                        int[] zpixel = null;
                        float alphaScale = 1.0f / (float)((1 << this.nBits[aIdx]) - 1);
                        int y = 0;
                        while (y < h) {
                            int rX = rminX;
                            int x = 0;
                            while (x < w) {
                                float normAlpha = (float)(pixel = (int[])raster.getDataElements(rX, rY, pixel))[aIdx] * alphaScale;
                                if (normAlpha != 0.0f) {
                                    for (int c = 0; c < aIdx; ++c) {
                                        pixel[c] = (int)((float)pixel[c] * normAlpha + 0.5f);
                                    }
                                    raster.setDataElements(rX, rY, pixel);
                                } else {
                                    if (zpixel == null) {
                                        zpixel = new int[this.numComponents];
                                    }
                                    raster.setDataElements(rX, rY, zpixel);
                                }
                                ++x;
                                ++rX;
                            }
                            ++y;
                            ++rY;
                        }
                        break block79;
                    }
                    case 2: {
                        short[] pixel = null;
                        short[] zpixel = null;
                        float alphaScale = 3.051851E-5f;
                        int y = 0;
                        while (y < h) {
                            int rX = rminX;
                            int x = 0;
                            while (x < w) {
                                float normAlpha = (float)(pixel = (short[])raster.getDataElements(rX, rY, pixel))[aIdx] * alphaScale;
                                if (normAlpha != 0.0f) {
                                    for (int c = 0; c < aIdx; ++c) {
                                        pixel[c] = (short)((float)pixel[c] * normAlpha + 0.5f);
                                    }
                                    raster.setDataElements(rX, rY, pixel);
                                } else {
                                    if (zpixel == null) {
                                        zpixel = new short[this.numComponents];
                                    }
                                    raster.setDataElements(rX, rY, zpixel);
                                }
                                ++x;
                                ++rX;
                            }
                            ++y;
                            ++rY;
                        }
                        break block79;
                    }
                    case 4: {
                        float[] pixel = null;
                        float[] zpixel = null;
                        int y = 0;
                        while (y < h) {
                            int rX = rminX;
                            int x = 0;
                            while (x < w) {
                                float normAlpha = (pixel = (float[])raster.getDataElements(rX, rY, pixel))[aIdx];
                                if (normAlpha != 0.0f) {
                                    int c = 0;
                                    while (c < aIdx) {
                                        int n = c++;
                                        pixel[n] = pixel[n] * normAlpha;
                                    }
                                    raster.setDataElements(rX, rY, pixel);
                                } else {
                                    if (zpixel == null) {
                                        zpixel = new float[this.numComponents];
                                    }
                                    raster.setDataElements(rX, rY, zpixel);
                                }
                                ++x;
                                ++rX;
                            }
                            ++y;
                            ++rY;
                        }
                        break block79;
                    }
                    case 5: {
                        double[] pixel = null;
                        double[] zpixel = null;
                        int y = 0;
                        while (y < h) {
                            int rX = rminX;
                            int x = 0;
                            while (x < w) {
                                double dnormAlpha = (pixel = (double[])raster.getDataElements(rX, rY, pixel))[aIdx];
                                if (dnormAlpha != 0.0) {
                                    int c = 0;
                                    while (c < aIdx) {
                                        int n = c++;
                                        pixel[n] = pixel[n] * dnormAlpha;
                                    }
                                    raster.setDataElements(rX, rY, pixel);
                                } else {
                                    if (zpixel == null) {
                                        zpixel = new double[this.numComponents];
                                    }
                                    raster.setDataElements(rX, rY, zpixel);
                                }
                                ++x;
                                ++rX;
                            }
                            ++y;
                            ++rY;
                        }
                        break block79;
                    }
                    default: {
                        throw new UnsupportedOperationException("This method has not been implemented for transferType " + this.transferType);
                    }
                }
            }
            switch (this.transferType) {
                case 0: {
                    byte[] pixel = null;
                    float alphaScale = 1.0f / (float)((1 << this.nBits[aIdx]) - 1);
                    int y = 0;
                    while (y < h) {
                        int rX = rminX;
                        int x = 0;
                        while (x < w) {
                            float normAlpha = (float)((pixel = (byte[])raster.getDataElements(rX, rY, pixel))[aIdx] & 0xFF) * alphaScale;
                            if (normAlpha != 0.0f) {
                                float invAlpha = 1.0f / normAlpha;
                                for (int c = 0; c < aIdx; ++c) {
                                    pixel[c] = (byte)((float)(pixel[c] & 0xFF) * invAlpha + 0.5f);
                                }
                                raster.setDataElements(rX, rY, pixel);
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
                    short[] pixel = null;
                    float alphaScale = 1.0f / (float)((1 << this.nBits[aIdx]) - 1);
                    int y = 0;
                    while (y < h) {
                        int rX = rminX;
                        int x = 0;
                        while (x < w) {
                            float normAlpha = (float)((pixel = (short[])raster.getDataElements(rX, rY, pixel))[aIdx] & 0xFFFF) * alphaScale;
                            if (normAlpha != 0.0f) {
                                float invAlpha = 1.0f / normAlpha;
                                for (int c = 0; c < aIdx; ++c) {
                                    pixel[c] = (short)((float)(pixel[c] & 0xFFFF) * invAlpha + 0.5f);
                                }
                                raster.setDataElements(rX, rY, pixel);
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
                    int[] pixel = null;
                    float alphaScale = 1.0f / (float)((1 << this.nBits[aIdx]) - 1);
                    int y = 0;
                    while (y < h) {
                        int rX = rminX;
                        int x = 0;
                        while (x < w) {
                            float normAlpha = (float)(pixel = (int[])raster.getDataElements(rX, rY, pixel))[aIdx] * alphaScale;
                            if (normAlpha != 0.0f) {
                                float invAlpha = 1.0f / normAlpha;
                                for (int c = 0; c < aIdx; ++c) {
                                    pixel[c] = (int)((float)pixel[c] * invAlpha + 0.5f);
                                }
                                raster.setDataElements(rX, rY, pixel);
                            }
                            ++x;
                            ++rX;
                        }
                        ++y;
                        ++rY;
                    }
                    break;
                }
                case 2: {
                    short[] pixel = null;
                    float alphaScale = 3.051851E-5f;
                    int y = 0;
                    while (y < h) {
                        int rX = rminX;
                        int x = 0;
                        while (x < w) {
                            float normAlpha = (float)(pixel = (short[])raster.getDataElements(rX, rY, pixel))[aIdx] * alphaScale;
                            if (normAlpha != 0.0f) {
                                float invAlpha = 1.0f / normAlpha;
                                for (int c = 0; c < aIdx; ++c) {
                                    pixel[c] = (short)((float)pixel[c] * invAlpha + 0.5f);
                                }
                                raster.setDataElements(rX, rY, pixel);
                            }
                            ++x;
                            ++rX;
                        }
                        ++y;
                        ++rY;
                    }
                    break;
                }
                case 4: {
                    float[] pixel = null;
                    int y = 0;
                    while (y < h) {
                        int rX = rminX;
                        int x = 0;
                        while (x < w) {
                            float normAlpha = (pixel = (float[])raster.getDataElements(rX, rY, pixel))[aIdx];
                            if (normAlpha != 0.0f) {
                                float invAlpha = 1.0f / normAlpha;
                                int c = 0;
                                while (c < aIdx) {
                                    int n = c++;
                                    pixel[n] = pixel[n] * invAlpha;
                                }
                                raster.setDataElements(rX, rY, pixel);
                            }
                            ++x;
                            ++rX;
                        }
                        ++y;
                        ++rY;
                    }
                    break;
                }
                case 5: {
                    double[] pixel = null;
                    int y = 0;
                    while (y < h) {
                        int rX = rminX;
                        int x = 0;
                        while (x < w) {
                            double dnormAlpha = (pixel = (double[])raster.getDataElements(rX, rY, pixel))[aIdx];
                            if (dnormAlpha != 0.0) {
                                double invAlpha = 1.0 / dnormAlpha;
                                int c = 0;
                                while (c < aIdx) {
                                    int n = c++;
                                    pixel[n] = pixel[n] * invAlpha;
                                }
                                raster.setDataElements(rX, rY, pixel);
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
        if (!this.signed) {
            return new ComponentColorModel(this.colorSpace, this.nBits, this.supportsAlpha, isAlphaPremultiplied, this.transparency, this.transferType);
        }
        return new ComponentColorModel(this.colorSpace, this.supportsAlpha, isAlphaPremultiplied, this.transparency, this.transferType);
    }

    @Override
    public boolean isCompatibleRaster(Raster raster) {
        SampleModel sm = raster.getSampleModel();
        if (sm instanceof ComponentSampleModel) {
            if (sm.getNumBands() != this.getNumComponents()) {
                return false;
            }
            for (int i = 0; i < this.nBits.length; ++i) {
                if (sm.getSampleSize(i) >= this.nBits[i]) continue;
                return false;
            }
            return raster.getTransferType() == this.transferType;
        }
        return false;
    }

    @Override
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        int dataSize = w * h * this.numComponents;
        WritableRaster raster = null;
        switch (this.transferType) {
            case 0: 
            case 1: {
                raster = Raster.createInterleavedRaster(this.transferType, w, h, this.numComponents, null);
                break;
            }
            default: {
                SampleModel sm = this.createCompatibleSampleModel(w, h);
                DataBuffer db = sm.createDataBuffer();
                raster = Raster.createWritableRaster(sm, db, null);
            }
        }
        return raster;
    }

    @Override
    public SampleModel createCompatibleSampleModel(int w, int h) {
        int[] bandOffsets = new int[this.numComponents];
        for (int i = 0; i < this.numComponents; ++i) {
            bandOffsets[i] = i;
        }
        switch (this.transferType) {
            case 0: 
            case 1: {
                return new PixelInterleavedSampleModel(this.transferType, w, h, this.numComponents, w * this.numComponents, bandOffsets);
            }
        }
        return new ComponentSampleModel(this.transferType, w, h, this.numComponents, w * this.numComponents, bandOffsets);
    }

    @Override
    public boolean isCompatibleSampleModel(SampleModel sm) {
        if (!(sm instanceof ComponentSampleModel)) {
            return false;
        }
        if (this.numComponents != sm.getNumBands()) {
            return false;
        }
        return sm.getTransferType() == this.transferType;
    }

    @Override
    public WritableRaster getAlphaRaster(WritableRaster raster) {
        if (!this.hasAlpha()) {
            return null;
        }
        int x = raster.getMinX();
        int y = raster.getMinY();
        int[] band = new int[]{raster.getNumBands() - 1};
        return raster.createWritableChild(x, y, raster.getWidth(), raster.getHeight(), x, y, band);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ComponentColorModel)) {
            return false;
        }
        ComponentColorModel cm = (ComponentColorModel)obj;
        if (this.supportsAlpha != cm.hasAlpha() || this.isAlphaPremultiplied != cm.isAlphaPremultiplied() || this.pixel_bits != cm.getPixelSize() || this.transparency != cm.getTransparency() || this.numComponents != cm.getNumComponents() || !this.colorSpace.equals(cm.colorSpace) || this.transferType != cm.transferType) {
            return false;
        }
        return Arrays.equals(this.nBits, cm.getComponentSize());
    }

    @Override
    public int hashCode() {
        int result = this.hashCode;
        if (result == 0) {
            result = 7;
            result = 89 * result + this.pixel_bits;
            result = 89 * result + Arrays.hashCode(this.nBits);
            result = 89 * result + this.transparency;
            result = 89 * result + (this.supportsAlpha ? 1 : 0);
            result = 89 * result + (this.isAlphaPremultiplied ? 1 : 0);
            result = 89 * result + this.numComponents;
            result = 89 * result + this.colorSpace.hashCode();
            this.hashCode = result = 89 * result + this.transferType;
        }
        return result;
    }
}

