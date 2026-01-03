/*
 * Decompiled with CFR 0.152.
 */
package java.awt.color;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.io.ObjectInputStream;
import sun.java2d.cmm.CMSManager;
import sun.java2d.cmm.ColorTransform;
import sun.java2d.cmm.PCMM;

public class ICC_ColorSpace
extends ColorSpace {
    private static final long serialVersionUID = 3455889114070431483L;
    private ICC_Profile thisProfile;
    private float[] minVal;
    private float[] maxVal;
    private float[] diffMinMax;
    private float[] invDiffMinMax;
    private boolean needScaleInit = true;
    private volatile transient ColorTransform this2srgb;
    private volatile transient ColorTransform srgb2this;
    private volatile transient ColorTransform this2xyz;
    private volatile transient ColorTransform xyz2this;

    public ICC_ColorSpace(ICC_Profile profile) {
        super(profile.getColorSpaceType(), profile.getNumComponents());
        int profileClass = profile.getProfileClass();
        if (profileClass != 0 && profileClass != 1 && profileClass != 2 && profileClass != 4 && profileClass != 6 && profileClass != 5) {
            throw new IllegalArgumentException("Invalid profile type");
        }
        this.thisProfile = profile;
        this.setMinMax();
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        if (this.thisProfile == null) {
            this.thisProfile = ICC_Profile.getInstance(1000);
        }
    }

    public ICC_Profile getProfile() {
        return this.thisProfile;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public float[] toRGB(float[] colorvalue) {
        if (this.this2srgb == null) {
            ICC_ColorSpace iCC_ColorSpace = this;
            synchronized (iCC_ColorSpace) {
                if (this.this2srgb == null) {
                    if (this.needScaleInit) {
                        this.setComponentScaling();
                    }
                    ICC_Profile srgb = ICC_Profile.getInstance(1000);
                    PCMM mdl = CMSManager.getModule();
                    this.this2srgb = mdl.createTransform(-1, this.thisProfile, srgb);
                }
            }
        }
        int nc = this.getNumComponents();
        short[] tmp = new short[nc];
        for (int i = 0; i < nc; ++i) {
            tmp[i] = (short)((colorvalue[i] - this.minVal[i]) * this.invDiffMinMax[i] + 0.5f);
        }
        tmp = this.this2srgb.colorConvert(tmp, null);
        float[] result = new float[3];
        for (int i = 0; i < 3; ++i) {
            result[i] = (float)(tmp[i] & 0xFFFF) / 65535.0f;
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public float[] fromRGB(float[] rgbvalue) {
        if (this.srgb2this == null) {
            ICC_ColorSpace iCC_ColorSpace = this;
            synchronized (iCC_ColorSpace) {
                if (this.srgb2this == null) {
                    if (this.needScaleInit) {
                        this.setComponentScaling();
                    }
                    ICC_Profile srgb = ICC_Profile.getInstance(1000);
                    PCMM mdl = CMSManager.getModule();
                    this.srgb2this = mdl.createTransform(-1, srgb, this.thisProfile);
                }
            }
        }
        short[] tmp = new short[3];
        for (int i = 0; i < 3; ++i) {
            tmp[i] = (short)(rgbvalue[i] * 65535.0f + 0.5f);
        }
        tmp = this.srgb2this.colorConvert(tmp, null);
        int nc = this.getNumComponents();
        float[] result = new float[nc];
        for (int i = 0; i < nc; ++i) {
            result[i] = (float)(tmp[i] & 0xFFFF) / 65535.0f * this.diffMinMax[i] + this.minVal[i];
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public float[] toCIEXYZ(float[] colorvalue) {
        if (this.this2xyz == null) {
            ICC_ColorSpace iCC_ColorSpace = this;
            synchronized (iCC_ColorSpace) {
                if (this.this2xyz == null) {
                    if (this.needScaleInit) {
                        this.setComponentScaling();
                    }
                    ICC_Profile xyz = ICC_Profile.getInstance(1001);
                    PCMM mdl = CMSManager.getModule();
                    this.this2xyz = mdl.createTransform(1, this.thisProfile, xyz);
                }
            }
        }
        int nc = this.getNumComponents();
        short[] tmp = new short[nc];
        for (int i = 0; i < nc; ++i) {
            tmp[i] = (short)((colorvalue[i] - this.minVal[i]) * this.invDiffMinMax[i] + 0.5f);
        }
        tmp = this.this2xyz.colorConvert(tmp, null);
        float ALMOST_TWO = 1.9999695f;
        float[] result = new float[3];
        for (int i = 0; i < 3; ++i) {
            result[i] = (float)(tmp[i] & 0xFFFF) / 65535.0f * ALMOST_TWO;
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public float[] fromCIEXYZ(float[] colorvalue) {
        if (this.xyz2this == null) {
            ICC_ColorSpace iCC_ColorSpace = this;
            synchronized (iCC_ColorSpace) {
                if (this.xyz2this == null) {
                    if (this.needScaleInit) {
                        this.setComponentScaling();
                    }
                    ICC_Profile xyz = ICC_Profile.getInstance(1001);
                    PCMM mdl = CMSManager.getModule();
                    this.xyz2this = mdl.createTransform(1, xyz, this.thisProfile);
                }
            }
        }
        short[] tmp = new short[3];
        float ALMOST_TWO = 1.9999695f;
        float factor = 65535.0f / ALMOST_TWO;
        for (int i = 0; i < 3; ++i) {
            tmp[i] = (short)(colorvalue[i] * factor + 0.5f);
        }
        tmp = this.xyz2this.colorConvert(tmp, null);
        int nc = this.getNumComponents();
        float[] result = new float[nc];
        for (int i = 0; i < nc; ++i) {
            result[i] = (float)(tmp[i] & 0xFFFF) / 65535.0f * this.diffMinMax[i] + this.minVal[i];
        }
        return result;
    }

    @Override
    public float getMinValue(int component) {
        this.rangeCheck(component);
        return this.minVal[component];
    }

    @Override
    public float getMaxValue(int component) {
        this.rangeCheck(component);
        return this.maxVal[component];
    }

    private void setMinMax() {
        int nc = this.getNumComponents();
        int type = this.getType();
        this.minVal = new float[nc];
        this.maxVal = new float[nc];
        if (type == 1) {
            this.minVal[0] = 0.0f;
            this.maxVal[0] = 100.0f;
            this.minVal[1] = -128.0f;
            this.maxVal[1] = 127.0f;
            this.minVal[2] = -128.0f;
            this.maxVal[2] = 127.0f;
        } else if (type == 0) {
            this.minVal[2] = 0.0f;
            this.minVal[1] = 0.0f;
            this.minVal[0] = 0.0f;
            this.maxVal[2] = 1.9999695f;
            this.maxVal[1] = 1.9999695f;
            this.maxVal[0] = 1.9999695f;
        } else {
            for (int i = 0; i < nc; ++i) {
                this.minVal[i] = 0.0f;
                this.maxVal[i] = 1.0f;
            }
        }
    }

    private void setComponentScaling() {
        int nc = this.getNumComponents();
        this.diffMinMax = new float[nc];
        this.invDiffMinMax = new float[nc];
        for (int i = 0; i < nc; ++i) {
            this.minVal[i] = this.getMinValue(i);
            this.maxVal[i] = this.getMaxValue(i);
            this.diffMinMax[i] = this.maxVal[i] - this.minVal[i];
            this.invDiffMinMax[i] = 65535.0f / this.diffMinMax[i];
        }
        this.needScaleInit = false;
    }
}

