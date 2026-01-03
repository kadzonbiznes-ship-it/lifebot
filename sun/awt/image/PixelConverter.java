/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.image.ColorModel;

public class PixelConverter {
    public static final PixelConverter instance = new PixelConverter();
    protected int alphaMask = 0;

    protected PixelConverter() {
    }

    public int rgbToPixel(int rgb, ColorModel cm) {
        Object obj = cm.getDataElements(rgb, null);
        switch (cm.getTransferType()) {
            case 0: {
                byte[] bytearr = (byte[])obj;
                int pix = 0;
                switch (bytearr.length) {
                    default: {
                        pix = bytearr[3] << 24;
                    }
                    case 3: {
                        pix |= (bytearr[2] & 0xFF) << 16;
                    }
                    case 2: {
                        pix |= (bytearr[1] & 0xFF) << 8;
                    }
                    case 1: 
                }
                return pix |= bytearr[0] & 0xFF;
            }
            case 1: 
            case 2: {
                short[] shortarr = (short[])obj;
                return (shortarr.length > 1 ? shortarr[1] << 16 : 0) | shortarr[0] & 0xFFFF;
            }
            case 3: {
                return ((int[])obj)[0];
            }
        }
        return rgb;
    }

    public int pixelToRgb(int pixel, ColorModel cm) {
        return pixel;
    }

    public final int getAlphaMask() {
        return this.alphaMask;
    }

    public static class UshortGray
    extends ByteGray {
        static final double SHORT_MULT = 257.0;
        static final double USHORT_RED_MULT = 76.843;
        static final double USHORT_GRN_MULT = 150.85899999999998;
        static final double USHORT_BLU_MULT = 29.298000000000002;
        public static final PixelConverter instance = new UshortGray();

        private UshortGray() {
        }

        @Override
        public int rgbToPixel(int rgb, ColorModel cm) {
            int red = rgb >> 16 & 0xFF;
            int grn = rgb >> 8 & 0xFF;
            int blu = rgb & 0xFF;
            return (int)((double)red * 76.843 + (double)grn * 150.85899999999998 + (double)blu * 29.298000000000002 + 0.5);
        }

        @Override
        public int pixelToRgb(int pixel, ColorModel cm) {
            return ((0xFF00 | (pixel >>= 8)) << 8 | pixel) << 8 | pixel;
        }
    }

    public static class ByteGray
    extends PixelConverter {
        static final double RED_MULT = 0.299;
        static final double GRN_MULT = 0.587;
        static final double BLU_MULT = 0.114;
        public static final PixelConverter instance = new ByteGray();

        private ByteGray() {
        }

        @Override
        public int rgbToPixel(int rgb, ColorModel cm) {
            int red = rgb >> 16 & 0xFF;
            int grn = rgb >> 8 & 0xFF;
            int blu = rgb & 0xFF;
            return (int)((double)red * 0.299 + (double)grn * 0.587 + (double)blu * 0.114 + 0.5);
        }

        @Override
        public int pixelToRgb(int pixel, ColorModel cm) {
            return ((0xFF00 | pixel) << 8 | pixel) << 8 | pixel;
        }
    }

    public static class ArgbBm
    extends PixelConverter {
        public static final PixelConverter instance = new ArgbBm();

        private ArgbBm() {
        }

        @Override
        public int rgbToPixel(int rgb, ColorModel cm) {
            return rgb | rgb >> 31 << 24;
        }

        @Override
        public int pixelToRgb(int pixel, ColorModel cm) {
            return pixel << 7 >> 7;
        }
    }

