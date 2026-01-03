/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.fs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystemException;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Locale;
import sun.nio.fs.WindowsException;
import sun.nio.fs.WindowsLinkSupport;
import sun.nio.fs.WindowsNativeDispatcher;
import sun.nio.fs.WindowsPath;

class WindowsFileStore
extends FileStore {
    private final String root;
    private final WindowsNativeDispatcher.VolumeInformation volInfo;
    private final int volType;
    private final String displayName;
    private int hashCode;

    private WindowsFileStore(String root) throws WindowsException {
        assert (root.charAt(root.length() - 1) == '\\');
        this.root = root;
        this.volInfo = WindowsNativeDispatcher.GetVolumeInformation(root);
        this.volType = WindowsNativeDispatcher.GetDriveType(root);
        String vol = this.volInfo.volumeName();
        this.displayName = !vol.isEmpty() ? vol : (this.volType == 2 ? "Removable Disk" : "");
    }

    static WindowsFileStore create(String root, boolean ignoreNotReady) throws IOException {
        try {
            return new WindowsFileStore(root);
        }
        catch (WindowsException x) {
            if (ignoreNotReady && x.lastError() == 21) {
                return null;
            }
            x.rethrowAsIOException(root);
            return null;
        }
    }

    static WindowsFileStore create(WindowsPath file) throws IOException {
        try {
            String target = WindowsLinkSupport.getFinalPath(file, true);
            try {
                return WindowsFileStore.createFromPath(target);
            }
            catch (WindowsException e) {
                if (e.lastError() != 144 && e.lastError() != 87 && e.lastError() != 267) {
                    throw e;
                }
                target = WindowsLinkSupport.getFinalPath(file);
                if (target == null) {
                    throw new FileSystemException(file.getPathForExceptionMessage(), null, "Couldn't resolve path");
                }
                return WindowsFileStore.createFromPath(target);
            }
        }
        catch (WindowsException x) {
            x.rethrowAsIOException(file);
            return null;
        }
    }

    private static WindowsFileStore createFromPath(String target) throws WindowsException {
        String root = WindowsNativeDispatcher.GetVolumePathName(target);
        return new WindowsFileStore(root);
    }

    WindowsNativeDispatcher.VolumeInformation volumeInformation() {
        return this.volInfo;
    }

    int volumeType() {
        return this.volType;
    }

    @Override
    public String name() {
        return this.volInfo.volumeName();
    }

    @Override
    public String type() {
        return this.volInfo.fileSystemName();
    }

    @Override
    public boolean isReadOnly() {
        return (this.volInfo.flags() & 0x80000) != 0;
    }

    private WindowsNativeDispatcher.DiskFreeSpace readDiskFreeSpaceEx() throws IOException {
        try {
            return WindowsNativeDispatcher.GetDiskFreeSpaceEx(this.root);
        }
        catch (WindowsException x) {
            x.rethrowAsIOException(this.root);
            return null;
        }
    }

    private WindowsNativeDispatcher.DiskFreeSpace readDiskFreeSpace() throws IOException {
        try {
            return WindowsNativeDispatcher.GetDiskFreeSpace(this.root);
        }
        catch (WindowsException x) {
            x.rethrowAsIOException(this.root);
            return null;
        }
    }

    @Override
    public long getTotalSpace() throws IOException {
        long space = this.readDiskFreeSpaceEx().totalNumberOfBytes();
        return space >= 0L ? space : Long.MAX_VALUE;
    }

    @Override
    public long getUsableSpace() throws IOException {
        long space = this.readDiskFreeSpaceEx().freeBytesAvailable();
        return space >= 0L ? space : Long.MAX_VALUE;
    }

    @Override
    public long getUnallocatedSpace() throws IOException {
        long space = this.readDiskFreeSpaceEx().freeBytesAvailable();
        return space >= 0L ? space : Long.MAX_VALUE;
    }

    @Override
    public long getBlockSize() throws IOException {
        return this.readDiskFreeSpace().bytesPerSector();
    }

    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
        if (type == null) {
            throw new NullPointerException();
        }
        return (V)((FileStoreAttributeView)null);
    }

    @Override
    public Object getAttribute(String attribute) throws IOException {
        if (attribute.equals("totalSpace")) {
            return this.getTotalSpace();
        }
        if (attribute.equals("usableSpace")) {
            return this.getUsableSpace();
        }
        if (attribute.equals("unallocatedSpace")) {
            return this.getUnallocatedSpace();
        }
        if (attribute.equals("bytesPerSector")) {
            return this.getBlockSize();
        }
        if (attribute.equals("volume:vsn")) {
            return this.volInfo.volumeSerialNumber();
        }
        if (attribute.equals("volume:isRemovable")) {
            return this.volType == 2;
        }
        if (attribute.equals("volume:isCdrom")) {
            return this.volType == 5;
        }
        throw new UnsupportedOperationException("'" + attribute + "' not recognized");
    }

    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        if (type == null) {
            throw new NullPointerException();
        }
        if (type == BasicFileAttributeView.class || type == DosFileAttributeView.class) {
            return true;
        }
        if (type == AclFileAttributeView.class || type == FileOwnerAttributeView.class) {
            return (this.volInfo.flags() & 8) != 0;
        }
        if (type == UserDefinedFileAttributeView.class) {
            return (this.volInfo.flags() & 0x40000) != 0;
        }
        return false;
    }

    @Override
    public boolean supportsFileAttributeView(String name) {
        if (name.equals("basic") || name.equals("dos")) {
            return true;
        }
        if (name.equals("acl")) {
            return this.supportsFileAttributeView(AclFileAttributeView.class);
        }
        if (name.equals("owner")) {
            return this.supportsFileAttributeView(FileOwnerAttributeView.class);
        }
        if (name.equals("user")) {
            return this.supportsFileAttributeView(UserDefinedFileAttributeView.class);
        }
        return false;
    }

    public boolean equals(Object ob) {
        if (ob == this) {
            return true;
        }
        if (ob instanceof WindowsFileStore) {
            WindowsFileStore other = (WindowsFileStore)ob;
            if (this.root.equals(other.root)) {
                return true;
            }
            if (this.volType == 3 && other.volumeType() == 3) {
                return this.root.equalsIgnoreCase(other.root);
            }
        }
        return false;
    }

    public int hashCode() {
        int hc = this.hashCode;
        if (hc == 0) {
            this.hashCode = hc = this.volType == 3 ? this.root.toLowerCase(Locale.ROOT).hashCode() : this.root.hashCode();
        }
        return hc;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.displayName);
        if (sb.length() > 0) {
            sb.append(" ");
        }
        sb.append("(");
        sb.append(this.root.subSequence(0, this.root.length() - 1));
        sb.append(")");
        return sb.toString();
    }
}

