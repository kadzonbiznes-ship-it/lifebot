/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.fs;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;
import sun.nio.fs.WindowsNativeDispatcher;
import sun.nio.fs.WindowsPath;

class WindowsException
extends Exception {
    static final long serialVersionUID = 2765039493083748820L;
    private int lastError;
    private String msg;

    WindowsException(int lastError) {
        this.lastError = lastError;
        this.msg = null;
    }

    WindowsException(String msg) {
        this.lastError = 0;
        this.msg = msg;
    }

    int lastError() {
        return this.lastError;
    }

    String errorString() {
        if (this.msg == null) {
            this.msg = WindowsNativeDispatcher.FormatMessage(this.lastError);
            if (this.msg == null) {
                this.msg = "Unknown error: 0x" + Integer.toHexString(this.lastError);
            }
        }
        return this.msg;
    }

    @Override
    public String getMessage() {
        return this.errorString();
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    private IOException translateToIOException(String file, String other) {
        if (this.lastError() == 0) {
            return new IOException(this.errorString());
        }
        if (this.lastError() == 2 || this.lastError() == 3) {
            return new NoSuchFileException(file, other, null);
        }
        if (this.lastError() == 80 || this.lastError() == 183) {
            return new FileAlreadyExistsException(file, other, null);
        }
        if (this.lastError() == 5) {
            return new AccessDeniedException(file, other, null);
        }
        return new FileSystemException(file, other, this.errorString());
    }

    void rethrowAsIOException(String file) throws IOException {
        IOException x = this.translateToIOException(file, null);
        throw x;
    }

    void rethrowAsIOException(WindowsPath file, WindowsPath other) throws IOException {
        String a = file == null ? null : file.getPathForExceptionMessage();
        String b = other == null ? null : other.getPathForExceptionMessage();
        IOException x = this.translateToIOException(a, b);
        throw x;
    }

    void rethrowAsIOException(WindowsPath file) throws IOException {
        this.rethrowAsIOException(file, null);
    }

    IOException asIOException(WindowsPath file) {
        return this.translateToIOException(file.getPathForExceptionMessage(), null);
    }
}

