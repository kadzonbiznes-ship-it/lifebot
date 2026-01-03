/*
 * Decompiled with CFR 0.152.
 */
package javax.imageio.stream;

import com.sun.imageio.stream.StreamCloser;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import javax.imageio.stream.ImageOutputStreamImpl;

public class FileCacheImageOutputStream
extends ImageOutputStreamImpl {
    private OutputStream stream;
    private File cacheFile;
    private RandomAccessFile cache;
    private long maxStreamPos = 0L;
    private final StreamCloser.CloseAction closeAction;

    public FileCacheImageOutputStream(OutputStream stream, File cacheDir) throws IOException {
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
    }

    @Override
    public int read() throws IOException {
        this.checkClosed();
        this.bitOffset = 0;
        int val = this.cache.read();
        if (val != -1) {
            ++this.streamPos;
        }
        return val;
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
        int nbytes = this.cache.read(b, off, len);
        if (nbytes != -1) {
            this.streamPos += (long)nbytes;
        }
        return nbytes;
    }

    @Override
    public void write(int b) throws IOException {
        this.flushBits();
        this.cache.write(b);
        ++this.streamPos;
        this.maxStreamPos = Math.max(this.maxStreamPos, this.streamPos);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.flushBits();
        this.cache.write(b, off, len);
        this.streamPos += (long)len;
        this.maxStreamPos = Math.max(this.maxStreamPos, this.streamPos);
    }

    @Override
    public long length() {
        try {
            this.checkClosed();
            return this.cache.length();
        }
        catch (IOException e) {
            return -1L;
        }
    }

    @Override
    public void seek(long pos) throws IOException {
        this.checkClosed();
        if (pos < this.flushedPos) {
            throw new IndexOutOfBoundsException();
        }
        this.cache.seek(pos);
        this.streamPos = this.cache.getFilePointer();
        this.maxStreamPos = Math.max(this.maxStreamPos, this.streamPos);
        this.bitOffset = 0;
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
        this.maxStreamPos = this.cache.length();
        this.seek(this.maxStreamPos);
        this.flushBefore(this.maxStreamPos);
        super.close();
        this.cache.close();
        this.cache = null;
        this.cacheFile.delete();
        this.cacheFile = null;
        this.stream.flush();
        this.stream = null;
        StreamCloser.removeFromQueue(this.closeAction);
    }

    @Override
    public void flushBefore(long pos) throws IOException {
        long flushBytes;
        long oFlushedPos = this.flushedPos;
        super.flushBefore(pos);
        if (flushBytes > 0L) {
            int len;
            int bufLen = 512;
            byte[] buf = new byte[bufLen];
            this.cache.seek(oFlushedPos);
            for (flushBytes = this.flushedPos - oFlushedPos; flushBytes > 0L; flushBytes -= (long)len) {
                len = (int)Math.min(flushBytes, (long)bufLen);
                this.cache.readFully(buf, 0, len);
                this.stream.write(buf, 0, len);
            }
            this.stream.flush();
        }
    }
}

