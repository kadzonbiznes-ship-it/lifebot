/*
 * Decompiled with CFR 0.152.
 */
package java.awt;

import java.awt.Component;
import java.awt.Image;
import java.awt.ImageMediaEntry;
import java.awt.MediaEntry;
import java.io.Serializable;
import sun.awt.image.MultiResolutionToolkitImage;

public class MediaTracker
implements Serializable {
    Component target;
    MediaEntry head;
    private static final long serialVersionUID = -483174189758638095L;
    public static final int LOADING = 1;
    public static final int ABORTED = 2;
    public static final int ERRORED = 4;
    public static final int COMPLETE = 8;
    static final int DONE = 14;

    public MediaTracker(Component comp) {
        this.target = comp;
    }

    public void addImage(Image image, int id) {
        this.addImage(image, id, -1, -1);
    }

    public synchronized void addImage(Image image, int id, int w, int h) {
        this.addImageImpl(image, id, w, h);
        Image rvImage = MediaTracker.getResolutionVariant(image);
        if (rvImage != null) {
            this.addImageImpl(rvImage, id, w == -1 ? -1 : 2 * w, h == -1 ? -1 : 2 * h);
        }
    }

    private void addImageImpl(Image image, int id, int w, int h) {
        this.head = MediaEntry.insert(this.head, new ImageMediaEntry(this, image, id, w, h));
    }

    public boolean checkAll() {
        return this.checkAll(false, true);
    }

    public boolean checkAll(boolean load) {
        return this.checkAll(load, true);
    }

    private synchronized boolean checkAll(boolean load, boolean verify) {
        MediaEntry cur = this.head;
        boolean done = true;
        while (cur != null) {
            if ((cur.getStatus(load, verify) & 0xE) == 0) {
                done = false;
            }
            cur = cur.next;
        }
        return done;
    }

    public synchronized boolean isErrorAny() {
        MediaEntry cur = this.head;
        while (cur != null) {
            if ((cur.getStatus(false, true) & 4) != 0) {
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    public synchronized Object[] getErrorsAny() {
        MediaEntry cur = this.head;
        int numerrors = 0;
        while (cur != null) {
            if ((cur.getStatus(false, true) & 4) != 0) {
                ++numerrors;
            }
            cur = cur.next;
        }
        if (numerrors == 0) {
            return null;
        }
        Object[] errors = new Object[numerrors];
        cur = this.head;
        numerrors = 0;
        while (cur != null) {
            if ((cur.getStatus(false, false) & 4) != 0) {
                errors[numerrors++] = cur.getMedia();
            }
            cur = cur.next;
        }
        return errors;
    }

    public void waitForAll() throws InterruptedException {
        this.waitForAll(0L);
    }

    public synchronized boolean waitForAll(long ms) throws InterruptedException {
        long end = System.currentTimeMillis() + ms;
        boolean first = true;
        int status;
        while (((status = this.statusAll(first, first)) & 1) != 0) {
            long timeout;
            first = false;
            if (ms == 0L) {
                timeout = 0L;
            } else {
                timeout = end - System.currentTimeMillis();
                if (timeout <= 0L) {
                    return false;
                }
            }
            this.wait(timeout);
        }
        return status == 8;
    }

    public int statusAll(boolean load) {
        return this.statusAll(load, true);
    }

    private synchronized int statusAll(boolean load, boolean verify) {
        MediaEntry cur = this.head;
        int status = 0;
        while (cur != null) {
            status |= cur.getStatus(load, verify);
            cur = cur.next;
        }
        return status;
    }

    public boolean checkID(int id) {
        return this.checkID(id, false, true);
    }

    public boolean checkID(int id, boolean load) {
        return this.checkID(id, load, true);
    }

    private synchronized boolean checkID(int id, boolean load, boolean verify) {
        MediaEntry cur = this.head;
        boolean done = true;
        while (cur != null) {
            if (cur.getID() == id && (cur.getStatus(load, verify) & 0xE) == 0) {
                done = false;
            }
            cur = cur.next;
        }
        return done;
    }

    public synchronized boolean isErrorID(int id) {
        MediaEntry cur = this.head;
        while (cur != null) {
            if (cur.getID() == id && (cur.getStatus(false, true) & 4) != 0) {
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    public synchronized Object[] getErrorsID(int id) {
        MediaEntry cur = this.head;
        int numerrors = 0;
        while (cur != null) {
            if (cur.getID() == id && (cur.getStatus(false, true) & 4) != 0) {
                ++numerrors;
            }
            cur = cur.next;
        }
        if (numerrors == 0) {
            return null;
        }
        Object[] errors = new Object[numerrors];
        cur = this.head;
        numerrors = 0;
        while (cur != null) {
            if (cur.getID() == id && (cur.getStatus(false, false) & 4) != 0) {
                errors[numerrors++] = cur.getMedia();
            }
            cur = cur.next;
        }
        return errors;
    }

    public void waitForID(int id) throws InterruptedException {
        this.waitForID(id, 0L);
    }

    public synchronized boolean waitForID(int id, long ms) throws InterruptedException {
        long end = System.currentTimeMillis() + ms;
        boolean first = true;
        int status;
        while (((status = this.statusID(id, first, first)) & 1) != 0) {
            long timeout;
            first = false;
            if (ms == 0L) {
                timeout = 0L;
            } else {
                timeout = end - System.currentTimeMillis();
                if (timeout <= 0L) {
                    return false;
                }
            }
            this.wait(timeout);
        }
        return status == 8;
    }

    public int statusID(int id, boolean load) {
        return this.statusID(id, load, true);
    }

    private synchronized int statusID(int id, boolean load, boolean verify) {
        MediaEntry cur = this.head;
        int status = 0;
        while (cur != null) {
            if (cur.getID() == id) {
                status |= cur.getStatus(load, verify);
            }
            cur = cur.next;
        }
        return status;
    }

    public synchronized void removeImage(Image image) {
        this.removeImageImpl(image);
        Image rvImage = MediaTracker.getResolutionVariant(image);
        if (rvImage != null) {
            this.removeImageImpl(rvImage);
        }
        this.notifyAll();
    }

    private void removeImageImpl(Image image) {
        MediaEntry cur = this.head;
        MediaEntry prev = null;
        while (cur != null) {
            MediaEntry next = cur.next;
            if (cur.getMedia() == image) {
                if (prev == null) {
                    this.head = next;
                } else {
                    prev.next = next;
                }
                cur.cancel();
            } else {
                prev = cur;
            }
            cur = next;
        }
    }

    public synchronized void removeImage(Image image, int id) {
        this.removeImageImpl(image, id);
        Image rvImage = MediaTracker.getResolutionVariant(image);
        if (rvImage != null) {
            this.removeImageImpl(rvImage, id);
        }
        this.notifyAll();
    }

    private void removeImageImpl(Image image, int id) {
        MediaEntry cur = this.head;
        MediaEntry prev = null;
        while (cur != null) {
            MediaEntry next = cur.next;
            if (cur.getID() == id && cur.getMedia() == image) {
                if (prev == null) {
                    this.head = next;
                } else {
                    prev.next = next;
                }
                cur.cancel();
            } else {
                prev = cur;
            }
            cur = next;
        }
    }

    public synchronized void removeImage(Image image, int id, int width, int height) {
        this.removeImageImpl(image, id, width, height);
        Image rvImage = MediaTracker.getResolutionVariant(image);
        if (rvImage != null) {
            this.removeImageImpl(rvImage, id, width == -1 ? -1 : 2 * width, height == -1 ? -1 : 2 * height);
        }
        this.notifyAll();
    }

    private void removeImageImpl(Image image, int id, int width, int height) {
        MediaEntry cur = this.head;
        MediaEntry prev = null;
        while (cur != null) {
            MediaEntry next = cur.next;
            if (cur.getID() == id && cur instanceof ImageMediaEntry && ((ImageMediaEntry)cur).matches(image, width, height)) {
                if (prev == null) {
                    this.head = next;
                } else {
                    prev.next = next;
                }
                cur.cancel();
            } else {
                prev = cur;
            }
            cur = next;
        }
    }

    synchronized void setDone() {
        this.notifyAll();
    }

    private static Image getResolutionVariant(Image image) {
        if (image instanceof MultiResolutionToolkitImage) {
            return ((MultiResolutionToolkitImage)image).getResolutionVariant();
        }
        return null;
    }
}

