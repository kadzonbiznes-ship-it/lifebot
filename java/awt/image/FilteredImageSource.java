/*
 * Decompiled with CFR 0.152.
 */
package java.awt.image;

import java.awt.image.ImageConsumer;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.util.Hashtable;

public class FilteredImageSource
implements ImageProducer {
    ImageProducer src;
    ImageFilter filter;
    private Hashtable<ImageConsumer, ImageFilter> proxies;

    public FilteredImageSource(ImageProducer orig, ImageFilter imgf) {
        this.src = orig;
        this.filter = imgf;
    }

    @Override
    public synchronized void addConsumer(ImageConsumer ic) {
        if (this.proxies == null) {
            this.proxies = new Hashtable();
        }
        if (!this.proxies.containsKey(ic)) {
            ImageFilter imgf = this.filter.getFilterInstance(ic);
            this.proxies.put(ic, imgf);
            this.src.addConsumer(imgf);
        }
    }

    @Override
    public synchronized boolean isConsumer(ImageConsumer ic) {
        return this.proxies != null && this.proxies.containsKey(ic);
    }

    @Override
    public synchronized void removeConsumer(ImageConsumer ic) {
        ImageFilter imgf;
        if (this.proxies != null && (imgf = this.proxies.get(ic)) != null) {
            this.src.removeConsumer(imgf);
            this.proxies.remove(ic);
            if (this.proxies.isEmpty()) {
                this.proxies = null;
            }
        }
    }

    @Override
    public synchronized void startProduction(ImageConsumer ic) {
        ImageFilter imgf;
        if (this.proxies == null) {
            this.proxies = new Hashtable();
        }
        if ((imgf = this.proxies.get(ic)) == null) {
            imgf = this.filter.getFilterInstance(ic);
            this.proxies.put(ic, imgf);
        }
        this.src.startProduction(imgf);
    }

    @Override
    public synchronized void requestTopDownLeftRightResend(ImageConsumer ic) {
        ImageFilter imgf;
        if (this.proxies != null && (imgf = this.proxies.get(ic)) != null) {
            imgf.resendTopDownLeftRight(this.src);
        }
    }
}

