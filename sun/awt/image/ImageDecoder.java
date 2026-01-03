/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import sun.awt.image.ImageConsumerQueue;
import sun.awt.image.ImageFormatException;
import sun.awt.image.InputStreamImageSource;

public abstract class ImageDecoder {
    InputStreamImageSource source;
    InputStream input;
    Thread feeder;
    protected boolean aborted;
    protected boolean finished;
    ImageConsumerQueue queue;
    ImageDecoder next;

    public ImageDecoder(InputStreamImageSource src, InputStream is) {
        this.source = src;
        this.input = is;
        this.feeder = Thread.currentThread();
    }

    public boolean isConsumer(ImageConsumer ic) {
        return ImageConsumerQueue.isConsumer(this.queue, ic);
    }

    public void removeConsumer(ImageConsumer ic) {
        this.queue = ImageConsumerQueue.removeConsumer(this.queue, ic, false);
        if (!this.finished && this.queue == null) {
            this.abort();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected ImageConsumerQueue nextConsumer(ImageConsumerQueue cq) {
        InputStreamImageSource inputStreamImageSource = this.source;
        synchronized (inputStreamImageSource) {
            if (this.aborted) {
                return null;
            }
            ImageConsumerQueue imageConsumerQueue = cq = cq == null ? this.queue : cq.next;
            while (cq != null) {
                if (cq.interested) {
                    return cq;
                }
                cq = cq.next;
            }
        }
        return null;
    }

    protected int setDimensions(int w, int h) {
        ImageConsumerQueue cq = null;
        int count = 0;
        while ((cq = this.nextConsumer(cq)) != null) {
            cq.consumer.setDimensions(w, h);
            ++count;
        }
        return count;
    }

    protected int setProperties(Hashtable<?, ?> props) {
        ImageConsumerQueue cq = null;
        int count = 0;
        while ((cq = this.nextConsumer(cq)) != null) {
            cq.consumer.setProperties(props);
            ++count;
        }
        return count;
    }

    protected int setColorModel(ColorModel model) {
        ImageConsumerQueue cq = null;
        int count = 0;
        while ((cq = this.nextConsumer(cq)) != null) {
            cq.consumer.setColorModel(model);
            ++count;
        }
        return count;
    }

    protected int setHints(int hints) {
        ImageConsumerQueue cq = null;
        int count = 0;
        while ((cq = this.nextConsumer(cq)) != null) {
            cq.consumer.setHints(hints);
            ++count;
        }
        return count;
    }

    protected void headerComplete() {
        this.feeder.setPriority(3);
    }

    protected int setPixels(int x, int y, int w, int h, ColorModel model, byte[] pix, int off, int scansize) {
        this.source.latchConsumers(this);
        ImageConsumerQueue cq = null;
        int count = 0;
        while ((cq = this.nextConsumer(cq)) != null) {
            cq.consumer.setPixels(x, y, w, h, model, pix, off, scansize);
            ++count;
        }
        return count;
    }

    protected int setPixels(int x, int y, int w, int h, ColorModel model, int[] pix, int off, int scansize) {
        this.source.latchConsumers(this);
        ImageConsumerQueue cq = null;
        int count = 0;
        while ((cq = this.nextConsumer(cq)) != null) {
            cq.consumer.setPixels(x, y, w, h, model, pix, off, scansize);
            ++count;
        }
        return count;
    }

    protected int imageComplete(int status, boolean done) {
        this.source.latchConsumers(this);
        if (done) {
            this.finished = true;
            this.source.doneDecoding(this);
        }
        ImageConsumerQueue cq = null;
        int count = 0;
        while ((cq = this.nextConsumer(cq)) != null) {
            cq.consumer.imageComplete(status);
            ++count;
        }
        return count;
    }

    public abstract void produceImage() throws IOException, ImageFormatException;

    public void abort() {
        this.aborted = true;
        this.source.doneDecoding(this);
        this.close();
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                ImageDecoder.this.feeder.interrupt();
                return null;
            }
        });
    }

    public synchronized void close() {
        if (this.input != null) {
            try {
                this.input.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }
}

