/*
 * Decompiled with CFR 0.152.
 */
package sun.security.action;

import java.io.File;
import java.io.FileInputStream;
import java.security.PrivilegedExceptionAction;

public class OpenFileInputStreamAction
implements PrivilegedExceptionAction<FileInputStream> {
    private final File file;

    public OpenFileInputStreamAction(File file) {
        this.file = file;
    }

    public OpenFileInputStreamAction(String filename) {
        this.file = new File(filename);
    }

    @Override
    public FileInputStream run() throws Exception {
        return new FileInputStream(this.file);
    }
}

