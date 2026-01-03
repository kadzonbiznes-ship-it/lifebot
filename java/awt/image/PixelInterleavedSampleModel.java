/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.image.ComponentSampleModel;
import java.awt.image.SampleModel;

public class PixelInterleavedSampleModel
extends ComponentSampleModel {
    public PixelInterleavedSampleModel(int dataType, int w, int h, int pixelStride, int scanlineStride, int[] bandOffsets) {
        super(dataType, w, h, pixelStride, scanlineStride, bandOffsets);
        int minBandOff = this.bandOffsets[0];
        int maxBandOff = this.bandOffsets[0];
        for (int i = 1; i < this.bandOffsets.length; ++i) {
            minBandOff = Math.min(minBandOff, this.bandOffsets[i]);
            maxBandOff = Math.max(maxBandOff, this.bandOffsets[i]);
        }
        if ((maxBandOff -= minBandOff) > scanlineStride) {
            throw new IllegalArgumentException("Offsets between bands must be less than the scanline  stride");
        }
        if (pixelStride * w > scanlineStride) {
            throw new IllegalArgumentException("Pixel stride times width must be less than or equal to the scanline stride");
        }
        if (pixelStride < maxBandOff) {
            throw new IllegalArgumentException("Pixel stride must be greater than or equal to the offsets between bands");
        }
    }

    @Override
    public SampleModel createCompatibleSampleModel(int w, int h) {
        int[] bandOff;
        int minBandoff = this.bandOffsets[0];
        int numBands = this.bandOffsets.length;
        for (int i = 1; i < numBands; ++i) {
            if (this.bandOffsets[i] >= minBandoff) continue;
            minBandoff = this.bandOffsets[i];
        }
        if (minBandoff > 0) {
            bandOff = new int[numBands];
            for (int i = 0; i < numBands; ++i) {
                bandOff[i] = this.bandOffsets[i] - minBandoff;
            }
        } else {
            bandOff = this.bandOffsets;
        }
        return new PixelInterleavedSampleModel(this.dataType, w, h, this.pixelStride, this.pixelStride * w, bandOff);
    }

    @Override
    public SampleModel createSubsetSampleModel(int[] bands) {
        int[] newBandOffsets = new int[bands.length];
        for (int i = 0; i < bands.length; ++i) {
            newBandOffsets[i] = this.bandOffsets[bands[i]];
        }
        return new PixelInterleavedSampleModel(this.dataType, this.width, this.height, this.pixelStride, this.scanlineStride, newBandOffsets);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ 1;
    }
}

