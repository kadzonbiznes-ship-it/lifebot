/*
 * Decompiled with CFR 0.152.
 */
package org.pbrands.util;

public class MacUtil {
    public static boolean isMacOs() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }
}

