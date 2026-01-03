/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import sun.net.www.ParseUtil;

public class PropertyExpander {
    public static String expand(String value) throws ExpandException {
        return PropertyExpander.expand(value, false);
    }

    public static String expand(String value, boolean encodeURL) throws ExpandException {
        if (value == null) {
            return null;
        }
        int p = value.indexOf("${");
        if (p == -1) {
            return value;
        }
        StringBuilder sb = new StringBuilder(value.length());
        int max = value.length();
        int i = 0;
        while (p < max) {
            int pe;
            if (p > i) {
                sb.append(value.substring(i, p));
            }
            if ((pe = p + 2) < max && value.charAt(pe) == '{') {
                if ((pe = value.indexOf("}}", pe)) == -1 || pe + 2 == max) {
                    sb.append(value.substring(p));
                    break;
                }
                sb.append(value.substring(p, ++pe + 1));
            } else {
                while (pe < max && value.charAt(pe) != '}') {
                    ++pe;
                }
                if (pe == max) {
                    sb.append(value.substring(p, pe));
                    break;
                }
                String prop = value.substring(p + 2, pe);
                if (prop.equals("/")) {
                    sb.append(File.separatorChar);
                } else {
                    String val = System.getProperty(prop);
                    if (val != null) {
                        if (encodeURL) {
                            try {
                                if (sb.length() > 0 || !new URI(val).isAbsolute()) {
                                    val = ParseUtil.encodePath(val);
                                }
                            }
                            catch (URISyntaxException use) {
                                val = ParseUtil.encodePath(val);
                            }
                        }
                        sb.append(val);
                    } else {
                        throw new ExpandException("unable to expand property " + prop);
                    }
                }
            }
            if ((p = value.indexOf("${", i = pe + 1)) != -1) continue;
            if (i >= max) break;
            sb.append(value.substring(i, max));
            break;
        }
        return sb.toString();
    }

    public static class ExpandException
    extends GeneralSecurityException {
        private static final long serialVersionUID = -7941948581406161702L;

        public ExpandException(String msg) {
            super(msg);
        }
    }
}

