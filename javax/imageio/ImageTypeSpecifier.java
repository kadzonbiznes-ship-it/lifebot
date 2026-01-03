/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio;

import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

public class ImageTypeSpecifier {
    protected ColorModel colorModel;
    protected SampleModel sampleModel;
    private static ImageTypeSpecifier[] BISpecifier;
    private static ColorSpace sRGB;

    private ImageTypeSpecifier() {
    }

    public ImageTypeSpecifier(ColorModel colorModel, SampleModel sampleModel) {
        if (colorModel == null) {
            throw new IllegalArgumentException("colorModel == null!");
        }
        if (sampleModel == null) {
            throw new IllegalArgumentException("sampleModel == null!");
        }
        if (!colorModel.isCompatibleSampleModel(sampleModel)) {
            throw new IllegalArgumentException("sampleModel is incompatible with colorModel!");
        }
        this.colorModel = colorModel;
        this.sampleModel = sampleModel;
    }

    public ImageTypeSpecifier(RenderedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("image == null!");
        }
        this.colorModel = image.getColorModel();
        this.sampleModel = image.getSampleModel();
    }

    public static ImageTypeSpecifier createPacked(ColorSpace colorSpace, int redMask, int greenMask, int blueMask, int alphaMask, int transferType, boolean isAlphaPremultiplied) {
        return new Packed(colorSpace, redMask, greenMask, blueMask, alphaMask, transferType, isAlphaPremultiplied);
    }

    static ColorModel createComponentCM(ColorSpace colorSpace, int numBands, int dataType, boolean hasAlpha, boolean isAlphaPremultiplied) {
        int transparency = hasAlpha ? 3 : 1;
        int[] numBits = new int[numBands];
        int bits = DataBuffer.getDataTypeSize(dataType);
        for (int i = 0; i < numBands; ++i) {
            numBits[i] = bits;
        }
        return new ComponentColorModel(colorSpace, numBits, hasAlpha, isAlphaPremultiplied, transparency, dataType);
    }

    public static ImageTypeSpecifier createInterleaved(ColorSpace colorSpace, int[] bandOffsets, int dataType, boolean hasAlpha, boolean isAlphaPremultiplied) {
        return new Interleaved(colorSpace, bandOffsets, dataType, hasAlpha, isAlphaPremultiplied);
    }

    public static ImageTypeSpecifier createBanded(ColorSpace colorSpace, int[] bankIndices, int[] bandOffsets, int dataType, boolean hasAlpha, boolean isAlphaPremultiplied) {
        return new Banded(colorSpace, bankIndices, bandOffsets, dataType, hasAlpha, isAlphaPremultiplied);
    }

    public static ImageTypeSpecifier createGrayscale(int bits, int dataType, boolean isSigned) {
        return new Grayscale(bits, dataType, isSigned, false, false);
    }

    public static ImageTypeSpecifier createGrayscale(int bits, int dataType, boolean isSigned, boolean isAlphaPremultiplied) {
        return new Grayscale(bits, dataType, isSigned, true, isAlphaPremultiplied);
    }

    public static ImageTypeSpecifier createIndexed(byte[] redLUT, byte[] greenLUT, byte[] blueLUT, byte[] alphaLUT, int bits, int dataType) {
        return new Indexed(redLUT, greenLUT, blueLUT, alphaLUT, bits, dataType);
    }

    public static ImageTypeSpecifier createFromBufferedImageType(int bufferedImageType) {
        if (bufferedImageType >= 1 && bufferedImageType <= 13) {
            return ImageTypeSpecifier.getSpecifier(bufferedImageType);
        }
        if (bufferedImageType == 0) {
            throw new IllegalArgumentException("Cannot create from TYPE_CUSTOM!");
        }
        throw new IllegalArgumentException("Invalid BufferedImage type!");
    }

    public static ImageTypeSpecifier createFromRenderedImage(RenderedImage image) {
        int bufferedImageType;
        if (image == null) {
            throw new IllegalArgumentException("image == null!");
        }
        if (image instanceof BufferedImage && (bufferedImageType = ((BufferedImage)image).getType()) != 0) {
            return ImageTypeSpecifier.getSpecifier(bufferedImageType);
        }
        return new ImageTypeSpecifier(image);
    }

    public int getBufferedImageType() {
        BufferedImage bi = this.createBufferedImage(1, 1);
        return bi.getType();
    }

    public int getNumComponents() {
        return this.colorModel.getNumComponents();
    }

    public int getNumBands() {
        return this.sampleModel.getNumBands();
    }

    public int getBitsPerBand(int band) {
        if (band < 0 || band >= this.getNumBands()) {
            throw new IllegalArgumentException("band out of range!");
        }
        return this.sampleModel.getSampleSize(band);
    }

    public SampleModel getSampleModel() {
        return this.sampleModel;
    }

    public SampleModel getSampleModel(int width, int height) {
        if ((long)width * (long)height > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("width*height > Integer.MAX_VALUE!");
        }
        return this.sampleModel.createCompatibleSampleModel(width, height);
    }

    public ColorModel getColorModel() {
        return this.colorModel;
    }

    public BufferedImage createBufferedImage(int width, int height) {
        try {
            SampleModel sampleModel = this.getSampleModel(width, height);
            WritableRaster raster = Raster.createWritableRaster(sampleModel, new Point(0, 0));
            return new BufferedImage(this.colorModel, raster, this.colorModel.isAlphaPremultiplied(), new Hashtable());
        }
        catch (NegativeArraySizeException e) {
            throw new IllegalArgumentException("Array size > Integer.MAX_VALUE!");
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof ImageTypeSpecifier)) {
            return false;
        }
        ImageTypeSpecifier that = (ImageTypeSpecifier)o;
        return this.colorModel.equals(that.colorModel) && this.sampleModel.equals(that.sampleModel);
    }

    public int hashCode() {
        return 9 * this.colorModel.hashCode() + 14 * this.sampleModel.hashCode();
    }

    private static ImageTypeSpecifier getSpecifier(int type) {
        if (BISpecifier[type] == null) {
            ImageTypeSpecifier.BISpecifier[type] = ImageTypeSpecifier.createSpecifier(type);
        }
        return BISpecifier[type];
    }

    private static ImageTypeSpecifier createSpecifier(int type) {
        switch (type) {
            case 1: {
                return ImageTypeSpecifier.createPacked(sRGB, 0xFF0000, 65280, 255, 0, 3, false);
            }
            case 2: {
                return ImageTypeSpecifier.createPacked(sRGB, 0xFF0000, 65280, 255, -16777216, 3, false);
            }
            case 3: {
                return ImageTypeSpecifier.createPacked(sRGB, 0xFF0000, 65280, 255, -16777216, 3, true);
            }
            case 4: {
                return ImageTypeSpecifier.createPacked(sRGB, 255, 65280, 0xFF0000, 0, 3, false);
            }
            case 5: {
                return ImageTypeSpecifier.createInterleaved(sRGB, new int[]{2, 1, 0}, 0, false, false);
            }
            case 6: {
                return ImageTypeSpecifier.createInterleaved(sRGB, new int[]{3, 2, 1, 0}, 0, true, false);
            }
            case 7: {
                return ImageTypeSpecifier.createInterleaved(sRGB, new int[]{3, 2, 1, 0}, 0, true, true);
            }
            case 8: {
                return ImageTypeSpecifier.createPacked(sRGB, 63488, 2016, 31, 0, 1, false);
            }
            case 9: {
                return ImageTypeSpecifier.createPacked(sRGB, 31744, 992, 31, 0, 1, false);
            }
            case 10: {
                return ImageTypeSpecifier.createGrayscale(8, 0, false);
            }
            case 11: {
                return ImageTypeSpecifier.createGrayscale(16, 1, false);
            }
            case 12: {
                return ImageTypeSpecifier.createGrayscale(1, 0, false);
            }
            case 13: {
                BufferedImage bi = new BufferedImage(1, 1, 13);
                IndexColorModel icm = (IndexColorModel)bi.getColorModel();
                int mapSize = icm.getMapSize();
                byte[] redLUT = new byte[mapSize];
                byte[] greenLUT = new byte[mapSize];
                byte[] blueLUT = new byte[mapSize];
                byte[] alphaLUT = new byte[mapSize];
                icm.getReds(redLUT);
                icm.getGreens(greenLUT);
                icm.getBlues(blueLUT);
                icm.getAlphas(alphaLUT);
                return ImageTypeSpecifier.createIndexed(redLUT, greenLUT, blueLUT, alphaLUT, 8, 0);
            }
        }
        throw new IllegalArgumentException("Invalid BufferedImage type!");
    }

    static {
        sRGB = ColorSpace.getInstance(1000);
        BISpecifier = new ImageTypeSpecifier[14];
    }

    static class Packed
    extends ImageTypeSpecifier {
        ColorSpace colorSpace;
        int redMask;
        int greenMask;
        int blueMask;
        int alphaMask;
        int transferType;
        boolean isAlphaPremultiplied;

        public Packed(ColorSpace colorSpace, int redMask, int greenMask, int blueMask, int alphaMask, int transferType, boolean isAlphaPremultiplied) {
            if (colorSpace == null) {
                throw new IllegalArgumentException("colorSpace == null!");
            }
            if (colorSpace.getType() != 5) {
                throw new IllegalArgumentException("colorSpace is not of type TYPE_RGB!");
            }
            if (transferType != 0 && transferType != 1 && transferType != 3) {
                throw new IllegalArgumentException("Bad value for transferType!");
            }
            if (redMask == 0 && greenMask == 0 && blueMask == 0 && alphaMask == 0) {
                throw new IllegalArgumentException("No mask has at least 1 bit set!");
            }
            this.colorSpace = colorSpace;
            this.redMask = redMask;
            this.greenMask = greenMask;
            this.blueMask = blueMask;
            this.alphaMask = alphaMask;
            this.transferType = transferType;
            this.isAlphaPremultiplied = isAlphaPremultiplied;
            int bits = 32;
            this.colorModel = new DirectColorModel(colorSpace, bits, redMask, greenMask, blueMask, alphaMask, isAlphaPremultiplied, transferType);
            this.sampleModel = this.colorModel.createCompatibleSampleModel(1, 1);
        }
    }

    static class Interleaved
    extends ImageTypeSpecifier {
        ColorSpace colorSpace;
        int[] bandOffsets;
        int dataType;
        boolean hasAlpha;
        boolean isAlphaPremultiplied;

        public Interleaved(ColorSpace colorSpace, int[] bandOffsets, int dataType, boolean hasAlpha, boolean isAlphaPremultiplied) {
            int minBandOffset;
            if (colorSpace == null) {
                throw new IllegalArgumentException("colorSpace == null!");
            }
            if (bandOffsets == null) {
                throw new IllegalArgumentException("bandOffsets == null!");
            }
            int numBands = colorSpace.getNumComponents() + (hasAlpha ? 1 : 0);
            if (bandOffsets.length != numBands) {
                throw new IllegalArgumentException("bandOffsets.length is wrong!");
            }
            if (dataType != 0 && dataType != 2 && dataType != 1 && dataType != 3 && dataType != 4 && dataType != 5) {
                throw new IllegalArgumentException("Bad value for dataType!");
            }
            this.colorSpace = colorSpace;
            this.bandOffsets = (int[])bandOffsets.clone();
            this.dataType = dataType;
            this.hasAlpha = hasAlpha;
            this.isAlphaPremultiplied = isAlphaPremultiplied;
            this.colorModel = ImageTypeSpecifier.createComponentCM(colorSpace, bandOffsets.length, dataType, hasAlpha, isAlphaPremultiplied);
            int maxBandOffset = minBandOffset = bandOffsets[0];
            for (int i = 0; i < bandOffsets.length; ++i) {
                int offset = bandOffsets[i];
                minBandOffset = Math.min(offset, minBandOffset);
                maxBandOffset = Math.max(offset, maxBandOffset);
            }
            int pixelStride = maxBandOffset - minBandOffset + 1;
            int w = 1;
            int h = 1;
            this.sampleModel = new PixelInterleavedSampleModel(dataType, w, h, pixelStride, w * pixelStride, bandOffsets);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Interleaved)) {
                return false;
            }
            Interleaved that = (Interleaved)o;
            if (!this.colorSpace.equals(that.colorSpace) || this.dataType != that.dataType || this.hasAlpha != that.hasAlpha || this.isAlphaPremultiplied != that.isAlphaPremultiplied || this.bandOffsets.length != that.bandOffsets.length) {
                return false;
            }
            for (int i = 0; i < this.bandOffsets.length; ++i) {
                if (this.bandOffsets[i] == that.bandOffsets[i]) continue;
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return super.hashCode() + 4 * this.bandOffsets.length + 25 * this.dataType + (this.hasAlpha ? 17 : 18);
        }
    }

    static class Banded
    extends ImageTypeSpecifier {
        ColorSpace colorSpace;
        int[] bankIndices;
        int[] bandOffsets;
        int dataType;
        boolean hasAlpha;
        boolean isAlphaPremultiplied;

        public Banded(ColorSpace colorSpace, int[] bankIndices, int[] bandOffsets, int dataType, boolean hasAlpha, boolean isAlphaPremultiplied) {
            if (colorSpace == null) {
                throw new IllegalArgumentException("colorSpace == null!");
            }
            if (bankIndices == null) {
                throw new IllegalArgumentException("bankIndices == null!");
            }
            if (bandOffsets == null) {
                throw new IllegalArgumentException("bandOffsets == null!");
            }
            if (bankIndices.length != bandOffsets.length) {
                throw new IllegalArgumentException("bankIndices.length != bandOffsets.length!");
            }
            if (dataType != 0 && dataType != 2 && dataType != 1 && dataType != 3 && dataType != 4 && dataType != 5) {
                throw new IllegalArgumentException("Bad value for dataType!");
            }
            int numBands = colorSpace.getNumComponents() + (hasAlpha ? 1 : 0);
            if (bandOffsets.length != numBands) {
                throw new IllegalArgumentException("bandOffsets.length is wrong!");
            }
            this.colorSpace = colorSpace;
            this.bankIndices = (int[])bankIndices.clone();
            this.bandOffsets = (int[])bandOffsets.clone();
            this.dataType = dataType;
            this.hasAlpha = hasAlpha;
            this.isAlphaPremultiplied = isAlphaPremultiplied;
            this.colorModel = ImageTypeSpecifier.createComponentCM(colorSpace, bankIndices.length, dataType, hasAlpha, isAlphaPremultiplied);
            int w = 1;
            int h = 1;
            this.sampleModel = new BandedSampleModel(dataType, w, h, w, bankIndices, bandOffsets);
        }

        @Override
        public boolean equals(Object o) {
            int i;
            if (!(o instanceof Banded)) {
                return false;
            }
            Banded that = (Banded)o;
            if (!this.colorSpace.equals(that.colorSpace) || this.dataType != that.dataType || this.hasAlpha != that.hasAlpha || this.isAlphaPremultiplied != that.isAlphaPremultiplied || this.bankIndices.length != that.bankIndices.length || this.bandOffsets.length != that.bandOffsets.length) {
                return false;
            }
            for (i = 0; i < this.bankIndices.length; ++i) {
                if (this.bankIndices[i] == that.bankIndices[i]) continue;
                return false;
            }
            for (i = 0; i < this.bandOffsets.length; ++i) {
                if (this.bandOffsets[i] == that.bandOffsets[i]) continue;
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return super.hashCode() + 3 * this.bandOffsets.length + 7 * this.bankIndices.length + 21 * this.dataType + (this.hasAlpha ? 19 : 29);
        }
    }

    static class Grayscale
    extends ImageTypeSpecifier {
        int bits;
        int dataType;
        boolean isSigned;
        boolean hasAlpha;
        boolean isAlphaPremultiplied;

        public Grayscale(int bits, int dataType, boolean isSigned, boolean hasAlpha, boolean isAlphaPremultiplied) {
            if (bits != 1 && bits != 2 && bits != 4 && bits != 8 && bits != 16) {
                throw new IllegalArgumentException("Bad value for bits!");
            }
            if (dataType != 0 && dataType != 2 && dataType != 1) {
                throw new IllegalArgumentException("Bad value for dataType!");
            }
            if (bits > 8 && dataType == 0) {
                throw new IllegalArgumentException("Too many bits for dataType!");
            }
            this.bits = bits;
            this.dataType = dataType;
            this.isSigned = isSigned;
            this.hasAlpha = hasAlpha;
            this.isAlphaPremultiplied = isAlphaPremultiplied;
            ColorSpace colorSpace = ColorSpace.getInstance(1003);
            if (bits == 8 && dataType == 0 || bits == 16 && (dataType == 2 || dataType == 1)) {
                int numBands = hasAlpha ? 2 : 1;
                int transparency = hasAlpha ? 3 : 1;
                int[] nBits = new int[numBands];
                nBits[0] = bits;
                if (numBands == 2) {
                    nBits[1] = bits;
                }
                this.colorModel = new ComponentColorModel(colorSpace, nBits, hasAlpha, isAlphaPremultiplied, transparency, dataType);
                int[] bandOffsets = new int[numBands];
                bandOffsets[0] = 0;
                if (numBands == 2) {
                    bandOffsets[1] = 1;
                }
                int w = 1;
                int h = 1;
                this.sampleModel = new PixelInterleavedSampleModel(dataType, w, h, numBands, w * numBands, bandOffsets);
            } else {
                int numEntries = 1 << bits;
                byte[] arr = new byte[numEntries];
                for (int i = 0; i < numEntries; ++i) {
                    arr[i] = (byte)(i * 255 / (numEntries - 1));
                }
                this.colorModel = new IndexColorModel(bits, numEntries, arr, arr, arr);
                this.sampleModel = new MultiPixelPackedSampleModel(dataType, 1, 1, bits);
            }
        }
    }

    static class Indexed
    extends ImageTypeSpecifier {
        byte[] redLUT;
        byte[] greenLUT;
        byte[] blueLUT;
        byte[] alphaLUT = null;
        int bits;
        int dataType;

        public Indexed(byte[] redLUT, byte[] greenLUT, byte[] blueLUT, byte[] alphaLUT, int bits, int dataType) {
            if (redLUT == null || greenLUT == null || blueLUT == null) {
                throw new IllegalArgumentException("LUT is null!");
            }
            if (bits != 1 && bits != 2 && bits != 4 && bits != 8 && bits != 16) {
                throw new IllegalArgumentException("Bad value for bits!");
            }
            if (dataType != 0 && dataType != 2 && dataType != 1 && dataType != 3) {
                throw new IllegalArgumentException("Bad value for dataType!");
            }
            if (bits > 8 && dataType == 0 || bits > 16 && dataType != 3) {
                throw new IllegalArgumentException("Too many bits for dataType!");
            }
            int len = 1 << bits;
            if (redLUT.length != len || greenLUT.length != len || blueLUT.length != len || alphaLUT != null && alphaLUT.length != len) {
                throw new IllegalArgumentException("LUT has improper length!");
            }
            this.redLUT = (byte[])redLUT.clone();
            this.greenLUT = (byte[])greenLUT.clone();
            this.blueLUT = (byte[])blueLUT.clone();
            if (alphaLUT != null) {
                this.alphaLUT = (byte[])alphaLUT.clone();
            }
            this.bits = bits;
            this.dataType = dataType;
            this.colorModel = alphaLUT == null ? new IndexColorModel(bits, redLUT.length, redLUT, greenLUT, blueLUT) : new IndexColorModel(bits, redLUT.length, redLUT, greenLUT, blueLUT, alphaLUT);
            if (bits == 8 && dataType == 0 || bits == 16 && (dataType == 2 || dataType == 1)) {
                int[] bandOffsets = new int[]{0};
                this.sampleModel = new PixelInterleavedSampleModel(dataType, 1, 1, 1, 1, bandOffsets);
            } else {
                this.sampleModel = new MultiPixelPackedSampleModel(dataType, 1, 1, bits);
            }
        }
    }
}

