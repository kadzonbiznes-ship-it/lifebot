/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.stream;

import com.sun.imageio.stream.StreamCloser;
import com.sun.imageio.stream.StreamFinalizer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import javax.imageio.stream.ImageInputStreamImpl;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;

public class FileCacheImageInputStream
extends ImageInputStreamImpl {
    private InputStream stream;
    private File cacheFile;
    private RandomAccessFile cache;
    private static final int BUFFER_LENGTH = 1024;
    private byte[] buf = new byte[1024];
    private long length = 0L;
    private boolean foundEOF = false;
    private final Object disposerReferent;
    private final DisposerRecord disposerRecord;
    private final StreamCloser.CloseAction closeAction;

    public FileCacheImageInputStream(InputStream stream, File cacheDir) throws IOException {
        if (stream == null) {
            throw new IllegalArgumentException("stream == null!");
        }
        if (cacheDir != null && !cacheDir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory!");
        }
        this.stream = stream;
        this.cacheFile = cacheDir == null ? Files.createTempFile("imageio", ".tmp", new FileAttribute[0]).toFile() : Files.createTempFile(cacheDir.toPath(), "imageio", ".tmp", new FileAttribute[0]).toFile();
        this.cache = new RandomAccessFile(this.cacheFile, "rw");
        this.closeAction = StreamCloser.createCloseAction(this);
        StreamCloser.addToQueue(this.closeAction);
        this.disposerRecord = new StreamDisposerRecord(this.cacheFile, this.cache);
        if (this.getClass() == FileCacheImageInputStream.class) {
            this.disposerReferent = new Object();
            Disposer.addRecord(this.disposerReferent, this.disposerRecord);
        } else {
            this.disposerReferent = new StreamFinalizer(this);
        }
    }

    private long readUntil(long pos) throws IOException {
        if (pos < this.length) {
            return pos;
        }
        if (this.foundEOF) {
            return this.length;
        }
        long len = pos - this.length;
        this.cache.seek(this.length);
        while (len > 0L) {
            int nbytes = this.stream.read(this.buf, 0, (int)Math.min(len, 1024L));
            if (nbytes == -1) {
                this.foundEOF = true;
                return this.length;
            }
            this.cache.write(this.buf, 0, nbytes);
            len -= (long)nbytes;
            this.length += (long)nbytes;
        }
        return pos;
    }

    @Override
    public int read() throws IOException {
        this.checkClosed();
        this.bitOffset = 0;
        long next = this.streamPos + 1L;
        long pos = this.readUntil(next);
        if (pos >= next) {
            this.cache.seek(this.streamPos++);
            return this.cache.read();
        }
        return -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        this.checkClosed();
        if (b == null) {
            throw new NullPointerException("b == null!");
        }
        if (off < 0 || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off+len > b.length || off+len < 0!");
        }
        this.bitOffset = 0;
        if (len == 0) {
            return 0;
        }
        long pos = this.readUntil(this.streamPos + (long)len);
        if ((len = (int)Math.min((long)len, pos - this.streamPos)) > 0) {
            this.cache.seek(this.streamPos);
            this.cache.readFully(b, off, len);
            this.streamPos += (long)len;
            return len;
        }
        return -1;
    }

    @Override
    public boolean isCached() {
        return true;
    }

    @Override
    public boolean isCachedFile() {
        return true;
    }

    @Override
    public boolean isCachedMemory() {
        return false;
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.disposerRecord.dispose();
        this.stream = null;
        this.cache = null;
        this.cacheFile = null;
        StreamCloser.removeFromQueue(this.closeAction);
    }

    @Override
    @Deprecated(since="9", forRemoval=true)
    protected void finalize() throws Throwable {
    }

    private static class StreamDisposerRecord
    implements DisposerRecord {
        private File cacheFile;
        private RandomAccessFile cache;

        public StreamDisposerRecord(File cacheFile, RandomAccessFile cache) {
            this.cacheFile = cacheFile;
            this.cache = cache;
        }

        @Override
        public synchronized void dispose() {
            if (this.cache != null) {
                try {
                    this.cache.close();
                }
                catch (IOException iOException) {
                }
                finally {
                    this.cache = null;
                }
            }
            if (this.cacheFile != null) {
                this.cacheFile.delete();
                this.cacheFile = null;
            }
        }
    }
}

