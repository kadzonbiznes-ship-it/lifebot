/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import sun.nio.ch.IOUtil;

public class FileKey {
    private long dwVolumeSerialNumber;
    private long nFileIndexHigh;
    private long nFileIndexLow;

    private FileKey() {
    }

    public static FileKey create(FileDescriptor fd) throws IOException {
        FileKey fk = new FileKey();
        fk.init(fd);
        return fk;
    }

    public int hashCode() {
        return (int)(this.dwVolumeSerialNumber ^ this.dwVolumeSerialNumber >>> 32) + (int)(this.nFileIndexHigh ^ this.nFileIndexHigh >>> 32) + (int)(this.nFileIndexLow ^ this.nFileIndexHigh >>> 32);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FileKey)) {
            return false;
        }
        FileKey other = (FileKey)obj;
        return this.dwVolumeSerialNumber == other.dwVolumeSerialNumber && this.nFileIndexHigh == other.nFileIndexHigh && this.nFileIndexLow == other.nFileIndexLow;
    }

    private native void init(FileDescriptor var1) throws IOException;

    private static native void initIDs();

    static {
        IOUtil.load();
        FileKey.initIDs();
    }
}

