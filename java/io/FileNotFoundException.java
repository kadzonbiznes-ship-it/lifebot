/*
 * Decompiled with CFR 0.152.
 */
package java.io;

import java.io.IOException;

public class FileNotFoundException
extends IOException {
    private static final long serialVersionUID = -897856973823710492L;

    public FileNotFoundException() {
    }

    public FileNotFoundException(String s) {
        super(s);
    }

    private FileNotFoundException(String path, String reason) {
        super(path + (reason == null ? "" : " (" + reason + ")"));
    }
}

