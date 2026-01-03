/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.lang.ref.WeakReference;
import java.security.AccessControlContext;
import java.security.AccessController;

public abstract class ImageWatched {
    public static Link endlink = new Link();
    public Link watcherList = endlink;

    public synchronized void addWatcher(ImageObserver iw) {
        if (iw != null && !this.isWatcher(iw)) {
            this.watcherList = new WeakLink(iw, this.watcherList);
        }
        this.watcherList = this.watcherList.removeWatcher(null);
    }

    public synchronized boolean isWatcher(ImageObserver iw) {
        return this.watcherList.isWatcher(iw);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeWatcher(ImageObserver iw) {
        ImageWatched imageWatched = this;
        synchronized (imageWatched) {
            this.watcherList = this.watcherList.removeWatcher(iw);
        }
        if (this.watcherList == endlink) {
            this.notifyWatcherListEmpty();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isWatcherListEmpty() {
        ImageWatched imageWatched = this;
        synchronized (imageWatched) {
            this.watcherList = this.watcherList.removeWatcher(null);
        }
        return this.watcherList == endlink;
    }

    public void newInfo(Image img, int info, int x, int y, int w, int h) {
        if (this.watcherList.newInfo(img, info, x, y, w, h)) {
            this.removeWatcher(null);
        }
    }

    protected abstract void notifyWatcherListEmpty();

    public static class Link {
        public boolean isWatcher(ImageObserver iw) {
            return false;
        }

        public Link removeWatcher(ImageObserver iw) {
            return this;
        }

        public boolean newInfo(Image img, int info, int x, int y, int w, int h) {
            return false;
        }
    }

    public static class WeakLink
    extends Link {
        private final AccWeakReference<ImageObserver> myref;
        private Link next;

        public WeakLink(ImageObserver obs, Link next) {
            this.myref = new AccWeakReference<ImageObserver>(obs);
            this.next = next;
        }

        @Override
        public boolean isWatcher(ImageObserver iw) {
            return this.myref.get() == iw || this.next.isWatcher(iw);
        }

        @Override
        public Link removeWatcher(ImageObserver iw) {
            ImageObserver myiw = (ImageObserver)this.myref.get();
            if (myiw == null) {
                return this.next.removeWatcher(iw);
            }
            if (myiw == iw) {
                return this.next;
            }
            this.next = this.next.removeWatcher(iw);
            return this;
        }

        private static boolean update(ImageObserver iw, AccessControlContext acc, Image img, int info, int x, int y, int w, int h) {
            if (acc != null || System.getSecurityManager() != null) {
                return AccessController.doPrivileged(() -> iw.imageUpdate(img, info, x, y, w, h), acc);
            }
            return false;
        }

        @Override
        public boolean newInfo(Image img, int info, int x, int y, int w, int h) {
            boolean ret = this.next.newInfo(img, info, x, y, w, h);
            ImageObserver myiw = (ImageObserver)this.myref.get();
            if (myiw == null) {
                ret = true;
            } else if (!WeakLink.update(myiw, this.myref.acc, img, info, x, y, w, h)) {
                this.myref.clear();
                ret = true;
            }
            return ret;
        }
    }

    static class AccWeakReference<T>
    extends WeakReference<T> {
        private final AccessControlContext acc = AccessController.getContext();

        AccWeakReference(T ref) {
            super(ref);
        }
    }
}

