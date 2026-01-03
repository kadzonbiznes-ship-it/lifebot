/*
 * Decompiled with CFR 0.152.
 */
package sun.awt;

import java.util.HashMap;
import java.util.Map;
import jdk.internal.util.OperatingSystem;

public class OSInfo {
    public static final WindowsVersion WINDOWS_UNKNOWN = new WindowsVersion(-1, -1);
    public static final WindowsVersion WINDOWS_95 = new WindowsVersion(4, 0);
    public static final WindowsVersion WINDOWS_98 = new WindowsVersion(4, 10);
    public static final WindowsVersion WINDOWS_ME = new WindowsVersion(4, 90);
    public static final WindowsVersion WINDOWS_2000 = new WindowsVersion(5, 0);
    public static final WindowsVersion WINDOWS_XP = new WindowsVersion(5, 1);
    public static final WindowsVersion WINDOWS_2003 = new WindowsVersion(5, 2);
    public static final WindowsVersion WINDOWS_VISTA = new WindowsVersion(6, 0);
    public static final WindowsVersion WINDOWS_7 = new WindowsVersion(6, 1);
    private static final String OS_VERSION = "os.version";
    private static final Map<String, WindowsVersion> windowsVersionMap = new HashMap<String, WindowsVersion>();
    private static final OSType CURRENT_OSTYPE = OSInfo.getOSTypeImpl();

    private OSInfo() {
    }

    public static OSType getOSType() {
        return CURRENT_OSTYPE;
    }

    private static OSType getOSTypeImpl() {
        OperatingSystem os = OperatingSystem.current();
        return switch (os) {
            case OperatingSystem.WINDOWS -> OSType.WINDOWS;
            case OperatingSystem.LINUX -> OSType.LINUX;
            case OperatingSystem.MACOS -> OSType.MACOSX;
            case OperatingSystem.AIX -> OSType.AIX;
            default -> OSType.UNKNOWN;
        };
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static WindowsVersion getWindowsVersion() throws SecurityException {
        String osVersion = System.getProperty(OS_VERSION);
        if (osVersion == null) {
            return WINDOWS_UNKNOWN;
        }
        Map<String, WindowsVersion> map = windowsVersionMap;
        synchronized (map) {
            WindowsVersion result = windowsVersionMap.get(osVersion);
            if (result == null) {
                String[] arr = osVersion.split("\\.");
                if (arr.length == 2) {
                    try {
                        result = new WindowsVersion(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
                    }
                    catch (NumberFormatException e) {
                        return WINDOWS_UNKNOWN;
                    }
                } else {
                    return WINDOWS_UNKNOWN;
                }
                windowsVersionMap.put(osVersion, result);
            }
            return result;
        }
    }

    static {
        windowsVersionMap.put(WINDOWS_95.toString(), WINDOWS_95);
        windowsVersionMap.put(WINDOWS_98.toString(), WINDOWS_98);
        windowsVersionMap.put(WINDOWS_ME.toString(), WINDOWS_ME);
        windowsVersionMap.put(WINDOWS_2000.toString(), WINDOWS_2000);
        windowsVersionMap.put(WINDOWS_XP.toString(), WINDOWS_XP);
        windowsVersionMap.put(WINDOWS_2003.toString(), WINDOWS_2003);
        windowsVersionMap.put(WINDOWS_VISTA.toString(), WINDOWS_VISTA);
        windowsVersionMap.put(WINDOWS_7.toString(), WINDOWS_7);
    }

    public static enum OSType {
        WINDOWS,
        LINUX,
        MACOSX,
        AIX,
        UNKNOWN;

    }

    public static class WindowsVersion
    implements Comparable<WindowsVersion> {
        private final int major;
        private final int minor;

        private WindowsVersion(int major, int minor) {
            this.major = major;
            this.minor = minor;
        }

        public int getMajor() {
            return this.major;
        }

        public int getMinor() {
            return this.minor;
        }

        @Override
        public int compareTo(WindowsVersion o) {
            int result = this.major - o.getMajor();
            if (result == 0) {
                result = this.minor - o.getMinor();
            }
            return result;
        }

        public boolean equals(Object obj) {
            return obj instanceof WindowsVersion && this.compareTo((WindowsVersion)obj) == 0;
        }

        public int hashCode() {
            return 31 * this.major + this.minor;
        }

        public String toString() {
            return this.major + "." + this.minor;
        }
    }
}

