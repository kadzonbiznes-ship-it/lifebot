/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.util.Hashtable;

public class ImageFilter
implements ImageConsumer,
Cloneable {
    protected ImageConsumer consumer;

    public ImageFilter getFilterInstance(ImageConsumer ic) {
        ImageFilter instance = (ImageFilter)this.clone();
        instance.consumer = ic;
        return instance;
    }

    @Override
    public void setDimensions(int width, int height) {
        this.consumer.setDimensions(width, height);
    }

    @Override
    public void setProperties(Hashtable<?, ?> props) {
        Hashtable p = (Hashtable)props.clone();
        Object o = p.get("filters");
        if (o == null) {
            p.put("filters", this.toString());
        } else if (o instanceof String) {
            p.put("filters", (String)o + this.toString());
        }
        this.consumer.setProperties(p);
    }

    @Override
    public void setColorModel(ColorModel model) {
        this.consumer.setColorModel(model);
    }

    @Override
    public void setHints(int hints) {
        this.consumer.setHints(hints);
    }

    @Override
    public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
        this.consumer.setPixels(x, y, w, h, model, pixels, off, scansize);
    }

    @Override
    public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
        this.consumer.setPixels(x, y, w, h, model, pixels, off, scansize);
    }

    @Override
    public void imageComplete(int status) {
        this.consumer.imageComplete(status);
    }

    public void resendTopDownLeftRight(ImageProducer ip) {
        ip.requestTopDownLeftRightResend(this);
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}

