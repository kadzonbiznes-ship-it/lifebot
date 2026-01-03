/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.image.ColorModel;
import java.awt.image.ImageFilter;
import java.util.Hashtable;

public class ReplicateScaleFilter
extends ImageFilter {
    protected int srcWidth;
    protected int srcHeight;
    protected int destWidth;
    protected int destHeight;
    protected int[] srcrows;
    protected int[] srccols;
    protected Object outpixbuf;

    public ReplicateScaleFilter(int width, int height) {
        if (width == 0 || height == 0) {
            throw new IllegalArgumentException("Width (" + width + ") and height (" + height + ") must be non-zero");
        }
        this.destWidth = width;
        this.destHeight = height;
    }

    @Override
    public void setProperties(Hashtable<?, ?> props) {
        Hashtable p = (Hashtable)props.clone();
        String key = "rescale";
        String val = this.destWidth + "x" + this.destHeight;
        Object o = p.get(key);
        if (o instanceof String) {
            String s = (String)o;
            val = s + ", " + val;
        }
        p.put(key, val);
        super.setProperties(p);
    }

    @Override
    public void setDimensions(int w, int h) {
        this.srcWidth = w;
        this.srcHeight = h;
        if (this.destWidth < 0) {
            if (this.destHeight < 0) {
                this.destWidth = this.srcWidth;
                this.destHeight = this.srcHeight;
            } else {
                this.destWidth = this.srcWidth * this.destHeight / this.srcHeight;
            }
        } else if (this.destHeight < 0) {
            this.destHeight = this.srcHeight * this.destWidth / this.srcWidth;
        }
        this.consumer.setDimensions(this.destWidth, this.destHeight);
    }

    private void calculateMaps() {
        this.srcrows = new int[this.destHeight + 1];
        for (int y = 0; y <= this.destHeight; ++y) {
            this.srcrows[y] = (2 * y * this.srcHeight + this.srcHeight) / (2 * this.destHeight);
        }
        this.srccols = new int[this.destWidth + 1];
        for (int x = 0; x <= this.destWidth; ++x) {
            this.srccols[x] = (2 * x * this.srcWidth + this.srcWidth) / (2 * this.destWidth);
        }
    }

    @Override
    public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
        int sy;
        byte[] outpix;
        if (this.srcrows == null || this.srccols == null) {
            this.calculateMaps();
        }
        int dx1 = (2 * x * this.destWidth + this.srcWidth - 1) / (2 * this.srcWidth);
        int dy1 = (2 * y * this.destHeight + this.srcHeight - 1) / (2 * this.srcHeight);
        Object object = this.outpixbuf;
        if (object instanceof byte[]) {
            byte[] outbytes = (byte[])object;
            outpix = outbytes;
        } else {
            outpix = new byte[this.destWidth];
            this.outpixbuf = outpix;
        }
        int dy = dy1;
        while ((sy = this.srcrows[dy]) < y + h) {
            int sx;
            int srcoff = off + scansize * (sy - y);
            int dx = dx1;
            while ((sx = this.srccols[dx]) < x + w) {
                outpix[dx] = pixels[srcoff + sx - x];
                ++dx;
            }
            if (dx > dx1) {
                this.consumer.setPixels(dx1, dy, dx - dx1, 1, model, outpix, dx1, this.destWidth);
            }
            ++dy;
        }
    }

    @Override
    public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
        int sy;
        int[] outpix;
        if (this.srcrows == null || this.srccols == null) {
            this.calculateMaps();
        }
        int dx1 = (2 * x * this.destWidth + this.srcWidth - 1) / (2 * this.srcWidth);
        int dy1 = (2 * y * this.destHeight + this.srcHeight - 1) / (2 * this.srcHeight);
        Object object = this.outpixbuf;
        if (object instanceof int[]) {
            int[] outints = (int[])object;
            outpix = outints;
        } else {
            outpix = new int[this.destWidth];
            this.outpixbuf = outpix;
        }
        int dy = dy1;
        while ((sy = this.srcrows[dy]) < y + h) {
            int sx;
            int srcoff = off + scansize * (sy - y);
            int dx = dx1;
            while ((sx = this.srccols[dx]) < x + w) {
                outpix[dx] = pixels[srcoff + sx - x];
                ++dx;
            }
            if (dx > dx1) {
                this.consumer.setPixels(dx1, dy, dx - dx1, 1, model, outpix, dx1, this.destWidth);
            }
            ++dy;
        }
    }
}

