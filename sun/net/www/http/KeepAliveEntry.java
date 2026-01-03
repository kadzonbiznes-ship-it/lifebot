/*
 * Decompiled with CFR 0.152.
 */
package sun.net.www.http;

import sun.net.www.http.HttpClient;

class KeepAliveEntry {
    final HttpClient hc;
    final long idleStartTime;

    KeepAliveEntry(HttpClient hc, long idleStartTime) {
        this.hc = hc;
        this.idleStartTime = idleStartTime;
    }
}

