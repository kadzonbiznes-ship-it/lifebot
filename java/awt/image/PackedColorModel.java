/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;

public abstract class PackedColorModel
extends ColorModel {
    int[] maskArray;
    int[] maskOffsets;
    float[] scaleFactors;
    private volatile int hashCode;

    public PackedColorModel(ColorSpace space, int bits, int[] colorMaskArray, int alphaMask, boolean isAlphaPremultiplied, int trans, int transferType) {
        super(bits, PackedColorModel.createBitsArray(colorMaskArray, alphaMask), space, alphaMask != 0, isAlphaPremultiplied, trans, transferType);
        if (bits < 1 || bits > 32) {
            throw new IllegalArgumentException("Number of bits must be between 1 and 32.");
        }
        this.maskArray = new int[this.numComponents];
        this.maskOffsets = new int[this.numComponents];
        this.scaleFactors = new float[this.numComponents];
        for (int i = 0; i < this.numColorComponents; ++i) {
            this.DecomposeMask(colorMaskArray[i], i, space.getName(i));
        }
        if (alphaMask != 0) {
            this.DecomposeMask(alphaMask, this.numColorComponents, "alpha");
            if (this.nBits[this.numComponents - 1] == 1) {
                this.transparency = 2;
            }
        }
    }

    public PackedColorModel(ColorSpace space, int bits, int rmask, int gmask, int bmask, int amask, boolean isAlphaPremultiplied, int trans, int transferType) {
        super(bits, PackedColorModel.createBitsArray(rmask, gmask, bmask, amask), space, amask != 0, isAlphaPremultiplied, trans, transferType);
        if (space.getType() != 5) {
            throw new IllegalArgumentException("ColorSpace must be TYPE_RGB.");
        }
        this.maskArray = new int[this.numComponents];
        this.maskOffsets = new int[this.numComponents];
        this.scaleFactors = new float[this.numComponents];
        this.DecomposeMask(rmask, 0, "red");
        this.DecomposeMask(gmask, 1, "green");
        this.DecomposeMask(bmask, 2, "blue");
        if (amask != 0) {
            this.DecomposeMask(amask, 3, "alpha");
            if (this.nBits[3] == 1) {
                this.transparency = 2;
            }
        }
    }

    public final int getMask(int index) {
        return this.maskArray[index];
    }

    public final int[] getMasks() {
        return (int[])this.maskArray.clone();
    }

    private void DecomposeMask(int mask, int idx, String componentName) {
        int off = 0;
        int count = this.nBits[idx];
        this.maskArray[idx] = mask;
        if (mask != 0) {
            while ((mask & 1) == 0) {
                mask >>>= 1;
                ++off;
            }
        }
        if (off + count > this.pixel_bits) {
            throw new IllegalArgumentException(componentName + " mask " + Integer.toHexString(this.maskArray[idx]) + " overflows pixel (expecting " + this.pixel_bits + " bits");
        }
        this.maskOffsets[idx] = off;
        this.scaleFactors[idx] = count == 0 ? 256.0f : 255.0f / (float)((1 << count) - 1);
    }

    @Override
    public SampleModel createCompatibleSampleModel(int w, int h) {
        return new SinglePixelPackedSampleModel(this.transferType, w, h, this.maskArray);
    }

    @Override
    public boolean isCompatibleSampleModel(SampleModel sm) {
        if (!(sm instanceof SinglePixelPackedSampleModel)) {
            return false;
        }
        if (this.numComponents != sm.getNumBands()) {
            return false;
        }
        if (sm.getTransferType() != this.transferType) {
            return false;
        }
        SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)sm;
        int[] bitMasks = sppsm.getBitMasks();
        if (bitMasks.length != this.maskArray.length) {
            return false;
        }
        int maxMask = (int)((1L << DataBuffer.getDataTypeSize(this.transferType)) - 1L);
        for (int i = 0; i < bitMasks.length; ++i) {
            if ((maxMask & bitMasks[i]) == (maxMask & this.maskArray[i])) continue;
            return false;
        }
        return true;
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
        if (!(obj instanceof PackedColorModel)) {
            return false;
        }
        PackedColorModel cm = (PackedColorModel)obj;
        if (this.supportsAlpha != cm.hasAlpha() || this.isAlphaPremultiplied != cm.isAlphaPremultiplied() || this.pixel_bits != cm.getPixelSize() || this.transparency != cm.getTransparency() || this.numComponents != cm.getNumComponents() || !this.colorSpace.equals(cm.colorSpace) || this.transferType != cm.transferType) {
            return false;
        }
        int numC = cm.getNumComponents();
        for (int i = 0; i < numC; ++i) {
            if (this.maskArray[i] == cm.getMask(i)) continue;
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
            result = 89 * result + this.transferType;
            this.hashCode = result = 89 * result + Arrays.hashCode(this.maskArray);
        }
        return result;
    }

    private static final int[] createBitsArray(int[] colorMaskArray, int alphaMask) {
        int numColors = colorMaskArray.length;
        int numAlpha = alphaMask == 0 ? 0 : 1;
        int[] arr = new int[numColors + numAlpha];
        for (int i = 0; i < numColors; ++i) {
            arr[i] = PackedColorModel.countBits(colorMaskArray[i]);
            if (arr[i] >= 0) continue;
            throw new IllegalArgumentException("Noncontiguous color mask (" + Integer.toHexString(colorMaskArray[i]) + "at index " + i);
        }
        if (alphaMask != 0) {
            arr[numColors] = PackedColorModel.countBits(alphaMask);
            if (arr[numColors] < 0) {
                throw new IllegalArgumentException("Noncontiguous alpha mask (" + Integer.toHexString(alphaMask));
            }
        }
        return arr;
    }

    private static final int[] createBitsArray(int rmask, int gmask, int bmask, int amask) {
        int[] arr = new int[3 + (amask == 0 ? 0 : 1)];
        arr[0] = PackedColorModel.countBits(rmask);
        arr[1] = PackedColorModel.countBits(gmask);
        arr[2] = PackedColorModel.countBits(bmask);
        if (arr[0] < 0) {
            throw new IllegalArgumentException("Noncontiguous red mask (" + Integer.toHexString(rmask));
        }
        if (arr[1] < 0) {
            throw new IllegalArgumentException("Noncontiguous green mask (" + Integer.toHexString(gmask));
        }
        if (arr[2] < 0) {
            throw new IllegalArgumentException("Noncontiguous blue mask (" + Integer.toHexString(bmask));
        }
        if (amask != 0) {
            arr[3] = PackedColorModel.countBits(amask);
            if (arr[3] < 0) {
                throw new IllegalArgumentException("Noncontiguous alpha mask (" + Integer.toHexString(amask));
            }
        }
        return arr;
    }

    private static final int countBits(int mask) {
        int count = 0;
        if (mask != 0) {
            while ((mask & 1) == 0) {
                mask >>>= 1;
            }
            while ((mask & 1) == 1) {
                mask >>>= 1;
                ++count;
            }
        }
        if (mask != 0) {
            return -1;
        }
        return count;
    }
}

