/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.GraphicsDevice;
import java.awt.ImageCapabilities;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;
import sun.awt.image.SunVolatileImage;

public abstract class GraphicsConfiguration {
    private static BufferCapabilities defaultBufferCaps;
    private static ImageCapabilities defaultImageCaps;

    protected GraphicsConfiguration() {
    }

    public abstract GraphicsDevice getDevice();

    public BufferedImage createCompatibleImage(int width, int height) {
        ColorModel model = this.getColorModel();
        WritableRaster raster = model.createCompatibleWritableRaster(width, height);
        return new BufferedImage(model, raster, model.isAlphaPremultiplied(), null);
    }

    public BufferedImage createCompatibleImage(int width, int height, int transparency) {
        if (this.getColorModel().getTransparency() == transparency) {
            return this.createCompatibleImage(width, height);
        }
        ColorModel cm = this.getColorModel(transparency);
        if (cm == null) {
            throw new IllegalArgumentException("Unknown transparency: " + transparency);
        }
        WritableRaster wr = cm.createCompatibleWritableRaster(width, height);
        return new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), null);
    }

    public VolatileImage createCompatibleVolatileImage(int width, int height) {
        VolatileImage vi;
        block2: {
            vi = null;
            try {
                vi = this.createCompatibleVolatileImage(width, height, null, 1);
            }
            catch (AWTException e) {
                if ($assertionsDisabled) break block2;
                throw new AssertionError();
            }
        }
        return vi;
    }

    public VolatileImage createCompatibleVolatileImage(int width, int height, int transparency) {
        VolatileImage vi;
        block2: {
            vi = null;
            try {
                vi = this.createCompatibleVolatileImage(width, height, null, transparency);
            }
            catch (AWTException e) {
                if ($assertionsDisabled) break block2;
                throw new AssertionError();
            }
        }
        return vi;
    }

    public VolatileImage createCompatibleVolatileImage(int width, int height, ImageCapabilities caps) throws AWTException {
        return this.createCompatibleVolatileImage(width, height, caps, 1);
    }

    public VolatileImage createCompatibleVolatileImage(int width, int height, ImageCapabilities caps, int transparency) throws AWTException {
        SunVolatileImage vi = new SunVolatileImage(this, width, height, transparency, caps);
        if (caps != null && caps.isAccelerated() && !((VolatileImage)vi).getCapabilities().isAccelerated()) {
            throw new AWTException("Supplied image capabilities could not be met by this graphics configuration.");
        }
        return vi;
    }

    public abstract ColorModel getColorModel();

    public abstract ColorModel getColorModel(int var1);

    public abstract AffineTransform getDefaultTransform();

    public abstract AffineTransform getNormalizingTransform();

    public abstract Rectangle getBounds();

    public BufferCapabilities getBufferCapabilities() {
        if (defaultBufferCaps == null) {
            defaultBufferCaps = new DefaultBufferCapabilities(this.getImageCapabilities());
        }
        return defaultBufferCaps;
    }

    public ImageCapabilities getImageCapabilities() {
        if (defaultImageCaps == null) {
            defaultImageCaps = new ImageCapabilities(false);
        }
        return defaultImageCaps;
    }

    public boolean isTranslucencyCapable() {
        return false;
    }

    private static class DefaultBufferCapabilities
    extends BufferCapabilities {
        public DefaultBufferCapabilities(ImageCapabilities imageCaps) {
            super(imageCaps, imageCaps, null);
        }
    }
}

