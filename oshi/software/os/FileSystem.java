/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  oshi.annotation.concurrent.ThreadSafe
 *  oshi.software.os.OSFileStore
 */
package oshi.software.os;

import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.software.os.OSFileStore;

@ThreadSafe
public interface FileSystem {
    public List<OSFileStore> getFileStores();

    public List<OSFileStore> getFileStores(boolean var1);

    public long getOpenFileDescriptors();

    public long getMaxFileDescriptors();

    public long getMaxFileDescriptorsPerProcess();
}

