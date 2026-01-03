/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import sun.awt.image.GifImageDecoder;
import sun.awt.image.ImageConsumerQueue;
import sun.awt.image.ImageDecoder;
import sun.awt.image.ImageFetchable;
import sun.awt.image.ImageFetcher;
import sun.awt.image.ImageFormatException;
import sun.awt.image.ImageRepresentation;
import sun.awt.image.JPEGImageDecoder;
import sun.awt.image.PNGImageDecoder;
import sun.awt.image.XbmImageDecoder;

public abstract class InputStreamImageSource
implements ImageProducer,
ImageFetchable {
    ImageConsumerQueue consumers;
    ImageDecoder decoder;
    ImageDecoder decoders;
    boolean awaitingFetch = false;

    abstract boolean checkSecurity(Object var1, boolean var2);

    int countConsumers(ImageConsumerQueue cq) {
        int i = 0;
        while (cq != null) {
            ++i;
            cq = cq.next;
        }
        return i;
    }

    synchronized int countConsumers() {
        ImageDecoder id = this.decoders;
        int i = this.countConsumers(this.consumers);
        while (id != null) {
            i += this.countConsumers(id.queue);
            id = id.next;
        }
        return i;
    }

    @Override
    public void addConsumer(ImageConsumer ic) {
        this.addConsumer(ic, false);
    }

    synchronized void printQueue(ImageConsumerQueue cq, String prefix) {
        while (cq != null) {
            System.out.println(prefix + String.valueOf(cq));
            cq = cq.next;
        }
    }

    synchronized void printQueues(String title) {
        System.out.println(title + "[ -----------");
        this.printQueue(this.consumers, "  ");
        ImageDecoder id = this.decoders;
        while (id != null) {
            System.out.println("    " + String.valueOf(id));
            this.printQueue(id.queue, "      ");
            id = id.next;
        }
        System.out.println("----------- ]" + title);
    }

    synchronized void addConsumer(ImageConsumer ic, boolean produce) {
        this.checkSecurity(null, false);
        ImageDecoder id = this.decoders;
        while (id != null) {
            if (id.isConsumer(ic)) {
                return;
            }
            id = id.next;
        }
        ImageConsumerQueue cq = this.consumers;
        while (cq != null && cq.consumer != ic) {
            cq = cq.next;
        }
        if (cq == null) {
            cq = new ImageConsumerQueue(this, ic);
            cq.next = this.consumers;
            this.consumers = cq;
        } else {
            if (!cq.secure) {
                Object context = null;
                SecurityManager security = System.getSecurityManager();
                if (security != null) {
                    context = security.getSecurityContext();
                }
                if (cq.securityContext == null) {
                    cq.securityContext = context;
                } else if (!cq.securityContext.equals(context)) {
                    this.errorConsumer(cq, false);
                    throw new SecurityException("Applets are trading image data!");
                }
            }
            cq.interested = true;
        }
        if (produce && this.decoder == null) {
            this.startProduction();
        }
    }

    @Override
    public synchronized boolean isConsumer(ImageConsumer ic) {
        ImageDecoder id = this.decoders;
        while (id != null) {
            if (id.isConsumer(ic)) {
                return true;
            }
            id = id.next;
        }
        return ImageConsumerQueue.isConsumer(this.consumers, ic);
    }

    private void errorAllConsumers(ImageConsumerQueue cq, boolean needReload) {
        while (cq != null) {
            if (cq.interested) {
                this.errorConsumer(cq, needReload);
            }
            cq = cq.next;
        }
    }

    private void errorConsumer(ImageConsumerQueue cq, boolean needReload) {
        cq.consumer.imageComplete(1);
        if (needReload && cq.consumer instanceof ImageRepresentation) {
            ((ImageRepresentation)cq.consumer).image.flush();
        }
        this.removeConsumer(cq.consumer);
    }

    @Override
    public synchronized void removeConsumer(ImageConsumer ic) {
        ImageDecoder id = this.decoders;
        while (id != null) {
            id.removeConsumer(ic);
            id = id.next;
        }
        this.consumers = ImageConsumerQueue.removeConsumer(this.consumers, ic, false);
    }

    @Override
    public void startProduction(ImageConsumer ic) {
        this.addConsumer(ic, true);
    }

    private synchronized void startProduction() {
        if (!this.awaitingFetch) {
            if (ImageFetcher.add(this)) {
                this.awaitingFetch = true;
            } else {
                ImageConsumerQueue cq = this.consumers;
                this.consumers = null;
                this.errorAllConsumers(cq, false);
            }
        }
    }

    private synchronized void stopProduction() {
        if (this.awaitingFetch) {
            ImageFetcher.remove(this);
            this.awaitingFetch = false;
        }
    }

    @Override
    public void requestTopDownLeftRightResend(ImageConsumer ic) {
    }

    protected abstract ImageDecoder getDecoder();

    protected ImageDecoder decoderForType(InputStream is, String content_type) {
        return null;
    }

    protected ImageDecoder getDecoder(InputStream is) {
        if (!is.markSupported()) {
            is = new BufferedInputStream(is);
        }
        try {
            is.mark(8);
            int c1 = is.read();
            int c2 = is.read();
            int c3 = is.read();
            int c4 = is.read();
            int c5 = is.read();
            int c6 = is.read();
            int c7 = is.read();
            int c8 = is.read();
            is.reset();
            is.mark(-1);
            if (c1 == 71 && c2 == 73 && c3 == 70 && c4 == 56) {
                return new GifImageDecoder(this, is);
            }
            if (c1 == 255 && c2 == 216 && c3 == 255) {
                return new JPEGImageDecoder(this, is);
            }
            if (c1 == 35 && c2 == 100 && c3 == 101 && c4 == 102) {
                return new XbmImageDecoder(this, is);
            }
            if (c1 == 137 && c2 == 80 && c3 == 78 && c4 == 71 && c5 == 13 && c6 == 10 && c7 == 26 && c8 == 10) {
                return new PNGImageDecoder(this, is);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void doFetch() {
        InputStreamImageSource inputStreamImageSource = this;
        synchronized (inputStreamImageSource) {
            if (this.consumers == null) {
                this.awaitingFetch = false;
                return;
            }
        }
        ImageDecoder imgd = this.getDecoder();
        if (imgd == null) {
            this.badDecoder();
        } else {
            this.setDecoder(imgd);
            try {
                imgd.produceImage();
            }
            catch (IOException | ImageFormatException e) {
                e.printStackTrace();
            }
            finally {
                this.removeDecoder(imgd);
                if (Thread.currentThread().isInterrupted() || !Thread.currentThread().isAlive()) {
                    this.errorAllConsumers(imgd.queue, true);
                } else {
                    this.errorAllConsumers(imgd.queue, false);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void badDecoder() {
        ImageConsumerQueue cq;
        InputStreamImageSource inputStreamImageSource = this;
        synchronized (inputStreamImageSource) {
            cq = this.consumers;
            this.consumers = null;
            this.awaitingFetch = false;
        }
        this.errorAllConsumers(cq, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setDecoder(ImageDecoder mydecoder) {
        ImageConsumerQueue cq;
        InputStreamImageSource inputStreamImageSource = this;
        synchronized (inputStreamImageSource) {
            mydecoder.next = this.decoders;
            this.decoders = mydecoder;
            this.decoder = mydecoder;
            mydecoder.queue = cq = this.consumers;
            this.consumers = null;
            this.awaitingFetch = false;
        }
        while (cq != null) {
            if (cq.interested && !this.checkSecurity(cq.securityContext, true)) {
                this.errorConsumer(cq, false);
            }
            cq = cq.next;
        }
    }

    private synchronized void removeDecoder(ImageDecoder mydecoder) {
        this.doneDecoding(mydecoder);
        ImageDecoder idprev = null;
        ImageDecoder id = this.decoders;
        while (id != null) {
            if (id == mydecoder) {
                if (idprev == null) {
                    this.decoders = id.next;
                    break;
                }
                idprev.next = id.next;
                break;
            }
            idprev = id;
            id = id.next;
        }
    }

    synchronized void doneDecoding(ImageDecoder mydecoder) {
        if (this.decoder == mydecoder) {
            this.decoder = null;
            if (this.consumers != null) {
                this.startProduction();
            }
        }
    }

    void latchConsumers(ImageDecoder id) {
        this.doneDecoding(id);
    }

    synchronized void flush() {
        this.decoder = null;
    }
}

