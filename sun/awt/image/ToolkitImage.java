/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.util.Hashtable;
import sun.awt.image.ImageRepresentation;
import sun.awt.image.InputStreamImageSource;
import sun.awt.image.NativeLibLoader;

public class ToolkitImage
extends Image {
    ImageProducer source;
    InputStreamImageSource src;
    ImageRepresentation imagerep;
    private int width = -1;
    private int height = -1;
    private Hashtable<?, ?> properties;
    private int availinfo;

    protected ToolkitImage() {
    }

    public ToolkitImage(ImageProducer is) {
        this.source = is;
        if (is instanceof InputStreamImageSource) {
            this.src = (InputStreamImageSource)is;
        }
    }

    @Override
    public ImageProducer getSource() {
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        return this.source;
    }

    public int getWidth() {
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        if ((this.availinfo & 1) == 0) {
            this.reconstruct(1);
        }
        return this.width;
    }

    @Override
    public synchronized int getWidth(ImageObserver iw) {
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        if ((this.availinfo & 1) == 0) {
            this.addWatcher(iw, true);
            if ((this.availinfo & 1) == 0) {
                return -1;
            }
        }
        return this.width;
    }

    public int getHeight() {
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        if ((this.availinfo & 2) == 0) {
            this.reconstruct(2);
        }
        return this.height;
    }

    @Override
    public synchronized int getHeight(ImageObserver iw) {
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        if ((this.availinfo & 2) == 0) {
            this.addWatcher(iw, true);
            if ((this.availinfo & 2) == 0) {
                return -1;
            }
        }
        return this.height;
    }

    @Override
    public Object getProperty(String name, ImageObserver observer) {
        Object o;
        if (name == null) {
            throw new NullPointerException("null property name is not allowed");
        }
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        if (this.properties == null) {
            this.addWatcher(observer, true);
            if (this.properties == null) {
                return null;
            }
        }
        if ((o = this.properties.get(name)) == null) {
            o = Image.UndefinedProperty;
        }
        return o;
    }

    public boolean hasError() {
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        return (this.availinfo & 0x40) != 0;
    }

    public int check(ImageObserver iw) {
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        if ((this.availinfo & 0x40) == 0 && (~this.availinfo & 7) != 0) {
            this.addWatcher(iw, false);
        }
        return this.availinfo;
    }

    public void preload(ImageObserver iw) {
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        if ((this.availinfo & 0x20) == 0) {
            this.addWatcher(iw, true);
        }
    }

    private synchronized void addWatcher(ImageObserver iw, boolean load) {
        if ((this.availinfo & 0x40) != 0) {
            if (iw != null) {
                iw.imageUpdate(this, 192, -1, -1, -1, -1);
            }
            return;
        }
        ImageRepresentation ir = this.getImageRep();
        ir.addWatcher(iw);
        if (load) {
            ir.startProduction();
        }
    }

    private synchronized void reconstruct(int flags) {
        if ((flags & ~this.availinfo) != 0) {
            if ((this.availinfo & 0x40) != 0) {
                return;
            }
            ImageRepresentation ir = this.getImageRep();
            ir.startProduction();
            while ((flags & ~this.availinfo) != 0) {
                try {
                    this.wait();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if ((this.availinfo & 0x40) == 0) continue;
                return;
            }
        }
    }

    synchronized void addInfo(int newinfo) {
        this.availinfo |= newinfo;
        this.notifyAll();
    }

    void setDimensions(int w, int h) {
        this.width = w;
        this.height = h;
        this.addInfo(3);
    }

    void setProperties(Hashtable<?, ?> props) {
        if (props == null) {
            props = new Hashtable();
        }
        this.properties = props;
        this.addInfo(4);
    }

    synchronized void infoDone(int status) {
        if (status == 1 || (~this.availinfo & 3) != 0) {
            this.addInfo(64);
        } else if ((this.availinfo & 4) == 0) {
            this.setProperties(null);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void flush() {
        ImageRepresentation ir;
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        ToolkitImage toolkitImage = this;
        synchronized (toolkitImage) {
            this.availinfo &= 0xFFFFFFBF;
            ir = this.imagerep;
            this.imagerep = null;
        }
        if (ir != null) {
            ir.abort();
        }
        if (this.src != null) {
            this.src.flush();
        }
    }

    protected ImageRepresentation makeImageRep() {
        return new ImageRepresentation(this, ColorModel.getRGBdefault(), false);
    }

    public synchronized ImageRepresentation getImageRep() {
        if (this.src != null) {
            this.src.checkSecurity(null, false);
        }
        if (this.imagerep == null) {
            this.imagerep = this.makeImageRep();
        }
        return this.imagerep;
    }

    @Override
    public Graphics getGraphics() {
        throw new UnsupportedOperationException("getGraphics() not valid for images created with createImage(producer)");
    }

    public ColorModel getColorModel() {
        ImageRepresentation imageRep = this.getImageRep();
        return imageRep.getColorModel();
    }

    public BufferedImage getBufferedImage() {
        ImageRepresentation imageRep = this.getImageRep();
        return imageRep.getBufferedImage();
    }

    @Override
    public void setAccelerationPriority(float priority) {
        super.setAccelerationPriority(priority);
        ImageRepresentation imageRep = this.getImageRep();
        imageRep.setAccelerationPriority(this.accelerationPriority);
    }

    static {
        NativeLibLoader.loadLibraries();
    }
}

