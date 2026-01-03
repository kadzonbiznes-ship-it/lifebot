/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.image.ImageConsumer;
import sun.awt.image.ImageRepresentation;
import sun.awt.image.InputStreamImageSource;

class ImageConsumerQueue {
    ImageConsumerQueue next;
    ImageConsumer consumer;
    boolean interested;
    Object securityContext;
    boolean secure;

    static ImageConsumerQueue removeConsumer(ImageConsumerQueue cqbase, ImageConsumer ic, boolean stillinterested) {
        ImageConsumerQueue cqprev = null;
        ImageConsumerQueue cq = cqbase;
        while (cq != null) {
            if (cq.consumer == ic) {
                if (cqprev == null) {
                    cqbase = cq.next;
                } else {
                    cqprev.next = cq.next;
                }
                cq.interested = stillinterested;
                break;
            }
            cqprev = cq;
            cq = cq.next;
        }
        return cqbase;
    }

    static boolean isConsumer(ImageConsumerQueue cqbase, ImageConsumer ic) {
        ImageConsumerQueue cq = cqbase;
        while (cq != null) {
            if (cq.consumer == ic) {
                return true;
            }
            cq = cq.next;
        }
        return false;
    }

    ImageConsumerQueue(InputStreamImageSource src, ImageConsumer ic) {
        this.consumer = ic;
        this.interested = true;
        if (ic instanceof ImageRepresentation) {
            ImageRepresentation ir = (ImageRepresentation)ic;
            if (ir.image.source != src) {
                throw new SecurityException("ImageRep added to wrong image source");
            }
            this.secure = true;
        } else {
            SecurityManager security = System.getSecurityManager();
            this.securityContext = security != null ? security.getSecurityContext() : null;
        }
    }

    public String toString() {
        return "[" + String.valueOf(this.consumer) + ", " + (this.interested ? "" : "not ") + "interested" + (String)(this.securityContext != null ? ", " + String.valueOf(this.securityContext) : "") + "]";
    }
}

