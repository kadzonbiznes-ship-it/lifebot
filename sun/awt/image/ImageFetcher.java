/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Vector;
import sun.awt.AppContext;
import sun.awt.image.FetcherInfo;
import sun.awt.image.ImageFetchable;

class ImageFetcher
extends Thread {
    static final int HIGH_PRIORITY = 8;
    static final int LOW_PRIORITY = 3;
    static final int ANIM_PRIORITY = 2;
    static final int TIMEOUT = 5000;

    private ImageFetcher() {
        throw new UnsupportedOperationException("Must erase locals");
    }

    private ImageFetcher(ThreadGroup threadGroup, int index) {
        super(threadGroup, null, "Image Fetcher " + index, 0L, false);
        this.setDaemon(true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean add(ImageFetchable src) {
        FetcherInfo info = FetcherInfo.getFetcherInfo();
        Vector<ImageFetchable> vector = info.waitList;
        synchronized (vector) {
            if (!info.waitList.contains(src)) {
                info.waitList.addElement(src);
                if (info.numWaiting == 0 && info.numFetchers < info.fetchers.length) {
                    ImageFetcher.createFetchers(info);
                }
                if (info.numFetchers > 0) {
                    info.waitList.notify();
                } else {
                    info.waitList.removeElement(src);
                    return false;
                }
            }
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void remove(ImageFetchable src) {
        FetcherInfo info = FetcherInfo.getFetcherInfo();
        Vector<ImageFetchable> vector = info.waitList;
        synchronized (vector) {
            if (info.waitList.contains(src)) {
                info.waitList.removeElement(src);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean isFetcher(Thread t) {
        FetcherInfo info = FetcherInfo.getFetcherInfo();
        Vector<ImageFetchable> vector = info.waitList;
        synchronized (vector) {
            for (int i = 0; i < info.fetchers.length; ++i) {
                if (info.fetchers[i] != t) continue;
                return true;
            }
        }
        return false;
    }

    public static boolean amFetcher() {
        return ImageFetcher.isFetcher(Thread.currentThread());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static ImageFetchable nextImage() {
        FetcherInfo info = FetcherInfo.getFetcherInfo();
        Vector<ImageFetchable> vector = info.waitList;
        synchronized (vector) {
            ImageFetchable src = null;
            long end = System.currentTimeMillis() + 5000L;
            while (src == null) {
                while (info.waitList.size() == 0) {
                    long now = System.currentTimeMillis();
                    if (now >= end) {
                        return null;
                    }
                    try {
                        ++info.numWaiting;
                        info.waitList.wait(end - now);
                    }
                    catch (InterruptedException e) {
                        ImageFetchable imageFetchable = null;
                        return imageFetchable;
                    }
                    finally {
                        --info.numWaiting;
                    }
                }
                src = info.waitList.elementAt(0);
                info.waitList.removeElement(src);
            }
            return src;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        Vector<ImageFetchable> vector;
        FetcherInfo info = FetcherInfo.getFetcherInfo();
        try {
            this.fetchloop();
            vector = info.waitList;
        }
        catch (Exception e) {
            Vector<ImageFetchable> vector2;
            try {
                e.printStackTrace();
                vector2 = info.waitList;
            }
            catch (Throwable throwable) {
                Vector<ImageFetchable> vector3 = info.waitList;
                synchronized (vector3) {
                    Thread me = Thread.currentThread();
                    for (int i = 0; i < info.fetchers.length; ++i) {
                        if (info.fetchers[i] != me) continue;
                        info.fetchers[i] = null;
                        --info.numFetchers;
                    }
                }
                throw throwable;
            }
            synchronized (vector2) {
                Thread me = Thread.currentThread();
                for (int i = 0; i < info.fetchers.length; ++i) {
                    if (info.fetchers[i] != me) continue;
                    info.fetchers[i] = null;
                    --info.numFetchers;
                }
            }
        }
        synchronized (vector) {
            Thread me = Thread.currentThread();
            for (int i = 0; i < info.fetchers.length; ++i) {
                if (info.fetchers[i] != me) continue;
                info.fetchers[i] = null;
                --info.numFetchers;
            }
        }
    }

    private void fetchloop() {
        Thread me = Thread.currentThread();
        while (ImageFetcher.isFetcher(me)) {
            Thread.interrupted();
            me.setPriority(8);
            ImageFetchable src = ImageFetcher.nextImage();
            if (src == null) {
                return;
            }
            try {
                src.doFetch();
            }
            catch (Exception e) {
                System.err.println("Uncaught error fetching image:");
                e.printStackTrace();
            }
            ImageFetcher.stoppingAnimation(me);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void startingAnimation() {
        FetcherInfo info = FetcherInfo.getFetcherInfo();
        Thread me = Thread.currentThread();
        Vector<ImageFetchable> vector = info.waitList;
        synchronized (vector) {
            for (int i = 0; i < info.fetchers.length; ++i) {
                if (info.fetchers[i] != me) continue;
                info.fetchers[i] = null;
                --info.numFetchers;
                me.setName("Image Animator " + i);
                if (info.waitList.size() > info.numWaiting) {
                    ImageFetcher.createFetchers(info);
                }
                return;
            }
        }
        me.setPriority(2);
        me.setName("Image Animator");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void stoppingAnimation(Thread me) {
        FetcherInfo info = FetcherInfo.getFetcherInfo();
        Vector<ImageFetchable> vector = info.waitList;
        synchronized (vector) {
            int index = -1;
            for (int i = 0; i < info.fetchers.length; ++i) {
                if (info.fetchers[i] == me) {
                    return;
                }
                if (info.fetchers[i] != null) continue;
                index = i;
            }
            if (index >= 0) {
                info.fetchers[index] = me;
                ++info.numFetchers;
                me.setName("Image Fetcher " + index);
                return;
            }
        }
    }

    private static void createFetchers(final FetcherInfo info) {
        ThreadGroup fetcherThreadGroup;
        AppContext appContext = AppContext.getAppContext();
        ThreadGroup threadGroup = appContext.getThreadGroup();
        try {
            if (threadGroup.getParent() != null) {
                fetcherThreadGroup = threadGroup;
            } else {
                threadGroup = Thread.currentThread().getThreadGroup();
                ThreadGroup parent = threadGroup.getParent();
                while (parent != null && parent.getParent() != null) {
                    threadGroup = parent;
                    parent = threadGroup.getParent();
                }
                fetcherThreadGroup = threadGroup;
            }
        }
        catch (SecurityException e) {
            fetcherThreadGroup = appContext.getThreadGroup();
        }
        final ThreadGroup fetcherGroup = fetcherThreadGroup;
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                for (int i = 0; i < info.fetchers.length; ++i) {
                    if (info.fetchers[i] != null) continue;
                    ImageFetcher f = new ImageFetcher(fetcherGroup, i);
                    try {
                        f.start();
                        info.fetchers[i] = f;
                        ++info.numFetchers;
                        break;
                    }
                    catch (Error error) {
                        // empty catch block
                    }
                }
                return null;
            }
        });
    }
}

