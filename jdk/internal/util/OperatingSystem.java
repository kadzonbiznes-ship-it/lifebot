/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.util;

import java.util.Locale;
import jdk.internal.vm.annotation.ForceInline;

public enum OperatingSystem {
    LINUX,
    MACOS,
    WINDOWS,
    AIX;

    private static final OperatingSystem CURRENT_OS;

    @ForceInline
    public static boolean isLinux() {
        return false;
    }

    @ForceInline
    public static boolean isMacOS() {
        return false;
    }

    @ForceInline
    public static boolean isWindows() {
        return true;
    }

    @ForceInline
    public static boolean isAix() {
        return false;
    }

    public static OperatingSystem current() {
        return CURRENT_OS;
    }

    private static OperatingSystem initOS() {
        return OperatingSystem.valueOf("windows".toUpperCase(Locale.ROOT));
    }

    static {
        CURRENT_OS = OperatingSystem.initOS();
    }
}

