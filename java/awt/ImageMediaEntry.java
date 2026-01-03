/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Image;
import java.awt.MediaEntry;
import java.awt.MediaTracker;
import java.awt.image.ImageObserver;
import java.io.Serializable;

class ImageMediaEntry
extends MediaEntry
implements ImageObserver,
Serializable {
    Image image;
    int width;
    int height;
    private static final long serialVersionUID = 4739377000350280650L;

    ImageMediaEntry(MediaTracker mt, Image img, int c, int w, int h) {
        super(mt, c);
        this.image = img;
        this.width = w;
        this.height = h;
    }

    boolean matches(Image img, int w, int h) {
        return this.image == img && this.width == w && this.height == h;
    }

    @Override
    Object getMedia() {
        return this.image;
    }

    @Override
    synchronized int getStatus(boolean doLoad, boolean doVerify) {
        if (doVerify) {
            int flags = this.tracker.target.checkImage(this.image, this.width, this.height, null);
            int s = this.parseflags(flags);
            if (s == 0) {
                if ((this.status & 0xC) != 0) {
                    this.setStatus(2);
                }
            } else if (s != this.status) {
                this.setStatus(s);
            }
        }
        return super.getStatus(doLoad, doVerify);
    }

    @Override
    void startLoad() {
        if (this.tracker.target.prepareImage(this.image, this.width, this.height, this)) {
            this.setStatus(8);
        }
    }

    int parseflags(int infoflags) {
        if ((infoflags & 0x40) != 0) {
            return 4;
        }
        if ((infoflags & 0x80) != 0) {
            return 2;
        }
        if ((infoflags & 0x30) != 0) {
            return 8;
        }
        return 0;
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        if (this.cancelled) {
            return false;
        }
        int s = this.parseflags(infoflags);
        if (s != 0 && s != this.status) {
            this.setStatus(s);
        }
        return (this.status & 1) != 0;
    }
}

