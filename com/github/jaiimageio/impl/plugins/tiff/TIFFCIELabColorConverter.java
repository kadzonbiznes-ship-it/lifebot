/*
 * Decompiled with CFR 0.152.
 */
package com.github.jaiimageio.impl.plugins.tiff;

import com.github.jaiimageio.plugins.tiff.TIFFColorConverter;

public class TIFFCIELabColorConverter
extends TIFFColorConverter {
    private static final float Xn = 95.047f;
    private static final float Yn = 100.0f;
    private static final float Zn = 108.883f;
    private static final float THRESHOLD = (float)Math.pow(0.008856, 0.3333333333333333);

    private float clamp(float x) {
        if (x < 0.0f) {
            return 0.0f;
        }
        if (x > 100.0f) {
            return 255.0f;
        }
        return x * 2.55f;
    }

    private float clamp2(float x) {
        if (x < 0.0f) {
            return 0.0f;
        }
        if (x > 255.0f) {
            return 255.0f;
        }
        return x;
    }

    @Override
    public void fromRGB(float r, float g, float b, float[] result) {
        float X = 0.412453f * r + 0.35758f * g + 0.180423f * b;
        float Y = 0.212671f * r + 0.71516f * g + 0.072169f * b;
        float Z = 0.019334f * r + 0.119193f * g + 0.950227f * b;
        float YYn = Y / 100.0f;
        float XXn = X / 95.047f;
        float ZZn = Z / 108.883f;
        YYn = YYn < 0.008856f ? 7.787f * YYn + 0.13793103f : (float)Math.pow(YYn, 0.3333333333333333);
        XXn = XXn < 0.008856f ? 7.787f * XXn + 0.13793103f : (float)Math.pow(XXn, 0.3333333333333333);
        ZZn = ZZn < 0.008856f ? 7.787f * ZZn + 0.13793103f : (float)Math.pow(ZZn, 0.3333333333333333);
        float LStar = 116.0f * YYn - 16.0f;
        float aStar = 500.0f * (XXn - YYn);
        float bStar = 200.0f * (YYn - ZZn);
        LStar *= 2.55f;
        if (aStar < 0.0f) {
            aStar += 256.0f;
        }
        if (bStar < 0.0f) {
            bStar += 256.0f;
        }
        result[0] = this.clamp2(LStar);
        result[1] = this.clamp2(aStar);
        result[2] = this.clamp2(bStar);
    }

    @Override
    public void toRGB(float x0, float x1, float x2, float[] rgb) {
        float fY;
        float YYn;
        float bStar;
        float LStar = x0 * 100.0f / 255.0f;
        float aStar = x1 > 128.0f ? x1 - 256.0f : x1;
        float f = bStar = x2 > 128.0f ? x2 - 256.0f : x2;
        if (LStar < 8.0f) {
            YYn = LStar / 903.3f;
            fY = 7.787f * YYn + 0.13793103f;
        } else {
            float YYn_cubeRoot = (LStar + 16.0f) / 116.0f;
            YYn = YYn_cubeRoot * YYn_cubeRoot * YYn_cubeRoot;
            fY = (float)Math.pow(YYn, 0.3333333333333333);
        }
        float Y = YYn * 100.0f;
        float fX = fY + aStar / 500.0f;
        float X = fX <= THRESHOLD ? 95.047f * (fX - 0.13793103f) / 7.787f : 95.047f * fX * fX * fX;
        float fZ = fY - bStar / 200.0f;
        float Z = fZ <= THRESHOLD ? 108.883f * (fZ - 0.13793103f) / 7.787f : 108.883f * fZ * fZ * fZ;
        float R = 3.240479f * X - 1.53715f * Y - 0.498535f * Z;
        float G = -0.969256f * X + 1.875992f * Y + 0.041556f * Z;
        float B = 0.055648f * X - 0.204043f * Y + 1.057311f * Z;
        rgb[0] = this.clamp(R);
        rgb[1] = this.clamp(G);
        rgb[2] = this.clamp(B);
    }
}

