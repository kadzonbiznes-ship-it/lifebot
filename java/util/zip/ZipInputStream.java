/*
 * Decompiled with CFR 0.152.
 */
package java.util.zip;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.zip.CRC32;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipCoder;
import java.util.zip.ZipConstants;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipUtils;
import sun.nio.cs.UTF_8;

public class ZipInputStream
extends InflaterInputStream
implements ZipConstants {
    private ZipEntry entry;
    private int flag;
    private CRC32 crc = new CRC32();
    private long remaining;
    private byte[] tmpbuf = new byte[512];
    private static final int STORED = 0;
    private static final int DEFLATED = 8;
    private boolean closed = false;
    private boolean entryEOF = false;
    private ZipCoder zc;
    private byte[] b = new byte[256];

    private void ensureOpen() throws IOException {
        if (this.closed) {
            throw new IOException("Stream closed");
        }
    }

    public ZipInputStream(InputStream in) {
        this(in, UTF_8.INSTANCE);
    }

    public ZipInputStream(InputStream in, Charset charset) {
        super(new PushbackInputStream(in, 512), new Inflater(true), 512);
        this.usesDefaultInflater = true;
        if (in == null) {
            throw new NullPointerException("in is null");
        }
        if (charset == null) {
            throw new NullPointerException("charset is null");
        }
        this.zc = ZipCoder.get(charset);
    }

    public ZipEntry getNextEntry() throws IOException {
        this.ensureOpen();
        if (this.entry != null) {
            this.closeEntry();
        }
        this.crc.reset();
        this.inf.reset();
        this.entry = this.readLOC();
        if (this.entry == null) {
            return null;
        }
        if (this.entry.method == 0) {
            this.remaining = this.entry.size;
        }
        this.entryEOF = false;
        return this.entry;
    }

    public void closeEntry() throws IOException {
        this.ensureOpen();
        while (this.read(this.tmpbuf, 0, this.tmpbuf.length) != -1) {
        }
        this.entryEOF = true;
    }

    @Override
    public int available() throws IOException {
        this.ensureOpen();
        if (this.entryEOF) {
            return 0;
        }
        return 1;
    }

    @Override
    public int read() throws IOException {
        return super.read();
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return super.readAllBytes();
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        return super.readNBytes(len);
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        return super.readNBytes(b, off, len);
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        super.skipNBytes(n);
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        return super.transferTo(out);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        this.ensureOpen();
        Objects.checkFromIndexSize(off, len, b.length);
        if (len == 0) {
            return 0;
        }
        if (this.entry == null) {
            return -1;
        }
        switch (this.entry.method) {
            case 8: {
                len = super.read(b, off, len);
                if (len == -1) {
                    this.readEnd(this.entry);
                    this.entryEOF = true;
                    this.entry = null;
                } else {
                    this.crc.update(b, off, len);
                }
                return len;
            }
            case 0: {
                if (this.remaining <= 0L) {
                    this.entryEOF = true;
                    this.entry = null;
                    return -1;
                }
                if ((long)len > this.remaining) {
                    len = (int)this.remaining;
                }
                if ((len = this.in.read(b, off, len)) == -1) {
                    throw new ZipException("unexpected EOF");
                }
                this.crc.update(b, off, len);
                this.remaining -= (long)len;
                if (this.remaining == 0L && this.entry.crc != this.crc.getValue()) {
                    throw new ZipException("invalid entry CRC (expected 0x" + Long.toHexString(this.entry.crc) + " but got 0x" + Long.toHexString(this.crc.getValue()) + ")");
                }
                return len;
            }
        }
        throw new ZipException("invalid compression method");
    }

    @Override
    public long skip(long n) throws IOException {
        int total;
        int len;
        if (n < 0L) {
            throw new IllegalArgumentException("negative skip length");
        }
        this.ensureOpen();
        int max = (int)Math.min(n, Integer.MAX_VALUE);
        for (total = 0; total < max; total += len) {
            len = max - total;
            if (len > this.tmpbuf.length) {
                len = this.tmpbuf.length;
            }
            if ((len = this.read(this.tmpbuf, 0, len)) != -1) continue;
            this.entryEOF = true;
            break;
        }
        return total;
    }

    @Override
    public void close() throws IOException {
        if (!this.closed) {
            super.close();
            this.closed = true;
        }
    }

    private ZipEntry readLOC() throws IOException {
        int blen;
        try {
            this.readFully(this.tmpbuf, 0, 30);
        }
        catch (EOFException e) {
            return null;
        }
        if (ZipUtils.get32(this.tmpbuf, 0) != 67324752L) {
            return null;
        }
        this.flag = ZipUtils.get16(this.tmpbuf, 6);
        int len = ZipUtils.get16(this.tmpbuf, 26);
        if (len > (blen = this.b.length)) {
            while (len > (blen *= 2)) {
            }
            this.b = new byte[blen];
        }
        this.readFully(this.b, 0, len);
        ZipEntry e = this.createZipEntry((this.flag & 0x800) != 0 ? ZipCoder.toStringUTF8(this.b, len) : this.zc.toString(this.b, len));
        if ((this.flag & 1) == 1) {
            throw new ZipException("encrypted ZIP entry not supported");
        }
        e.method = ZipUtils.get16(this.tmpbuf, 8);
        e.xdostime = ZipUtils.get32(this.tmpbuf, 10);
        if ((this.flag & 8) == 8) {
            if (e.method != 8) {
                throw new ZipException("only DEFLATED entries can have EXT descriptor");
            }
        } else {
            e.crc = ZipUtils.get32(this.tmpbuf, 14);
            e.csize = ZipUtils.get32(this.tmpbuf, 18);
            e.size = ZipUtils.get32(this.tmpbuf, 22);
        }
        if ((len = ZipUtils.get16(this.tmpbuf, 28)) > 0) {
            byte[] extra = new byte[len];
            this.readFully(extra, 0, len);
            e.setExtra0(extra, e.csize == 0xFFFFFFFFL || e.size == 0xFFFFFFFFL, true);
        }
        return e;
    }

    protected ZipEntry createZipEntry(String name) {
        return new ZipEntry(name);
    }

    private void readEnd(ZipEntry e) throws IOException {
        int n = this.inf.getRemaining();
        if (n > 0) {
            ((PushbackInputStream)this.in).unread(this.buf, this.len - n, n);
        }
        if ((this.flag & 8) == 8) {
            if (this.inf.getBytesWritten() > 0xFFFFFFFFL || this.inf.getBytesRead() > 0xFFFFFFFFL) {
                this.readFully(this.tmpbuf, 0, 24);
                long sig = ZipUtils.get32(this.tmpbuf, 0);
                if (sig != 134695760L) {
                    e.crc = sig;
                    e.csize = ZipUtils.get64(this.tmpbuf, 4);
                    e.size = ZipUtils.get64(this.tmpbuf, 12);
                    ((PushbackInputStream)this.in).unread(this.tmpbuf, 20, 4);
                } else {
                    e.crc = ZipUtils.get32(this.tmpbuf, 4);
                    e.csize = ZipUtils.get64(this.tmpbuf, 8);
                    e.size = ZipUtils.get64(this.tmpbuf, 16);
                }
            } else {
                this.readFully(this.tmpbuf, 0, 16);
                long sig = ZipUtils.get32(this.tmpbuf, 0);
                if (sig != 134695760L) {
                    e.crc = sig;
                    e.csize = ZipUtils.get32(this.tmpbuf, 4);
                    e.size = ZipUtils.get32(this.tmpbuf, 8);
                    ((PushbackInputStream)this.in).unread(this.tmpbuf, 12, 4);
                } else {
                    e.crc = ZipUtils.get32(this.tmpbuf, 4);
                    e.csize = ZipUtils.get32(this.tmpbuf, 8);
                    e.size = ZipUtils.get32(this.tmpbuf, 12);
                }
            }
        }
        if (e.size != this.inf.getBytesWritten()) {
            throw new ZipException("invalid entry size (expected " + e.size + " but got " + this.inf.getBytesWritten() + " bytes)");
        }
        if (e.csize != this.inf.getBytesRead()) {
            throw new ZipException("invalid entry compressed size (expected " + e.csize + " but got " + this.inf.getBytesRead() + " bytes)");
        }
        if (e.crc != this.crc.getValue()) {
            throw new ZipException("invalid entry CRC (expected 0x" + Long.toHexString(e.crc) + " but got 0x" + Long.toHexString(this.crc.getValue()) + ")");
        }
    }

    private void readFully(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int n = this.in.read(b, off, len);
            if (n == -1) {
                throw new EOFException();
            }
            off += n;
            len -= n;
        }
    }
}