    public static class ArgbPre
    extends PixelConverter {
        public static final PixelConverter instance = new ArgbPre();

        private ArgbPre() {
            this.alphaMask = -16777216;
        }

        @Override
        public int rgbToPixel(int rgb, ColorModel cm) {
            if (rgb >> 24 == -1) {
                return rgb;
            }
            int a = rgb >>> 24;
            int r = rgb >> 16 & 0xFF;
            int g = rgb >> 8 & 0xFF;
            int b = rgb & 0xFF;
            int a2 = a + (a >> 7);
            r = r * a2 >> 8;
            g = g * a2 >> 8;
            b = b * a2 >> 8;
            return a << 24 | r << 16 | g << 8 | b;
        }

        @Override
        public int pixelToRgb(int pixel, ColorModel cm) {
            int a = pixel >>> 24;
            if (a == 255 || a == 0) {
                return pixel;
            }
            int r = pixel >> 16 & 0xFF;
            int g = pixel >> 8 & 0xFF;
            int b = pixel & 0xFF;
            r = ((r << 8) - r) / a;
            g = ((g << 8) - g) / a;
            b = ((b << 8) - b) / a;
            return a << 24 | r << 16 | g << 8 | b;
        }
    }

    public static class RgbaPre
    extends PixelConverter {
        public static final PixelConverter instance = new RgbaPre();

        private RgbaPre() {
            this.alphaMask = 255;
        }

        @Override
        public int rgbToPixel(int rgb, ColorModel cm) {
            if (rgb >> 24 == -1) {
                return rgb << 8 | rgb >>> 24;
            }
            int a = rgb >>> 24;
            int r = rgb >> 16 & 0xFF;
            int g = rgb >> 8 & 0xFF;
            int b = rgb & 0xFF;
            int a2 = a + (a >> 7);
            r = r * a2 >> 8;
            g = g * a2 >> 8;
            b = b * a2 >> 8;
            return r << 24 | g << 16 | b << 8 | a;
        }

        @Override
        public int pixelToRgb(int pixel, ColorModel cm) {
            int a = pixel & 0xFF;
            if (a == 255 || a == 0) {
                return pixel >>> 8 | pixel << 24;
            }
            int r = pixel >>> 24;
            int g = pixel >> 16 & 0xFF;
            int b = pixel >> 8 & 0xFF;
            r = ((r << 8) - r) / a;
            g = ((g << 8) - g) / a;
            b = ((b << 8) - b) / a;
            return r << 24 | g << 16 | b << 8 | a;
        }
    }

    public static class Rgba
    extends PixelConverter {
        public static final PixelConverter instance = new Rgba();

        private Rgba() {
            this.alphaMask = 255;
        }

        @Override
        public int rgbToPixel(int rgb, ColorModel cm) {
            return rgb << 8 | rgb >>> 24;
        }

        @Override
        public int pixelToRgb(int pixel, ColorModel cm) {
            return pixel << 24 | pixel >>> 8;
        }
    }

    public static class Bgrx
    extends PixelConverter {
        public static final PixelConverter instance = new Bgrx();

        private Bgrx() {
        }

        @Override
        public int rgbToPixel(int rgb, ColorModel cm) {
            return rgb << 24 | (rgb & 0xFF00) << 8 | rgb >> 8 & 0xFF00;
        }

        @Override
        public int pixelToRgb(int pixel, ColorModel cm) {
            return 0xFF000000 | (pixel & 0xFF00) << 8 | pixel >> 8 & 0xFF00 | pixel >>> 24;
        }
    }

    public static class Xbgr
    extends PixelConverter {
        public static final PixelConverter instance = new Xbgr();

        private Xbgr() {
        }

        @Override
        public int rgbToPixel(int rgb, ColorModel cm) {
            return (rgb & 0xFF) << 16 | rgb & 0xFF00 | rgb >> 16 & 0xFF;
        }

        @Override
        public int pixelToRgb(int pixel, ColorModel cm) {
            return 0xFF000000 | (pixel & 0xFF) << 16 | pixel & 0xFF00 | pixel >> 16 & 0xFF;
        }
    }

    public static class Ushort4444Argb
    extends PixelConverter {
        public static final PixelConverter instance = new Ushort4444Argb();

        private Ushort4444Argb() {
            this.alphaMask = 61440;
        }

        @Override
        public int rgbToPixel(int rgb, ColorModel cm) {
            int a = rgb >> 16 & 0xF000;
            int r = rgb >> 12 & 0xF00;
            int g = rgb >> 8 & 0xF0;
            int b = rgb >> 4 & 0xF;
            return a | r | g | b;
        }

        @Override
        public int pixelToRgb(int pixel, ColorModel cm) {
            int a = pixel & 0xF000;
            a = (pixel << 16 | pixel << 12) & 0xFF000000;
            int r = pixel & 0xF00;
            r = (pixel << 12 | pixel << 8) & 0xFF0000;
            int g = pixel & 0xF0;
            g = (pixel << 8 | pixel << 4) & 0xFF00;
            int b = pixel & 0xF;
            b = (pixel << 4 | pixel << 0) & 0xFF;
            return a | r | g | b;
        }
    }

