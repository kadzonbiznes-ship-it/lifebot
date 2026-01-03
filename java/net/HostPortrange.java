/*
 * Decompiled with CFR 0.152.
 */
package java.net;

import java.util.Formatter;
import java.util.Locale;
import sun.net.util.IPAddressUtil;

class HostPortrange {
    String hostname;
    String scheme;
    int[] portrange;
    boolean wildcard;
    boolean literal;
    boolean ipv6;
    boolean ipv4;
    static final int PORT_MIN = 0;
    static final int PORT_MAX = 65535;
    static final int CASE_DIFF = -32;
    static final int[] HTTP_PORT = new int[]{80, 80};
    static final int[] HTTPS_PORT = new int[]{443, 443};
    static final int[] NO_PORT = new int[]{-1, -1};

    boolean equals(HostPortrange that) {
        return this.hostname.equals(that.hostname) && this.portrange[0] == that.portrange[0] && this.portrange[1] == that.portrange[1] && this.wildcard == that.wildcard && this.literal == that.literal;
    }

    public int hashCode() {
        return this.hostname.hashCode() + this.portrange[0] + this.portrange[1];
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    HostPortrange(String scheme, String str) {
        String portstr = null;
        this.scheme = scheme;
        if (str.charAt(0) == '[') {
            byte[] ip;
            this.literal = true;
            this.ipv6 = true;
            int rb = str.indexOf(93);
            if (rb == -1) {
                throw new IllegalArgumentException("invalid IPv6 address: " + str);
            }
            String hoststr = str.substring(1, rb);
            int sep = str.indexOf(58, rb + 1);
            if (sep != -1 && str.length() > sep) {
                portstr = str.substring(sep + 1);
            }
            if ((ip = IPAddressUtil.textToNumericFormatV6(hoststr)) == null) {
                throw new IllegalArgumentException("illegal IPv6 address");
            }
            StringBuilder sb = new StringBuilder();
            Formatter formatter = new Formatter(sb, Locale.US);
            formatter.format("%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x", ip[0], ip[1], ip[2], ip[3], ip[4], ip[5], ip[6], ip[7], ip[8], ip[9], ip[10], ip[11], ip[12], ip[13], ip[14], ip[15]);
            this.hostname = sb.toString();
        } else {
            String hoststr;
            int sep = str.indexOf(58);
            if (sep != -1 && str.length() > sep) {
                hoststr = str.substring(0, sep);
                portstr = str.substring(sep + 1);
            } else {
                String string = hoststr = sep == -1 ? str : str.substring(0, sep);
            }
            if (hoststr.lastIndexOf(42) > 0) {
                throw new IllegalArgumentException("invalid host wildcard specification");
            }
            if (hoststr.startsWith("*")) {
                this.wildcard = true;
                if (hoststr.equals("*")) {
                    hoststr = "";
                } else {
                    if (!hoststr.startsWith("*.")) throw new IllegalArgumentException("invalid host wildcard specification");
                    hoststr = HostPortrange.toLowerCase(hoststr.substring(1));
                }
            } else {
                int lastdot = hoststr.lastIndexOf(46);
                if (lastdot != -1 && hoststr.length() > 1) {
                    boolean ipv4 = true;
                    int len = hoststr.length();
                    for (int i = lastdot + 1; i < len; ++i) {
                        char c = hoststr.charAt(i);
                        if (c >= '0' && c <= '9') continue;
                        ipv4 = false;
                        break;
                    }
                    this.ipv4 = this.literal = ipv4;
                    if (ipv4) {
                        byte[] ip = IPAddressUtil.validateNumericFormatV4((String)hoststr);
                        if (ip == null) {
                            throw new IllegalArgumentException("illegal IPv4 address");
                        }
                        StringBuilder sb = new StringBuilder();
                        Formatter formatter = new Formatter(sb, Locale.US);
                        formatter.format("%d.%d.%d.%d", ip[0], ip[1], ip[2], ip[3]);
                        hoststr = sb.toString();
                    } else {
                        hoststr = HostPortrange.toLowerCase(hoststr);
                    }
                }
            }
            this.hostname = hoststr;
        }
        try {
            this.portrange = this.parsePort(portstr);
            return;
        }
        catch (Exception e) {
            throw new IllegalArgumentException("invalid port range: " + portstr);
        }
    }

    static String toLowerCase(String s) {
        int len = s.length();
        StringBuilder sb = null;
        for (int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            if (c >= 'a' && c <= 'z' || c == '.') {
                if (sb == null) continue;
                sb.append(c);
                continue;
            }
            if (c >= '0' && c <= '9' || c == '-') {
                if (sb == null) continue;
                sb.append(c);
                continue;
            }
            if (c >= 'A' && c <= 'Z') {
                if (sb == null) {
                    sb = new StringBuilder(len);
                    sb.append(s, 0, i);
                }
                sb.append((char)(c - -32));
                continue;
            }
            String message = String.format("Invalid character \\u%04x in hostname", c);
            throw new IllegalArgumentException(message);
        }
        return sb == null ? s : sb.toString();
    }

    public boolean literal() {
        return this.literal;
    }

    public boolean ipv4Literal() {
        return this.ipv4;
    }

    public boolean ipv6Literal() {
        return this.ipv6;
    }

    public String hostname() {
        return this.hostname;
    }

    public int[] portrange() {
        return this.portrange;
    }

    public boolean wildcard() {
        return this.wildcard;
    }

    int[] defaultPort() {
        if (this.scheme.equals("http")) {
            return HTTP_PORT;
        }
        if (this.scheme.equals("https")) {
            return HTTPS_PORT;
        }
        return NO_PORT;
    }

    int[] parsePort(String port) {
        if (port == null || port.isEmpty()) {
            return this.defaultPort();
        }
        if (port.equals("*")) {
            return new int[]{0, 65535};
        }
        try {
            int dash = port.indexOf(45);
            if (dash == -1) {
                int p = Integer.parseInt(port);
                return new int[]{p, p};
            }
            String low = port.substring(0, dash);
            String high = port.substring(dash + 1);
            int l = low.isEmpty() ? 0 : Integer.parseInt(low);
            int h = high.isEmpty() ? 65535 : Integer.parseInt(high);
            if (l < 0 || h < 0 || h < l) {
                return this.defaultPort();
            }
            return new int[]{l, h};
        }
        catch (IllegalArgumentException e) {
            return this.defaultPort();
        }
    }
}

