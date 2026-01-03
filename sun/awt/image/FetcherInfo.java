/*
 * Decompiled with CFR 0.152.
 */
package sun.awt.image;

import java.util.Vector;
import sun.awt.AppContext;
import sun.awt.image.ImageFetchable;

class FetcherInfo {
    static final int MAX_NUM_FETCHERS_PER_APPCONTEXT = 4;
    Thread[] fetchers = new Thread[4];
    int numFetchers = 0;
    int numWaiting = 0;
    Vector<ImageFetchable> waitList = new Vector();
    private static final Object FETCHER_INFO_KEY = new StringBuffer("FetcherInfo");

    private FetcherInfo() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static FetcherInfo getFetcherInfo() {
        AppContext appContext;
        AppContext appContext2 = appContext = AppContext.getAppContext();
        synchronized (appContext2) {
            FetcherInfo info = (FetcherInfo)appContext.get(FETCHER_INFO_KEY);
            if (info == null) {
                info = new FetcherInfo();
                appContext.put(FETCHER_INFO_KEY, info);
            }
            return info;
        }
    }
}

