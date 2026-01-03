/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import sun.awt.image.ByteComponentRaster;
import sun.awt.image.BytePackedRaster;

public class OffScreenImageSource
implements ImageProducer {
    BufferedImage image;
    int width;
    int height;
    Hashtable<?, ?> properties;
    private ImageConsumer theConsumer;

    public OffScreenImageSource(BufferedImage image, Hashtable<?, ?> properties) {
        this.image = image;
        this.properties = properties != null ? properties : new Hashtable();
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    public OffScreenImageSource(BufferedImage image) {
        this(image, null);
    }

    @Override
    public synchronized void addConsumer(ImageConsumer ic) {
        this.theConsumer = ic;
        this.produce();
    }

    @Override
    public synchronized boolean isConsumer(ImageConsumer ic) {
        return ic == this.theConsumer;
    }

    @Override
    public synchronized void removeConsumer(ImageConsumer ic) {
        if (this.theConsumer == ic) {
            this.theConsumer = null;
        }
    }

    @Override
    public void startProduction(ImageConsumer ic) {
        this.addConsumer(ic);
    }

    @Override
    public void requestTopDownLeftRightResend(ImageConsumer ic) {
    }

    private void sendPixels() {
        int x;
        ColorModel cm = this.image.getColorModel();
        WritableRaster raster = this.image.getRaster();
        int numDataElements = raster.getNumDataElements();
        int dataType = raster.getDataBuffer().getDataType();
        int[] scanline = new int[this.width * numDataElements];
        boolean needToCvt = true;
        if (cm instanceof IndexColorModel) {
            byte[] pixels = new byte[this.width];
            this.theConsumer.setColorModel(cm);
            if (raster instanceof ByteComponentRaster) {
                needToCvt = false;
                for (y = 0; y < this.height; ++y) {
                    raster.getDataElements(0, y, this.width, 1, pixels);
                    this.theConsumer.setPixels(0, y, this.width, 1, cm, pixels, 0, this.width);
                }
            } else if (raster instanceof BytePackedRaster) {
                needToCvt = false;
                for (y = 0; y < this.height; ++y) {
                    raster.getPixels(0, y, this.width, 1, scanline);
                    for (x = 0; x < this.width; ++x) {
                        pixels[x] = (byte)scanline[x];
                    }
                    this.theConsumer.setPixels(0, y, this.width, 1, cm, pixels, 0, this.width);
                }
            } else if (dataType == 2 || dataType == 3) {
                needToCvt = false;
                for (y = 0; y < this.height; ++y) {
                    raster.getPixels(0, y, this.width, 1, scanline);
                    this.theConsumer.setPixels(0, y, this.width, 1, cm, scanline, 0, this.width);
                }
            }
        } else if (cm instanceof DirectColorModel) {
            this.theConsumer.setColorModel(cm);
            needToCvt = false;
            switch (dataType) {
                case 3: {
                    for (int y = 0; y < this.height; ++y) {
                        raster.getDataElements(0, y, this.width, 1, scanline);
                        this.theConsumer.setPixels(0, y, this.width, 1, cm, scanline, 0, this.width);
                    }
                    break;
                }
                case 0: {
                    byte[] bscanline = new byte[this.width];
                    for (int y = 0; y < this.height; ++y) {
                        raster.getDataElements(0, y, this.width, 1, bscanline);
                        for (x = 0; x < this.width; ++x) {
                            scanline[x] = bscanline[x] & 0xFF;
                        }
                        this.theConsumer.setPixels(0, y, this.width, 1, cm, scanline, 0, this.width);
                    }
                    break;
                }
                case 1: {
                    short[] sscanline = new short[this.width];
                    for (int y = 0; y < this.height; ++y) {
                        raster.getDataElements(0, y, this.width, 1, sscanline);
                        for (int x2 = 0; x2 < this.width; ++x2) {
                            scanline[x2] = sscanline[x2] & 0xFFFF;
                        }
                        this.theConsumer.setPixels(0, y, this.width, 1, cm, scanline, 0, this.width);
                    }
                    break;
                }
                default: {
                    needToCvt = true;
                }
            }
        }
        if (needToCvt) {
            ColorModel newcm = ColorModel.getRGBdefault();
            this.theConsumer.setColorModel(newcm);
            for (int y = 0; y < this.height; ++y) {
                for (x = 0; x < this.width; ++x) {
                    scanline[x] = this.image.getRGB(x, y);
                }
                this.theConsumer.setPixels(0, y, this.width, 1, newcm, scanline, 0, this.width);
            }
        }
    }

    private void produce() {
        block5: {
            try {
                this.theConsumer.setDimensions(this.image.getWidth(), this.image.getHeight());
                this.theConsumer.setProperties(this.properties);
                this.sendPixels();
                this.theConsumer.imageComplete(2);
                if (this.theConsumer != null) {
                    try {
                        this.theConsumer.imageComplete(3);
                    }
                    catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (NullPointerException e) {
                if (this.theConsumer == null) break block5;
                e.printStackTrace();
                this.theConsumer.imageComplete(1);
            }
        }
    }
}

