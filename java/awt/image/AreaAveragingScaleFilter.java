/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.image.ColorModel;
import java.awt.image.ReplicateScaleFilter;

public class AreaAveragingScaleFilter
extends ReplicateScaleFilter {
    private static final ColorModel rgbmodel = ColorModel.getRGBdefault();
    private static final int neededHints = 6;
    private boolean passthrough;
    private float[] reds;
    private float[] greens;
    private float[] blues;
    private float[] alphas;
    private int savedy;
    private int savedyrem;

    public AreaAveragingScaleFilter(int width, int height) {
        super(width, height);
    }

    @Override
    public void setHints(int hints) {
        this.passthrough = (hints & 6) != 6;
        super.setHints(hints);
    }

    private void makeAccumBuffers() {
        this.reds = new float[this.destWidth];
        this.greens = new float[this.destWidth];
        this.blues = new float[this.destWidth];
        this.alphas = new float[this.destWidth];
    }

    private int[] calcRow() {
        float origmult = (float)this.srcWidth * (float)this.srcHeight;
        if (!(this.outpixbuf instanceof int[])) {
            this.outpixbuf = new int[this.destWidth];
        }
        int[] outpix = (int[])this.outpixbuf;
        for (int x = 0; x < this.destWidth; ++x) {
            float mult = origmult;
            int a = Math.round(this.alphas[x] / mult);
            if (a <= 0) {
                a = 0;
            } else if (a >= 255) {
                a = 255;
            } else {
                mult = this.alphas[x] / 255.0f;
            }
            int r = Math.round(this.reds[x] / mult);
            int g = Math.round(this.greens[x] / mult);
            int b = Math.round(this.blues[x] / mult);
            if (r < 0) {
                r = 0;
            } else if (r > 255) {
                r = 255;
            }
            if (g < 0) {
                g = 0;
            } else if (g > 255) {
                g = 255;
            }
            if (b < 0) {
                b = 0;
            } else if (b > 255) {
                b = 255;
            }
            outpix[x] = a << 24 | r << 16 | g << 8 | b;
        }
        return outpix;
    }

    private void accumPixels(int x, int y, int w, int h, ColorModel model, Object pixels, int off, int scansize) {
        int dyrem;
        int dy;
        if (this.reds == null) {
            this.makeAccumBuffers();
        }
        int sy = y;
        int syrem = this.destHeight;
        if (sy == 0) {
            dy = 0;
            dyrem = 0;
        } else {
            dy = this.savedy;
            dyrem = this.savedyrem;
        }
        while (sy < y + h) {
            if (dyrem == 0) {
                for (int i = 0; i < this.destWidth; ++i) {
                    this.blues[i] = 0.0f;
                    this.greens[i] = 0.0f;
                    this.reds[i] = 0.0f;
                    this.alphas[i] = 0.0f;
                }
                dyrem = this.srcHeight;
            }
            int amty = syrem < dyrem ? syrem : dyrem;
            int sx = 0;
            int dx = 0;
            int sxrem = 0;
            int dxrem = this.srcWidth;
            float a = 0.0f;
            float r = 0.0f;
            float g = 0.0f;
            float b = 0.0f;
            while (sx < w) {
                if (sxrem == 0) {
                    sxrem = this.destWidth;
                    int rgb = pixels instanceof byte[] ? ((byte[])pixels)[off + sx] & 0xFF : ((int[])pixels)[off + sx];
                    rgb = model.getRGB(rgb);
                    a = rgb >>> 24;
                    r = rgb >> 16 & 0xFF;
                    g = rgb >> 8 & 0xFF;
                    b = rgb & 0xFF;
                    if (a != 255.0f) {
                        float ascale = a / 255.0f;
                        r *= ascale;
                        g *= ascale;
                        b *= ascale;
                    }
                }
                int amtx = sxrem < dxrem ? sxrem : dxrem;
                float mult = (float)amtx * (float)amty;
                int n = dx;
                this.alphas[n] = this.alphas[n] + mult * a;
                int n2 = dx;
                this.reds[n2] = this.reds[n2] + mult * r;
                int n3 = dx;
                this.greens[n3] = this.greens[n3] + mult * g;
                int n4 = dx;
                this.blues[n4] = this.blues[n4] + mult * b;
                if ((sxrem -= amtx) == 0) {
                    ++sx;
                }
                if ((dxrem -= amtx) != 0) continue;
                ++dx;
                dxrem = this.srcWidth;
            }
            if ((dyrem -= amty) == 0) {
                int[] outpix = this.calcRow();
                do {
                    this.consumer.setPixels(0, dy, this.destWidth, 1, rgbmodel, outpix, 0, this.destWidth);
                    ++dy;
                } while ((syrem -= amty) >= amty && amty == this.srcHeight);
            } else {
                syrem -= amty;
            }
            if (syrem != 0) continue;
            syrem = this.destHeight;
            ++sy;
            off += scansize;
        }
        this.savedyrem = dyrem;
        this.savedy = dy;
    }

    @Override
    public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
        if (this.passthrough) {
            super.setPixels(x, y, w, h, model, pixels, off, scansize);
        } else {
            this.accumPixels(x, y, w, h, model, pixels, off, scansize);
        }
    }

    @Override
    public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
        if (this.passthrough) {
            super.setPixels(x, y, w, h, model, pixels, off, scansize);
        } else {
            this.accumPixels(x, y, w, h, model, pixels, off, scansize);
        }
    }
}

