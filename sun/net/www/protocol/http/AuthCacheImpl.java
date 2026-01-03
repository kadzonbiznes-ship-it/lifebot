/*
 * Decompiled with CFR 0.152.
 */
package sun.net.www.protocol.http;

import java.net.Authenticator;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.WeakHashMap;
import sun.net.www.protocol.http.AuthCache;
import sun.net.www.protocol.http.AuthCacheValue;
import sun.net.www.protocol.http.AuthenticationInfo;

public class AuthCacheImpl
implements AuthCache {
    HashMap<String, LinkedList<AuthCacheValue>> hashtable = new HashMap();
    private static final Map<Authenticator, AuthCacheImpl> caches = Collections.synchronizedMap(new WeakHashMap());

    public void setMap(HashMap<String, LinkedList<AuthCacheValue>> map) {
        this.hashtable = map;
    }

    @Override
    public synchronized void put(String pkey, AuthCacheValue value) {
        LinkedList<AuthCacheValue> list = this.hashtable.get(pkey);
        String skey = value.getPath();
        if (list == null) {
            list = new LinkedList();
            this.hashtable.put(pkey, list);
        }
        ListIterator<AuthCacheValue> iter = list.listIterator();
        while (iter.hasNext()) {
            AuthenticationInfo inf = (AuthenticationInfo)iter.next();
            if (inf.path != null && !inf.path.startsWith(skey)) continue;
            iter.remove();
        }
        iter.add(value);
    }

    @Override
    public synchronized AuthCacheValue get(String pkey, String skey) {
        Object result = null;
        LinkedList<AuthCacheValue> list = this.hashtable.get(pkey);
        if (list == null || list.size() == 0) {
            return null;
        }
        if (skey == null) {
            return list.get(0);
        }
        for (AuthCacheValue authCacheValue : list) {
            AuthenticationInfo inf = (AuthenticationInfo)authCacheValue;
            if (!skey.startsWith(inf.path)) continue;
            return inf;
        }
        return null;
    }

    @Override
    public synchronized void remove(String pkey, AuthCacheValue entry) {
        LinkedList<AuthCacheValue> list = this.hashtable.get(pkey);
        if (list == null) {
            return;
        }
        if (entry == null) {
            list.clear();
            return;
        }
        ListIterator iter = list.listIterator();
        while (iter.hasNext()) {
            AuthenticationInfo inf = (AuthenticationInfo)iter.next();
            if (!entry.equals(inf)) continue;
            iter.remove();
        }
    }

    public static AuthCacheImpl getDefault() {
        return AuthCacheImpl.getAuthCacheFor(null);
    }

    public static AuthCacheImpl getAuthCacheFor(Authenticator auth) {
        return caches.computeIfAbsent(auth, k -> new AuthCacheImpl());
    }
}

