/*
 * Decompiled with CFR 0.152.
 */
package sun.security.ssl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Pattern;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import sun.net.util.IPAddressUtil;
import sun.security.action.GetPropertyAction;
import sun.security.ssl.SSLLogger;

final class Utilities {
    private static final String indent = "  ";
    private static final Pattern lineBreakPatern = Pattern.compile("\\r\\n|\\n|\\r");
    private static final HexFormat HEX_FORMATTER = HexFormat.of().withUpperCase();

    Utilities() {
    }

    static List<SNIServerName> addToSNIServerNameList(List<SNIServerName> serverNames, String hostname) {
        SNIHostName sniHostName = Utilities.rawToSNIHostName(hostname);
        if (sniHostName == null) {
            return serverNames;
        }
        int size = serverNames.size();
        ArrayList<SNIServerName> sniList = size != 0 ? new ArrayList<SNIServerName>(serverNames) : new ArrayList(1);
        boolean reset = false;
        for (int i = 0; i < size; ++i) {
            SNIServerName serverName = (SNIServerName)sniList.get(i);
            if (serverName.getType() != 0) continue;
            sniList.set(i, sniHostName);
            if (SSLLogger.isOn && SSLLogger.isOn("ssl")) {
                SSLLogger.fine("the previous server name in SNI (" + serverName + ") was replaced with (" + sniHostName + ")", new Object[0]);
            }
            reset = true;
            break;
        }
        if (!reset) {
            sniList.add(sniHostName);
        }
        return Collections.unmodifiableList(sniList);
    }

    private static SNIHostName rawToSNIHostName(String hostname) {
        block4: {
            if (hostname != null && hostname.endsWith(".")) {
                hostname = hostname.substring(0, hostname.length() - 1);
            }
            if (!(hostname == null || hostname.indexOf(46) <= 0 || hostname.endsWith(".") || IPAddressUtil.isIPv4LiteralAddress(hostname) || IPAddressUtil.isIPv6LiteralAddress(hostname))) {
                try {
                    return new SNIHostName(hostname);
                }
                catch (IllegalArgumentException iae) {
                    if (!SSLLogger.isOn || !SSLLogger.isOn("ssl")) break block4;
                    SSLLogger.fine(hostname + "\" is not a legal HostName for  server name indication", new Object[0]);
                }
            }
        }
        return null;
    }

    static boolean getBooleanProperty(String propName, boolean defaultValue) {
        String b = GetPropertyAction.privilegedGetProperty(propName);
        if (b == null) {
            return defaultValue;
        }
        if (b.equalsIgnoreCase("false")) {
            return false;
        }
        if (b.equalsIgnoreCase("true")) {
            return true;
        }
        throw new RuntimeException("Value of " + propName + " must either be 'true' or 'false'");
    }

    static String indent(String source) {
        return Utilities.indent(source, indent);
    }

    static String indent(String source, String prefix) {
        StringBuilder builder = new StringBuilder();
        if (source == null) {
            builder.append("\n").append(prefix).append("<blank message>");
        } else {
            String[] lines = lineBreakPatern.split(source);
            boolean isFirst = true;
            for (String line : lines) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append("\n");
                }
                builder.append(prefix).append(line);
            }
        }
        return builder.toString();
    }

    static String byte16HexString(int id) {
        return "0x" + HEX_FORMATTER.toHexDigits((short)id);
    }

    static String toHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        return HEX_FORMATTER.formatHex(bytes);
    }

    static String toHexString(long lv) {
        StringBuilder builder = new StringBuilder(128);
        boolean isFirst = true;
        do {
            if (isFirst) {
                isFirst = false;
            } else {
                builder.append(' ');
            }
            HEX_FORMATTER.toHexDigits(builder, (byte)lv);
        } while ((lv >>>= 8) != 0L);
        builder.reverse();
        return builder.toString();
    }

    static byte[] toByteArray(BigInteger bi) {
        byte[] b = bi.toByteArray();
        if (b.length > 1 && b[0] == 0) {
            int n = b.length - 1;
            byte[] newarray = new byte[n];
            System.arraycopy(b, 1, newarray, 0, n);
            b = newarray;
        }
        return b;
    }

    static void reverseBytes(byte[] arr) {
        int i = 0;
        for (int j = arr.length - 1; i < j; ++i, --j) {
            Utilities.swap(arr, i, j);
        }
    }

    static <T> boolean contains(T[] array, T item) {
        for (T t : array) {
            if (!item.equals(t)) continue;
            return true;
        }
        return false;
    }

    private static void swap(byte[] arr, int i, int j) {
        byte tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
}

