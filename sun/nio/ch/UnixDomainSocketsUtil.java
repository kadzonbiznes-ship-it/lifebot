/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import jdk.internal.util.StaticProperty;
import sun.net.NetProperties;

class UnixDomainSocketsUtil {
    private UnixDomainSocketsUtil() {
    }

    static Charset getCharset() {
        return StandardCharsets.UTF_8;
    }

    static String getTempDir() {
        PrivilegedAction<String> action = () -> {
            String s = NetProperties.get("jdk.net.unixdomain.tmpdir");
            if (s != null) {
                return s;
            }
            String temp = System.getenv("TEMP");
            if (temp != null) {
                return temp;
            }
            return StaticProperty.javaIoTmpDir();
        };
        return AccessController.doPrivileged(action);
    }
}

