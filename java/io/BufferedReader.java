/*
 * Decompiled with CFR 0.152.
 */
package java.io;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jdk.internal.misc.InternalLock;

public class BufferedReader
extends Reader {
    private Reader in;
    private char[] cb;
    private int nChars;
    private int nextChar;
    private static final int INVALIDATED = -2;
    private static final int UNMARKED = -1;
    private int markedChar = -1;
    private int readAheadLimit = 0;
    private boolean skipLF = false;
    private boolean markedSkipLF = false;
    private static final int DEFAULT_CHAR_BUFFER_SIZE = 8192;
    private static final int DEFAULT_EXPECTED_LINE_LENGTH = 80;

    public BufferedReader(Reader in, int sz) {
        super(in);
        if (sz <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        this.in = in;
        this.cb = new char[sz];
        this.nChars = 0;
        this.nextChar = 0;
    }

    public BufferedReader(Reader in) {
        this(in, 8192);
    }

    private void ensureOpen() throws IOException {
        if (this.in == null) {
            throw new IOException("Stream closed");
        }
    }

    private void fill() throws IOException {
        int n;
        int dst;
        if (this.markedChar <= -1) {
            dst = 0;
        } else {
            int delta = this.nextChar - this.markedChar;
            if (delta >= this.readAheadLimit) {
                this.markedChar = -2;
                this.readAheadLimit = 0;
                dst = 0;
            } else {
                if (this.readAheadLimit <= this.cb.length) {
                    System.arraycopy(this.cb, this.markedChar, this.cb, 0, delta);
                    this.markedChar = 0;
                    dst = delta;
                } else {
                    char[] ncb = new char[this.readAheadLimit];
                    System.arraycopy(this.cb, this.markedChar, ncb, 0, delta);
                    this.cb = ncb;
                    this.markedChar = 0;
                    dst = delta;
                }
                this.nextChar = this.nChars = delta;
            }
        }
        while ((n = this.in.read(this.cb, dst, this.cb.length - dst)) == 0) {
        }
        if (n > 0) {
            this.nChars = dst + n;
            this.nextChar = dst;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int read() throws IOException {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                int n = this.implRead();
                return n;
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            return this.implRead();
        }
    }

    private int implRead() throws IOException {
        this.ensureOpen();
        while (true) {
            if (this.nextChar >= this.nChars) {
                this.fill();
                if (this.nextChar >= this.nChars) {
                    return -1;
                }
            }
            if (!this.skipLF) break;
            this.skipLF = false;
            if (this.cb[this.nextChar] != '\n') break;
            ++this.nextChar;
        }
        return this.cb[this.nextChar++];
    }

    private int read1(char[] cbuf, int off, int len) throws IOException {
        if (this.nextChar >= this.nChars) {
            if (len >= this.cb.length && this.markedChar <= -1 && !this.skipLF) {
                return this.in.read(cbuf, off, len);
            }
            this.fill();
        }
        if (this.nextChar >= this.nChars) {
            return -1;
        }
        if (this.skipLF) {
            this.skipLF = false;
            if (this.cb[this.nextChar] == '\n') {
                ++this.nextChar;
                if (this.nextChar >= this.nChars) {
                    this.fill();
                }
                if (this.nextChar >= this.nChars) {
                    return -1;
                }
            }
        }
        int n = Math.min(len, this.nChars - this.nextChar);
        System.arraycopy(this.cb, this.nextChar, cbuf, off, n);
        this.nextChar += n;
        return n;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                int n = this.implRead(cbuf, off, len);
                return n;
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            return this.implRead(cbuf, off, len);
        }
    }

    private int implRead(char[] cbuf, int off, int len) throws IOException {
        int n1;
        this.ensureOpen();
        Objects.checkFromIndexSize(off, len, cbuf.length);
        if (len == 0) {
            return 0;
        }
        int n = this.read1(cbuf, off, len);
        if (n <= 0) {
            return n;
        }
        while (n < len && this.in.ready() && (n1 = this.read1(cbuf, off + n, len - n)) > 0) {
            n += n1;
        }
        return n;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    String readLine(boolean ignoreLF, boolean[] term) throws IOException {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                String string = this.implReadLine(ignoreLF, term);
                return string;
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            return this.implReadLine(ignoreLF, term);
        }
    }

    private String implReadLine(boolean ignoreLF, boolean[] term) throws IOException {
        boolean omitLF;
        StringBuilder s = null;
        this.ensureOpen();
        boolean bl = omitLF = ignoreLF || this.skipLF;
        if (term != null) {
            term[0] = false;
        }
        while (true) {
            int i;
            if (this.nextChar >= this.nChars) {
                this.fill();
            }
            if (this.nextChar >= this.nChars) {
                if (s != null && s.length() > 0) {
                    return s.toString();
                }
                return null;
            }
            boolean eol = false;
            char c = '\u0000';
            if (!omitLF || this.cb[this.nextChar] == '\n') {
                // empty if block
            }
            this.skipLF = false;
            omitLF = false;
            for (i = ++this.nextChar; i < this.nChars; ++i) {
                c = this.cb[i];
                if (c != '\n' && c != '\r') continue;
                if (term != null) {
                    term[0] = true;
                }
                eol = true;
                break;
            }
            int startChar = this.nextChar;
            this.nextChar = i;
            if (eol) {
                String str;
                if (s == null) {
                    str = new String(this.cb, startChar, i - startChar);
                } else {
                    s.append(this.cb, startChar, i - startChar);
                    str = s.toString();
                }
                ++this.nextChar;
                if (c == '\r') {
                    this.skipLF = true;
                }
                return str;
            }
            if (s == null) {
                s = new StringBuilder(80);
            }
            s.append(this.cb, startChar, i - startChar);
        }
    }

    public String readLine() throws IOException {
        return this.readLine(false, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long skip(long n) throws IOException {
        if (n < 0L) {
            throw new IllegalArgumentException("skip value is negative");
        }
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                long l = this.implSkip(n);
                return l;
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            return this.implSkip(n);
        }
    }

    private long implSkip(long n) throws IOException {
        long r;
        long d;
        this.ensureOpen();
        for (r = n; r > 0L; r -= d) {
            if (this.nextChar >= this.nChars) {
                this.fill();
            }
            if (this.nextChar >= this.nChars) break;
            if (this.skipLF) {
                this.skipLF = false;
                if (this.cb[this.nextChar] == '\n') {
                    ++this.nextChar;
                }
            }
            if (r <= (d = (long)(this.nChars - this.nextChar))) {
                this.nextChar += (int)r;
                r = 0L;
                break;
            }
            this.nextChar = this.nChars;
        }
        return n - r;
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
                boolean bl = this.implReady();
                return bl;
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            return this.implReady();
        }
    }

    private boolean implReady() throws IOException {
        this.ensureOpen();
        if (this.skipLF) {
            if (this.nextChar >= this.nChars && this.in.ready()) {
                this.fill();
            }
            if (this.nextChar < this.nChars) {
                if (this.cb[this.nextChar] == '\n') {
                    ++this.nextChar;
                }
                this.skipLF = false;
            }
        }
        return this.nextChar < this.nChars || this.in.ready();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void mark(int readAheadLimit) throws IOException {
        if (readAheadLimit < 0) {
            throw new IllegalArgumentException("Read-ahead limit < 0");
        }
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.implMark(readAheadLimit);
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.implMark(readAheadLimit);
        }
    }

    private void implMark(int readAheadLimit) throws IOException {
        this.ensureOpen();
        this.readAheadLimit = readAheadLimit;
        this.markedChar = this.nextChar;
        this.markedSkipLF = this.skipLF;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void reset() throws IOException {
        Object lock = this.lock;
        if (lock instanceof InternalLock) {
            InternalLock locker = (InternalLock)lock;
            locker.lock();
            try {
                this.implReset();
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.implReset();
        }
    }

    private void implReset() throws IOException {
        this.ensureOpen();
        if (this.markedChar < 0) {
            throw new IOException(this.markedChar == -2 ? "Mark invalid" : "Stream not marked");
        }
        this.nextChar = this.markedChar;
        this.skipLF = this.markedSkipLF;
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
                this.implClose();
            }
            finally {
                locker.unlock();
            }
        }
        Object object = lock;
        synchronized (object) {
            this.implClose();
        }
    }

    private void implClose() throws IOException {
        if (this.in == null) {
            return;
        }
        try {
            this.in.close();
        }
        finally {
            this.in = null;
            this.cb = null;
        }
    }

    public Stream<String> lines() {
        Iterator<String> iter = new Iterator<String>(){
            String nextLine = null;

            @Override
            public boolean hasNext() {
                if (this.nextLine != null) {
                    return true;
                }
                try {
                    this.nextLine = BufferedReader.this.readLine();
                    return this.nextLine != null;
                }
                catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public String next() {
                if (this.nextLine != null || this.hasNext()) {
                    String line = this.nextLine;
                    this.nextLine = null;
                    return line;
                }
                throw new NoSuchElementException();
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, 272), false);
    }
}