    public static class Ushort555Rgb
    extends PixelConverter {
        public static final PixelConverter instance = new Ushort555Rgb();

        private Ushort555Rgb() {
        }

        @Override
        public int rgbToPixel(int rgb, ColorModel cm) {
            return rgb >> 9 & 0x7C00 | rgb >> 6 & 0x3E0 | rgb >> 3 & 0x1F;
        }

        @Override
        public int pixelToRgb(int pixel, ColorModel cm) {
            int r = pixel >> 10 & 0x1F;
            r = r << 3 | r >> 2;
            int g = pixel >> 5 & 0x1F;
            g = g << 3 | g >> 2;
            int b = pixel & 0x1F;
            b = b << 3 | b >> 2;
            return 0xFF000000 | r << 16 | g << 8 | b;
        }
    }

    public static class Ushort555Rgbx
    extends PixelConverter {
        public static final PixelConverter instance = new Ushort555Rgbx();

        private Ushort555Rgbx() {
        }

        @Override
        public int rgbToPixel(int rgb, ColorModel cm) {
            return rgb >> 8 & 0xF800 | rgb >> 5 & 0x7C0 | rgb >> 2 & 0x3E;
        }

        @Override
        public int pixelToRgb(int pixel, ColorModel cm) {
            int r = pixel >> 11 & 0x1F;
            r = r << 3 | r >> 2;
            int g = pixel >> 6 & 0x1F;
            g = g << 3 | g >> 2;
            int b = pixel >> 1 & 0x1F;
            b = b << 3 | b >> 2;
            return 0xFF000000 | r << 16 | g << 8 | b;
        }
    }

    public static class Ushort565Rgb
    extends PixelConverter {
        public static final PixelConverter instance = new Ushort565Rgb();

        private Ushort565Rgb() {
        }

        @Override
        public int rgbToPixel(int rgb, ColorModel cm) {
            return rgb >> 8 & 0xF800 | rgb >> 5 & 0x7E0 | rgb >> 3 & 0x1F;
        }

        @Override
        public int pixelToRgb(int pixel, ColorModel cm) {
            int r = pixel >> 11 & 0x1F;
            r = r << 3 | r >> 2;
            int g = pixel >> 5 & 0x3F;
            g = g << 2 | g >> 4;
            int b = pixel & 0x1F;
            b = b << 3 | b >> 2;
            return 0xFF000000 | r << 16 | g << 8 | b;
        }
    }

    public static class Argb
    extends PixelConverter {
        public static final PixelConverter instance = new Argb();

        private Argb() {
            this.alphaMask = -16777216;
        }

        @Override
        public int rgbToPixel(int rgb, ColorModel cm) {
            return rgb;
        }

        @Override
        public int pixelToRgb(int pixel, ColorModel cm) {
            return pixel;
        }
    }

    public static class Xrgb
    extends PixelConverter {
        public static final PixelConverter instance = new Xrgb();

        private Xrgb() {
        }

        @Override
        public int rgbToPixel(int rgb, ColorModel cm) {
            return rgb;
        }

        @Override
        public int pixelToRgb(int pixel, ColorModel cm) {
            return 0xFF000000 | pixel;
        }
    }

    public static class Rgbx
    extends PixelConverter {
        public static final PixelConverter instance = new Rgbx();

        private Rgbx() {
        }

        @Override
        public int rgbToPixel(int rgb, ColorModel cm) {
            return rgb << 8;
        }

        @Override
        public int pixelToRgb(int pixel, ColorModel cm) {
            return 0xFF000000 | pixel >> 8;
        }
    }
}

