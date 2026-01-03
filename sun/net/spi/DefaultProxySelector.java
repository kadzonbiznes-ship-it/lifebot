/*
 * Decompiled with CFR 0.152.
 */
package sun.net.spi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jdk.internal.loader.BootLoader;
import sun.net.NetProperties;
import sun.net.SocksProxy;

public class DefaultProxySelector
extends ProxySelector {
    static final String[][] props = new String[][]{{"http", "http.proxy", "proxy", "socksProxy"}, {"https", "https.proxy", "proxy", "socksProxy"}, {"ftp", "ftp.proxy", "ftpProxy", "proxy", "socksProxy"}, {"socket", "socksProxy"}};
    private static final String SOCKS_PROXY_VERSION = "socksProxyVersion";
    private static boolean hasSystemProxies = false;
    private static final List<Proxy> NO_PROXY_LIST = List.of(Proxy.NO_PROXY);

    public static int socksProxyVersion() {
        return AccessController.doPrivileged(new PrivilegedAction<Integer>(){

            @Override
            public Integer run() {
                return NetProperties.getInteger(DefaultProxySelector.SOCKS_PROXY_VERSION, 5);
            }
        });
    }

    @Override
    public List<Proxy> select(URI uri) {
        String auth;
        if (uri == null) {
            throw new IllegalArgumentException("URI can't be null.");
        }
        String protocol = uri.getScheme();
        String host = uri.getHost();
        if (host == null && (auth = uri.getAuthority()) != null) {
            int i = auth.indexOf(64);
            if (i >= 0) {
                auth = auth.substring(i + 1);
            }
            if ((i = auth.lastIndexOf(58)) >= 0) {
                auth = auth.substring(0, i);
            }
            host = auth;
        }
        if (protocol == null || host == null) {
            throw new IllegalArgumentException("protocol = " + protocol + " host = " + host);
        }
        NonProxyInfo pinfo = null;
        if ("http".equalsIgnoreCase(protocol)) {
            pinfo = NonProxyInfo.httpNonProxyInfo;
        } else if ("https".equalsIgnoreCase(protocol)) {
            pinfo = NonProxyInfo.httpNonProxyInfo;
        } else if ("ftp".equalsIgnoreCase(protocol)) {
            pinfo = NonProxyInfo.ftpNonProxyInfo;
        } else if ("socket".equalsIgnoreCase(protocol)) {
            pinfo = NonProxyInfo.socksNonProxyInfo;
        }
        final String proto = protocol;
        final NonProxyInfo nprop = pinfo;
        final String urlhost = host.toLowerCase(Locale.ROOT);
        Proxy[] proxyArray = AccessController.doPrivileged(new PrivilegedAction<Proxy[]>(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public Proxy[] run() {
                String phost = null;
                int pport = 0;
                String nphosts = null;
                InetSocketAddress saddr = null;
                for (int i = 0; i < props.length; ++i) {
                    int j;
                    if (!props[i][0].equalsIgnoreCase(proto)) continue;
                    for (j = 1; j < props[i].length && ((phost = NetProperties.get(props[i][j] + "Host")) == null || phost.length() == 0); ++j) {
                    }
                    if (phost == null || phost.isEmpty()) {
                        if (hasSystemProxies) {
                            String sproto = proto.equalsIgnoreCase("socket") ? "socks" : proto;
                            return DefaultProxySelector.this.getSystemProxies(sproto, urlhost);
                        }
                        return null;
                    }
                    if (nprop != null) {
                        nphosts = NetProperties.get(nprop.property);
                        NonProxyInfo sproto = nprop;
                        synchronized (sproto) {
                            if (nphosts == null) {
                                if (nprop.defaultVal != null) {
                                    nphosts = nprop.defaultVal;
                                } else {
                                    nprop.hostsSource = null;
                                    nprop.pattern = null;
                                }
                            } else if (!nphosts.isEmpty()) {
                                nphosts = nphosts + "|localhost|127.*|[::1]|0.0.0.0|[::0]";
                            }
                            if (nphosts != null && !nphosts.equals(nprop.hostsSource)) {
                                nprop.pattern = DefaultProxySelector.toPattern(nphosts);
                                nprop.hostsSource = nphosts;
                            }
                            if (DefaultProxySelector.shouldNotUseProxyFor(nprop.pattern, urlhost)) {
                                return null;
                            }
                        }
                    }
                    if ((pport = NetProperties.getInteger(props[i][j] + "Port", 0).intValue()) == 0 && j < props[i].length - 1) {
                        for (int k = 1; k < props[i].length - 1; ++k) {
                            if (k == j || pport != 0) continue;
                            pport = NetProperties.getInteger(props[i][k] + "Port", 0);
                        }
                    }
                    if (pport == 0) {
                        pport = j == props[i].length - 1 ? DefaultProxySelector.this.defaultPort("socket") : DefaultProxySelector.this.defaultPort(proto);
                    }
                    saddr = InetSocketAddress.createUnresolved(phost, pport);
                    if (j == props[i].length - 1) {
                        return new Proxy[]{SocksProxy.create(saddr, DefaultProxySelector.socksProxyVersion())};
                    }
                    return new Proxy[]{new Proxy(Proxy.Type.HTTP, saddr)};
                }
                return null;
            }
        });
        if (proxyArray != null) {
            return Stream.of(proxyArray).distinct().collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        }
        return NO_PROXY_LIST;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        if (uri == null || sa == null || ioe == null) {
            throw new IllegalArgumentException("Arguments can't be null.");
        }
    }

    private int defaultPort(String protocol) {
        if ("http".equalsIgnoreCase(protocol)) {
            return 80;
        }
        if ("https".equalsIgnoreCase(protocol)) {
            return 443;
        }
        if ("ftp".equalsIgnoreCase(protocol)) {
            return 80;
        }
        if ("socket".equalsIgnoreCase(protocol)) {
            return 1080;
        }
        return -1;
    }

    private static native boolean init();

    private synchronized native Proxy[] getSystemProxies(String var1, String var2);

    static boolean shouldNotUseProxyFor(Pattern pattern, String urlhost) {
        if (pattern == null || urlhost.isEmpty()) {
            return false;
        }
        boolean matches = pattern.matcher(urlhost).matches();
        return matches;
    }

    static Pattern toPattern(String mask) {
        boolean disjunctionEmpty = true;
        StringJoiner joiner = new StringJoiner("|");
        for (String disjunct : mask.split("\\|")) {
            if (disjunct.isEmpty()) continue;
            disjunctionEmpty = false;
            String regex = DefaultProxySelector.disjunctToRegex(disjunct.toLowerCase());
            joiner.add(regex);
        }
        return disjunctionEmpty ? null : Pattern.compile(joiner.toString());
    }

    static String disjunctToRegex(String disjunct) {
        String regex = disjunct.equals("*") ? ".*" : (disjunct.startsWith("*") && disjunct.endsWith("*") ? ".*" + Pattern.quote(disjunct.substring(1, disjunct.length() - 1)) + ".*" : (disjunct.startsWith("*") ? ".*" + Pattern.quote(disjunct.substring(1)) : (disjunct.endsWith("*") ? Pattern.quote(disjunct.substring(0, disjunct.length() - 1)) + ".*" : Pattern.quote(disjunct))));
        return regex;
    }

    static {
        String key = "java.net.useSystemProxies";
        Boolean b = AccessController.doPrivileged(new PrivilegedAction<Boolean>(){

            @Override
            public Boolean run() {
                return NetProperties.getBoolean("java.net.useSystemProxies");
            }
        });
        if (b != null && b.booleanValue()) {
            BootLoader.loadLibrary("net");
            hasSystemProxies = DefaultProxySelector.init();
        }
    }

    static class NonProxyInfo {
        static final String defStringVal = "localhost|127.*|[::1]|0.0.0.0|[::0]";
        String hostsSource;
        Pattern pattern;
        final String property;
        final String defaultVal;
        static NonProxyInfo ftpNonProxyInfo = new NonProxyInfo("ftp.nonProxyHosts", null, null, "localhost|127.*|[::1]|0.0.0.0|[::0]");
        static NonProxyInfo httpNonProxyInfo = new NonProxyInfo("http.nonProxyHosts", null, null, "localhost|127.*|[::1]|0.0.0.0|[::0]");
        static NonProxyInfo socksNonProxyInfo = new NonProxyInfo("socksNonProxyHosts", null, null, "localhost|127.*|[::1]|0.0.0.0|[::0]");

        NonProxyInfo(String p, String s, Pattern pattern, String d) {
            this.property = p;
            this.hostsSource = s;
            this.pattern = pattern;
            this.defaultVal = d;
        }
    }
}

