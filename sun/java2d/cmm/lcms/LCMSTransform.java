/*
 * Decompiled with CFR 0.152.
 */
package sun.java2d.cmm.lcms;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.lang.ref.Reference;
import sun.awt.AWTAccessor;
import sun.java2d.cmm.ColorTransform;
import sun.java2d.cmm.lcms.LCMS;
import sun.java2d.cmm.lcms.LCMSImageLayout;
import sun.java2d.cmm.lcms.LCMSProfile;

final class LCMSTransform
implements ColorTransform {
    private volatile NativeTransform transform;
    private final LCMSProfile[] lcmsProfiles;
    private final int renderingIntent;
    private final int numInComponents;
    private final int numOutComponents;

    LCMSTransform(int renderingIntent, ICC_Profile ... profiles) {
        AWTAccessor.ICC_ProfileAccessor acc = AWTAccessor.getICC_ProfileAccessor();
        this.lcmsProfiles = new LCMSProfile[profiles.length];
        for (int i = 0; i < profiles.length; ++i) {
            this.lcmsProfiles[i] = LCMS.getLcmsProfile(acc.cmmProfile(profiles[i]));
            profiles[i].getNumComponents();
        }
        this.renderingIntent = renderingIntent == -1 ? 0 : renderingIntent;
        this.numInComponents = profiles[0].getNumComponents();
        this.numOutComponents = profiles[profiles.length - 1].getNumComponents();
    }

    @Override
    public int getNumInComponents() {
        return this.numInComponents;
    }

    @Override
    public int getNumOutComponents() {
        return this.numOutComponents;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void doTransform(LCMSImageLayout in, LCMSImageLayout out) {
        NativeTransform tfm = this.transform;
        if (tfm == null || !tfm.match(in, out)) {
            LCMSTransform lCMSTransform = this;
            synchronized (lCMSTransform) {
                tfm = this.transform;
                if (tfm == null || !tfm.match(in, out)) {
                    tfm = new NativeTransform();
                    tfm.inFormatter = in.pixelType;
                    tfm.outFormatter = out.pixelType;
                    tfm.ID = LCMS.createTransform(this.lcmsProfiles, this.renderingIntent, tfm.inFormatter, tfm.outFormatter, tfm);
                    this.transform = tfm;
                }
            }
        }
        LCMS.colorConvert(tfm.ID, in.width, in.height, in.offset, in.nextRowOffset, out.offset, out.nextRowOffset, in.dataArray, out.dataArray, in.dataType, out.dataType);
        Reference.reachabilityFence(tfm);
    }

    private static boolean isLCMSSupport(BufferedImage src, BufferedImage dst) {
        boolean dstAlpha = dst.getColorModel().hasAlpha();
        boolean srcAlpha = src.getColorModel().hasAlpha();
        boolean srcPre = srcAlpha && src.getColorModel().isAlphaPremultiplied();
        return !dstAlpha && !srcPre || dstAlpha == srcAlpha;
    }

    @Override
    public void colorConvert(BufferedImage src, BufferedImage dst) {
        int i;
        LCMSImageLayout srcIL;
        LCMSImageLayout dstIL;
        if (LCMSTransform.isLCMSSupport(src, dst) && (dstIL = LCMSImageLayout.createImageLayout(dst)) != null && (srcIL = LCMSImageLayout.createImageLayout(src)) != null) {
            this.doTransform(srcIL, dstIL);
            return;
        }
        WritableRaster srcRas = src.getRaster();
        WritableRaster dstRas = dst.getRaster();
        ColorModel srcCM = src.getColorModel();
        ColorModel dstCM = dst.getColorModel();
        int w = src.getWidth();
        int h = src.getHeight();
        int srcNumComp = srcCM.getNumColorComponents();
        int dstNumComp = dstCM.getNumColorComponents();
        int precision = 8;
        float maxNum = 255.0f;
        for (i = 0; i < srcNumComp; ++i) {
            if (srcCM.getComponentSize(i) <= 8) continue;
            precision = 16;
            maxNum = 65535.0f;
        }
        for (i = 0; i < dstNumComp; ++i) {
            if (dstCM.getComponentSize(i) <= 8) continue;
            precision = 16;
            maxNum = 65535.0f;
        }
        float[] srcMinVal = new float[srcNumComp];
        float[] srcInvDiffMinMax = new float[srcNumComp];
        ColorSpace cs = srcCM.getColorSpace();
        for (int i2 = 0; i2 < srcNumComp; ++i2) {
            srcMinVal[i2] = cs.getMinValue(i2);
            srcInvDiffMinMax[i2] = maxNum / (cs.getMaxValue(i2) - srcMinVal[i2]);
        }
        cs = dstCM.getColorSpace();
        float[] dstMinVal = new float[dstNumComp];
        float[] dstDiffMinMax = new float[dstNumComp];
        for (int i3 = 0; i3 < dstNumComp; ++i3) {
            dstMinVal[i3] = cs.getMinValue(i3);
            dstDiffMinMax[i3] = (cs.getMaxValue(i3) - dstMinVal[i3]) / maxNum;
        }
        boolean dstHasAlpha = dstCM.hasAlpha();
        boolean needSrcAlpha = srcCM.hasAlpha() && dstHasAlpha;
        float[] dstColor = dstHasAlpha ? new float[dstNumComp + 1] : new float[dstNumComp];
        if (precision == 8) {
            byte[] srcLine = new byte[w * srcNumComp];
            byte[] dstLine = new byte[w * dstNumComp];
            float[] alpha = null;
            if (needSrcAlpha) {
                alpha = new float[w];
            }
            srcIL = new LCMSImageLayout(srcLine, this.getNumInComponents());
            dstIL = new LCMSImageLayout(dstLine, this.getNumOutComponents());
            for (int y = 0; y < h; ++y) {
                int i4;
                int x;
                Object pixel = null;
                float[] color = null;
                int idx = 0;
                for (x = 0; x < w; ++x) {
                    pixel = srcRas.getDataElements(x, y, pixel);
                    color = srcCM.getNormalizedComponents(pixel, color, 0);
                    for (i4 = 0; i4 < srcNumComp; ++i4) {
                        srcLine[idx++] = (byte)((color[i4] - srcMinVal[i4]) * srcInvDiffMinMax[i4] + 0.5f);
                    }
                    if (!needSrcAlpha) continue;
                    alpha[x] = color[srcNumComp];
                }
                this.doTransform(srcIL, dstIL);
                pixel = null;
                idx = 0;
                for (x = 0; x < w; ++x) {
                    for (i4 = 0; i4 < dstNumComp; ++i4) {
                        dstColor[i4] = (float)(dstLine[idx++] & 0xFF) * dstDiffMinMax[i4] + dstMinVal[i4];
                    }
                    if (needSrcAlpha) {
                        dstColor[dstNumComp] = alpha[x];
                    } else if (dstHasAlpha) {
                        dstColor[dstNumComp] = 1.0f;
                    }
                    pixel = dstCM.getDataElements(dstColor, 0, pixel);
                    dstRas.setDataElements(x, y, pixel);
                }
            }
        } else {
            short[] srcLine = new short[w * srcNumComp];
            short[] dstLine = new short[w * dstNumComp];
            float[] alpha = null;
            if (needSrcAlpha) {
                alpha = new float[w];
            }
            srcIL = new LCMSImageLayout(srcLine, this.getNumInComponents());
            dstIL = new LCMSImageLayout(dstLine, this.getNumOutComponents());
            for (int y = 0; y < h; ++y) {
                int i5;
                int x;
                Object pixel = null;
                float[] color = null;
                int idx = 0;
                for (x = 0; x < w; ++x) {
                    pixel = srcRas.getDataElements(x, y, pixel);
                    color = srcCM.getNormalizedComponents(pixel, color, 0);
                    for (i5 = 0; i5 < srcNumComp; ++i5) {
                        srcLine[idx++] = (short)((color[i5] - srcMinVal[i5]) * srcInvDiffMinMax[i5] + 0.5f);
                    }
                    if (!needSrcAlpha) continue;
                    alpha[x] = color[srcNumComp];
                }
                this.doTransform(srcIL, dstIL);
                pixel = null;
                idx = 0;
                for (x = 0; x < w; ++x) {
                    for (i5 = 0; i5 < dstNumComp; ++i5) {
                        dstColor[i5] = (float)(dstLine[idx++] & 0xFFFF) * dstDiffMinMax[i5] + dstMinVal[i5];
                    }
                    if (needSrcAlpha) {
                        dstColor[dstNumComp] = alpha[x];
                    } else if (dstHasAlpha) {
                        dstColor[dstNumComp] = 1.0f;
                    }
                    pixel = dstCM.getDataElements(dstColor, 0, pixel);
                    dstRas.setDataElements(x, y, pixel);
                }
            }
        }
    }

    @Override
    public void colorConvert(Raster src, WritableRaster dst, float[] srcMinVal, float[] srcMaxVal, float[] dstMinVal, float[] dstMaxVal) {
        int i;
        SampleModel srcSM = src.getSampleModel();
        SampleModel dstSM = dst.getSampleModel();
        int srcTransferType = src.getTransferType();
        int dstTransferType = dst.getTransferType();
        boolean srcIsFloat = srcTransferType == 4 || srcTransferType == 5;
        boolean dstIsFloat = dstTransferType == 4 || dstTransferType == 5;
        int w = src.getWidth();
        int h = src.getHeight();
        int srcNumBands = src.getNumBands();
        int dstNumBands = dst.getNumBands();
        float[] srcScaleFactor = new float[srcNumBands];
        float[] dstScaleFactor = new float[dstNumBands];
        float[] srcUseMinVal = new float[srcNumBands];
        float[] dstUseMinVal = new float[dstNumBands];
        for (i = 0; i < srcNumBands; ++i) {
            if (srcIsFloat) {
                srcScaleFactor[i] = 65535.0f / (srcMaxVal[i] - srcMinVal[i]);
                srcUseMinVal[i] = srcMinVal[i];
                continue;
            }
            srcScaleFactor[i] = srcTransferType == 2 ? 2.0000305f : 65535.0f / (float)((1 << srcSM.getSampleSize(i)) - 1);
            srcUseMinVal[i] = 0.0f;
        }
        for (i = 0; i < dstNumBands; ++i) {
            if (dstIsFloat) {
                dstScaleFactor[i] = (dstMaxVal[i] - dstMinVal[i]) / 65535.0f;
                dstUseMinVal[i] = dstMinVal[i];
                continue;
            }
            dstScaleFactor[i] = dstTransferType == 2 ? 0.49999237f : (float)((1 << dstSM.getSampleSize(i)) - 1) / 65535.0f;
            dstUseMinVal[i] = 0.0f;
        }
        int ys = src.getMinY();
        int yd = dst.getMinY();
        short[] srcLine = new short[w * srcNumBands];
        short[] dstLine = new short[w * dstNumBands];
        LCMSImageLayout srcIL = new LCMSImageLayout(srcLine, this.getNumInComponents());
        LCMSImageLayout dstIL = new LCMSImageLayout(dstLine, this.getNumOutComponents());
        int y = 0;
        while (y < h) {
            float sample;
            int i2;
            int xs = src.getMinX();
            int idx = 0;
            int x = 0;
            while (x < w) {
                for (i2 = 0; i2 < srcNumBands; ++i2) {
                    sample = src.getSampleFloat(xs, ys, i2);
                    srcLine[idx++] = (short)((sample - srcUseMinVal[i2]) * srcScaleFactor[i2] + 0.5f);
                }
                ++x;
                ++xs;
            }
            this.doTransform(srcIL, dstIL);
            int xd = dst.getMinX();
            idx = 0;
            x = 0;
            while (x < w) {
                for (i2 = 0; i2 < dstNumBands; ++i2) {
                    sample = (float)(dstLine[idx++] & 0xFFFF) * dstScaleFactor[i2] + dstUseMinVal[i2];
                    dst.setSample(xd, yd, i2, sample);
                }
                ++x;
                ++xd;
            }
            ++y;
            ++ys;
            ++yd;
        }
    }

    @Override
    public void colorConvert(Raster src, WritableRaster dst) {
        int i;
        int i2;
        LCMSImageLayout srcIL;
        LCMSImageLayout dstIL = LCMSImageLayout.createImageLayout(dst, null);
        if (dstIL != null && (srcIL = LCMSImageLayout.createImageLayout(src, null)) != null) {
            this.doTransform(srcIL, dstIL);
            return;
        }
        SampleModel srcSM = src.getSampleModel();
        SampleModel dstSM = dst.getSampleModel();
        int srcTransferType = src.getTransferType();
        int dstTransferType = dst.getTransferType();
        int w = src.getWidth();
        int h = src.getHeight();
        int srcNumBands = src.getNumBands();
        int dstNumBands = dst.getNumBands();
        int precision = 8;
        float maxNum = 255.0f;
        for (i2 = 0; i2 < srcNumBands; ++i2) {
            if (srcSM.getSampleSize(i2) <= 8) continue;
            precision = 16;
            maxNum = 65535.0f;
        }
        for (i2 = 0; i2 < dstNumBands; ++i2) {
            if (dstSM.getSampleSize(i2) <= 8) continue;
            precision = 16;
            maxNum = 65535.0f;
        }
        float[] srcScaleFactor = new float[srcNumBands];
        float[] dstScaleFactor = new float[dstNumBands];
        for (i = 0; i < srcNumBands; ++i) {
            srcScaleFactor[i] = srcTransferType == 2 ? maxNum / 32767.0f : maxNum / (float)((1 << srcSM.getSampleSize(i)) - 1);
        }
        for (i = 0; i < dstNumBands; ++i) {
            dstScaleFactor[i] = dstTransferType == 2 ? 32767.0f / maxNum : (float)((1 << dstSM.getSampleSize(i)) - 1) / maxNum;
        }
        int ys = src.getMinY();
        int yd = dst.getMinY();
        if (precision == 8) {
            byte[] srcLine = new byte[w * srcNumBands];
            byte[] dstLine = new byte[w * dstNumBands];
            srcIL = new LCMSImageLayout(srcLine, this.getNumInComponents());
            dstIL = new LCMSImageLayout(dstLine, this.getNumOutComponents());
            int y = 0;
            while (y < h) {
                int sample;
                int i3;
                int xs = src.getMinX();
                int idx = 0;
                int x = 0;
                while (x < w) {
                    for (i3 = 0; i3 < srcNumBands; ++i3) {
                        sample = src.getSample(xs, ys, i3);
                        srcLine[idx++] = (byte)((float)sample * srcScaleFactor[i3] + 0.5f);
                    }
                    ++x;
                    ++xs;
                }
                this.doTransform(srcIL, dstIL);
                int xd = dst.getMinX();
                idx = 0;
                x = 0;
                while (x < w) {
                    for (i3 = 0; i3 < dstNumBands; ++i3) {
                        sample = (int)((float)(dstLine[idx++] & 0xFF) * dstScaleFactor[i3] + 0.5f);
                        dst.setSample(xd, yd, i3, sample);
                    }
                    ++x;
                    ++xd;
                }
                ++y;
                ++ys;
                ++yd;
            }
        } else {
            short[] srcLine = new short[w * srcNumBands];
            short[] dstLine = new short[w * dstNumBands];
            srcIL = new LCMSImageLayout(srcLine, this.getNumInComponents());
            dstIL = new LCMSImageLayout(dstLine, this.getNumOutComponents());
            int y = 0;
            while (y < h) {
                int sample;
                int i4;
                int xs = src.getMinX();
                int idx = 0;
                int x = 0;
                while (x < w) {
                    for (i4 = 0; i4 < srcNumBands; ++i4) {
                        sample = src.getSample(xs, ys, i4);
                        srcLine[idx++] = (short)((float)sample * srcScaleFactor[i4] + 0.5f);
                    }
                    ++x;
                    ++xs;
                }
                this.doTransform(srcIL, dstIL);
                int xd = dst.getMinX();
                idx = 0;
                x = 0;
                while (x < w) {
                    for (i4 = 0; i4 < dstNumBands; ++i4) {
                        sample = (int)((float)(dstLine[idx++] & 0xFFFF) * dstScaleFactor[i4] + 0.5f);
                        dst.setSample(xd, yd, i4, sample);
                    }
                    ++x;
                    ++xd;
                }
                ++y;
                ++ys;
                ++yd;
            }
        }
    }

    @Override
    public short[] colorConvert(short[] src, short[] dst) {
        if (dst == null) {
            dst = new short[src.length / this.numInComponents * this.numOutComponents];
        }
        this.doTransform(new LCMSImageLayout(src, this.numInComponents), new LCMSImageLayout(dst, this.numOutComponents));
        return dst;
    }

    @Override
    public byte[] colorConvert(byte[] src, byte[] dst) {
        if (dst == null) {
            dst = new byte[src.length / this.numInComponents * this.numOutComponents];
        }
        this.doTransform(new LCMSImageLayout(src, this.numInComponents), new LCMSImageLayout(dst, this.numOutComponents));
        return dst;
    }

    private static final class NativeTransform {
        private long ID;
        private int inFormatter;
        private int outFormatter;

        private NativeTransform() {
        }

        private boolean match(LCMSImageLayout in, LCMSImageLayout out) {
            return this.inFormatter == in.pixelType && this.outFormatter == out.pixelType;
        }
    }
}

