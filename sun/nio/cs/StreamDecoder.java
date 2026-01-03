/*
 * Decompiled with CFR 0.152.
 */
package sun.nio.cs;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import jdk.internal.misc.InternalLock;
import sun.nio.cs.HistoricallyNamedCharset;

public class StreamDecoder
extends Reader {
    private static final int MIN_BYTE_BUFFER_SIZE = 32;
    private static final int DEFAULT_BYTE_BUFFER_SIZE = 8192;
    private volatile boolean closed;
    private boolean haveLeftoverChar = false;
    private char leftoverChar;
    private final Charset cs;
    private final CharsetDecoder decoder;
    private final ByteBuffer bb;
    private final InputStream in;
    private final ReadableByteChannel ch;

    private void ensureOpen() throws IOException {
        if (this.closed) {
            throw new IOException("Stream closed");
        }
    }

    public static StreamDecoder forInputStreamReader(InputStream in, Object lock, String charsetName) throws UnsupportedEncodingException {
        try {
            return new StreamDecoder(in, lock, Charset.forName(charsetName));
        }
        catch (IllegalCharsetNameException | UnsupportedCharsetException x) {
            throw new UnsupportedEncodingException(charsetName);
        }
    }

    public static StreamDecoder forInputStreamReader(InputStream in, Object lock, Charset cs) {
        return new StreamDecoder(in, lock, cs);
    }

    public static StreamDecoder forInputStreamReader(InputStream in, Object lock, CharsetDecoder dec) {
        return new StreamDecoder(in, lock, dec);
    }

    public static StreamDecoder forDecoder(ReadableByteChannel ch, CharsetDecoder dec, int minBufferCap) {
        return new StreamDecoder(ch, dec, minBufferCap);
    }

    public String getEncoding() {
        if (this.isOpen()) {
            return this.encodingName();
        }
        return null;
    }

    @Override
    public int read() throws IOException {
        return this.read0();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int read0() throws IOException {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                int n = this.lockedRead0();
                return n;
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            return this.lockedRead0();
        }
    }

    private int lockedRead0() throws IOException {
        if (this.haveLeftoverChar) {
            this.haveLeftoverChar = false;
            return this.leftoverChar;
        }
        char[] cb = new char[2];
        int n = this.read(cb, 0, 2);
        switch (n) {
            case -1: {
                return -1;
            }
            case 2: {
                this.leftoverChar = cb[1];
                this.haveLeftoverChar = true;
            }
            case 1: {
                return cb[0];
            }
        }
        assert (false) : n;
        return -1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int read(char[] cbuf, int offset, int length) throws IOException {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                int n = this.lockedRead(cbuf, offset, length);
                return n;
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            return this.lockedRead(cbuf, offset, length);
        }
    }

    private int lockedRead(char[] cbuf, int offset, int length) throws IOException {
        int off = offset;
        int len = length;
        this.ensureOpen();
        if (off < 0 || off > cbuf.length || len < 0 || off + len > cbuf.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        int n = 0;
        if (this.haveLeftoverChar) {
            cbuf[off] = this.leftoverChar;
            ++off;
            this.haveLeftoverChar = false;
            n = 1;
            if (--len == 0 || !this.implReady()) {
                return n;
            }
        }
        if (len == 1) {
            int c = this.read0();
            if (c == -1) {
                return n == 0 ? -1 : n;
            }
            cbuf[off] = (char)c;
            return n + 1;
        }
        int nr = this.implRead(cbuf, off, off + len);
        return nr < 0 ? (n == 1 ? 1 : nr) : n + nr;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean ready() throws IOException {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                boolean bl = this.lockedReady();
                return bl;
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            return this.lockedReady();
        }
    }

    private boolean lockedReady() throws IOException {
        this.ensureOpen();
        return this.haveLeftoverChar || this.implReady();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() throws IOException {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.lockedClose();
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.lockedClose();
        }
    }

    private void lockedClose() throws IOException {
        if (this.closed) {
            return;
        }
        try {
            this.implClose();
        }
        finally {
            this.closed = true;
        }
    }

    private boolean isOpen() {
        return !this.closed;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void fillZeroToPosition() throws IOException {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.lockedFillZeroToPosition();
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.lockedFillZeroToPosition();
        }
    }

    private void lockedFillZeroToPosition() {
        Arrays.fill(this.bb.array(), this.bb.arrayOffset(), this.bb.arrayOffset() + this.bb.position(), (byte)0);
    }

    StreamDecoder(InputStream in, Object lock, Charset cs) {
        this(in, lock, cs.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE));
    }

    StreamDecoder(InputStream in, Object lock, CharsetDecoder dec) {
        super(lock);
        this.cs = dec.charset();
        this.decoder = dec;
        this.in = in;
        this.ch = null;
        this.bb = ByteBuffer.allocate(8192);
        this.bb.flip();
    }

    StreamDecoder(ReadableByteChannel ch, CharsetDecoder dec, int mbc) {
        this.in = null;
        this.ch = ch;
        this.decoder = dec;
        this.cs = dec.charset();
        this.bb = ByteBuffer.allocate(mbc < 0 ? 8192 : (mbc < 32 ? 32 : mbc));
        this.bb.flip();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int readBytes() throws IOException {
        this.bb.compact();
        try {
            if (this.ch != null) {
                int n = this.ch.read(this.bb);
                if (n < 0) {
                    int n2 = n;
                    return n2;
                }
            } else {
                int lim = this.bb.limit();
                int pos = this.bb.position();
                assert (pos <= lim);
                int rem = pos <= lim ? lim - pos : 0;
                int n = this.in.read(this.bb.array(), this.bb.arrayOffset() + pos, rem);
                if (n < 0) {
                    int n3 = n;
                    return n3;
                }
                if (n == 0) {
                    throw new IOException("Underlying input stream returned zero bytes");
                }
                assert (n <= rem) : "n = " + n + ", rem = " + rem;
                this.bb.position(pos + n);
            }
        }
        finally {
            this.bb.flip();
        }
        int rem = this.bb.remaining();
        assert (rem != 0) : rem;
        return rem;
    }

    int implRead(char[] cbuf, int off, int end) throws IOException {
        assert (end - off > 1);
        CharBuffer cb = CharBuffer.wrap(cbuf, off, end - off);
        if (cb.position() != 0) {
            cb = cb.slice();
        }
        boolean eof = false;
        while (true) {
            CoderResult cr;
            if ((cr = this.decoder.decode(this.bb, cb, eof)).isUnderflow()) {
                if (eof || !cb.hasRemaining() || cb.position() > 0 && !this.inReady()) break;
                int n = this.readBytes();
                if (n >= 0) continue;
                eof = true;
                if (cb.position() != 0 || this.bb.hasRemaining()) continue;
                break;
            }
            if (cr.isOverflow()) {
                assert (cb.position() > 0);
                break;
            }
            cr.throwException();
        }
        if (eof) {
            this.decoder.reset();
        }
        if (cb.position() == 0) {
            if (eof) {
                return -1;
            }
            assert (false);
        }
        return cb.position();
    }

    String encodingName() {
        return this.cs instanceof HistoricallyNamedCharset ? ((HistoricallyNamedCharset)((Object)this.cs)).historicalName() : this.cs.name();
    }

    private boolean inReady() {
        try {
            return this.in != null && this.in.available() > 0 || this.ch instanceof FileChannel;
        }
        catch (IOException x) {
            return false;
        }
    }

    boolean implReady() {
        return this.bb.hasRemaining() || this.inReady();
    }

    void implClose() throws IOException {
        if (this.ch != null) {
            this.ch.close();
        } else {
            this.in.close();
        }
    }
}

