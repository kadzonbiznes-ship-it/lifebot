/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import sun.awt.image.BufferedImageDevice;

public final class BufferedImageGraphicsConfig
extends GraphicsConfiguration {
    private static final int numconfigs = 12;
    private static BufferedImageGraphicsConfig[] standardConfigs = new BufferedImageGraphicsConfig[12];
    private static BufferedImageGraphicsConfig[] scaledConfigs = new BufferedImageGraphicsConfig[12];
    private final GraphicsDevice device;
    private final ColorModel model;
    private final Raster raster;
    private final double scaleX;
    private final double scaleY;

    public static BufferedImageGraphicsConfig getConfig(BufferedImage bImg) {
        return BufferedImageGraphicsConfig.getConfig(bImg, 1.0, 1.0);
    }

    public static BufferedImageGraphicsConfig getConfig(BufferedImage bImg, double scaleX, double scaleY) {
        BufferedImageGraphicsConfig ret;
        BufferedImageGraphicsConfig[] configs;
        int type = bImg.getType();
        BufferedImageGraphicsConfig[] bufferedImageGraphicsConfigArray = configs = scaleX == 1.0 && scaleY == 1.0 ? standardConfigs : scaledConfigs;
        if (type > 0 && type < 12 && (ret = configs[type]) != null && ret.scaleX == scaleX && ret.scaleY == scaleY) {
            return ret;
        }
        ret = new BufferedImageGraphicsConfig(bImg, null, scaleX, scaleY);
        if (type > 0 && type < 12) {
            configs[type] = ret;
        }
        return ret;
    }

    public BufferedImageGraphicsConfig(BufferedImage bufImg, Component comp, double scaleX, double scaleY) {
        if (comp == null) {
            this.device = new BufferedImageDevice(this);
        } else {
            Graphics2D g2d = (Graphics2D)comp.getGraphics();
            this.device = g2d.getDeviceConfiguration().getDevice();
        }
        this.model = bufImg.getColorModel();
        this.raster = bufImg.getRaster().createCompatibleWritableRaster(1, 1);
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    @Override
    public GraphicsDevice getDevice() {
        return this.device;
    }

    @Override
    public BufferedImage createCompatibleImage(int width, int height) {
        WritableRaster wr = this.raster.createCompatibleWritableRaster(width, height);
        return new BufferedImage(this.model, wr, this.model.isAlphaPremultiplied(), null);
    }

    @Override
    public ColorModel getColorModel() {
        return this.model;
    }

    @Override
    public ColorModel getColorModel(int transparency) {
        if (this.model.getTransparency() == transparency) {
            return this.model;
        }
        switch (transparency) {
            case 1: {
                return new DirectColorModel(24, 0xFF0000, 65280, 255);
            }
            case 2: {
                return new DirectColorModel(25, 0xFF0000, 65280, 255, 0x1000000);
            }
            case 3: {
                return ColorModel.getRGBdefault();
            }
        }
        return null;
    }

    @Override
    public AffineTransform getDefaultTransform() {
        return AffineTransform.getScaleInstance(this.scaleX, this.scaleY);
    }

    @Override
    public AffineTransform getNormalizingTransform() {
        return new AffineTransform();
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
}

