/*
 * Decompiled with CFR 0.152.
 */
package sun.print;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import sun.print.PrinterGraphicsDevice;

public final class PrinterGraphicsConfig
extends GraphicsConfiguration {
    static ColorModel theModel;
    private final GraphicsDevice device;
    private final int pageWidth;
    private final int pageHeight;
    private final AffineTransform deviceTransform;

    public PrinterGraphicsConfig(String printerID, AffineTransform deviceTx, int pageWid, int pageHgt) {
        this.pageWidth = pageWid;
        this.pageHeight = pageHgt;
        this.deviceTransform = deviceTx;
        this.device = new PrinterGraphicsDevice(this, printerID);
    }

    @Override
    public GraphicsDevice getDevice() {
        return this.device;
    }

    @Override
    public ColorModel getColorModel() {
        if (theModel == null) {
            BufferedImage bufImg = new BufferedImage(1, 1, 5);
            theModel = bufImg.getColorModel();
        }
        return theModel;
    }

    @Override
    public ColorModel getColorModel(int transparency) {
        switch (transparency) {
            case 1: {
                return this.getColorModel();
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
        return new AffineTransform(this.deviceTransform);
    }

    @Override
    public AffineTransform getNormalizingTransform() {
        return new AffineTransform();
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(0, 0, this.pageWidth, this.pageHeight);
    }
}

