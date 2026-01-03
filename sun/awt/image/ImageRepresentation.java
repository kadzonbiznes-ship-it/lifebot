/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import sun.awt.image.ByteComponentRaster;
import sun.awt.image.ImageWatched;
import sun.awt.image.InputStreamImageSource;
import sun.awt.image.IntegerComponentRaster;
import sun.awt.image.NativeLibLoader;
import sun.awt.image.SunWritableRaster;
import sun.awt.image.ToolkitImage;

public class ImageRepresentation
extends ImageWatched
implements ImageConsumer {
    InputStreamImageSource src;
    ToolkitImage image;
    int tag;
    long pData;
    int width = -1;
    int height = -1;
    int hints;
    int availinfo;
    Rectangle newbits;
    BufferedImage bimage;
    WritableRaster biRaster;
    protected ColorModel cmodel;
    ColorModel srcModel = null;
    int[] srcLUT = null;
    int srcLUTtransIndex = -1;
    int numSrcLUT = 0;
    boolean forceCMhint;
    int sstride;
    boolean isDefaultBI = false;
    boolean isSameCM = false;
    static boolean s_useNative;
    private boolean consuming = false;
    private int numWaiters;

    private static native void initIDs();

    public ImageRepresentation(ToolkitImage im, ColorModel cmodel, boolean forceCMhint) {
        this.image = im;
        if (this.image.getSource() instanceof InputStreamImageSource) {
            this.src = (InputStreamImageSource)this.image.getSource();
        }
        this.setColorModel(cmodel);
        this.forceCMhint = forceCMhint;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized void reconstruct(int flags) {
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        int missinginfo = flags & ~this.availinfo;
        if ((this.availinfo & 0x40) == 0 && missinginfo != 0) {
            ++this.numWaiters;
            try {
                this.startProduction();
                missinginfo = flags & ~this.availinfo;
                while ((this.availinfo & 0x50) == 0 && missinginfo != 0) {
                    try {
                        this.wait();
                    }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        this.decrementWaiters();
                        return;
                    }
                    missinginfo = flags & ~this.availinfo;
                }
            }
            finally {
                this.decrementWaiters();
            }
        }
    }

    @Override
    public void setDimensions(int w, int h) {
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        this.image.setDimensions(w, h);
        this.newInfo(this.image, 3, 0, 0, w, h);
        if (w <= 0 || h <= 0) {
            this.imageComplete(1);
            return;
        }
        if (this.width != w || this.height != h) {
            this.bimage = null;
        }
        this.width = w;
        this.height = h;
        this.availinfo |= 3;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    ColorModel getColorModel() {
        return this.cmodel;
    }

    BufferedImage getBufferedImage() {
        return this.bimage;
    }

    protected BufferedImage createImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?, ?> properties) {
        BufferedImage bi = new BufferedImage(cm, raster, isRasterPremultiplied, null);
        bi.setAccelerationPriority(this.image.getAccelerationPriority());
        return bi;
    }

    @Override
    public void setProperties(Hashtable<?, ?> props) {
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        this.image.setProperties(props);
        this.newInfo(this.image, 4, 0, 0, 0, 0);
    }

    @Override
    public void setColorModel(ColorModel model) {
        DirectColorModel dcm;
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        this.srcModel = model;
        if (model instanceof IndexColorModel) {
            if (model.getTransparency() == 3) {
                this.cmodel = ColorModel.getRGBdefault();
                this.srcLUT = null;
            } else {
                IndexColorModel icm = (IndexColorModel)model;
                this.numSrcLUT = icm.getMapSize();
                this.srcLUT = new int[Math.max(this.numSrcLUT, 256)];
                icm.getRGBs(this.srcLUT);
                this.srcLUTtransIndex = icm.getTransparentPixel();
                this.cmodel = model;
            }
        } else if (this.cmodel == null) {
            this.cmodel = model;
            this.srcLUT = null;
        } else if (model instanceof DirectColorModel && (dcm = (DirectColorModel)model).getRedMask() == 0xFF0000 && dcm.getGreenMask() == 65280 && dcm.getBlueMask() == 255) {
            this.cmodel = model;
            this.srcLUT = null;
        }
        this.isSameCM = this.cmodel == model;
    }

    void createBufferedImage() {
        DirectColorModel dcm;
        this.isDefaultBI = false;
        try {
            this.biRaster = this.cmodel.createCompatibleWritableRaster(this.width, this.height);
            this.bimage = this.createImage(this.cmodel, this.biRaster, this.cmodel.isAlphaPremultiplied(), null);
        }
        catch (Exception e) {
            this.cmodel = ColorModel.getRGBdefault();
            this.biRaster = this.cmodel.createCompatibleWritableRaster(this.width, this.height);
            this.bimage = this.createImage(this.cmodel, this.biRaster, false, null);
        }
        int type = this.bimage.getType();
        if (this.cmodel == ColorModel.getRGBdefault() || type == 1 || type == 3) {
            this.isDefaultBI = true;
        } else if (this.cmodel instanceof DirectColorModel && (dcm = (DirectColorModel)this.cmodel).getRedMask() == 0xFF0000 && dcm.getGreenMask() == 65280 && dcm.getBlueMask() == 255) {
            this.isDefaultBI = true;
        }
    }

    private void convertToRGB() {
        int w = this.bimage.getWidth();
        int h = this.bimage.getHeight();
        int size = w * h;
        DataBufferInt dbi = new DataBufferInt(size);
        int[] newpixels = SunWritableRaster.stealData(dbi, 0);
        if (this.cmodel instanceof IndexColorModel && this.biRaster instanceof ByteComponentRaster && this.biRaster.getNumDataElements() == 1) {
            ByteComponentRaster bct = (ByteComponentRaster)this.biRaster;
            byte[] data = bct.getDataStorage();
            int coff = bct.getDataOffset(0);
            for (int i = 0; i < size; ++i) {
                newpixels[i] = this.srcLUT[data[coff + i] & 0xFF];
            }
        } else {
            Object srcpixels = null;
            int off = 0;
            for (int y = 0; y < h; ++y) {
                for (int x = 0; x < w; ++x) {
                    srcpixels = this.biRaster.getDataElements(x, y, srcpixels);
                    newpixels[off++] = this.cmodel.getRGB(srcpixels);
                }
            }
        }
        SunWritableRaster.markDirty(dbi);
        this.isSameCM = false;
        this.cmodel = ColorModel.getRGBdefault();
        int[] bandMasks = new int[]{0xFF0000, 65280, 255, -16777216};
        this.biRaster = Raster.createPackedRaster(dbi, w, h, w, bandMasks, null);
        this.bimage = this.createImage(this.cmodel, this.biRaster, this.cmodel.isAlphaPremultiplied(), null);
        this.srcLUT = null;
        this.isDefaultBI = true;
    }

    @Override
    public void setHints(int h) {
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        this.hints = h;
    }

    private native boolean setICMpixels(int var1, int var2, int var3, int var4, int[] var5, byte[] var6, int var7, int var8, IntegerComponentRaster var9);

    private native boolean setDiffICM(int var1, int var2, int var3, int var4, int[] var5, int var6, int var7, IndexColorModel var8, byte[] var9, int var10, int var11, ByteComponentRaster var12, int var13);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    @Override
    public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pix, int off, int scansize) {
        block34: {
            int lineOff;
            block40: {
                int[] storage;
                IntegerComponentRaster iraster;
                block39: {
                    int[] storage2;
                    block38: {
                        block35: {
                            block36: {
                                block37: {
                                    lineOff = off;
                                    Object newLUT = null;
                                    if (this.src != null) {
                                        this.src.checkSecurity(null, false);
                                    }
                                    ImageRepresentation imageRepresentation = this;
                                    // MONITORENTER : imageRepresentation
                                    if (this.bimage == null) {
                                        if (this.cmodel == null) {
                                            this.cmodel = model;
                                        }
                                        this.createBufferedImage();
                                    }
                                    if (w <= 0 || h <= 0) {
                                        // MONITOREXIT : imageRepresentation
                                        return;
                                    }
                                    int biWidth = this.biRaster.getWidth();
                                    int biHeight = this.biRaster.getHeight();
                                    int x1 = x + w;
                                    int y1 = y + h;
                                    if (x < 0) {
                                        off -= x;
                                        x = 0;
                                    } else if (x1 < 0) {
                                        x1 = biWidth;
                                    }
                                    if (y < 0) {
                                        off -= y * scansize;
                                        y = 0;
                                    } else if (y1 < 0) {
                                        y1 = biHeight;
                                    }
                                    if (x1 > biWidth) {
                                        x1 = biWidth;
                                    }
                                    if (y1 > biHeight) {
                                        y1 = biHeight;
                                    }
                                    if (x >= x1 || y >= y1) {
                                        // MONITOREXIT : imageRepresentation
                                        return;
                                    }
                                    w = x1 - x;
                                    h = y1 - y;
                                    if (off < 0) throw new ArrayIndexOutOfBoundsException("Data offset out of bounds.");
                                    if (off >= pix.length) {
                                        throw new ArrayIndexOutOfBoundsException("Data offset out of bounds.");
                                    }
                                    int remainder = pix.length - off;
                                    if (remainder < w) {
                                        throw new ArrayIndexOutOfBoundsException("Data array is too short.");
                                    }
                                    int num = scansize < 0 ? off / -scansize + 1 : (scansize > 0 ? (remainder - w) / scansize + 1 : h);
                                    if (h > num) {
                                        throw new ArrayIndexOutOfBoundsException("Data array is too short.");
                                    }
                                    if (this.isSameCM && this.cmodel != model && this.srcLUT != null && model instanceof IndexColorModel && this.biRaster instanceof ByteComponentRaster) {
                                        IndexColorModel icm = (IndexColorModel)model;
                                        ByteComponentRaster bct = (ByteComponentRaster)this.biRaster;
                                        int numlut = this.numSrcLUT;
                                        if (!this.setDiffICM(x, y, w, h, this.srcLUT, this.srcLUTtransIndex, this.numSrcLUT, icm, pix, off, scansize, bct, bct.getDataOffset(0))) {
                                            this.convertToRGB();
                                        } else {
                                            bct.markDirty();
                                            if (numlut != this.numSrcLUT) {
                                                int nbits;
                                                boolean hasAlpha = icm.hasAlpha();
                                                if (this.srcLUTtransIndex != -1) {
                                                    hasAlpha = true;
                                                }
                                                icm = new IndexColorModel(nbits, this.numSrcLUT, this.srcLUT, 0, hasAlpha, this.srcLUTtransIndex, (nbits = icm.getPixelSize()) > 8 ? 1 : 0);
                                                this.cmodel = icm;
                                                this.bimage = this.createImage(icm, bct, false, null);
                                            }
                                            // MONITOREXIT : imageRepresentation
                                            return;
                                        }
                                    }
                                    if (!this.isDefaultBI) break block35;
                                    iraster = (IntegerComponentRaster)this.biRaster;
                                    if (this.srcLUT == null || !(model instanceof IndexColorModel)) break block36;
                                    if (model != this.srcModel) {
                                        ((IndexColorModel)model).getRGBs(this.srcLUT);
                                        this.srcModel = model;
                                    }
                                    if (!s_useNative) break block37;
                                    if (!this.setICMpixels(x, y, w, h, this.srcLUT, pix, off, scansize, iraster)) {
                                        this.abort();
                                        // MONITOREXIT : imageRepresentation
                                        return;
                                    }
                                    iraster.markDirty();
                                    break block34;
                                }
                                storage2 = new int[w * h];
                                int soff = 0;
                                break block38;
                            }
                            storage = new int[w];
                            break block39;
                        }
                        if (this.cmodel != model || !(this.biRaster instanceof ByteComponentRaster) || this.biRaster.getNumDataElements() != 1) break block40;
                        ByteComponentRaster bt = (ByteComponentRaster)this.biRaster;
                        if (off == 0 && scansize == w) {
                            bt.putByteData(x, y, w, h, pix);
                            break block34;
                        } else {
                            byte[] bpix = new byte[w];
                            int poff = off;
                            for (int yoff = y; yoff < y + h; poff += scansize, ++yoff) {
                                System.arraycopy(pix, poff, bpix, 0, w);
                                bt.putByteData(x, yoff, w, 1, bpix);
                            }
                        }
                        break block34;
                    }
                    for (int yoff = 0; yoff < h; ++yoff, lineOff += scansize) {
                        int poff = lineOff;
                        for (int i = 0; i < w; ++i) {
                            storage2[soff++] = this.srcLUT[pix[poff++] & 0xFF];
                        }
                    }
                    iraster.setDataElements(x, y, w, h, storage2);
                    break block34;
                }
                for (int yoff = y; yoff < y + h; ++yoff, lineOff += scansize) {
                    int poff = lineOff;
                    for (int i = 0; i < w; ++i) {
                        storage[i] = model.getRGB(pix[poff++] & 0xFF);
                    }
                    iraster.setDataElements(x, yoff, w, 1, storage);
                }
                this.availinfo |= 8;
                break block34;
            }
            for (int yoff = y; yoff < y + h; ++yoff, lineOff += scansize) {
                int poff = lineOff;
                for (int xoff = x; xoff < x + w; ++xoff) {
                    this.bimage.setRGB(xoff, yoff, model.getRGB(pix[poff++] & 0xFF));
                }
            }
            this.availinfo |= 8;
        }
        // MONITOREXIT : imageRepresentation
        if ((this.availinfo & 0x10) != 0) return;
        this.newInfo(this.image, 8, x, y, w, h);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pix, int off, int scansize) {
        int lineOff = off;
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        ImageRepresentation imageRepresentation = this;
        synchronized (imageRepresentation) {
            if (this.bimage == null) {
                if (this.cmodel == null) {
                    this.cmodel = model;
                }
                this.createBufferedImage();
            }
            int[] storage = new int[w];
            if (this.cmodel instanceof IndexColorModel) {
                this.convertToRGB();
            }
            if (model == this.cmodel && this.biRaster instanceof IntegerComponentRaster) {
                IntegerComponentRaster iraster = (IntegerComponentRaster)this.biRaster;
                if (off == 0 && scansize == w) {
                    iraster.setDataElements(x, y, w, h, pix);
                } else {
                    int yoff = y;
                    while (yoff < y + h) {
                        System.arraycopy(pix, lineOff, storage, 0, w);
                        iraster.setDataElements(x, yoff, w, 1, storage);
                        ++yoff;
                        lineOff += scansize;
                    }
                }
            } else {
                if (model.getTransparency() != 1 && this.cmodel.getTransparency() == 1) {
                    this.convertToRGB();
                }
                if (this.isDefaultBI) {
                    IntegerComponentRaster iraster = (IntegerComponentRaster)this.biRaster;
                    int[] data = iraster.getDataStorage();
                    if (this.cmodel.equals(model)) {
                        int sstride = iraster.getScanlineStride();
                        int doff = y * sstride + x;
                        int yoff = 0;
                        while (yoff < h) {
                            System.arraycopy(pix, lineOff, data, doff, w);
                            doff += sstride;
                            ++yoff;
                            lineOff += scansize;
                        }
                        iraster.markDirty();
                    } else {
                        int yoff = y;
                        while (yoff < y + h) {
                            int poff = lineOff;
                            for (int i = 0; i < w; ++i) {
                                storage[i] = model.getRGB(pix[poff++]);
                            }
                            iraster.setDataElements(x, yoff, w, 1, storage);
                            ++yoff;
                            lineOff += scansize;
                        }
                    }
                    this.availinfo |= 8;
                } else {
                    Object tmp = null;
                    int yoff = y;
                    while (yoff < y + h) {
                        int poff = lineOff;
                        for (int xoff = x; xoff < x + w; ++xoff) {
                            int pixel = model.getRGB(pix[poff++]);
                            tmp = this.cmodel.getDataElements(pixel, tmp);
                            this.biRaster.setDataElements(xoff, yoff, tmp);
                        }
                        ++yoff;
                        lineOff += scansize;
                    }
                    this.availinfo |= 8;
                }
            }
        }
        if ((this.availinfo & 0x10) == 0) {
            this.newInfo(this.image, 8, x, y, w, h);
        }
    }

    public BufferedImage getOpaqueRGBImage() {
        if (this.bimage.getType() == 2) {
            int w = this.bimage.getWidth();
            int h = this.bimage.getHeight();
            int size = w * h;
            DataBufferInt db = (DataBufferInt)this.biRaster.getDataBuffer();
            int[] pixels = SunWritableRaster.stealData(db, 0);
            for (int i = 0; i < size; ++i) {
                if (pixels[i] >>> 24 == 255) continue;
                return this.bimage;
            }
            DirectColorModel opModel = new DirectColorModel(24, 0xFF0000, 65280, 255);
            int[] bandmasks = new int[]{0xFF0000, 65280, 255};
            WritableRaster opRaster = Raster.createPackedRaster(db, w, h, w, bandmasks, null);
            try {
                BufferedImage opImage = this.createImage(opModel, opRaster, false, null);
                return opImage;
            }
            catch (Exception e) {
                return this.bimage;
            }
        }
        return this.bimage;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void imageComplete(int status) {
        int info;
        boolean done;
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        switch (status) {
            default: {
                done = true;
                info = 128;
                break;
            }
            case 1: {
                this.image.addInfo(64);
                done = true;
                info = 64;
                this.dispose();
                break;
            }
            case 3: {
                done = true;
                info = 32;
                break;
            }
            case 2: {
                done = false;
                info = 16;
            }
        }
        ImageRepresentation imageRepresentation = this;
        synchronized (imageRepresentation) {
            if (done) {
                this.image.getSource().removeConsumer(this);
                this.consuming = false;
                this.newbits = null;
                if (this.bimage != null) {
                    this.bimage = this.getOpaqueRGBImage();
                }
            }
            this.availinfo |= info;
            this.notifyAll();
        }
        this.newInfo(this.image, info, 0, 0, this.width, this.height);
        this.image.infoDone(status);
    }

    void startProduction() {
        if (!this.consuming) {
            this.consuming = true;
            this.image.getSource().startProduction(this);
        }
    }

    private synchronized void checkConsumption() {
        if (this.isWatcherListEmpty() && this.numWaiters == 0 && (this.availinfo & 0x20) == 0) {
            this.dispose();
        }
    }

    @Override
    public synchronized void notifyWatcherListEmpty() {
        this.checkConsumption();
    }

    private synchronized void decrementWaiters() {
        --this.numWaiters;
        this.checkConsumption();
    }

    public boolean prepare(ImageObserver iw) {
        boolean done;
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        if ((this.availinfo & 0x40) != 0) {
            if (iw != null) {
                iw.imageUpdate(this.image, 192, -1, -1, -1, -1);
            }
            return false;
        }
        boolean bl = done = (this.availinfo & 0x20) != 0;
        if (!done) {
            this.addWatcher(iw);
            this.startProduction();
            done = (this.availinfo & 0x20) != 0;
        }
        return done;
    }

    public int check(ImageObserver iw) {
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        if ((this.availinfo & 0x60) == 0) {
            this.addWatcher(iw);
        }
        return this.availinfo;
    }

    public boolean drawToBufImage(Graphics g, ToolkitImage img, int x, int y, Color bg, ImageObserver iw) {
        boolean abort;
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        if ((this.availinfo & 0x40) != 0) {
            if (iw != null) {
                iw.imageUpdate(this.image, 192, -1, -1, -1, -1);
            }
            return false;
        }
        boolean done = (this.availinfo & 0x20) != 0;
        boolean bl = abort = (this.availinfo & 0x80) != 0;
        if (!done && !abort) {
            this.addWatcher(iw);
            this.startProduction();
            boolean bl2 = done = (this.availinfo & 0x20) != 0;
        }
        if (done || 0 != (this.availinfo & 0x10)) {
            g.drawImage(this.bimage, x, y, bg, null);
        }
        return done;
    }

    public boolean drawToBufImage(Graphics g, ToolkitImage img, int x, int y, int w, int h, Color bg, ImageObserver iw) {
        boolean abort;
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        if ((this.availinfo & 0x40) != 0) {
            if (iw != null) {
                iw.imageUpdate(this.image, 192, -1, -1, -1, -1);
            }
            return false;
        }
        boolean done = (this.availinfo & 0x20) != 0;
        boolean bl = abort = (this.availinfo & 0x80) != 0;
        if (!done && !abort) {
            this.addWatcher(iw);
            this.startProduction();
            boolean bl2 = done = (this.availinfo & 0x20) != 0;
        }
        if (done || 0 != (this.availinfo & 0x10)) {
            g.drawImage(this.bimage, x, y, w, h, bg, null);
        }
        return done;
    }

    public boolean drawToBufImage(Graphics g, ToolkitImage img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bg, ImageObserver iw) {
        boolean abort;
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        if ((this.availinfo & 0x40) != 0) {
            if (iw != null) {
                iw.imageUpdate(this.image, 192, -1, -1, -1, -1);
            }
            return false;
        }
        boolean done = (this.availinfo & 0x20) != 0;
        boolean bl = abort = (this.availinfo & 0x80) != 0;
        if (!done && !abort) {
            this.addWatcher(iw);
            this.startProduction();
            boolean bl2 = done = (this.availinfo & 0x20) != 0;
        }
        if (done || 0 != (this.availinfo & 0x10)) {
            g.drawImage(this.bimage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bg, null);
        }
        return done;
    }

    public boolean drawToBufImage(Graphics g, ToolkitImage img, AffineTransform xform, ImageObserver iw) {
        boolean abort;
        Graphics2D g2 = (Graphics2D)g;
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        if ((this.availinfo & 0x40) != 0) {
            if (iw != null) {
                iw.imageUpdate(this.image, 192, -1, -1, -1, -1);
            }
            return false;
        }
        boolean done = (this.availinfo & 0x20) != 0;
        boolean bl = abort = (this.availinfo & 0x80) != 0;
        if (!done && !abort) {
            this.addWatcher(iw);
            this.startProduction();
            boolean bl2 = done = (this.availinfo & 0x20) != 0;
        }
        if (done || 0 != (this.availinfo & 0x10)) {
            g2.drawImage(this.bimage, xform, null);
        }
        return done;
    }

    synchronized void abort() {
        this.image.getSource().removeConsumer(this);
        this.consuming = false;
        this.newbits = null;
        this.bimage = null;
        this.biRaster = null;
        this.cmodel = null;
        this.srcLUT = null;
        this.isDefaultBI = false;
        this.isSameCM = false;
        this.newInfo(this.image, 128, -1, -1, -1, -1);
        this.availinfo &= 0xFFFFFF87;
    }

    synchronized void dispose() {
        this.image.getSource().removeConsumer(this);
        this.consuming = false;
        this.newbits = null;
        this.availinfo &= 0xFFFFFFC7;
    }

    public void setAccelerationPriority(float priority) {
        if (this.bimage != null) {
            this.bimage.setAccelerationPriority(priority);
        }
    }

    static {
        NativeLibLoader.loadLibraries();
        ImageRepresentation.initIDs();
        s_useNative = true;
    }
}

