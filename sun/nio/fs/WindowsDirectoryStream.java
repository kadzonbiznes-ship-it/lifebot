/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.fs;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.NoSuchElementException;
import sun.nio.fs.NativeBuffer;
import sun.nio.fs.WindowsException;
import sun.nio.fs.WindowsFileAttributes;
import sun.nio.fs.WindowsNativeDispatcher;
import sun.nio.fs.WindowsPath;

class WindowsDirectoryStream
implements DirectoryStream<Path> {
    private final WindowsPath dir;
    private final DirectoryStream.Filter<? super Path> filter;
    private final long handle;
    private final String firstName;
    private final NativeBuffer findDataBuffer;
    private final Object closeLock = new Object();
    private boolean isOpen = true;
    private Iterator<Path> iterator;

    WindowsDirectoryStream(WindowsPath dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        this.dir = dir;
        this.filter = filter;
        try {
            String search = dir.getPathForWin32Calls();
            char last = search.charAt(search.length() - 1);
            search = last == ':' || last == '\\' ? search + "*" : search + "\\*";
            WindowsNativeDispatcher.FirstFile first = WindowsNativeDispatcher.FindFirstFile(search);
            this.handle = first.handle();
            this.firstName = first.name();
            this.findDataBuffer = WindowsFileAttributes.getBufferForFindData();
        }
        catch (WindowsException x) {
            if (x.lastError() == 267) {
                throw new NotDirectoryException(dir.getPathForExceptionMessage());
            }
            x.rethrowAsIOException(dir);
            throw new AssertionError();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() throws IOException {
        Object object = this.closeLock;
        synchronized (object) {
            if (!this.isOpen) {
                return;
            }
            this.isOpen = false;
        }
        this.findDataBuffer.release();
        try {
            WindowsNativeDispatcher.FindClose(this.handle);
        }
        catch (WindowsException x) {
            x.rethrowAsIOException(this.dir);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Iterator<Path> iterator() {
        if (!this.isOpen) {
            throw new IllegalStateException("Directory stream is closed");
        }
        WindowsDirectoryStream windowsDirectoryStream = this;
        synchronized (windowsDirectoryStream) {
            if (this.iterator != null) {
                throw new IllegalStateException("Iterator already obtained");
            }
            this.iterator = new WindowsDirectoryIterator(this.firstName);
            return this.iterator;
        }
    }

    private class WindowsDirectoryIterator
    implements Iterator<Path> {
        private boolean atEof = false;
        private String first;
        private Path nextEntry;
        private String prefix;

        WindowsDirectoryIterator(String first) {
            this.first = first;
            this.prefix = WindowsDirectoryStream.this.dir.needsSlashWhenResolving() ? WindowsDirectoryStream.this.dir.toString() + "\\" : WindowsDirectoryStream.this.dir.toString();
        }

        private boolean isSelfOrParent(String name) {
            return name.equals(".") || name.equals("..");
        }

        private Path acceptEntry(String s, BasicFileAttributes attrs) {
            WindowsPath entry = WindowsPath.createFromNormalizedPath(WindowsDirectoryStream.this.dir.getFileSystem(), this.prefix + s, attrs);
            try {
                if (WindowsDirectoryStream.this.filter.accept(entry)) {
                    return entry;
                }
            }
            catch (IOException ioe) {
                throw new DirectoryIteratorException(ioe);
            }
            return null;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private Path readNextEntry() {
            Path entry;
            if (this.first != null) {
                this.nextEntry = this.isSelfOrParent(this.first) ? null : this.acceptEntry(this.first, null);
                this.first = null;
                if (this.nextEntry != null) {
                    return this.nextEntry;
                }
            }
            while (true) {
                WindowsFileAttributes attrs;
                String name = null;
                Object object = WindowsDirectoryStream.this.closeLock;
                synchronized (object) {
                    try {
                        if (WindowsDirectoryStream.this.isOpen) {
                            name = WindowsNativeDispatcher.FindNextFile(WindowsDirectoryStream.this.handle, WindowsDirectoryStream.this.findDataBuffer.address());
                        }
                    }
                    catch (WindowsException x) {
                        IOException ioe = x.asIOException(WindowsDirectoryStream.this.dir);
                        throw new DirectoryIteratorException(ioe);
                    }
                    if (name == null) {
                        this.atEof = true;
                        return null;
                    }
                    if (this.isSelfOrParent(name)) {
                        continue;
                    }
                    attrs = WindowsFileAttributes.fromFindData(WindowsDirectoryStream.this.findDataBuffer.address());
                }
                entry = this.acceptEntry(name, attrs);
                if (entry != null) break;
            }
            return entry;
        }

        @Override
        public synchronized boolean hasNext() {
            if (this.nextEntry == null && !this.atEof) {
                this.nextEntry = this.readNextEntry();
            }
            return this.nextEntry != null;
        }

        @Override
        public synchronized Path next() {
            Path result = null;
            if (this.nextEntry == null && !this.atEof) {
                result = this.readNextEntry();
            } else {
                result = this.nextEntry;
                this.nextEntry = null;
            }
            if (result == null) {
                throw new NoSuchElementException();
            }
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

