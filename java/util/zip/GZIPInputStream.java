/*
 * Decompiled with CFR 0.152.
 */
package java.util.zip;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

public class GZIPInputStream
extends InflaterInputStream {
    protected CRC32 crc = new CRC32();
    protected boolean eos;
    private boolean closed = false;
    public static final int GZIP_MAGIC = 35615;
    private static final int FTEXT = 1;
    private static final int FHCRC = 2;
    private static final int FEXTRA = 4;
    private static final int FNAME = 8;
    private static final int FCOMMENT = 16;
    private byte[] tmpbuf = new byte[128];

    private void ensureOpen() throws IOException {
        if (this.closed) {
            throw new IOException("Stream closed");
        }
    }

    public GZIPInputStream(InputStream in, int size) throws IOException {
        super(in, in != null ? new Inflater(true) : null, size);
        this.usesDefaultInflater = true;
        this.readHeader(in);
    }

    public GZIPInputStream(InputStream in) throws IOException {
        this(in, 512);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        this.ensureOpen();
        if (this.eos) {
            return -1;
        }
        int n = super.read(buf, off, len);
        if (n == -1) {
            if (!this.readTrailer()) return this.read(buf, off, len);
            this.eos = true;
            return n;
        } else {
            this.crc.update(buf, off, n);
        }
        return n;
    }

    @Override
    public void close() throws IOException {
        if (!this.closed) {
            super.close();
            this.eos = true;
            this.closed = true;
        }
    }

    private int readHeader(InputStream this_in) throws IOException {
        CheckedInputStream in = new CheckedInputStream(this_in, this.crc);
        this.crc.reset();
        if (this.readUShort(in) != 35615) {
            throw new ZipException("Not in GZIP format");
        }
        if (this.readUByte(in) != 8) {
            throw new ZipException("Unsupported compression method");
        }
        int flg = this.readUByte(in);
        this.skipBytes(in, 6);
        int n = 10;
        if ((flg & 4) == 4) {
            int m = this.readUShort(in);
            this.skipBytes(in, m);
            n += m + 2;
        }
        if ((flg & 8) == 8) {
            do {
                ++n;
            } while (this.readUByte(in) != 0);
        }
        if ((flg & 0x10) == 16) {
            do {
                ++n;
            } while (this.readUByte(in) != 0);
        }
        if ((flg & 2) == 2) {
            int v = (int)this.crc.getValue() & 0xFFFF;
            if (this.readUShort(in) != v) {
                throw new ZipException("Corrupt GZIP header");
            }
            n += 2;
        }
        this.crc.reset();
        return n;
    }

    private boolean readTrailer() throws IOException {
        InputStream in = this.in;
        int n = this.inf.getRemaining();
        if (n > 0) {
            in = new SequenceInputStream(new ByteArrayInputStream(this.buf, this.len - n, n), new FilterInputStream(this, in){

                @Override
                public void close() throws IOException {
                }
            });
        }
        if (this.readUInt(in) != this.crc.getValue() || this.readUInt(in) != (this.inf.getBytesWritten() & 0xFFFFFFFFL)) {
            throw new ZipException("Corrupt GZIP trailer");
        }
        if (this.in.available() > 0 || n > 26) {
            int m = 8;
            try {
            }
            catch (IOException ze) {
                return true;
            }
            this.inf.reset();
            if (n > (m += this.readHeader(in))) {
                this.inf.setInput(this.buf, this.len - n + m, n - m);
            }
            return false;
        }
        return true;
    }

    private long readUInt(InputStream in) throws IOException {
        long s = this.readUShort(in);
        return (long)this.readUShort(in) << 16 | s;
    }

    private int readUShort(InputStream in) throws IOException {
        int b = this.readUByte(in);
        return this.readUByte(in) << 8 | b;
    }

    private int readUByte(InputStream in) throws IOException {
        int b = in.read();
        if (b == -1) {
            throw new EOFException();
        }
        if (b < -1 || b > 255) {
            throw new IOException(this.in.getClass().getName() + ".read() returned value out of range -1..255: " + b);
        }
        return b;
    }

    private void skipBytes(InputStream in, int n) throws IOException {
        while (n > 0) {
            int len = in.read(this.tmpbuf, 0, n < this.tmpbuf.length ? n : this.tmpbuf.length);
            if (len == -1) {
                throw new EOFException();
            }
            n -= len;
        }
    }
}

