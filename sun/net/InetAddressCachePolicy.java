/*
 * Decompiled with CFR 0.152.
 */
package sun.net;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Security;

public final class InetAddressCachePolicy {
    private static final String cachePolicyProp = "networkaddress.cache.ttl";
    private static final String cachePolicyPropFallback = "sun.net.inetaddr.ttl";
    private static final String cacheStalePolicyProp = "networkaddress.cache.stale.ttl";
    private static final String cacheStalePolicyPropFallback = "sun.net.inetaddr.stale.ttl";
    private static final String negativeCachePolicyProp = "networkaddress.cache.negative.ttl";
    private static final String negativeCachePolicyPropFallback = "sun.net.inetaddr.negative.ttl";
    public static final int FOREVER = -1;
    public static final int NEVER = 0;
    public static final int DEFAULT_POSITIVE = 30;
    private static volatile int cachePolicy = -1;
    private static volatile int staleCachePolicy = 0;
    private static volatile int negativeCachePolicy = 0;
    private static boolean propertySet;
    private static boolean propertyNegativeSet;

    private static Integer getProperty(final String cachePolicyProp, final String cachePolicyPropFallback) {
        return AccessController.doPrivileged(new PrivilegedAction<Integer>(){

            @Override
            public Integer run() {
                String tmpString2;
                try {
                    tmpString2 = Security.getProperty(cachePolicyProp);
                    if (tmpString2 != null) {
                        return Integer.valueOf(tmpString2);
                    }
                }
                catch (NumberFormatException tmpString2) {
                    // empty catch block
                }
                try {
                    tmpString2 = System.getProperty(cachePolicyPropFallback);
                    if (tmpString2 != null) {
                        return Integer.decode(tmpString2);
                    }
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
                return null;
            }
        });
    }

    public static int get() {
        return cachePolicy;
    }

    public static int getStale() {
        return staleCachePolicy;
    }

    public static int getNegative() {
        return negativeCachePolicy;
    }

    public static synchronized void setIfNotSet(int newPolicy) {
        if (!propertySet) {
            InetAddressCachePolicy.checkValue(newPolicy, cachePolicy);
            cachePolicy = newPolicy;
        }
    }

    public static void setNegativeIfNotSet(int newPolicy) {
        if (!propertyNegativeSet) {
            negativeCachePolicy = newPolicy < 0 ? -1 : newPolicy;
        }
    }

    private static void checkValue(int newPolicy, int oldPolicy) {
        if (newPolicy == -1) {
            return;
        }
        if (oldPolicy == -1 || newPolicy < oldPolicy || newPolicy < -1) {
            throw new SecurityException("can't make InetAddress cache more lax");
        }
    }

    static {
        Integer tmp = InetAddressCachePolicy.getProperty(cachePolicyProp, cachePolicyPropFallback);
        if (tmp != null) {
            cachePolicy = tmp < 0 ? -1 : tmp;
            propertySet = true;
        } else if (System.getSecurityManager() == null) {
            cachePolicy = 30;
        }
        tmp = InetAddressCachePolicy.getProperty(negativeCachePolicyProp, negativeCachePolicyPropFallback);
        if (tmp != null) {
            negativeCachePolicy = tmp < 0 ? -1 : tmp;
            propertyNegativeSet = true;
        }
        if (cachePolicy > 0 && (tmp = InetAddressCachePolicy.getProperty(cacheStalePolicyProp, cacheStalePolicyPropFallback)) != null) {
            staleCachePolicy = tmp;
        }
    }
}

