/*
 * Decompiled with CFR 0.152.
 */
package com.sun.imageio.plugins.jpeg;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.plugins.jpeg.JPEGHuffmanTable;
import javax.imageio.plugins.jpeg.JPEGQTable;

public class JPEG {
    public static final int TEM = 1;
    public static final int SOF0 = 192;
    public static final int SOF1 = 193;
    public static final int SOF2 = 194;
    public static final int SOF3 = 195;
    public static final int DHT = 196;
    public static final int SOF5 = 197;
    public static final int SOF6 = 198;
    public static final int SOF7 = 199;
    public static final int JPG = 200;
    public static final int SOF9 = 201;
    public static final int SOF10 = 202;
    public static final int SOF11 = 203;
    public static final int DAC = 204;
    public static final int SOF13 = 205;
    public static final int SOF14 = 206;
    public static final int SOF15 = 207;
    public static final int RST0 = 208;
    public static final int RST1 = 209;
    public static final int RST2 = 210;
    public static final int RST3 = 211;
    public static final int RST4 = 212;
    public static final int RST5 = 213;
    public static final int RST6 = 214;
    public static final int RST7 = 215;
    public static final int RESTART_RANGE = 8;
    public static final int SOI = 216;
    public static final int EOI = 217;
    public static final int SOS = 218;
    public static final int DQT = 219;
    public static final int DNL = 220;
    public static final int DRI = 221;
    public static final int DHP = 222;
    public static final int EXP = 223;
    public static final int APP0 = 224;
    public static final int APP1 = 225;
    public static final int APP2 = 226;
    public static final int APP3 = 227;
    public static final int APP4 = 228;
    public static final int APP5 = 229;
    public static final int APP6 = 230;
    public static final int APP7 = 231;
    public static final int APP8 = 232;
    public static final int APP9 = 233;
    public static final int APP10 = 234;
    public static final int APP11 = 235;
    public static final int APP12 = 236;
    public static final int APP13 = 237;
    public static final int APP14 = 238;
    public static final int APP15 = 239;
    public static final int COM = 254;
    public static final int DENSITY_UNIT_ASPECT_RATIO = 0;
    public static final int DENSITY_UNIT_DOTS_INCH = 1;
    public static final int DENSITY_UNIT_DOTS_CM = 2;
    public static final int NUM_DENSITY_UNIT = 3;
    public static final int ADOBE_IMPOSSIBLE = -1;
    public static final int ADOBE_UNKNOWN = 0;
    public static final int ADOBE_YCC = 1;
    public static final int ADOBE_YCCK = 2;
    public static final String vendor = "Oracle Corporation";
    public static final String version = "0.5";
    static final String[] names = new String[]{"JPEG", "jpeg", "JPG", "jpg"};
    static final String[] suffixes = new String[]{"jpg", "jpeg"};
    static final String[] MIMETypes = new String[]{"image/jpeg"};
    public static final String nativeImageMetadataFormatName = "javax_imageio_jpeg_image_1.0";
    public static final String nativeImageMetadataFormatClassName = "com.sun.imageio.plugins.jpeg.JPEGImageMetadataFormat";
    public static final String nativeStreamMetadataFormatName = "javax_imageio_jpeg_stream_1.0";
    public static final String nativeStreamMetadataFormatClassName = "com.sun.imageio.plugins.jpeg.JPEGStreamMetadataFormat";
    public static final int JCS_UNKNOWN = 0;
    public static final int JCS_GRAYSCALE = 1;
    public static final int JCS_RGB = 2;
    public static final int JCS_YCbCr = 3;
    public static final int JCS_CMYK = 4;
    public static final int JCS_YCCK = 5;
    public static final int NUM_JCS_CODES = 6;
    static final int[][] bandOffsets = new int[][]{{0}, {0, 1}, {0, 1, 2}, {0, 1, 2, 3}};
    static final int[] bOffsRGB = new int[]{2, 1, 0};
    static final ColorSpace sRGB = ColorSpace.getInstance(1000);
    public static final float DEFAULT_QUALITY = 0.75f;

    static boolean isJFIFcompliant(ImageTypeSpecifier imageType, boolean input) {
        ColorModel cm = imageType.getColorModel();
        if (cm.hasAlpha()) {
            return false;
        }
        int numComponents = imageType.getNumComponents();
        if (numComponents == 1) {
            return true;
        }
        if (numComponents != 3) {
            return false;
        }
        return input ? cm.getColorSpace().getType() == 5 : cm.getColorSpace().getType() == 3;
    }

    static int transformForType(ImageTypeSpecifier imageType, boolean input) {
        int retval = -1;
        ColorModel cm = imageType.getColorModel();
        switch (cm.getColorSpace().getType()) {
            case 6: {
                retval = 0;
                break;
            }
            case 5: {
                retval = input ? 1 : 0;
                break;
            }
            case 3: {
                retval = 1;
                break;
            }
            case 9: {
                retval = input ? 2 : -1;
            }
        }
        return retval;
    }

    static float convertToLinearQuality(float quality) {
        if (quality <= 0.0f) {
            quality = 0.01f;
        }
        if (quality > 1.0f) {
            quality = 1.0f;
        }
        quality = quality < 0.5f ? 0.5f / quality : 2.0f - quality * 2.0f;
        return quality;
    }

    static JPEGQTable[] getDefaultQTables() {
        JPEGQTable[] qTables = new JPEGQTable[]{JPEGQTable.K1Div2Luminance, JPEGQTable.K2Div2Chrominance};
        return qTables;
    }

    static JPEGHuffmanTable[] getDefaultHuffmanTables(boolean wantDC) {
        JPEGHuffmanTable[] tables = new JPEGHuffmanTable[2];
        if (wantDC) {
            tables[0] = JPEGHuffmanTable.StdDCLuminance;
            tables[1] = JPEGHuffmanTable.StdDCChrominance;
        } else {
            tables[0] = JPEGHuffmanTable.StdACLuminance;
            tables[1] = JPEGHuffmanTable.StdACChrominance;
        }
        return tables;
    }
}

