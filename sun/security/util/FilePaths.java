/*
 * Decompiled with CFR 0.152.
 */
package sun.security.util;

import java.io.File;
import jdk.internal.util.StaticProperty;

public class FilePaths {
    public static String cacerts() {
        return StaticProperty.javaHome() + File.separator + "lib" + File.separator + "security" + File.separator + "cacerts";
    }
}

