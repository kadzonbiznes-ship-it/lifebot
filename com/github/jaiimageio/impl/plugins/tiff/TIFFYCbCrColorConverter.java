/*
 * Decompiled with CFR 0.152.
 */
package com.github.jaiimageio.impl.plugins.tiff;

import com.github.jaiimageio.impl.plugins.tiff.TIFFImageMetadata;
import com.github.jaiimageio.plugins.tiff.TIFFColorConverter;
import com.github.jaiimageio.plugins.tiff.TIFFField;

public class TIFFYCbCrColorConverter
extends TIFFColorConverter {
    private float LumaRed = 0.299f;
    private float LumaGreen = 0.587f;
    private float LumaBlue = 0.114f;
    private float referenceBlackY = 0.0f;
    private float referenceWhiteY = 255.0f;
    private float referenceBlackCb = 128.0f;
    private float referenceWhiteCb = 255.0f;
    private float referenceBlackCr = 128.0f;
    private float referenceWhiteCr = 255.0f;
    private float codingRangeY = 255.0f;
    private float codingRangeCbCr = 127.0f;

    public TIFFYCbCrColorConverter(TIFFImageMetadata metadata) {
        TIFFImageMetadata tmetadata = metadata;
        TIFFField f = tmetadata.getTIFFField(529);
        if (f != null && f.getCount() == 3) {
            this.LumaRed = f.getAsFloat(0);
            this.LumaGreen = f.getAsFloat(1);
            this.LumaBlue = f.getAsFloat(2);
        }
        if ((f = tmetadata.getTIFFField(532)) != null && f.getCount() == 6) {
            this.referenceBlackY = f.getAsFloat(0);
            this.referenceWhiteY = f.getAsFloat(1);
            this.referenceBlackCb = f.getAsFloat(2);
            this.referenceWhiteCb = f.getAsFloat(3);
            this.referenceBlackCr = f.getAsFloat(4);
            this.referenceWhiteCr = f.getAsFloat(5);
        }
    }

    @Override
    public void fromRGB(float r, float g, float b, float[] result) {
        float Y = this.LumaRed * r + this.LumaGreen * g + this.LumaBlue * b;
        float Cb = (b - Y) / (2.0f - 2.0f * this.LumaBlue);
        float Cr = (r - Y) / (2.0f - 2.0f * this.LumaRed);
        result[0] = Y * (this.referenceWhiteY - this.referenceBlackY) / this.codingRangeY + this.referenceBlackY;
        result[1] = Cb * (this.referenceWhiteCb - this.referenceBlackCb) / this.codingRangeCbCr + this.referenceBlackCb;
        result[2] = Cr * (this.referenceWhiteCr - this.referenceBlackCr) / this.codingRangeCbCr + this.referenceBlackCr;
    }

    @Override
    public void toRGB(float x0, float x1, float x2, float[] rgb) {
        float Y = (x0 - this.referenceBlackY) * this.codingRangeY / (this.referenceWhiteY - this.referenceBlackY);
        float Cb = (x1 - this.referenceBlackCb) * this.codingRangeCbCr / (this.referenceWhiteCb - this.referenceBlackCb);
        float Cr = (x2 - this.referenceBlackCr) * this.codingRangeCbCr / (this.referenceWhiteCr - this.referenceBlackCr);
        rgb[0] = Cr * (2.0f - 2.0f * this.LumaRed) + Y;
        rgb[2] = Cb * (2.0f - 2.0f * this.LumaBlue) + Y;
        rgb[1] = (Y - this.LumaBlue * rgb[2] - this.LumaRed * rgb[0]) / this.LumaGreen;
    }
}

