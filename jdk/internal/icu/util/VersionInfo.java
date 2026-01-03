/*
 * Decompiled with CFR 0.152.
 */
package jdk.internal.icu.util;

import java.util.HashMap;

public final class VersionInfo {
    @Deprecated
    public static final String ICU_DATA_VERSION_PATH = "72b";
    private int m_version_;
    private static final HashMap<Integer, Object> MAP_ = new HashMap();
    private static final String INVALID_VERSION_NUMBER_ = "Invalid version number: Version number may be negative or greater than 255";

    public static VersionInfo getInstance(String version) {
        int index;
        int length = version.length();
        int[] array = new int[]{0, 0, 0, 0};
        int count = 0;
        for (index = 0; count < 4 && index < length; ++index) {
            char c = version.charAt(index);
            if (c == '.') {
                ++count;
                continue;
            }
            if ((c = (char)(c - 48)) < '\u0000' || c > '\t') {
                throw new IllegalArgumentException(INVALID_VERSION_NUMBER_);
            }
            int n = count;
            array[n] = array[n] * 10;
            int n2 = count;
            array[n2] = array[n2] + c;
        }
        if (index != length) {
            throw new IllegalArgumentException("Invalid version number: String '" + version + "' exceeds version format");
        }
        for (int i = 0; i < 4; ++i) {
            if (array[i] >= 0 && array[i] <= 255) continue;
            throw new IllegalArgumentException(INVALID_VERSION_NUMBER_);
        }
        return VersionInfo.getInstance(array[0], array[1], array[2], array[3]);
    }

    public static VersionInfo getInstance(int major, int minor, int milli, int micro) {
        if (major < 0 || major > 255 || minor < 0 || minor > 255 || milli < 0 || milli > 255 || micro < 0 || micro > 255) {
            throw new IllegalArgumentException(INVALID_VERSION_NUMBER_);
        }
        int version = VersionInfo.getInt(major, minor, milli, micro);
        Integer key = version;
        Object result = MAP_.get(key);
        if (result == null) {
            result = new VersionInfo(version);
            MAP_.put(key, result);
        }
        return (VersionInfo)result;
    }

    public int compareTo(VersionInfo other) {
        int diff = (this.m_version_ >>> 1) - (other.m_version_ >>> 1);
        if (diff != 0) {
            return diff;
        }
        return (this.m_version_ & 1) - (other.m_version_ & 1);
    }

    private VersionInfo(int compactversion) {
        this.m_version_ = compactversion;
    }

    private static int getInt(int major, int minor, int milli, int micro) {
        return major << 24 | minor << 16 | milli << 8 | micro;
    }
}

